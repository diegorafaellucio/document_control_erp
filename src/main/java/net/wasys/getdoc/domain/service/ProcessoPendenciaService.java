package net.wasys.getdoc.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.ProcessoPendencia;
import net.wasys.getdoc.domain.repository.ProcessoPendenciaRepository;
import net.wasys.getdoc.domain.vo.ProcessoPendenciaVO;

@Service
public class ProcessoPendenciaService {

	@Autowired private ProcessoPendenciaRepository processoPendenciaRepository;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ProcessoPendencia pp) {
		processoPendenciaRepository.saveOrUpdate(pp);
	}

	public ProcessoPendenciaVO getLastPendenciaByProcesso(Long processoId) {
		return processoPendenciaRepository.getLastPendenciaByProcesso(processoId);
	}
}
