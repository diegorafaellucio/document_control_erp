package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.StatusAtendimento;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.time.DateUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

public class RelatorioStatusLaboralSinteticoVO {

	private List<String> horas;
	private Map<String, List<StatusAtendimentoVO>> statusAtendimentoMap = new TreeMap<>();

	public List<String> getHoras() {
		return horas;
	}

	public void setHoras(List<String> horas) {
		this.horas = horas;
	}

	public Map<String, List<StatusAtendimentoVO>> getStatusAtendimentoMap() {
		return statusAtendimentoMap;
	}

	public void addStatusAtendimentoVO(String key, StatusAtendimentoVO statusAtendimentoVO) {
		List<StatusAtendimentoVO> list = statusAtendimentoMap.get(key);
		list = list != null ? list : new ArrayList<>();
		list.add(statusAtendimentoVO);
		statusAtendimentoMap.put(key, list);
	}

	public List<StatusAtendimentoVO> getStatusAtendimento(String analista) {
		List<StatusAtendimentoVO> list = statusAtendimentoMap.get(analista);
		return list;
	}

	public String getKeyFormatada(String key) {
		String[] split = key.split(" - ");
		return split[1] + " " + split[0];
	}

	public class StatusAtendimentoVO {

		private String analista;
		private Object[] logAtendimento;
		private Date inicio;
		private Date fim;
		private Long processoId;
		private String processoNumero;

		public String getAnalista() {
			return analista;
		}

		public void setAnalista(String analista) {
			this.analista = analista;
		}

		public Long getProcessoId() {
			return processoId;
		}

		public void setProcessoId(Long processoId) {
			this.processoId = processoId;
		}

		public String getProcessoNumero() {
			return processoNumero;
		}

		public void setProcessoNumero(String processoNumero) {
			this.processoNumero = processoNumero;
		}

		public void addLog(StatusAtendimento statusAtendimento, String nomeStatusLaboral, Date dataInicio, Date dataFim, Long tempo) {
			inicio = dataInicio;
			fim = dataFim;
			logAtendimento = (new Object[]{statusAtendimento, nomeStatusLaboral, DummyUtils.format(dataInicio, "HH:mm"), DummyUtils.format(dataFim, "HH:mm"), DummyUtils.formatarMilisegundosParaHoraMinutoSegundo(tempo)});
		}

		public Date getInicio() {
			return inicio;
		}

		public Date getFim() {
			return fim;
		}

		public String getHoraInicio() {
			return DummyUtils.formatTime(inicio);
		}

		public String getHoraFim() {
			return DummyUtils.formatTime(fim);
		}

		public int getMinutosDesdeInicio() {

			Date inicio = getInicio();
			String horaInicio = horas.get(0);
			String[] split = horaInicio.split(":");
			inicio = DateUtils.setHours(inicio, Integer.parseInt(split[0]));
			inicio = DateUtils.setMinutes(inicio, Integer.parseInt(split[1]));
			inicio = DateUtils.truncate(inicio, Calendar.MINUTE);

			Date inicioLog = getInicio();
			if(inicio.after(inicioLog)) {
				return 0;
			}

			Duration duration = Duration.between(inicio.toInstant(), inicioLog.toInstant());
			long minutes = duration.toMinutes();
			return (int) minutes;
		}

		public int getMinutosLimite() {

			Date inicio = getInicio();
			BigDecimal horaInicio = DummyUtils.getHoras(getHoraInicio());
			String horaLimiteInicioStr = horas.get(0);
			BigDecimal horaLimiteInicio = DummyUtils.getHoras(horaLimiteInicioStr);
			if(horaInicio.compareTo(horaLimiteInicio) < 0) {
				String[] split = horaLimiteInicioStr.split(":");
				inicio = DateUtils.setHours(inicio, Integer.parseInt(split[0]));
				inicio = DateUtils.setMinutes(inicio, Integer.parseInt(split[1]));
				inicio = DateUtils.truncate(inicio, Calendar.MINUTE);
			}

			Date fim = getFim();

			if(fim == null){
				fim = new Date();
			}

			Duration duration = Duration.between(inicio.toInstant(), fim.toInstant());
			long minutes = duration.toMinutes();
			return (int) minutes;
		}

		public int getMinutos() {
			Date inicio = getInicio();
			Date fim = getFim();
			Duration duration = Duration.between(inicio.toInstant(), fim.toInstant());
			long minutes = duration.toMinutes();
			return (int) minutes;
		}

		public Object[] getLogAtendimento() {
			return logAtendimento;
		}
	}
}
