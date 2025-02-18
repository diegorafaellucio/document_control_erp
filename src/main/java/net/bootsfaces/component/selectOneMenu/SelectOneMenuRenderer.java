package net.bootsfaces.component.selectOneMenu;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.render.FacesRenderer;

import net.bootsfaces.component.SelectItemAndComponent;
import net.bootsfaces.component.SelectItemUtils;
import net.bootsfaces.component.ajax.AJAXRenderer;
import net.bootsfaces.component.inputText.InputTextRenderer;
import net.bootsfaces.render.CoreInputRenderer;
import net.bootsfaces.render.H;
import net.bootsfaces.render.IResponsive;
import net.bootsfaces.render.IResponsiveLabel;
import net.bootsfaces.render.R;
import net.bootsfaces.render.Responsive;
import net.bootsfaces.render.Tooltip;
import net.wasys.getdoc.domain.entity.CampoAbstract;

@FacesRenderer(componentFamily = "net.bootsfaces.component", rendererType = "net.bootsfaces.component.selectOneMenu.SelectOneMenu")
@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class SelectOneMenuRenderer extends CoreInputRenderer {
	private static final Logger LOGGER = Logger.getLogger(InputTextRenderer.class.getName());

	public void decode(FacesContext context, UIComponent component) {
		SelectOneMenu menu = (SelectOneMenu) component;
		if (menu.isDisabled() || menu.isReadonly()) {
			return;
		}
		String outerClientId = menu.getClientId(context);
		String clientId = outerClientId + "Inner";
		String submittedOptionValue = (String) context.getExternalContext().getRequestParameterMap().get(clientId);

		//TODO alterado
		Map<String, Object> attributes = component.getAttributes();
		CampoAbstract campo = (CampoAbstract) attributes.get("campo");
		if (campo != null) {
			menu.setSubmittedValue(submittedOptionValue);
			menu.setValid(true);
			menu.validateValue(context, (Object) submittedOptionValue);
			new AJAXRenderer().decode(context, component, clientId);
			return;
		}

		List items = SelectItemUtils.collectOptions((FacesContext) context, (UIComponent) menu);
		if (null != submittedOptionValue) {
			for (int index = 0; index < items.size(); ++index) {
				String currentOptionValueAsString;
				SelectItem currentOption = ((SelectItemAndComponent) items.get(index)).getSelectItem();
				Object currentOptionValue = null;
				if (currentOption instanceof SelectItem && !currentOption.isDisabled()
						&& null == (currentOptionValue = currentOption.getValue())) {
					currentOptionValue = currentOption.getLabel();
				}
				if (!submittedOptionValue.equals(currentOptionValueAsString = currentOptionValue instanceof String
						? (String) currentOptionValue
								: String.valueOf(index)))
					continue;
				menu.setSubmittedValue(currentOptionValue);
				menu.setValid(true);
				menu.validateValue(context, (Object) submittedOptionValue);
				new AJAXRenderer().decode(context, component, clientId);
				return;
			}
			menu.validateValue(context, (Object) null);
			menu.setSubmittedValue((Object) null);
			menu.setValid(false);
			return;
		}
		menu.setValid(true);
		menu.validateValue(context, (Object) submittedOptionValue);
		menu.setSubmittedValue((Object) submittedOptionValue);
		new AJAXRenderer().decode(context, component, clientId);
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		SelectOneMenu menu = (SelectOneMenu) component;
		if (!menu.isRendered()) {
			return;
		}
		ResponseWriter rw = context.getResponseWriter();
		String outerClientId = menu.getClientId(context);
		boolean clientIdHasBeenRendered = false;
		String clientId = outerClientId + "Inner";
		String span = null;
		if (!SelectOneMenuRenderer.isHorizontalForm((UIComponent) component)
				&& null != (span = this.startColSpanDiv(rw, menu, outerClientId))) {
			Tooltip.generateTooltip((FacesContext) context, (UIComponent) menu, (ResponseWriter) rw);
			clientIdHasBeenRendered = true;
		}
		rw.startElement("div", (UIComponent) menu);
		rw.writeAttribute("class", (Object) this.getWithFeedback(this.getInputMode(menu.isInline()), component),
				"class");
		if (!clientIdHasBeenRendered) {
			rw.writeAttribute("id", (Object) outerClientId, "id");
			Tooltip.generateTooltip((FacesContext) context, (UIComponent) menu, (ResponseWriter) rw);
		}
		this.writeAttribute(rw, "dir", (Object) menu.getDir(), "dir");
		this.addLabel(rw, clientId, menu, outerClientId);
		if (SelectOneMenuRenderer.isHorizontalForm((UIComponent) component)) {
			span = this.startColSpanDiv(rw, menu, null);
		}
		UIComponent prependingAddOnFacet = menu.getFacet("prepend");
		UIComponent appendingAddOnFacet = menu.getFacet("append");
		boolean hasAddon = this.startInputGroupForAddOn(rw, prependingAddOnFacet != null, appendingAddOnFacet != null,
				menu);
		this.addPrependingAddOnToInputGroup(context, rw, prependingAddOnFacet, prependingAddOnFacet != null, menu);
		this.renderSelectTag(context, rw, clientId, menu, outerClientId);
		this.addAppendingAddOnToInputGroup(context, rw, appendingAddOnFacet, appendingAddOnFacet != null, menu);
		this.closeInputGroupForAddOn(rw, hasAddon);
		rw.endElement("div");
		this.closeColSpanDiv(rw, span);
		Tooltip.activateTooltips((FacesContext) context, (UIComponent) menu);
	}

	protected void addAppendingAddOnToInputGroup(FacesContext context, ResponseWriter rw,
			UIComponent appendingAddOnFacet, boolean hasAppendingAddOn, SelectOneMenu menu) throws IOException {
		if (hasAppendingAddOn) {
			R.decorateFacetComponent((UIComponent) menu, (UIComponent) appendingAddOnFacet, (FacesContext) context,
					(ResponseWriter) rw);
		}
	}

	protected void addLabel(ResponseWriter rw, String clientId, SelectOneMenu menu, String outerClientId)
			throws IOException {
		String label = menu.getLabel();
		if (!menu.isRenderLabel()) {
			label = null;
		}
		if (label != null) {
			rw.startElement("label", (UIComponent) menu);
			rw.writeAttribute("for", (Object) clientId, "for");
			this.generateErrorAndRequiredClass((UIInput) menu, rw, outerClientId, menu.getLabelStyleClass(),
					Responsive.getResponsiveLabelClass((IResponsiveLabel) menu), "control-label");
			this.writeAttribute(rw, "style", (Object) menu.getLabelStyle());
			rw.writeText((Object) label, null);
			rw.endElement("label");
		}
	}

	protected void addPrependingAddOnToInputGroup(FacesContext context, ResponseWriter rw,
			UIComponent prependingAddOnFacet, boolean hasPrependingAddOn, SelectOneMenu menu) throws IOException {
		if (hasPrependingAddOn) {
			R.decorateFacetComponent((UIComponent) menu, (UIComponent) prependingAddOnFacet, (FacesContext) context,
					(ResponseWriter) rw);
		}
	}

	protected void closeColSpanDiv(ResponseWriter rw, String span) throws IOException {
		if (span != null && span.trim().length() > 0) {
			rw.endElement("div");
		}
	}

	protected void closeInputGroupForAddOn(ResponseWriter rw, boolean hasAddon) throws IOException {
		if (hasAddon) {
			rw.endElement("div");
		}
	}

	protected void renderSelectTag(FacesContext context, ResponseWriter rw, String clientId, SelectOneMenu menu,
			String outerClientId) throws IOException {
		this.renderSelectTag(rw, menu);
		this.renderSelectTagAttributes(rw, clientId, menu, outerClientId);
		this.renderOptions(context, rw, menu);
		this.renderInputTagEnd(rw);
	}

	protected void renderOptions(FacesContext context, ResponseWriter rw, SelectOneMenu menu) throws IOException {
		List items = SelectItemUtils.collectOptions((FacesContext) context, (UIComponent) menu);
		for (int index = 0; index < items.size(); ++index) {
			SelectItemAndComponent option = (SelectItemAndComponent) items.get(index);
			this.renderOption(context, menu, rw, option.getSelectItem(), index, option.getComponent());
		}
	}

	protected void renderOption(FacesContext context, SelectOneMenu menu, ResponseWriter rw, SelectItem selectItem,
			int index, UIComponent itemComponent) throws IOException {
		String itemLabel = selectItem.getLabel();
		String description = selectItem.getDescription();
		Object itemValue = selectItem.getValue();
		this.renderOption(context, menu, rw, index, itemLabel, description, itemValue, selectItem.isDisabled(),
				selectItem.isEscape(), itemComponent);
	}

	private Converter findImplicitConverter(FacesContext context, UIComponent component) {
		Class valueType;
		ValueExpression ve = component.getValueExpression("value");
		if (ve != null && (valueType = ve.getType(context.getELContext())) != null) {
			return context.getApplication().createConverter(valueType);
		}
		return null;
	}

	private String getOptionAsString(FacesContext context, SelectOneMenu menu, Object value, Converter converter)
			throws ConverterException {
		if (converter == null) {
			if (value == null) {
				return "";
			}
			if (value instanceof String) {
				return (String) value;
			}
			Converter implicitConverter = this.findImplicitConverter(context, (UIComponent) menu);
			return implicitConverter == null
					? value.toString()
							: implicitConverter.getAsString(context, (UIComponent) menu, value);
		}
		return converter.getAsString(context, (UIComponent) menu, value);
	}

	private Object coerceToModelType(FacesContext ctx, Object value, Class<? extends Object> itemValueType) {
		Object newValue;
		try {
			ExpressionFactory ef = ctx.getApplication().getExpressionFactory();
			newValue = ef.coerceToType(value, itemValueType);
		} catch (ELException ele) {
			newValue = value;
		} catch (IllegalArgumentException iae) {
			newValue = value;
		}
		return newValue;
	}

	private boolean isSelected(FacesContext context, SelectOneMenu menu, Object value, Object itemValue,
			Converter converter) {
		if (itemValue == null && value == null) {
			return true;
		}
		if (value != null) {
			Object compareValue;
			if (converter == null) {
				compareValue = this.coerceToModelType(context, itemValue, value.getClass());
			} else {
				compareValue = itemValue;
				if (compareValue instanceof String && !(value instanceof String)) {
					compareValue = converter.getAsObject(context, (UIComponent) menu, (String) compareValue);
				}
			}
			if (value.equals(compareValue)) {
				return true;
			}
		}
		return false;
	}

	private void renderOption(FacesContext context, SelectOneMenu menu, ResponseWriter rw, int index, String itemLabel,
			String description, Object itemValue, boolean isDisabled, boolean isEscape, UIComponent itemComponent)
					throws IOException {
		Object optionValue;
		Object selectedOption;
		Object submittedValue = menu.getSubmittedValue();
		Converter converter = menu.getConverter();
		String itemValueAsString = this.getOptionAsString(context, menu, itemValue, converter);
		if (submittedValue != null) {
			selectedOption = submittedValue;
			optionValue = itemValueAsString;
		} else {
			selectedOption = menu.getValue();
			optionValue = itemValue;
		}
		boolean isItemLabelBlank = itemLabel == null || itemLabel.trim().isEmpty();
		itemLabel = isItemLabelBlank ? "&nbsp;" : itemLabel;
		rw.startElement("option", itemComponent);
		rw.writeAttribute("data-label", (Object) itemLabel, null);
		if (description != null) {
			rw.writeAttribute("title", (Object) description, null);
		}
		if (itemValue != null) {
			String value = itemValue instanceof String ? (String) itemValue : String.valueOf(index);
			rw.writeAttribute("value", (Object) value, "value");
			if (this.isSelected(context, menu, selectedOption, optionValue, converter)) {
				rw.writeAttribute("selected", (Object) "true", "selected");
			}
		} else if (itemLabel.equals(selectedOption)) {
			rw.writeAttribute("selected", (Object) "true", "selected");
		}
		if (isDisabled) {
			rw.writeAttribute("disabled", (Object) "disabled", "disabled");
		}
		if (isEscape && !isItemLabelBlank) {
			rw.writeText((Object) itemLabel, null);
		} else {
			rw.write(itemLabel);
		}
		rw.endElement("option");
	}

	protected void renderSelectTag(ResponseWriter rw, SelectOneMenu menu) throws IOException {
		rw.write("\n");
		rw.startElement("select", (UIComponent) menu);
	}

	protected void renderSelectTagAttributes(ResponseWriter rw, String clientId, SelectOneMenu menu,
			String outerClientId) throws IOException {
		String cssClass;
		rw.writeAttribute("id", (Object) clientId, null);
		rw.writeAttribute("name", (Object) clientId, null);
		StringBuilder sb = new StringBuilder(20);
		sb.append("form-control");
		String fsize = menu.getFieldSize();
		if (fsize != null) {
			sb.append(" input-").append(fsize);
		}
		if ((cssClass = menu.getStyleClass()) != null) {
			sb.append(" ").append(cssClass);
		}
		sb.append(" ").append(this.getErrorAndRequiredClass((UIInput) menu, outerClientId));
		String s = sb.toString().trim();
		if (s != null && s.length() > 0) {
			rw.writeAttribute("class", (Object) s, "class");
		}
		if (menu.isDisabled()) {
			rw.writeAttribute("disabled", (Object) "disabled", null);
		}
		if (menu.isReadonly()) {
			rw.writeAttribute("readonly", (Object) "readonly", null);
		}
		AJAXRenderer.generateBootsFacesAJAXAndJavaScript((FacesContext) FacesContext.getCurrentInstance(),
				(ClientBehaviorHolder) menu, (ResponseWriter) rw, (boolean) false);
		R.encodeHTML4DHTMLAttrs((ResponseWriter) rw, (Map) menu.getAttributes(), (String[]) H.SELECT_ONE_MENU);
	}

	protected void renderInputTagEnd(ResponseWriter rw) throws IOException {
		rw.endElement("select");
	}

	protected String startColSpanDiv(ResponseWriter rw, SelectOneMenu menu, String clientId) throws IOException {
		String clazz = Responsive.getResponsiveStyleClass((IResponsive) menu, (boolean) false);
		if (clazz != null && clazz.trim().length() > 0) {
			clazz = clazz.trim();
			rw.startElement("div", (UIComponent) menu);
			rw.writeAttribute("class", (Object) clazz, "class");
			rw.writeAttribute("id", (Object) clientId, "id");
			return clazz;
		}
		return null;
	}

	protected boolean startInputGroupForAddOn(ResponseWriter rw, boolean hasPrependingAddOn, boolean hasAppendingAddOn,
			SelectOneMenu menu) throws IOException {
		boolean hasAddon;
		boolean bl = hasAddon = hasAppendingAddOn || hasPrependingAddOn;
		if (hasAddon) {
			rw.startElement("div", (UIComponent) menu);
			rw.writeAttribute("class", (Object) "input-group", "class");
		}
		return hasAddon;
	}
}