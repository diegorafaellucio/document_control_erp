package net.wasys.util.other;

import lombok.extern.slf4j.Slf4j;
import net.wasys.util.DummyUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HorasUteisCalculatorTest {

	@Test
	public void teste1 () {

		HorasUteisCalculator.Expediente expediente = new HorasUteisCalculator.Expediente(new String[]{"09:00", "12:00", "12:00", "17:00"});
		HorasUteisCalculator huc = new HorasUteisCalculator(expediente, null);

	}

	@Test
	public void teste2 () {

		long inicio = System.currentTimeMillis();

		HorasUteisCalculator.Expediente expediente = new HorasUteisCalculator.Expediente(new String[]{"09:00", "12:00", "13:00", "18:00"});
		HorasUteisCalculator huc = new HorasUteisCalculator(expediente, null);

		int count = 100;
		for (int i = 0; i < count; i++) {

			DummyUtils.systraceThread("");

			BigDecimal horasExpediente = expediente.getHoras();
			BigDecimal horas1 = huc.getHoras(DummyUtils.parseDateTime("02/09/2014 10:56"), DummyUtils.parseDateTime("02/09/2014 12:27"));
			check(DummyUtils.getHoras(horasExpediente, horas1, false), "01:04h");

			BigDecimal horas2 = huc.getHoras(DummyUtils.parseDateTime("02/09/2014 10:56"), DummyUtils.parseDateTime("02/09/2014 13:26"));
			check(DummyUtils.getHoras(horasExpediente, horas2, false), "01:30h");

			BigDecimal horas3 = huc.getHoras(DummyUtils.parseDateTime("02/09/2014 10:56"), DummyUtils.parseDateTime("03/09/2014 13:26"));
			check(DummyUtils.getHoras(horasExpediente, horas3, false), "09:30h");

			BigDecimal horas4 = huc.getHoras(DummyUtils.parseDateTime("02/09/2014 10:56"), DummyUtils.parseDateTime("03/10/2014 13:26"));
			check(DummyUtils.getHoras(horasExpediente, horas4, false), "185:30h");

			BigDecimal horas5 = huc.getHoras(DummyUtils.parseDateTime("02/09/2014 10:56"), DummyUtils.parseDateTime("03/10/2015 13:26"));
			check(DummyUtils.getHoras(horasExpediente, horas5, false), "2270:04h");

			BigDecimal horas6 = huc.getHoras(DummyUtils.parseDateTime3("02/09/2010 10:56:01:999"), DummyUtils.parseDateTime3("03/10/2015 13:26:01:001"));
			check(DummyUtils.getHoras(horasExpediente, horas6, false), "10614:03h");
		}

		DummyUtils.systraceThread(System.currentTimeMillis() - inicio + "ms. para processar " + count);

		List<Date> feriados = Arrays.asList(DummyUtils.parseDate("14/04/2017"), DummyUtils.parseDate("21/04/2017"), DummyUtils.parseDate("01/05/2017"));
		expediente = new HorasUteisCalculator.Expediente(new String[]{"09:00", "12:00", "12:00", "18:00"});
		huc = new HorasUteisCalculator(expediente, null);
		huc.setFeriados(feriados);

		DummyUtils.systraceThread("");
		DummyUtils.systraceThread("09:00 12:00 12:00 18:00");
		testDecimal(huc, "29/04/2017 09:10", 18, "03/05/2017 18:00");
		testDecimal(huc, "01/05/2017 09:10", 18, "03/05/2017 18:00");
		testDecimal(huc, "02/05/2017 09:10", 18, "04/05/2017 09:10");
		testDecimal(huc, "03/05/2017 09:10", 18, "05/05/2017 09:10");
		testDecimal(huc, "03/05/2017 13:10", 18, "05/05/2017 13:10");
		testDecimal(huc, "03/05/2017 13:10", 1, "03/05/2017 14:10");
		testDecimal(huc, "03/05/2017 18:50", 10, "05/05/2017 10:00");
		testDecimal(huc, "03/05/2017 08:50", 2, "03/05/2017 11:00");

		DummyUtils.systraceThread("");
		DummyUtils.systraceThread("08:00 12:00 13:00 18:30");
		expediente = new HorasUteisCalculator.Expediente(new String[]{"08:00", "12:00", "13:00", "18:30"});
		huc = new HorasUteisCalculator(expediente, null);
		huc.setFeriados(feriados);

		testDecimal(huc, "29/04/2017 09:10", 1, "02/05/2017 09:00");
		testDecimal(huc, "03/05/2017 11:30", 19, "05/05/2017 11:30");
		testDecimal(huc, "08/05/2017 11:37", 19, "10/05/2017 11:37");
		test2Decimal(huc, "08/05/2017 11:37:46", 19, "10/05/2017 11:37");
		test2Decimal(huc, "08/05/2017 11:37:06", 19.25, "10/05/2017 11:52");
		test2Decimal(huc, "08/05/2017 11:37:06", 19.5, "10/05/2017 13:07");
		testDecimal(huc, "11/05/2017 09:25", 19, "15/05/2017 09:25");
		testDecimal(huc, "03/05/2017 13:10", 1.5, "03/05/2017 14:40");
		testDecimal(huc, "03/05/2017 13:10", 5.25, "03/05/2017 18:25");
		testDecimal(huc, "03/05/2017 13:10", 5.5, "04/05/2017 08:10");
		testDecimal(huc, "03/05/2017 13:10", 0.5, "03/05/2017 13:40");

		System.exit(0);
	}

	private static void test2Decimal(HorasUteisCalculator huc, String horaInicio, double addHorasDecimal, String esperado) {
		Date data = DummyUtils.parseDateTime2(horaInicio);
		test(huc, horaInicio, addHorasDecimal, esperado, data);
	}

	private static void testDecimal(HorasUteisCalculator huc, String horaInicio, double addHorasDecimal, String esperado) {
		Date data = DummyUtils.parseDateTime(horaInicio);
		test(huc, horaInicio, addHorasDecimal, esperado, data);
	}

	private static void test(HorasUteisCalculator huc, String horaInicio, double addHorasDecimal, String esperado, Date data) {

		Date horaFim = huc.addHoras(data, new BigDecimal(addHorasDecimal));

		int horas = (int) addHorasDecimal % 60;
		int minutos = (int) (addHorasDecimal * 60) % 60;
		String horasAddStr = horas + ":" + minutos;
		System.out.print(horaInicio + " + " + horasAddStr + "h = ");
		check(DummyUtils.formatDateTime(horaFim), esperado);
	}

	private static void check(String obtido, String esperado) {

		DummyUtils.systraceThread(obtido + " -> " + (obtido.equals(esperado) ? "ok" : "erro!!! esperado " + esperado));
	}
}
