package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.UsuarioCampus;
import net.wasys.getdoc.domain.repository.UsuarioCampusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioCampusService {

	@Autowired private UsuarioCampusRepository usuarioCampusRepository;

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long cppId) {
		usuarioCampusRepository.deleteById(cppId);
	}

	public UsuarioCampus findByUsuarioIdAndCampusId(Long usuarioId, Long campusId) {
		return usuarioCampusRepository.findByUsuarioIdAndCampusId(usuarioId, campusId);
	}
}
