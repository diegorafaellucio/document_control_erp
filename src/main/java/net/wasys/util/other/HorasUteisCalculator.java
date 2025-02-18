package net.wasys.util.other;

import net.wasys.util.DummyUtils;
import org.apache.commons.lang.time.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Felipe Maschio
 * @created 19/10/2010
 */
public class HorasUteisCalculator {

	private static BigDecimal zero = new BigDecimal("0");
	private static BigDecimal minutosHora = new BigDecimal("60");
	private static BigDecimal segundosHora = new BigDecimal("3600");
	private static BigDecimal milissegundosHora = new BigDecimal("3600000");

	private final Expediente expediente;
	private final BigDecimal maxEsperado;
	private List<Date> feriados;

	public HorasUteisCalculator(Expediente expediente, BigDecimal max) {
		this.expediente = expediente;
		this.maxEsperado = max;
	}

	public void setFeriados(List<Date> feriados) {
		this.feriados = feriados;
	}

	public Expediente getExpediente() {
		return expediente;
	}

	public void addParalizacao(List<Date> datas){
		feriados.addAll(datas);
	}

	public void removeParalizacao(List<Date> datas){
		feriados.removeAll(datas);
	}

	public BigDecimal getHorasExpediente() {
		return expediente.getHoras();
	}

	public BigDecimal getHoras(Date dataInicio, Date dataFim) {

		boolean inverso = dataInicio.after(dataFim);
		if(inverso) {
			Date aux = dataInicio;
			dataInicio = dataFim;
			dataFim = aux;
		}

		BigDecimal horasUteis = zero;

		Calendar inicio = Calendar.getInstance();
		inicio.setTime(dataInicio);

		Calendar fim = Calendar.getInstance();
		fim.setTime(dataFim);

		BigDecimal horaInicio = getHora(inicio);

		if(horaInicio.compareTo(expediente.chegadaManha) < 0){
			horaInicio = expediente.chegadaManha;
		}

		//se começa e termina no mesmo dia
		if(DateUtils.isSameDay(inicio, fim)){

			if(isDiaUtil(inicio)) {
				BigDecimal horaFinal = getHora(fim);
				BigDecimal aux = calculaHorasUteis(horaInicio, horaFinal, expediente);
				horasUteis = horasUteis.add(aux);
			}
		}
		else{
			if(isDiaUtil(inicio) && horaInicio.compareTo(expediente.chegadaManha) >= 0){
				BigDecimal aux = calculaHorasUteis(horaInicio, expediente.saidaTarde, expediente);
				horasUteis = horasUteis.add(aux);
			}

			do{
				if(maxEsperado != null && horasUteis.compareTo(maxEsperado) > 0){
					break;
				}

				inicio.add(Calendar.DAY_OF_MONTH, 1);

				//se nao eh feriado nem final de semana
				if(isDiaUtil(inicio)){

					//se as datas forem no mesmo dia, calcula as horas uteis ateh o fim (hora) da ultima data
					if(DateUtils.isSameDay(inicio, fim)){

						BigDecimal horaFinal = getHora(fim);
						BigDecimal aux = calculaHorasUteis(expediente.chegadaManha, horaFinal, expediente);
						horasUteis = horasUteis.add(aux);
					}
					else{
						BigDecimal aux = calculaHorasUteis(expediente.chegadaManha, expediente.saidaTarde, expediente);
						horasUteis = horasUteis.add(aux);
					}
				}
			}
			while (inicio.get(Calendar.YEAR) < fim.get(Calendar.YEAR) || (inicio.get(Calendar.YEAR) == fim.get(Calendar.YEAR) && inicio.get(Calendar.DAY_OF_YEAR) < fim.get(Calendar.DAY_OF_YEAR)));
		}

		return horasUteis.multiply(new BigDecimal(inverso ? -1 : 1));
	}

	public boolean isDiaUtil(Date data) {
		return isDiaUtil(DateUtils.toCalendar(data));
	}

