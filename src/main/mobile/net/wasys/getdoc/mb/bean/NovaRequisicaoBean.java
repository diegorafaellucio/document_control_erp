package net.wasys.getdoc.mb.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.util.ddd.SpringJob;
import org.apache.commons.collections.CollectionUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.wasys.getdoc.domain.entity.CampoAbstract;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.TipoCampo;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.service.TipoCampoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.CriaProcessoResultVO;
import net.wasys.getdoc.domain.vo.CriacaoProcessoVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.mb.Toast;
import net.wasys.getdoc.mb.Toast.Type;
import net.wasys.getdoc.mb.enumerator.Tela;
import net.wasys.getdoc.mb.model.ButtonModel;
import net.wasys.getdoc.mb.model.ButtonModel.Icon;
import net.wasys.getdoc.mb.model.ButtonModel.Position;
import net.wasys.getdoc.mb.model.ToolbarModel;
import net.wasys.getdoc.mb.model.UploadResultModel;
import net.wasys.getdoc.mb.model.ViewModel;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.Bolso;

@ManagedBean
@ViewScoped
public class NovaRequisicaoBean extends MobileBean {

	@Autowired private ResourceService resourceService;
	@Autowired private ProcessoService processoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private TipoProcessoService tipoProcessoService;

	private Tela origem;
	private TipoProcesso tipoProcesso;
	private List<TipoProcesso> tiposProcessos;

	private List<TipoCampoGrupo> grupos;
	private Map<TipoCampoGrupo, List<CampoAbstract>> secoes;

	public void init() {
		if (!Faces.isPostback() && !Faces.isValidationFailed()) {
			List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(RoleGD.GD_COMERCIAL);
			tiposProcessos = tipoProcessoService.findAtivos(permissoes);
		}
	}

	public void onTipoProcessoChange(ValueChangeEvent event) {
		secoes = new HashMap<>();
		grupos = new ArrayList<>();
		tipoProcesso = (TipoProcesso) event.getNewValue();
		if (tipoProcesso != null) {
			//tipoProcesso.setPreencherViaOcr(true);
			Long tipoProcessoId = tipoProcesso.getId();
			Map<TipoCampoGrupo, List<TipoCampo>> map = tipoCampoService.findMapByTipoProcesso(tipoProcessoId);
			Set<Entry<TipoCampoGrupo,List<TipoCampo>>> entrySet = map.entrySet();
			for (Entry<TipoCampoGrupo, List<TipoCampo>> entry : entrySet) {
				List<CampoAbstract> campos = new ArrayList<>();
				for (TipoCampo campo : entry.getValue()) {
					campos.add(campo);
				}
				TipoCampoGrupo grupo = entry.getKey();
				grupos.add(grupo);
				secoes.put(grupo, campos);
			}
		}
	}

	public void salvar() {

		try {

			Set<CampoAbstract> campos = new HashSet<>();
			Collection<List<CampoAbstract>> values = secoes.values();

			for (List<CampoAbstract> list : values) {
				campos.addAll(list);
			}

			CriacaoProcessoVO vo = new CriacaoProcessoVO();
			vo.setTipoProcesso(tipoProcesso);
			vo.setUsuario(usuario);
			vo.setValoresCampos(campos);

			Processo processo = processoService.criaProcesso(vo);

			Toast toast = new Toast(Type.SUCCESS, getMessage("rascunhoSalvo.sucesso"));
			Faces.setFlashAttribute(Toast.class.getName(), toast);

			Long processoId = processo.getId();
			String origemName = origem.name();
			String redirec = String.format("mobile/requisicao/edicao.xhtml?id=%1$s&origem=%2$s", String.valueOf(processoId), origemName);
			Faces.redirect(redirec);

		} catch (Exception e) {
			showToast(e);
		}
	}

	public void onScanReady() {
		String upload = Faces.getRequestParameter("upload");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			UploadResultModel resultModel = mapper.readValue(upload, UploadResultModel.class);
			if (CollectionUtils.isNotEmpty(resultModel.names)) {
				String parent = resourceService.getValue(ResourceService.TMP_MOBILE_PATH);
				final List<FileVO> arquivos = new ArrayList<>();
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
					final Bolso<CriaProcessoResultVO> bolso = new Bolso<>();
					SpringJob job = new SpringJob() {
						@Override
						public void execute() throws Exception {
							Origem origem = getDevice();
							CriaProcessoResultVO vo = processoService.criaProcesso(usuario, tipoProcesso, arquivos, origem);
							bolso.setObjeto(vo);
						}
					};
					job.setApplicationContext(applicationContext);
					DummyUtils.executarThread(job);
					CriaProcessoResultVO vo = bolso.getObjeto();
					Exception e = vo.getException();
					Processo processo = vo.getProcesso();
					if(e != null && processo != null) {
						String message = getErrorMessage(e);
						Toast toast = new Toast(Type.SUCCESS, message);
						Faces.setFlashAttribute(Toast.class.getName(), toast);
						String redirec = String.format("mobile/requisicao/edicao.xhtml?id=%1$s&origem=%2$s", String.valueOf(processo.getId()), Tela.FILA_TRABALHO.name());
						Faces.redirect(redirec);
					}
					else if(e != null) {
						throw e;
					}
					else {
						String message = getMessage("documentosEnviados.sucesso");
						Toast toast = new Toast(Type.SUCCESS, message);
						Faces.setFlashAttribute(Toast.class.getName(), toast);
						String redirec = "mobile/requisicao/fila.xhtml";
						Faces.redirect(redirec);
					}
				}
			}
		} catch (Exception e) {
			showToast(e);
		}
	}

	public String getDeviceInitializeScript() {
		ViewModel viewModel = new ViewModel();
		viewModel.title = messageService.getValue("processoNovo.titulo");
		viewModel.toolbar = new ToolbarModel();
		viewModel.toolbar.buttons = new ArrayList<>();
		if (tipoProcesso != null && tipoProcesso.isPreencherViaOcr()) {
			viewModel.toolbar.buttons.add(new ButtonModel(2, Icon.CAMERA, Position.RIGHT));
		}
		viewModel.toolbar.buttons.add(new ButtonModel(1, Icon.UNDO, Position.RIGHT));
		return viewModel.parse();
	}

	public List<CampoAbstract> getCamposBy(TipoCampoGrupo grupo) {
		List<CampoAbstract> campos = secoes.get(grupo);
		return campos;
	}

	public Tela getOrigem() {
		return origem;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public List<TipoCampoGrupo> getGrupos() {
		return grupos;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setOrigem(Tela origem) {
		this.origem = origem;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}
}