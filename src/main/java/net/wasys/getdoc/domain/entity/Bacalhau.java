package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="bacalhau_log")
public class Bacalhau extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date data;
	private Integer totalArquivos;
	private Integer totalErros;
	private String tipoExecucao;
	private String arquivosErro;

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

	@Column(name = "DATA")
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Column(name = "TOTAL_ARQUIVOS")
	public Integer getTotalArquivos() {
		return totalArquivos;
	}

	public void setTotalArquivos(Integer totalArquivos) {
		this.totalArquivos = totalArquivos;
	}

	@Column(name = "TOTAL_ERROS")
	public Integer getTotalErros() {
		return totalErros;
	}

	public void setTotalErros(Integer totalErros) {
		this.totalErros = totalErros;
	}

	@Column(name = "TIPO_EXECUCAO")
	public String getTipoExecucao() {
		return tipoExecucao;
	}

	public void setTipoExecucao(String tipoExecucao) {
		this.tipoExecucao = tipoExecucao;
	}

	@Column(name = "ARQUIVOS_ERRO")
	public String getArquivosErro() {
		return arquivosErro;
	}

	public void setArquivosErro(String arquivosErro) {
		this.arquivosErro = arquivosErro;
	}
}
