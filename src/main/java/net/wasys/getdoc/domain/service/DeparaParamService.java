package net.wasys.getdoc.domain.service;

import java.util.List;

import net.wasys.getdoc.domain.entity.Regra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.entity.DeparaParam;
import net.wasys.getdoc.domain.repository.DeparaParamRepository;

@Service
public class DeparaParamService {

	@Autowired private DeparaParamRepository deparaParamRepository;

	public DeparaParam get(Long id) {
		return deparaParamRepository.get(id);
	}

	public List<DeparaParam> findBySubRegraId(Long id) {
		return deparaParamRepository.findBySubRegraId(id);
	}

	public void deattach(DeparaParam deparaParam) {
		deparaParamRepository.deatach(deparaParam);
	}

	public List<DeparaParam> findByRegra(Regra regra) {
		Long regraId = regra.getId();
		return deparaParamRepository.findByRegraId(regraId);
	}
}