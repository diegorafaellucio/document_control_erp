package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.repository.DashboardDiarioRepository;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO.Pizza;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO.SituacaoPorHora;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO.TempoOperacao;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.DashboardDiarioFiltro;
import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardDiarioService {

	public enum DashboardDiarioEnum {INSCRITO, EM_CONFERENCIA, CONFERIDO, APROVADO}

	@Autowired private DashboardDiarioRepository dashboardDiarioRepository;
	@Autowired private BaseRegistroService baseRegistroService;

	public DashboardDiarioVO buildVO(DashboardDiarioFiltro filtro) {

		SituacaoPorHora inscritoPorHora = dashboardDiarioRepository.getSituacaoPorHora(filtro, DashboardDiarioEnum.INSCRITO, 0, 23);
		SituacaoPorHora digitalizadoPorHora = dashboardDiarioRepository.getSituacaoPorHora(filtro, DashboardDiarioEnum.EM_CONFERENCIA, 0 , 23);
		SituacaoPorHora pendentePorHora = dashboardDiarioRepository.getPendentePorHora(filtro, 6 ,18);
		SituacaoPorHora aprovadoPorHora = dashboardDiarioRepository.getSituacaoPorHora(filtro, DashboardDiarioEnum.APROVADO, 6 ,18);
		TempoOperacao tempoOperacao = makeTempoOperacao(filtro);
		List<Pizza> inscritoTipoProcesso = dashboardDiarioRepository.findPizzaTiposProcessoByStatus(filtro, DashboardDiarioEnum.INSCRITO);
		List<Pizza> digitalizadoTipoProcesso = dashboardDiarioRepository.findPizzaTiposProcessoByStatus(filtro, DashboardDiarioEnum.EM_CONFERENCIA);
		List<Pizza> conferidoTipoProcesso = dashboardDiarioRepository.findPizzaTiposProcessoByStatus(filtro, DashboardDiarioEnum.CONFERIDO);
		List<Pizza> pendenciaPorDocumento = dashboardDiarioRepository.findPendenciaPorDocumento(filtro);
		List<Pizza> pendenciaPorIrregularidade = dashboardDiarioRepository.findPendenciaPorIrregularidade(filtro);

		DashboardDiarioVO dashboardDiarioVO = new DashboardDiarioVO();
		dashboardDiarioVO.setInscritoPorHora(inscritoPorHora);
		dashboardDiarioVO.setEmConferenciaPorHora(digitalizadoPorHora);
		dashboardDiarioVO.setPendentePorHora(pendentePorHora);
		dashboardDiarioVO.setAprovadoPorHora(aprovadoPorHora);
		dashboardDiarioVO.setTempoOperacao(tempoOperacao);
		dashboardDiarioVO.setInscritoTipoProcesso(inscritoTipoProcesso);
		dashboardDiarioVO.setDigitalizadoTipoProcesso(digitalizadoTipoProcesso);
		dashboardDiarioVO.setConferidoTipoProcesso(conferidoTipoProcesso);
		dashboardDiarioVO.setPendenciaPorDocumento(pendenciaPorDocumento);
		dashboardDiarioVO.setPendenciaPorIrregularidade(pendenciaPorIrregularidade);

		return dashboardDiarioVO;
	}

	private TempoOperacao makeTempoOperacao(DashboardDiarioFiltro filtro){
		List<Double> digitalizado = dashboardDiarioRepository.findTempoMedioBySituacao(filtro, StatusProcesso.RASCUNHO);
		List<Double> conferido = dashboardDiarioRepository.findTempoMedioBySituacao(filtro, StatusProcesso.EM_ANALISE);

		TempoOperacao tempoOperacao = new TempoOperacao();
		tempoOperacao.setDigitalizado(digitalizado);
		tempoOperacao.setConferido(conferido);

		return tempoOperacao;
	}

	public List<RegistroValorVO> findRegistroValorVOCombos(String chaveUnicidade, Long baseInternaID, String colunaPesquisa ){
		chaveUnicidade = DummyUtils.limparCharsChaveUnicidade(chaveUnicidade);

		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setBaseInterna(new BaseInterna(baseInternaID));
		filtro.addCampoFiltro(colunaPesquisa, chaveUnicidade);

		return baseRegistroService.findByFiltro(filtro, null, null);
	}
}
