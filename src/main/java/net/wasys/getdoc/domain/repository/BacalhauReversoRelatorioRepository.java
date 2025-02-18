package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.BacalhauReversoRelatorio;
import net.wasys.getdoc.domain.service.BacalhauReversoRelatorioService;
import net.wasys.util.ddd.HibernateRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BacalhauReversoRelatorioRepository extends HibernateRepository<BacalhauReversoRelatorio> {

    public BacalhauReversoRelatorioRepository() {
        super(BacalhauReversoRelatorio.class);
    }
}
