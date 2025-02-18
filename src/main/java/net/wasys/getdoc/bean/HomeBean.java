package net.wasys.getdoc.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class HomeBean extends AbstractBean {

	protected void initBean() {

		Usuario usuario = getUsuarioLogado();
		if(usuario != null) {
			RoleGD role = usuario.getRoleGD();
			Subperfil subperfilAtivo = usuario.getSubperfilAtivo();

			Long subperfilAtivoId = subperfilAtivo != null ? subperfilAtivo.getId() : null;
			if(Subperfil.SUBPERFIS_FIES_PROUNI_IDS.contains(subperfilAtivoId)) {
				redirect("/requisicoes/candidato/");
			}
			else if(RoleGD.GD_SALA_MATRICULA.equals(role)){
				redirect("/consultas/candidato/");
			}
			else if(RoleGD.GD_CONSULTA.equals(role)){
				redirect("/requisicoes/");
			}
			else {
				redirect("/requisicoes/fila/");
			}
		}
	}
}
