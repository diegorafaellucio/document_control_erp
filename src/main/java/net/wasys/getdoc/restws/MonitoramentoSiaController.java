package net.wasys.getdoc.restws;

import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.service.ConsultaExternaService;
import net.wasys.getdoc.restws.dto.MonitoramentoSiaDTO;
import net.wasys.util.DummyUtils;
import net.wasys.util.rest.AbstractController;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Controller
@RequestMapping(path="/monitoramento-sia", produces=MediaType.APPLICATION_JSON_VALUE)
public class MonitoramentoSiaController extends AbstractController {

	@Autowired private ConsultaExternaService consultaExternaService;

	@Override
	public boolean ifSecurityOk() {
		return true;
	}

	@RequestMapping(value="/check/{data}")
	public ResponseEntity<?> check(@PathVariable("data") String dataStr) {

		systraceThread("dataStr: dataStr");
		try {
			Date data = "now".equalsIgnoreCase(dataStr) || StringUtils.isBlank(dataStr) ? new Date() : DummyUtils.parse(dataStr, "yyyy-MM-dd_HH:mm");
			List<TipoConsultaExterna> tiposConsultaExterna = new ArrayList<>();
			for (TipoConsultaExterna tipoConsultaExterna : TipoConsultaExterna.values()) {
				if(tipoConsultaExterna.name().startsWith("SIA_")) {
					tiposConsultaExterna.add(tipoConsultaExterna);
				}
			}

			Map<TipoConsultaExterna, MonitoramentoSiaDTO.ServicoDTO> map = new LinkedHashMap<>();
			for (TipoConsultaExterna tipoConsultaExterna : tiposConsultaExterna) {
				MonitoramentoSiaDTO.ServicoDTO servicoDTO = new MonitoramentoSiaDTO.ServicoDTO();
				servicoDTO.setNome(tipoConsultaExterna.name());
				servicoDTO.setQuantidadeSucesso(0l);
				servicoDTO.setQuantidadeErro(0l);
				map.put(tipoConsultaExterna, servicoDTO);
			}

			List<Object[]> list = consultaExternaService.findToMonitoramento(data, tiposConsultaExterna);
			for (Object[] obj : list) {
				TipoConsultaExterna tipo = (TipoConsultaExterna) obj[0];
				StatusConsultaExterna status = (StatusConsultaExterna) obj[1];
				Long quantidade = (Long) obj[2];
				Number tempoN = (Number) obj[3];
				Long tempo = tempoN != null ? tempoN.longValue() : null;

				MonitoramentoSiaDTO.ServicoDTO servicoDTO = map.get(tipo);
				servicoDTO = servicoDTO != null ? servicoDTO : new MonitoramentoSiaDTO.ServicoDTO();
				servicoDTO.setNome(tipo.name());
				if(StatusConsultaExterna.SUCESSO.equals(status) || StatusConsultaExterna.INATIVA.equals(status)) {
					servicoDTO.setQuantidadeSucesso(quantidade);
					servicoDTO.setTempoMedioSucesso(tempo);
				}
				else {
					servicoDTO.setQuantidadeErro(quantidade);
					servicoDTO.setTempoMedioErro(tempo);
				}
				map.put(tipo, servicoDTO);
			}

			Collection<MonitoramentoSiaDTO.ServicoDTO> servicos = map.values();
			MonitoramentoSiaDTO.ServicoDTO[] servicosArray = new MonitoramentoSiaDTO.ServicoDTO[servicos.size()];
			servicos.toArray(servicosArray);

			MonitoramentoSiaDTO dto = new MonitoramentoSiaDTO();
			dto.setServicos(servicosArray);
			return new ResponseEntity<>(dto, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return handleException(e);
		}
	}
}
