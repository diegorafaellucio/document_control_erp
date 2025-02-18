package net.wasys.getdoc.mb.bean;

import java.io.File;
import java.util.ArrayList;
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
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.vo.DigitalizacaoVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.ImagemVO;
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
public class CapturaDocumentoBean extends MobileBean {

	@Autowired private ProcessoService processoService;
	@Autowired private ResourceService resourceService;
	@Autowired private ApplicationContext applicationContext;

	private Long id;
	private Tela origem;

	private Processo processo;
	private ProcessoAutorizacao regra;

	private List<ImagemVO> imagens;

	public void init() {
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			processo = processoService.get(id);
			regra = new ProcessoAutorizacao(usuario, processo, processoService);
			imagens = new ArrayList<>();
		}
	}

	public void onUpload() {
		String upload = Faces.getRequestParameter("upload");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			UploadResultModel resultModel = mapper.readValue(upload, UploadResultModel.class);
			if (CollectionUtils.isNotEmpty(resultModel.names)) {
				for (String name : resultModel.names) {
					ImagemVO imagemVO = new ImagemVO();
					imagemVO.setNome(name);
					String caminho = String.format("%1$s/rest/file/load/%2$s", Faces.getRequestContextPath(), name);
					imagemVO.setCaminho(caminho);
					imagens.add(imagemVO);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void excluir(ImagemVO imagem) {
		if (CollectionUtils.isNotEmpty(imagens)) {
			imagens.remove(imagem);
		}
	}

	public void salvar() {
		if (CollectionUtils.isNotEmpty(imagens)) {
			String parent = resourceService.getValue(ResourceService.TMP_MOBILE_PATH);
			List<FileVO> arquivos = new ArrayList<>();
			for (ImagemVO imagemVO : imagens) {
				Long id = imagemVO.getId();
				if (id == null) {
					String nome = imagemVO.getNome();
					File file = new File(parent, nome);
					if (file.exists()) {
						FileVO fileVO = new FileVO();
						fileVO.setFile(file);
						String hashChecksum = DummyUtils.getHashChecksum(file);
						fileVO.setHash(hashChecksum);
						fileVO.setName(nome);
						arquivos.add(fileVO);
					}
				}
			}
			if (CollectionUtils.isNotEmpty(arquivos)) {
				final DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
				digitalizacaoVO.setProcesso(processo);
				digitalizacaoVO.setArquivos(arquivos);
				digitalizacaoVO.setOrigem(getDevice());
				try {
					SpringJob job = new SpringJob() {
						@Override
						public void execute() throws Exception {
							processoService.digitalizarImagens(usuario, digitalizacaoVO);
						}
					};
					job.setApplicationContext(applicationContext);
					DummyUtils.executarThread(job, false);

					showToastByMessageKey(Type.SUCCESS, "alteracaoSalva.sucesso");
					Long id = processo.getId();
					String name = origem.name();
					String redirec = String.format("mobile/requisicao/documento/lista.xhtml?id=%1$s&origem=%2$s", String.valueOf(id), name);
					Faces.redirect(redirec);
				} catch (Exception e) {
					showToast(e);
				}
			}
		}
	}

	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("captura.label");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.CAMERA, Position.RIGHT));
		viewModel.toolbar.buttons.add(new ButtonModel(2, Icon.UNDO, Position.RIGHT));
		return viewModel.parse();
	}

	public Long getId() {
		return id;
	}

	public Tela getOrigem() {
		return origem;
	}

	public Processo getProcesso() {
		return processo;
	}

	public ProcessoAutorizacao getRegra() {
		return regra;
	}

	public List<ImagemVO> getImagens() {
		return imagens;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOrigem(Tela origem) {
		this.origem = origem;
	}
}