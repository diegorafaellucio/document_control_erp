package net.wasys.getdoc.domain.enumeration;

public enum StatusDocumentoPortal {

	NAO_ENTREGUE(StatusDocumento.INCLUIDO, StatusDocumento.EXCLUIDO),
	ENTREGUE(StatusDocumento.DIGITALIZADO),
	REPROVADO(StatusDocumento.PENDENTE),
	APROVADO(StatusDocumento.APROVADO);

	private StatusDocumento[] statusDocumento;

	StatusDocumentoPortal(StatusDocumento... statusDocumento){
		this.statusDocumento = statusDocumento;
	}

	public static StatusDocumentoPortal getByStatus(StatusDocumento statusDocumento){
		StatusDocumentoPortal[] statusPortal = StatusDocumentoPortal.values();
		for(StatusDocumentoPortal key : statusPortal){
			StatusDocumento[] statuss = key.statusDocumento;
			for (StatusDocumento status : statuss) {
				if(status.equals(statusDocumento)) {
					return key;
				}
			}
		}
		return null;
	}
}
