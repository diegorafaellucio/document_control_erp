package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.bean.datamodel.ExecucaoGeracaoRelatorioVoDataModel;
import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.ExecucaoGeracaoRelatorioService;
import net.wasys.getdoc.domain.service.RelatorioPendenciaDocumentoExporter;
import net.wasys.getdoc.domain.service.RelatorioPendenciaDocumentoService;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.RelatorioPendenciaDocumentoVO;
import net.wasys.getdoc.domain.vo.filtro.ExecucaoGeracaoRelatorioFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.primefaces.model.DefaultStreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@ManagedBean
@SessionScoped
public class RelatorioPendenciaDocumentoBean extends AbstractBean {

	@Autowired private RelatorioPendenciaDocumentoService relatorioPendenciaDocumentoService;
	@Autowired private ExecucaoGeracaoRelatorioService execucaoGeracaoRelatorioService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private BaseRegistroService baseRegistroService;

	private List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS;
	private RelatorioPendenciaDocumentoFiltro filtro;
	private RelatorioPendenciaDocumentoExporter exporter;
	private RelatorioPendenciaDocumentoVO documentoSelecionadoVO;
	private ExecucaoGeracaoRelatorioVoDataModel execucaoGeracaoRelatorioDataModel;
	private List<String> todasRegionais = new ArrayList<>(0);

	@Override
	protected void initBean() {

		relatorioPendenciaDocumentoVOS = new ArrayList<>();

		List<RegistroValorVO> regionais = baseRegistroService.findByBaseInterna(BaseInterna.REGIONAL_ID);
		todasRegionais = regionais.stream().map(RegistroValorVO::getChaveUnicidade).collect(Collectors.toList());

		filtro = new RelatorioPendenciaDocumentoFiltro();
		filtro.setTodasRegionais(todasRegionais);
		filtro.setApenasPendentes(true);
		filtro.setSituacaoAluno(true);
		filtro.setTipo(RelatorioPendenciaDocumentoFiltro.Tipo.TODOS);

		execucaoGeracaoRelatorioDataModel = new ExecucaoGeracaoRelatorioVoDataModel();
		execucaoGeracaoRelatorioDataModel.setFiltro(new ExecucaoGeracaoRelatorioFiltro());
		execucaoGeracaoRelatorioDataModel.setService(execucaoGeracaoRelatorioService);
	}

	public void buscar() {
		if (validarInformacoesFiltro()) {
			filtro.setPagina(true);
			relatorioPendenciaDocumentoVOS = relatorioPendenciaDocumentoService.findRelatorio(filtro);
		}
	}

	private boolean validarInformacoesFiltro() {

		List<String> regionais = filtro.getRegionais();
		String cpf = filtro.getCpf();
		String matricula = filtro.getMatricula();

		if (isEmpty(regionais) && isBlank(cpf) && isBlank(matricula)) {
			addMessageError("erroPesquisaDocumentosPendentes.error.selecionar");
			return false;
		}

		return true;
	}

	public void exportar() {
		Usuario usuarioLogado = getUsuarioLogado();
		if (validarInformacoesFiltro()) {
			filtro.setPagina(false);
			if (exporter == null) {
				exporter = applicationContext.getBean(RelatorioPendenciaDocumentoExporter.class);
				exporter.setFiltro(filtro);
				exporter.setUsuario(usuarioLogado);
				exporter.start();
			}
		}
	}

	public void baixar() {

		Exception error = exporter.getException();
		if(error != null) {
			addMessageError(error);
		}
		else {
			Map<String, File> mapFile = exporter.getMapFile();
			try {
				if(mapFile == null || mapFile.isEmpty()) return;
				String fileName2 = mapFile.keySet().stream().findFirst().get();
				File file = mapFile.get(fileName2);

				FileInputStream fis = new FileInputStream(file);
				String fileName = "relatorio-pendencias-documentos-alunos.xlsx";
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

	public DefaultStreamedContent baixarRelatorioGerado(Long execucaoGeracaoRelatorioId) {

		try {

			File tempFile = execucaoGeracaoRelatorioService.findArquivoTemporarioByExecucaoId(execucaoGeracaoRelatorioId);
			DummyUtils.deleteOnExitFile(tempFile);

			FileInputStream fis = new FileInputStream(tempFile);

			String tempFileName = tempFile.getName();
			tempFileName = tempFileName.replaceAll("(_\\d*)", "");

			DefaultStreamedContent dsc  = DefaultStreamedContent.builder()
					.contentType("application/octet-stream; charset=UTF-8")
					.name(tempFileName)
					.stream(() -> fis)
					.build();

			return dsc;
		}
		catch (Exception e) {

			e.printStackTrace();
			addMessageError(e);
		}

		return null;
	}

	public void verificar() {

		if (exporter == null) {
			Ajax.data("terminou", null);
		}
		else if (exporter.isFinalizado()) {
			Ajax.data("terminou", true);
		}
		else {
			Ajax.data("terminou", false);
		}
	}

	public RelatorioPendenciaDocumentoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(RelatorioPendenciaDocumentoFiltro filtro) {
		this.filtro = filtro;
	}

	public RelatorioPendenciaDocumentoExporter getExporter() {
		return exporter;
	}

	public void setExporter(RelatorioPendenciaDocumentoExporter exporter) {
		this.exporter = exporter;
	}


	public List<RelatorioPendenciaDocumentoVO> getRelatorioPendenciaDocumentoVOS() {
		return relatorioPendenciaDocumentoVOS;
	}

	public RelatorioPendenciaDocumentoVO getDocumentoSelecionadoVO() {
		return documentoSelecionadoVO;
	}

	public void setDocumentoSelecionadoVO(RelatorioPendenciaDocumentoVO documentoSelecionadoVO) {
		this.documentoSelecionadoVO = documentoSelecionadoVO;
	}

	public ExecucaoGeracaoRelatorioVoDataModel getExecucaoGeracaoRelatorioDataModel() {
		return execucaoGeracaoRelatorioDataModel;
	}
}