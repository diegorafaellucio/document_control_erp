package net.wasys.getdoc.domain.vo;

import java.util.List;

public class DadosReaproveitamentoVO {

	private Long processoCaptacaoId;
	private ReaproveitamentoProcessoVO dadosProcesso;
	private List<ReaproveitamentoMembroFamiliarVO> membrosFamiliares;
	private List<ReaproveitamentoDocumentoVO> documentos;

	public void setDadosProcesso(ReaproveitamentoProcessoVO dadosProcesso) {
		this.dadosProcesso = dadosProcesso;
	}

	public ReaproveitamentoProcessoVO getDadosProcesso() {
		return dadosProcesso;
	}

	public List<ReaproveitamentoMembroFamiliarVO> getMembrosFamiliares() {
		return membrosFamiliares;
	}

	public void setMembrosFamiliares(List<ReaproveitamentoMembroFamiliarVO> membrosFamiliares) {
		this.membrosFamiliares = membrosFamiliares;
	}

	public List<ReaproveitamentoDocumentoVO> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<ReaproveitamentoDocumentoVO> documentos) {
		this.documentos = documentos;
	}

	@Override public String toString() {
		return "DadosReaproveitamentoVO{" +
				"processoCaptacaoId=" + processoCaptacaoId +
				", dadosProcesso=" + dadosProcesso +
				", membrosFamiliares=" + membrosFamiliares +
				", documentos=" + documentos +
				'}';
	}
}
