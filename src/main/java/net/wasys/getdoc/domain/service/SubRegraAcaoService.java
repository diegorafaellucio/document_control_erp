package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.repository.SubRegraAcaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubRegraAcaoService {

	@Autowired private SubRegraAcaoRepository subRegraAcaoRepository;

	public SubRegraAcao get(Long id) {
		return subRegraAcaoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(SubRegraAcao subRegraAcao) {
		subRegraAcaoRepository.saveOrUpdate(subRegraAcao);
	}

	public List<SubRegraAcao> findBySubRegra(Long subRegraId) {
		return subRegraAcaoRepository.findBySubRegra(subRegraId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void delete(SubRegraAcao subRegraAcao) {
		Long subRegraAcaoId = subRegraAcao.getId();
		subRegraAcaoRepository.deleteById(subRegraAcaoId);
	}
}