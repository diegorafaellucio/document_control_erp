package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.EmailRecebidoAnexo;
import net.wasys.getdoc.domain.enumeration.TipoArquivoBacalhau;

import java.util.Date;

public abstract class BacalhauArquivoEmailRecebidoAnexoVO implements BacalhauArquivoVO {

	private EmailRecebidoAnexo anexo;

	public BacalhauArquivoEmailRecebidoAnexoVO(EmailRecebidoAnexo anexo) {
		this.anexo = anexo;
	}

	@Override
	public TipoArquivoBacalhau getTipoArquivo() {
		return TipoArquivoBacalhau.EMAIL_RECEBIDO;
	}

	@Override
	public Date getDataRegistro() {
		EmailRecebido emailRecebido = anexo.getEmailRecebido();
		return emailRecebido.getData();
	}

	@Override
	public Long getRegistroId() {
		return anexo.getId();
	}

	@Override
	public String getHashChecksum() {
		return anexo.getHashChecksum();
	}

	@Override
	public String getCaminho() {
		return anexo.getPath();
	}
}
