package net.wasys.getdoc.mb.model;

import net.wasys.getdoc.domain.entity.Usuario;

public class UsuarioModel {

	public Long id;
	public String nome;
	public String email;
	
	public static UsuarioModel from(Usuario usuario) {
		UsuarioModel model = new UsuarioModel();
		model.id = usuario.getId();
		model.nome = usuario.getNome();
		model.email = usuario.getEmail();
		return model;
	}
}
