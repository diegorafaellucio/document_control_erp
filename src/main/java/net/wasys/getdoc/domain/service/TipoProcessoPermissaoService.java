package net.wasys.getdoc.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.TipoProcessoPermissao;
import net.wasys.getdoc.domain.repository.TipoProcessoPermissaoRepository;

@Service
public class TipoProcessoPermissaoService {

	@Autowired private TipoProcessoPermissaoRepository tipoProcessoPermissaoRepository;

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long utpId) {
		tipoProcessoPermissaoRepository.deleteById(utpId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(TipoProcessoPermissao tpp) {
		tipoProcessoPermissaoRepository.saveOrUpdate(tpp);
	}

	public List<TipoProcessoPermissao> findByTipoProcesso(Long tipoProcessoId) {
		return tipoProcessoPermissaoRepository.findByTipoProcesso(tipoProcessoId);
	}
}
