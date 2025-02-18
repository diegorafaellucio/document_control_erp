package net.wasys.getdoc.bean;

import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class TipoCampoCrudBean extends AbstractBean {

	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private BaseInternaService baseInternaService;

	private Long tipoProcessoId;
	private TipoProcesso tipoProcesso;
	private TipoCampo tipoCampo;
	private TipoCampoGrupo grupo;
	private List<TipoCampoGrupo> grupos;
	private Map<TipoCampoGrupo, List<TipoCampo>> camposMap;
	private boolean optionDeleteGrupo = false;
	private List<Situacao> situacoes;
	private String campoPai;
	private List<CampoAbstract.CampoPai> filiacoes;
	private List<TipoCampo> camposPais;
	private List<BaseInterna> basesInternas;
	private List<Situacao> proximasSelecionadas;

	protected void initBean() {

		if(tipoProcesso == null) {
			if(tipoProcessoId == null) {
				redirect("/cadastros/tipos-processos/");
				return;
			}
			this.tipoProcesso = tipoProcessoService.get(tipoProcessoId);
			if(tipoProcesso == null) {
				redirect("/cadastros/tipos-processos/");
				return;
			}
		}

		camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
		grupos = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
		situacoes = situacaoService.findAtivas(null, tipoProcessoId);
		basesInternas = baseInternaService.findAtivos();

		if(camposMap.isEmpty()) {
			camposMap.put(null, null);//pra exibir a tabela, mesmo vazia
		}
	}

	private void arrumaSituacoes() {
		Set<Long> antigos = new HashSet<Long>();
		for(TipoCampoGrupoSituacao ps: grupo.getSituacoes()) {
			antigos.add(ps.getSituacao().getId());
		}
		for(Situacao s: proximasSelecionadas) {
			TipoCampoGrupoSituacao ps = new TipoCampoGrupoSituacao();
			if (antigos.contains(s.getId())) {
				antigos.remove(s.getId());
				continue;
			}
			ps.setSituacao(s);
			ps.setTipoCampoGrupo(grupo);
			grupo.getSituacoes().add(ps);
		}
		for (Iterator<?> iterator = grupo.getSituacoes().iterator(); iterator.hasNext();) {
			TipoCampoGrupoSituacao next = (TipoCampoGrupoSituacao) iterator.next();
			if (antigos.contains(next.getSituacao().getId())) {
				iterator.remove();
			}
		}
	}

	public void salvar() {

		try {
			boolean insert = isInsert(tipoCampo);
			Usuario usuario = getUsuarioLogado();

			String pais = null;
			if(filiacoes != null && !filiacoes.isEmpty()) {
				ObjectMapper objectMapper = new ObjectMapper();
				pais = objectMapper.writeValueAsString(filiacoes);
			}
			tipoCampo.setPais(pais);

			tipoCampoService.saveOrUpdate(tipoProcesso, tipoCampo, usuario);

			camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void espelhar() {

		try {
			boolean insert = isInsert(tipoCampo);
			Usuario usuario = getUsuarioLogado();

			tipoCampoService.espelhar(tipoCampo, usuario);

			camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void salvarGrupo() {

		try {
			boolean insert = isInsert(grupo);
			Usuario usuario = getUsuarioLogado();

			arrumaSituacoes();

			grupo.setTipoProcesso(tipoProcesso);

			tipoCampoGrupoService.saveOrUpdate(grupo, usuario);

			camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
			grupos = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();
		try {
			if (isOptionDeleteGrupo()) {
				grupo.setTipoProcesso(tipoProcesso);
				Long grupoId = grupo.getId();
				tipoCampoGrupoService.excluir(grupoId, usuarioLogado);

				grupos = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
				setOptionDeleteGrupo(false);
			}
			else {
				Long tipoCampoId = tipoCampo.getId();
				tipoCampoService.excluir(tipoCampoId, usuarioLogado);
			}

			camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void subirOrdem(TipoCampo tipoCampo) {

		Usuario usuario = getUsuarioLogado();
		tipoCampoService.subirOrdem(tipoCampo, usuario);
		camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
	}

	public void subirOrdem(TipoCampoGrupo tipoCampoGrupo) {

		Usuario usuario = getUsuarioLogado();
		tipoCampoGrupoService.subirOrdem(tipoCampoGrupo, usuario);
		grupos = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
		camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
	}

	public void descerOrdem(TipoCampo tipoCampo) {

		Usuario usuario = getUsuarioLogado();
		tipoCampoService.descerOrdem(tipoCampo, usuario);
		camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
	}

	public void descerOrdem(TipoCampoGrupo tipoCampoGrupo) {

		Usuario usuario = getUsuarioLogado();
		tipoCampoGrupoService.descerOrdem(tipoCampoGrupo, usuario);
		grupos = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
		camposMap = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
	}

	public TipoCampo getTipoCampo() {
		if(tipoCampo == null) {
			tipoCampo = new TipoCampo();
		}
		return tipoCampo;
	}

	public void setTipoCampo(TipoCampo tipoCampo) {
		if(tipoCampo == null) {
			tipoCampo = new TipoCampo();
			camposPais = new ArrayList<>();
			filiacoes = new ArrayList<>();
		}
		else {
			tipoCampo = tipoCampoService.get(tipoCampo.getId());
			filiacoes = tipoCampo.getPaisObject();
			checkBiggerNum(1);
			camposPais = tipoCampoService.findPossiveisPais(tipoCampo);
			for (TipoCampo tc : camposPais) {
				Hibernate.initialize(tc.getGrupo());
			}
		}
		this.tipoCampo = tipoCampo;
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public List<TipoCampoGrupo> getGrupos() {
		return grupos;
	}

	public List<TipoCampoGrupo> getGruposMenosGrupoAtual() {

		List<TipoCampoGrupo> gruposFiltrados = new ArrayList<>(grupos);

		if (!gruposFiltrados.isEmpty()) {
			gruposFiltrados.removeIf(cg -> cg.getId().equals(grupo.getId()));
		}

		return gruposFiltrados;
	}

	public Map<TipoCampoGrupo, List<TipoCampo>> getCamposMap() {
		return camposMap;
	}

	public TipoCampoGrupo getGrupo() {
		return grupo;
	}

	public void setGrupo(TipoCampoGrupo grupo) {

		if(grupo == null) {
			grupo = new TipoCampoGrupo();
			grupo.setTipoProcesso(tipoProcesso);
		}
		else {
			Long situacaoId = grupo.getId();
			grupo = tipoCampoGrupoService.get(situacaoId);
		}

		proximasSelecionadas = new ArrayList<Situacao>();
		for(TipoCampoGrupoSituacao ps: grupo.getSituacoes()) {
			proximasSelecionadas.add(ps.getSituacao());
		}

		this.grupo = grupo;
	}

	public void selecionaTipoEntrada(ValueChangeEvent event) {

		TipoEntradaCampo tipoEntrada = (TipoEntradaCampo) event.getNewValue();
		tipoCampo.setTipo(tipoEntrada);
	}

	public boolean solicitarInfoTamanho() {

		TipoEntradaCampo tipo = tipoCampo != null ? tipoCampo.getTipo() : null;
		if(tipo == null) {
			return false;
		}

		switch (tipo) {
		case INTEIRO:
			return true;
		case TEXTO:
			return true;
		case TEXTO_LONGO:
			return true;
		default:
			break;
		}

		return false;
	}

	public boolean solicitarInfoOpcoes() {
		TipoEntradaCampo tipo = tipoCampo != null ? tipoCampo.getTipo() : null;
		if(tipo == null) {
			return false;
		}
		switch (tipo) {
		case COMBO_BOX:
		case COMBO_BOX_MULTI:
		case RADIO:
			return true;
		default:
			break;
		}
		return false;
	}

	public boolean solicitarInfoBaseInterna() {
		TipoEntradaCampo tipo = tipoCampo != null ? tipoCampo.getTipo() : null;
		if(tipo == null) {
			return false;
		}
		switch (tipo) {
			case COMBO_BOX_ID:
				return true;
			default:
				break;
		}
		return false;
	}

	public boolean isOptionDeleteGrupo() {
		return optionDeleteGrupo;
	}

	public void setOptionDeleteGrupo(boolean optionDeleteGrupo) {
		this.optionDeleteGrupo = optionDeleteGrupo;
	}

	public void setOptionDeleteGrupo(boolean optionDeleteGrupo, TipoCampoGrupo grupo) {
		setGrupo(grupo);
		this.optionDeleteGrupo = optionDeleteGrupo;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public List<CampoAbstract.CampoPai> getFiliacoes() {
		return filiacoes;
	}

	public List<TipoCampo> getCamposPais() {
		return camposPais;
	}

	public String getCampoPai() {
		return campoPai;
	}

	public void setCampoPai(String campoPai) {
		this.campoPai = campoPai;
	}

	public void addFiliacao() {
		CampoAbstract.CampoPai campoPai = new CampoAbstract.CampoPai();
		int biggerNum = 1;
		biggerNum = checkBiggerNum(biggerNum);
		campoPai.setNome("pai" + biggerNum);
		filiacoes.add(campoPai);
	}

	public void removeFiliacao(CampoAbstract.CampoPai pai) {
		filiacoes.remove(pai);
	}

	private int checkBiggerNum(int biggerNum) {
		for (CampoAbstract.CampoPai pai : filiacoes) {
			String nome = pai.getNome();
			if(StringUtils.isNotBlank(nome)) {
				pai.setNome("pai" + biggerNum++);
			} else {
				String paiNum = nome.replace("pai", "");
				biggerNum = Math.max(biggerNum, Integer.parseInt(paiNum));
			}
		}
		return biggerNum;
	}

	public List<BaseInterna> getBasesInternas() {
		return basesInternas;
	}

	public List<Situacao> getProximasSelecionadas() {
		return proximasSelecionadas;
	}

	public void setProximasSelecionadas(List<Situacao> proximasSelecionadas) {
		this.proximasSelecionadas = proximasSelecionadas;
	}
}
