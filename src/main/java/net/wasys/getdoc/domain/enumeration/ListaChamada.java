package net.wasys.getdoc.domain.enumeration;

import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

public enum ListaChamada {
	PRIMEIRA_CHAMADA("Primeira Chamada"),
	SEGUNDA_CHAMADA("Segunda Chamada"),
	TERCEIRA_CHAMADA("Terceira Chamada"),
	QUARTA_CHAMADA("Quarta Chamada"),
	QUINTA_CHAMADA("Quinta Chamada"),
	LISTA_ESPERA("Lista de Espera");

	private String chamada;

	ListaChamada(String chamada){
		this.chamada = chamada;
	}

	public String getChamada() {
		return chamada;
	}

	public static ListaChamada getByChamada(String chamada){

		if(StringUtils.isBlank(chamada)){
			return null;
		}

		chamada = normalizeDados(chamada);

		ListaChamada[] listaChamadas = ListaChamada.values();
		for(ListaChamada key : listaChamadas){
			String keyChamada = StringUtils.upperCase(key.getChamada());
			if(StringUtils.upperCase(chamada).equals(keyChamada)) {
				return key;
			}
		}
		return null;
	}

	private static String normalizeDados(String chamada) {

		List<String> primeiraChamadaAleatoria = Arrays.asList("1ª CHAMADA", "1º CHAMADA", "1 CHAMADA");
		List<String> segundaChamadaAleatoria = Arrays.asList("2ª CHAMADA", "2º CHAMADA", "2 CHAMADA");
		List<String> terceiraChamadaAleatoria = Arrays.asList("3ª CHAMADA", "3º CHAMADA", "3 CHAMADA");
		List<String> quartaChamadaAleatoria = Arrays.asList("4ª CHAMADA", "4º CHAMADA", "4 CHAMADA");
		List<String> quintaChamadaAleatoria = Arrays.asList("5ª CHAMADA", "5º CHAMADA", "5 CHAMADA");
		List<String> listaDeEsperaAleatoria = Arrays.asList("Lista Espera");

		if(primeiraChamadaAleatoria.contains(chamada.toUpperCase())) {
			chamada = StringUtils.upperCase(PRIMEIRA_CHAMADA.getChamada());
		}
		else if(segundaChamadaAleatoria.contains(chamada.toUpperCase())) {
			chamada = StringUtils.upperCase(SEGUNDA_CHAMADA.getChamada());
		}
		else if(terceiraChamadaAleatoria.contains(chamada.toUpperCase())) {
			chamada = TERCEIRA_CHAMADA.getChamada();
		}
		else if(quartaChamadaAleatoria.contains(chamada.toUpperCase())) {
			chamada = QUARTA_CHAMADA.getChamada();
		}
		else if(quintaChamadaAleatoria.contains(chamada.toUpperCase())) {
			chamada = QUINTA_CHAMADA.getChamada();
		}
		else if(listaDeEsperaAleatoria.contains(chamada.toUpperCase())) {
			chamada = LISTA_ESPERA.getChamada();
		}

		return chamada;
	}
}
