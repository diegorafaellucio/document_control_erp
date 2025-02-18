package net.wasys.getdoc.domain.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.util.DummyUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class RelatorioPendenciaDocumentoRepository extends HibernateRepository<Documento> {

    public RelatorioPendenciaDocumentoRepository() {
        super(Documento.class);
    }

    public List<Object[]> findRelatorio(RelatorioPendenciaDocumentoFiltro filtro) {
        Map<String, Object> params = new HashMap<>();

        StringBuilder hql = RelatorioPendenciaDocumentoRepositoryCustom.createHqlRelatorio(params, filtro);
        Query query = createQuery(hql.toString(), params);
        query.setTimeout(GetdocConstants.QUERY_TIMEOUT);

        return query.list();
    }

    public List<Object[]> findRelatorioSisFiesSisProuni(RelatorioPendenciaDocumentoFiltro filtro) {
        Map<String, Object> params = new HashMap<>();

        StringBuilder hql = RelatorioPendenciaDocumentoRepositoryCustom.createHqlRelatorioSisFiesSisProuni(params, filtro);
        Query query = createQuery(hql.toString(), params);
        query.setTimeout(GetdocConstants.QUERY_TIMEOUT);

        return query.list();
    }
}
