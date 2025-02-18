package net.wasys.getdoc.mb.controller;

import net.wasys.getdoc.domain.entity.Role;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.mb.enumerator.DeviceHeader;
import net.wasys.getdoc.mb.enumerator.DeviceSO;
import net.wasys.getdoc.mb.exception.MessageException;
import net.wasys.getdoc.mb.model.CredencialModel;
import net.wasys.getdoc.mb.model.ResultModel;
import net.wasys.getdoc.mb.model.UsuarioModel;
import net.wasys.getdoc.mb.utils.TypeUtils;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * UsuarioController.java
 * @pascke
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController extends MobileController {
	
	@Autowired private UsuarioService usuarioService;
	
	@RequestMapping(value="/autenticar", method=RequestMethod.POST)
	public ResponseEntity<UsuarioModel> autenticar(@RequestBody CredencialModel credentialModel) {
		Usuario usuario = usuarioService.autenticar(credentialModel.login, credentialModel.senha);
		if (usuario == null) {
			throw new MessageKeyException("UsuarioInvalido");
		}
		Set<Role> roles = usuario.getRoles();
		if (CollectionUtils.isEmpty(roles)) {
			throw new MessageKeyException("UsuarioNaoAutorizado");
		}
		Set<String> nomes = new HashSet<>();
		for (Role role : roles) {
			nomes.add(role.getNome());
		}
		if (!nomes.contains(RoleGD.GD_COMERCIAL.name())) {
			throw new MessageKeyException("UsuarioNaoAutorizado");
		}

		UsuarioModel usuarioModel = UsuarioModel.from(usuario);
		return new ResponseEntity<>(usuarioModel, HttpStatus.OK);
	}
	
	@RequestMapping(value="/push/del", method=RequestMethod.GET)
	public ResponseEntity<ResultModel> delPushToken() {
		if (usuario == null) {
			throw new MessageException("Usuario e obrigatório!");
		}
		Long id = usuario.getId();
		usuarioService.delDevicePushToken(id);
		ResultModel resultModel = new ResultModel();
		resultModel.success = true;
		return new ResponseEntity<ResultModel>(resultModel, HttpStatus.OK);
	}
	
	@RequestMapping(value="/push/add/{pushToken}", method=RequestMethod.GET)
	public ResponseEntity<ResultModel> addPushToken(@PathVariable String pushToken, HttpServletRequest request) {
		if (usuario == null) {
			throw new MessageException("Usuario e obrigatorio!");
		}
		Long id = usuario.getId();
		String so = request.getHeader(DeviceHeader.DEVICE_SO.key);
		DeviceSO deviceSO = TypeUtils.parse(DeviceSO.class, StringUtils.upperCase(so));
		if (deviceSO == null) {
			throw new MessageException("Header Device-SO e obrigatorio!");
		}
		usuarioService.addDevicePushToken(id, deviceSO, pushToken);
		ResultModel resultModel = new ResultModel();
		resultModel.success = true;
		return new ResponseEntity<ResultModel>(resultModel, HttpStatus.OK);
	}
	
	@RequestMapping(value="/recuperar", method=RequestMethod.POST)
	public ResponseEntity<ResultModel> recuperar(@RequestBody CredencialModel credentialModel) throws Exception {
		Usuario usuario = usuarioService.enviarRedefinicaoSenha(credentialModel.login);
		ResultModel resultModel = new ResultModel(true, getMessage("RecuperacaoSenhaMessage", usuario.getEmail()));
		return new ResponseEntity<ResultModel>(resultModel, HttpStatus.OK);
	}
}