package net.wasys.getdoc.rest.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusGeracaoPastaVermelha;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoPasta;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.rest.exception.AlunoRestException;
import net.wasys.getdoc.rest.request.vo.*;
import net.wasys.getdoc.rest.response.vo.ArquivoDownload;
import net.wasys.getdoc.rest.response.vo.DadosPastaVermelhaResponse;
import net.wasys.getdoc.rest.response.vo.DadosReaproveitamentoResponse;
import net.wasys.getdoc.rest.response.vo.DownloadAnexoResponse;
import net.wasys.getdoc.rest.response.vo.IniciarCriacaoPastaVermelhaResponse;
import net.wasys.getdoc.rest.response.vo.ProcessoReaproveitamentoResponse;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

import static java.util.Collections.singletonList;
import static net.wasys.util.DummyUtils.systraceThread;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Service
public class AlunoServiceRest extends SuperServiceRest {

	@Autowired private ProcessoService processoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ReaproveitamentoService reaproveitamentoService;
	@Autowired private RelatorioPastaVermelhaService relatorioPastaVermelhaService;
	@Autowired private DownloadFileService downloadFileService;
	@Autowired private ConsultaCandidatoService consultaCandidatoService;

	public DadosReaproveitamentoResponse reaproveitarDados(RequestReaproveitarDados requestReaproveitarDados) throws Exception {

		String numCandidato = requestReaproveitarDados.getNumCandidato();
		String numInscricao = requestReaproveitarDados.getNumInscricao();
		String cpf = requestReaproveitarDados.getCpf();

		if (isBlank(numCandidato) && isBlank(numInscricao)) {
			throw new AlunoRestException("identificador.nao.informado");
		}

		Processo processo = reaproveitamentoService.buscarProcesso(numCandidato, numInscricao, cpf);
		//logger.info("Processo encontrado para reaproveitamento: " + processo + ", : " + requestReaproveitarDados);

		DadosReaproveitamentoResponse response = new DadosReaproveitamentoResponse();

		if (processo != null) {

			ReaproveitamentoProcessoVO dadosProcesso = reaproveitamentoService.buscarDadosProcesso(processo);

			Boolean ignorarArquivos = requestReaproveitarDados.getIgnorarArquivos();
			ignorarArquivos = ignorarArquivos != null ? ignorarArquivos : false;
			List<ReaproveitamentoDocumentoVO> documentos = reaproveitamentoService.buscarDocumentos(processo, ignorarArquivos);

			List<ReaproveitamentoMembroFamiliarVO> membrosFamiliares = reaproveitamentoService.buscarMembrosFamiliares(processo);

			Long processoId = processo.getId();
			response.setProcessoCaptacaoId(processoId);
			response.setDadosProcesso(dadosProcesso);
			response.setDocumentos(documentos);
			response.setMembrosFamiliares(membrosFamiliares);

			//logger.info("dadosProcesso: " + dadosProcesso + ", documentos: " + documentos + ", membrosFamiliares: " + membrosFamiliares);
		}

		return response;
	}

	public void notificarReaproveitamento(RequestNotificarReaproveitamento request) throws Exception {

		systraceThread("Requisição para notificar reaproveitamento=" + request);

		List<Processo> processos = getProcessosFromRequest(request);

		String matricula = request.getMatricula();
		Long getdocAlunoProcessoId = request.getProcessoId();
		processoService.bloquearProcessosReaproveitados(processos, matricula, getdocAlunoProcessoId);

		systraceThread("Requisição para notificar finalizada com sucesso. request=" + request);
	}

	public DadosReaproveitamentoResponse validarReaproveitamento(Long processoId) throws Exception {

		systraceThread("Requisição para validar transforma" + processoId);

		Processo processo = processoService.get(processoId);

		DadosReaproveitamentoResponse response = new DadosReaproveitamentoResponse();

		if (processo != null) {

			ReaproveitamentoProcessoVO dadosProcesso = reaproveitamentoService.buscarDadosProcesso(processo);

			Boolean ignorarArquivos = true;
			ignorarArquivos = ignorarArquivos != null ? ignorarArquivos : false;
			List<ReaproveitamentoDocumentoVO> documentos = reaproveitamentoService.buscarDocumentos(processo, ignorarArquivos);

			List<ReaproveitamentoMembroFamiliarVO> membrosFamiliares = reaproveitamentoService.buscarMembrosFamiliares(processo);

			response.setProcessoCaptacaoId(processoId);
			response.setDadosProcesso(dadosProcesso);
			response.setDocumentos(documentos);
			response.setMembrosFamiliares(membrosFamiliares);

			//logger.info("dadosProcesso: " + dadosProcesso + ", documentos: " + documentos + ", membrosFamiliares: " + membrosFamiliares);
		}

		systraceThread("Requisição para validar transforma finalizada com sucesso. processoId=" + processoId);
		return response;
	}

