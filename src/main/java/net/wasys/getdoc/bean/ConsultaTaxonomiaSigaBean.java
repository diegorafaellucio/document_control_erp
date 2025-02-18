package net.wasys.getdoc.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.*;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.domain.vo.CampoLineVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.RowTreeVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.TaxonomiaFiltro;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class ConsultaTaxonomiaSigaBean extends AbstractBean {

	@Autowired private DocumentoService documentoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private BaseInternaService baseInternaService;
    @Autowired private AlunoService alunoService;

    private static final int UM = 1;
    private Long id;
	private TaxonomiaFiltro filtro = new TaxonomiaFiltro();
	private CampoGrupo grupo = new CampoGrupo();
	private Processo processo;
    private Aluno aluno;
	private TreeNode root = new DefaultTreeNode(null);
	private DefaultTreeNode treeSubClasses = new DefaultTreeNode();
	private List<Documento> documentoList = new ArrayList<Documento>();
	private List<Aluno> alunos = new ArrayList<Aluno>();
	private Documento documentoSelecionado;
	private boolean expandir;
	private boolean hierarquiaFinalizada = false;
	List<RegistroValorVO> baseRegistroList = new ArrayList<RegistroValorVO>();
	private Map<String, Integer> mapQtd = new LinkedHashMap<String, Integer>();

	protected void initBean() {

        if(id != null) {
            buscarTaxonomiaAluno();
        }
	}

    private void buscarTaxonomiaAluno() {
        Aluno aluno =   alunoService.get(id);
        filtro.setCpfCnpj(aluno.getCpf());
        buscarInformacoesTaxonomia();
    }

    public void buscar() {

        if(StringUtils.isBlank(filtro.getCpfCnpj()) && StringUtils.isBlank(filtro.getMatricula()) && StringUtils.isBlank(filtro.getNome())){
            addMessageError("erroPesquisaDocumentosAlunoTaxonimoa.error.selecionar");
            return;
        }else if(StringUtils.isBlank(filtro.getCpfCnpj()) && StringUtils.isNotBlank(filtro.getNome())){
            alunos = alunoService.findAlunoPorNomeComProcessoByFiltro(filtro);
            if(alunos != null && alunos.size() == UM){
            	String cpf = alunos.get(0).getCpf();
            	alunos.clear();
            	this.filtro.setCpfCnpj(cpf);
				buscarInformacoesTaxonomia();
			}
        } else{
            buscarInformacoesTaxonomia();
        }
	}

	private void buscarInformacoesTaxonomia(){
        criarBaseRegistroTaxonomia();
        criarListaDeDocumentos();
        if(documentoList.isEmpty()) {
            return;
        }
        criarArvoreTaxonomia();
    }

	private void criarListaDeDocumentos() {

		root = new DefaultTreeNode(null);

		documentoList = documentoService.findDocumentosByAlunoFiltro(filtro);
		if(documentoList.isEmpty()){
			addMessageError("erroPesquisaDocumentosAlunoTaxonimoa.error");
		}

		processo = !documentoList.isEmpty() ? documentoList.get(0).getProcesso() : null;

		if(processo != null && processo.getAluno() != null && processo.getAluno().getId() != null){
            Long idAluno = processo.getAluno().getId();
            aluno = alunoService.get(idAluno);
        }
	}

	private void criarBaseRegistroTaxonomia() {

		BaseInterna baseInterna = baseInternaService.get(BaseInterna.TAXONOMIA);
		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setBaseInterna(baseInterna);
		baseRegistroList = baseRegistroService.findByFiltro(filtro, null, null);
	}

	private int criarClassesFilha(RegistroValorVO vo, DefaultTreeNode defaultTreeNode) {
		int qtdDocumentos = 0;
		do {
			qtdDocumentos += criarHierarquiaDaArvore(vo, defaultTreeNode);
		}
		while (!hierarquiaFinalizada) ;

		return qtdDocumentos;
	}

	private int criarHierarquiaDaArvore(RegistroValorVO vo, DefaultTreeNode defaultTreeNode) {

		int qtdDocumentos = 0;

		String cod = vo.getValor("cod");
		for (RegistroValorVO registroValorVO : baseRegistroList) {
			String codPai = registroValorVO.getValor("cod_pai");
			if (!StringUtil.isBlank(codPai) && codPai.equals(cod)) {

				BaseRegistro baseRegistro = registroValorVO.getBaseRegistro();
				List<Documento> documentos = buscarDocumentos(baseRegistro);
				int size = documentos.size();

				String label = registroValorVO.getLabel();
				RowTreeVO rowTreeVO = new RowTreeVO();
				rowTreeVO.setClasse(label);
				rowTreeVO.setQtdDocumentos(size > 0 ? size : null);
				DefaultTreeNode treeNode = new DefaultTreeNode(rowTreeVO, defaultTreeNode);
				treeNode.setExpanded(expandir);

				criarLinhaComDocumentos(documentos, treeNode);
				int i = criarClassesFilha(registroValorVO, treeNode);
				qtdDocumentos += size + i;
			}
		}
		hierarquiaFinalizada = true;
		return qtdDocumentos;
	}

	private void criarArvoreTaxonomia() {

		for (RegistroValorVO vo : baseRegistroList) {
			String codPai = vo.getValor("cod_pai");
			if(StringUtil.isBlank(codPai)) {

				String label = vo.getLabel();
				RowTreeVO rowTreeVO = new RowTreeVO();
				rowTreeVO.setClasse(label);
				treeSubClasses = new DefaultTreeNode(rowTreeVO, root);
				treeSubClasses.setExpanded(expandir);

				BaseRegistro baseRegistro = vo.getBaseRegistro();
				List<Documento> documentos = buscarDocumentos(baseRegistro);
				criarLinhaComDocumentos(documentos, treeSubClasses);

				int qtdDocumentos = criarClassesFilha(vo, treeSubClasses);
				if(qtdDocumentos > 0) {
					mapQtd.put(label, qtdDocumentos);
				}
			}
		}
	}

	private void criarLinhaComDocumentos(List<Documento> documentos, DefaultTreeNode defaultTreeNode) {

		for(Documento documento : documentos) {
			Date dataDigitalizacao = documento.getDataDigitalizacao();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dataDigitalizacao != null ? dataDigitalizacao : new Date());
			calendar.add(Calendar.YEAR, +3);
			RowTreeVO rowMainDoc = new RowTreeVO();
			rowMainDoc.setDocumento(documento);
			rowMainDoc.setDataValidade(calendar.getTime());
			criarLinhaNaArvore(rowMainDoc, defaultTreeNode).setExpanded(expandir);
		}
	}

	private List<Documento> buscarDocumentos(BaseRegistro baseRegistro) {

		List<Documento> list = new ArrayList<>();
		for (Documento documento : documentoList) {
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			BaseRegistro brDocumento = tipoDocumento.getBaseRegistro();
			if (brDocumento != null && baseRegistro.equals(brDocumento)) {
				list.add(documento);
			}
		}
		return list;
	}

	private DefaultTreeNode criarLinhaNaArvore(RowTreeVO rowTreeVO, DefaultTreeNode defaultTreeNode ) {
		return new DefaultTreeNode(rowTreeVO, defaultTreeNode);
	}

	public List<CampoLineVO> getCampos() {

		List<CampoLineVO> list = new ArrayList<>();
		if(processo == null) {
			return list;
		}

		List<Campo> camposList =  new ArrayList<>();
		Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
		for (CampoGrupo gruposCampo : gruposCampos) {
			String nome = gruposCampo.getNome();
			if(CampoMap.CampoEnum.CANDIDATO_CPF.getGrupo().getNome().equals(nome)) {
				Set<Campo> campos = gruposCampo.getCampos();
				camposList = new ArrayList<>(campos);
			}
		}

		for (Campo campo : camposList) {
			TipoEntradaCampo tipo = campo.getTipo();
			if(TipoEntradaCampo.TEXTO_LONGO.equals(tipo) || TipoEntradaCampo.COMBO_BOX_MULTI.equals(tipo)) {
				CampoLineVO vo = new CampoLineVO();
				vo.setColunaUnica(true);
				vo.setCampo1(campo);
				list.add(vo);
			}
			else {
				int lastIdx = list.size() - 1;
				CampoLineVO vo = lastIdx >= 0 ? list.get(lastIdx) : null;
				if(vo == null) {
					vo = new CampoLineVO();
					list.add(vo);
				}
				boolean colunaUnica = vo.isColunaUnica();
				Campo c1 = vo.getCampo1();
				Campo c2 = vo.getCampo2();
				if(!colunaUnica && c1 == null)
					vo.setCampo1(campo);
				else if(!colunaUnica && c2 == null)
					vo.setCampo2(campo);
				else {
					vo = new CampoLineVO();
					vo.setCampo1(campo);
					list.add(vo);
				}
			}
		}
		return list;
	}

	public TreeNode getRoot() {
		return root;
	}

	public TaxonomiaFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(TaxonomiaFiltro filtro) {
		this.filtro = filtro;
	}

	public CampoGrupo getGrupo() {
		return grupo;
	}

	public Processo getProcesso() {
		return processo;
	}

	public Documento getDocumentoSelecionado() {
		return documentoSelecionado;
	}

	public void setDocumentoSelecionado(Documento documentoSelecionado) {
		this.documentoSelecionado = documentoSelecionado;
	}

	public String getUrlVisualizacaoDocumentoSelecionado() {

		if(documentoSelecionado == null) {
			return "xxx";
		}

		Processo processo = documentoSelecionado.getProcesso();
		Long processoId = processo.getId();
		Long documentoId = documentoSelecionado.getId();
		return "/processos/processo-visualizar.xhtml?id=" + processoId + "&documentoId=" + documentoId;
	}

	public boolean isExpandir() {
		return expandir;
	}

	public void setExpandir(boolean expandir) {
		this.expandir = expandir;
		buscar();
	}

	public Integer getMapQtd(String classe) {
		return mapQtd.get(classe);
	}

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public List<Aluno> getAlunos() {
        return alunos;
    }

    public void setAlunos(List<Aluno> alunos) {
        this.alunos = alunos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}