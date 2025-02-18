package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.BaseRelacionamento;
import net.wasys.getdoc.domain.repository.BaseRelacionamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
public class BaseRelacionamentoService {

	@Autowired private BaseRelacionamentoRepository baseRelacionamentoRepository;

	public BaseRelacionamento get(Long id) {
		return baseRelacionamentoRepository.get(id);
	}

	public BaseRelacionamento buscarRelacionamentoDaColuna(Collection<BaseRelacionamento> relacionamentos, String nomeColuna) {

		BaseRelacionamento relacionamento = null;
		for (BaseRelacionamento rel : relacionamentos) {

			String chaveExtrangeira = rel.getChaveExtrangeira();
			chaveExtrangeira = chaveExtrangeira.replace("\"", "").replace("[", "").replace("]", "");

			if (chaveExtrangeira.equals(nomeColuna)) {
				relacionamento = rel;
				break;
			}
		}

		return relacionamento;
	}

	public List<BaseRelacionamento> findByBaseInterna(Long baseInternaId) {
		return baseRelacionamentoRepository.findByBaseInterna(baseInternaId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long baseRelacionamentoId) {
		baseRelacionamentoRepository.deleteById(baseRelacionamentoId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(BaseRelacionamento baseRelacionamento) {
		baseRelacionamentoRepository.saveOrUpdate(baseRelacionamento);
	}
}