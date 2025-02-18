package net.wasys.getdoc.rest.config;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.ExceptionService;
import net.wasys.getdoc.rest.exception.*;
import net.wasys.getdoc.rest.response.vo.ApiError;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.MessageKeyListException;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired private ExceptionService exceptionService;

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = "Parâmetros de entrada inválidos: Malformed JSON request";
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex), ex);
    }

    @ExceptionHandler(HTTP401Exception.class)
    public ResponseEntity<Object> handleHTTP401Exception(HttpServletRequest httpServletRequest, HTTP401Exception ex) {
        ApiError apiError = new ApiError(ex.getStatus(), getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(ModeloOcrRestException.class)
    public ResponseEntity<Object> handleModeloOcrRestException(HttpServletRequest httpServletRequest, ModeloOcrRestException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(FeriadoRestException.class)
    public ResponseEntity<Object> handleFeriadoRestException(HttpServletRequest httpServletRequest, FeriadoRestException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(IrregularidadeRestException.class)
    public ResponseEntity<Object> handleIrregularidadeRestException(HttpServletRequest httpServletRequest, IrregularidadeRestException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(ModeloDocumentoRestException.class)
    public ResponseEntity<Object> handleModeloDocumentoRestException(HttpServletRequest httpServletRequest, ModeloDocumentoRestException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(TextoPadraoRestException.class)
    public ResponseEntity<Object> handleTextoPadraoRestException(HttpServletRequest httpServletRequest, TextoPadraoRestException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(SubPerfilRestException.class)
    public ResponseEntity<Object> handleSubPerfilRestException(HttpServletRequest httpServletRequest, SubPerfilRestException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(UsuarioRestException.class)
    public ResponseEntity<Object> handleUsuarioRestException(HttpServletRequest httpServletRequest, UsuarioRestException ex) {
        ApiError apiError = new ApiError(ex.getStatus(), getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(TipoDocumentoRestException.class)
    public ResponseEntity<Object> handleTipoDocumentoRestException(HttpServletRequest httpServletRequest, TipoDocumentoRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,  getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(SituacaoRestException.class)
    public ResponseEntity<Object> handleSituacaoRestException(HttpServletRequest httpServletRequest, SituacaoRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,  getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(DocumentoRestException.class)
    public ResponseEntity<Object> handleDocumentoRestException(HttpServletRequest httpServletRequest, DocumentoRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,  getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    private String getMsg(MessageKeyException ex) {
        return exceptionService.getMessage(ex);
    }

    @ExceptionHandler(ProcessoRestException.class)
    public ResponseEntity<Object> handleProcessoRestException(HttpServletRequest httpServletRequest, ProcessoRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,  getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(DadosObrigatorioRequestException.class)
    public ResponseEntity<Object> handleDadosObrigatorioRequestException(HttpServletRequest httpServletRequest, DadosObrigatorioRequestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(ValidatorException.class)
    public ResponseEntity<Object> handleValidatorException(HttpServletRequest httpServletRequest, ValidatorException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(MessageKeyException.class)
    public ResponseEntity<Object> handleMessageKeyException(HttpServletRequest httpServletRequest, MessageKeyException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(MessageKeyListException.class)
    public ResponseEntity<Object> handleMessageKeyException(HttpServletRequest httpServletRequest, MessageKeyListException ex) {
        String message = exceptionService.getMessage(ex);
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, message, ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(AnexoRestException.class)
    public ResponseEntity<Object> handleAnexoRestException(HttpServletRequest httpServletRequest, AnexoRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(AreaRestException.class)
    public ResponseEntity<Object> handleAreaRestException(HttpServletRequest httpServletRequest, AreaRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(SubAreaRestException.class)
    public ResponseEntity<Object> handleSubAreaRestException(HttpServletRequest httpServletRequest, SubAreaRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(TipoProcessoRestException.class)
    public ResponseEntity<Object> handleTipoProcessoRestException(HttpServletRequest httpServletRequest, TipoProcessoRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(TipoCampoRestException.class)
    public ResponseEntity<Object> handleTipoCampoRestException(HttpServletRequest httpServletRequest, TipoCampoRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
		return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(ParametroRestException.class)
    public ResponseEntity<Object> handleParametroRestException(HttpServletRequest httpServletRequest, ParametroRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(AlunoRestException.class)
    public ResponseEntity<Object> handleAlunoRestException(HttpServletRequest httpServletRequest, AlunoRestException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, getMsg(ex), ex);
        return buildResponseEntity(apiError, ex);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleControllerException(HttpServletRequest httpServletRequest, Throwable ex) {
        logger.error("Erro inesperado", ex);
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, ex), ex);
    }

    @ResponseBody
    private ResponseEntity<Object> buildResponseEntity(ApiError apiError, Throwable exception) {
        systraceThread(String.valueOf(apiError), LogLevel.ERROR);
        exception.printStackTrace();
        LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
        String stackTrace = ExceptionUtils.getStackTrace(exception);
        logAcesso.setException(stackTrace);
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
