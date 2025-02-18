package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.ReaproveitamentoCampanhaVO;
import net.wasys.getdoc.domain.vo.ReaproveitamentoDocumentoVO;
import net.wasys.getdoc.domain.vo.ReaproveitamentoMembroFamiliarVO;
import net.wasys.getdoc.domain.vo.ReaproveitamentoProcessoVO;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.apache.commons.beanutils.BeanUtils.copyProperties;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Service
public class ReaproveitamentoService {

	@Autowired private ReaproveitarDocumentoService reaproveitarDocumentoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ProcessoService processoService;
	@Autowired private AlunoService alunoService;
	@Autowired private BaseRegistroService baseRegistroService;

	public Processo buscarProcesso(String numCandidato, String numInscricao, String cpf) {

		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		DummyUtils.addParameter(logAcesso, "RS-numCandidato", numCandidato);
		DummyUtils.addParameter(logAcesso, "RS-numInscricao", numInscricao);
		DummyUtils.addParameter(logAcesso, "RS-cpf", cpf);

		List<Processo> processosFiltrados = new ArrayList<>(0);

		if (isNotBlank(cpf)) {
			ProcessoFiltro filtro = new ProcessoFiltro();

			cpf = DummyUtils.getCpf(cpf);
			Aluno aluno;
			AlunoFiltro alunoFiltro = new AlunoFiltro();
			alunoFiltro.setCpf(cpf);
			alunoFiltro.setOrdenar("aluno.id", SortOrder.DESCENDING);
			List<Aluno> alunoList = alunoService.findByFiltro(alunoFiltro, 0, 1);
			if (!alunoList.isEmpty()) {
				aluno = alunoList.get(0);

				DummyUtils.addParameter(logAcesso, "RS-aluno", aluno.getId());

				filtro.setAluno(aluno);
				filtro.setOrdenar("processo.id", SortOrder.DESCENDING);
//				filtro.setDesconsiderarTipoProcessoIds(Arrays.asList(TipoProcesso.ISENCAO_DISCIPLINAS));
				filtro.setDesconsiderarStatusProcesso(Arrays.asList(StatusProcesso.CANCELADO));
				filtro.setTiposProcesso(Arrays.asList(new TipoProcesso(TipoProcesso.SIS_PROUNI), new TipoProcesso(TipoProcesso.SIS_FIES)));

				filtro.setFetch(Arrays.asList(ProcessoFiltro.Fetch.CAMPANHA, ProcessoFiltro.Fetch.SITUACAO, ProcessoFiltro.Fetch.TIPO_PROCESSO));

				List<Processo> processosFiltradosFinanciamento = processoService.findByFiltro(filtro, null, null);
				DummyUtils.addParameter(logAcesso, "RS-processosFiltradosFinanciamento", processosFiltradosFinanciamento.size());

				for (Processo processosFiltrado : processosFiltradosFinanciamento) {
					String numCandidatoProcesso = processosFiltrado.getNumCandidato();
					StatusProcesso status = processosFiltrado.getStatus();
					if(isNotBlank(numCandidatoProcesso)) {
						if(numCandidato.equals(numCandidatoProcesso) && StatusProcesso.CONCLUIDO.equals(status)) {
							processosFiltrados = Arrays.asList(processosFiltrado);
							break;
						} else if (numCandidato.equals(numCandidatoProcesso)){
							processosFiltrados = Arrays.asList(processosFiltrado);
						}
					} else {
						processosFiltrados = Arrays.asList(processosFiltrado);
					}
				}
			}
		}

		DummyUtils.addParameter(logAcesso, "RS-processosFiltrados", processosFiltrados.size());

		if (processosFiltrados.isEmpty() && (isNotBlank(numCandidato) || isNotBlank(numInscricao))) {

			ProcessoFiltro filtro = new ProcessoFiltro();
			filtro.setOrdenar("processo.id", SortOrder.DESCENDING);
			filtro.setDesconsiderarStatusProcesso(Arrays.asList(StatusProcesso.CANCELADO));
			filtro.setDesconsiderarTipoProcessoIds(Arrays.asList(TipoProcesso.ISENCAO_DISCIPLINAS, TipoProcesso.SIS_PROUNI, TipoProcesso.SIS_FIES));

			if (isNotBlank(numCandidato)) {
				filtro.setCamposFiltro(CampoMap.CampoEnum.NUM_CANDIDATO, singletonList(numCandidato));
			}

			if (isNotBlank(numInscricao)) {
				filtro.setCamposFiltro(CampoMap.CampoEnum.NUM_INSCRICAO, singletonList(numInscricao));
			}

			filtro.setFetch(Arrays.asList(ProcessoFiltro.Fetch.CAMPANHA, ProcessoFiltro.Fetch.SITUACAO, ProcessoFiltro.Fetch.TIPO_PROCESSO));

			processosFiltrados = processoService.findByFiltro(filtro, null, null);

			DummyUtils.addParameter(logAcesso, "RS-processosFiltrados2", processosFiltrados.size());
		}

		return processosFiltrados.isEmpty() ? null : processosFiltrados.get(0);
	}

