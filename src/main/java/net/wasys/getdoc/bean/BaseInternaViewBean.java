package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.BaseRegistroDataModel;
import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.BaseRelacionamento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.ColunaNovaVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@ManagedBean
@ViewScoped
public class BaseInternaViewBean extends AbstractBean {

	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private BaseRegistroValorService baseRegistroValorService;
	@Autowired private BaseRelacionamentoService baseRelacionamentoService;
	@Autowired private ApplicationContext applicationContext;

	private Long baseInternaId;
	private BaseInterna baseInterna;
	private BaseRegistroDataModel dataModel;
	private BaseRegistroFiltro filtro = new BaseRegistroFiltro();

	private ObjectMapper om = new ObjectMapper();
	private List<String> colunasUnicidade;
	private List<String> colunas;
	private List<ColunaNovaVO> colunasNovas = new ArrayList<>(0);
	private RegistroValorVO registroValorVO;
	private List<BaseRelacionamento> relacionamentos;
	private BaseInternaExporter exporter;

	@SuppressWarnings("unchecked")
	public void initBean() {

		baseInterna = baseInternaService.get(baseInternaId);
		relacionamentos = baseRelacionamentoService.findByBaseInterna(baseInternaId);

		carregarColunasBaseInterna();

		filtro.setBaseInterna(baseInterna);
		filtro.setQntColunas(colunas.size());
		filtro.setAtivo(true);

		dataModel = new BaseRegistroDataModel();
		dataModel.setService(baseRegistroService);
		dataModel.setFiltro(filtro);
	}

	public void buscar() {

		Map<String, String> colunaValor = carregarValoresBusca();

		filtro.setUsarLikeCampos(true);
		filtro.getCamposFiltro().clear();
		for (String coluna : colunaValor.keySet()) {
			String valor = colunaValor.get(coluna);
			if(StringUtils.isNotBlank(valor)) {
				valor = StringUtils.trim(valor);
				filtro.addCampoFiltro(coluna, valor);
			}
		}
	}

