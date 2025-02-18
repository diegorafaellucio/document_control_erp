package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.RelatoriosService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.RelatorioAcompanhamentoVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.faces.AbstractBean;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omnifaces.util.Ajax;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
public class RelatorioAcompanhamentoEmAbertoBean extends AbstractBean {

	@Autowired private RelatoriosService relatoriosService;
    @Autowired private BaseRegistroService baseRegistroService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private ProcessoFiltro filtro;

	private List<RelatorioAcompanhamentoVO> list;
	private List<TipoProcesso> tiposProcessos;
    private List<RegistroValorVO> regional;
    private List<RegistroValorVO> campus;
    private List<String> regionaisSelecionados;
    private List<String> campusSelecionados;

	protected void initBean() {

		filtro = new ProcessoFiltro();
		campusSelecionados = new ArrayList<>();
		regionaisSelecionados = new ArrayList<>();
		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcessos = tipoProcessoService.findAtivos(permissoes);
		regional = baseRegistroService.findByBaseInterna(BaseInterna.REGIONAL_ID);
		campus = baseRegistroService.findByBaseInterna(BaseInterna.CAMPUS_ID);
		filtro.setCamposFiltro(null);

		buscar();
		geraGrafico(true);
	}

	public void buscar() {
		filtro.setCamposFiltro(null);
		filtro.setCamposFiltro(CampoMap.CampoEnum.REGIONAL, regionaisSelecionados);
        filtro.setCamposFiltro(CampoMap.CampoEnum.CAMPUS, campusSelecionados);
		list = relatoriosService.getRelatorioEmAbertoStatus(filtro, true);
	}

	public void geraGrafico(Boolean init) {

		if(!init) {
			buscar();
		}

		JSONArray okArray = new JSONArray();
		JSONArray alertasArray = new JSONArray();
		JSONArray atrasadosArray = new JSONArray();

		for (RelatorioAcompanhamentoVO vo : list) {
			StatusProcesso status = vo.getStatus();
			if (status == null) {
				break;
			}
			okArray.put(vo.getOk());
			alertasArray.put(vo.getAlertas());
			atrasadosArray.put(vo.getAtrasados());
		}

		JSONArray dados = new JSONArray();

		JSONObject okObject = new JSONObject();
		okObject.put("name", "OK");
		okObject.put("color", "#2b908f");
		okObject.put("data", okArray);
		dados.put(okObject);

		JSONObject alertasObject = new JSONObject();
		alertasObject.put("name", "Alertas");
		alertasObject.put("color", "rgb(248, 161, 63)");
		alertasObject.put("data", alertasArray);
		dados.put(alertasObject);

		JSONObject atrasadosObject = new JSONObject();
		atrasadosObject.put("name", "Atrasados");
		atrasadosObject.put("color", "rgba(186, 60, 61, 0.901961)");
		atrasadosObject.put("data", atrasadosArray);
		dados.put(atrasadosObject);

		Ajax.oncomplete("desenharGrafico(" + dados + ")");
	}

	public List<RelatorioAcompanhamentoVO> getList() {
		return list;
	}

	public void setList(List<RelatorioAcompanhamentoVO> list) {
		this.list = list;
	}

	public ProcessoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(ProcessoFiltro filtro) {
		this.filtro = filtro;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(List<TipoProcesso> tiposProcessos) {
		this.tiposProcessos = tiposProcessos;
	}

    public List<RegistroValorVO> getRegional() {
        return regional;
    }

    public List<RegistroValorVO> getCampus() {
        return campus;
    }

    public List<String> getRegionaisSelecionados() {
        return regionaisSelecionados;
    }

    public void setRegionaisSelecionados(List<String> regionaisSelecionados) {
        this.regionaisSelecionados = regionaisSelecionados;
    }

    public List<String> getCampusSelecionados() {
        return campusSelecionados;
    }

    public void setCampusSelecionados(List<String> campusSelecionados) {
        this.campusSelecionados = campusSelecionados;
    }
}

