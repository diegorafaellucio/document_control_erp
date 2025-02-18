package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@ManagedBean
@ViewScoped
public class UsuarioEditBean extends AbstractBean {

	@Autowired private UsuarioService usuarioService;
	@Autowired private AreaService areaService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SubperfilService subperfilService;
	@Autowired private ResourceService resourceService;
	@Autowired private BaseRegistroService baseRegistroService;

	private Long id;
	private Usuario usuario;
	private List<Area> areas;
	private List<TipoProcesso> tiposProcessos;
	private List<RegistroValorVO> campus;
	private List<RegistroValorVO> regional;
	private List<Subperfil> subperfils;
	private List<String> tiposProcessosSelecionados;
	private List<String> subperfilsSelecionados;
	private HashMap<Long, Integer> nivelSubperfilMap;
	private List<String> campusSelecionados;
	private List<String> regionaisSelecionados;
	private boolean disabled;

	protected void initBean() {

		subperfils = subperfilService.findAll();
		initUsuario();

		this.areas = areaService.findAtivas();
		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcessos = tipoProcessoService.findAtivos(permissoes);

		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		disabled = StringUtils.isNotBlank(geralEndpoint);
	}

	private void initUsuario() {

		if(usuario == null) {
			if(id != null) {
				this.usuario = usuarioService.get(id);
			} else {
				this.usuario = new Usuario();
			}
		}

		tiposProcessosSelecionados = new ArrayList<>();
		Set<UsuarioTipoProcesso> tiposProcessos2 = usuario.getTiposProcessos();
		for (UsuarioTipoProcesso utp : tiposProcessos2) {
			TipoProcesso tipoProcesso = utp.getTipoProcesso();
			Long tipoProcessoId = tipoProcesso.getId();
			String tipoProcessoIdStr = tipoProcessoId.toString();
			tiposProcessosSelecionados.add(tipoProcessoIdStr);
		}

		subperfilsSelecionados = new ArrayList<>();
		nivelSubperfilMap = new HashMap<>();
		Set<UsuarioSubperfil> usuarioSubperfils2 = usuario.getSubperfils();
		for (UsuarioSubperfil utp : usuarioSubperfils2) {
			Subperfil subperfil = utp.getSubperfil();
			Long subperfilId = subperfil.getId();
			String subperfilIdStr = subperfilId.toString();
			subperfilsSelecionados.add(subperfilIdStr);
			Integer nivel = utp.getNivel();
			nivelSubperfilMap.put(subperfilId, nivel);
		}

		Collections.sort(subperfils, (o1, o2) -> {
			Long id1 = o1.getId();
			Long id2 = o2.getId();
			Integer nivel1 = nivelSubperfilMap.get(id1);
			Integer nivel2 = nivelSubperfilMap.get(id2);
			nivel1 = nivel1 != null ? nivel1 : 0;
			nivel2 = nivel2 != null ? nivel2 : 0;
			int compareTo = DummyUtils.compareTo(nivel2, nivel1, false);
			if(compareTo == 0) {
				String descricao1 = o1.getDescricao();
				String descricao2 = o2.getDescricao();
				compareTo = descricao1.compareTo(descricao2);
			}
			return compareTo;
		});

		campusSelecionados = new ArrayList<>();
		Set<UsuarioCampus> campusSelecionados2 = usuario.getCampus();
		for (UsuarioCampus utp : campusSelecionados2) {
			BaseRegistro baseRegistro = utp.getCampus();
			String chaveUnicidade = baseRegistro.getChaveUnicidade();
			campusSelecionados.add(chaveUnicidade);
		}

		regionaisSelecionados = new ArrayList<>();
		Set<UsuarioRegional> regionaisSelecionados2 = usuario.getRegionais();
		for (UsuarioRegional utp : regionaisSelecionados2) {
			BaseRegistro baseRegistro = utp.getRegional();
			String chaveUnicidade = baseRegistro.getChaveUnicidade();
			regionaisSelecionados.add(chaveUnicidade);
		}

		regional = baseRegistroService.findByBaseInterna(BaseInterna.REGIONAL_ID);

		campus = baseRegistroService.findByRelacionados(BaseInterna.CAMPUS_ID, regionaisSelecionados, TipoCampo.COD_REGIONAL);
		List<RegistroValorVO> mostrarSelecionados = new ArrayList<>();
		for(RegistroValorVO valorVO : campus){
			BaseRegistro baseRegistro = valorVO.getBaseRegistro();
			String chaveUnicidade = baseRegistro.getChaveUnicidade();
			if(campusSelecionados.contains(chaveUnicidade)){
				mostrarSelecionados.add(valorVO);
			}
		}
		campus = mostrarSelecionados;
	}

