package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.CalendarioDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.ListaChamada;
import net.wasys.getdoc.domain.enumeration.TipoCalendario;
import net.wasys.getdoc.domain.enumeration.TipoParceiro;
import net.wasys.getdoc.domain.enumeration.TipoProuni;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ManagedBean
@ViewScoped
public class CalendarioCrudBean extends AbstractBean {

	@Autowired private CalendarioService calendarioService;
	@Autowired private CalendarioCriterioService calendarioCriterioService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private CalendarioCriterioSituacaoService calendarioCriterioSituacaoService;
	@Autowired private SituacaoService situacaoService;

	private CalendarioDataModel dataModel;
	private Calendario calendario;
	private CalendarioCriterio calendarioCriterio = new CalendarioCriterio();
	private ScheduleModel lazyModel = new DefaultScheduleModel();
	private List<TipoProcesso> tiposProcesso;
	private Date dataInicioCalendarioValid;
	private Date dataFimCalendarioValid;
	private Date dataFimCalendarioCriterioValid;
	private List<Situacao> calendarioCriterioSituacaoSelecionado;
	private List<Situacao> situacoesSisProuni;

	public void initBean() {

		dataModel = new CalendarioDataModel();
		dataModel.setService(calendarioService);
		tiposProcesso = tipoProcessoService.findByIds(Arrays.asList(TipoProcesso.SIS_PROUNI, TipoProcesso.TE_PROUNI));

		initSituacaoesCalendarioCriteria();
	}

