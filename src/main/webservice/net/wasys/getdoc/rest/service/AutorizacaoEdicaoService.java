package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.StatusSolicitacao;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.response.vo.PermissaoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service para consolidar as regras de edição do processo.
 */
@Service
public class AutorizacaoEdicaoService extends SuperServiceRest{

    @Autowired
    private ProcessoService processoService;
    @Autowired
    private SubperfilService subperfilService;
    @Autowired
    private SituacaoService situacaoService;
    @Autowired
    private DocumentoService documentoService;
    @Autowired
    private ParametroService parametroService;

    public PermissaoResponse verificaPermissoes(Usuario usuario, String imgPath, Long processoId) throws ProcessoRestException {
        return verificaPermissoes(usuario, imgPath, processoId, null);
    }

    public PermissaoResponse verificaPermissoes(Usuario usuario, String imgPath, Long processoId, Solicitacao solicitacao) throws ProcessoRestException {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        PermissaoResponse permissaoResponse = new PermissaoResponse();
        permissaoResponse.setPodeAdicionarDocumento(podeAdicionarDocumento(usuario, processo));
        permissaoResponse.setPodeAdicionarSolicitacao(podeAdicionarSolicitacao(usuario, processo));
        permissaoResponse.setPodeAlterarPrazoExpirado(podeAlterarPrazoExpirado(usuario, processo));
        permissaoResponse.setPodeCancelar(podeCancelar(usuario, processo));
        permissaoResponse.setPodeConcluir(podeConcluir(usuario,  processo));
        permissaoResponse.setPodeConcluirSituacao(podeConcluirSituacao(usuario, processo));
        permissaoResponse.setPodeDigitalizarDocumento(podeDigitalizarDocumento(usuario, imgPath, processo));
        permissaoResponse.setPodeDigitalizarDocumentoTwain(podeDigitalizarDocumentoTwain(usuario, imgPath, processo));
        permissaoResponse.setPodeEditar(podeEditar(usuario, processo));
        permissaoResponse.setPodeEditarCampos(podeEditarCampos(usuario, processo));
        permissaoResponse.setPodeEditarProcesso(podeEditarProcesso(usuario,processo));
        permissaoResponse.setPodeEmAcompanhamento(podeEmAcompanhamento(usuario, processo));
        permissaoResponse.setPodeEnviarEmail(podeEnviarEmail(usuario, processo));
        permissaoResponse.setPodeEnviarProcesso(podeEnviarProcesso(usuario, processo));
        permissaoResponse.setPodeExcluir(podeExcluir(usuario, processo));
        permissaoResponse.setPodeMarcarEmailLido(podeMarcarEmailLido(usuario));
        permissaoResponse.setPodeReenviarAnalise(podeReenviarAnalise(usuario, processo));
        permissaoResponse.setPodeResponderPendencia(podeResponderPendencia(usuario, processo));
        permissaoResponse.setPodePendenciar(podePendenciar(usuario, processo));
        permissaoResponse.setPodeRegistrarEvidencia(podeRegistrarEvidencia(usuario, processo));
        permissaoResponse.setPodeTrocarAnalista(podeTrocarAnalista(usuario));
        permissaoResponse.setPodeTrocarTipoProcesso(podeTrocarTipoProcesso(usuario, processo));
        permissaoResponse.setPodeSubirNivelPrioridade(podeSubirNivelPrioridade(usuario));
        permissaoResponse.setPodeVerificarProxima(podeVerificarProxima(usuario));
        permissaoResponse.setPodeIniciarTrabalho(podeIniciarTrabalho(usuario, processo));
        permissaoResponse.setPodeMarcarEmailNaoLido(podeMarcarEmailNaoLido(usuario));
        permissaoResponse.setMostrarAbaAjuda(mostrarAbaAjuda(usuario));
        permissaoResponse.setMostrarNumeroProcesso(mostrarNumeroProcesso(usuario));
        permissaoResponse.setMostrarAbaEmails(mostrarAbaEmails(processo));
        permissaoResponse.setMostrarAbaAcompanhamento(mostrarAbaAcompanhamento(processo));
        permissaoResponse.setMostrarAbaSolicitacoes(mostrarAbaSolicitacoes(processo));
        permissaoResponse.setPodeIniciarTarefa(podeIniciarTarefa(usuario, processo));
        permissaoResponse.setPodeReprocessarTodasRegras(podeReprocessarTodasRegras(usuario, processo));
        permissaoResponse.setPodeReprocessarErrosRegras(podeReprocessarErrosRegras(usuario, processo));
        permissaoResponse.setMostrarAbaAnexos(mostrarAbaAnexos());

        if(solicitacao != null){
            permissaoResponse.setPodeResponderSolicitacao(podeResponderSolicitacao(usuario, solicitacao));
            permissaoResponse.setPodeNaoAceitar(podeNaoAceitar(usuario, solicitacao));
            permissaoResponse.setPodeRecusarSolicitacao(podeRecusarSolicitacao(usuario, solicitacao));
        }

        return permissaoResponse;
    }

