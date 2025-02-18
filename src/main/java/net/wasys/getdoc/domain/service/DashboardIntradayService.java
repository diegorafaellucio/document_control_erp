package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.repository.DashboardIntradayRepository;
import net.wasys.getdoc.domain.vo.DashboardIntradayVO.TipoProcessoPorDia;
import net.wasys.getdoc.domain.vo.DashboardIntradayVO.TempoOperacao;
import net.wasys.getdoc.domain.vo.DashboardIntradayVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.DashboardDiarioFiltro;
import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DashboardIntradayService {

	@Autowired private DashboardIntradayRepository dashboardIntradayRepository;
	@Autowired private BaseRegistroService baseRegistroService;

	public DashboardIntradayVO buildVO(DashboardDiarioFiltro filtro) {

		TipoProcessoPorDia tipoProcessoPorDia = dashboardIntradayRepository.getTipoProcessoPorDia(filtro, 1 ,31);
		filtro.setTratados(true);
		TipoProcessoPorDia tipoProcessoPorDiaTratado = dashboardIntradayRepository.getTipoProcessoPorDia(filtro, 1 ,31);
		TempoOperacao tempoOperacao = makeTempoOperacao(filtro);
		List<Long> produtividadePorDia = dashboardIntradayRepository.getProdutividadePorDia(filtro);

		DashboardIntradayVO dashboardIntradayVO = new DashboardIntradayVO();
		dashboardIntradayVO.setTipoProcessoPorDia(tipoProcessoPorDia);
		dashboardIntradayVO.setTempoOperacao(tempoOperacao);
		dashboardIntradayVO.setTipoProcessoPorDiaTratado(tipoProcessoPorDiaTratado);
		dashboardIntradayVO.setProdutividadePorDia(produtividadePorDia);

		return dashboardIntradayVO;
	}

	private TempoOperacao makeTempoOperacao(DashboardDiarioFiltro filtro){
		List<Double> conferido = dashboardIntradayRepository.findTempoMedioBySituacao(filtro, StatusProcesso.EM_ANALISE);

		TempoOperacao tempoOperacao = new TempoOperacao();
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

	public List<Long> calculaTotal(TipoProcessoPorDia tipoProcessoPorDia) {
		List<Long> conferidoPorDiaQtd = new ArrayList<>();
		Map<String, List<Long>> qtdPorDia = tipoProcessoPorDia.getQtdPorDia();
		Set<String> tipoProcesso = qtdPorDia.keySet();
		for (String nome : tipoProcesso) {
			List<Long> longList = qtdPorDia.get(nome);
			for(int i = 1; i < longList.size(); i++) {
				Long valorParaSomar = longList.get(i);
				if(conferidoPorDiaQtd.size() == 31) {
					Long valorSomado =  conferidoPorDiaQtd.get(i-1);
					valorSomado += valorParaSomar;
					conferidoPorDiaQtd.set(i-1, valorSomado);
				} else{
					conferidoPorDiaQtd.add(i-1, valorParaSomar);
				}
			}
		}
		return conferidoPorDiaQtd;
	}
}
