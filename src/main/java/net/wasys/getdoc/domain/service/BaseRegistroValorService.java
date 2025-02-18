package net.wasys.getdoc.domain.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.BaseRegistroValor;
import net.wasys.getdoc.domain.repository.BaseRegistroValorRepository;

@Service
public class BaseRegistroValorService {

	@Autowired private BaseRegistroValorRepository baseRegistroValorRepository;

	public BaseRegistroValor get(Long id) {
		return baseRegistroValorRepository.get(id);
	}

	public void criarRegistroValor(Map<String, String> colunaValor, BaseRegistro registro) {

		BaseRegistroValor registroValor;
		for (Entry<String, String> cv : colunaValor.entrySet()) {

			registroValor = new BaseRegistroValor();
			registroValor.setBaseRegistro(registro);
			registroValor.setNome(cv.getKey());
			registroValor.setValor(cv.getValue());

			baseRegistroValorRepository.saveOrUpdate(registroValor);
		}
	}

	public void removerRegistroValores(Long id) {
		baseRegistroValorRepository.deleteByBaseRegistroId(id);
	}

	public List<String> getColunasRegistro(Long baseInternaId) {
		return baseRegistroValorRepository.getColunasRegistro(baseInternaId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void deleteByRegistro(Long registroId) {
		baseRegistroValorRepository.deleteByRegistro(registroId);
	}

	public BaseRegistroValor getByNomeAndBaseInterna(String valor, Long baseInternaId) {
		return baseRegistroValorRepository.getByNomeAndBaseInterna(valor, baseInternaId);
	}
}