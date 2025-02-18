package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.repository.DashboardMensalRepository;
import net.wasys.getdoc.domain.vo.DashboardMensalVO;
import net.wasys.getdoc.domain.vo.DashboardMensalVO.ListaProcessoVO;
import net.wasys.getdoc.domain.vo.DashboardMensalVO.TempoOperacao;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.DashboardMensalFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import net.wasys.util.other.ExcelCsvWriter;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class DashboardMensalService {

	@Autowired private DashboardMensalRepository dashboardMensalRepository;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private SessionFactory sessionFactory;

	public DashboardMensalVO buildVO(DashboardMensalFiltro filtro) {

		ListaProcessoVO tipoProcessoPorDia = dashboardMensalRepository.getTipoProcessoPorDia(filtro, 1 ,filtro.getDiasMes());
		//filtro.setTratados(true);
		//TipoProcessoPorDia tipoProcessoPorDiaTratado = dashboardMensalRepository.getTipoProcessoPorDia(filtro, 1 ,31);
		ListaProcessoVO tipoProcessoPorMes = dashboardMensalRepository.getTipoProcessoPorMes(filtro);
		TempoOperacao tempoOperacao = makeTempoOperacao(filtro, "DIARIO");
		TempoOperacao tempoOperacaoMes = makeTempoOperacao(filtro, "MENSAL");
		List<Long> produtividadePorDia = dashboardMensalRepository.getProdutividadePorDia(filtro);
		List<Long> produtividadePorMes = dashboardMensalRepository.getProdutividadePorMes(filtro);

		DashboardMensalVO dashboardMensalVO = new DashboardMensalVO();
		dashboardMensalVO.setTipoProcessoPorDia(tipoProcessoPorDia);
		dashboardMensalVO.setTipoProcessoPorMes(tipoProcessoPorMes);
		dashboardMensalVO.setTempoOperacao(tempoOperacao);
		dashboardMensalVO.setTempoOperacaoMes(tempoOperacaoMes);
		//dashboardMensalVO.setTipoProcessoPorDiaTratado(tipoProcessoPorDiaTratado);
		dashboardMensalVO.setProdutividadePorDia(produtividadePorDia);
		dashboardMensalVO.setProdutividadePorMes(produtividadePorMes);

		return dashboardMensalVO;
	}

	private TempoOperacao makeTempoOperacao(DashboardMensalFiltro filtro, String modo){
		List<Double> conferido;
		if(modo.equals("DIARIO")) {
			conferido = dashboardMensalRepository.findTempoMedioDiarioBySituacao(filtro, StatusProcesso.EM_ANALISE);
		}
		else{
			conferido = dashboardMensalRepository.findTempoMedioMensalBySituacao(filtro, StatusProcesso.EM_ANALISE);
		}

		TempoOperacao tempoOperacao = new TempoOperacao();
		tempoOperacao.setConferido(conferido);

		return tempoOperacao;
	}

	public List<RegistroValorVO> findRegistroValorVOCombos(String chaveUnicidade, Long baseInternaID, String colunaPesquisa ){
		chaveUnicidade = DummyUtils.limparCharsChaveUnicidade(chaveUnicidade);

		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setBaseInterna(new BaseInterna(baseInternaID));
		filtro.addCampoFiltro(colunaPesquisa, chaveUnicidade);

		return baseRegistroService.findByFiltro(filtro, null, null);
	}

	public List<Long> calculaTotal(ListaProcessoVO tipoProcessoPorDia, Integer diasMes) {
		List<Long> conferidoPorDiaQtd = new ArrayList<>();
		Map<String, List<Long>> qtdPorDia = tipoProcessoPorDia.getQtdPorDia();
		Set<String> tipoProcesso = qtdPorDia.keySet();
		for (String nome : tipoProcesso) {
			List<Long> longList = qtdPorDia.get(nome);
			for(int i = 1; i < longList.size(); i++) {
				Long valorParaSomar = longList.get(i);
				if(conferidoPorDiaQtd.size() == diasMes) {
					Long valorSomado =  conferidoPorDiaQtd.get(i-1);
					valorSomado += valorParaSomar;
					conferidoPorDiaQtd.set(i-1, valorSomado);
				} else{
					conferidoPorDiaQtd.add(i-1, valorParaSomar);
				}
			}
		}
		return conferidoPorDiaQtd;
	}

	public List<Long> calculaTotalMes(ListaProcessoVO tipoProcessoPorMes) {
		List<Long> conferidoPorMesQtd = new ArrayList<>();
		Map<String, List<Long>> qtdPorMes = tipoProcessoPorMes.getQtdPorDia();
		Set<String> tipoProcesso = qtdPorMes.keySet();
		for (String nome : tipoProcesso) {
			List<Long> longList = qtdPorMes.get(nome);
			for(int i = 0; i < longList.size(); i++) {
				Long valorParaSomar = longList.get(i);
				if(conferidoPorMesQtd.size() == 4) {
					Long valorSomado =  conferidoPorMesQtd.get(i);
					valorSomado += valorParaSomar;
					conferidoPorMesQtd.set(i, valorSomado);
				} else{
					conferidoPorMesQtd.add(i, valorParaSomar);
				}
			}
		}
		return conferidoPorMesQtd;
	}

	public File render(DashboardMensalFiltro filtro, Usuario usuario) {

		try {

			String fileOrigemNome =  "dashboard-mensal.xlsx";

			String extensao = DummyUtils.getExtensao(fileOrigemNome);

			File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/" + fileOrigemNome);

			File file = File.createTempFile("dashboard-mensal-", "." + extensao);
			DummyUtils.deleteOnExitFile(file);
			FileUtils.copyFile(fileOrigem, file);

			ExcelCsvWriter ecw = new ExcelCsvWriter();

			ExcelWriter ew = new ExcelWriter();
			ew.criarArquivo(extensao);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);
			ew.criaPlanilha("Processos");
			ecw.setWriter(ew);

			ecw.criaLinha(0);

			List<DashboardMensalVO.DashProcessoVO> processos = dashboardMensalRepository.getProcessos(filtro);
			if(processos.size() > 0) {
				renderRowsProcessos(ecw, usuario, processos);
			}

			DummyUtils.deleteFile(file);
			File fileDestino = File.createTempFile("dashboard-mensal", ".xlsx");
			DummyUtils.deleteOnExitFile(fileDestino);

			FileOutputStream fos = new FileOutputStream(fileDestino);
			workbook.write(fos);
			workbook.close();

			return fileDestino;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void renderRowsProcessos(ExcelCsvWriter ew, Usuario usuario, List<DashboardMensalVO.DashProcessoVO> processos) {

		int total = processos.size();
		systraceThread("iniciando extração. total de registros: " + total);
		long inicio = System.currentTimeMillis();

		int rowNum = 1;
		do {
			List<DashboardMensalVO.DashProcessoVO> processos2 = new ArrayList<>();
			for (int i = 0; i < 200 && !processos.isEmpty(); i++) {
				DashboardMensalVO.DashProcessoVO temp = processos.remove(0);
				processos2.add(temp);
			}

			String login = usuario.getLogin();

			for (int i = 0; i < processos2.size(); i++) {

				DashboardMensalVO.DashProcessoVO processo = processos2.get(i);

				if(rowNum == 1) {
					renderCabecalho(ew);
				}
				ew.criaLinha(rowNum++);
				renderBody(ew, processo, true);

				if(rowNum % 100 == 0) {
					long fim = System.currentTimeMillis();
					systraceThread(rowNum + " de " + total + ". usuário: " + login + ". " + (fim - inicio) + "ms.");
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!processos.isEmpty());
	}

	private void renderBody(ExcelCsvWriter ew, DashboardMensalVO.DashProcessoVO processo, boolean carregarCamposDinamicos) {
		ew.escrever(processo.getId());
		ew.escrever(processo.getProcessoId());
		ew.escrever(processo.getMotivoRequisicao());
		ew.escrever(processo.getNome());
		ew.escrever(processo.getNomeSocial());
		ew.escrever(processo.getCpf());
		ew.escrever(processo.getPassaporte());
		ew.escrever(processo.getIdentidade());
		ew.escrever(processo.getOrgaoEmissor());
		ew.escrever(processo.getDataEmissao());
		ew.escrever(processo.getCurso());
		ew.escrever(processo.getCampus());
		ew.escrever(processo.getModalidadeEnsino());
		ew.escrever(processo.getFormaIngresso());
		ew.escrever(processo.getInstituicao());
		ew.escrever(processo.getRegional());
		ew.escrever(processo.getDataCriacao());
		ew.escrever(processo.getDataEnvioAnalise());
		ew.escrever(processo.getDataFinalizacaoAnalise());
		ew.escrever(processo.getDataFinalizacao());
		ew.escrever(processo.getPrazoLimiteAnalise());
		ew.escrever(processo.getTempoEmAnalise());
		ew.escrever(processo.getSlaPrevisto());
		ew.escrever(processo.getSlaAtendido());
		ew.escrever(processo.getSituacao());
		ew.escrever(processo.getStatus());
		ew.escrever(processo.getAnalista());
		ew.escrever(processo.getAnalistaLogin());
	}

	private void renderCabecalho(ExcelCsvWriter ew) {

		ew.escrever("ID", 3000);
		ew.escrever("Processo ID", 3000);
		ew.escrever("Tipo Processo", 3000);
		ew.escrever("Nome", 12000);
		ew.escrever("Nome Social", 8000);
		ew.escrever("CPF", 8000);
		ew.escrever("Passaporte", 3000);
		ew.escrever("Identidade", 8000);
		ew.escrever("Orgao Emissor", 3000);
		ew.escrever("Data Emissao", 5000);
		ew.escrever("Curso", 12000);
		ew.escrever("Campus", 9000);
		ew.escrever("Modalidade Ensino", 8000);
		ew.escrever("Forma Ingresso", 8000);
		ew.escrever("Instituição", 10000);
		ew.escrever("Regional", 8000);
		ew.escrever("Data Criacao", 5000);
		ew.escrever("Data Envio Analise", 5000);
		ew.escrever("Data Finalizacao Analise", 5000);
		ew.escrever("Data Finalizacao", 5000);
		ew.escrever("Prazo Limite Analise", 4000);
		ew.escrever("Tempo em Analise", 3000);
		ew.escrever("SLA PREVISTO", 3000);
		ew.escrever("SLA Atendido", 3000);
		ew.escrever("Situacao", 3000);
		ew.escrever("Status", 4000);
		ew.escrever("Analista", 8000);
		ew.escrever("Login Analista", 6000);
	}
}
