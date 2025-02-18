package net.wasys.getdoc.bean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import net.wasys.getdoc.bean.datamodel.BaseInternaDataModel;
import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRelacionamento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.BaseInternaService;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.BaseRegistroValorService;
import net.wasys.getdoc.domain.service.BaseRelacionamentoService;
import net.wasys.getdoc.domain.service.ColunaValorImporter;
import net.wasys.getdoc.domain.service.ColunaValorImporter.ResultadoProcessamentoVO;
import net.wasys.getdoc.domain.vo.LinhaVO;
import net.wasys.getdoc.domain.vo.ResultVO;
import net.wasys.getdoc.domain.vo.filtro.BaseInternaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.MessageKeyListException;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.rest.jackson.ObjectMapper;

import static net.wasys.util.DummyUtils.systraceThread;

@ManagedBean
@ViewScoped
public class BaseInternaCrudBean extends AbstractBean {

	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private BaseRelacionamentoService baseRelacionamentoService;
	@Autowired private BaseRegistroValorService baseRegistroValorService;

	private BaseInternaFiltro filtro = new BaseInternaFiltro();
	private BaseInternaDataModel dataModel;
	private BaseInterna baseInterna;
	private ColunaValorImporter importer;

	private List<String> colunasUnicidadeList;
	private List<String> todasColunas;
	private String colunaUnicidade;

	private String fileName;
	private File file;
	private List<BaseRelacionamento> relacionamentos;
	private List<BaseInterna> basesInternas;

	public void initBean() {

		dataModel = new BaseInternaDataModel();
		dataModel.setService(baseInternaService);
		dataModel.setFiltro(filtro);

		basesInternas = baseInternaService.findAtivos();
	}

