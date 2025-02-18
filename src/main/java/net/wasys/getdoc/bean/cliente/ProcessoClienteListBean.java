package net.wasys.getdoc.bean.cliente;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.enumeration.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.omnifaces.util.Faces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.CampoGrupo;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.EmailEnviado;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.ItemPendente;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.DocumentoService;
import net.wasys.getdoc.domain.service.ProcessoLogService;
import net.wasys.getdoc.domain.service.ProcessoPendenciaService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.DigitalizacaoVO;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.getdoc.domain.vo.EvidenciaVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.LogVO;
import net.wasys.getdoc.domain.vo.ProcessoPendenciaVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.http.ImagemFilter;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.ReflectionBeanComparator;

@ManagedBean
@ViewScoped
public class ProcessoClienteListBean extends AbstractBean {

	@Autowired private ProcessoService processoService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ProcessoPendenciaService processoPendenciaService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ProcessoLogService processoLogService;

	private ProcessoFiltro filtro = new ProcessoFiltro();

	private List<Processo> list = new ArrayList<>();
	private Map<Processo, Map<CampoGrupo, List<Campo>>> campos = new HashMap<Processo, Map<CampoGrupo,List<Campo>>>();
	private Map<Processo, List<DocumentoVO>> documentos = new HashMap<>();
	private Map<Processo, List<DocumentoVO>> documentosExcluidos = new HashMap<>();
	private Map<Processo, String> documentosMapJson = new HashMap<>();
	private Map<Processo, String> documentosJson = new HashMap<>();
	private DigitalizacaoVO digitalizacaoVO;
	private Map<Processo, ItemPendente> itensPendente = new HashMap<>();
	private Map<Processo, ProcessoPendenciaVO> pendencia = new HashMap<>();
	private EvidenciaVO evidenciaVO;
	private List<Situacao> situacoes;
	private Map<Processo, List<LogVO>> logs = new HashMap<>();;

	protected void initBean() {
		consultar();
	}

	public void consultar() {

		/*filtro.setChassi("9C2KD033006R00348");
		filtro.setContrato("0882280708");
		filtro.setChaveNfe("008.822.807-08");*/

		Long processoId = (Long) getFlashAttribute("processoId");
		if(processoId != null) {
			Processo processo = processoService.get(processoId);
			//String contrato = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUMERO);
			String cpfCnpj = DummyUtils.getCpfCnpj(processo);
			//filtro.setNumero(contrato);
			filtro.setCpfCnpj(cpfCnpj);
		}

		Long processoIdFiltro = filtro.getProcessoId();
		if (processoIdFiltro == null &&
				StringUtils.isBlank(filtro.getCpfCnpj())) {
			list.clear();
			//addMessageWarn("emptyMessage.label");
			return;
		}

		if (processoIdFiltro != null &&
				StringUtils.isNotBlank(filtro.getCpfCnpj())) {

			list = processoService.findByFiltro(filtro, 0, null);
			if (list.isEmpty()) {
				addMessageWarn("emptyMessage.label");
			}

			for (Processo processo : list) {
				initDadosProcesso(processo);
			}
		}
		else {
			addMessageWarn("todosCamposObrigatorios.label");
		}
	}

	private void initDadosProcesso(Processo processo) {
		Set<CampoGrupo> grupos = processo.getGruposCampos();
		for (Iterator<CampoGrupo> iterator = grupos.iterator(); iterator.hasNext();) {
			CampoGrupo campoGrupo = (CampoGrupo) iterator.next();
			if (!"Dados pessoais".equals(campoGrupo.getNome().trim())) {
				iterator.remove();
			}			
		}
		Map<CampoGrupo, List<Campo>> camposMap = new LinkedHashMap<>();
		for (CampoGrupo grupo : grupos) {
			Set<Campo> campos = grupo.getCampos();
			for (Campo campo : campos) {
				if ("Dados pessoais".equals(grupo.getNome().trim())) {
					String nome = campo.getNome().trim();
					if (!"CONTRATO".equals(nome) &&
							!"CHASSI".equals(nome) &&
							!"NOME COMPLETO".equals(nome) &&
							!"CPF/CNPJ".equals(nome)) {
						continue;
					}
				}

				List<Campo> list = camposMap.get(grupo);
				list = list != null ? list : new ArrayList<Campo>();
				camposMap.put(grupo, list);
				list.add(campo);
			}
		}
		this.campos.put(processo, camposMap);

		carregarDocumentos(processo);

		carregarLogs(processo);

		//carregaSolicitacoesPendentes();

		//		this.novaSituacao = null;

		Long processoId = processo.getId();
		ProcessoPendenciaVO processoPendencia = processoPendenciaService.getLastPendenciaByProcesso(processoId);
		pendencia.put(processo, processoPendencia);

		if(processoPendencia != null) {

			List<DocumentoVO> documentos = getDocumentos(processo);
			for (DocumentoVO documentoVO : documentos) {

				if(!documentoVO.isExcluido() && documentoVO.isPendente()) {

					processoPendencia.addDocumentoPendente(documentoVO);
				}
			}
		}
	}

