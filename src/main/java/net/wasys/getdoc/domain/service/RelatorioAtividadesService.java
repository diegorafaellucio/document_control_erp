package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.entity.*;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.filtro.ProcessoLogFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;

@Service
public class RelatorioAtividadesService {

	@Autowired private ProcessoLogService processoLogService;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private ResourceService resourceService;

	public File render(ProcessoLogFiltro filtro) {

		try {
			File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/relatorio-atividades.xlsx");

			File file = File.createTempFile("relatorio-atividades1", ".xlsx");
			DummyUtils.deleteOnExitFile(file);
			FileUtils.copyFile(fileOrigem, file);

			ExcelWriter ew = new ExcelWriter();
			ew.abrirArquivo(file);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);

			Sheet sheet = ew.selecionarPlanilha("Plan1");
			renderRows(filtro, ew, sheet);

			File fileDestino = File.createTempFile("relatorio-atividades2", ".xlsx");
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

	private void renderRows(ProcessoLogFiltro filtro, ExcelWriter ew, Sheet sheet) {

		final List<Long> ids = processoLogService.findIdsByFiltro(filtro);

		int rowNum = 1;

		do {
			List<Long> ids2 = new ArrayList<Long>();
			for (int i = 0; i < 200 && !ids.isEmpty(); i++) {
				Long id = ids.remove(0);
				ids2.add(id);
			}

			List<ProcessoLog> list = processoLogService.findByIds(ids2);

			for (int i = 0; i < list.size(); i++) {

				ProcessoLog ml = list.get(i);

				ew.criaLinha(sheet, rowNum++);

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

	private void renderBody(ExcelWriter ew, ProcessoLog ml) {

		Long id = ml.getId();
		ew.escrever(id);

		Processo processo = ml.getProcesso();
		Long processoId = processo.getId();
		ew.escrever(processoId);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		String tipoProcessoNome = tipoProcesso != null ? tipoProcesso.getNome() : "";
		ew.escrever(tipoProcessoNome);

		//		String numeroManifestacao = processo.getNumeroManifestacao();
		//		ew.escrever(numeroManifestacao);

		String numero = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CPF_CNPJ);
		ew.escrever(numero);

		//		Canal canal = processo.getCanal();
		//		String canalDescricao = canal != null ? canal.getDescricao() : null;
		//		ew.escrever(canalDescricao);

		//		SubMotivo subMotivo = processo.getSub Motivo();
		//		Motivo motivo = subMotivo != null ? subMotivo.getMotivo() : null;
		//		String motivoDescricao = motivo != null ? motivo.getDescricao() : "";
		//		ew.escrever(motivoDescricao);

		//		String subMotivoDescricao = subMotivo != null ? subMotivo.getDescricao() : "";
		//		ew.escrever(subMotivoDescricao);

		StatusProcesso status = processo.getStatus();
		String statusStr = resourceService.getValue("StatusProcesso." + status.name() + ".label");
		ew.escrever(statusStr);

		Date dataCriacao = processo.getDataCriacao();
		ew.escreverDateTime(dataCriacao);

		Usuario analista = processo.getAnalista();
		String analistaNome = analista != null ? analista.getNome() : "";
		ew.escrever(analistaNome);

		AcaoProcesso acao = ml.getAcao();
		String acaoStr = resourceService.getValue("AcaoProcesso." + acao.name() + ".label");
		ew.escrever(acaoStr);

		Date data = ml.getData();
		ew.escreverDate(data);

		ew.escreverTime(data);

		Usuario usuario = ml.getUsuario();
		String usuarioNome = usuario != null ? usuario.getNome() : "";
		ew.escrever(usuarioNome);

		String usuarioLogin = usuario != null ? usuario.getLogin() : "";
		ew.escrever(usuarioLogin);

		ProcessoLog mlAnterior = processoLogService.findAlteracaoSituacaoAnterior(ml);
		if (mlAnterior != null) {
			Situacao situacaoDe = mlAnterior.getSituacaoAnterior();
			String situacaoDeNome = situacaoDe != null ? situacaoDe.getNome() : "";
			ew.escrever(situacaoDeNome);

			Situacao situacaoPara = mlAnterior.getSituacao();
			String situacaoParaNome = situacaoPara != null ? situacaoPara.getNome() : "";
			ew.escrever(situacaoParaNome);
		} else {
			ew.escrever("");
			ew.escrever("");
		}

		TipoEvidencia tipoEvidencia = ml.getTipoEvidencia();
		String tipoEvidenciaDescricao = tipoEvidencia != null ? tipoEvidencia.getDescricao() : "";
		ew.escrever(tipoEvidenciaDescricao);

		String areaDescricao = "";
		Solicitacao solicitacao = ml.getSolicitacao();
		if (solicitacao != null) {
			Area area = solicitacao.getSubarea().getArea();
			areaDescricao = area != null ? area.getDescricao() : "";
		}
		ew.escrever(areaDescricao);

		String observacao = ml.getObservacao();
		ew.escrever(observacao);
	}
}
