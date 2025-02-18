package net.wasys.getdoc.domain.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.wasys.getdoc.domain.entity.*;
import org.apache.commons.lang.StringUtils;

import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.util.DummyUtils;

public class EvidenciaVO {

	private Processo processo;
	private String observacaoTmp;
	private AcaoProcesso acao;
	private List<FileVO> arquivos = new ArrayList<>();
	private TipoEvidencia tipoEvidencia;
	private Situacao situacao;
	private Integer prazoDias;
	private boolean showTipoEvidencia = true;
	private boolean showPrazoDias = false;
	private boolean showSituacao;
	private boolean showEmail = false;
	private List<String> destinatariosList;
	private List<? extends CampoAbstract> campos;
	private Subarea subarea;

	public String getObservacaoTmp() {
		return observacaoTmp;
	}

	public void setObservacaoTmp(String observacaoTmp) {
		this.observacaoTmp = StringUtils.trim(observacaoTmp);
	}

	public AcaoProcesso getAcao() {
		return acao;
	}

	public void setAcao(AcaoProcesso acao) {
		this.acao = acao;
	}

	public List<FileVO> getArquivos() {
		return arquivos;
	}

	public void removerAnexo(FileVO fileVO) {

		this.arquivos.remove(fileVO);

		File file = fileVO.getFile();
		DummyUtils.deleteFile(file);
	}

	public void addAnexo(String fileName, File tmpFile) {

		String fileSize = DummyUtils.toFileSize(tmpFile.length());

		FileVO fileVO = new FileVO();
		fileVO.setFile(tmpFile);
		fileVO.setName(fileName);
		fileVO.setLength(fileSize);

		this.arquivos.add(fileVO);
	}

	public List<String> getFilesNames() {

		List<String> filesNames = new ArrayList<>();
		for (FileVO fileVO : arquivos) {
			String name = fileVO.getName();
			filesNames.add(name);
		}

		return filesNames;
	}

	public TipoEvidencia getTipoEvidencia() {
		return tipoEvidencia;
	}

	public void setTipoEvidencia(TipoEvidencia tipoEvidencia) {
		this.tipoEvidencia = tipoEvidencia;
	}

	public boolean getShowTipoEvidencia() {
		return showTipoEvidencia;
	}

	public void setShowTipoEvidencia(boolean showTipoEvidencia) {
		this.showTipoEvidencia = showTipoEvidencia;
	}

	public boolean getShowPrazoDias() {
		return showPrazoDias;
	}

	public void setShowPrazoDias(boolean showPrazoDias) {
		this.showPrazoDias = showPrazoDias;
	}

	public Integer getPrazoDias() {
		return prazoDias;
	}

	public void setPrazoDias(Integer prazoDias) {
		this.prazoDias = prazoDias;
	}

	public boolean getShowSituacao() {
		return showSituacao;
	}

	public void setShowSituacao(boolean showSituacao) {
		this.showSituacao = showSituacao;
	}

	public boolean getShowEmail() {
		return showEmail;
	}

	public void setShowEmail(boolean showEmail) {
		this.showEmail = showEmail;
	}

	public Situacao getSituacao() {
		return situacao;
	}

	public void setSituacao(Situacao situacao) {
		this.situacao = situacao;
	}

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public List<String> getDestinatariosList() {
		return destinatariosList;
	}

	public void setDestinatariosList(List<String> destinatariosList) {
		this.destinatariosList = destinatariosList;
	}

	public List<? extends CampoAbstract> getCampos() {
		return campos;
	}

	public void setCampos(List<? extends CampoAbstract> campos) {
		this.campos = campos;
	}

	public Subarea getSubarea() {
		return subarea;
	}

	public void setSubarea(Subarea subarea) {
		this.subarea = subarea;
	}
}
