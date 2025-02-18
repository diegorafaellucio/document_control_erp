package net.wasys.getdoc.domain.service.webservice.detranarn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsDetranArnVO;

@Service
public class DetranArnOutrosEstadosService extends DetranArnService {

	@Autowired private ParametroService parametroService;

	@Override
	protected String montarUrl(MultiValueMap<String, Object> parametrosAPI) {

		MultiValueMap<String, String> stringMapParametrosAPI = toStringMultiValueMap(parametrosAPI);

		ConfiguracoesWsDetranArnVO cwda = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_ARN, ConfiguracoesWsDetranArnVO.class);
		
		String uriString = UriComponentsBuilder
				.fromHttpUrl(cwda.getEndPointOutros())
				.queryParams(stringMapParametrosAPI)
				.toUriString();

		return uriString;
	}
}