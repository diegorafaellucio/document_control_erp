package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.sql.Connection;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ManagedBean
@ViewScoped
public class ArbitrarySqlQueryBean extends AbstractBean {

	@Autowired private SessionFactory sessionFactory;

	private String sqlQuery;

	private List<Map<String, Object>> select;

	protected void initBean() {}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> select(String sql) {

		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

		List<Map<String, Object>> list = query.list();

		List<Map<String, Object>> list2 = new ArrayList<>();
		for (Map<String, Object> map : list) {

			Map<String, Object> map2 = new LinkedHashMap<>();

			for (String col : map.keySet()) {
				Object value = map.get(col);
				col = col.replace("_", " ");
				map2.put(col, value);
			}

			list2.add(map2);
		}

		return list2;
	}

	public void executar() {

		select = null;

		try {

			sqlQuery = StringUtils.trim(sqlQuery);
			if(StringUtils.lowerCase(sqlQuery).startsWith("select")) {
				select = select(sqlQuery);
			}
			else {

				SessionImpl session = (SessionImpl) sessionFactory.getCurrentSession();
				Connection connection = session.connection();

				Statement statement = connection.createStatement();
				int executeUpdate = statement.executeUpdate(sqlQuery);
				SQLWarning warnings = statement.getWarnings();

				getRequest().setAttribute("executeUpdate", executeUpdate);
				if(warnings != null) {
					getRequest().setAttribute("warnings", warnings);
				}
			}
		}
		catch (Exception e) {

			e.printStackTrace();

			String stackTrace = ExceptionUtils.getStackTrace(e);
			String stringToHTML = DummyUtils.stringToHTML(stackTrace);
			getRequest().setAttribute("warnings", stringToHTML);
		}
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public List<Map<String, Object>> getSelect() {
		return select;
	}

	public void setSelect(List<Map<String, Object>> select) {
		this.select = select;
	}
}
