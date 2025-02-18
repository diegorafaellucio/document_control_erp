package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.Estado;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "ALUNO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"CPF", "NOME"}))
public class Aluno extends net.wasys.util.ddd.Entity {

	public static final String CPF_ALUNO_GENERICO = "999.999.999-99";

	private Long id;
	private String nome;
	private String nomeSocial;
	private String cpf;
	private String passaporte;
	private String identidade;
	private Estado ufIdentidade;
	private String orgaoEmissor;
	private Date dataEmissao;
	private String mae;
	private String pai;
	private boolean ativa = true;
	private Date dataCadastro;

	public Aluno() {}

	public Aluno(Long id) {this.id = id;}

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

	@Column(name="NOME_SOCIAL")
	public String getNomeSocial() {
		return nomeSocial;
	}

	public void setNomeSocial(String nomeSocial) {
		this.nomeSocial = nomeSocial;
	}

	@Column(name="CPF")
	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	@Column(name="PASSAPORTE")
	public String getPassaporte() {
		return passaporte;
	}

	public void setPassaporte(String passaporte) {
		this.passaporte = passaporte;
	}

	@Column(name="IDENTIDADE")
	public String getIdentidade() {
		return identidade;
	}

	public void setIdentidade(String identidade) {
		this.identidade = identidade;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="UF_IDENTIDADE")
	public Estado getUfIdentidade() {
		return ufIdentidade;
	}

	public void setUfIdentidade(Estado ufIdentidade) {
		this.ufIdentidade = ufIdentidade;
	}

	@Column(name="ORGAO_EMISSOR")
	public String getOrgaoEmissor() {
		return orgaoEmissor;
	}

	public void setOrgaoEmissor(String orgaoEmissor) {
		this.orgaoEmissor = orgaoEmissor;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_EMISSAO")
	public Date getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(Date dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	@Column(name="MAE")
	public String getMae() {
		return mae;
	}

	public void setMae(String mae) {
		this.mae = mae;
	}

	@Column(name="PAI")
	public String getPai() {
		return pai;
	}

	public void setPai(String pai) {
		this.pai = pai;
	}

	@Column(name="ATIVA")
	public boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_CADASTRO")
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}
}
