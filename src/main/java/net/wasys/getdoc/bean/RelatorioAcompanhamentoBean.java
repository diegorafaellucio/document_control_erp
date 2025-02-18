package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.RelatorioAcompanhamentoVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ManagedBean
@ViewScoped
public class RelatorioAcompanhamentoBean extends AbstractBean {

	@Autowired private RelatoriosService relatoriosService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoPrazoService tipoPrazoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private BaseRegistroService baseRegistroService;

	private ProcessoFiltro filtro = new ProcessoFiltro();

	private List<RelatorioAcompanhamentoVO> list;
	private List<Situacao> situacoes;
	private boolean agrupar = true;
	private boolean historico = false;
	private List<RegistroValorVO> formasIngresso;
	private List<RegistroValorVO> regionais;
	private List<RegistroValorVO> listCampus;
	private String regional;
	private String campus;
	private String formaIngresso;

	public enum PadraoLinha {
		SITUACOES,
		ANALISTAS,
		AREAS,
		MOTIVOS,
		STATUS,
	}

	private PadraoLinha padraoLinha = PadraoLinha.SITUACOES;
	private List<TipoProcesso> tiposProcessos;

	protected void initBean() {

		situacoes = situacaoService.findAtivas(null);

		buscar();

		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcessos = tipoProcessoService.findAtivos(permissoes);

		formasIngresso =  baseRegistroService.findByBaseInterna(BaseInterna.FORMA_INGRESSO_ID);
		regionais = baseRegistroService.findByBaseInterna(BaseInterna.REGIONAL_ID);

	}

	public void findInstituicoesCampus(){
		List<RegistroValorVO> relacionados = baseRegistroService.findByRelacionados(BaseInterna.CAMPUS_ID, Arrays.asList(regional), TipoCampo.COD_REGIONAL);
		this.listCampus = relacionados;
	}


	public void detalhes(RelatorioAcompanhamentoVO vo) {

		ProcessoFiltro clone = filtro.clone();

		if (padraoLinha.equals(PadraoLinha.STATUS)) {
			StatusProcesso status = vo.getStatus();
			if (status != null) {
				List<StatusProcesso> statusList = new ArrayList<>();
				statusList.add(status);
				clone.setStatusList(statusList);
			}
		}
		else if (padraoLinha.equals(PadraoLinha.ANALISTAS)) {
			Usuario analista = vo.getAnalista();
			clone.setAnalista(analista);
			if (analista == null) {
				clone.setSemAnalista(true);
			}
		}
		else if (padraoLinha.equals(PadraoLinha.MOTIVOS)) {
			TipoProcesso tipoProcesso = vo.getTipoProcesso();
			if (tipoProcesso != null) {
				List<TipoProcesso> tiposProcesso = new ArrayList<>();
				tiposProcesso.add(tipoProcesso);
				clone.setTiposProcesso(tiposProcesso);
			}
		}
		else if (padraoLinha.equals(PadraoLinha.AREAS)) {
			Area area = vo.getArea();
			clone.setAreaPendenciaAnalista(area);
		}
		else if (padraoLinha.equals(PadraoLinha.SITUACOES)) {
			Situacao situacao = vo.getSituacao();
			if (agrupar) {
				List<Situacao> situacaos = situacaoService.findByNome(situacao.getNome());
				clone.setSituacao(situacaos);
			}
			else {
				clone.setSituacao(Arrays.asList(situacao));
			}
		}

		setFlashAttribute("relatorioAcompanhamentoVO", vo);
		setFlashAttribute("processoFiltro", clone);
		redirect("/requisicoes/fila/");
	}

	public String formatarPrazo(Double horasPrazo, TipoPrazo tipoPrazo) {
		return tipoPrazoService.formatarPrazo(horasPrazo, tipoPrazo);
	}

	public void buscar() {
		filtro.setCamposFiltro(null);
		if(formaIngresso != null && !formaIngresso.isEmpty()){
			filtro.setCamposFiltro(CampoMap.CampoEnum.FORMA_DE_INGRESSO, Arrays.asList(formaIngresso));
		}
		if(regional != null && !regional.isEmpty()) {
			filtro.setCamposFiltro(CampoMap.CampoEnum.REGIONAL, Arrays.asList(regional));
		}
		if(campus != null && !campus.isEmpty()) {
			filtro.setCamposFiltro(CampoMap.CampoEnum.CAMPUS, Arrays.asList(campus));
		}
		if (padraoLinha.equals(PadraoLinha.STATUS)) {
			list = relatoriosService.getRelatorioEmAbertoStatus(filtro, false);
		}
		else if (padraoLinha.equals(PadraoLinha.ANALISTAS)) {
			list = relatoriosService.getRelatorioEmAbertoAnalista(filtro);
		}
		else if (padraoLinha.equals(PadraoLinha.MOTIVOS)) {
			list = relatoriosService.getRelatorioEmAbertoMotivo(filtro);
		}
		else if (padraoLinha.equals(PadraoLinha.AREAS)) {
			Usuario usuario = getUsuarioLogado();
			list = relatoriosService.getRelatorioEmAbertoArea(filtro, usuario);
		}
		else if (padraoLinha.equals(PadraoLinha.SITUACOES)) {
			list = relatoriosService.getRelatorioEmAbertoSituacao(filtro, agrupar, historico);
		}
	}

	public List<RelatorioAcompanhamentoVO> getList() {
		return list;
	}

	public void setList(List<RelatorioAcompanhamentoVO> list) {
		this.list = list;
	}

	public ProcessoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(ProcessoFiltro filtro) {
		this.filtro = filtro;
	}

	public PadraoLinha getPadraoLinha() {
		return padraoLinha;
	}

	public void setPadraoLinha(PadraoLinha padraoLinha) {
		this.padraoLinha = padraoLinha;
	}

	public PadraoLinha[] getPadroesLinhas() {
		return PadraoLinha.values();
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(List<TipoProcesso> tiposProcessos) {
		this.tiposProcessos = tiposProcessos;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public void setSituacoes(List<Situacao> situacoes) {
		this.situacoes = situacoes;
	}

	public boolean isAgrupar() {
		return agrupar;
	}

	public void setAgrupar(boolean agrupar) {
		this.agrupar = agrupar;
	}

	public boolean isHistorico() { return historico; }

	public void setHistorico(boolean historico) { this.historico = historico; }

	public List<RegistroValorVO> getFormasIngresso() {
		return formasIngresso;
	}

	public void setFormasIngresso(List<RegistroValorVO> formasIngresso) {
		this.formasIngresso = formasIngresso;
	}

	public List<RegistroValorVO> getRegionais() {
		return regionais;
	}

	public void setRegionais(List<RegistroValorVO> regionais) {
		this.regionais = regionais;
	}

	public List<RegistroValorVO> getListCampus() {
		return listCampus;
	}

	public void setListCampus(List<RegistroValorVO> listCampus) {
		this.listCampus = listCampus;
	}

	public String getRegional() {
		return regional;
	}

	public void setRegional(String regional) {
		this.regional = regional;
	}

	public String getCampus() {
		return campus;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	public String getFormaIngresso() {
		return formaIngresso;
	}

	public void setFormaIngresso(String formaIngresso) {
		this.formaIngresso = formaIngresso;
	}
}
