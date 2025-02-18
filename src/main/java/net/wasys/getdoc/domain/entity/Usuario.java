package net.wasys.getdoc.domain.entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import lombok.extern.slf4j.Slf4j;
import net.wasys.util.other.Criptografia;
import org.apache.commons.lang.StringUtils;

import net.wasys.getdoc.domain.enumeration.MotivoBloqueioUsuario;
import net.wasys.getdoc.domain.enumeration.MotivoDesativacaoUsuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.mb.enumerator.DeviceSO;
import net.wasys.util.DummyUtils;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames="EMAIL"))
public class Usuario extends net.wasys.util.ddd.Entity implements HttpSessionBindingListener {

	public static final String ADMIN_POC_LOGIN = "admin_poc";
	public static final String PORTAL_GRADUCAO = "portalgraduacaovh";
	public static final String PORTAL_POS_GRADUACAO = "portalposgraduacao";
	public static final String PORTAL_CVC = "portal.cvc";
	public static Long CLIENTE_ID = 1L;

	public static final String LOGIN_ADMIN = "admin";

	private static ConcurrentHashMap<Long, ConcurrentLinkedQueue<HttpSession>> registroSessao = new ConcurrentHashMap<>();

	private Long id;
	private Long geralId;
	private String nome;
	private String login;
	private String senha;
	private String email;
	private String telefone;
	private StatusUsuario status;
	private Date dataExpiracaoSenha;
	private String senhasAnteriores;
	private Date dataCadastro;
	private Date dataUltimoAcesso;
	private MotivoBloqueioUsuario motivoBloqueio;
	private Date dataBloqueio;
	private Date dataExpiracaoBloqueio;
	private MotivoDesativacaoUsuario motivoDesativacao;
	private boolean gestorArea;
	private Date dataAtualizacao;
	private boolean ordemAtividadesFixa = true;
	private Long processoAtualId;
	private boolean podeTrocarProcessoAtual;
	private boolean podeExcluirImagem = false;
	private boolean notificarAtrasoRequisicoes = true;
	private boolean notificarAtrasoSolicitacoes = true;
	private Date dataSincronizacao;
	private Date dataExpiracao;
	private Date dataProcessoAtual;
	private boolean distribuirDemandaAutomaticamente = false;
	private Date dataUltimaNotificacaoSemDemanda;
	private StatusLaboral statusLaboral;
	private StatusLaboral statusLaboralProgramado;
	private Boolean programouStatusLaboral = false;
	private String oidAzure;
	private Date dataAlteracao = new Date();
	private boolean podeRemoverObrigatoriedade = false;

	private Area area;
	private Subperfil subperfilAtivo;

	// Mobile
	private DeviceSO deviceSO;
	private String devicePushToken;

	private Set<Role> roles = new HashSet<>(0);
	private Set<UsuarioTipoProcesso> tiposProcessos = new HashSet<>(0);
	private Set<UsuarioSubperfil> subperfils = new HashSet<>(0);
	private Set<UsuarioCampus> campus = new HashSet<>(0);
	private Set<UsuarioRegional> regionais = new HashSet<>(0);

	private Runnable logoffListener;

	public Usuario() {}

