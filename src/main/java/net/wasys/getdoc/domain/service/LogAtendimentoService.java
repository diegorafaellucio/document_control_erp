package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusAtendimento;
import net.wasys.getdoc.domain.repository.LogAtendimentoRepository;
import net.wasys.getdoc.domain.vo.RelatorioStatusLaboralSinteticoVO;
import net.wasys.getdoc.domain.vo.filtro.LogAtendimentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class LogAtendimentoService {


	@Autowired private ApplicationContext applicationContext;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private LogAtendimentoRepository logAtendimentoRepository;
	@Autowired private StatusLaboralService statusLaboralService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private ProcessoService processoService;

	public LogAtendimento get(Long id) {
		return logAtendimentoRepository.get(id);
	}

	public int countByFiltro(LogAtendimentoFiltro filtro) {
		return logAtendimentoRepository.countByFiltro(filtro);
	}

	public List<LogAtendimento> findByFiltro(LogAtendimentoFiltro filtro, Integer first, Integer pageSize) {
		return logAtendimentoRepository.findByFiltro(filtro, first, pageSize);
	}

	public List<Long> findIdsByFiltro(LogAtendimentoFiltro filtro) {
		return logAtendimentoRepository.findIdsByFiltro(filtro);
	}

	public List<LogAtendimento> findByIds(List<Long> ids) {
		return logAtendimentoRepository.findByIds(ids);
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdate(LogAtendimento logAtendimento) {
		logAtendimentoRepository.saveOrUpdate(logAtendimento);
	}

	public void saveOrUpdateNewSession(LogAtendimento log) {

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			LogAtendimentoService logAtendimentoService = applicationContext.getBean(LogAtendimentoService.class);
			logAtendimentoService.saveOrUpdate(log);
		});

		Thread ct = Thread.currentThread();
		StackTraceElement[] stArray = ct.getStackTrace();
		StackTraceElement st = null;
		for (int i = 2; i < stArray.length; i++) {
			st = stArray[i];
			String className = st.getClassName();
			if (!className.contains("$") && className.contains("getdoc")) {
				break;
			}
		}
		StackTraceElement st2 = st;
		tw.setExceptionHandler((e, stackTrace) -> {
			String message = DummyUtils.getExceptionMessage(e);
			String methodName2 = st2.toString();
			systraceThread("Erro ao salvar logAtendimento em " + methodName2 + ": " + message, LogLevel.ERROR);
		});

		tw.runNewThread();
	}

	public void encerrarUltimoLog(Usuario analista) {
		LogAtendimento ultimo = getUltimoLogAtendimento(analista);
		finalizaLogAtendimento(ultimo, null);
	}

	public void encerrarUltimoLogComData(Usuario analista, Date data) {
		LogAtendimento ultimo = getUltimoLogAtendimento(analista);
		finalizaLogAtendimento(ultimo, data);
	}

	public LogAtendimento getUltimoLogAtendimento(Usuario analista) {
		return logAtendimentoRepository.getUltimoLogAtendimento(analista);
	}

	public LogAtendimento getUltimoLogAtendimentoByAnalistaAndProcesso(Usuario analista, Processo processo) {
		return logAtendimentoRepository.getUltimoLogAtendimentoByAnalistaAndProcesso(analista, processo);
	}

	public LogAtendimento criarDisponivel(Long usuarioId) {
		StatusLaboral disponivel = statusLaboralService.getFixo(StatusAtendimento.DISPONIVEL);
		return criaLogAtendimento(usuarioId, disponivel);
	}

	public LogAtendimento criarPausaSistema(Long usuarioId) {
		StatusLaboral pausaSistema = statusLaboralService.getFixo(StatusAtendimento.PAUSA_SISTEMA);
		return criaLogAtendimento(usuarioId, pausaSistema);
	}

	public LogAtendimento criarEmAnalise(Long usuarioId) {
		StatusLaboral emAnalise = statusLaboralService.getFixo(StatusAtendimento.EM_ANALISE);
		return criaLogAtendimento(usuarioId, emAnalise);
	}

	public LogAtendimento criaLogAtendimento(Long usuarioId, StatusLaboral statusLaboral) {

		Usuario analista = usuarioService.get(usuarioId);

		if (!analista.getDistribuirDemandaAutomaticamente()) {
			return null;
		}

		LogAtendimento ultimoLogAtendimento = getUltimoLogAtendimento(analista);

		if (ultimoLogAtendimento != null && ultimoLogAtendimento.getStatusLaboral().equals(statusLaboral)) {
			systraceThread("Ultimo log de atendimento possui mesmo situacao do novo.");
			return ultimoLogAtendimento;
		}

		finalizaLogAtendimento(ultimoLogAtendimento, null);

		final LogAtendimento log = new LogAtendimento();

		Processo processo = null;
		Situacao situacao = null;
		Date dataEmAnalise = null;

		Long processoAtualId = analista.getProcessoAtualId();
		if(processoAtualId != null){
			processo = processoService.get(processoAtualId);
			situacao = processo.getSituacao();
			dataEmAnalise = processo.getDataEnvioAnalise();
		}

		log.setAnalista(analista);
		log.setStatusLaboral(statusLaboral);
		log.setInicio(new Date());
		log.setSituacaoInicial(situacao);
		log.setProcesso(processo);
		log.setDataEmAnalise(dataEmAnalise);

		saveOrUpdateNewSession(log);

		analista.setStatusLaboral(statusLaboral);
		usuarioService.merge(analista);

		DummyUtils.sleep(1000);

		return log;
	}

	public void finalizaLogAtendimento(LogAtendimento log, Date data) {

		if (log == null) {
			systraceThread("log null");
			return;
		}

		boolean fimVazio = log.getFim() == null;

		if (!fimVazio) {
			systraceThread("log ja finalizado");
			return;
		}

		Processo processo = log.getProcesso();
		if(processo != null) {
			Situacao situacao = processo.getSituacao();
			Situacao situacaoInicial = log.getSituacaoInicial();
			if(situacaoInicial != null && (situacao != situacaoInicial)) {
				log.setSituacaoFinal(situacao);
			}
		}

		if(data != null){
			log.setFim(data);
		} else {
			log.setFim(new Date());
		}

		long tempo = log.getFim().getTime() - log.getInicio().getTime();
		log.setTempo(tempo);
		saveOrUpdate(log);
	}

	public void encerrarProgramacao(Usuario analista) {
		analista.setProgramouStatusLaboral(false);
		usuarioService.merge(analista);
	}

	public void pausar(StatusLaboral statusLaboral, Usuario analista) {
		encerrarProgramacao(analista);
		criaLogAtendimento(analista.getId(), statusLaboral);
	}

	public void entrarEmPausa(StatusLaboral statusLaboral, Usuario analista) {

		StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
		boolean estaEmAnalise = usuarioService.estaEmAnalise(analista);

		if (!StatusAtendimento.DISPONIVEL.equals(statusAtendimento) && estaEmAnalise) {
			usuarioService.programarSituacaoAtendimento(analista, statusLaboral);
		}
		else {
			pausar(statusLaboral, analista);
		}
	}

	public void entrarSituacaoAtendimentoProgramada(Usuario analista) {

		StatusLaboral situacaoAtendimentoProgramada = analista.getStatusLaboralProgramado();
		Boolean programouStatusLaboral = analista.getProgramouStatusLaboral();

		if (programouStatusLaboral) {
			pausar(situacaoAtendimentoProgramada, analista);
			analista.setProgramouStatusLaboral(false);

			usuarioService.merge(analista);
		}
	}

	public boolean verificarTempoPausa(Usuario analista) {
		LogAtendimento ultimoLogAtendimento = getUltimoLogAtendimento(analista);
		if(ultimoLogAtendimento == null) return false;
		StatusLaboral statusLaboral = ultimoLogAtendimento.getStatusLaboral();
		StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
		if(StatusAtendimento.PAUSA.equals(statusAtendimento)) {
			Date inicio = ultimoLogAtendimento.getInicio();
			Date atual = new Date();
			long tempo = atual.getTime() - inicio.getTime();
			if (tempo < 5000L) {
				return true;
			}
		}
		return false;
	}

	public File render(LogAtendimentoFiltro filtro) {

		try {
			File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/relatorio-status-laboral.xlsx");

			File file = File.createTempFile("relatorio-status-laboral1", ".xlsx");
			DummyUtils.deleteOnExitFile(file);
			FileUtils.copyFile(fileOrigem, file);

			ExcelWriter ew = new ExcelWriter();
			ew.abrirArquivo(file);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);

			ew.selecionarPlanilha("Plan1");
			renderRows(filtro, ew);

			File fileDestino = File.createTempFile("relatorio-status-laboral2", ".xlsx");
			DummyUtils.deleteOnExitFile(fileDestino);

			FileOutputStream fos = new FileOutputStream(fileDestino);
			workbook.write(fos);
			workbook.close();
			DummyUtils.deleteFile(file);

			return fileDestino;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (InvalidFormatException e) {
			throw new RuntimeException(e);
		}
	}

	private void renderRows(LogAtendimentoFiltro filtro, ExcelWriter ew) {

		final List<Long> ids = findIdsByFiltro(filtro);

		int rowNum = 1;

		do {
			List<Long> ids2 = new ArrayList<Long>();
			for (int i = 0; i < 200 && !ids.isEmpty(); i++) {
				Long id = ids.remove(0);
				ids2.add(id);
			}

			List<LogAtendimento> list = findByIds(ids2);

			for (int i = 0; i < list.size(); i++) {

				LogAtendimento ml = list.get(i);

				ew.criaLinha(rowNum++);

				renderBody(ew, ml);

				try {
					Thread.sleep(4);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!ids.isEmpty());
	}

	private void renderBody(ExcelWriter ew, LogAtendimento ml) {

		Long id = ml.getId();
		ew.escrever(id);

		Processo processo = ml.getProcesso();
		String processoId = processo != null ? processo.getId().toString() : "";
		ew.escrever(processoId);

		String tipoProcessoNome = processo != null ? processo.getTipoProcesso().getNome(): "";
		ew.escrever(tipoProcessoNome);

		Situacao situacaoInicial = ml.getSituacaoInicial();
		String situacaoInicialNome = situacaoInicial != null ? situacaoInicial.getNome(): "";
		ew.escrever(situacaoInicialNome);

		Situacao situacaoFinal = ml.getSituacaoFinal();
		String situacaoFinalNome = situacaoFinal != null ? situacaoFinal.getNome(): "";
		ew.escrever(situacaoFinalNome);

		Date dataEmAnalise = ml.getDataEmAnalise();
		ew.escreverDate(dataEmAnalise);
		ew.escreverTime(dataEmAnalise);

		StatusLaboral status = ml.getStatusLaboral();
		String statusStr = status.getNome();
		ew.escrever(statusStr);

		Usuario analista = ml.getAnalista();

		String analistaLogin = analista != null ? analista.getLogin() : "";
		ew.escrever(analistaLogin);

		String analistaNome = analista != null ? analista.getNome() : "";
		ew.escrever(analistaNome);

		Date dataInicio = ml.getInicio();
		ew.escreverDate(dataInicio);
		ew.escreverTime(dataInicio);

		Date dataFim = ml.getFim();
		ew.escreverDate(dataFim);
		ew.escreverTime(dataFim);

		String tempo = DummyUtils.formatarMilisegundosParaHoraMinutoSegundo(ml.getTempo());
		ew.escrever(tempo);
	}

	public RelatorioStatusLaboralSinteticoVO getRelatorioSintetico(LogAtendimentoFiltro filtro) {

		RelatorioStatusLaboralSinteticoVO vo = new RelatorioStatusLaboralSinteticoVO();
		BigDecimal horaMin = new BigDecimal(25);
		BigDecimal horaMax = new BigDecimal(0);
		List<LogAtendimento> logs = logAtendimentoRepository.findByFiltro(filtro, null, null);

		for (LogAtendimento log : logs) {

			Processo processo = log.getProcesso();
			Long processoId = processo != null ? processo.getId() : null;
			String processoNumero = processoId != null? processoId.toString() : null;
			Usuario usuario = log.getAnalista();
			String analista = usuario.getNome();
			Date inicio = log.getInicio();
			Date fim = log.getFim();
			Long tempo = log.getTempo();

			String key = analista + " - " + DummyUtils.format(inicio, "dd/MM");

			RelatorioStatusLaboralSinteticoVO.StatusAtendimentoVO statusAtendimentoVO = vo.new StatusAtendimentoVO();
			statusAtendimentoVO.setAnalista(analista);
			statusAtendimentoVO.setProcessoId(processoId);
			statusAtendimentoVO.setProcessoNumero(processoNumero);
			vo.addStatusAtendimentoVO(key, statusAtendimentoVO);

			StatusLaboral statusLaboral = log.getStatusLaboral();
			StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
			String statusLaboralNome = statusLaboral.getNome();
			statusAtendimentoVO.addLog(statusAtendimento, statusLaboralNome, inicio, fim, tempo);

			BigDecimal hora = DummyUtils.getHoras(DummyUtils.format(inicio, "HH:mm"));
			horaMin = horaMin.min(hora);
			horaMax = horaMax.max(hora);
		}

		List<String> horas = new ArrayList<>();
		DateTime dateTimeIni = new DateTime().dayOfMonth().roundFloorCopy();
		DateTime dateTime = dateTimeIni;

		while (dateTime.dayOfMonth().equals(dateTimeIni.dayOfMonth())) {

			String horaStr = dateTime.toString("HH:mm");
			BigDecimal hora = DummyUtils.getHoras(horaStr);
			DateTime dateTimePlus = dateTime.plusMinutes(20);
			String horaPlusStr = dateTimePlus.toString("HH:mm");
			BigDecimal horaPlus = DummyUtils.getHoras(horaPlusStr);

			if(horaPlus.compareTo(horaMin) > 0 && hora.compareTo(horaMax) < 0) {
				horas.add(horaStr);
			}

			dateTime = dateTimePlus;
		}
		vo.setHoras(horas);

		return vo;
	}

	public void atualizarSituacaoFinal(LogAtendimento logAtendimento, Situacao situacao) {
		logAtendimentoRepository.atualizarSituacaoFinal(logAtendimento, situacao);
	}
}
