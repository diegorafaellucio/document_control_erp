package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.Advertencia;
import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusFacial;
import net.wasys.getdoc.domain.enumeration.StatusOcr;

import java.util.*;

public class DocumentoVO {

	private Long id;
	private String nome;
	private Long processoId;
	private StatusDocumento status;
	private boolean aprovavel;
	private boolean rejeitavel;
	private boolean temComentario;
	private boolean podeCopiar;
	private boolean podeMover;
	private boolean podeCortar;
	private boolean podeExcluir;
	private boolean digitalizavel;
	private boolean justificavel;
	private int countInconsistencias;
	private Date dataDigitalizacao;
	private String irregularidadeNome;
	private String pendenciaObservacao;
	private String pendenciaJustificativa;
	private boolean obrigatorio;
	private TipoDocumento tipoDocumento;
	private Integer versaoAtual;
	private StatusOcr statusOcr;
	private StatusFacial statusFacial;
	private boolean faceReconhecida = true;
	private Long irregularidadeId;
	private boolean sempreTipificar;
	private Long modeloDocumentoId;
	private boolean grupoRelacionadoApagado = false;
	private Date validadeExpiracao;
	private boolean requisitarDataValidadeExpiracao;
	private boolean requisitarDataEmissao;
	private boolean requisitarDataPorModelo;
	private boolean existeAdvertencia;
	private boolean isDocumentoOutros;
	private Boolean vencido;
	private Boolean permiteMarcarTermoAceiteIngressoSimplificado;


	private Map<Integer, List<ImagemVO>> imagens;
	private Map<Long, String> possiveisModelosDocumento;
	private Map<Long, String> modelosDocumentoValidarExpiracao;
	private Origem origem;
	private boolean possuiFullText;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public StatusDocumento getStatus() {
		return status;
	}

	public void setStatus(StatusDocumento status) {
		this.status = status;
	}

	public Map<Integer, List<ImagemVO>> getImagens() {
		return imagens;
	}

	public void setImagens(Map<Integer, List<ImagemVO>> imagens) {
		this.imagens = imagens;
	}

	public boolean getAprovavel() {
		return aprovavel;
	}

	public void setAprovavel(boolean aprovavel) {
		this.aprovavel = aprovavel;
	}

	public boolean getRejeitavel() {
		return rejeitavel;
	}

	public void setRejeitavel(boolean rejeitavel) {
		this.rejeitavel = rejeitavel;
	}

	public boolean getTemComentario() {
		return temComentario;
	}

	public void setTemComentario(boolean temComentario) {
		this.temComentario = temComentario;
	}

	public boolean getPodeCopiar() {
		return podeCopiar;
	}

	public void setPodeCopiar(boolean podeCopiar) {
		this.podeCopiar = podeCopiar;
	}

	public boolean getPodeMover() {
		return podeMover;
	}

	public void setPodeMover(boolean podeMover) {
		this.podeMover = podeMover;
	}

	public boolean getPodeExcluir() {
		return podeExcluir;
	}

	public void setPodeExcluir(boolean podeExcluir) {
		this.podeExcluir = podeExcluir;
	}

	public int getCountInconsistencias() {
		return countInconsistencias;
	}

	public void setCountInconsistencias(int countInconsistencias) {
		this.countInconsistencias = countInconsistencias;
	}

	public void setDigitalizavel(boolean digitalizavel) {
		this.digitalizavel = digitalizavel;
	}

	public boolean getDigitalizavel() {
		return digitalizavel;
	}

	public void setJustificavel(boolean justificavel) {
		this.justificavel = justificavel;
	}

	public boolean getJustificavel() {
		return justificavel;
	}

	public Date getDataDigitalizacao() {
		return dataDigitalizacao;
	}

	public void setDataDigitalizacao(Date dataDigitalizacao) {
		this.dataDigitalizacao = dataDigitalizacao;
	}

	public String getPendenciaObservacao() {
		return pendenciaObservacao;
	}

	public void setPendenciaObservacao(String pendenciaObservacao) {
		this.pendenciaObservacao = pendenciaObservacao;
	}

	public void setPendenciaJustificativa(String pendenciaJustificativa) {
		this.pendenciaJustificativa = pendenciaJustificativa;
	}

	public String getPendenciaJustificativa() {
		return pendenciaJustificativa;
	}

	public String getIrregularidadeNome() {
		return irregularidadeNome;
	}

	public void setIrregularidadeNome(String irregularidadeNome) {
		this.irregularidadeNome = irregularidadeNome;
	}

	public boolean isExcluido() {
		boolean excluido = StatusDocumento.EXCLUIDO.equals(status);
		return excluido;
	}

	public boolean isPendente() {
		boolean pendente = StatusDocumento.PENDENTE.equals(status);
		return pendente;
	}

	public boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public Integer getVersaoAtual() {
		return versaoAtual;
	}

	public void setVersaoAtual(Integer versaoAtual) {
		this.versaoAtual = versaoAtual;
	}

