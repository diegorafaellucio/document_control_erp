package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoExtensaoRelatorio;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "EXECUCAO_GERACAO_RELATORIO")
public class ExecucaoGeracaoRelatorio extends net.wasys.util.ddd.Entity implements Cloneable {

	private Long id;
	private ConfiguracaoGeracaoRelatorio configuracaoGeracaoRelatorio;
	private Date inicio;
	private Date fim;
	private String caminhoArquivo;
	private Boolean sucesso;
	private String observacao;
	private TipoExtensaoRelatorio extensao;

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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CONFIGURACAO_GERACAO_RELATORIO_ID", nullable=false)
	public ConfiguracaoGeracaoRelatorio getConfiguracaoGeracaoRelatorio() {
		return configuracaoGeracaoRelatorio;
	}

	public void setConfiguracaoGeracaoRelatorio(ConfiguracaoGeracaoRelatorio configuracaoGeracaoRelatorio) {
		this.configuracaoGeracaoRelatorio = configuracaoGeracaoRelatorio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="INICIO", nullable = false)
	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="FIM")
	public Date getFim() {
		return fim;
	}

	public void setFim(Date fim) {
		this.fim = fim;
	}

	@Column(name="CAMINHO_ARQUIVO", length = 255)
	public String getCaminhoArquivo() {
		return caminhoArquivo;
	}

	public void setCaminhoArquivo(String caminhoArquivo) {
		this.caminhoArquivo = caminhoArquivo;
	}

	@Column(name="SUCESSO")
	public Boolean getSucesso() {
		return sucesso;
	}

	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}

	@Column(name="OBSERVACAO", length = 800)
	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "EXTENSAO", nullable = false)
	public TipoExtensaoRelatorio getExtensao() {
		return extensao;
	}

	public void setExtensao(TipoExtensaoRelatorio extensao) {
		this.extensao = extensao;
	}

	@Override
	public ExecucaoGeracaoRelatorio clone() {
		try {
			ExecucaoGeracaoRelatorio clone = (ExecucaoGeracaoRelatorio) super.clone();
			clone.setId(null);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
