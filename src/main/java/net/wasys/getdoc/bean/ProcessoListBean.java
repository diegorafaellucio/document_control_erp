package net.wasys.getdoc.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.util.faces.FacesUtil;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.ProcessoDataModel;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro.ConsiderarData;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.SuperBeanComparator;

@ManagedBean
@SessionScoped
public class ProcessoListBean extends AbstractBean {

	public static final String DATA_INICIAL_FILTRO = "30/09/2019";

	@Autowired private ProcessoService processoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private FilaConfiguracaoService filaConfiguracaoService;
	@Autowired private BaseRegistroService baseRegistroService;

	private ProcessoFiltro filtro;
	private ProcessoDataModel dataModel = new ProcessoDataModel();
	private List<Usuario> analistas;
	private List<TipoProcesso> tiposProcessos;
	private List<Situacao> situacoes;

	private List<Processo> processosSelecionados;
	private List<Situacao> situacoesConclusao;
	private Situacao novaSituacao;

	private Map<String, List<Long>> colunasPersonalizadas;

	private TipoProcesso tipoProcessoCampos;
	private Boolean permissaoFiltroTP;
	private FilaConfiguracao filaConfiguracao;

	protected void initBean() {

		UsuarioFiltro f = new UsuarioFiltro();
		f.setRole(RoleGD.GD_ANALISTA);
		f.setStatus(StatusUsuario.ATIVO);
		analistas = usuarioService.findByFiltroToSelect(f);

		Usuario usuario = getUsuarioLogado();
		if(tiposProcessos == null) {
			RoleGD roleGD = usuario.getRoleGD();
			List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
			tiposProcessos = tipoProcessoService.findAtivosAndInitialize(permissoes);
		}
		if(colunasPersonalizadas == null) {
			colunasPersonalizadas = processoService.montarColunaPersonalizada(usuario);
		}
		if(situacoes == null) {
			situacoes = situacaoService.findAtivasToSelect(null);
			Collections.sort(situacoes, new SuperBeanComparator<>("tipoProcesso.nome, nome"));
		}
		if(situacoesConclusao == null) {
			situacoesConclusao = situacaoService.findAtivasToSelect(StatusProcesso.CONCLUIDO);
		}

		if(filtro == null) {
			limpar();
		}

		if(usuario.isAreaRole() || usuario.isComercialRole()) {
			//filtro.setArea(area);
			filtro.setAutor(usuario);
		}
		else if(usuario.isRequisitanteRole()) {
			filtro.setAutor(usuario);
		}

		relatorioLicenciamentoParameters();

		dataModel.setFiltro(filtro);
		dataModel.setService(processoService);

		filaConfiguracao = filaConfiguracaoService.carregarConfiguracoesFila(usuario);
	}

