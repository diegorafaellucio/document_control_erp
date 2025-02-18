package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Advertencia;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.Pendencia;
import net.wasys.getdoc.domain.repository.AdvertenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdvertenciaService {

	@Autowired private AdvertenciaRepository advertenciaRepository;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Advertencia advertencia) {
		advertenciaRepository.saveOrUpdate(advertencia);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long id) {
		advertenciaRepository.deleteById(id);
	}

	public Advertencia getLast(Long documentoId) {
		List<Long> documentosIds = Arrays.asList(documentoId);
		List<Advertencia> list = advertenciaRepository.getLast(documentosIds);
		return list.isEmpty() ? null : list.get(0);
	}

	public Map<Irregularidade, Integer> countIrregularidadesByProcesso(Long processoId) {
		return advertenciaRepository.countIrregularidadesByProcesso(processoId);
	}
}
