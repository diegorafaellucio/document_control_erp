package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ColunaProcessoSeletivo;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ColunaProcessoSeletivoRepository extends HibernateRepository<ColunaProcessoSeletivo> {

	public ColunaProcessoSeletivoRepository() {
		super(ColunaProcessoSeletivo.class);
	}

	public ColunaProcessoSeletivo getByColunaAndNumeroLinhaAndNomeArquivoImportacao(String coluna, int numeroLinha, String nomeArquivoImportacao) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append("select cps from ").append(clazz.getName()).append(" cps ");
		hql.append(" where cps.nome = ?");
		params.add(coluna);
		hql.append(" and numeroLinha = ?");
		params.add(numeroLinha);
		hql.append(" and nomeArquivoImportacao = ?");
		params.add(nomeArquivoImportacao);

		Query query = createQuery(hql.toString(), params);

		return (ColunaProcessoSeletivo) query.uniqueResult();
	}
}
