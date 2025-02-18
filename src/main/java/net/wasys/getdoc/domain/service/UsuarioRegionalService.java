package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.repository.UsuarioRegionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioRegionalService {

	@Autowired private UsuarioRegionalRepository usuarioRegionalRepository;

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long rgpId) {
		usuarioRegionalRepository.deleteById(rgpId);
	}
}
