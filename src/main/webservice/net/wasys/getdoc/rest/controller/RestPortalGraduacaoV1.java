package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.SessaoHttpRequestService;
import net.wasys.getdoc.rest.request.vo.*;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.getdoc.rest.service.PortalGraduacaoServiceRest;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.servlet.LogAcessoFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@RequestMapping("/portal-graduacao/v1")
@Api(tags="/portal-graduacao", description="Serviços relacionados ao Processo, exclusivos para uso do Portal Graduação.")
public class RestPortalGraduacaoV1 extends SuperController {

	public static final Long TIPO_ALUNO_INSCRITO = 1L;
	public static final Long TIPO_ALUNO_CANDIDATO = 2L;

	@Autowired private RestUsuarioV1 restUsuarioV1;
	@Autowired private PortalGraduacaoServiceRest portalGraduacaoServiceRest;
	@Autowired private SessaoHttpRequestService sessaoHttpRequestService;

	@RequestMapping(value="/login", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Autentica o usuário.", response=LoginResponseGraduacao.class, notes="Realiza autenticação do usuário através do realm.")
	public ResponseEntity<LoginResponseGraduacao> autenticar(HttpServletRequest request, @RequestBody RequestLogin requestLogin) throws Exception {
		ResponseEntity login = restUsuarioV1.login(request, requestLogin);
		LoginResponse body = (LoginResponse) login.getBody();
		LoginResponseGraduacao login2 = new LoginResponseGraduacao();
		String nome = body.getNome();
		login2.setNome(nome);
		return new ResponseEntity(login2, HttpStatus.OK);
	}

	@RequestMapping(path="/lista-documentos", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Lista os documentos que o inscrito deve enviar.", response=DocumentoGraduacaoResponse.class)
	public ResponseEntity<DocumentoGraduacaoResponse> listaDocumentos(HttpServletRequest request, @RequestBody RequestListaDocumentosGraduacao vo) {

		StatusProcessoGraduacaoResponse response = portalGraduacaoServiceRest.listaDocumentos(vo);

		return new ResponseEntity(response, HttpStatus.OK);
	}

	@RequestMapping(path="/lista-documentos-proxy", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Lista os documentos proxy que o inscrito deve enviar.", response=StatusProcessoGraduacaoResponse.class)
	public ResponseEntity<StatusProcessoGraduacaoResponse> listaDocumentosProxy(HttpServletRequest request, @RequestBody RequestListaDocumentosProxyGraduacao vo) {
		StatusProcessoGraduacaoResponse response = portalGraduacaoServiceRest.listaDocumentosProxy(vo);

		return new ResponseEntity(response, HttpStatus.OK);
	}

	@RequestMapping(path="/status-documentos", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Consulta o status dos documentos do processo.", response=StatusProcessoGraduacaoResponse.class)
	public ResponseEntity<StatusProcessoGraduacaoResponse> statusDocumentos(HttpServletRequest request, @RequestBody RequestStatusDocumentosGraduacao vo) throws MessageKeyException {
		StatusProcessoGraduacaoResponse response = portalGraduacaoServiceRest.statusDocumentos(vo);
		return new ResponseEntity(response, HttpStatus.OK);
	}

	@RequestMapping(path="/lista-documentos-categoria-proxy/{categoria}/{processoId}", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Lista os documentos que o aluno deve enviar para a categoria indicada.", response=StatusProcessoGraduacaoResponse.class)
	public ResponseEntity<StatusProcessoGraduacaoResponse> listaDocumentosCategoriaProxy(HttpServletRequest request, @PathVariable String categoria, @PathVariable Long processoId) {
		StatusProcessoGraduacaoResponse response = portalGraduacaoServiceRest.listaDocumentosCategoriaProxy(categoria, processoId);
		return new ResponseEntity(response, HttpStatus.OK);
	}

	@InitBinder
	public void initBinder(WebDataBinder dataBinder) {
		dataBinder.registerCustomEditor(RequestUploadDocumentoGraduacao.class, new PropertyEditorSupport() {
			Object value;
			@Override
			public Object getValue() {
				return value;
			}
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				try {
					value = DummyUtils.jsonToObject(text, RequestUploadDocumentoGraduacao.class);
				}
				catch (Exception e) {
					LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
					if(logAcesso != null) {
						Map<String, String> map = new HashMap<>();
						map.put("PARSER_ERROR", text);
						String json = DummyUtils.objectToJson(map);
						logAcesso.setParameters(json);
					}
					throw e;
				}
			}
		});
	}

	@RequestMapping(path="/upload-documento", method=RequestMethod.POST, consumes=MediaType.MULTIPART_FORM_DATA_VALUE, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Upload dos documentos do inscrito.", response=DocumentoGraduacaoResponse.class)
	public ResponseEntity<DocumentoGraduacaoResponse> uploadDocumento(MultipartHttpServletRequest request, @RequestParam("dados") RequestUploadDocumentoGraduacao dados, @RequestParam ("file") List<MultipartFile> files) throws Exception {
		HttpSession session = request.getSession();
		Usuario usuario = (Usuario) session.getAttribute(AbstractBean.USUARIO_SESSION_KEY);
		if(usuario == null) {
			SessaoHttpRequest sessaoHttpRequest = sessaoHttpRequestService.findByJSessionId(request);
			usuario = sessaoHttpRequest.getUsuario();
		}
		DocumentoGraduacaoResponse response = portalGraduacaoServiceRest.uploadDocumento(dados, files, usuario);

		return new ResponseEntity(response, HttpStatus.OK);
	}

	@RequestMapping(path="/upload-documento-proxy", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Upload dos documentos Proxy do inscrito.", response=DocumentoGraduacaoResponse.class)
	public ResponseEntity<DocumentoGraduacaoResponse> uploadDocumento(HttpServletRequest request, @RequestBody RequestProxyGraduacao vo) throws Exception {

		HttpSession session = request.getSession();
		Usuario usuario = (Usuario) session.getAttribute(AbstractBean.USUARIO_SESSION_KEY);
		if(usuario == null) {
			SessaoHttpRequest sessaoHttpRequest = sessaoHttpRequestService.findByJSessionId(request);
			usuario = sessaoHttpRequest.getUsuario();
		}

		DocumentoGraduacaoResponse response = portalGraduacaoServiceRest.uploadDocumentoProxy(vo, usuario);

		return new ResponseEntity(response, HttpStatus.OK);
	}

	@RequestMapping(path="/get-last-documento-proxy", method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(value="Buscar ultimo documento do candidato.", response=DocumentoGraduacaoResponse.class)
	public ResponseEntity getLastDocumentoStream(HttpServletRequest request, @RequestBody RequestDadosLastDocumentoRequestVO vo) throws Exception {
		ResponseDownloadProxyGraduacao response = portalGraduacaoServiceRest.getLastDocumentoProxy(vo);
		return new ResponseEntity(response, HttpStatus.OK);
	}
}