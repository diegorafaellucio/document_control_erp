package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.UsuarioDataModel;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.ExportacaoUsuarioService;
import net.wasys.getdoc.domain.service.SubperfilService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class UsuarioListBean extends AbstractBean {

	@Autowired private UsuarioService usuarioService;
	@Autowired private ExportacaoUsuarioService exportacaoUsuarioService;
	@Autowired private SubperfilService subperfilService;
	@Autowired private ResourceService resourceService;

	private Long usuarioId;
	private boolean bloqueadosHide = true;
	private boolean inativosHide = true;
	private UsuarioDataModel ativosDataModel;
	private UsuarioDataModel bloqueadosDataModel;
	private UsuarioDataModel inativosDataModel;
	private int countBloqueados;
	private int countInativos;
	private List<Subperfil> subperfis;
	private boolean disabled;

	private UsuarioFiltro filtro = new UsuarioFiltro();

	protected void initBean() {
		subperfis = subperfilService.findAll();
		buscar();
		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		disabled = StringUtils.isNotBlank(geralEndpoint);
	}

	public void buscar() {

		UsuarioFiltro filtro1 = filtro.clone();
		filtro1.setStatus(StatusUsuario.ATIVO);
		ativosDataModel = new UsuarioDataModel();
		ativosDataModel.setFiltro(filtro1);
		ativosDataModel.setService(usuarioService);

		UsuarioFiltro filtro2 = filtro.clone();
		filtro2.setStatus(StatusUsuario.BLOQUEADO);
		bloqueadosDataModel = new UsuarioDataModel();
		bloqueadosDataModel.setFiltro(filtro2);
		bloqueadosDataModel.setService(usuarioService);
		countBloqueados = usuarioService.countByFiltro(filtro2);

		UsuarioFiltro filtro3 = filtro.clone();
		filtro3.setStatus(StatusUsuario.INATIVO);
		inativosDataModel = new UsuarioDataModel();
		inativosDataModel.setFiltro(filtro3);
		inativosDataModel.setService(usuarioService);
		countInativos = usuarioService.countByFiltro(filtro3);
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		try {
			usuarioService.excluir(usuarioId, usuarioLogado);

			bloqueadosHide = true;
			inativosHide = true;

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void exportar() {

		try {
			File fileTmp;
			fileTmp = File.createTempFile("usuarios_", ".csv");
			DummyUtils.deleteOnExitFile(fileTmp);
			FileOutputStream fos = new FileOutputStream(fileTmp);

			//prefixo do arquivo, necessário para UTF-8
			fos.write(239);
			fos.write(187);
			fos.write(191);

			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			PrintWriter writer = new PrintWriter(osw);

			exportacaoUsuarioService.exportar(writer);

			writer.flush();
			writer.close();

			Faces.sendFile(fileTmp, false);

			DummyUtils.deleteFile(fileTmp);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void carregarBloqueados() {

		if(bloqueadosHide) {
			bloqueadosHide = false;
		} else {
			bloqueadosHide = true;
		}
	}

	public void carregarInativos() {

		if(inativosHide) {
			inativosHide = false;
		} else {
			inativosHide = true;
		}
	}

	public boolean isBloqueadosHide() {
		return bloqueadosHide;
	}

	public boolean isInativosHide() {
		return inativosHide;
	}

	public UsuarioDataModel getAtivosDataModel() {
		return ativosDataModel;
	}

	public UsuarioDataModel getBloqueadosDataModel() {
		return bloqueadosDataModel;
	}

	public UsuarioDataModel getInativosDataModel() {
		return inativosDataModel;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public int getCountBloqueados() {
		return countBloqueados;
	}

	public int getCountInativos() {
		return countInativos;
	}

	public UsuarioFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(UsuarioFiltro filtro) {
		this.filtro = filtro;
	}

	public List<Subperfil> getSubperfis() {
		return subperfis;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}