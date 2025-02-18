package net.wasys.getdoc.mb.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.vo.EditarProcessoVO;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import org.apache.commons.collections.CollectionUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.DeserializationFeature;

import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.CampoGrupo;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.DocumentoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.vo.EvidenciaVO;
import net.wasys.getdoc.domain.vo.ImagemVO;
import net.wasys.getdoc.mb.Toast;
import net.wasys.getdoc.mb.Toast.Type;
import net.wasys.getdoc.mb.enumerator.Tela;
import net.wasys.getdoc.mb.model.ButtonModel;
import net.wasys.getdoc.mb.model.ButtonModel.Icon;
import net.wasys.getdoc.mb.model.ButtonModel.Position;
import net.wasys.getdoc.mb.model.ToolbarModel;
import net.wasys.getdoc.mb.model.UploadResultModel;
import net.wasys.getdoc.mb.model.ViewModel;
import net.wasys.getdoc.mb.regra.ProcessoAutorizacao;
import net.wasys.util.rest.jackson.ObjectMapper;

@ManagedBean
@ViewScoped
public class EdicaoRequisicaoBean extends MobileBean {

	@Autowired private ResourceService resourceService;
	@Autowired private ProcessoService processoService;
	@Autowired private DocumentoService documentoService;

	private Long id;
	private Tela origem;
	private boolean edicao;
	private String horasRestantes;

	private Processo processo;
	private ProcessoAutorizacao regra;
	private EvidenciaVO evidencia;

	private List<ImagemVO> imagens;
	private List<CampoGrupo> grupos;
	private Map<CampoGrupo, List<Campo>> secoes;

	public void init() {

		if (!Faces.isPostback() && !Faces.isValidationFailed()) {

			Toast toast = Faces.getFlashAttribute(Toast.class.getName());
			if (toast != null) {
				showToast(toast);
			}

			processo = processoService.get(id);
			regra = new ProcessoAutorizacao(usuario, processo, processoService);
			horasRestantes = processoService.getHorasRestantes(processo);

			secoes = new HashMap<>();
			grupos = new ArrayList<CampoGrupo>();

			Set<CampoGrupo> set = processo.getGruposCampos();
			for (CampoGrupo key : set) {
				List<Campo> value = new ArrayList<>();
				Set<Campo> campos = key.getCampos();
				for (Campo campo : campos) {
					value.add(campo);
				}
				grupos.add(key);
				secoes.put(key, value);
			}
		}
	}

	public void editar() {
		edicao = true;
	}

	public void onAddEvidenciaClick() {
		imagens = new ArrayList<>();
		this.evidencia = new EvidenciaVO();
		this.evidencia.setAcao(AcaoProcesso.RESPOSTA_PENDENCIA);
		this.evidencia.setShowTipoEvidencia(false);
		this.evidencia.setShowPrazoDias(false);
		this.evidencia.setShowSituacao(false);
	}

	public void removerImagem(ImagemVO imagem) {
		imagens.remove(imagem);
	}

	public void salvarCampos() {

		try {
			Collection<List<Campo>> values = secoes.values();
			Map<Long, String> camposMap = new HashMap<>();
			for (List<Campo> list : values) {
				for (Campo campo : list) {
					Long campoId = campo.getId();
					String campoValor = campo.getValor();
					camposMap.put(campoId, campoValor);
				}
			}

			Long processoId = processo.getId();
			EditarProcessoVO editarProcessoVO = new EditarProcessoVO();
			editarProcessoVO.setProcessoId(processoId);
			editarProcessoVO.setUsuario(usuario);
			editarProcessoVO.setValores(camposMap);
			processoService.atualizarProcesso(editarProcessoVO);
			setEdicao(false);

			Toast toast = new Toast(Type.SUCCESS, getMessage("rascunhoSalvo.sucesso"));
			Faces.setFlashAttribute(Toast.class.getName(), toast);

			String redirec = String.format("mobile/requisicao/edicao.xhtml?id=%1$s&origem=%2$s", String.valueOf(processo.getId()), origem.name());
			Faces.redirect(redirec);
		}
		catch (Exception e) {
			showToast(e);
		}
	}

