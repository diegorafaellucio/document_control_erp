package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.ProcessoNotificarAtilaRepository;
import net.wasys.getdoc.domain.repository.TipoDocumentoRepository;
import net.wasys.getdoc.domain.vo.filtro.TipoDocumentoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcessoNotificarAtilaService {

	@Autowired private ProcessoNotificarAtilaRepository processoNotificarAtilaRepository;

	@Transactional(rollbackFor = Exception.class)
	public void saverOrUpdate(ProcessoNotificarAtila processoNotificarAtila) {
		processoNotificarAtilaRepository.saveOrUpdate(processoNotificarAtila);

	}

	public ProcessoNotificarAtila get(Long id) {
		return processoNotificarAtilaRepository.get(id);
	}

	public ProcessoNotificarAtila findByProcessoId(Long processoId) {
		return processoNotificarAtilaRepository.findByProcessoId(processoId);
	}

	public List<ProcessoNotificarAtila> findByProcessoIds(List<Long> processoIds){
		return processoNotificarAtilaRepository.findByProcessoIds(processoIds);
	}

	public List<ProcessoNotificarAtila> findToNotificar() {
		return processoNotificarAtilaRepository.findToNotificar();
	}
}
