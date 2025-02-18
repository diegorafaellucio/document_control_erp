package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.email.EmailSender;
import net.wasys.util.email.FileAttachment;
import net.wasys.util.other.StringZipUtils;
import net.wasys.util.other.VelocityEngineUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.Session;
import java.io.File;
import java.io.StringWriter;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class EmailSmtpService {

	private static final Map<String, String> toAlerta = new LinkedHashMap<String, String>();
	static {
		String mode = DummyUtils.getMode();
		if("dev".equals(mode)) {
			//ninguém
		}
		else if("homolog".equals(mode)) {
			toAlerta.put("rodrigo.goncalves@neobpo.com.br", "Rodrigo Gonçalves");
		}
		else {
			toAlerta.put("raul.noguchi@neobpo.com.br", "Raul Noguchi");
			toAlerta.put("alexandre.fonseca@neobpo.com.br", "Alexandre Fonseca");
			toAlerta.put("rodrigo.goncalves@neobpo.com.br", "Rodrigo Gonçalves");
		}
	}

	@Autowired private ResourceService resourceService;
	@Autowired private EmailEnviadoService emailEnviadoService;
	@Autowired private UsuarioSubperfilService usuarioSubperfilService;
	@Autowired private ParametroService parametroService;

	private File getFile(String imagem) {
		File tempFile = DummyUtils.getFileFromResource("/net/wasys/getdoc/email/" + imagem);
		return tempFile;
	}

	public String getBody(String tamplateFile, Map<String, Object> model, Map<String, File> attachments) {

		String tamplatePath = "/net/wasys/getdoc/email/" + tamplateFile;

		model.put("logoImagemUrl", "cid:logo.png");

		StringWriter writer = new StringWriter();
		VelocityEngineUtils.merge(tamplatePath, writer, model);
		String content = String.valueOf(writer);
		return content;
	}

	protected void enviar(EmailSmtpVO vo) {

		String subject = vo.getSubject();
		String body = vo.getBody();
		Map<String, String> to = vo.getTo();
		Map<String, String> cc = vo.getCc();
		Map<String, String> cco = vo.getCco();
		Map<String, File> attachments = vo.getAttachments();
		attachments = attachments != null ? attachments : new HashMap<>();
		String nomeSistema = vo.getNomeSistema();
		File logoFile = getFile("logo-getdoc-200px.png");
		attachments.put("logo.png", logoFile);

		String mode = DummyUtils.getMode();
		String sufix = StringUtils.isBlank(mode) ? "": " *" + mode + "*";
		subject = subject + sufix;

		nomeSistema = StringUtils.isNotBlank(nomeSistema) ? nomeSistema : resourceService.getValue("nome.sistema.email");
		String emailSistema = resourceService.getValue("email.sistema");
		String smtpHost = resourceService.getValue("smtp.host");
		String smtpPort = resourceService.getValue("smtp.port");
		String smtpLogin = resourceService.getValue("smtp.username");
		String smtpSenha = resourceService.getValue("smtp.senha");
		String proxyHost = resourceService.getValue("smtp.proxy.host");
		String proxyPort = resourceService.getValue("smtp.proxy.port");
		String isSsl = resourceService.getValue("smtp.isSsl");
		String startTls = resourceService.getValue("smtp.starttls.enable");

		EmailSender sender = new EmailSender(smtpHost, smtpLogin, smtpSenha);
		Session mailSession = sender.getMailSession();
		Properties props = mailSession.getProperties();

		if("true".equals(isSsl)) {
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
		}

		if(StringUtils.isNotBlank(smtpPort)) {
			props.put("mail.smtp.port", smtpPort);
		}

		if("true".equals(startTls)) {
			props.put("mail.smtp.starttls.enable", "true");
		}

		if(StringUtils.isNotBlank(proxyHost)) {
			proxyPort = StringUtils.isNotBlank(proxyPort) ? proxyPort : "3128";
			props.setProperty("socksProxyHost", proxyHost);
			props.setProperty("socksProxyPort", proxyPort);
		}

		sender.setFrom(emailSistema, nomeSistema);
		sender.setSubject(subject);
		Boolean convite = vo.getConvite();
		if(convite) {
			sender.setContent(body, "text/calendar; method=REQUEST; charset=ISO-8859-1");
		} else {
			sender.setContent(body, "text/html; charset=UTF-8");
		}

		if(to != null) {
			for (String email2 : to.keySet()) {
				String nome = to.get(email2);
				sender.addTo(email2, nome);
			}
		}

		if(cc != null) {
			for (String email2 : cc.keySet()) {
				String nome = cc.get(email2);
				sender.addCC(email2, nome);
			}
		}

		if(cco != null) {
			for (String email2 : cco.keySet()) {
				String nome = cco.get(email2);
				sender.addBCC(email2, nome);
			}
		}

		if(attachments != null) {
			for (String fileName : attachments.keySet()) {
				File file = attachments.get(fileName);
				FileAttachment attachment = new FileAttachment();
				attachment.setFile(file);
				attachment.setFileName(fileName);
				sender.addAttachment(attachment);
			}
		}

		systraceThread("enviando... from: " + emailSistema + " to: " + to + " " + subject + " > smtpHost: " + smtpHost + " smtpPort: " + smtpPort + " smtpLogin: " + smtpLogin + " smtpSenha: " + smtpSenha + " isSsl: " + isSsl);
		if(to == null || to.isEmpty()) {
			systraceThread("nenhum destinatário...");
		} else {
			sender.send();
		}
		systraceThread("enviou");
	}

	public void enviarRedefinicaoSenha(Usuario usuario, String link) {

		Map<String, Object> model = new HashMap<>();
		model.put("link", link);

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("redefinicao-senha.htm", model, attachments);

		Map<String, String> to = new HashMap<String, String>();

		String email = usuario.getEmail();
		String nome = usuario.getNome();
		to.put(email, nome);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		vo.setSubject("Solicitação de nova senha");
		vo.setTo(to);
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	public void enviarEmail(EmailEnviado ee, ProcessoLog log, List<FileVO> arquivos, String body) {

		if(body == null) {
			Map<String, Object> model = new HashMap<String, Object>();
			String observacao = log.getObservacao();
			observacao = DummyUtils.stringToHTML(observacao);
			model.put("conteudo", observacao);
			Processo processo = log.getProcesso();
			model.put("processo", processo);
			model.put("dummyUtils", new DummyUtils());
			Map<String, File> attachments = new HashMap<>();
			body = getBody("envio-email-cliente.htm", model, attachments);
		}

		Map<String, File> attachments = new HashMap<>();
		if (arquivos != null) {
			for (FileVO arquivo : arquivos) {
				attachments.put(arquivo.getName(), arquivo.getFile());
			}
		}

		List<String> emails = emailEnviadoService.getEmailsDestinatarios(ee);
		Map<String, String> to = new LinkedHashMap<String, String>();
		for (String email : emails) {
			to.put(email, null);
		}

		String assunto = ee.getAssunto();
		String codigo = ee.getCodigo();
		EmailSmtpVO vo = new EmailSmtpVO();
		if(assunto.contains(EmailEnviadoService.CONVITE_EMAIL)){
			vo.setConvite(true);
		}
		vo.setBody(body);
		vo.setSubject(codigo + " " + assunto);
		vo.setTo(to);
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
			throw e;
		}
	}

	public void enviarNotificacaoAtrasoAnalista(Usuario analista, List<ProcessoVO> list) {

		Map<String, String> to = new LinkedHashMap<String, String>();
		String email = analista.getEmail();
		to.put(email, null);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("processos", list);
		model.put("dummyUtils", new DummyUtils());

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("atrasos-analista.htm", model, attachments);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setSubject("Requisições Atrasadas - GetDoc");
		vo.setBody(body);
		vo.setTo(to);
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	public void enviarNotificacaoAlteracaoDadosAluno(Aluno alunoOld, Aluno alunoNew, Processo processo) throws Exception {

		String emailsStr = parametroService.getValor(ParametroService.P.EMAILS_NOTIFICACAO);
		if(StringUtils.isBlank(emailsStr)) {
			return;
		}

		String[] emails = emailsStr.split(",");

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("alunoOld", alunoOld);
		model.put("alunoNew", alunoNew);
		model.put("processo", processo);

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("//envia-notificacao-alteracao-aluno.htm", model, attachments);
		String assunto = "Notificação de Alteração Dados do Aluno - GetDoc";

		EmailVO ee = new EmailVO();
		ee.setDestinatariosList(Arrays.asList(emails));
		ee.setAssunto(assunto);
		ee.setBody(body);

		emailEnviadoService.enviarEmail(ee, processo, null);
	}

	public void enviarNotificacaoCandidatoSisFies(List<Documento> documentos, String email, String curso, ProcessoLog log) throws Exception {

		Processo processo = log.getProcesso();

		List<Documento> documentosObrigatorios = new ArrayList<>();
		List<Documento> documentosCasosEspeciais = new ArrayList<>();
		List<Documento> documentosComposicaoFamiliar = new ArrayList<>();
		for(Documento documento : documentos) {
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if(tipoDocumento == null) continue;
			Long tipoDocumentoId = tipoDocumento.getId();
			boolean obrigatorio = documento.getObrigatorio();
			if(obrigatorio) {
				documentosObrigatorios.add(documento);
			}
			else if(TipoDocumento.DOCUMENTOS_ESPECIAIS_FIES_NOFICACAO.contains(tipoDocumentoId)) {
				documentosCasosEspeciais.add(documento);
			}

			if(TipoDocumento.DOCUMENTOS_COMP_FAMILIAR_FIES_NOFICACAO.contains(tipoDocumentoId)) {
				documentosComposicaoFamiliar.add(documento);
			}
		}

		String localOferta = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.LOCAL_DE_OFERTA);

		Map<String, String> to = new LinkedHashMap<String, String>();
		to.put(email, null);

		String nomeAluno = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NOME_DO_CANDIDATO);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("nomeAluno", nomeAluno);
		model.put("curso", curso);
		model.put("documentosObrigatorios", documentosObrigatorios);
		model.put("documentosCasosEspeciais", documentosCasosEspeciais);
		model.put("documentosComposicaoFamiliar", documentosComposicaoFamiliar);
		model.put("localOferta", localOferta);

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("envia-notificacao-candidato-sisfies.htm", model, attachments);
		String assunto = "Notificação de Requerimento de Documentos - GetDoc";

		EmailVO ee = new EmailVO();
		ee.setAssunto(assunto);
		ee.setDestinatariosList(Arrays.asList(email));
		ee.setBody(body);
		ee.setLogCriacao(log);

		emailEnviadoService.enviarEmail(ee, processo, null);
	}

	public void enviarNotificacaoCandidatoSisProuni(List<Documento> documentos, String email, String curso, ProcessoLog log) throws Exception {

		Processo processo = log.getProcesso();

		List<Documento> documentosObrigatorios = new ArrayList<>();
		List<Documento> documentosEspeciais = new ArrayList<>();
		List<Documento> documentosComposicaoFamiliar = new ArrayList<>();
		for(Documento documento : documentos) {
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if(tipoDocumento == null) continue;

			Long tipoDocumentoId = tipoDocumento.getId();
			boolean obrigatorio = documento.getObrigatorio();

			if(Arrays.asList(TipoDocumento.BOLETIM_DESEMPENHO_ENEM_ID, TipoDocumento.FICHA_DE_INSCRICAO_ID).contains(tipoDocumentoId))  {
				documentosObrigatorios.add(documento);
			}

			if(obrigatorio && !TipoDocumento.DOCUMENTO_TCB_TR_PROUNI_NOFICACAO.contains(tipoDocumentoId)) {
				documentosObrigatorios.add(documento);
			}

			if(TipoDocumento.DOCUMENTOS_ESPECIAIS_PROUNI_NOFICACAO.contains(tipoDocumentoId)){
				documentosEspeciais.add(documento);
			}

			if(TipoDocumento.DOCUMENTOS_COMP_FAMILIAR_PROUNI_NOFICACAO.contains(tipoDocumentoId)) {
				documentosComposicaoFamiliar.add(documento);
			}
		}

		String localOferta = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.LOCAL_DE_OFERTA);

		Map<String, String> to = new LinkedHashMap<String, String>();
		to.put(email, null);

		String nomeAluno = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NOME_DO_CANDIDATO);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("nomeAluno", nomeAluno);
		model.put("curso", curso);
		model.put("documentosObrigatorios", documentosObrigatorios);
		model.put("documentosEspeciais", documentosEspeciais);
		model.put("documentosComposicaoFamiliar", documentosComposicaoFamiliar);
		model.put("localOferta", localOferta);

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("envia-notificacao-candidato-sisprouni.htm", model, attachments);

		String assunto = "Notificação de Requerimento de Documentos - GetDoc";

		EmailVO ee = new EmailVO();
		ee.setAssunto(assunto);
		ee.setDestinatariosList(Arrays.asList(email));
		ee.setBody(body);
		ee.setLogCriacao(log);

		emailEnviadoService.enviarEmail(ee, processo, null);
	}

	public void enviarPendenciaCandidatoSisProuni(List<Documento> documentos, String email, String curso, ProcessoLog log) throws Exception {

		Processo processo = log.getProcesso();

		List<Documento> documentosObrigatorios = new ArrayList<>();

		List<StatusDocumento> statusPendentes = Arrays.asList(StatusDocumento.PENDENTE, StatusDocumento.INCLUIDO);

		for(Documento documento : documentos) {
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if(tipoDocumento == null) continue;

			StatusDocumento documentoStatus = documento.getStatus();
			if(!statusPendentes.contains(documentoStatus)) continue;

			Long tipoDocumentoId = tipoDocumento.getId();
			boolean obrigatorio = documento.getObrigatorio();

			if(Arrays.asList(TipoDocumento.BOLETIM_DESEMPENHO_ENEM_ID, TipoDocumento.FICHA_DE_INSCRICAO_ID).contains(tipoDocumentoId))  {
				documentosObrigatorios.add(documento);
			}

			if(obrigatorio && !TipoDocumento.DOCUMENTO_TCB_TR_PROUNI_NOFICACAO.contains(tipoDocumentoId)) {
				documentosObrigatorios.add(documento);
			}
		}

		String localOferta = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.LOCAL_DE_OFERTA);

		Map<String, String> to = new LinkedHashMap<String, String>();
		to.put(email, null);

		String nomeAluno = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NOME_DO_CANDIDATO);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("nomeAluno", nomeAluno);
		model.put("curso", curso);
		model.put("documentosObrigatorios", documentosObrigatorios);
		model.put("localOferta", localOferta);

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("envia-pendencia-candidato-sisprouni.htm", model, attachments);

		String assunto = "Notificação de Requerimento de Documentos - GetDoc";

		EmailVO ee = new EmailVO();
		ee.setAssunto(assunto);
		ee.setDestinatariosList(Arrays.asList(email));
		ee.setBody(body);
		ee.setLogCriacao(log);

		emailEnviadoService.enviarEmail(ee, processo, null);
	}

	public void enviarNotificacaoAtrasoGestores(List<Usuario> gestores, List<ProcessoVO> list) {

		Map<String, String> to = new LinkedHashMap<String, String>();
		for (Usuario gestor : gestores) {
			String email = gestor.getEmail();
			to.put(email, null);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("processos", list);
		model.put("dummyUtils", new DummyUtils());

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("atrasos-gestores.htm", model, attachments);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		vo.setSubject("Requisições Atrasadas - GetDoc");
		vo.setTo(to);
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	public void enviarNotificacaoAtrasoSolicitacoesArea(List<Usuario> gestoresArea, List<SolicitacaoVO> list) {

		Map<String, String> to = new LinkedHashMap<String, String>();
		for (Usuario gestorArea : gestoresArea) {
			String email = gestorArea.getEmail();
			to.put(email, null);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("solicitacoes", list);
		model.put("dummyUtils", new DummyUtils());

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("atrasos-solicitacoes-area.htm", model, attachments);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		vo.setTo(to);
		vo.setSubject("Solicitações Atrasadas - GetDoc");
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	public void enviarNotificacaoAtrasoSolicitacoes(List<Usuario> gestores, Map<Area, List<SolicitacaoVO>> map, Map<Area, List<Usuario>> mapGestoresArea) {

		Map<String, String> to = new LinkedHashMap<>();
		for (Usuario gestor : gestores) {
			String email = gestor.getEmail();
			to.put(email, null);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("map", map);
		model.put("mapGestoresArea", mapGestoresArea);
		int total = 0;
		for (Area area : map.keySet()) {
			List<SolicitacaoVO> list = map.get(area);
			total += list.size();
		}
		model.put("total", total);
		model.put("dummyUtils", new DummyUtils());

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("atrasos-solicitacoes.htm", model, attachments);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		String subject = "Solicitações Atrasadas - GetDoc";
		vo.setSubject(subject);
		vo.setTo(to);
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	public void enviarNotificaoRelatorioGeradoSucesso(String nomeRelatorio, boolean isDisponivelAnexo, String urlRelatorio, String[] destinatarios, Map<String, File> anexos) {

		Map<String, Object> model = new HashMap<>();
		String urlSistema = resourceService.getValue(ResourceService.URL_SISTEMA_EXTRANET);
		model.put("urlSistema", urlSistema);
		model.put("nomeRelatorio", nomeRelatorio);
		model.put("isDisponivelAnexo", isDisponivelAnexo);
		model.put("urlRelatorio", urlRelatorio);

		Map<String, String> to = new LinkedHashMap<>();
		for (String destinatario : destinatarios) {
			to.put(destinatario, null);
		}

		String body = getBody("envia-notificacao-relatorio-gerado-sucesso.htm", model, anexos);

		EmailSmtpVO email = new EmailSmtpVO();
		email.setSubject("Notificação de Geração de Relatório");
		email.setTo(to);
		email.setAttachments(anexos);
		email.setBody(body);

		enviar(email);
	}

	public void enviarNotificaoRelatorioGeradoErro(String nomeRelatorio, String urlRelatorio, String[] destinatarios) {

		Map<String, Object> model = new HashMap<>();
		String urlSistema = resourceService.getValue(ResourceService.URL_SISTEMA_EXTRANET);
		model.put("urlSistema", urlSistema);
		model.put("nomeRelatorio", nomeRelatorio);
		model.put("urlRelatorio", urlRelatorio);

		Map<String, String> to = new LinkedHashMap<>();
		for (String destinatario : destinatarios) {
			to.put(destinatario, null);
		}

		String body = getBody("envia-notificacao-relatorio-gerado-erro.htm", model, null);

		EmailSmtpVO email = new EmailSmtpVO();
		email.setSubject("Notificação de Geração de Relatório");
		email.setTo(to);
		email.setBody(body);

		enviar(email);
	}

	public void enviarNovaSolicitacao(ProcessoLog logSolicitacao) {

		Solicitacao solicitacao = logSolicitacao.getSolicitacao();
		Subarea subarea = solicitacao.getSubarea();
		List<String> emailsList = subarea.getEmailsList();
		Map<String, String> to = new LinkedHashMap<String, String>();
		for (String email : emailsList) {
			to.put(email, null);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("log", logSolicitacao);
		model.put("dummyUtils", new DummyUtils());

		String urlSistema = resourceService.getValue(ResourceService.URL_SISTEMA_EXTRANET);

		Processo processo = logSolicitacao.getProcesso();
		Long processoId = processo.getId();
		model.put("link", urlSistema + "/requisicoes/fila/edit/" + processoId);

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("nova-solicitacao.htm", model, attachments);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		String subject = "Nova Solicitações - GetDoc";
		vo.setSubject(subject);
		vo.setTo(to);
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	public void enviarEmailException(String msg, Exception e) {

		String stackTrace = ExceptionUtils.getStackTrace(e);
		enviarEmailException(msg, e, stackTrace);
	}

	public void enviarEmailException(String msg, Exception e, String stackTrace) {

		String message = ExceptionUtils.getRootCauseMessage(e);
		message = StringUtils.isNotBlank(message) ? message : e.getMessage();

		if(stackTrace == null) {
			stackTrace = ExceptionUtils.getStackTrace(e);
		}

		enviarEmailException(msg, stackTrace, message);
	}

	public void enviarEmailException(String msg, String stackTrace, String message) {

		StringBuilder body = new StringBuilder();

		body.append("Falha ").append(msg).append("<br>");
		body.append("<br>Erro: ").append(message).append("<br><br>");

		stackTrace = DummyUtils.stringToHTML(stackTrace);
		body.append(stackTrace);

		String serverName = DummyUtils.getServer();
		String lanIp = DummyUtils.getLanIp();
		String externalIp = "";//DummyUtils.getExternalIp();
		String dataStr = DummyUtils.formatDateTime2(new Date());
		body.append("<div style='font-size: 11px; padding-top: 30px;'>Origem: ").append(serverName).append(" ").append(lanIp).append(" ").append(externalIp).append(" data: ").append(dataStr).append("</div>");

		String subject = DummyUtils.SYSPREFIX + "Erro inesperado: " + msg;
		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body.toString());
		vo.setSubject(subject);
		vo.setTo(toAlerta);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	public void enviarBacalhau(BacalhauVO bacalhauVO, Map<String, String> emails, String prefixo) {

		Date dataInicio = bacalhauVO.getDataInicio();
		String dataInicioStr = DummyUtils.formatDateTime(dataInicio);
		Date dataFim = bacalhauVO.getDataFim();
		String dataFimStr = DummyUtils.formatDateTime(dataFim);
		int totalArquivos = bacalhauVO.getTotalArquivos();
		int ferradas = bacalhauVO.getFerradas();

		StringBuilder html = new StringBuilder();
		html.append("<table>");
		html.append("	<tr>");
		html.append("		<td>Total de Arquivos:</td>");
		html.append("		<td>").append(totalArquivos).append("</td>");
		html.append("	</tr>");
		html.append("	<tr>");
		html.append("		<td>Per&iacute;odo:</td>");
		html.append("		<td>").append(dataInicioStr).append(" &agrave; ").append(dataFimStr).append("</td>");
		html.append("	</tr>");
		html.append("	<tr>");
		html.append("		<td>Total de imagens ferradas:</td>");
		html.append("		<td>").append(ferradas).append("</td>");
		html.append("	</tr>");
		html.append("</table>");

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(html.toString());
		vo.setTo(emails);
		if (ferradas == 0) {
			vo.setSubject(DummyUtils.SYSPREFIX + "Tudo ok no bacalhau " + prefixo);
		} else {
			vo.setSubject(DummyUtils.SYSPREFIX + ferradas + " imagens ferradas no bacalhau " + prefixo);
		}
		enviar(vo);
	}

	public void enviarAlertaPop3(EmailRecebido er) {

		byte[] conteudoHtml = er.getConteudoHtml();
		String conteudoHtmlStr = StringZipUtils.uncompress(conteudoHtml);

		String subject = er.getSubject();
		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(conteudoHtmlStr);
		vo.setSubject(DummyUtils.SYSPREFIX + "Alerta Pop3: " + subject);
		vo.setTo(toAlerta);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	public void enviarNotificacaoResetSenha(Usuario autor, Usuario usuario, String urlSistema) {

		Map<String, String> to = new LinkedHashMap<String, String>();
		String email = usuario.getEmail();
		String nome = usuario.getNome();
		to.put(email, nome);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("urlSistema", urlSistema);
		String nomeSistema = resourceService.getValue("nomeSistema");
		model.put("nomeSistema", nomeSistema);
		String nomeAutor = autor.getNome();
		model.put("autor", nomeAutor);
		String login = usuario.getLogin();
		model.put("login", login);

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("notificacao-reset-senha.htm", model, attachments);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		vo.setSubject("Notificação de reinicialização de senha");
		vo.setTo(to);
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}

	private void handleException(EmailSmtpVO vo, Exception e) {
		systraceThread("Falha no envio de e-mail: " + DummyUtils.getExceptionMessage(e));
		systraceThread("assunto: " + vo.getSubject());
		systraceThread("body: " + vo.getBody());
		systraceThread("to: " + vo.getTo());
		e.printStackTrace();
	}

	public void enviarEmailAnalistaSemDemanda(Usuario analista, Map<String, String> emailsGestoresNeo) {

		if ("dev".equals(DummyUtils.getMode())) {
			systraceThread("Envio de e-mail sobre analista sem demanda não habilitado quando getdoc.mode=dev.", LogLevel.ERROR);
			return;
		}

		Map<String, Object> model = new HashMap<>();
		String analistaNome = analista.getNome();
		model.put("analista", analistaNome);
		Set<UsuarioSubperfil> usps = analista.getSubperfils();
		List<UsuarioSubperfil> uspsList = usuarioSubperfilService.orderByNivel(usps);
		List<String> subperfis = new ArrayList<>();
		for (UsuarioSubperfil usp : uspsList) {
			Subperfil subperfil = usp.getSubperfil();
			String subperfilDescricao = subperfil.getDescricao();
			subperfis.add(subperfilDescricao);
		}
		String subperfisStr = subperfis.toString();
		subperfisStr = subperfisStr.replace("[", "");
		subperfisStr = subperfisStr.replace("]", "");
		model.put("subperfis", subperfisStr);
		Long analistaId = analista.getId();
		model.put("analistaId", analistaId);
		String urlSistema = resourceService.getValue(ResourceService.URL_SISTEMA_EXTRANET);
		model.put("urlSistema", urlSistema);
		Map<String, File> attachments = new HashMap<>();
		String body = getBody("email-analista-sem-demanda.htm", model, attachments);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		vo.setSubject(DummyUtils.SYSPREFIX + "Analista Sem Demanda " + analistaNome);
		vo.setTo(emailsGestoresNeo);
		vo.setAttachments(attachments);
		enviar(vo);
	}

	public void enviarNotificacaoEnviosAnaliseJob(String nomeJob, List<Long> processosOksIds, List<Long> processosErrosIds) {

		String urlSistema = resourceService.getValue(ResourceService.URL_SISTEMA_EXTRANET);
		String valor = parametroService.getValor(ParametroService.P.EMAILS_ENVIO_ANALISE_JOB);
		if(StringUtils.isBlank(valor)) return;

		Map<String, String> to = new LinkedHashMap<String, String>();
		String[] emails = valor.split(",");
		for (String email : emails) {
			boolean valido = DummyUtils.validarEmail(email.trim());
			if(valido){
				to.put(email.trim(), null);
			}
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("nomeJob", nomeJob);
		model.put("processos", processosOksIds);
		model.put("erros", processosErrosIds);
		model.put("data", new Date());
		model.put("urlSistema", urlSistema);
		model.put("dummyUtils", new DummyUtils());

		Map<String, File> attachments = new HashMap<>();
		String body = getBody("notificacao-envios-analise-job.htm", model, attachments);

		EmailSmtpVO vo = new EmailSmtpVO();
		vo.setBody(body);
		vo.setSubject("Processos Enviados para Análise JOB - GetDoc");
		vo.setTo(to);
		vo.setAttachments(attachments);
		try {
			enviar(vo);
		}
		catch (Exception e) {
			handleException(vo, e);
		}
	}
}
