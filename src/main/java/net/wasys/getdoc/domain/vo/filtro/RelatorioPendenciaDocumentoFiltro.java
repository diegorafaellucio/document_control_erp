package net.wasys.getdoc.domain.vo.filtro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.AlunoVO;

public class RelatorioPendenciaDocumentoFiltro implements Serializable {

	public static final int CSV = 1;
	public static final int EXCEL = 2;

	public enum Tipo {
        /*FIES,
        SIS_PROUNI,*/
        TODOS;
    }

    private List<String> regionais = new ArrayList<>();
    private List<String> campus;
    private List<String> curso;
    private String modalidadeEnsino;
    private String formaIngresso;
    private AlunoVO aluno;
    private String cpf;
    private String matricula;
    private String numCandidato;
    private String numInscricao;
    private Boolean apenasPendentes;
    private Tipo tipo;
	private Boolean poloParceiro;
	private List<Long> situacoesIds;
    private Boolean situacaoAluno;
    private List<String> periodosIngresso;
    private boolean pagina;
	private List<Long> tiposProcessoIds;
	private Boolean regionalVazia;
	private List<String> todasRegionais;
	private List<StatusProcesso> statusProcessos;
	private Date inicioDataFinalizacaoAnalise;
	private boolean dehMenosUm;
	private boolean sisFiesOuSisProuni;

    public List<String> getCampus() {
        return campus;
    }

    public void setCampus(List<String> campus) {
        this.campus = campus;
    }

    public List<String> getCurso() {
        return curso;
    }

    public void setCurso(List<String> curso) {
        this.curso = curso;
    }

    public String getModalidadeEnsino() {
		return modalidadeEnsino;
	}

	public void setModalidadeEnsino(String modalidadeEnsino) {
		this.modalidadeEnsino = modalidadeEnsino;
	}

	public String getFormaIngresso() {
		return formaIngresso;
	}

	public void setFormaIngresso(String formaIngresso) {
		this.formaIngresso = formaIngresso;
	}

	public AlunoVO getAluno() {
        return aluno;
    }

    public void setAluno(AlunoVO aluno) {
        this.aluno = aluno;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Boolean getApenasPendentes() {
        return apenasPendentes;
    }

    public void setApenasPendentes(Boolean apenasPendentes) {
        this.apenasPendentes = apenasPendentes;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

	public Boolean getPoloParceiro() {
		return poloParceiro;
	}

	public void setPoloParceiro(Boolean poloParceiro) {
		this.poloParceiro = poloParceiro;
	}

	public String getNumCandidato() {
		return numCandidato;
	}

	public void setNumCandidato(String numCandidato) {
		this.numCandidato = numCandidato;
	}


	public String getNumInscricao() {
		return numInscricao;
	}

	public void setNumInscricao(String numInscricao) {
		this.numInscricao = numInscricao;
	}

    public List<Long> getSituacoesIds() {
        return situacoesIds;
    }

    public void setSituacoesIds(List<Long> situacoesIds) {
        this.situacoesIds = situacoesIds;
    }

	public Boolean getSituacaoAluno() {
		return situacaoAluno;
	}

	public void setSituacaoAluno(Boolean situacaoAluno) {
		this.situacaoAluno = situacaoAluno;
	}

	public List<String> getPeriodosIngresso() {
		return periodosIngresso;
	}

	public void setPeriodosIngresso(List<String> periodosIngresso) {
		this.periodosIngresso = periodosIngresso;
	}

	public boolean isPagina() {
		return pagina;
	}

	public void setPagina(boolean pagina) {
		this.pagina = pagina;
	}

	public List<String> getRegionais() {
		return regionais;
	}

	public void setRegionais(List<String> regionais) {
		this.regionais = regionais;
	}

	public List<Long> getTiposProcessoIds() {
		return tiposProcessoIds;
	}

	public void setTiposProcessoIds(List<Long> tiposProcessoIds) {
		this.tiposProcessoIds = tiposProcessoIds;
	}

	public Boolean getRegionalVazia() {
		return regionalVazia;
	}

	public void setRegionalVazia(Boolean regionalVazia) {
		this.regionalVazia = regionalVazia;
	}

	public List<String> getTodasRegionais() {
		return todasRegionais;
	}

	public void setTodasRegionais(List<String> todasRegionais) {
		this.todasRegionais = todasRegionais;
	}

	public List<StatusProcesso> getStatusProcessos() {
		return statusProcessos;
	}

	public void setStatusProcessos(List<StatusProcesso> statusProcessos) {
		this.statusProcessos = statusProcessos;
	}

	public Date getInicioDataFinalizacaoAnalise() {
		return inicioDataFinalizacaoAnalise;
	}

	public void setInicioDataFinalizacaoAnalise(Date inicioDataFinalizacaoAnalise) {
		this.inicioDataFinalizacaoAnalise = inicioDataFinalizacaoAnalise;
	}

	public boolean isDehMenosUm() {
		return dehMenosUm;
	}

	public void setDehMenosUm(boolean dehMenosUm) {
		this.dehMenosUm = dehMenosUm;
	}

	public boolean isSisFiesOuSisProuni() {
		return sisFiesOuSisProuni;
	}

	public void setSisFiesOuSisProuni(boolean sisFiesOuSisProuni) {
		this.sisFiesOuSisProuni = sisFiesOuSisProuni;
	}

	@Override
	public String toString() {
		return "RelatorioPendenciaDocumentoFiltro{" +
				"regionais=" + regionais +
				", campus=" + campus +
				", curso=" + curso +
				", modalidadeEnsino='" + modalidadeEnsino + '\'' +
				", formaIngresso='" + formaIngresso + '\'' +
				", aluno=" + aluno +
				", cpf='" + cpf + '\'' +
				", matricula='" + matricula + '\'' +
				", numCandidato='" + numCandidato + '\'' +
				", numInscricao='" + numInscricao + '\'' +
				", apenasPendentes=" + apenasPendentes +
				", tipo=" + tipo +
				", poloParceiro=" + poloParceiro +
				", situacoesIds=" + situacoesIds +
				", situacaoAluno=" + situacaoAluno +
				", periodosIngresso=" + periodosIngresso +
				", pagina=" + pagina +
				", tiposProcessoIds=" + tiposProcessoIds +
				", regionalVazia=" + regionalVazia +
				", todasRegionais=" + todasRegionais +
				", statusProcessos=" + statusProcessos +
				", inicioDataFinalizacaoAnalise=" + inicioDataFinalizacaoAnalise +
				", dehMenosUm=" + dehMenosUm +
				", sisFiesOuSisProuni=" + sisFiesOuSisProuni +
				'}';
	}
}
