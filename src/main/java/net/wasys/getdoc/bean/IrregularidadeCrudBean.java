package net.wasys.getdoc.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.IrregularidadeFiltro;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.IrregularidadeDataModel;
import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.IrregularidadeTipoDocumentoService;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ManagedBean
@ViewScoped
public class IrregularidadeCrudBean extends AbstractBean {

	@Autowired private IrregularidadeService irregularidadeService;
	@Autowired private IrregularidadeTipoDocumentoService irregularidadeTipoDocumentoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;


	private IrregularidadeDataModel dataModel;
	private Irregularidade irregularidade = new Irregularidade();
	private Map<TipoProcesso, List<TipoDocumento>> mapDocumentosAssociados;
	private IrregularidadeFiltro filtro = new IrregularidadeFiltro();
	private List <TipoProcesso> todosTiposProcessos;
	private List <TipoDocumento> tipoDocumentos;



	protected void initBean() {

		todosTiposProcessos = tipoProcessoService.findAll(null,null);
		filtro.setAtiva(true);
		filtro.setPastaAmarela(IrregularidadeFiltro.PastaAmarela.AMBOS);

		buscar();
	}

	public void buscar(){

		setSimNaoAmbos();

		dataModel = new IrregularidadeDataModel();
		dataModel.setService(irregularidadeService);
		dataModel.setFiltro(filtro);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(irregularidade);
			Usuario usuario = getUsuarioLogado();

			irregularidadeService.saveOrUpdate(irregularidade, usuario);

			this.mapDocumentosAssociados = irregularidadeTipoDocumentoService.getMapTipoDocumentoByIrregularidades(irregularidade);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long irregularidadeId = irregularidade.getId();

		try {
			irregularidadeService.excluir(irregularidadeId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void setSimNaoAmbos() {
		IrregularidadeFiltro.PastaAmarela pastaAmarela = filtro.getPastaAmarela();

		if(pastaAmarela.equals(IrregularidadeFiltro.PastaAmarela.SIM)){
			filtro.setIrregularidadePastaAmarela(true);
		}else if(pastaAmarela.equals(IrregularidadeFiltro.PastaAmarela.NAO)){
			filtro.setIrregularidadePastaAmarela(false);
		}else if(pastaAmarela.equals(IrregularidadeFiltro.PastaAmarela.AMBOS)){
			filtro.setIrregularidadePastaAmarela(null);
		}
	}

	public IrregularidadeDataModel getDataModel() {
		return dataModel;
	}

	public Irregularidade getIrregularidade() {
		return irregularidade;
	}

	public void setIrregularidade(Irregularidade irregularidade) {

		if(irregularidade == null) {
			irregularidade = new Irregularidade();
			this.mapDocumentosAssociados = null;
		}
		else {
			this.mapDocumentosAssociados = irregularidadeTipoDocumentoService.getMapTipoDocumentoByIrregularidades(irregularidade);
		}

		this.irregularidade = irregularidade;
	}

	public List<TipoProcesso> getTiposProcessos() {
		if (mapDocumentosAssociados != null) {
			Set<TipoProcesso> tipoProcessos = mapDocumentosAssociados.keySet();
			return new ArrayList<>(tipoProcessos);
		}
		return null;
	}

	public List<TipoDocumento> getTipoDocumento(TipoProcesso tipoProcesso) { ;
		List<TipoDocumento> tipoDocumentoList = mapDocumentosAssociados.get(tipoProcesso);
		return tipoDocumentoList;
	}

	public void findTipoDocumento() {
		tipoDocumentos = tipoDocumentoService.findByTipoProcesso(filtro.getIdProcessoEscolhido(), null, null);
	}

	public Map<TipoProcesso, List<TipoDocumento>> getMapDocumentosAssociados() {
		return mapDocumentosAssociados;
	}

	public IrregularidadeFiltro getFiltro() {
		return filtro;
	}

	public List<TipoProcesso> getTodosTiposProcessos() {
		return todosTiposProcessos;
	}

	public void setTodosTiposProcessos(List<TipoProcesso> todosTiposProcessos) {
		this.todosTiposProcessos = todosTiposProcessos;
	}

	public List<TipoDocumento> getTipoDocumentos() {
		return tipoDocumentos;
	}

	public void setTipoDocumentos(List<TipoDocumento> tipoDocumentos) {
		this.tipoDocumentos = tipoDocumentos;
	}


}
