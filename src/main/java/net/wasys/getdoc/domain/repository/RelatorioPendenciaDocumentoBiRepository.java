package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class RelatorioPendenciaDocumentoBiRepository extends HibernateRepository<Documento> {


    @Autowired
    public void setSessionFactory(@Qualifier("sfBi") SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public RelatorioPendenciaDocumentoBiRepository() {
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