	public ProcessoReaproveitamentoResponse buscarProcessoReaproveitamento(RequestBuscarProcessoReaproveitamento request) {

		systraceThread("Requisição para buscar processo para reaproveitamento=" + request);

		String numCandidato = request.getNumCandidato();
		String numInscricao = request.getNumInscricao();
		String cpf = request.getCpf();
		Boolean validarSituacaoAnterior = request.getValidarSituacaoAnterior();

		if (isBlank(numCandidato) && isBlank(numInscricao)) {
			throw new AlunoRestException("identificador.nao.informado");
		}

		Processo processo = null;
		try {
			processo = reaproveitamentoService.buscarProcesso(numCandidato, numInscricao, cpf);
		}
		catch (AlunoRestException exception) {
			// se não achar ou parâmetros estiverem inválidos não faz nada
		}

		systraceThread("Processo encontrado=" + processo);

		ProcessoReaproveitamentoResponse response = new ProcessoReaproveitamentoResponse();
		if(processo != null) {
			Situacao situacao = processo.getSituacao();
			if(reaproveitamentoService.isSituacaoAluno(situacao) && validarSituacaoAnterior) {
				Long situacaoIdAntesDoBloqueio = reaproveitamentoService.getSituacaoIdAntesDoBloqueio(processo);
				situacao = situacaoService.get(situacaoIdAntesDoBloqueio);
			}
			Long id = processo.getId();
			boolean sisFiesOrSisProuni = processo.isSisFiesOrSisProuni();
			StatusProcesso status = situacao.getStatus();
			Boolean usaTermo = processo.getUsaTermo();
			TipoPasta tipoPasta = TipoPasta.SEM_PASTA;
			if(usaTermo != null) {
				tipoPasta = usaTermo == true ? TipoPasta.PASTA_VERMELHA : TipoPasta.SEM_PASTA;
			}
			if(tipoPasta.equals(TipoPasta.SEM_PASTA)) {
				if(!status.equals(StatusProcesso.CONCLUIDO)) {
					tipoPasta = TipoPasta.PASTA_VERMELHA;
				}
			}

			response.setProcessoCaptacaoId(id);
			response.setStatusProcesso(status);
			response.setProuniOuFies(sisFiesOrSisProuni);
			response.setStatusProcesso(status);
			response.setTipoPasta(tipoPasta);
		}

		return response;
	}

	public IniciarCriacaoPastaVermelhaResponse iniciarCriacaoPastaVermelha(IniciarCriacaoPastaVermelhaRequestVO request) {

		systraceThread("Iniciando criação do relatório pasta vermelha para a request=" + request);

		String uuidRequest = relatorioPastaVermelhaService.iniciarCriacaoRelatorio(request);

		IniciarCriacaoPastaVermelhaResponse response = new IniciarCriacaoPastaVermelhaResponse();
		response.setUuid(uuidRequest);

		return response;
	}

	public DadosPastaVermelhaResponse buscarDadosPastaVermelha(BaixarDadosPastaVermelhaRequestVO request) {

		String uuid = request.getUuid();
		systraceThread("Buscando relatório pela UUID=" + uuid);

		DadosPastaVermelhaResponse response = new DadosPastaVermelhaResponse();
		if (relatorioPastaVermelhaService.estaExecutandoUUID(uuid)) {
			response.setStatusGeracaoPastaVermelha(StatusGeracaoPastaVermelha.EXECUTANDO);
			systraceThread("Relatório em execução.");
		}
		else {

			File relatorioCompactado = relatorioPastaVermelhaService.buscarRelatorioPorUUID(uuid);

			if (relatorioCompactado != null) {

				ArquivoDownload arquivoDownload = new ArquivoDownload();
				arquivoDownload.setNome("pasta-vermelha-compactado");
				arquivoDownload.setExtensao(GetdocConstants.EXTENSAO_DEFINICAO_ZIP);
				arquivoDownload.setPath(relatorioCompactado.getPath());
				arquivoDownload.setTamanho(relatorioCompactado.length());

				DownloadAnexoResponse download = downloadFileService.download(arquivoDownload);
				response.setDownloadAnexoResponse(download);
				systraceThread("Relatório finalizado.");
			}
			else {
				response.setStatusGeracaoPastaVermelha(StatusGeracaoPastaVermelha.NAO_ENCONTRADO);
				systraceThread("Relatório não encontrado para a UUID=" + uuid);
			}
		}

		return response;
	}

