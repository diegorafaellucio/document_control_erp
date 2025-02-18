package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Feriado;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.FeriadoService;
import net.wasys.getdoc.rest.exception.FeriadoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroFeriado;
import net.wasys.getdoc.rest.response.vo.FeriadoResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class FeriadoServiceRest extends SuperServiceRest{

    @Autowired
    private FeriadoService feriadoService;


    public List<FeriadoResponse> getFeriados(Usuario usuario, int min, int max) {
        List<Feriado> all = feriadoService.findAll(false, min, max);
        if(CollectionUtils.isEmpty(all)){
            return null;
        }

        List<FeriadoResponse> list = new ArrayList<>();
        all.forEach(feriado -> {
            list.add(new FeriadoResponse(feriado));
        });
        return list;
    }

    public FeriadoResponse detalhar(Usuario usuario, Long id) {
        Feriado feriado = feriadoService.get(id);
        if(feriado == null){
            throw new FeriadoRestException("feriado.nao.localizado.id", id);
        }
        return new FeriadoResponse(feriado);
    }

    public FeriadoResponse cadastrar(Usuario usuario, RequestCadastroFeriado requestCadastroFeriado) {
        Feriado feriado = new Feriado();
        return saveOrUpdate(usuario, feriado, requestCadastroFeriado);
    }

    public FeriadoResponse editar(Usuario usuario, Long id,  RequestCadastroFeriado requestCadastroFeriado) {
        Feriado feriado = feriadoService.get(id);
        if(feriado == null){
            throw new FeriadoRestException("feriado.nao.localizado.id", id);
        }
        return saveOrUpdate(usuario, feriado, requestCadastroFeriado);
    }

    private FeriadoResponse saveOrUpdate(Usuario usuario, Feriado feriado, RequestCadastroFeriado requestCadastroFeriado) {
        validaRequestParameters(requestCadastroFeriado);

        feriado.setData(requestCadastroFeriado.getData());
        feriado.setDescricao(requestCadastroFeriado.getDescricao());
        feriadoService.saveOrUpdate(feriado, usuario);
        return new FeriadoResponse(feriado);
    }

    public boolean excluir(Usuario usuario, Long id) {
        Feriado feriado = feriadoService.get(id);
        if(feriado == null){
            throw new FeriadoRestException("feriado.nao.localizado.id", id);
        }
        feriadoService.excluir(feriado.getId(), usuario);
        return true;
    }
}