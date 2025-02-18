package net.wasys.getdoc.bean;

import java.util.*;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import net.wasys.getdoc.bean.vo.ProcessoSiaVO;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.vo.ProcessoEditAutorizacao;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@SessionScoped
public class ConsultaCandidatoBean extends AbstractBean {

	@Autowired AlunoService alunoService;
	@Autowired DocumentoService documentoService;
	@Autowired ProcessoService processoService;
	@Autowired CalendarioService calendarioService;
	@Autowired SituacaoService situacaoService;
	@Autowired ConsultaCandidatoService consultaCandidatoService;
	@Autowired BaseRegistroService baseRegistroService;
	@Autowired TipificacaoVisionApiService tipificacaoVisionApiService;
	@Autowired EmailSmtpService emailSmtpService;
	@Autowired TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired TipoProcessoService tipoProcessoService;

	private AlunoFiltro alunoFiltro = new AlunoFiltro();
	private ProcessoEditBean processoEditBean;
	private Long processoId;
	private boolean adicionarDocumentoRevisao;
	private List<ProcessoSiaVO> processoSiaVOS;
	private List<ProcessoSiaVO> processoSiaVOsMesmoCpfCnpj;
	private Processo processoIsencao;
	private Integer processosAbertos;
	private Integer processosFechados;
	private String horasRestantesSituacao;
	private Boolean renderizarCampoPerguntaTE;
	private Boolean existeFinanciamento;

	protected void initBean() {

		if(processoId != null) {
			Processo processo = processoService.get(processoId);
			if(processo == null){
				processoId = null;
				limpar();
				return;
			}
			processoEditBean = new ProcessoEditBean();
			processoEditBean.setProcesso(processo);
			carregarEditBean();
			buscarProcessoIsencao();

			ProcessoEditAutorizacao autorizacao = new ProcessoEditAutorizacao();
			autorizacao.setBean(processoEditBean);
			autorizacao.setTipificacaoVisionApiService(tipificacaoVisionApiService);
			autorizacao.setProcessoService(processoService);
			autorizacao.setProcesso(processo);
			Usuario usuario = getUsuarioLogado();
			autorizacao.setUsuarioLogado(usuario);
			autorizacao.setTelaCandidato(true);
			processoEditBean.setAutorizacao(autorizacao);
			processoEditBean.setChave((int) (Math.random() * 1000000000));
			this.adicionarDocumentoRevisao = false;

			processosAbertos = processoService.countAbertosByCpfCnpj(processo);
			processosFechados = processoService.countFechadosByCpfCnpj(processo);

			renderizarCampoPerguntaTE = false;
			existeFinanciamento = null;
			alunoFiltro = new AlunoFiltro();
		}
	}

	private void buscarProcessoIsencao() {
		Processo processo = processoService.get(processoId);
		this.processoIsencao = processoService.getProcessoIsencaoDisciplinas(processo);
		if(processoIsencao != null) {
			this.horasRestantesSituacao = processoService.getHorasRestantesSituacao(processoIsencao);
		}
	}

	public void limpar() {
		processoId = null;
		processoSiaVOS = null;
		processoEditBean = null;
		alunoFiltro = new AlunoFiltro();
		redirect("/consultas/candidato/");
	}

