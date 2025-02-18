package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.CampoModeloOcrDataModel;
import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.CampoModeloOcr;
import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.BaseInternaService;
import net.wasys.getdoc.domain.service.CampoModeloOcrService;
import net.wasys.getdoc.domain.service.ModeloOcrService;
import net.wasys.getdoc.domain.vo.filtro.BaseInternaFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Arrays;
import java.util.List;

@ManagedBean
@ViewScoped
public class CampoModeloOcrCrudBean extends AbstractBean {

	@Autowired private CampoModeloOcrService campoModeloOcrService;
	@Autowired private ModeloOcrService modeloOcrService;
	@Autowired private BaseInternaService baseInternaService;

	private CampoModeloOcrDataModel dataModel;
	private CampoModeloOcr campoModeloOcr;
	private Long modeloOcrId;
	private ModeloOcr  modeloOcr;
	private List<CampoModeloOcr.Operador> operadorList;
	private List<CampoModeloOcr.TipoComparacao> tipoComparacaoList;
	private List<CampoModeloOcr.ValorComparacaoSimilaridade> valorComparacaoSimilaridadeList;

	private List<BaseInterna> baseInternas;

	public void initBean() {

		if(modeloOcr == null) {
			if(modeloOcrId == null) {
				redirect("/cadastros/modelos-ocr/");
				return;
			}
			this.modeloOcr = modeloOcrService.get(modeloOcrId);
			if(this.modeloOcr == null) {
				redirect("/cadastros/modelos-ocr/");
				return;
			}
		}

		dataModel = new CampoModeloOcrDataModel();
		dataModel.setService(campoModeloOcrService);
		dataModel.setModeloOcrId(modeloOcrId);

		campoModeloOcr = new CampoModeloOcr();

		CampoModeloOcr.Operador[] values = CampoModeloOcr.Operador.values();
		operadorList = Arrays.asList(values);

		CampoModeloOcr.TipoComparacao[] tipoComparacaos = CampoModeloOcr.TipoComparacao.values();
		tipoComparacaoList = Arrays.asList(tipoComparacaos);

		CampoModeloOcr.ValorComparacaoSimilaridade[] valorComparacaoSimilaridades = CampoModeloOcr.ValorComparacaoSimilaridade.values();
		valorComparacaoSimilaridadeList = Arrays.asList(valorComparacaoSimilaridades);

		BaseInternaFiltro baseInternaFiltro = new BaseInternaFiltro();
		baseInternaFiltro.setNome("OCR");
		baseInternas = baseInternaService.findByFiltro(baseInternaFiltro, null, null);

	}

	public void salvar() {

		try {
			boolean insert = isInsert(campoModeloOcr);
			Usuario usuario = getUsuarioLogado();

			campoModeloOcr.setDescricao(campoModeloOcr.getDescricao().toUpperCase());
			campoModeloOcr.setLabelOcr(campoModeloOcr.getLabelOcr().toLowerCase());
			campoModeloOcr.setNome(campoModeloOcr.getNome().toUpperCase());

			campoModeloOcr.setModeloOcr(modeloOcr);

			campoModeloOcrService.saveOrUpdate(campoModeloOcr);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");

			campoModeloOcr = new CampoModeloOcr();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}


	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long campoModeloOcrId = campoModeloOcr.getId();

		try {
			campoModeloOcrService.excluir(campoModeloOcrId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}


	public CampoModeloOcrDataModel getDataModel() {
		return dataModel;
	}

	public void setDataModel(CampoModeloOcrDataModel dataModel) {
		this.dataModel = dataModel;
	}

	public CampoModeloOcr getCampoModeloOcr() {
		return campoModeloOcr;
	}

	public void setCampoModeloOcr(CampoModeloOcr campoModeloOcr) {
		if(campoModeloOcr == null) {
			this.campoModeloOcr = new CampoModeloOcr();
		} else {
			this.campoModeloOcr = campoModeloOcr;
			this.campoModeloOcr.setModeloOcr(modeloOcr);
		}
	}

	public Long getModeloOcrId() {
		return modeloOcrId;
	}

	public void setModeloOcrId(Long modeloOcrId) {
		this.modeloOcrId = modeloOcrId;
	}

	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {
		this.modeloOcr = modeloOcr;
	}

	public List<CampoModeloOcr.Operador> getOperadorList() {
		return operadorList;
	}

	public void setOperadorList(List<CampoModeloOcr.Operador> operadorList) {
		this.operadorList = operadorList;
	}

	public List<CampoModeloOcr.TipoComparacao> getTipoComparacaoList() {
		return tipoComparacaoList;
	}

	public void setTipoComparacaoList(List<CampoModeloOcr.TipoComparacao> tipoComparacaoList) {
		this.tipoComparacaoList = tipoComparacaoList;
	}

	public List<CampoModeloOcr.ValorComparacaoSimilaridade> getValorComparacaoSimilaridadeList() {
		return valorComparacaoSimilaridadeList;
	}

	public void setValorComparacaoSimilaridadeList(List<CampoModeloOcr.ValorComparacaoSimilaridade> valorComparacaoSimilaridadeList) {
		this.valorComparacaoSimilaridadeList = valorComparacaoSimilaridadeList;
	}

	public List<BaseInterna> getBaseInternas() {
		return baseInternas;
	}

	public void setBaseInternas(List<BaseInterna> baseInternas) {
		this.baseInternas = baseInternas;
	}
}
