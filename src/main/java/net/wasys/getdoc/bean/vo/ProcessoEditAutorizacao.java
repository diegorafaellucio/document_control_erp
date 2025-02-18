package net.wasys.getdoc.bean.vo;

import net.wasys.getdoc.bean.ProcessoEditBean;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.util.DummyUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ProcessoEditAutorizacao {

	private Usuario usuarioLogado;
	private Processo processo;
	private ProcessoEditBean bean;
	private ProcessoService processoService;
	private SubperfilService subperfilService;
	private SituacaoService situacaoService;
	private TipificacaoVisionApiService tipificacaoVisionApiService;
	private boolean telaCandidato = false;
	private TipificacaoDarknetService tipificacaoDarknetService;
	private CampoGrupoService campoGrupoService;

	public boolean podeRemoverGrupo(CampoGrupo grupo) {
		return grupo != null && grupo.getGrupoDinamico();
	}

	public boolean podeExcluir() {

		Usuario usuario = getUsuarioLogado();
		Usuario autor = processo.getAutor();
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		StatusProcesso status = processo.getStatus();

		if(usuario.equals(autor) && StatusProcesso.RASCUNHO.equals(status)) {
			return true;
		}

		if(usuario.isAdminRole()) {
			return true;
		}

		return false;
	}

	public boolean podeEmAcompanhamento() {

		StatusProcesso status = processo.getStatus();
		if(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE).contains(status)) {
			return false;
		}

		if(StatusProcesso.CANCELADO.equals(status)
				|| StatusProcesso.CONCLUIDO.equals(status)
				|| StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeReenviarAnalise() {

		StatusProcesso status = processo.getStatus();

		if(!StatusProcesso.CANCELADO.equals(status)
				&& !StatusProcesso.CONCLUIDO.equals(status)
				&& !StatusProcesso.EM_ACOMPANHAMENTO.equals(status)) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeConcluir() {

		StatusProcesso status = processo.getStatus();
		if(Arrays.asList(StatusProcesso.AGUARDANDO_ANALISE).contains(status)) {
			return false;
		}

		if(StatusProcesso.CANCELADO.equals(status)
				|| StatusProcesso.CONCLUIDO.equals(status)
				|| StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {

			ItemPendente itemPendente = processoService.getItemPendenteConclusao(processo, usuario);
			bean.setItemPendente(itemPendente);

			return true;
		}

		return false;
	}

	public boolean podeConcluirSituacao() {

		StatusProcesso status = processo != null ? processo.getStatus() : null;
		if(isTelaCandidato() && processo != null && processo.isSisFiesOrSisProuni() && !StatusProcesso.RASCUNHO.equals(status)) {
			return true;
		}

		boolean salaMatriculaRole = usuarioLogado.isSalaMatriculaRole();
		if(salaMatriculaRole){
			return false;
		}

		RoleGD roleGD = usuarioLogado.getRoleGD();
		if(RoleGD.GD_COMERCIAL.equals(roleGD)) {
			return false;
		}

		if(StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}

		return podeEditar();
	}

	public boolean podeResponderPendencia() {

		StatusProcesso status = processo.getStatus();
		if(!StatusProcesso.PENDENTE.equals(status)) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {

			//FIXME: esse métodos está sendo chamado várias vezes no bean, fazendo a mesma consulta
			ItemPendente itemPendente = processoService.getItemPendenteResponderPendencia(processo, usuario);
			bean.setItemPendente(itemPendente);

			return true;
		}

		return false;
	}

	public boolean podeEnviarProcesso() {

		StatusProcesso status = processo.getStatus();
		if(!StatusProcesso.RASCUNHO.equals(status) && !StatusProcesso.PENDENTE.equals(status) && !processo.isMestradoDoutoradoMedicina()) {
			return false;
		}

		if(processo.isSisFiesOrSisProuni()) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isSalaMatriculaRole()) {
			ItemPendente itemPendente = processoService.getItemPendenteEnviarProcesso(processo, usuario);
			bean.setItemPendente(itemPendente);
			return true;
		}

		return false;
	}

	public boolean podeEnviarRevisao() {

		StatusProcesso statusProcesso = processo.getStatus();

		if(StatusProcesso.EM_ANALISE.equals(statusProcesso) || StatusProcesso.EM_ACOMPANHAMENTO.equals(statusProcesso)){
			return false;
		}

		Long alunoProcessoId = processo.getAlunoProcessoId();
		if(alunoProcessoId != null) {
			return false;
		}

		return bean.podePedirRevisaoIsencao();
	}

	public boolean podeEnviarIsencao() {
		return bean.podePedirIsencao();
	}

	public boolean podeEnviarRevisaoAntigo() {

		Long alunoProcessoId = processo.getAlunoProcessoId();
		if(alunoProcessoId != null) {
			return false;
		}

		String campoProcessoValor = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.RESULTADO_ISENCAO_DISCIPLINA);
		StatusProcesso processoStatus = processo.getStatus();
		if(campoProcessoValor == null || StatusProcesso.EM_ANALISE.equals(processoStatus) || StatusProcesso.EM_ACOMPANHAMENTO.equals(processoStatus)){
			return false;
		}

		if(campoProcessoValor.equals("Deferido") || campoProcessoValor.equals("Indeferido")) {
			return true;
		}

		return false;
	}

	public boolean podePendenciar() {

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

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeCancelar() {

		StatusProcesso status = processo.getStatus();
		if(StatusProcesso.CANCELADO.equals(status)
				|| StatusProcesso.CONCLUIDO.equals(status)
				|| StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole()) {
			return true;
		}

		return false;
	}

	public boolean podeRegistrarEvidencia() {

		if(!podeEditarProcesso()) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}
		else if(usuario.isRequisitanteRole()) {

			Usuario autor = processo.getAutor();
			if(!usuario.equals(autor)) {
				return false;
			}

			StatusProcesso status = processo.getStatus();
			if(StatusProcesso.PENDENTE.equals(status)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeDigitalizarDocumentoTwain() {
		return podeDigitalizarDocumento(null);
	}

	public boolean podeDigitalizarDocumento(DocumentoVO documentoVO) {

		Situacao situacao = processo.getSituacao();
		boolean permiteEditarDocumentos = situacao != null ? situacao.isPermiteEditarDocumentos() : true;
		boolean bloqueioDeDigitalizacaoPorCalendario = situacao.isBloqueioDeDigitalizacaoPorCalendario();
		TipoDocumento tipoDocumento = documentoVO != null ? documentoVO.getTipoDocumento() : null;
		Long codOrigem = tipoDocumento != null ? tipoDocumento.getCodOrigem() : null;
		Long tipoDocumentoId = tipoDocumento != null ? tipoDocumento.getId() : null;
		StatusDocumento statusDoc = documentoVO != null ? documentoVO.getStatus() : null;

		if(!permiteEditarDocumentos){
			if(bloqueioDeDigitalizacaoPorCalendario && TipoDocumento.DOCUMENTO_TCB_TR_PROUNI_NOFICACAO_CALENDARIO.contains(tipoDocumentoId)) {
				return true;
			}
			return false;
		}

		if(bloqueioDeDigitalizacaoPorCalendario && Arrays.asList(TipoDocumento.TERMO_DE_PENDENCIA_SISPROUNI_ID, TipoDocumento.CHECKLIST_ID).equals(tipoDocumentoId)) {
			return !StatusDocumento.APROVADO.equals(statusDoc);
		}

			if(StatusDocumento.APROVADO.equals(statusDoc)){
				Boolean isPastaAmarela = documentoVO.getExisteAdvertencia();
				if(isPastaAmarela) {
					return true;
				}
				Date hoje = new Date();
				Date dataExpiracao = (documentoVO.getValidadeExpiracao() != null? documentoVO.getValidadeExpiracao() : hoje);

				if(hoje.after(dataExpiracao)){
					documentoVO.setVencido(true);
					return true;
				}
				return false;
			}

		if(codOrigem != null){
			if (TipoDocumento.TERMO_ACEITE_INGRESSO_SIMPLIFICADO_COD_ORIGEM.equals(codOrigem)) {
				documentoVO.setPermiteMarcarTermoAceiteIngressoSimplificado(false);
				return false;
			}
		}

		if(!podeEditarProcesso()) {
			return false;
		}

		List<DocumentoVO> documentos = bean.getDocumentos();
		if (documentos == null || documentos.isEmpty()) {
			return false;
		}

		Long situacaoId = situacao.getId();
		if(telaCandidato && Situacao.ANALISE_ISENCAO_IDS.contains(situacaoId)){
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isComercialRole() || usuario.isSalaMatriculaRole()) {
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
			}
			else if(StatusProcesso.PENDENTE.equals(status)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeCarregarDocumento() {

		Situacao situacao = processo.getSituacao();
		boolean permiteEditarDocumentos = situacao != null ? situacao.isPermiteEditarDocumentos() : true;
		if(!permiteEditarDocumentos){
			return false;
		}

		if(!podeEditarProcesso()) {
			return false;
		}

		boolean permiteTipificacao = situacao != null ? situacao.isPermiteTipificacao() : false;
		if(!permiteTipificacao){
			return false;
		}

		List<DocumentoVO> documentos = bean.getDocumentos();
		if (documentos == null || documentos.isEmpty()) {
			return false;
		}

		Long situacaoId = situacao.getId();
		if(telaCandidato && Situacao.ANALISE_ISENCAO_IDS.contains(situacaoId)){
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isComercialRole() || usuario.isSalaMatriculaRole()) {
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
			}
			else if(StatusProcesso.PENDENTE.equals(status)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeVisualizarDocumento() {

		List<DocumentoVO> documentos = bean.getDocumentos();
		if (documentos == null || documentos.isEmpty()) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isComercialRole() || usuario.isSalaMatriculaRole()) {
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
			}
			else if(StatusProcesso.PENDENTE.equals(status)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeAdicionarSolicitacao() {

		if(!podeEditarProcesso()) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeEnviarEmail() {

		if(!podeEditarProcesso()) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeEnviarEmailNotificacaoCandidatoSisFiesAndSisProuni() {

		if(!podeEditarProcesso()) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		Long subperfilAtivoId = subperfilAtivo != null ? subperfilAtivo.getId() : null;

		StatusProcesso status = processo.getStatus();
		if(processo.isSisFiesOrSisProuni()
				&& !StatusProcesso.CONCLUIDO.equals(status)
				&& Subperfil.SUBPERFIS_FIES_PROUNI_IDS.contains(subperfilAtivoId)) {
			return true;
		}

		if(processo.isSisFiesOrSisProuni() && (usuario.isAdminRole() || usuario.isGestorRole())) {
			return true;
		}

		return false;
	}

	private boolean podeEditarProcesso() {

		StatusProcesso status = processo.getStatus();
		List<StatusProcesso> statusFechado = StatusProcesso.getStatusFechado();
		if(statusFechado.contains(status) && !telaCandidato) {
			return false;
		}

		return true;
	}

	public boolean podeAlterarPrazoExpirado() {

		if(!podeEditarProcesso()) {
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
		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.equals(processo.getAnalista())) {
			return true;
		}

		return false;
	}

	public boolean podeTrocarAnalista() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole()) {
			return true;
		}

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if (subperfilAtivo != null) {
			Long subperfilAtivoId = subperfilAtivo.getId();
			if (Subperfil.CSC_ADM_ID.equals(subperfilAtivoId)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeTrocarTipoProcesso() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole()) {
			return true;
		}

		if(!podeEditarProcesso()) {
			return false;
		}

		if(usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeAdicionarDocumento() {

		Situacao situacao = processo.getSituacao();
		boolean permiteEditarDocumentos = situacao != null ? situacao.isPermiteEditarDocumentos() : true;
		if(!permiteEditarDocumentos){
			return false;
		}

		if(!podeEditarProcesso()) {
			return false;
		}

		Long situacaoId = situacao.getId();
		if(telaCandidato && Situacao.ANALISE_ISENCAO_IDS.contains(situacaoId)){
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isComercialRole() || usuario.isSalaMatriculaRole()) {
			return true;
		}
		if(usuario.isAreaRole()) {

			Usuario autor = processo.getAutor();
			if(usuario.equals(autor)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeExcluirDocumento(DocumentoVO docVO) {

		Situacao situacao = processo.getSituacao();
		boolean permiteEditarDocumentos = situacao != null ? situacao.isPermiteEditarDocumentos() : true;
		if(!permiteEditarDocumentos){
			return false;
		}

		if(!podeEditarProcesso()) {
			return false;
		}

		Long situacaoId = situacao.getId();
		if(telaCandidato && Situacao.ANALISE_ISENCAO_IDS.contains(situacaoId)){
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if (subperfilAtivo != null) {
			Long subperfilAtivoId = subperfilAtivo.getId();
			if (usuario.isSalaMatriculaRole() && Subperfil.SUBPERFIS_FIES_PROUNI_IDS.contains(subperfilAtivoId)) {
				return true;
			}
		}

		if(usuario.isAdminRole() || usuario.isGestorRole() || (usuario.isAnalistaRole() && docVO.getCountImagens() == 0)) {
			return true;
		}
		if(usuario.isAreaRole()) {

			Usuario autor = processo.getAutor();
			if(usuario.equals(autor)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeSubirNivelPrioridade() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole()) {
			return true;
		}

		return false;
	}

	public boolean podeEditar() {

		if(!podeEditarProcesso()) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();

		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isComercialRole() || usuario.isSalaMatriculaRole()) {
			return true;
		}

		Usuario analista = processo.getAnalista();
		if(analista != null && analista.equals(usuario)) {
			return true;
		}

		Boolean podeEditar = false;
		Subperfil subperfil = usuario.getSubperfilAtivo();
		if (subperfil != null) {

			FilaConfiguracao fc = subperfil.getFilaConfiguracao();
			boolean permiteEditarOutros = fc != null && fc.isPermissaoEditarOutros();
			if (permiteEditarOutros) {
				Situacao situacao = processo.getSituacao();
				Set<SubperfilSituacao> sss = subperfil.getSituacoes();
				for (SubperfilSituacao ss : sss) {
					Situacao situacao2 = ss.getSituacao();
					if(situacao2.equals(situacao)) {
						podeEditar = true;
					}
				}
			}
		}

		return podeEditar;
	}

	public boolean podeUsarCheckList() {
		RoleGD roleGD = usuarioLogado.getRoleGD();

		if(RoleGD.GD_COMERCIAL.equals(roleGD)) {
			return false;
		}

		return true;
	}

	public boolean podeVerificarProxima() {

		Usuario usuario = getUsuarioLogado();
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

	public boolean podeIniciarTrabalho() {

		Usuario usuario = getUsuarioLogado();
		if(!usuario.isAnalistaRole()) {
			return false;
		}

		Usuario analista = processo.getAnalista();
		if(analista != null) {
			return false;
		}

		Boolean podeVerificarProximaRequisicao = true;
		Subperfil subperfil = usuarioLogado.getSubperfilAtivo();
		if (subperfil != null) {
			FilaConfiguracao filaConfiguracao = subperfil.getFilaConfiguracao();
			if (filaConfiguracao != null) {
				podeVerificarProximaRequisicao = filaConfiguracao.isVerificarProximaRequisicao();
			}

			if(podeVerificarProximaRequisicao) {
				return false;
			}

			Situacao situacao = processo.getSituacao();
			Set<SubperfilSituacao> sss = subperfil.getSituacoes();
			for (SubperfilSituacao ss : sss) {
				Situacao situacao2 = ss.getSituacao();
				if(situacao2.equals(situacao)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean podeEditarCampos() {

		Situacao situacao = processo.getSituacao();
		boolean permiteEditarCampos = situacao != null ? situacao.isPermiteEditarCampos() : true;

		if(permiteEditarCampos && processo.isSisFiesOrSisProuni()) {
			return true;
		}

		if(!permiteEditarCampos){
			return false;
		}

		if(!podeEditarProcesso()) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isComercialRole()) {
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
			}
			else if(StatusProcesso.PENDENTE.equals(status)) {
				return true;
			}
		}

		if(processo.isSisFiesOrSisProuni() && usuario.isSalaMatriculaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeResponderSolicitacao(Solicitacao solicitacao) {

		StatusSolicitacao status = solicitacao.getStatus();
		if(StatusSolicitacao.RESPONDIDA.equals(status) || StatusSolicitacao.RECUSADA.equals(status)) {
			return false;
		}

		Date dataFinalizacao = solicitacao.getDataFinalizacao();
		if(dataFinalizacao != null) {
			return false;
		}

		Usuario usuario = getUsuarioLogado();
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

	public boolean podeAceitarSolicitacao(Solicitacao solicitacao) {

		Usuario usuario = getUsuarioLogado();
		if(!usuario.isAdminRole() && !usuario.isAnalistaRole() && !usuario.isGestorRole()) {
			return false;
		}

		Date dataFinalizacao = solicitacao.getDataFinalizacao();
		if(dataFinalizacao != null) {
			return false;
		}

		StatusSolicitacao status = solicitacao.getStatus();
		if(StatusSolicitacao.RESPONDIDA.equals(status) || StatusSolicitacao.RECUSADA.equals(status)) {
			return true;
		}

		return false;
	}

	public boolean podeNaoAceitar(Solicitacao solicitacao) {
		return podeAceitarSolicitacao(solicitacao);
	}

	public boolean podeRecusarSolicitacao(Solicitacao solicitacao) {

		Usuario usuario = getUsuarioLogado();

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

	public boolean podeMarcarEmailLido() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole()) {
			return true;
		}

		return false;
	}

	public boolean podeMarcarEmailNaoLido() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean mostrarAbaAjuda() {
		Usuario usuario = getUsuarioLogado();
		return !usuario.isRequisitanteRole();
	}

	public boolean mostrarNumeroProcesso() {
		return true;
	}

	public boolean mostrarAbaEmails() {

		StatusProcesso status = processo.getStatus();
		if(StatusProcesso.RASCUNHO.equals(status) && !processo.isSisFiesOrSisProuni()) {
			return false;
		}

		return true;
	}

	public boolean mostrarAbaAcompanhamento() {

		/*StatusProcesso status = processo.getStatus();
		if(StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}*/
		return true;
	}

	public boolean mostrarAbaSolicitacoes() {

		StatusProcesso status = processo.getStatus();
		if(StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}
		return true;
	}

	public Usuario getUsuarioLogado() {
		return usuarioLogado;
	}

	public void setUsuarioLogado(Usuario usuarioLogado) {
		this.usuarioLogado = usuarioLogado;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public void setBean(ProcessoEditBean bean) {
		this.bean = bean;
	}

	public void setProcessoService(ProcessoService processoService) {
		this.processoService = processoService;
	}

	public boolean podeIniciarTarefa() {

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

		Usuario usuario = getUsuarioLogado();
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

	public void setSubperfilService(SubperfilService subperfilService) {
		this.subperfilService = subperfilService;
	}

	public void setSituacaoService(SituacaoService situacaoService) {
		this.situacaoService = situacaoService;
	}

	public void setCampoGrupoService(CampoGrupoService campoGrupoService) {
		this.campoGrupoService = campoGrupoService;
	}

	public boolean podeReprocessarTodasRegras() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole()) {
			return true;
		}

		return false;
	}

	public boolean podeReprocessarRegra(ProcessoRegra processoRegra) {
		Usuario usuario = getUsuarioLogado();

		StatusProcessoRegra status = processoRegra.getStatus();
		if(usuario.isAnalistaRole() && StatusProcessoRegra.PENDENTE.equals(status)) {
			return true;
		}

		if(usuario.isAdminRole() || usuario.isGestorRole()) {
			return true;
		}

		return false;
	}

	public boolean podeReprocessarErrosRegras() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeVisualizarPendenciaDocumento(StatusDocumento statusDocumento) {

		if(StatusDocumento.PENDENTE.equals(statusDocumento)){
			return true;
		}

		return false;
	}

	public boolean podeExtrairTextoDocumento() {
		return tipificacaoDarknetService.isHabilitada();
	}

	public boolean mostrarAbaAnexos() {
		return true;
	}

	public void setTipificacaoVisionApiService(TipificacaoVisionApiService tipificacaoVisionApiService) {
		this.tipificacaoVisionApiService = tipificacaoVisionApiService;
	}

	public void setTelaCandidato(boolean telaCandidato) {
		this.telaCandidato = telaCandidato;
	}

	public boolean isTelaCandidato() {
		return telaCandidato;
	}

	public boolean podeConcluirSituacaoAndVincularProcessoComSiaTelaCadidato() {

		if(isTelaCandidato() && processo.isSisFiesOrSisProuni()) {
			return true;
		}

		return podeEditar() && podeConcluirSituacao() && !isTelaCandidato();
	}

	public boolean podeAlterarStatusDocumentoTelaCandidato() {

		Usuario usuario = getUsuarioLogado();
		if(usuario.isAdminRole() || usuario.isGestorRole() && processo.isSisFiesOrSisProuni()) {
			return true;
		}

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if(subperfilAtivo == null) {
			return false;
		}

		Long subperfilAtivoId = subperfilAtivo.getId();
		if(processo.isSisFiesOrSisProuni() && Subperfil.SUBPERFIS_FIES_PROUNI_IDS.contains(subperfilAtivoId)) {
			return true;
		}

		return podeEditar() && podeConcluirSituacao() && !isTelaCandidato();
	}

	public boolean podeAlterarModalidade() {

		Usuario usuario = getUsuarioLogado();
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Boolean isTransferenciaExterna = tipoProcessoId.equals(TipoProcesso.TRANSFERENCIA_EXTERNA) || tipoProcessoId.equals(TipoProcesso.MEDICINA_TE);
		Boolean isProcessoOriginal = processoService.isProcessoOriginal(processo);

		if(usuario.isSalaMatriculaRole() && isProcessoOriginal) {
			return false;
		}

		if((usuario.isAdminRole() || usuario.isGestorRole() || usuario.isSalaMatriculaRole()) && isTransferenciaExterna) {
			return true;
		}

		return podeEditar() && !isTelaCandidato() && isTransferenciaExterna;
	}

	public boolean desabilitarPesquisarTelaCandidato() {

		Usuario usuario = getUsuarioLogado();
		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();

		if(processo != null && processo.isSisFiesOrSisProuni()) {
			return true;
		}

		return false;
	}

	public TipificacaoDarknetService getTipificacaoDarknetService() {
		return tipificacaoDarknetService;
	}

	public void setTipificacaoDarknetService(TipificacaoDarknetService tipificacaoDarknetService) {
		this.tipificacaoDarknetService = tipificacaoDarknetService;
	}

	public boolean isIsencao(Processo processo){
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		if(TipoProcesso.ISENCAO_DISCIPLINAS.equals(tipoProcessoId)){
			return true;
		}else{
			return false;
		}
	}

	public boolean validarGrupoMateriasIsentas(Processo processo){
		Long processoId = processo.getId();
		Set<CampoGrupo> gruposCampos = processo.getGruposCampos();

		for(CampoGrupo grupoCampo : gruposCampos){
			String grupoNome = grupoCampo.getNome();
			if(CampoMap.GrupoEnum.COMPOSICAO_MATERIAS_ISENTAS.getNome().equals(grupoNome)){
				TipoCampoGrupo tipoSubgrupo = grupoCampo.getTipoSubgrupo();
				Long tipoSubgrupoId = tipoSubgrupo.getId();
				boolean existMateriaIsenta = campoGrupoService.existByProcesso(processoId, tipoSubgrupoId);
				if(!existMateriaIsenta){
					return false;
				}
			}
		}
		return true;
	}
}
