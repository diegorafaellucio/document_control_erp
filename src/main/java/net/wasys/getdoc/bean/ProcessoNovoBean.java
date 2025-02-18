package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.TipoCampoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.CriacaoProcessoVO;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.util.*;

@ManagedBean
@ViewScoped
public class ProcessoNovoBean extends AbstractBean {

	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private ProcessoService processoService;

	protected List<TipoProcesso> tiposProcessos;
	protected TipoProcesso tipoProcesso;
	protected Map<TipoCampoGrupo, List<CampoAbstract>> tiposCampos;
	private Long tipoProcessoId;

	protected void initBean() {

		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcessos = tipoProcessoService.findAtivos(permissoes);

		if(tipoProcessoId != null) {

			boolean permitido = false;
			TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
			Set<TipoProcessoPermissao> tpps = tipoProcesso.getPermissoes();
			for (TipoProcessoPermissao tpp : tpps) {
				PermissaoTP permissao = tpp.getPermissao();
				if(permissoes == null || permissoes.contains(permissao)) {
					permitido = true;
				}
			}

			if(tipoProcesso != null && permitido) {
				this.tipoProcesso = tipoProcesso;
				selecionaTipoProcesso(tipoProcessoId);
			}
			else {
				addMessageError(new MessageKeyException("tipoProcessoInvalido.error"));
			}
		}
	}

	public void salvar() {

		try {
			Set<CampoAbstract> valoresCampos = getValoresCampos();
			Usuario usuario = getUsuarioLogado();

			CriacaoProcessoVO vo = new CriacaoProcessoVO();
			vo.setTipoProcesso(tipoProcesso);
			vo.setUsuario(usuario);
			vo.setValoresCampos(valoresCampos);
			vo.setOrigem(Origem.WEB);

			Processo processo = processoService.criaProcesso(vo);

			addMessage("rascunhoSalvo.sucesso");
			redirect("/requisicoes/fila/edit/" + processo.getId());
		}
		catch (ProcessoService.ProcessoCriadoException e) {
			addMessageError(e);
			Processo processo = e.getProcesso();
			redirect("/requisicoes/fila/edit/" + processo.getId());
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected Set<CampoAbstract> getValoresCampos() {
		Map<String, Object> viewMap = getViewMap();
		Set<CampoAbstract> camposSet = (Set<CampoAbstract>) viewMap.get(CAMPOS_MAP);
		return camposSet;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void selecionaTipoProcesso(ValueChangeEvent event) {
		Object newValue = event.getNewValue();
		if(newValue instanceof TipoProcesso) {
			tipoProcesso = (TipoProcesso) newValue;
			Long tipoProcessoId = tipoProcesso.getId();
			selecionaTipoProcesso(tipoProcessoId);
			redirect("/requisicoes/novo/" + tipoProcessoId);
		}
	}

	@SuppressWarnings("unchecked")
	protected void selecionaTipoProcesso(Long tipoProcessoId) {

		Map<TipoCampoGrupo, List<TipoCampo>> map1 = tipoCampoService.findMapByTipoProcesso(tipoProcessoId);
		Map<TipoCampoGrupo, List<CampoAbstract>> map2 = new LinkedHashMap<>();
		Set<CampoAbstract> camposSet = new LinkedHashSet<>();

		for (TipoCampoGrupo tipoCampoGrupo : map1.keySet()) {
			Boolean criacaoProcesso = tipoCampoGrupo.getCriacaoProcesso();
			if (criacaoProcesso) {
                List<TipoCampo> list = map1.get(tipoCampoGrupo);
                List<CampoAbstract> list2 = new ArrayList<>();
                for (TipoCampo tipoCampo : list) {
                    list2.add(tipoCampo);
                    camposSet.add(tipoCampo);
				}
                map2.put(tipoCampoGrupo, list2);
			}
		}

		Map<String, Object> viewMap = getViewMap();
		viewMap.put(CAMPOS_MAP, camposSet);

		this.tiposCampos = map2;
	}

	public List<TipoCampoGrupo> getGrupos() {
		if(tiposCampos == null) {
			return null;
		}
		return new ArrayList<>(tiposCampos.keySet());
	}

	public List<CampoAbstract> getCampos1(TipoCampoGrupo grupo) {
		List<CampoAbstract> list = tiposCampos.get(grupo);
		int size = list.size();
		int meio = (int) Math.ceil(size / 2d);
		return list.subList(0, meio);
	}

	public List<CampoAbstract> getCampos2(TipoCampoGrupo grupo) {
		List<CampoAbstract> list = tiposCampos.get(grupo);
		int size = list.size();
		int meio = (int) Math.ceil(size / 2d);
		return list.subList(meio, size);
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@Override
	public void verificarBloqueioProcesso() {
		//nao faz nada
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}
}