	public ReaproveitamentoProcessoVO buscarDadosProcesso(Processo processo) throws InvocationTargetException, IllegalAccessException {

		ReaproveitamentoCampanhaVO dadosCampanha = getReaproveitamentoCampanhaVO(processo);
		Long situacaoAntesDoBloqueioId = getSituacaoIdAntesDoBloqueio(processo);
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		ReaproveitamentoProcessoVO vo = new ReaproveitamentoProcessoVO();
		vo.setDadosCampanha(dadosCampanha);
		//vo.setUsaTermo(processo.getUsaTermo());
		vo.setSituacaoId(situacaoAntesDoBloqueioId);
		vo.setFies(false);
		vo.setProuni(false);

		if(tipoProcessoId.equals(TipoProcesso.SIS_FIES) || tipoProcessoId.equals(TipoProcesso.TE_FIES)){
			vo.setFies(true);
		}

		if(tipoProcessoId.equals(TipoProcesso.SIS_PROUNI) || tipoProcessoId.equals(TipoProcesso.TE_PROUNI)){
			vo.setProuni(true);
		}

		return vo;
	}

	private ReaproveitamentoCampanhaVO getReaproveitamentoCampanhaVO(Processo processo) throws IllegalAccessException, InvocationTargetException {

		Campanha campanha = processo.getCampanha();

		ReaproveitamentoCampanhaVO dadosCampanha = null;
		if (campanha != null) {

			dadosCampanha = new ReaproveitamentoCampanhaVO();
			try {
				copyProperties(dadosCampanha, campanha);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
				throw e;
			}
		}

		return dadosCampanha;
	}

	public Long getSituacaoIdAntesDoBloqueio(Processo processo) {

		Situacao situacaoAntesDoBloqueio = null;

		Situacao situacao = processo.getSituacao();
		if (isSituacaoAluno(situacao)) {
			situacaoAntesDoBloqueio = buscarSituacaoAntesDoBloqueio(processo, processo.getSituacao(), null);
		}

		if (situacaoAntesDoBloqueio == null) {
			situacaoAntesDoBloqueio = situacao;
		}

		return situacaoAntesDoBloqueio.getId();
	}

	private Situacao buscarSituacaoAntesDoBloqueio(Processo processo, Situacao situacaoAtual, ProcessoLog processoLogAtual) {

		ProcessoLog processoLogAnterior = processoLogService.findSituacaoAnterior(processo, situacaoAtual, processoLogAtual);
		if (processoLogAnterior == null) return null;

		Situacao situacaoAnterior = processoLogAnterior.getSituacaoAnterior();

		if (isSituacaoAluno(situacaoAnterior)) {
			situacaoAnterior = buscarSituacaoAntesDoBloqueio(processo, situacaoAnterior, processoLogAnterior);
		}

		return situacaoAnterior;
	}

	public boolean isSituacaoAluno(Situacao situacao) {

		String nome = situacao.getNome();
		return StringUtils.endsWithIgnoreCase(nome, Situacao.ALUNO);
	}

	public List<ReaproveitamentoDocumentoVO> buscarDocumentos(Processo processo, boolean ignorarArquivos) throws IOException {
		List<ReaproveitamentoDocumentoVO> vos = reaproveitarDocumentoService.buscarDocumentosReaproveitaveis(processo, ignorarArquivos);
		return vos;
	}

	@Transactional
	public List<ReaproveitamentoMembroFamiliarVO> buscarMembrosFamiliares(Processo processo) {

		List<ReaproveitamentoMembroFamiliarVO> vos = new ArrayList<>();

		Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
		for (CampoGrupo campoGrupo : gruposCampos) {

			String nomeGrupo = campoGrupo.getNome();
			if (StringUtils.containsIgnoreCase(nomeGrupo, CampoMap.GrupoEnum.MEMBRO_FAMILIAR.getNome())) {

				ReaproveitamentoMembroFamiliarVO membroFamiliarVO = new ReaproveitamentoMembroFamiliarVO();
				membroFamiliarVO.setNomeGrupo(nomeGrupo);

				Set<Campo> campos = campoGrupo.getCampos();
				for (Campo campo : campos) {

					String nomeCampo = campo.getNome();
					String campoValor = campo.getValor();
					if (CampoMap.CampoEnum.NOME_MEMBRO_FAMILIAR.getNome().equals(nomeCampo)) {
						membroFamiliarVO.setNome(campoValor);
					}
					else if (CampoMap.CampoEnum.DATA_NASCIMENTO.getNome().equals(nomeCampo)) {
						membroFamiliarVO.setDataNascimento(campoValor);
					}
					else if (CampoMap.CampoEnum.PARENTESCO.getNome().equals(nomeCampo)) {
						membroFamiliarVO.setParentesco(campoValor);
					}
					else if (CampoMap.CampoEnum.RENDA.getNome().equals(nomeCampo)) {
						membroFamiliarVO.setRenda(campoValor);
					}
				}

				vos.add(membroFamiliarVO);
			}
		}

		return vos;
	}
}
