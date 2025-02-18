package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoProcessoSeletivo;

import javax.persistence.*;

@Entity(name = "COLUNA_PROCESSO_SELETIVO")
public class ColunaProcessoSeletivo extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nome;
	private String valor;
	private int numeroLinha;
	private String nomeArquivoImportacao;
	private TipoProcessoSeletivo tipoProcessoSeletivo;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="NOME")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="VALOR")
	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@Column(name="NUMERO_LINHA", nullable=false)
	public int getNumeroLinha() {
		return numeroLinha;
	}

	public void setNumeroLinha(int numeroLinha) {
		this.numeroLinha = numeroLinha;
	}

	@Column(name="NOME_ARQUIVO_IMPORTACAO")
	public String getNomeArquivoImportacao() {
		return nomeArquivoImportacao;
	}

	public void setNomeArquivoImportacao(String nomeArquivoImportacao) {
		this.nomeArquivoImportacao = nomeArquivoImportacao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_PROCESSO_SELETIVO")
	public TipoProcessoSeletivo getTipoProcessoSeletivo() {
		return tipoProcessoSeletivo;
	}

	public void setTipoProcessoSeletivo(TipoProcessoSeletivo tipoProcessoSeletivo) {
		this.tipoProcessoSeletivo = tipoProcessoSeletivo;
	}
}
