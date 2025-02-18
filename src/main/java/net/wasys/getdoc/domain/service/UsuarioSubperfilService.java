package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.UsuarioSubperfil;
import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.repository.UsuarioSubperfilRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class UsuarioSubperfilService {

	@Autowired private UsuarioSubperfilRepository usuarioSubperfilRepository;

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long uspId) {
		usuarioSubperfilRepository.deleteById(uspId);
	}

	public List<UsuarioSubperfil> orderByNivel(Collection<UsuarioSubperfil> usps) {

		List<UsuarioSubperfil> uspsList = new ArrayList<>(usps);
		Collections.sort(uspsList, (o1, o2) -> {
			Integer nivel1 = o1.getNivel();
			Integer nivel2 = o2.getNivel();
			nivel1 = nivel1 != null ? nivel1 : 0;
			nivel2 = nivel2 != null ? nivel2 : 0;
			int compareTo = DummyUtils.compareTo(nivel2, nivel1, false);
			if(compareTo == 0) {
				Subperfil s1 = o1.getSubperfil();
				Subperfil s2 = o2.getSubperfil();
				String descricao1 = s1.getDescricao();
				String descricao2 = s2.getDescricao();
				compareTo = descricao1.compareTo(descricao2);
			}
			return compareTo;
		});

		return uspsList;
	}

	public List<UsuarioSubperfil> findByAnalista(Long analistaId) {
		List<UsuarioSubperfil> list = usuarioSubperfilRepository.findByAnalista(analistaId);
		return orderByNivel(list);
	}
}
