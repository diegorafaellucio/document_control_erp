package net.wasys.getdoc.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.SubperfilDataModel;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class SubperfilCrudBean extends AbstractBean {

	@Autowired private SubperfilService subperfilService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private FilaConfiguracaoService filaConfiguracao;

	private SubperfilDataModel dataModel;
	private List<Subperfil> subperfis;

	private Subperfil entity;
	private List<Situacao> situacoes;
	private List<TipoDocumento> tipoDocumentos;
	private List<FilaConfiguracao> filas;

	private Map<TipoProcesso, List<Situacao>> situacoesMap = new HashMap<>();
	private Map<TipoProcesso, List<TipoDocumento>> tipoDocumentosMap = new HashMap<>();
	private List<TipoProcesso> tiposProcessos;

	private List<List<Situacao>> selectedSituacoes = new ArrayList<>();
	private List<List<TipoDocumento>> selectedTipoDocumentos = new ArrayList<>();

	protected void initBean() {

		dataModel = new SubperfilDataModel();
		dataModel.setService(subperfilService);

		tiposProcessos = tipoProcessoService.findAtivos(null);
		filas = filaConfiguracao.findAll();

		situacoes = situacaoService.findAtivas(null);
		tipoDocumentos = tipoDocumentoService.findByTipoProcesso(null, null, null);
		situacoesMap = new HashMap<>();
		tipoDocumentosMap = new HashMap<>();
		for (TipoProcesso tp : tiposProcessos) {
			situacoesMap.put(tp, new ArrayList<>());
			tipoDocumentosMap.put(tp, new ArrayList<>());
		}

		for (Situacao s : situacoes) {
			TipoProcesso tipoProcesso = s.getTipoProcesso();
			List<Situacao> list = situacoesMap.get(tipoProcesso);
			if(list != null) {
				list.add(s);
			}
		}

		for (TipoDocumento td : tipoDocumentos) {
			TipoProcesso tipoProcesso = td.getTipoProcesso();
			List<TipoDocumento> list = tipoDocumentosMap.get(tipoProcesso);
			if(list != null) {
				list.add(td);
			}
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long tipoEvidenciaId = entity.getId();

		try {
			subperfilService.excluir(tipoEvidenciaId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
			setEntity(null);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void salvar() {

		try {
			List<Situacao> situacoesSelecionadas = new ArrayList<>();
			for(int i=0; i< tiposProcessos.size(); i++) {
				Object list = (Object) selectedSituacoes.get(i);
				Object[] list2 = (Object[]) list;
				for (int j = 0; j < list2.length; j++) {
					situacoesSelecionadas.add((Situacao) list2[j]);
				}
			}

			List<TipoDocumento> tipoDocumentosSelecionados = new ArrayList<>();
			for(int i=0; i< tiposProcessos.size(); i++) {
				Object list = selectedTipoDocumentos.get(i);
				Object[] list2 = (Object[]) list;
				for (int j = 0; j < list2.length; j++) {
					tipoDocumentosSelecionados.add((TipoDocumento) list2[j]);
				}
			}

			boolean insert = isInsert(getEntity());
			Usuario usuario = getUsuarioLogado();

			Set<Long> antigos = new HashSet<>();
			Set<SubperfilSituacao> situacoes2 = entity.getSituacoes();
			for(SubperfilSituacao ps: situacoes2) {
				antigos.add(ps.getSituacao().getId());
			}
			for(Situacao s: situacoesSelecionadas) {
				SubperfilSituacao ps = new SubperfilSituacao();
				Long situacaoId = s.getId();
				if (antigos.contains(situacaoId)) {
					antigos.remove(situacaoId);
					continue;
				}
				ps.setSituacao(s);
				ps.setSubperfil(entity);
				situacoes2.add(ps);
			}
			for (Iterator<SubperfilSituacao> it = situacoes2.iterator(); it.hasNext();) {
				SubperfilSituacao next = it.next();
				Situacao situacao = next.getSituacao();
				Long situacaoId = situacao.getId();
				if (antigos.contains(situacaoId)) {
					it.remove();
				}
			}

			Set<Long> antigos2 = new HashSet<>();
			Set<SubperfilTipoDocumento> tipoDocumentos2 = entity.getTipoDocumentos();
			for(SubperfilTipoDocumento ps: tipoDocumentos2) {
				antigos2.add(ps.getTipoDocumento().getId());
			}
			for(TipoDocumento s: tipoDocumentosSelecionados) {
				SubperfilTipoDocumento ps = new SubperfilTipoDocumento();
				Long tipoDocumentoId = s.getId();
				if (antigos2.contains(tipoDocumentoId)) {
					antigos2.remove(tipoDocumentoId);
					continue;
				}
				ps.setTipoDocumento(s);
				ps.setSubperfil(entity);
				tipoDocumentos2.add(ps);
			}
			for (Iterator<SubperfilTipoDocumento> it = tipoDocumentos2.iterator(); it.hasNext();) {
				SubperfilTipoDocumento next = it.next();
				TipoDocumento tipoDocumento = next.getTipoDocumento();
				Long tipoDocumentoId = tipoDocumento.getId();
				if (antigos2.contains(tipoDocumentoId)) {
					it.remove();
				}
			}
			subperfilService.saveOrUpdate(entity, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}	

	public SubperfilDataModel getDataModel() {
		return dataModel;
	}

	public List<Subperfil> getSubperfis() {
		return subperfis;
	}

	public Subperfil getEntity() {
		return entity;
	}

	public void setEntity(Subperfil entity) {
		if (entity == null) {
			entity = new Subperfil();
		}
		else {
			Long entityId = entity.getId();
			entity = subperfilService.get(entityId);
			Set<SubperfilSituacao> situacoes2 = entity.getSituacoes();
			Set<SubperfilTipoDocumento> tipoDocumentos2 = entity.getTipoDocumentos();
			Hibernate.initialize(situacoes2);
			Hibernate.initialize(tipoDocumentos2);
		}

		situacoes = situacaoService.findAtivas(null);
		tipoDocumentos = tipoDocumentoService.findByTipoProcesso(null, null, null);
		Map<TipoProcesso, List<Situacao>> situacoesMap2 = new HashMap<>();
		Map<TipoProcesso, List<TipoDocumento>> tipoDocumentosMap2 = new HashMap<>();
		for (TipoProcesso tp : tiposProcessos) {
			situacoesMap2.put(tp, new ArrayList<>());
			tipoDocumentosMap2.put(tp, new ArrayList<>());
		}

		Set<SubperfilSituacao> situacoes2 = entity.getSituacoes();
		Set<SubperfilTipoDocumento> tipoDocumentos2 = entity.getTipoDocumentos();

		for(SubperfilSituacao ps: situacoes2) {
			Situacao situacao = ps.getSituacao();
			TipoProcesso tipoProcesso = situacao.getTipoProcesso();
			List<Situacao> situacoes = situacoesMap2.get(tipoProcesso);
			List<Situacao> list = situacoes != null ? situacoes : new ArrayList<>();
			list.add(situacao);
		}
		selectedSituacoes.clear();
		for (TipoProcesso tp : tiposProcessos) {
			List<Situacao> list = situacoesMap2.get(tp);
			selectedSituacoes.add(list);
		}

		for(SubperfilTipoDocumento ps: tipoDocumentos2) {
			TipoDocumento tipoDocumento = ps.getTipoDocumento();
			TipoProcesso tipoProcesso = tipoDocumento.getTipoProcesso();
			List<TipoDocumento> situacoes = tipoDocumentosMap2.get(tipoProcesso);
			List<TipoDocumento> list = situacoes != null ? situacoes : new ArrayList<>();
			list.add(tipoDocumento);
		}
		selectedTipoDocumentos.clear();
		for (TipoProcesso tp : tiposProcessos) {
			List<Situacao> list = situacoesMap2.get(tp);
			selectedSituacoes.add(list);
			List<TipoDocumento> list2 = tipoDocumentosMap2.get(tp);
			selectedTipoDocumentos.add(list2);
		}
		this.entity = entity;
	}

	public List<TipoProcesso> getTipoProcessos(Subperfil subperfil) {
		Set<TipoProcesso> tiposProcessosSet = new HashSet<>();
		Set<SubperfilSituacao> situacoes = subperfil.getSituacoes();
		for (SubperfilSituacao subperfilSituacao : situacoes) {
			Situacao situacao = subperfilSituacao.getSituacao();
			tiposProcessosSet.add(situacao.getTipoProcesso());
		}

		List<TipoProcesso> tiposProcessos = new ArrayList<>(tiposProcessosSet);
		Collections.sort(tiposProcessos, tipoComparator);		
		return tiposProcessos;
	}

	static Comparator<Situacao> situacaoComparator = new Comparator<Situacao>() {
		@Override
		public int compare(Situacao o1, Situacao o2) {
			return o1.getNome().compareTo(o2.getNome());
		}
	};

	static Comparator<TipoDocumento> tipoDocumentoComparator = new Comparator<TipoDocumento>() {
		@Override
		public int compare(TipoDocumento o1, TipoDocumento o2) {
			return o1.getNome().compareTo(o2.getNome());
		}
	};

	static Comparator<TipoProcesso> tipoComparator = new Comparator<TipoProcesso>() {
		@Override
		public int compare(TipoProcesso o1, TipoProcesso o2) {
			return o1.getNome().compareTo(o2.getNome());
		}
	};

	public List<Situacao> getSituacoes(Subperfil subperfil, TipoProcesso tipoProcesso) {
		List<Situacao> situacoes = new LinkedList<>();
		Set<SubperfilSituacao> spss = subperfil.getSituacoes();
		for (SubperfilSituacao subperfilSituacao : spss) {
			Situacao situacao = subperfilSituacao.getSituacao();
			TipoProcesso tp = situacao.getTipoProcesso();
			if (tipoProcesso.equals(tp)) {
				situacoes.add(situacao);
			}
		}

		Collections.sort(situacoes, situacaoComparator);
		return situacoes;
	}

	public List<TipoDocumento> getTipoDocumento(Subperfil subperfil, TipoProcesso tipoProcesso) {
		List<TipoDocumento> tipoDocumentos = new LinkedList<>();
		Set<SubperfilTipoDocumento> spss = subperfil.getTipoDocumentos();
		for (SubperfilTipoDocumento subperfilTipoDocumento : spss) {
			TipoDocumento tipoDocumento = subperfilTipoDocumento.getTipoDocumento();
			TipoProcesso tp = tipoDocumento.getTipoProcesso();
			if (tipoProcesso.equals(tp)) {
				tipoDocumentos.add(tipoDocumento);
			}
		}

		Collections.sort(tipoDocumentos, tipoDocumentoComparator);
		return tipoDocumentos;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public void setSituacoes(List<Situacao> situacoes) {
		this.situacoes = situacoes;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(List<TipoProcesso> tiposProcessos) {
		this.tiposProcessos = tiposProcessos;
	}

	public Map<TipoProcesso, List<Situacao>> getSituacoesMap() {
		return situacoesMap;
	}

	public void setSituacoesMap(Map<TipoProcesso, List<Situacao>> situacoesMap) {
		this.situacoesMap = situacoesMap;
	}


	public Map<TipoProcesso, List<TipoDocumento>> getTipoDocumentosMap() {
		return tipoDocumentosMap;
	}

	public void setTipoDocumentosMap(Map<TipoProcesso, List<TipoDocumento>> tipoDocumentosMap) {
		this.tipoDocumentosMap = tipoDocumentosMap;
	}

	public List<List<Situacao>> getSelectedSituacoes() {
		return selectedSituacoes;
	}

	public void setSelectedSituacoes(List<List<Situacao>> selectedSituacoes) {
		this.selectedSituacoes = selectedSituacoes;
	}

	public List<List<TipoDocumento>> getSelectedTipoDocumentos() {
		return selectedTipoDocumentos;
	}

	public void setSelectedTipoDocumentos(List<List<TipoDocumento>> selectedTipoDocumentos) {
		this.selectedTipoDocumentos = selectedTipoDocumentos;
	}

	public List<FilaConfiguracao> getFilas() {
		return filas;
	}
}
