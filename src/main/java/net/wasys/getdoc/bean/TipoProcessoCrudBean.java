package net.wasys.getdoc.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.TipoProcessoDataModel;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.TipoProcessoPermissao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.TipoPrazoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class TipoProcessoCrudBean extends AbstractBean {

	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoPrazoService tipoPrazoService;

	private Long tipoProcessoId;
	private TipoProcessoDataModel dataModel;
	private TipoProcesso tipoProcesso;
	private List<String> permissoesSelecionadas;
	private List<Situacao> situacoes;
	private List<Situacao> situacoesConclusao;
	private BigDecimal prazo;
	private BigDecimal prazoAdvertir;
    private List<String> permissoeseEnvioFarolRegra;

    protected void initBean() {

		dataModel = new TipoProcessoDataModel();
		dataModel.setService(tipoProcessoService);

		situacoes = situacaoService.findAtivas(null);
		situacoesConclusao = situacaoService.findAtivas(null);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(tipoProcesso);
			Usuario usuario = getUsuarioLogado();

			TipoPrazo tipoPrazo = tipoProcesso.getTipoPrazo();
			BigDecimal horasDecimal = tipoPrazoService.converterPrazoParaHorasDecimal(prazo, tipoPrazo);
			tipoProcesso.setHorasPrazo(horasDecimal);

			TipoPrazo tipoPrazoAdvertir = tipoProcesso.getTipoPrazoAdvertir();
			BigDecimal horasAdvertirDecimal = tipoPrazoService.converterPrazoParaHorasDecimal(prazoAdvertir, tipoPrazoAdvertir);
			tipoProcesso.setHorasPrazoAdvertir(horasAdvertirDecimal);

            tipoProcesso.setPermissoeseEnvioFarolRegra(DummyUtils.listToString(permissoeseEnvioFarolRegra));
			tipoProcessoService.saveOrUpdate(tipoProcesso, permissoesSelecionadas, usuario);

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
			tipoProcessoService.excluir(tipoProcessoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void setDuplicar(TipoProcesso tipoProcesso) {

		try {
			Usuario usuario = getUsuarioLogado();

			tipoProcessoService.duplicar(tipoProcesso, usuario);

			addMessage("registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public TipoProcessoDataModel getDataModel() {
		return dataModel;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {

		List<String> permissoesSelecionadas = new ArrayList<>();

		if(tipoProcesso == null) {
			this.tipoProcesso = new TipoProcesso();

			for (PermissaoTP p : PermissaoTP.values()) {
				permissoesSelecionadas.add(p.name());
			}

			situacoes = new ArrayList<>();
			situacoesConclusao = new ArrayList<>();
		}
		else {
			Long tipoProcessoId = tipoProcesso.getId();
			tipoProcesso = tipoProcessoService.get(tipoProcessoId);
			this.tipoProcesso = tipoProcesso;

			Set<TipoProcessoPermissao> permissoes = tipoProcesso.getPermissoes();
			for (TipoProcessoPermissao tpp : permissoes) {
				PermissaoTP permissao = tpp.getPermissao();
				permissoesSelecionadas.add(permissao.name());
			}

			situacoes = situacaoService.findByTipoProcesso(tipoProcessoId);
			situacoesConclusao = situacaoService.findByTipoProcesso(tipoProcessoId);
		}

		this.permissoesSelecionadas = permissoesSelecionadas;
		if(tipoProcesso != null){
			this.permissoeseEnvioFarolRegra = DummyUtils.stringToList(tipoProcesso.getPermissoeseEnvioFarolRegra(), String.class);
		}
    }

	public List<String> getPermissoesSelecionadas() {
		return permissoesSelecionadas;
	}

	public void setPermissoesSelecionadas(List<String> permissoesSelecionadas) {
		this.permissoesSelecionadas = permissoesSelecionadas;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public List<Situacao> getSituacoesConclusao() {
		return situacoesConclusao;
	}

	public BigDecimal getPrazo() {

		if(tipoProcesso != null) {
			BigDecimal horasPrazo = tipoProcesso.getHorasPrazo();
			TipoPrazo tipoPrazo = tipoProcesso.getTipoPrazo();

			if (horasPrazo != null) {
				prazo = tipoPrazoService.calcularPrazo(horasPrazo, tipoPrazo);
			}
		}

		return prazo;
	}

	public void setPrazo(BigDecimal prazo) {
		this.prazo = prazo;
	}

	public BigDecimal getPrazoAdvertir() {

		if(tipoProcesso != null) {
			BigDecimal horasPrazoAdvertir = tipoProcesso.getHorasPrazoAdvertir();
			TipoPrazo tipoPrazoAdvertir = tipoProcesso.getTipoPrazoAdvertir();

			if (horasPrazoAdvertir != null) {
				prazoAdvertir = tipoPrazoService.calcularPrazo(horasPrazoAdvertir, tipoPrazoAdvertir);
			}
		}

		return prazoAdvertir;
	}

	public void setPrazoAdvertir(BigDecimal prazoAdvertir) {
		this.prazoAdvertir = prazoAdvertir;
	}

    public List<String> getPermissoeseEnvioFarolRegra() {
        return permissoeseEnvioFarolRegra;
    }

    public void setPermissoeseEnvioFarolRegra(List<String> permissoeseEnvioFarolRegra) {
        this.permissoeseEnvioFarolRegra = permissoeseEnvioFarolRegra;
    }
}
