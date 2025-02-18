package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.RelatorioGeralDataModel;
import net.wasys.getdoc.bean.datamodel.RelatorioGeralEtapaDataModel;
import net.wasys.getdoc.bean.datamodel.RelatorioGeralHistoricoDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.DownloadVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro.ConsiderarData;
import net.wasys.getdoc.job.RelatorioGeralJob;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean
@SessionScoped
public class RelatorioGeralBean extends AbstractBean {

	@Autowired private RelatorioGeralService relatorioGeralService;
	@Autowired private RelatorioGeralEtapaService relatorioGeralEtapaService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ParametroService parametroService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ResourceService resourceService;
	@Autowired private BaseRegistroService baseRegistroService;

	private RelatorioGeralHistoricoDataModel historicoDataModel;
	private Long usuarioId;
	private RelatorioGeralDataModel ativosDataModel;
	private RelatorioGeralEtapaDataModel relatorioGeralEtapaDataModel;
	private RelatorioGeralExporter exporter;

	private List<TipoProcesso> tiposProcessos;
	private List<RelatorioGeralFiltro.Tipo> tipoRelatorioGeralEnumList;
	private List<Situacao> situacoes;
	private RelatorioGeralFiltro filtro = new RelatorioGeralFiltro();
	private String dataUltimaExecucao;
	private Boolean executando;
	private File[] relatoriosSalvosPorData;
	private TipoProcesso tipoProcessoCampos;
	private Boolean permissaoFiltroTP;
	private RelatorioGeralDataModel relatorioGeralDataModel;

