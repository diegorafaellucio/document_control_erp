package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.service.MonitoramentoService;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class MonitoramentoBean extends AbstractBean {

	@Autowired private MonitoramentoService monitoramentoService;

	private String erro;
	private Map<String, BigDecimal> parametros;

	public void securityCheck() { }

	protected void initBean() {

		try {
			ServletContext servletContext = getServletContext();
			parametros = monitoramentoService.getParametros(servletContext);
		}
		catch (RuntimeException e) {

			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter ps = new PrintWriter(sw);
			e.printStackTrace(ps);

			this.erro = sw.getBuffer().toString();
			erro = StringEscapeUtils.escapeXml(erro);

			return;
		}
	}

	public String getErro() {
		return erro;
	}

	public Map<String, BigDecimal> getParametros() {
		return parametros;
	}
}
