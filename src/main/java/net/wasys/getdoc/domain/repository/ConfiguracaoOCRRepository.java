package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ConfiguracaoOCR;
import net.wasys.getdoc.domain.entity.ConfiguracaoOCRInstituicao;
import net.wasys.getdoc.domain.entity.SubRegra;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class ConfiguracaoOCRRepository extends HibernateRepository<ConfiguracaoOCR> {

	public ConfiguracaoOCRRepository() {
		super(ConfiguracaoOCR.class);
	}

	public List<ConfiguracaoOCR> findAll() {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" from ").append(clazz.getName()).append(" co ");
		hql.append(" order by co.nome ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public boolean existsByNome(String nome) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" co ");
		hql.append(" where co.nome = ? ");
		params.add(nome);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public int countAll() {
		Query query = createQuery(" select count(*) from " + clazz.getName());
		return query.list().size();
	}

    public boolean isFluxoAprovacaoTipificacaoAtivo(Long processoId, String instituicao) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" co, ");
		hql.append(ConfiguracaoOCRInstituicao.class.getName()).append(" coi ");
		hql.append(" where co.tipoProcesso.id = ? ");
		hql.append(" and co.id = coi.configuracaoOCR.id ");
		hql.append(" and coi.codigoInstituicao = ? ");
		hql.append(" and co.tipificacaoAtiva is true ");
		params.add(processoId);
		params.add(Long.parseLong(instituicao));

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
    }

	public boolean isFluxoAprovacaoIAAtivo(Long processoId, String instituicao) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" co, ");
		hql.append(ConfiguracaoOCRInstituicao.class.getName()).append(" coi ");
		hql.append(" where co.tipoProcesso.id = ? ");
		hql.append(" and co.id = coi.configuracaoOCR.id ");
		hql.append(" and coi.codigoInstituicao = ? ");
		hql.append(" and co.analiseAtiva is true ");
		params.add(processoId);
		params.add(Long.parseLong(instituicao));

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}

	public boolean isFluxoAprovacaoOCRAtivo(Long processoId, String instituicao) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" co, ");
		hql.append(ConfiguracaoOCRInstituicao.class.getName()).append(" coi ");
		hql.append(" where co.tipoProcesso.id = ? ");
		hql.append(" and co.id = coi.configuracaoOCR.id ");
		hql.append(" and coi.codigoInstituicao = ? ");
		hql.append(" and co.ocrAtivo is true ");
		params.add(processoId);
		params.add(Long.parseLong(instituicao));

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}

	public boolean isFluxoAprovacaoDocumentos(Long processoId, String instituicao) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" co, ");
		hql.append(ConfiguracaoOCRInstituicao.class.getName()).append(" coi ");
		hql.append(" where co.tipoProcesso.id = ? ");
		hql.append(" and co.id = coi.configuracaoOCR.id ");
		hql.append(" and coi.codigoInstituicao = ? ");
		hql.append(" and co.aprovacaoAtiva is true ");
		params.add(processoId);
		params.add(Long.parseLong(instituicao));

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}
}
