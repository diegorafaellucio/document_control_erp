package net.wasys.getdoc.domain.enumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public enum StatusProcesso {

	RASCUNHO,
	AGUARDANDO_ANALISE,
	EM_ANALISE,
	PENDENTE,
	EM_ACOMPANHAMENTO,
	ENCAMINHADO,
	CONCLUIDO,
	CANCELADO;

	private static List<StatusProcesso> pendenciaAnalista = Arrays.asList(EM_ANALISE, PENDENTE, EM_ACOMPANHAMENTO);
	private static List<StatusProcesso> pendenciaRequisitante = Arrays.asList(RASCUNHO, PENDENTE);
	private static List<StatusProcesso> statusEmAndamento = Arrays.asList(EM_ANALISE, EM_ACOMPANHAMENTO);
	private static List<StatusProcesso> statusFechado = Arrays.asList(CONCLUIDO, CANCELADO);
	private static List<StatusProcesso> statusFimAnalise = Arrays.asList(EM_ACOMPANHAMENTO, CONCLUIDO, CANCELADO);
	private static List<StatusProcesso> todosStatus = Arrays.asList(RASCUNHO, AGUARDANDO_ANALISE, EM_ANALISE, PENDENTE, EM_ACOMPANHAMENTO, ENCAMINHADO, CONCLUIDO, CANCELADO);

	public static List<StatusProcesso> getPendenciaAnalista() {
		return pendenciaAnalista;
	}

	public static List<StatusProcesso> getPendenciaRequisitante() {
		return pendenciaRequisitante;
	}

	public static List<StatusProcesso> getStatusEmAndamento() {
		return statusEmAndamento;
	}

	public static List<StatusProcesso> getStatusFechado() {
		return statusFechado;
	}

	public static List<StatusProcesso> getStatusFimAnalise() {
		return statusFimAnalise;
	}

	public static List<StatusProcesso> getTodosStatus() {
		return todosStatus;
	}

	public static List<StatusProcesso> statusParaValores(List<String> valores) {

		List<StatusProcesso> status = new ArrayList<>();

		if (isEmpty(valores)) {
			return status;
		}

		for (String val : valores) {

			StatusProcesso statusProcesso = StatusProcesso.valueOf(val);
			status.add(statusProcesso);
		}

		return status;
	}
}
