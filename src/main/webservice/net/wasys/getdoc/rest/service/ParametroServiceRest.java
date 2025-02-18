package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.FilaConfiguracao;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.FilaConfiguracaoService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.rest.exception.DocumentoRestException;
import net.wasys.getdoc.rest.exception.ParametroRestException;
import net.wasys.getdoc.rest.request.vo.RequestConfiguracaoLayout;
import net.wasys.getdoc.rest.request.vo.RequestHorarioExpedientePermitido;
import net.wasys.getdoc.rest.response.vo.*;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class ParametroServiceRest extends SuperServiceRest {

    @Autowired
    private ParametroService parametroService;
    @Autowired
    private FilaConfiguracaoService filaConfiguracaoService;

    public Map<ParametroService.P, String> getHorarioExpediente(Usuario usuario) {
        Map<ParametroService.P, String> mapHorarioExpediente = new HashMap<>();
        mapHorarioExpediente.put(ParametroService.P.CHEGADA_MANHA, parametroService.getValor(ParametroService.P.CHEGADA_MANHA));
        mapHorarioExpediente.put(ParametroService.P.SAIDA_MANHA, parametroService.getValor(ParametroService.P.SAIDA_MANHA));
        mapHorarioExpediente.put(ParametroService.P.CHEGADA_TARDE, parametroService.getValor(ParametroService.P.CHEGADA_TARDE));
        mapHorarioExpediente.put(ParametroService.P.SAIDA_TARDE, parametroService.getValor(ParametroService.P.SAIDA_TARDE));
        return mapHorarioExpediente;
    }

    public Map<ParametroService.P, String> getHorarioAcesso(Usuario usuario) {
        Map<ParametroService.P, String> mapConfigHorarioAcesso = new HashMap<>();
        mapConfigHorarioAcesso.put(ParametroService.P.HORARIO_ABERTURA, parametroService.getValor(ParametroService.P.HORARIO_ABERTURA));
        mapConfigHorarioAcesso.put(ParametroService.P.HORARIO_FECHAMENTO, parametroService.getValor(ParametroService.P.HORARIO_FECHAMENTO));
        mapConfigHorarioAcesso.put(ParametroService.P.PERMITIR_ACESSO_FINAL_SEMANA_FERIADO, parametroService.getValor(ParametroService.P.PERMITIR_ACESSO_FINAL_SEMANA_FERIADO));
        return mapConfigHorarioAcesso;
    }


    public HorarioExpedientePermitidoResponse getHorarioExpedientePermitido(Usuario usuario) {
        HorarioExpedientePermitidoResponse response = new HorarioExpedientePermitidoResponse();

        response.setChegadaManha(parametroService.getValor(ParametroService.P.CHEGADA_MANHA));
        response.setSaidaManha(parametroService.getValor(ParametroService.P.SAIDA_MANHA));
        response.setChegadaTarde(parametroService.getValor(ParametroService.P.CHEGADA_TARDE));
        response.setSaidaTarde(parametroService.getValor(ParametroService.P.SAIDA_TARDE));

        response.setHorarioAbertura(parametroService.getValor(ParametroService.P.HORARIO_ABERTURA));
        response.setHorarioFechamento(parametroService.getValor(ParametroService.P.HORARIO_FECHAMENTO));
        response.setPermitirAcessoFinalSemanaFeriado(Boolean.parseBoolean(parametroService.getValor(ParametroService.P.PERMITIR_ACESSO_FINAL_SEMANA_FERIADO)));

        return response;
    }


    public HorarioExpedientePermitidoResponse atualizarParametros(Usuario usuario, RequestHorarioExpedientePermitido requestHorarioExpedientePermitido) {

        Map<ParametroService.P, String> map = new HashMap<>();
        map.put(ParametroService.P.CHEGADA_MANHA, requestHorarioExpedientePermitido.getChegadaManha());
        map.put(ParametroService.P.SAIDA_MANHA,  requestHorarioExpedientePermitido.getSaidaManha());
        map.put(ParametroService.P.CHEGADA_TARDE,  requestHorarioExpedientePermitido.getChegadaTarde());
        map.put(ParametroService.P.SAIDA_TARDE, requestHorarioExpedientePermitido.getSaidaTarde());
        map.put(ParametroService.P.HORARIO_ABERTURA,  requestHorarioExpedientePermitido.getHorarioAbertura());
        map.put(ParametroService.P.HORARIO_FECHAMENTO,  requestHorarioExpedientePermitido.getHorarioFechamento());
        map.put(ParametroService.P.PERMITIR_ACESSO_FINAL_SEMANA_FERIADO, String.valueOf(requestHorarioExpedientePermitido.isPermitirAcessoFinalSemanaFeriado()));

        Set<ParametroService.P> ps = map.keySet();
        ps.forEach(p -> {
            parametroService.setValor(p, map.get(p));
        });

        HorarioExpedientePermitidoResponse response = new HorarioExpedientePermitidoResponse(requestHorarioExpedientePermitido);
        return response;
    }

    public ConfiguracaoLayoutResponse cadastrarCustomizacao(Usuario usuario, RequestConfiguracaoLayout requestConfiguracaoLayout) throws IOException {

        Map<ParametroService.P, String> map = new HashMap<>();
        map.put(ParametroService.P.NAVBAR, requestConfiguracaoLayout.getNavbar());
        map.put(ParametroService.P.ACCENT, requestConfiguracaoLayout.getAccent());


        parametroService.salvarCustomizacaoAngular(map);

        ConfiguracaoLayoutResponse response = new ConfiguracaoLayoutResponse();
        response.setAccent(requestConfiguracaoLayout.getAccent());
        response.setNavbar(requestConfiguracaoLayout.getNavbar());
        response.setLogo("imgfiles/imagem_layout/logo.png");

       return response;
    }

    public ConfiguracaoLayoutResponse cadastrarCustomizacaoDefault(Usuario usuario, RequestConfiguracaoLayout requestConfiguracaoLayout) throws IOException {

        Map<ParametroService.P, String> map = new HashMap<>();
        map.put(ParametroService.P.NAVBAR, requestConfiguracaoLayout.getNavbar());
        map.put(ParametroService.P.ACCENT, requestConfiguracaoLayout.getAccent());

        parametroService.salvarCustomizacaoAngular(map);

        File file = parametroService.getLogoDefault();
        parametroService.salvarLogo(file);

        ConfiguracaoLayoutResponse response = new ConfiguracaoLayoutResponse();
        response.setAccent(requestConfiguracaoLayout.getAccent());
        response.setNavbar(requestConfiguracaoLayout.getNavbar());
        response.setLogo("imgfiles/imagem_layout/logo.png");

        return response;
    }


    public void cadastrarLogo(Usuario usuario, MultipartFile multipartFile) throws IOException {

        if (multipartFile == null) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        File convFile = null;
        if(multipartFile != null) {
            convFile = new File(multipartFile.getOriginalFilename());
            multipartFile.transferTo(convFile);
        }

        parametroService.salvarLogo(convFile);

    }


    public ConfiguracaoLayoutResponse getCustomizacao() throws IOException {
        ConfiguracaoLayoutResponse response = new ConfiguracaoLayoutResponse();

        Map<String, String> map =  parametroService.getCustomizacaoAngular();

        response.setAccent(map.get("ACCENT"));
        response.setNavbar(map.get("NAVBAR"));
        response.setLogo("imgfiles/imagem_layout/logo.png");

        return response;
    }
}