package net.wasys.getdoc.bean;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.XStream;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.Funcionalidade;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.faces.FacesUtil;
import net.wasys.util.menu.Item;
import net.wasys.util.menu.Menu;
import net.wasys.util.menu.MenuItem;

@ManagedBean
@SessionScoped
public class MenuBean extends AbstractBean {

	private static final String ACTIVE_ITEM_KEY = "active_item_key";

	private static Menu menuStatico;

	private Menu menu;
	private List<MenuItem> arvoreMenuItens;

	protected void initBean() { }

	public String menuClass(Item menuItem) {

		Item activeItem = getActiveItem();

		if(activeItem == null) {
			return "";
		}

		if(menuItem.equals(activeItem) || activeItem.hasParent(menuItem)) {
			return "active";
		}

		return "";
	}

	public List<Item> getCaminhoDoPao() {

		Item activeItem = getActiveItem();
		Item itemAtual = activeItem;
		if(itemAtual == null) {
			return null;
		}

		List<Item> caminhoDoPao = new ArrayList<>();
		do {
			caminhoDoPao.add(itemAtual);
			itemAtual = itemAtual.getParent();
		}
		while(itemAtual != null);

		List<MenuItem> menuList = getArvoreMenuItens();
		if(menuList == null) {
			return null;
		}

		//TODO: precisa disso?
		if(activeItem.getLabelKey().equals("menu-candidato.label")){
			return caminhoDoPao;
		}

		Item homeMenuItem = new Item();
		homeMenuItem.setLabelKey("menu-home.label");
		caminhoDoPao.add(homeMenuItem);

		Collections.reverse(caminhoDoPao);

		return caminhoDoPao;
	}

	public Funcionalidade getFuncionalidadeAtual() {

		Item activeItem = getActiveItem();
		String id = activeItem.getId();
		if(StringUtils.isNotBlank(id)) {
			return Funcionalidade.valueOf(id);
		}

		Item parent = activeItem.getParent();
		if(parent != null) {

			String id2 = parent.getId();
			if(StringUtils.isNotBlank(id2)) {
				return Funcionalidade.valueOf(id2);
			}

			Item parent2 = parent.getParent();
			if(parent2 != null) {

				String id3 = parent2.getId();
				if(StringUtils.isNotBlank(id3)) {
					return Funcionalidade.valueOf(id3);
				}
			}
		}

		return null;
	}

	public Item getActiveItem() {

		Map<String, Object> viewMap = getViewMap();

		Item activeItem = (Item) viewMap.get(ACTIVE_ITEM_KEY);
		if(activeItem == null) {

			Menu menu = getMenuEstatico();
			activeItem = menu.getActiveItem();
			viewMap.put(ACTIVE_ITEM_KEY, activeItem);
		}

		return activeItem;
	}

	public List<MenuItem> getArvoreMenuItens() {

		Usuario usuario = getUsuarioLogado();
		if(usuario == null) {
			return null;
		}

		if(arvoreMenuItens != null) {
			return arvoreMenuItens;
		}

		Menu menu = getMenu();
		List<MenuItem> arvore = menu.getArvoreMenuItens();
		List<MenuItem> amiDest = new ArrayList<MenuItem>(arvore);

		for (MenuItem mi1 : arvore) {

			Funcionalidade funcionalidade1 = mi1.getFuncionalidade();
			if(funcionalidade1 != null && !funcionalidade1.podeAcessar(mi1, usuario)) {
				amiDest.remove(mi1);
				continue;
			}

			List<MenuItem> mItens = mi1.getMenuItens();
			if(mItens != null) {

				List<MenuItem> mItensDest = new ArrayList<>(mItens);
				for (MenuItem mi2 : mItens) {

					Funcionalidade funcionalidade2 = mi2.getFuncionalidade();
					if(funcionalidade2 != null && !funcionalidade2.isVisualizavel(usuario)) {
						mItensDest.remove(mi2);
						continue;
					}
				}

				mi1.setMenuItens(mItensDest);

				if(mItensDest.isEmpty()) {
					amiDest.remove(mi1);
				}
			}
		}

		arvoreMenuItens = amiDest;
		return arvoreMenuItens;
	}

	public List<Item> getTodasFuncionalidades() {

		Menu menu = getMenuEstatico();
		List<Item> todasFuncionalidades = menu.getTodasFuncionalidades();
		return todasFuncionalidades;
	}

	public static Menu getMenuEstatico() {

		if(menuStatico == null) {
			menuStatico = getMenuFromXml();
		}

		return menuStatico;
	}

	private Menu getMenu() {

		if(menu == null) {
			menu = getMenuFromXml();
		}

		return menu;
	}

	private static Menu getMenuFromXml() {

		ServletContext servletContext = FacesUtil.getServletContext();
		InputStream inputStream = servletContext.getResourceAsStream("/WEB-INF/menu.xml");

		XStream xStream = new XStream();
		xStream.processAnnotations(Menu.class);

		Menu menu = (Menu) xStream.fromXML(inputStream);

		menu.processaParents();

		return menu;
	}

	public boolean podeEditar() {

		Funcionalidade funcionalidade = getFuncionalidadeAtual();
		Usuario usuarioLogado = getUsuarioLogado();

		if(funcionalidade == null) {
			return false;
		}

		return funcionalidade.isEditavel(usuarioLogado);
	}

	public boolean podeCadastrar() {

		Funcionalidade funcionalidade = getFuncionalidadeAtual();
		Usuario usuarioLogado = getUsuarioLogado();

		if(funcionalidade == null) {
			return false;
		}

		return funcionalidade.isCadastravel(usuarioLogado);
	}

	public boolean podeExcluir() {

		Funcionalidade funcionalidade = getFuncionalidadeAtual();
		Usuario usuarioLogado = getUsuarioLogado();

		if(funcionalidade == null) {
			return false;
		}

		return funcionalidade.isExcluivel(usuarioLogado);
	}
}
