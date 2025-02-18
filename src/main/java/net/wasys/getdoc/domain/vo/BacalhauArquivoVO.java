package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoArquivoBacalhau;

import java.util.Date;

public interface BacalhauArquivoVO {

	public TipoArquivoBacalhau getTipoArquivo();

	public Date getDataRegistro();

	public Long getRegistroId();

	public String getHashChecksum();

	public String getCaminho();

	public String criaCaminho();
}
