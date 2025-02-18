package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistroValor;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.service.BaseInternaService;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.BaseRegistroValorService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseInternaFiltro;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.rest.exception.BaseInternaRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroBaseInterna;
import net.wasys.getdoc.rest.request.vo.RequestProcessoSeguro;
import net.wasys.getdoc.rest.request.vo.RequestVisualizarBaseInterna;
import net.wasys.getdoc.rest.response.vo.BaseInternaResponse;
import net.wasys.getdoc.rest.response.vo.RegistroValorResponse;
import net.wasys.getdoc.rest.response.vo.SegurosResponse;
import net.wasys.util.DummyUtils;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class BaseInternaServiceRest extends SuperServiceRest {

    @Autowired private BaseInternaService baseInternaService;
    @Autowired private BaseRegistroService baseRegistroService;
    @Autowired private BaseRegistroValorService baseRegistroValorService;
    @Autowired private ProcessoService processoService;

    public List<BaseInternaResponse> getBaseAtivas() {
        List<BaseInterna> all = baseInternaService.findAtivos();
        if(CollectionUtils.isEmpty(all)){
            return null;
        }

        List<BaseInternaResponse> list = new ArrayList<>();
        all.forEach(baseInterna -> {
            list.add(new BaseInternaResponse(baseInterna));
        });
        return list;
    }

    public List<BaseInternaResponse> getAll(BaseInternaFiltro baseInternaFiltro, int min, int max) {
        List<BaseInterna> all = baseInternaService.findByFiltro(baseInternaFiltro,min, max);
        if(CollectionUtils.isEmpty(all)){
            return null;
        }

        List<BaseInternaResponse> list = new ArrayList<>();
        all.forEach(baseInterna -> {
            list.add(new BaseInternaResponse(baseInterna));
        });
        return list;
    }

    public BaseInternaResponse detalhar(Usuario usuario, Long id) {
        BaseInterna baseInterna = baseInternaService.get(id);
        if(baseInterna == null){
            throw new BaseInternaRestException("baseInterna.nao.localizado.id", id);
        }
        return new BaseInternaResponse(baseInterna);
    }

    public BaseInternaResponse cadastrar(Usuario usuario, RequestCadastroBaseInterna requestCadastroBaseInterna) {
        BaseInterna baseInterna = new BaseInterna();
        return saveOrUpdate(usuario, baseInterna, requestCadastroBaseInterna);
    }

    public BaseInternaResponse editar(Usuario usuario, Long id, RequestCadastroBaseInterna requestCadastroBaseInterna) {
        BaseInterna baseInterna = baseInternaService.get(id);
        if(baseInterna == null){
            throw new BaseInternaRestException("baseInterna.nao.localizado.id", id);
        }
        return saveOrUpdate(usuario, baseInterna, requestCadastroBaseInterna);
    }

    private BaseInternaResponse saveOrUpdate(Usuario usuario, BaseInterna baseInterna, RequestCadastroBaseInterna requestCadastroBaseInterna) {
        validaRequestParameters(requestCadastroBaseInterna);

        baseInterna.setColunasUnicidade(requestCadastroBaseInterna.getColunasUnicidade());
        baseInterna.setAtiva(requestCadastroBaseInterna.isAtiva());
        baseInterna.setNome(requestCadastroBaseInterna.getNome());
        baseInterna.setDescricao(requestCadastroBaseInterna.getDescricao());
        baseInterna.setColunaLabel(requestCadastroBaseInterna.getColunaLabel());
        baseInternaService.saveOrUpdate(baseInterna, null, usuario);
        return new BaseInternaResponse(baseInterna);
    }

    public boolean excluir(Usuario usuario, Long id) {
        BaseInterna baseInterna = baseInternaService.get(id);
        if(baseInterna == null){
            throw new BaseInternaRestException("baseInterna.nao.localizado.id", id);
        }
        baseInternaService.excluir(baseInterna.getId(), usuario);
        return true;
    }

    public RegistroValorResponse visualizar(Usuario usuario, Long id, int min, int max, RequestVisualizarBaseInterna requestVisualizarBaseInterna){
        ObjectMapper om = new ObjectMapper();
        BaseInterna baseInterna = baseInternaService.get(id);
        List<String> colunasUnicidade = om.readValue(baseInterna.getColunasUnicidade(), List.class);
        List<String> colunas = baseRegistroValorService.getColunasRegistro(id);

        BaseRegistroFiltro filtro = new BaseRegistroFiltro();
        filtro.setBaseInterna(baseInterna);
        filtro.setQntColunas(colunas.size());

        if(requestVisualizarBaseInterna.getBusca() != null) {
            String chaveUnicidade = baseRegistroService.montarChaveUnicidade(baseInterna, requestVisualizarBaseInterna.getBusca());
            filtro.setChaveUnicidade(chaveUnicidade);
        }

        int count = baseRegistroService.countByFiltro(filtro);

        RegistroValorResponse response = new RegistroValorResponse();
        response.setColunas(colunas);
        response.setCount(count);
        response.setColunasUnicidade(colunasUnicidade);

        if(count == 0) {
            return response;
        }
        response.setDados(baseRegistroService.findByFiltro(filtro, min, max));

        return response;
    }
}