	public StatusOcr getStatusOcr() {
		return statusOcr;
	}

	public void setStatusOcr(StatusOcr statusOcr) {
		this.statusOcr = statusOcr;
	}

	public boolean getFaceReconhecida() {
		return faceReconhecida;
	}

	public void setFaceReconhecida(boolean faceReconhecida) {
		this.faceReconhecida = faceReconhecida;
	}

	public StatusFacial getStatusFacial() {
		return statusFacial;
	}

	public void setStatusFacial(StatusFacial statusFacial) {
		this.statusFacial = statusFacial;
	}

	public int getCountImagens() {

		Map<Integer, List<ImagemVO>> imagens = getImagens();
		if(imagens == null || imagens.isEmpty()) {
			return 0;
		}

		Set<Integer> versoesSet = imagens.keySet();
		List<Integer> versoesList = new ArrayList<>(versoesSet);
		int size = versoesList.size();
		Integer ultimaVersao = versoesList.get(size - 1);

		List<ImagemVO> list = imagens.get(ultimaVersao);
		return list.size();
	}

	public Long getIrregularidadeId() {
		return irregularidadeId;
	}

	public void setIrregularidadeId(Long irregularidadeId) {
		this.irregularidadeId = irregularidadeId;
	}

	public boolean getSempreTipificar() {
		return sempreTipificar;
	}

	public void setSempreTipificar(boolean sempreTipificar) {
		this.sempreTipificar = sempreTipificar;
	}

	public Map<Long, String> getPossiveisModelosDocumento() {
		return possiveisModelosDocumento;
	}

	public void setPossiveisModelosDocumento(Map<Long, String> possiveisModelosDocumento) {
		this.possiveisModelosDocumento = possiveisModelosDocumento;
	}

	public Long getModeloDocumentoId() {
		return modeloDocumentoId;
	}

	public void setModeloDocumentoId(Long modeloDocumentoId) {
		this.modeloDocumentoId = modeloDocumentoId;
	}

	public boolean isPodeCortar() { return podeCortar; }

	public void setPodeCortar(boolean podeCortar) { this.podeCortar = podeCortar; }

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	public boolean isGrupoRelacionadoApagado() {
		return grupoRelacionadoApagado;
	}

	public void setGrupoRelacionadoApagado(boolean grupoRelacionadoApagado) {
		this.grupoRelacionadoApagado = grupoRelacionadoApagado;
	}

	public Date getValidadeExpiracao() {
		return validadeExpiracao;
	}

	public void setValidadeExpiracao(Date validadeExpiracao) {
		this.validadeExpiracao = validadeExpiracao;
	}

	public boolean isRequisitarDataValidadeExpiracao() {
		return requisitarDataValidadeExpiracao;
	}

	public void setRequisitarDataValidadeExpiracao(boolean requisitarDataValidadeExpiracao) {
		this.requisitarDataValidadeExpiracao = requisitarDataValidadeExpiracao;
	}

	public boolean isRequisitarDataEmissao() {
		return requisitarDataEmissao;
	}

	public void setRequisitarDataEmissao(boolean requisitarDataEmissao) {
		this.requisitarDataEmissao = requisitarDataEmissao;
	}

	public Map<Long, String> getModelosDocumentoValidarExpiracao() {
		return modelosDocumentoValidarExpiracao;
	}

	public void setModelosDocumentoValidarExpiracao(Map<Long, String> modelosDocumentoValidarExpiracao) {
		this.modelosDocumentoValidarExpiracao = modelosDocumentoValidarExpiracao;
	}

	public boolean isRequisitarDataPorModelo() {
		return requisitarDataPorModelo;
	}

	public void setRequisitarDataPorModelo(boolean requisitarDataValidadeExpiracaoPorModelo) {
		this.requisitarDataPorModelo = requisitarDataValidadeExpiracaoPorModelo;
	}

	public boolean getExisteAdvertencia() {
		return existeAdvertencia;
	}

	public void setExisteAdvertencia(boolean existeAdvertencia) {
		this.existeAdvertencia = existeAdvertencia;
	}

	public boolean isPossuiFullText() {
		return possuiFullText;
	}

	public void setPossuiFullText(boolean possuiFullText) {
		this.possuiFullText = possuiFullText;
	}

	public boolean isDocumentoOutros() {
		return isDocumentoOutros;
	}

	public void setDocumentoOutros(boolean documentoOutros) {
		isDocumentoOutros = documentoOutros;
	}

	public Boolean getVencido() {
		return vencido;
	}

	public void setVencido(Boolean vencido) {
		this.vencido = vencido;
	}

	public Boolean getPermiteMarcarTermoAceiteIngressoSimplificado() {
		return permiteMarcarTermoAceiteIngressoSimplificado;
	}

	public void setPermiteMarcarTermoAceiteIngressoSimplificado(Boolean permiteMarcarTermoAceiteIngressoSimplificado) {
		this.permiteMarcarTermoAceiteIngressoSimplificado = permiteMarcarTermoAceiteIngressoSimplificado;
	}
}
