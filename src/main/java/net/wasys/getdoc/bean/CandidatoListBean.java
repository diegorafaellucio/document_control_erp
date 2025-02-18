package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.ProcessoDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro.ConsiderarData;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.faces.FacesUtil;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@ManagedBean
@SessionScoped
public class CandidatoListBean extends AbstractBean {

	@Autowired private ProcessoService processoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private FilaConfiguracaoService filaConfiguracaoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private EtapaService etapaService;

	private ProcessoFiltro filtro;
	private ProcessoDataModel dataModel = new ProcessoDataModel();
	private List<TipoProcesso> tiposProcessos;
	private RelatorioCandidatoExporter exporter;
	private List<String> situacoes;

	private Map<String, List<Long>> colunasPersonalizadas;
	private List<RegistroValorVO> regionais;
	private List<RegistroValorVO> listCampus;
	private String regional;
	private String campus;
	private List<String> resultadoIsencao;
	private List<String> etapas;
	private Boolean pesquisaAvanc = true;
	private List<String> localDeOfertaList = new ArrayList<>();

	private Boolean permissaoFiltroTP;

	protected void initBean() {

		Usuario usuario = getUsuarioLogado();
		if(tiposProcessos == null) {
			RoleGD roleGD = usuario.getRoleGD();
			List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
			tiposProcessos = tipoProcessoService.findAtivosAndInitialize(permissoes);
		}
		if(colunasPersonalizadas == null) {
			carregarColunasPersonalizadas();
		}
		if(etapas == null){
			this.etapas = etapaService.findNomesAtivas();
		}
		if(situacoes == null) {
			List<Situacao> situacoesList = situacaoService.findAtivasToSelect(null);
			situacoes = new ArrayList<>();
			for(Situacao situacao : situacoesList){
				String nome = situacao.getNome();
				if(!situacoes.contains(nome) && !Situacao.ANALISE_ISENCAO_CONCLUIDA.equals(nome)){
					situacoes.add(nome);
				}
			}
		}

		if(filtro == null) {
			limpar();
		}

		if(usuario.isAreaRole() || usuario.isComercialRole()) {
			filtro.setAutor(usuario);
		}
		else if(usuario.isRequisitanteRole()) {
			filtro.setAutor(usuario);
		}

		relatorioLicenciamentoParameters();
		regionais = baseRegistroService.findByBaseInterna(BaseInterna.REGIONAL_ID);

		dataModel.setFiltro(filtro);
		dataModel.setService(processoService);
	}

	public boolean carregarLocalDeOferta(ProcessoFiltro filtro) {

		List<TipoProcesso> tiposProcesso = filtro.getTiposProcesso();
		if(tiposProcesso == null) return false;

		for(TipoProcesso tipoProcesso : tiposProcesso) {
			Long tipoProcessoId = tipoProcesso.getId();
			boolean isSisFiesOrSisProuni = Arrays.asList(TipoProcesso.SIS_PROUNI, TipoProcesso.SIS_FIES).contains(tipoProcessoId);
			if(isSisFiesOrSisProuni) {
				localDeOfertaList = localDeOfertaList.isEmpty() ? processoService.getLocalDeOferta() : localDeOfertaList;
				return true;
			}
		}

		if(this.filtro != null) {
			this.filtro.setLocalDeOferta(null) ;
		}

		return false;
	}

	public List<String> completaLocalDeOferta(String query) {

		if(StringUtils.isNotBlank(query)) {
			List<String> sugestoes = new ArrayList<>();
			for(String sugestao : localDeOfertaList) {
				sugestao = sugestao.toUpperCase();

				if(sugestao.contains(query.toUpperCase())) {
					sugestoes.add(sugestao);
				}
			}
			return sugestoes;
		}

		return new ArrayList<String>(localDeOfertaList);
	}

