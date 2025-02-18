package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.enumeration.StatusDocumentoPortal;
import net.wasys.util.DummyUtils;

import java.util.Date;
import java.util.List;

@ApiModel(value="DocumentoGraduacaoResponse")
public class DocumentoGraduacaoResponse {

	@ApiModelProperty(notes="ID do documento.")
	private Long id;

	@ApiModelProperty(notes="ID do tipo de documento.")
	private Long tipoDocumentoId;

	@ApiModelProperty(notes="Nome do documento.")
	private String nome;

	@ApiModelProperty(notes="Status do documento.")
	private StatusDocumentoPortal status;

	@ApiModelProperty(notes="Observação do analista a respeito da irregularidade, caso houver.")
	private String observacao;

	@ApiModelProperty(notes="Identifica se o documento é obrigatório ou não.")
	private boolean obrigatorio;

	@ApiModelProperty(notes="Indica se o documento é considerado como 'entregue', ou não.")
	private boolean entregue;

	@ApiModelProperty(notes="Indica se o documento trata-se do contrato educacional.")
	private boolean contratoEducacional;

	@ApiModelProperty(notes="Descrição do documento que será exibida ao inscrito.")
	private String descricao;

	@ApiModelProperty(notes="Indica se permite ou não o upload manual do documento.")
	private boolean permiteUploadManual;

	@ApiModelProperty(notes="Códigos dos documentos equivalentes.")
	private List<Long> equivalentes;

	@ApiModelProperty(notes="Data da última atualização/digitalização do documento.")
	private Date dataDigitalizacao;

	@ApiModelProperty(notes="Indica se está vencido ou não o documento.")
	private boolean vencido;

	public DocumentoGraduacaoResponse() {
	}

	public DocumentoGraduacaoResponse(TipoDocumento tipoDocumento) {
		Long tipoDocumentoId = tipoDocumento.getId();
		Long codSia = tipoDocumento.getCodOrigem();
		String descricao = tipoDocumento.getDescricao();
		String nome = tipoDocumento.getNome();
		boolean obrigatorio = tipoDocumento.getObrigatorio();
		boolean contratoEducacional = TipoDocumento.CONTRATO_PRESTACAO_SERVICO_EDUCACIONAIS.contains(tipoDocumentoId);
		descricao = descricao != null ? DummyUtils.htmlToString(descricao) : null;

		boolean permiteUpload = tipoDocumento.isPermiteUpload();
		this.tipoDocumentoId = codSia;
		this.nome = nome;
		this.observacao = null;
		this.entregue = false;
		this.vencido = false;
		this.obrigatorio = obrigatorio;
		this.contratoEducacional = contratoEducacional;
		this.descricao = descricao;
		this.permiteUploadManual = permiteUpload;
		this.status = StatusDocumentoPortal.NAO_ENTREGUE;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTipoDocumentoId() {
		return tipoDocumentoId;
	}

	public void setTipoDocumentoId(Long tipoDocumentoId) {
		this.tipoDocumentoId = tipoDocumentoId;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public StatusDocumentoPortal getStatus() {
		return status;
	}

	public void setStatus(StatusDocumentoPortal status) {
		this.status = status;
		if(status != null && status.ordinal() > StatusDocumentoPortal.NAO_ENTREGUE.ordinal()) {
			this.entregue = true;
		}
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public List<Long> getEquivalentes() {
		return equivalentes;
	}

	public void setEquivalentes(List<Long> equivalentes) {
		this.equivalentes = equivalentes;
	}

	public boolean getEntregue() {
		return entregue;
	}

	public void setEntregue(boolean entregue) {
		this.entregue = entregue;
	}

	public boolean getContratoEducacional() {
		return contratoEducacional;
	}

	public void setContratoEducacional(boolean contratoEducacional) {
		this.contratoEducacional = contratoEducacional;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public boolean getPermiteUploadManual() {
		return permiteUploadManual;
	}

	public void setPermiteUploadManual(boolean permiteUploadManual) {
		this.permiteUploadManual = permiteUploadManual;
	}

	public Date getDataDigitalizacao() {
		return dataDigitalizacao;
	}

	public void setDataDigitalizacao(Date dataDigitalizacao) {
		this.dataDigitalizacao = dataDigitalizacao;
	}

	public boolean isVencido() {
		return vencido;
	}

	public void setVencido(boolean vencido) {
		this.vencido = vencido;
	}
}
