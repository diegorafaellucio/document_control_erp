package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.service.BloqueioProcessoService;
import net.wasys.getdoc.domain.vo.VerificacaoBloqueioVO;
import net.wasys.getdoc.rest.response.vo.DocumentoGraduacaoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@RequestMapping("/fila-trabalho/v1")
@Api(tags="/fila-trabalho", description="Serviços relacionados a tela de fila de trabalho.")
public class RestFilaTrabalhoV1 extends SuperController {

	@Autowired private BloqueioProcessoService bloqueioProcessoService;

	@RequestMapping(path="/verificar-bloqueio", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Lista dos processos bloqueados na fila de trabalho do analista.", response=DocumentoGraduacaoResponse.class)
	public ResponseEntity<Map> verificarBloqueio(@RequestBody List<Long> processosIds) {

		List<VerificacaoBloqueioVO> processosBloqueados = bloqueioProcessoService.findVerificacoesBloqueios(processosIds, false);
		Map<Long, String> map = new HashMap<>();
		for (VerificacaoBloqueioVO vo : processosBloqueados) {
			Long processoId = vo.getProcessoId();
			String usuarioNome = vo.getUsuarioNome();
			map.put(processoId, usuarioNome);
		}

		return new ResponseEntity(map, HttpStatus.OK);
	}
}