package net.wasys.getdoc.rest.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.*;
import net.wasys.getdoc.rest.exception.*;
import net.wasys.getdoc.rest.request.vo.*;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.ReflectionBeanComparator;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class ProcessoServiceRest extends SuperServiceRest {

    @Autowired private UploadMultipartServiceRest uploadMultipartServiceRest;
    @Autowired private ProcessoService processoService;
    @Autowired private SituacaoService situacaoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private ProcessoLogService processoLogService;
    @Autowired private RelatorioGeralService relatorioGeralService;
    @Autowired private RegraService regraService;
    @Autowired private DocumentoLogService documentoLogService;
    @Autowired private DocumentoService documentoService;
    @Autowired private TipoCampoService tipoCampoService;
    @Autowired private CampoService campoService;
    @Autowired private TipoProcessoService tipoProcessoService;
    @Autowired private AutorizacaoEdicaoService autorizacaoEdicaoService;
    @Autowired private TipoEvidenciaService tipoEvidenciaService;
    @Autowired private EmailEnviadoService emailEnviadoService;
    @Autowired private BaseInternaService baseInternaService;
    @Autowired private BaseRegistroService baseRegistroService;
    @Autowired private ProcessoRegraService processoRegraService;
    @Autowired private BaseInternaServiceRest baseInternaServiceRest;
    @Autowired private EmailRecebidoService emailRecebidoService;
    @Autowired private ExceptionService exceptionService;
    @Autowired private DownloadFileService downloadFileService;
    @Autowired private BloqueioProcessoService bloqueioProcessoService;

    /**
     * Centraliza a pesquisa inicial da Fila de Trabalho.
     *
     * @param usuarioLogado
     * @param inicio
     * @param max
     * @return
     */
    public ProcessoFiltro getFiltroInicialFila(Usuario usuarioLogado, int inicio, int max) {

        ProcessoFiltro filtro = filtro(usuarioLogado, false);
        filtro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.PENDENCIAS_ANALISTA);

        if(usuarioLogado.isAnalistaRole()) {
            Subperfil subperfilAtivo = usuarioLogado.getSubperfilAtivo();
            if(subperfilAtivo != null) {
                Set<SubperfilSituacao> situacoes = subperfilAtivo.getSituacoes();
                List<Situacao> situacoesList = new ArrayList<>();
                for (SubperfilSituacao ss : situacoes) {
                    Situacao situacao = ss.getSituacao();
                    situacoesList.add(situacao);
                }
                filtro.setSituacao(situacoesList);
            }
        }

        return filtro;
    }

    /**
     * Filtro default para consultar a fila de trabalho.
     *
     * @param usuario
     * @return
     */
    private ProcessoFiltro filtro(Usuario usuario, Boolean pesquisa) {

        ProcessoFiltro filtro = new ProcessoFiltro();

        if (usuario.isAdminRole() || usuario.isGestorRole()) {

            List<StatusProcesso> statusList = StatusProcesso.getStatusEmAndamento();
            filtro.setStatusList(statusList);

            filtro.setUsuarioRascunhos(usuario);
        } else if (usuario.isAnalistaRole() && !pesquisa) {

            Subperfil subperfil = usuario.getSubperfilAtivo();
            filtro.confBySubperfil(usuario, subperfil);

            filtro.setUsuarioRascunhos(usuario);
        } else if (usuario.isAreaRole()) {

            Area area = usuario.getArea();
            if (area == null) {
                return filtro;
            }

            filtro.setAreaPendencia(area);

            filtro.setUsuarioRascunhos(usuario);
        } else if (usuario.isRequisitanteRole()) {

            List<StatusProcesso> statusList = StatusProcesso.getPendenciaRequisitante();
            statusList = new ArrayList<>(statusList);

            filtro.setStatusList(statusList);

			filtro.setAutor(usuario);

		}

        return filtro;
    }

    /**
     * Retorna os StatusProcesso de acordo com o usuário.
     *
     * @param usuario
     * @return
     */
    public List<StatusProcesso> getStatusProcesso(Usuario usuario) {

        List<StatusProcesso> statusList = null;

        if (usuario.isAdminRole() || usuario.isGestorRole()) {
            statusList = StatusProcesso.getStatusEmAndamento();
        } else if (usuario.isAnalistaRole()) {

            Subperfil subperfil = usuario.getSubperfilAtivo();
            String statusFila = null;
            if (subperfil != null) {
                FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
                if (filaConfiguracao != null) {
                    statusFila = filaConfiguracao.getStatus();
                }
            }
            statusList = DummyUtils.stringToList(statusFila, StatusProcesso.class);
        } else if (usuario.isRequisitanteRole()) {

            statusList = StatusProcesso.getPendenciaRequisitante();
            statusList = new ArrayList<>(statusList);
        }
        return statusList;
    }

    /**
     * Retorna a fila de trabalho considerando um filtro default para isso.
     *
     * @param usuario
     * @param inicio
     * @param max
     * @return
     */
    public FilaTrabalhoResponse getFilaTrabalho(Usuario usuario, int inicio, int max) {

        ProcessoFiltro processoFiltro = getFiltroInicialFila(usuario, inicio, max);

        List<ProcessoVO> vOsByFiltro = processoService.findVOsByPendencia(processoFiltro, inicio, max);

        FilaTrabalhoResponse filaTrabalhoResponse = getFilaTrabalhoResponse(usuario, processoFiltro, vOsByFiltro);

        return filaTrabalhoResponse;
    }

    /** Consolida o retorno da fila de processo. */
    private FilaTrabalhoResponse getFilaTrabalhoResponse(Usuario usuario, ProcessoFiltro processoFiltro, List<ProcessoVO> vOsByFiltro) {
        FilaTrabalhoResponse filaTrabalhoResponse = new FilaTrabalhoResponse();

        /** Monta o contador de StatusProcesso. */
        Map<StatusProcesso, Long> mapCountStatus = processoService.getStatusAnalise(usuario, processoFiltro);
        if (mapCountStatus != null) {
            Set<StatusProcesso> statusProcessos = mapCountStatus.keySet();
            Long quantidadeProcessos = 0L;
            for (StatusProcesso s : statusProcessos) {
                ContadorStatusProcesso count = new ContadorStatusProcesso();
                count.setStatusProcesso(StatusProcessoResponse.from(s));
                Long quantidadeDoStatus = mapCountStatus.get(s);
                count.setQuantidade(quantidadeDoStatus);
                quantidadeProcessos += quantidadeDoStatus;
                filaTrabalhoResponse.addContadorStatusProcesso(count);
            }
            filaTrabalhoResponse.setQuantidadeDeProcessos(quantidadeProcessos);
        }

        /**
         * Monta a lista de processos da fila.
         */
        List<FilaTrabalhoVoResponse> listFilaTravalhoResponse = null;
        if (vOsByFiltro != null && CollectionUtils.isNotEmpty(vOsByFiltro)) {
            listFilaTravalhoResponse = new ArrayList<>();
            for (ProcessoVO pVo : vOsByFiltro) {
                Processo processo = pVo.getProcesso();
                Long id = processo.getId();
                BloqueioProcesso bloqueioProcesso = bloqueioProcessoService.getByProcesso(id);
                String nome = "";
                if(bloqueioProcesso != null){
                    Long usuarioId = bloqueioProcesso.getUsuarioId();
                    Usuario usuarioBloqueio = usuarioService.get(usuarioId);
                    if(usuarioBloqueio != null) {
                        nome = usuarioBloqueio.getNome();
                    }
                }
                pVo.setNomeAnalistaBloqueio(nome);

                listFilaTravalhoResponse.add(new FilaTrabalhoVoResponse(pVo));
            }
        }
        filaTrabalhoResponse.setFilaTrabalhoResponse(listFilaTravalhoResponse);

        filaTrabalhoResponse.setPermiteFiltrar(mostrarFiltro(usuario));
        filaTrabalhoResponse.setPermiteTrocarAnalista(podeTrocarAnalistas(usuario));
        filaTrabalhoResponse.setPermiteVerificarProxima(podeVerificarProxima(usuario));
        filaTrabalhoResponse.setPermiteFiltrarAnalista(mostrarFiltroAnalista(usuario));
        filaTrabalhoResponse.setPermiteCriar(verificaPermissaoCriacaoProcesso(usuario));
        filaTrabalhoResponse.setPermiteConclusaoEmMassa(permiteConclusaoEmMassa(usuario));

        return filaTrabalhoResponse;
    }

    private List<Long> getIds(List<ProcessoVO> list) {
        List<Long> ids = new ArrayList<>();
        if (list != null && list.size() > 0) {
            list.forEach(processoVO -> {
                ids.add(processoVO.getProcesso().getId());
            });
        }
        return ids;
    }

    private boolean mostrarFiltro(Usuario usuarioLogado) {
        //return usuarioLogado.isAdminRole() || usuarioLogado.isGestorRole() || usuarioLogado.isAnalistaRole();
        return true;
    }

    private boolean mostrarFiltroAnalista(Usuario usuarioLogado) {
        return !usuarioLogado.isAnalistaRole() && !usuarioLogado.isComercialRole();
    }

    private boolean podeTrocarAnalistas(Usuario usuarioLogado) {
        boolean adminRole = usuarioLogado.isAdminRole();
        boolean gestorRole = usuarioLogado.isGestorRole();
        return adminRole || gestorRole;
    }

    private boolean podeVerificarProxima(Usuario usuarioLogado) {

        if (!usuarioLogado.isAnalistaRole()) {
            return false;
        }

        Boolean valor = false;
        Subperfil subperfil = usuarioLogado.getSubperfilAtivo();
        if (subperfil != null) {
            FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
            if (filaConfiguracao != null) {
                valor = filaConfiguracao.isVerificarProximaRequisicao();
            }
        }
        return valor;
    }

    /**
     * Converte o StatusProcesso para o VO contendo informações adicionais importantes, mantendo regra de negócio aqui no back.
     *
     * @param usuario
     * @return
     */
    public List<StatusProcessoResponse> getStatusProcessoResponse(Usuario usuario) {
        List<StatusProcesso> statusList = getStatusProcesso(usuario);

        List<StatusProcessoResponse> statusProcessoResponses = StatusProcessoResponse.get();
        if(statusList != null) {
            for (StatusProcesso statusProcesso : statusList) {

                statusProcessoResponses.forEach(selecionado -> {
                    if (statusProcesso == selecionado.getId()) {
                        selecionado.setSelecionado(true);
                        return;
                    }
                });
            }
        }
        return statusProcessoResponses;
    }

    /**
     * Consulta a fila de trabalho através de um filtro informado pelo usuário.
     *
     * @param usuario
     * @param inicio
     * @param max
     * @param requestFiltroFila
     * @return
     */
    public FilaTrabalhoResponse buscarFilaTrabalho(Usuario usuario, int inicio, int max, RequestFiltroFila requestFiltroFila) {

		ProcessoFiltro processoFiltro = filtro(usuario, false);

        if (requestFiltroFila.getAnalistaId() != null) {
            processoFiltro.setAnalista(usuarioService.get(requestFiltroFila.getAnalistaId()));
        }

        processoFiltro.setOrdenar("id", SortOrder.ASCENDING);
        processoFiltro.setStatusList(requestFiltroFila.getStatusList());
        processoFiltro.setTiposProcesso(requestFiltroFila.getTiposProcessoList());
        processoFiltro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.PENDENCIAS_ANALISTA);
        processoFiltro.setUsuarioRascunhos(usuario);
        processoFiltro.setTexto(requestFiltroFila.getTexto());
        processoFiltro.setPossuiEmailNaoLido(requestFiltroFila.isEmailNaoLido());
        processoFiltro.setStatusPrazo(requestFiltroFila.getStatusPrazoList());
        //processoFiltro.setOrcamentoAprovacao(requestFiltroFila.getOrcamentoAprovacao());
        processoFiltro.setConsiderarData(requestFiltroFila.getConsiderarData());
        processoFiltro.setDataInicio(requestFiltroFila.getDataInicio());
        processoFiltro.setDataFim(requestFiltroFila.getDataFim());

        List<ProcessoVO> vOsByFiltro = processoService.findVOsByFiltro(processoFiltro, inicio, max);
        FilaTrabalhoResponse filaTrabalhoResponse = getFilaTrabalhoResponse(usuario, processoFiltro, vOsByFiltro);
        return filaTrabalhoResponse;
    }

    /**
     * @param usuario
     * @param requestTrocarAnalista
     * @return
     */
    public String trocarAnalista(Usuario usuario, RequestTrocarAnalista requestTrocarAnalista) throws DadosObrigatorioRequestException {
        validaRequestParameters(requestTrocarAnalista);
        Usuario novoAnalista = usuarioService.get(requestTrocarAnalista.getAnalistaId());
        processoService.trocarAnalistas(requestTrocarAnalista.getProcessoId(), novoAnalista, usuario);
        String value = messageService.getValue("alteracaoAnalistas.sucesso", requestTrocarAnalista.getProcessoId().size(), novoAnalista.getNome());
        return value;
    }

    /**
     * @param usuario
     * @param requestConcluirProcessos
     * @return
     */
    public String concluirProcessosMassa(Usuario usuario, RequestConcluirProcessos requestConcluirProcessos) throws DadosObrigatorioRequestException {

        validaRequestParameters(requestConcluirProcessos);
        Situacao novaSituacao = situacaoService.get(requestConcluirProcessos.getSituacaoId());

        List<Processo> listProcessos = new ArrayList<>();
        for (Long pId : requestConcluirProcessos.getProcessosIds()) {
            Processo processo = processoService.get(pId);
            if (processo != null) {
                listProcessos.add(processoService.get(pId));
            }
        }

        String observacao = requestConcluirProcessos.getObservacao();
        try {
            processoService.concluirEmMassa(listProcessos, usuario, novaSituacao, observacao);
        } catch (Exception e) {
            e.printStackTrace();
            String exceptionMessage = DummyUtils.getExceptionMessage(e);
            throw new DadosObrigatorioRequestException(exceptionMessage);
        }

        String value = messageService.getValue("conclusaoProcessos.sucesso", listProcessos.size());
        return value;
    }

    public DetalhesProcesso detalhar(Usuario usuarioLogado, String imagePath, Long processoId) throws ProcessoRestException {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        DetalhesProcesso detalhesProcesso = new DetalhesProcesso();
        detalhesProcesso.parserProcesso(processo);
        detalhesProcesso.setPossuiRegras(verificaRegras(processo));

        /**
         * Marca o processo como lido caso o usuário logado seja o analista designado.
         */
        Usuario analista = processo.getAnalista();
        if (usuarioLogado.equals(analista)) {
            processoLogService.marcarComoLido(processo.getId(), usuarioLogado);
        }

        StatusProcesso status = processo.getStatus();
        Usuario autor = processo.getAutor();
        if (StatusProcesso.RASCUNHO.equals(status) && usuarioLogado.equals(autor)) {
            detalhesProcesso.setExibirAlertaRascunho(true);
        }

        /**
         * Recupera os tempos restante do status e situação.
         */
        String horasRestantes = processoService.getHorasRestantes(processo);
        String horasRestantesSituacao = processoService.getHorasRestantesSituacao(processo);
        detalhesProcesso.setTempoRestanteStatus(horasRestantes);
        detalhesProcesso.setTempoRestanteSituacao(horasRestantesSituacao);

        /**
         * Verifica quantidade de processos abertos/fechados para determinado CPF/CNPJ.
         */
        Integer processosAbertos = processoService.countAbertosByCpfCnpj(processo);
        Integer processosFechados = processoService.countFechadosByCpfCnpj(processo);
        detalhesProcesso.setQuantidadeProcessosAbertos(processosAbertos);
        detalhesProcesso.setQuantidadeProcessosFechados(processosFechados);

        /**
         * Verifica se o acesso ao processo está bloqueado.
         */
        boolean acessoBloqueado = processoService.isAcessoBloqueado(processo.getId(), usuarioLogado);
        if (acessoBloqueado) {
            throw new ProcessoRestException("usuario.nao.tem.permissao.para.acessar.processo");
        }

        RelatorioGeral relatorioGeral = relatorioGeralService.getByProcessoAndCriaCasoNaoExista(processo.getId());
        detalhesProcesso.parserProcesso(relatorioGeral);

        /**
         * Recupera as permissões desse  usuário x processo x solicitação.
         */
        PermissaoResponse permissaoResponse = autorizacaoEdicaoService.verificaPermissoes(usuarioLogado, imagePath, processo.getId());
        detalhesProcesso.setPermissao(permissaoResponse);

        BloqueioProcesso bloqueioProcesso = bloqueioProcessoService.getByProcesso(processoId);
        if(bloqueioProcesso != null){
            Long usuarioId = bloqueioProcesso.getUsuarioId();
            Usuario usuario = usuarioService.get(usuarioId);

        }else
        if(usuarioLogado.isAnalistaRole() || usuarioLogado.isAnalistaRole()) {
            Subperfil subperfilAtivo = usuarioLogado.getSubperfilAtivo();
            Set<SubperfilSituacao> situacoes = subperfilAtivo.getSituacoes();
            Situacao situacao = processo.getSituacao();
            for(SubperfilSituacao ss: situacoes){
                Situacao ssSituacao = ss.getSituacao();
                if(ssSituacao.equals(situacao)){
                    bloqueioProcessoService.bloquear(processoId, usuarioLogado);
                }
            }
        }

        return detalhesProcesso;
    }

    /**
     * Converte o Map de grupos de campos do processo para um List.
     */
    public List<GrupoCamposResponse> getGruposCampoByProcesso(Usuario usuario, Long processoId) throws ProcessoRestException {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        List<GrupoCamposResponse> listGrupos = new ArrayList<>();

        Map<CampoGrupo, List<Campo>> map = campoService.findMapByProcesso(usuario, processoId, true);
        for (CampoGrupo grupo : map.keySet()) {

            List<Campo> campos = map.get(grupo);
            GrupoCamposResponse grupoCampos = new GrupoCamposResponse(grupo);
            grupoCampos.parserCampos(campos);
            listGrupos.add(grupoCampos);
        }

        return listGrupos;
    }

    /** Verifica se o processo possui regras associadas. */
    public boolean verificaRegras(Processo processo) {

        TipoProcesso tipoProcesso = processo.getTipoProcesso();
        Long tipoProcessoId = tipoProcesso.getId();
        RegraFiltro filtro = new RegraFiltro();
        filtro.setTipoProcessoId(tipoProcessoId);
        filtro.setAtiva(true);
        filtro.setVigencia(new Date());
        int count = regraService.countByFiltro(filtro);
        return count > 0;
    }

    public List<AcompanhamentoResponse> getAcompanhamento(Long processoId) throws ProcessoRestException {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }
        List<AcompanhamentoResponse> list = null;

        List<ProcessoLog> logs1 = processoLogService.findByProcesso(processoId);
        List<DocumentoLog> logs2 = documentoLogService.findByProcesso(processoId);

        List<LogVO> logs = new ArrayList<>();
        for (ProcessoLog pl : logs1) {
            LogVO vo = new LogVO(pl);
            logs.add(vo);
        }
        for (DocumentoLog dl : logs2) {
            LogVO vo = new LogVO(dl);
            logs.add(vo);
        }

        Collections.sort(logs, new ReflectionBeanComparator<>("data desc, id desc"));
        if (logs != null && logs.size() > 0) {
            list = new ArrayList<>();
            for (LogVO log : logs) {
                list.add(parseAcompanhamento(log));
            }
        }
        return list;
    }

    /**
     * Retorna os Acompanhamento/Logs de um determinado processo.
     * @param log
     * @return
     */
    private AcompanhamentoResponse parseAcompanhamento(LogVO log) {
        AcompanhamentoResponse acompanhamento = new AcompanhamentoResponse(log);
        StringBuffer sb = new StringBuffer();
        sb.append(messageService.getValue(log.getAcaoKey()));

        if(log.getDocumento() != null){
            sb.append(" - ").append(log.getDocumento().getNome());
        }

        if(log.getTipoEvidencia() != null){
            sb.append(" - ").append(log.getTipoEvidencia().getDescricao());
        }

        if(log.getSolicitacao() != null){
            sb.append(" - ").append(log.getSolicitacao().getSubarea().getArea().getDescricao());
            sb.append(" / ").append(log.getSolicitacao().getSubarea().getDescricao());
        }
        acompanhamento.setAcao(sb.toString());
        return acompanhamento;
    }

    /**
     * Retorna os documentos de um determinado Processo.
     * @param usuario
     * @param imagePath
     * @param processoId
     * @return
     */
    public ListaDocumentoResponse getDocumentos(Usuario usuario, String imagePath, Long processoId) throws ProcessoRestException {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        ListaDocumentoResponse listaDocumentoResponse = new ListaDocumentoResponse();
        listaDocumentoResponse.setBadgeDocumentosPendente(temAlertarDocumentos(usuario, processo));

        List<DocumentoResponse> list = null;

        List<DocumentoVO> vOsByProcesso = documentoService.findVOsByProcesso(processoId, usuario, imagePath);
        if(vOsByProcesso != null && vOsByProcesso.size() > 0){
            list = new ArrayList<>();
            for(DocumentoVO docVo : vOsByProcesso){
                list.add(new DocumentoResponse(docVo));
            }
        }

        listaDocumentoResponse.setDocumentos(list);
        return listaDocumentoResponse;
    }

    //FIXME esse método TODO está sendo duplicado em ProcessoEditBean.carregarTemAlertarDocumentos()!!!
    private int temAlertarDocumentos(Usuario usuario, Processo processo) {

        List<StatusDocumento> statusDocumentos = new ArrayList<>();
        if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
            statusDocumentos.add(StatusDocumento.DIGITALIZADO);
            statusDocumentos.add(StatusDocumento.INCLUIDO);
        }
        else if(usuario.isRequisitanteRole()) {
            statusDocumentos.add(StatusDocumento.PENDENTE);
            statusDocumentos.add(StatusDocumento.INCLUIDO);
        }
        else if(usuario.isAreaRole()) {

            Usuario autor = processo.getAutor();
            if(usuario.equals(autor)) {
                statusDocumentos.add(StatusDocumento.PENDENTE);
                statusDocumentos.add(StatusDocumento.INCLUIDO);
            }
        }
        else {
            return 0;
        }

        if(statusDocumentos == null) {
            return 0;
        }

        DocumentoFiltro filtro = new DocumentoFiltro();
        filtro.setProcesso(processo);
        filtro.setStatusDocumentoList(statusDocumentos);
        int count = documentoService.countByFiltro(filtro);
        return count;
    }

    private boolean verificaPermissaoCriacaoProcesso(Usuario usuario) {
        Funcionalidade funcionalidade = Funcionalidade.PROCESSOS;
		boolean isAnalistaRole = usuario.isAnalistaRole();
		if(funcionalidade == null || isAnalistaRole) {
            return false;
        }
        return funcionalidade.isCadastravel(usuario);
    }

    private boolean permiteConclusaoEmMassa(Usuario usuario){

        if (usuario.isAdminRole()){
            return true;
        }

        Subperfil subperfil = usuario.getSubperfilAtivo();

        if(subperfil != null){
            return subperfil.getPermiteConclusaoEmMassa();
        }

        return false;
    }

    public List<GrupoCamposResponse> getGruposCampoByProcessoAndSituacao(Usuario usuario, Long processoId, Long situacaoId) throws ProcessoRestException {

        Processo processo = processoService.get(processoId);

        List<Campo> campos = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processo.getId(), situacaoId, null, null);

        Map<Long, GrupoCamposResponse> map = new LinkedHashMap<>();
        for (Campo campo : campos) {

            CampoGrupo grupo = campo.getGrupo();
            Long grupoId = grupo.getId();
            GrupoCamposResponse gcr = map.get(grupoId);
            gcr = gcr != null ? gcr : new GrupoCamposResponse(grupo);
            map.put(grupoId, gcr);

            List<CampoResponse> campos1 = gcr.getCampos();
            campos1 = campos1 != null ? campos1 : new ArrayList<>();
            gcr.setCampos(campos1);
            campos1.add(new CampoResponse(campo));
        }

        return new ArrayList<>(map.values());
    }

    /**
     * Retorna os campos do tipo de processo.
     * @param 'usuario'
     * @param tipoProcessoId
     * @return
     */
    public List<GrupoCamposResponse> getGruposCampoByTipoProcesso(Usuario usuario,Long tipoProcessoId) throws ProcessoRestException {

        TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
        if(tipoProcesso == null){
            throw new ProcessoRestException("tipo.processo.nao.localizado");
        }

        List<TipoCampoGrupo> grupos = new ArrayList<>();

        Map<TipoCampoGrupo, List<TipoCampo>> map = tipoCampoService.findMapByTipoProcesso(tipoProcesso.getId());
        Set<Map.Entry<TipoCampoGrupo,List<TipoCampo>>> entrySet = map.entrySet();
        for (Map.Entry<TipoCampoGrupo, List<TipoCampo>> entry : entrySet) {
            TipoCampoGrupo grupo = entry.getKey();
            if (grupo.getCriacaoProcesso()) {
                List<CampoAbstract> campos = new ArrayList<>();
                for (TipoCampo campo : entry.getValue()) {
                    campos.add(campo);
                }
                grupos.add(grupo);
            }
        }

        List<GrupoCamposResponse> listGrupos = null;
        if (grupos != null && !grupos.isEmpty()) {
            listGrupos = new ArrayList<>();
            for (TipoCampoGrupo tcg : grupos) {
                GrupoCamposResponse grupoCampos = new GrupoCamposResponse(tcg);
                List<TipoCampo> listCampos = map.get(tcg);
                for (TipoCampo tc : listCampos){
                    String pais = tc.getPais();
                    if(TipoEntradaCampo.COMBO_BOX_ID.equals(tc.getTipo()) && StringUtils.isBlank(pais)){
                        BaseInterna baseInterna1 = tc.getBaseInterna();
                        BaseInterna baseInterna = baseInternaService.get(baseInterna1.getId());
                        tc.setBaseInterna(baseInterna);
                        BaseRegistroFiltro filtro = new BaseRegistroFiltro();
                        filtro.setBaseInterna(baseInterna);
                        List<RegistroValorVO> opcoes = baseRegistroService.findByFiltro(filtro, null, null);
                        tc.setOpcoesBaseInterna(opcoes);
                    }
                }
                grupoCampos.parserCampos(listCampos);
                listGrupos.add(grupoCampos);
            }
        }

        return listGrupos;
    }

    public NovoProcessoResponse criarNovoProcesso(Usuario usuario, RequestCriarProcesso requestCriarProcesso, Origem origem) throws Exception {

        validaRequestParameters(requestCriarProcesso);

        List<RequestCampo> campos = requestCriarProcesso.getCampos();
        if(campos == null || campos.size() <= 0){
            throw new ProcessoRestException("processo.campos.nao.informados");
        }

        TipoProcesso tipoProcesso = tipoProcessoService.get(requestCriarProcesso.getTipoProcessoid());
        if(tipoProcesso == null){
            throw new ProcessoRestException("tipo.processo.nao.localizado");
        }

        if(campos == null || campos.size() <= 0){
            throw new ProcessoRestException("processo.campos.nao.informados");
        }

        Set<CampoAbstract> valoresCampos = new HashSet<>();
        campos.forEach(requestCampo -> {
            TipoCampo tipoCampo = tipoCampoService.get(requestCampo.getTipoCampoId());
            if(tipoCampo == null){
                try {
                    throw new ProcessoRestException("processo.tipo.campo.nao.localizado");
                } catch (ProcessoRestException e) {
                    e.printStackTrace();
                }
            }
            tipoCampo.setValor(requestCampo.getValor());
            tipoCampo.setOpcaoId(requestCampo.getOpcaoId());
            valoresCampos.add(tipoCampo);
        });

        CriacaoProcessoVO vo = new CriacaoProcessoVO();
        vo.setTipoProcesso(tipoProcesso);
        vo.setOrigem(origem);
        vo.setUsuario(usuario);
        vo.setValoresCampos(valoresCampos);

        NovoProcessoResponse response = new NovoProcessoResponse();
        try {
            Processo processo = processoService.criaProcesso(vo);
            response.setProcesso(processo);
        } catch (ProcessoService.ProcessoCriadoException e) {
            Processo processo = e.getProcesso();
            response.setProcesso(processo);
            Throwable cause = e.getCause();
            String message = exceptionService.getMessage(cause);
            response.setWarningMessage("Erro inesperado: " + message);
        }
        return response;
    }

    public boolean priorizar(Usuario usuario, Long processoId) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }
        processoService.subirNivelPrioridade(processo, usuario);
        return true;
    }

    public boolean enviarParaAnalise(Usuario usuario, Long processoId) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }
        processoService.enviarParaAnalise(processo, usuario);
        return true;
    }

    public List<ProcessoMesmoClienteResponse> requisicoesMesmoCliente(Usuario usuario, Long processoId) {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }
        ProcessoFiltro filtro = new ProcessoFiltro();
        String cpfCnpj = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CPF_CNPJ);
        filtro.setCpfCnpj(cpfCnpj);

        List<StatusProcesso> statusList = Arrays.asList(StatusProcesso.CONCLUIDO, StatusProcesso.CANCELADO, StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE, StatusProcesso.PENDENTE);
        filtro.setStatusList(statusList);

        List<ProcessoVO> vOsByFiltro = processoService.findVOsByFiltro(filtro, null, null);

        List<ProcessoMesmoClienteResponse> list = null;
        if(CollectionUtils.isNotEmpty(vOsByFiltro)){
            list = new ArrayList<>();
            for(ProcessoVO p : vOsByFiltro){
                list.add(new ProcessoMesmoClienteResponse(p));
            }
        }
        return list;
    }

    public DetalhesProcesso editar(Usuario usuario, String imagePath, Long processoId, RequestEditarProcesso requestEditarProcesso) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Map<Long, String> valores = requestEditarProcesso.getValorCampo();

        EditarProcessoVO editarProcessoVO = new EditarProcessoVO();
        editarProcessoVO.setValores(valores);
        editarProcessoVO.setUsuario(usuario);
        editarProcessoVO.setProcessoId(processoId);

        processoService.atualizarProcesso(editarProcessoVO);

        return detalhar(usuario, imagePath, processoId);
    }

    public boolean excluir(Usuario usuario, Long processoId) {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }
        processoService.excluir(processo.getId(), usuario);
        return true;
    }

    public boolean cancelar(Usuario usuario, Long processoId, RequestCancelarProcesso requestCancelarProcesso) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Situacao situacao = situacaoService.get(requestCancelarProcesso.getSituacaoId());
        if(situacao == null){
            throw new SituacaoRestException("situacao.nao.localizada.com.id", requestCancelarProcesso.getSituacaoId());
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_SOLICITACAO_CANCELAMENTO);
        EvidenciaVO evidenciaVO = getEvidenciaVo(usuario, processo, situacao, AcaoProcesso.CANCELAMENTO, key, requestCancelarProcesso);
        List<Campo> campos = preencheCamposSolicitacao(usuario, processo, requestCancelarProcesso);
        evidenciaVO.setCampos(campos);
        processoService.salvarEvidencia(evidenciaVO, processo, usuario);

        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        return true;
    }

    public List<SituacaoResponse> consultaMotivosCancelamento(Usuario usuario, Long processoId) {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        List<SituacaoResponse> list = null;

        List<Situacao> situacoes = getSituacoes(usuario, processo, StatusProcesso.CANCELADO);
        if(CollectionUtils.isNotEmpty(situacoes)){
            list = new ArrayList<>();
            for (Situacao situacoe : situacoes) {
                list.add(new SituacaoResponse(situacoe));
            }
        }
        return list;
    }

    private List<Situacao> getSituacoes(Usuario usuario, Processo processo, StatusProcesso status) {

        TipoProcesso tipoProcesso = processo.getTipoProcesso();
        Long tipoProcessoId = tipoProcesso.getId();

        List<Situacao> situacoes = null;
        if(status != null) {
            situacoes = situacaoService.findAtivas(status, tipoProcessoId);
            return CollectionUtils.isNotEmpty(situacoes) ? situacoes : null;
        }

        situacoes = new ArrayList<>();
        Situacao situacao = processo.getSituacao();
        if (situacao == null) {
            SituacaoFiltro filtro = new SituacaoFiltro();
            filtro.setTipoProcessoId(tipoProcessoId);
            filtro.setAtiva(true);
            situacoes = situacaoService.findByFiltro(filtro, null, null);
            return CollectionUtils.isNotEmpty(situacoes) ? situacoes : null;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole()) {
            SituacaoFiltro filtro = new SituacaoFiltro();
            filtro.setTipoProcessoId(tipoProcessoId);
            filtro.setAtiva(true);
            situacoes = situacaoService.findByFiltro(filtro, null, null);
            return CollectionUtils.isNotEmpty(situacoes) ? situacoes : null;
        }

        Long situacaoId = situacao.getId();
        situacao = situacaoService.get(situacaoId);
        Set<ProximaSituacao> proximas = situacao.getProximas();
        for(ProximaSituacao ps: proximas) {
            Situacao proxima = ps.getProxima();
            situacoes.add(proxima);
        }

        Collections.sort(situacoes, new SuperBeanComparator<Situacao>("nome"));
        return CollectionUtils.isNotEmpty(situacoes) ? situacoes : null;
    }

    public boolean adicionarDocumento(Usuario usuario, Long processoId, List<Long> documentosId) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        processoService.adicionarDocumento(processo, documentosId, usuario);
        return true;
    }

    public boolean excluirDocumento(Usuario usuario, Long processoId, Long documentoId) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Documento documento = documentoService.get(documentoId);
        if (documento == null) {
            throw new DocumentoRestException("documento.nao.localizado.com.id", documentoId);
        }

        processoService.excluirDocumento(processo, documento, usuario);
        return true;
    }

    public List<SituacaoResponse> consultaMotivosAcompanhamento(Usuario usuario, Long processoId) {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        List<SituacaoResponse> list = null;

        List<Situacao> situacoes = getSituacoes(usuario, processo, StatusProcesso.EM_ACOMPANHAMENTO);
        if(CollectionUtils.isNotEmpty(situacoes)){
            list = new ArrayList<>();
            for (Situacao situacoe : situacoes) {
                list.add(new SituacaoResponse(situacoe));
            }
        }
        return list;
    }

    public boolean acompanhar(Usuario usuario, Long processoId,  RequestAcompanharProcesso requestAcompanharProcesso) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Situacao situacao = situacaoService.get(requestAcompanharProcesso.getSituacaoId());
        if(situacao == null){
            throw new SituacaoRestException("situacao.nao.localizada.com.id", requestAcompanharProcesso.getSituacaoId());
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, processo.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_SOLICITACAO_ACOMPANHAMENTO);

        EvidenciaVO evidenciaVO = getEvidenciaVo(usuario, processo, situacao, AcaoProcesso.EM_ACOMPANHAMENTO, key, requestAcompanharProcesso);
        List<Campo> campos = preencheCamposSolicitacao(usuario, processo, requestAcompanharProcesso);
        evidenciaVO.setCampos(campos);
        processoService.salvarEvidencia(evidenciaVO, processo, usuario);
        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        return true;
    }

    private EvidenciaVO getEvidenciaVo(Usuario usuario, Processo processo, Situacao situacao, AcaoProcesso acaoProcesso, String mapKeyAnexos, RequestSolicitacaoProcesso requestSolicitacaoProcesso) {
        EvidenciaVO evidenciaVO = new EvidenciaVO();

        switch (acaoProcesso){
            case EM_ACOMPANHAMENTO:{
                evidenciaVO.setShowTipoEvidencia(false);
                evidenciaVO.setShowPrazoDias(true);
                evidenciaVO.setShowSituacao(true);
            }
            break;
            case CANCELAMENTO:{
                evidenciaVO.setShowTipoEvidencia(false);
                evidenciaVO.setShowSituacao(true);
            }
            case ALTERACAO_SITUACAO:{
                evidenciaVO.setShowTipoEvidencia(false);
                evidenciaVO.setShowPrazoDias(false);
                evidenciaVO.setShowSituacao(true);
            }
            break;
        }

        evidenciaVO.setAcao(acaoProcesso);
        evidenciaVO.setSituacao(situacao);
        evidenciaVO.setDestinatariosList(requestSolicitacaoProcesso.getEmails());
        evidenciaVO.setObservacaoTmp(requestSolicitacaoProcesso.getObservacao());
        evidenciaVO.setDestinatariosList(requestSolicitacaoProcesso.getDestinatarios());

        List<String> keys = requestSolicitacaoProcesso.getAnexos();
        evidenciaVO = setAnexoFile(processo, evidenciaVO, mapKeyAnexos, keys);
        return evidenciaVO;
    }

    private EvidenciaVO setAnexoFile(Processo processo, EvidenciaVO evidenciaVO, String key, List<String> keys) {
        if(CollectionUtils.isNotEmpty(keys)) {
            Map<String, UploadArquivo> mapChecksumFilePath = uploadMultipartServiceRest.get(key);
            if (mapChecksumFilePath == null || mapChecksumFilePath.keySet() == null || mapChecksumFilePath.keySet().size() <= 0) {
                throw new ProcessoRestException("documento.mapChecksumFilePath.null", processo.getId(), processo.getTipoProcesso().getNome());
            }

            /**
             * Objeto retorno ainda não processou nenhuma imagem, mantém como null...
             */
            Map<String, String> mapStatusUpload = new HashMap<>();
            keys.forEach(checksum -> {
                mapStatusUpload.put(checksum, null);
            });

            Set<String> listCheckSum = mapChecksumFilePath.keySet();
            for (String keyChecksum : listCheckSum) {
                UploadArquivo uploadArquivo = mapChecksumFilePath.get(keyChecksum);

                File tmpFile = FileUtils.getFile(uploadArquivo.getPath());
                if (!tmpFile.exists()) {
                    throw new AnexoRestException("anexo.file.null", tmpFile.getAbsolutePath());
                }
                DummyUtils.deleteOnExitFile(tmpFile);
                evidenciaVO.addAnexo(uploadArquivo.getNome(), tmpFile);

                //se chegou até aqui, atualiza o status com sucesso.
                mapStatusUpload.put(keyChecksum, uploadArquivo.getNome());
            }
        }
        return evidenciaVO;
    }

    public Set<String> passo1UploadAnexoRegistroEvidencia(Usuario usuario, Long processoId, MultipartFile[] multipartFiles) throws MessageKeyException {
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_EVIDENCIA, multipartFiles);
        return anexos;
    }

    public boolean registrarEvidencia(Usuario usuario, Long processoId, RequestRegistrarEvidencia requestRegistrarEvidencia) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        TipoEvidencia tipoEvidencia = tipoEvidenciaService.get(requestRegistrarEvidencia.getTipoEvidenciaId());
        if(tipoEvidencia == null){
            throw new TipoEvidenciaRestException("tipoevidencia.nao.localizado.id", requestRegistrarEvidencia.getTipoEvidenciaId());
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, processo.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_EVIDENCIA);

        EvidenciaVO evidenciaVO = new EvidenciaVO();
        evidenciaVO.setAcao(AcaoProcesso.REGISTRO_EVIDENCIA);
        evidenciaVO.setDestinatariosList(requestRegistrarEvidencia.getDestinatarios());
        evidenciaVO.setObservacaoTmp(requestRegistrarEvidencia.getObservacao());
        evidenciaVO = setAnexoFile(processo, evidenciaVO, key, requestRegistrarEvidencia.getAnexos());
        processoService.salvarEvidencia(evidenciaVO, processo, usuario);
        return true;
    }

    public Set<String> passo1UploadAnexoSolicitacaoCancelamento(Usuario usuario, Long processoId, MultipartFile[] multipartFiles) {
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_SOLICITACAO_CANCELAMENTO, multipartFiles);
        return anexos;
    }

    public Set<String> passo1UploadAnexoSolicitacaoAcompanhamento(Usuario usuario, Long processoId, MultipartFile[] multipartFiles) {
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_SOLICITACAO_ACOMPANHAMENTO, multipartFiles);
        return anexos;
    }

    public List<String> getDestinatarios(Usuario usuario, Long processoId) {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Set<CampoGrupo> grupos = processo.getGruposCampos();
        Set<CampoAbstract> camposSet = new LinkedHashSet<CampoAbstract>();
        Map<CampoGrupo, List<Campo>> camposMap = new LinkedHashMap<>();
        for (CampoGrupo grupo : grupos) {
            Set<Campo> campos = grupo.getCampos();
            for (Campo campo : campos) {
                List<Campo> list = camposMap.get(grupo);
                list = list != null ? list : new ArrayList<Campo>();
                camposMap.put(grupo, list);
                list.add(campo);
                camposSet.add(campo);
            }
        }

        Set<String> set = new TreeSet<>();

        for (CampoGrupo campoGrupo : camposMap.keySet()) {

            List<Campo> campos = camposMap.get(campoGrupo);
            for (Campo campo : campos) {

                TipoEntradaCampo tipo = campo.getTipo();
                if(TipoEntradaCampo.EMAIL.equals(tipo)) {

                    String nome = campo.getNome();
                    String valor = campo.getValor();

                    if(StringUtils.isNotBlank(valor)) {

                        nome = DummyUtils.capitalize(nome);
                        set.add(nome + " [" + valor + "]");
                    }
                }
            }
        }

        Usuario autor = processo.getAutor();
        if (autor != null) {
            set.add("Autor [" + autor.getEmail() + "]");
        }

        ArrayList<String> list = new ArrayList<>();
        list.addAll(set);
        return CollectionUtils.isNotEmpty(list) ? list : null;
    }

    public Set<String> passo1UploadAnexoEnvioEmail(Usuario usuario, Long processoId, MultipartFile[] multipartFiles) {
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, processo.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_ENVIO_EMAIL, multipartFiles);
        return anexos;
    }

    public boolean enviarEmail(Usuario usuario, Long processoId, RequestEnviarEmail requestEnviarEmail) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, processo.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_ENVIO_EMAIL);

        EmailVO emailVO = new EmailVO();
        emailVO.setObservacaoTmp(requestEnviarEmail.getObservacao());
        emailVO.setDestinatariosList(requestEnviarEmail.getDestinatarios());

        List<String> anexos = requestEnviarEmail.getAnexos();

        if(CollectionUtils.isNotEmpty(anexos)) {
            Map<String, UploadArquivo> mapChecksumFilePath = uploadMultipartServiceRest.get(key);
            if (mapChecksumFilePath == null || mapChecksumFilePath.keySet() == null || mapChecksumFilePath.keySet().size() <= 0) {
                throw new ProcessoRestException("processo.mapChecksumFilePath.null", processo.getId(), processo.getTipoProcesso().getNome());
            }

            Set<String> listCheckSum = mapChecksumFilePath.keySet();
            for (String keyChecksum : listCheckSum) {
                UploadArquivo uploadArquivo = mapChecksumFilePath.get(keyChecksum);

                File tmpFile = FileUtils.getFile(uploadArquivo.getPath());
                if (!tmpFile.exists()) {
                    throw new AnexoRestException("anexo.file.null", tmpFile.getAbsolutePath());
                }
                DummyUtils.deleteOnExitFile(tmpFile);
                emailVO.addAnexo(uploadArquivo.getNome(), tmpFile);
            }
        }
        emailEnviadoService.enviarEmail(emailVO, processo, usuario);
        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        return true;
    }

    public Set<String> passo1UploadAnexoConcluirTarefa(Usuario usuario, Long processoId, MultipartFile[] multipartFiles) {
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, processo.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_CONCLUIR_TAREFA, multipartFiles);
        return anexos;
    }

    public boolean concluirTarefa(Usuario usuario, Long processoId, RequestConcluirTarefa requestConcluirTarefa) throws Exception {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

		Long situacaoId = requestConcluirTarefa.getSituacaoId();
		Situacao situacao = situacaoService.get(situacaoId);
        if(situacao == null){
            throw new SituacaoRestException("situacao.nao.localizada.com.id", situacaoId);
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_CONCLUIR_TAREFA);

        EvidenciaVO evidenciaVO = getEvidenciaVo(usuario, processo, situacao, AcaoProcesso.ALTERACAO_SITUACAO, key, requestConcluirTarefa);
        List<Campo> campos = preencheCamposSolicitacao(usuario, processo, requestConcluirTarefa);
        evidenciaVO.setCampos(campos);
        processoService.salvarEvidencia(evidenciaVO, processo, usuario);
        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        Long usuarioId = usuario.getId();
        bloqueioProcessoService.desbloquearByUsuario(usuarioId);
        return true;
    }

    public List<SituacaoResponse> consultaMotivosConcluirTarefa(Usuario usuario, Long processoId) {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        List<SituacaoResponse> list = null;

        List<Situacao> situacoes = getSituacoes(usuario, processo, null);
        if(CollectionUtils.isNotEmpty(situacoes)){
            list = new ArrayList<>();
            for (Situacao situacoe : situacoes) {
                list.add(new SituacaoResponse(situacoe));
            }
        }
        return list;
    }

    public List<SituacaoResponse> consultaMotivosReabrirTarefa(Usuario usuario, Long processoId) {
        return consultaMotivosConcluirTarefa(usuario, processoId);
    }

    public Set<String> passo1UploadAnexoReabrirTarefa(Usuario usuario, Long processoId, MultipartFile[] multipartFiles) {
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_REABRIR_TAREFA, multipartFiles);
        return anexos;
    }

    public boolean reabrirTarefa(Usuario usuario, Long processoId, RequestReabrirTarefa requestReabrirTarefa) throws Exception {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Situacao situacao = situacaoService.get(requestReabrirTarefa.getSituacaoId());
        if(situacao == null){
            throw new SituacaoRestException("situacao.nao.localizada.com.id", requestReabrirTarefa.getSituacaoId());
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_REABRIR_TAREFA);

        EvidenciaVO evidenciaVO = getEvidenciaVo(usuario, processo, situacao, AcaoProcesso.ALTERACAO_SITUACAO, key, requestReabrirTarefa);
        List<Campo> campos = preencheCamposSolicitacao(usuario, processo, requestReabrirTarefa);
        evidenciaVO.setCampos(campos);
        processoService.salvarEvidencia(evidenciaVO, processo, usuario);
        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        return true;
    }

    public List<ProcessoLogAnexoResponse> getAnexosAcompanhamento(Long processoLogId) {
        ProcessoLog processoLog = processoLogService.get(processoLogId);
        if(processoLog == null){
            throw new ProcessoRestException("processo.log.nao.localizado", processoLogId);
        }
        Set<ProcessoLogAnexo> anexos = processoLog.getAnexos();
        if(CollectionUtils.isEmpty(anexos)){
            return null;
        }

        List<ProcessoLogAnexoResponse> list = new ArrayList<>();
        anexos.forEach(processoLogAnexo -> {
            list.add(new ProcessoLogAnexoResponse(processoLogAnexo));
        });
        return list;

    }

    public ListaProcessosResponse pesquisar(Usuario usuario, int inicio, int max, RequestPesquisaProcesso requestPesquisaProcesso) {

        ProcessoFiltro.ConsiderarData considerarData = requestPesquisaProcesso.getConsiderarData();
        Date dataInicio = requestPesquisaProcesso.getDataInicio();
        Date dataFim = requestPesquisaProcesso.getDataFim();
        String processoNumero = requestPesquisaProcesso.getNumero();
        Long processoId = StringUtils.isNotBlank(processoNumero) ? Long.parseLong(processoNumero.trim()) : null;

        ProcessoFiltro filtro = filtro(usuario, true);
        filtro.setConsiderarData(considerarData);
        filtro.setDataInicio(dataInicio);
        filtro.setProcessoId(processoId);
        filtro.setDataFim(dataFim);

        if(CollectionUtils.isNotEmpty(requestPesquisaProcesso.getSituacao())){
            List<Situacao> byIds = situacaoService.findByIds(requestPesquisaProcesso.getSituacao());
            if(CollectionUtils.isEmpty(byIds)){
                throw new ProcessoRestException("processo.pesquisa.situacao.nao.localizada");
            }
            filtro.setSituacao(byIds);
        }

        if(CollectionUtils.isNotEmpty(requestPesquisaProcesso.getTipoProcesso())){
            List<TipoProcesso> tiposProcesso = tipoProcessoService.findByIds(requestPesquisaProcesso.getTipoProcesso());
            if(CollectionUtils.isEmpty(tiposProcesso)){
                throw new ProcessoRestException("processo.pesquisa.tipoprocesso.nao.localizado");
            }
            filtro.setTiposProcesso(tiposProcesso);
        }

        filtro.setCpfCnpj(requestPesquisaProcesso.getCpfOuCnpj());
        filtro.setNomeCliente(requestPesquisaProcesso.getNomeCliente());
        filtro.setTexto(requestPesquisaProcesso.getTexto());
        filtro.setStatusList(requestPesquisaProcesso.getStatusProcesso());
        filtro.setPossuiEmailNaoLido(requestPesquisaProcesso.isEmailNaoLido());

        ListaProcessosResponse listaProcessosResponse = new ListaProcessosResponse();
        listaProcessosResponse.setCount(processoService.countByFiltro(filtro));

        if(listaProcessosResponse.getCount() > 0) {
            List<ProcessoVO> rows = new ArrayList<>();
            List<Long> processosIds = new ArrayList<>();

            Map<Long, Boolean> emailNaoLidoMap = new HashMap<>();
            Set<Long> evidenciasNaoLidas = new HashSet<>();
            Set<Long> reenviosNaoLidos = new HashSet<>();

            List<Processo> byFiltro = processoService.findByFiltro(filtro, inicio, max);
            byFiltro.forEach(processo -> {
                ProcessoVO processoVO = new ProcessoVO(processo);
                Long processoVoId = processo.getId();
                processoVO.setStatusOcr(processo.getStatusOcr());

                rows.add(processoVO);
                processosIds.add(processoVoId);
            });

            evidenciasNaoLidas = processoLogService.getEvidenciasNaoLidas(processosIds);
            reenviosNaoLidos = processoLogService.getReenviosNaoLidas(processosIds);
            emailNaoLidoMap = emailRecebidoService.getNaoLidos(processosIds);

            for (ProcessoVO processoVo : rows) {

                Processo processo = processoVo.getProcesso();
                Long processoVoId = processo.getId();

                Boolean possuiEmailNaoLido = emailNaoLidoMap.get(processoVoId);
                processoVo.setPossuiEmailNaoLido(possuiEmailNaoLido);

                boolean evidenciaNaoLida = evidenciasNaoLidas.contains(processoVoId);
                processoVo.setEvidenciaNaoLida(evidenciaNaoLida);

                boolean reenvioNaoLido = reenviosNaoLidos.contains(processoVoId);
                processoVo.setReenvioNaoLido(reenvioNaoLido);

                listaProcessosResponse.add(new ProcessoResponse(processoVo));
            }
        }

        return listaProcessosResponse;
    }

    private List<Campo> preencheCamposSolicitacao(Usuario usuario,Processo processo, RequestSolicitacaoProcesso requestSolicitacaoProcesso){
        List<RequestCampo> campos = requestSolicitacaoProcesso.getCampos();
        if(campos == null || campos.size() <= 0){
            return null;
        }

        Long processoId = processo.getId();
        Long situacaoId = requestSolicitacaoProcesso.getSituacaoId();
        List<Campo> campos2 = campoService.findByProcessoSituacaoOrNomeGrupo(usuario, processoId, situacaoId, null, null);
        Map<Long, Campo> campoMap = new LinkedHashMap<>();
        for (Campo campo : campos2) {
            Long tipoCampoId = campo.getTipoCampoId();
            campoMap.put(tipoCampoId, campo);
        }

        for (RequestCampo campo : campos) {
            Long tipoCampoId = campo.getTipoCampoId();
            Campo campo1 = campoMap.get(tipoCampoId);
            if(campo1 != null) {
                String valor = campo.getValor();
                campo1.setValor(valor);
            }
        }

        return campos2;
    }

    public List<OpcoesBaseInternaResponse> getOpcoesFiltro(Usuario usuario, RequestOpcoesFiltro rof) {

        Long tipoCampoId = rof.getTipoCampoId();
        List<String> criterios = rof.getCriterios();
        List<Long> idsPais = rof.getIdsPais();
        List<String> valoresPais = rof.getValoresPais();

        CampoDinamicoFiltroVO campoDinamicoFiltroVO = new CampoDinamicoFiltroVO();
        campoDinamicoFiltroVO.setTipoCampoId(tipoCampoId);
        campoDinamicoFiltroVO.setCriterios(criterios);
        campoDinamicoFiltroVO.setIdsPais(idsPais);
        campoDinamicoFiltroVO.setValoresPais(valoresPais);

        Map<String, String> opcoesMap = campoService.findOpcoesCampoDinamico(usuario, campoDinamicoFiltroVO);

        return toOpcoesBaseInternaResponse(opcoesMap);
    }

    private List<OpcoesBaseInternaResponse> toOpcoesBaseInternaResponse(Map<String, String> opcoesMap) {
        List<OpcoesBaseInternaResponse> responseList = new ArrayList<>();
        Set<Map.Entry<String,String>> set =  opcoesMap.entrySet();
        List<Map.Entry<String , String>> list = new ArrayList<>(set);
        for(Map.Entry<String , String> entry : list ) {
            OpcoesBaseInternaResponse response = new OpcoesBaseInternaResponse();
            response.setId(entry.getKey());
            response.setNome(entry.getValue());
            responseList.add(response);
        }
        return responseList;
    }

    public Long getProximaRequisicao(Usuario usuario) throws Exception {

        Processo processo = processoService.proximoProcessoComLock(usuario);
        if (processo == null) {
            throw new ProcessoRestException("processo.proxima.requisicao.nao.localizada");
        }

        return processo.getId();
    }

    public List<ProcessoRegraResponse> getRegrasProcesso(Usuario usuarioLogado, Long processoId) {

        ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
        filtro.setProcessoId(processoId);
        List<ProcessoRegra> processoRegras = processoRegraService.findLasts(filtro);

        if(processoRegras == null || processoRegras.size() == 0){
            return new ArrayList<>();
        }

        if(processoRegras.stream().allMatch(o -> o.getFarol() != null)) {
            processoRegras.sort(Comparator.comparing(ProcessoRegra::getFarol).reversed());
        }

        List<ProcessoRegraResponse> listResponse = new ArrayList<>();
        for(ProcessoRegra pr: processoRegras){
            listResponse.add(new ProcessoRegraResponse(pr));
        }
        return listResponse;
    }

    public List<ProcessoRegraResponse> reprocessarRegra(Usuario usuarioLogado, Long regraId) {

        ProcessoRegra regra = processoRegraService.getById(regraId);
        if(regra == null){
            throw new ProcessoRestException("processo.processar.regra.nao.localizada");
        }

        processoRegraService.reprocessarRegra(regra.getProcesso(), regra.getRegra(), usuarioLogado);
        return getRegrasProcesso(usuarioLogado, regra.getProcesso().getId());
    }

    public List<ProcessoRegraResponse> reprocessarRegrasTodas(Usuario usuarioLogado, Long processoId) {

        Processo processo = processoService.get(processoId);
        if(processo == null){
            throw new ProcessoRestException("processo.nao.localizado");
        }

        processoRegraService.reprocessarTodasRegras(processo, usuarioLogado);
        return getRegrasProcesso(usuarioLogado, processo.getId());
    }

    public List<ProcessoRegraResponse> regraRerocessarComErros(Usuario usuarioLogado, Long processoId) {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        processoRegraService.reprocessarRegrasComErro(processo, usuarioLogado);
        return getRegrasProcesso(usuarioLogado, processo.getId());
    }


    public List<ResultadoConsultaVO> consultasExternas(Usuario usuario, Long processoId) {

        if(usuario.isComercialRole()){
            return new ArrayList<>();
        }
        List<ResultadoConsultaVO> resultadoConsultaVOS;

        resultadoConsultaVOS =  processoRegraService.carregarConsultas(processoId);

        return resultadoConsultaVOS;

    }

	public List<FilaTrabalhoVoResponse> getAtualizacoesFila(List<Long> idsProcessos) {

    	List<FilaTrabalhoVoResponse> filaTrabalhoVoResponses = new ArrayList<>();

		for(Long id: idsProcessos){
			FilaTrabalhoVoResponse filaTrabalhoVoResponse = new FilaTrabalhoVoResponse();
			BloqueioProcesso bloqueioProcesso = bloqueioProcessoService.getByProcesso(id);
			if(bloqueioProcesso != null) {
				Long usuarioId = bloqueioProcesso.getUsuarioId();
				Usuario usuario = usuarioService.get(usuarioId);
				String nome = usuario.getNome();
				filaTrabalhoVoResponse.setProcessoId(id);
				filaTrabalhoVoResponse.setNomeAnalistaBloqueio(nome);
				filaTrabalhoVoResponses.add(filaTrabalhoVoResponse);
			}
		}

		return filaTrabalhoVoResponses;
	}
}