	public boolean isDiaUtil(Calendar inicio) {

		boolean diaUtil = inicio.get(Calendar.DAY_OF_WEEK) != 1 && inicio.get(Calendar.DAY_OF_WEEK) != 7;

		if(diaUtil && feriados != null) {
			Calendar dia = DateUtils.truncate(inicio, Calendar.DAY_OF_MONTH);
			Date time = dia.getTime();
			diaUtil = !feriados.contains(time);
		}

		return diaUtil;
	}

	public long calculaDiasUteis(Date inicio, Date fim) {
		inicio = net.wasys.getdoc.mb.utils.DateUtils.getFirstTimeOfDay(inicio);
		fim = net.wasys.getdoc.mb.utils.DateUtils.getFirstTimeOfDay(fim);
		long count = 1;

		while (!inicio.equals(fim)){
			if(isDiaUtil(inicio)){
				count++;
			}
			inicio = net.wasys.getdoc.mb.utils.DateUtils.getFirstTimeOfDay(DateUtils.addDays(inicio, 1));
		}

		return count;
	}

	public static BigDecimal calculaHorasUteis(BigDecimal horaInicio, BigDecimal horaFim, Expediente expediente) {

		if(horaInicio.compareTo(horaFim) > 0){
			return zero;
		}

		BigDecimal horasUteis = zero;

		if(horaInicio.compareTo(expediente.chegadaManha) < 0){
			horaInicio = expediente.chegadaManha;
		}

		if(horaInicio.compareTo(expediente.saidaManha) >= 0 && horaInicio.compareTo(expediente.chegadaTarde) < 0){
			horaInicio = expediente.chegadaTarde;
		}
		else if(horaInicio.compareTo(expediente.saidaTarde) > 0){
			horaInicio = expediente.saidaTarde;
		}

		if(horaFim.compareTo(expediente.saidaManha) > 0 && horaFim.compareTo(expediente.chegadaTarde) < 0){
			horaFim = expediente.saidaManha; 
		}
		else if(horaFim.compareTo(expediente.saidaTarde) > 0){
			horaFim = expediente.saidaTarde;
		}

		BigDecimal aux = horaFim.subtract(horaInicio);
		horasUteis = horasUteis.add(aux);

		//tira a hora de almoço
		if(horaInicio.compareTo(expediente.saidaManha) < 0 && horaFim.compareTo(expediente.chegadaTarde) > 0){
			BigDecimal aux2 = expediente.chegadaTarde.subtract(expediente.saidaManha);
			horasUteis = horasUteis.subtract(aux2);
		}

		horasUteis = horasUteis.compareTo(zero) < 0 ? zero : horasUteis;

		return horasUteis;
	}

	private static BigDecimal getHora(Calendar c) {

		BigDecimal hora = new BigDecimal(c.get(Calendar.HOUR_OF_DAY));
		BigDecimal minuto = new BigDecimal(c.get(Calendar.MINUTE));
		BigDecimal segundo = new BigDecimal(c.get(Calendar.SECOND));
		BigDecimal millisecond = new BigDecimal(c.get(Calendar.MILLISECOND));

		return getHora(hora, minuto, segundo, millisecond);
	}

	private static BigDecimal getHora(BigDecimal hora, BigDecimal minuto) {
		return getHora(hora, minuto, null, null);
	}

	private static BigDecimal getHora(BigDecimal hora, BigDecimal minuto, BigDecimal segundo, BigDecimal millisecond) {

		BigDecimal aux = divide(minuto, minutosHora);
		BigDecimal result = hora.add(aux);

		if(segundo != null) {
			aux = divide(segundo, segundosHora);
			result = result.add(aux);
		}

		if(millisecond != null) {
			aux = divide(millisecond, milissegundosHora);
			result = result.add(aux);
		}

		return result;
	}

	private static BigDecimal divide(BigDecimal tanto, BigDecimal por) {
		return tanto.divide(por, 10, RoundingMode.CEILING);
	}

