package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="RequestListaDocumentosGraduacao")
public class RequestListaDocumentosGraduacao {

    @ApiModelProperty(notes="Código do Aluno (número da inscrição ou número do candidato).", required = true)
    private String alunoCod;

    @ApiModelProperty(notes="ID do Tipo de Aluno (1 = Inscrição; 2 = Candidato).", required = true)
    private Long tipoAlunoId;

	@ApiModelProperty(notes="ID da Classificação do Processo (1 = Vestibular; 2 = TE; 3 = MSV Externa; 5 = MSV Interna; 7 = Enem, 15 = Pós Graduação).", required = true)
	private Long classificacaoProcessoId;

	public String getAlunoCod() {
		return alunoCod;
	}

	public void setAlunoCod(String alunoCod) {
		this.alunoCod = alunoCod;
	}

	public Long getTipoAlunoId() {
		return tipoAlunoId;
	}

	public void setTipoAlunoId(Long tipoAlunoId) {
		this.tipoAlunoId = tipoAlunoId;
	}

	public Long getClassificacaoProcessoId() {
		return classificacaoProcessoId;
	}

	public void setClassificacaoProcessoId(Long classificacaoProcessoId) {
		this.classificacaoProcessoId = classificacaoProcessoId;
	}
}
