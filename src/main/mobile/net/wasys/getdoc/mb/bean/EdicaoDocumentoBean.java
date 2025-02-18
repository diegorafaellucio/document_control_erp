package net.wasys.getdoc.mb.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import net.wasys.util.ddd.SpringJob;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.DeserializationFeature;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.service.DocumentoService;
import net.wasys.getdoc.domain.service.ImagemService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.vo.DigitalizacaoVO;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.ImagemVO;
import net.wasys.getdoc.domain.vo.ProcessoVO;
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
import net.wasys.getdoc.mb.utils.TypeUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.rest.jackson.ObjectMapper;

@ManagedBean
@ViewScoped
public class EdicaoDocumentoBean extends MobileBean {

	@Autowired private ImagemService imagemService;
	@Autowired private ProcessoService processoService;
	@Autowired private ResourceService resourceService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ApplicationContext applicationContext;

	private Long id;
	private Long imagemId;

	private Tela origem;
	private Processo processo;
	private Documento documento;

	private ProcessoAutorizacao regra;

	private ProcessoVO processoVO;
	private DocumentoVO documentoVO;

	private List<Integer> versoes;
	private Integer versao;

	private List<ImagemVO> imagens;

	public void init() {
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			iniciar();
		}
	}

	private void iniciar() {
		documento = documentoService.get(id);
		processo = documento.getProcesso();

		regra = new ProcessoAutorizacao(usuario, processo, processoService);
		processoVO = new ProcessoVO(processo);

		carregar(null);

		versoes = new ArrayList<>();
		versao = documentoVO.getVersaoAtual();
		for (int i = 1 ; i <= versao; i ++) {
			versoes.add(i);
		}
	}

	private void carregar(Integer versao) {

		String contextPath = Faces.getRequestContextPath();
		String imagePath = contextPath + ImagemFilter.PATH;

		documentoVO = documentoService.createVOBy(documento, usuario, imagePath);
		if (versao == null) {
			versao = documentoVO.getVersaoAtual();
		}

		if(versao.equals(this.versao)) {
			return;
		}

		this.imagens = null;

		Map<Integer, List<ImagemVO>> hash = documentoVO.getImagens();
		if (MapUtils.isNotEmpty(hash)) {
			this.imagens = hash.get(versao);
		}

		if (this.imagens == null) {
			this.imagens = new ArrayList<>();
		}
	}

	public void onImagensChange(ValueChangeEvent event) {
		Integer versao = (Integer) event.getNewValue();
		carregar(versao);	
	}

	public boolean isSalvarEnable() {
		if (regra.podeDigitalizarDocumento()) {
			if (CollectionUtils.isNotEmpty(imagens)) {
				for (ImagemVO imagemVO : imagens) {
					Long id = imagemVO.getId();
					if (id == null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isExcluirEnable() {
		if (regra.podeDigitalizarDocumento()) {
			return hasAnyImagemSalva();
		}
		return false;
	}

	public boolean isDesfazerEnable() {
		if (CollectionUtils.isNotEmpty(imagens)) {
			return !hasAnyImagemSalva();
		}
		return false;
	}

	public boolean hasAnyImagemSalva() {
		for (ImagemVO imagemVO : imagens) {
			Long id = imagemVO.getId();
			if (id != null) {
				return true;
			}
		}
		return false;
	}

	public void justificar() {
		try {
			String observacao = Faces.getRequestParameter("observacao");
			if (StringUtils.isBlank(observacao)) {
				showToastByMessageKey(Type.DANGER, "validacao-obrigatorio.error", getMessage("observacao.label"));
				return;
			}
			documentoService.justificar(documento.getId(), usuario, observacao);
			showToastByMessageKey(Type.SUCCESS, "alteracaoSalva.sucesso");
			iniciar();
		}
		catch (Exception e) {
			showToast(e);
		}
	}

	public void excluir() {
		if (imagemId != null) {
			Imagem imagem = imagemService.get(imagemId);
			documentoService.excluirImagem(imagem, usuario, true);
		}
		iniciar();
	}

	public void desfazer() {
		carregar(null);
	}

	public void onExcluir() {
		this.imagemId = TypeUtils.parse(Long.class, Faces.getRequestParameter("imagemId"));
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
					if (hasAnyImagemSalva()) {
						imagens.clear();
					}
					imagens.add(imagemVO);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				digitalizacaoVO.setArquivos(arquivos);
				digitalizacaoVO.setProcesso(processo);
				digitalizacaoVO.setDocumento(documento);
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
				} catch (Exception e) {
					showToast(e);
				}
			}
		}
	}

	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = documentoVO.getNome();
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.UNDO, Position.RIGHT));
		return viewModel.parse();
	}

	public Long getId() {
		return id;
	}

	public Long getImagemId() {
		return imagemId;
	}

	public Tela getOrigem() {
		return origem;
	}

	public ProcessoAutorizacao getRegra() {
		return regra;
	}

	public ProcessoVO getProcessoVO() {
		return processoVO;
	}

	public DocumentoVO getDocumentoVO() {
		return documentoVO;
	}

	public List<ImagemVO> getImagens() {
		return imagens;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setImagemId(Long imagemId) {
		this.imagemId = imagemId;
	}

	public void setOrigem(Tela origem) {
		this.origem = origem;
	}

	public Integer getVersao() {
		return versao;
	}

	public void setVersao(Integer versao) {
		this.versao = versao;
	}

	public List<Integer> getVersoes() {
		return versoes;
	}
}