	public void concluirEmMassa() {

		try {
			List<Processo> processos = new ArrayList<>();
			for (Processo vo : processosSelecionados) {

				Processo processo = vo;
				processos.add(processo);
			}

			Usuario usuario = getUsuarioLogado();
			// TODO CAMPO OBSERVACAO
			processoService.concluirEmMassa(processos, usuario, novaSituacao, "");

			setRequestAttribute("fecharModal", true);
			addMessage("conclusaoProcessos.sucesso", processosSelecionados.size());

			novaSituacao = null;
			processosSelecionados = null;

			initBean();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void relatorioLicenciamentoParameters(){

		if (FacesUtil.getParam("tipoProcessoId") != null) {
			String tipoProcessoId = FacesUtil.getParam("tipoProcessoId");
			Long id = Long.parseLong(tipoProcessoId);
			TipoProcesso tp = tipoProcessoService.get(id);
			tiposProcessos.clear();
			tiposProcessos.add(tp);
			filtro.setTiposProcesso(tiposProcessos);
		}
		if (FacesUtil.getParam("inicio") != null){
			String inicio = FacesUtil.getParam("inicio");
			String fim = FacesUtil.getParam("fim");
			try {
				SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy");
				Date date = fmt.parse(inicio);
				filtro.setDataInicio(date);
				date = fmt.parse(fim);
				filtro.setDataFim(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean podeTrocarAnalistas() {

		Usuario usuario = getUsuarioLogado();
		boolean adminRole = usuario.isAdminRole();
		boolean gestorRole = usuario.isGestorRole();

		return adminRole || gestorRole;
	}

	public void limpar() {
		tipoProcessoCampos = null;
		permissaoFiltroTP = null;
		tiposProcessos = null;
		Date date = getDateInicialDefault();

		filtro = new ProcessoFiltro();
		filtro.setDataInicio(date);
		filtro.setDataFim(new Date());
		dataModel.setFiltro(filtro);
		dataModel.setBuscar(false);
		initBean();
	}

	private Date getDateInicialDefault() {

		Date date = null;
		try {
			SimpleDateFormat fmt = new SimpleDateFormat("d/M/yyyy");
			date = fmt.parse(DATA_INICIAL_FILTRO);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return date;
	}

	public void buscar() {
		dataModel.setBuscar(true);
		initBean();
	}

	public List<Campo> findCamposColunaPersonalizada(List<Long> tipoCampoIds, Long processoId) {
		return processoService.findCamposColunaPersonalizada(tipoCampoIds, processoId);
	}

	public String getValorBaseInternaLabel(Long baseInternaId, String chaveUnicidade) {
		return processoService.getValorBaseInternaLabel(baseInternaId, chaveUnicidade);
	}

	public void confirmarFiltros() {

		try {
			filtro.configuraTipoProcessoCampos(tipoProcessoCampos, baseRegistroService);

			setRequestAttribute("fecharModal", true);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void bloqueiaTiposProcessos(ValueChangeEvent event) {

		tipoProcessoCampos = (TipoProcesso) event.getNewValue();

		if (tipoProcessoCampos != null) {
			permissaoFiltroTP = true;
			filtro.setTiposProcesso(null);
		}
		else {
			permissaoFiltroTP = false;
			filtro.setCamposFiltro(null);
		}
	}

	public ProcessoDataModel getDataModel() {
		return dataModel;
	}

	public ProcessoFiltro getFiltro() {
		return filtro;
	}

	public ConsiderarData[] getConsiderarDatas() {
		return ConsiderarData.values();
	}

	public List<Usuario> getAnalistas() {
		return analistas;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public List<Processo> getProcessosSelecionados() {
		return processosSelecionados;
	}

	public void setProcessosSelecionados(List<Processo> processosSelecionados) {
		this.processosSelecionados = processosSelecionados;
	}

	public Situacao getNovaSituacao() {
		return novaSituacao;
	}

	public void setNovaSituacao(Situacao novaSituacao) {
		this.novaSituacao = novaSituacao;
	}

	public List<Situacao> getSituacoesConclusao() {
		return situacoesConclusao;
	}

	public void setSituacoesConclusao(List<Situacao> situacoesConclusao) {
		this.situacoesConclusao = situacoesConclusao;
	}

	public TipoProcesso getTipoProcessoCampos() {
		return tipoProcessoCampos;
	}

	public void setTipoProcessoCampos(TipoProcesso tipoProcessoCampos) {
		this.tipoProcessoCampos = tipoProcessoCampos;
	}

	public Boolean getPermissaoFiltroTP() {
		return permissaoFiltroTP;
	}

	public void setPermissaoFiltroTP(Boolean permissaoFiltroTP) {
		this.permissaoFiltroTP = permissaoFiltroTP;
	}

	public Map<String, List<Long>> getColunasPersonalizadas() {
		return colunasPersonalizadas;
	}

	public void setColunasPersonalizadas(Map<String, List<Long>> colunasPersonalizadas) {
		this.colunasPersonalizadas = colunasPersonalizadas;
	}

	public FilaConfiguracao getFilaConfiguracao() {
		return filaConfiguracao;
	}
}
