package net.wasys.getdoc.faces.validator;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.CampoAbstract;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.faces.FacesUtil;
import org.apache.commons.lang.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@FacesValidator("campoValidator")
public class CampoValidator implements javax.faces.validator.Validator {

	private enum Regex {

		CEP_REGEX("^\\d\\d\\d\\d\\d-\\d\\d\\d$", "validacao-cepFormatoInvalido.error"),
		CNPJ_REGEX("^\\d\\d\\.\\d\\d\\d\\.\\d\\d\\d/\\d\\d\\d\\d-\\d\\d$", "validacao-cnpjFormatoInvalido.error"),
		CPF_REGEX("^\\d\\d\\d\\.\\d\\d\\d\\.\\d\\d\\d-\\d\\d$", "validacao-cpfFormatoInvalido.error"),
		CPF_CNPJ_REGEX("^(\\d\\d\\d\\.\\d\\d\\d\\.\\d\\d\\d-\\d\\d)|(\\d\\d\\.\\d\\d\\d\\.\\d\\d\\d/\\d\\d\\d\\d-\\d\\d)$", "validacao-cpfCnpjFormatoInvalido.error"),
		HORA_REGEX("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$", "validacao-horaFormatoInvalido.error"),
		TELEFONE_REGEX("^(\\(\\d\\d\\) \\d\\d\\d\\d-\\d\\d\\d\\d)|(\\(\\d\\d\\) \\d\\d\\d\\d\\d-\\d\\d\\d\\d)$", "validacao-telefoneFormatoInvalido.error"),
		EMAIL_REGEX("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", "validacao-emailFormatoInvalido.error");

		private String expression;
		private String messageKey;

		private Regex(String expression, String messageKey) {
			this.expression = expression;
			this.messageKey = messageKey;
		}

		public String getExpression() {
			return expression;
		}

