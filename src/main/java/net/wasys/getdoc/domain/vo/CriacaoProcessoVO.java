package net.wasys.getdoc.domain.vo;

import java.util.Collection;
import java.util.Map;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.Origem;

public class CriacaoProcessoVO {

	private TipoProcesso tipoProcesso;
	private Usuario usuario;
	private Aluno aluno;
	private Collection<CampoAbstract> valoresCampos;
	private AcaoProcesso acao;
	private Processo processo;
	private Map<TipoDocumento, DigitalizacaoVO> digitalizacao;
	private Origem origem;
	private Usuario analista;
	private Processo processoOriginal;

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Collection<CampoAbstract> getValoresCampos() {
		return valoresCampos;
	}

	public void setValoresCampos(Collection<CampoAbstract> valoresCampos) {
		this.valoresCampos = valoresCampos;
	}

	public AcaoProcesso getAcao() {
		return acao;
	}

	public void setAcao(AcaoProcesso acao) {
		this.acao = acao;
	}

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}
	
	public Map<TipoDocumento, DigitalizacaoVO> getDigitalizacao() {
		return digitalizacao;
	}

	public void setDigitalizacao(Map<TipoDocumento, DigitalizacaoVO> digitalizacao) {
		this.digitalizacao = digitalizacao;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public Usuario getAnalista() {
		return analista;
	}

	public void setAnalista(Usuario analista) {
		this.analista = analista;
	}

	public Processo getProcessoOriginal() {
		return processoOriginal;
	}

	public void setProcessoOriginal(Processo processoOriginal) {
		this.processoOriginal = processoOriginal;
	}
}
