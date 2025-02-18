package net.wasys.getdoc.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.repository.UsuarioTipoProcessoRepository;

@Service
public class UsuarioTipoProcessoService {

	@Autowired private UsuarioTipoProcessoRepository usuarioTipoProcessoRepository;

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long utpId) {
		usuarioTipoProcessoRepository.deleteById(utpId);
	}

	public boolean usuarioAtendeTipoProcesso(Long usuarioId, Long tipoProcessoId) {
		return usuarioTipoProcessoRepository.usuarioAtendeTipoProcesso(usuarioId, tipoProcessoId);
	}
}
