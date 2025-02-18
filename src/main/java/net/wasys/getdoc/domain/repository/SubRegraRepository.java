package net.wasys.getdoc.domain.repository;

import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.SubRegra;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class SubRegraRepository extends HibernateRepository<SubRegra> {

	public SubRegraRepository() {
		super(SubRegra.class);
	}
}