	public Usuario(Long id, String nome, String login) {
		this.id = id;
		this.nome = nome;
		this.login = login;
	}

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ATUALIZACAO", nullable=false)
	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	@Column(name="NOME", nullable=false, length=100)
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="SENHA", nullable=false, length=20)
	public String getSenha() {
		return this.senha;
	}

	public void setSenha(String senha) {
		this.senha = Criptografia.encryptIfNot(Criptografia.SENHA, senha);
	}

	@Column(name="LOGIN", length=20)
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = StringUtils.lowerCase(login);
	}

	@Column(name="EMAIL", length=100)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name="TELEFONE", length=15)
	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS", nullable=false)
	public StatusUsuario getStatus() {
		return status;
	}

	public void setStatus(StatusUsuario status) {
		this.status = status;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="MOTIVO_BLOQUEIO")
	public MotivoBloqueioUsuario getMotivoBloqueio() {
		return motivoBloqueio;
	}

	public void setMotivoBloqueio(MotivoBloqueioUsuario motivoBloqueio) {
		this.motivoBloqueio = motivoBloqueio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_EXPIRACAO_SENHA")
	public Date getDataExpiracaoSenha() {
		return dataExpiracaoSenha;
	}

	public void setDataExpiracaoSenha(Date dataExpiracaoSenha) {
		this.dataExpiracaoSenha = dataExpiracaoSenha;
	}

	@Column(name="SENHAS_ANTERIORES")
	public String getSenhasAnteriores() {
		return senhasAnteriores;
	}

	public void setSenhasAnteriores(String senhasAnteriores) {
		this.senhasAnteriores = senhasAnteriores;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_CADASTRO")
	public Date getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_BLOQUEIO")
	public Date getDataBloqueio() {
		return dataBloqueio;
	}

	public void setDataBloqueio(Date dataBloqueio) {
		this.dataBloqueio = dataBloqueio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ULTIMO_ACESSO")
	public Date getDataUltimoAcesso() {
		return dataUltimoAcesso;
	}

	public void setDataUltimoAcesso(Date dataUltimoAcesso) {
		this.dataUltimoAcesso = dataUltimoAcesso;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_EXPIRACAO_BLOQUEIO")
	public Date getDataExpiracaoBloqueio() {
		return dataExpiracaoBloqueio;
	}

	public void setDataExpiracaoBloqueio(Date dataExpiracaoBloqueio) {
		this.dataExpiracaoBloqueio = dataExpiracaoBloqueio;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="MOTIVO_DESATIVACAO")
	public MotivoDesativacaoUsuario getMotivoDesativacao() {
		return motivoDesativacao;
	}

	public void setMotivoDesativacao(MotivoDesativacaoUsuario motivoDesativacao) {
		this.motivoDesativacao = motivoDesativacao;
	}

	@Column(name="GESTOR_AREA")
	public boolean getGestorArea() {
		return gestorArea;
	}

	public void setGestorArea(boolean gestorArea) {
		this.gestorArea = gestorArea;
	}

	@Column(name="ORDEM_ATIVIDADES_FIXA")
	public boolean getOrdemAtividadesFixa() {
		return ordemAtividadesFixa;
	}

	public void setOrdemAtividadesFixa(boolean ordemAtividadesFixa) {
		this.ordemAtividadesFixa = ordemAtividadesFixa;
	}

	@Column(name="PROCESSO_ATUAL_ID")
	public Long getProcessoAtualId() {
		return processoAtualId;
	}

	public void setProcessoAtualId(Long processoAtualId) {
		this.processoAtualId = processoAtualId;
	}

	@Column(name="PODE_TROCAR_PROCESSO_ATUAL")
	public boolean getPodeTrocarProcessoAtual() {
		return podeTrocarProcessoAtual;
	}

	public void setPodeTrocarProcessoAtual(boolean podeTrocarProcessoAtual) {
		this.podeTrocarProcessoAtual = podeTrocarProcessoAtual;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_PROCESSO_ATUAL")
	public Date getDataProcessoAtual() {
		return dataProcessoAtual;
	}

	public void setDataProcessoAtual(Date dataProcessoAtual) {
		this.dataProcessoAtual = dataProcessoAtual;
	}

	@Column(name="DISTRIBUIR_DEMANDA_AUTOMATICAMENTE")
	public boolean getDistribuirDemandaAutomaticamente() {
		return distribuirDemandaAutomaticamente;
	}

	public void setDistribuirDemandaAutomaticamente(boolean distribuirDemandaAutomaticamente) {
		this.distribuirDemandaAutomaticamente = distribuirDemandaAutomaticamente;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ULTIMA_NOTIFICACAO_SEM_DEMANDA")
	public Date getDataUltimaNotificacaoSemDemanda() {
		return dataUltimaNotificacaoSemDemanda;
	}

	public void setDataUltimaNotificacaoSemDemanda(Date dataUltimaNotificacaoSemDemanda) {
		this.dataUltimaNotificacaoSemDemanda = dataUltimaNotificacaoSemDemanda;
	}

	@Column(name="NOTIFICAR_ATRASO_REQUISICOES")
	public boolean getNotificarAtrasoRequisicoes() {
		return notificarAtrasoRequisicoes;
	}

	public void setNotificarAtrasoRequisicoes(boolean notificarAtrasoRequisicoes) {
		this.notificarAtrasoRequisicoes = notificarAtrasoRequisicoes;
	}

	@Column(name="PODE_EXCLUIR_IMAGEM")
	public boolean isPodeExcluirImagem() {
		return podeExcluirImagem;
	}

	public void setPodeExcluirImagem(boolean podeExcluirImagem) {
		this.podeExcluirImagem = podeExcluirImagem;
	}

	@Column(name="NOTIFICAR_ATRASO_SOLICITACOES")
	public boolean getNotificarAtrasoSolicitacoes() {
		return notificarAtrasoSolicitacoes;
	}

	public void setNotificarAtrasoSolicitacoes(boolean notificarAtrasoSolicitacoes) {
		this.notificarAtrasoSolicitacoes = notificarAtrasoSolicitacoes;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_SINCRONIZACAO")
	public Date getDataSincronizacao() {
		return dataSincronizacao;
	}

	public void setDataSincronizacao(Date dataSincronizacao) {
		this.dataSincronizacao = dataSincronizacao;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="AREA_ID")
	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBPERFIL_ATIVO_ID")
	public Subperfil getSubperfilAtivo() {
		return subperfilAtivo;
	}

	public void setSubperfilAtivo(Subperfil subperfilAtivo) {
		this.subperfilAtivo = subperfilAtivo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="DEVICE_SO", length=7)
	public DeviceSO getDeviceSO() {
		return deviceSO;
	}

	@Column(name="DEVICE_PUSH_TOKEN", length=255)
	public String getDevicePushToken() {
		return devicePushToken;
	}

	public void setDeviceSO(DeviceSO deviceSO) {
		this.deviceSO = deviceSO;
	}

	public void setDevicePushToken(String devicePushToken) {
		this.devicePushToken = devicePushToken;
	}

	@Column(name="GERAL_ID")
	public Long getGeralId() {
		return geralId;
	}

	public void setGeralId(Long geralId) {
		this.geralId = geralId;
	}

	@Column(name="DATA_EXPIRACAO")
	public Date getDataExpiracao() {
		return dataExpiracao;
	}

	public void setDataExpiracao(Date dataExpiracao) {
		this.dataExpiracao = dataExpiracao;
	}

	@Column(name="OID_AZURE")
	public String getOidAzure() {
		return oidAzure;
	}

	public void setOidAzure(String oidAzure) {
		this.oidAzure = oidAzure;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="usuario", cascade=CascadeType.ALL)
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="usuario", cascade=CascadeType.ALL)
	public Set<UsuarioTipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(Set<UsuarioTipoProcesso> tiposProcessos) {
		this.tiposProcessos = tiposProcessos;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="usuario", cascade=CascadeType.ALL)
	public Set<UsuarioSubperfil> getSubperfils() {
		return subperfils;
	}

	public void setSubperfils(Set<UsuarioSubperfil> subperfils) {
		this.subperfils = subperfils;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="usuario", cascade=CascadeType.ALL)
	public Set<UsuarioCampus> getCampus() {
		return campus;
	}

	public void setCampus(Set<UsuarioCampus> campus) {
		this.campus = campus;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="usuario", cascade=CascadeType.ALL)
	public Set<UsuarioRegional> getRegionais() {
		return regionais;
	}

	public void setRegionais(Set<UsuarioRegional> regionais) {
		this.regionais = regionais;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="STATUS_LABORAL_ID")
	public StatusLaboral getStatusLaboral() {
		return statusLaboral;
	}

	public void setStatusLaboral(StatusLaboral statusLaboral) {
		this.statusLaboral = statusLaboral;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="STATUS_ATENDIMENTO_PROGRAMADA_ID")
	public StatusLaboral getStatusLaboralProgramado() {
		return statusLaboralProgramado;
	}

	public void setStatusLaboralProgramado(StatusLaboral statusLaboralProgramado) {
		this.statusLaboralProgramado = statusLaboralProgramado;
	}

	@Column(name = "PROGRAMOU_STATUS_ATENDIMENTO")
	public Boolean getProgramouStatusLaboral() {
		return programouStatusLaboral;
	}

	public void setProgramouStatusLaboral(Boolean programouStatusLaboral) {
		this.programouStatusLaboral = programouStatusLaboral;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_ALTERACAO")
	public Date getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(Date dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	@Transient
	public void setRoleGD(RoleGD roleGD) {
		setRole(RoleGD.class, roleGD);
	}

	@Transient
	private <T> void setRole(Class<T> class1, T roleEnum) {

		Role role = getRole(class1);
		if(roleEnum == null) {
			Set<Role> roles = getRoles();
			roles.remove(role);
		}
		else {
			if(role == null) {
				role = new Role();
				role.setUsuario(this);
				Set<Role> roles = getRoles();
				roles.add(role);
			}
			role.setNome(String.valueOf(roleEnum));
		}
	}

	@Transient
	private Role getRole(Class<?> clazz) {

		Set<Role> roles = getRoles();
		for (Role role : roles) {

			String nome = role.getNome();
			Object enumValue = DummyUtils.getEnumValue(clazz.getSimpleName(), nome);
			if(enumValue != null) {
				return role;
			}
		}

		return null;
	}

	@Transient
	public RoleGD getRoleGD() {
		return getRoleEnum(RoleGD.class);
	}

	@Transient
	@SuppressWarnings("unchecked")
	private <T> T getRoleEnum(Class<T> clazz) {

		Set<Role> roles = getRoles();
		Map<String, Role> map = new HashMap<>();
		for (Role role : roles) {
			map.put(role.getNome(), role);
		}

		List<Enum> enuns = (List<Enum>) Arrays.asList(clazz.getEnumConstants());
		for (Enum enun : enuns) {
			Role role = map.get(enun.name());
			if(role != null) {
				return (T) enun;
			}
		}

		return null;
	}

	@Override
	@Transient
	public void valueBound(HttpSessionBindingEvent event) {

		HttpSession session = event.getSession();

		RoleGD roleGD = getRoleGD();
		ConcurrentLinkedQueue<HttpSession> sessionsList = registroSessao.get(id);
		sessionsList = sessionsList != null ? sessionsList : new ConcurrentLinkedQueue<>();
		registroSessao.put(id, sessionsList);
		if(sessionsList != null && !RoleGD.GD_API.equals(roleGD)) {
			for (HttpSession session2 : sessionsList) {
				if(!session2.equals(session)) {
					session2.invalidate();
				}
			}
		}
		sessionsList.add(session);

		ServletContext servletContext = session.getServletContext();
		synchronized (servletContext) {

			Map<String, Integer> count = getCountMap(servletContext);

			String nomePerfil = roleGD.name();
			Subperfil subperfil = getSubperfilAtivo();
			if(subperfil != null) {
				nomePerfil += " - " + subperfil.getDescricao();
			}

			Integer countAtual = count.get(nomePerfil);
			countAtual = countAtual != null ? countAtual : 0;
			countAtual = countAtual + 1;
			count.put(nomePerfil, countAtual);
		}
	}

	@Override
	@Transient
	public void valueUnbound(HttpSessionBindingEvent event) {

		Long id = getId();
		HttpSession session = event.getSession();
		ConcurrentLinkedQueue<HttpSession> sessionList = registroSessao.get(id);
		if(sessionList != null) {
			sessionList.remove(session);
		}

		if(logoffListener != null) {
			try {
				logoffListener.run();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}

		ServletContext servletContext = event.getSession().getServletContext();
		synchronized (servletContext) {

			Map<String, Integer> count = getCountMap(servletContext);

			RoleGD roleGD = getRoleGD();
			String nomePerfil = roleGD.name();
			Subperfil subperfil = getSubperfilAtivo();
			if(subperfil != null) {
				nomePerfil += " - " + subperfil.getDescricao();
			}

			Integer countAtual = count.get(nomePerfil);
			countAtual = countAtual != null ? countAtual : 0;
			countAtual = countAtual - 1;
			count.put(nomePerfil, countAtual);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Integer> getCountMap(ServletContext servletContext) {

		Map<String, Integer> count = (Map<String, Integer>) servletContext.getAttribute("usuariosLogadosCount");

		if(count == null) {
			count = new HashMap<String, Integer>();
			servletContext.setAttribute("usuariosLogadosCount", count);
		}

		return count;
	}

	public void setLogoffListener(Runnable logoffListener) {
		this.logoffListener = logoffListener;
	}

	@Transient
	public boolean isAdminRole() {
		RoleGD roleGD = getRoleGD();
		return RoleGD.GD_ADMIN.equals(roleGD);
	}

	@Transient
	public boolean isGestorRole() {
		RoleGD roleGD = getRoleGD();
		return RoleGD.GD_GESTOR.equals(roleGD);
	}

	@Transient
	public boolean isAnalistaRole() {
		RoleGD roleGD = getRoleGD();
		return RoleGD.GD_ANALISTA.equals(roleGD);
	}

	@Transient
	public boolean isComercialRole() {
		RoleGD roleGD = getRoleGD();
		return RoleGD.GD_COMERCIAL.equals(roleGD);
	}

	@Transient
	public boolean isAreaRole() {
		RoleGD roleGD = getRoleGD();
		return RoleGD.GD_AREA.equals(roleGD);
	}

	@Transient
	public boolean isRequisitanteRole() {
		RoleGD roleGD = getRoleGD();
		boolean isRequisitante = RoleGD.GD_COMERCIAL.equals(roleGD);
		return isRequisitante;
	}

	@Transient
	public boolean isSalaMatriculaRole() {
		RoleGD roleGD = getRoleGD();
		boolean isSalaMatrica = RoleGD.GD_SALA_MATRICULA.equals(roleGD);
		return isSalaMatrica;
	}
	@Column(name="PODE_REMOVER_OBRIGATORIEDADE")
	public boolean isPodeRemoverObrigatoriedade() {
		return podeRemoverObrigatoriedade;
	}

	public void setPodeRemoverObrigatoriedade(boolean podeRemoverObrigatoriedade) {
		this.podeRemoverObrigatoriedade = podeRemoverObrigatoriedade;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getId() + "{nome:" + getNome() + "}";
	}
}