    private boolean podeExcluir(Usuario usuario, Processo processo) {

        Usuario autor = processo.getAutor();
        StatusProcesso status = processo.getStatus();

        if(usuario.equals(autor) && StatusProcesso.RASCUNHO.equals(status)) {
            return true;
        }

        return usuario.isAdminRole();

    }

    private boolean podeEmAcompanhamento(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();
        if(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE).contains(status)) {
            return false;
        }

        if(StatusProcesso.CANCELADO.equals(status)
                || StatusProcesso.CONCLUIDO.equals(status)
                || StatusProcesso.RASCUNHO.equals(status)) {
            return false;
        }

        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole();

    }

    private boolean podeReenviarAnalise(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();

        if(!StatusProcesso.CANCELADO.equals(status)
                && !StatusProcesso.CONCLUIDO.equals(status)
                && !StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
            return false;
        }

        return usuario.isAdminRole() || usuario.isGestorRole();

    }

    private boolean podeConcluir(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();
        if(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE).contains(status) && !usuario.isAdminRole() && !usuario.isGestorRole() && !usuario.isAnalistaRole()) {
            return false;
        }

        if(StatusProcesso.CANCELADO.equals(status)
                || StatusProcesso.CONCLUIDO.equals(status)
                || StatusProcesso.RASCUNHO.equals(status)
                || StatusProcesso.PENDENTE.equals(status)) {
            return false;
        }

        //            ItemPendente itemPendente = processoService.getItemPendenteConclusao(processo, usuario);
        //            bean.setItemPendente(itemPendente); //TODO retornar isso para o front
        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole();

    }

    private boolean podeConcluirSituacao(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();
        if(Arrays.asList(StatusProcesso.RASCUNHO, StatusProcesso.AGUARDANDO_ANALISE).contains(status)) {
            return false;
        }

        if(StatusProcesso.CANCELADO.equals(status)
                || StatusProcesso.CONCLUIDO.equals(status)
                || StatusProcesso.RASCUNHO.equals(status)
                || StatusProcesso.PENDENTE.equals(status)) {
            return false;
        }

        return usuario.isAdminRole() || usuario.isGestorRole() || (usuario.isAnalistaRole() && usuario.equals(processo.getAnalista()));

    }

    private boolean podeResponderPendencia(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();
        if(!StatusProcesso.PENDENTE.equals(status)) {
            return false;
        }

        //            ItemPendente itemPendente = processoService.getItemPendenteResponderPendencia(processo, usuario);
        //            bean.setItemPendente(itemPendente);//TODO retornar isso para o front
        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isRequisitanteRole();

    }

    private boolean podeEnviarProcesso(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();
        if(!StatusProcesso.RASCUNHO.equals(status) && !StatusProcesso.PENDENTE.equals(status)) {
            return false;
        }

        //            ItemPendente itemPendente = processoService.getItemPendenteEnviarProcesso(processo, usuario);
        //            bean.setItemPendente(itemPendente); //TODO retornar isso para o frontend
        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.isRequisitanteRole();

    }

    private boolean podePendenciar(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();
        if(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE).contains(status)) {
            return false;
        }

        if(StatusProcesso.CANCELADO.equals(status)
                || StatusProcesso.CONCLUIDO.equals(status)
                || StatusProcesso.PENDENTE.equals(status)
                || StatusProcesso.RASCUNHO.equals(status)) {
            return false;
        }

        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole();

    }

    private boolean podeCancelar(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();
        if(StatusProcesso.CANCELADO.equals(status)
                || StatusProcesso.CONCLUIDO.equals(status)
                || StatusProcesso.RASCUNHO.equals(status)) {
            return false;
        }

        return usuario.isAdminRole() || usuario.isGestorRole();

    }

    private boolean podeRegistrarEvidencia(Usuario usuario, Processo processo) {

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
            return true;
        }
        else if(usuario.isRequisitanteRole()) {

            Usuario autor = processo.getAutor();
            if(!usuario.equals(autor)) {
                return false;
            }

            StatusProcesso status = processo.getStatus();
            return StatusProcesso.PENDENTE.equals(status);
        }

        return false;
    }

    private boolean podeDigitalizarDocumentoTwain(Usuario usuario, String imgPath, Processo processo) {
        return podeDigitalizarDocumento(usuario, imgPath, processo);
    }

    private boolean podeDigitalizarDocumento(Usuario usuario, String imgPath, Processo processo) {

        Situacao situacao = processo.getSituacao();
        boolean permiteEditarDocumentos = situacao != null ? situacao.isPermiteEditarDocumentos() : true;
        if(!permiteEditarDocumentos){
            return false;
        }

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        List<DocumentoVO> documentos = documentoService.findVOsByProcesso(processo.getId(), usuario, imgPath);

        if (documentos == null || documentos.isEmpty()) {
            return false;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole()) {
            return true;
        }
        else if(usuario.isRequisitanteRole() || usuario.isAreaRole()) {

            Usuario autor = processo.getAutor();
            if(!usuario.equals(autor)) {
                return false;
            }

            StatusProcesso status = processo.getStatus();
            if(StatusProcesso.RASCUNHO.equals(status)) {
                return true;
            } else return StatusProcesso.PENDENTE.equals(status);
        }

        return false;
    }

    private boolean podeAdicionarSolicitacao(Usuario usuario, Processo processo) {

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole();

    }

    private boolean podeEnviarEmail(Usuario usuario, Processo processo) {

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole();

    }

    private boolean podeEditarProcesso(Usuario usuario, Processo processo) {

        if(usuario.isAnalistaRole()){
            return false;
        }
        StatusProcesso status = processo.getStatus();
        List<StatusProcesso> statusFechado = StatusProcesso.getStatusFechado();
        return !statusFechado.contains(status) || StatusProcesso.PENDENTE.equals(status);
    }

    private boolean podeAlterarPrazoExpirado(Usuario usuario, Processo processo) {

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        // somente apos Aguardando Análise
        if (processo.getStatus() == null ||
                StatusProcesso.CANCELADO.equals(processo.getStatus()) ||
                StatusProcesso.RASCUNHO.equals(processo.getStatus()) ||
                StatusProcesso.AGUARDANDO_ANALISE.equals(processo.getStatus()) ||
                StatusProcesso.CONCLUIDO.equals(processo.getStatus())) {
            return false;
        }

        //quem preenche: analista e gestor/admin
        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.equals(processo.getAnalista());

    }

    private boolean podeTrocarAnalista(Usuario usuario) {

        return usuario.isAdminRole() || usuario.isGestorRole();

    }

    private boolean podeTrocarTipoProcesso(Usuario usuario, Processo processo) {

        if(usuario.isAdminRole() || usuario.isGestorRole()) {
            return true;
        }

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        return usuario.isAnalistaRole();

    }

    private boolean podeAdicionarDocumento(Usuario usuario, Processo processo) {

        Situacao situacao = processo.getSituacao();
        boolean permiteEditarDocumentos = situacao != null ? situacao.isPermiteEditarDocumentos() : true;
        if(!permiteEditarDocumentos){
            return false;
        }

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isRequisitanteRole()) {
            return true;
        }
        if(usuario.isAreaRole()) {

            Usuario autor = processo.getAutor();
            return usuario.equals(autor);
        }

        return false;
    }

    private boolean podeSubirNivelPrioridade(Usuario usuario) {

        return usuario.isAdminRole() || usuario.isGestorRole();

    }

    private boolean podeEditar(Usuario usuario, Processo processo) {

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        if(usuario.isAnalistaRole()){
            return false;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole()) {
            return true;
        }

        Usuario analista = processo.getAnalista();
        if(analista != null && analista.equals(usuario)) {
            return true;
        }

        StatusProcesso status = processo.getStatus();
        if(StatusProcesso.PENDENTE.equals(status) || StatusProcesso.RASCUNHO.equals(status)){
            return true;
        }

        boolean podeEditarOutros = false;
        Subperfil subperfil = usuario.getSubperfilAtivo();
        if (subperfil != null) {
            FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
            if (filaConfiguracao != null) {
                podeEditarOutros = filaConfiguracao.isPermissaoEditarOutros();
            }
        }

        return podeEditarOutros;
    }

    private boolean podeVerificarProxima(Usuario usuario) {

        if(!usuario.isAnalistaRole()) {
            return false;
        }

        Boolean valor = true;
        Subperfil subperfil = usuario.getSubperfilAtivo();
        if (subperfil != null) {
            FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
            if (filaConfiguracao != null) {
                valor = filaConfiguracao.isVerificarProximaRequisicao();
            }
        }

        return valor;
    }

    private boolean podeIniciarTrabalho(Usuario usuario, Processo processo) {

        if(!usuario.isAnalistaRole()) {
            return false;
        }

        Boolean valor = true;
        Subperfil subperfil = usuario.getSubperfilAtivo();
        if (subperfil != null) {
            FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
            if (filaConfiguracao != null) {
                valor = filaConfiguracao.isVerificarProximaRequisicao();
            }
        }

        boolean pode = !valor;

        if(pode) {
            Usuario analista = processo.getAnalista();
            if(usuario.equals(analista)) {
                return false;
            }
        }

        return pode;
    }

    public boolean podeEditarCampos(Usuario usuario, Processo processo) {

        Situacao situacao = processo.getSituacao();
        boolean permiteEditarCampos = situacao != null ? situacao.isPermiteEditarCampos() : true;
        if(!permiteEditarCampos){
            return false;
        }

        if(!podeEditarProcesso(usuario, processo)) {
            return false;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole()) {
            return true;
        }
        else if(usuario.isRequisitanteRole()) {

            Usuario autor = processo.getAutor();
            if(!usuario.equals(autor)) {
                return false;
            }

            StatusProcesso status = processo.getStatus();
            if(StatusProcesso.RASCUNHO.equals(status)) {
                return true;
            } else return StatusProcesso.PENDENTE.equals(status);
        }

        return false;
    }

    private boolean podeAceitarSolicitacao(Usuario usuario, Solicitacao solicitacao) {

        if(!usuario.isAdminRole() && !usuario.isAnalistaRole() && !usuario.isGestorRole()) {
            return false;
        }

        Date dataFinalizacao = solicitacao.getDataFinalizacao();
        if(dataFinalizacao != null) {
            return false;
        }

        StatusSolicitacao status = solicitacao.getStatus();
        return StatusSolicitacao.RESPONDIDA.equals(status) || StatusSolicitacao.RECUSADA.equals(status);

    }

    private boolean podeMarcarEmailLido(Usuario usuario) {

        return usuario.isAdminRole() || usuario.isGestorRole();

    }

    private boolean podeMarcarEmailNaoLido(Usuario usuario) {
        return usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole();

    }

    private boolean mostrarAbaAjuda(Usuario usuario) {
        return !usuario.isRequisitanteRole();
    }

    private boolean mostrarNumeroProcesso(Usuario usuario) {
        return !usuario.isRequisitanteRole();
    }

    private boolean mostrarAbaEmails(Processo processo) {

        StatusProcesso status = processo.getStatus();
        return !StatusProcesso.RASCUNHO.equals(status);
    }

    private boolean mostrarAbaAcompanhamento(Processo processo) {

        StatusProcesso status = processo.getStatus();
        return !StatusProcesso.RASCUNHO.equals(status);
    }

    private boolean mostrarAbaSolicitacoes(Processo processo) {

        StatusProcesso status = processo.getStatus();
        return !StatusProcesso.RASCUNHO.equals(status);
    }

    private boolean podeIniciarTarefa(Usuario usuario, Processo processo) {

        StatusProcesso status = processo.getStatus();

        if(StatusProcesso.CANCELADO.equals(status)
                || StatusProcesso.CONCLUIDO.equals(status)
                || StatusProcesso.RASCUNHO.equals(status)) {
            return false;
        }

        if(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE).contains(status)) {
            return false;
        }

        Situacao situacao = processo.getSituacao();
        Long situacaoId = situacao.getId();
        situacao = situacaoService.get(situacaoId);
        if (situacao.isDistribuicaoAutomatica()) {
            return false;
        }

        if (processo.getAnalista() != null) {
            return false;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole()) {
            return true;
        }

        if (usuario.isAnalistaRole()) {
            Subperfil subperfil = usuario.getSubperfilAtivo();
            Long subperfilId = subperfil.getId();
            subperfil = subperfilService.get(subperfilId);
            Set<SubperfilSituacao> situacoes2 = subperfil.getSituacoes();
            for (SubperfilSituacao sps : situacoes2) {
                Situacao situacao2 = sps.getSituacao();
                if (situacao.equals(situacao2)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean podeReprocessarTodasRegras(Usuario usuario, Processo processo) {
        StatusProcesso status = processo.getStatus();
        if(StatusProcesso.CONCLUIDO.equals(status) || StatusProcesso.CANCELADO.equals(status)){
            return false;
        }
        Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
        if(subperfilAtivo != null){
            Long subperfilAtivoId = subperfilAtivo.getId();
            if(Subperfil.CONFERENTE_ID.equals(subperfilAtivoId)){
                return true;
            }
        }

        return usuario.isAdminRole();
    }

    private boolean podeReprocessarErrosRegras(Usuario usuario, Processo processo) {
        StatusProcesso status = processo.getStatus();
        if(StatusProcesso.CONCLUIDO.equals(status) || StatusProcesso.CANCELADO.equals(status)){
            return false;
        }

        return usuario.isAdminRole() || usuario.isAnalistaRole();
    }

    private boolean podeResponderSolicitacao(Usuario usuario, Solicitacao solicitacao) {

        StatusSolicitacao status = solicitacao.getStatus();
        if(StatusSolicitacao.RESPONDIDA.equals(status) || StatusSolicitacao.RECUSADA.equals(status)) {
            return false;
        }

        Date dataFinalizacao = solicitacao.getDataFinalizacao();
        if(dataFinalizacao != null) {
            return false;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
            return true;
        }

        if(usuario.isAreaRole()) {

            Subarea subarea = solicitacao.getSubarea();
            Area area1 = subarea.getArea();
            Area area2 = usuario.getArea();

            return area1.equals(area2);
        }

        return false;
    }

    private boolean podeNaoAceitar(Usuario usuario, Solicitacao solicitacao) {
        return podeAceitarSolicitacao(usuario, solicitacao);
    }

    private boolean podeRecusarSolicitacao(Usuario usuario, Solicitacao solicitacao) {

        StatusSolicitacao status = solicitacao.getStatus();
        if(!StatusSolicitacao.ENVIADA.equals(status)) {
            return false;
        }

        if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
            return true;
        }

        if(usuario.isAreaRole()) {

            Subarea subarea = solicitacao.getSubarea();
            Area area1 = subarea.getArea();
            Area area2 = usuario.getArea();

            return area1.equals(area2);
        }


        return false;
    }

    private boolean mostrarAbaAnexos() {
        return true;
    }
}
