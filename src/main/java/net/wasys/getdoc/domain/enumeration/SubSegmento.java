package net.wasys.getdoc.domain.enumeration;

public enum SubSegmento {

	RENAULT_TESTE_DRIVE("160", true),
	RENAULT_NOVOS("161", true),
	RENAULT_USADOS("162", true),
	RENAULT_TC_ZERO("165", true),
	RENAULT_VENDAS_ESPECIAIS("166", true),
	RENAULT_ACORDOS("170", true),
	RENAULT_TAC_ZERO("172", true),
	NISSAN_ECOMMERCE("167", false),
	NISSAN_VENDAS_ESPECIAIS("168", false),
	NISSAN_TC_ZERO("169", false),
	NISSAN_ACORDOS("171", false),
	NISSAN_TAC_ZERO("173", false),
	NISSAN_NOVOS("163", false),
	NISSAN_USADOS("164", false);

	private String codigo;
	private boolean renault;

	SubSegmento(String codigo, boolean renault) {
		this.codigo = codigo;
		this.renault = renault;
	}

	public String getCodigo() {
		return codigo;
	}

	public boolean isRenault() {
		return renault;
	}

	public static SubSegmento getByCodigo(String codigo) {
		SubSegmento[] values = values();
		for (SubSegmento subSegmento : values) {
			String codigo2 = subSegmento.getCodigo();
			if(codigo2.equals(codigo)) {
				return subSegmento;
			}
		}
		return null;
	}
}
