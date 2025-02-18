package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.RelatorioGeralCampos;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.webservice.sia.SiaService;
import net.wasys.getdoc.domain.vo.RelatorioIsencaoDisciplinasVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.excel.ExcelWriter;
import net.wasys.util.other.ExcelCsvWriter;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.*;

@Service
public class RelatorioIsencaoDisciplinasService {

	public static final String PROBLEMA_DOC = "Problema na Documentação";

	@Autowired private ProcessoService processoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private SiaService siaService;


	public RelatorioIsencaoDisciplinasVO createRelatorioIsencaoDisciplinas(RelatorioGeralFiltro filtro, boolean isMedicina){

		RelatorioIsencaoDisciplinasVO vo = new RelatorioIsencaoDisciplinasVO();
		List<Date> datas = new ArrayList<>();
		Map<String, RelatorioIsencaoDisciplinasVO.SituacaoPorDia> map = new HashMap<>();

		ProcessoFiltro filtro2 = new ProcessoFiltro();
		filtro2.setDataInicio(filtro.getDataInicio());
		filtro2.setDataFim(filtro.getDataFim());
		if(RelatorioGeralFiltro.ConsiderarData.CRIACAO.equals(filtro.getConsiderarData())){
			filtro2.setConsiderarData(ProcessoFiltro.ConsiderarData.CRIACAO);
		}
		else if (RelatorioGeralFiltro.ConsiderarData.ENVIO_ANALISE.equals(filtro.getConsiderarData())){
			filtro2.setConsiderarData(ProcessoFiltro.ConsiderarData.ENVIO_ANALISE);
		}
		else {
			filtro2.setConsiderarData(ProcessoFiltro.ConsiderarData.FINALIZACAO);
		}

		List<Situacao> situacoes = new ArrayList<>();
		if(isMedicina) {
			Situacao.ISENCAO_DISCIPLINAS_MEDICINA_IDS.forEach(Long -> {
				Situacao situacao = new Situacao(Long);
				situacoes.add(situacao);
			});
		} else {
			Situacao.ANALISE_ISENCAO_IDS.forEach(Long -> {
				Situacao situacao = new Situacao(Long);
				situacoes.add(situacao);
			});
		}
		filtro2.setSituacao(situacoes);
		filtro2.setStatusList(StatusProcesso.getStatusEmAndamento());
		filtro2.setCamposFiltro(filtro.getCamposFiltro());

		List<Long> ids = processoService.findIdsByFiltro(filtro2, null, null);
		List<Processo> processos = processoService.findByIds(ids);

		buildSituacaoPorDia(datas, map, processos);
		vo.setAreaSituacaoPorDia(map);

		findTotalPeriodo(map);

		Map<Date, Integer> dateIntegerMap = findTotalPorData(datas);

		vo.setTotalPorData(dateIntegerMap);

		datas = new ArrayList<>(new LinkedHashSet<>(datas));
		Collections.sort(datas);

		vo.setDatas(datas);

		return  vo;
	}

