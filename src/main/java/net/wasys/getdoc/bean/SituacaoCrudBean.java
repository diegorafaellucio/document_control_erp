package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.SituacaoDataModel;
import net.wasys.getdoc.bean.vo.DiagramModelVO;
import net.wasys.getdoc.bean.vo.LinkDataArrayVO;
import net.wasys.getdoc.bean.vo.NodeDataArrayVO;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoRegra;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.RegraFiltro;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.SuperBeanComparator;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.math.BigDecimal;
import java.util.*;

@ManagedBean
@ViewScoped
public class SituacaoCrudBean extends AbstractBean {

	@Autowired private SituacaoService situacaoService;
	@Autowired private EtapaService etapaService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoPrazoService tipoPrazoService;
	@Autowired private SubperfilService subperfilService;
	@Autowired private RegraService regraService;

	private SituacaoFiltro filtro = new SituacaoFiltro();
	private Map<TipoProcesso, SituacaoDataModel> dataModelMap = new LinkedHashMap<TipoProcesso, SituacaoDataModel>();
	private Situacao situacao;
	private List<StatusProcesso> statusProcessos;
	private List<Situacao> proximas;
	private List<Situacao> proximasSelecionadas;
	private List<TipoProcesso> tiposProcessos;
	private List<TipoProcesso> todosTiposProcessos;
	private List<Etapa> etapas;
	private BigDecimal prazo;
	private BigDecimal prazoAdvertir;
	private Long tipoProcessoId;
	private List<Subperfil> subperfilList;
	private List<Subperfil> subperfisSelecionados;
	private String diagramModelVO;
	private String regrasFluxo;

	protected void initBean() {

		statusProcessos = Arrays.asList(StatusProcesso.values());

		todosTiposProcessos = tipoProcessoService.findAll(null, null);

		subperfilList = subperfilService.findAll();
		if(tipoProcessoId != null){
			filtro.setTipoProcessoId(tipoProcessoId);
		}
		buscar();
	}

	public void buscar() {

		dataModelMap.clear();
		if (filtro.getTipoProcessoId() == null ) {
			tiposProcessos = tipoProcessoService.findAll(null, null);
		}
		else {
			TipoProcesso tipoProcesso = tipoProcessoService.get(filtro.getTipoProcessoId());
			if(tiposProcessos != null) {
				tiposProcessos.clear();
			}else{
				tiposProcessos = new ArrayList<>();
			}
			tiposProcessos.add(tipoProcesso);
		}
		for (TipoProcesso tp : tiposProcessos) {
			Long tipoProcessoId = tp.getId();
			SituacaoFiltro filtro1 = filtro.clone();
			filtro1.setTipoProcessoId(tipoProcessoId);
			SituacaoDataModel dataModel = new SituacaoDataModel();
			dataModel.setFiltro(filtro1);
			dataModel.setService(situacaoService);
			dataModelMap.put(tp, dataModel);
		}
	}

