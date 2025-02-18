package net.wasys.getdoc.bean;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.getdoc.http.ImagemFilter;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.SuperBeanComparator;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.*;

@ManagedBean
@ViewScoped
public class UtilBean extends AbstractBean {

	@Autowired private UsuarioService usuarioService;
	@Autowired private ProcessoService processoService;
	@Autowired private EnderecoCepService enderecoCepService;
	@Autowired private ParametroService parametroService;
	@Autowired private CampoService campoService;
	@Autowired private MessageService messageService;

	public final static int QTDE_REGISTROS_PAGINA = 15;

	private DataTable lazyDataTable;
	private Object bolso;
	private Object getSelectItemsFromEnum;
	private int pagina;


	protected void initBean() { }

	public <T> T getValue(String enumClassName, String value) {
		return DummyUtils.getEnumValue(enumClassName, value);
	}

	public <T> T[] getValues(String enumClassName) {
		return DummyUtils.getEnumValues(enumClassName);
	}

	@SuppressWarnings("rawtypes")
	public String[] getValuesStr(String enumClassName) {
		Object[] enumValues = DummyUtils.getEnumValues(enumClassName);
		String[] arrayStr = new String[enumValues.length];
		for (int i = 0; i < enumValues.length; i++) {
			arrayStr[i] = ((Enum) enumValues[i]).name();
		}
		return arrayStr;
	}

	public Map<String, String> getCustomizacao() {
		return parametroService.getCustomizacao();
	}

	public String cccCorFontTituloBarra() {
		return "#"+parametroService.getValor(P.COR_FONTE_TITULO_BARRA);
	}

	public String cccCorMenu() {
		return "#"+parametroService.getValor(P.COR_MENU);
	}

	public String cccCorMenuSelecionado() {
		return "#"+parametroService.getValor(P.COR_MENU_SELECIONADO);
	}

	public String cccCorFonteMenu() {
		return "#"+parametroService.getValor(P.COR_FONTE_MENU);
	}

	public String cccCorFonteMenuSelecionado() {
		return "#"+parametroService.getValor(P.COR_FONTE_MENU_SELECIONADO);
	}

	public String getDateStr(Date data) {

		if (data == null) {
			return "";
		}

		String format = DummyUtils.formatDate(data);
		return format;
	}

	public static String getDateTimeStr2(Date data) {
		return DummyUtils.formatDateTime2(data);
	}

	public String getDateTimeStr(Date data) {

		if (data == null) {
			return "";
		}

		String format = DummyUtils.formatDateTime(data);
		return format;
	}

	public String getDateTimeLongStr(Long dataLong) {

		if (dataLong == null) {
			return "";
		}

		Date data = new Date(dataLong);
		String format = DummyUtils.formatDateTime(data);
		return format;
	}

	public String toFileSize(Long bytes) {
		if(bytes == null) {
			return "";
		}
		return DummyUtils.toFileSize(bytes);
	}

	public List<Usuario> getUsuarios() {
		UsuarioFiltro filtro = new UsuarioFiltro();
		List<Usuario> usuarios = usuarioService.findByFiltroToSelect(filtro);
		return usuarios;
	}

	public String getDataHoraAtual() {
		return DummyUtils.formatDateTime2(new Date());
	}

	public String stringToHTML(String str) {
		return DummyUtils.stringToHTML(str);
	}

	public boolean isAdminRole() {
		return getUsuarioLogado().isAdminRole();
	}

	public boolean isExtensaoImagem(String extensao) {
		extensao = extensao.toLowerCase();
		return extensao.equals("gif") || extensao.equals("png") || extensao.equals("jpg") || extensao.equals("jpeg");
	}

	public String getPathImagem(Object anexo) {
		if (anexo instanceof ProcessoLogAnexo) {
			return getPathImagem((ProcessoLogAnexo)anexo);
		}
		return getPathImagem((EmailRecebidoAnexo)anexo);
	}

	private String getPathImagem(ProcessoLogAnexo anexo) {

		String contextPath = getContextPath();
		ProcessoLog processoLog = anexo.getProcessoLog();
		Long id = processoLog.getId();
		String path = contextPath + ImagemFilter.PATH + (id != null ? ImagemFilter.ANEXO_PL : ImagemFilter.ANEXO_ER);
		String caminho = ProcessoLogAnexo.criaPath(anexo, path, "/");
		return caminho;
	}

