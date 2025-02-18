package net.wasys.getdoc.domain.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.entity.ConsultaExternaLog;
import net.wasys.getdoc.domain.repository.ConsultaExternaLogRepository;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.getdoc.domain.vo.filtro.ConsultaExternaLogFiltro;

@Service
public class ConsultaExternaLogService {

	@Autowired private ConsultaExternaLogRepository consultaExternaLogRepository;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ConsultaExternaLog consultaExternaLog) {
		consultaExternaLogRepository.saveOrUpdate(consultaExternaLog);
	}

	@Transactional(rollbackFor=Exception.class)
	public void criarConsultaExternaLog(WebServiceClientVO vo, ConsultaExterna consultaExterna) {

		ConsultaExternaLog consultaExternaLog = new ConsultaExternaLog();
		consultaExternaLog.setProcesso(vo.getProcesso());
		consultaExternaLog.setUsuario(vo.getUsuario());
		consultaExternaLog.setConsultaExterna(consultaExterna);
		consultaExternaLog.setData(new Date());

		//TODO gambi
		if(vo.getProcesso() != null) {
			saveOrUpdate(consultaExternaLog);
		}
	}

	public Integer countByFiltro(ConsultaExternaLogFiltro filtro) {
		return consultaExternaLogRepository.countByFiltro(filtro);
	}

	public List<ConsultaExternaLog> findByFiltro(ConsultaExternaLogFiltro filtro, Integer first, Integer pageSize) {
		return consultaExternaLogRepository.findByFiltro(filtro, first, pageSize);
	}

	public List<Integer> total() {
		return consultaExternaLogRepository.total();
	}
}
