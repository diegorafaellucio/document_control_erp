package net.wasys.getdoc.bean;

import com.sun.faces.util.CollectionsUtils;
import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.entity.LogAnaliseIA;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.service.LogAnaliseIAService;
import net.wasys.getdoc.domain.service.LogAnaliseIAExporter;
import net.wasys.getdoc.domain.vo.filtro.LogAnaliseIAFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.highcharts.columnstackchart.ColumnStackedChartVO;
import net.wasys.util.highcharts.columnstackchart.ColumnStackedSeriesVO;
import net.wasys.util.highcharts.piechart.PieChartDataVO;
import net.wasys.util.highcharts.piechart.PieChartSeriesVO;
import net.wasys.util.highcharts.piechart.PieChartVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ManagedBean
@SessionScoped
public class RelatorioLogAnaliseIABean extends AbstractBean {

	@Autowired private LogAnaliseIAService logAnaliseIAService;
	@Autowired private ApplicationContext applicationContext;

	private LogAnaliseIAFiltro logAnaliseIAFiltro = new LogAnaliseIAFiltro();

	private Integer totalProcessos;
	private Integer totalDocumentos;
	private Integer totalDocumentosTipificados;
	private Integer totalDocumentosOCR;

	private LogAnaliseIAExporter exporter;

	protected void initBean() {

		Date date = new Date();
		logAnaliseIAFiltro.setDataInicio(date);
		logAnaliseIAFiltro.setStatusDocumentoList(Arrays.asList(StatusDocumento.DIGITALIZADO, StatusDocumento.APROVADO, StatusDocumento.PENDENTE));
		logAnaliseIAFiltro.setDataFim(date);
		logAnaliseIAFiltro.setConsiderarData(LogAnaliseIAFiltro.ConsiderarData.CRIACAO);
		logAnaliseIAFiltro.setTipoDocumentoIdList(TipoDocumento.DOCUMENTOS_OCR);
		buscar();

	}

	public LogAnaliseIAFiltro.ConsiderarData[] getConsiderarDatas() {
		return LogAnaliseIAFiltro.ConsiderarData.values();
	}

	public LogAnaliseIAFiltro getLogAnaliseIAFiltro() {
		return logAnaliseIAFiltro;
	}

	public void setLogAnaliseIAFiltro(LogAnaliseIAFiltro logAnaliseIAFiltro) {
		this.logAnaliseIAFiltro = logAnaliseIAFiltro;
	}

	public void buscar(){
		List<LogAnaliseIA> list = logAnaliseIAService.findByFiltro(logAnaliseIAFiltro);

		List<LogAnaliseIA> processos = list.stream().filter(distinctByKey(logAnaliseIA -> logAnaliseIA.getProcesso().getId())).collect(Collectors.toList());
		List<LogAnaliseIA> documentos = list.stream().filter(distinctByKey(logAnaliseIA -> logAnaliseIA.getDocumento().getId())).collect(Collectors.toList());
		List<LogAnaliseIA> documentosTipificados = list.stream().filter(logAnaliseIA -> logAnaliseIA.isTipificou()).collect(Collectors.toList());
		List<LogAnaliseIA> documentosOCR = list.stream().filter(logAnaliseIA -> logAnaliseIA.isOcr()).collect(Collectors.toList());

		totalProcessos = processos.size();
		totalDocumentos = documentos.size();
		totalDocumentosTipificados = documentosTipificados.size();
		totalDocumentosOCR = documentosOCR.size();

		gerarGraficoStatusProcesso(processos);
		gerarGraficoStatusTipificacao(documentos);
		gerarGraficoStatusOCR(documentosTipificados);
		gerarGraficoStatusDocumentos(documentosOCR);

		gerarGraficoAcompanhamentoProcessos(processos);

	}

