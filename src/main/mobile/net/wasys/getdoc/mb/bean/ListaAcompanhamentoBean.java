package net.wasys.getdoc.mb.bean;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections.CollectionUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.DeserializationFeature;

import net.wasys.getdoc.domain.entity.DocumentoLog;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.TipoEvidencia;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.service.DocumentoLogService;
import net.wasys.getdoc.domain.service.ProcessoLogService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.service.TipoEvidenciaService;
import net.wasys.getdoc.domain.vo.EvidenciaVO;
import net.wasys.getdoc.domain.vo.ImagemVO;
import net.wasys.getdoc.domain.vo.LogVO;
import net.wasys.getdoc.domain.vo.SolicitacaoVO;
import net.wasys.getdoc.mb.Toast.Type;
import net.wasys.getdoc.mb.enumerator.Tela;
import net.wasys.getdoc.mb.model.ButtonModel;
import net.wasys.getdoc.mb.model.ButtonModel.Icon;
import net.wasys.getdoc.mb.model.ButtonModel.Position;
import net.wasys.getdoc.mb.model.ToolbarModel;
import net.wasys.getdoc.mb.model.UploadResultModel;
import net.wasys.getdoc.mb.model.ViewModel;
import net.wasys.getdoc.mb.regra.ProcessoAutorizacao;
import net.wasys.util.other.ReflectionBeanComparator;
import net.wasys.util.rest.jackson.ObjectMapper;

@ManagedBean
@ViewScoped
public class ListaAcompanhamentoBean extends MobileBean {

	@Autowired private ResourceService resourceService;
	@Autowired private ProcessoService processoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private TipoEvidenciaService tipoEvidenciaService;
	
	// Processo.id
	private Long id;
	private Long tipoEvidenciaId;
	
	private LogVO row;
	private Tela origem;
	private Processo processo;
	private ProcessoAutorizacao regra;
	private EvidenciaVO evidencia;
	
	private List<LogVO> rows;
	private List<ImagemVO> imagens;
	
	private List<TipoEvidencia> options;
	private List<SolicitacaoVO> solicitacoes;
	
	public void init() {
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			processo = processoService.get(id);
			regra = new ProcessoAutorizacao(usuario, processo, processoService);
			options = tipoEvidenciaService.findAtivas();
			carregar();
		}
	}
	
	private void carregar() {
		rows = new ArrayList<>();
		List<ProcessoLog> processoLogs = processoLogService.findByProcesso(id);
		if (CollectionUtils.isNotEmpty(processoLogs)) {
			for (ProcessoLog processoLog : processoLogs) {
				rows.add(new LogVO(processoLog));
			}
		}
		List<DocumentoLog> documentoLogs = documentoLogService.findByProcesso(id);
		if (CollectionUtils.isNotEmpty(documentoLogs)) {
			for (DocumentoLog documentoLog : documentoLogs) {
				rows.add(new LogVO(documentoLog));
			}
		}
		Collections.sort(rows, new ReflectionBeanComparator<>("data, id"));
	}
	
	public void onAdicionarClick() {
		imagens = new ArrayList<>();
		evidencia = new EvidenciaVO();
		evidencia.setAcao(AcaoProcesso.REGISTRO_EVIDENCIA);
	}
	
	public void remover(ImagemVO imagem) {
		imagens.remove(imagem);
	}
	
	public void salvar() {
		try {
			if (CollectionUtils.isNotEmpty(imagens)) {
				String parent = resourceService.getValue(ResourceService.TMP_MOBILE_PATH);
				for (ImagemVO imagemVO : imagens) {
					Long id = imagemVO.getId();
					if (id == null) {
						String nome = imagemVO.getNome();
						File file = new File(parent, nome);
						if (file.exists()) {
							evidencia.addAnexo(nome, file);
						}
					}
				}
			}
			if (tipoEvidenciaId != null) {
				TipoEvidencia tipoEvidencia = new TipoEvidencia();
				tipoEvidencia.setId(tipoEvidenciaId);
				evidencia.setTipoEvidencia(tipoEvidencia);
			}
			processoService.salvarEvidencia(evidencia, processo, usuario);
			showToastByMessageKey(Type.SUCCESS, "alteracaoSalva.sucesso");
		}
		catch (Exception e) {
			showToast(e);
		}
		evidencia = null;
		tipoEvidenciaId = null;
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
	
	public void onObservacaoClick(LogVO row) {
		this.row = row;
	}
	
	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("acompanhamento.label");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		if (regra.podeRegistrarEvidencia()) {
			viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.PLUS, Position.RIGHT));
		}
		viewModel.toolbar.buttons.add(new ButtonModel(2, Icon.UNDO, Position.RIGHT));
		return viewModel.parse();
	}
	
	public Long getId() {
		return id;
	}
	
	public Long getTipoEvidenciaId() {
		return tipoEvidenciaId;
	}
	
	public LogVO getRow() {
		return row;
	}
	
	public Tela getOrigem() {
		return origem;
	}
	
	public ProcessoAutorizacao getRegra() {
		return regra;
	}
	
	public EvidenciaVO getEvidencia() {
		return evidencia;
	}
	
	public List<LogVO> getRows() {
		return rows;
	}
	
	public List<ImagemVO> getImagens() {
		return imagens;
	}
	
	public List<TipoEvidencia> getOptions() {
		return options;
	}
	
	public List<SolicitacaoVO> getSolicitacoes() {
		return solicitacoes;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setTipoEvidenciaId(Long tipoEvidenciaId) {
		this.tipoEvidenciaId = tipoEvidenciaId;
	}
	
	public void setOrigem(Tela origem) {
		this.origem = origem;
	}
}
