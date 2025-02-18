package net.wasys.getdoc.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.service.ResourceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.AreaDataModel;
import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.AreaService;
import net.wasys.getdoc.domain.service.SubareaService;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class AreaCrudBean extends AbstractBean {

	@Autowired private AreaService areaService;
	@Autowired private SubareaService subareaService;
	@Autowired private ResourceService resourceService;

	private AreaDataModel dataModel;
	private Area area;
	private List<Subarea> subareas;

	private boolean disabled;

	public void initBean() {

		dataModel = new AreaDataModel();
		dataModel.setService(areaService);

		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		disabled = StringUtils.isNotBlank(geralEndpoint);
	}

	public void salvar() {
		try {
			boolean insert = isInsert(area);
			Usuario usuario = getUsuarioLogado();
			areaService.saveOrUpdate(area, subareas, usuario);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {
		Usuario usuarioLogado = getUsuarioLogado();
		Long areaId = area.getId();
		try {
			areaService.excluir(areaId, usuarioLogado);
			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public AreaDataModel getDataModel() {
		return dataModel;
	}

	public List<Subarea> getSubareas() {
		return subareas;
	}

	public void addSubarea() {
		Subarea subarea = new Subarea();
		subarea.setArea(area);
		subarea.setAtivo(true);
		subarea.setDataAtualizacao(new Date());
		subareas.add(subarea);
	}

	public void removeSubarea(Subarea sa) {
		subareas.remove(sa);
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {

		if(area == null) {

			area = new Area();

			Subarea subarea = new Subarea();
			subarea.setArea(area);
			subarea.setAtivo(true);
			subarea.setDataAtualizacao(new Date());
			subarea.setDescricao("Padrão");

			this.area = area;
			subareas = new ArrayList<>();
			subareas.add(subarea);
		}
		else {

			this.area = area;
			Long areaId = area.getId();

			subareas = subareaService.findByArea(areaId);
		}
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