	public void salvar() {

		TipoPrazo tipoPrazo = situacao.getTipoPrazo();
		BigDecimal horasDecimal = tipoPrazoService.converterPrazoParaHorasDecimal(prazo, tipoPrazo);
		situacao.setHorasPrazo(horasDecimal);

		TipoPrazo tipoPrazoAdvertir = situacao.getTipoPrazoAdvertir();
		BigDecimal horasAdvertirDecimal = tipoPrazoService.converterPrazoParaHorasDecimal(prazoAdvertir, tipoPrazoAdvertir);
		situacao.setHorasPrazoAdvertir(horasAdvertirDecimal);

		try {
			boolean insert = isInsert(situacao);
			Usuario usuario = getUsuarioLogado();

			arrumaProximos();
			arrumaSubperfil();

			situacaoService.saveOrUpdate(situacao, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void espelhar() {

		TipoPrazo tipoPrazo = situacao.getTipoPrazo();
		BigDecimal horasDecimal = tipoPrazoService.converterPrazoParaHorasDecimal(prazo, tipoPrazo);
		situacao.setHorasPrazo(horasDecimal);

		try {
			boolean insert = isInsert(situacao);
			Usuario usuario = getUsuarioLogado();

			arrumaProximos();
			arrumaSubperfil();

			situacaoService.espelharConfiguracao(situacao, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private void arrumaProximos() {
		Set<Long> antigos = new HashSet<Long>();
		for(ProximaSituacao ps: situacao.getProximas()) {
			antigos.add(ps.getProxima().getId());
		}
		for(Situacao s: proximasSelecionadas) {
			ProximaSituacao ps = new ProximaSituacao();
			if (antigos.contains(s.getId())) {
				antigos.remove(s.getId());
				continue;
			}
			ps.setProxima(s);
			ps.setAtual(situacao);
			situacao.getProximas().add(ps);
		}
		for (Iterator<?> iterator = situacao.getProximas().iterator(); iterator.hasNext();) {
			ProximaSituacao next = (ProximaSituacao) iterator.next();
			if (antigos.contains(next.getProxima().getId())) {
				iterator.remove();
			}
		}
	}

	private void arrumaSubperfil() {
		Set<Long> antigos = new HashSet<Long>();
		Set<SituacaoSubperfil> subperfis = situacao.getSubperfis();
		for(SituacaoSubperfil situacaoSubperfil : subperfis) {
			Subperfil subperfil = situacaoSubperfil.getSubperfil();
			Long subperfilId = subperfil.getId();
			antigos.add(subperfilId);
		}

		for(Subperfil s: subperfisSelecionados) {
			SituacaoSubperfil situacaoSubperfil = new SituacaoSubperfil();
			if (antigos.contains(s.getId())) {
				antigos.remove(s.getId());
				continue;
			}
			situacaoSubperfil.setSubperfil(s);
			situacaoSubperfil.setSituacao(situacao);
			situacao.getSubperfis().add(situacaoSubperfil);
		}
		for (Iterator<?> iterator = situacao.getSubperfis().iterator(); iterator.hasNext();) {
			SituacaoSubperfil next = (SituacaoSubperfil) iterator.next();
			Subperfil subperfil = next.getSubperfil();
			Long subperfilId = subperfil.getId();
			if (antigos.contains(subperfilId)) {
				iterator.remove();
			}
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long situacaoId = situacao.getId();

		try {
			situacaoService.excluir(situacaoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
			redirect("/cadastros/situacoes/");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		situacao = null;
	}

	public String formatarPrazo(Double horasPrazo, TipoPrazo tipoPrazo) {
		return tipoPrazoService.formatarPrazo(horasPrazo, tipoPrazo);
	}

	public Situacao getSituacao() {
		return situacao;
	}

	public void carregarSituacao() {
		String situacaoIdStr = Faces.getRequestParameter("situacaoId");
		if(situacaoIdStr == null) return;
		Long situacaoId = new Long(situacaoIdStr);
		Situacao situacao = situacaoService.get(situacaoId);
		setSituacao(situacao);
	}

	public void setSituacao(Situacao situacao) {

		if(situacao == null) {
			situacao = new Situacao();
			proximas = null;
			prazo = BigDecimal.ZERO;
			Long tipoProcessoId = filtro.getTipoProcessoId();
			if(tipoProcessoId != null) {
				TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
				situacao.setTipoProcesso(tipoProcesso);
			}
			this.etapas = null;
		}
		else {
			Long situacaoId = situacao.getId();
			situacao = situacaoService.get(situacaoId);
			SituacaoFiltro filtro2 = new SituacaoFiltro();
			TipoProcesso tipoProcesso = situacao.getTipoProcesso();
			if(tipoProcesso != null) {
				Long tipoProcessoId = tipoProcesso.getId();
				filtro2.setTipoProcessoId(tipoProcessoId);
				proximas = situacaoService.findByFiltro(filtro2, null, null);
				proximas.remove(situacao);
			}
			Long tipoProcessoId = tipoProcesso.getId();
			etapas = etapaService.findByTipoProcesso(tipoProcessoId);
		}

		proximasSelecionadas = new ArrayList<Situacao>();
		for(ProximaSituacao ps: situacao.getProximas()) {
			proximasSelecionadas.add(ps.getProxima());
		}

		subperfisSelecionados = new ArrayList<Subperfil>();
		Set<SituacaoSubperfil> subperfis = situacao.getSubperfis();
		for(SituacaoSubperfil situacaoSubperfil : subperfis) {
			Subperfil subperfil = situacaoSubperfil.getSubperfil();
			subperfisSelecionados.add(subperfil);
		}

		this.situacao = situacao;
	}

	public void selecionaTipoProcesso(ValueChangeEvent event) {

		Object newValue = event.getNewValue();

		if(newValue instanceof TipoProcesso) {

			TipoProcesso tipoProcesso = (TipoProcesso) newValue;
			Long tipoProcessoId = tipoProcesso.getId();

			SituacaoFiltro filtro2 = new SituacaoFiltro();
			filtro2.setTipoProcessoId(tipoProcessoId);
			proximas = situacaoService.findByFiltro(filtro2, null, null);
			proximas.remove(situacao);
		}
		else {

			this.proximas = null;
		}
	}

	public void defineCampoContarSlaEtapa(ValueChangeEvent event) {

		Object newValue = event.getNewValue();

		if(newValue instanceof Etapa) {
			Etapa etapa = (Etapa) newValue;
			boolean etapaFinal = etapa.getEtapaFinal();
			situacao.setContarTempoSlaEtapa(!etapaFinal);
		}

	}

	public void carregarFluxoGrama(TipoProcesso tipoProcesso){
		Long tipoProcessoId = tipoProcesso.getId();
		List<Situacao> ativas = situacaoService.findAtivas(null, tipoProcessoId);
		List<NodeDataArrayVO> nodeDataArray = new ArrayList<>();
		List<LinkDataArrayVO> linkDataArray = new ArrayList<>();
		RegraFiltro regraFiltro = new RegraFiltro();
		Map<Long, String> regraMap = new HashMap<>();

		for (Situacao situacao : ativas){
			Long id = situacao.getId();
			String nome = situacao.getNome();
			StatusProcesso status = situacao.getStatus();

			NodeDataArrayVO nodeDataArrayVO = new NodeDataArrayVO();
			nodeDataArrayVO.setKey(id);
			nodeDataArrayVO.setText(nome);
			nodeDataArrayVO.setStroke("#00A9C9");
			nodeDataArrayVO.setCategory("");

			if(StatusProcesso.RASCUNHO.equals(status) || StatusProcesso.CONCLUIDO.equals(status)) {
				nodeDataArrayVO.setStroke("#5ce747");
			}

			if(StatusProcesso.PENDENTE.equals(status)) {
				nodeDataArrayVO.setStroke("#DC3C00");
			}

			regraFiltro.setAtiva(true);
			regraFiltro.setSituacao(situacao);
			regraFiltro.setTipoProcessoId(tipoProcessoId);
			List<Regra> regras = regraService.findByFiltro(regraFiltro, null, null);

			List<Long> regraImediata = new ArrayList<>();
			List<Long> regraAgendada = new ArrayList<>();
			List<Long> regraDecisaoFluxo = new ArrayList<>();
			for(Regra regra : regras){
				Long regraId = regra.getId();
				Integer decisaoFluxo = regra.getDecisaoFluxo();
				TipoExecucaoRegra tipoExecucao = regra.getTipoExecucao();
				if(decisaoFluxo != null){
					regraDecisaoFluxo.add(regraId);
				} else if (TipoExecucaoRegra.IMEDIATA.equals(tipoExecucao)){
					regraImediata.add(regraId);
				} else {
					regraAgendada.add(regraId);
				}
				regraMap.put(regraId, regra.getNome());
			}
			nodeDataArrayVO.setVisibleRI(regraImediata.size() > 0);
			nodeDataArrayVO.setVisibleRA(regraAgendada.size() > 0);
			nodeDataArrayVO.setVisibleRD(regraDecisaoFluxo.size() > 0);
			nodeDataArrayVO.setDescRI(regraImediata.toArray());
			nodeDataArrayVO.setDescRA(regraAgendada.toArray());
			nodeDataArrayVO.setDescRD(regraDecisaoFluxo.toArray());

			nodeDataArray.add(nodeDataArrayVO);

			List<ProximaSituacao> proximasList = getProximasList(situacao);
			for(ProximaSituacao proximaSituacao : proximasList) {
				Situacao situacaoAtual = proximaSituacao.getAtual();
				Long situacaoAtualId = situacaoAtual.getId();
				Situacao situacaoProxima = proximaSituacao.getProxima();
				Long situacaoProximaId = situacaoProxima.getId();
				LinkDataArrayVO linkDataArrayVO = new LinkDataArrayVO();
				linkDataArrayVO.setFrom(situacaoAtualId);
				linkDataArrayVO.setTo(situacaoProximaId);
				linkDataArray.add(linkDataArrayVO);
			}
		}

		DiagramModelVO diagramModelVO = new DiagramModelVO();
		diagramModelVO.setClasse("go.GraphLinksModel");
		diagramModelVO.setNodeDataArray(nodeDataArray);
		diagramModelVO.setLinkDataArray(linkDataArray);

		regrasFluxo = DummyUtils.objectToJson(regraMap);
		String diagramModel = DummyUtils.objectToJson(diagramModelVO);
		diagramModel = diagramModel.replace("classe", "class");
		diagramModel = diagramModel.replace("\"", "\\\"");
		this.diagramModelVO = diagramModel;
	}

	public List<StatusProcesso> getStatusProcessos() {
		return statusProcessos;
	}

	public void setStatusProcessos(List<StatusProcesso> statusProcessos) {
		this.statusProcessos = statusProcessos;
	}

	public List<Situacao> getProximas() {
		return proximas;
	}

	public void setProximas(List<Situacao> proximas) {
		this.proximas = proximas;
	}

	public List<Situacao> getProximasSelecionadas() {
		return proximasSelecionadas;
	}

	public void setProximasSelecionadas(List<Situacao> proximasSelecionadas) {
		this.proximasSelecionadas = proximasSelecionadas;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(List<TipoProcesso> tiposProcesso) {
		this.tiposProcessos = tiposProcesso;
	}

	public SituacaoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(SituacaoFiltro filtro) {
		this.filtro = filtro;
	}

	public Map<TipoProcesso, SituacaoDataModel> getDataModelMap() {
		return dataModelMap;
	}

	public BigDecimal getPrazo() {

		BigDecimal horasPrazo = situacao.getHorasPrazo();
		TipoPrazo tipoPrazo = situacao.getTipoPrazo();

		if(horasPrazo != null) {
			prazo = tipoPrazoService.calcularPrazo(horasPrazo, tipoPrazo);
		}

		return prazo;
	}

	public void setPrazo(BigDecimal prazo) {
		this.prazo = prazo;
	}

	public BigDecimal getPrazoAdvertir() {

		BigDecimal horasPrazoAdvertir = situacao.getHorasPrazoAdvertir();
		TipoPrazo tipoPrazoAdvertir = situacao.getTipoPrazoAdvertir();

		if(horasPrazoAdvertir != null) {
			prazoAdvertir = tipoPrazoService.calcularPrazo(horasPrazoAdvertir, tipoPrazoAdvertir);
		}

		return prazoAdvertir;
	}

	public void setPrazoAdvertir(BigDecimal prazoAdvertir) {
		this.prazoAdvertir = prazoAdvertir;
	}

	public List<ProximaSituacao> getProximasList(Situacao situacao) {
		Set<ProximaSituacao> proximas = situacao.getProximas();
		List<ProximaSituacao> proximasList = new ArrayList<>();
		proximasList.addAll(proximas);
		Collections.sort(proximasList, new SuperBeanComparator<ProximaSituacao>("proxima.nome"));
		return proximasList;
	}

	public List<TipoProcesso> getTodosTiposProcessos() {
		return todosTiposProcessos;
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public List<Etapa> getEtapas() {
		return etapas;
	}

	public List<Subperfil> getSubperfilList() {
		return subperfilList;
	}

	public List<Subperfil> getSubperfisSelecionados() {
		return subperfisSelecionados;
	}

	public void setSubperfisSelecionados(List<Subperfil> subperfisSelecionados) {
		this.subperfisSelecionados = subperfisSelecionados;
	}

	public String getDiagramModelVO() {
		return diagramModelVO;
	}

	public String getRegrasFluxo() {
		return regrasFluxo;
	}
}
