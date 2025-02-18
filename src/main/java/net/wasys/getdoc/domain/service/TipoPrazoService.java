package net.wasys.getdoc.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import net.wasys.util.other.HorasUteisCalculator;

@Service
public class TipoPrazoService {

	@Autowired private ParametroService parametroService;

	public BigDecimal converterPrazoParaHorasDecimal(BigDecimal prazo, TipoPrazo tipoPrazo) {

		BigDecimal horasExpediente = buscarHorasExpediente();

		if(TipoPrazo.DIAS.equals(tipoPrazo)) {
			prazo = horasExpediente.multiply(prazo);
		}
		else if(TipoPrazo.MINUTOS.equals(tipoPrazo)) {
			return prazo.divide(new BigDecimal(60), 3, RoundingMode.CEILING);
		}

		return prazo;
	}

	public BigDecimal calcularPrazo(BigDecimal horasPrazo, TipoPrazo tipoPrazo) {

		BigDecimal hora = horasPrazo;

		if (TipoPrazo.DIAS.equals(tipoPrazo)) {
			BigDecimal horasExpediente = buscarHorasExpediente();
			BigDecimal prazoEmDias = horasPrazo.divide(horasExpediente, 2, RoundingMode.HALF_UP);
			hora = prazoEmDias;
		} else if (TipoPrazo.MINUTOS.equals(tipoPrazo)) {
			hora = horasPrazo.multiply(new BigDecimal(60));
		}

		return hora;
	}

	public String formatarPrazo(Double horasPrazo, TipoPrazo tipoPrazo) {

		if(horasPrazo == null) {
			return "";
		}

		String horaPrazoFormatado = String.valueOf(horasPrazo);

		if(TipoPrazo.DIAS.equals(tipoPrazo)) {
			BigDecimal prazoBD = new BigDecimal(horasPrazo);
			BigDecimal horasExpediente = buscarHorasExpediente();
			BigDecimal prazoEmDias = prazoBD.divide(horasExpediente, RoundingMode.CEILING);

			horaPrazoFormatado = prazoEmDias + "d";
		}
		else if(TipoPrazo.HORAS.equals(tipoPrazo)) {
			horaPrazoFormatado = horasPrazo.intValue() + "h";
		}
		else if(TipoPrazo.MINUTOS.equals(tipoPrazo)) {
			horaPrazoFormatado = Math.round(horasPrazo * 60) + "min";
		}

		return horaPrazoFormatado;
	}

	private BigDecimal buscarHorasExpediente() {

		String[] expedienteArray = parametroService.getExpediente();
		HorasUteisCalculator.Expediente expediente = new HorasUteisCalculator.Expediente(expedienteArray);
		return expediente.getHoras();
	}
}