	public void salvar() {
		try {
			setColunasUnicidadeNoBean();
			boolean insert = isInsert(baseInterna);
			Usuario usuario = getUsuarioLogado();
			baseInternaService.saveOrUpdate(baseInterna, relacionamentos, usuario);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void upload(FileUploadEvent event) {

		UploadedFile uploadedFile = event.getFile();
		String fileName = uploadedFile.getFileName();
		String extensao = DummyUtils.getExtensao(fileName);
		try {

			this.fileName = fileName;
			this.file = File.createTempFile("base-", "." + extensao);
			DummyUtils.deleteOnExitFile(this.file);

			InputStream is = uploadedFile.getInputStream();

			FileUtils.copyInputStreamToFile(is, file);

			addMessage("arquivoCarregado.sucesso");
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			addMessageError("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public void importar() {

		if(importer == null) {
			importer = applicationContext.getBean(ColunaValorImporter.class);
			importer.setFile(file);
			importer.setColunasUnicidade(baseInterna.getColunasUnicidadeList());
			importer.setUsuario(getUsuarioLogado());
			importer.start();
		}
	}

	public void addRelacionamento() {
		BaseRelacionamento relacionamento = new BaseRelacionamento();
		relacionamento.setBaseInterna(baseInterna);
		relacionamentos.add(relacionamento);
	}

	public void removeRelacionamento(BaseRelacionamento relacionamento) {
		relacionamentos.remove(relacionamento);
	}

	public void verificar() {

		Usuario usuario = getUsuarioLogado();
		String login = usuario.getLogin();

		if(importer == null) {
			systraceThread(login + ". não iniciado");
			return;
		}
		if(importer.isFinalizado()) {

			Exception error = importer.getError();
			if(error != null) {
				addMessageError("erroInesperado.error", error.toString());
			} else {
				finalizarImportacao(importer.getResultadoProcessamento(), usuario);
			}

			systraceThread("finalizado");
			redirect("/cadastros/bases-internas/");
		}
		else {
			systraceThread(login + ". não finalizado");
		}
	}

	private void finalizarImportacao(ResultadoProcessamentoVO resultado, Usuario usuario) {

		if(resultado == null) {
			addMessageError("erroInesperado.error", "Importação de base não retornou nenhum resultado.");
		}
		else if (resultado.getException() == null) {

			try {
				List<LinhaVO> linhas = resultado.getLinhas();

				ResultVO result = baseRegistroService.atualizarRegistros(baseInterna, linhas, usuario, file, fileName);
				mostrarMensagemSucessoImportacao(result);
			} catch (Exception e) {
				addMessageError(e);
				redirect("/cadastros/bases-internas/");
			}
		}
		else {
			mostrarMensagemErroImportacao(resultado.getException());
		}
	}

	private void mostrarMensagemSucessoImportacao(ResultVO resultVO) {

		addMessage("baseInterna-importada.sucesso");
		addMessage("baseInterna.inserts", resultVO.getInserts());
		addMessage("baseInterna.updates", resultVO.getUpdates());
		addMessage("baseInterna.deletes", resultVO.getDeletes());
	}

	private void mostrarMensagemErroImportacao(Exception ex) {

		if(ex instanceof MessageKeyException) {
			addMessageError(ex);
		}
		else if(ex instanceof MessageKeyListException) {
			for (MessageKeyException mke : ((MessageKeyListException) ex).getMessageKeyExceptions()) {
				addMessageError(mke);
			}
		}
	}

	public List<String> colunasUnicidadeAutoComplete(String query) {
		return colunasUnicidadeAutoComplete(baseInterna, query);
	}

	public List<String> colunasRelacionamentoAutoComplete(String query) {
		FacesContext context = FacesContext.getCurrentInstance();
		return colunasUnicidadeAutoComplete(baseInterna, query);
	}

	public List<String> colunasUnicidadeAutoComplete(BaseInterna baseInterna, String query) {
		Set<String> colunas = new LinkedHashSet<>();
		colunas.add(query);
		Long baseInternaId = baseInterna.getId();
		if(baseInternaId != null) {
			colunas.addAll(baseRegistroValorService.getColunasRegistro(baseInternaId));
		}
		return new ArrayList<>(colunas);
	}

	public void setBaseInterna(BaseInterna baseInterna) {

		if(baseInterna == null) {
			baseInterna = new BaseInterna();
			colunasUnicidadeList = new ArrayList<>(5);
			colunaUnicidade = null;
			relacionamentos = new ArrayList<>();
		}
		else {
			carregarColunasUnicidadeList(baseInterna.getColunasUnicidade());
			Long baseInternaId = baseInterna.getId();
			relacionamentos = baseRelacionamentoService.findByBaseInterna(baseInternaId);
			todasColunas = baseRegistroValorService.getColunasRegistro(baseInternaId);
		}

		this.baseInterna = baseInterna;
	}

	@SuppressWarnings("unchecked")
	private void carregarColunasUnicidadeList(String colunasUnicidade) {
		if(colunasUnicidade != null && !colunasUnicidade.isEmpty()) {
			ObjectMapper om = new ObjectMapper();
			colunasUnicidadeList = om.readValue(colunasUnicidade, List.class);
		}
		else {
			colunasUnicidadeList = new ArrayList<>(5);
		}
	}

	private void setColunasUnicidadeNoBean() {
		ObjectMapper om = new ObjectMapper();
		String colunasUnicidadeJson = om.writeValueAsString(colunasUnicidadeList);
		baseInterna.setColunasUnicidade(colunasUnicidadeJson);
	}

	public void excluir() {
		Usuario usuarioLogado = getUsuarioLogado();
		Long baseInternaId = baseInterna.getId();
		try {
			baseInternaService.excluir(baseInternaId, usuarioLogado);
			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public BaseInternaDataModel getDataModel() {
		return dataModel;
	}

	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public String getFileName() {
		return fileName;
	}

	public ColunaValorImporter getImporter() {
		return importer;
	}

	public String getColunaUnicidade() {
		return colunaUnicidade;
	}

	public void setColunaUnicidade(String colunaUnicidade) {
		this.colunaUnicidade = colunaUnicidade;
	}

	public List<String> getColunasUnicidadeList() {
		return colunasUnicidadeList;
	}

	public void setColunasUnicidadeList(List<String> colunasUnicidadeList) {
		this.colunasUnicidadeList = colunasUnicidadeList;
	}

	public BaseInternaFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(BaseInternaFiltro filtro) {
		this.filtro = filtro;
	}

	public List<BaseRelacionamento> getRelacionamentos() {
		return relacionamentos;
	}

	public List<BaseInterna> getBasesInternas() {
		return basesInternas;
	}

	public List<String> getTodasColunas() {
		return todasColunas;
	}
}
