package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.UsuarioRegional;
import net.wasys.util.ddd.HibernateRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRegionalRepository extends HibernateRepository<UsuarioRegional> {

	public UsuarioRegionalRepository() {
		super(UsuarioRegional.class);
	}

}
