package net.wasys.getdoc.restws;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;

import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import net.wasys.getdoc.domain.service.MonitoramentoService;
import net.wasys.util.rest.AbstractController;
import net.wasys.util.rest.dto.MonitoramentoDTO;

import static net.wasys.util.DummyUtils.systraceThread;

@Controller
@RequestMapping(path="/monitoramento", produces=MediaType.APPLICATION_JSON_VALUE)
public class MonitoramentoController extends AbstractController {

	@Autowired private MonitoramentoService monitoramentoService;

	@Override
	public boolean ifSecurityOk() {
		return true;
	}

	@RequestMapping(value="/check")
	public ResponseEntity<?> check() {

		systraceThread("");
		try {
			ServletContext servletContext = request.getServletContext();
			MonitoramentoDTO dto = monitoramentoService.getDTO(servletContext);

			return new ResponseEntity<>(dto, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return handleException(e);
		}
	}

	@RequestMapping(value="/checkjobs")
	public ResponseEntity<?> checkjobs() {

		try {
			ServletContext servletContext = request.getServletContext();
			MonitoramentoDTO dto = monitoramentoService.getDTOJobs(servletContext);

			return new ResponseEntity<MonitoramentoDTO>(dto, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return handleException(e);
		}
	}
}
