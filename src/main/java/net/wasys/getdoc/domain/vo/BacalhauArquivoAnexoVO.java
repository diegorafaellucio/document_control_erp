package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoArquivoBacalhau;

import java.util.Date;

public abstract class BacalhauArquivoAnexoVO implements BacalhauArquivoVO {

	private ProcessoLogAnexo anexo;

	public BacalhauArquivoAnexoVO(ProcessoLogAnexo anexo) {
		this.anexo = anexo;
	}

	@Override
	public TipoArquivoBacalhau getTipoArquivo() {

		ProcessoLog processoLog = anexo.getProcessoLog();
		EmailEnviado emailEnviado = processoLog.getEmailEnviado();
		if (emailEnviado != null) {
			return TipoArquivoBacalhau.EMAIL_ENVIADO;
		}

		ProcessoPendencia pendencia = processoLog.getPendencia();
		if (pendencia != null) {
			return TipoArquivoBacalhau.PENDENCIA;
		}

		Solicitacao solicitacao = processoLog.getSolicitacao();
		if (solicitacao != null) {
			return TipoArquivoBacalhau.SOLICITACAO;
		}

		return TipoArquivoBacalhau.ANEXO;
	}

	@Override
	public Date getDataRegistro() {
		ProcessoLog processoLog = anexo.getProcessoLog();
		return processoLog.getData();
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
