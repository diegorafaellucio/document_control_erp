package net.wasys.getdoc.domain.enumeration;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public enum StatusDocumento {

	INCLUIDO,
	EXCLUIDO,
	DIGITALIZADO,
	PENDENTE,
	APROVADO,
	PROCESSANDO,
	;

	public static List<StatusDocumento> statusParaValores(java.util.List<String> valores) {

		List<StatusDocumento> status = new ArrayList<>();

		if (isEmpty(valores)) {
			return status;
		}

		for (String val : valores) {

			StatusDocumento statusDocumento = StatusDocumento.valueOf(val);
			status.add(statusDocumento);
		}

		return status;
	}
}
