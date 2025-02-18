package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Parametro;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class BiRepository extends HibernateRepository<Parametro> {

    public BiRepository() {
        super(Parametro.class);
    }

    @Autowired
	public void setSessionFactory(@Qualifier("sfBi") SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
}
