package net.wasys.getdoc.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.entity.FuncaoJs;
import net.wasys.getdoc.domain.repository.FuncaoJsRepository;

@Service
public class FuncaoJsService {

	@Autowired private FuncaoJsRepository funcaoJsRepository;

	public FuncaoJs get(Long id) {
		return funcaoJsRepository.get(id);
	}

	public List<FuncaoJs> findAll() {
		return funcaoJsRepository.findAll();
	}
}