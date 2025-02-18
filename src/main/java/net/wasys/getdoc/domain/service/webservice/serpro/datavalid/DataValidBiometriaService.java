package net.wasys.getdoc.domain.service.webservice.serpro.datavalid;

import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsDataValidVO;
import net.wasys.getdoc.domain.vo.DataValidBiometriaRequestVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.rest.RestClient;
import org.apache.http.entity.ByteArrayEntity;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DataValidBiometriaService extends DataValidService {

	protected String chamarWebService(ConfiguracoesWsDataValidVO cwid, String accessToken, MultiValueMap<String, Object> parametros) throws Exception {

		File fotoFile = (File) parametros.getFirst(DataValidBiometriaRequestVO.FOTO);
		byte[] fotoBytes = FileUtil.readAsByteArray(fotoFile);
		Base64.Encoder encoder = Base64.getEncoder();
		String fotoStr = encoder.encodeToString(fotoBytes);
		String cpf = (String) parametros.getFirst(DataValidBiometriaRequestVO.CPF);
		Date dataValidadeCnh = (Date) parametros.getFirst(DataValidBiometriaRequestVO.DATA_VALIDADE_CNH);
		String dataValidadeCnhStr = DummyUtils.format(dataValidadeCnh, "yyyy-MM-dd");
		String nome = (String) parametros.getFirst(DataValidBiometriaRequestVO.NOME);
		nome = DummyUtils.substituirCaracteresEspeciais(nome);
		Date dataNascimento = (Date) parametros.getFirst(DataValidBiometriaRequestVO.DATA_NASCIMENTO);
		String dataNascimentoStr = DummyUtils.format(dataNascimento, "yyyy-MM-dd");
		String nomeMaeFinanciado = (String) parametros.getFirst(DataValidBiometriaRequestVO.NOME_MAE);
		nomeMaeFinanciado = DummyUtils.substituirCaracteresEspeciais(nomeMaeFinanciado);

		String endpointFace = cwid.getEndpointFace();

		RestClient rc2 = new RestClient(endpointFace);
		rc2.setTimeout(60 * 1000L);

		Map<String, String> headers2 = new LinkedHashMap<>();
		headers2.put("Authorization", "Bearer " + accessToken);
		headers2.put("Content-Type", "application/json");
		rc2.setHeaders(headers2);

		Map<String, String> key = new LinkedHashMap<>();
		key.put("cpf", cpf);

		Map<String, Object> answer = new LinkedHashMap<>();
		answer.put("biometria_face", fotoStr);
		Map<String, String> cnhMap = new LinkedHashMap<>();
		cnhMap.put("data_validade", dataValidadeCnhStr);
		answer.put("cnh", cnhMap);
		Map<String, String> filiacaoMap = new LinkedHashMap<>();
		filiacaoMap.put("nome_mae", nomeMaeFinanciado);
		answer.put("filiacao", filiacaoMap);
		answer.put("nome", nome);
		answer.put("data_nascimento", dataNascimentoStr);

		FaceRequest faceRequest = new FaceRequest();
		faceRequest.setKey(key);
		faceRequest.setAnswer(answer);
		String faceRequestJson = DummyUtils.objectToJson(faceRequest);

		ByteArrayEntity entity2 = new ByteArrayEntity(faceRequestJson.getBytes());
		return rc2.execute(entity2, String.class);
	}

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {
		return null;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		DataValidBiometriaRequestVO vo2 = (DataValidBiometriaRequestVO) vo;
		File fotoPath = vo2.getFoto();
		String cpf = vo2.getCpf();
		Date dataValidadeCnh = vo2.getDataValidadeCnh();
		String nome = vo2.getNome();
		Date dataNascimento = vo2.getDataNascimento();
		String nomeMae = vo2.getNomeMae();

		MultiValueMap<String, Object> parametros = new LinkedMultiValueMap<>(3);
		parametros.add(DataValidBiometriaRequestVO.FOTO, fotoPath);
		parametros.add(DataValidBiometriaRequestVO.CPF, cpf);
		parametros.add(DataValidBiometriaRequestVO.DATA_VALIDADE_CNH, dataValidadeCnh);
		parametros.add(DataValidBiometriaRequestVO.NOME, nome);
		parametros.add(DataValidBiometriaRequestVO.DATA_NASCIMENTO, dataNascimento);
		parametros.add(DataValidBiometriaRequestVO.NOME_MAE, nomeMae);
		return parametros;
	}

	@Override
	protected boolean isPost() {
		return true;
	}

	@Override
	protected String montarUrl(MultiValueMap<String, Object> parametrosAPI) {
		return null;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		return StatusConsultaExterna.SUCESSO;
	}
}
