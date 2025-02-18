package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;

import java.util.Date;

public class RelatorioOperacaoVO {

	private Long processoId;
	private String situacao;
	private Origem origem;
	private String regional;
	private String campus;
	private String formaIngresso;
	private String periodoIngresso;
	private Long documentoId;
	private String nomeDocumento;
	private StatusDocumento statusDocumento;
	private Long totalImagens;
	private AcaoDocumento acaoDocumento;
	private String irregularidade;
	private String observacao;
	private Date data;
	private String usuario;
	private String usuarioNome;

	public RelatorioOperacaoVO(Object[] objects) {
		this.processoId = (Long) objects[0];
		this.situacao = (String) objects[1];
		this.origem = (Origem) objects[2];
		this.regional = (String) objects[3];
		this.campus = (String) objects[4];
		this.formaIngresso = (String) objects[5];
		this.periodoIngresso = (String) objects[6];
		this.documentoId = (Long) objects[7];
		this.nomeDocumento = (String) objects[8];
		this.statusDocumento = (StatusDocumento) objects[9];
		this.totalImagens = (Long) objects[10];
		this.acaoDocumento = (AcaoDocumento) objects[11];
		if(AcaoDocumento.REJEITOU.equals(acaoDocumento)) {
			this.irregularidade = (String) objects[12];
			this.observacao = (String) objects[13];
		}
		this.data = (Date) objects[14];
		this.usuario = (String) objects[15];
		this.usuarioNome = (String) objects[16];
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public String getSituacao() {
		return situacao;
	}

	public void setSituacao(String situacao) {
		this.situacao = situacao;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	public String getRegional() {
		return regional;
	}

	public void setRegional(String regional) {
		this.regional = regional;
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	public String getFormaIngresso() {
		return formaIngresso;
	}

	public void setFormaIngresso(String formaIngresso) {
		this.formaIngresso = formaIngresso;
	}

	public String getPeriodoIngresso() {
		return periodoIngresso;
	}

	public void setPeriodoIngresso(String periodoIngresso) {
		this.periodoIngresso = periodoIngresso;
	}

	public Long getDocumentoId() {
		return documentoId;
	}

	public void setDocumentoId(Long documentoId) {
		this.documentoId = documentoId;
	}

	public String getNomeDocumento() {
		return nomeDocumento;
	}

	public void setNomeDocumento(String nomeDocumento) {
		this.nomeDocumento = nomeDocumento;
	}

	public StatusDocumento getStatusDocumento() {
		return statusDocumento;
	}

	public void setStatusDocumento(StatusDocumento statusDocumento) {
		this.statusDocumento = statusDocumento;
	}

	public Long getTotalImagens() {
		return totalImagens;
	}

	public void setTotalImagens(Long totalImagens) {
		this.totalImagens = totalImagens;
	}

	public AcaoDocumento getAcaoDocumento() {
		return acaoDocumento;
	}

	public void setAcaoDocumento(AcaoDocumento acaoDocumento) {
		this.acaoDocumento = acaoDocumento;
	}

	public String getIrregularidade() {
		return irregularidade;
	}

	public void setIrregularidade(String irregularidade) {
		this.irregularidade = irregularidade;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getUsuarioNome() {
		return usuarioNome;
	}

	public void setUsuarioNome(String usuarioNome) {
		this.usuarioNome = usuarioNome;
	}
}
