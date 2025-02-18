package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.BacalhauImagemPerdida;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoBacalhau;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.vo.filtro.BacalhauFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class BacalhauImagemPerdidaRepository extends HibernateRepository<BacalhauImagemPerdida> {

	public BacalhauImagemPerdidaRepository() {
		super(BacalhauImagemPerdida.class);
	}

	public List<Long> findByImagemIdList(List<Long> imagemIds) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append("select id from ").append(clazz.getName());
		hql.append(" where imagem.id in (:imagemIds))");

		params.put("imagemIds", imagemIds);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public int countByFiltro(BacalhauFiltro filtro) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select count(*) from ").append(clazz.getName()).append(" b where 1=1 ");

		makeWhere(filtro, params, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<BacalhauImagemPerdida> findByFiltro(BacalhauFiltro filtro, Integer first, Integer pageSize) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(clazz.getName()).append(" b where 1=1 ");

		makeWhere(filtro, params, hql);

		String campoOrdem = filtro.getCampoOrdem();
		if(campoOrdem != null) {

			campoOrdem = campoOrdem.replace("log.", "la.");
			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";

			hql.append(" order by ").append(campoOrdem).append(ordemStr);
		}
		else {
			hql.append(" order by data desc ");
		}

		Query query = createQuery(hql.toString(), params);

		if(first != null) {
			query.setFirstResult(first);
		}

		if(pageSize != null) {
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	private void makeWhere(BacalhauFiltro filtro, Map<String, Object> params, StringBuilder hql) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Boolean apenasErros = filtro.getApenasErros();
		boolean recuperadaDoCache = filtro.getRecuperadaDoCache();
		boolean ahRecuperar = filtro.getAhRecuperar();
		TipoExecucaoBacalhau tipoExecucaoBacalhau = filtro.getTipoExecucaoBacalhau();

		if(dataInicio != null && dataFim != null) {
			dataInicio = DateUtils.getFirstTimeOfDay(dataInicio);
			dataFim = DateUtils.getLastTimeOfDay(dataFim);

			hql.append(" and b.data >= :dataInicio ");
			hql.append(" and b.data <= :dataFim ");

			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);
		}

		if(apenasErros){
			hql.append(" and b.erro <> '' ");
		}

		if(recuperadaDoCache) {
			hql.append(" and b.recuperadaDoCache = true");
		}

		if(ahRecuperar) {
			hql.append(" and b.recuperadaDoCache is null");
		}

		if(tipoExecucaoBacalhau != null) {
			hql.append(" and b.tipoExecucao = :tipoExecucao");
			params.put("tipoExecucao", tipoExecucaoBacalhau);
		}
	}

	public Map<String, Integer> verificarStatusBacalhauGeral() {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select count(*) as cheched,");
		hql.append("   (select count(*) from imagem) - (count(*)) as faltam, ");
		hql.append("   (select count(*) from bacalhau_imagem_perdida) as imagens_perdidas, ");
		hql.append("   (select count(*) from bacalhau_imagem_perdida where recuperada_do_cache = true) as imagens_recuperadas, ");
		hql.append("   (select count(*) from bacalhau_imagem_perdida where recuperada_do_cache = false or erro <> '') as imagens_nao_recuperadas ");
		hql.append(" from imagem where id < (");
		hql.append("    select cast(cast(cast(p.valor as json)->:ultimaImagemProcessadaId as text) as int) from parametro p where p.chave = :agendamentoBacalhauGeral)");

		params.put(BacalhauImagemPerdida.ULTIMA_IMAGEM_PROCESSADA_ID, BacalhauImagemPerdida.ULTIMA_IMAGEM_PROCESSADA_ID);
		params.put("agendamentoBacalhauGeral", ParametroService.P.AGENDAMENTO_BACALHAU_GERAL.name());

		SQLQuery sqlQuery = createSQLQuery(hql, params);
		Map<String, Integer> statusMap = new LinkedHashMap<>();

		Object[] result = (Object[]) sqlQuery.uniqueResult();
		if(result != null) {
			BigInteger checked = (BigInteger) result[0];
			BigInteger faltam = (BigInteger) result[1];
			BigInteger imagensPerdidas = (BigInteger) result[2];
			BigInteger imagensRecuperadas = (BigInteger) result[3];
			BigInteger imagensNaoRecuperadas = (BigInteger) result[4];

			statusMap.put("checked", checked.intValue());
			statusMap.put("faltam", faltam.intValue());
			statusMap.put("imagensPerdidas", imagensPerdidas.intValue());
			statusMap.put("imagensRecuperadas", imagensRecuperadas.intValue());
			statusMap.put("imagensNaoRecuperadas", imagensNaoRecuperadas.intValue());
		}

		return statusMap;
	}
}
