package net.wasys.getdoc.bean;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.ReferenceByIdMarshallingStrategy;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import net.wasys.getdoc.domain.entity.LogAlteracao;
import net.wasys.getdoc.domain.service.LogAlteracaoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.Entity;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class LogAlteracaoViewBean extends AbstractBean {

	@Autowired private LogAlteracaoService logAlteracaoService;

	private Long id;

	private LogAlteracao depois;
	private Map<String, String> depoisMap;

	private LogAlteracao antes;
	private Map<String, String> antesMap;

	private Long proximoId;
	private LogAlteracao antes2;

	protected void initBean() {

		depois = logAlteracaoService.get(id);
		depoisMap = buildDadosMap(depois);

		antes = logAlteracaoService.getPrevio(depois);
		if(antes != null) {

			antesMap = buildDadosMap(antes);
			antes2 = logAlteracaoService.getPrevio(antes);
		}

		LogAlteracao proximo = logAlteracaoService.getProximo(depois);
		if(proximo != null) {
			proximoId = proximo.getId();
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> buildDadosMap(LogAlteracao logAlteracao) {

		XStream xStream = new XStream();
		xStream.setMarshallingStrategy(new ReferenceByIdMarshallingStrategy());
		xStream.registerConverter(new MapEntryConverter());

		String dados = logAlteracao.getDados();
		if(dados.isEmpty()){
			dados = "<net.wasys.getdoc.domain.entity.LogAlteracao id=\"1\">" +
					"  <descricao>Log não disponível</descricao>" +
					"</net.wasys.getdoc.domain.entity.LogAlteracao>";
		}

		Map<String, String> map = (Map<String, String>) xStream.fromXML(dados);
		map = new TreeMap<String, String>(map);

		return map;
	}

	public boolean estaDiferente(String key) {

		Map<String, String> antesMap = getAntesMap();
		if(antesMap == null) {
			return true;
		}

		Map<String, String> depoisMap = getDepoisMap();

		String antes = antesMap.get(key);
		String depois = depoisMap.get(key);

		return !StringUtils.equals(antes, depois);
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public LogAlteracao getAntes2() {
		return antes2;
	}

	public LogAlteracao getAntes() {
		return antes;
	}

	public List<String> getAntesKeyList() {
		if(antesMap == null) {
			return null;
		}
		return new ArrayList<>(antesMap.keySet());
	}

	public Map<String, String> getAntesMap() {
		return antesMap;
	}

	public LogAlteracao getDepois() {
		return depois;
	}

	public List<String> getDepoisKeyList() {
		return new ArrayList<>(depoisMap.keySet());
	}

	public Map<String, String> getDepoisMap() {
		return depoisMap;
	}

	public Long getProximoId() {
		return proximoId;
	}

	public class MapEntryConverter implements Converter {

		private Map<String, String> referenciaMap = new HashMap<String, String>();

		@SuppressWarnings("rawtypes")
		public boolean canConvert(Class clazz) {
			return Entity.class.isAssignableFrom(clazz);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {}

		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

			Map<String, String> map = new HashMap<String, String>();

			while(reader.hasMoreChildren()) {

				reader.moveDown();

				String nodeName = reader.getNodeName();
				String className = reader.getAttribute("class");
				String reference = reader.getAttribute("reference");

				if(StringUtils.isBlank(className) && StringUtils.isNotBlank(reference)) {

					String value = referenciaMap.get(nodeName + "_" + reference);
					map.put(nodeName, value);
				}
				else if(StringUtils.isNotBlank(className)) {

					String resolvesTo = reader.getAttribute("resolves-to");
					className = StringUtils.isNotBlank(resolvesTo) ? resolvesTo : className;
					className = DummyUtils.getClassName(className);

					Class<?> clazz;
					if("sql-timestamp".equals(className)) {

						clazz = Timestamp.class;
					}
					else {

						try {
							clazz = Class.forName(className);
						}
						catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}

					Object value;
					try {
						value = context.convertAnother(reader, clazz);
					}catch (Exception e){
						value = null;
					}
					if(value instanceof Set) {
						try {
							value = getValueStringFromMap(value);
						}catch (Exception e){
							value = null;
						}
					}

					try {
						map.put(nodeName, String.valueOf(value));
					}catch (Exception e){
						map.put(nodeName, null);
					}
				}
				else {

					String value = reader.getValue();
					map.put(nodeName, value);
				}

				reader.moveUp();
			}

			return map;
		}

		@SuppressWarnings("unchecked")
		private Object getValueStringFromMap(Object value) {

			StringBuilder buffer = new StringBuilder();
			List<Object> list = new ArrayList<>((Set<Object>) value);
			Collections.sort(list, new Comparator<Object>() {

				@Override
				public int compare(Object o1, Object o2) {

					String str1 = o1.toString();
					String str2 = o2.toString();
					return str1.compareTo(str2);
				}
			});

			for (Object obj : list) {

				String str = obj.toString();
				formatarObjeto(buffer, str);
			}

			String str = buffer.toString();
			str = DummyUtils.stringToHTML(str);
			return str;
		}

		private void formatarObjeto(StringBuilder buffer, String str) {

			str = str.replace("{", "");
			str = str.replace("}", "");
			String[] split = str.split(",");
			for (int i = 0; i < split.length; i++) {
				split[i] = StringUtils.trim(split[i]);
			}
			Arrays.sort(split);

			for (String str2 : split) {

				str2 = StringUtils.trim(str2);

				String label = str2.substring(0, str2.indexOf("="));
				String valor = str2.substring(str2.indexOf("=") + 1, str2.length());

				str2 = "<label class='label-log-alteracao2'>" + label + ":</label>" + valor;

				buffer.append("\n").append(str2);
			}

			buffer.append("\n");
		}
	}
}
