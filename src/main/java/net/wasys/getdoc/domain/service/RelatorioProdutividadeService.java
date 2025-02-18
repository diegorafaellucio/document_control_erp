package net.wasys.getdoc.domain.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.filtro.CamposFiltro;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.vo.RelatorioProdutividadeVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeFiltro.Tipo;
import net.wasys.util.DummyUtils;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;

@Service
public class RelatorioProdutividadeService {

	@Autowired private ProcessoLogService processoLogService;

	public List<RelatorioProdutividadeVO> getRelatorioProdutividade(RelatorioProdutividadeFiltro filtro) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Tipo tipo = filtro.getTipo();
		List<CampoDinamicoVO> camposFiltros = filtro.getCamposFiltro();

		if(RelatorioProdutividadeFiltro.Tipo.VISUALIZACAO_POR_ANALISTA.equals(tipo)) {
			return getRelatorioProdutividadeAnalista(filtro);
		}
		else if(RelatorioProdutividadeFiltro.Tipo.VISUALIZACAO_POR_MOTIVO.equals(tipo)) {
			return getRelatorioProdutividadeTipoRequisicao(dataInicio, dataFim, camposFiltros);
		}
		else if(RelatorioProdutividadeFiltro.Tipo.VISUALIZACAO_POR_SITUACAO.equals(tipo)) {
			return getRelatorioProdutividadeSituacao(dataInicio, dataFim, camposFiltros);
		}
		else if(RelatorioProdutividadeFiltro.Tipo.VISUALIZACAO_POR_ANALISTA_CSC.equals(tipo)) {
			return getRelatorioProdutividadeAnalistaCSC(filtro);
		}
		else if(RelatorioProdutividadeFiltro.Tipo.VISUALIZACAO_POR_ANALISTA_MEDICINA.equals(tipo)) {
			return getRelatorioProdutividadeAnalistaMedicina(filtro);
		}

		return null;
	}

	private List<RelatorioProdutividadeVO> getRelatorioProdutividadeTipoRequisicao(Date dataInicio, Date dataFim, List<CampoDinamicoVO> camposFiltros) {

		List<RelatorioProdutividadeVO> list = processoLogService.getRelatorioProdutividadeTipoRequisicao(dataInicio, dataFim, camposFiltros);

		calculaTotal(list);

		return list;
	}

	private List<RelatorioProdutividadeVO> getRelatorioProdutividadeSituacao(Date dataInicio, Date dataFim, List<CampoDinamicoVO> camposFiltros) {

		List<RelatorioProdutividadeVO> list = processoLogService.getRelatorioProdutividadeSituacao(dataInicio, dataFim, camposFiltros);

		calculaTotal(list);

		return list;
	}

	private List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalista(RelatorioProdutividadeFiltro filtro) {

		List<RelatorioProdutividadeVO> list = processoLogService.getRelatorioProdutividadeAnalista(filtro);

		calculaTotal(list);

		return list;
	}

	private List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalistaCSC(RelatorioProdutividadeFiltro filtro) {

		List<RelatorioProdutividadeVO> list = processoLogService.getRelatorioProdutividadeAnalistaCSC(filtro);

		calculaTotal(list);

		return list;
	}

	private List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalistaMedicina(RelatorioProdutividadeFiltro filtro) {

		List<RelatorioProdutividadeVO> list = processoLogService.getRelatorioProdutividadeAnalistaMedicina(filtro);

		calculaTotal(list);

		return list;
	}

	private void calculaTotal(List<RelatorioProdutividadeVO> list) {

		RelatorioProdutividadeVO voTotal = new RelatorioProdutividadeVO();
		voTotal.setRegistroDescricao("Total");

		for (RelatorioProdutividadeVO vo : list) {

			Long atividades = vo.getAtividades();
			Long cadastroManual = vo.getCadastroManual();
			Long cadastroAutomatio = vo.getCadastroAutomatio();
			Long emAcompanhamento = vo.getEmAcompanhamento();
			Long finalizadosAcompanhamento = vo.getFinalizadosAcompanhamento();
			Long finalizadosAnalise = vo.getFinalizadosAnalise();
			Long manifestacoes = vo.getRequisicoes();
			Long finalizadosPreAnalise = vo.getFinalizadosPreAnalise();


			voTotal.setAtividades(atividades + voTotal.getAtividades());
			voTotal.setCadastroAutomatio(cadastroAutomatio + voTotal.getCadastroAutomatio());
			voTotal.setCadastroManual(cadastroManual + voTotal.getCadastroManual());
			voTotal.setEmAcompanhamento(emAcompanhamento + voTotal.getEmAcompanhamento());
			voTotal.setFinalizadosAcompanhamento(finalizadosAcompanhamento + voTotal.getFinalizadosAcompanhamento());
			voTotal.setFinalizadosAnalise(finalizadosAnalise + voTotal.getFinalizadosAnalise());
			voTotal.setRequisicoes(manifestacoes + voTotal.getRequisicoes());
			voTotal.setFinalizadosPreAnalise(finalizadosPreAnalise + voTotal.getFinalizadosPreAnalise());
		}

		list.add(voTotal);
	}

	public File render(RelatorioProdutividadeFiltro filtro) {

		try {
			File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/relatorio-produtividade.xlsx");

			File fileTmp = File.createTempFile("relatorio-produtividade1", ".xlsx");
			DummyUtils.deleteOnExitFile(fileTmp);
			FileUtils.copyFile(fileOrigem, fileTmp);

			ExcelWriter ew = new ExcelWriter();
			ew.abrirArquivo(fileTmp);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);

			Sheet sheet = ew.selecionarPlanilha("Plan1");

			renderRows(filtro, ew, sheet);

			File fileDestino = File.createTempFile("relatorio-produtividade2", ".xlsx");
			DummyUtils.deleteOnExitFile(fileDestino);

			FileOutputStream fos = new FileOutputStream(fileDestino);
			workbook.write(fos);
			workbook.close();

			return fileDestino;
		}
		catch (IOException | InvalidFormatException e) {
			throw new RuntimeException(e);
		}
	}

	private void renderRows(RelatorioProdutividadeFiltro filtro, ExcelWriter ew, Sheet sheet) {

		List<RelatorioProdutividadeVO> relatorio = getRelatorioProdutividade(filtro);

		int rowNum = 1;

		for (RelatorioProdutividadeVO vo : relatorio) {

			ew.criaLinha(sheet, rowNum++);
			renderBody(ew, vo);
		}
	}

	private void renderBody(ExcelWriter ew, RelatorioProdutividadeVO vo) {

		String registroDescricao = vo.getRegistroDescricao();
		ew.escrever(registroDescricao);

		long manifestacoes = vo.getRequisicoes();
		ew.escrever(manifestacoes);

		long atividades = vo.getAtividades();
		ew.escrever(atividades);

		long cadastroManual = vo.getCadastroManual();
		ew.escrever(cadastroManual);

		long cadastroAutomatio = vo.getCadastroAutomatio();
		ew.escrever(cadastroAutomatio);

		long cadastroTotal = cadastroManual + cadastroAutomatio;
		ew.escrever(cadastroTotal);

		long emAcompanhamento = vo.getEmAcompanhamento();
		ew.escrever(emAcompanhamento);

		long finalizadosAnalise = vo.getFinalizadosAnalise();
		ew.escrever(finalizadosAnalise);

		long finalizadosAcompanhamento = vo.getFinalizadosAcompanhamento();
		ew.escrever(finalizadosAcompanhamento);

		long finalizadosTotal = finalizadosAnalise + finalizadosAcompanhamento;
		ew.escrever(finalizadosTotal);
	}
}
