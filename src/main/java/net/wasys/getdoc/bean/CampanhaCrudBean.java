package net.wasys.getdoc.bean;

import com.fasterxml.jackson.core.type.TypeReference;
import net.wasys.getdoc.bean.datamodel.CampanhaDataModel;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.CampanhaEquivalenciaVO;
import net.wasys.getdoc.domain.vo.CampanhaTipoDocumentoVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.CampanhaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@ViewScoped
public class CampanhaCrudBean extends AbstractBean {

	@Autowired private CampanhaService campanhaService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private BaseInternaService baseInternaService;

	private CampanhaDataModel dataModel;
	private Long tipoProcessoId;
	private TipoProcesso tipoProcesso;
	private Campanha campanha;
	private List<RegistroValorVO> instituicoes;
	private List<RegistroValorVO> campus;
	private List<RegistroValorVO> cursos;
	private List<String> instituicoesSelect;
	private List<String> campusSelect;
	private List<String> cursosSelect;
	private List<CampanhaEquivalenciaVO> equivalenciaVOs;
	private List<CampanhaTipoDocumentoVO> tiposDocumentos;
	private boolean possuiPadrao;

	public void initBean() {

		if(tipoProcesso == null) {
			if(tipoProcessoId == null) {
				redirect("/cadastros/tipos-processos/");
				return;
			}
			this.tipoProcesso = tipoProcessoService.get(tipoProcessoId);
			if(tipoProcesso == null) {
				redirect("/cadastros/tipos-processos/");
				return;
			}
		}
		campanha = new Campanha();

		dataModel = new CampanhaDataModel();
		dataModel.setService(campanhaService);
		dataModel.setTipoProcessoId(tipoProcessoId);

		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		BaseInterna baseInterna = baseInternaService.get(BaseInterna.INSTITUICAO_ID);
		filtro.setBaseInterna(baseInterna);
		filtro.setAtivo(true);
		instituicoes = baseRegistroService.findByFiltro(filtro, null, null);
		Long tipoProcessoId = tipoProcesso.getId();
		possuiPadrao = campanhaService.possuiPadraoByTipoProcessoId(tipoProcessoId);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(campanha);
			Usuario usuario = getUsuarioLogado();
			campanha.setInstituicoes(DummyUtils.listToJson(instituicoesSelect));
			campanha.setCampus(DummyUtils.listToJson(campusSelect));
			campanha.setCursos(DummyUtils.listToJson(cursosSelect));

			campanhaService.saveOrUpdate(campanha, usuario, equivalenciaVOs, tiposDocumentos);

			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
			possuiPadrao = campanhaService.possuiPadraoByTipoProcessoId(tipoProcessoId);
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long campanhaId = campanha.getId();

		try {
			campanhaService.excluir(campanhaId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void adicionarEquivalencia(){
		CampanhaEquivalenciaVO equivalenciaVO = new CampanhaEquivalenciaVO();
		this.equivalenciaVOs.add(equivalenciaVO);
	}

	public void removerEquivalencia(CampanhaEquivalenciaVO equivalenciaVo) {
		this.equivalenciaVOs.remove(equivalenciaVo);
	}

	public CampanhaDataModel getDataModel() {
		return dataModel;
	}

	public Campanha getCampanha() {
		return campanha;
	}

	public void setCampanha(Campanha campanha) {
		if(campanha == null) {
			campanha = new Campanha();
			CampanhaFiltro filtro = new CampanhaFiltro();
			filtro.setPadrao(true);
			filtro.setTipoProcessoId(tipoProcessoId);
			Campanha campanha2 = campanhaService.getByFiltro(filtro);
			this.tiposDocumentos = campanhaService.getTiposDocumento(campanha2, tipoProcessoId);
			campanha.setTipoProcesso(tipoProcesso);
			instituicoesSelect = new ArrayList<>();
			campusSelect = new ArrayList<>();
			cursosSelect = new ArrayList<>();
			equivalenciaVOs = new ArrayList<>();
		}
		else {
			Long campanhaId = campanha.getId();
			campanha = campanhaService.get(campanhaId);
			String instituicao = campanha.getInstituicoes();
			String campus = campanha.getCampus();
			String curso = campanha.getCursos();

			this.tiposDocumentos = campanhaService.getTiposDocumento(campanha, tipoProcessoId);

			String equivalenciasJson = campanha.getEquivalencias();
			this.equivalenciaVOs = campanhaService.getEquivalenciasObj(equivalenciasJson);

			this.instituicoesSelect = DummyUtils.jsonToList(instituicao, new TypeReference<List<String>>() {});
			this.campusSelect = DummyUtils.jsonToList(campus, new TypeReference<List<String>>() {});
			this.cursosSelect = DummyUtils.jsonToList(curso,  new TypeReference<List<String>>() {});
		}

		this.campanha = campanha;
	}

	public List<CampanhaTipoDocumentoVO> getTiposDocumentos() {
		return tiposDocumentos;
	}

	public void setTiposDocumentos(List<CampanhaTipoDocumentoVO> tiposDocumentos) {
		this.tiposDocumentos = tiposDocumentos;
	}

	public List<CampanhaEquivalenciaVO> getEquivalenciaVOs() {
		return equivalenciaVOs;
	}

	public void setEquivalenciaVOs(List<CampanhaEquivalenciaVO> equivalenciaVOs) {
		this.equivalenciaVOs = equivalenciaVOs;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}


	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public List<RegistroValorVO> getInstituicoes() {
		return instituicoes;
	}

	public void setInstituicoes(List<RegistroValorVO> instituicoes) {
		this.instituicoes = instituicoes;
	}

	public List<RegistroValorVO> getCampus() {
		if(instituicoesSelect != null){
			campus = baseRegistroService.findByRelacionados(BaseInterna.CAMPUS_ID, instituicoesSelect, TipoCampo.COD_INSTITUICAO);
		}
		return campus;
	}

	public void setCampus(List<RegistroValorVO> campus) {
		this.campus = campus;
	}

	public List<RegistroValorVO> getCursos() {
		if(CollectionUtils.isNotEmpty(campusSelect) && CollectionUtils.isEmpty(cursos)){
			cursos = baseRegistroService.findByBaseInterna(BaseInterna.CURSO_ID);
		}
		return cursos;
	}

	public void setCursos(List<RegistroValorVO> cursos) {
		this.cursos = cursos;
	}

	public List<String> getInstituicoesSelect() {
		return instituicoesSelect;
	}

	public void setInstituicoesSelect(List<String> instituicoesSelect) {
		this.instituicoesSelect = instituicoesSelect;
	}

	public List<String> getCampusSelect() {
		return campusSelect;
	}

	public void setCampusSelect(List<String> campusSelect) {
		this.campusSelect = campusSelect;
	}

	public List<String> getCursosSelect() {
		return cursosSelect;
	}

	public void setCursosSelect(List<String> cursosSelect) {
		this.cursosSelect = cursosSelect;
	}

	public boolean getPossuiPadrao() {
		return possuiPadrao;
	}

	public void limpaPadrao(boolean padrao){
		if(padrao && campanha != null){
			campanha.setInicioVigencia(null);
			campanha.setFimVigencia(null);
			campanha.setInstituicoes(null);
			campanha.setCampus(null);
			campanha.setCursos(null);
		}
	}

	public void setExibir(CampanhaTipoDocumentoVO vo){
		Boolean exibirNoPortal = vo.getExibirNoPortal();
		vo.setExibirNoPortal(!exibirNoPortal);
	}
}
