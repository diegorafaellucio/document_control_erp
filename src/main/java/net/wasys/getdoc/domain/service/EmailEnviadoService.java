package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusDocumentoPortal;
import net.wasys.getdoc.domain.repository.EmailEnviadoRepository;
import net.wasys.getdoc.domain.vo.EmailVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.StringZipUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class EmailEnviadoService {

	private static final Pattern PATTERN_EMAIL_DESTINATARIO = Pattern.compile(".*<(.*)>.*");
	private static final Pattern PATTERN_CODIGO_EMAIL = Pattern.compile(".*(\\[(\\d+/\\d+)\\]).*");
	private static final String REGEX_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	public static final String CONVITE_EMAIL = "CONVITE";

	@Autowired private EmailEnviadoRepository emailEnviadoRepository;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ProcessoLogAnexoService processoLogAnexoService;
	@Autowired private ProcessoService processoService;
	@Autowired private ResourceService resourceService;
	@Autowired private ParametroService parametroService;
	@Autowired private DocumentoService documentoService;
	@Autowired private PendenciaService pendenciaService;
	@Autowired private CampanhaService campanhaService;
	@Autowired private TextoPadraoService textoPadraoService;

	@Transactional(rollbackFor=Exception.class)
	public void enviarEmail(EmailVO vo, Processo processo, Usuario usuario) throws Exception {

		List<String> destinatarios = vo.getDestinatariosList();
		String destinatariosStr = destinatarios.toString();
		destinatariosStr = destinatariosStr.replaceAll("^\\[", "");
		destinatariosStr = destinatariosStr.replaceAll("\\]$", "");
		destinatariosStr = destinatariosStr.replaceAll(", ", ",");
		destinatariosStr = destinatariosStr.replaceAll("\\[", "<");
		destinatariosStr = destinatariosStr.replaceAll("\\]", ">");
		String assunto = vo.getAssunto();
		assunto = StringUtils.isNotBlank(assunto) ? assunto : "Contato " + resourceService.getValue(ResourceService.NOME_EMPRESA_ASSUNTO_EMAIL);
		String observacaoTmp = vo.getObservacaoTmp();
		String body = vo.getBody();

		if(StringUtils.isNotBlank(body) && StringUtils.isBlank(observacaoTmp)) {
			observacaoTmp = DummyUtils.htmlToString(body);
		}

		ProcessoLog log = vo.getLogCriacao();
		if(log == null) {
			log = new ProcessoLog();
			log.setProcesso(processo);
			log.setUsuario(usuario);
			log.setAcao(AcaoProcesso.ENVIO_EMAIL);
			log.setObservacao(observacaoTmp);
		}

		EmailEnviado ee = new EmailEnviado();
		ee.setAssunto(assunto);
		ee.setDataEnvio(new Date());
		ee.setProcesso(processo);
		ee.setDestinatarios(destinatariosStr);

		validarDestinatarios(ee);

		emailEnviadoRepository.saveOrUpdate(ee);

		String codigo = criaCodigo(ee);
		ee.setCodigo(codigo);
		emailEnviadoRepository.saveOrUpdate(ee);

		log.setEmailEnviado(ee);
		processoLogService.saveOrUpdate(log);

		List<FileVO> arquivos = vo.getArquivos();
		for (FileVO arquivo : arquivos) {
			String fileName = arquivo.getName();
			parametroService.validarArquivoPermitido(fileName);
		}

		emailSmtpService.enviarEmail(ee, log, arquivos, body);

		for (FileVO fileVO : arquivos) {
			processoLogAnexoService.criar(log, fileVO);
		}

		processoService.verificarFluxoTrabalho(processo, usuario, AcaoProcesso.ENVIO_EMAIL);
	}

	public void enviarConviteEmail(EmailVO vo, Processo processo, Usuario usuario) throws Exception {

		List<String> destinatarios = vo.getDestinatariosList();
		String destinatariosStr = destinatarios.toString();
		destinatariosStr = destinatariosStr.replaceAll("^\\[", "");
		destinatariosStr = destinatariosStr.replaceAll("\\]$", "");
		destinatariosStr = destinatariosStr.replaceAll(", ", ",");
		destinatariosStr = destinatariosStr.replaceAll("\\[", "<");
		destinatariosStr = destinatariosStr.replaceAll("\\]", ">");
		String assunto = vo.getAssunto();
		String observacaoTmp = vo.getObservacaoTmp();
		Long processoId = processo.getId();
		String usuarioNome = usuario.getNome();
		String usuarioEmail = usuario.getEmail();
		String dataInicioConvite = DummyUtils.formatDateTime6(vo.getDataInicioConvite());
		String dataFimConvite = DummyUtils.formatDateTime6(vo.getDataFimConvite());

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("conteudo", observacaoTmp);
		model.put("assunto", assunto);
		model.put("processoId", processoId);
		model.put("usuarioNome", usuarioNome);
		model.put("usuarioEmail", usuarioEmail);
		model.put("dataInicioConvite", dataInicioConvite);
		model.put("dataFimConvite", dataFimConvite);
		model.put("dummyUtils", new DummyUtils());
		Map<String, File> attachments = new HashMap<>();
		String body = emailSmtpService.getBody("envio-email-convite.ics", model, attachments);

		observacaoTmp = DummyUtils.htmlToString(observacaoTmp);

		ProcessoLog log = vo.getLogCriacao();
		if(log == null) {
			log = new ProcessoLog();
			log.setProcesso(processo);
			log.setUsuario(usuario);
			log.setAcao(AcaoProcesso.ENVIO_CONVITE);
			log.setObservacao(observacaoTmp);
		}

		EmailEnviado ee = new EmailEnviado();
		ee.setAssunto("["+ CONVITE_EMAIL + "] " + assunto);
		ee.setDataEnvio(new Date());
		ee.setProcesso(processo);
		ee.setDestinatarios(destinatariosStr);

		validarDestinatarios(ee);

		emailEnviadoRepository.saveOrUpdate(ee);

		String codigo = criaCodigo(ee);
		ee.setCodigo(codigo);
		emailEnviadoRepository.saveOrUpdate(ee);

		log.setEmailEnviado(ee);
		processoLogService.saveOrUpdate(log);

		List<FileVO> arquivos = vo.getArquivos();
		for (FileVO arquivo : arquivos) {
			String fileName = arquivo.getName();
			parametroService.validarArquivoPermitido(fileName);
		}

		emailSmtpService.enviarEmail(ee, log, arquivos, body);

		for (FileVO fileVO : arquivos) {
			processoLogAnexoService.criar(log, fileVO);
		}

		processoService.verificarFluxoTrabalho(processo, usuario, AcaoProcesso.ENVIO_CONVITE);
	}

	private void validarDestinatarios(EmailEnviado ee) throws MessageKeyException {

		List<String> emails = getEmailsDestinatarios(ee);
		for (String email : emails) {

			boolean matches = email.matches(REGEX_EMAIL);
			if(!matches) {

				throw new MessageKeyException("emailInvalido2.error", email);
			}
		}
	}

	public List<String> getEmailsDestinatarios(EmailEnviado ee) {

		List<String> destinatariosList = ee.getDestinatariosList();
		List<String> list = new ArrayList<>();
		for (String destinatario : destinatariosList) {

			String email = null;

			Matcher matcher = PATTERN_EMAIL_DESTINATARIO.matcher(destinatario);
			if(matcher.find()) {
				email = matcher.group(1);
			} else {
				email = destinatario;
			}

			email = StringUtils.trim(email);
			list.add(email);
		}

		return list;
	}

	private String criaCodigo(EmailEnviado ee) {
		Long emailEnviadoId = ee.getId();
		int random = (int) (Math.random() * 9999);
		return "[" + random + "/" + emailEnviadoId + "]";
	}

	public EmailEnviado identificarEmail(EmailRecebido er) {

		String subject = er.getSubject();
		EmailEnviado ee = identificarEmail(subject);
		if(ee != null) {
			return ee;
		}

		byte[] conteudo = er.getConteudo();
		String conteudoStr = StringZipUtils.uncompress(conteudo);

		ee = identificarEmail(conteudoStr);
		if(ee != null) {
			return ee;
		}

		return null;
	}

	private EmailEnviado identificarEmail(String str) {

		if(StringUtils.isBlank(str)) {
			return null;
		}

		Matcher matcher = PATTERN_CODIGO_EMAIL.matcher(str);
		while(matcher.find()) {

			String group = matcher.group(1);

			EmailEnviado ee = emailEnviadoRepository.getByCodigo(group);

			if(ee != null) {
				return ee;
			}
		}

		return null;
	}

	public List<EmailVO> findVosByProcesso(Long processoId) {

		List<EmailVO> vos = emailEnviadoRepository.findVosByProcesso(processoId);
		return vos;
	}

	@Transactional(rollbackFor=Exception.class)
	public void enviarNotificacaoAprovacao(Long processoId, String email, Usuario usuario) throws Exception {

		Processo processo = processoService.get(processoId);
		Aluno aluno = processo.getAluno();
		String nome = aluno.getNome();
		String num = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUM_INSCRICAO);
		if(StringUtils.isBlank(num)) {
			num = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUM_CANDIDATO);
		}
		Map<String, String> descsDocs = new HashMap<>();

		String aprovadoLabel = resourceService.getValue("StatusDocumentoPortal.APROVADO.label");
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
		for (Documento documento : documentos) {
			StatusDocumento status = documento.getStatus();
			if(StatusDocumento.APROVADO.equals(status)) {
				String documentoNome = documento.getNome();
				descsDocs.put(documentoNome, aprovadoLabel);
			}
		}

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		String link;
		boolean isPosGraduacao;
		if(tipoProcessoId.equals(TipoProcesso.POS_GRADUACAO)){
			link = resourceService.getValue(ResourceService.PORTAL_CANDIDATO_POS);
			isPosGraduacao = true;
		} else {
			link = resourceService.getValue(ResourceService.PORTAL_CANDIDATO);
			isPosGraduacao = false;
		}

		TextoPadrao textoPadrao = textoPadraoService.get(TextoPadrao.NOTIFICACAO_CANDIDATO_APROVACAO_ID);
		String texto = textoPadrao.getTexto();

		Map<String, Object> model = new HashMap<>();
		model.put("nome", nome);
		model.put("numInscricao", num);
		model.put("documentos", descsDocs);
		model.put("link", link);
		model.put("isPosGraduacao", isPosGraduacao);
		model.put("texto", texto);

		Map<String, File> attachments = new LinkedHashMap<>();
		String body = emailSmtpService.getBody("envio-email-documentacao-aprovada.htm", model, attachments);

		List<String> destinatariosList = new ArrayList<>();
		if(StringUtils.isBlank(email)) {
			email = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.EMAIL);
			if(StringUtils.isBlank(email)){
				processoService.gravarErroDeNotificacaoAluno(processo, email);
				return;
			}
		}

		boolean isEmailValido = DummyUtils.validarEmail(email);
		if(!isEmailValido) {
			processoService.gravarErroDeNotificacaoAluno(processo, email);
			return;
		}

		destinatariosList.add(email);

		ProcessoLog log = processoLogService.criaLog(processo, usuario, AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_APROVACAO);

		EmailVO emailVO = new EmailVO();
		emailVO.setDestinatariosList(destinatariosList);
		emailVO.setBody(body);
		emailVO.setLogCriacao(log);
		String assunto = resourceService.getValue(ResourceService.NOME_EMPRESA_ASSUNTO_EMAIL);
		assunto += " - Documentação Aprovada";
		emailVO.setAssunto(assunto);

		enviarEmail(emailVO, processo, usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void enviarNotificacaoAnaliseIsencao(Long processoId) throws Exception {

		Processo processo = processoService.get(processoId);
		String email = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.EMAIL);

		if(StringUtils.isBlank(email)) {
			processoLogService.criaLog(processo, null, AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_ANALISE_ISENCAO, "Nenhum e-mail informado");
			systraceThread("Nenhum email informado para o processo: " + processoId);
			return;
		}

		String numInscricao = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUM_INSCRICAO);
		String resultado = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.RESULTADO_ISENCAO_DISCIPLINA);

		ProcessoLog log = processoLogService.criaLog(processo, null, AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_ANALISE_ISENCAO);

		Aluno aluno = processo.getAluno();

		List<String> to = new ArrayList<>();
		to.add(email);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("aluno", aluno);
		model.put("dummyUtils", new DummyUtils());
		model.put("numInscricao", numInscricao);
		model.put("resultado", resultado);

		Map<String, File> attachments = new HashMap<>();
		String body = emailSmtpService.getBody("envia-notificacao-analise-isencao.htm", model, attachments);

		EmailVO emailVO = new EmailVO();
		emailVO.setBody(body);
		emailVO.setDestinatariosList(to);
		emailVO.setLogCriacao(log);
		String assunto = resourceService.getValue(ResourceService.NOME_EMPRESA_ASSUNTO_EMAIL);
		assunto += " - Documentação Aprovada";
		emailVO.setAssunto(assunto);

		enviarEmail(emailVO, processo, null);
	}

	@Transactional(rollbackFor=Exception.class)
	public void enviarNotificacaoPendencia(Long processoId, String email, Usuario usuario, AcaoProcesso acaoProcesso) throws Exception {

		Processo processo = processoService.get(processoId);
		Aluno aluno = processo.getAluno();
		String nome = aluno.getNome();
		String num = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUM_INSCRICAO);
		if(StringUtils.isBlank(num)) {
			num = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUM_CANDIDATO);
		}
		Map<String, String> descsDocs = new HashMap<>();

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);
		Campanha campanha = processo.getCampanha();
		if(campanha != null) {

			List<Long> obrigatorios = new ArrayList<>();
			Map<Long, List<Long>> equivalidos = new LinkedHashMap<>();
			campanhaService.carregaObrigatoriosAndEquivalencias(campanha, obrigatorios, null, equivalidos);

			Map<Long, Documento> documentosMap = new HashMap<>();
			for (Documento documento : documentos) {
				TipoDocumento tipoDocumento = documento.getTipoDocumento();
				if(tipoDocumento != null) {
					Long tipoDocumentoId = tipoDocumento.getId();
					documentosMap.put(tipoDocumentoId, documento);
				}
			}

			for (Documento documento : documentos) {
				TipoDocumento tipoDocumento = documento.getTipoDocumento();
				Long tipoDocumentoId = tipoDocumento != null ? tipoDocumento.getId() : null;
				if(obrigatorios.contains(tipoDocumentoId)) {

					StatusDocumento status = documento.getStatus();
					StatusDocumentoPortal status2 = StatusDocumentoPortal.getByStatus(status);
					List<Long> equivalentes = equivalidos.get(tipoDocumentoId);
					if(equivalentes != null) {
						for (Long equivalenteId : equivalentes) {
							Documento equivalente = documentosMap.get(equivalenteId);
							if(equivalente != null) {
								StatusDocumento status3 = equivalente.getStatus();
								StatusDocumentoPortal status4 = StatusDocumentoPortal.getByStatus(status3);
								if(status2.ordinal() < status4.ordinal()) {
									status2 = status4;
								}
							}
						}
					}

					if(StatusDocumentoPortal.NAO_ENTREGUE.equals(status2) || StatusDocumentoPortal.REPROVADO.equals(status2)) {
						String documentoNome = documento.getNome();
						String label = resourceService.getValue("StatusDocumentoPortal." + status2.name() + ".label");

						if(StatusDocumentoPortal.REPROVADO.equals(status2)) {
							Long documentoId = documento.getId();
							Pendencia pendencia = pendenciaService.getLast(documentoId);
							if(pendencia != null) {
								Irregularidade irregularidade = pendencia.getIrregularidade();
								String irregularidadeNome = irregularidade.getNome();
								label += " - " + irregularidadeNome;
							}
						}

						descsDocs.put(documentoNome, label);
					}
				}
			}
		}

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		String link;
		boolean isPosGraduacao;
		if(tipoProcessoId.equals(TipoProcesso.POS_GRADUACAO)){
			link = resourceService.getValue(ResourceService.PORTAL_CANDIDATO_POS);
			isPosGraduacao = true;
		} else {
			link = resourceService.getValue(ResourceService.PORTAL_CANDIDATO);
			isPosGraduacao = false;
		}

		TextoPadrao textoPadrao;
		String texto;
		if(AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_RASCUNHO.equals(acaoProcesso)) {
			textoPadrao = textoPadraoService.get(TextoPadrao.NOTIFICACAO_CANDIDATO_RASCUNHO_ID);
			texto = textoPadrao.getTexto();
		} else {
			textoPadrao = textoPadraoService.get(TextoPadrao.NOTIFICACAO_CANDIDATO_REPROVACAO_ID);
			texto = textoPadrao.getTexto();
		}

		Map<String, Object> model = new HashMap<>();
		model.put("nome", nome);
		model.put("numInscricao", num);
		model.put("documentos", descsDocs);
		model.put("link", link);
		model.put("isPosGraduacao", isPosGraduacao);
		model.put("texto", texto);

		Map<String, File> attachments = new LinkedHashMap<>();

		String body = emailSmtpService.getBody("envio-email-documentacao-pendente.htm", model, attachments);

		if(StringUtils.isBlank(email)) {
			email = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.EMAIL);
			if(StringUtils.isBlank(email)){
				processoService.gravarErroDeNotificacaoAluno(processo, email);
				return;
			}
		}

		boolean isEmailValido = DummyUtils.validarEmail(email);
		if(!isEmailValido) {
			processoService.gravarErroDeNotificacaoAluno(processo, email);
			return;
		}

		List<String> destinatariosList = new ArrayList<>();
		destinatariosList.add(email);

		ProcessoLog log = processoLogService.criaLog(processo, usuario, acaoProcesso);

		EmailVO emailVO = new EmailVO();
		emailVO.setBody(body);
		emailVO.setLogCriacao(log);
		emailVO.setDestinatariosList(destinatariosList);
		String assunto = resourceService.getValue(ResourceService.NOME_EMPRESA_ASSUNTO_EMAIL);
		assunto += " - Documentação Pendente";
		emailVO.setAssunto(assunto);

		for (Map.Entry<String, File> entry : attachments.entrySet()) {
			emailVO.addAnexo(entry.getKey(), entry.getValue());
		}

		enviarEmail(emailVO, processo, usuario);
	}
}
