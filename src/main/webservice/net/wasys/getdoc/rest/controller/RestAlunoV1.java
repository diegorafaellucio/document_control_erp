package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.enumeration.StatusGeracaoPastaVermelha;
import net.wasys.getdoc.rest.request.vo.*;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.getdoc.rest.service.AlunoServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
@RequestMapping("/aluno/v1")
@Api(tags = "/aluno", description = "Serviços relacionados ao Aluno.")
public class RestAlunoV1 extends SuperController {

	@Autowired private AlunoServiceRest alunoServiceRest;

	@RequestMapping(path = "/buscar_processo_reaproveitamento", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Busca o Processo ID do processo que será reaproveitado, se houver.", notes = "Retorna o ID do processo que será utilizado para reaproveitamento. Caso os parâmetros estejam errados, ou nenhum processo seja encontrado, retorna \"null\".")
	public ResponseEntity<?> buscarProcessoReaproveitamento(@RequestBody RequestBuscarProcessoReaproveitamento requestBuscarProcessoReaproveitamento) {

		ProcessoReaproveitamentoResponse response = alunoServiceRest.buscarProcessoReaproveitamento(requestBuscarProcessoReaproveitamento);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/reaproveitar_dados", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Busca dados para reaproveitamento de processo.", notes = "Retorna dados referentes ao processo, campanha, composição familiar, documentos e imagens do aluno")
	public ResponseEntity<?> reaproveitarDados(@RequestBody RequestReaproveitarDados requestReaproveitarDados) throws Exception {

		DadosReaproveitamentoResponse response = alunoServiceRest.reaproveitarDados(requestReaproveitarDados);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/buscar_processo_reaproveitamento_isencao_disciplinas", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Busca o Processo ID do processo que será reaproveitado, se houver.", notes = "Retorna o ID do processo que será utilizado para reaproveitamento. Caso os parâmetros estejam errados, ou nenhum processo seja encontrado, retorna \"null\".")
	public ResponseEntity<?> buscarProcessoReaproveitamentoIsencaoDisciplinas() {

		DadosReaproveitamentoResponse response = alunoServiceRest.buscarProcessoReaproveitamentoIsencaoDisciplinas();

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/notificar_reaproveitamento", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Notifica o reaproveitamento de um processo.", notes = "Ao ser notificado, processo será bloqueado e não sofrerá alterações no GetDoc Captação")
	public ResponseEntity<?> notificarReaproveitamento(@RequestBody RequestNotificarReaproveitamento request) throws Exception {

		alunoServiceRest.notificarReaproveitamento(request);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(path = "/notificar_reaproveitamento-isencao-disciplinas", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value = "Notifica o reaproveitamento de um processo de isenção.", notes = "Ao ser notificado, processo sairá da lista de processos de isenção pendentes")
	public ResponseEntity<?> notificarReaproveitamentoIsencaoDisciplinas(@RequestBody RequestNotificarReaproveitamento request) throws Exception {

		alunoServiceRest.notificarReaproveitamentoIsencaoDisciplinas(request);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(
			path = "/validar_reaproveitamento/{processoId}",
			method = RequestMethod.GET,
			produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(
			value = "Informa a situação de pasta do processo."
	)
	public ResponseEntity<?> validarReaproveitamento(@PathVariable Long processoId) throws Exception {
		DadosReaproveitamentoResponse response = alunoServiceRest.validarReaproveitamento(processoId);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/iniciar_criacao_pasta_vermelha", method = RequestMethod.POST)
	@ApiOperation(value = "Retorna um UUID identificando o inicio da criação do arquivo para ser buscado no /baixar_pasta_vermelha.")
	public ResponseEntity<?> iniciarCriacaoPastaVermelha(@RequestBody IniciarCriacaoPastaVermelhaRequestVO request) throws Exception {

		IniciarCriacaoPastaVermelhaResponse response = alunoServiceRest.iniciarCriacaoPastaVermelha(request);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(path = "/baixar_pasta_vermelha", method = RequestMethod.POST, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
	@ApiOperation(value = "Retorna um csv compactado de todos os documentos obrigatórios pendentes para serem utilizado no relatório de pasta vermelha do GetDocAluno.")
	public ResponseEntity<?> baixarDadosPastaVermelha(@RequestBody BaixarDadosPastaVermelhaRequestVO request) throws Exception {

		DadosPastaVermelhaResponse response = alunoServiceRest.buscarDadosPastaVermelha(request);

		if (response.getDownloadAnexoResponse() != null) {
			DownloadAnexoResponse download = response.getDownloadAnexoResponse();
			return new ResponseEntity<>(download.getIsr(), download.getRespHeaders(), HttpStatus.OK);
		}
		else {

			StatusGeracaoPastaVermelha status = response.getStatusGeracaoPastaVermelha();
			if (StatusGeracaoPastaVermelha.NAO_ENCONTRADO.equals(status)) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			else {
				return new ResponseEntity<>(HttpStatus.ACCEPTED);
			}
		}
	}
}