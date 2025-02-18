package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import net.wasys.getdoc.bean.datamodel.ModeloDocumentoDataModel;
import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.ExportacaoPalavrasEEService;
import net.wasys.getdoc.domain.service.ModeloDocumentoService;
import net.wasys.getdoc.domain.service.ModeloOcrService;
import net.wasys.getdoc.domain.service.TipificacaoVisionApiService;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ManagedBean
@ViewScoped
public class ModeloDocumentoCrudBean extends AbstractBean {

	@Autowired private ModeloDocumentoService modeloDocumentoService;
	@Autowired private ExportacaoPalavrasEEService exportacaoPalavrasEEService;
	@Autowired private ModeloOcrService modeloOcrService;

	private ModeloDocumentoDataModel dataModel;
	private ModeloDocumento modeloDocumento;
	private List<String> palavrasEsperadasList;
	private List<String> palavrasExcludentesList;
	private List<ModeloOcr> modelosOcr;

	public void initBean() {

		if (modeloDocumento == null){
			modeloDocumento = new ModeloDocumento();
		}

		modelosOcr = modeloOcrService.findAtivos();

		dataModel = new ModeloDocumentoDataModel();
		dataModel.setService(modeloDocumentoService);

	}

	public void salvar() {

		try {
			Integer percentualMininoTipificacao = modeloDocumento.getPercentualMininoTipificacao();
			boolean visionHabilitada = modeloDocumento.getVisionApiHabilitada();
			if(visionHabilitada && (percentualMininoTipificacao == null || percentualMininoTipificacao <= 0)){
				addMessageError("erroPercentualMinimoTipificacao.error");
				return;
			}

			boolean insert = isInsert(modeloDocumento);
			Usuario usuario = getUsuarioLogado();

			StringBuilder palavrasEsperadas = new StringBuilder();
			if(palavrasEsperadasList != null) {
				Set<String> palavrasEsperadasSet = new LinkedHashSet<>(palavrasEsperadasList);
				for (String palavraEsperada : palavrasEsperadasSet) {
					if(StringUtils.isNotBlank(palavraEsperada)) {
						palavrasEsperadas.append(palavraEsperada).append(";");
					}
				}
			}
			modeloDocumento.setPalavrasEsperadas(palavrasEsperadas.toString());

			if(palavrasExcludentesList != null) {
				StringBuilder palavrasExcludentes = new StringBuilder();
				Set<String> palavrasExcludentesSet = new LinkedHashSet<>(palavrasExcludentesList);
				for (String palavraExcludente : palavrasExcludentesSet) {
					if(StringUtils.isNotBlank(palavraExcludente)) {
						palavrasExcludentes.append(palavraExcludente).append(";");
					}
				}

				modeloDocumento.setPalavrasExcludentes(palavrasExcludentes.toString());
			}

			modeloDocumentoService.saveOrUpdate(modeloDocumento, usuario);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void exportarPalavrasEE() {

		try {
			File fileTmp;
			fileTmp = File.createTempFile("ModeloDocumento-Palavras-EE_", ".csv");
			DummyUtils.deleteOnExitFile(fileTmp);
			FileWriter fw = new FileWriter(fileTmp);
			PrintWriter writer = new PrintWriter(fw);

			exportacaoPalavrasEEService.exportar(writer);

			writer.flush();
			writer.close();

			Faces.sendFile(fileTmp, false);
			// exportarArquivo("usuarios.csv", fileTmp);

			DummyUtils.deleteFile(fileTmp);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long modeloDocumentoId = modeloDocumento.getId();

		try {
			modeloDocumentoService.excluir(modeloDocumentoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public ModeloDocumentoDataModel getDataModel() {
		return dataModel;
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {

		if(modeloDocumento == null) {
			modeloDocumento = new ModeloDocumento();
			modeloDocumento.setPercentualMininoTipificacao(TipificacaoVisionApiService.PERCENTUAL_MINIMO_TIPIFICACAO_DEFAULT);
		}

		String palavrasEsperadas = modeloDocumento.getPalavrasEsperadas();
		palavrasEsperadas = palavrasEsperadas != null ? palavrasEsperadas : "";
		String[] palavrasEsperadasSplit = palavrasEsperadas.split(";");
		palavrasEsperadasList = new ArrayList<>();
		for (String palavraEsperada : palavrasEsperadasSplit) {
			if(StringUtils.isNotBlank(palavraEsperada)) {
				palavrasEsperadasList.add(palavraEsperada);
			}
		}

		String palavrasExcludentes = modeloDocumento.getPalavrasExcludentes();
		palavrasExcludentes = palavrasExcludentes != null ? palavrasExcludentes : "";
		String[] palavrasExcludentesSplit = palavrasExcludentes.split(";");
		palavrasExcludentesList = new ArrayList<>();
		for (String palavraExcludente : palavrasExcludentesSplit) {
			if(StringUtils.isNotBlank(palavraExcludente)) {
				palavrasExcludentesList.add(palavraExcludente);
			}
		}

		this.modeloDocumento = modeloDocumento;
	}

	public List<String> getPalavrasEsperadasList() {
		return palavrasEsperadasList;
	}

	public void setPalavrasEsperadasList(List<String> palavrasEsperadasList) {
		this.palavrasEsperadasList = palavrasEsperadasList;
	}

	public List<String> getPalavrasEsperadas(String query) {
		List<String> list = new ArrayList<>();
		list.add(query);
		return list;
	}

	public List<String> getPalavrasExcludentesList() {
		return palavrasExcludentesList;
	}

	public void setPalavrasExcludentesList(List<String> palavrasExcludentesList) {
		this.palavrasExcludentesList = palavrasExcludentesList;
	}

	public List<String> getPalavrasExcludentes(String query) {
		List<String> list = new ArrayList<>();
		list.add(query);
		return list;
	}

	public List<ModeloOcr> getModelosOcr() {
		return modelosOcr;
	}

	public void setModelosOcr(List<ModeloOcr> modelosOcr) {
		this.modelosOcr = modelosOcr;
	}
}
