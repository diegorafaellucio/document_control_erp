package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.EtapaDataModel;
import net.wasys.getdoc.domain.entity.Etapa;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.FaseEtapa;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import net.wasys.getdoc.domain.service.EtapaService;
import net.wasys.getdoc.domain.service.TipoPrazoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ManagedBean
@ViewScoped
public class EtapaCrudBean extends AbstractBean {

	@Autowired private EtapaService etapaService;
	@Autowired private TipoPrazoService tipoPrazoService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private Map<TipoProcesso, EtapaDataModel> dataModelMap = new LinkedHashMap<TipoProcesso, EtapaDataModel>();
	private Etapa etapa;
	private BigDecimal prazo;
	private BigDecimal prazoAdvertir;
	private List<TipoProcesso> todosTiposProcessos;
	private Long tipoProcessoId;
	private List<TipoProcesso> tiposProcessos;
	private List<FaseEtapa> faseEtapaList;

    protected void initBean() {

		todosTiposProcessos = tipoProcessoService.findAll(null, null);

    	if(tipoProcessoId != null){
			tiposProcessos = Arrays.asList(tipoProcessoService.get(tipoProcessoId));
		}else {
			tiposProcessos = tipoProcessoService.findAll(null, null);
		}
		dataModelMap.clear();
		for (TipoProcesso tp : tiposProcessos) {
			Long tipoProcessoId = tp.getId();
			EtapaDataModel dataModel = new EtapaDataModel();
			dataModel.setTipoProcessoId(tipoProcessoId);
			dataModel.setService(etapaService);
			dataModelMap.put(tp, dataModel);
		}

		this.faseEtapaList = Arrays.asList(FaseEtapa.values());
	}

	public void salvar() {

		try {
			boolean insert = isInsert(etapa);
			Usuario usuario = getUsuarioLogado();

			TipoPrazo tipoPrazo = etapa.getTipoPrazo();
			BigDecimal horasDecimal = tipoPrazoService.converterPrazoParaHorasDecimal(prazo, tipoPrazo);
			etapa.setHorasPrazo(horasDecimal);

			TipoPrazo tipoPrazoAdvertir = etapa.getTipoPrazoAdvertir();
			BigDecimal horasAdvertirDecimal = tipoPrazoService.converterPrazoParaHorasDecimal(prazoAdvertir, tipoPrazoAdvertir);
			etapa.setHorasPrazoAdvertir(horasAdvertirDecimal);

			etapaService.saveOrUpdate(etapa, usuario);

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		try {
			etapaService.excluir(etapa, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public Map<TipoProcesso, EtapaDataModel> getDataModelMap() {
		return dataModelMap;
	}

	public Etapa getEtapa() {
		return etapa;
	}

	public void setEtapa(Etapa etapa) {
    	if(etapa == null) {
			this.etapa = new Etapa();
		}
		else {
			Long etapasId = etapa.getId();
			etapa = etapaService.get(etapasId);
			this.etapa = etapa;
		}
    }

	public BigDecimal getPrazo() {

		if(etapa != null) {
			BigDecimal horasPrazo = etapa.getHorasPrazo();
			TipoPrazo tipoPrazo = etapa.getTipoPrazo();

			if (horasPrazo != null) {
				prazo = tipoPrazoService.calcularPrazo(horasPrazo, tipoPrazo);
			}
		}

		return prazo;
	}

	public int calcPrazo(BigDecimal horasPrazo, TipoPrazo tipoPrazo) {
    	if(horasPrazo == null || tipoPrazo == null) return 0;
		return tipoPrazoService.calcularPrazo(horasPrazo, tipoPrazo).intValue();
	}

	public void setPrazo(BigDecimal prazo) {
		this.prazo = prazo;
	}

	public BigDecimal getPrazoAdvertir() {

		if(etapa != null) {
			BigDecimal horasPrazoAdvertir = etapa.getHorasPrazoAdvertir();
			TipoPrazo tipoPrazoAdvertir = etapa.getTipoPrazoAdvertir();

			if (horasPrazoAdvertir != null) {
				prazoAdvertir = tipoPrazoService.calcularPrazo(horasPrazoAdvertir, tipoPrazoAdvertir);
			}
		}

		return prazoAdvertir;
	}

	public void setPrazoAdvertir(BigDecimal prazoAdvertir) {
		this.prazoAdvertir = prazoAdvertir;
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

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public List<FaseEtapa> getFaseEtapaList() {
		return faseEtapaList;
	}
}
