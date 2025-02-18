
package net.wasys.getdoc.domain.repository;

import java.util.*;

import net.wasys.getdoc.domain.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.getdoc.mb.enumerator.DeviceSO;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class UsuarioRepository extends HibernateRepository<Usuario> {

	private final static long TIMEOUT_CACHE = (1000 * 60 * 5);//5 minutos

	public UsuarioRepository() {
		super(Usuario.class);
	}

	public void delDevicePushToken(Long id) {
		StringBuilder hql = new StringBuilder();
		hql	.append("update ").append(clazz.getName()).append(" set ");
		hql	.append(	"deviceSO = null, ");
		hql	.append(	"devicePushToken = null ");
		hql	.append("where id = :id ");
		Query query = createQuery(hql);
		query.setLong("id", id);
		query.executeUpdate();
	}

	public void addDevicePushToken(Long id, DeviceSO deviceSO, String devicePushToken) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();
		hql	.append("update ").append(clazz.getName()).append(" set ");
		hql	.append(	"deviceSO = :deviceSO, ");
		hql	.append(	"devicePushToken = :devicePushToken ");
		hql	.append("where id = :id ");
		params.put("id", id);
		params.put("deviceSO", deviceSO);
		params.put("devicePushToken", devicePushToken);
		Query query = createQuery(hql, params);
		query.executeUpdate();
	}

	public List<Usuario> findByFiltroToSelect(UsuarioFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select new ").append(Usuario.class.getName()).append("(u.id, u.nome, u.login) ");
		hql.append(" from ").append(clazz.getName()).append(" u  ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by u.nome ");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		List<Usuario> list = listCache(params, query, TIMEOUT_CACHE);
		Set<Usuario> set = new LinkedHashSet<>(list);//pra tirar os repetidos
		return new ArrayList<>(set);
	}

	public List<Usuario> findByFiltro(UsuarioFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" u left outer join fetch u.roles rs ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append("order by u.nome");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		List<Usuario> list = query.list();
		Set<Usuario> set = new LinkedHashSet<>(list);//pra tirar os repetidos
		return new ArrayList<Usuario>(set);
	}

	public int countByFiltro(UsuarioFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" u ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	private Map<String, Object> makeQuery(UsuarioFiltro filtro, StringBuilder hql) {

		StatusUsuario status = filtro.getStatus();
		RoleGD role = filtro.getRole();
		Boolean notificarAtrasoRequisicoes = filtro.getNotificarAtrasoRequisicoes();
		Boolean gestorArea = filtro.getGestorArea();
		Area area = filtro.getArea();
		Boolean notificarAtrasoSolicitacoes = filtro.getNotificarAtrasoSolicitacoes();
		String login = filtro.getLogin();
		String nome = filtro.getNome();
		Subperfil subperfil = filtro.getSubperfil();
		Map<String, Object> params = new LinkedHashMap<>();
		Usuario usuario = filtro.getUsuario();
		List<Long> subperfisIds = filtro.getSubperfisIds();
		String emailLike = filtro.getEmailLike();

		hql.append(" where 1=1 ");

		if (role != null) {
			hql.append(" and (select count(*) from Role r where r.usuario.id = u.id and r.nome = :roleName) > 0 ");
			String roleName = role.name();
			params.put("roleName", roleName);
		}
		else {
			hql.append(" and ( ");
			hql.append(" 	select count(*) from Role r where r.usuario.id = u.id and r.nome in ( ''");
			for (RoleGD r : RoleGD.values()) {
				hql.append(", '" + r.name() + "' ");
			}
			hql.append(" )) > 0 ");
		}

		if(StringUtils.isNotBlank(login)) {
			hql.append(" and u.login = :login ");
			params.put("login", login);
		}

		if(StringUtils.isNotBlank(nome)) {
			hql.append(" and lower(u.nome) like :nome ");
			nome = StringUtils.lowerCase(nome);
			params.put("nome", "%" + nome + "%");
		}

		if(status != null) {
			hql.append(" and u.status = :status ");
			params.put("status", status);
		}

		if(subperfil != null) {
			hql.append(" and u.subperfilAtivo = :subperfil ");
			params.put("subperfil", subperfil);
		}

		if(subperfisIds != null && !subperfisIds.isEmpty()){
			hql.append(" and u.subperfilAtivo.id in (:subperfisIds) ");
			params.put("subperfisIds", subperfisIds);
		}

		if(notificarAtrasoRequisicoes != null) {
			hql.append(" and u.notificarAtrasoRequisicoes is :notificarAtrasoRequisicoes ");
			params.put("notificarAtrasoRequisicoes", notificarAtrasoRequisicoes);
		}

		if(notificarAtrasoSolicitacoes != null) {
			hql.append(" and u.notificarAtrasoSolicitacoes is :notificarAtrasoSolicitacoes ");
			params.put("notificarAtrasoSolicitacoes", notificarAtrasoSolicitacoes);
		}

		if(gestorArea != null) {
			hql.append(" and u.gestorArea is :gestorArea ");
			params.put("gestorArea", gestorArea);
		}

		if(area != null) {
			hql.append(" and u.area.id = :areaId ");
			Long areaId = area.getId();
			params.put("areaId", areaId);
		}

		if(StringUtils.isNotBlank(emailLike)) {
			hql.append(" and u.email like :emailLike ");
			params.put("emailLike", "%" + emailLike + "%");
		}

		return params;
	}

	public Usuario autenticar(String login, String senha) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(getStartQuery()).append(" ");
		hql.append("where login = ? ");
		hql.append("and senha = ? ");

		params.add(login);
		params.add(senha);

		Query query = createQuery(hql.toString(), params);

		return (Usuario) query.uniqueResult();
	}

	public Usuario getByLogin(String login) {
		return getByLogin(login, false);
	}

	public Usuario getByLogin(String login, boolean cache) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select u ");
		hql.append(getStartQuery()).append(" u ");
		hql.append(" left outer join fetch u.subperfilAtivo sp ");
		hql.append(" left outer join fetch sp.situacoes ");
		hql.append(" left outer join fetch sp.filaConfiguracao ");
		hql.append(" left outer join fetch u.area a ");
		hql.append(" left outer join fetch u.roles rs ");
		hql.append(" where u.login = :login ");

		params.put("login", login);

		Query query = createQuery(hql.toString(), params);

		if(cache) {
			List<Object> list = listCache(params, query, TIMEOUT_CACHE);
			return list.isEmpty() ? null : (Usuario) list.get(0);
		}
		else {
			return (Usuario) query.uniqueResult();
		}
	}

	public List<Usuario> findByDataAcesso(Date inicio, Date fim, Boolean apenasLocal) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" from ").append(clazz.getName()).append(" u ");
		hql.append(" where u.status = ? ");
		params.add(StatusUsuario.ATIVO);

		if(inicio != null) {
			hql.append(" and u.dataUltimoAcesso >= ? ");
			params.add(inicio);
		}

		if(fim != null) {
			hql.append(" and u.dataUltimoAcesso <= ? ");
			params.add(fim);
		}

		if(apenasLocal != null && apenasLocal) {
			hql.append(" and u.geralId is null ");
		}

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public Date getUltimaDataSincronizacao() {

		StringBuilder hql = new StringBuilder();

		hql.append(" select max(u.dataSincronizacao) from ").append(clazz.getName()).append(" u ");
		hql.append(" where u.dataAtualizacao <= now() ");

		Query query = createQuery(hql.toString());

		return (Date) query.uniqueResult();
	}

	public Usuario getByGeralId(Long geralId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where geralId = ? ");
		params.add(geralId);

		Query query = createQuery(hql.toString(), params);

		return (Usuario) query.uniqueResult();
	}

	public Usuario getByEmail(String email) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select u ");
		hql.append(getStartQuery()).append(" u ");
		hql.append(" left outer join fetch u.area a ");
		hql.append(" left outer join fetch u.roles rs ");
		hql.append(" where u.email = ? ");
		params.add(email);

		Query query = createQuery(hql.toString(), params);

		return (Usuario) query.uniqueResult();
	}

	public void atualizarProcessoAtualId(Long usuarioId, Long processoAtualId, boolean podeTrocarProcessoAtual) {

		StringBuilder hql = new StringBuilder();
		hql.append(" update ").append(clazz.getName()).append(" set ");
		hql.append(" 	processoAtualId = ").append(processoAtualId).append(",");
		hql.append(" 	podeTrocarProcessoAtual = ").append(podeTrocarProcessoAtual);
		hql.append(" where id = ").append(usuarioId);

		Query query = createQuery(hql.toString());
		query.executeUpdate();
	}

	public Map<Long, Date> getDatasUltimosAcessos(Date dataCorte) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select u.geralId, u.dataUltimoAcesso ");
		hql.append(" from ").append(Usuario.class.getName()).append(" u ");

		hql.append(" where u.dataUltimoAcesso >= ? ");
		params.add(dataCorte);

		Query query = createQuery(hql.toString(), params);

		List<Object[]> list = query.list();
		Map<Long, Date> map = new HashMap<>();

		for (Object[] object : list) {

			Long usuarioId = (Long) object[0];
			Date data = (Date) object[1];
			map.put(usuarioId, data);
		}

		return map;
	}

	public boolean atendeSituacao(Long analistaId, Long situacaoId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(Usuario.class.getName()).append(" u ");
		hql.append(" where u.id = ? ");
		params.add(analistaId);
		hql.append(" and ( ");
		hql.append(" 	select count(*) from ").append(SubperfilSituacao.class.getName()).append(" ss where ss.subperfil.id = u.subperfilAtivo.id and ss.situacao.id = ?");
		hql.append(" ) > 0 ");
		params.add(situacaoId);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public String getSenhaPoc() {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		hql.append(" select u.senha from ").append(Usuario.class.getName()).append(" u ");
		hql.append(" where u.login = '").append(Usuario.ADMIN_POC_LOGIN).append("' ");

		Query query = createQuery(hql.toString(), params);

		return (String) query.uniqueResult();
	}

	public RoleGD getRoleGD(Usuario usuario) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select r.nome from ").append(Role.class.getName()).append(" r ");
		hql.append(" where r.usuario.id = :usuarioId ");
		hql.append(" and r.nome in (:rolesGD) ");

		Long usuarioId = usuario.getId();
		params.put("usuarioId", usuarioId);
		List<String> rolesGD = new ArrayList<>();
		RoleGD[] values = RoleGD.values();
		for (RoleGD role : values) {
			rolesGD.add(role.name());
		}
		params.put("rolesGD", rolesGD);

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);
		String roleStr = (String) query.uniqueResult();
		return RoleGD.valueOf(roleStr);
	}

	public void atualizarDataUltimaNotificacaoSemDemanda(Long usuarioId, Date date) {

		StringBuilder hql = new StringBuilder();
		hql.append(" update ").append(clazz.getName()).append(" set ");
		hql.append(" 	dataUltimaNotificacaoSemDemanda = :data ");
		hql.append(" where id = :usuarioId ");

		Query query = createQuery(hql.toString());
		query.setTimestamp("data", date);
		query.setLong("usuarioId", usuarioId);
		query.executeUpdate();
	}
}
