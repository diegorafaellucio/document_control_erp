package net.wasys.getdoc.domain.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.enumeration.TipoRegistro;

@Entity(name="LOG_ALTERACAO")
public class LogAlteracao extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoRegistro tipoRegistro;
	private TipoAlteracao tipoAlteracao;
	private Date data;
	private String dados;
	private Long registroId;

	private Usuario usuario;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_REGISTRO", nullable=false)
	public TipoRegistro getTipoRegistro() {
		return this.tipoRegistro;
	}

	public void setTipoRegistro(TipoRegistro tipoRegistro) {
		this.tipoRegistro = tipoRegistro;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_ALTERACAO", nullable=false)
	public TipoAlteracao getTipoAlteracao() {
		return tipoAlteracao;
	}

	public void setTipoAlteracao(TipoAlteracao tipoAlteracao) {
		this.tipoAlteracao = tipoAlteracao;
	}

	@Deprecated
	public void setData(Date data) {
		this.data = data;
	}

	@Version
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	@Column(name="DADOS")
	public String getDados() {
		return dados;
	}

	public void setDados(String dados) {
		this.dados = dados;
	}

	@Column(name="REGISTRO_ID", nullable=false)
	public Long getRegistroId() {
		return registroId;
	}

	public void setRegistroId(Long registroId) {
		this.registroId = registroId;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_ID")
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
}
