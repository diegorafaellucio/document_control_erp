package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.FeriadoDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.FeriadoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.*;

@ManagedBean
@ViewScoped
public class FeriadoCrudBean extends AbstractBean {

	@Autowired private FeriadoService feriadoService;
	@Autowired private ResourceService resourceService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private FeriadoDataModel dataModel;
	private Feriado feriado;
	private List<Situacao> situacoes;
	private List<TipoProcesso> tiposProcessos;
	private Map<TipoProcesso, List<Situacao>> situacoesMap = new HashMap<>();
	private List<List<Situacao>> selectedSituacoes = new ArrayList<>();
	private Date inicioVigencia;
	private Date fimVigencia;
	private Boolean paralizacao = false;

	private boolean disabled;

	public void initBean() {

		dataModel = new FeriadoDataModel();
		dataModel.setService(feriadoService);
		dataModel.setParalizacao(paralizacao);

		tiposProcessos = tipoProcessoService.findAtivos(null);
		situacoes = situacaoService.findAtivas(null);
		situacoesMap = new HashMap<>();
		for (TipoProcesso tp : tiposProcessos) {
			situacoesMap.put(tp, new ArrayList<>());
		}

		for (Situacao s : situacoes) {
			TipoProcesso tipoProcesso = s.getTipoProcesso();
			List<Situacao> list = situacoesMap.get(tipoProcesso);
			if(list != null) {
				list.add(s);
			}
		}

		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		disabled = StringUtils.isNotBlank(geralEndpoint);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(feriado);
			Usuario usuario = getUsuarioLogado();

			feriado.setParalizacao(false);

			feriadoService.saveOrUpdate(feriado, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void salvarParalizacao() {

		try {
			boolean insert = isInsert(feriado);
			Usuario usuario = getUsuarioLogado();

			List<Situacao> situacoesSelecionadas = new ArrayList<>();
			for(int i=0; i< tiposProcessos.size(); i++) {
				Object list = (Object) selectedSituacoes.get(i);
				Object[] list2 = (Object[]) list;
				for (int j = 0; j < list2.length; j++) {
					situacoesSelecionadas.add((Situacao) list2[j]);
				}
			}

			Set<Long> antigos = new HashSet<>();
			Set<FeriadoSituacao> situacoes2 = feriado.getSituacoes();
			for(FeriadoSituacao ps: situacoes2) {
				antigos.add(ps.getSituacao().getId());
			}
			for(Situacao s: situacoesSelecionadas) {
				FeriadoSituacao fs = new FeriadoSituacao();
				Long situacaoId = s.getId();
				if (antigos.contains(situacaoId)) {
					antigos.remove(situacaoId);
					continue;
				}
				fs.setSituacao(s);
				fs.setFeriado(feriado);
				situacoes2.add(fs);
			}
			for (Iterator<FeriadoSituacao> it = situacoes2.iterator(); it.hasNext();) {
				FeriadoSituacao next = it.next();
				Situacao situacao = next.getSituacao();
				Long situacaoId = situacao.getId();
				if (antigos.contains(situacaoId)) {
					it.remove();
				}
			}

            Calendar cal = Calendar.getInstance();
			cal.setTime(inicioVigencia);
			List<Date> diasParalizacao = new ArrayList<>();
			feriado.setData(inicioVigencia);
			for (Date dt = inicioVigencia; dt.compareTo (fimVigencia) <= 0; ) {
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				diasParalizacao.add(cal.getTime());
				cal.add (Calendar.DATE, +1);
				dt = cal.getTime();
			}

			Set<FeriadoParalizacao> paralizacoes = feriado.getParalizacoes();
			List<Date> antigos2 = new ArrayList<>();
			for (FeriadoParalizacao paralizacao : paralizacoes) {
				Date data = paralizacao.getData();
				antigos2.add(data);
			}
			for (Date date : diasParalizacao) {
				if(antigos2.contains(date)){
					antigos2.remove(date);
					continue;
				}
				FeriadoParalizacao fp = new FeriadoParalizacao();
				fp.setFeriado(feriado);
				fp.setData(date);
				paralizacoes.add(fp);
			}

			for (Iterator<FeriadoParalizacao> it = paralizacoes.iterator(); it.hasNext();) {
				FeriadoParalizacao next = it.next();
				Date data = next.getData();
				if (antigos2.contains(data)) {
					it.remove();
				}
			}

			feriado.setParalizacoes(paralizacoes);
			feriado.setParalizacao(true);

			feriadoService.saveOrUpdate(feriado, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long feriadoId = feriado.getId();

		try {
			feriadoService.excluir(feriadoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}


	public FeriadoDataModel getDataModel() {
		return dataModel;
	}

	public Feriado getFeriado() {
		return feriado;
	}

	public void setFeriado(Feriado feriado) {

		if(feriado == null) {
			feriado = new Feriado();
		} else {
			feriado = feriadoService.get(feriado.getId());
			Set<FeriadoSituacao> situacoes2 = feriado.getSituacoes();
			Set<FeriadoParalizacao> paralizacoes = feriado.getParalizacoes();
			Hibernate.initialize(situacoes2);
			Hibernate.initialize(paralizacoes);
		}

		Set<FeriadoParalizacao> paralizacoes = feriado.getParalizacoes();
		if(paralizacoes != null && !paralizacoes.isEmpty()) {
			for (FeriadoParalizacao paralizacao : paralizacoes) {
				Date data = paralizacao.getData();

				if (inicioVigencia == null) {
					inicioVigencia = data;
				} else if (inicioVigencia.after(data)) {
					inicioVigencia = data;
				}

				if (fimVigencia == null) {
					fimVigencia = data;
				} else if (fimVigencia.before(data)) {
					fimVigencia = data;
				}
			}
		}

		situacoes = situacaoService.findAtivas(null);
		Map<TipoProcesso, List<Situacao>> situacoesMap2 = new HashMap<>();
		for (TipoProcesso tp : tiposProcessos) {
			situacoesMap2.put(tp, new ArrayList<>());
		}

		Set<FeriadoSituacao> situacoes2 = feriado.getSituacoes();

		for(FeriadoSituacao fs: situacoes2) {
			Situacao situacao = fs.getSituacao();
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

		this.feriado = feriado;
	}
	public void buscar() {
		initBean();
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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

	public List<List<Situacao>> getSelectedSituacoes() {
		return selectedSituacoes;
	}

	public void setSelectedSituacoes(List<List<Situacao>> selectedSituacoes) {
		this.selectedSituacoes = selectedSituacoes;
	}

	public Date getInicioVigencia() {
		return inicioVigencia;
	}

	public void setInicioVigencia(Date inicioVigencia) {
		this.inicioVigencia = inicioVigencia;
	}

	public Date getFimVigencia() {
		return fimVigencia;
	}

	public void setFimVigencia(Date fimVigencia) {
		this.fimVigencia = fimVigencia;
	}

	public Boolean getParalizacao() {
		return paralizacao;
	}

	public void setParalizacao(Boolean paralizacao) {
		this.paralizacao = paralizacao;
	}
}
