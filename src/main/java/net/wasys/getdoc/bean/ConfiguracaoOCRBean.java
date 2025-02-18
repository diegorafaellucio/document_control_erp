package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.ConfiguracaoOCRDataModel;
import net.wasys.getdoc.domain.entity.ConfiguracaoOCR;
import net.wasys.getdoc.domain.entity.GrupoModeloDocumento;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.ConfiguracaoOCRInstituicaoService;
import net.wasys.getdoc.domain.service.ConfiguracaoOCRService;
import net.wasys.getdoc.domain.service.GrupoModeloDocumentoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

@ManagedBean
@ViewScoped
public class ConfiguracaoOCRBean extends AbstractBean {

	@Autowired private ConfiguracaoOCRService configuracaoOCRService;
	@Autowired private ConfiguracaoOCRInstituicaoService configuracaoOCRInstituicaoService;
	private ConfiguracaoOCRDataModel configuracaoOCRDataModel;
	private ConfiguracaoOCR configuracaoOCR;
	private int totalIES;

	protected void initBean() {
		configuracaoOCRDataModel = new ConfiguracaoOCRDataModel();
		configuracaoOCRDataModel.setService(configuracaoOCRService);
	}

	public ConfiguracaoOCRDataModel getConfiguracaoOCRDataModel() {
		return configuracaoOCRDataModel;
	}

	public void setConfiguracaoOCRDataModel(ConfiguracaoOCRDataModel configuracaoOCRDataModel) {
		this.configuracaoOCRDataModel = configuracaoOCRDataModel;
	}

	public ConfiguracaoOCR getConfiguracaoOCR() {
		return configuracaoOCR;
	}

	public void setConfiguracaoOCR(ConfiguracaoOCR configuracaoOCR) {
		this.configuracaoOCR = configuracaoOCR;
	}

	public void salvar() {

		try {
			boolean insert = isInsert(configuracaoOCR);
			Usuario usuario = getUsuarioLogado();

			configuracaoOCRService.saveOrUpdate(configuracaoOCR, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public int getTotalIES(Long configuracaoOCRId) {
		return configuracaoOCRInstituicaoService.countByConfiguracaoOCR(configuracaoOCRId);
	}

	public void setTotalIES(int totalIES) {
		this.totalIES = totalIES;
	}
}