	private void gerarGraficoAcompanhamentoProcessos(List<LogAnaliseIA> list) {
		LocalDate dataInicio = DummyUtils.dateToLocalDate(logAnaliseIAFiltro.getDataInicio());
		LocalDate dataFinal = DummyUtils.dateToLocalDate(logAnaliseIAFiltro.getDataFim());

		ColumnStackedChartVO columnStackedChartVO = new ColumnStackedChartVO();
		List<String> categories = new ArrayList<>();
		List<Long> analises = new ArrayList<>();
		List<Long> pendentes = new ArrayList<>();
		List<Long> aprovados = new ArrayList<>();
		for (LocalDate data = dataInicio; data.isBefore(dataFinal) || data.isEqual(dataFinal); data = data.plusDays(1)) {
			LocalDate dataFiltroInicio = data;
			categories.add(DummyUtils.formatLocalDate(data));
			Map<String, List<LogAnaliseIA>> statusProcessoDia = list.stream().filter(logAnaliseIA -> DummyUtils.dateToLocalDate(logAnaliseIA.getData()).isEqual(dataFiltroInicio)).collect(Collectors.groupingBy(LogAnaliseIA::getStatusProcesso));

			List<LogAnaliseIA> analise = statusProcessoDia.get("1.1 - Em Análise");
			List<LogAnaliseIA> pendente = statusProcessoDia.get("2.0 - Documentação Pendente");
			List<LogAnaliseIA> aprovado = statusProcessoDia.get("4.0 - Documentação Aprovada");

			analises.add(CollectionUtils.isEmpty(analise) ? 0L :  analise.size());
			pendentes.add(CollectionUtils.isEmpty(pendente) ? 0L :  pendente.size());
			aprovados.add(CollectionUtils.isEmpty(aprovado) ? 0L :  aprovado.size());
		}

		ColumnStackedSeriesVO columnStackedSeriesAnalise = new ColumnStackedSeriesVO();
		columnStackedSeriesAnalise.setName("1.1 - Em Análise");
		columnStackedSeriesAnalise.setData(analises);
		ColumnStackedSeriesVO columnStackedSeriesPendente = new ColumnStackedSeriesVO();
		columnStackedSeriesPendente.setName("2.0 - Documentação Pendente");
		columnStackedSeriesPendente.setData(pendentes);
		ColumnStackedSeriesVO columnStackedSeriesAprovado = new ColumnStackedSeriesVO();
		columnStackedSeriesAprovado.setName("4.0 - Documentação Aprovada");
		columnStackedSeriesAprovado.setData(aprovados);

		columnStackedChartVO.setCategories(categories);

		List<ColumnStackedSeriesVO> columnStackedSeriesVOS = new ArrayList<>();
		columnStackedSeriesVOS.add(columnStackedSeriesAnalise);
		columnStackedSeriesVOS.add(columnStackedSeriesPendente);
		columnStackedSeriesVOS.add(columnStackedSeriesAprovado);
		columnStackedChartVO.setSeries(columnStackedSeriesVOS);

		String columnStackedChartVOJson = DummyUtils.objectToJson(columnStackedChartVO);

		Ajax.oncomplete("gerarGraficoAcompanhamentoProcessos(" + columnStackedChartVOJson + ")");
	}

	private void gerarGraficoStatusProcesso(List<LogAnaliseIA> list) {
		Map<String, List<LogAnaliseIA>> map = list.stream().collect(Collectors.groupingBy(LogAnaliseIA::getStatusProcesso));

		PieChartVO pieChartData = new PieChartVO();
		pieChartData.setTitle("Total Processos - " + list.size());

		PieChartSeriesVO pieChartSeriesVO = new PieChartSeriesVO();
		pieChartSeriesVO.setColorByPoint(true);
		pieChartSeriesVO.setName("Processos");

		List<PieChartDataVO> pieChartDataVOS = new ArrayList<>();

		for (Map.Entry<String, List<LogAnaliseIA>> entry : map.entrySet()) {
			PieChartDataVO pieChartDataVO = new PieChartDataVO();
			pieChartDataVO.setName(entry.getKey());
			pieChartDataVO.setY(new Double(entry.getValue().size()));

			pieChartDataVOS.add(pieChartDataVO);
		}

		pieChartSeriesVO.setData(pieChartDataVOS);
		List<PieChartSeriesVO> pieChartSeriesVOS = new ArrayList<>();
		pieChartSeriesVOS.add(pieChartSeriesVO);

		pieChartData.setSeries(pieChartSeriesVOS);

		Ajax.oncomplete("gerarGraficoStatusProcesso(" + DummyUtils.objectToJson(pieChartData) + ")");
	}

	private void gerarGraficoStatusTipificacao(List<LogAnaliseIA> list) {
		Map<Boolean, List<LogAnaliseIA>> map = list.stream().collect(Collectors.groupingBy(LogAnaliseIA::isTipificou));

		PieChartVO pieChartData = new PieChartVO();
		pieChartData.setTitle("Total Documentos - " + list.size());

		PieChartSeriesVO pieChartSeriesVO = new PieChartSeriesVO();
		pieChartSeriesVO.setColorByPoint(true);
		pieChartSeriesVO.setName("Tipificações");

		List<PieChartDataVO> pieChartDataVOS = new ArrayList<>();

		for (Map.Entry<Boolean, List<LogAnaliseIA>> entry : map.entrySet()) {
			PieChartDataVO pieChartDataVO = new PieChartDataVO();
			pieChartDataVO.setName(entry.getKey() ? "Tipificados" : "Tipificação com falha");
			pieChartDataVO.setY(new Double(entry.getValue().size()));

			pieChartDataVOS.add(pieChartDataVO);
		}

		pieChartSeriesVO.setData(pieChartDataVOS);
		List<PieChartSeriesVO> pieChartSeriesVOS = new ArrayList<>();
		pieChartSeriesVOS.add(pieChartSeriesVO);

		pieChartData.setSeries(pieChartSeriesVOS);

		Ajax.oncomplete("gerarGraficoStatusTipificacao(" + DummyUtils.objectToJson(pieChartData) + ")");
	}

