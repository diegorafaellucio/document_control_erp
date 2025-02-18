package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.vo.ReaproveitamentoDocumentoVO;
import net.wasys.getdoc.domain.vo.ReaproveitamentoMembroFamiliarVO;
import net.wasys.getdoc.domain.vo.ReaproveitamentoProcessoVO;
import net.wasys.getdoc.domain.vo.ReaproveitamentoProcessosIsencaoDisciplinasVO;

import java.util.List;

@ApiModel(value = "DadosReaproveitamentoResponse")
public class DadosReaproveitamentoResponse {

	@ApiModelProperty(notes = "ID do processo do aluno no GetDoc Captação")
	private Long processoCaptacaoId;
	@ApiModelProperty(notes = "Dados do processo e da campanha vinculada ao mesmo")
	private ReaproveitamentoProcessoVO dadosProcesso;
	@ApiModelProperty(notes = "Lista com detalhes dos grupos da composição familiar do aluno")
	private List<ReaproveitamentoMembroFamiliarVO> membrosFamiliares;
	@ApiModelProperty(notes = "Lista com detalhes dos documentos e suas imagens")
	private List<ReaproveitamentoDocumentoVO> documentos;
	@ApiModelProperty(notes = "lista de matriculas para transformar processos de isenção de disciplina")
	private List<ReaproveitamentoProcessosIsencaoDisciplinasVO> isencaoDeDisciplinas;


	public Long getProcessoCaptacaoId() {
		return processoCaptacaoId;
	}

	public void setProcessoCaptacaoId(Long processoCaptacaoId) {
		this.processoCaptacaoId = processoCaptacaoId;
	}

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

	public List<ReaproveitamentoProcessosIsencaoDisciplinasVO> getIsencaoDeDisciplinas() {
		return isencaoDeDisciplinas;
	}

	public void setIsencaoDeDisciplinas(List<ReaproveitamentoProcessosIsencaoDisciplinasVO> isencaoDeDisciplinas) {
		this.isencaoDeDisciplinas = isencaoDeDisciplinas;
	}


	@Override public String toString() {
	return "DadosReaproveitamentoResponse{" +
			"processoCaptacaoId=" + processoCaptacaoId +
			", dadosProcesso=" + dadosProcesso +
			", membrosFamiliares=" + membrosFamiliares +
			", documentos=" + documentos +
			", isencaoDeDisciplinas=" + isencaoDeDisciplinas +
			'}';
}
}
