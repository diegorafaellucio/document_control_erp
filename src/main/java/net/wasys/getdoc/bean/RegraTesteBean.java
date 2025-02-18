package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.ProcessoRegra;
import net.wasys.getdoc.domain.entity.ProcessoRegraLog;
import net.wasys.getdoc.domain.entity.Regra;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.RegraService;
import net.wasys.getdoc.domain.vo.OrigemValorVO;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

@ManagedBean
@ViewScoped
public class RegraTesteBean extends AbstractBean {

	@Autowired private RegraService regraService;

	private Long regraId;
	private Regra regra;
	private ProcessoRegra processoRegra;
	private Map<String, Set<OrigemValorVO>> dependencias;
	private ProcessoRegraLog log;

	protected void initBean() {

		regra = regraService.get(regraId);
		dependencias = regraService.buscarDependencias(regra);
	}

	public void testarRegra() {

		try {
			prepararValoresDependencia();
			Usuario usuario = getUsuarioLogado();

			processoRegra = regraService.testarRegra(regra, dependencias, usuario);

			addMessage("subRegrasExecutadas.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private void prepararValoresDependencia() {

		dependencias = regraService.buscarDependencias(regra);

		Map<String, String> valoresDependencias = carregarValoresDependencias();

		Set<Entry<String, String>> entrySet = valoresDependencias.entrySet();
		for (String fonte : dependencias.keySet()) {

			Set<OrigemValorVO> origemValorVOs = dependencias.get(fonte);
			for (Entry<String, String> valorDependencia : entrySet) {

				String origem = valorDependencia.getKey();

				for (OrigemValorVO vo : origemValorVOs) {

					String origemDependencia = vo.getOrigem();
					origemDependencia = fonte + origemDependencia;
					if(origemDependencia.equals(origem) ) {
						String valor = valorDependencia.getValue();
						vo.setValor(valor);
					}
				}
			}
		}
	}

	private Map<String, String> carregarValoresDependencias() {

		Map<String, String> colunaValor = new HashMap<>();
		HttpServletRequest request = getRequest();
		Enumeration<String> parameterNames = request.getParameterNames();

		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if(parameterName.startsWith("dependencia_")) {
				String valor = request.getParameter(parameterName);
				String coluna = parameterName.replace("dependencia_", "");

				if(!isBlank(valor)) {
					colunaValor.put(coluna, valor);
				}
			}
		}

		return colunaValor;
	}

	public void limpar() {
		initBean();
	}

	public Long getRegraId() {
		return regraId;
	}

	public void setRegraId(Long regraId) {
		this.regraId = regraId;
	}

	public Map<String, Set<OrigemValorVO>> getDependencias() {
		return dependencias;
	}

	public ProcessoRegra getProcessoRegra() {
		return processoRegra;
	}

	public ProcessoRegraLog getLog() {
		return log;
	}

	public void setLog(ProcessoRegraLog log) {
		this.log = log;
	}

	public Regra getRegra() {
		return regra;
	}
}

