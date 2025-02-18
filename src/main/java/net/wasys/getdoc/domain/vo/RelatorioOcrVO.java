package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.CampoOcr;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.LogOcr;
import net.wasys.getdoc.domain.entity.Processo;

import java.util.ArrayList;
import java.util.List;

public class RelatorioOcrVO {

	private Processo processo;
	private Documento documento;
	private LogOcr logOcr;
	private Long countCampoOcr;
	private Long countCampoOcrSucesso;
	private Long countCampoOcrFalha;
	private List<CampoOcr> camposOcr = new ArrayList<>();

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	public LogOcr getLogOcr() {
		return logOcr;
	}

	public void setLogOcr(LogOcr logOcr) {
		this.logOcr = logOcr;
	}

	public Long getCountCampoOcr() {
		return countCampoOcr;
	}

	public void setCountCampoOcr(Long countCampoOcr) {
		this.countCampoOcr = countCampoOcr;
	}

	public Long getCountCampoOcrSucesso() {
		return countCampoOcrSucesso;
	}

	public void setCountCampoOcrSucesso(Long countCampoOcrSucesso) {
		this.countCampoOcrSucesso = countCampoOcrSucesso;
	}

	public Long getCountCampoOcrFalha() {
		return countCampoOcrFalha;
	}

	public void setCountCampoOcrFalha(Long countCampoOcrFalha) {
		this.countCampoOcrFalha = countCampoOcrFalha;
	}

	public List<CampoOcr> getCamposOcr() {
		return camposOcr;
	}

	public void addCampoOcr(CampoOcr ocr) {
		camposOcr.add(ocr);
	}
}