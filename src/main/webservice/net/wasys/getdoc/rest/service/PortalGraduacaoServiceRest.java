package net.wasys.getdoc.rest.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.service.webservice.aluno.GetDocAlunoService;
import net.wasys.getdoc.domain.service.webservice.sia.SiaService;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.rest.controller.RestPortalGraduacaoV1;
import net.wasys.getdoc.rest.request.vo.*;
import net.wasys.getdoc.rest.response.vo.DocumentoGraduacaoResponse;
import net.wasys.getdoc.rest.response.vo.ResponseDownloadProxyGraduacao;
import net.wasys.getdoc.rest.response.vo.StatusProcessoGraduacaoResponse;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.Bolso;
import net.wasys.util.other.SuperBeanComparator;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class PortalGraduacaoServiceRest extends SuperServiceRest {

	@Autowired private ConsultaCandidatoService consultaCandidatoService;
	@Autowired private DocumentoService documentoService;
	@Autowired private PendenciaService pendenciaService;
	@Autowired private ProcessoService processoService;
	@Autowired private CampanhaService campanhaService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private AlunoService alunoService;
	@Autowired private SiaService siaService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private GetDocAlunoService getDocAlunoService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private CampoService campoService;
	@Autowired private ResourceService resourceService;

	public List<DocumentoGraduacaoResponse> findDocumentos(Processo processo, Long tipoProcessoId, Long tipoAlunoId, String codAluno, boolean proxy) {

		Campanha campanha;
		String cpfCanditato;
		if(processo != null) {
			campanha = processo.getCampanha();
			if (campanha == null) {
				campanha = campanhaService.getByProcesso(processo);
			}
			Aluno aluno = processo.getAluno();
			cpfCanditato = aluno.getCpf();
		}
		else {
			ConsultaInscricoesVO consultaVO = new ConsultaInscricoesVO();
			AlunoFiltro alunoFiltro = new AlunoFiltro();

			if (RestPortalGraduacaoV1.TIPO_ALUNO_INSCRITO.equals(tipoAlunoId)) {
				alunoFiltro.setNumInscricao(codAluno);
				consultaVO = siaService.consultaInscricao(alunoFiltro);
				if(consultaVO == null) {
					consultaVO = consultaCandidatoService.getConsultaInscricoesVO(codAluno, null);
				}
			}else if (RestPortalGraduacaoV1.TIPO_ALUNO_CANDIDATO.equals(tipoAlunoId)) {
				alunoFiltro.setNumCandidato(codAluno);
				consultaVO = siaService.consultaInscricao(alunoFiltro);
				if(consultaVO == null) {
					consultaVO = consultaCandidatoService.getConsultaInscricoesVO(null, codAluno);
				}
			}

			if (consultaVO == null) {
				throw new MessageKeyException("candidatoNaoEncontrado.error");
			} else if (tipoProcessoId == null) {
				Long codFormaIngresso = consultaVO.getCodFormaIngresso();
				tipoProcessoId = TipoProcessoPortal.getByClassificacao(codFormaIngresso);
			}

			campanha = campanhaService.getByConsultaLinhaTempoSia(consultaVO, tipoProcessoId);
			cpfCanditato = consultaVO.getCpf();
		}

		String exibirPortalStr = campanha.getExibirNoPortalIds();
		List<Long> exibirPortal = null;
		if(exibirPortalStr != null) {
			exibirPortal = new ArrayList<>();
			String[] exibirIdsArr = exibirPortalStr.split(",");
			for (String idStr : exibirIdsArr) {
				if (!idStr.isEmpty()) {
					long id = new Long(idStr);
					exibirPortal.add(id);
				}
			}
		}

		List<Long> obrigatorios = new ArrayList<>();
		Map<Long, List<Long>> equivalenciasMap = new LinkedHashMap<>();
		campanhaService.carregaObrigatoriosAndEquivalencias(campanha, obrigatorios, equivalenciasMap, null);

		Map<Long, List<Long>> equivalenciasSiaMap = new LinkedHashMap<>();
		for(Long equivalenteId : equivalenciasMap.keySet()){
			List<Long> equivalencias = equivalenciasMap.get(equivalenteId);
			List<Long> updateCodSia = tipoDocumentoService.findCodSia(equivalencias);
			equivalenciasSiaMap.put(equivalenteId, updateCodSia);
		}

		List<TipoDocumento> tiposDocumentos = tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);

		Map<Long, DocumentoGraduacaoResponse> map = new LinkedHashMap<>();
		for (TipoDocumento tipoDocumento : tiposDocumentos) {
			DocumentoGraduacaoResponse vo = buildDocumentoGraduacaoResponse(obrigatorios, exibirPortal, equivalenciasSiaMap, tipoDocumento, proxy);
			if(vo != null) {
				Long tipoDocumentoId = tipoDocumento.getId();
				map.put(tipoDocumentoId, vo);
			}
		}

		if(processo != null) {
			DocumentoFiltro filtro = new DocumentoFiltro();
			filtro.setProcesso(processo);
			filtro.setFetch(Arrays.asList(DocumentoFiltro.Fetch.TIPO_DOCUMENTO));
			List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);

			for (Documento documento : new ArrayList<>(documentos)) {
				Long processoId = processo.getId();
				TipoDocumento tipoDocumento = documento.getTipoDocumento();
				String nomeDocumento = documento.getNome();
				if(nomeDocumento.contains(Documento.POSFIX_MEMBRO_FAMILIAR)){
					Long tipoDocumentoId = getTipoDocumentoIdMembroFamiliar(documento);
					String membroFamiliar = nomeDocumento.substring(nomeDocumento.indexOf(Documento.POSFIX_MEMBRO_FAMILIAR));
					DocumentoGraduacaoResponse vo = new DocumentoGraduacaoResponse(tipoDocumento);
					if(vo != null) {
						List<CampoGrupo> grupos = campoGrupoService.findByProcessoIdAndNome(processoId, membroFamiliar);
						CampoGrupo grupo = grupos.get(0);
						Long grupoId = grupo.getId();
						List<Campo> campos = campoService.findByGrupoIdAndNome(grupoId, CampoMap.CampoEnum.NOME_COMPOSICAO.getNome());
						Campo membro = campos.get(0);
						String nomeMembro = membro.getValor();
						vo.setNome(nomeDocumento + " - " + nomeMembro);
						vo.setTipoDocumentoId(tipoDocumentoId);
						map.put(tipoDocumentoId, vo);
					}
				}
			}

			ArrayList<Documento> documentosSobrando = preencheStatusDocumentos(equivalenciasMap, map, documentos);

			//documentos não encontrados no map
			for (Documento documento : documentosSobrando) {
				TipoDocumento tipoDocumento = documento.getTipoDocumento();
				if(tipoDocumento != null) {
					DocumentoGraduacaoResponse vo = buildDocumentoGraduacaoResponse(obrigatorios, exibirPortal, equivalenciasSiaMap, tipoDocumento, proxy);
					if (vo != null) {
						Long tipoDocumentoId = tipoDocumento.getId();
						map.put(tipoDocumentoId, vo);
					}
				}
			}

			preencheObservacoes(map, documentos);
		}
		else {
			List<Long> codOrigens = new ArrayList<>();
			List<TipoDocumento> tiposDocumentosReaproveitaveis = tipoDocumentoService.findByReaproveitavel(tipoProcessoId);
			tiposDocumentosReaproveitaveis.forEach(tipoDocumento -> {
				Long codOrigem = tipoDocumento.getCodOrigem();
				if(codOrigem != null) {
					codOrigens.add(codOrigem);
				}
			});

			AlunoConsultaVO vo = new AlunoConsultaVO(TipoConsultaExterna.CONSULTA_DOCUMENTO_GRADUACAO_GETDOC_ALUNO);
			vo.setCpf(cpfCanditato);
			vo.setCodOrigens(codOrigens);
			Map<String, List<String>> consultar = getDocAlunoService.consultarDocumentosReaproveitaveis(vo);

			if (consultar != null && !consultar.isEmpty()) {
				Set<String> codOrigensStr = consultar.keySet();
				for(String cod : codOrigensStr) {
					for (DocumentoGraduacaoResponse documentoGraduacaoResponse : map.values()) {
						Long tipoDocumentoId = documentoGraduacaoResponse.getTipoDocumentoId();
						if (cod != null && Long.valueOf(cod).equals(tipoDocumentoId)) {
							documentoGraduacaoResponse.setStatus(StatusDocumentoPortal.ENTREGUE);
						}
					}
				}
			}
		}

		return new ArrayList<>(map.values());
	}

	private void preencheObservacoes(Map<Long, DocumentoGraduacaoResponse> map, List<Documento> documentos) {

		Map<Long, DocumentoGraduacaoResponse> map2 = new LinkedHashMap<>();

		for (Documento documento : documentos) {
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if(tipoDocumento != null) {
				Long tipoDocumentoId = tipoDocumento.getId();
				String nomeDocumento = documento.getNome();
				if(nomeDocumento.contains(Documento.POSFIX_MEMBRO_FAMILIAR)){
					tipoDocumentoId = getTipoDocumentoIdMembroFamiliar(documento);
				}
				DocumentoGraduacaoResponse vo = map.get(tipoDocumentoId);
				if (vo != null) {
					Long documentoId = documento.getId();
					Date dataDigitalizacao = documento.getDataDigitalizacao();
					vo.setDataDigitalizacao(dataDigitalizacao);
					map2.put(documentoId, vo);
				}
			}
		}

		Set<Long> keySet = map2.keySet();
		List<Long> documentosIds = new ArrayList<>(keySet);
		Map<Long, Pendencia> map3 = pendenciaService.findLasts(documentosIds);

		for (Long documentoId : keySet) {
			Pendencia pendencia = map3.get(documentoId);
			if (pendencia != null) {
				Irregularidade irregularidade = pendencia.getIrregularidade();
				String nome = irregularidade.getNome();
				DocumentoGraduacaoResponse vo = map2.get(documentoId);
				vo.setObservacao(nome);
			}
		}
	}

	private ArrayList<Documento> preencheStatusDocumentos(Map<Long, List<Long>> equivalenciasMap, Map<Long, DocumentoGraduacaoResponse> map, List<Documento> documentos) {

		ArrayList<Documento> documentosSobrando = new ArrayList<>(documentos);
		for (Documento documento : documentos) {
			StatusDocumento status = documento.getStatus();
			StatusDocumentoPortal statusPortal = StatusDocumentoPortal.getByStatus(status);
			statusPortal = statusPortal != null ? statusPortal : StatusDocumentoPortal.NAO_ENTREGUE;

			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if (tipoDocumento != null) {
				Long tipoDocumentoId = tipoDocumento.getId();
				String nomeDocumento = documento.getNome();
				if(nomeDocumento.contains(Documento.POSFIX_MEMBRO_FAMILIAR)){
					tipoDocumentoId = getTipoDocumentoIdMembroFamiliar(documento);
				}
				DocumentoGraduacaoResponse vo = map.get(tipoDocumentoId);
				if (vo != null) {
					Long documentoId = documento.getId();
					vo.setId(documentoId);
					documentosSobrando.remove(documento);
					StatusDocumentoPortal statusPortal2 = vo.getStatus();
					if (statusPortal2.ordinal() < statusPortal.ordinal()) {
						vo.setStatus(statusPortal);
					}
				}

				List<Long> equivalidos = equivalenciasMap.get(tipoDocumentoId);
				if (equivalidos != null) {
					for (Long equivalidoId : equivalidos) {
						DocumentoGraduacaoResponse vo2 = map.get(equivalidoId);
						if (vo2 != null) {
							StatusDocumentoPortal statusPortal3 = vo2.getStatus();
							if (statusPortal3.ordinal() < statusPortal.ordinal()) {
								vo2.setStatus(statusPortal);
							}
						}
					}
				}
			}
		}
		return documentosSobrando;
	}

	private DocumentoGraduacaoResponse buildDocumentoGraduacaoResponse(List<Long> obrigatorios, List<Long> exibirPortal, Map<Long, List<Long>> equivalenciasSiaMap, TipoDocumento tipoDocumento, boolean proxy) {

		Long tipoDocumentoId = tipoDocumento.getId();
		String nomedoc = tipoDocumento.getNome();
		boolean exibirNoPortal;

		if(!proxy) {
			if (exibirPortal == null) {
				exibirNoPortal = tipoDocumento.getExibirNoPortal();
			} else if (exibirPortal.contains(tipoDocumentoId)) {
				exibirNoPortal = true;
			} else {
				exibirNoPortal = false;
			}

			if (Documento.NOME_OUTROS.equals(nomedoc) || Documento.NOME_TIFICANDO.equals(nomedoc) || !exibirNoPortal) {
				return null;
			}
		}

		DocumentoGraduacaoResponse vo = new DocumentoGraduacaoResponse(tipoDocumento);
		List<Long> equivalentes = equivalenciasSiaMap.get(tipoDocumentoId);
		vo.setEquivalentes(equivalentes);
		boolean obrigatorio = obrigatorios != null ? obrigatorios.contains(tipoDocumentoId) : tipoDocumento.getObrigatorio();
		vo.setObrigatorio(obrigatorio);
		vo.setStatus(StatusDocumentoPortal.NAO_ENTREGUE);
		boolean aceiteContrato = tipoDocumento.getAceiteContrato();
		vo.setContratoEducacional(aceiteContrato);

		if(proxy) {
			vo.setTipoDocumentoId(tipoDocumentoId);
		}

		return vo;
	}

	private Processo getProcessoUpload(RequestUploadDocumentoGraduacao upload, Usuario usuario) throws Exception {

		String codAluno = upload.getAlunoCod();
		Long tipoAlunoId = upload.getTipoAlunoId();
		String cpf = upload.getCpf();
		cpf = DummyUtils.getCpf(cpf);
		String nomeAluno = upload.getNome();

		validacaoConsulta(tipoAlunoId, codAluno);

		validacaoUpload(cpf, nomeAluno);

		Processo processo = findByFiltro(tipoAlunoId, codAluno);

		if(processo != null){
			return processo;
		}

		ConsultaInscricoesVO consultaVO;
		AlunoFiltro alunoFiltro = new AlunoFiltro();

		if (RestPortalGraduacaoV1.TIPO_ALUNO_INSCRITO.equals(tipoAlunoId)) {
			alunoFiltro.setNumInscricao(codAluno);
			consultaVO = siaService.consultaInscricao(alunoFiltro);
			if(consultaVO == null) {
				consultaVO = consultaCandidatoService.getConsultaInscricoesVO(codAluno, null);
			}
		} else {
			alunoFiltro.setNumCandidato(codAluno);
			consultaVO = siaService.consultaInscricao(alunoFiltro);
			if(consultaVO == null) {
				consultaVO = consultaCandidatoService.getConsultaInscricoesVO(null, codAluno);
			}
		}

		if(consultaVO == null){
			throw new MessageKeyException("candidatoNaoEncontrado.error");
		}

		Map<String, String> campoValor = consultaCandidatoService.getCampoValorMap(consultaVO);

		Long codFormaIngresso = consultaVO.getCodFormaIngresso();
		Long tipoProcessoId = TipoProcessoPortal.getByClassificacao(codFormaIngresso);
		return criaProcessoPortal(campoValor, usuario, tipoProcessoId, consultaVO);
	}

	private void validacaoUpload(String cpf, String nomeAluno) {
		if(cpf == null || cpf.isEmpty()){
			throw new MessageKeyException("validacao-obrigatorio.error", "Nome do Inscrito");
		}
		else if(nomeAluno == null || nomeAluno.isEmpty()){
			throw new MessageKeyException("validacao-obrigatorio.error", "Nome do Aluno");
		}
	}

	private Processo criaProcessoPortal(Map<String, String> campoValor, Usuario usuario, Long tipoProcessoId, ConsultaInscricoesVO consultaVO) throws Exception{

		TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);

		Map<TipoCampoGrupo, List<TipoCampo>> mapCampos = tipoCampoService.findMapByTipoProcesso(tipoProcessoId);

		Set<TipoCampo> list = preencheCampos(campoValor, mapCampos);

		Aluno aluno = alunoService.saveOrUpdateAluno(consultaVO, usuario);

		Set<CampoAbstract> valoresCampos = new LinkedHashSet<>();
		valoresCampos.addAll(list);

		CriacaoProcessoVO vo = new CriacaoProcessoVO();
		vo.setTipoProcesso(tipoProcesso);
		vo.setAluno(aluno);
		vo.setValoresCampos(valoresCampos);
		vo.setUsuario(usuario);
		String login = usuario.getLogin();
		if(Usuario.PORTAL_GRADUCAO.equals(login)){
			vo.setOrigem(Origem.PORTAL_GRADUCAO);
		}
		else if(Usuario.PORTAL_POS_GRADUACAO.equals(login)){
			vo.setOrigem(Origem.PORTAL_POS_GRADUACAO);
		}
		else if(Usuario.PORTAL_CVC.equals(login)){
			vo.setOrigem(Origem.PORTAL_CVC);
		}

		return processoService.criaProcesso(vo);
	}

	private Set<TipoCampo> preencheCampos(Map<String, String> campoValor, Map<TipoCampoGrupo, List<TipoCampo>> mapCampos) {
		Set<TipoCampo> list = new LinkedHashSet<>();
		for (TipoCampoGrupo grupo : mapCampos.keySet()) {
			Set<TipoCampo> campos = grupo.getCampos();
			for (TipoCampo campo : campos) {
				list.add(campo);
			}
		}

		Set<String> camposNome = campoValor.keySet();

		for (TipoCampo tipoCampo : list) {
			String nomeCampo = tipoCampo.getNome();
			nomeCampo = DummyUtils.substituirCaracteresEspeciais(nomeCampo);
			for (String nome : camposNome) {
				String nomeMap = DummyUtils.substituirCaracteresEspeciais(nome);
				if (nomeMap.equals(nomeCampo)) {
					tipoCampo.setValor(campoValor.get(nome));
				}
			}
		}

		return list;
	}

	public Processo findByFiltro(Long tipoAlunoId, String codAluno) {

		ProcessoFiltro filtro = new ProcessoFiltro();
		filtro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.RECENTEMENTE_APROVADO);
		if (RestPortalGraduacaoV1.TIPO_ALUNO_INSCRITO.equals(tipoAlunoId)) {
			filtro.setCamposFiltro(Arrays.asList(new CampoDinamicoVO(CampoMap.CampoEnum.NUM_INSCRICAO, codAluno)));
		}
		if (RestPortalGraduacaoV1.TIPO_ALUNO_CANDIDATO.equals(tipoAlunoId)) {
			filtro.setCamposFiltro(Arrays.asList(new CampoDinamicoVO(CampoMap.CampoEnum.NUM_CANDIDATO, codAluno)));
		}

		filtro.setTiposProcessoNot(Arrays.asList(TipoProcesso.ISENCAO_DISCIPLINAS));

		List<Processo> processos = processoService.findByFiltro(filtro, null, null);

		if(!processos.isEmpty()){
			return processos.get(0);
		}

		return null;
	}

	public StatusProcessoGraduacaoResponse listaDocumentos(RequestListaDocumentosGraduacao vo) {
		Long tipoAlunoId = vo.getTipoAlunoId();
		String alunoCod = vo.getAlunoCod();
		Long classificacaoProcessoId = vo.getClassificacaoProcessoId();
		Long tipoProcessoId = TipoProcessoPortal.getByClassificacao(classificacaoProcessoId);

		validacaoConsulta(tipoAlunoId, alunoCod);

		Processo processo = findByFiltro(tipoAlunoId, alunoCod);
		return listaDocumentos(processo, tipoProcessoId, tipoAlunoId, alunoCod, false);
	}

	public StatusProcessoGraduacaoResponse listaDocumentosProxy(RequestListaDocumentosProxyGraduacao vo) {
		Long processoId = vo.getProcessoId();
		Processo processo = processoService.get(processoId);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		return listaDocumentos(processo, tipoProcessoId, null, null, true);
	}

	public StatusProcessoGraduacaoResponse statusDocumentos(RequestStatusDocumentosGraduacao vo) {
		Long tipoAlunoId = vo.getTipoAlunoId();
		String alunoCod = vo.getAlunoCod();
		Long classificacaoProcessoId = vo.getClassificacaoProcessoId();
		Long tipoProcessoId = TipoProcessoPortal.getByClassificacao(classificacaoProcessoId);

		validacaoConsulta(tipoAlunoId, alunoCod);

		Processo processo = findByFiltro(tipoAlunoId, alunoCod);
		return listaDocumentos(processo, tipoProcessoId,tipoAlunoId, alunoCod, false);
	}

	private StatusProcessoGraduacaoResponse listaDocumentos(Processo processo, Long tipoProcessoPortalId, Long tipoAlunoId, String alunoCod, boolean proxy) {

		List<DocumentoGraduacaoResponse> documentos = findDocumentos(processo, tipoProcessoPortalId, tipoAlunoId, alunoCod, proxy);

		StatusProcessoGraduacaoResponse response = new StatusProcessoGraduacaoResponse();
		if(processo != null) {
			Long processoId = processo.getId();
			response.setId(processoId);
			StatusProcesso statusProcesso = processo.getStatus();
			response.setStatus(statusProcesso);
		}
		response.setDocumentos(documentos);

		return response;
	}

	private void validacaoConsulta(Long tipoAlunoId, String alunoCod) {
		if(alunoCod == null || alunoCod.isEmpty()){
			throw new MessageKeyException("validacao-obrigatorio.error", "Código do Aluno");
		}
		else if(tipoAlunoId == null){
			throw new MessageKeyException("validacao-obrigatorio.error", "ID do Tipo de Aluno");
		}

		if (!RestPortalGraduacaoV1.TIPO_ALUNO_INSCRITO.equals(tipoAlunoId) && !RestPortalGraduacaoV1.TIPO_ALUNO_CANDIDATO.equals(tipoAlunoId)) {
			throw new MessageKeyException("codAlunoInvalido.error", tipoAlunoId);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public DocumentoGraduacaoResponse uploadDocumento(RequestUploadDocumentoGraduacao vo, List<MultipartFile> files, Usuario usuario) throws Exception {

		String params = DummyUtils.objectToJson(vo);
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		logAcesso.setParameters(params);

		Bolso<DocumentoGraduacaoResponse> responseBolso = new Bolso<>();
		TransactionWrapper tw1 = new TransactionWrapper(applicationContext);
		tw1.setRunnable(() -> {
			PortalGraduacaoServiceRest portalGraduacaoServiceRest = applicationContext.getBean(PortalGraduacaoServiceRest.class);
			DocumentoGraduacaoResponse response = portalGraduacaoServiceRest.uploadDocumento2(vo, files, usuario);
			responseBolso.setObjeto(response);
		});
		tw1.runNewThread();
		tw1.throwException();

		DocumentoGraduacaoResponse response = responseBolso.getObjeto();
		if(response == null) {
			return null;
		}

		TransactionWrapper tw2 = new TransactionWrapper(applicationContext);
		tw2.setRunnable(() -> {
			Long documentoId = response.getId();
			Documento documento = documentoService.get(documentoId);
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			boolean aceiteContrato = tipoDocumento.getAceiteContrato();
			if(aceiteContrato) {
				siaService.notificarDocumento(documento);
			}
		});
		tw2.runNewThread();

		return response;
	}

	@Transactional(rollbackFor=Exception.class)
	public DocumentoGraduacaoResponse uploadDocumentoProxy(RequestProxyGraduacao vo, Usuario usuario) throws Exception {

		String params = DummyUtils.objectToJson(vo);
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		logAcesso.setParameters(params);

		Bolso<DocumentoGraduacaoResponse> responseBolso = new Bolso<>();
		TransactionWrapper tw1 = new TransactionWrapper(applicationContext);
		tw1.setRunnable(() -> {
			PortalGraduacaoServiceRest portalGraduacaoServiceRest = applicationContext.getBean(PortalGraduacaoServiceRest.class);
			DocumentoGraduacaoResponse response = portalGraduacaoServiceRest.uploadDocumento3(vo, usuario);
			responseBolso.setObjeto(response);
		});
		tw1.runNewThread();
		tw1.throwException();

		DocumentoGraduacaoResponse response = responseBolso.getObjeto();
		if(response == null) {
			return null;
		}

		return response;
	}

	/** @deprecated não usar, usar apenas uploadDocumento() */
	@Deprecated
	@Transactional(rollbackFor=Exception.class)
	public DocumentoGraduacaoResponse uploadDocumento2(RequestUploadDocumentoGraduacao vo, List<MultipartFile> files, Usuario usuario) throws Exception {
		if(files == null || files.isEmpty()){
			throw new MessageKeyException("validacao-obrigatorio.error", "Upload do Arquivo");
		}

		Processo processo = getProcessoUpload(vo, usuario);
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Long codSia = vo.getTipoDocumentoId();
		TipoDocumento tipoDocumento = tipoDocumentoService.getByCodOrigem(tipoProcessoId, codSia);
		if(tipoDocumento == null) {
			throw new MessageKeyException("tipoDocumentoPortal.error", String.valueOf(codSia));
		}

		Long tipoDocumentoId = tipoDocumento.getId();
		Long processoId = processo.getId();
		String login = usuario.getLogin();
		Documento documento = documentoService.getFirstByTipoDocumentoId(tipoDocumentoId, processoId);
		boolean aceiteContrato = tipoDocumento.getAceiteContrato();
		boolean termoPastaVermelha = tipoDocumento.getTermoPastaVermelha();
		Long codOrigem = tipoDocumento.getCodOrigem();
		StatusDocumento statusDoc = documento.getStatus();

		boolean isAprovado = StatusDocumento.APROVADO.equals(statusDoc);

		if(isAprovado){
			if (!aceiteContrato && !termoPastaVermelha) {
				throw new MessageKeyException("statusDocumentoAprovado.error");
			}
		}

		DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
		digitalizacaoVO.setDocumento(documento);
		digitalizacaoVO.setProcesso(processo);
		if(Usuario.PORTAL_GRADUCAO.equals(login)) {
			digitalizacaoVO.setOrigem(Origem.PORTAL_GRADUCAO);
		} else if(Usuario.PORTAL_POS_GRADUACAO.equals(login)){
			digitalizacaoVO.setOrigem(Origem.PORTAL_POS_GRADUACAO);
		} else if(Usuario.PORTAL_CVC.equals(login)){
			digitalizacaoVO.setOrigem(Origem.PORTAL_CVC);
		} else {
			digitalizacaoVO.setOrigem(Origem.SERVICE);
		}

		List<FileVO> list = new ArrayList<>();
		String nomesArquivos = vo.getNomeArquivo();
		String extensao = DummyUtils.getExtensao(nomesArquivos);
		String[] nomesArquivosSplit = nomesArquivos.split("\\|");

		for (int i = 0; i < files.size(); i++) {

			String nomeArquivo = nomesArquivos;

			MultipartFile multipartFile = files.get(i);
			if(nomesArquivosSplit.length > i) {
				nomeArquivo = nomesArquivosSplit[i];
				extensao = DummyUtils.getExtensao(nomeArquivo);
			}

			File tempFile = File.createTempFile("upload-portal-", "." + extensao);
			DummyUtils.deleteOnExitFile(tempFile);
			InputStream is = multipartFile.getInputStream();
			FileUtils.copyInputStreamToFile(is, tempFile);

			if(GetdocConstants.EXTENSAO_DEFINICAO_HTML.contains(extensao)){
				File tempPdf = File.createTempFile("arquivo-html-portal", ".pdf");
				ConverterProperties converterProperties = new ConverterProperties();
				HtmlConverter.convertToPdf(new FileInputStream(tempFile), new FileOutputStream(tempPdf), converterProperties);
				nomeArquivo = tempPdf.getName();
				tempFile = tempPdf;
			}

			FileVO fileVO = new FileVO();
			fileVO.setName(nomeArquivo);
			fileVO.setFile(tempFile);
			list.add(fileVO);
		}

		digitalizacaoVO.setArquivos(list);

		processoService.digitalizarImagens(usuario, digitalizacaoVO);

		boolean isAceiteContrato = tipoDocumento.getAceiteContrato();
		if(isAceiteContrato) {
			ModeloDocumento modeloDocumento = documento.getModeloDocumento();
			documentoService.aprovar(documento, modeloDocumento, usuario);
		}

		List<DocumentoGraduacaoResponse> documentos = findDocumentos(processo, tipoProcessoId, null, null, false);
		StatusProcesso statusProcesso = processo.getStatus();
		int quantidade = 0;
		List<StatusDocumentoPortal> statusPendente = Arrays.asList(StatusDocumentoPortal.NAO_ENTREGUE, StatusDocumentoPortal.REPROVADO);
		for (DocumentoGraduacaoResponse vo2 : documentos) {
			StatusDocumentoPortal statusDocumentoPortal = vo2.getStatus();
			boolean obrigatorio = vo2.getObrigatorio();
			if(statusPendente.contains(statusDocumentoPortal) && obrigatorio) {
				quantidade++;
			}
		}

		if(quantidade == 0 && Arrays.asList(StatusProcesso.RASCUNHO, StatusProcesso.PENDENTE).contains(statusProcesso) &&
				(!tipoProcessoId.equals(TipoProcesso.SIS_PROUNI) && !tipoProcessoId.equals(TipoProcesso.SIS_FIES))){
			Situacao situacaoInicial = tipoProcesso.getSituacaoInicial();
			StatusProcesso status = situacaoInicial.getStatus();
			processoService.enviarParaAnalise(processo, usuario, status, null, null, true, false);
		}

		for (DocumentoGraduacaoResponse vo2 : documentos) {
			Long tipoDocumentoId1 = vo2.getTipoDocumentoId();
			if(tipoDocumentoId1.equals(codSia)) {
				return vo2;
			}
		}

		return null;
	}

	/** @deprecated não usar, usar apenas uploadDocumentoProxy() */
	@Deprecated
	@Transactional(rollbackFor=Exception.class)
	public DocumentoGraduacaoResponse uploadDocumento3(RequestProxyGraduacao vo, Usuario usuario) throws Exception {
		RequestProxyGraduacao.DadosUploadDocumentoCaptacaoRequestVO[] arquivos = vo.getArquivos();
		if(arquivos == null || arquivos.length == 0){
			throw new MessageKeyException("validacao-obrigatorio.error", "Upload do Arquivo");
		}

		Boolean fgMembroFamiliar = false;
		String strMembro = null;
		Long processoId = vo.getProcessoId();
		Processo processo = processoService.get(processoId);
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		Long tipoDocumentoId = vo.getTipoDocumentoId();

		Map<String, Object> metadadosMap =  new LinkedHashMap<>();
		metadadosMap.put("tipoDocIdParams", tipoDocumentoId);
		metadadosMap.put("tipoProcessoIdParams", tipoProcessoId);

		TipoDocumento tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);
		if (tipoDocumento == null) {
			fgMembroFamiliar = true;
			String tipoDocumentoComMembro = String.valueOf(tipoDocumentoId);
			String strNumMembro = StringUtils.right(tipoDocumentoComMembro, 3);
			strMembro = Documento.POSFIX_MEMBRO_FAMILIAR + " (" + String.valueOf(Integer.parseInt(strNumMembro)) + ")";
			tipoDocumento = getTipoDocumentoMembroFamiliar(tipoDocumentoId);
			tipoDocumentoId = tipoDocumento.getId();
		}

		if(tipoDocumento == null) {
			throw new MessageKeyException("tipoDocumentoPortal.error", String.valueOf(tipoDocumentoId));
		}

		String login = usuario.getLogin();

		Documento documento;
		if(fgMembroFamiliar){
			documento = documentoService.getFirstByTipoDocumentoIdMembroFamiliar(tipoDocumentoId, processoId, strMembro);
		} else {
			documento = documentoService.getFirstByTipoDocumentoId(tipoDocumentoId, processoId);
		}

		if(documento == null) {
			documento = processoService.criaDocumento(processo, tipoDocumento, usuario);
		}

		JSONObject metadadosJson = new JSONObject(metadadosMap);
		String metadadosStr = metadadosJson.toString();
		documento.setMetaDados(metadadosStr);

		DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
		digitalizacaoVO.setDocumento(documento);
		digitalizacaoVO.setProcesso(processo);
		if(Usuario.PORTAL_GRADUCAO.equals(login)) {
			digitalizacaoVO.setOrigem(Origem.PORTAL_GRADUCAO);
		} else if(Usuario.PORTAL_POS_GRADUACAO.equals(login)){
			digitalizacaoVO.setOrigem(Origem.PORTAL_POS_GRADUACAO);
		} else if(Usuario.PORTAL_CVC.equals(login)){
				digitalizacaoVO.setOrigem(Origem.PORTAL_CVC);
		} else {
			digitalizacaoVO.setOrigem(Origem.SERVICE);
		}

		List<FileVO> list = new ArrayList<>();

		for (RequestProxyGraduacao.DadosUploadDocumentoCaptacaoRequestVO arquivo : arquivos) {

			String nomeArquivo = arquivo.getNomeArquivo();
			String path = arquivo.getPath();
			File tempFile = new File(path);
			FileVO fileVO = new FileVO();
			fileVO.setName(nomeArquivo);
			fileVO.setFile(tempFile);
			list.add(fileVO);
		}

		digitalizacaoVO.setArquivos(list);

		processoService.digitalizarImagens(usuario, digitalizacaoVO);

		boolean isAceiteContrato = tipoDocumento.getAceiteContrato();
		if(isAceiteContrato) {
			ModeloDocumento modeloDocumento = documento.getModeloDocumento();
			documentoService.aprovar(documento, modeloDocumento, usuario);
		}

		List<DocumentoGraduacaoResponse> documentos = findDocumentos(processo, tipoProcessoId, null, null, true);
		StatusProcesso statusProcesso = processo.getStatus();
		int quantidade = 0;
		List<StatusDocumentoPortal> statusPendente = Arrays.asList(StatusDocumentoPortal.NAO_ENTREGUE, StatusDocumentoPortal.REPROVADO);
		for (DocumentoGraduacaoResponse vo2 : documentos) {
			StatusDocumentoPortal statusDocumentoPortal = vo2.getStatus();
			boolean obrigatorio = vo2.getObrigatorio();
			if(statusPendente.contains(statusDocumentoPortal) && obrigatorio) {
				quantidade++;
			}
		}

		if(quantidade == 0 && Arrays.asList(StatusProcesso.RASCUNHO, StatusProcesso.PENDENTE).contains(statusProcesso)){
			processoService.enviarParaAnalise(processo, usuario);
		}
		Long voTipoDocumentoId = vo.getTipoDocumentoId();
		for (DocumentoGraduacaoResponse vo2 : documentos) {
			Long tipoDocumentoId1 = vo2.getTipoDocumentoId();
			if(tipoDocumentoId1.equals(voTipoDocumentoId)) {
				return vo2;
			}
		}

		return null;
	}

	private TipoDocumento getTipoDocumentoMembroFamiliar(Long tipoDocumentoId){
		String tipoDocumentoComMembro = String.valueOf(tipoDocumentoId);
		String strTipoDocumento = tipoDocumentoComMembro.substring(0,(tipoDocumentoComMembro.length() - 3));
		tipoDocumentoId = Long.parseLong(strTipoDocumento);
		return tipoDocumentoService.get(tipoDocumentoId);
	}

	private Long getTipoDocumentoIdMembroFamiliar(Documento documento){
		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		String nomeDocumento = documento.getNome();
		String strTipoDocumentoId = String.valueOf(tipoDocumento.getId());
		String membroFamiliar = nomeDocumento.substring(nomeDocumento.indexOf(Documento.POSFIX_MEMBRO_FAMILIAR));
		String numMembro = membroFamiliar.replaceAll("[^\\d.]", "");
		if(numMembro.length() >= 2){
			strTipoDocumentoId = strTipoDocumentoId + "0" + numMembro;
		}
		else{
			strTipoDocumentoId = strTipoDocumentoId + "00" + numMembro;
		}
		return Long.parseLong(strTipoDocumentoId);
	}

	public ResponseDownloadProxyGraduacao getLastDocumentoProxy(RequestDadosLastDocumentoRequestVO vo) throws IOException {

		Long tipoDocumentoId = vo.getTipoDocumentoId();
		Long processoId = vo.getProcessoId();
		Boolean fgMembroFamiliar = false;
		String strMembro = null;
		TipoDocumento tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);

		if (tipoDocumento == null) {
			fgMembroFamiliar = true;
			String tipoDocumentoComMembro = String.valueOf(tipoDocumentoId);
			//FIXME não entendi essa lógica, gambi para ajustar o problema em prod
			tipoDocumentoComMembro = tipoDocumentoComMembro.length() == 1 ? "00".concat(tipoDocumentoComMembro) : tipoDocumentoComMembro;
			tipoDocumentoComMembro = tipoDocumentoComMembro.length() == 2 ? "0".concat(tipoDocumentoComMembro) : tipoDocumentoComMembro;
			String strNumMembro = tipoDocumentoComMembro.substring(3);
			strMembro = Documento.POSFIX_MEMBRO_FAMILIAR + " (" + String.valueOf(Integer.parseInt(strNumMembro)) + ")";
			tipoDocumento = getTipoDocumentoMembroFamiliar(tipoDocumentoId);
			tipoDocumentoId = tipoDocumento.getId();
		}

		Documento documento;
		if(fgMembroFamiliar){
			documento = documentoService.getFirstByTipoDocumentoIdMembroFamiliar(tipoDocumentoId, processoId, strMembro);
		} else {
			documento = documentoService.getFirstByTipoDocumentoId(tipoDocumentoId, processoId);
		}

		Integer versaoAtual = documento.getVersaoAtual();
		Long documentoId = documento.getId();
		DownloadVO download = documentoService.getDownload(documentoId, versaoAtual);
		return copiarDownloadParaArquivoTemporario(download);
	}

	private ResponseDownloadProxyGraduacao copiarDownloadParaArquivoTemporario(DownloadVO download) throws IOException {
		ResponseDownloadProxyGraduacao responseDownloadProxyGraduacao = new ResponseDownloadProxyGraduacao();

		File file = download.getFile();
		String storageTmPath = resourceService.getValue(ResourceService.STORAGE_TMP_PATH);
		String arquivoOriginalName = download.getFileName();
		String extensao = DummyUtils.getExtensao(arquivoOriginalName);
		String prefix = DummyUtils.SYSPREFIX + "/reaproveitamento/proxy/";
		prefix = prefix.replace("[", "").replace("]", "").replace(" ", "");

		File tmp = new File(storageTmPath + File.separator + prefix, file.length() + "." + extensao);
		FileUtils.copyFile(file, tmp);
		String path = tmp.getPath();
		responseDownloadProxyGraduacao.setNomeArquivo(file.length() + "." + extensao);
		responseDownloadProxyGraduacao.setPath(path);

		return responseDownloadProxyGraduacao;
	}

	public StatusProcessoGraduacaoResponse listaDocumentosCategoriaProxy(String chaveCategoria, Long processoId) {

		StatusProcessoGraduacaoResponse response = new StatusProcessoGraduacaoResponse();
		List<DocumentoGraduacaoResponse> documentoGraduacaoResponses = new ArrayList<>();

		Processo processo = processoService.get(processoId);

		if(processo == null){
			throw new MessageKeyException("processoNaoLocalizado.error", processoId);
		}

		List<Documento> documentos = documentoService.findByProcessoIdAndCategoriaDocumento(processoId, chaveCategoria);
		if(documentos.isEmpty()) {
			return response;
		}

		for(Documento documento : documentos) {
			StatusDocumento status = documento.getStatus();
			if(!StatusDocumento.EXCLUIDO.equals(status)) {
				Long documentoId = documento.getId();
				Date dataDigitalizacao = documento.getDataDigitalizacao();
				StatusDocumentoPortal statusDocumentoPortal = StatusDocumentoPortal.getByStatus(status);
				boolean entregue = statusDocumentoPortal.equals(StatusDocumentoPortal.ENTREGUE) || statusDocumentoPortal.equals(StatusDocumentoPortal.APROVADO);
				TipoDocumento tipoDocumento = documento.getTipoDocumento();
				Long tipoDocumentoId = tipoDocumento.getId();
				Date validadeExpiracao = documento.getValidadeExpiracao();
				boolean vencido = false;
				if (validadeExpiracao != null) vencido = validadeExpiracao.before(new Date());
				DocumentoGraduacaoResponse vo = new DocumentoGraduacaoResponse(tipoDocumento);
				String nomeDocumento = documento.getNome();
				if(nomeDocumento.contains(Documento.POSFIX_MEMBRO_FAMILIAR)){
					String membroFamiliar = nomeDocumento.substring(nomeDocumento.indexOf(Documento.POSFIX_MEMBRO_FAMILIAR));
					if(vo != null) {
						List<CampoGrupo> grupos = campoGrupoService.findByProcessoIdAndNome(processoId, membroFamiliar);
						CampoGrupo grupo = grupos.get(0);
						Long grupoId = grupo.getId();
						List<Campo> campos = campoService.findByGrupoIdAndNome(grupoId, CampoMap.CampoEnum.NOME_COMPOSICAO.getNome());
						Campo membro = campos.get(0);
						String nomeMembro = membro.getValor();
						vo.setNome(nomeDocumento + " - " + nomeMembro);
					}
				}
				vo.setId(documentoId);
				vo.setTipoDocumentoId(tipoDocumentoId);
				vo.setStatus(statusDocumentoPortal);
				vo.setDataDigitalizacao(dataDigitalizacao);
				vo.setEntregue(entregue);
				vo.setVencido(vencido);
				documentoGraduacaoResponses.add(vo);
			}
		}

		List<DocumentoGraduacaoResponse> documentoResponses = new ArrayList<>(documentoGraduacaoResponses);
		documentoResponses.sort(new SuperBeanComparator<>("nome"));
		List<DocumentoGraduacaoResponse> documentosDigitalizados = new ArrayList<>();
		List<DocumentoGraduacaoResponse> documentosPendentes = new ArrayList<>();
		List<DocumentoGraduacaoResponse> documentosAprovados = new ArrayList<>();
		List<DocumentoGraduacaoResponse> documentosIncluidos = new ArrayList<>();
		for(DocumentoGraduacaoResponse documentoAlunoResponse : documentoResponses) {
			StatusDocumentoPortal status = documentoAlunoResponse.getStatus();
			if(StatusDocumentoPortal.ENTREGUE.equals(status)){
				documentosDigitalizados.add(documentoAlunoResponse);
			} else if (StatusDocumentoPortal.REPROVADO.equals(status)){
				documentosPendentes.add(documentoAlunoResponse);
			} else if (StatusDocumentoPortal.APROVADO.equals(status)){
				documentosAprovados.add(documentoAlunoResponse);
			} else {
				documentosIncluidos.add(documentoAlunoResponse);
			}
		}

		List<DocumentoGraduacaoResponse> documentoAlunoResponsesOrder = new ArrayList<>();
		documentoAlunoResponsesOrder.addAll(documentosDigitalizados);
		documentoAlunoResponsesOrder.addAll(documentosPendentes);
		documentoAlunoResponsesOrder.addAll(documentosAprovados);
		documentoAlunoResponsesOrder.addAll(documentosIncluidos);

		StatusProcesso status = processo.getStatus();
		response.setId(processoId);
		response.setStatus(status);
		response.setDocumentos(documentoAlunoResponsesOrder);

		return response;
	}
}