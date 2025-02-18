package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.enumeration.TipoArquivoBacalhau;

import java.util.Date;

public abstract class BacalhauArquivoImagemVO implements BacalhauArquivoVO {

	private Imagem imagem;

	public BacalhauArquivoImagemVO(Imagem imagem) {
		this.imagem = imagem;
	}

	@Override
	public TipoArquivoBacalhau getTipoArquivo() {
		return TipoArquivoBacalhau.DOCUMENTO;
	}

	@Override
	public Date getDataRegistro() {
		Documento documento = imagem.getDocumento();
		return documento.getDataDigitalizacao();
	}

	@Override
	public Long getRegistroId() {
		return imagem.getId();
	}

	@Override
	public String getHashChecksum() {
		return imagem.getHashChecksum();
	}

	@Override
	public String getCaminho() {
		return imagem.getCaminho();
	}
}