	public void salvar() {

		try {
			boolean insert = isInsert(calendario);
			Usuario usuario = getUsuarioLogado();

			if (validacoesCalendario()) return;

			calendarioService.saveOrUpdate(calendario, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private boolean validacoesCalendario() {

		Date dataInicio = calendario.getDataInicio();
		Date dataFim = calendario.getDataFim();

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dataFim.getTime());
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		dataFim = cal.getTime();
		calendario.setDataFim(dataFim);

		if(dataFim.before(dataInicio)){
			addMessageError("dataInvalida.error", DummyUtils.formatDate(dataFim));
			return true;
		}

		if(dataInicio.after(dataFim)){
			addMessageError("dataInvalida.error", DummyUtils.formatDate(dataInicio));
			return true;
		}

		String periodoIngresso = calendario.getPeriodoIngresso();
		if(!periodoIngresso.matches("\\d{4}\\.\\d")){
			addMessageError("periodoDeIngressoInvalido.error", periodoIngresso);
			return true;
		}

		List<CalendarioCriterio> criterios = calendario.getCriterios();
		if(!criterios.isEmpty()){
			if(!dataInicio.equals(dataInicioCalendarioValid) || !dataFim.equals(dataFimCalendarioValid)) {
				addMessageError("naoPodeAlterarDataCalendarioComCriteriosCadastrados.error", criterios.size());
				return true;
			}
		}

		TipoProcesso tipoProcesso = calendario.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		if(!mostrarCriterioParceiro(tipoProcessoId)){
			calendario.setTipoParceiro(TipoParceiro.NULO);
			calendario.setTipoProuni(TipoProuni.NULO);
		}

		return false;
	}

	public void salvarCriterio() {

		try {
			boolean insert = isInsert(calendarioCriterio);

			if (validacoesCriterio()) return;

			calendarioCriterioService.saveOrUpdate(calendarioCriterio, calendarioCriterioSituacaoSelecionado);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	private boolean validacoesCriterio() {

		Date calendarioCriterioDataInicio = calendarioCriterio.getDataInicio();
		Date calendarioCriterioDataFim = calendarioCriterio.getDataFim();
		Date calendarioDataInicio = calendario.getDataInicio();
		Date calendarioDataFim = calendario.getDataFim();

		if(calendarioCriterioDataInicio.before(calendarioDataInicio)){
			addMessageError("dataInvalida.error", DummyUtils.formatDateTime(calendarioCriterioDataInicio));
			return true;
		}

		if(calendarioCriterioDataFim.after(calendarioDataFim)){
			addMessageError("dataInvalida.error", DummyUtils.formatDateTime(calendarioCriterioDataFim));
			return true;
		}

		if (calendarioCriterioDataFim.before(calendarioCriterioDataInicio)) {
			addMessageError("dataInvalida.error", DummyUtils.formatDateTime(calendarioCriterioDataFim));
			return true;
		}

		if (calendarioCriterioDataFim.after(calendarioCriterioDataFim)) {
			addMessageError("dataInvalida.error", DummyUtils.formatDateTime(calendarioCriterioDataFim));
			return true;
		}

		if((!calendarioCriterioDataFim.equals(dataFimCalendarioCriterioValid)) && calendarioCriterioDataFim.after(new Date())) {
				calendarioCriterio.setExecutado(false);
		}

		return false;
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long calendarioId = calendario.getId();

		try {
			calendarioService.excluir(calendarioId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluirCriterio() {

		Long calendarioId = calendarioCriterio.getId();

		try {
			calendarioCriterioService.deleteById(calendarioId);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}


	public CalendarioDataModel getDataModel() {
		return dataModel;
	}

	public Calendario getCalendario() {
		return calendario;
	}

	public void setCalendario(Calendario calendario) {
		if(calendario == null) {
			calendario = new Calendario();
		} else {
			Long calendarioId = calendario.getId();
			calendario = calendarioService.get(calendarioId);
			this.dataInicioCalendarioValid = calendario.getDataInicio();
			this.dataFimCalendarioValid = calendario.getDataFim();
		}

		this.calendario = calendario;
	}

	public CalendarioCriterio getCalendarioCriterio() {
		return calendarioCriterio;
	}

	public void setCalendarioCriterio(CalendarioCriterio calendarioCriterio) {
		if(calendarioCriterio == null) {
			calendarioCriterio = new CalendarioCriterio();
		} else {
			Long calendarioCriterioId = calendarioCriterio.getId();
			calendarioCriterio = calendarioCriterioService.get(calendarioCriterioId);
			Calendario calendario = calendarioCriterio.getCalendario();
			Long calendarioId = calendario.getId();
			calendario = calendarioService.get(calendarioId);
			this.calendario = calendario;
			this.dataFimCalendarioCriterioValid = calendarioCriterio.getDataFim();
			this.calendarioCriterioSituacaoSelecionado = calendarioCriterioSituacaoService.findSituacaoByCalendarioCriterio(calendarioCriterio);
		}

		this.calendarioCriterio = calendarioCriterio;
	}

	public void setCriterio(Calendario calendario) {
		this.calendario = calendario;
		calendarioCriterio = new CalendarioCriterio();
		calendarioCriterio.setCalendario(calendario);
	}

	private void initSituacaoesCalendarioCriteria() {
		if(CollectionUtils.isEmpty(this.situacoesSisProuni)) {
			SituacaoFiltro situacaoFiltro = new SituacaoFiltro();
			situacaoFiltro.setTipoProcessoId(TipoProcesso.SIS_PROUNI);
			situacaoFiltro.setAtiva(true);
			List<Situacao> situacoes = situacaoService.findByFiltro(situacaoFiltro, null, null);
			this.situacoesSisProuni = situacoes;
		}
	}

	public ScheduleModel getLazyModel(List<CalendarioCriterio> calendarioCriterio) {
		lazyModel.clear();
		for(CalendarioCriterio criterio : calendarioCriterio) {

			TipoCalendario tipoCalendario = criterio.getTipoCalendario();
			ListaChamada chamada = criterio.getChamada();
			String labelTipoCalendario = "TipoCalendario." + tipoCalendario + ".label";
			String msgTipoCalendario = getMessage(labelTipoCalendario);
			String labelChamada = "ListaChamada." + chamada + ".label";
			String msgChamada = getMessage(labelChamada);
			Date dataInicio = criterio.getDataInicio();
			Date dataFim = criterio.getDataFim();

			String name = msgChamada + " - " + msgTipoCalendario;
			DefaultScheduleEvent event  = DefaultScheduleEvent.builder()
					.title(name)
					.data(dataInicio)
					.data(dataFim)
					.build();


			if (TipoCalendario.DIVULGACAO_PRE_SELECIONADOS.equals(tipoCalendario)) {
				event.setStyleClass(TipoCalendario.DIVULGACAO_PRE_SELECIONADOS.toString());
			} else if (TipoCalendario.RECEBER_DOCUMENTOS.equals(tipoCalendario)) {
				event.setStyleClass(TipoCalendario.RECEBER_DOCUMENTOS.toString());
			} else {
				event.setStyleClass(TipoCalendario.REGISTRO_EMISSAO_TERMO.toString());
				event.setDescription ( "<script> alert ('Prazo final para o candidato ter a documentação aprovada e realizar a emissão do termo e todos processos a partir desta data será solicitada a emissão de TR.'); </script> " );
			}

			lazyModel.addEvent(event);
		}
		return lazyModel;
	}

	public List<TipoProcesso> getTiposProcesso() {
		return tiposProcesso;
	}

	public Boolean mostrarCriterioParceiro(Long tipoProcessoId) {
		if(TipoProcesso.TE_PROUNI.equals(tipoProcessoId)){
			return false;
		}
		return true;
	}

	public List<Situacao> getCalendarioCriterioSituacaoSelecionado() {
		return calendarioCriterioSituacaoSelecionado;
	}

	public void setCalendarioCriterioSituacaoSelecionado(List<Situacao> calendarioCriterioSituacaoSelecionado) {
		this.calendarioCriterioSituacaoSelecionado = calendarioCriterioSituacaoSelecionado;
	}

	public List<Situacao> getSituacoesSisProuni() {
		return situacoesSisProuni;
	}

}
