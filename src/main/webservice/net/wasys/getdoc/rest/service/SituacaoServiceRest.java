package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.getdoc.rest.exception.SituacaoRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroSituacao;
import net.wasys.getdoc.rest.response.vo.DetalhesSituacaoResponse;
import net.wasys.getdoc.rest.response.vo.SituacaoListaResponse;
import net.wasys.getdoc.rest.response.vo.SituacaoResponse;
import net.wasys.util.other.HorasUteisCalculator;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class SituacaoServiceRest extends SuperServiceRest {

    @Autowired private SituacaoService situacaoService;
    @Autowired private TipoProcessoService tipoProcessoService;
    @Autowired private ParametroService parametroService;

    public List<SituacaoResponse> findAtivas(Usuario usuario) {
        List<Situacao> ativas = situacaoService.findAtivas(null);
        List<SituacaoResponse> list = parser(ativas);
        return list;
    }

    private List<SituacaoResponse> parser(List<Situacao> ativas) {
        Collections.sort(ativas, new SuperBeanComparator<>("tipoProcesso.nome, nome"));

        List<SituacaoResponse> list = null;

        if (ativas != null && ativas.size() > 0) {
            list = new ArrayList<>();
            for (Situacao s : ativas) {
                list.add(new SituacaoResponse(s));
            }
        }
        return list;
    }

    public List<SituacaoListaResponse> getSituacoesByTipoProcesso(Usuario usuario, Long tipoProcessoId, int min, int max) throws TipoProcessoRestException {

        TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
        if (tipoProcesso == null) {
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", tipoProcessoId);
        }

        SituacaoFiltro filtro = new SituacaoFiltro();
        filtro.setTipoProcessoId(tipoProcessoId);

        List<Situacao> byFiltro = situacaoService.findByFiltro(filtro, min, max);

        List<SituacaoListaResponse> list = null;

        if (byFiltro != null && byFiltro.size() > 0) {

            BigDecimal horasExpediente = buscarHorasExpediente();

            list = new ArrayList<>();
            for (Situacao s : byFiltro) {
                SituacaoListaResponse situacaoListaResponse = new SituacaoListaResponse(s);
                if(s.getHorasPrazo() != null && s.getTipoPrazo() != null) {
                    situacaoListaResponse.setPrazo(formatarPrazo(horasExpediente, s.getHorasPrazo().doubleValue(), s.getTipoPrazo()));
                }else{
                    situacaoListaResponse.setPrazo("");
                }
                list.add(situacaoListaResponse);
            }
        }
        return list;
    }

    public DetalhesSituacaoResponse detalhar(Usuario usuario, Long id) throws SituacaoRestException {
        Situacao situacao = situacaoService.get(id);
        if (situacao == null) {
            throw new SituacaoRestException("situacao.nao.localizada.com.id", id);
        }

        SituacaoFiltro filtro = new SituacaoFiltro();
        filtro.setTipoProcessoId(situacao.getTipoProcesso().getId());
        List<Situacao> proximas = getProximasSituacoes(situacao, filtro);

        List<Situacao> proximasSelecionadas = new ArrayList<Situacao>();
        for (ProximaSituacao ps : situacao.getProximas()) {
            proximasSelecionadas.add(ps.getProxima());
        }

        BigDecimal horasExpediente = buscarHorasExpediente();

        DetalhesSituacaoResponse detalhesSituacaoResponse = new DetalhesSituacaoResponse(situacao);
        detalhesSituacaoResponse.setSituacoesDisponiveis(proximas);
        detalhesSituacaoResponse.setSituacoesSelecionadas(proximasSelecionadas);
        detalhesSituacaoResponse.setPrazo(getPrazo(situacao, horasExpediente));
        BigDecimal horasPrazo = situacao.getHorasPrazo();
        if(horasPrazo != null) {
            detalhesSituacaoResponse.setPrazoDescricao(formatarPrazo(horasExpediente, horasPrazo.doubleValue(), situacao.getTipoPrazo()));
        }
        return detalhesSituacaoResponse;
    }

    private List<Situacao> getProximasSituacoes(Situacao situacao, SituacaoFiltro filtro) {
        List<Situacao> proximas = null;

        if (situacao == null) {
            Long tipoProcessoId = filtro.getTipoProcessoId();
            if (tipoProcessoId != null) {
                TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
                situacao.setTipoProcesso(tipoProcesso);
            }
        } else {

            SituacaoFiltro filtro2 = new SituacaoFiltro();
            TipoProcesso tipoProcesso = situacao.getTipoProcesso();
            if (tipoProcesso != null) {
                Long tipoProcessoId = tipoProcesso.getId();
                filtro2.setTipoProcessoId(tipoProcessoId);
                proximas = situacaoService.findByFiltro(filtro2, null, null);
                proximas.remove(situacao);
            }
        }
        return proximas;
    }

    public List<SituacaoResponse> situacoesByTipoProcesso(Usuario usuario, Long tipoProcessoId) {
        TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
        if (tipoProcesso == null) {
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", tipoProcessoId);
        }

        List<Situacao> proximas = situacaoService.findByTipoProcesso(tipoProcesso.getId());
        if(CollectionUtils.isEmpty(proximas)){
            return null;
        }

        List<SituacaoResponse> list = new ArrayList<>();
        proximas.forEach(situacao -> {
            list.add(new SituacaoResponse(situacao));
        });
        return list;
    }

    private Integer getPrazo(Situacao situacao, BigDecimal horasExpediente) {

        Integer prazo = null;
        BigDecimal horasPrazo = situacao.getHorasPrazo();
        if (situacao != null && horasPrazo != null) {

            TipoPrazo tipoPrazo = situacao.getTipoPrazo();
            if (tipoPrazo != null && TipoPrazo.DIAS.equals(tipoPrazo)) {
                BigDecimal horasPrazoBD = horasPrazo;
                BigDecimal prazoEmDias = horasPrazoBD.divide(horasExpediente, RoundingMode.CEILING);

                prazo = prazoEmDias.intValue();
            } else if (tipoPrazo != null && TipoPrazo.MINUTOS.equals(tipoPrazo)) {
                prazo = horasPrazo.multiply(new BigDecimal(60)).intValue();
            } else {
                prazo = horasPrazo.intValue();
            }
        }
        return prazo;
    }

    private String formatarPrazo(BigDecimal horasExpediente, Double horasPrazo, TipoPrazo tipoPrazo) {

        if (horasPrazo == null) {
            return "";
        }

        if (TipoPrazo.DIAS.equals(tipoPrazo)) {
            BigDecimal prazoBD = new BigDecimal(horasPrazo);
            BigDecimal prazoEmDias = prazoBD.divide(horasExpediente, RoundingMode.CEILING);

            return prazoEmDias + "d";
        } else if (TipoPrazo.HORAS.equals(tipoPrazo)) {
            return horasPrazo.intValue() + "h";
        } else if (TipoPrazo.MINUTOS.equals(tipoPrazo)) {
            return Math.round(horasPrazo * 60) + "min";
        }

        return String.valueOf(horasPrazo);
    }

    private BigDecimal converterPrazoParaHorasDouble(TipoPrazo tipoPrazo, int prazo, BigDecimal horasExpediente) {

        BigDecimal prazoBD = new BigDecimal(prazo);

        if (TipoPrazo.DIAS.equals(tipoPrazo)) {
            BigDecimal prazoEmHoras = horasExpediente.multiply(prazoBD);
            return prazoEmHoras;
        } else if (TipoPrazo.HORAS.equals(tipoPrazo)) {
            return prazoBD;
        } else if (TipoPrazo.MINUTOS.equals(tipoPrazo)) {
            return prazoBD.divide(new BigDecimal(60), 3, RoundingMode.CEILING);
        }
        return prazoBD;
    }

    private BigDecimal buscarHorasExpediente() {
        String[] expedienteArray = parametroService.getExpediente();
        HorasUteisCalculator.Expediente expediente = new HorasUteisCalculator.Expediente(expedienteArray);
        return expediente.getHoras();
    }

    public SituacaoListaResponse cadastrar(Usuario usuario, RequestCadastroSituacao requestCadastroSituacao) {
        Situacao situacao = new Situacao();
        return saveOrUpdate(usuario, situacao, requestCadastroSituacao);
    }

    public SituacaoListaResponse editar(Usuario usuario, Long id, RequestCadastroSituacao requestCadastroSituacao) {
        Situacao situacao = situacaoService.get(id);
        if (situacao == null) {
            throw new SituacaoRestException("situacao.nao.localizada.com.id", id);
        }
        return saveOrUpdate(usuario, situacao, requestCadastroSituacao);
    }

    private SituacaoListaResponse saveOrUpdate(Usuario usuario, Situacao situacao, RequestCadastroSituacao requestCadastroSituacao) {
        validaRequestParameters(requestCadastroSituacao);

        TipoProcesso tipoProcesso = tipoProcessoService.get(requestCadastroSituacao.getTipoProcessoId());
        if (tipoProcesso == null) {
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", requestCadastroSituacao.getTipoProcessoId());
        }

        BigDecimal horasExpediente = buscarHorasExpediente();
        TipoPrazo tipoPrazo = requestCadastroSituacao.getTipoPrazo();
        if(tipoPrazo != null) {
            situacao.setHorasPrazo(converterPrazoParaHorasDouble(tipoPrazo, requestCadastroSituacao.getPrazo().intValue(), horasExpediente));
        }
        situacao.setTipoProcesso(tipoProcesso);
        situacao.setAtiva(requestCadastroSituacao.isAtiva());
        situacao.setDistribuicaoAutomatica(requestCadastroSituacao.isDistribuicaoAutomatica());
        situacao.setNotificarAutor(requestCadastroSituacao.isNotificarAutorClient());
        situacao.setNome(requestCadastroSituacao.getNome());
        situacao.setStatus(requestCadastroSituacao.getStatusInicial());
        situacao.setTipoPrazo(tipoPrazo);

        if (CollectionUtils.isNotEmpty(requestCadastroSituacao.getProximasSituacoes())) {
            List<Situacao> proximasSelecionadas = new ArrayList<>();
            requestCadastroSituacao.getProximasSituacoes().forEach(proximaSituacaoId -> {
                Situacao proxima = situacaoService.get(proximaSituacaoId);
                if (proxima == null) {
                    throw new SituacaoRestException("situacao.nao.localizada.com.id", proximaSituacaoId);
                }
                proximasSelecionadas.add(proxima);

            });
            arrumaProximos(situacao, proximasSelecionadas);
        }

        situacaoService.saveOrUpdate(situacao, usuario);

        SituacaoListaResponse situacaoListaResponse = new SituacaoListaResponse(situacao);
        situacaoListaResponse.setPrazo(formatarPrazo(horasExpediente, situacao.getHorasPrazo().doubleValue(), situacao.getTipoPrazo()));
        return situacaoListaResponse;
    }

    private void arrumaProximos(Situacao situacao, List<Situacao> proximasSelecionadas) {
        if(situacao != null && CollectionUtils.isNotEmpty(proximasSelecionadas)) {
            Set<Long> antigos = new HashSet<Long>();
            for (ProximaSituacao ps : situacao.getProximas()) {
                antigos.add(ps.getProxima().getId());
            }
            for (Situacao s : proximasSelecionadas) {
                ProximaSituacao ps = new ProximaSituacao();
                if (antigos.contains(s.getId())) {
                    antigos.remove(s.getId());
                    continue;
                }
                ps.setProxima(s);
                ps.setAtual(situacao);
                situacao.getProximas().add(ps);
            }
            for (Iterator<?> iterator = situacao.getProximas().iterator(); iterator.hasNext(); ) {
                ProximaSituacao next = (ProximaSituacao) iterator.next();
                if (antigos.contains(next.getProxima().getId())) {
                    iterator.remove();
                }
            }
        }
    }

    public boolean excluir(Usuario usuario, Long id) {
        Situacao situacao = situacaoService.get(id);
        if (situacao == null) {
            throw new SituacaoRestException("situacao.nao.localizada.com.id", id);
        }

        situacaoService.excluir(situacao.getId(), usuario);
        return true;
    }

    public List<SituacaoResponse> getProximasSituacoes(Long situacaoAtualId){
        Situacao situacao = situacaoService.get(situacaoAtualId);
        if (situacao == null) {
            throw new SituacaoRestException("situacao.nao.localizada.com.id", situacaoAtualId);
        }

        List<Situacao> byFiltro = situacaoService.getProximasByAtual(situacaoAtualId);

        List<SituacaoResponse> list = new ArrayList<>();
        byFiltro.forEach(sit -> list.add(new SituacaoResponse(sit)));
        return list;
    }

}