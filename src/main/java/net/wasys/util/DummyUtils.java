package net.wasys.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.CampoAbstract;
import net.wasys.getdoc.domain.entity.CampoGrupo;
import net.wasys.getdoc.domain.entity.GrupoAbstract;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.CampoMap.GrupoEnum;
import net.wasys.util.ddd.Entity;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.SpringJob;
import net.wasys.util.other.Base64;
import net.wasys.util.other.Bolso;
import net.wasys.util.rest.jackson.ObjectMapper;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static net.wasys.getdoc.GetdocConstants.*;

public class DummyUtils {

	public static final String SYSPREFIX = "[getdoc_captacao] ";
	private static final int[] pesoCPF = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
	private static final int[] pesoCNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
	private static final NumberFormat MINUTO_NF = NumberFormat.getNumberInstance(GetdocConstants.LOCALE_PT_BR);
	private static final NumberFormat SEGUNDO_NF = NumberFormat.getNumberInstance(GetdocConstants.LOCALE_PT_BR);
	private static final NumberFormat KILOBYTE_NF = NumberFormat.getNumberInstance(GetdocConstants.LOCALE_PT_BR);
	private static final NumberFormat MEGABYTES_NF = NumberFormat.getNumberInstance(GetdocConstants.LOCALE_PT_BR);
	private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(GetdocConstants.LOCALE_PT_BR);
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(GetdocConstants.LOCALE_PT_BR);
	private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
	private static final String DATE_TIME_FORMAT_2 = "dd/MM/yyyy HH:mm:ss";
	private static final String DATE_TIME_FORMAT_3 = "dd/MM/yyyy HH:mm:ss:SSS";
	private static final String DATE_TIME_FORMAT_4 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String DATE_TIME_FORMAT_5 = "yyyy-MM-dd HH:mm:ss";
	private static final String DATE_TIME_FORMAT_6 = "yyyyMMdd'T'HHmmss";
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	private static final String DATE_FORMAT_2 = "yyyyMM";
	private static final String DATE_FORMAT_3 = "dd.MM.yyyy";
	private static final String TIME_FORMAT = "HH:mm";
	private static final String TIME_FORMAT2 = "HH:mm:ss";
	private final static long TIMEOUT_CACHE = (1000 * 60 * 10);//10 minutos
	private static Bolso<String> ipLanCache = new Bolso<>();
	private static Bolso<String> ipExternalCache = new Bolso<>();
	private static Logger DELETE_LOGGER = LoggerFactory.getLogger("FILE_DELETE_LOGGER");
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
	public static LogLevel LOG_LEVEL = LogLevel.INFO;

	static {

		MINUTO_NF.setMinimumFractionDigits(2);
		MINUTO_NF.setMaximumFractionDigits(2);

		SEGUNDO_NF.setMinimumIntegerDigits(2);
		SEGUNDO_NF.setMaximumFractionDigits(0);

		KILOBYTE_NF.setMaximumFractionDigits(0);

		MEGABYTES_NF.setMaximumFractionDigits(2);
	}

	public static String formataMinutosSegundos(Double minutos) {

		String format = MINUTO_NF.format(minutos);
		String[] split = format.split(",");

		Long segundos = split.length > 0 ? Long.parseLong(split[1]) : 0;
		segundos = segundos * 60 / 100;
		String segundosStr = SEGUNDO_NF.format(segundos);

		return (StringUtils.isEmpty(split[0]) ? "0" : split[0]) + "min " + segundosStr + "seg ";// + minutos;
	}

	public static String formatarMilisegundosParaSegundos(Long milisegundos) {

		if(milisegundos == null) {
			return "0s";
		}

		BigDecimal milisegundosBD = new BigDecimal(milisegundos);
		BigDecimal segundos = milisegundosBD.divide(new BigDecimal(1000)).remainder(new BigDecimal(60));
		segundos = segundos.setScale(2, RoundingMode.HALF_EVEN);

		String segundosStr = segundos.toString();
		return segundosStr + "s";
	}

	public static BigDecimal getHoras(String horas) {

		String horaStr = horas.substring(0, 2);
		String minutoStr = horas.substring(3, 5);

		BigDecimal minutoBD = new BigDecimal(minutoStr);
		BigDecimal sessenta = new BigDecimal("60");

		minutoBD = minutoBD.divide(sessenta, 2, RoundingMode.CEILING);

		BigDecimal horaBD = new BigDecimal(horaStr);
		horaBD = horaBD.setScale(2);

		horaBD = horaBD.add(minutoBD);

		return horaBD;
	}

	public static String getHoras(BigDecimal horasExpediente, BigDecimal tempo, boolean showSegundos) {
		return getHoras(horasExpediente, tempo, false, showSegundos);
	}

	public static String getHoras(BigDecimal horasExpediente, BigDecimal tempo, boolean showDias, boolean showSegundos) {

		if(tempo == null) {
			return null;
		}

		boolean negativo = false;
		if(tempo.signum() == -1) {
			negativo = true;
			tempo = tempo.abs();
		}

		int dias = 0;
		if(showDias) {
			while(tempo.compareTo(horasExpediente) >= 0) {
				tempo = tempo.subtract(horasExpediente);
				dias++;
			}
		}

		BigDecimal horas = tempo.setScale(0, RoundingMode.DOWN);
		BigDecimal minutos = tempo.subtract(horas);
		minutos = minutos.multiply(new BigDecimal(60));
		minutos = minutos.setScale(0, RoundingMode.CEILING);

		double d = tempo.doubleValue();
		// vamos converter para segundos primeiro, e arredondando um pouco para evitar alguns problemas esquisitos
		long s= Math.round (d * 3600);
		// Agora vamos calcular horas, minutos e segundos
		long h = s / 3600;
		long m = (s - 3600 * h) / 60;
		s = s % 60;

		String diasStr = (dias > 0 ? dias + "d " : "");
		String hStr = (h < 10 ? "0" : "") + h;
		String mStr = (m < 10 ? "0" : "") + m;
		String sStr = (s < 10 ? "0" : "") + s;

		StringBuilder str = new StringBuilder();
		str.append(negativo ? "-" : "");
		str.append(diasStr);
		//		boolean apenasDias = dias > 0 && h == 0 && m == 0 && s == 0;
		//		if(!apenasDias) {

		str.append(hStr);
		str.append(":");
		str.append(mStr);
		str.append(showSegundos ? (":" + sStr) : "");
		str.append("h");
		//		}

		return str.toString();
	}

	public static int getMesesEntre(Date inicio, Date fim) {

		Calendar inicioC = Calendar.getInstance();
		inicioC.setTime(inicio);
		Calendar fimC = Calendar.getInstance();
		fimC.setTime(fim);

		int meses = 0;
		while(inicioC.before(fimC)) {

			meses++;
			inicioC.add(Calendar.MONTH, 1);
		}

		return meses;
	}

	public static String getExtensao(String fileName) {

		fileName = StringUtils.trim(fileName);
		if(StringUtils.isBlank(fileName)) {
			return null;
		}

		int lastIndexOf = fileName.lastIndexOf('.');
		if(lastIndexOf < 0) {
			return null;
		}

		String extensao = fileName.substring(lastIndexOf + 1, fileName.length());
		extensao = StringUtils.lowerCase(extensao);
		return extensao;
	}

