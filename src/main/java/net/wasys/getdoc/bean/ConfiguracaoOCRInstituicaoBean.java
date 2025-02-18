package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.ConfiguracaoORCInstituicaoDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;

@ManagedBean
@ViewScoped
public class ConfiguracaoOCRInstituicaoBean extends AbstractBean {

	@Autowired private ConfiguracaoOCRInstituicaoService configuracaoOCRInstituicaoService;
	@Autowired private ConfiguracaoOCRService configuracaoOCRService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private BaseRegistroValorService baseRegistroValorService;
	private Long configuracaoOCRId;
	private ConfiguracaoOCRInstituicao configuracaoOCRInstituicao;
	private ConfiguracaoORCInstituicaoDataModel configuracaoORCInstituicaoDataModel;
	private ConfiguracaoOCR configuracaoOCR;
	private RegistroValorVO registroValorVO;
	private List<RegistroValorVO> registroValorVOList;

	public void initBean() {

		if(configuracaoOCR == null) {
			if(configuracaoOCRId == null) {
				redirect("/admin/configuracao_ocr/");
				return;
			}
			this.configuracaoOCR = configuracaoOCRService.get(configuracaoOCRId);
			if(this.configuracaoOCR == null) {
				redirect("/admin/configuracao_ocr/");
				return;
			}
		}

		configuracaoORCInstituicaoDataModel = new ConfiguracaoORCInstituicaoDataModel();
		configuracaoORCInstituicaoDataModel.setService(configuracaoOCRInstituicaoService);
		configuracaoORCInstituicaoDataModel.setConfiguracaoOCRId(configuracaoOCRId);

		configuracaoOCRInstituicao = new ConfiguracaoOCRInstituicao();

		registroValorVOList = baseRegistroService.findByBaseInterna(BaseInterna.INSTITUICAO_ID);


	}

	public void salvar() {

		try {
			boolean insert = isInsert(configuracaoOCRInstituicao);
			Usuario usuario = getUsuarioLogado();

			configuracaoOCRInstituicao.setConfiguracaoOCR(configuracaoOCR);
			configuracaoOCRInstituicao.setNomeInstituicao(registroValorVO.getLabel());
			configuracaoOCRInstituicao.setCodigoInstituicao(Long.parseLong(registroValorVO.getValor("COD_INSTITUICAO")));

			configuracaoOCRInstituicaoService.saveOrUpdate(configuracaoOCRInstituicao, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");

		}
		catch (Exception e) {
			addMessageError(e);
		}
	}


	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long configuracaoOCRInstituicaoId = configuracaoOCRInstituicao.getId();

		try {
			configuracaoOCRInstituicaoService.excluir(configuracaoOCRInstituicaoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public ConfiguracaoOCRInstituicaoService getConfiguracaoOCRInstituicaoService() {
		return configuracaoOCRInstituicaoService;
	}

	public void setConfiguracaoOCRInstituicaoService(ConfiguracaoOCRInstituicaoService configuracaoOCRInstituicaoService) {
		this.configuracaoOCRInstituicaoService = configuracaoOCRInstituicaoService;
	}

	public Long getConfiguracaoOCRId() {
		return configuracaoOCRId;
	}

	public void setConfiguracaoOCRId(Long configuracaoOCRId) {
		this.configuracaoOCRId = configuracaoOCRId;
	}

	public ConfiguracaoOCRInstituicao getConfiguracaoOCRInstituicao() {
		return configuracaoOCRInstituicao;
	}

	public void setConfiguracaoOCRInstituicao(ConfiguracaoOCRInstituicao configuracaoOCRInstituicao) {
		this.configuracaoOCRInstituicao = configuracaoOCRInstituicao;
	}

	public ConfiguracaoORCInstituicaoDataModel getConfiguracaoORCInstituicaoDataModel() {
		return configuracaoORCInstituicaoDataModel;
	}

	public void setConfiguracaoORCInstituicaoDataModel(ConfiguracaoORCInstituicaoDataModel configuracaoORCInstituicaoDataModel) {
		this.configuracaoORCInstituicaoDataModel = configuracaoORCInstituicaoDataModel;
	}

	public ConfiguracaoOCR getConfiguracaoOCR() {
		return configuracaoOCR;
	}

	public void setConfiguracaoOCR(ConfiguracaoOCR configuracaoOCR) {
		this.configuracaoOCR = configuracaoOCR;
	}

	public List<RegistroValorVO> getRegistroValorVOList() {
		return registroValorVOList;
	}

	public void setRegistroValorVOList(List<RegistroValorVO> registroValorVOList) {
		this.registroValorVOList = registroValorVOList;
	}

	public RegistroValorVO getRegistroValorVO() {
		return registroValorVO;
	}

	public void setRegistroValorVO(RegistroValorVO registroValorVO) {
		this.registroValorVO = registroValorVO;
	}
}
