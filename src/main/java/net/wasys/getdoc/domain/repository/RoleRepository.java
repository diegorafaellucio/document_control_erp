package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Role;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class RoleRepository extends HibernateRepository<Role> {

	public RoleRepository() {
		super(Role.class);
	}

	public void delete(Long roleId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" delete from ").append(clazz.getName());
		hql.append(" where id = ? ");
		params.add(roleId);

		Query query = createQuery(hql.toString(), params);

		query.executeUpdate();
	}
}