	public static Timer iniciarPrintMemoria() {

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {

				String msgMemoria = getLogMemoria();
				systraceThread(msgMemoria);
			}
		}, 0, 2000);

		return timer;
	}

	public static String getLogMemoria() {

		Runtime runtime = Runtime.getRuntime();
		long freeMemory = runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		long totalMemory = runtime.totalMemory();

		return "usada: " + ((totalMemory - freeMemory) / 1024 / 1024) + "MB maxMemory: " + (maxMemory / 1024 / 1024) + "MB freeMemory: " + (freeMemory / 1024 / 1024) + "MB totalMemory: " + (totalMemory / 1024 / 1024) + "MB";
	}

	public static String capitalize(String str) {

		if(StringUtils.isBlank(str)) {
			return str;
		}

		str = str.replaceAll("([^ ])/([^ ])", "$1 / $2");

		str = WordUtils.capitalize(str.toLowerCase());

		str = str.replaceAll("Rg([^\\w])", "RG$1");
		str = str.replaceAll("([^\\w])Rg$", "$1RG");
		str = str.replaceAll("([^\\w])Rg$", "$1RG");
		str = str.replaceAll("^Rg$", "RG");

		str = str.replaceAll("Cpf([^\\w])", "CPF$1");
		str = str.replaceAll("([^\\w])Cpf$", "$1CPF");
		str = str.replaceAll("([^\\w])cpf$", "$1CPF");
		str = str.replaceAll("^Cpf$", "CPF");

		str = str.replaceAll("Cnpj([^\\w])", "CNPJ$1");
		str = str.replaceAll("([^\\w])Cnpj$", "$1CNPJ");
		str = str.replaceAll("([^\\w])cnpj$", "$1CNPJ");
		str = str.replaceAll("^Cnpj$", "CNPJ");

		str = str.replaceAll("Cnh([^\\w])", "CNH$1");
		str = str.replaceAll("([^\\w])Cnh$", "$1CNH");
		str = str.replaceAll("([^\\w])cnpj$", "$1CNH");
		str = str.replaceAll("^Cnh$", "CNH");

		str = str.replaceAll("Cep([^\\w])", "CEP$1");
		str = str.replaceAll("([^\\w])Cep$", "$1CEP");
		str = str.replaceAll("([^\\w])cep$", "$1CEP");
		str = str.replaceAll("^Cep$", "CEP");

		str = str.replaceAll("Uf([^\\w])", "UF$1");
		str = str.replaceAll("([^\\w])Uf$", "$1UF");
		str = str.replaceAll("([^\\w])uf$", "$1UF");
		str = str.replaceAll("^Uf$", "UF");

		str = str.replaceAll("Ddd([^\\w])", "DDD$1");
		str = str.replaceAll("([^\\w])Ddd$", "$1DDD");
		str = str.replaceAll("([^\\w])Ddd$", "$1DDD");
		str = str.replaceAll("^Ddd$", "DDD");

		str = str.replace("Da ", "da ");
		str = str.replace("De ", "de ");
		str = str.replace("Do ", "do ");
		str = str.replace("A ", "a ");
		str = str.replace("E ", "e ");
		str = str.replace("Com ", "com ");

		return str;
	}

	public static String stringToHTML(String valor) {

		if(valor == null) {
			return "";
		}

		valor = valor.replace("\n", "<br/>");
		valor = valor.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		valor = valor.replace("  ", "&nbsp;&nbsp;");

		return valor;
	}

	public static String htmlToString(String html) {

		html = Jsoup.parse(html).wholeText();
		html = StringUtils.trim(html);
		html = html.replaceAll("\r", "");
		html = html.replaceAll("\n", "`%`");
		html = html.replaceAll("^[ \t]*`%`[ \t]*`%`", "");
		html = html.replaceAll("`%`[ \t]*`%`$", "");
		html = StringUtils.trim(html);
		html = html.replaceAll("`%`", "\n");
		html = html.replaceAll("\n[ \t]*", "\n");
		return html;
	}

	public static String stringToJson(String str) {

		if(StringUtils.isNotBlank(str) && str.startsWith("{")) {
			try {
				JSONObject json = new JSONObject(str) ;
				str = json.toString(4);
				return str;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return str;
	}

	public static String formatCurrency(BigDecimal bd) {

		if(bd == null) {
			return null;
		}

		String format = CURRENCY_FORMAT.format(bd.doubleValue());
		format = format.substring(3, format.length());
		return format;
	}

	public static String formatNumber(BigDecimal bd) {

		if(bd == null) {
			return null;
		}

		String format = NUMBER_FORMAT.format(bd.doubleValue());
		return format;
	}

	public static BigDecimal stringToCurrency(String str) {

		if(StringUtils.isBlank(str)) {
			return null;
		}

		//trata os casos onde os centavos estão separados por ponto em vez de virgula
		if(str.matches(".*\\.\\d{1,2}$")) {
			str = str.replaceAll("\\.(\\d{1,2})$", ",$1");
		}

		try {
			Number parse = CURRENCY_FORMAT.parse("R$ " + str);
			BigDecimal bd = new BigDecimal(parse.toString());
			return bd;
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static BigDecimal stringToNumber(String str) {

		if(StringUtils.isBlank(str)) {
			return null;
		}

		try {
			Number parse = NUMBER_FORMAT.parse(str);
			BigDecimal bd = new BigDecimal(parse.toString());
			return bd;
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getCpf(Object obj) {

		String cpf = getCpfCnpjDesformatado(obj);
		if(StringUtils.isBlank(cpf)) {
			return null;
		}
		String cpf2 = StringUtils.leftPad(cpf, 11, "0");

		String cpf3 = "";
		cpf3 += cpf2.substring(0, 3) + ".";
		cpf3 += cpf2.substring(3, 6) + ".";
		cpf3 += cpf2.substring(6, 9) + "-";
		cpf3 += cpf2.substring(9, 11);

		return cpf3;
	}

	public static String getCpfCnpj(Object obj) {

		String str = getCpfCnpjDesformatado(obj);
		if(StringUtils.isBlank(str)) {
			return null;
		}
		if(str.matches(".*[^\\.\\-\\d].*")) {
			return (String) obj;
		}

		if(str.length() > 11) {
			return getCnpj(obj);
		} else {
			return getCpf(obj);
		}
	}

	private static String getCnpj(Object obj) {

		String cnpj = getCpfCnpjDesformatado(obj);
		if(StringUtils.isBlank(cnpj)) {
			return null;
		}
		String cnpj2 = StringUtils.leftPad(cnpj, 14, "0");

		String cnpj3 = "";
		cnpj3 += cnpj2.substring(0, 2) + ".";
		cnpj3 += cnpj2.substring(2, 5) + ".";
		cnpj3 += cnpj2.substring(5, 8) + "/";
		cnpj3 += cnpj2.substring(8, 12) + "-";
		cnpj3 += cnpj2.substring(12, 14);

		return cnpj3;
	}

	public static String getCpfCnpjDesformatado(Object obj) {

		if(obj == null) {
			return null;
		}

		String cpfCnpj = obj.toString();
		cpfCnpj = StringUtils.trim(cpfCnpj);
		if (StringUtils.isBlank(cpfCnpj)) {
			return null;
		}

		String cpfCnpj2 = cpfCnpj;
		cpfCnpj2 = cpfCnpj2.replace(".", "");
		cpfCnpj2 = cpfCnpj2.replace("-", "");
		cpfCnpj2 = cpfCnpj2.replace("/", "");
		cpfCnpj2 = cpfCnpj2.replace(" ", "");
		cpfCnpj2 = cpfCnpj2.replace("\"", "");

		return cpfCnpj2;
	}

	public static boolean isCpfCnpjValido(String cpfCnpj) {

		String str = getCpfCnpjDesformatado(cpfCnpj);

		if(str.length() > 11) {

			//cnpj
			return isCnpjValido(str);
		}
		else {

			//cpf
			return isCpfValido(str);
		}
	}

	private static int calcularDigito(String str, int[] peso) {

		int soma = 0;
		for (int indice=str.length()-1, digito; indice >= 0; indice-- ) {
			digito = Integer.parseInt(str.substring(indice,indice+1));
			soma += digito*peso[peso.length-str.length()+indice];
		}
		soma = 11 - soma % 11;
		return soma > 9 ? 0 : soma;
	}

	public static boolean isCpfValido(String cpf) {

		if (cpf == null) {
			return false;
		}

		cpf = getCpfCnpjDesformatado(cpf);
		if (cpf.length() != 11) {
			return false;
		}

		Integer digito1 = calcularDigito(cpf.substring(0,9), pesoCPF);
		Integer digito2 = calcularDigito(cpf.substring(0,9) + digito1, pesoCPF);
		return cpf.equals(cpf.substring(0,9) + digito1.toString() + digito2.toString());
	}

	public static boolean isCnpjValido(String cnpj) {

		cnpj = getCpfCnpjDesformatado(cnpj);

		if ((cnpj==null)||(cnpj.length()!=14)) {
			return false;
		}

		Integer digito1 = calcularDigito(cnpj.substring(0,12), pesoCNPJ);
		Integer digito2 = calcularDigito(cnpj.substring(0,12) + digito1, pesoCNPJ);
		return cnpj.equals(cnpj.substring(0,12) + digito1.toString() + digito2.toString());
	}

	public static String getClassName(Entity obj) {

		Class<?> clazz = obj.getClass();
		String className = clazz.getName();
		className = getClassName(className);
		return className;
	}

	public static String getClassName(String className) {

		if(className.contains("_$$_")) {
			int indexOf = className.indexOf("_$$_");
			className = className.substring(0, indexOf);
		}
		return className;
	}

	public static String getMode() {
		String mode = System.getProperty("getdoc.mode");
		return mode;
	}

	public static boolean isDev() {
		String mode = getMode();
		return StringUtils.equals(mode, "dev");
	}

	public static boolean isHomolog() {
		String mode = getMode();
		return StringUtils.equals(mode, "homolog");
	}

	public static boolean getLogarAcesso() {
		String logarAcessos = System.getProperty("getdoc.logarAcessos");
		return "true".equals(logarAcessos);
	}

	public static String listToString(List<?> list) {

		if(list == null) {
			return null;
		}

		String str = list.toString();
		str = str.replace("[", "");
		str = str.replace("]", "");
		str = str.replace(" ", "");
		return str;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> stringToList(String listStr, Class<T> resultType) {

		if(StringUtils.isBlank(listStr)) {
			return null;
		}

		List<T> list = new ArrayList<>();
		String[] split = listStr.split(",");
		for (String str : split) {
			if(StringUtils.isNotBlank(str)) {

				if(resultType.isEnum()) {
					T t = getEnumValue(resultType.getSimpleName(), str);
					list.add(t);
				}
				else {
					Constructor<?>[] constructors = resultType.getConstructors();
					for (Constructor<?> constructor : constructors) {
						Class<?>[] parameterTypes = constructor.getParameterTypes();
						Class<?>[] classesArray = new Class[] {String.class};
						if(Arrays.equals(classesArray, parameterTypes)) {
							try {
								T t = (T) constructor.newInstance(str);
								list.add(t);
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
					}
				}
			}
		}

		return list;
	}

	public static void forceDelete(File file) {

		try {
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getHashChecksum(File file) {

		try {
			FileInputStream fis = new FileInputStream(file);
			return getHashChecksum(fis);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getHashChecksum(InputStream fis) {

		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] dataBytes = new byte[1024];

			int nread = 0;
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}

			//convert the byte to hex format
			byte[] mdbytes = md.digest();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			fis.close();

			return sb.toString();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void copyFile(File origem, File destino) {

		try {
			FileUtils.copyFile(origem, destino);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String toMegabytes(long size) {
		double mb = size / 1024d / 1024d;
		String mbStr = MEGABYTES_NF.format(mb);
		return mbStr;
	}

	public static String toKilobyte(long size) {
		double kb = size / 1024d;
		String kbStr = KILOBYTE_NF.format(kb);
		return kbStr;
	}

	public static String toFileSize(Long size) {

		if(size == null) {
			return "";
		}

		double d = size / 1024d / 1024d;
		if((int) d > 0) {
			return toMegabytes(size) + " MB";
		}
		d = size / 1024d;
		if((int) d > 0) {
			return toKilobyte(size) + " KB";
		}
		return size + " B";
	}

	@SuppressWarnings("unchecked")
	public static <T> T getEnumValue(String enumClassName, String value) {

		Object[] values = getEnumValues(enumClassName);
		for (Object object : values) {

			if(String.valueOf(object).equals(value)) {
				return (T) object;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] getEnumValues(String enumClassName) {

		try {
			enumClassName = "net.wasys.getdoc.domain.enumeration." + enumClassName;

			Class<?> clazz = Class.forName(enumClassName);

			if (!clazz.isEnum()) {
				throw new RuntimeException(enumClassName + " não é uma enum");
			}

			return (T[]) clazz.getEnumConstants();
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static Calendar toCalendar(Date date) {

		if(date == null) {
			return null;
		}

		Calendar c = Calendar.getInstance();
		c.setTime(date);;
		return c;
	}

	public static Date toDate(Calendar c) {

		if(c == null) {
			return null;
		}

		return c.getTime();
	}

	public static File getPrintFile(String arquivo) {

		try {
			arquivo = arquivo.replace("data:image/png;base64,", "");
			byte[] decodeBase64 = Base64.decodeBase64(arquivo.getBytes());
			if(decodeBase64.length == 0) {
				return null;
			}

			File tempFile = File.createTempFile("uploadedImagePrint_", ".jpg");
			deleteOnExitFile(tempFile);

			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));

			bos.write(decodeBase64);

			bos.flush();
			bos.close();

			return tempFile;
		}
		catch (Exception e) {
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	public static long getMinutosEntre(Date inicio, Date fim) {
		long duration  = fim.getTime() - inicio.getTime();
		long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		return diffInMinutes;
	}

	public static void join(Runnable runnable, int timeout) {

		Thread thread = new Thread(runnable);
		thread.start();
		try {
			thread.join(timeout);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void sleep(long timeout) {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String formatDateTime2(Date data) {
		return format(data, DATE_TIME_FORMAT_2);
	}

	public static String formatDateTime3(Date data) {
		return format(data, DATE_TIME_FORMAT_3);
	}

	public static String formatDateTime4(Date data) {
		return format(data, DATE_TIME_FORMAT_4);
	}

	public static String formatDateTime5(Date data) {
		return format(data, DATE_TIME_FORMAT_5);
	}

	public static String formatDateTime6(Date data) {
		return format(data, DATE_TIME_FORMAT_6);
	}

	public static String formatDateTime(Date data) {
		return format(data, DATE_TIME_FORMAT);
	}

	public static String formatDate(Date data) {
		return format(data, DATE_FORMAT);
	}

	public static String formatTime(Date data) {
		return format(data, TIME_FORMAT);
	}

	public static String formatTime2(Date data) {
		return format(data, TIME_FORMAT2);
	}

	public static String format(Date data, String pattern) {
		return format(data, new SimpleDateFormat(pattern));
	}

	private static String format(Date data, SimpleDateFormat dateTimeFormat) {
		if(data == null) {
			return null;
		}
		return dateTimeFormat.format(data);
	}

	public static String formatDate2(Date data) {
		return format(data, DATE_FORMAT_2);
	}

	public static Date parseDateTime(String valor) {
		return parse(valor, DATE_TIME_FORMAT);
	}

	public static Date parseDateTime2(String valor) {
		return parse(valor, DATE_TIME_FORMAT_2);
	}

	public static Date parseDateTime3(String valor) {
		return parse(valor, DATE_TIME_FORMAT_3);
	}

	public static Date parseDateTime4(String valor) {
		return parse(valor, DATE_TIME_FORMAT_4);
	}

	public static Date parseDate(String valor) {
		return parse(valor, DATE_FORMAT);
	}

	public static Date parseDate3(String valor) {
		return parse(valor, DATE_FORMAT_3);
	}

	public static Date parseTime(String valor) {
		return parse(valor, TIME_FORMAT);
	}

	public static Date parse(String valor, String pattern) {
		return parse(valor, new SimpleDateFormat(pattern));
	}

	private static Date parse(String valor, SimpleDateFormat dateTimeFormat3) {
		if(StringUtils.isBlank(valor)) {
			return null;
		}
		try {
			return dateTimeFormat3.parse(valor);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getCampoProcessoValor(Processo processo, CampoMap.CampoEnum campo) {
		GrupoEnum grupo = campo.getGrupo();
		String nomeGrupo = grupo.getNome();
		String nomeCampo = campo.getNome();
		String valor = getCampoProcessoValor(processo, nomeGrupo, nomeCampo);
		return valor;
	}

	public static String getCampoProcessoValor(Processo processo, String nomeGrupo, String nomeCampo) {
		Campo campo = getCampoProcesso(processo, nomeGrupo, nomeCampo);
		String valor = campo != null ? campo.getValor() : null;
		return valor;
	}

	public static Campo getCampoProcesso(Processo processo, String nomeGrupo, String nomeCampo) {
		nomeGrupo = substituirCaracteresEspeciais(nomeGrupo);
		nomeCampo = substituirCaracteresEspeciais(nomeCampo);
		Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
		for (CampoGrupo grupo : gruposCampos) {
			String nomeGrupo2 = grupo.getNome();
			nomeGrupo2 = substituirCaracteresEspeciais(nomeGrupo2);
			if(nomeGrupo == null || StringUtils.equalsIgnoreCase(nomeGrupo, nomeGrupo2)) {
				Set<Campo> campos = grupo.getCampos();
				for (Campo campo : campos) {
					String nomeCampo2 = campo.getNome();
					nomeCampo2 = substituirCaracteresEspeciais(nomeCampo2);
					if(StringUtils.equalsIgnoreCase(nomeCampo, nomeCampo2)) {
						return campo;
					}
				}
			}
		}
		return null;
	}

	public static CampoGrupo getGrupoProcesso(Processo processo, GrupoEnum grupoEnum) {
		String nomeGrupo = grupoEnum.getNome();
		nomeGrupo = substituirCaracteresEspeciais(nomeGrupo);
		Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
		for (CampoGrupo grupo : gruposCampos) {
			String nomeGrupo2 = grupo.getNome();
			nomeGrupo2 = substituirCaracteresEspeciais(nomeGrupo2);
			if(nomeGrupo == null || StringUtils.equalsIgnoreCase(nomeGrupo, nomeGrupo2)) {
				return grupo;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends CampoAbstract> T getCampoProcesso(Processo processo, CampoMap.CampoEnum campo) {
		return (T) getCampoProcesso(processo, campo.getGrupo().getNome(), campo.getNome());
	}

	public static <T extends CampoAbstract> String getCampoProcessoValor(Collection<T> campos, CampoMap.CampoEnum campoEnum) {
		T campo = getCampoProcesso(campos, campoEnum);
		String valor = campo != null ? campo.getValor() : null;
		return valor;
	}

	public static <T extends CampoAbstract> T getCampoProcesso(Collection<T> campos, CampoMap.CampoEnum campo) {
		GrupoEnum grupo = campo.getGrupo();
		String nomeGrupo = grupo.getNome();
		String nomeCampo = campo.getNome();
		T c = getCampoProcesso(campos, nomeGrupo, nomeCampo);
		return c;
	}

	public static <T extends CampoAbstract> T getCampoProcesso(Collection<T> campos, String nomeGrupo, String nomeCampo) {
		nomeGrupo = substituirCaracteresEspeciais(nomeGrupo);
		nomeCampo = substituirCaracteresEspeciais(nomeCampo);
		for (T campo : campos) {
			GrupoAbstract grupo = campo.getGrupo();
			String nomeGrupo2 = grupo.getNome();
			nomeGrupo2 = substituirCaracteresEspeciais(nomeGrupo2);
			String nomeCampo2 = campo.getNome();
			nomeCampo2 = substituirCaracteresEspeciais(nomeCampo2);
			if((nomeGrupo == null || StringUtils.equalsIgnoreCase(nomeGrupo, nomeGrupo2))
					&& StringUtils.equalsIgnoreCase(nomeCampo, nomeCampo2)) {
				return campo;
			}
		}
		return null;
	}

	public static boolean equals(Object obj1, Object obj2) {

		if(obj1 == null && obj2 == null) {
			return true;
		}
		if(obj1 == null || obj2 == null) {
			return false;
		}

		boolean equals = obj1.equals(obj2);
		return equals;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compare(Comparable o1, Comparable o2) {

		if(o1 == null && o2 == null) {
			return 0;
		}
		else if(o1 == null) {
			return 0;
		}
		else if(o2 == null) {
			return 1;
		}

		return o1.compareTo(o2);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int compareTo(Comparable obj1, Comparable obj2, boolean nullFirst) {

		if(obj1 == null && obj2 == null) {
			return 0;
		}
		Comparable null1 = nullFirst ? obj1 : obj2;
		Comparable null2 = nullFirst ? obj2 : obj1;
		if(null1 == null) {
			return -1;
		}
		else if(null2 == null) {
			return 1;
		}

		return obj1.compareTo(obj2);
	}


	public static <I, O> O convertTypes(I valor, Class<O> resultType) {

		if(valor == null) {
			return null;
		}

		try {
			Constructor<O> constructor = resultType.getConstructor(String.class);
			String valorStr = valor.toString();
			if(StringUtils.isBlank(valorStr)) {
				return null;
			}
			O result = constructor.newInstance(valorStr);
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String getTelefone(String str) {

		if(StringUtils.isBlank(str)) {
			return null;
		}
		str = StringUtils.trim(str);
		int length = str.length();
		if(length < 10) {
			return str;
		}

		str = str.replace(" ", "");
		str = str.replace("(", "");
		str = str.replace(")", "");
		str = str.replace("-", "");
		str = str.replaceAll("^0", "");

		length = str.length();
		String ddd = str.substring(0, 2);
		String aux1 = str.substring(2, length - 4);
		String aux2 = str.substring(length - 4, length);

		return "(" + ddd + ") " + aux1 + "-" + aux2;
	}

	public static String substituirCaracteresEspeciais(String str) {

		if(StringUtils.isBlank(str)) {
			return str;
		}

		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

	public static Date max(Date data1, Date data2) {

		if(data1 == null) {
			return data2;
		}
		else if(data2 == null) {
			return data1;
		}

		return data1.after(data2) ? data1 : data2;
	}

	public static double getSimilaridade(String s1, String s2) {

		if (s1.length() < s2.length()) { // s1 should always be bigger
			String swap = s1; s1 = s2; s2 = swap;
		}
		int bigLen = s1.length();
		if (bigLen == 0) { return 1.0; /* both strings are zero length */ }
		return (bigLen - computeEditDistance(s1, s2)) / (double) bigLen;
	}

	public static int computeEditDistance(String s1, String s2) {

		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue),
									costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	public static double getSimilaridadePosicional(String s1, String s2) {

		if (s1.length() < s2.length()) { // s1 should always be bigger
			String swap = s1; s1 = s2; s2 = swap;
		}
		int bigLen = s1.length();
		if (bigLen == 0) { return 1.0; /* both strings are zero length */ }

		int equals = 0;
		int s2Length = s2.length();
		for (int i = 0; i < bigLen; i++) {

			String charAt1 = String.valueOf(s1.charAt(i));
			String charAt2 = i < s2Length ? String.valueOf(s2.charAt(i)) : "";

			if(charAt1.equals(charAt2)) {
				equals++;
			}
		}

		return (double)equals / (double)bigLen;
	}

	public static void threadSleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
	}

	public static BigDecimal getMedia(Collection<? extends Number> valores, int scale) {

		if(valores == null || valores.isEmpty()) {
			return null;
		}

		BigDecimal total = new BigDecimal("0");
		for (Number valor : valores) {
			total = total.add(new BigDecimal(valor.toString()));
		}

		BigDecimal media = total.divide(new BigDecimal(valores.size()), scale, RoundingMode.CEILING);
		return media;
	}

	public static BigDecimal getMedia1(Integer total, Set<BigDecimal> valores) {

		if(total == 0) {
			return null;
		}

		BigDecimal valorTotal = new BigDecimal(0);
		for (BigDecimal valor : valores) {

			valorTotal = valorTotal.add(valor);
		}

		BigDecimal media = valorTotal.divide(new BigDecimal(total), RoundingMode.HALF_UP);
		return media;
	}

	/** Cria um arquivo novo. Caso já exista um com o mesmo nome acrescenta um contador ("nomeexistente(1)") */
	public static File getFileDestino(File dirDestino, String fileName) {

		String extensao = DummyUtils.getExtensao(fileName);
		String nomeSemExtensao = fileName.substring(0, fileName.lastIndexOf("."));

		File file;
		int count = 0;
		do {

			String nome = count == 0 ? nomeSemExtensao : nomeSemExtensao + "(" + count + ")";

			String nomeCompleto = nome + "." + extensao;

			file = new File(dirDestino, nomeCompleto);

			count++;
		}
		while(file.exists());

		return file;
	}

	public static void executarThread(SpringJob job) throws Exception {
		executarThread(job, true);
	}

	public static void executarThread(SpringJob job, boolean join) throws Exception {

		Thread thread = new Thread(job);
		thread.start();

		if(join) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		Exception exception = job.getException();
		if (exception != null) {
			throw exception;
		}
	}

	public static String getCurrentMethodName() {
		return getCurrentMethodName(3);
	}

	public static String getCurrentMethodName(int idx) {

		Thread ct = Thread.currentThread();
		StackTraceElement[] st = ct.getStackTrace();
		StackTraceElement ste = st[idx];
		String className = ste.getClassName();
		int lastIndexOf = className.lastIndexOf(".");
		int length = className.length();
		String simpleClassName = className.substring(lastIndexOf + 1,  length);
		String mn = ste.getMethodName();

		return simpleClassName + "." + mn + "()";
	}

	public static String getExceptionMessage(Throwable e) {
		String message = e.getMessage();
		String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
		if(rootCauseMessage != null) {
			if(message == null || rootCauseMessage.endsWith(message)) {
				if(e instanceof MessageKeyException) {
					Object[] args = ((MessageKeyException) e).getArgs();
					rootCauseMessage += " " + (args != null ? Arrays.asList((Object[]) args) : "");
				}
				return rootCauseMessage;
			}
			else if(!rootCauseMessage.equals(message)) {
				return message + " Caused by: " + rootCauseMessage;
			}
		}
		return message;
	}

	public static File getFileFromResource(String path) {

		int lastIndexOfBarra = path.lastIndexOf("/");
		int lastIndexOfPonto = path.lastIndexOf(".");
		String nome = path.substring(lastIndexOfBarra + 1, lastIndexOfPonto > -1 ? lastIndexOfPonto : path.length());
		String extensao = lastIndexOfPonto > -1 ? path.substring(lastIndexOfPonto + 1) : "";

		try {
			ClassLoader classLoader = DummyUtils.class.getClassLoader();
			InputStream is = classLoader.getResourceAsStream(path);

			if(is == null) {
				is = DummyUtils.class.getResourceAsStream(path);
				if(is == null) {
					throw new FileNotFoundException(path);
				}
			}

			File tempFile = File.createTempFile(nome, "." + extensao);
			deleteOnExitFile(tempFile);
			FileUtils.copyInputStreamToFile(is, tempFile);

			return tempFile;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String getServer() {

		String server = System.getProperty("getdoc.server");
		if(StringUtils.isNotBlank(server)) {
			return server;
		}

		try {
			File file = new File("/etc/hostname");
			if(file.exists()) {
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				server = br.readLine();
				br.close();
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		if(server == null) {
			server = "localhost";
		}

		return server;
	}

	public static boolean isTempFile(File file) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if(StringUtils.isBlank(tmpDir)) {
			systraceThread("java.io.tmpdir não encontrado", LogLevel.ERROR);
			return false;
		}
		String absolutePath = file.getAbsolutePath();
		return absolutePath.contains(tmpDir) || absolutePath.contains("reaproveitamento");
	}

	public static String getLanIp() {

		long finalTime = ipLanCache != null ? ipLanCache.getFinalTime() : 0;
		long now = System.currentTimeMillis();
		String result = ipLanCache != null ? ipLanCache.getObjeto() : null;

		if(result == null || finalTime < now) {

			result = findLanIp();

			ipLanCache.setObjeto(result);
			ipLanCache.setFinalTime(now + TIMEOUT_CACHE);
		}

		return result;
	}

	public static String getExternalIp() {

		long finalTime = ipExternalCache != null ? ipExternalCache.getFinalTime() : 0;
		long now = System.currentTimeMillis();
		String result = ipExternalCache != null ? ipExternalCache.getObjeto() : null;

		if(result == null || finalTime < now) {

			result = findExternalIp();

			ipExternalCache.setObjeto(result);
			ipExternalCache.setFinalTime(now + TIMEOUT_CACHE);
		}

		return result;
	}

	private static String findExternalIp() {

		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			InputStream stream = whatismyip.openStream();
			InputStreamReader isr = new InputStreamReader(stream);
			BufferedReader in = new BufferedReader(isr);

			String ip = in.readLine();
			return ip;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "unknown";
		}
	}

	@SuppressWarnings("rawtypes")
	private static String findLanIp() {

		try {
			InetAddress candidateAddress = null;
			// Iterate all NICs (network interface cards)...
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// Iterate all IP addresses assigned to each card...
				for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {

						if (inetAddr.isSiteLocalAddress()) {
							// Found non-loopback site-local address. Return it immediately...
							return inetAddr.getHostAddress();
						}
						else if (candidateAddress == null) {
							// Found non-loopback address, but not necessarily site-local.
							// Store it as a candidate to be returned if site-local address is not subsequently found...
							candidateAddress = inetAddr;
							// Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
							// only the first. For subsequent iterations, candidate will be non-null.
						}
					}
				}
			}
			if (candidateAddress != null) {
				// We did not find a site-local address, but we found some other non-loopback address.
				// Server might have a non-site-local address assigned to its NIC (or it might be running
				// IPv6 which deprecates the "site-local" concept).
				// Return this non-loopback candidate address...
				return candidateAddress.getHostAddress();
			}
			// At this point, we did not find a non-loopback address.
			// Fall back to returning whatever InetAddress.getLocalHost() returns...
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null) {
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress.getHostAddress();
		}
		catch (Exception e) {
			e.printStackTrace();
			return "unknown";
		}
	}

	public static String getStackTrace(Exception e) {
		return getStackTrace(e, 10000);
	}

	public static String getStackTrace(Exception e, int limit) {
		String stackTrace = ExceptionUtils.getStackTrace(e);
		if(stackTrace.length() > limit) {
			stackTrace = stackTrace.substring(0, limit);
		}
		return stackTrace;
	}

	public static String removerEspacos(String str) {

		if(StringUtils.isBlank(str)) {
			return str;
		}

		str = str.replaceAll("\u00A0", "");

		return str.replaceAll("\\s","");
	}

	public static String formatarNomeVariavel(String fonte) {

		if(StringUtils.isNotBlank(fonte)) {
			fonte = DummyUtils.substituirCaracteresEspeciais(fonte);
			fonte = DummyUtils.removerEspacos(fonte);
			fonte = fonte.replace(":", "");
			fonte = fonte.replace("-", "");
			fonte = fonte.replaceAll("[0-9\\.]", "");
		}

		return fonte;
	}

	public static <T> T jsonToObject(String jsonAsString, Class<T> clazz) {
		if(StringUtils.isNotBlank(jsonAsString)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			T obj = mapper.readValue(jsonAsString, clazz);
			return obj;
		}
		return null;
	}

	public static <T> T jsonToObject(String jsonAsString, TypeReference tr) {
		if(StringUtils.isNotBlank(jsonAsString)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				T obj = mapper.readValue(jsonAsString, tr);
				return obj;
			}
			catch (IOException e) {
				throw  new RuntimeException(e);
			}
		}
		return null;
	}

	public static String listToJson(List<?> list) {

		if (list == null) {
			return null;
		}

		if (!list.isEmpty()) {
			JSONArray jsonArray = new JSONArray(list);
			return jsonArray.toString();
		}
		return null;
	}

	public static <T> List<T> jsonToList(String json, TypeReference tr){
		if(StringUtils.isNotBlank(json)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.readValue(json, tr);
			} catch (IOException e) {
				throw  new RuntimeException(e);
			}
		}
		return null;
	}


	public static String objectToJson(Object obj) {
		if(obj == null) {
			return null;
		}
		ObjectMapper om = new ObjectMapper();
		String json = om.writeValueAsString(obj);
		return json;
	}

	public static <T> List<T> removeItens(List<T> source, int size) {
		List<T> arrayList = new ArrayList<>(source.subList(0, Math.min(source.size(), size)));
		source.removeAll(arrayList);
		return arrayList;
	}

	public static List<String> getPoliticasCrivo(String politicasConf) {

		politicasConf = politicasConf.replaceAll("\\[[^\\]]*\\]", "");
		String[] split = politicasConf.split(";");
		List<String> list = new ArrayList<>();
		for (String politica : split) {
			politica = StringUtils.trim(politica);
			if(StringUtils.isNotBlank(politica)) {
				list.add(politica);
			}
		}
		return list;
	}

	public static List<String> getParametrosPoliticaCrivo(String politicasConf, String politicaSelecionada) {

		List<String> list = new ArrayList<>();
		String[] split = politicasConf.split(";");
		for (String politicaConf : split) {
			String politica = politicaConf.replaceAll("\\[.*\\]", "");
			politica = StringUtils.trim(politica);
			if(politica.equals(politicaSelecionada)) {
				politicaConf = politicaConf.replaceAll("^.*\\[", "");
				politicaConf = politicaConf.replaceAll("\\].*$", "");
				String[] split2 = politicaConf.split(",");
				for (String param : split2) {
					param = StringUtils.trim(param);
					if(StringUtils.isNotBlank(param)) {
						list.add(param);
					}
				}
			}
		}
		return list;
	}

	public static boolean isDateEquals(Date date1, Date date2, String pattern) {
		if (compareDate(date1, date2, pattern) == 0) {
			return true;
		}
		return false;
	}

	private static int compareDate(Date date1, Date date2, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String sA = format(date1, sdf);
		String sB = format(date2, sdf);
		return sA.compareTo(sB);
	}

	public static BigDecimal getPercent(BigDecimal qtde, BigDecimal total, int scale) {

		if(total == null || total.intValue() == 0) {
			return null;
		}

		BigDecimal percent = qtde.divide(total, scale + 2, RoundingMode.HALF_UP);
		percent = percent.multiply(new BigDecimal("100"));
		percent = percent.setScale(scale, RoundingMode.HALF_UP);

		return percent;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return sortByValue(map, Map.Entry.comparingByValue());
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, Comparator<? super Map.Entry<K, V>> comparator) {
		List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(comparator);
		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static BigDecimal getValorMoeda(String valorStr) {
		if(StringUtils.isBlank(valorStr)) return null;
		try {
			String aux1 = valorStr.substring(0, valorStr.length() - 2);
			aux1 = aux1.replace(",", "");
			aux1 = aux1.replace(".", "");
			String aux2 = valorStr.substring(valorStr.length() - 2, valorStr.length());
			BigDecimal valor = new BigDecimal(aux1 + "." + aux2);
			return valor;
		}
		catch (Exception e) {
			String exceptionMessage = getExceptionMessage(e);
			systraceThread("valorStr: " + valorStr + " " + exceptionMessage, LogLevel.ERROR);
			e.printStackTrace();
			return null;
		}
	}

	public static List<Future> checkTimeout(Collection<Future> futures, long timeoutLimit) {
		List<Future> futuresCancelados = new ArrayList<>();
		do {
			long agora = System.currentTimeMillis();
			for (Future future : new ArrayList<>(futures)) {
				boolean done = future.isDone();
				if(done) {
					futures.remove(future);
				} else if(agora > timeoutLimit) {
					future.cancel(true);
					futures.remove(future);
					futuresCancelados.add(future);
				}
			}
			DummyUtils.sleep(1000);
		}
		while(!futures.isEmpty());
		return futuresCancelados;
	}

	public static boolean isJson(String release) {
		release = StringUtils.trim(release);
		if(StringUtils.isBlank(release)) {
			return false;
		}
		release = release.replace("\n", " ");
		release = release.replace("\r", " ");
		//termina e começa com {} ou [{}]
		return release.matches("^\\[{0,1} *\\{.*\\} *\\]{0,1}$");
	}

	public static void registrarTrailer(File file) {

		try {
			int lineSize = 0;
			//verifica o tamanho das linhas do arquivo chegando as últimas linhas
			String line = null;
			ReversedLinesFileReader rlfr = new ReversedLinesFileReader(file, Charset.forName("UTF-8"));
			for (int i = 0; i < 5 && (line = rlfr.readLine()) != null; i++) {
				lineSize = Math.max(lineSize, line.length());
			}
			rlfr.close();
			lineSize++;

			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
			String dataGeracao = sdf1.format(new Date());

			LineNumberReader lnr = new LineNumberReader(new FileReader(file));
			lnr.skip(file.length());
			int lineNumber = lnr.getLineNumber();
			lnr.close();

			String identificador = "9999";

			String trailer = StringUtils.rightPad(identificador + dataGeracao + lineNumber, lineSize, " ");
			trailer = "\n" + trailer;
			Files.write(Paths.get(file.getAbsolutePath()), trailer.getBytes(), StandardOpenOption.APPEND);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static Map<?, ?> jsonStringToMap(String json) {
		if(!isJson(json)) {
			return null;
		}
		Map<?, ?> map = new ObjectMapper().readValue(json, LinkedHashMap.class);
		return map;
	}

	public static int getIdade(Date dataNascimento, Date dataBaseCalculo) {

		Calendar dataNascimentoCal = Calendar.getInstance();
		dataNascimentoCal.setTimeInMillis(dataNascimento.getTime());

		LocalDate localDateNascimento = LocalDate.ofYearDay(dataNascimentoCal.get(Calendar.YEAR), dataNascimentoCal.get(Calendar.DAY_OF_YEAR));

		Calendar dataBaseCalculoCal = Calendar.getInstance();
		dataBaseCalculoCal.setTime(dataBaseCalculo);

		LocalDate localDateBaseCalculo = LocalDate.ofYearDay(dataBaseCalculoCal.get(Calendar.YEAR), dataBaseCalculoCal.get(Calendar.DAY_OF_YEAR));

		return Period.between(localDateNascimento, localDateBaseCalculo).getYears();
	}

	public static int getDiasEntre(Date inicio, Date fim) {

		Calendar dataNascimentoCal = Calendar.getInstance();
		dataNascimentoCal.setTimeInMillis(inicio.getTime());
		LocalDate localInicio = LocalDate.ofYearDay(dataNascimentoCal.get(Calendar.YEAR), dataNascimentoCal.get(Calendar.DAY_OF_YEAR));

		Calendar fimCal = Calendar.getInstance();
		fimCal.setTime(fim);
		LocalDate localFim = LocalDate.ofYearDay(fimCal.get(Calendar.YEAR), fimCal.get(Calendar.DAY_OF_YEAR));

		return Period.between(localInicio, localFim).getDays();
	}

	public static Date truncateInicioDia(Date date) {
		if(date == null) return null;
		Date dia = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
		return dia;
	}

	public static Date truncateFinalDia(Date date) {
		Date hoje = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
		hoje = DateUtils.addDays(hoje, 1);
		hoje = DateUtils.addMilliseconds(hoje, -1);
		return hoje;
	}

	public static String getChamador() {
		String chamador = null;
		Thread ct = Thread.currentThread();
		StackTraceElement[] st = ct.getStackTrace();
		for (int i = 3; i < st.length; i++) {
			StackTraceElement ste = st[i];
			String className = ste.getClassName();
			if(className.startsWith("net.wasys") && !className.contains("$") && !className.equals(HibernateRepository.class.getName())) {
				int lastIndexOf = className.lastIndexOf(".");
				int length = className.length();
				String simpleClassName = className.substring(lastIndexOf + 1,  length);
				String mn = ste.getMethodName();
				String aux = simpleClassName + "." + mn + "():" + ste.getLineNumber();
				if(chamador == null) {
					chamador = aux;
				} else {
					chamador = aux + " > " + chamador;
					break;
				}
			}
		}
		return chamador;
	}

	public static String limparCharsChaveUnicidade(String str){
		if(StringUtils.isBlank(str)) {
			return str;
		}
		return str.replaceAll("\\[\"(.*)\"\\]", "$1");
	}

	public static String gerarSenhaAleatoria() {
		String random = UUID.randomUUID().toString();
		random = random.replace("-", "");
		String senha = StringUtils.substring(random, 0, 6);
		senha += "@" + StringUtils.substring(random, 6, 9);
		return senha;
	}

	public static File getFile(String tempPrefix, UploadedFile uploadedFile) {

		try {
			String fileName = uploadedFile.getFileName();
			String extensao = DummyUtils.getExtensao(fileName);

			File tmpFile = File.createTempFile(tempPrefix, "." + extensao);
			deleteOnExitFile(tmpFile);

			InputStream is = uploadedFile.getInputStream();
			FileUtils.copyInputStreamToFile(is, tmpFile);

			return tmpFile;
		}
		catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			new MessageKeyException("erroInesperadoUpload.error", rootCauseMessage);
		}

		return null;
	}

	public static String removerJsonTags(String str) {
		if (StringUtils.isNotBlank(str)) {
			str = str.replace("{", "");
			str = str.replace("}", "");
			str = str.replace("[", "");
			str = str.replace("]", "");
			str = str.replace("\"", "");
			str = str.replace("\\", "");
		}
		return str;
	}

	public static List<String> converterList(String str) {
		try {
			if (StringUtils.isNoneBlank(str)) {
				str = removerJsonTags(str);
				return Arrays.asList(str.split(","));
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	public static void deleteFile(File file) {
		boolean tempFile = isTempFile(file);
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		String remoteUser = logAcesso != null ? logAcesso.getRemoteUser() : "";
		String mode = getMode();
		mode = mode != null ? mode + " " : "";
		mode += Thread.currentThread().getName() + " - ";
		String trace = SYSPREFIX + mode + "; " + getCurrentMethodName(4) + "; " + getCurrentMethodName(3) + "; " + file.getPath() + "; tempFile: " + tempFile + "; remoteUser: " +remoteUser;
		DELETE_LOGGER.info(trace);
		//if(tempFile) {
		//file.delete();
		//}
	}

	public static void deleteOnExitFile(File file) {
		boolean tempFile = isTempFile(file);
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		String remoteUser = logAcesso != null ? logAcesso.getRemoteUser() : "";
		String mode = getMode();
		mode = mode != null ? mode + " " : "";
		mode += Thread.currentThread().getName() + " - ";
		String trace = SYSPREFIX + mode + "; " + getCurrentMethodName(4) + "; " + getCurrentMethodName(3) + "; " + file.getPath() + "; tempFile: " + tempFile + "; remoteUser: " +remoteUser;
		DELETE_LOGGER.warn(trace);
		//if(tempFile) {
		//file.deleteOnExit();
		//}
	}

	public static File gerarThreadDump() throws IOException {

		ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMxBean.dumpAllThreads(true, true);

		File tmp = File.createTempFile("threaddump-", ".txt");

		FileWriter arq = new FileWriter(tmp);
		PrintWriter gravarArq = new PrintWriter(arq);

		for (ThreadInfo info : threadInfos) {
			String threadInfoStr = getThreadInfoStr(info);
			gravarArq.printf(threadInfoStr);
		}
		arq.flush();
		arq.close();

		return tmp;
	}

	private static String getThreadInfoStr(ThreadInfo info) {
		StringBuilder sb = new StringBuilder("\"" + info.getThreadName() + "\"" + " Id=" + info.getThreadId() + " " + info.getThreadState());
		if (info.getLockName() != null) {
			sb.append(" on " + info.getLockName());
		}
		if (info.getLockOwnerName() != null) {
			sb.append(" owned by \"" + info.getLockOwnerName() + "\" Id=" + info.getLockOwnerId());
		}
		if (info.isSuspended()) {
			sb.append(" (suspended)");
		}
		if (info.isInNative()) {
			sb.append(" (in native)");
		}
		sb.append('\n');
		int i = 0;
		StackTraceElement[] stackTrace = info.getStackTrace();
		for (; i < stackTrace.length; i++) {
			StackTraceElement ste = stackTrace[i];
			sb.append("\tat " + ste.toString());
			sb.append('\n');
			if (i == 0 && info.getLockInfo() != null) {
				Thread.State ts = info.getThreadState();
				switch (ts) {
					case BLOCKED:
						sb.append("\t-  blocked on " + info.getLockInfo());
						sb.append('\n');
						break;
					case WAITING:
						sb.append("\t-  waiting on " + info.getLockInfo());
						sb.append('\n');
						break;
					case TIMED_WAITING:
						sb.append("\t-  waiting on " + info.getLockInfo());
						sb.append('\n');
						break;
					default:
				}
			}

			for (MonitorInfo mi : info.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked " + mi);
					sb.append('\n');
				}
			}
		}
		if (i < stackTrace.length) {
			sb.append("\t...");
			sb.append('\n');
		}

		LockInfo[] locks = info.getLockedSynchronizers();
		if (locks.length > 0) {
			sb.append("\n\tNumber of locked synchronizers = " + locks.length);
			sb.append('\n');
			for (LockInfo li : locks) {
				sb.append("\t- " + li);
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	public static void escrever(File file, String texto) {
		try {
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter conexao = new BufferedWriter(fw);
			conexao.write(texto);
			conexao.newLine();
			conexao.close();
		} catch (Exception e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			throw new MessageKeyException("erroInesperado.error", rootCauseMessage);
		}
	}

	public static boolean validarEmail(String email){

		Matcher matcher = pattern.matcher(email);

		return matcher.matches();

	}

	public static void mergePdf(List<String> caminhos, String fileName) {
		try {
			PDFMergerUtility ut = new PDFMergerUtility();
			for (String caminho : caminhos) {
				ut.addSource(new FileInputStream(caminho));
			}
			ut.setDestinationFileName(fileName);
			ut.mergeDocuments();
		} catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			new MessageKeyException("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public static void compactarParaZip(String arqSaida, List<String> arqEntradas) {
		int cont;
		byte[] dados = new byte[TAMANHO_BUFFER];

		try {
			FileOutputStream destino = new FileOutputStream(new File(arqSaida));
			ZipOutputStream saida = new ZipOutputStream(new BufferedOutputStream(destino));

			for (String arqEntrada : arqEntradas) {
				File file = new File(arqEntrada);
				FileInputStream streamDeEntrada = new FileInputStream(file);
				BufferedInputStream origem = new BufferedInputStream(streamDeEntrada, TAMANHO_BUFFER);
				ZipEntry entry = new ZipEntry(file.getName());
				saida.putNextEntry(entry);

				while ((cont = origem.read(dados, 0, TAMANHO_BUFFER)) != -1) {
					saida.write(dados, 0, cont);
				}
				origem.close();
			}
			saida.close();
		} catch (IOException e) {
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			new MessageKeyException("erroInesperadoUpload.error", rootCauseMessage);
		}
	}

	public static BigDecimal formatarMilisegundosParaHoras(Long milisegundos) {

		if(milisegundos == null) {
			return null;
		}

		Float milisegundosBD = new Float(milisegundos);
		Float horas = (milisegundosBD / 3600000) % 24;
		BigDecimal horasBD = new BigDecimal(horas);
		horasBD = horasBD.setScale(2, BigDecimal.ROUND_HALF_DOWN);

		return horasBD;
	}

	public static String formatStringToRegex(String str, String regex){

		if(StringUtils.isBlank(str)) {
			return null;
		}

		Matcher m = Pattern.compile(regex).matcher(str);
		if (m.find()) str = m.group(0);
		return str;
	}

	public static void setCookieLogin(HttpServletResponse response, boolean value) {
		Cookie cookie1 = new Cookie(GetdocConstants.LOGIN_AZURE, String.valueOf(value));
		cookie1.setPath("/");
		response.addCookie(cookie1);
	}

	public static String toJsonFormat(String str) {

		if(StringUtils.isBlank(str)){
			return "";
		}

		try {
			org.primefaces.shaded.json.JSONObject json = new org.primefaces.shaded.json.JSONObject(str) ;
			str = json.toString(4);
			str = DummyUtils.stringToHTML(str);
			return str;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return str;
	}

	public static String formatarMilisegundosParaHoraMinutoSegundo(Long milisegundos) {

		if(milisegundos == null) {
			return "";
		}

		String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milisegundos),
				TimeUnit.MILLISECONDS.toMinutes(milisegundos) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milisegundos)),
				(TimeUnit.MILLISECONDS.toSeconds(milisegundos) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milisegundos))) > 0 ? (TimeUnit.MILLISECONDS.toSeconds(milisegundos) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milisegundos))) : 0);
		return hms;
	}

	public static String calcularTempoEntreDatas(Date dataInicio, Date dataFinal){
		if(dataFinal == null){
			return null;
		}

		Long ms =  dataFinal.getTime() - dataInicio.getTime();

		return formatarMilisegundosParaHoraMinutoSegundo(ms);
	}

	public static List<String> formatarChaveUnicidadeBaseInterna(List<String> chavesUnicidade) {
		return chavesUnicidade.stream().map(chave -> "[\"" + chave + "\"]").collect(Collectors.toList());
	}

	public static String formatarChaveUnicidadeBaseInterna(String chave) {
		chave = "[\"" + chave + "\"]";
		return chave;
	}

	public static void setLogLevel(LogLevel logLevel) {

		if(logLevel == null) {
			logLevel = DummyUtils.LOG_LEVEL;
		}

		DummyUtils.LOG_LEVEL = logLevel;
	}

	public static LogLevel getLogLevel() {
		return LOG_LEVEL;
	}

	public static void addParameter(LogAcesso logAcesso, String paramName, Object value) {

		if(logAcesso == null) {
			return;
		}

		String parameters = logAcesso.getParameters();
		Map<Object, Object> paramsMap = (Map<Object, Object>) DummyUtils.jsonStringToMap(parameters);
		paramsMap = paramsMap != null ? paramsMap : new LinkedHashMap<>();

		String valueStr = value != null ? value.toString() : "";
		valueStr = valueStr.replace("\"", "'");

		paramsMap.put(paramName, valueStr);

		String parameters2 = DummyUtils.objectToJson(paramsMap);
		logAcesso.setParameters(parameters2);
	}

	public static void addException(LogAcesso logAcesso, Exception e) {
		if(logAcesso == null) {
			return;
		}
		String stackTrace = ExceptionUtils.getStackTrace(e);
		addException(logAcesso, stackTrace);
	}

	public static void addException(LogAcesso logAcesso, String stackTrace) {
		String exception = logAcesso.getException();
		exception = exception != null ? exception + "\n\n" : "";
		exception += stackTrace;
		logAcesso.setException(exception);
	}

	public static Date parseDate4(String valor) {
		return parse(valor, DATE_FORMAT_4);
	}

	public static void systraceThread(Object msg, LogLevel... level) {
		LogLevel level0 = level != null && level.length > 0 ? level[0] : LogLevel.DEBUG;
		if(LOG_LEVEL.encompasses(level0)) {

			String mode = getMode();
			mode = mode != null ? mode + " " : "";

			Thread ct = Thread.currentThread();
			String threadName = ct.getName();
			System.out.println(SYSPREFIX + level0.name() + " "  + mode + threadName + " " + getCurrentMethodName(3) + " > " + msg);
		}
	}

	public static LocalDate dateToLocalDate(Date dataInicio) {
		return dataInicio.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

    public static String formatLocalDate(LocalDate data) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		return data.format(dateTimeFormatter);

    }

	public static void mudarNomeThread(String nomeThread) {

		Thread current = Thread.currentThread();
		int identityHashCode = System.identityHashCode(current);
		current.setName(nomeThread + "-" + identityHashCode);
	}

	public static BigDecimal getMax(Collection<? extends Number> valores) {

		if(valores == null || valores.isEmpty()) {
			return null;
		}

		BigDecimal max = new BigDecimal("0");
		for (Number valor : valores) {
			if(valor != null) {
				max = max.max(new BigDecimal(valor.toString()));
			}
		}

		return max;
	}
}