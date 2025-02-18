package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.ProcessoRegra;
import net.wasys.getdoc.domain.entity.ProcessoRegraLog;
import net.wasys.getdoc.domain.repository.ProcessoRegraLogRepository;
import net.wasys.getdoc.domain.vo.filtro.ProcessoRegraFiltro;
import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ProcessoRegraLogService {

	@Autowired private ProcessoRegraLogRepository processoRegraLogRepository;

	public ProcessoRegraLog get(Long id) {
		return processoRegraLogRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRES_NEW)
	public void saveOrUpdate(ProcessoRegraLog log) {
		processoRegraLogRepository.saveOrUpdate(log);
	}

	public List<ProcessoRegraLog> findByProcessoRegra(Long processoRegraId) {

		List<ProcessoRegraLog> processoRegraLogs = processoRegraLogRepository.findByProcessoRegra(processoRegraId);

		for (ProcessoRegraLog log : processoRegraLogs) {
			ProcessoRegra processoRegra2 = log.getProcessoRegra();
			processoRegra2.setProcessoRegraLogs(processoRegraLogs);
			//só o primeiro já basta, todos os demais logs tem o mesmo objeto
			break;
		}
		return processoRegraLogs;
	}

	public List<ProcessoRegraLog> findByFiltro(ProcessoRegraFiltro filtro) {

		List<ProcessoRegraLog> processoRegraLogs = processoRegraLogRepository.findByFiltro(filtro);

		for (ProcessoRegraLog log : processoRegraLogs) {
			ProcessoRegra processoRegra2 = log.getProcessoRegra();
			processoRegra2.setProcessoRegraLogs(processoRegraLogs);
			//só o primeiro já basta, todos os demais logs tem o mesmo objeto
			break;
		}
		return processoRegraLogs;
	}

	public List<Long> findToExpurgo(Date dataCorte, int max) {
		DummyUtils.systraceThread("dataCorte: " + DummyUtils.formatDateTime(dataCorte) + " max: " + max);
		return processoRegraLogRepository.findToExpurgo(dataCorte, max);
	}

	public void expurgar(List<Long> list) {
		DummyUtils.systraceThread("list.size() " + list.size());
		processoRegraLogRepository.expurgar(list);
	}
}
