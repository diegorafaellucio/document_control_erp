package net.wasys.getdoc.bean;

import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class ConfiguracaoEditBean extends AbstractBean {

	@Autowired private ParametroService parametroService;

	private Map<String, String> map;

	protected void initBean() {

		map = parametroService.getConfiguracao();
	}

	public void salvar() {

		try {
			formatarExtensoesPermitidas();
			parametroService.salvarConfiguracao(map);

			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private void formatarExtensoesPermitidas(){
		String whiteList = map.get(P.EXTENSOES_WHITE_LIST.name());
		whiteList = whiteList.toLowerCase();
		whiteList = whiteList.replaceAll("[^,a-z0-9]", "");
		map.put(P.EXTENSOES_WHITE_LIST.name(), whiteList);

		String blackList = map.get(P.EXTENSOES_BLACK_LIST.name());
		blackList = blackList.toLowerCase();
		blackList = blackList.replaceAll("[^,a-z0-9]", "");
		map.put(P.EXTENSOES_BLACK_LIST.name(), blackList);
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

}
