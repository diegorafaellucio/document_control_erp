package net.wasys.getdoc.mb.model;

public class CredencialModel {

	public String login;
	public String senha;

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{login:" + login + ",senha:" + senha + "}";
	}
}
