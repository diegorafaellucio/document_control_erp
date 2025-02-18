package net.wasys.getdoc.bean;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.util.other.SuperBeanComparator;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.service.RelatorioProdutividadeService;
import net.wasys.getdoc.domain.vo.RelatorioProdutividadeVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeFiltro;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@SessionScoped
public class RelatorioProdutividadeBean extends AbstractBean {

	@Autowired private RelatorioProdutividadeService relatorioProdutividadeService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private RelatorioProdutividadeFiltro filtro = new RelatorioProdutividadeFiltro();

	private List<RelatorioProdutividadeVO> list;
	private List<Situacao> situacoes;
	private List<TipoProcesso> tiposProcesso;

	protected void initBean() {

		Date date = new Date();
		filtro.setDataInicio(date);
		filtro.setDataFim(date);
		filtro.setTipo(RelatorioProdutividadeFiltro.Tipo.VISUALIZACAO_POR_ANALISTA);
		filtro.setAgrupar(true);
		tiposProcesso = tipoProcessoService.findAtivos(null);
	}

	public void listarSituacoes(){
		TipoProcesso tipoProcesso = filtro.getTipoProcesso();
		if(tipoProcesso != null){
			Long tipoProcessoId = tipoProcesso.getId();
			situacoes = situacaoService.findAtivas(null, tipoProcessoId);
		}else{
			situacoes = situacaoService.findAtivas(null);
		}
		Collections.sort(situacoes, new SuperBeanComparator<>("tipoProcesso.nome, nome"));
	}

	public void buscar() {
		list = relatorioProdutividadeService.getRelatorioProdutividade(filtro);
	}

	public void exportar() {

		File file = relatorioProdutividadeService.render(filtro);

		try {
			FileInputStream fis = new FileInputStream(file);
			Faces.sendFile(fis, "relatorio-produtividade.xlsx", false);
		}
		catch (Exception e1) {
			addMessageError(e1);
		}
	}

	public RelatorioProdutividadeFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(RelatorioProdutividadeFiltro filtro) {
		this.filtro = filtro;
	}

	public List<RelatorioProdutividadeVO> getList() {
		return list;
	}

	public void setList(List<RelatorioProdutividadeVO> list) {
		this.list = list;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public List<TipoProcesso> getTiposProcesso() {
		return tiposProcesso;
	}
}
