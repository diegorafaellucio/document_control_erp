package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.RegraDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.RegraFiltro;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.util.*;

@ManagedBean
@ViewScoped
public class RegraCrudBean extends AbstractBean {

	@Autowired private RegraService regraService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private SubperfilService subperfilService;
	@Autowired private RegraSubperfilService regraSubperfilService;

	private RegraDataModel dataModel;
	private Regra regra;
	private List<TipoProcesso> tiposProcessos;
	private TipoProcesso selectedTipoProcesso;
	private List<Situacao> situacoes;
	private List<String> rolesGDSelecionados = new ArrayList<>();
	private List<RoleGD> rolesGD = new ArrayList<>();
	private List<Subperfil> subperfilList = new ArrayList<>();
	private List<Subperfil> subperfilSelecionadoList = new ArrayList<>();

	private RegraFiltro filtro;

	protected void initBean() {

		if(filtro == null) {
			filtro = new RegraFiltro();
			filtro.setStatusArray(new Boolean[] {true});
			filtro.setApenasVigentes(true);
			filtro.setVigencia(new Date());
		}

		dataModel = new RegraDataModel();
		dataModel.setService(regraService);
		dataModel.setFiltro(filtro);

		tiposProcessos = tipoProcessoService.findAtivos(null);
		rolesGD = Arrays.asList(RoleGD.values());

		subperfilList = subperfilService.findAll();
	}

	public void salvar() {

		try {
			boolean insert = isInsert(regra);
			Usuario usuario = getUsuarioLogado();

			List<RegraRole> regraRoles = regra.getRegraRoles();
			for (RegraRole rr : new ArrayList<>(regraRoles)) {
				String role = rr.getRole();
				if (!rolesGDSelecionados.contains(role)) {
					regraRoles.remove(rr);
				}
			}

			for(String rgd: rolesGDSelecionados){
				boolean novoRegistro = true;
				for (RegraRole rr : regraRoles) {
					String role = rr.getRole();
					if(rgd.equals(role)){
						novoRegistro = false;
					}
				}
				if(novoRegistro) {
					RegraRole regraRole = new RegraRole();
					regraRole.setRole(rgd);
					regraRole.setRegra(regra);
					regraRoles.add(regraRole);
				}
			}

			regraService.saveOrUpdate(regra, usuario, Arrays.asList(selectedTipoProcesso), subperfilSelecionadoList);

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void duplicar() {

		try {
			Usuario usuario = getUsuarioLogado();
			regraService.duplicar(regra, usuario);

			filtro.setStatusList(null);
			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();
		Long regraId = regra.getId();

		try {
			regraService.excluir(regraId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
			redirect("/cadastros/regras/");
		}
		catch (Exception e) {
			regra = regraService.get(regraId);
			tiposProcessos = null;
			addMessageError(e);
		}
	}

	public Regra getRegra() {
		return regra;
	}

	public void setRegra(Regra regra) {

		if(regra == null) {
			regra = new Regra();
			selectedTipoProcesso = null;
			situacoes = null;
			rolesGDSelecionados = null;
			subperfilSelecionadoList.clear();
			subperfilSelecionadoList.addAll(subperfilList);
		}
		else {
			Long situacaoId = regra.getId();
			regra = regraService.get(situacaoId);
			selectedTipoProcesso = null;
			Set<RegraTipoProcesso> rtps = regra.getTiposProcessos();
			for (RegraTipoProcesso rtp : rtps) {
				TipoProcesso tipoProcesso = rtp.getTipoProcesso();
				//restringe a apenas um
				selectedTipoProcesso = tipoProcesso;
				break;
			}
			initSituacoes(selectedTipoProcesso);
			List<RegraRole> regraRoles = regra.getRegraRoles();
			rolesGDSelecionados = new ArrayList<>();
			for(RegraRole rr: regraRoles) {
				String role = rr.getRole();
				rolesGDSelecionados.add(role);
			}

			subperfilSelecionadoList.clear();
			Long regraId = regra.getId();
			List<RegraSubperfil> regraSubperfilList = regraSubperfilService.findByRegra(regraId);
			for(RegraSubperfil regraSubperfil : regraSubperfilList) {
				Subperfil subperfil = regraSubperfil.getSubperfil();
				subperfilSelecionadoList.add(subperfil);
			}
		}

		this.regra = regra;
	}

	public void changeTipoProcesso(ValueChangeEvent event) {
		Object newValue = event.getNewValue();
		TipoProcesso tipoProcesso = (TipoProcesso) newValue;
		initSituacoes(tipoProcesso);
	}

	private void initSituacoes(TipoProcesso tipoProcesso) {
		Long tipoProcessoId = tipoProcesso.getId();
		SituacaoFiltro filtro2 = new SituacaoFiltro();
		filtro2.setTipoProcessoId(tipoProcessoId);
		filtro2.setAtiva(true);
		situacoes = situacaoService.findByFiltro(filtro2, null, null);
	}

	public RegraDataModel getDataModel() {
		return dataModel;
	}

	public TipoProcesso getSelectedTipoProcesso() {
		return selectedTipoProcesso;
	}

	public void setSelectedTipoProcesso(TipoProcesso selectedTipoProcesso) {
		this.selectedTipoProcesso = selectedTipoProcesso;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public RegraFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(RegraFiltro filtro) {
		this.filtro = filtro;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public List<RoleGD> getRoles() {
		return this.rolesGD;
	}

	public List<String> getRolesGDSelecionados() {
		return rolesGDSelecionados;
	}

	public void setRolesGDSelecionados(List<String> rolesGDSelecionados) {
		this.rolesGDSelecionados = rolesGDSelecionados;
	}

	public List<Subperfil> getSubperfilList() {
		return subperfilList;
	}

	public List<Subperfil> getSubperfilSelecionadoList() {
		return subperfilSelecionadoList;
	}

	public void setSubperfilSelecionadoList(List<Subperfil> subperfilSelecionadoList) {
		this.subperfilSelecionadoList = subperfilSelecionadoList;
	}
}