	public Date addHoras(Date data, BigDecimal horasBD) {

		Calendar c = Calendar.getInstance();
		c.setTime(data);
		c = DateUtils.truncate(c, Calendar.MINUTE);

		boolean soma = horasBD.compareTo(zero) > 0;

		while(soma ? horasBD.compareTo(zero) > 0 : horasBD.compareTo(zero) < 0){

			BigDecimal hora = getHora(c);

			if(hora.compareTo(expediente.chegadaManha) < 0) {

				//vai pro começo da manhã
				BigDecimal chegadaManha = expediente.chegadaManha;
				BigDecimal horaManha = chegadaManha.setScale(0, RoundingMode.DOWN);
				BigDecimal minutoManha = chegadaManha.subtract(horaManha);
				minutoManha = minutoManha.multiply(minutosHora);
				minutoManha = minutoManha.setScale(0, RoundingMode.CEILING);
				c.set(Calendar.HOUR_OF_DAY, horaManha.intValue());
				c.set(Calendar.MINUTE, minutoManha.intValue());
				hora = getHora(c);
			}
			else if(hora.compareTo(expediente.saidaManha) >= 0 && hora.compareTo(expediente.chegadaTarde) < 0) {

				//vai pro começo da tarde
				BigDecimal chegadaTarde = expediente.chegadaTarde;
				BigDecimal horaTarde = chegadaTarde.setScale(0, RoundingMode.DOWN);
				BigDecimal minutoTarde = chegadaTarde.subtract(horaTarde);
				minutoTarde = minutoTarde.multiply(minutosHora);
				minutoTarde = minutoTarde.setScale(0, RoundingMode.CEILING);
				c.set(Calendar.HOUR_OF_DAY, horaTarde.intValue());
				c.set(Calendar.MINUTE, minutoTarde.intValue());
				hora = getHora(c);
			}
			else if(hora.compareTo(expediente.saidaTarde) >= 0) {

				avancarDia(c);
				hora = getHora(c);
			}

			if(!isDiaUtil(c)) {
				avancarDia(c);
			}
			else {

				if(expediente.isHorarioExpedienteManha(hora)) {

					BigDecimal horasManha = expediente.saidaManha.subtract(hora);

					if(horasManha.compareTo(horasBD) > 0) {
						BigDecimal minutos = horasBD.multiply(minutosHora);
						minutos = minutos.setScale(0, RoundingMode.HALF_EVEN);
						c.add(Calendar.MINUTE, minutos.intValue());
						break;
					}

					BigDecimal minutosManha = horasManha.multiply(minutosHora);
					minutosManha = minutosManha.setScale(0, RoundingMode.HALF_EVEN);

					horasBD = horasBD.subtract(horasManha);
					c.add(Calendar.MINUTE, minutosManha.intValue());
				}

				if(expediente.isHorarioExpedienteTarde(hora)) {

					BigDecimal horasTarde = expediente.saidaTarde.subtract(hora);
					if(horasTarde.compareTo(horasBD) > 0) {
						BigDecimal minutos = horasBD.multiply(minutosHora);
						minutos = minutos.setScale(0, RoundingMode.HALF_EVEN);
						c.add(Calendar.MINUTE, minutos.intValue());
						break;
					}

					if(horasTarde.compareTo(horasBD) > 0) {
						horasTarde = horasBD;
					}

					BigDecimal minutosTarde = horasTarde.multiply(minutosHora);
					minutosTarde = minutosTarde.setScale(0, RoundingMode.HALF_EVEN);

					horasBD = horasBD.subtract(horasTarde);
					c.add(Calendar.MINUTE, minutosTarde.intValue());
				}
			}
		}

		return c.getTime();
	}

	private void avancarDia(Calendar c) {

		//vai pro começo da manhã do próximo dia
		BigDecimal chegadaManha = expediente.chegadaManha;
		BigDecimal horaManha = chegadaManha.setScale(0, RoundingMode.DOWN);
		BigDecimal minutoManha = chegadaManha.subtract(horaManha);
		minutoManha = minutoManha.multiply(minutosHora);
		minutoManha = minutoManha.setScale(0, RoundingMode.CEILING);
		c.set(Calendar.HOUR_OF_DAY, horaManha.intValue());
		c.set(Calendar.MINUTE, minutoManha.intValue());
		c.add(Calendar.DAY_OF_MONTH, 1);
	}

