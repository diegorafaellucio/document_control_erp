package net.wasys.getdoc.domain.repository;

import java.util.*;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.BloqueioProcesso;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.vo.VerificacaoBloqueioVO;
import net.wasys.util.ddd.HibernateRepository;

/**
 * @author Felipe Maschio
 * @created 26/11/2010
 */
@Repository
public class BloqueioProcessoRepository extends HibernateRepository<BloqueioProcesso> {

	public BloqueioProcessoRepository() {
		super(BloqueioProcesso.class);
	}

	public BloqueioProcesso getLastBoqueioByUsuario(Long usuarioId) {

		Query query = createQuery(getStartQuery() + " where usuarioId = ? order by id desc");
		query.setFirstResult(0);
		query.setMaxResults(1);

		query.setParameter(0, usuarioId);

		return (BloqueioProcesso) query.uniqueResult();
	}

	public BloqueioProcesso getLastBoqueioByProcesso(Long processoId) {

		Query query = createQuery(getStartQuery() + " where processoId = ? order by id desc");
		query.setFirstResult(0);
		query.setMaxResults(1);

		query.setParameter(0, processoId);

		return (BloqueioProcesso) query.uniqueResult();
	}

	public int limparVencidos(Date timeoutDate) {

		Query query = createQuery("delete from " + clazz.getName() + " where data < ?");
		query.setParameter(0, timeoutDate);

		return query.executeUpdate();
	}

	public void deleteByUsuarioId(Long usuarioId) {

		Query query = createQuery("delete from " + clazz.getName() + " where usuarioId = ?");
		query.setParameter(0, usuarioId);

		query.executeUpdate();
	}

	public List<VerificacaoBloqueioVO> findVerificacoesBloqueios(Collection<Long> ids, boolean outher) {

		StringBuilder hql = new StringBuilder();
		HashMap<String, Object> params = new HashMap<>();

		hql.append(" select ");
		hql.append(" 	p.id ");
		if(!outher) {
			hql.append(" 	, b, u.nome");
		}
		hql.append(" from ");
		if(!outher) {
			hql.append(BloqueioProcesso.class.getName()).append(" b, ");
			hql.append(Usuario.class.getName()).append(" u, ");
		}
		hql.append(Processo.class.getName()).append(" p ");
		hql.append(" where 1=1 ");
		if(!outher) {
			hql.append(" 	and p.id = b.processoId ");
			hql.append(" 	and u.id = b.usuarioId ");
		}
		hql.append(" 	and p.id in (:ids) ");

		params.put("ids", ids);

		Query query = createQuery(hql.toString(), params);

		List<?> list = query.list();
		Set<Long> aux = new LinkedHashSet<>();
		List<VerificacaoBloqueioVO> result = new ArrayList<>();

		for (Object object : list) {

			Object[] array = (Object[]) object;
			Long processoId = (Long) array[0];

			if(aux.contains(processoId)) {
				continue;
			}
			aux.add(processoId);

			VerificacaoBloqueioVO vo = new VerificacaoBloqueioVO();
			vo.setProcessoId(processoId);

			if(!outher) {

				BloqueioProcesso bloqueio = (BloqueioProcesso) array[1];
				String usuarioNome = (String) array[2];

				vo.setBloqueio(bloqueio);
				vo.setUsuarioNome(usuarioNome);
			}

			result.add(vo);
		}

		return result;
	}
}