	private void carregarColunasPersonalizadas() {

		Long filaConsultaCandidato = null;
		Usuario usuarioLogado = getUsuarioLogado();
		Subperfil subperfilAtivo = usuarioLogado.getSubperfilAtivo();
		if(subperfilAtivo != null) {
			FilaConfiguracao filaConfiguracao = subperfilAtivo.getFilaConfiguracao();
			filaConsultaCandidato = filaConfiguracao != null ? filaConfiguracao.getId() : FilaConfiguracao.FILA_CONSULTA_CANDIDATO;
		}
		else {
			filaConsultaCandidato = FilaConfiguracao.FILA_CONSULTA_CANDIDATO;
		}

		FilaConfiguracao fila = filaConfiguracaoService.get(filaConsultaCandidato);
		if(fila == null){
			fila = filaConfiguracaoService.getPadrao();
		}
		colunasPersonalizadas = processoService.montarColunaPersonalizada(fila);
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

	public void limpar() {
		permissaoFiltroTP = null;
		regional = null;
		campus = null;
		tiposProcessos = null;
		resultadoIsencao = null;
		filtro = new ProcessoFiltro();
		Calendar cal = Calendar.getInstance();
		filtro.setDataFim(cal.getTime());
		cal.add(Calendar.MONTH, -3);
		filtro.setDataInicio(cal.getTime());
		dataModel.setFiltro(filtro);
		dataModel.setBuscar(false);
		initBean();
	}

	public void buscar() {
		filtro.setCamposFiltro(null);
		if(regional != null && !regional.isEmpty()) {
			filtro.setCamposFiltro(CampoMap.CampoEnum.REGIONAL, Arrays.asList(regional));
		}
		Usuario usuarioLogado = getUsuarioLogado();
		if(campus != null && !campus.isEmpty()) {
			filtro.setCamposFiltro(CampoMap.CampoEnum.CAMPUS, Arrays.asList(campus));
		} else if(usuarioLogado.isSalaMatriculaRole()) {
			/*List<String> campus = new ArrayList<>();
			Set<UsuarioCampus> usuarioLogadoCampus = usuarioLogado.getCampus();

			for (UsuarioCampus logadoCampus : usuarioLogadoCampus) {
				BaseRegistro campus1 = logadoCampus.getCampus();
				String chaveUnicidade = campus1.getChaveUnicidade();

				campus.add(chaveUnicidade);
			}
			filtro.setCampus(null);
			filtro.setCamposFiltro(CampoMap.CampoEnum.CAMPUS, campus);*/
		}
		if(resultadoIsencao != null && !resultadoIsencao.isEmpty()){
			filtro.setCamposFiltro(CampoMap.CampoEnum.RESULTADO_ISENCAO_DISCIPLINA, resultadoIsencao);
		}

		dataModel.setBuscar(true);
		initBean();
	}

	public void findInstituicoesCampus(){
		List<RegistroValorVO> relacionados = baseRegistroService.findByRelacionados(BaseInterna.CAMPUS_ID, Arrays.asList(regional), TipoCampo.COD_REGIONAL);
		Usuario usuarioLogado = getUsuarioLogado();

		RoleGD roleGD = usuarioLogado.getRoleGD();
		if(roleGD.equals(RoleGD.GD_SALA_MATRICULA)) {
			List<RegistroValorVO> relacionados1 = new ArrayList<>();
			Set<UsuarioCampus> usuarioCampusSet = usuarioLogado.getCampus();

			for (UsuarioCampus usuarioCampus : usuarioCampusSet) {
				BaseRegistro campus = usuarioCampus.getCampus();
				Optional<RegistroValorVO> first = relacionados.stream().filter(r -> r.getBaseRegistro().getId().equals(campus.getId())).findFirst();

				if(first.isPresent()) {
					RegistroValorVO registroValorVO = first.get();

					relacionados1.add(registroValorVO);
				}
			}
			this.listCampus = relacionados1;
		} else {
			this.listCampus = relacionados;
		}
	}

	public List<Campo> findCamposColunaPersonalizada(List<Long> tipoCampoIds, Long processoId) {
		return processoService.findCamposColunaPersonalizada(tipoCampoIds, processoId);
	}

	public String getValorBaseInternaLabel(Long baseInternaId, String chaveUnicidade) {
		return processoService.getValorBaseInternaLabel(baseInternaId, chaveUnicidade);
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
				String fileName = "relatorio-geral.xlsx";
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
			exporter = applicationContext.getBean(RelatorioCandidatoExporter.class);
			exporter.setFiltro(filtro);
			Usuario usuarioLogado = getUsuarioLogado();
			exporter.setUsuario(usuarioLogado);
			exporter.start();
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

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
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

	public RelatorioCandidatoExporter getExporter() {
		return exporter;
	}

	public List<String> getSituacoes() {
		return situacoes;
	}

	public List<String> getResultadoIsencao() {
		return resultadoIsencao;
	}

	public void setResultadoIsencao(List<String> resultadoIsencao) {
		this.resultadoIsencao = resultadoIsencao;
	}

	public List<String> getEtapas() {
		return etapas;
	}

	public void setEtapas(List<String> etapas) {
		this.etapas = etapas;
	}

	public Boolean getPesquisaAvanc() {
		return pesquisaAvanc;
	}

	public void setPesquisaAvanc(Boolean pesquisaAvanc) {
		this.pesquisaAvanc = pesquisaAvanc;
	}

	public List<String> getLocalDeOfertaList() {
		return localDeOfertaList;
	}
}
