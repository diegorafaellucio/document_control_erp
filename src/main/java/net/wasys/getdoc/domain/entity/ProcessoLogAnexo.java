package net.wasys.getdoc.domain.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.wasys.util.DummyUtils;

@Entity(name="PROCESSO_LOG_ANEXO")
public class ProcessoLogAnexo extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nome;
	private String path;
	private String extensao;
	private Long tamanho;
	private String hashChecksum;
	private Integer alturaImagem=0;	
	private Integer larguraImagem=0;

	private ProcessoLog processoLog;

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

	@Column(name="nome", nullable=false, length=100)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="path", nullable=false, length=200)
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Column(name="extensao", nullable=false, length=5)
	public String getExtensao() {
		return extensao;
	}

	public void setExtensao(String extensao) {
		this.extensao = extensao;
	}

	@Column(name="tamanho", nullable=false)
	public Long getTamanho() {
		return tamanho;
	}

	public void setTamanho(Long tamanho) {
		this.tamanho = tamanho;
	}

	@Column(name="HASH_CHECKSUM", nullable=false)
	public String getHashChecksum() {
		return hashChecksum;
	}

	public void setHashChecksum(String hashChecksum) {
		this.hashChecksum = hashChecksum;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PROCESSO_LOG_ID", nullable=false)
	public ProcessoLog getProcessoLog() {
		return processoLog;
	}

	public void setProcessoLog(ProcessoLog processoLog) {
		this.processoLog = processoLog;
	}

	public static String criaPath(ProcessoLogAnexo anexo, String pathRaiz, String separador) {

		String nome = anexo.getNome();
		String extensao = DummyUtils.getExtensao(nome);

		ProcessoLog log = anexo.getProcessoLog();
		Date dataCriacao = log.getData();

		Calendar data = Calendar.getInstance();
		data.setTime(dataCriacao);
		int ano = data.get(Calendar.YEAR);
		int mes = data.get(Calendar.MONTH) + 1;
		String mesStr = mes <= 9 ? "0" + mes : String.valueOf(mes);
		int dia = data.get(Calendar.DAY_OF_MONTH);
		String diaStr = dia <= 9 ? "0" + dia : String.valueOf(dia);

		Long anexoId = anexo.getId();
		String anexoIdStr = anexoId.toString();
		int length = anexoIdStr.length();
		String agrupador = anexoIdStr.substring(length - 3, length);

		StringBuilder caminho = new StringBuilder(pathRaiz);
		caminho.append(ano).append(separador);
		caminho.append(mesStr).append(separador);
		caminho.append(diaStr).append(separador);
		caminho.append(agrupador).append(separador);

		caminho.append(anexoId).append("_").append(anexoId).append(".").append(extensao);

		return caminho.toString();
	}
	
	@Column(name="altura_imagem", nullable=false)
	public Integer getAlturaImagem() {
		return alturaImagem;
	}

	public void setAlturaImagem(Integer alturaImagem) {
		this.alturaImagem = alturaImagem;
	}
	
	@Column(name="largura_imagem", nullable=false)
	public Integer getLarguraImagem() {
		return larguraImagem;
	}

	public void setLarguraImagem(Integer larguraImagem) {
		this.larguraImagem = larguraImagem;
	}
	
}
