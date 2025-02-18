package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoConfiguracaoRelatorio;
import net.wasys.getdoc.domain.enumeration.TipoExtensaoRelatorio;

import javax.persistence.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Entity(name = "CONFIGURACAO_GERACAO_RELATORIO")
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"NOME"}), @UniqueConstraint(columnNames={"TIPO", "OPCOES", "HORARIO", "ATIVO"})})
public class ConfiguracaoGeracaoRelatorio extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nome;
	private TipoConfiguracaoRelatorio tipo;
	private String opcoes;
	private String horario;
	private String emailsEnvioSucesso;
	private String emailsEnvioErro;
	private Boolean executaEmExpediente;
	private boolean ativo = true;
	private TipoExtensaoRelatorio extensao;
	private boolean dehMenosUm = false;

	@Id
	@Override
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "nome", length = 60, nullable = false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", length = 120, nullable = false)
	public TipoConfiguracaoRelatorio getTipo() {
		return tipo;
	}

	public void setTipo(TipoConfiguracaoRelatorio tipo) {
		this.tipo = tipo;
	}

	@Column(name = "opcoes", length = 8000)
	public String getOpcoes() {
		return opcoes;
	}

	public void setOpcoes(String opcoes) {
		this.opcoes = opcoes;
	}

	@Column(name = "horario", length = 60, nullable = false)
	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	@Column(name = "EMAILS_ENVIO_SUCESSO", length = 600)
	public String getEmailsEnvioSucesso() {
		return emailsEnvioSucesso;
	}

	public void setEmailsEnvioSucesso(String emailsEnvioSucesso) {
		this.emailsEnvioSucesso = emailsEnvioSucesso;
	}

	@Column(name = "EMAILS_ENVIO_ERRO", length = 600)
	public String getEmailsEnvioErro() {
		return emailsEnvioErro;
	}

	public void setEmailsEnvioErro(String emailsEnvioErro) {
		this.emailsEnvioErro = emailsEnvioErro;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "EXECUTA_EM_HORARIO_EXPEDIENTE")
	public Boolean getExecutaEmExpediente() {
		return executaEmExpediente;
	}

	public void setExecutaEmExpediente(Boolean executaEmExpediente) {
		this.executaEmExpediente = executaEmExpediente;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "EXTENSAO", nullable = false)
	public TipoExtensaoRelatorio getExtensao() {
		return extensao;
	}

	public void setExtensao(TipoExtensaoRelatorio extensao) {
		this.extensao = extensao;
	}

	@Column(name = "DEH_MENOS_UM")
	public boolean isDehMenosUm() {
		return dehMenosUm;
	}

	public void setDehMenosUm(boolean dehMenosUm) {
		this.dehMenosUm = dehMenosUm;
	}

	@Transient
	public String[] getEmailsEnvioSucessoList() {
		return isNotBlank(emailsEnvioSucesso) ? emailsEnvioSucesso.split(",") : null;
	}

	@Transient
	public String getEmailsEnvioSucessoString() {

		if (isNotBlank(emailsEnvioSucesso)) {
			return String.join(", ", emailsEnvioSucesso.split(","));
		}

		return "";
	}

	@Transient
	public String[] getEmailsEnvioErroList() {
		return isNotBlank(emailsEnvioErro) ? emailsEnvioErro.split(",") : null;
	}

	@Transient
	public String getEmailsEnvioErroString() {

		if (isNotBlank(emailsEnvioErro)) {
			return String.join(", ", emailsEnvioErro.split(","));
		}

		return "";
	}

	@Override public String toString() {
		return "ConfiguracaoGeracaoRelatorio{" +
				"id=" + id +
				", tipo=" + tipo +
				", opcoes='" + opcoes + '\'' +
				", horario='" + horario + '\'' +
				", emailsEnvioSucesso='" + emailsEnvioSucesso + '\'' +
				", emailsEnvioErro='" + emailsEnvioErro + '\'' +
				", ativo=" + ativo +
				'}';
	}
}