	private String getPathImagem(EmailRecebidoAnexo anexo) {

		String contextPath = getContextPath();
		String path = contextPath + ImagemFilter.PATH + ImagemFilter.ANEXO_ER;
		String caminho = EmailRecebidoAnexo.criaPath(anexo, path, "/");
		return caminho;
	}

	public void buscarPorCep() {

		String cep = Faces.getRequestParameter("cep");
		String grupoId = Faces.getRequestParameter("grupoId");

		EnderecoCep endereco = enderecoCepService.getByCep(cep);

		Ajax.data("endereco", endereco);
		Ajax.data("grupoId", grupoId);

		Ajax.oncomplete("preencherEndereco()");
	}

	public String capitalize(String str) {
		return DummyUtils.capitalize(str);
	}

	public String getNumero(Processo processo) {
		return processo.getId().toString();
	}

	public Processo getProcesso(Long processoId) {
		return processoService.get(processoId);
	}

	public String getMode() {
		return DummyUtils.getMode();
	}

	public long getFileSizeLimit() {
		return GetdocConstants.FILE_SIZE_LIMIT;
	}

	public String getKilobytesStr(long bytes) {
		String kilobyte = DummyUtils.toKilobyte(bytes);
		return kilobyte;
	}

	public String substituirCaracteresEspeciais(String str) {
		return DummyUtils.substituirCaracteresEspeciais(str);
	}

	public String htmlToString(String str) {
		return DummyUtils.htmlToString(str);
	}

	/** Retorna um datable com lazy loading e as configurações definidas para o sistema. */
	public DataTable getLazyDataTable() {
		return lazyDataTable;
	}

	/** Configura um datable com lazy loading e as configurações definidas para o sistema. */
	public void setLazyDataTable(DataTable lazyDataTable) {
		lazyDataTable.setLazy(true);
		configurarDataTable(lazyDataTable);
	}

	private void configurarDataTable(DataTable dataTable) {
		dataTable.setFirst(0);
		dataTable.setPaginator(true);
		dataTable.setPaginatorAlwaysVisible(true);
		dataTable.setScrollable(true);
		dataTable.setRows(QTDE_REGISTROS_PAGINA);
		dataTable.setPaginatorPosition("bottom");
		dataTable.setEmptyMessage(getMessage("emptyMessage.label"));
		dataTable.setCurrentPageReportTemplate("Total: {totalRecords} | {currentPage} de {totalPages} ");
		dataTable.setPaginatorTemplate("{CurrentPageReport} {FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink}");
	}

	public String formatarJson(String str) {

		if(StringUtils.isNotBlank(str)) {
			str = formatarJson2(str);
			str = DummyUtils.stringToHTML(str);
			return str;
		}

		return str;
	}

