package net.wasys.getdoc.domain.vo.filtro;
import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.TipoDocumento;

public class IrregularidadeFiltro  {

	private String nome;
	private Boolean irregularidadePastaAmarela;
	private boolean ativa;
	private TipoDocumento tipoDocumentoEscolhido;
	private Long idProcessoEscolhido;
	private PastaAmarela pastaAmarela;

	public enum PastaAmarela {
		SIM,
		NAO,
		AMBOS;
	}


	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Boolean isIrregularidadePastaAmarela() {
		return irregularidadePastaAmarela;
	}

	public void setIrregularidadePastaAmarela(Boolean irregularidadePastaAmarela) {
		this.irregularidadePastaAmarela = irregularidadePastaAmarela;
	}

	public TipoDocumento getTipoDocumentoEscolhido() {
		return tipoDocumentoEscolhido;
	}

	public void setTipoDocumentoEscolhido(TipoDocumento tipoDocumentoEscolhido) {
		this.tipoDocumentoEscolhido = tipoDocumentoEscolhido;
	}
	public Long getIdProcessoEscolhido() {
		return idProcessoEscolhido;
	}

	public void setIdProcessoEscolhido(Long idProcesoEscolhido) {
		this.idProcessoEscolhido = idProcesoEscolhido;
	}

	public boolean isAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	public PastaAmarela getPastaAmarela() {
		return pastaAmarela;
	}

	public void setPastaAmarela(PastaAmarela ativa) {
		this.pastaAmarela = ativa;
	}
}