	public void buscar() {

		try {
			String numInscricao = alunoFiltro.getNumInscricao();
			String numCanditato = alunoFiltro.getNumCandidato();
			String cpf = alunoFiltro.getCpf();

			if(StringUtils.isBlank(numInscricao) && StringUtils.isBlank(numCanditato) && StringUtils.isBlank(cpf)) {
				addMessageError("validacao-obrigatorio.error", "Número de Canditato, ou Número de Inscrição, ou CPF");
				if(processoId != null){
					carregarEditBean();
				}
				return;
			}

			criaOuAtualizaProcessoSia(numInscricao, numCanditato);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void carregarDadosBean() {
		carregarEditBean();
	}

	private void buscarPorCpf(String numCpf) throws Exception {

		AlunoFiltro alunoFiltro = new AlunoFiltro();
		alunoFiltro.setCpf(numCpf);

		processoSiaVOS = consultaCandidatoService.consultarInscricoesSIA(alunoFiltro);

 		if(processoSiaVOS == null || processoSiaVOS.isEmpty()) {
			addMessageWarn("candidatoCpfNaoEncontrado.error", DummyUtils.getCpf(numCpf));
		}
	}

	public void carregarProcessosSiaMesmoCpfCnpj() throws Exception {
		if(processoId != null) {
			Processo processo = processoService.get(processoId);
			Aluno aluno = processo.getAluno();
			String numCpf = aluno.getCpf();
			AlunoFiltro alunoFiltro = new AlunoFiltro();
			if (StringUtils.isNotBlank(numCpf)) {
				numCpf = DummyUtils.getCpfCnpjDesformatado(numCpf);
				alunoFiltro.setCpf(numCpf);
			}

			List<ProcessoSiaVO> siaVOS = consultaCandidatoService.consultarInscricoesSIA(alunoFiltro);

			this.processoSiaVOsMesmoCpfCnpj = siaVOS.stream().filter(p -> p.getProcesso() != null).collect(Collectors.toList());
		}
	}

	public void criaOuAtualizaProcessoSia(String numInscricao, String numCanditato) throws Exception {

		AlunoFiltro alunoFiltro = new AlunoFiltro();
		alunoFiltro.setNumInscricao(numInscricao);
		alunoFiltro.setNumCandidato(numCanditato);
		String cpf = this.alunoFiltro.getCpf();
		Processo processo = null;

		if (StringUtils.isNotBlank(numInscricao) || StringUtils.isNotBlank(numCanditato)) {
			Usuario usuario = getUsuarioLogado();
			processo = consultaCandidatoService.criaOuAtualizaProcessoSia(usuario, alunoFiltro);
			if (processo == null) {
				addMessageWarn("candidatoNaoEncontrado.error");
			}
		}

		if (StringUtils.isNotBlank(cpf)) {
			cpf = DummyUtils.getCpfCnpjDesformatado(cpf);
			buscarPorCpf(cpf);
		} else {
			processoSiaVOS = null;
		}

		if (processo != null){
			processoId = processo.getId();
			processoEditBean = new ProcessoEditBean();
			processoEditBean.setProcesso(processo);
			carregarEditBean();
			redirect("/consultas/candidato/" + processoId);
		} else {
			processoEditBean = null;
		}
	}

	private void carregarEditBean() {
		Processo processo = processoEditBean.getProcesso();
		Long processoId = processo.getId();
		processoEditBean.initCampos(false);
		processoEditBean.carregarDocumentos();
		processoEditBean.carregarPendencia(processoId);
		processoEditBean.carregarCampanhas();
		processoEditBean.setHorasRestantes(processoService.getHorasRestantesEtapa(processo));
		processoEditBean.setHorasRestantesSituacao(processoService.getHorasRestantesSituacao(processo));
		processoEditBean.carregarEmailOriginalFiesAndProuni();
		processoEditBean.iniciaRegras();
		processoEditBean.carregarLogs();
		processoEditBean.carregaLinkAluno();
		processoEditBean.atualizarPermissaoEdicaoDocumentoProUni();
	}

	public void removerGrupoDinamico() {
		processoEditBean.removerGrupoDinamico();
	}

	public void salvarDigitalizacao(boolean revisao) {

		DigitalizacaoVO digitalizacaoVO = processoEditBean.getDigitalizacaoVO();
		if(revisao) digitalizacaoVO.setReenvioAnalise(false);

		try {
			Usuario usuario = getUsuarioLogado();
			processoService.digitalizarImagens(usuario, digitalizacaoVO);
			processoEditBean.setDigitalizacaoVO(null);

			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarEditBean();
	}

	public void excluirDocumento() {

		Processo processo = processoEditBean.getProcesso();
		Documento documento = processoEditBean.getDocumento();

		try {
			Usuario usuario = getUsuarioLogado();
			processoService.excluirDocumento(processo, documento, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarEditBean();
	}

	public void salvarDigitalizacaoTwain() {

		try {
			Map<Long, List<FileVO>> map = processoEditBean.getImagensMap();
			DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
			digitalizacaoVO.setProcesso(processoEditBean.getProcesso());
			digitalizacaoVO.setArquivosMap(map);
			digitalizacaoVO.setOrigem(Origem.WEB);

			Usuario usuario = getUsuarioLogado();
			processoService.digitalizarImagens(usuario, digitalizacaoVO);

			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarEditBean();
	}

	public void salvarRevisao() {

		try {
			Usuario usuario = getUsuarioLogado();
			List<Campo> camposSituacao = this.processoEditBean.getCamposSituacao();
			EvidenciaVO evidenciaVO = processoEditBean.getEvidenciaVO();
			Processo processo = processoService.get(processoId);
			evidenciaVO.setCampos(camposSituacao);

			boolean temDigitalizado = false;
			List<DocumentoVO> documentos = this.processoEditBean.getDocumentos();
			for(DocumentoVO vo : documentos ){
				StatusDocumento status = vo.getStatus();
				TipoDocumento tipoDocumento = vo.getTipoDocumento();
				Long codOrigem = tipoDocumento != null ? tipoDocumento.getCodOrigem() : null;
				if(StatusDocumento.DIGITALIZADO.equals(status) && TipoDocumento.DOCUMENTOS_REVISAO.contains(codOrigem)){
					temDigitalizado = true;
				}
			}

			boolean salvarRevisao;
			boolean revisaoAntiga = processoEditBean.isRevisaoAntiga();
			if(revisaoAntiga){
				salvarRevisao = consultaCandidatoService.salvarRevisaoAntigo(evidenciaVO, usuario, processo, temDigitalizado, adicionarDocumentoRevisao);
			} else {
				salvarRevisao = consultaCandidatoService.salvarRevisao(processoIsencao, usuario, processo, camposSituacao, temDigitalizado, adicionarDocumentoRevisao);
			}

			if(salvarRevisao) {
				setRequestAttribute("fecharModal", true);
				addMessage("alteracaoSalva.sucesso");
				redirect("/consultas/candidato/");
			}else{
				addMessageError("documentoRevisaoIsencaoObrigatorio.error");
				return;
			}

			this.adicionarDocumentoRevisao = false;
			this.processoEditBean.setEvidenciaVO(null);
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarEditBean();
	}

	public void adicionarDocumento() {

		Processo processo = processoEditBean.getProcesso();
		Long novoDocumentoId = processoEditBean.getNovoDocumento();

		try {
			Usuario usuario = getUsuarioLogado();
			processoService.adicionarDocumento(processo, Arrays.asList(novoDocumentoId), usuario);

			setRequestAttribute("fecharModal", true);
			addMessage("alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarEditBean();
	}

	public void atualizarProcessoComSia() {

		try {
			Processo processo = processoEditBean.getProcesso();
			Usuario usuarioLogado = getUsuarioLogado();

			processoService.atualizarProcessoComSiaFiesProuni(processoId, usuarioLogado, null, alunoFiltro, existeFinanciamento);
			addMessage("processoVinculoSia.sucesso");

			renderizarCampoPerguntaTE = false;
			existeFinanciamento = null;
		}
		catch (MessageKeyException e) {

			String key = e.getKey();
			if("candidatoTE.campoPergunta.required.Prouni.error".equals(key) || "candidatoTE.campoPergunta.required.Fies.error".equals(key)) {
				renderizarCampoPerguntaTE = true;
			}

			addMessageError(e);
		}
		catch (Exception e) {
			String msgErro = String.valueOf(e.getMessage());
			if(msgErro.equals("query did not return a unique result: 2")) {
				addMessageError("processoJaExistenteComTipoDeBolsa.error");
			}else{
				addMessageError(e);
			}
		}

		carregarEditBean();
	}

	public boolean verificaStatusProcesso(String status) {
		String[] split = status.split(",");
		for (String s : split) {
			s = StringUtils.trim(s);
			Processo processo = processoEditBean.getProcesso();
			StatusProcesso statusProcesso = processo.getStatus();
			String statusName = statusProcesso.name();
			if (statusName.contains(s)) {
				return true;
			}
		}
		return false;
	}

	public AlunoFiltro getAlunoFiltro() {
		return alunoFiltro;
	}

	public void setAlunoFiltro(AlunoFiltro alunoFiltro) {
		this.alunoFiltro = alunoFiltro;
	}

	public ProcessoEditBean getProcessoEditBean() {
		return processoEditBean;
	}

	public void setProcessoEditBean(ProcessoEditBean processoEditBean) {
		this.processoEditBean = processoEditBean;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public void enviarParaAnalise() {
		processoEditBean.enviarParaAnalise2();
		redirect("/consultas/candidato/");
	}

	public boolean isAdicionarDocumentoRevisao() {
		return adicionarDocumentoRevisao;
	}

	public void setAdicionarDocumentoRevisao(boolean adicionarDocumentoRevisao) {
		this.adicionarDocumentoRevisao = adicionarDocumentoRevisao;
	}

	public List<Long> getDocumentosRevisaoIds(){
		return TipoDocumento.DOCUMENTOS_REVISAO;
	}

	public List<ProcessoSiaVO> getProcessoSiaVOS() {
		return processoSiaVOS;
	}

	public Processo getProcessoIsencao() {
		return processoIsencao;
	}

	public String getHorasRestantesSituacao() {
		return horasRestantesSituacao;
	}

	public List<ProcessoSiaVO> getProcessoSiaVOsMesmoCpfCnpj() {
		return processoSiaVOsMesmoCpfCnpj;
	}

	public void setProcessoSiaVOsMesmoCpfCnpj(List<ProcessoSiaVO> processoSiaVOsMesmoCpfCnpj) {
		this.processoSiaVOsMesmoCpfCnpj = processoSiaVOsMesmoCpfCnpj;
	}

	public Integer getProcessosAbertos() {
		return processosAbertos;
	}

	public void setProcessosAbertos(Integer processosAbertos) {
		this.processosAbertos = processosAbertos;
	}

	public Integer getProcessosFechados() {
		return processosFechados;
	}

	public void setProcessosFechados(Integer processosFechados) {
		this.processosFechados = processosFechados;
	}

	public boolean getRenderizarCampoPerguntaTE() {
		return renderizarCampoPerguntaTE;
	}

	public Boolean getExisteFinanciamento() {
		return existeFinanciamento;
	}

	public void setExisteFinanciamento(Boolean existeFinanciamento) {
		this.existeFinanciamento = existeFinanciamento;
	}
}