package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.DeparaParam;
import net.wasys.getdoc.domain.entity.DeparaRetorno;
import net.wasys.getdoc.domain.repository.DeparaParamRepository;
import net.wasys.getdoc.domain.repository.DeparaRetornoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeparaRetornoService {

	@Autowired private DeparaRetornoRepository deparaRetornoRepository;

	public DeparaRetorno get(Long id) {
		return deparaRetornoRepository.get(id);
	}

	public List<DeparaRetorno> findBySubRegraId(Long subRegraId) {
		return deparaRetornoRepository.findBySubRegraId(subRegraId);
	}

	public void deattach(DeparaRetorno deparaRetorno) {
		deparaRetornoRepository.deatach(deparaRetorno);
	}
}