	private List<Processo> getProcessosFromRequest(RequestNotificarReaproveitamento requestNotificarReaproveitamentoDados) {

		String numCandidato = requestNotificarReaproveitamentoDados.getNumCandidato();
		String numInscricao = requestNotificarReaproveitamentoDados.getNumInscricao();
		Long processoCaptacaoId = requestNotificarReaproveitamentoDados.getProcessoCaptacaoId();

		Processo processo = processoService.get(processoCaptacaoId);
		if (processo != null) {
			return Arrays.asList(processo);
		}

		if (isBlank(numCandidato) && isBlank(numInscricao)) {
			throw new AlunoRestException("identificador.nao.informado");
		}

		List<Processo> processosFiltrados = new ArrayList<>();
		if (isNotBlank(numCandidato)) {

			ProcessoFiltro filtro = new ProcessoFiltro();
			filtro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.RECENTEMENTE_APROVADO);
			filtro.setCamposFiltro(CampoMap.CampoEnum.NUM_CANDIDATO, singletonList(numCandidato));
			boolean filtrarIsencaoDisciplinas = requestNotificarReaproveitamentoDados.isFiltrarIsencaoDisciplinas();
			if(filtrarIsencaoDisciplinas) {
				filtro.setTiposProcesso(Arrays.asList(new TipoProcesso(TipoProcesso.ISENCAO_DISCIPLINAS)));
			}
			else {

				filtro.setDesconsiderarTipoProcessoIds(Arrays.asList(TipoProcesso.ISENCAO_DISCIPLINAS));
			}

			processosFiltrados = processoService.findByFiltro(filtro, null, null);
		}