	protected void initBean() {

		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcessos = tipoProcessoService.findAtivosAndInitialize(permissoes);

		dataUltimaExecucao = parametroService.getValor(ParametroService.P.ULTIMA_DATA_RELATORIO_GERAL);

		if(situacoes == null) {
			situacoes = situacaoService.findAtivas(null);
			Collections.sort(situacoes, new SuperBeanComparator<>("tipoProcesso.nome, nome"));
		}

		executando = TransactionWrapperJob.isExecutando(RelatorioGeralJob.class);

		Date date = new Date();
		filtro.setDataInicio(date);
		filtro.setDataFim(date);
		filtro.setConsiderarData(ConsiderarData.CRIACAO);

		ativosDataModel = new RelatorioGeralDataModel();
		ativosDataModel.setFiltro(filtro);
		ativosDataModel.setService(relatorioGeralService);

		relatorioGeralEtapaDataModel = new RelatorioGeralEtapaDataModel();
		relatorioGeralEtapaDataModel.setFiltro(filtro);
		relatorioGeralEtapaDataModel.setService(relatorioGeralEtapaService);

		relatorioGeralDataModel = new RelatorioGeralDataModel();
		relatorioGeralDataModel.setFiltro(filtro);
		relatorioGeralDataModel.setService(relatorioGeralService);

		RelatorioGeralFiltro.Tipo[] tipos = RelatorioGeralFiltro.Tipo.values();
		tipoRelatorioGeralEnumList = Arrays.asList(tipos);

		File diretorio = new File(resourceService.getValue(ResourceService.RELATORIO_GERAL_PATH));
		this.relatoriosSalvosPorData = diretorio.listFiles();
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
				String fileName = "relatorio-geral.xlsx";
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

	public void exportar() {

		if(exporter == null) {
			exporter = applicationContext.getBean(RelatorioGeralExporter.class);
			exporter.setFiltro(filtro);
			Usuario usuarioLogado = getUsuarioLogado();
			exporter.setUsuario(usuarioLogado);
			exporter.start();
		}
	}

	public void baixarRelatorioSalvo(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			Faces.sendFile(fis, file.getName(), false);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public StreamedContent downloadDocumento(File file) {
		try {
			DownloadVO vo = new DownloadVO();
			vo.setFile(file);
			vo.setFileName(file.getName());

			DummyUtils.deleteOnExitFile(file);
			String fileName = vo.getFileName();

			FileInputStream fis = new FileInputStream(file);
			DefaultStreamedContent dsc =
					DefaultStreamedContent.builder()
							.contentType("application/csv")
							.name(fileName)
							.stream(() -> fis)
							.build();
			return dsc;
		}
		catch (Exception e) {
			addMessageError(e);
			return null;
		}
	}
	public void bloqueiaTiposProcessos(ValueChangeEvent event) {

		tipoProcessoCampos = (TipoProcesso) event.getNewValue();

		if (tipoProcessoCampos != null) {
			permissaoFiltroTP = true;
			filtro.setTiposProcesso(null);
		}
		else {
			permissaoFiltroTP = false;
			filtro.setCamposFiltro(null);
		}
	}

	public void showHistorico() {
		historicoDataModel = new RelatorioGeralHistoricoDataModel();
		String diretorio = resourceService.getValue(ResourceService.RELATORIO_GERAL_HISTORICO_PATH);
		historicoDataModel.setDiretorio(diretorio);
	}

	public boolean isTipoEtapa() {
		RelatorioGeralFiltro.Tipo tipo = filtro.getTipo();
		return RelatorioGeralFiltro.Tipo.ETAPAS.equals(tipo);
	}

	public RelatorioGeralDataModel getAtivosDataModel() {
		return ativosDataModel;
	}

	public RelatorioGeralEtapaDataModel getRelatorioGeralEtapaDataModel() {
		return relatorioGeralEtapaDataModel;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public RelatorioGeralFiltro getFiltro() {
		return filtro;
	}

	public void confirmarFiltros() {

		try {
			filtro.configuraTipoProcessoCampos(tipoProcessoCampos, baseRegistroService);

			setRequestAttribute("fecharModal", true);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void buscar() {
		List<TipoProcesso> tipoProcessos = filtro.getTiposProcesso();
		if(tipoProcessos == null || tipoProcessos.isEmpty()){
			relatorioGeralDataModel.setBuscar(true);
		}else{
			filtro.setTiposProcessoList(tipoProcessos);
			filtro.setCampoFiltro(true);
			relatorioGeralDataModel.setBuscar(true);
		}
	}

	public ConsiderarData[] getConsiderarDatas() {
		return ConsiderarData.values();
	}

	public void setFiltro(RelatorioGeralFiltro filtro) {
		this.filtro = filtro;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public RelatorioGeralExporter getExporter() {
		return exporter;
	}

	public String getDataUltimaExecucao() {
		return dataUltimaExecucao;
	}

	public Boolean getExecutando() {
		return executando;
	}

	public List<Situacao> getSituacoes() { return situacoes; }

	public List<RelatorioGeralFiltro.Tipo> getTipoRelatorioGeralEnumList() {
		return tipoRelatorioGeralEnumList;
	}

	public File[] getRelatoriosSalvosPorData() {
		return relatoriosSalvosPorData;
	}

	public RelatorioGeralHistoricoDataModel getHistoricoDataModel() {
		return historicoDataModel;
	}

	public TipoProcesso getTipoProcessoCampos() {
		return tipoProcessoCampos;
	}

	public void setTipoProcessoCampos(TipoProcesso tipoProcessoCampos) {
		this.tipoProcessoCampos = tipoProcessoCampos;
	}

	public Boolean getPermissaoFiltroTP() {
		return permissaoFiltroTP;
	}

	public void setPermissaoFiltroTP(Boolean permissaoFiltroTP) {
		this.permissaoFiltroTP = permissaoFiltroTP;
	}

	public void limpar() {
		tipoProcessoCampos = null;
		permissaoFiltroTP = null;
		tiposProcessos = null;
		Date date = getDateInicialDefault();

		filtro = new RelatorioGeralFiltro();
		filtro.setDataInicio(date);
		filtro.setDataFim(new Date());
		relatorioGeralDataModel.setFiltro(filtro);
		relatorioGeralDataModel.setBuscar(false);
		initBean();
	}

	private Date getDateInicialDefault() {

		Date date = null;
		try {
			SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy");
			date = filtro.getDataInicio();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return date;
	}
}
