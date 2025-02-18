package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

@Entity(name="ADMIN_AJUDA")
@Table(uniqueConstraints=@UniqueConstraint(columnNames="caminho"))
public class AdminAjuda extends net.wasys.util.ddd.Entity {

	private Long id;
	private String caminho;
	private Long tamanho;
	private String hashChecksum;
	private String extensao;
	private String nome;
	private String descricao;
	private Date dataCriacao;
	private Usuario analista;


	@Id
	@Override
	@Column(name="id", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="caminho")
	public String getCaminho() {
		return this.caminho;
	}

	public void setCaminho(String caminho) {
		this.caminho = caminho;
	}

	@Column(name="tamanho")
	public Long getTamanho() {
		return tamanho;
	}

	public void setTamanho(Long tamanho) {
		this.tamanho = tamanho;
	}

	@Column(name="hash_checksum")
	public String getHashChecksum() {
		return hashChecksum;
	}

	public void setHashChecksum(String hashChecksum) {
		this.hashChecksum = hashChecksum;
	}

	@Column(name="extensao", length=10)
	public String getExtensao() {
		return extensao;
	}

	public void setExtensao(String extensao) {
		this.extensao = extensao;
	}

	@Column(name="nome", nullable=false)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="descricao")
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="data_criacao", nullable=false)
	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="usuario_id")
	public Usuario getAnalista() {
		return analista;
	}

	public void setAnalista(Usuario analista) {
		this.analista = analista;
	}

	@Transient
	public static String gerarCaminho(String dir, AdminAjuda adminAjuda) {
		return gerarCaminho(dir, adminAjuda, File.separator);
	}

	@Transient
	public static String gerarCaminho(String dir, AdminAjuda adminAjuda, String separador) {

		Long adminAjudaId = adminAjuda.getId();
		String extensao = adminAjuda.getExtensao();;

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int ano = c.get(Calendar.YEAR);
		int mes = c.get(Calendar.MONTH) + 1;
		String mesStr = mes <= 9 ? "0" + mes : String.valueOf(mes);
		int dia = c.get(Calendar.DAY_OF_MONTH);
		String diaStr = dia <= 9 ? "0" + dia : String.valueOf(dia);

		StringBuilder caminho = new StringBuilder(dir);
		caminho.append(ano).append(separador);
		caminho.append(mesStr).append(separador);
		caminho.append(diaStr).append(separador);

		caminho.append(adminAjudaId).append(".").append(extensao);

		return caminho.toString();
	}
}
