package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.bean.datamodel.ConfiguracaoGeracaoRelatorioDataModel;
import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.ConfiguracaoGeracaoRelatorio;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoConfiguracaoRelatorio;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.ConfiguracaoGeracaoRelatorioService;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.ConfiguracaoGeracaoRelatorioFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.*;
import static org.apache.commons.collections4.CollectionUtils.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@ManagedBean
@ViewScoped
public class ConfiguracaoGeracaoRelatorioBean extends AbstractBean {

	@Autowired private ConfiguracaoGeracaoRelatorioService configuracaoGeracaoRelatorioService;
	@Autowired private BaseRegistroService baseRegistroService;

	@ManagedProperty(value = "#{relatorioPendenciaDocumentoFiltroBean}")
	private RelatorioPendenciaDocumentoFiltroBean relatorioPendenciaDocumentoFiltroBean;

	private RelatorioPendenciaDocumentoFiltro relatorioPendenciaDocumentoFiltro;
	private ConfiguracaoGeracaoRelatorio configuracaoGeracaoRelatorio;
	private ConfiguracaoGeracaoRelatorioDataModel geracaoRelatorioDataModel;
	private ConfiguracaoGeracaoRelatorioFiltro filtro = new ConfiguracaoGeracaoRelatorioFiltro();
	private List<String> todasRegionais = new ArrayList<>(0);

	@Override
	protected void initBean() {

		Usuario usuarioLogado = getUsuarioLogado();

		List<RegistroValorVO> regionais = baseRegistroService.findByBaseInterna(BaseInterna.REGIONAL_ID);
		todasRegionais = regionais.stream().map(RegistroValorVO::getChaveUnicidade).collect(Collectors.toList());

		relatorioPendenciaDocumentoFiltro = new RelatorioPendenciaDocumentoFiltro();
		relatorioPendenciaDocumentoFiltro.setApenasPendentes(true);
		relatorioPendenciaDocumentoFiltro.setSituacaoAluno(true);
		relatorioPendenciaDocumentoFiltro.setTipo(RelatorioPendenciaDocumentoFiltro.Tipo.TODOS);

		if (usuarioLogado.isAdminRole() || usuarioLogado.isGestorRole()) {

			geracaoRelatorioDataModel = new ConfiguracaoGeracaoRelatorioDataModel();
			geracaoRelatorioDataModel.setFiltro(filtro);
			geracaoRelatorioDataModel.setService(configuracaoGeracaoRelatorioService);
		}
	}

	public void salvarConfiguracaoGeracaoRelatorio() {

		try {

			relatorioPendenciaDocumentoFiltro.setTodasRegionais(todasRegionais);

			boolean insert = isInsert(configuracaoGeracaoRelatorio);

			configuracaoGeracaoRelatorioService.salvarConfiguracao(configuracaoGeracaoRelatorio, relatorioPendenciaDocumentoFiltro, getUsuarioLogado());

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void editar(ConfiguracaoGeracaoRelatorio cgr) {

		String opcoes = cgr.getOpcoes();
		if (isNotBlank(opcoes)) {

			RelatorioPendenciaDocumentoFiltro rpdFiltro = DummyUtils.jsonToObject(opcoes, RelatorioPendenciaDocumentoFiltro.class);

			if (rpdFiltro != null) {

				List<String> regionais = rpdFiltro.getRegionais();
				if (isNotEmpty(regionais)) {
					relatorioPendenciaDocumentoFiltroBean.findCampus(regionais);
				}

				List<String> campus = rpdFiltro.getCampus();
				if (isNotEmpty(campus)) {
					relatorioPendenciaDocumentoFiltroBean.findCursos(campus);
				}
			}
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();
		Long configId = configuracaoGeracaoRelatorio.getId();

		try {
			configuracaoGeracaoRelatorioService.excluir(configId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void duplicar() {

		try {
			Usuario usuario = getUsuarioLogado();
			configuracaoGeracaoRelatorioService.duplicar(configuracaoGeracaoRelatorio, usuario);

			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void executarRelatorio() {

		try {

			configuracaoGeracaoRelatorioService.gerarRelatorio(configuracaoGeracaoRelatorio, getUsuarioLogado());

			addFaceMessage(FacesMessage.SEVERITY_INFO, "Execução iniciada.");
		}
		catch (Exception e) {

			e.printStackTrace();
			addMessageError(e);
		}
	}

	public ConfiguracaoGeracaoRelatorio getConfiguracaoGeracaoRelatorio() {

		if (configuracaoGeracaoRelatorio == null) {
			configuracaoGeracaoRelatorio = new ConfiguracaoGeracaoRelatorio();
		}

		return configuracaoGeracaoRelatorio;
	}

	public void setConfiguracaoGeracaoRelatorio(ConfiguracaoGeracaoRelatorio configuracaoGeracaoRelatorio) {

		if (configuracaoGeracaoRelatorio == null) {

			configuracaoGeracaoRelatorio = new ConfiguracaoGeracaoRelatorio();
			configuracaoGeracaoRelatorio.setTipo(TipoConfiguracaoRelatorio.RELATORIO_PENDENCIA_DOCUMENTO);

			relatorioPendenciaDocumentoFiltro = new RelatorioPendenciaDocumentoFiltro();

			relatorioPendenciaDocumentoFiltroBean.findCampus(emptyList());
			relatorioPendenciaDocumentoFiltroBean.findCursos(emptyList());
		}
		else if (isNotBlank(configuracaoGeracaoRelatorio.getOpcoes())) {

			String opcoes = configuracaoGeracaoRelatorio.getOpcoes();
			RelatorioPendenciaDocumentoFiltro rpdFiltro = DummyUtils.jsonToObject(opcoes, RelatorioPendenciaDocumentoFiltro.class);

			if (rpdFiltro != null) {
				relatorioPendenciaDocumentoFiltro = rpdFiltro;
			}
		}

		this.configuracaoGeracaoRelatorio = configuracaoGeracaoRelatorio;
	}

	public ConfiguracaoGeracaoRelatorioDataModel getGeracaoRelatorioDataModel() {
		return geracaoRelatorioDataModel;
	}

	public RelatorioPendenciaDocumentoFiltro getRelatorioPendenciaDocumentoFiltro() {
		return relatorioPendenciaDocumentoFiltro;
	}

	public void setRelatorioPendenciaDocumentoFiltro(RelatorioPendenciaDocumentoFiltro relatorioPendenciaDocumentoFiltro) {
		this.relatorioPendenciaDocumentoFiltro = relatorioPendenciaDocumentoFiltro;
	}

	@SuppressWarnings("unused")
	public void setRelatorioPendenciaDocumentoFiltroBean(RelatorioPendenciaDocumentoFiltroBean relatorioPendenciaDocumentoFiltroBean) {
		this.relatorioPendenciaDocumentoFiltroBean = relatorioPendenciaDocumentoFiltroBean;
	}

	public ConfiguracaoGeracaoRelatorioFiltro getFiltro() {
		return filtro;
	}
}
