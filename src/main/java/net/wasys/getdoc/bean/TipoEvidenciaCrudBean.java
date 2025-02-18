package net.wasys.getdoc.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import java.util.*;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.TipoEvidenciaDataModel;
import net.wasys.getdoc.domain.service.TipoEvidenciaService;
import net.wasys.util.faces.AbstractBean;
import net.wasys.getdoc.domain.service.SubperfilService;


@ManagedBean
@ViewScoped
public class TipoEvidenciaCrudBean extends AbstractBean {

	@Autowired private TipoEvidenciaService tipoEvidenciaService;
	@Autowired private SubperfilService subperfilService;

	private TipoEvidenciaDataModel dataModel;
	private TipoEvidencia tipoEvidencia;
    private List<RoleGD> rolesGD = new ArrayList<>();
    private List<String> rolesGDSelecionados = new ArrayList<>();
    private List<TipoEvidenciaRole> tipoEvidenciaRoles = new ArrayList<>();

	protected void initBean() {
		dataModel = new TipoEvidenciaDataModel();
		dataModel.setService(tipoEvidenciaService);

		rolesGD = Arrays.asList(RoleGD.values());
	}

	public void salvar() {

		try {
			boolean insert = isInsert(tipoEvidencia);
			Usuario usuario = getUsuarioLogado();

			if(tipoEvidenciaRoles != null) {
				for (TipoEvidenciaRole ter : new ArrayList<>(tipoEvidenciaRoles)) {
					String role = ter.getRole();
					if (!rolesGDSelecionados.contains(role)) {
						tipoEvidenciaRoles.remove(ter);
					}
				}
			}

			for(String rgd: rolesGDSelecionados){
				boolean novoRegistro = true;
				for (TipoEvidenciaRole ter : tipoEvidenciaRoles) {
					String role = ter.getRole();
					if(rgd.equals(role)){
						novoRegistro = false;
					}
				}

				if(novoRegistro) {
					TipoEvidenciaRole tipoEvidenciaRole = new TipoEvidenciaRole();
					tipoEvidenciaRole.setRole(rgd);
					if(!insert) {
                        tipoEvidenciaRole.setTipoEvidencia(tipoEvidencia);
                    }
					tipoEvidenciaRoles.add(tipoEvidenciaRole);
				}
			}

			tipoEvidencia.setTiposEvidenciasRoles(tipoEvidenciaRoles);

			tipoEvidenciaService.saveOrUpdate(tipoEvidencia, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long tipoEvidenciaId = tipoEvidencia.getId();

		try {
			tipoEvidenciaService.excluir(tipoEvidenciaId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public TipoEvidenciaDataModel getDataModel() {
		return dataModel;
	}

	public TipoEvidencia getTipoEvidencia() {
		return tipoEvidencia;
	}

	public void setTipoEvidencia(TipoEvidencia tipoEvidencia) {

        if(tipoEvidencia == null) {
            tipoEvidencia = new TipoEvidencia();
        }

        rolesGDSelecionados = new ArrayList<>();

        tipoEvidenciaRoles = tipoEvidencia.getTiposEvidenciasRoles();
        if(tipoEvidenciaRoles != null) {
            tipoEvidenciaRoles.forEach(ter -> {
                String role = ter.getRole();
                rolesGDSelecionados.add(role);
            });
        }

        this.tipoEvidencia = tipoEvidencia;
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
}
