package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoExecucaoBacalhau;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BacalhauVO {

	private File relatorio;
	private int ferradas;
	private Date dataFim;
	private Date dataInicio;
	private int totalArquivos;
	private List<BacalhauArquivoVO> arquivosErro = new ArrayList<BacalhauArquivoVO>();
	private TipoExecucaoBacalhau tipoExecucao;

	public void addFerrada(BacalhauArquivoVO arquivo) {
		ferradas++;
		setArquivosErro(arquivo);
	}

	public void addTotalArquivos() {
		totalArquivos++;
	}

	public int getTotalArquivos() {
		return totalArquivos;
	}

	public int getFerradas() {
		return ferradas;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public File getRelatorio() {
		return relatorio;
	}

	public void setRelatorio(File relatorio) {
		this.relatorio = relatorio;
	}

	public List<BacalhauArquivoVO> getArquivosErros() {
		return this.arquivosErro;
	}

	public void setArquivosErro(BacalhauArquivoVO arquivosErro) {
		this.arquivosErro.add(arquivosErro);
	}

	public TipoExecucaoBacalhau getTipoExecucao() {
		return tipoExecucao;
	}

	public void setTipoExecucao(TipoExecucaoBacalhau tipoExecucao) {
		this.tipoExecucao = tipoExecucao;
	}
}