	public  BigDecimal getJornadaExpediente() {

		BigDecimal chegadaManha = expediente.chegadaManha;
		BigDecimal saidaManha = expediente.saidaManha;
		BigDecimal chegadaTarde = expediente.chegadaTarde;
		BigDecimal saidaTarde = expediente.saidaTarde;

		int i = chegadaManha.intValue();
		int i1 = saidaManha.intValue();
		int i2 = chegadaTarde.intValue();
		int i3 = saidaTarde.intValue();

		int result = (i1 - i) + (i3 - i2);

		BigDecimal bd = BigDecimal.valueOf(result);

		return bd;
	}

	public static class Expediente {

		private static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");

		public final BigDecimal chegadaManha;
		public final BigDecimal saidaManha;
		public final BigDecimal chegadaTarde;
		public final BigDecimal saidaTarde;

		public Expediente(String[] horarios) {

			BigDecimal horaChegadaManha = new BigDecimal(horarios[0].substring(0, 2));
			BigDecimal minutoChegadaManha = new BigDecimal(horarios[0].substring(3, 5));
			BigDecimal horaSaidaManha = new BigDecimal(horarios[1].substring(0, 2));
			BigDecimal minutoSaidaManha = new BigDecimal(horarios[1].substring(3, 4));
			BigDecimal horaChegadaTarde = new BigDecimal(horarios[2].substring(0, 2));
			BigDecimal minutoChegadaTarde = new BigDecimal(horarios[2].substring(3, 5));
			BigDecimal horaSaidaTarde = new BigDecimal(horarios[3].substring(0, 2));
			BigDecimal minutoSaidaTarde = new BigDecimal(horarios[3].substring(3, 5));

			chegadaManha = getHora(horaChegadaManha, minutoChegadaManha);
			saidaManha = getHora(horaSaidaManha, minutoSaidaManha);
			chegadaTarde = getHora(horaChegadaTarde, minutoChegadaTarde);
			saidaTarde = getHora(horaSaidaTarde, minutoSaidaTarde);
		}

		public boolean isHorarioExpediente(Date date) {
			return isHorarioExpediente(date, true);
		}

		public boolean isHorarioExpediente(Date date, boolean verificarFimSemana) {

			if(verificarFimSemana) {
				Calendar c = DateUtils.toCalendar(date);
				boolean diaUtil = c.get(Calendar.DAY_OF_WEEK) != 1 && c.get(Calendar.DAY_OF_WEEK) != 7;
				if(!diaUtil) {
					return false;
				}
			}

			String dataStr = SDF.format(date);
			BigDecimal horaBD = DummyUtils.getHoras(dataStr);

			return isHorarioExpediente(horaBD);
		}

		private boolean isHorarioExpediente(BigDecimal horaBD) {
			if(isHorarioExpedienteManha(horaBD)) {
				return true;
			}
			if(isHorarioExpedienteTarde(horaBD)) {
				return true;
			}
			return false;
		}

		private boolean isHorarioExpedienteManha(BigDecimal horaBD) {
			if(horaBD.compareTo(chegadaManha)  >= 0 && horaBD.compareTo(saidaManha) <= 0) {
				return true;
			}
			return false;
		}

		private boolean isHorarioExpedienteTarde(BigDecimal horaBD) {
			if(horaBD.compareTo(chegadaTarde) >= 0 && horaBD.compareTo(saidaTarde) <= 0) {
				return true;
			}
			return false;
		}

		public BigDecimal getHoras() {

			BigDecimal horas = saidaManha.subtract(chegadaManha).add(saidaTarde).subtract(chegadaTarde);
			return horas;
		}
	}

	@Override public String toString() {
		return "HorasUteisCalculator{" +
				"maxEsperado=" + maxEsperado +
				", feriados=" + feriados +
				'}';
	}
}
