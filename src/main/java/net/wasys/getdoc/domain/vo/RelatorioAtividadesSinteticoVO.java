package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

public class RelatorioAtividadesSinteticoVO {

	private List<String> horas;
	private Map<String, List<AtividadeVO>> atividadesMap = new TreeMap<>();

	public List<String> getHoras() {
		return horas;
	}

	public void setHoras(List<String> horas) {
		this.horas = horas;
	}

	public Map<String, List<AtividadeVO>> getAtividadesMap() {
		return atividadesMap;
	}

	public void addAtividadeVO(String key, AtividadeVO atividadeVO) {
		List<AtividadeVO> list = atividadesMap.get(key);
		list = list != null ? list : new ArrayList<>();
		list.add(atividadeVO);
		atividadesMap.put(key, list);
	}

	public List<AtividadeVO> getAtividades(String analista) {
		List<AtividadeVO> list = atividadesMap.get(analista);
		return list;
	}

	public String getKeyFormatada(String key) {
		String[] split = key.split(" - ");
		return split[1] + " " + split[0];
	}

	public class AtividadeVO {

		private String analista;
		private List<Object[]> acoes = new ArrayList<>();
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

		public void addLog(Date data, AcaoProcesso acao, String observacao) {
			if(inicio == null) {
				inicio = data;
			}
			fim = data;
			if(StringUtils.isNotBlank(observacao)) {
				observacao = getObservacaoCurta(80, observacao);
			}

			acoes.add(new Object[]{DummyUtils.format(data, "HH:mm"), acao, observacao});
		}

		public List<Object[]> getAcoes() {
			return acoes;
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

		public int getCount() {
			return acoes.size();
		}

		public String getObservacaoCurta(int size, String observacao) {

			observacao = observacao.replace("\t", " ");
			observacao = observacao.replace("\n", " ");
			observacao = observacao.replace("\r", " ");
			while(observacao.contains("  ")) {
				observacao = observacao.replace("  ", " ");
			}

			if(observacao.length() > size) {
				observacao = observacao.substring(0, (size - 5)) + "[...]";
			}
			return observacao;
		}
	}
}
