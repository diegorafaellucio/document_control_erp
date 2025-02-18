package net.wasys.getdoc.domain.service.webservice.detranarn;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsDetranArnVO;
import net.wasys.getdoc.domain.vo.DetranArnRequestVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;

@Service
public abstract class DetranArnService extends RestWebServiceClient {

	private static final String CAMPO_PLACA = "strPlaca";
	private static final String CAMPO_SENHA = "strSenha";
	private static final String CAMPO_USUARIO = "strUsuario";
	private static final String CAMPO_UF = "strUf";
	private static final String CAMPO_CHASSI = "strChassi";

	@Autowired private ParametroService parametroService;

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {

		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(2);

		ConfiguracoesWsDetranArnVO cwda = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_ARN, ConfiguracoesWsDetranArnVO.class);
		queryParams.add(CAMPO_USUARIO, cwda.getLogin());
		queryParams.add(CAMPO_SENHA, cwda.getSenha());

		return queryParams;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		DetranArnRequestVO detranArnConsultaVo = (DetranArnRequestVO) vo;
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(3);

		String uf = detranArnConsultaVo.getUf();
		String placa = detranArnConsultaVo.getPlaca();
		String chassi = detranArnConsultaVo.getChassi();

		queryParams.add(CAMPO_UF, uf);
		preencherPlacaOuChassi(queryParams, placa, chassi);

		return queryParams;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {

		JsonNode statusPesquisa = resultadoJson.findPath("statusPesquisa");
		return !statusPesquisa.isMissingNode() && "1".equals(statusPesquisa.asText()) ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO;
	}

	private void preencherPlacaOuChassi(MultiValueMap<String, Object> queryParams, String placa, String chassi) {

		if (!StringUtils.isBlank(chassi)) {
			queryParams.add(CAMPO_CHASSI, chassi);
			queryParams.add(CAMPO_PLACA, "");
		} else {
			queryParams.add(CAMPO_CHASSI, "");
			queryParams.add(CAMPO_PLACA, placa);
		}
	}

	@Override
	protected boolean isPost() {
		return false;
	}
}