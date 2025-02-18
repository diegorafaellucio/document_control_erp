package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.Pendencia;
import net.wasys.getdoc.domain.repository.PendenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PendenciaService {

	@Autowired private PendenciaRepository pendenciaRepository;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Pendencia pendencia) {
		pendenciaRepository.saveOrUpdate(pendencia);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long id) {
		pendenciaRepository.deleteById(id);
	}

	public Map<Long, Pendencia> findLasts(List<Long> documentosIds) {
		Map<Long, Pendencia> map = new HashMap<>();
		if(documentosIds.isEmpty()) {
			return map;
		}
		List<Pendencia> list = pendenciaRepository.getLast(documentosIds);
		for (Pendencia pendencia : list) {
			Documento documento = pendencia.getDocumento();
			Long documentoId = documento.getId();
			map.put(documentoId, pendencia);
		}
		return map;
	}

	public Map<Long, Pendencia> findMapToNotificar(Long processoId) {
		Map<Long, Pendencia> map = new HashMap<>();

		List<Pendencia> list = pendenciaRepository.findToNotificar(processoId);
		for (Pendencia pendencia : list) {
			Documento documento = pendencia.getDocumento();
			Long documentoId = documento.getId();
			map.put(documentoId, pendencia);
		}
		return map;
	}

	public List<Pendencia> findToNotificar(Long processoId) {
		return pendenciaRepository.findToNotificar(processoId);
	}

	public Pendencia getLast(Long documentoId) {
		List<Long> documentosIds = Arrays.asList(documentoId);
		List<Pendencia> list = pendenciaRepository.getLast(documentosIds);
		return list.isEmpty() ? null : list.get(0);
	}

	public Long getLastIrregularidade(Long documentoId) {
		return pendenciaRepository.getLastIrregularidade(documentoId);

	}

	public Map<Irregularidade, Integer> countIrregularidadesByProcesso(Long processoId) {
		return pendenciaRepository.countIrregularidadesByProcesso(processoId);
	}
}
