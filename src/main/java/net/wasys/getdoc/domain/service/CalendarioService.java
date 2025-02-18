package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.CalendarioRepository;
import net.wasys.getdoc.domain.vo.filtro.CalendarioCriterioFiltro;
import net.wasys.getdoc.domain.vo.filtro.CalendarioFiltro;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class CalendarioService {


	@Autowired private CalendarioRepository calendarioRepository;
	@Autowired private CalendarioCriterioService calendarioCriterioService;
	@Autowired private CalendarioCriterioSituacaoService calendarioCriterioSituacaoService;
	@Autowired private DocumentoService documentoService;
	@Autowired private LogAlteracaoService logAlteracaoService;

	public Calendario get(Long id) {
		return calendarioRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Calendario calendario, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(calendario);

		try {
			calendarioRepository.saveOrUpdate(calendario);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(calendario, usuario, tipoAlteracao);
	}

	public List<Calendario> findAll(Integer inicio, Integer max) {
		return calendarioRepository.findAll(inicio, max);
	}

	public int count() {
		return calendarioRepository.count();
	}

	public Integer countByFiltro(CalendarioFiltro filtro) {
		return calendarioRepository.countByFiltro(filtro);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long calendarioId, Usuario usuario) throws MessageKeyException {

		Calendario calendario = get(calendarioId);

		logAlteracaoService.registrarAlteracao(calendario, usuario, TipoAlteracao.EXCLUSAO);

		try {
			calendarioRepository.deleteById(calendarioId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<Calendario> findByFiltro(CalendarioFiltro filtro) {
		return calendarioRepository.findByFiltro(filtro);
	}

	public Calendario getByFiltro(CalendarioFiltro filtro) {
		return calendarioRepository.getByFiltro(filtro);
	}

	public Boolean podeReceberDocumentos(Processo processo) {

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		String periodoIngresso;
		String resultadoProuni;
		TipoParceiro tipoParceiro;
		TipoProuni tipoProuni;
		ListaChamada listaChamada = null;
		if(TipoProcesso.TE_PROUNI.equals(tipoProcessoId)) {
			periodoIngresso = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.PERIODO_DE_INGRESSO);
			tipoParceiro = TipoParceiro.NULO;
			tipoProuni = TipoProuni.NULO;
		} else {
			periodoIngresso = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.PERIODO_DE_INGRESSO_IMPORTACAO);
			String poloParceiro = StringUtils.upperCase(DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.POLO_PARCEIRO));
			tipoParceiro = TipoParceiro.getByEhPoloParceiro(poloParceiro);
			String chamada = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NUMERO_CHAMADA);
			listaChamada = ListaChamada.getByChamada(chamada);
			String tipoProuniCampo = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.TIPO_PROUNI);
			tipoProuni = TipoProuni.getByTipoPruno(tipoProuniCampo);
		}
		periodoIngresso = DummyUtils.formatStringToRegex(periodoIngresso, "\\d{4}\\.\\d");

		if(tipoProuni == null || listaChamada == null || tipoParceiro == null){
			return true;
		}

		CalendarioFiltro calendarioFiltro = new CalendarioFiltro();
		calendarioFiltro.setDataAtual();
		calendarioFiltro.setAtivo(true);
		calendarioFiltro.setPeriodoIngresso(periodoIngresso);
		calendarioFiltro.setTipoParceiro(tipoParceiro);
		calendarioFiltro.setTipoProuni(tipoProuni);
		calendarioFiltro.setTipoProcessoId(tipoProcessoId);
		Calendario calendario = getByFiltro(calendarioFiltro);

		Situacao situacao = processo.getSituacao();
		if(calendario != null) {
			situacao.setBloqueioDeDigitalizacaoPorCalendario(true);
			DocumentoFiltro filtro = new DocumentoFiltro();
			filtro.setProcesso(processo);
			filtro.setTipoDocumentoIdList(Arrays.asList(TipoDocumento.TERMO_DE_PENDENCIA_SISPROUNI_ID, TipoDocumento.CHECKLIST_ID, TipoDocumento.FOTOCOPIA_DA_CARTEIRA_DE_IDENTIDADE_SISPROUNI_ID, TipoDocumento.FOTOCOPIA_CPF_SISPROUNI_ID));
			filtro.setStatusDocumentoList(Arrays.asList(StatusDocumento.APROVADO));
			int termoPendenciaAprovado = documentoService.countByFiltro(filtro);
			int quantidadeDeDocumentosAprovadosFiltrados = 3;
			if(termoPendenciaAprovado == quantidadeDeDocumentosAprovadosFiltrados) return true;

			resultadoProuni = StringUtils.upperCase(DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.RESULTADO_SISPROUNI));
			if(resultadoProuni != null && resultadoProuni.equals("APROVADO - TCB")) return true;

			Long id = calendario.getId();
			CalendarioCriterioFiltro calendarioCriterioFiltro = new CalendarioCriterioFiltro();
			calendarioCriterioFiltro.setDataAtual();
			calendarioCriterioFiltro.setCalendarioId(id);
			calendarioCriterioFiltro.setChamada(listaChamada);
			calendarioCriterioFiltro.setAtivo(true);
			calendarioCriterioFiltro.setTipoCalendario(TipoCalendario.RECEBER_DOCUMENTOS);
			CalendarioCriterio calendarioCriterio = calendarioCriterioService.getByFiltro(calendarioCriterioFiltro);

			List<Situacao> listSituacaoPermitido = calendarioCriterio != null ? calendarioCriterioSituacaoService.findSituacaoByCalendarioCriterio(calendarioCriterio) : null;
			if(CollectionUtils.isNotEmpty(listSituacaoPermitido)) {
				for (Situacao situacaoPermitido : listSituacaoPermitido) {
					if(situacaoPermitido.equals(situacao)) {
						return true;
					}
				}
				return false;
			}

			return calendarioCriterio != null;
		}

		return situacao.isPermiteEditarDocumentos();
	}
}
