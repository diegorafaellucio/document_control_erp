package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.ConfiguracaoLoginAzureDataModel;
import net.wasys.getdoc.domain.entity.ConfiguracaoLoginAzure;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.ConfiguracaoLoginAzureService;
import net.wasys.getdoc.domain.service.SubperfilService;
import net.wasys.getdoc.domain.vo.filtro.ConfiguracaoLoginAzureFiltro;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

@ManagedBean
@ViewScoped
public class ConfiguracaoLoginAzureBean extends AbstractBean {

	@Autowired private ConfiguracaoLoginAzureService configuracaoLoginAzureService;
	@Autowired private SubperfilService subperfilService;

	private ConfiguracaoLoginAzureDataModel dataModel;
	private ConfiguracaoLoginAzureFiltro filtro = new ConfiguracaoLoginAzureFiltro();
	private ConfiguracaoLoginAzure configuracaoLoginAzure;
	private List<Subperfil> subperfilList;

	protected void initBean() {

		dataModel = new ConfiguracaoLoginAzureDataModel();
		dataModel.setFiltro(filtro);
		dataModel.setService(configuracaoLoginAzureService);

		subperfilList = subperfilService.findAll();
	}

	public ConfiguracaoLoginAzureDataModel getDataModel() {
		return dataModel;
	}

	public ConfiguracaoLoginAzureFiltro getFiltro() {
		return filtro;
	}

	public void salvar() {

		try {
			boolean insert = isInsert(configuracaoLoginAzure);
			Usuario usuario = getUsuarioLogado();

			configuracaoLoginAzureService.saveOrUpdate(configuracaoLoginAzure, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long configuracaoLoginAzureId = configuracaoLoginAzure.getId();

		try {
			configuracaoLoginAzureService.excluir(configuracaoLoginAzureId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void setFiltro(ConfiguracaoLoginAzureFiltro filtro) {
		this.filtro = filtro;
	}

	public ConfiguracaoLoginAzure getConfiguracaoLoginAzure() {
		return configuracaoLoginAzure;
	}

	public void setConfiguracaoLoginAzure(ConfiguracaoLoginAzure configuracaoLoginAzure) {

		if(configuracaoLoginAzure == null) {
			configuracaoLoginAzure = new ConfiguracaoLoginAzure();
		}

		this.configuracaoLoginAzure = configuracaoLoginAzure;
	}

	public List<Subperfil> getSubperfilList() {
		return subperfilList;
	}
}