		public String getMessageKey() {
			return messageKey;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void validate(FacesContext context, UIComponent paramUIComponent, Object valorObj) throws ValidatorException {

		Map<String, Object> attributes = paramUIComponent.getAttributes();
		CampoAbstract campo = (CampoAbstract) attributes.get("campo");

		if(campo == null) {
			throw new RuntimeException("Atributo campo está nulo. Adicione \"f:campo='#{campo}'\" à tag do elemento.");
		}

		String valor = null;
		if(valorObj instanceof String[]) {
			valor = DummyUtils.listToString(Arrays.asList((String[]) valorObj));
		} else {
			valor = (String) valorObj;
		}

		campo.setValor(valor);

		validate(campo, true);

		FacesContext facesContext = FacesContext.getCurrentInstance();
		UIViewRoot viewRoot = facesContext.getViewRoot();
		Map<String, Object> viewMap = viewRoot.getViewMap();
		Set<CampoAbstract> camposSet = (Set<CampoAbstract>) viewMap.get(AbstractBean.CAMPOS_MAP);
		camposSet = camposSet != null ? camposSet : new LinkedHashSet<>();
		camposSet.add(campo);
		viewMap.put(AbstractBean.CAMPOS_MAP, camposSet);
	}

	public static void validate(CampoAbstract campo, boolean validarObrigatorio) {

		String valor = campo.getValor();

		if(validarObrigatorio) {
			validaObrigatorio(campo, valor);
		}

		if(StringUtils.isEmpty(valor)) {
			return;
		}

		TipoEntradaCampo tipo = campo.getTipo();

		switch (tipo) {
		case CEP:
			validarRegex(campo, valor, Regex.CEP_REGEX);
			break;
		case CNPJ:
			validarCnpj(campo, valor);
			break;
		case CPF:
			validarCpf(campo, valor);
			break;
		case CPF_CNPJ:
			validarCpfCnpj(campo, valor);
			break;
		case DATA:
			validarData(campo, valor);
			break;
		case EMAIL:
			valor = valor.replace(";",",");
			String[] split = valor.split(",");
			for (int i = 0; i < split.length; i++) {
				String email = split[i];
				email = StringUtils.trim(email);
				validarRegex(campo, email, Regex.EMAIL_REGEX);
			}
			break;
		case HORA:
			validarRegex(campo, valor, Regex.HORA_REGEX);
			break;
		case INTEIRO:
			validarInteiro(campo, valor);
			break;
		case MOEDA:
			validarMoeda(campo, valor);
			break;
		case TELEFONE:
			validarRegex(campo, valor, Regex.TELEFONE_REGEX);
			break;
		case TEXTO:
			validarTexto(campo, valor);
			break;
		case TEXTO_LONGO:
			validarTexto(campo, valor);
			break;
		case COMBO_BOX:
		case COMBO_BOX_MULTI:
			break;
		case UF:
			break;
		case PLACA:
			break;
		case RADIO:
			break;
		case PORCENTAGEM:
			break;
		}
	}

	private static void validarCpfCnpj(CampoAbstract campo, String valor) {

		validarRegex(campo, valor, Regex.CPF_CNPJ_REGEX);

		boolean cpfCnpjValido = DummyUtils.isCpfCnpjValido(valor);
		if(!cpfCnpjValido) {
			String nomeCampo = campo.getNome();
			throwMessage("validacao-cpfCnpjInvalido.error", nomeCampo, valor);
		}
	}

	private static void validarCnpj(CampoAbstract campo, String valor) {

		validarRegex(campo, valor, Regex.CNPJ_REGEX);

		boolean cnpjValido = DummyUtils.isCnpjValido(valor);
		if(!cnpjValido) {
			String nomeCampo = campo.getNome();
			throwMessage("validacao-cnpjInvalido.error", nomeCampo, valor);
		}
	}

	private static void validarCpf(CampoAbstract campo, String valor) {

		validarRegex(campo, valor, Regex.CPF_REGEX);

		boolean cpfValido = DummyUtils.isCpfValido(valor);
		if(!cpfValido) {
			String nomeCampo = campo.getNome();
			throwMessage("validacao-cpfInvalido.error", nomeCampo, valor);
		}
	}

	private static void validarMoeda(CampoAbstract campo, String valor) {

		BigDecimal valorMoeda = DummyUtils.stringToCurrency(valor);

		if(valorMoeda == null) {
			String nomeCampo = campo.getNome();
			throwMessage("validacao-moedaFormatoInvalido.error", nomeCampo, valor);
		}
	}

	private static void validarInteiro(CampoAbstract campo, String valorStr) {

		try {
			int length = valorStr.length();

			Integer tamanhoMinimo = campo.getTamanhoMinimo();
			if(tamanhoMinimo != null && length < tamanhoMinimo) {

				String nome = campo.getNome();
				throwMessage("validacao-tamanhoMinimoInt.error", nome, tamanhoMinimo);
			}

			Integer tamanhoMaximo = campo.getTamanhoMaximo();
			if(tamanhoMaximo != null && length > tamanhoMaximo) {

				String nome = campo.getNome();
				throwMessage("validacao-tamanhoMaximoInt.error", nome, tamanhoMaximo);
			}
		}
		catch (NumberFormatException e) {
			String nomeCampo = campo.getNome();
			throwMessage("validacao-inteiroFormatoInvalido.error", nomeCampo);
		}
	}

	private static void validarData(CampoAbstract campo, String valor) {

		try {
			new SimpleDateFormat("dd/MM/yyyy").parse(valor);
		}
		catch (ParseException e) {
			String nomeCampo = campo.getNome();
			systraceThread("data inválida: " + valor + ". campo: " + nomeCampo, LogLevel.ERROR);
			throwMessage("validacao-dataFormatoInvalido.error", nomeCampo);
		}
	}

	private static void validarRegex(CampoAbstract campo, String valor, Regex regex) {

		String expression = regex.getExpression();
		boolean matches = valor.matches(expression);
		if(!matches) {
			String nomeCampo = campo.getNome();
			String messageKey = regex.getMessageKey();
			throwMessage(messageKey, nomeCampo);
		}
	}

	private static void validarTexto(CampoAbstract campo, String valor) {

		int length = valor.length();

		Integer tamanhoMinimo = campo.getTamanhoMinimo();
		if(tamanhoMinimo != null && length < tamanhoMinimo) {

			String nome = campo.getNome();
			throwMessage("validacao-tamanhoMinimo.error", nome, tamanhoMinimo);
		}

		Integer tamanhoMaximo = campo.getTamanhoMaximo();
		if(tamanhoMaximo != null && length > tamanhoMaximo) {

			String nome = campo.getNome();
			throwMessage("validacao-tamanhoMaximo.error", nome, tamanhoMaximo);
		}
	}

	private static void validaObrigatorio(CampoAbstract campo, String valor) {

		boolean obrigatorio = campo.getObrigatorio();
		if(obrigatorio && StringUtils.isBlank(valor)) {

			String nome = campo.getNome();
			throwMessage("validacao-obrigatorio.error", nome);
		}
	}

	private static void throwMessage(String key, Object... args) {

		ResourceBundle messages = FacesUtil.getMessages();
		String message = messages.getString(key);
		message = MessageFormat.format(message, args);
		throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
	}
}
