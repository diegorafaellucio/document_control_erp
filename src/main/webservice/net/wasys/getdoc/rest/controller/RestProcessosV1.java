package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.vo.ResultadoConsultaVO;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.request.vo.*;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.getdoc.rest.service.ProcessoServiceRest;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * @author jonas.baggio@wasys.com.br
 * @create 24 de jul de 2018 15:18:53
 */
@CrossOrigin
@Controller
@RequestMapping("/processos/v1")
@Api(tags = "/processo", description = "Serviços relacionados ao Processo.")
public class RestProcessosV1 extends SuperController {

    @Autowired
    private ProcessoServiceRest processoServiceRest;

    @RequestMapping(
            path = "/fila-trabalho/{inicio}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cosnulta fila de trabalho.",
            response = FilaTrabalhoResponse.class,
            notes = "Serviço responsável por carregar a fila de trabalho com os filtros default, com as pendências priorizadas " +
                    "para o usuário logado."
    )
    public ResponseEntity filaTrabalho(HttpServletRequest request, @PathVariable int inicio, @PathVariable int max) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        FilaTrabalhoResponse filaTrabalhoResponse = processoServiceRest.getFilaTrabalho(sessaoHttpRequest.getUsuario(), inicio, max);
        return new ResponseEntity(filaTrabalhoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/status",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os status dos processos.",
            response = StatusProcessoResponse.class
    )
    public ResponseEntity getStatus(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<StatusProcessoResponse> statusProcessoResponse = processoServiceRest.getStatusProcessoResponse(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(statusProcessoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/sla",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os SLA disponíveis.",
            response = StatusPrazoResponse.class
    )
    public ResponseEntity getSla(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        return new ResponseEntity(StatusPrazoResponse.get(), HttpStatus.OK);
    }

    @RequestMapping(
            path = "/fila-trabalho/buscar/{inicio}/{max}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cosnulta fila de trabalho.",
            response = FilaTrabalhoResponse.class,
            notes = "Serviço responsável por carregar a fila de trabalho com os filtros default, com as pendências priorizadas " +
                    "para o usuário logado."
    )
    public ResponseEntity filaTrabalhoFiltrar(HttpServletRequest request, @PathVariable int inicio, @PathVariable int max, @RequestBody RequestFiltroFila requestFiltroFila) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        FilaTrabalhoResponse filaTrabalhoResponse = processoServiceRest.buscarFilaTrabalho(sessaoHttpRequest.getUsuario(), inicio, max, requestFiltroFila);
        return new ResponseEntity(filaTrabalhoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/trocar-analista",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Realiza a proca do analista do(s) processo(s).",
            response = String.class
    )
    public ResponseEntity trocarAnalista(HttpServletRequest request, @RequestBody RequestTrocarAnalista requestTrocarAnalista) throws HTTP401Exception, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        String s = processoServiceRest.trocarAnalista(sessaoHttpRequest.getUsuario(), requestTrocarAnalista);
        return new ResponseEntity(s, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/concluir",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Mudo a situação de um ou mais processos.",
            response = String.class
    )
    public ResponseEntity concluirProcessosMassa(HttpServletRequest request, @RequestBody RequestConcluirProcessos requestConcluirProcessos) throws DadosObrigatorioRequestException, HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        String s = processoServiceRest.concluirProcessosMassa(sessaoHttpRequest.getUsuario(), requestConcluirProcessos);
        return new ResponseEntity(s, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os registros/detalhes de um processo.",
            response = DetalhesProcesso.class
    )
    public ResponseEntity detalharProcesso(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesProcesso detalhes = processoServiceRest.detalhar(sessaoHttpRequest.getUsuario(), getImagePath(request), processoId);
        return new ResponseEntity(detalhes, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/grupos-campo/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os grupos de campos de um processo.",
            response = GrupoCamposResponse.class
    )
    public ResponseEntity getGruposCamposByProcesso(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<GrupoCamposResponse> gruposCampo = processoServiceRest.getGruposCampoByProcesso(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(gruposCampo, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/acompanhamento/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os logs/acompanhamento do processo.",
            response = AcompanhamentoResponse.class
    )
    public ResponseEntity getAcompanhamento(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        getSessaoHttpRequest(request);
        List<AcompanhamentoResponse> acompanhamento = processoServiceRest.getAcompanhamento(processoId);
        return new ResponseEntity(acompanhamento, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/acompanhamento/anexos/{processoLogId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os anexos do acompanhamento do processo.",
            response = ProcessoLogAnexoResponse.class
    )
    public ResponseEntity getAnexosAcompanhamento(HttpServletRequest request, @PathVariable Long processoLogId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        getSessaoHttpRequest(request);
        List<ProcessoLogAnexoResponse> anexosAcompanhamento = processoServiceRest.getAnexosAcompanhamento(processoLogId);
        return new ResponseEntity(anexosAcompanhamento, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/documentos/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os documentos do processo.",
            response = ListaDocumentoResponse.class
    )
    public ResponseEntity getDocumentos(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaDocumentoResponse documentos = processoServiceRest.getDocumentos(sessaoHttpRequest.getUsuario(), getImagePath(request), processoId);
        return new ResponseEntity(documentos, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/tipo/campos/{tipoProcessoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os grupos de campos de um processo.",
            response = GrupoCamposResponse.class
    )
    public ResponseEntity getGruposCamposByTipoProcesso(HttpServletRequest request, @PathVariable Long tipoProcessoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<GrupoCamposResponse> gruposCampo = processoServiceRest.getGruposCampoByTipoProcesso(sessaoHttpRequest.getUsuario(), tipoProcessoId);
        return new ResponseEntity(gruposCampo, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/novo",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cria um novo processo/requisição.",
            response = NovoProcessoResponse.class
    )
    public ResponseEntity novo(HttpServletRequest request, @RequestBody RequestCriarProcesso requestCriarProcesso) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Origem origem = getOrigemFromHeader(request);
        NovoProcessoResponse novoProcessoResponse = processoServiceRest.criarNovoProcesso(sessaoHttpRequest.getUsuario(), requestCriarProcesso,origem);
        return new ResponseEntity(novoProcessoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/priorizar/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Prioriza um processo."
    )
    public ResponseEntity priorizar(HttpServletRequest request, @PathVariable Long processoId) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.priorizar(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/enviar-para-analise/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Envia um processo para análise."
    )
    public ResponseEntity enviarParaAnalise(HttpServletRequest request, @PathVariable Long processoId) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.enviarParaAnalise(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/requisicoes/cliente/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Envia um processo para análise.",
            response = ProcessoMesmoClienteResponse.class
    )
    public ResponseEntity getRequisicoesMesmoCliente(HttpServletRequest request, @PathVariable Long processoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ProcessoMesmoClienteResponse> processoMesmoClienteResponse = processoServiceRest.requisicoesMesmoCliente(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(processoMesmoClienteResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Altera os dados cadastrais de um processo."
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestEditarProcesso requestEditarProcesso) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesProcesso detalhes = processoServiceRest.editar(sessaoHttpRequest.getUsuario(), getImagePath(request), processoId, requestEditarProcesso);
        return new ResponseEntity(detalhes, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{processoId}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Deleta um processo."
    )
    public ResponseEntity deletar(HttpServletRequest request, @PathVariable Long processoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.excluir(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/motivos-cancelamento/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os motivos possíveis para cancelametno do proceso.",
            response = SituacaoResponse.class
    )
    public ResponseEntity consultaMotivosParaCancelamento(HttpServletRequest request, @PathVariable Long processoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SituacaoResponse> situacaoResponses = processoServiceRest.consultaMotivosCancelamento(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(situacaoResponses, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/adicionar-documento/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Adiciona um documento a um processo."
    )
    public ResponseEntity adicionarDocumento(HttpServletRequest request, @PathVariable Long processoId, @RequestBody List<Long> documentosId) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.adicionarDocumento(sessaoHttpRequest.getUsuario(), processoId, documentosId);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/excluir-documento/{documentoId}/{processoId}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Exclui um documento de um processo."
    )
    public ResponseEntity excluirDocumento(HttpServletRequest request, @PathVariable Long processoId, @PathVariable Long documentoId) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.excluirDocumento(sessaoHttpRequest.getUsuario(), processoId, documentoId);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/motivos-acompanhamento/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os motivos possíveis para acompanhamento do proceso.",
            response = SituacaoResponse.class
    )
    public ResponseEntity consultaMotivosParaAcompanhamento(HttpServletRequest request, @PathVariable Long processoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SituacaoResponse> situacaoResponses = processoServiceRest.consultaMotivosAcompanhamento(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(situacaoResponses, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/upload-evidencia/{processoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem para registrar evidencia.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload de imagem para registrar uma evidência."
    )
    public ResponseEntity passo1UploadAnexoRegistroEvidencia(MultipartHttpServletRequest request, @PathVariable Long processoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = processoServiceRest.passo1UploadAnexoRegistroEvidencia(sessaoHttpRequest.getUsuario(), processoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/registrar-evidencia/{processoId}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Registra um acompanhamento para o processo.",
            response = SituacaoResponse.class
    )
    public ResponseEntity registrarEvidencia(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestRegistrarEvidencia requestRegistrarEvidencia) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.registrarEvidencia(sessaoHttpRequest.getUsuario(), processoId, requestRegistrarEvidencia);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/upload-cancelamento/{processoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem para registrar cancelamento.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload de imagem para registrar cancelamento."
    )
    public ResponseEntity passo1UploadAnexoSolicitacaoCancelamento(MultipartHttpServletRequest request, @PathVariable Long processoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = processoServiceRest.passo1UploadAnexoSolicitacaoCancelamento(sessaoHttpRequest.getUsuario(), processoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cancelar/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cencela um processo."
    )
    public ResponseEntity cancelar(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestCancelarProcesso requestCancelarProcesso) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.cancelar(sessaoHttpRequest.getUsuario(), processoId, requestCancelarProcesso);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/upload-acompanhamento/{processoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem para registrar acompanhamento.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload de imagem para registrar acompanhamento."
    )
    public ResponseEntity passo1UploadAnexoSolicitacaoAcompanhamento(MultipartHttpServletRequest request, @PathVariable Long processoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = processoServiceRest.passo1UploadAnexoSolicitacaoAcompanhamento(sessaoHttpRequest.getUsuario(), processoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/acompanhar/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Solicita acompanhamento do processo."
    )
    public ResponseEntity acompanhar(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestAcompanharProcesso requestAcompanharProcesso) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.acompanhar(sessaoHttpRequest.getUsuario(), processoId, requestAcompanharProcesso);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/destinatarios/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém a lista de destinatários possíveis para o processo.",
            response = String.class
    )
    public ResponseEntity getDestinatarios(HttpServletRequest request, @PathVariable Long processoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<String> list = processoServiceRest.getDestinatarios(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/upload-email/{processoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem para anexar no envio de email.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload de imagem para ser anexada no envio de email."
    )
    public ResponseEntity passo1UploadAnexoEnvioEmail(MultipartHttpServletRequest request, @PathVariable Long processoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = processoServiceRest.passo1UploadAnexoEnvioEmail(sessaoHttpRequest.getUsuario(), processoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/enviar-email/{processoId}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Envia email."
    )
    public ResponseEntity enviarEmail(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestEnviarEmail requestEnviarEmail) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.enviarEmail(sessaoHttpRequest.getUsuario(), processoId, requestEnviarEmail);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/motivos-concluir-tarefa/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os motivos possíveis para concluir a tarefa.",
            response = SituacaoResponse.class
    )
    public ResponseEntity consultaMotivosParaConcluirTarefa(HttpServletRequest request, @PathVariable Long processoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SituacaoResponse> situacaoResponses = processoServiceRest.consultaMotivosConcluirTarefa(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(situacaoResponses, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/upload-concluir-tarefa/{processoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem para registrar conclusão da tarefa.",
            response = String.class
    )
    public ResponseEntity passo1UploadAnexoConcluirTarefa(MultipartHttpServletRequest request, @PathVariable Long processoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = processoServiceRest.passo1UploadAnexoConcluirTarefa(sessaoHttpRequest.getUsuario(), processoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/concluir-tarefa/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Concluir tarefa."
    )
    public ResponseEntity concluirTarefa(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestConcluirTarefa requestConcluirTarefa) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.concluirTarefa(sessaoHttpRequest.getUsuario(), processoId, requestConcluirTarefa);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/motivos-reabrir-tarefa/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os motivos possíveis para concluir a tarefa.",
            response = SituacaoResponse.class
    )
    public ResponseEntity consultaMotivosParaReabrirTarefa(HttpServletRequest request, @PathVariable Long processoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SituacaoResponse> situacaoResponses = processoServiceRest.consultaMotivosReabrirTarefa(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(situacaoResponses, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/upload-reabrir-tarefa/{processoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem para registrar reabertura da tarefa.",
            response = String.class
    )
    public ResponseEntity passo1UploadAnexoReabrirTarefa(MultipartHttpServletRequest request, @PathVariable Long processoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = processoServiceRest.passo1UploadAnexoReabrirTarefa(sessaoHttpRequest.getUsuario(), processoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/reabrir-tarefa/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Concluir tarefa."
    )
    public ResponseEntity reabrirTarefa(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestReabrirTarefa requestReabrirTarefa) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = processoServiceRest.reabrirTarefa(sessaoHttpRequest.getUsuario(), processoId, requestReabrirTarefa);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/pesquisar/{min}/{max}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Pesquisa processos.",
            response = ListaProcessosResponse.class,
            notes = "Serviço responsável por pesquisar processos."
    )
    public ResponseEntity pesquisar(HttpServletRequest request, @PathVariable int min, @PathVariable int max, @RequestBody RequestPesquisaProcesso requestPesquisaProcesso) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaProcessosResponse listaProcessosResponse = processoServiceRest.pesquisar(sessaoHttpRequest.getUsuario(), min, max, requestPesquisaProcesso);
        return new ResponseEntity(listaProcessosResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/campos/situacao/{processoId}/{situacaoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os grupos de campos de um processo com situacao.",
            response = GrupoCamposResponse.class
    )
    public ResponseEntity getGruposCamposByTipoProcessoAndSituacao(HttpServletRequest request, @PathVariable Long processoId,@PathVariable Long situacaoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<GrupoCamposResponse> gruposCampo = processoServiceRest.getGruposCampoByProcessoAndSituacao(sessaoHttpRequest.getUsuario(), processoId,situacaoId);
        return new ResponseEntity(gruposCampo, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/campos/opcoes-filtro",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Busca as opções de um campo filho, segundo o que foi selecionado no campo pai.",
            response = RequestOpcoesFiltro.class
    )
    public ResponseEntity getOpcoesFiltro(HttpServletRequest request, @RequestBody RequestOpcoesFiltro rof) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<OpcoesBaseInternaResponse> response = processoServiceRest.getOpcoesFiltro(sessaoHttpRequest.getUsuario(), rof);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/proxima-requisicao",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Solicita id da proxima requisição do processo.",
            response = Long.class
    )
    public ResponseEntity getProximaRequisicao(HttpServletRequest request) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Long response = processoServiceRest.getProximaRequisicao(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/regras/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as regras de um processo.",
            response = ProcessoRegraResponse.class
    )
    public ResponseEntity regrasProcesso(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
         List<ProcessoRegraResponse> response = processoServiceRest.getRegrasProcesso(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/regra-reprocessar/{regraId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Reprocessa uma regra especifica de um processo.",
            response = ProcessoRegraResponse.class
    )
    public ResponseEntity regraRerocessar(HttpServletRequest request, @PathVariable Long regraId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ProcessoRegraResponse> response = processoServiceRest.reprocessarRegra(sessaoHttpRequest.getUsuario(), regraId);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/regras-reprocessar-todas/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Reprocessa todas as regras de um processo.",
            response = ProcessoRegraResponse.class
    )
    public ResponseEntity regraRerocessarTodas(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ProcessoRegraResponse> response = processoServiceRest.reprocessarRegrasTodas(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/regras-reprocessar-erros/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Reprocessa todas as regras de um processo.",
            response = ProcessoRegraResponse.class
    )
    public ResponseEntity regraRerocessarComErros(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ProcessoRegraResponse> response = processoServiceRest.regraRerocessarComErros(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/consultas-externas/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "recupera as consultas externas.",
            response = ResultadoConsultaVO.class
    )
    public ResponseEntity getconsultasExternas(HttpServletRequest request, @PathVariable Long processoId) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ResultadoConsultaVO> resultadoConsultaVOS = this.processoServiceRest.consultasExternas(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(resultadoConsultaVOS, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/atualizacao-fila",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Busca atualizações na fila de trabalho."
    )
    public ResponseEntity getAtualizacoesFila(HttpServletRequest request, HttpServletResponse response, @RequestBody List<Long> idsProcessos) {

        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<FilaTrabalhoVoResponse> filaTrabalhoVoResponses = this.processoServiceRest.getAtualizacoesFila(idsProcessos);

        return new ResponseEntity(filaTrabalhoVoResponses, HttpStatus.OK);
    }
}