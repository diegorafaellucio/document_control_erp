package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.WebProxy;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.*;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.MetadadosArquivoVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.StringZipUtils;
import net.wasys.util.other.VelocityEngineUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;


@Service
public class EmailExchangeService {

	private static final String PROCESSO_SELETIVO_FIES = "PROCESSO SELETIVO FIES";
	public static final String PROCESSO_SELETIVO_PROUNI = "PROCESSO SELETIVO PROUNI";

	@Autowired private ResourceService resourceService;
	@Autowired private EmailRecebidoService emailRecebidoService;
	@Autowired private EmailEnviadoService emailEnviadoService;
	@Autowired private ImagemService imagemService;
	@Autowired private ProcessoService processoService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ParametroService parametroService;
	@Autowired private ColunaProcessoSeletivoService colunaProcessoSeletivoService;

	@Transactional(rollbackFor = Exception.class)
	public void importarEmails() {

		ExchangeService service = new ExchangeService();
		String username = resourceService.getValue(ResourceService.EWS_USERNAME);
		String password = resourceService.getValue(ResourceService.EWS_PASSWORD);
		String urlStr = resourceService.getValue(ResourceService.EWS_URL);
		String mailbox = resourceService.getValue(ResourceService.EWS_USERNAME);
		String proxyHost = resourceService.getValue(ResourceService.PROXY_HOST);
		Integer proxyPort = resourceService.getValue(ResourceService.PROXY_PORT, Integer.class);

		if(!proxyHost.isEmpty() && proxyPort != null ) {
			WebProxy proxy = new WebProxy(proxyHost,proxyPort);
			service.setWebProxy(proxy);
		}

		try {
			URI url = new URI(urlStr);
			ExchangeCredentials credentials = new WebCredentials(username, password);
			service.setCredentials(credentials);
			service.setUrl(url);
			ItemView view = new ItemView(5000);
			Mailbox mb = new Mailbox();
			mb.setAddress(mailbox);
			FolderId folderId = new FolderId(WellKnownFolderName.Inbox, mb);
			FindItemsResults<Item> findResults = service.findItems(folderId, view);
			ArrayList<Item> items = findResults.getItems();

			int count = 1;
			for (Item item : items) {
				boolean deletarEmail = false;
				item.load();
				PropertySet propSet = new PropertySet(BasePropertySet.FirstClassProperties);
				propSet.add(ItemSchema.MimeContent);
				ItemId id = item.getId();
				EmailMessage message = EmailMessage.bind(service, id,propSet);

				String subject = message.getSubject();
				boolean carregarAnexos =  carregarAnexos(subject);
				EmailRecebido er = lerMensagemFromEWS(message, carregarAnexos, mailbox);
				count++;

				String uid = id.getUniqueId();
				boolean exists = existsByMessageUid(uid);
				if(exists) {
					continue;
				}

				EmailEnviado ee = emailEnviadoService.identificarEmail(er);
				if(ee != null) {
					Processo processo = ee.getProcesso();
					if(processo == null) continue;

					if(processo.isSisFiesOrSisProuni()) {
						digitalizarAnexosAosDocumentosDoProcesso(message, processo);
						verificaEnvioParaAnalise(processo);
						deletarEmail = true;
					}
					else {
						salvarAnexo(mailbox, message, ee, processo);
					}
				}

				//Move o email para a lixeira
				if(deletarEmail) {
					item.delete(DeleteMode.MoveToDeletedItems);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private boolean existsByMessageUid(String uid) {

		boolean exists = emailRecebidoService.existsByMessageUid(uid);
		if(exists) {
			EmailRecebido er = emailRecebidoService.getByMessageUid(uid);
			Processo processo = er.getProcesso();
			exists = !processo.isSisFiesOrSisProuni();
		}

		return exists;
	}

	private void verificaEnvioParaAnalise(Processo processo) throws Exception {

		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		StatusProcesso status = processo.getStatus();
		Long processoId = processo.getId();
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		int max = TipoProcesso.SIS_PROUNI.equals(tipoProcessoId) ? GetdocConstants.MINIMO_DOCUMENTACAO_ENVIO_ANALISE_PROUNI : GetdocConstants.MINIMO_DOCUMENTACAO_ENVIO_ANALISE_FIES;

		Integer count = imagemService.countByProcessoId(processoId);
		if(count >= max && (StatusProcesso.RASCUNHO.equals(status) || Situacao.SITUACAO_CONCLUIDO_FIES_PROUNI_ID.contains(situacaoId))) {
			processoService.enviarParaAnalise(processo, null);
		}
	}

	private void salvarAnexo(String mailbox, EmailMessage message, EmailEnviado ee, Processo processo) throws Exception {
		EmailRecebido er;
		er = lerMensagemFromEWS(message, true, mailbox);
		er.setEmailEnviado(ee);
		er.setProcesso(processo);
		emailRecebidoService.save(er);
	}

	private void digitalizarAnexosAosDocumentosDoProcesso(EmailMessage message, Processo processo) throws Exception {

		boolean hasAttachments = message.getHasAttachments();
		if(!hasAttachments) {
			return;
		}

		StringBuilder arquivosNaoSuportado = new StringBuilder();
		List<FileVO> documentos = new ArrayList<>();
		AttachmentCollection attachments = message.getAttachments();
		for(Attachment att : attachments) {

			String fileName = getFileName(att);
			if(StringUtils.isBlank(fileName)) {
				continue;
			}

			if(StringUtils.contains(fileName, "logo")) {
				continue;
			}

			String extensao = DummyUtils.getExtensao(fileName);
			if(extensao != null && (extensao.equals(fileName) || extensao.length() > 5)) {
				extensao = "";
			}

			String contentType = att.getContentType();
			if(contentType == null || (contentType.startsWith("message/rfc822") && StringUtils.isBlank(extensao))) {
				extensao = "eml";
				fileName += "." + extensao;
			}

			boolean extensaoPermitida = isExtensaoPermitida(arquivosNaoSuportado, fileName, processo);
			if(!extensaoPermitida) {
				continue;
			}

			File file = File.createTempFile("exchange_", "." + extensao);
			DummyUtils.deleteOnExitFile(file);

			MetadadosArquivoVO metadadosArquivoVO = new MetadadosArquivoVO();
			metadadosArquivoVO.setOrigem(Origem.EMAIL);

			FileVO vo = new FileVO();
			vo.setName(fileName);
			vo.setFile(file);
			vo.setMetadadosArquivoVO(metadadosArquivoVO);

			documentos.add(vo);

			saveEmailAttachment(att, file);
		}

		if(!documentos.isEmpty()) {
			Documento documento = documentoService.makeOutros(processo);

			ImagemTransaction transaction = new ImagemTransaction();
			documentoService.digitalizarImagens(transaction, null, documento, documentos, AcaoDocumento.DIGITALIZOU, Origem.EMAIL);
			transaction.commit();

			String log = arquivosNaoSuportado.length() > 0 ? "Arquivos invalidos: <br>" + arquivosNaoSuportado.toString() : "";
			processoLogService.criaLog(processo,null, AcaoProcesso.DIGITALIZOU_DOCUMENTOS_POR_EMAIL, log);
		}
	}

	private boolean isExtensaoPermitida(StringBuilder arquivosNaoSuportado, String fileName, Processo processo) {

		Long processoId = processo.getId();
		try {
			String extensao = DummyUtils.getExtensao(fileName);
			parametroService.validarArquivoPermitido(fileName);
		}
		catch (Exception e) {
			e.printStackTrace();
			arquivosNaoSuportado.append(fileName).append("<br>");
			return false;
		}

		return true;
	}

	private EmailRecebido lerMensagemFromEWS(EmailMessage message, boolean carregarAnexos, String mailbox) throws Exception {

		EmailRecebido er = new EmailRecebido();
		String subject = message.getSubject();
		subject = subject != null ? subject : "";
		if (subject.length() > 300) {
			subject = subject.substring(0, 295) + "[...]";
		}
		subject = removeCaractereEstranho(subject);

		Date sentDate = message.getDateTimeSent();

		Date receivedDate = message.getDateTimeReceived();

		String replyToStr = "";

		EmailAddressCollection toRecipients = message.getToRecipients();
		List<EmailAddress> items = toRecipients.getItems();
		for(EmailAddress item : items) {
			String address = item.getAddress().trim();

			if(address.equals(mailbox)) continue;

			replyToStr += StringUtils.isNotBlank(replyToStr) ? ";" + address : address;
		}

		String fromStr = message.getFrom().getAddress() != null ? message.getFrom().getAddress() : null;
		if (message.getFrom().getAddress() != null && fromStr.length() > 500) {
			fromStr = fromStr.substring(0, 495) + "[...]";
		}

		er.setSubject(subject);
		er.setSentDate(sentDate);
		er.setReceivedDate(receivedDate);
		er.setEmailFrom(fromStr);
		er.setReplyTo(replyToStr);

		//FolderId folder = item.getParentFolderId();
		String uid = message.getId().getUniqueId();
		String cid = message.getConversationId().getUniqueId();
		er.setMessageUid(uid);
		er.setConversationId(cid);

		Object msgObj = "[???]";

		msgObj = message;

		StringBuilder conteudoHtml = new StringBuilder();
		Set<EmailRecebidoAnexo> anexos = er.getAnexos();

		readContent(msgObj, conteudoHtml, anexos, carregarAnexos);

		for (EmailRecebidoAnexo anexo : anexos) {
			anexo.setEmailRecebido(er);
		}

		String conteudoHtmlStr = conteudoHtml.toString();
		conteudoHtmlStr = StringUtils.trim(conteudoHtmlStr);
		if(StringUtils.isNotBlank(conteudoHtmlStr)) {
			byte[] compress = StringZipUtils.compress(conteudoHtmlStr);
			er.setConteudoHtml(compress);
		}

		return er;
	}

	private String removeCaractereEstranho(String text) {
		text = text.replace((char) 8201, ' ');
		return text;
	}

	private void readContent(Object msgObj, StringBuilder conteudoHtml, Collection<EmailRecebidoAnexo> anexos, boolean carregarAnexos) throws Exception {

		if(msgObj instanceof EmailMessage){
			EmailMessage message = (EmailMessage) msgObj;
			MessageBody body = ((EmailMessage) msgObj).getBody();
			String str = body.toString();
			str = removeCaractereEstranho(str);
			conteudoHtml.append(str);
			if(carregarAnexos){
				carregaAnexosEWS(message, anexos);
			}
		}
		else {

			Class<?> class1 = msgObj.getClass();
			String simpleName = class1.getSimpleName();
			conteudoHtml.append("---------- Mensagem não identificada ----------");
			conteudoHtml.append("<br><i>[" + simpleName + "]</i><br><br> " + msgObj);
		}
	}

	private void carregaAnexosEWS(EmailMessage message, Collection<EmailRecebidoAnexo> anexos) throws Exception {

		if (message.getHasAttachments()) {

			for (Attachment att : message.getAttachments()) {

				String fileName = getFileName(att);

				if (StringUtils.isBlank(fileName)) {
					continue;
				}
				String extensao = DummyUtils.getExtensao(fileName);
				if (extensao != null && (extensao.equals(fileName) || extensao.length() > 5)) {
					extensao = "";
				}
				String contentType = att.getContentType();
				if (contentType == null || (contentType.startsWith("message/rfc822") && StringUtils.isBlank(extensao))) {
					extensao = "eml";
					fileName += "." + extensao;
				}

				File file = File.createTempFile("exchange_", "." + extensao);
				DummyUtils.deleteOnExitFile(file);

				EmailRecebidoAnexo anexo = new EmailRecebidoAnexo();
				anexo.setExtensao(extensao!=null?extensao:"");
				anexo.setFileName(fileName);
				saveEmailAttachment(att, file);
				String absolutePath = file.getAbsolutePath();
				anexo.setPath(absolutePath);
				anexo.setAttachment(true);
				if (anexo != null) {
					anexos.add(anexo);
				}

				if(!fileName.contains("pré-selecionados") && extensao.equals("xlsx")) {
					colunaProcessoSeletivoService.atualizarDados(file, fileName);
				}
			}
		}
	}

	private String getFileName(Attachment att) throws MessagingException {

		String fileName = att.getName();

		if(StringUtils.isBlank(fileName)) {
			return null;
		}

		try {
			fileName = MimeUtility.decodeText(fileName);
		}
		catch (UnsupportedEncodingException e) {}

		fileName = fileName.replace("\\", "_");
		fileName = fileName.replace("/", "_");
		fileName = fileName.replace(":", "_");
		fileName = fileName.replace("*", "_");
		fileName = fileName.replace("?", "_");
		fileName = fileName.replace("\"", "_");
		fileName = fileName.replace("<", "_");
		fileName = fileName.replace(">", "_");
		fileName = fileName.replace("|", "_");

		return fileName;
	}


	public void saveEmailAttachment(Attachment attachment, File file) throws Exception {

		String absolutePath = file.getAbsolutePath();
		if (attachment instanceof FileAttachment) {
			FileAttachment fileAttachment = (FileAttachment) attachment;
			DummyUtils.sleep(2500);
			fileAttachment.load(absolutePath);
		}
		else if(attachment instanceof ItemAttachment){
			ItemAttachment itemAttachment = (ItemAttachment) attachment;
			itemAttachment.load(ItemSchema.MimeContent);
			MimeContent mc = itemAttachment.getItem().getMimeContent();
			try(FileOutputStream fs = new FileOutputStream(absolutePath)){
				fs.write(mc.getContent());
			}
		}
	}

/*	public void enviarEmail(EmailEnviado ee, ProcessoLog log, List<FileVO> arquivos, String body) {

		String assunto = "";
		Processo processo = log.getProcesso();
		if(body == null) {
			Map<String, Object> model = new HashMap<String, Object>();
			assunto = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.ASSUNTO);
			String canal = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CANAL);
			String observacao = log.getObservacao();
			observacao += DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CONTEUDO);
			if (StringUtils.isNotBlank(observacao) && observacao.length() >= 5){
				String finalObservacao = observacao.substring(observacao.length() - 5);
				if (finalObservacao.equals("[...]")){
					Long processoId = processo.getId();
					EmailRecebido historicoEmail = emailRecebidoService.getLastByProcessoId(processoId);
					observacao = historicoEmail != null ? setConteudo(historicoEmail, log.getObservacao()) : log.getObservacao();
				}
			} else {
				observacao += "";
			}
			String nome = log.getUsuario()!=null?log.getUsuario().getNome(): "";
			observacao = DummyUtils.stringToHTML(observacao);
			model.put("conteudo", observacao);
			model.put("processo", processo);
			model.put("canal", canal != null ? canal : "");
			model.put("nome", nome);
			model.put("dummyUtils", new DummyUtils());
			model.put("logoImagemUrl", "cid:logo.png");
			body = getBody("envio-email-cliente.htm", model);
		}

		Map<String, File> attachments = new HashMap<>();
		File logoFile = getFile("logo_helbor_email.png");
		attachments.put("logo.png", logoFile);
		if (arquivos != null) {
			for (FileVO arquivo : arquivos) {
				attachments.put(arquivo.getName(), arquivo.getFile());
			}
		}

		Map<String, String> cc = new LinkedHashMap<>();
		List<String> destinatariosList = ee.getDestinatariosList();
		for(String email : destinatariosList) {
			cc.put(email, email);
		}

		boolean encaminhado = ee  != null ? ee.getEncaminhado() : false ;

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		vo.setSubject(assunto);
		vo.setAttachments(attachments);
		vo.setCc(cc);
		vo.setForward(encaminhado);

		enviarEWS(vo, processo);
	}*/

	/*protected void enviarEWS(EmailSmtpVO vo, Processo processo){

		ExchangeService service = new ExchangeService();
		service.setTraceEnabled(true);
		EmailAddress from = new EmailAddress();
		String username = resourceService.getValue(ResourceService.EWS_USERNAME);
		String password = resourceService.getValue(ResourceService.EWS_PASSWORD);
		String discoverUrl = resourceService.getValue(ResourceService.EWS_DISCOVERURL);

		ExchangeCredentials credentials = null;

		String bodyContent = vo.getBody();
		Map<String, File> attachments = vo.getAttachments();

		credentials = new WebCredentials(username,password);
		service.setCredentials(credentials);

		String canalEmail = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CANAL);
        List<CanalAtendimento> canais = canalAtendimentoService.findAtivos();
        for (CanalAtendimento ca : canais) {
            String canal = ca.getCanal();
            String conta = ca.getConta();
            if(canalEmail.equals(canal)){
                from.setName(canal);
                from.setAddress(conta);
            }
        }

		try {
			service.autodiscoverUrl(discoverUrl);

			String itemIdCodigo = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CODIGO);
			ItemId itemId = new ItemId(itemIdCodigo);

            boolean replyToAll = false;
			EmailMessage msg = EmailMessage.bind(service, itemId);
			ResponseMessage responseMessage = msg.createReply(replyToAll);

			Mailbox sharedMailbox = new Mailbox(from.getAddress());
			WellKnownFolderName sentItems = WellKnownFolderName.SentItems;
			FolderId sharedMailboxSendItems = new FolderId(sentItems, sharedMailbox);

			EmailMessage reply = responseMessage.save();
            for(Map.Entry<String, File> entry : attachments.entrySet()) {
                File value = entry.getValue();
                reply.getAttachments().addFileAttachment(value.getAbsolutePath());
            }

			reply.getBccRecipients().clear();
			Map<String, String> cc = vo.getCc();
			Set<String> strings = cc.keySet();
			for(String string : strings) {
				EmailAddress emailAddress = new EmailAddress();
				emailAddress.setAddress(string);
				reply.getBccRecipients().add(emailAddress);
			}

			reply.setFrom(from != null ? from : null);
			reply.setBody(MessageBody.getMessageBodyFromText(bodyContent));
			reply.update(ConflictResolutionMode.AutoResolve);

			Boolean forward = vo.getForward();

			if (forward) {
				reply.createForward();
			}

			reply.sendAndSaveCopy(sharedMailboxSendItems);

			*//*if(!forward) {
				reply.sendAndSaveCopy(sharedMailboxSendItems);
			}
			else {
				EmailAddressCollection bccRecipients = reply.getBccRecipients();
				List<EmailAddress> items = bccRecipients.getItems();
				reply.forward(MessageBody.getMessageBodyFromText(bodyContent), items);
			}*//*

            DummyUtils.systraceThread("E-mail enviado...");

		} catch (Exception e) {
			e.printStackTrace();//("Exception occurred while sending EWS Mail ", e);
		}
	}*/

	private String getBody(String templateFile, Map<String, Object> model) {

		String templatePath = "/net/wasys/getdoc/email/" + templateFile;

		StringWriter writer = new StringWriter();
		VelocityEngineUtils.merge(templatePath, writer, model);
		String content = String.valueOf(writer);
		return content;
	}

	private File getFile(String imagem) {
		File tempFile = DummyUtils.getFileFromResource("/net/wasys/getdoc/email/" + imagem);
		return tempFile;
	}

	private String setConteudo(EmailRecebido emailRecebido, String corpoNovo){

		StringBuilder mensagem = new StringBuilder();

		mensagem.append(corpoNovo);

		String historico = emailRecebido.getConteudoLong();
		if(StringUtils.isNotBlank(historico)) {
			mensagem.append("<br/><br/>");
			mensagem.append("-------------- Mensagem Original --------------");
			mensagem.append("<br/>");
			/*mensagem.append("<br/>De: ");
			mensagem.append(StringUtils.isNotBlank(emailRecebido.getEmailFrom()) ? emailRecebido.getEmailFrom() : "Veio do inferno");
            Date sentDate = emailRecebido.getSentDate();
            String sentDateStr = DummyUtils.formatDateTime(sentDate);
            mensagem.append("<br/>Enviada em: ");
            mensagem.append(sentDateStr);
			mensagem.append("<br/>Para: ");
			mensagem.append(canal);
			mensagem.append("<br/>Assunto: ");
			mensagem.append(emailRecebido.getSubject());
			mensagem.append("<br/><br/>");*/
			mensagem.append(DummyUtils.htmlToString(historico));
		}
		return mensagem.toString();
	}

	private boolean carregarAnexos(String subject) {
		if (subject == null) {
			return false;
		}

		if(subject.toUpperCase().equals(PROCESSO_SELETIVO_FIES) || subject.toUpperCase().equals(PROCESSO_SELETIVO_PROUNI)) {
			return true;
		}

		return false;
	}
}