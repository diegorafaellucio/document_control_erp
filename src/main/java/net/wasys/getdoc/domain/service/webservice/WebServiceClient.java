package net.wasys.getdoc.domain.service.webservice;

import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.service.ConsultaExternaLogService;
import net.wasys.getdoc.domain.service.ConsultaExternaService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Date;
import java.util.TreeMap;

public abstract class WebServiceClient {

	protected abstract MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo);
	protected abstract MultiValueMap<String, Object> getParametros(WebServiceClientVO vo);
	protected abstract ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros);
	protected abstract StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson);

	@Autowired private ConsultaExternaService consultaExternaService;
	@Autowired private ConsultaExternaLogService consultaExternaLogService;
	@Autowired protected ResourceService resourceService;

	private ObjectMapper om = new ObjectMapper();

	@Transactional(rollbackFor=Exception.class)
	public ConsultaExterna consultar(WebServiceClientVO vo) {

		MultiValueMap<String, Object> parametros = getParametros(vo);
		TipoConsultaExterna tipoConsultaExterna = vo.getTipoConsultaExterna();
		ConsultaExterna consultaExternaAnterior = buscarConsultaExternaAnterior(tipoConsultaExterna, parametros);
		if (consultaExternaAnterior == null) {
			ConsultaExterna ce = realizarNovaConsulta(vo, parametros);
			return ce;
		}
		else {
			consultaExternaLogService.criarConsultaExternaLog(vo, consultaExternaAnterior);
			return consultaExternaAnterior;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ConsultaExterna consultarInvalidandoConsultaAnterior(WebServiceClientVO vo) {

		MultiValueMap<String, Object> parametros = getParametros(vo);

		ConsultaExterna consultaExternaAnterior = buscarConsultaExternaAnterior(vo.getTipoConsultaExterna(), parametros);
		if(consultaExternaAnterior != null) {
			consultaExternaAnterior.setStatus(StatusConsultaExterna.INATIVA);
			consultaExternaService.saveOrUpdate(consultaExternaAnterior);
		}

		return realizarNovaConsulta(vo, parametros);
	}

	protected ConsultaExterna realizarNovaConsulta(WebServiceClientVO vo, MultiValueMap<String, Object> parametros) {

		MultiValueMap<String, Object> parametrosAPI = getParametrosApi(vo);
		parametrosAPI = parametrosAPI != null ? parametrosAPI : new LinkedMultiValueMap<>();
		parametrosAPI.putAll(parametros);

		ConsultaExterna consultaExterna = chamarWebService(parametrosAPI);

		TipoConsultaExterna tipo = consultaExterna != null ? consultaExterna.getTipo() : null;
		String mensagem = consultaExterna != null ? consultaExterna.getMensagem() : null;
		if(StringUtils.isBlank(mensagem) && (
				TipoConsultaExterna.SIA_CONSULTA_LINHA_TEMPO.equals(tipo) ||
				TipoConsultaExterna.SIA_CONSULTA_INSCRICOES.equals(tipo) ||
				TipoConsultaExterna.SIA_CONSULTA_COMPROVANTE_INSCRICAO.equals(tipo) ||
				TipoConsultaExterna.SIA_ATUALIZA_DOCUMENTO.equals(tipo) ||
				TipoConsultaExterna.SIA_ANALISE_ISENCAO_DISCIPLINAS.equals(tipo)
		)) {
			JSONObject resultado = new JSONObject(consultaExterna.getResultado());
			if(!resultado.getBoolean("Success") && resultado.has("Message")) {
				mensagem = resultado.getString("Message");
				consultaExterna.setMensagem(mensagem);
			}
		}

		if(consultaExterna != null) {
			atualizarConsultaExterna(consultaExterna, vo, parametros);
			consultaExternaService.salvarComLog(consultaExterna, vo);
		}

		return consultaExterna;
	}

	private ConsultaExterna buscarConsultaExternaAnterior(TipoConsultaExterna consultaExterna, MultiValueMap<String, Object> parametros) {

		String parametrosJson = ordenarEConverterParametrosParaJson(parametros);
		ConsultaExterna consultaExternaAnterior = consultaExternaService.buscarConsultaAnteriorValida(parametrosJson, consultaExterna);

		return consultaExternaAnterior;
	}

	protected void atualizarConsultaExterna(ConsultaExterna consultaExterna, WebServiceClientVO vo, MultiValueMap<String, Object> parametros) {

		consultaExterna.setUsuario(vo.getUsuario());
		consultaExterna.setData(new Date());
		String parametrosJson = ordenarEConverterParametrosParaJson(parametros);
		consultaExterna.setParametros(parametrosJson);
		consultaExterna.setTipo(vo.getTipoConsultaExterna());

		StatusConsultaExterna status = StatusConsultaExterna.ERRO;
		String resultado = consultaExterna.getResultado();
		try {
			if(StringUtils.isNotBlank(resultado)) {
				JsonNode readTree = om.readTree(resultado);
				status = validarSucessoOuErro(consultaExterna, readTree);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			String stackTrace = DummyUtils.getStackTrace(e);
			consultaExterna.setStackTrace(stackTrace);

			String mensagem = DummyUtils.getExceptionMessage(e);
			consultaExterna.setMensagem(mensagem);
		}
		consultaExterna.setStatus(status);

		if(StatusConsultaExterna.ERRO.equals(status)) {
			if(consultaExterna.getStackTrace() == null) {
				consultaExterna.setStackTrace(resultado);
			}
			if(consultaExterna.getMensagem() == null) {
				if(resultado != null && resultado.length() > 1000) {
					resultado = resultado.substring(0, 999);
				}
				consultaExterna.setMensagem(resultado);
			}
		}
	}

	private String ordenarEConverterParametrosParaJson(MultiValueMap<String, Object> parametros) {
		return om.writeValueAsString(new TreeMap<>(parametros));
	}
}
