package net.wasys.getdoc.bean;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.TipoDocumentoFiltro;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.TipoDocumentoDataModel;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class TipoDocumentoCrudBean extends AbstractBean {

	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private ModeloOcrService modeloOcrService;
	@Autowired private ModeloDocumentoService modeloDocumentoService;
	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private TipoPrazoService tipoPrazoService;
	@Autowired private IrregularidadeService irregularidadeService;
	@Autowired private IrregularidadeTipoDocumentoService irregularidadeTipoDocumentoService;
	@Autowired private CategoriaDocumentoService categoriaDocumentoService;
	@Autowired private GrupoModeloDocumentoService grupoModeloDocumentoService;

	private TipoDocumentoDataModel dataModel;
	private Long tipoProcessoId;
	private TipoProcesso tipoProcesso;
	private TipoDocumento tipoDocumento = new TipoDocumento();
	private List<ModeloOcr> modelosOcr;
	private List<RegistroValorVO> baseRegistroVO = new ArrayList<>();
	private BigDecimal prazo;
	private List<ModeloDocumento> modelosDocumentos;
	private List<ModeloDocumento> modelosDocumentosSelecionados;
	private List<Irregularidade> irregularidadeList;
	private List<Irregularidade> irregularidadeSelecionadas;
	private TipoExpiracao tipoExpiracaoDocumento;
	private List<ModeloDocumento> modelosDocumentosSelecionadosParaExpiracao;
	private List<CategoriaDocumento> categoriasDocumento;
	private List<CategoriaDocumento> categoriasDocumentoSelecionadas;

	private List<GrupoModeloDocumento> gruposModeloDocumento;
	private List<GrupoModeloDocumento> gruposModeloDocumentoSelecionados;
	private List<GrupoModeloDocumento> gruposModeloDocumentosSelecionadosParaExpiracao;
	private TipoDocumentoFiltro filtro;

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

		if(filtro == null) {
			filtro = new TipoDocumentoFiltro();
		}

		filtro.setTipoProcessoId(tipoProcessoId);

		dataModel = new TipoDocumentoDataModel();
		dataModel.setService(tipoDocumentoService);
		dataModel.setFiltro(filtro);

		modelosOcr = modeloOcrService.findAtivos();
		categoriasDocumento = categoriaDocumentoService.findAtivos();
		modelosDocumentos = modeloDocumentoService.findAtivos();

		gruposModeloDocumento = grupoModeloDocumentoService.findAll();

		buscarInformacoesBaseRegistro();

		this.irregularidadeList = irregularidadeService.findAtivas();
	}

	private void buscarInformacoesBaseRegistro(){
		BaseInterna baseInterna = baseInternaService.get(BaseInterna.TAXONOMIA);
		if(baseInterna != null) {
			BaseRegistroFiltro filtro = new BaseRegistroFiltro();
			filtro.setBaseInterna(baseInterna);
			baseRegistroVO = baseRegistroService.findByFiltro(filtro, null, null);
		}
	}

	public void salvar() {

		if (prazo != null) {
			TipoPrazo tipoPrazo = tipoDocumento.getTipoPrazo();
			BigDecimal horasDecimal = tipoPrazoService.converterPrazoParaHorasDecimal(prazo, tipoPrazo);
			tipoDocumento.setHorasPrazo(horasDecimal);
		}else {
			tipoDocumento.setHorasPrazo(null);
		}

		try {
			boolean insert = isInsert(tipoDocumento);
			Usuario usuario = getUsuarioLogado();

			boolean isValidadeExpiracaoPorModeloDocumento = Arrays.asList(TipoExpiracao.ALGUNS, TipoExpiracao.TODOS).contains(tipoExpiracaoDocumento) ? true : false;
			if(Arrays.asList(TipoExpiracao.TODOS, TipoExpiracao.NAO).contains(tipoExpiracaoDocumento)) modelosDocumentosSelecionadosParaExpiracao = new ArrayList<>();

			tipoDocumento.setTipoProcesso(tipoProcesso);
			tipoDocumento.setRequisitarDataPorModeloDocumento(isValidadeExpiracaoPorModeloDocumento);

			if(irregularidadeSelecionadas == null) irregularidadeSelecionadas = new ArrayList<>();

			Set<Irregularidade> set = new LinkedHashSet<>(irregularidadeSelecionadas);
			tipoDocumentoService.saveOrUpdate(tipoDocumento, usuario, modelosDocumentosSelecionados, modelosDocumentosSelecionadosParaExpiracao, categoriasDocumentoSelecionadas, new ArrayList<>(set), gruposModeloDocumentoSelecionados, gruposModeloDocumentosSelecionadosParaExpiracao);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
			initBean();
			String path = getContextPath();
			redirect(path + "/cadastros/tipos-processos/documentos/" + tipoProcessoId);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long tipoDocumentoId = tipoDocumento.getId();

		try {
			tipoDocumentoService.excluir(tipoDocumentoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void subirOrdem(TipoDocumento tipoDocumento) {

		Usuario usuario = getUsuarioLogado();
		tipoDocumentoService.subirOrdem(tipoDocumento, usuario);
	}

	public void descerOrdem(TipoDocumento tipoDocumento) {

		Usuario usuario = getUsuarioLogado();
		tipoDocumentoService.descerOrdem(tipoDocumento, usuario);
	}

	public void ajustarExpiracao(String elemento){
		Boolean requisitarDataEmissao = tipoDocumento.getRequisitarDataEmissao();
		Boolean requisitarDataValidadeExpiracao = tipoDocumento.getRequisitarDataValidadeExpiracao();
		Integer validadeExpiracao = tipoDocumento.getValidadeExpiracao();
		switch (elemento){
			case "tempoValidadeExpiracao":
				if(validadeExpiracao > 0){
					tipoDocumento.setRequisitarDataValidadeExpiracao(false);
					tipoDocumento.setRequisitarDataEmissao(true);
				}
				break;
			case "requisitarDataValidadeExpiracao":
				tipoExpiracaoDocumento = TipoExpiracao.NAO;
				if(requisitarDataEmissao){
					tipoDocumento.setRequisitarDataEmissao(false);
					tipoDocumento.setValidadeExpiracao(0);
				}
				break;
			case "requisitarDataEmissao":
				tipoExpiracaoDocumento = TipoExpiracao.NAO;
				if(requisitarDataValidadeExpiracao){
					tipoDocumento.setRequisitarDataValidadeExpiracao(false);
				}
				else{
					tipoDocumento.setValidadeExpiracao(0);
				}
				break;
		}
	}

	private void defineConfiguracaoTipoExpiracaoDocumento(TipoDocumento tipoDocumento) {

		boolean validadeExpiracaoPorModeloDocumento = tipoDocumento.getRequisitarDataPorModeloDocumento();
		if((modelosDocumentosSelecionadosParaExpiracao == null || modelosDocumentosSelecionadosParaExpiracao.isEmpty()) && validadeExpiracaoPorModeloDocumento) {
			this.tipoExpiracaoDocumento = TipoExpiracao.TODOS;
		}
		else if(modelosDocumentosSelecionadosParaExpiracao != null && !modelosDocumentosSelecionadosParaExpiracao.isEmpty()) {
			this.tipoExpiracaoDocumento = TipoExpiracao.ALGUNS;
		}
		else {
			this.tipoExpiracaoDocumento = TipoExpiracao.NAO;
		}
	}

	public String getCorIrregularidade(Long irregularidadeid) {
		if (irregularidadeid == null) {
			return null;
		}
		Irregularidade irregularidade = irregularidadeService.get(irregularidadeid);
		String corIrregularidade = "";
		String isPastaAmarela = "#cfcc1f";
		String isNotPastaAmarela = "#fff";

		if(irregularidade != null) {
			boolean irregularidadePastaAmarela = irregularidade.getIrregularidadePastaAmarela();
			corIrregularidade = irregularidadePastaAmarela ? isPastaAmarela : isNotPastaAmarela;
		}

		return corIrregularidade;
	}

	public TipoDocumentoDataModel getDataModel() {
		return dataModel;
	}

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {

		if(tipoDocumento == null) {
			this.tipoDocumento = new TipoDocumento();
		} else {
			this.tipoDocumento = tipoDocumento;
			this.prazo = tipoDocumento.getHorasPrazo();
			Long tipoDocumentoId = tipoDocumento.getId();

			this.modelosDocumentosSelecionados = tipoDocumentoService.findModelosDocumentos(tipoDocumentoId);
			this.modelosDocumentosSelecionadosParaExpiracao = tipoDocumentoService.findModelosDocumentoToRequisitarExpiracao(tipoDocumentoId);
			this.categoriasDocumentoSelecionadas = tipoDocumentoService.findCategoriasDocumento(tipoDocumentoId);
			this.irregularidadeSelecionadas = irregularidadeTipoDocumentoService.findIrregularidadesByTipoDocumentoId(tipoDocumentoId);
			this.gruposModeloDocumentoSelecionados = tipoDocumentoService.findGruposModeloDocumento(tipoDocumentoId);
			this.gruposModeloDocumentosSelecionadosParaExpiracao = tipoDocumentoService.findGruposModeloDocumentoToRequisitarExpiracao(tipoDocumentoId);

			defineConfiguracaoTipoExpiracaoDocumento(tipoDocumento);
		}
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

	public List<ModeloOcr> getModelosOcr() {
		return modelosOcr;
	}

	public List<ModeloDocumento> getModelosDocumentos() {
		return modelosDocumentos;
	}

	public List<ModeloDocumento> getModelosDocumentosSelecionados() {
		return modelosDocumentosSelecionados;
	}

	public void setModelosDocumentosSelecionados(List<ModeloDocumento> modelosDocumentosSelecionados) {
		this.modelosDocumentosSelecionados = modelosDocumentosSelecionados;
	}

	public List<RegistroValorVO> getBaseRegistroVO() {
		return baseRegistroVO;
	}

	public BigDecimal getPrazo() {

		BigDecimal horasPrazo = tipoDocumento.getHorasPrazo();
		TipoPrazo tipoPrazo = tipoDocumento.getTipoPrazo();

		if(horasPrazo != null) {
			prazo = tipoPrazoService.calcularPrazo(horasPrazo, tipoPrazo);
		}

		return prazo;
	}

	public void setPrazo(BigDecimal prazo) {
		this.prazo = prazo;
	}

	public List<Irregularidade> getIrregularidadeList() {
		return irregularidadeList;
	}

	public List<Irregularidade> getIrregularidadesAutoComplete(String query) {

		String queryLowerCase = query.toLowerCase();
		List<Irregularidade> irregularidadesFiltradas =  irregularidadeList.stream().filter(t -> t.getNome().toLowerCase().contains(queryLowerCase)).collect(Collectors.toList());

		return irregularidadesFiltradas;
	}

	public List<Irregularidade> getIrregularidadeSelecionadas() {
		return irregularidadeSelecionadas;
	}

	public void setIrregularidadeSelecionadas(List<Irregularidade> irregularidadeSelecionadas) {
		this.irregularidadeSelecionadas = irregularidadeSelecionadas;
	}

	public TipoExpiracao getTipoExpiracaoDocumento() {
		return tipoExpiracaoDocumento;
	}

	public void setTipoExpiracaoDocumento(TipoExpiracao tipoExpiracaoDocumento) {
		this.tipoExpiracaoDocumento = tipoExpiracaoDocumento;
	}

	public List<ModeloDocumento> getModelosDocumentosSelecionadosParaExpiracao() {
		return modelosDocumentosSelecionadosParaExpiracao;
	}

	public void setModelosDocumentosSelecionadosParaExpiracao(List<ModeloDocumento> modelosDocumentosSelecionadosParaExpiracao) {
		this.modelosDocumentosSelecionadosParaExpiracao = modelosDocumentosSelecionadosParaExpiracao;
	}

	public List<CategoriaDocumento> getCategoriasDocumento() {
		return categoriasDocumento;
	}

	public void setCategoriasDocumento(List<CategoriaDocumento> categoriasDocumento) {
		this.categoriasDocumento = categoriasDocumento;
	}

	public List<CategoriaDocumento> getCategoriasDocumentoSelecionadas() {
		return categoriasDocumentoSelecionadas;
	}

	public void setCategoriasDocumentoSelecionadas(List<CategoriaDocumento> categoriasDocumentoSelecionadas) {
		this.categoriasDocumentoSelecionadas = categoriasDocumentoSelecionadas;
	}

	public enum TipoExpiracao {
		TODOS,
		ALGUNS,
		NAO,
		;
	}

	public List<GrupoModeloDocumento> getGruposModeloDocumento() {
		return gruposModeloDocumento;
	}

	public void setGruposModeloDocumento(List<GrupoModeloDocumento> gruposModeloDocumento) {
		this.gruposModeloDocumento = gruposModeloDocumento;
	}

	public List<GrupoModeloDocumento> getGruposModeloDocumentoSelecionados() {
		return gruposModeloDocumentoSelecionados;
	}

	public void setGruposModeloDocumentoSelecionados(List<GrupoModeloDocumento> gruposModeloDocumentoSelecionados) {
		this.gruposModeloDocumentoSelecionados = gruposModeloDocumentoSelecionados;
	}

	public List<GrupoModeloDocumento> getGruposModeloDocumentosSelecionadosParaExpiracao() {
		return gruposModeloDocumentosSelecionadosParaExpiracao;
	}

	public void setGruposModeloDocumentosSelecionadosParaExpiracao(List<GrupoModeloDocumento> gruposModeloDocumentosSelecionadosParaExpiracao) {
		this.gruposModeloDocumentosSelecionadosParaExpiracao = gruposModeloDocumentosSelecionadosParaExpiracao;
	}

	public TipoDocumentoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(TipoDocumentoFiltro filtro) {
		this.filtro = filtro;
	}
}