	private void gerarGraficoStatusOCR(List<LogAnaliseIA> list) {
		Map<Boolean, List<LogAnaliseIA>> map = list.stream().collect(Collectors.groupingBy(LogAnaliseIA::isOcr));

		PieChartVO pieChartData = new PieChartVO();
		pieChartData.setTitle("Documentos Tipificados - " + list.size());

		PieChartSeriesVO pieChartSeriesVO = new PieChartSeriesVO();
		pieChartSeriesVO.setColorByPoint(true);
		pieChartSeriesVO.setName("OCR");

		List<PieChartDataVO> pieChartDataVOS = new ArrayList<>();

		for (Map.Entry<Boolean, List<LogAnaliseIA>> entry : map.entrySet()) {
			PieChartDataVO pieChartDataVO = new PieChartDataVO();
			pieChartDataVO.setName(entry.getKey() ? "OCR realizado" : "OCR não realizado");
			pieChartDataVO.setY(new Double(entry.getValue().size()));

			pieChartDataVOS.add(pieChartDataVO);
		}

		pieChartSeriesVO.setData(pieChartDataVOS);
		List<PieChartSeriesVO> pieChartSeriesVOS = new ArrayList<>();
		pieChartSeriesVOS.add(pieChartSeriesVO);

		pieChartData.setSeries(pieChartSeriesVOS);

		Ajax.oncomplete("gerarGraficoStatusOCR(" + DummyUtils.objectToJson(pieChartData) + ")");
	}

	private void gerarGraficoStatusDocumentos(List<LogAnaliseIA> list) {
		Map<String, List<LogAnaliseIA>> map = list.stream().collect(Collectors.groupingBy(LogAnaliseIA::getStatusDocumento));

		PieChartVO pieChartData = new PieChartVO();
		pieChartData.setTitle("Total documentos com OCR - " + list.size());

		PieChartSeriesVO pieChartSeriesVO = new PieChartSeriesVO();
		pieChartSeriesVO.setColorByPoint(true);
		pieChartSeriesVO.setName("Status Documento");

		List<PieChartDataVO> pieChartDataVOS = new ArrayList<>();

		for (Map.Entry<String, List<LogAnaliseIA>> entry : map.entrySet()) {
			PieChartDataVO pieChartDataVO = new PieChartDataVO();
			pieChartDataVO.setName(entry.getKey());
			pieChartDataVO.setY(new Double(entry.getValue().size()));

			pieChartDataVOS.add(pieChartDataVO);
		}

		pieChartSeriesVO.setData(pieChartDataVOS);
		List<PieChartSeriesVO> pieChartSeriesVOS = new ArrayList<>();
		pieChartSeriesVOS.add(pieChartSeriesVO);

		pieChartData.setSeries(pieChartSeriesVOS);

		Ajax.oncomplete("gerarGraficoStatusDocumentos(" + DummyUtils.objectToJson(pieChartData) + ")");
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
	{
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	public Integer getTotalProcessos() {
		return totalProcessos;
	}

	public void setTotalProcessos(Integer totalProcessos) {
		this.totalProcessos = totalProcessos;
	}

	public Integer getTotalDocumentos() {
		return totalDocumentos;
	}

	public void setTotalDocumentos(Integer totalDocumentos) {
		this.totalDocumentos = totalDocumentos;
	}

	public Integer getTotalDocumentosTipificados() {
		return totalDocumentosTipificados;
	}

	public void setTotalDocumentosTipificados(Integer totalDocumentosTipificados) {
		this.totalDocumentosTipificados = totalDocumentosTipificados;
	}

	public Integer getTotalDocumentosOCR() {
		return totalDocumentosOCR;
	}

	public void setTotalDocumentosOCR(Integer totalDocumentosOCR) {
		this.totalDocumentosOCR = totalDocumentosOCR;
	}

	public void exportar() {

		if(exporter == null) {
			exporter = applicationContext.getBean(LogAnaliseIAExporter.class);
			exporter.setFiltro(logAnaliseIAFiltro);
			Usuario usuarioLogado = getUsuarioLogado();
			exporter.setUsuario(usuarioLogado);
			exporter.start();
		}
	}

	public void verificar() {

		if(exporter == null) {
			return;
		}

		if(exporter.isFinalizado()) {
			Ajax.data("terminou", true);
		}
		else {
			Ajax.data("terminou", false);
		}
	}

	public void baixar() {

		Exception error = exporter.getError();
		if(error != null) {
			addMessageError(error);
		}
		else {
			File file = exporter.getFile();
			try {
				FileInputStream fis = new FileInputStream(file);
				String fileName = "relatorio-log-analise-ia.xlsx";
				String fileName2 = exporter.getFileName();
				if(StringUtils.isNotBlank(fileName2)) {
					fileName = fileName2;
				}
				Faces.sendFile(fis, fileName, true);
			}
			catch (Exception e1) {
				addMessageError(e1);
			}
		}

		exporter = null;
	}
}
