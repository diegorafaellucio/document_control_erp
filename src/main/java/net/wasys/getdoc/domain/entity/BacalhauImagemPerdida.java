package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoArquivoBacalhau;
import net.wasys.getdoc.domain.enumeration.TipoErroBacalhau;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoBacalhau;

import javax.persistence.*;
import java.util.Date;

@Entity(name="BACALHAU_IMAGEM_PERDIDA")
public class BacalhauImagemPerdida extends net.wasys.util.ddd.Entity {

	public static String ULTIMA_IMAGEM_PROCESSADA_ID = "ultimaImagemProcessadaId";
	public static String AGENDAMENTO_FINALIZADO = "agendamentoFinalizado";

	private Long id;
	private Imagem imagem;
	private TipoArquivoBacalhau tipoArquivo;
	private String caminhoCache;
	private Boolean recuperadaDoCache;
	private TipoExecucaoBacalhau tipoExecucao;
	private Date data;
	private String erro;
	private TipoErroBacalhau tipoErro;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IMAGEM_ID", nullable=false)
	public Imagem getImagem() {
		return imagem;
	}

	public void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}

	@Column(name="CAMINHO_CACHE")
	public String getCaminhoCache() {
		return caminhoCache;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_ARQUIVO", nullable=false)
	public TipoArquivoBacalhau getTipoArquivo() {
		return tipoArquivo;
	}

	public void setTipoArquivo(TipoArquivoBacalhau tipoArquivo) {
		this.tipoArquivo = tipoArquivo;
	}

	public void setCaminhoCache(String caminhoCache) {
		this.caminhoCache = caminhoCache;
	}

	@Column(name="RECUPERADA_DO_CACHE")
	public Boolean getRecuperadaDoCache() {
		return recuperadaDoCache;
	}

	public void setRecuperadaDoCache(Boolean recuperadaDoCache) {
		this.recuperadaDoCache = recuperadaDoCache;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_EXECUCAO", nullable=false)
	public TipoExecucaoBacalhau getTipoExecucao() {
		return tipoExecucao;
	}

	public void setTipoExecucao(TipoExecucaoBacalhau tipoExecucao) {
		this.tipoExecucao = tipoExecucao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Column(name = "ERRO")
	public String getErro() {
		return erro;
	}

	public void setErro(String erro) {
		this.erro = erro;
	}

	@Column(name = "TIPO_ERRO")
	public TipoErroBacalhau getTipoErro() {
		return tipoErro;
	}

	public void setTipoErro(TipoErroBacalhau tipoErro) {
		this.tipoErro = tipoErro;
	}
}