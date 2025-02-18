package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.RelatorioGeralDataModel;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.RelatorioIsencaoDisciplinasVO;
import net.wasys.getdoc.domain.vo.RelatorioProdutividadeVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro.ConsiderarData;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeFiltro;
import net.wasys.getdoc.job.RelatorioGeralJob;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@ManagedBean
@ViewScoped
public class RelatorioIsencaoDisciplinasMedicinaBean extends AbstractBean {

	@Autowired private ApplicationContext applicationContext;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ParametroService parametroService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private RelatorioProdutividadeService relatorioProdutividadeService;
	@Autowired private RelatorioIsencaoDisciplinasService relatorioIsencaoDisciplinasService;
	@Autowired private RelatorioGeralService relatorioGeralService;

	public enum Tipo {
		RELATORIO_ANALISE_ISENCAO_ACOMPANHAMENTO,
		RELATORIO_ANALISE_ISENCAO_MEDICINA_PRODUTIVIDADE
	}

	private Long usuarioId;
	private RelatorioGeralDataModel ativosDataModel;
	private RelatorioIsencaoDisciplinasVO relatorioIsencaoDisciplinasVO;
	private List<RelatorioProdutividadeVO> relatorioProdutividadeVOS;
	private RelatorioGeralExporter exporter;

	private List<TipoProcesso> tiposProcessos;
	private RelatorioGeralFiltro filtro = new RelatorioGeralFiltro();
	private String dataUltimaExecucao;
	private Boolean executando;
	private List<Situacao> situacoes;
	private Tipo tipoRelatorioIsencao;
	private boolean agrupar = true;

	protected void initBean() {

		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcessos = tipoProcessoService.findAtivosAndInitialize(permissoes);

		dataUltimaExecucao = parametroService.getValor(ParametroService.P.ULTIMA_DATA_RELATORIO_GERAL);

		if(situacoes == null) {
			situacoes = situacaoService.findAtivas(null);
			Collections.sort(situacoes, new SuperBeanComparator<>("tipoProcesso.nome, nome"));
		}

		executando = TransactionWrapperJob.isExecutando(RelatorioGeralJob.class);

		Calendar cal = Calendar.getInstance();
		filtro.setDataFim(cal.getTime());
		cal.add(Calendar.MONTH, -3);
		filtro.setDataInicio(cal.getTime());
		filtro.setTipo(RelatorioGeralFiltro.Tipo.ISENCAO_DISCIPLINAS);
		filtro.setConsiderarData(ConsiderarData.CRIACAO);
		filtro.setSituacoesIds(Situacao.ISENCAO_DISCIPLINAS_MEDICINA_IDS);
		tipoRelatorioIsencao = Tipo.RELATORIO_ANALISE_ISENCAO_ACOMPANHAMENTO;

		ativosDataModel = new RelatorioGeralDataModel();
		ativosDataModel.setFiltro(filtro);
		ativosDataModel.setService(relatorioGeralService);
	}

	public void baixar() {

		Exception error = exporter.getError();
		if(error != null) {
			addMessageError(error);
		}
		else {
			File file = exporter.getFile();
			try {
				FileInputStream fis = new FileInputStream(file);
				String fileName = "relatorio-isencao-disciplinas.xlsx";
				String fileName2 = exporter.getFileName();
				if(StringUtils.isNotBlank(fileName2)) {
					fileName = fileName2;
				}
				Faces.sendFile(fis, fileName, true);
			}
			catch (Exception e1) {
				addMessageError(e1);
			}
		}

		exporter = null;
	}

	public void verificar() {

		if(exporter == null) {
			return;
		}

		if(exporter.isFinalizado()) {
			Ajax.data("terminou", true);
		}
		else {
			Ajax.data("terminou", false);
		}
	}

	public void exportar() {

		if(exporter == null) {
			exporter = applicationContext.getBean(RelatorioGeralExporter.class);
			exporter.setFiltro(filtro);
			Usuario usuarioLogado = getUsuarioLogado();
			exporter.setUsuario(usuarioLogado);
			exporter.start();
		}
	}

	public Long sum(Map<Date, Integer> dateIntegerMap){
		Long totalPeriodo = 0L;
		for(Date data : dateIntegerMap.keySet()){
			Integer integer = dateIntegerMap.get(data);
			totalPeriodo = totalPeriodo + integer;
		}
		return totalPeriodo;
	}

	public Long sum2(Map<String, Map<Date, Integer>> map, Date data){
		Long totalData = 0L;
		for(String situacao : map.keySet()){
			Map<Date, Integer> dateIntegerMap = map.get(situacao);
			Integer integer = dateIntegerMap.get(data);
			totalData = totalData + (integer == null ? 0 : integer);
		}
		return totalData;
	}

	public void buscar(){
		if(Tipo.RELATORIO_ANALISE_ISENCAO_ACOMPANHAMENTO.equals(tipoRelatorioIsencao)){
			relatorioIsencaoDisciplinasVO = relatorioIsencaoDisciplinasService.createRelatorioIsencaoDisciplinas(filtro, true);
		} else if (Tipo.RELATORIO_ANALISE_ISENCAO_MEDICINA_PRODUTIVIDADE.equals(tipoRelatorioIsencao)){
			RelatorioProdutividadeFiltro produtividadeFiltro = new RelatorioProdutividadeFiltro();
			produtividadeFiltro.setDataInicio(filtro.getDataInicio());
			produtividadeFiltro.setDataFim(filtro.getDataFim());
			produtividadeFiltro.setTipo(RelatorioProdutividadeFiltro.Tipo.VISUALIZACAO_POR_ANALISTA_MEDICINA);
			produtividadeFiltro.setAgrupar(agrupar);
			relatorioProdutividadeVOS = relatorioProdutividadeService.getRelatorioProdutividade(produtividadeFiltro);
		}
	}

	public RelatorioIsencaoDisciplinasVO getRelatorioIsencaoDisciplinasVO() {
		return relatorioIsencaoDisciplinasVO;
	}

	public void setRelatorioIsencaoDisciplinasVO(RelatorioIsencaoDisciplinasVO relatorioIsencaoDisciplinasVO) {
		this.relatorioIsencaoDisciplinasVO = relatorioIsencaoDisciplinasVO;
	}

	public RelatorioGeralDataModel getAtivosDataModel() {
		return ativosDataModel;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public RelatorioGeralFiltro getFiltro() {
		return filtro;
	}

	public ConsiderarData[] getConsiderarDatas() {
		return ConsiderarData.values();
	}

	public void setFiltro(RelatorioGeralFiltro filtro) {
		this.filtro = filtro;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public RelatorioGeralExporter getExporter() {
		return exporter;
	}

	public String getDataUltimaExecucao() {
		return dataUltimaExecucao;
	}

	public Boolean getExecutando() {
		return executando;
	}

	public List<Situacao> getSituacoes() { return situacoes; }

	public void setSituacoes(List<Situacao> situacoes) { this.situacoes = situacoes; }

	public Tipo getTipoRelatorioIsencao() {
		return tipoRelatorioIsencao;
	}

	public void setTipoRelatorioIsencao(Tipo tipoRelatorioIsencao) {
		this.tipoRelatorioIsencao = tipoRelatorioIsencao;
	}

	public boolean isAgrupar() {
		return agrupar;
	}

	public void setAgrupar(boolean agrupar) {
		this.agrupar = agrupar;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeVOS() {
		return relatorioProdutividadeVOS;
	}
}