	public String formatarJson2(String str) {

		if(StringUtils.isNotBlank(str)) {
			try {
				str = DummyUtils.stringToJson(str);
				str = DummyUtils.stringToHTML(str);
				return str;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return str;
	}

	public static String stringToJson(String str) {
		return DummyUtils.stringToJson(str);
	}

	public String formatarMilisegundosParaSegundos(Long milisegundos) {
		return DummyUtils.formatarMilisegundosParaSegundos(milisegundos);
	}

	public Object getBolso() {
		return bolso;
	}

	public void setBolso(Object bolso) {
		this.bolso = bolso;
	}

	public List<?> toList(Collection<?> collection) {
		return collection != null ? new ArrayList<>(collection) : null;
	}

	public void filtroCampoDinamico() {
		String criterio = Faces.getRequestParameter("criterio");
		String chaveFiltro = Faces.getRequestParameter("filtro");
		String paiIdStr = Faces.getRequestParameter("paiId");
		String tipoCampoIdStr = Faces.getRequestParameter("tipoCampoId");
		String grupoNome = Faces.getRequestParameter("gruponome");
		Ajax.data("tipoCampoId", tipoCampoIdStr);
		Ajax.data("gruponome", grupoNome);

		Long paiId = new Long(paiIdStr);
		Long tipoCampoId = new Long(tipoCampoIdStr);
		Map<String, String> opcoesMap = campoService.findOpcoesCampoDinamico(criterio, chaveFiltro, paiId, tipoCampoId);
		Ajax.data("opcoes", opcoesMap);
	}

	public boolean isInstanceOf(String str, Object obj) throws ClassNotFoundException {
		return obj != null && Class.forName(str).isAssignableFrom(obj.getClass());
	}

	public Object getEntryList(Object obj) {
		if(obj instanceof Map) {
			Map map = (Map) obj;
			Set set = map.entrySet();
			return new ArrayList<>(set);
		}
		return obj;
	}

	public List<SelectItem> getSelectItemsFromEnum(String enumClassName) {
		Object[] enumValues = DummyUtils.getEnumValues(enumClassName);
		List<SelectItem> list = new ArrayList<>();
		for (Object enumValue : enumValues) {
			String label = messageService.getValue(enumClassName + "." + enumValue + ".label");
			label = StringUtils.isNotBlank(label) ? label : enumValue.toString();
			SelectItem si = new SelectItem();
			si.setValue(enumValue);
			si.setLabel(label);
			list.add(si);
		}
		Collections.sort(list, new SuperBeanComparator<>("label"));
		return list;
	}

	public Map<?, ?> jsonToMap(String json) {
		try {
			if(StringUtils.isBlank(json) || !json.startsWith("{") || json.contains("EndOfLife")) {
				return null;
			}
			Map<String, Map<String, Map<String, String>>> map = (Map<String, Map<String, Map<String, String>>>) DummyUtils.jsonToObject(json, Map.class);
			return map;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getServer() {
		return DummyUtils.getServer();
	}

	public String getThreadName() {
		return Thread.currentThread().getName();
	}

	public LogAcesso getLogAcesso() {
		return LogAcessoFilter.getLogAcesso();
	}

	public String getNomeDadosInscrito() {
		return CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome();
	}

	public boolean isSubPerfilCSCADM(){
		Usuario usuarioLogado = getUsuarioLogado();
		Subperfil subperfilAtivo = usuarioLogado.getSubperfilAtivo();
		if(subperfilAtivo != null) {
			Long subperfilAtivoId = subperfilAtivo.getId();
			return Subperfil.CSC_ADM_ID.equals(subperfilAtivoId);
		}
		return false;
	}

	public boolean isSubPerfilSID(){
		Usuario usuarioLogado = getUsuarioLogado();
		Subperfil subperfilAtivo = usuarioLogado.getSubperfilAtivo();
		if(subperfilAtivo != null) {
			Long subperfilAtivoId = subperfilAtivo.getId();
			return Subperfil.SUBPERFIS_CSC_IDS.contains(subperfilAtivoId);
		}
		return false;
	}

	public static Long getCscAdmId() {
		return Subperfil.CSC_ADM_ID;
	}

	public Integer getIntervaloDistribuicaoAutomatica() {
		return parametroService.getValor(P.INTERVALO_DISTRIBUICAO_AUTOMATICA, Integer.class);
	}

	public String getDataProcessoAtual() {
		Usuario usuarioLogado = getUsuarioLogado();
		if(usuarioLogado == null) {
			return null;
		}
		Date dataProcessoAtual = usuarioLogado.getDataProcessoAtual();
		if(dataProcessoAtual == null) {
			return null;
		}
		return DummyUtils.format(dataProcessoAtual, "yyyy-MM-dd'T'HH:mm:ss");
	}

	public String formatarMilisegundosParaHoraMinutoSegundo(Long milisegundos) {
		return DummyUtils.formatarMilisegundosParaHoraMinutoSegundo(milisegundos);
	}

	public String calcularTempoEntreDatas(Date dataInicio, Date dataFinal){
		return DummyUtils.calcularTempoEntreDatas(dataInicio, dataFinal);
	}

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public void goToPage(String componente) {
		DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(componente);
		pagina = pagina > 0 ? pagina : 1;
		int first = (pagina - 1) * QTDE_REGISTROS_PAGINA;
		d.setFirst(first);
	}
}
