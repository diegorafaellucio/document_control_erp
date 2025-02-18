package net.wasys.getdoc.mb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.mb.model.VersionModel;

@Controller
@RequestMapping("/cache")
public class CacheController extends MobileController {

	@Autowired private ParametroService parametroService;

	@RequestMapping("/verificar")
	public ResponseEntity<VersionModel> verificar() {
		Integer number = parametroService.getMobileCacheVersion();
		if (number == null) {
			number = 1;
		}
		VersionModel model = new VersionModel();
		model.number = number;
		return new ResponseEntity<VersionModel>(model, HttpStatus.OK);
	}
}