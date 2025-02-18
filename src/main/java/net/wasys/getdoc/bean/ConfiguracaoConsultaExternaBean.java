package net.wasys.getdoc.bean;

import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.vo.*;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class ConfiguracaoConsultaExternaBean extends AbstractBean {

	@Autowired private ParametroService parametroService;

	private ConfiguracoesWsDetranArnVO configuracoesWsDetranArnVO;
	private ConfiguracoesWsInfocarVO configuracoesWsInfocarVO;
	private ConfiguracoesWsCrivoVO configuracoesWsCrivoVO;
	private ConfiguracoesWsCredilinkVO configuracoesWsCrediLinkVO;
	private ConfiguracoesWsNfeInteresseVO configuracoesWsNfeInteresseVO;
	private ConfiguracoesWsDataValidVO configuracoesWsDataValidVO;
	private ConfiguracoesWsRenavamVO configuracoesWsRenavamVO;
	private ConfiguracoesWsBrScanVO configuracoesWsBrScanVO;
	private ConfiguracoesWsAzureVO configuracoesWsAzureVO;
	private ConfiguracoesWsSiaVO configuracoesWsSiaVO;
	private ConfiguracoesWsDarknetVO configuracoesWsDarknetVO;
	private ConfiguracoesWsGetDocAlunoVO configuracoesWsAlunoVO;
	private ConfiguracoesWsOcrSpaceVO configuracoesWsOcrSpaceVO;
	private ConfiguracoesWsAtilaVO configuracoesWsAtilaVO;

	private Map<String, String> map;

	protected void initBean() {

		map = parametroService.getConfiguracoesConsultaExterna();
		configuracoesWsDetranArnVO = getDTOFromMap(P.CONFIGURACOES_WS_ARN, ConfiguracoesWsDetranArnVO.class);
		configuracoesWsDetranArnVO = configuracoesWsDetranArnVO != null ? configuracoesWsDetranArnVO : new ConfiguracoesWsDetranArnVO();
		configuracoesWsInfocarVO = getDTOFromMap(P.CONFIGURACOES_WS_INFOCAR, ConfiguracoesWsInfocarVO.class);
		configuracoesWsInfocarVO = configuracoesWsInfocarVO != null ? configuracoesWsInfocarVO : new ConfiguracoesWsInfocarVO();
		configuracoesWsCrivoVO = getDTOFromMap(P.CONFIGURACOES_CRIVO, ConfiguracoesWsCrivoVO.class);
		configuracoesWsCrivoVO = configuracoesWsCrivoVO != null ? configuracoesWsCrivoVO : new ConfiguracoesWsCrivoVO();
		configuracoesWsCrediLinkVO = getDTOFromMap(P.CONFIGURACOES_WS_CREDILINK, ConfiguracoesWsCredilinkVO.class);
		configuracoesWsCrediLinkVO = configuracoesWsCrediLinkVO != null ? configuracoesWsCrediLinkVO : new ConfiguracoesWsCredilinkVO();
		configuracoesWsNfeInteresseVO = getDTOFromMap(P.CONFIGURACOES_WS_NFE_INTERESSE, ConfiguracoesWsNfeInteresseVO.class);
		configuracoesWsNfeInteresseVO = configuracoesWsNfeInteresseVO != null ? configuracoesWsNfeInteresseVO : new ConfiguracoesWsNfeInteresseVO();
		configuracoesWsDataValidVO = getDTOFromMap(P.CONFIGURACOES_WS_DATAVALID, ConfiguracoesWsDataValidVO.class);
		configuracoesWsDataValidVO = configuracoesWsDataValidVO != null ? configuracoesWsDataValidVO : new ConfiguracoesWsDataValidVO();
		configuracoesWsRenavamVO = getDTOFromMap(P.CONFIGURACOES_WS_RENAVAM, ConfiguracoesWsRenavamVO.class);
		configuracoesWsRenavamVO = configuracoesWsRenavamVO != null ? configuracoesWsRenavamVO : new ConfiguracoesWsRenavamVO();
		configuracoesWsBrScanVO = getDTOFromMap(P.CONFIGURACOES_WS_BRSCAN, ConfiguracoesWsBrScanVO.class);
		configuracoesWsBrScanVO = configuracoesWsBrScanVO != null ? configuracoesWsBrScanVO : new ConfiguracoesWsBrScanVO();
		configuracoesWsAzureVO = getDTOFromMap(P.CONFIGURACOES_WS_AZURE, ConfiguracoesWsAzureVO.class);
		configuracoesWsAzureVO = configuracoesWsAzureVO != null ? configuracoesWsAzureVO : new ConfiguracoesWsAzureVO();
		configuracoesWsSiaVO = getDTOFromMap(P.CONFIGURACOES_WS_SIA, ConfiguracoesWsSiaVO.class);
		configuracoesWsSiaVO = configuracoesWsSiaVO != null ? configuracoesWsSiaVO : new ConfiguracoesWsSiaVO();
		configuracoesWsDarknetVO = getDTOFromMap(P.CONFIGURACOES_WS_DARKNET, ConfiguracoesWsDarknetVO.class);
		configuracoesWsDarknetVO = configuracoesWsDarknetVO != null ? configuracoesWsDarknetVO : new ConfiguracoesWsDarknetVO();
		configuracoesWsAlunoVO = getDTOFromMap(P.CONFIGURACOES_WS_ALUNO, ConfiguracoesWsGetDocAlunoVO.class);
		configuracoesWsAlunoVO = configuracoesWsAlunoVO != null ? configuracoesWsAlunoVO : new ConfiguracoesWsGetDocAlunoVO();
		configuracoesWsOcrSpaceVO = getDTOFromMap(P.CONFIGURACOES_WS_OCRSPACE, ConfiguracoesWsOcrSpaceVO.class);
		configuracoesWsOcrSpaceVO = configuracoesWsOcrSpaceVO != null ? configuracoesWsOcrSpaceVO : new ConfiguracoesWsOcrSpaceVO();
		configuracoesWsAtilaVO = getDTOFromMap(P.CONFIGURACOES_WS_ATILA, ConfiguracoesWsAtilaVO.class);
		configuracoesWsAtilaVO = configuracoesWsAtilaVO != null ? configuracoesWsAtilaVO : new ConfiguracoesWsAtilaVO();
	}

	private <T> T getDTOFromMap(P p, Class<T> clazz) {
		T objectFromString = DummyUtils.jsonToObject(map.get(p.name()), clazz);
		return objectFromString;
	}

	public void salvar() {

		try {
			map.put(P.CONFIGURACOES_WS_ARN.name(), DummyUtils.objectToJson(configuracoesWsDetranArnVO));
			map.put(P.CONFIGURACOES_WS_INFOCAR.name(), DummyUtils.objectToJson(configuracoesWsInfocarVO));
			map.put(P.CONFIGURACOES_CRIVO.name(), DummyUtils.objectToJson(configuracoesWsCrivoVO));
			map.put(P.CONFIGURACOES_WS_CREDILINK.name(), DummyUtils.objectToJson(configuracoesWsCrediLinkVO));
			map.put(P.CONFIGURACOES_WS_NFE_INTERESSE.name(), DummyUtils.objectToJson(configuracoesWsNfeInteresseVO));
			map.put(P.CONFIGURACOES_WS_DATAVALID.name(), DummyUtils.objectToJson(configuracoesWsDataValidVO));
			map.put(P.CONFIGURACOES_WS_RENAVAM.name(), DummyUtils.objectToJson(configuracoesWsRenavamVO));
			map.put(P.CONFIGURACOES_WS_BRSCAN.name(), DummyUtils.objectToJson(configuracoesWsBrScanVO));
			map.put(P.CONFIGURACOES_WS_AZURE.name(), DummyUtils.objectToJson(configuracoesWsAzureVO));
			map.put(P.CONFIGURACOES_WS_SIA.name(), DummyUtils.objectToJson(configuracoesWsSiaVO));
			map.put(P.CONFIGURACOES_WS_DARKNET.name(), DummyUtils.objectToJson(configuracoesWsDarknetVO));
			map.put(P.CONFIGURACOES_WS_ALUNO.name(), DummyUtils.objectToJson(configuracoesWsAlunoVO));
			map.put(P.CONFIGURACOES_WS_OCRSPACE.name(), DummyUtils.objectToJson(configuracoesWsOcrSpaceVO));
			map.put(P.CONFIGURACOES_WS_ATILA.name(), DummyUtils.objectToJson(configuracoesWsAtilaVO));
			parametroService.salvarConfiguracaoConsultaExterna(map);

			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public ConfiguracoesWsDetranArnVO getConfiguracoesWsDetranArnVO() {
		return configuracoesWsDetranArnVO;
	}

	public ConfiguracoesWsInfocarVO getConfiguracoesWsInfocarVO() {
		return configuracoesWsInfocarVO;
	}

	public ConfiguracoesWsCrivoVO getConfiguracoesWsCrivoVO() {
		return configuracoesWsCrivoVO;
	}

	public ConfiguracoesWsCredilinkVO getConfiguracoesWsCrediLinkVO() {
		return configuracoesWsCrediLinkVO;
	}

	public ConfiguracoesWsNfeInteresseVO getConfiguracoesWsNfeInteresseVO() {
		return configuracoesWsNfeInteresseVO;
	}

	public ConfiguracoesWsDataValidVO getConfiguracoesWsDataValidVO() {
		return configuracoesWsDataValidVO;
	}

	public ConfiguracoesWsRenavamVO getConfiguracoesWsRenavamVO() {
		return configuracoesWsRenavamVO;
	}

	public ConfiguracoesWsBrScanVO getConfiguracoesWsBrScanVO() {
		return configuracoesWsBrScanVO;
	}

	public ConfiguracoesWsAzureVO getConfiguracoesWsAzureVO() {
		return configuracoesWsAzureVO;
	}

	public ConfiguracoesWsSiaVO getConfiguracoesWsSiaVO() {
		return configuracoesWsSiaVO;
	}

	public ConfiguracoesWsDarknetVO getConfiguracoesWsDarknetVO() {
		return configuracoesWsDarknetVO;
	}

	public ConfiguracoesWsGetDocAlunoVO getConfiguracoesWsAlunoVO() {
		return configuracoesWsAlunoVO;
	}

	public ConfiguracoesWsOcrSpaceVO getConfiguracoesWsOcrSpaceVO() {return configuracoesWsOcrSpaceVO;}

	public ConfiguracoesWsAtilaVO getConfiguracoesWsAtila() {
		return configuracoesWsAtilaVO;
	}

}