	public void salvar() {

		try {
			Map<String, String> colunaValor = new LinkedHashMap<>();
			criarColunasNovas(colunaValor);

			BaseRegistro baseRegistro = registroValorVO.getBaseRegistro();
			Long registroId = baseRegistro.getId();
			boolean insert = registroId == null;

			baseRegistro.setBaseInterna(baseInterna);

			Usuario usuario = getUsuarioLogado();
			carregarValores("registro_", colunaValor);
			baseRegistroService.saveOrUpdate(baseRegistro, colunaValor, usuario);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarColunasBaseInterna();
		carregarRegistroValorVOSelecionado();
	}

	private void criarColunasNovas(Map<String, String> colunaValor) {

		if (CollectionUtils.isNotEmpty(colunasNovas)) {
			for (ColunaNovaVO colunaNova : colunasNovas) {
				baseRegistroService.adicionarColuna(baseInterna, colunaNova);
				String nome = colunaNova.getNome();
				String valor = colunaNova.getValor();
				colunaValor.put(nome, valor);
			}
			colunasNovas.clear();
		}
	}

	public void excluir() {
		try {
			Long registroId = registroValorVO.getRegistroId();
			baseRegistroService.excluir(registroId, getUsuarioLogado());
			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregarColunasBaseInterna();
	}

	public void addColuna() {
		colunasNovas.add(new ColunaNovaVO());
	}

	public boolean isChaveEstrangeira(String nomeColuna) {
		return baseRelacionamentoService.buscarRelacionamentoDaColuna(relacionamentos, nomeColuna) != null;
	}

	public Map<String, String> valoresBaseEstrangeira(String nomeColuna) {
		return baseRegistroService.valoresBaseEstrangeira(relacionamentos, nomeColuna);
	}

	private Map<String, String> carregarValoresBusca() {
		String prefix = "coluna_";
		Map<String, String> colunaValor = new LinkedHashMap<>();
		carregarValores(prefix, colunaValor);
		return colunaValor;
	}

	private void carregarValores(String prefix, Map<String, String> colunaValor) {

		HttpServletRequest request = getRequest();
		Enumeration<String> parameterNames = request.getParameterNames();

		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if(parameterName.startsWith(prefix)) {
				String valor = request.getParameter(parameterName);
				String coluna = parameterName.replace(prefix, "");

				colunaValor.put(coluna, valor);
			}
		}
	}

	private void carregarColunasBaseInterna() {

		colunas = baseRegistroValorService.getColunasRegistro(baseInternaId);

		if (CollectionUtils.isEmpty(colunas)) {
			String colunasUnicidade = baseInterna.getColunasUnicidade();
			List<String> colunasUnicidadeList = om.readValue(colunasUnicidade, List.class);
			colunas.addAll(colunasUnicidadeList);
		}

		filtro.setQntColunas(colunas.size());
	}

	private void carregarRegistroValorVOSelecionado() {

		Long registroId = registroValorVO.getRegistroId();
		if (registroId != null) {

			BaseRegistroFiltro baseRegistroFiltro = new BaseRegistroFiltro();
			baseRegistroFiltro.setId(registroId);
			baseRegistroFiltro.setQntColunas(colunas.size());

			List<RegistroValorVO> baseRegistroAtualizado = baseRegistroService.findByFiltro(filtro, null, null);
			registroValorVO = baseRegistroAtualizado.get(0);
		}
	}

	public void setBaseInternaId(Long baseInternaId) {
		this.baseInternaId = baseInternaId;
	}

	public Long getBaseInternaId() {
		return baseInternaId;
	}

	public BaseRegistroDataModel getDataModel() {
		return dataModel;
	}

	public List<String> getColunas() {
		return colunas;
	}

	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public net.wasys.getdoc.domain.vo.RegistroValorVO getRegistroValorVO() {
		return registroValorVO;
	}

	public void setRegistroValorVO(net.wasys.getdoc.domain.vo.RegistroValorVO registroValorVO) {
		if(registroValorVO == null) {
			registroValorVO = new RegistroValorVO();
			registroValorVO.setBaseRegistro(new BaseRegistro());
		}
		this.registroValorVO = registroValorVO;
	}

	public List<ColunaNovaVO> getColunasNovas() {
		return colunasNovas;
	}

	public void setColunasNovas(List<ColunaNovaVO> colunasNovas) {
		this.colunasNovas = colunasNovas;
	}

	public BaseRegistroFiltro getFiltro() {
		return filtro;
	}

	public Boolean isSrincronismo(){

		if(BaseInterna.MANTENEDORA_ID.equals(baseInternaId)
				|| BaseInterna.INSTITUICAO_ID.equals(baseInternaId)
				|| BaseInterna.REGIONAL_ID.equals(baseInternaId)
				|| BaseInterna.MODALIDADE_ENSINO_ID.equals(baseInternaId)
				|| BaseInterna.CURRICULO_CURSO_ID.equals(baseInternaId)
				|| BaseInterna.TURNO_ID.equals(baseInternaId)
				|| BaseInterna.TIPO_CURSO_ID.equals(baseInternaId)
				|| BaseInterna.FORMA_INGRESSO_ID.equals(baseInternaId)
				|| BaseInterna.CURSO_ID.equals(baseInternaId)
				|| BaseInterna.CAMPUS_ID.equals(baseInternaId)
				|| BaseInterna.BANCOS_FINANCIAMENTOS_ID.equals(baseInternaId)
				|| BaseInterna.TIPO_BOLSA_PROUNI_ID.equals(baseInternaId)){
			return true;
		}

		return false;
	}

	public void baixar() {

		Usuario usuario = getUsuarioLogado();
		String login = usuario.getLogin();

		Exception error = exporter.getError();
		if(error != null) {
			addMessageError(error);
		}
		else {
			File file = exporter.getFile();
			try {
				FileInputStream fis = new FileInputStream(file);
				String fileName = "base-interna.xlsx";
				String fileName2 = exporter.getFileName();
				if(StringUtils.isNotBlank(fileName2)) {
					fileName = fileName2;
				}
				Faces.sendFile(fis, fileName, true);
			}
			catch (Exception e1) {
				addMessageError(e1);
			}
		}

		exporter = null;
	}

	public void verificar() {

		if(exporter == null) {
			return;
		}

		if(exporter.isFinalizado()) {
			Ajax.data("terminou", true);
		}
		else {
			Ajax.data("terminou", false);
		}
	}

	public void exportar() {

		if(exporter == null) {
			exporter = applicationContext.getBean(BaseInternaExporter.class);
			exporter.setFiltro(filtro);
			exporter.start();
		}
	}
}