	public void salvar() {

		try {
			boolean insert = isInsert(usuario);
			Usuario usuarioLogado = getUsuarioLogado();

			fillSubperfilsSelecionadosNivelPrioridade();

			if(this.subperfilsSelecionados.size() > 0 ){
				String subPerfilResponse = this.subperfilsSelecionados.get(0);
				Subperfil subperfil = subperfilService.get(Long.parseLong(subPerfilResponse));
				usuario.setSubperfilAtivo(subperfil);
			}

			usuarioService.saveOrUpdate(usuario, tiposProcessosSelecionados, usuarioLogado, subperfilsSelecionados, nivelSubperfilMap);

			initUsuario();
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private void fillSubperfilsSelecionadosNivelPrioridade() {
		HttpServletRequest request = getRequest();
		String[] subperfilSelecionados = request.getParameterValues("subperfilSelecionado");
		if(subperfilSelecionados != null) {
			this.subperfilsSelecionados = Arrays.asList(subperfilSelecionados);
			this.nivelSubperfilMap = new HashMap<>();
			for (String subperfilSelecionado : this.subperfilsSelecionados) {
				String nivel = request.getParameter("nivelSubperfil_" + subperfilSelecionado);
				if(StringUtils.isNumeric(nivel)) {
					this.nivelSubperfilMap.put(new Long(subperfilSelecionado), StringUtils.isNotBlank(nivel) ? new Integer(nivel) : null);
				}
			}
		}
	}

	public void reiniciarSenha() {

		Usuario usuarioLogado = getUsuarioLogado();

		try {
			usuarioService.reiniciarSenha(usuario, usuarioLogado);
			addMessage("senhaResetada.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void prorrogarAcesso() {

		Usuario usuarioLogado = getUsuarioLogado();

		try {
			usuarioService.prorrogarAcesso(usuario, usuarioLogado);
			addMessage("senhaResetada.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void desativarUsuario() {

		Usuario usuarioLogado = getUsuarioLogado();

		try {
			usuarioService.desativarUsuario(usuario, usuarioLogado);
			addMessage("usuarioDesativado.sucesso");
			redirect("/cadastros/usuarios/edit/?id=" + usuario.getId());
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void ativarUsuario() {

		Usuario usuarioLogado = getUsuarioLogado();

		try {
			usuarioService.ativarUsuario(usuario, usuarioLogado);
			addMessage("usuarioAtivado.sucesso");
			redirect("/cadastros/usuarios/edit/?id=" + usuario.getId());
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public List<RegistroValorVO> getCampus(){
		return campus;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public List<RoleGD> getRoles() {
		return Arrays.asList(RoleGD.values());
	}

	public boolean getRenderDesativarBtn() {
		StatusUsuario status = usuario.getStatus();
		return !StatusUsuario.INATIVO.equals(status) && !disabled;
	}

	public boolean getRenderAtivarBtn() {
		StatusUsuario status = usuario.getStatus();
		return !StatusUsuario.ATIVO.equals(status) && !disabled;
	}

	public boolean exibirArea() {
		if(usuario.isAreaRole()) {
			return true;
		}
		return false;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public List<RegistroValorVO> getRegional() {
		return regional;
	}

	public List<String> getTiposProcessosSelecionados() {
		return tiposProcessosSelecionados;
	}

	public void setTiposProcessosSelecionados(List<String> tiposProcessosSelecionados) {
		this.tiposProcessosSelecionados = tiposProcessosSelecionados;
	}

	public List<String> getSubperfilsSelecionados() {
		return subperfilsSelecionados;
	}

	public void setSubperfilsSelecionados(List<String> subperfilsSelecionados) {
		this.subperfilsSelecionados = subperfilsSelecionados;
	}

	public List<String> getCampusSelecionados() {
		return campusSelecionados;
	}

	public void setCampusSelecionados(List<String> campusSelecionados) {
		this.campusSelecionados = campusSelecionados;
	}

	public List<String> getRegionaisSelecionados() {
		return regionaisSelecionados;
	}

	public void setRegionaisSelecionados(List<String> regionaisSelecionados) {
		this.regionaisSelecionados = regionaisSelecionados;
	}

	public List<Area> getAreas() {
		return areas;
	}

	public List<Subperfil> getSubperfils() {
		return subperfils;
	}

	public void setSubperfils(List<Subperfil> subperfils) {
		this.subperfils = subperfils;
	}

	public HashMap<Long, Integer> getNivelSubperfilMap() {
		return nivelSubperfilMap;
	}
}
