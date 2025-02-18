package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.ModeloDocumentoService;
import net.wasys.getdoc.domain.service.ModeloOcrService;
import net.wasys.getdoc.domain.service.TipoDocumentoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.rest.exception.TipoDocumentoRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoDocumento;
import net.wasys.getdoc.rest.response.vo.ListaTipoDocumentoResponse;
import net.wasys.getdoc.rest.response.vo.TipoDocumentoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class TipoDocumentoServiceRest extends SuperServiceRest{

    @Autowired
    private TipoProcessoService tipoProcessoService;
    @Autowired
    private TipoDocumentoService tipoDocumentoService;
    @Autowired
    private ModeloDocumentoService modeloDocumentoService;
    @Autowired
    private ModeloOcrService modeloOcrService;


    public ListaTipoDocumentoResponse getTiposDocumento(Usuario usuario, Long tipoProcessoId, int min, int max) {
        TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
        if(tipoProcesso == null){
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", tipoProcessoId);
        }

        ListaTipoDocumentoResponse listaTipoDocumentoResponse = new ListaTipoDocumentoResponse();
        listaTipoDocumentoResponse.setProcessoNome(tipoProcesso.getNome());
        int cout = tipoDocumentoService.countByTipoProcesso(tipoProcesso.getId());
        listaTipoDocumentoResponse.setCount(cout);
        if(cout > 0){
            List<TipoDocumento> list = tipoDocumentoService.findByTipoProcesso(tipoProcesso.getId(), min, max);
            list.forEach(tipoDocumento -> {
                listaTipoDocumentoResponse.add(new TipoDocumentoResponse(tipoDocumento));
            });
        }
        return listaTipoDocumentoResponse;
    }

    public TipoDocumentoResponse editarTipoDocumento(Usuario usuario, Long tipoDocumentoId, RequestCadastroTipoDocumento requestCadastroTipoDocumento) {
        validaRequestParameters(requestCadastroTipoDocumento);

        TipoDocumento tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);
        if(tipoDocumento == null){
            throw new TipoDocumentoRestException("tipo.documento.nao.localizado.id", tipoDocumentoId);
        }

        return saveOrUpdate(usuario, requestCadastroTipoDocumento, tipoDocumento);
    }

    public TipoDocumentoResponse adicionarTipoDocumento(Usuario usuario, Long tipoProcessoId, RequestCadastroTipoDocumento requestCadastroTipoDocumento) {

        validaRequestParameters(requestCadastroTipoDocumento);

        TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
        if(tipoProcesso == null){
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", tipoProcessoId);
        }

        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setTipoProcesso(tipoProcesso);
        return saveOrUpdate(usuario, requestCadastroTipoDocumento, tipoDocumento);
    }

    private TipoDocumentoResponse saveOrUpdate(Usuario usuario, RequestCadastroTipoDocumento requestCadastroTipoDocumento, TipoDocumento tipoDocumento) {
        ModeloOcr modeloOcr = null;
        if(requestCadastroTipoDocumento.getModeloOcrId() != null){
            modeloOcr = modeloOcrService.get(requestCadastroTipoDocumento.getModeloOcrId());
            if(modeloOcr == null){
                throw new TipoDocumentoRestException("tipo.documento.modelo.ocr.nao.localizado.id", requestCadastroTipoDocumento.getModeloOcrId());
            }
        }

        ModeloDocumento modeloDocumento = null;
        if(requestCadastroTipoDocumento.getModeloDocumentoId() != null){
            modeloDocumento = modeloDocumentoService.get(requestCadastroTipoDocumento.getModeloDocumentoId());
            if(modeloDocumento == null){
                throw new TipoDocumentoRestException("tipo.documento.modelo.documento.nao.localizado.id", requestCadastroTipoDocumento.getModeloDocumentoId());
            }
        }

//      tipoDocumento.setModeloDocumento(modeloDocumento);
        tipoDocumento.setModeloOcr(modeloOcr);
        tipoDocumento.setOrdem(requestCadastroTipoDocumento.getOrdem());
        tipoDocumento.setNome(requestCadastroTipoDocumento.getNome());
        tipoDocumento.setObrigatorio(requestCadastroTipoDocumento.isObrigatorio());
        tipoDocumento.setTaxaCompressao(requestCadastroTipoDocumento.getTaxaCompressao());
        tipoDocumento.setReconhecimentoFacial(requestCadastroTipoDocumento.isReconhecimentoFacial());
        tipoDocumento.setMaximoPaginas(requestCadastroTipoDocumento.getMaximoPaginas());
        tipoDocumentoService.saveOrUpdate(tipoDocumento, usuario, null, null, null,null, null, null);
        return new TipoDocumentoResponse(tipoDocumento);
    }

    public boolean excluirTipoDocumento(Usuario usuario, Long tipoDocumentoId) {
        TipoDocumento tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);
        if(tipoDocumento == null){
            throw new TipoDocumentoRestException("tipo.documento.nao.localizado.id", tipoDocumentoId);
        }

        tipoDocumentoService.excluir(tipoDocumentoId, usuario);
        return true;
    }

    public TipoDocumentoResponse detalhar(Usuario usuario, Long tipoDocumentoId) {
        TipoDocumento tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);
        if(tipoDocumento == null){
            throw new TipoDocumentoRestException("tipo.documento.nao.localizado.id", tipoDocumentoId);
        }
        return new TipoDocumentoResponse(tipoDocumento);
    }
}