	public void reenviar() {
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
			processoService.salvarEvidencia(evidencia, processo, usuario);
			Toast toast = new Toast(Type.SUCCESS, getMessage("alteracaoSalva.sucesso"));
			Faces.setFlashAttribute(Toast.class.getName(), toast);
			if (Tela.PESQUISA.equals(origem)) {
				Faces.redirect("mobile/pesquisa/lista.xhtml");
			} else {
				Faces.redirect("mobile/requisicao/fila.xhtml");
			}
		}
		catch (Exception exception) {
			showToast(exception);
		}
	}

	public void enviar() {
		try {
			processoService.enviarParaAnalise(processo, usuario);
			Toast toast = new Toast(Type.SUCCESS, getMessage("registroAlterado.sucesso"));
			Faces.setFlashAttribute(Toast.class.getName(), toast);
			if (Tela.PESQUISA.equals(origem)) {
				Faces.redirect("mobile/pesquisa/lista.xhtml");
			} else {
				Faces.redirect("mobile/requisicao/fila.xhtml");
			}
		}
		catch (Exception exception) {
			showToast(exception);
		}
	}

	public void excluir() {
		try {
			processoService.excluir(id, usuario);
			Toast toast = new Toast(Type.SUCCESS, getMessage("registroExcluido.sucesso"));
			Faces.setFlashAttribute(Toast.class.getName(), toast);
			if (Tela.PESQUISA.equals(origem)) {
				Faces.redirect("mobile/pesquisa/lista.xhtml");
			} else {
				Faces.redirect("mobile/requisicao/fila.xhtml");
			}
		}
		catch (Exception exception) {
			showToast(exception);
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

	public List<Campo> getCamposBy(CampoGrupo grupo) {
		List<Campo> campos = secoes.get(grupo);
		return campos;
	}

	public boolean mostrarAcoes() {
		return regra.podeEnviar()
				|| regra.podeExcluir()
				|| regra.podeReenviar();
	}

	public boolean mostrarLinkEmails() {
		StatusProcesso status = processo.getStatus();
		if (StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}
		return true;
	}

	public boolean mostrarLinkDocumentos() {
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		return documentoService.countByFiltro(filtro) != 0;
	}

	public boolean mostrarLinkSolicitacoes() {
		StatusProcesso status = processo.getStatus();
		if (StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}
		return true;
	}

	public boolean mostrarLinkAcompanhamento() {
		StatusProcesso status = processo.getStatus();
		if (StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}
		return true;
	}

	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("detalhesProcesso.titulo");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		if (regra.podeEditar()) {
			viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.PENCIL, Position.RIGHT));
		}
		int id = 0;
		if (Tela.FILA_TRABALHO.equals(origem)) {
			id = 2;
		} else if (Tela.PESQUISA.equals(origem)) {
			id = 3;
		}
		if (id > 0) {
			viewModel.toolbar.buttons.add(new ButtonModel(id, Icon.UNDO, Position.RIGHT));
		}
		return viewModel.parse();
	}

	public Long getId() {
		return id;
	}

	public Tela getOrigem() {
		return origem;
	}

	public boolean isEdicao() {
		return edicao;
	}

	public String getHorasRestantes() {
		return horasRestantes;
	}

	public List<ImagemVO> getImagens() {
		return imagens;
	}

	public List<CampoGrupo> getGrupos() {
		return grupos;
	}

	public Processo getProcesso() {
		return processo;
	}

	public ProcessoAutorizacao getRegra() {
		return regra;
	}

	public EvidenciaVO getEvidencia() {
		return evidencia;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOrigem(Tela origem) {
		this.origem = origem;
	}

	public void setEdicao(boolean edicao) {
		this.edicao = edicao;
	}
}