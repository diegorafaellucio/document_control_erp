package net.wasys.getdoc.mb.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.wasys.getdoc.mb.enumerator.Tela;
import net.wasys.getdoc.mb.model.LinkModel;

/**
 *
 * LinkController.java
 * @pascke
 */
@Controller
@RequestMapping("/link")
public class LinkController extends MobileController {

	@RequestMapping(value="/listar", method=RequestMethod.POST)
	public ResponseEntity<List<LinkModel>> listar() {
		List<LinkModel> links = new ArrayList<>();
		links.add(new LinkModel(getMessage("Novo"), "requisicao/nova.xhtml?origem=" + Tela.NOVA_REQUISICAO.name()));
		links.add(new LinkModel(getMessage("FilaTrabalho"), "requisicao/fila.xhtml"));
		links.add(new LinkModel(getMessage("Pesquisa"), "pesquisa/lista.xhtml"));
		return new ResponseEntity<List<LinkModel>>(links, HttpStatus.OK);
	}
}