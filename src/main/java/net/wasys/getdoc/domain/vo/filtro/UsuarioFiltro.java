package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;

import java.util.List;

public class UsuarioFiltro implements Cloneable {

	private RoleGD role;
	private StatusUsuario status;
	private Boolean notificarAtrasoRequisicoes;
	private Boolean gestorArea;
	private Area area;
	private Subperfil subperfil;
	private List<Long> subperfisIds;
	private Boolean notificarAtrasoSolicitacoes;
	private String login;
	private String nome;
	private Usuario usuario;
	private String emailLike;

	public StatusUsuario getStatus() {
		return status;
	}

	public void setStatus(StatusUsuario status) {
		this.status = status;
	}

	public RoleGD getRole() {
		return role;
	}

	public void setRole(RoleGD role) {
		this.role = role;
	}

	public Boolean getNotificarAtrasoRequisicoes() {
		return notificarAtrasoRequisicoes;
	}

	public void setNotificarAtrasoRequisicoes(Boolean notificarAtrasoRequisicoes) {
		this.notificarAtrasoRequisicoes = notificarAtrasoRequisicoes;
	}

	public Boolean getGestorArea() {
		return gestorArea;
	}

	public void setGestorArea(Boolean gestorArea) {
		this.gestorArea = gestorArea;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public Boolean getNotificarAtrasoSolicitacoes() {
		return notificarAtrasoSolicitacoes;
	}

	public void setNotificarAtrasoSolicitacoes(Boolean notificarAtrasoSolicitacoes) {
		this.notificarAtrasoSolicitacoes = notificarAtrasoSolicitacoes;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmailLike() {
		return emailLike;
	}

	public void setEmailLike(String emailLike) {
		this.emailLike = emailLike;
	}

	public UsuarioFiltro clone() {
		try {
			return (UsuarioFiltro) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public Subperfil getSubperfil() {
		return subperfil;
	}

	public void setSubperfil(Subperfil subperfil) {
		this.subperfil = subperfil;
	}

	public Usuario getUsuario() { return usuario; }

	public void setUsuario(Usuario usuario) { this.usuario = usuario; }

	public List<Long> getSubperfisIds() {
		return subperfisIds;
	}

	public void setSubperfisIds(List<Long> subperfisIds) {
		this.subperfisIds = subperfisIds;
	}
}