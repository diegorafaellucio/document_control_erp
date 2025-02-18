package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.TipoCampoRestException;
import net.wasys.getdoc.rest.exception.TipoDocumentoRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoCampo;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoCampoGrupo;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoDocumento;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class TipoCampoServiceRest extends SuperServiceRest{

    @Autowired
    private TipoCampoGrupoService tipoCampoGrupoService;
    @Autowired
    private BaseInternaService baseInternaService;
    @Autowired
    private SituacaoService situacaoService;
    @Autowired
    private TipoProcessoService tipoProcessoService;
    @Autowired
    private TipoCampoService tipoCampoService;

    public ListaTipoCampoResponse findByTipoProcesso(Usuario usuario, Long tipoProcessoId) throws TipoProcessoRestException {
        TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
        if(tipoProcesso == null){
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", tipoProcesso);
        }

        ListaTipoCampoResponse listaTipoCampoResponse = new ListaTipoCampoResponse();
        listaTipoCampoResponse.setProcessoNome(tipoProcesso.getNome());

        List<TipoCampoGrupo> byTipoProcesso = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
        byTipoProcesso.forEach(tcg -> {
            GrupoCamposResponse gcr = new GrupoCamposResponse(tcg);

            List<CampoResponse> listCampos = new ArrayList<>();
            tcg.getCampos().forEach(tipoCampo -> {
                listCampos.add(new CampoResponse(tipoCampo));
            });
            gcr.setCampos(listCampos);
            listaTipoCampoResponse.add(gcr);
        });
        return listaTipoCampoResponse;
    }

    public CampoResponse adicionarTipoCampo(Usuario usuario, Long tipoProcessoId, RequestCadastroTipoCampo requestCadastroTipoCampo) throws DadosObrigatorioRequestException, TipoProcessoRestException, TipoCampoRestException {
        validaRequestParameters(requestCadastroTipoCampo);

        TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
        if(tipoProcesso == null){
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", tipoProcessoId);
        }

        TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.get(requestCadastroTipoCampo.getTipoCampoGrupoId());
        if(tipoCampoGrupo == null){
            throw new TipoCampoRestException("tipo.campo.grupo.nao.localizado.id", requestCadastroTipoCampo.getTipoCampoGrupoId());
        }

        BaseInterna baseInterna = null;
        if(requestCadastroTipoCampo.getBaseInternaId() != null) {
            baseInterna = baseInternaService.get(requestCadastroTipoCampo.getBaseInternaId());
        }

        TipoCampo tipoCampo = new TipoCampo();
        tipoCampo.setGrupo(tipoCampoGrupo);
        tipoCampo.setBaseInterna(baseInterna);
        return saveOrUpdateTipoCampo(usuario, requestCadastroTipoCampo,tipoCampo);
    }

    private CampoResponse saveOrUpdateTipoCampo(Usuario usuario, RequestCadastroTipoCampo requestCadastroTipoCampo, TipoCampo tipoCampo) {
        tipoCampo.setGrupo(tipoCampo.getGrupo());
        if(tipoCampo.getBaseInterna() != null) {
            tipoCampo.setBaseInterna(tipoCampo.getBaseInterna());
        }
        tipoCampo.setTipo(requestCadastroTipoCampo.getTipo());
        tipoCampo.setDica(requestCadastroTipoCampo.getDica());
        tipoCampo.setEditavel(requestCadastroTipoCampo.isEditavel());
        tipoCampo.setNome(requestCadastroTipoCampo.getNome());
        tipoCampo.setObrigatorio(requestCadastroTipoCampo.isObrigatorio());
        tipoCampo.setOrdem(requestCadastroTipoCampo.getOrdem());
        tipoCampo.setOpcoes(requestCadastroTipoCampo.getOpcoes());
        tipoCampo.setTamanhoMinimo(requestCadastroTipoCampo.getTamanhoMinimo());
        tipoCampo.setTamanhoMaximo(requestCadastroTipoCampo.getTamanhoMaximo());
        tipoCampo.setPais(requestCadastroTipoCampo.getPais());
        tipoCampo.setCriterioExibicao(requestCadastroTipoCampo.getCriterioExibicao());
        tipoCampo.setCriterioFiltro(requestCadastroTipoCampo.getCriterioFiltro());

        try {
            tipoCampoService.saveOrUpdate(tipoCampo.getGrupo().getTipoProcesso(), tipoCampo, usuario);
            return new CampoResponse(tipoCampo);
        } catch (MessageKeyException e) {
            throw new TipoCampoRestException(e.getKey());
        }
    }

    public CampoResponse editarCampo(Usuario usuario, Long tipoCampoId, RequestCadastroTipoCampo requestCadastroTipoCampo) throws DadosObrigatorioRequestException{
        validaRequestParameters(requestCadastroTipoCampo);

        TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.get(requestCadastroTipoCampo.getTipoCampoGrupoId());
        if(tipoCampoGrupo == null){
            throw new TipoCampoRestException("tipo.campo.grupo.nao.localizado.id", requestCadastroTipoCampo.getTipoCampoGrupoId());
        }

        TipoCampo tipoCampo = tipoCampoService.get(tipoCampoId);
        if(tipoCampo == null){
            throw new TipoCampoRestException("tipo.campo.nao.localizado.id", tipoCampoId);
        }
        BaseInterna baseInterna = null;
        if(requestCadastroTipoCampo.getBaseInternaId() != null) {
            baseInterna = baseInternaService.get(requestCadastroTipoCampo.getBaseInternaId());
        }

        tipoCampo.setGrupo(tipoCampoGrupo);
        tipoCampo.setBaseInterna(baseInterna);
        return saveOrUpdateTipoCampo(usuario, requestCadastroTipoCampo, tipoCampo);
    }

    public TipoCampoGrupoResponse adicionarGrupoCampo(Usuario usuario, Long tipoProcessoId, RequestCadastroTipoCampoGrupo requestCadastroGrupoCampo) throws TipoProcessoRestException {

        validaRequestParameters(requestCadastroGrupoCampo);

        TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
        if(tipoProcesso == null){
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", tipoProcessoId);
        }


        Situacao situacao = situacaoService.get(requestCadastroGrupoCampo.getSituacaoId());

        TipoCampoGrupo tipoCampoGrupo = new TipoCampoGrupo();
        tipoCampoGrupo.setTipoProcesso(tipoProcesso);
        // FIXME
        /*if(situacao != null){
            tipoCampoGrupo.setSituacao(situacao);
        }*/
        tipoCampoGrupo.setCriacaoProcesso(requestCadastroGrupoCampo.isAdicionarCriacaoProcesso());
        tipoCampoGrupo.setAbertoPadrao(requestCadastroGrupoCampo.isSempreVisivel());
        tipoCampoGrupo.setNome(requestCadastroGrupoCampo.getNome());
        tipoCampoGrupoService.saveOrUpdate(tipoCampoGrupo, usuario);

        return new TipoCampoGrupoResponse(tipoCampoGrupo);
    }

    /**
     *
     * @param usuario
     * @param tipoGrupoCampoId
     * @param requestCadastroGrupoCampo
     * @return
     */
    public TipoCampoGrupoResponse editarGrupoCampo(Usuario usuario, Long tipoGrupoCampoId, RequestCadastroTipoCampoGrupo requestCadastroGrupoCampo) {
        validaRequestParameters(requestCadastroGrupoCampo);

        TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.get(tipoGrupoCampoId);
        if(tipoCampoGrupo == null){
            throw new TipoProcessoRestException("tipo.campo.grupo.nao.localizado.id", tipoGrupoCampoId);
        }

        tipoCampoGrupo.setCriacaoProcesso(requestCadastroGrupoCampo.isAdicionarCriacaoProcesso());
        tipoCampoGrupo.setAbertoPadrao(requestCadastroGrupoCampo.isSempreVisivel());
        tipoCampoGrupo.setNome(requestCadastroGrupoCampo.getNome());

        Situacao situacao = situacaoService.get(requestCadastroGrupoCampo.getSituacaoId());
        // FIXME
        /*
        if(situacao != null){
            tipoCampoGrupo.setSituacao(situacao);
        }*/

        tipoCampoGrupoService.saveOrUpdate(tipoCampoGrupo, usuario);

        return new TipoCampoGrupoResponse(tipoCampoGrupo);

    }

    public boolean excluirGrupoCampo(Usuario usuario, Long tipoGrupoCampoId) {
        TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.get(tipoGrupoCampoId);
        if(tipoCampoGrupo == null){
            throw new TipoProcessoRestException("tipo.campo.grupo.nao.localizado.id", tipoGrupoCampoId);
        }

        tipoCampoGrupoService.excluir(tipoGrupoCampoId, usuario);
        return true;
    }

    public boolean excluirCampo(Usuario usuario, Long tipoCampoId) {
        TipoCampo tipoCampo = tipoCampoService.get(tipoCampoId);
        if(tipoCampo == null){
            throw new TipoCampoRestException("tipo.campo.nao.localizado.id", tipoCampoId);
        }

        tipoCampoService.excluir(tipoCampoId, usuario);
        return true;
    }

    public CampoResponse detalharTipoCampo(Usuario usuario, Long tipoCampoId) {
        TipoCampo tipoCampo = tipoCampoService.get(tipoCampoId);
        if(tipoCampo == null){
            throw new TipoCampoRestException("tipo.campo.nao.localizado.id", tipoCampoId);
        }

        return new CampoResponse(tipoCampo);
    }

    public TipoCampoGrupoResponse detalharTipoCampoGrupo(Usuario usuario, Long tipoCampoGrupoId) {
        TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.get(tipoCampoGrupoId);
        if(tipoCampoGrupo == null){
            throw new TipoProcessoRestException("tipo.campo.grupo.nao.localizado.id", tipoCampoGrupoId);
        }

        return new TipoCampoGrupoResponse(tipoCampoGrupo);

    }
}