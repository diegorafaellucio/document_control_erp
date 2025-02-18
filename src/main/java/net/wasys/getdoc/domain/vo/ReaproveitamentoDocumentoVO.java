package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.StatusDocumento;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReaproveitamentoDocumentoVO {

	private Long documentoCaptacaoId;
	private String nome;
	private List<ReaproveitamentoImagemVO> imagens = new ArrayList<>(0);
	private List<String> labelsDarknet;
	private Date validade;
	private String codOrigem;
	private String metadados;
	private StatusDocumento statusDocumento;
	private String usuarioDigitalizou;
	private String usuarioAprovou;
	private String posfixo;
	private Boolean obrigatorio;
	private Integer versaoAtual;
	private String motivoPendencia;
	private String observacaoPendencia;
	private String motivoAdvertencia;
	private String observacaoAdvertencia;

	public Long getDocumentoCaptacaoId() {
		return documentoCaptacaoId;
	}

	public void setDocumentoCaptacaoId(Long documentoCaptacaoId) {
		this.documentoCaptacaoId = documentoCaptacaoId;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public List<String> getLabelsDarknet() {
		return labelsDarknet;
	}

	public void setLabelsDarknet(List<String> labelsDarknet) {
		this.labelsDarknet = labelsDarknet;
	}

	public Date getValidade() {
		return validade;
	}

	public void setValidade(Date validade) {
		this.validade = validade;
	}

	public String getCodOrigem() {
		return codOrigem;
	}

	public void setCodOrigem(String codOrigem) {
		this.codOrigem = codOrigem;
	}

	public String getMetadados() {
		return metadados;
	}

	public void setMetadados(String metadados) {
		this.metadados = metadados;
	}

	public List<ReaproveitamentoImagemVO> getImagens() {
		return imagens;
	}

	public void setImagens(List<ReaproveitamentoImagemVO> imagens) {
		this.imagens = imagens;
	}

	public String getUsuarioDigitalizou() {
		return usuarioDigitalizou;
	}

	public void setUsuarioDigitalizou(String usuarioDigitalizou) {
		this.usuarioDigitalizou = usuarioDigitalizou;
	}

	public String getUsuarioAprovou() {
		return usuarioAprovou;
	}

	public void setUsuarioAprovou(String usuarioAprovou) {
		this.usuarioAprovou = usuarioAprovou;
	}

	public String getPosfixo() {
		return posfixo;
	}

	public void setPosfixo(String posfixo) {
		this.posfixo = posfixo;
	}

	public Boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(Boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public StatusDocumento getStatusDocumento() {
		return statusDocumento;
	}

	public void setStatusDocumento(StatusDocumento statusDocumento) {
		this.statusDocumento = statusDocumento;
	}

	public Integer getVersaoAtual() {
		return versaoAtual;
	}

	public void setVersaoAtual(Integer versaoAtual) {
		this.versaoAtual = versaoAtual;
	}

	public String getMotivoPendencia() {
		return motivoPendencia;
	}

	public void setMotivoPendencia(String motivoPendencia) {
		this.motivoPendencia = motivoPendencia;
	}

	public String getObservacaoPendencia() {
		return observacaoPendencia;
	}

	public void setObservacaoPendencia(String observacaoPendencia) {
		this.observacaoPendencia = observacaoPendencia;
	}

	public String getMotivoAdvertencia() {
		return motivoAdvertencia;
	}

	public void setMotivoAdvertencia(String motivoAdvertencia) {
		this.motivoAdvertencia = motivoAdvertencia;
	}

	public String getObservacaoAdvertencia() {
		return observacaoAdvertencia;
	}

	public void setObservacaoAdvertencia(String observacaoAdvertencia) {
		this.observacaoAdvertencia = observacaoAdvertencia;
	}

	@Override public String toString() {
		return "ReaproveitamentoDocumentoVO{" +
				"documentoCaptacaoId=" + documentoCaptacaoId +
				", nome='" + nome + '\'' +
				", imagens=" + imagens +
				", labelsDarknet=" + labelsDarknet +
				", validade=" + validade +
				", codOrigem='" + codOrigem + '\'' +
				", metadados='" + metadados + '\'' +
				", statusDocumento=" + statusDocumento +
				", usuarioDigitalizou='" + usuarioDigitalizou + '\'' +
				", usuarioAprovou='" + usuarioAprovou + '\'' +
				", posfixo='" + posfixo + '\'' +
				", obrigatorio=" + obrigatorio +
				'}';
	}
}
