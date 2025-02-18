package net.wasys.getdoc.domain.enumeration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.util.menu.Item;

public enum Funcionalidade {

	DADOS_ALUNO(
			new P(RoleGD.GD_ADMIN, true, false, false, false)),
	ALUNOS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, true, false)),
	AJUDA_AREA(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, true, false)),
	AJUDA_ANALISTA(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true)),
	ADMIN_AJUDA(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_ANALISTA, true, false, false, false),
			new P(RoleGD.GD_AREA, true, false, false, false),
			new P(RoleGD.GD_COMERCIAL, true, false, false, false)),
	USUARIOS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	SUBPERFIL(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	CONFIGURACOES(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	CUSTOMIZACAO(
			new P(RoleGD.GD_ADMIN, true, true, true, true)),
	EMAILS_POP3(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	LOGS_ALTERACOES(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	FERIADOS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	CALENDARIO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	MODELOS_DOCUMENTO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	CATEGORIAS_DOCUMENTO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true)),
	IRREGULARIDADES(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	BASES_INTERNAS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	MODELOS_OCR(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true)),
	SITUACOES(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	ETAPAS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	TEXTOS_PADROES(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	AREAS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	FILA_CONFIGURACAO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	TIPOS_PROCESSOS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	TIPOS_EVIDENCIAS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	DASHBOARD(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	DASHBOARD_DIARIO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	DASHBOARD_INTRADAY(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	DASHBOARD_MENSAL(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_GERAL(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_ACOMPANHAMENTO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_ACOMPANHAMENTO_EM_ABERTO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_LICENCIAMENTO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_LICENCIAMENTO_OCR(
			new P(RoleGD.GD_ADMIN, true, true, true, true)
	),
	RELATORIO_PRODUTIVIDADE(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_PRODUTIVIDADE_PROUNI(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_ATIVIDADE(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_EFICIENCIA_AREAS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_EXECUCAO_REGRAS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_OCR(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_TIPIFICACAO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	PROCESSOS_FILA(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_ANALISTA, true, true, true, true),
			new P(RoleGD.GD_AREA, true, true, true, false),
			new P(RoleGD.GD_COMERCIAL, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	PROCESSOS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_ANALISTA, true, true, true, true),
			new P(RoleGD.GD_AREA, true, true, true, false),
			new P(RoleGD.GD_COMERCIAL, true, true, true, true),
			new P(RoleGD.GD_CONSULTA, true, false, false, false),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	LOGS_IMPORTACOES(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	REGRAS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	RELATORIO_CONSULTA_EXTERNA(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_INDICIO_FRAUDE(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	CONFIGURACOES_CONSULTA_EXTERNA(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
    CONSULTA_ALUNO_CANDIDATO(
            new P(RoleGD.GD_ADMIN, true, true, true, true),
            new P(RoleGD.GD_GESTOR, true, true, true, true),
            new P(RoleGD.GD_CONSULTA, true, false, true, false),
			new P(RoleGD.GD_PESQUISA, true, false, true, false)),
	RELATORIO_PENDENCIA_DOCUMENTO_ALUNO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	CONSULTA_CANDIDATO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_CONSULTA, false, false, true, false),
			new P(RoleGD.GD_SALA_MATRICULA, true, true, true, true),
			new P(RoleGD.GD_ANALISTA, false, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, true, false)),
	CONSULTA_CANDIDATO_LIST(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_CONSULTA, false, false, true, false),
			new P(RoleGD.GD_ANALISTA, false, true, true, true),
			new P(RoleGD.GD_SALA_MATRICULA, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, true, false)),
	RELATORIO_ISENCAO_DISCIPLINAS(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_ANALISTA, false, true, true, true),
			new P(RoleGD.GD_SALA_MATRICULA, false, false, false, false),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_ISENCAO_DISCIPLINAS_MEDICINA(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_ANALISTA, false, true, true, true),
			new P(RoleGD.GD_SALA_MATRICULA, false, false, false, false),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	IMPORTACAO_PROCESSO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	CONFIGURACAO_LOGIN_AZURE(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true)),
	STATUS_LABORAL(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, true, false, false, false)),
	RELATORIO_STATUS_LABORAL(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_LOGIN_LOG(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	RELATORIO_OPERACAO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	CONFIGURACAO_GERACAO_RELATORIO(
			new P(RoleGD.GD_ADMIN, true, true, true, true)),
	RELATORIO_ANALISE_IA(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true)
	),
	CATEGORIA_GRUPO_MODELO_DOCUMENTO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_CONSULTA, true, false, false, false)),
	GRUPO_MODELO_DOCUMENTO(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_CONSULTA, true, false, false, false)),
	RELATORIO_COMPARACAO_ASSERTIVIDADE(
			new P(RoleGD.GD_ADMIN, true, true, true, true),
			new P(RoleGD.GD_GESTOR, true, true, true, true),
			new P(RoleGD.GD_CONSULTA, true, false, true, false),
			new P(RoleGD.GD_PESQUISA, false, false, false, false)),
	CONFIGURACAO_OCR(
			new P(RoleGD.GD_ADMIN, true, true, true, true)
	)
	;
	//public P(roleGD, podeVisualizar, podeCadastrar, podeEditar, podeExcluir) {

	private Map<RoleGD, P> permissoesMap = new HashMap<>();

	private Funcionalidade(P... permissoes) {

		for (P p : permissoes) {

			RoleGD roleGD = p.getRoleGD();
			permissoesMap.put(roleGD, p);
		}
	}

	public boolean isVisualizavel(Usuario usuario) {
		RoleGD roleGD = usuario.getRoleGD();
		P permissao = permissoesMap.get(roleGD);
		return permissao != null && permissao.getPodeVisualizar(usuario);
	}

	public boolean isCadastravel(Usuario usuario) {
		RoleGD roleGD = usuario.getRoleGD();
		P permissao = permissoesMap.get(roleGD);
		return permissao != null && permissao.getPodeCadastrar(usuario);
	}

	public boolean isEditavel(Usuario usuario) {
		RoleGD roleGD = usuario.getRoleGD();
		P permissao = permissoesMap.get(roleGD);
		return permissao.getPodeEditar(usuario);
	}

	public boolean isExcluivel(Usuario usuario) {
		RoleGD roleGD = usuario.getRoleGD();
		P permissao = permissoesMap.get(roleGD);
		return permissao.getPodeExcluir(usuario);
	}

	/** Permissão */
	private static class P {

		private RoleGD roleGD;
		private boolean podeVisualizar;
		private boolean podeCadastrar;
		private boolean podeEditar;
		private boolean podeExcluir;

		public P(RoleGD roleGD, boolean podeVisualizar, boolean podeCadastrar, boolean podeEditar, boolean podeExcluir) {
			this.roleGD = roleGD;
			this.podeVisualizar = podeVisualizar;
			this.podeCadastrar = podeCadastrar;
			this.podeEditar = podeEditar;
			this.podeExcluir = podeExcluir;
		}

		public RoleGD getRoleGD() {
			return roleGD;
		}

		public boolean getPodeVisualizar(Usuario usuario) {
			return podeVisualizar;
		}

		public boolean getPodeCadastrar(Usuario usuario) {
			return podeCadastrar;
		}

		public boolean getPodeEditar(Usuario usuario) {
			return podeEditar;
		}

		public boolean getPodeExcluir(Usuario usuario) {
			return podeExcluir;
		}
	}

	public List<RoleGD> getRoles() {

		Set<RoleGD> keySet = permissoesMap.keySet();
		ArrayList<RoleGD> list = new ArrayList<>(keySet);
		return list;
	}

	public boolean podeAcessar(Item mi, Usuario usuario) {

		if(!isVisualizavel(usuario)) {
			return false;
		}

		return true;
	}
}