		if (isEmpty(processosFiltrados)) {

			if (isNotBlank(numInscricao)) {

				ProcessoFiltro filtro = new ProcessoFiltro();
				filtro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.RECENTEMENTE_APROVADO);
				filtro.setCamposFiltro(CampoMap.CampoEnum.NUM_INSCRICAO, singletonList(numInscricao));
				boolean filtrarIsencaoDisciplinas = requestNotificarReaproveitamentoDados.isFiltrarIsencaoDisciplinas();
				if(filtrarIsencaoDisciplinas) {
					filtro.setTiposProcesso(Arrays.asList(new TipoProcesso(TipoProcesso.ISENCAO_DISCIPLINAS)));
				}
				else {
					filtro.setDesconsiderarTipoProcessoIds(Arrays.asList(TipoProcesso.ISENCAO_DISCIPLINAS));
				}
				processosFiltrados = processoService.findByFiltro(filtro, null, null);
			}
		}

		if (processosFiltrados.isEmpty()) {
			throw new MessageKeyException("processoNaoEncontradoIdentificadores.error", "numCandidato=" + numCandidato + ", numInscricao=" + numInscricao);
		}

		return processosFiltrados;
	}

	public static DadosReaproveitamentoVO getDadosReaproveitamentoMock() {

		List<ReaproveitamentoMembroFamiliarVO> membroFamiliarList = new ArrayList<>();
		membroFamiliarList.add(new ReaproveitamentoMembroFamiliarVO("Membro Familiar (2)", "Nome1", "Pai", "01/01/1990", "100,20"));
		membroFamiliarList.add(new ReaproveitamentoMembroFamiliarVO("Membro Familiar (3)", "Nome2", "Mãe", "01/01/1970", "200,20"));

		List<ReaproveitamentoImagemVO> reaproveitamentoImagemVOList = new ArrayList<>();
		reaproveitamentoImagemVOList.add(getReaproveitamentoImagemMock());

		ReaproveitamentoDocumentoVO vo1 = new ReaproveitamentoDocumentoVO();
		vo1.setCodOrigem("1");
		vo1.setDocumentoCaptacaoId(123L);
		vo1.setImagens(reaproveitamentoImagemVOList);
		vo1.setPosfixo("ALUNO");
		vo1.setObrigatorio(false);
		vo1.setUsuarioDigitalizou("Fulano");
		vo1.setStatusDocumento(StatusDocumento.DIGITALIZADO);

		ReaproveitamentoDocumentoVO vo2 = new ReaproveitamentoDocumentoVO();
		vo2.setCodOrigem("1");
		vo2.setDocumentoCaptacaoId(124L);
		vo2.setImagens(reaproveitamentoImagemVOList);
		vo2.setPosfixo("MEMBRO FAMILIAR" + " (1)");
		vo2.setObrigatorio(false);
		vo2.setUsuarioAprovou("Ciclano");
		vo2.setStatusDocumento(StatusDocumento.APROVADO);

		ReaproveitamentoCampanhaVO dadosCampanha = new ReaproveitamentoCampanhaVO();
		dadosCampanha.setCampus("Campus1");
		dadosCampanha.setCursos("curso1");
		dadosCampanha.setDescricao("Descricao1");
		dadosCampanha.setEquivalencias("equivalencias");
		dadosCampanha.setFimVigencia(DummyUtils.parseDateTime("01/01/2020 12:00"));
		dadosCampanha.setInicioVigencia(DummyUtils.parseDateTime("01/01/2020 13:00"));
		dadosCampanha.setInstituicoes("instituicoes1");
		dadosCampanha.setPadrao(false);
		dadosCampanha.setTipoDocumentoObrigatorioIds("1,2,3,4,5");

		ReaproveitamentoProcessoVO dadosProcesso = new ReaproveitamentoProcessoVO();
		dadosProcesso.setUsaTermo(true);
		dadosProcesso.setDadosCampanha(dadosCampanha);

		DadosReaproveitamentoVO dadosReaproveitamentoVO = new DadosReaproveitamentoVO();
		dadosReaproveitamentoVO.setMembrosFamiliares(membroFamiliarList);
		dadosReaproveitamentoVO.setDocumentos(Arrays.asList(vo1, vo2));
		dadosReaproveitamentoVO.setDadosProcesso(dadosProcesso);

		return dadosReaproveitamentoVO;
	}

	public static ReaproveitamentoImagemVO getReaproveitamentoImagemMock() {

		File temp = DummyUtils.getFileFromResource("/net/wasys/getdoc/imagens/image_test.jpg");
		String caminho = temp.getAbsolutePath();
		String metadados = "{\"tamanhoOriginal\":\"91056\",\"dataDigitalizacao\":\"05/12/2019 13:40:42:530\",\"tamanhoFinal\":\"91056\"}";

		return new ReaproveitamentoImagemVO(caminho, metadados);
	}

	public DadosReaproveitamentoResponse buscarProcessoReaproveitamentoIsencaoDisciplinas() {
		List<ReaproveitamentoProcessosIsencaoDisciplinasVO> isencaoDeDisciplinas = getProcessosPendentesDeTransformaParaIsencao();
		DadosReaproveitamentoResponse response = new DadosReaproveitamentoResponse();
		response.setIsencaoDeDisciplinas(isencaoDeDisciplinas);
		return response;
	}

	private List<ReaproveitamentoProcessosIsencaoDisciplinasVO> getProcessosPendentesDeTransformaParaIsencao() {
		List<ReaproveitamentoProcessosIsencaoDisciplinasVO> isencaoDeDisciplinas = new ArrayList<>();

		ProcessoFiltro filtro = new ProcessoFiltro();
		filtro.setTipoOrdenacao(ProcessoFiltro.TipoOrdenacao.RECENTEMENTE_APROVADO);
		filtro.setTiposProcesso(Arrays.asList(new TipoProcesso(TipoProcesso.ISENCAO_DISCIPLINAS)));
		filtro.setStatusList(Arrays.asList(StatusProcesso.CONCLUIDO));
		filtro.setDesconsiderarSituacoesIds(Arrays.asList(Situacao.ISENCAO_DISCIPLINAS_ALUNO));
		List<Processo> list = processoService.findByFiltro(filtro, 0, 2);
		for (Processo processo : list) {
			String numInscricao = processo.getNumInscricao();
			String numCandidato = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUM_CANDIDATO);
			ConsultaInscricoesVO consultaInscricoesVO = consultaCandidatoService.getConsultaInscricoesVO(numInscricao, numCandidato);
			String matricula = consultaInscricoesVO.getCodMatricula();
			if(StringUtils.isBlank(matricula)) continue;
			Long processoId = processo.getId();
			Situacao situacao = processo.getSituacao();
			Long situacaoId = situacao.getId();

			List<ReaproveitamentoCampoVO> voCampoList = new ArrayList<>();
			CampoMap.CampoEnum campoEnumPreAnaliseFeitaFoi = CampoMap.CampoEnum.RESULTADO_REVISAO_ISENCAO;
			ReaproveitamentoCampoVO campoVoPreAnaliseFeitaFoi = getReaproveitamentoCampoVO(processo, campoEnumPreAnaliseFeitaFoi);
			if(campoVoPreAnaliseFeitaFoi != null) voCampoList.add(campoVoPreAnaliseFeitaFoi);

			CampoMap.CampoEnum campoEnumPreAnaliseResultado = CampoMap.CampoEnum.RESULTADO_PRE_ANALISE_ISENCAO_DISCIPLINA;
			ReaproveitamentoCampoVO campoVoPreAnaliseResultado = getReaproveitamentoCampoVO(processo, campoEnumPreAnaliseResultado);
			if(campoVoPreAnaliseResultado != null) voCampoList.add(campoVoPreAnaliseResultado);

			CampoMap.CampoEnum campoEnumPreAnaliseObservacao = CampoMap.CampoEnum.OBSERVACAO_PRE_ANALISE_ISENCAO_DISCIPLINA;
			ReaproveitamentoCampoVO campoVoPreAnaliseObservacao = getReaproveitamentoCampoVO(processo, campoEnumPreAnaliseObservacao);
			if(campoVoPreAnaliseObservacao != null) voCampoList.add(campoVoPreAnaliseObservacao);

			CampoMap.CampoEnum campoEnumIsencaoResultado = CampoMap.CampoEnum.RESULTADO_ISENCAO_DISCIPLINA;
			ReaproveitamentoCampoVO campoVoIsencaoResultado = getReaproveitamentoCampoVO(processo, campoEnumIsencaoResultado);
			if(campoVoIsencaoResultado != null) voCampoList.add(campoVoIsencaoResultado);

			CampoMap.CampoEnum campoEnumIsencaoObservacao = CampoMap.CampoEnum.OBSERVACAO_ISENCAO_DISCIPLINA;
			ReaproveitamentoCampoVO campoVoIsencaoObservacao = getReaproveitamentoCampoVO(processo, campoEnumIsencaoObservacao);
			if(campoVoIsencaoObservacao != null) voCampoList.add(campoVoIsencaoObservacao);

			CampoMap.CampoEnum campoEnumRevisaoAproveitamento = CampoMap.CampoEnum.DICIPLINAS_REVER_APROVEITAMENTO_REVISAO_ISENCAO_DISCIPLINA;
			ReaproveitamentoCampoVO campoVoRevisaoAproveitamento = getReaproveitamentoCampoVO(processo, campoEnumRevisaoAproveitamento);
			if(campoVoRevisaoAproveitamento != null) voCampoList.add(campoVoRevisaoAproveitamento);

			CampoMap.CampoEnum campoEnumRevisaoParecerAnalise = CampoMap.CampoEnum.PARECER_DA_ANALISE_REVISAO_ISENCAO_DISCIPLINA;
			ReaproveitamentoCampoVO campoVoRevisaoParecerAnalise = getReaproveitamentoCampoVO(processo, campoEnumRevisaoParecerAnalise);
			if(campoVoRevisaoParecerAnalise != null) voCampoList.add(campoVoRevisaoParecerAnalise);

			CampoMap.CampoEnum campoEnumRevisaoResultado = CampoMap.CampoEnum.RESULTADO_REVISAO_ISENCAO_DISCIPLINA;
			ReaproveitamentoCampoVO campoVoRevisaoResultado = getReaproveitamentoCampoVO(processo, campoEnumRevisaoResultado);
			if(campoVoRevisaoResultado != null) voCampoList.add(campoVoRevisaoResultado);

			CampoMap.CampoEnum campoEnumIesGrupo = CampoMap.CampoEnum.IES_DE_GRUPO;
			ReaproveitamentoCampoVO campoVoIesGrupo = getReaproveitamentoCampoVO(processo, campoEnumIesGrupo);
			if(campoVoIesGrupo != null) voCampoList.add(campoVoIesGrupo);

			CampoMap.CampoEnum campoEnumDisciplinasAprovadas = CampoMap.CampoEnum.DISCIPLINAS_APROVADA_ORIGEM;
			ReaproveitamentoCampoVO campoVoDisciplinasAprovadas = getReaproveitamentoCampoVO(processo, campoEnumDisciplinasAprovadas);
			if(campoVoDisciplinasAprovadas != null) voCampoList.add(campoVoDisciplinasAprovadas);

			CampoMap.CampoEnum campoEnumDisciplinasIsentas = CampoMap.CampoEnum.DISCIPLINAS_ISENTAS_ORIGEM;
			ReaproveitamentoCampoVO campoVoDisciplinasIsentas = getReaproveitamentoCampoVO(processo, campoEnumDisciplinasIsentas);
			if(campoVoDisciplinasIsentas != null) voCampoList.add(campoVoDisciplinasIsentas);

			CampoMap.CampoEnum campoEnumIsencaoConcedida = CampoMap.CampoEnum.ISENCOES_CONCEDIDAS;
			ReaproveitamentoCampoVO campoVoIsencaoConcedida = getReaproveitamentoCampoVO(processo, campoEnumIsencaoConcedida);
			if(campoVoIsencaoConcedida != null) voCampoList.add(campoVoIsencaoConcedida);

			CampoMap.CampoEnum campoEnumDisciplinasReprovadas = CampoMap.CampoEnum.DISCIPLINAS_REPROVADAS_ORIGEM;
			ReaproveitamentoCampoVO campoVoDisciplinasReprovadas = getReaproveitamentoCampoVO(processo, campoEnumDisciplinasReprovadas);
			if(campoVoDisciplinasReprovadas != null) voCampoList.add(campoVoDisciplinasReprovadas);

			CampoMap.CampoEnum campoEnumCurriculoAtual = CampoMap.CampoEnum.CURRICULO_ATUAL;
			ReaproveitamentoCampoVO campoVoCurriculoAtual = getReaproveitamentoCampoVO(processo, campoEnumCurriculoAtual);
			if(campoVoCurriculoAtual != null) voCampoList.add(campoVoCurriculoAtual);

			CampoMap.CampoEnum campoIesOrigem = CampoMap.CampoEnum.IES_DE_ORIGEM_REAPROVEITAR;
			ReaproveitamentoCampoVO campoVoIesOrigem = getReaproveitamentoCampoVO(processo, campoIesOrigem);
			if(campoVoIesOrigem != null) voCampoList.add(campoVoIesOrigem);

			CampoMap.CampoEnum campoEnumCursoOrigem = CampoMap.CampoEnum.CURSO_DE_ORIGEM_REAPROVEITAR;
			ReaproveitamentoCampoVO campoVoCursoOrigem = getReaproveitamentoCampoVO(processo, campoEnumCursoOrigem);
			if(campoVoCursoOrigem != null) voCampoList.add(campoVoCursoOrigem);

			ReaproveitamentoProcessosIsencaoDisciplinasVO vo = new ReaproveitamentoProcessosIsencaoDisciplinasVO();
			vo.setProcessoCaptacaoId(processoId);
			vo.setMatricula(matricula);
			vo.setCampos(voCampoList);
			vo.setSituacaoId(situacaoId);

			isencaoDeDisciplinas.add(vo);
		}

		return isencaoDeDisciplinas;
	}

	private ReaproveitamentoCampoVO getReaproveitamentoCampoVO(Processo processo, CampoMap.CampoEnum campoEnum) {
		String valor = DummyUtils.getCampoProcessoValor(processo, campoEnum);
		if(StringUtils.isNotBlank(valor)) {
			ReaproveitamentoCampoVO vo = new ReaproveitamentoCampoVO();
			vo.setGrupo(campoEnum.getGrupo().getNome());
			vo.setCampo(campoEnum.getNome());
			vo.setValor(valor);
			return vo;
		}
		return null;
	}

	public void notificarReaproveitamentoIsencaoDisciplinas(RequestNotificarReaproveitamento request) throws Exception {
		Long processoId = request.getProcessoId();
		Processo processo = processoService.get(processoId);
		Situacao situacao = situacaoService.get(Situacao.ISENCAO_DISCIPLINAS_ALUNO);
		processoService.concluir(processo, null, situacao);
	}

}