	private void buildSituacaoPorDia(List<Date> datas, Map<String, RelatorioIsencaoDisciplinasVO.SituacaoPorDia> map, List<Processo> processos) {

		for(Processo processo : processos){
			Situacao situacao = processo.getSituacao();
			String nome = situacao.getNome();
			Date prazoLimiteEmAcompanhamento = processo.getPrazoLimiteEmAcompanhamento();
			datas.add(prazoLimiteEmAcompanhamento);

			String numCurso = DummyUtils.limparCharsChaveUnicidade(DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CURSO));
			String area = DummyUtils.limparCharsChaveUnicidade(siaService.getRelacional(BaseInterna.AREA_ID, numCurso, TipoCampo.COD_CURSO, TipoCampo.NOM_AREA));
			area = area.equals("null") ? "vazio" : area;

			RelatorioIsencaoDisciplinasVO.SituacaoPorDia situacaoPorDia = map.get(area);
			if(situacaoPorDia == null){
				RelatorioIsencaoDisciplinasVO.SituacaoPorDia rs = new RelatorioIsencaoDisciplinasVO.SituacaoPorDia();
				Map<String, Map<Date, Integer>> situacaoQtdPorDia = new HashMap<>();
				Map<Date, Integer> qtdPorDia = new HashMap<>();
				qtdPorDia.put(prazoLimiteEmAcompanhamento, 1);
				situacaoQtdPorDia.put(nome, qtdPorDia);
				rs.setQtdPorDia(situacaoQtdPorDia);
				map.put(area, rs);
			}
			else{
				Map<String, Map<Date, Integer>> qtdPorDia = situacaoPorDia.getQtdPorDia();
				Map<Date, Integer> dateIntegerMap = qtdPorDia.get(nome);
				if(dateIntegerMap != null){
					Integer integer = dateIntegerMap.get(prazoLimiteEmAcompanhamento);
					if(integer == null) {
						dateIntegerMap.put(prazoLimiteEmAcompanhamento, 1);
					}
					else {
						integer++;
						dateIntegerMap.put(prazoLimiteEmAcompanhamento, integer);
					}
					qtdPorDia.put(nome, dateIntegerMap);
				}
				else {
					Map<Date, Integer> qtdPorDia2 = new HashMap<>();
					qtdPorDia2.put(prazoLimiteEmAcompanhamento, 1);
					qtdPorDia.put(nome, qtdPorDia2);
				}
			}
		}
	}

	private Map<Date, Integer> findTotalPorData(List<Date> datas) {

		Map<Date, Integer> dateIntegerMap = new HashMap<>();
		for(Date data : datas){
			Integer integer = dateIntegerMap.get(data);
			if(integer != null){
				integer = integer + 1;
				dateIntegerMap.put(data, integer);
			}
			else {
				dateIntegerMap.put(data, 1);
			}
		}
		return dateIntegerMap;
	}

	private void findTotalPeriodo(Map<String, RelatorioIsencaoDisciplinasVO.SituacaoPorDia> map) {

		Set<String> areas = map.keySet();
		long valorTotal = 0;
		for (String areasStr : areas) {
			RelatorioIsencaoDisciplinasVO.SituacaoPorDia situacaoPorDia = map.get(areasStr);
			Map<String, Map<Date, Integer>> qtdPorDia = situacaoPorDia.getQtdPorDia();
			Set<String> situacoes = qtdPorDia.keySet();
			for (String situacao : situacoes) {
				Map<Date, Integer> dateIntegerMap = qtdPorDia.get(situacao);
				for (Date data : dateIntegerMap.keySet()) {
					Integer integer = dateIntegerMap.get(data);
					valorTotal = valorTotal + integer;
				}
			}
			situacaoPorDia.setTotalPeriodo(valorTotal);
			valorTotal = 0;
		}
	}

	public LinkedMultiValueMap<String, String> renderCabecalho(ExcelCsvWriter ew) {

		ew.escrever(RelatorioGeralCampos.ColunasEnum.ID.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PROCESSO_ID.getNome(), 3000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.MOTIVO_DA_REQUISICAO.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.NOME.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.NOME_SOCIAL.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.CPF.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PASSAPORTE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.IDENTIDADE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.ORGAO_EMISSOR.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_EMISSAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.MAE.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PAI.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_DE_CRIACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_DE_ENVIO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_EM_ANALISE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ANALISE_H.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ANALISE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FIM_ANALISE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_ACOMPANHAMENTO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_ACOMPANHAMENTO.getNome() + " - " + RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ACOMPANHAMENTO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ACOMPANHAMENTO.getNome() + " - " + RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FIM_EM_ACOMPANHAMENTO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FIM_EM_ACOMPANHAMENTO.getNome() + " - " + RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_ULTIMA_TRATATIVA.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FINALIZACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO.getNome(), 8000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.STATUS.getNome(), 5000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PRIORIDADE.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR_LOGIN.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.AUTOR_PERFIL.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.ANALISTA.getNome(), 6000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.ANALISTA_LOGIN.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_RASCUNHO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_AGUARD_AN.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.VEZES_AGUARD_AN.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_PENDENTE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.VEZES_PENDENTE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_EM_ACOMPANHAMENTO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.VEZES_EM_ACOMPANHAMENTO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_EM_ANALISE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.VEZES_EM_ANALISE.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_ATE_FIN_AN.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_ATE_FIN.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_SLA_CRIACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_SLA_CRIACAO_DIAS.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_SLA_TRATATIVA.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_SLA_TRATATIVA_DIAS.getNome(), 4000);

		LinkedMultiValueMap<String, String> ordenados = tipoCampoService.findListByGrupoAndNome(TipoProcesso.ANALISE_ISENCAO_IDS);
		for (String grupo : ordenados.keySet()) {
			List<String> campos = ordenados.get(grupo);
			for (String campo : campos) {
				ew.escrever(grupo + " - " + campo, 8000);
				if (RelatorioGeralCampos.CAMPOS_COD.contains(campo.toUpperCase())) {
					ew.escrever(grupo + " - " + "Cod. " + campo, 4000);
				}
				if (CampoMap.CampoEnum.CAMPUS.getNome().equals(campo.toUpperCase())) {
					ew.escrever(grupo + " - " + "Polo Parceiro", 4000);
				}
			}
		}

		ew.escrever(RelatorioGeralCampos.ColunasEnum.STATUS_SITUACAO.getNome(), 3000);
		ew.escrever("Relatório Situação - " + RelatorioGeralCampos.ColunasEnum.SITUACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.SITUACAO_ANTERIOR.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_TOTAL.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_SITUACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.HORA_SITUACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.DATA_FIM_SITUACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.HORA_FIM_SITUACAO.getNome(), 4000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_TRATATIVA.getNome(), 3000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.INICIO_ETAPA.getNome(), 5000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.FIM_ETAPA.getNome(), 5000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.PRAZO_ANALISE_ETAPA.getNome(), 5000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.HHMM.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.TEMPO_TOTAL_ETAPA.getNome(), 3000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.CONTAGEM_ETAPA_CONLUIDA.getNome(), 3000);
		ew.escrever(RelatorioGeralCampos.ColunasEnum.ANALISTA_ANALISE_ISENCAO.getNome());
		ew.escrever(RelatorioGeralCampos.ColunasEnum.SUBPERFIL_ATIVO.getNome(), 3000);
		return ordenados;
	}
}
