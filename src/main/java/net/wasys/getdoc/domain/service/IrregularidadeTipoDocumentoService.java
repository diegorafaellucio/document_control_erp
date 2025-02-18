package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.IrregularidadeTipoDocumento;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.repository.IrregularidadeTipoDocumentoRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IrregularidadeTipoDocumentoService {

	@Autowired private IrregularidadeTipoDocumentoRepository irregularidadeTipoDocumentoRepository;

	public IrregularidadeTipoDocumento get(Long id) {
		return irregularidadeTipoDocumentoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(IrregularidadeTipoDocumento irregularidadeTipoDocumento) throws MessageKeyException {

		try {
			irregularidadeTipoDocumentoRepository.saveOrUpdate(irregularidadeTipoDocumento);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<IrregularidadeTipoDocumento> findAll(Integer inicio, Integer max) {
		return irregularidadeTipoDocumentoRepository.findAll(inicio, max);
	}

	public int count() {
		return irregularidadeTipoDocumentoRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long irregularidadeTipoDocumentoId) throws MessageKeyException {

		try {
			irregularidadeTipoDocumentoRepository.deleteById(irregularidadeTipoDocumentoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<IrregularidadeTipoDocumento> findByTipoDocumentoId(Long tipoDocumentoId) {
		return irregularidadeTipoDocumentoRepository.findByTipoDocumentoId(tipoDocumentoId);
	}

	public List<Irregularidade> findIrregularidadesByTipoDocumentoId(Long tipoDocumentoId) {
		return irregularidadeTipoDocumentoRepository.findIrregularidadesByTipoDocumentoId(tipoDocumentoId);
	}

	public IrregularidadeTipoDocumento findIrregularidadesByTipoDocumentoIdAndIrregularidade(Long tipoDocumentoId, Irregularidade irregularidade) {
		return irregularidadeTipoDocumentoRepository.findIrregularidadesByTipoDocumentoIdAndIrregularidade(tipoDocumentoId, irregularidade);
	}

	public List<TipoDocumento> findTipoDocumentoByIrregularidades(Irregularidade irregularidade) {
		return irregularidadeTipoDocumentoRepository.findTipoDocumentoByIrregularidades(irregularidade);
	}

	public Map<TipoProcesso, List<TipoDocumento>> getMapTipoDocumentoByIrregularidades(Irregularidade irregularidade) {
		Map<TipoProcesso, List<TipoDocumento>> map = new LinkedHashMap<>();

		List<IrregularidadeTipoDocumento> irregularidadeTipoDocumentoList = irregularidadeTipoDocumentoRepository.findByIrregularidades(irregularidade);
		for (IrregularidadeTipoDocumento irregularidadeTipoDocumento : irregularidadeTipoDocumentoList) {
			TipoDocumento tipoDocumento = irregularidadeTipoDocumento.getTipoDocumento();
			TipoProcesso tipoProcesso = tipoDocumento.getTipoProcesso();
			List<TipoDocumento> tipoDocumentos = map.get(tipoProcesso);
			if(tipoDocumentos == null || tipoDocumentos.isEmpty()) tipoDocumentos = new ArrayList<>();
			tipoDocumentos.add(tipoDocumento);
			map.put(tipoProcesso, tipoDocumentos);
		}

		return map;
	}
}
