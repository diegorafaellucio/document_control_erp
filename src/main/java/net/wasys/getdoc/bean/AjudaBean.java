package net.wasys.getdoc.bean;

import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.enumeration.Resposta;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.entity.Ajuda;
import net.wasys.getdoc.domain.entity.Ajuda.Objetivo;
import net.wasys.getdoc.domain.entity.Ajuda.Tipo;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.AjudaService;
import net.wasys.getdoc.domain.service.TipoDocumentoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class AjudaBean extends AbstractBean {

	@Autowired private AjudaService ajudaService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;

	private Long tipoProcessoId;
	private Long tipoDocumentoId;
	private Long id;

	private Ajuda ajuda;
	private List<TipoProcesso> tiposProcessos;
	private TipoDocumento tipoDocumento;

	private Ajuda raiz = null;
	private TreeNode root;
	private TreeNode singleSelectedTreeNode;

	private boolean forcado = false;
	private Resposta respostaNovoFilho = Resposta.OK;

	protected void initBean() {

		if (id != null) {
			ajuda = ajudaService.get(id);
			raiz = ajuda;
			TipoProcesso tipoProcesso = ajuda.getTipoProcesso();
			tipoProcessoId = tipoProcesso.getId();
			criar(ajuda);
		}

		Usuario usuario = getUsuarioLogado();
		RoleGD role = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(role);
		tiposProcessos = tipoProcessoService.findAtivos(permissoes);
	}

	protected void criar(Ajuda raiz) {
		root = null;
		if (raiz != null) {
			root = new DefaultTreeNode();
			root.setExpanded(true);
			criar(root, raiz);
		}
	}

	private void criar(TreeNode node, Ajuda ajuda) {

		TreeNode child = addNode(node, ajuda);

		Long ajudaId = ajuda.getId();
		if(ajudaId == null) {
			return;
		}

		List<Ajuda> inferiores = ajuda.getInferiores();
		Ajuda infOk = getEmptyNode(ajuda, Resposta.OK);
		Ajuda infNao = getEmptyNode(ajuda, Resposta.NAO);
		Ajuda infSim = getEmptyNode(ajuda, Resposta.SIM);
		for (Ajuda inf : inferiores) {
			Resposta resposta = inf.getResposta();
			if(Resposta.OK.equals(resposta)) {
				infOk = inf;
			} else if(Resposta.NAO.equals(resposta)) {
				infNao = inf;
			} else if(Resposta.SIM.equals(resposta)) {
				infSim = inf;
			}
		}
		Tipo tipo = ajuda.getTipo();
		if(Tipo.PERGUNTA.equals(tipo)) {
			criar(child, infNao);
			criar(child, infSim);
		} else {
			criar(child, infOk);
		}
	}

	private Ajuda getEmptyNode(Ajuda ajuda, Resposta resposta) {
		Ajuda nova = new Ajuda();
		nova.setSuperior(ajuda);
		nova.setResposta(resposta);
		return nova;
	}

	private TreeNode addNode(TreeNode parentNode, Ajuda ajuda) {
		TreeNode node = new DefaultTreeNode(ajuda, parentNode);
		node.setExpanded(true);
		return node;
	}

	public TreeNode getRoot() {
		return root;
	}

	public TreeNode getSingleSelectedTreeNode() {
		return singleSelectedTreeNode;
	}

	public void setSingleSelectedTreeNode(TreeNode singleSelectedTreeNode) {
		this.singleSelectedTreeNode = singleSelectedTreeNode;
	}

	public void onNodeSelect(NodeSelectEvent event) {
		List<TreeNode> brothers = event.getTreeNode().getParent().getChildren();
		for ( TreeNode sister : brothers ) {
			sister.setPartialSelected(true);
		}
	}

	public void trocaTipoDocumento() {

		raiz.setTipoDocumento(tipoDocumento);
		ajudaService.salvar(ajuda, null);

		setRequestAttribute("fecharModal", true);
		addMessage("alteracaoSalva.sucesso");
	}

	public void salvar() {
		try {
			TipoDocumento tipoDocumento = ajuda.getTipoDocumento();
			Long tipoDocumentoId = tipoDocumento != null ? tipoDocumento.getId() : null;
			if (new Long(0).equals(tipoDocumentoId)) {
				ajuda.setTipoDocumento(null);
			}

			//prevent if it wasn't selected Resposta for new Pergunta
			boolean ok = true;
			for (Ajuda inferior : ajuda.getInferiores()) {
				Resposta resposta = inferior.getResposta();
				if (Resposta.OK.equals(resposta)) {
					ok = false;
				}
			}

			Tipo tipo = ajuda.getTipo();
			if (Tipo.PERGUNTA.equals(tipo) && Resposta.OK.equals(respostaNovoFilho) && !ok) {
				//FIXME vai cair aqui em algum momento? Se sim, tem que colocar uma mensagem de erro
				return;
			}

			boolean insert = isInsert(ajuda);
			ajudaService.salvar(ajuda, respostaNovoFilho);
			id = id != null ? id : ajuda.getId();
			respostaNovoFilho = Resposta.OK;
			forcado = false;

			Ajuda superior = ajuda.getSuperior();
			if(insert && superior == null) {
				redirect("/cadastros/ckecklists/edit/" + id);
			}

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
			carregar();
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void iniciar() {
		ajuda = new Ajuda();
		ajuda.setTipo(Tipo.PERGUNTA);
		ajuda.setObjetivo(getObjetivo());
		ajuda.setResposta(Resposta.OK);
		ajuda.setTipoProcesso(new TipoProcesso(tipoProcessoId));
		if (tipoDocumentoId != null) {
			ajuda.setTipoDocumento(new TipoDocumento(tipoDocumentoId));
		}
	}

	public void editar(Long id) {
		ajuda = ajudaService.get(id);
		forcado = false;
	}

	public void excluir() {
		try {
			if (!raiz.equals(ajuda)) {
				ajudaService.excluir(ajuda);
				carregar();
			}
		} catch (Exception e) {
			addMessageError(e);
		}
	}

	public void adicionar(Long id, Resposta resposta) {
		ajuda = new Ajuda();
		ajuda.setObjetivo(getObjetivo());
		ajuda.setResposta(resposta);
		ajuda.setTipoProcesso(new TipoProcesso(tipoProcessoId));

		Ajuda superior = ajudaService.get(id);
		if ( superior.getTipo() == Tipo.PERGUNTA && resposta == Resposta.OK ) {
			ajuda.setTipo(Tipo.PERGUNTA);
			forcado = true;
		}
		else {
			ajuda.setTipo(Tipo.DICA);
			forcado = false;
		}
		ajuda.setSuperior(superior);
	}

	public void carregar() {
		if (id != null) {
			raiz = ajudaService.get(id);
			tipoProcessoId = raiz.getTipoProcesso().getId();
		}
		criar(raiz);
	}

	public boolean podeExcluir(Ajuda ajuda) {

		List<Ajuda> inferiores = ajuda.getInferiores();
		if(inferiores.isEmpty()) {
			return true;
		}

		Ajuda superior = ajuda.getSuperior();
		if(superior != null) {
			Tipo tipoSuperior = superior.getTipo();
			if(Tipo.DICA.equals(tipoSuperior)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeTrocarTipo() {

		if(getForcado()) {
			return false;
		}

		if(ajuda == null) {
			return true;
		}

		Tipo tipo = ajuda.getTipo();
		if(Tipo.DICA.equals(tipo)) {
			return true;
		}
		else {
			List<Ajuda> inferiores = ajuda.getInferiores();
			return inferiores.isEmpty() || inferiores.size() == 1;
		}
	}

	public boolean vaiReposicionarFilho() {

		if(ajuda == null) {
			return false;
		}

		Tipo tipo = ajuda.getTipo();
		if(Tipo.PERGUNTA.equals(tipo) && podeTrocarTipo()) {
			List<Ajuda> inferiores = ajuda.getInferiores();
			for (Ajuda inf : inferiores) {
				Resposta resposta = inf.getResposta();
				if(Resposta.OK.equals(resposta)) {
					return true;
				}
			}
		}

		return false;
	}

	public Objetivo getObjetivo() {

		//TODO
		return Objetivo.REQUISICAO;
	}

	public Ajuda getAjuda() {
		return ajuda;
	}

	public void setAjuda(Ajuda ajuda) {
		this.ajuda = ajuda;
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		if (tipoProcessoId != null && tipoProcessoId < 1) {
			tipoProcessoId = null;
		}
		this.tipoProcessoId = tipoProcessoId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<TipoDocumento> getTiposDocumentos() {
		if (tipoProcessoId == null) {
			return new ArrayList<>();
		}
		return tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);
	}

	public Long getTipoDocumentoId() {
		return tipoDocumentoId;
	}

	public void setTipoDocumentoId(Long tipoDocumentoId) {
		this.tipoDocumentoId = tipoDocumentoId;
	}

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public boolean getForcado() {
		return forcado;
	}

	public void setForcado(boolean forcado) {
		this.forcado = forcado;
	}

	public Resposta getRespostaNovoFilho() {
		return respostaNovoFilho;
	}

	public void setRespostaNovoFilho(Resposta respostaNovoFilho) {
		this.respostaNovoFilho = respostaNovoFilho;
	}

	public Ajuda getRaiz() {
		return raiz;
	}
}