	private boolean podeEditarProcesso(Processo processo) {

		StatusProcesso status = processo.getStatus();
		if(Arrays.asList(StatusProcesso.CONCLUIDO, StatusProcesso.CANCELADO, StatusProcesso.AGUARDANDO_ANALISE).contains(status)) {
			return false;
		}

		return true;
	}


	public boolean podeDigitalizarDocumento(Processo processo) {

		if(!podeEditarProcesso(processo)) {
			return false;
		}

		List<DocumentoVO> documentos = this.documentos.get(processo);

		if (documentos == null || documentos.isEmpty()) {
			return false;
		}

		StatusProcesso status = processo.getStatus();
		if(StatusProcesso.RASCUNHO.equals(status)) {
			return true;
		}
		else if(StatusProcesso.PENDENTE.equals(status)) {
			return true;
		}

		return false;
	}

	public void criarDigitalizacao(Processo processo, Long documentoId) {
		Documento documento = documentoService.get(documentoId);
		this.digitalizacaoVO = new DigitalizacaoVO();
		this.digitalizacaoVO.setDocumento(documento);
		this.digitalizacaoVO.setOrigem(Origem.WEB);
	}

	public void salvarDigitalizacao() {

		try {
			Usuario usuario = usuarioService.getUsuarioCliente();

			processoService.digitalizarImagens(usuario, digitalizacaoVO);

			digitalizacaoVO = null;

			addMessage("alteracaoSalva.sucesso");
			redirect("/cliente/requisicoes/");

			//consultar();
			//initDadosProcesso();
			//carregarDocumentos();
			setRequestAttribute("fecharModal", true);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void uploadAnexo(FileUploadEvent event) {

		List<FileVO> arquivos = null;
		if(this.evidenciaVO != null) {
			arquivos = this.evidenciaVO.getArquivos();
		}
		else if(this.digitalizacaoVO != null) {
			arquivos = this.digitalizacaoVO.getArquivos();
		}

		if(arquivos.size() >= 5) {
			addMessageError("numeroMaximoArquivos.error");
			return;
		}

		UploadedFile uploadedFile = event.getFile();

		File tmpFile = getFile(uploadedFile);

		if(tmpFile != null) {

			String fileName = uploadedFile.getFileName();

			if(this.evidenciaVO != null) {
				this.evidenciaVO.addAnexo(fileName, tmpFile);
			}			
			else if(this.digitalizacaoVO != null) {
				this.digitalizacaoVO.addAnexo(fileName, tmpFile);
			}

			addMessage("arquivoCarregado.sucesso");
		}
	}

	private File getFile(UploadedFile uploadedFile) {

		try {

			File tmpFile = File.createTempFile("anexo-proc-", ".tmp");
			DummyUtils.deleteOnExitFile(tmpFile);

			InputStream is = uploadedFile.getInputStream();
			FileUtils.copyInputStreamToFile(is, tmpFile);

			return tmpFile;
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}

		return null;
	}

	public void removerAnexo(FileVO fileVO) {
		if(this.digitalizacaoVO != null) {
			this.digitalizacaoVO.removerAnexo(fileVO);
		}
	}

	public void carregarDocumentos(Processo processo) {

		Usuario usuario = getUsuarioLogado();
		Long processoId = processo.getId();
		String contextPath = getContextPath();
		String imagePath = contextPath + ImagemFilter.PATH;

		List<DocumentoVO> documentos = documentoService.findVOsByProcesso(processoId, usuario, imagePath);


		Map<Long, DocumentoVO> documentosMap = new LinkedHashMap<Long, DocumentoVO>();
		List<DocumentoVO> documentosIncluidos = new ArrayList<>();
		List<DocumentoVO> documentosExcluidos = new ArrayList<>();

		for (DocumentoVO vo : documentos) {
			Long documentoId = vo.getId();
			documentosMap.put(documentoId, vo);
			StatusDocumento status = vo.getStatus();
			if(StatusDocumento.EXCLUIDO.equals(status)) {
				documentosExcluidos.add(vo);
			} else {
				documentosIncluidos.add(vo);
			}
		}
		this.documentos.put(processo, documentosIncluidos);
		this.documentosExcluidos.put(processo, documentosExcluidos);

		try {
			ObjectMapper mapper = new ObjectMapper();
			documentosMapJson.put(processo, mapper.writeValueAsString(documentosMap));
			documentosJson.put(processo, mapper.writeValueAsString(documentos));
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean podeResponderPendencia(Processo processo) {

		StatusProcesso status = processo.getStatus();
		if(!StatusProcesso.PENDENTE.equals(status)) {
			return false;
		}

		ItemPendente itemPendente = processoService.getItemPendenteResponderPendencia(processo, null);
		itensPendente.put(processo, itemPendente);

		return true;
	}

	public void criarRespostaPendencia(Processo processo) {
		this.evidenciaVO = new EvidenciaVO();
		this.evidenciaVO.setProcesso(processo);
		this.evidenciaVO.setAcao(AcaoProcesso.RESPOSTA_PENDENCIA);
		this.evidenciaVO.setShowTipoEvidencia(false);
		this.evidenciaVO.setShowPrazoDias(false);
		this.evidenciaVO.setShowSituacao(false);
		this.situacoes = null;
	}

	public void salvarEvidencia() {

		try {
			Usuario usuario = usuarioService.getUsuarioCliente();
			Processo processo = evidenciaVO.getProcesso();

			processoService.salvarEvidencia(evidenciaVO, processo, usuario);

			addMessage("alteracaoSalva.sucesso");

			redirect("/cliente/requisicoes/");

			this.evidenciaVO = null;
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void justificarDocumento() {

		try {
			String documentoIdStr = Faces.getRequestParameter("docId");
			Long documentoId = new Long(documentoIdStr);
			Usuario usuario = usuarioService.getUsuarioCliente();
			String observacao = Faces.getRequestParameter("observacao");

			Documento documento = documentoService.get(documentoId);

			if(StringUtils.isBlank(observacao)) {
				addMessageError("validacao-obrigatorio.error", "Observação");
				return;
			}

			documentoService.justificar(documentoId, usuario, observacao);

			Processo processo = documento.getProcesso();
			initDadosProcesso(processo);
			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private void carregarLogs(Processo processo) {

		Long processoId = processo.getId();
		List<ProcessoLog> logs1 = processoLogService.findByProcesso(processoId);

		List<LogVO> logs = new ArrayList<>();
		for (ProcessoLog pl : logs1) {

			AcaoProcesso acao = pl.getAcao();
			if (AcaoProcesso.ENVIO_ANALISE.equals(acao) 
					|| AcaoProcesso.ENVIO_PENDENCIA.equals(acao) 
					|| AcaoProcesso.ALTERACAO_SITUACAO.equals(acao)) {

				LogVO vo = new LogVO(pl);
				logs.add(vo);
			}
			else {

				EmailEnviado emailEnviado = pl.getEmailEnviado();
				if(emailEnviado != null) {

					String emailCliente = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.EMAIL);
					String destinatarios = emailEnviado.getDestinatarios();
					if(destinatarios.contains(emailCliente)) {

						LogVO vo = new LogVO(pl);
						logs.add(vo);
					}
				}
			}
		}

		Collections.sort(logs, new ReflectionBeanComparator<>("data, id"));

		this.logs.put(processo, logs);
	}	

	public void limpar() {
		filtro = new ProcessoFiltro();
		initBean();
	}

	public ProcessoFiltro getFiltro() {
		return filtro;
	}

	public List<Processo> getList() {
		return list;
	}

	public List<Campo> getCampos1(CampoGrupo grupo, Processo processo) {
		Map<CampoGrupo, List<Campo>> map = campos.get(processo);
		List<Campo> list = map.get(grupo);
		if (list == null) {
			return null;
		}
		int size = list.size();
		int meio = (int) Math.ceil(size / 2d);
		return list.subList(0, meio);
	}

	public List<Campo> getCampos2(CampoGrupo grupo, Processo processo) {
		Map<CampoGrupo, List<Campo>> map = campos.get(processo);
		List<Campo> list = map.get(grupo);
		if (list == null) {
			return null;
		}		
		int size = list.size();
		int meio = (int) Math.ceil(size / 2d);
		return list.subList(meio, size);
	}

	public List<DocumentoVO> getDocumentos(Processo processo) {
		return documentos.get(processo);
	}

	public DigitalizacaoVO getDigitalizacaoVO() {
		return digitalizacaoVO;
	}

	public void setDigitalizacaoVO(DigitalizacaoVO digitalizacaoVO) {
		this.digitalizacaoVO = digitalizacaoVO;
	}

	public ItemPendente getItemPendente(Processo processo) {
		return itensPendente.get(processo);
	}

	public ProcessoPendenciaVO getPendencia(Processo processo) {
		return pendencia.get(processo);
	}

	public EvidenciaVO getEvidenciaVO() {
		return evidenciaVO;
	}

	public void setEvidenciaVO(EvidenciaVO evidenciaVO) {
		this.evidenciaVO = evidenciaVO;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public void setSituacoes(List<Situacao> situacoes) {
		this.situacoes = situacoes;
	}

	public List<LogVO> getLogs(Processo processo) {
		List<LogVO> list2 = logs.get(processo);
		return list2;
	}


}