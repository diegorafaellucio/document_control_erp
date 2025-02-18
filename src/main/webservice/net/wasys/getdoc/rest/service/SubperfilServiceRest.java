package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.SubperfilService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.rest.exception.SituacaoRestException;
import net.wasys.getdoc.rest.exception.SubPerfilRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroSubperfil;
import net.wasys.getdoc.rest.response.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class SubperfilServiceRest extends SuperServiceRest{

    @Autowired
    private SubperfilService subperfilService;
    @Autowired
    private TipoProcessoService tipoProcessoService;
    @Autowired
    private SituacaoService situacaoService;


    public List<DetalhesSubPerfilResponse> getSubperfis(Usuario usuario, int min, int max) {
        List<Subperfil> all = subperfilService.findAll(min, max);
        if(CollectionUtils.isEmpty(all)){
            return null;
        }

        List<DetalhesSubPerfilResponse> list = new ArrayList<>();

        all.forEach(subperfil -> {
            DetalhesSubPerfilResponse detalhesSubPerfilResponse = new DetalhesSubPerfilResponse(subperfil);
            detalhesSubPerfilResponse.setListTipoProcesso(getTipoProcessos(subperfil));
            list.add(detalhesSubPerfilResponse);
        });

        return list;
    }

    public List<SubperfilTipoProcessoResponse> getTipoProcessos(Subperfil subperfil) {
        Set<SubperfilTipoProcessoResponse> tiposProcessosSet = new HashSet<>();

        Set<SubperfilSituacao> situacoes = subperfil.getSituacoes();
        for (SubperfilSituacao subperfilSituacao : situacoes) {
            SubperfilTipoProcessoResponse subperfilTipoProcessoResponse = new SubperfilTipoProcessoResponse(subperfilSituacao.getSituacao().getTipoProcesso());
            subperfilTipoProcessoResponse.setSituacoes(getSituacoes(subperfil, subperfilTipoProcessoResponse.getId()));
            tiposProcessosSet.add(subperfilTipoProcessoResponse);
        }

        List<SubperfilTipoProcessoResponse> tiposProcessos = new ArrayList<>(tiposProcessosSet);
        Collections.sort(tiposProcessos, Comparator.comparing(SubperfilTipoProcessoResponse::getNome));
        return tiposProcessos;
    }

    public List<SubperfilSituacaoResponse> getSituacoes(Subperfil subperfil, Long tipoProcessoId) {
        List<SubperfilSituacaoResponse> situacoes = new LinkedList<>();
        Set<SubperfilSituacao> spss = subperfil.getSituacoes();
        for (SubperfilSituacao subperfilSituacao : spss) {
            Situacao situacao = subperfilSituacao.getSituacao();
            TipoProcesso tp = situacao.getTipoProcesso();
            if (tipoProcessoId.equals(tp.getId())) {
                situacoes.add(new SubperfilSituacaoResponse(situacao));
            }
        }

        Collections.sort(situacoes, Comparator.comparing(SubperfilSituacaoResponse::getNome));
        return situacoes;
    }

    public DetalhesSubPerfilResponse detalhar(Usuario usuario, Long id) {
        Subperfil subperfil = subperfilService.get(id);
        if(subperfil == null){
            throw new SubPerfilRestException("subperfil.nao.localizado.id", id);
        }
        DetalhesSubPerfilResponse detalhesSubPerfilResponse = new DetalhesSubPerfilResponse(subperfil);
        detalhesSubPerfilResponse.setListTipoProcesso(getTipoProcessos(subperfil));
        return detalhesSubPerfilResponse;
    }

    public DetalhesSubPerfilResponse cadastrar(Usuario usuario, RequestCadastroSubperfil requestCadastroSubPerfil) {
        Subperfil subperfil = new Subperfil();
        return saveOrUpdate(usuario, subperfil, requestCadastroSubPerfil);
    }

    public DetalhesSubPerfilResponse editar(Usuario usuario, Long id,  RequestCadastroSubperfil requestCadastroSubPerfil) {
        Subperfil subperfil = subperfilService.get(id);
        if(subperfil == null){
            throw new SubPerfilRestException("subperfil.nao.localizado.id", id);
        }
        return saveOrUpdate(usuario, subperfil, requestCadastroSubPerfil);
    }

    private DetalhesSubPerfilResponse saveOrUpdate(Usuario usuario, Subperfil subperfil, RequestCadastroSubperfil requestCadastroSubPerfil) {
        validaRequestParameters(requestCadastroSubPerfil);

        subperfil.setDescricao(requestCadastroSubPerfil.getDescricao());

        List<Situacao> listSituacao = new ArrayList<>();

        List<Long> situacoesId = requestCadastroSubPerfil.getSituacoesId();
        situacoesId.forEach(sId -> {
            Situacao situacao = situacaoService.get(sId);
            if (situacao == null) {
                throw new SituacaoRestException("situacao.nao.localizada.com.id", sId);
            }
            listSituacao.add(situacao);
        });

        Set<Long> antigos = new HashSet<Long>();
        Set<SubperfilSituacao> situacoes2 = subperfil.getSituacoes();
        for(SubperfilSituacao ps: situacoes2) {
            antigos.add(ps.getSituacao().getId());
        }
        for(Situacao s: listSituacao) {
            SubperfilSituacao ps = new SubperfilSituacao();
            Long situacaoId = s.getId();
            if (antigos.contains(situacaoId)) {
                antigos.remove(situacaoId);
                continue;
            }
            ps.setSituacao(s);
            ps.setSubperfil(subperfil);
            situacoes2.add(ps);
        }
        for (Iterator<SubperfilSituacao> it = situacoes2.iterator(); it.hasNext();) {
            SubperfilSituacao next = it.next();
            Situacao situacao = next.getSituacao();
            Long situacaoId = situacao.getId();
            if (antigos.contains(situacaoId)) {
                it.remove();
            }
        }

        subperfilService.saveOrUpdate(subperfil, usuario);

        DetalhesSubPerfilResponse detalhesSubPerfilResponse = new DetalhesSubPerfilResponse(subperfil);
        detalhesSubPerfilResponse.setListTipoProcesso(getTipoProcessos(subperfil));
        return detalhesSubPerfilResponse;
    }

    public boolean excluir(Usuario usuario, Long id) {
        Subperfil subperfil = subperfilService.get(id);
        if(subperfil == null){
            throw new SubPerfilRestException("subperfil.nao.localizado.id", id);
        }
        subperfilService.excluir(subperfil.getId(), usuario);
        return true;
    }

    public Set<SubperfilTipoProcessoResponse> getSituacoesTipoProcesso(Usuario usuario) {
        List<TipoProcesso> tiposProcessos = tipoProcessoService.findAtivos(null);

        List<Situacao> situacoes = situacaoService.findAtivas(null);

        Set<SubperfilTipoProcessoResponse> listaSituacaoTipoProcessos = new HashSet<>();

        Map<TipoProcesso, List<Situacao>> situacoesMap = new HashMap<>();
        for (TipoProcesso tp : tiposProcessos) {
            situacoesMap.put(tp, new ArrayList<>());
        }

        for (Situacao s : situacoes) {
            TipoProcesso tipoProcesso = s.getTipoProcesso();
            List<Situacao> list = situacoesMap.get(tipoProcesso);
            if(list != null) {
                list.add(s);
            }
        }

        Set<TipoProcesso> tipoProcessos = situacoesMap.keySet();
        tipoProcessos.forEach(tipoProcesso -> {
            SubperfilTipoProcessoResponse sptpr = new SubperfilTipoProcessoResponse(tipoProcesso);
            List<Situacao> situacaos = situacoesMap.get(tipoProcesso);

            List<SubperfilSituacaoResponse> subperfilSituacaoResponseList = new ArrayList<>();
            for (Situacao situacao : situacaos) {
                subperfilSituacaoResponseList.add(new SubperfilSituacaoResponse(situacao));
            }
            sptpr.setSituacoes(subperfilSituacaoResponseList);
            listaSituacaoTipoProcessos.add(sptpr);
        });

        return listaSituacaoTipoProcessos;
    }
}