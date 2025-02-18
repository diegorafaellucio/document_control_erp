package net.wasys.getdoc.domain.service.webservice.brscan;

import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ImagemService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
import net.wasys.getdoc.domain.vo.BrScanRequestVO;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsBrScanVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.rest.RestClient;
import org.apache.http.entity.ByteArrayEntity;
import org.aspectj.util.FileUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class BrScanService extends RestWebServiceClient {

    @Autowired private ParametroService parametroService;
    @Autowired private ImagemService imagemService;

    protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

        ConfiguracoesWsBrScanVO cwbs = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_BRSCAN, ConfiguracoesWsBrScanVO.class);
        String resultadoJson = null;
        String mensagem = null;
        String stackTrace = null;

        try {
            resultadoJson = cadastrarConsulta(cwbs, parametros);
        }catch (Exception e) {
            mensagem = DummyUtils.getExceptionMessage(e);
            mensagem = "Erro ao chamar WebService: " + mensagem;
            systraceThread(mensagem, LogLevel.ERROR);
            e.printStackTrace();
            stackTrace = DummyUtils.getStackTrace(e);
        }

        ConsultaExterna consultaExterna = new ConsultaExterna();
        consultaExterna.setResultado(resultadoJson);
        consultaExterna.setStackTrace(stackTrace);
        consultaExterna.setMensagem(mensagem);
        return consultaExterna;
    }

    private String cadastrarConsulta(ConfiguracoesWsBrScanVO cwbs, MultiValueMap<String, Object> parametros) throws IOException {

        String documentoIdstr = (String) parametros.getFirst(BrScanRequestVO.DOCUMENTO);
        Long documentoId = new Long(documentoIdstr);
        String documentoStr = getImgBase64(documentoId);

        String selfieIdStr = (String) parametros.getFirst(BrScanRequestVO.SELFIE);
        Long selfieId = new Long(selfieIdStr);
        String selfieStr = getImgBase64(selfieId);

        String cpf = (String) parametros.getFirst(BrScanRequestVO.CPF);
        String endpointCadastrar = cwbs.getEndpointCadastrar();

        RestClient rc1 = new RestClient(endpointCadastrar);
        rc1.setRepeatTimes(1);

        Map<String, String> headers1 = new LinkedHashMap<>();
        headers1.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        rc1.setHeaders(headers1);

        Map<String, Object> map = new LinkedHashMap<>();
        String usuario = cwbs.getUsuario();
        String senha = cwbs.getSenha();
        map.put("codLogin", usuario);
        map.put("desSenha", senha);

        Map<String, Object> indexadorAdicional = new LinkedHashMap<>();
        indexadorAdicional.put("campo", "Número do CPF");
        indexadorAdicional.put("valor", cpf);

        List<Object> indexadoresAdicionais = new ArrayList<>();
        indexadoresAdicionais.add(indexadorAdicional);
        map.put("indexadoresAdicionais", indexadoresAdicionais);

        Map<String, Object> arquivo = new LinkedHashMap<>();

        List<Map<String, Object>> arquivos = new ArrayList<>();
        arquivo.put("tipo", "Doc. de Identificação");
        arquivo.put("arquivo", documentoStr);
        arquivos.add(arquivo);

        Map<String, Object> selfie = new LinkedHashMap<>();
        selfie.put("tipo", "Doc. de Identificação");
        selfie.put("arquivo", selfieStr);
        arquivos.add(selfie);
        map.put("arquivos", arquivos);

        String json = DummyUtils.objectToJson(map);

        ByteArrayEntity entity2 = new ByteArrayEntity(json.getBytes("UTF-8"));

        systraceThread("acessando url de consulta...");
        Map map2 = null;
        try {
            map2 = rc1.execute(entity2, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        systraceThread(String.valueOf(map2));

        JSONObject jsonRet = new JSONObject(map2);

        return jsonRet.toString();
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
    protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {
        return null;
    }

    @Override
    protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {
        BrScanRequestVO vo2 = (BrScanRequestVO) vo;
        String documento = ((BrScanRequestVO) vo).getDocumentoIdentificacao();
        String cpf = ((BrScanRequestVO) vo).getCpf();
        String selfie = ((BrScanRequestVO) vo).getSelfie();

        MultiValueMap<String, Object> parametros = new LinkedMultiValueMap<>(3);
        parametros.add(BrScanRequestVO.DOCUMENTO, documento);
        parametros.add(BrScanRequestVO.CPF, cpf);
        parametros.add(BrScanRequestVO.SELFIE, selfie);
        return parametros;
    }

    @Override
    protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
        //se chegou até aqui é pq retornou um json, se retornou um json considera como sucesso
        return StatusConsultaExterna.SUCESSO;
    }

    private String getImgBase64(Long imgId) throws IOException {
        Base64.Encoder encoder = Base64.getEncoder();

        Imagem imagem = imagemService.getPrimeiraImagem(imgId);
        File file = imagemService.getFile(imagem);
        byte[] selfieBytes = FileUtil.readAsByteArray(file);
        return encoder.encodeToString(selfieBytes);
    }

}