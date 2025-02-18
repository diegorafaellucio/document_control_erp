package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.vo.RelatorioProdutividadeProuniVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeProuniFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
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

@Service
public class RelatorioProdutividadeProuniService {

	@Autowired private ProcessoLogService processoLogService;
	@Autowired private SessionFactory sessionFactory;

	public List<RelatorioProdutividadeProuniVO> getRelatorioProdutividadeProuni(RelatorioProdutividadeProuniFiltro filtro) {
		return processoLogService.getRelatorioProdutividadeProuni(filtro);
	}

	public File render(RelatorioProdutividadeProuniFiltro filtro) {
		try {

			String fileOrigemNome = "relatorio-produtividade-prouni.xlsx";

			String extensao = DummyUtils.getExtensao(fileOrigemNome);

			File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/" + fileOrigemNome);

			File file = File.createTempFile("relatorio-produtividade-prouni", "." + extensao);
			DummyUtils.deleteOnExitFile(file);
			FileUtils.copyFile(fileOrigem, file);

			ExcelWriter ew = new ExcelWriter();
			ew.abrirArquivo(file);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);

			ew.selecionarPlanilha("Planilha1");

			renderRows(ew, filtro);

			file.delete();
			File fileDestino = File.createTempFile("relatorio-produtividade-prouni", ".xlsx");
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

	private void renderRows(ExcelWriter ew, RelatorioProdutividadeProuniFiltro filtro) {

		final List<RelatorioProdutividadeProuniVO> list = getRelatorioProdutividadeProuni(filtro);

		int rowNum = 1;
		do {
			List<RelatorioProdutividadeProuniVO> list2 = new ArrayList<>();
			for (int i = 0; i < 200 && !list.isEmpty(); i++) {
				RelatorioProdutividadeProuniVO remove = list.remove(0);
				list2.add(remove);
			}

			for (int i = 0; i < list2.size(); i++) {

				RelatorioProdutividadeProuniVO vo = list2.get(i);

				ew.criaLinha(rowNum++);
				renderBody(ew, vo);

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!list.isEmpty());
	}

	@SuppressWarnings("ConstantConditions")
	private void renderBody(ExcelWriter ew, RelatorioProdutividadeProuniVO vo) {

		Long processoId = vo.getProcessoId();
		ew.escrever(processoId);

		String diretoria = vo.getDiretoria();
		ew.escrever(diretoria);

		String nomeProcesso = vo.getNomeProcesso();
		ew.escrever(nomeProcesso);

		String subNomeProcesso = vo.getSubNomeProcesso();
		ew.escrever(subNomeProcesso);

		String servico = vo.getServico();
		ew.escrever(servico);

		String sla = vo.getSla();
		ew.escrever(sla);

		String dataAbertura = vo.getDataAbertura();
		ew.escrever(dataAbertura);

		String dataVencimento = vo.getDataVencimento();
		ew.escrever(dataVencimento);

		String dataFechamento = vo.getDataFechamento();
		ew.escrever(dataFechamento);

		String solicitante = vo.getSolicitante();
		ew.escrever(solicitante);

		String responsavel = vo.getResponsavel();
		ew.escrever(responsavel);

		Integer quantidadeReabertura = vo.getQuantidadeReabertura();
		ew.escrever(quantidadeReabertura);

		Integer quantidadeImagens = vo.getQuantidadeImagens();
		ew.escrever(quantidadeImagens);

		Integer quantidadeAcoes = vo.getQuantidadeAcoes();
		ew.escrever(quantidadeAcoes);

		String status = vo.getStatus();
		ew.escrever(status);
	}
}