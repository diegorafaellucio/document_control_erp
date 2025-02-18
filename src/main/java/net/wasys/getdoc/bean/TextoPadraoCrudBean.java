package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.TextoPadraoDataModel;
import net.wasys.getdoc.domain.entity.TextoPadrao;
import net.wasys.getdoc.domain.entity.TextoPadraoTipoProcesso;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.TextoPadraoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.filtro.TextoPadraoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ManagedBean
@ViewScoped
public class TextoPadraoCrudBean extends AbstractBean {

	@Autowired private TextoPadraoService textoPadraoService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private TextoPadraoDataModel dataModel;
	private TextoPadrao textoPadrao;
	private List<TipoProcesso> tiposProcessos;

	private TextoPadraoFiltro filtro = new TextoPadraoFiltro();
	private List<String> allPermissoesDeUso = new ArrayList<>();
	private List<String> currentPermissoesDeUso = new ArrayList<>();
	private List<TipoProcesso> tiposProcessosSelecionados;

	protected void initBean() {

		dataModel = new TextoPadraoDataModel();
		dataModel.setService(textoPadraoService);
		dataModel.setFiltro(filtro);

		tiposProcessos = tipoProcessoService.findAtivos(null);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(textoPadrao);
			Usuario usuario = getUsuarioLogado();

			arrumarPermissoesDeUso();

			textoPadraoService.saveOrUpdate(textoPadrao, usuario, tiposProcessosSelecionados);

			setRequestAttribute("fecharModal", true);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();
		Long textoPadraoId = textoPadrao.getId();

		try {
			textoPadraoService.excluir(textoPadraoId, usuarioLogado);

			textoPadrao = null;

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public TextoPadraoDataModel getDataModel() {
		return dataModel;
	}

	public TextoPadrao getTextoPadrao() {
		if (textoPadrao != null) {
			String texto = textoPadrao.getTexto();
			String toHTML = DummyUtils.stringToHTML(texto);
			textoPadrao.setTexto(toHTML);
		}
		return textoPadrao;
	}

	public void setTextoPadrao(TextoPadrao textoPadrao) {

		if(textoPadrao == null) {
			textoPadrao = new TextoPadrao();
			tiposProcessosSelecionados = new ArrayList<>();
			currentPermissoesDeUso = new ArrayList<>();
		}
		else {
			Long textoPadraoId = textoPadrao.getId();
			textoPadrao = textoPadraoService.get(textoPadraoId);

			String permissoesDeUso = textoPadrao.getPermissoesDeUso();
			if (StringUtils.isNotBlank(permissoesDeUso)) {
				List<String> allPermissoesDeUso = getAllPermissoesDeUso();
				currentPermissoesDeUso = new ArrayList<>();
				for (String permissao : allPermissoesDeUso) {
					if (permissoesDeUso.contains(permissao)) {
						currentPermissoesDeUso.add(permissao);
					}
				}
			}

			Set<TextoPadraoTipoProcesso> tiposProcessos = textoPadrao.getTiposProcessos();
			tiposProcessosSelecionados = new ArrayList<>();
			for (TextoPadraoTipoProcesso tptp : tiposProcessos) {
				TipoProcesso tipoProcesso = tptp.getTipoProcesso();
				tiposProcessosSelecionados.add(tipoProcesso);
			}
		}

		this.textoPadrao = textoPadrao;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(List<TipoProcesso> tiposProcesso) {
		this.tiposProcessos = tiposProcesso;
	}

	public TextoPadraoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(TextoPadraoFiltro filtro) {
		this.filtro = filtro;
	}

	public List<String> getAllPermissoesDeUso() {
		allPermissoesDeUso.clear();
		allPermissoesDeUso.add(TextoPadrao.ENVIO_EMAIL);
		allPermissoesDeUso.add(TextoPadrao.REGISTRO_EVIDENCIA);
		allPermissoesDeUso.add(TextoPadrao.SOLICITACAO_AREA);
		return allPermissoesDeUso;
	}

	public void setAllPermissoesDeUso(List<String> allPermissoesDeUso) {
		this.allPermissoesDeUso = allPermissoesDeUso;
	}

	public List<String> getCurrentPermissoesDeUso() {
		return currentPermissoesDeUso;
	}

	public void setCurrentPermissoesDeUso(List<String> currentPermissoesDeUso) {
		this.currentPermissoesDeUso = currentPermissoesDeUso;
	}

	public void arrumarPermissoesDeUso() {
		String tmp = this.currentPermissoesDeUso.toString();
		textoPadrao.setPermissoesDeUso(tmp);
	}

	public List<TipoProcesso> getTiposProcessosSelecionados() {
		return tiposProcessosSelecionados;
	}

	public void setTiposProcessosSelecionados(List<TipoProcesso> tiposProcessosSelecionados) {
		this.tiposProcessosSelecionados = tiposProcessosSelecionados;
	}

	public StreamedContent downloadArquivoVisualizacao(Long textoPadraoId) {
		try {
			File bodyFile = File.createTempFile("arquivo-body", ".html");
			String bodyExemplo = textoPadraoService.getBodyExemplo(textoPadraoId);
			DummyUtils.escrever(bodyFile, bodyExemplo);

			FileInputStream fis = new FileInputStream(bodyFile);
			String name = "exemplo-email.html";
			DefaultStreamedContent dsc  = DefaultStreamedContent.builder()
					.contentType("text/html")
					.name(name)
					.stream(() -> fis)
					.build();

			return dsc;
		} catch (Exception e) {
			addMessageError(e);
			return null;
		}
	}
}
