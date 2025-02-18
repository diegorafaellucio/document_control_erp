package net.wasys.getdoc.domain.vo.filtro;

public class RelatorioAtividadeFiltro extends ProcessoLogFiltro {

	public enum Tipo {
		ANALITICO,
		SINTETICO
	}

	private Tipo tipo;

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}
}
