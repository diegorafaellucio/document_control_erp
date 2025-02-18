package net.wasys.getdoc.mb.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.util.ddd.SpringJob;
import org.apache.commons.collections.CollectionUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.DeserializationFeature;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.service.DocumentoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.vo.DigitalizacaoVO;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.http.ImagemFilter;
import net.wasys.getdoc.mb.Toast.Type;
import net.wasys.getdoc.mb.enumerator.Tela;
import net.wasys.getdoc.mb.model.ButtonModel;
import net.wasys.getdoc.mb.model.ButtonModel.Icon;
import net.wasys.getdoc.mb.model.ButtonModel.Position;
import net.wasys.getdoc.mb.model.ToolbarModel;
import net.wasys.getdoc.mb.model.UploadResultModel;
import net.wasys.getdoc.mb.model.ViewModel;
import net.wasys.getdoc.mb.regra.ProcessoAutorizacao;
import net.wasys.util.DummyUtils;
import net.wasys.util.rest.jackson.ObjectMapper;

@ManagedBean
@ViewScoped
public class ListaDocumentoBean extends MobileBean {

	@Autowired private ProcessoService processoService;
	@Autowired private ResourceService resourceService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ApplicationContext applicationContext;
	
	// Processo.id
	private Long id;
	private Long documentoId;

	private Tela origem;
	private Processo processo;
	private ProcessoAutorizacao regra;
	
	private List<DocumentoVO> rows;
	private List<DocumentoVO> options;
	
	public void init() {
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			processo = processoService.get(id);
			regra = new ProcessoAutorizacao(usuario, processo, processoService);
			carregar();
		}
	}
	
	private void carregar() {
		String contextPath = Faces.getRequestContextPath();
		String imagePath = contextPath + ImagemFilter.PATH;
		rows = new ArrayList<>();
		options = new ArrayList<>();
		List<DocumentoVO> documentos = documentoService.findVOsByProcesso(id, usuario, imagePath);
		for (DocumentoVO documento : documentos) {
			StatusDocumento status = documento.getStatus();
			if (!StatusDocumento.EXCLUIDO.equals(status)) {
				rows.add(documento);
			} else {
				options.add(documento);
			}
		}
	}
	
	public void adicionar() {
		if (documentoId != null) {
			try {
				processoService.adicionarDocumento(processo, Arrays.asList(documentoId), usuario);
				documentoId = null;
				showToastByMessageKey(Type.SUCCESS, "alteracaoSalva.sucesso");
				carregar();
			} catch (Exception e) {
				showToast(e);
			}
		}
	}
	
	public void salvar() {
		String upload = Faces.getRequestParameter("upload");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			UploadResultModel resultModel = mapper.readValue(upload, UploadResultModel.class);
			if (CollectionUtils.isNotEmpty(resultModel.names)) {
				String parent = resourceService.getValue(ResourceService.TMP_MOBILE_PATH);
				List<FileVO> arquivos = new ArrayList<>();
				for (String name : resultModel.names) {
					File file = new File(parent, name);
					if (file.exists()) {
						FileVO vo = new FileVO();
						vo.setFile(file);
						String hashChecksum = DummyUtils.getHashChecksum(file);
						vo.setHash(hashChecksum);
						vo.setName(name);
						arquivos.add(vo);
					}
				}
				if (CollectionUtils.isNotEmpty(arquivos)) {
					final DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
					digitalizacaoVO.setProcesso(processo);
					digitalizacaoVO.setArquivos(arquivos);
					digitalizacaoVO.setOrigem(getDevice());
					SpringJob job = new SpringJob() {
						@Override
						public void execute() throws Exception {
							processoService.digitalizarImagens(usuario, digitalizacaoVO);
						}
					};
					job.setApplicationContext(applicationContext);
					DummyUtils.executarThread(job);
					showToastByMessageKey(Type.SUCCESS, "alteracaoSalva.sucesso");
					carregar();
				}
			}
		} catch (Exception e) {
			showToast(e);
		}
	}
	
	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("documentos.label");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		if (regra.podeAdicionarDocumento()) {
			viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.PLUS, Position.RIGHT));
		}
		viewModel.toolbar.buttons.add(new ButtonModel(2, Icon.UNDO, Position.RIGHT));
		return viewModel.parse();
	}
	
	public Long getId() {
		return id;
	}
	
	public Tela getOrigem() {
		return origem;
	}
	
	public ProcessoAutorizacao getRegra() {
		return regra;
	}
	
	public Long getDocumentoId() {
		return documentoId;
	}
	
	public List<DocumentoVO> getRows() {
		return rows;
	}
	
	public List<DocumentoVO> getOptions() {
		return options;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setOrigem(Tela origem) {
		this.origem = origem;
	}
	
	public void setDocumentoId(Long documentoId) {
		if (documentoId != null && documentoId == 0) {
			documentoId = null;
		}
		this.documentoId = documentoId;
	}
}
