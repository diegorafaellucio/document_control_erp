package net.wasys.getdoc.domain.vo;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.TipoTipificacao;

public class FileVO implements Cloneable {

	private File file;
	private String text;
	private String name;
	private String length;
	private String hash;
	private List<CampoOcr> camposOcr;
	private ModeloDocumento modeloTipificacao;
	private Map<String, String> metadados;
	private TipoTipificacao tipoTipificacao;
	private MetadadosArquivoVO metadadosArquivoVO;
	private Origem origem;
	private ImagemMeta imagemMeta;

	private Imagem imagem;
	private boolean modeloDeDocumentoNaoIdentificado ;
	private GrupoModeloDocumento grupoModeloDocumento;

	private boolean extraiuOCR;

	public FileVO() {}

	public FileVO(File file) {
		this.file = file;
		this.name = file != null ? file.getName() : null;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public List<CampoOcr> getCamposOcr() {
		return camposOcr;
	}

	public void setCampos(List<CampoOcr> camposOcr) {
		this.camposOcr = camposOcr;
	}

	public ModeloDocumento getModeloTipificacao() {
		return modeloTipificacao;
	}

	public void setModeloTipificacao(ModeloDocumento modeloTipificacao) {
		this.modeloTipificacao = modeloTipificacao;
	}

	public Map<String, String> getMetadados() {
		return metadados;
	}

	public void setMetadados(Map<String, String> metadados) {
		this.metadados = metadados;
	}

	public TipoTipificacao getTipoTipificacao() {
		return tipoTipificacao;
	}

	public void setTipoTipificacao(TipoTipificacao tipoTipificacao) {
		this.tipoTipificacao = tipoTipificacao;
	}

	public MetadadosArquivoVO getMetadadosArquivoVO() { return metadadosArquivoVO; }

	public void setMetadadosArquivoVO(MetadadosArquivoVO metadadosArquivoVO) { this.metadadosArquivoVO = metadadosArquivoVO; }

	public ImagemMeta getImagemMeta() {
		return imagemMeta;
	}

	public void setImagemMeta(ImagemMeta imagemMeta) {
		this.imagemMeta = imagemMeta;
	}

	@Override
	public FileVO clone() {
		try {
			return (FileVO) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	@Override
	public String toString() {
		return getClass().getName() + "{file:" + file.getName() + "}";
	}

	public Imagem getImagem() {
		return imagem;
	}

	public void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}

	public boolean isExtraiuOCR() {
		return extraiuOCR;
	}

	public void setExtraiuOCR(boolean extraiuOCR) {
		this.extraiuOCR = extraiuOCR;
	}

	public boolean isModeloDeDocumentoNaoIdentificado() {
		return modeloDeDocumentoNaoIdentificado;
	}

	public void setModeloDeDocumentoNaoIdentificado(boolean modeloDeDocumentoNaoIdentificado) {
		this.modeloDeDocumentoNaoIdentificado = modeloDeDocumentoNaoIdentificado;
	}

	public GrupoModeloDocumento getGrupoModeloDocumento() {
		return grupoModeloDocumento;
	}

	public void setGrupoModeloDocumento(GrupoModeloDocumento grupoModeloDocumento) {
		this.grupoModeloDocumento = grupoModeloDocumento;
	}

}