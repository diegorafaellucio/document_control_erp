package net.wasys.util.faces.component;

import net.bootsfaces.component.inputText.InputText;
import net.bootsfaces.component.message.Message;
import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.CampoAbstract;
import net.wasys.getdoc.domain.entity.GrupoAbstract;
import net.wasys.getdoc.domain.enumeration.Estado;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.faces.validator.CampoValidator;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.component.script.CommandScript;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderComponentEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

@FacesComponent(value="net.wasys.util.faces.component.CampoComponent", createTag=true, tagName="campo")
public class CampoComponent extends UIComponentBase implements SystemEventListener {

	private UIInput campoUI;
	private Boolean podeEditar;

	public CampoComponent() {
		FacesContext context = FacesContext.getCurrentInstance();
		UIViewRoot root = context.getViewRoot();
		root.subscribeToViewEvent(PreRenderComponentEvent.class, this);
	}

	@Override
	public String getFamily() {
		return "getdoc.component";
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {

		CampoAbstract campo = getCampo(context);
		if(campo == null) {
			return;
		}

		boolean podeEditar = podeEditar(context, campo);
		TipoEntradaCampo tipo = campo.getTipo();
		if(!podeEditar) {

			ValueExpression labelVE = getValueExpression("label");
			String label = labelVE != null ? labelVE.toString() : null;
			if (StringUtils.isBlank(label)) {
				String nome = campo.getNome();
				label = DummyUtils.capitalize(nome);
			}
			String valorLabel = campo.getValorLabel();

			Long campoId = campo.getId();
			String campoNome = campo.getNome();
			campoNome = DummyUtils.substituirCaracteresEspeciais(campoNome);
			String pais = campo.getPais();
			String criterioExibicao = campo.getCriterioExibicao();
			String criterioFiltro = campo.getCriterioFiltro();
			GrupoAbstract grupo = campo.getGrupo();
			String grupoNome = grupo.getNome();
			grupoNome = DummyUtils.substituirCaracteresEspeciais(grupoNome);
			TipoEntradaCampo campoTipo = campo.getTipo();
			Long tipoCampoId = campo.getTipoCampoId();

			ResponseWriter rw = context.getResponseWriter();
			rw.append("<div style='height: 20px;'>");
			rw.append("	<div style=\"width: 170px; float: left;\"><label class=\"control-label\">").append(label).append(":</label></div>");
			rw.append("	<div style=\"float: left; margin-bottom: 5px;\" class=\"valorCampo\"");
			rw.append("		campoNome='").append(campoNome).append("' ");
			rw.append("		grupoNome='").append(grupoNome).append("' ");
			rw.append("		campoTipo='").append(String.valueOf(campoTipo)).append("' ");
			rw.append("		tipoCampoId='").append(String.valueOf(tipoCampoId)).append("' ");
			if(pais != null)
				rw.append("		pais='").append(pais).append("' ");
			if(criterioExibicao != null)
				rw.append("		criterioExibicao='").append(criterioExibicao).append("' ");
			if(criterioFiltro != null)
				rw.append("		criterioFiltro='").append(criterioFiltro).append("' ");
			if(campoId != null)
				rw.append("		campoId='").append(String.valueOf(campoId)).append("' ");
			rw.append(">").append(valorLabel).append("</div>");
			rw.append("</div>");
		}
		else {

			campoUI = null;
			List<UIComponent> children = getChildren();
			children.clear();

			if(TipoEntradaCampo.RADIO.equals(tipo)) {
				ResponseWriter rw = context.getResponseWriter();
				rw.append("<span class=\"radioLista\">");
			}

			initEditComponent(campo);

			String valor = campo.getValor();
			campoUI = (UIInput) setValor(children, valor);

			if(campoUI != null) {
				Map<String, Object> attributes = campoUI.getAttributes();
				String grupoNome = (String) attributes.get("grupoNome");
				String campoNome = (String) attributes.get("campoNome");

				String campoNome2 = campo.getNome();
				campoNome2 = DummyUtils.substituirCaracteresEspeciais(campoNome2);
				GrupoAbstract grupo = campo.getGrupo();
				String grupoNome2 = grupo.getNome();
				grupoNome2 = DummyUtils.substituirCaracteresEspeciais(grupoNome2);
				if(!grupoNome2.equals(grupoNome) || !campoNome2.equals(campoNome)) {

					buildCampo(campo);
					setValor(children, valor);
				}
			}

			super.encodeBegin(context);
		}
	}

	private UIComponent setValor(List<UIComponent> children, String valor) {

		UIComponent campoUI = null;
		for (UIComponent uiComponent : children) {
			if(uiComponent instanceof CampoInputText) {
				campoUI = uiComponent;
				((CampoInputText) uiComponent).setValue(valor);
			} else if(uiComponent instanceof CampoInputTextArea) {
				campoUI = uiComponent;
				((CampoInputTextArea) uiComponent).setValue(valor);
			} else if(uiComponent instanceof CampoSelectOneMenu) {
				campoUI = uiComponent;
				((CampoSelectOneMenu) uiComponent).setValue(valor);
			} else if(uiComponent instanceof CampoHtmlSelectOneRadio) {
				campoUI = uiComponent;
				((CampoHtmlSelectOneRadio) uiComponent).setValue(valor);
			} else if(uiComponent instanceof CampoSelectManyCheckbox) {
				campoUI = uiComponent;
				((CampoSelectManyCheckbox) uiComponent).setValue(valor != null ? valor.split(",") : null);
			}
		}

		return campoUI;
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {

		CampoAbstract campo = getCampo(context);
		if(campo == null) {
			return;
		}

		ResponseWriter rw = context.getResponseWriter();

		if(podeEditar(context, campo)) {
			super.encodeEnd(context);

			TipoEntradaCampo tipo = campo.getTipo();
			if(TipoEntradaCampo.RADIO.equals(tipo)) {
				rw.append("</span>");
			}
		}

		Long campoId = campo.getId();
		rw.append("<script>handleDicaCampo(" + campoId + ")</script>");
	}

	private CampoAbstract getCampo(FacesContext context) {

		ELContext elContext = context.getELContext();
		ValueExpression campoVE = getValueExpression("campo");

		if(campoVE == null) {
			return null;
		}

		CampoAbstract campo = (CampoAbstract) campoVE.getValue(elContext);
		return campo;
	}

	@Override
	public void processEvent(SystemEvent event) throws AbortProcessingException {

		FacesContext context = FacesContext.getCurrentInstance();
		if (!context.isPostback()) {

			UIComponent parent = this;
			while(parent != null) {
				if(!parent.isRendered()) {
					//não renderiza...
					return;
				}
				parent = parent.getParent();
			}

			CampoAbstract campo = getCampo(context);
			if(campo != null) {

				if(!podeEditar(context, campo)) {
					return;
				}

				initEditComponent(campo);
			}
		}
	}

	private void initEditComponent(CampoAbstract campo) {
		buildPreppend(campo);
		buildCampo(campo);
		buildAppend(campo);
		buildMessage(campo);
	}

	private boolean podeEditar(FacesContext context, CampoAbstract campo) {

		if(podeEditar != null) {
			return podeEditar;
		}

		ELContext elContext = context.getELContext();

		ValueExpression editVE = getValueExpression("edit");
		Boolean edit = editVE != null ? (Boolean) editVE.getValue(elContext) : null;
		if(edit == null || !edit) {
			podeEditar = false;
			return podeEditar;
		}

		if(campo == null) {
			return false;
		}

		this.podeEditar = true;
		return podeEditar;
	}

	private void buildMessage(CampoAbstract campo2) {

		ValueExpression errorClassVE = getValueExpression("errorClass");
		FacesContext context = FacesContext.getCurrentInstance();
		ELContext elContext = context.getELContext();
		String errorClass = errorClassVE != null ? (String) errorClassVE.getValue(elContext) : null;
		errorClass = errorClass != null ? errorClass : "";

		Message message = new Message();
		String id = campoUI.getId();
		message.setFor(id);
		message.setStyleClass(errorClass);
		List<UIComponent> children = getChildren();
		children.add(message);
	}

	private void buildPreppend(CampoAbstract campo) {
	}

	private UIInput buildCampo(CampoAbstract campo) {

		try {
			if(campoUI == null) {
				TipoEntradaCampo tipo = campo.getTipo();
				if (Arrays.asList(TipoEntradaCampo.COMBO_BOX, TipoEntradaCampo.UF, TipoEntradaCampo.COMBO_BOX_ID).contains(tipo)) {
					campoUI = new CampoSelectOneMenu();
				}
				else if (Arrays.asList(TipoEntradaCampo.COMBO_BOX_MULTI).contains(tipo)) {
					campoUI = new CampoSelectManyCheckbox();
				}
				else if (TipoEntradaCampo.RADIO.equals(tipo)) {
					campoUI = new CampoHtmlSelectOneRadio();
				}
				else if (TipoEntradaCampo.TEXTO_LONGO.equals(tipo)) {
					campoUI = new CampoInputTextArea();
				}
				else {
					campoUI = new CampoInputText();
				}
			}

			configureCampo(campo);
		}
		catch (Exception e) {
			systraceThread("Erro ao configurar campo: " + campo, LogLevel.ERROR);
			e.printStackTrace();
			throw e;
		}

		return campoUI;
	}

	private void configureCampo(CampoAbstract campo) {

		String label = (String) getAttributes().get("label");
		if (StringUtils.isBlank(label)) {
			String nome = campo.getNome();
			label = DummyUtils.capitalize(nome);
		}

		List<UIComponent> children = getChildren();
		ValueExpression placeholderVE = getValueExpression("placeholder");
		String placeholder = placeholderVE != null ? placeholderVE.toString() : null;
		Long campoId = campo.getId();
		String campoNome = campo.getNome();
		campoNome = DummyUtils.substituirCaracteresEspeciais(campoNome);
		TipoEntradaCampo tipo = campo.getTipo();
		String valor = campo.getValor();
		GrupoAbstract grupo = campo.getGrupo();
		String grupoNome = grupo.getNome();
		grupoNome = DummyUtils.substituirCaracteresEspeciais(grupoNome);
		Long tipoCampoId = campo.getTipoCampoId();
		String pais = campo.getPais();
		String criterioExibicao = campo.getCriterioExibicao();
		String criterioFiltro = campo.getCriterioFiltro();

		boolean obrigatorio = campo.getObrigatorio();
		campoUI.setRequired(obrigatorio);
		campoUI.addValidator(new CampoValidator());

		/**os atributos adicionados aqui só serão levados em consideração se estiverem em net.bootsfaces.render.H*/
		Map<String, Object> attributes = campoUI.getAttributes();
		attributes.put("campo", campo);
		if(campoId != null) {
			attributes.put("campoId", campoId);
		}
		attributes.put("campoNome", campoNome);
		attributes.put("grupoNome", grupoNome);
		attributes.put("campoTipo", tipo);
		attributes.put("tipoCampoId", tipoCampoId);

		if(pais != null)
			attributes.put("pais", pais);
		if(criterioExibicao != null)
			attributes.put("criterioExibicao", criterioExibicao);
		if(criterioFiltro != null)
			attributes.put("criterioFiltro", criterioFiltro);

		if (Arrays.asList(TipoEntradaCampo.CEP, TipoEntradaCampo.CNPJ, TipoEntradaCampo.CPF,
				TipoEntradaCampo.CPF_CNPJ, TipoEntradaCampo.DATA, TipoEntradaCampo.EMAIL, TipoEntradaCampo.HORA,
				TipoEntradaCampo.INTEIRO, TipoEntradaCampo.MOEDA, TipoEntradaCampo.PLACA,
				TipoEntradaCampo.PORCENTAGEM, TipoEntradaCampo.TELEFONE, TipoEntradaCampo.TEXTO).contains(tipo)) {

			CampoInputText inputText = (CampoInputText) campoUI;
			inputText.setLabel(label);
			if(campoId != null) {
				inputText.setName("campo-" + campoId);
			} else {
				inputText.setName("campo-" + tipoCampoId);
			}
			inputText.setPlaceholder(placeholder);

			Integer tamanhoMaximo = campo.getTamanhoMaximo();
			if(tamanhoMaximo != null) {
				inputText.setMaxlength(tamanhoMaximo);
			}

			if(TipoEntradaCampo.CEP.equals(tipo)) {
				Long grupoId = grupo.getId();
				inputText.setOnblur("buscarPorCep({cep: $(this).val(), grupoId: " + grupoId + "})");

				CommandScript commandScript = new CommandScript();
				commandScript.setName("buscarPorCep");
				FacesContext context = FacesContext.getCurrentInstance();
				Application application = context.getApplication();
				ExpressionFactory expressionFactory = application.getExpressionFactory();
				ELContext elContext = context.getELContext();
				MethodExpression methodExpression = expressionFactory.createMethodExpression(elContext, "#{utilBean.buscarPorCep}", null, new Class[] {});
				commandScript.setActionExpression(methodExpression);
				children.add(commandScript);
			}

			children.add(campoUI);
		}
		else if (Arrays.asList(TipoEntradaCampo.TEXTO_LONGO).contains(tipo)) {

			CampoInputTextArea inputTextArea = (CampoInputTextArea) campoUI;
			inputTextArea.setLabel(label);
			inputTextArea.setPlaceholder(placeholder);

			Integer tamanhoMaximo = campo.getTamanhoMaximo();
			if(tamanhoMaximo != null) {
				inputTextArea.setMaxlength(tamanhoMaximo);
			}

			children.add(campoUI);
			FacesContext context = FacesContext.getCurrentInstance();
			String outerClientId = campoUI.getClientId(context);
			String clientId = "input_" + outerClientId;
			inputTextArea.setName(clientId);
		}
		else if (Arrays.asList(TipoEntradaCampo.COMBO_BOX, TipoEntradaCampo.UF, TipoEntradaCampo.COMBO_BOX_ID).contains(tipo)) {

			CampoSelectOneMenu selectOneMenu = (CampoSelectOneMenu) campoUI;
			selectOneMenu.setLabel(label);

			List<UIComponent> children2 = selectOneMenu.getChildren();
			UISelectItem option = new UISelectItem();
			option.setItemLabel("");
			option.setItemValue("");
			children2.add(option);

			if(TipoEntradaCampo.UF.equals(tipo)) {
				List<String> opcoesList = new ArrayList<>();
				if(TipoEntradaCampo.UF.equals(tipo)) {
					opcoesList = new ArrayList<>();
					Estado[] enumValues = DummyUtils.getEnumValues("Estado");
					for (Estado estado : enumValues) {
						opcoesList.add(estado.name());
					}
				}
				setOpcoes(children2, opcoesList);
			}
			else if(TipoEntradaCampo.COMBO_BOX.equals(tipo)) {
				List<String> opcoesList = campo.getOpcoesList();
				setOpcoes(children2, opcoesList);
				if(StringUtils.isNotBlank(valor)) {
					selectOneMenu.setAjax(true);
					selectOneMenu.setUpdate("form-evidencia:campos-situacao-panel-id");
				}
			}
			else if(TipoEntradaCampo.COMBO_BOX_ID.equals(tipo)) {
				BaseInterna baseInterna = campo.getBaseInterna();
				List<RegistroValorVO> opcoes = campo.getOpcoesBaseInterna();
				if(opcoes != null) {
					for (RegistroValorVO opcao : opcoes) {
						BaseRegistro baseRegistro = opcao.getBaseRegistro();
						String chaveUnicidade = baseRegistro.getChaveUnicidade();
						String labelStr = opcao.getLabel();
						option = new UISelectItem();
						option.setItemValue(chaveUnicidade);
						option.setItemLabel(labelStr);
						children2.add(option);
					}
				}
			}

			children.add(campoUI);
			FacesContext context = FacesContext.getCurrentInstance();
			String outerClientId = campoUI.getClientId(context);
			String clientId = outerClientId + "Inner";
			selectOneMenu.setName(clientId);
			selectOneMenu.setStyleClass("filtro");
		}
		else if (Arrays.asList(TipoEntradaCampo.COMBO_BOX_MULTI).contains(tipo)) {

			CampoSelectManyCheckbox selectMultiMenu = (CampoSelectManyCheckbox) campoUI;
			selectMultiMenu.setLabel(label);
			List<UIComponent> children2 = selectMultiMenu.getChildren();
			UISelectItem option = new UISelectItem();

			List<String> opcoesList = campo.getOpcoesList();
			setOpcoes(children2, opcoesList);

			HtmlOutputLabel htmlOutputLabel = new HtmlOutputLabel();
			htmlOutputLabel.setValue(label);
			htmlOutputLabel.setFor(selectMultiMenu.getId());
			children.add(htmlOutputLabel);

			children.add(campoUI);
			FacesContext context = FacesContext.getCurrentInstance();
			String outerClientId = campoUI.getClientId(context);
			String clientId = outerClientId;
			selectMultiMenu.setName(clientId);
			selectMultiMenu.setLayout("responsive");
			selectMultiMenu.setColumns(3);
			selectMultiMenu.setRole("presentation");
			selectMultiMenu.setStyleClass("ui-grid ui-grid-responsive");
		}
		else if (Arrays.asList(TipoEntradaCampo.RADIO).contains(tipo)) {

			CampoHtmlSelectOneRadio selectOneRadio = (CampoHtmlSelectOneRadio) campoUI;
			selectOneRadio.setLabel(label);
			attributes.put("display", "inline");

			List<String> opcoesList = campo.getOpcoesList();
			selectOneRadio.setColumns(opcoesList.size());

			if(opcoesList != null) {
				List<UIComponent> children2 = selectOneRadio.getChildren();
				for (String string : opcoesList) {
					UISelectItem option = new UISelectItem();
					option.setItemLabel(string);
					option.setItemValue(string);
					Map<String, Object> attributes2 = option.getAttributes();
					attributes2.put("campoNome", campoNome);
					attributes2.put("grupoNome", grupoNome);
					attributes2.put("campoTipo", tipo);
					attributes2.put("tipoCampoId", tipoCampoId);
					children2.add(option);
				}
			}

			HtmlOutputLabel htmlOutputLabel = new HtmlOutputLabel();
			htmlOutputLabel.setValue(label);
			htmlOutputLabel.setFor(selectOneRadio.getId());
			children.add(htmlOutputLabel);

			children.add(campoUI);
			FacesContext context = FacesContext.getCurrentInstance();
			String outerClientId = campoUI.getClientId(context);
			String clientId = outerClientId;
			selectOneRadio.setName(clientId);
		}

		setValor(children, valor);

		switch (tipo) {
			case CEP:
				InputText inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-cep");
				break;
			case CNPJ:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-cnpj");
				break;
			case COMBO_BOX:
			case COMBO_BOX_MULTI:
				break;
			case CPF:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-cpf");
				break;
			case CPF_CNPJ:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-cpf-cnpj");
				break;
			case DATA:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-date");
				break;
			case EMAIL:
				break;
			case HORA:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-time");
				break;
			case INTEIRO:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-number");
				break;
			case MOEDA:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-money");
				break;
			case PLACA:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-placa");
				break;
			case PORCENTAGEM:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-money");
				break;
			case RADIO:
				break;
			case TELEFONE:
				inputText = (InputText) campoUI;
				inputText.setStyleClass("mask-phone");
				break;
			case TEXTO:
				break;
			case TEXTO_LONGO:
				break;
			case UF:
				break;
		}
	}

	private void setOpcoes(List<UIComponent> children2, List<String> opcoesList) {
		UISelectItem option;
		if (opcoesList != null) {
			for (String string : opcoesList) {
				option = new UISelectItem();
				option.setItemLabel(string);
				option.setItemValue(string);
				children2.add(option);
			}
		}
	}

	private void buildAppend(CampoAbstract campo) {

		StringBuilder append = new StringBuilder();

		String dica = campo.getDica();
		if (StringUtils.isNotBlank(dica)) {
			Long campoId = campo.getId();
			append.append("<span ");
			append.append(" class=\"tooltipDica-" + campoId + "\"");
			append.append(" title=\"" + dica + "\"");
			append.append(">");
			append.append("<i ");
			append.append(" class=\"fa fa-question-circle\"");
			append.append(" aria-hidden=\"true\"");
			append.append("></i>");
			append.append("</span>");
		}

		TipoEntradaCampo tipo = campo.getTipo();
		if(TipoEntradaCampo.DATA.equals(tipo)) {
			append.append("<i ");
			append.append(" class=\"fa fa-calendar-o fa-12x14\"");
			append.append("></i>");
		}

		if(append.length() > 0) {
			Map<String, UIComponent> facets = campoUI.getFacets();
			facets.put("append", new TextLiteral(append.toString()));
		}
	}

	@Override
	public boolean isListenerForSource(Object source) {
		return (source instanceof UIViewRoot);
	}
}
