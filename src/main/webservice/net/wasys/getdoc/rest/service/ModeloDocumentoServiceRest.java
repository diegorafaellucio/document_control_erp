package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.ModeloDocumentoService;
import net.wasys.getdoc.rest.exception.ModeloDocumentoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroModeloDocumento;
import net.wasys.getdoc.rest.response.vo.DetalhesModeloDocumentoResponse;
import net.wasys.getdoc.rest.response.vo.ModeloDocumentoResponse;
import net.wasys.getdoc.rest.response.vo.ModeloOcrResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class ModeloDocumentoServiceRest extends SuperServiceRest{

    @Autowired
    private ModeloDocumentoService modeloDocumentoService;

    public List<ModeloDocumentoResponse> getAtivos(Usuario usuario) {
        List<ModeloDocumento> ativos = modeloDocumentoService.findAtivos();
        List<ModeloDocumentoResponse> listModelosDocumento = new ArrayList<>();

        ativos.forEach(modeloDocumento -> {
            listModelosDocumento.add(new ModeloDocumentoResponse(modeloDocumento));
        });

        return listModelosDocumento;
    }

    public List<ModeloDocumentoResponse> getModelos(Usuario usuario, int min, int max) {
        List<ModeloDocumento> ativos = modeloDocumentoService.findAll(min, max);
        List<ModeloDocumentoResponse> listModelosDocumento = new ArrayList<>();

        ativos.forEach(modeloDocumento -> {
            listModelosDocumento.add(new ModeloDocumentoResponse(modeloDocumento));
        });

        return listModelosDocumento;
    }

    public DetalhesModeloDocumentoResponse detalhar(Usuario usuario, Long id) throws ModeloDocumentoRestException {
        ModeloDocumento modeloDocumento = modeloDocumentoService.get(id);
        if(modeloDocumento == null){
            throw new ModeloDocumentoRestException("modelodocumento.nao.localizado.id", id);
        }
        return new DetalhesModeloDocumentoResponse(modeloDocumento);
    }

    public DetalhesModeloDocumentoResponse cadastrar(Usuario usuario, RequestCadastroModeloDocumento requestCadastroModeloDocumento) {
        ModeloDocumento modeloDocumento = new ModeloDocumento();
        return saveOrUpdate(usuario, requestCadastroModeloDocumento, modeloDocumento);
    }

    public DetalhesModeloDocumentoResponse editar(Usuario usuario, Long id, RequestCadastroModeloDocumento requestCadastroModeloDocumento) {
        ModeloDocumento modeloDocumento = modeloDocumentoService.get(id);
        if(modeloDocumento == null){
            throw new ModeloDocumentoRestException("modelodocumento.nao.localizado.id", id);
        }
        return saveOrUpdate(usuario, requestCadastroModeloDocumento, modeloDocumento);
    }

    private DetalhesModeloDocumentoResponse saveOrUpdate(Usuario usuario, RequestCadastroModeloDocumento requestCadastroModeloDocumento, ModeloDocumento modeloDocumento) {
        validaRequestParameters(requestCadastroModeloDocumento);

        modeloDocumento.setPalavrasEsperadas(requestCadastroModeloDocumento.getPalavrasEsperadas());
        modeloDocumento.setPalavrasExcludentes(requestCadastroModeloDocumento.getPalavrasExcludentes());
        modeloDocumento.setAtivo(requestCadastroModeloDocumento.isAtivo());
        modeloDocumento.setDescricao(requestCadastroModeloDocumento.getDescricao());

        modeloDocumentoService.saveOrUpdate(modeloDocumento, usuario);
        return new DetalhesModeloDocumentoResponse(modeloDocumento);
    }

    public boolean excluir(Usuario usuario, Long id) {
        ModeloDocumento modeloDocumento = modeloDocumentoService.get(id);
        if(modeloDocumento == null){
            throw new ModeloDocumentoRestException("modelodocumento.nao.localizado.id", id);
        }
        modeloDocumentoService.excluir(modeloDocumento.getId(), usuario);
        return true;
    }
}