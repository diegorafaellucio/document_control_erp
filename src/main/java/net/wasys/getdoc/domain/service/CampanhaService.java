package net.wasys.getdoc.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.CampanhaRepository;
import net.wasys.getdoc.domain.vo.CampanhaEquivalenciaVO;
import net.wasys.getdoc.domain.vo.CampanhaTipoDocumentoVO;
import net.wasys.getdoc.domain.vo.ConsultaInscricoesVO;
import net.wasys.getdoc.domain.vo.filtro.CampanhaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CampanhaService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private CampanhaRepository campanhaRepository;
	@Autowired private TipoDocumentoService tipoDocumentoService;

	public Campanha get(Long id) {
		return campanhaRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Campanha campanha, Usuario usuario, List<CampanhaEquivalenciaVO> vos, List<CampanhaTipoDocumentoVO> campanhaTiposDocumentos) throws MessageKeyException {
		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(campanha);

		try {
			//transforma a lista de vo's dos documentos equivalentes em um json apenas com os ids
			Map<Long, List<Long>> equivalenciaIds = new HashMap<>();
			for(CampanhaEquivalenciaVO equivalenciaVO : vos){
				TipoDocumento documentoEquivalente = equivalenciaVO.getDocumentoEquivalente();
				long tipoDocumentoId = documentoEquivalente.getId();

				List<Long> tipoDocumentoIds = new ArrayList<>();
				List<TipoDocumento> tiposDocumentos = equivalenciaVO.getDocumentosEquivalidos();
				for(TipoDocumento tp : tiposDocumentos){
					long tipoDocumentoId2 = tp.getId();
					tipoDocumentoIds.add(tipoDocumentoId2);
				}

				equivalenciaIds.put(tipoDocumentoId, tipoDocumentoIds);
			}
			JSONObject json = new JSONObject(equivalenciaIds);
			campanha.setEquivalencias(json.toString());

			//transforma a lista dos documentos obrigatorios em uma string apenas com os ids
			List<Long> tipoDocumentoObrigatorioIds = new ArrayList<>();
			List<Long> exibeNoPortalIds = new ArrayList<>();
			for(CampanhaTipoDocumentoVO ctp : campanhaTiposDocumentos){
				TipoDocumento tp = ctp.getTipoDocumento();
				long tipoDocumentoId = tp.getId();
				if(ctp.getObrigatorio()) {
					tipoDocumentoObrigatorioIds.add(tipoDocumentoId);
				}
				if(ctp.getExibirNoPortal()){
					exibeNoPortalIds.add(tipoDocumentoId);
				}
			}
			String documentosObrigatoriosIdsStr = StringUtils.join(tipoDocumentoObrigatorioIds, ",");
			campanha.setTipoDocumentoObrigatorioIds(documentosObrigatoriosIdsStr);
			String exibeNoPortalIdsStr = StringUtils.join(exibeNoPortalIds, ",");
			campanha.setExibirNoPortalIds(exibeNoPortalIdsStr);

			Date inicioVigencia = campanha.getInicioVigencia();
			Date fimVigencia = campanha.getFimVigencia();
			if (inicioVigencia != null && fimVigencia != null && inicioVigencia.after(fimVigencia)) {
				throw new MessageKeyException("validaCampanha.periodoInvalido.error");
			} 

			existsConcorrente(campanha);

			campanhaRepository.saveOrUpdate(campanha);

			logAlteracaoService.registrarAlteracao(campanha, usuario, tipoAlteracao);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long id, Usuario usuario) throws MessageKeyException {

		Campanha tipoEvidencia = get(id);
		logAlteracaoService.registrarAlteracao(tipoEvidencia, usuario, TipoAlteracao.EXCLUSAO);

		try {
			campanhaRepository.deleteById(id);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<CampanhaEquivalenciaVO> getEquivalenciasObj(String equivalenciasJson) {
		List<CampanhaEquivalenciaVO> equivalenciaVOs = new ArrayList<>();

		if (equivalenciasJson != null) {
			JSONObject equivalenciasJsonObj = new JSONObject(equivalenciasJson);

			for(String key: equivalenciasJsonObj.keySet()) {
				JSONArray jsonArray = equivalenciasJsonObj.getJSONArray(key);
				long documentoEquivalenteId = Long.parseLong(key);
				TipoDocumento documentoEquivalente = tipoDocumentoService.get(documentoEquivalenteId);

				List<TipoDocumento> documentosEquivalidos = new ArrayList<>();
				for (int i = 0; i < jsonArray.length(); i++) {

					long documentoEquivalidoId = jsonArray.getLong(i);
					TipoDocumento tipoDocumentoEquivalido = tipoDocumentoService.get(documentoEquivalidoId);
					if(tipoDocumentoEquivalido != null) {
						documentosEquivalidos.add(tipoDocumentoEquivalido);
					}
				}

				CampanhaEquivalenciaVO equivalenciaVO = new CampanhaEquivalenciaVO();
				equivalenciaVO.setDocumentoEquivalente(documentoEquivalente);
				equivalenciaVO.setDocumentosEquivalidos(documentosEquivalidos);

				equivalenciaVOs.add(equivalenciaVO);
			}

		}
		return equivalenciaVOs;
	}

	public List<CampanhaTipoDocumentoVO> getTiposDocumento(Campanha campanha, Long tipoProcessoId) {
		List<Long> tiposDocumentoObrigatorio = null;
		List<Long> exibirNoPortal = null;
		List<CampanhaTipoDocumentoVO> campanhaTipoDocumentoVOList = new ArrayList<>();
		String documentosObrigatoriosIds = (campanha==null? null :  campanha.getTipoDocumentoObrigatorioIds());
		String exibirNoPortalIds = (campanha==null? null :  campanha.getExibirNoPortalIds());
		List<TipoDocumento> tiposDocumentos = tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);


		if (documentosObrigatoriosIds != null) {
			tiposDocumentoObrigatorio = new ArrayList<>();
			String[] obrigatoriedadesIdsArr = documentosObrigatoriosIds.split(",");
			for (String idStr : obrigatoriedadesIdsArr) {
				if (!idStr.isEmpty()) {
					long id = new Long(idStr);
					tiposDocumentoObrigatorio.add(id);
				}
			}
		}

		if(exibirNoPortalIds != null){
			exibirNoPortal = new ArrayList<>();
			String[] exibirIdsArr = exibirNoPortalIds.split(",");
			for (String idStr : exibirIdsArr) {
				if (!idStr.isEmpty()) {
					long id = new Long(idStr);
					exibirNoPortal.add(id);
				}
			}
		}

		for (TipoDocumento tipoDocumento : tiposDocumentos) {
			CampanhaTipoDocumentoVO documentoCampanha = new CampanhaTipoDocumentoVO();
			documentoCampanha.setTipoDocumento(tipoDocumento);
			Long tipoDocumentoId = tipoDocumento.getId();

			if(tiposDocumentoObrigatorio == null){
				documentoCampanha.setObrigatorio(tipoDocumento.getObrigatorio());
			}
			else if (tiposDocumentoObrigatorio.contains(tipoDocumentoId)) {
				documentoCampanha.setObrigatorio(true);
			}
			else {
				documentoCampanha.setObrigatorio(false);
			}

			if(exibirNoPortal == null){
				documentoCampanha.setExibirNoPortal(tipoDocumento.getExibirNoPortal());
			}
			else if(exibirNoPortal.contains(tipoDocumentoId)) {
				documentoCampanha.setExibirNoPortal(true);
			}
			else{
				documentoCampanha.setExibirNoPortal(false);
			}

			campanhaTipoDocumentoVOList.add(documentoCampanha);
		}
		return campanhaTipoDocumentoVOList;
	}

	public List<Campanha> findByTipoProcesso(Long tipoProcessoId, Integer inicio, Integer max) {
		return campanhaRepository.findByTipoProcesso(tipoProcessoId, inicio, max);
	}

	public Campanha getByFiltro(CampanhaFiltro filtro) {
		return campanhaRepository.getByFiltro(filtro);
	}

	public Campanha getVigenteByTipoProcesso(Long tipoProcessoId, Date data) {
		return campanhaRepository.getVigenteByTipoProcesso(tipoProcessoId, data);
	}

	public void existsConcorrente(Campanha campanha) {
		List<Campanha> campanhasConcorrentes = campanhaRepository.existsConcorrente(campanha);

		if(campanhasConcorrentes.size() > 0) {
			String strDescricao = "";
			for(Campanha campanhaConcorrente : campanhasConcorrentes){
				strDescricao = strDescricao + " " +campanhaConcorrente.getDescricao();
			}

			throw new MessageKeyException("campanhaConcorrente.error", strDescricao);
		}
	}

	public int countByTipoProcesso(Long tipoProcessoId) {
		return campanhaRepository.countByTipoProcesso(tipoProcessoId);
	}

	public void carregaObrigatoriosAndEquivalencias(Campanha campanha, List<Long> obrigatorios, Map<Long, List<Long>> equivalenciasMap, Map<Long, List<Long>> equivalidosMap) {

		Long campanhaId = campanha.getId();
		campanha = get(campanhaId);

		if(obrigatorios != null) {
			String obrigatoriosId = campanha.getTipoDocumentoObrigatorioIds();
			if (obrigatoriosId != null) {
				String[] obgArray = obrigatoriosId.split(",");
				for(String idStr : obgArray){
					if(!idStr.isEmpty()) {
						long id = new Long(idStr);
						obrigatorios.add(id);
					}
				}
			}
		}

		Map<Long, List<Long>> equivalencias = null;
		if(equivalenciasMap != null || equivalidosMap != null) {
			String equivalenciaJson = campanha.getEquivalencias();
			equivalencias = DummyUtils.jsonToObject(equivalenciaJson,  new TypeReference<Map<Long, List<Long>>>() {});
		}

		if(equivalenciasMap != null) {
			equivalenciasMap.putAll(equivalencias);
		}

		if(equivalidosMap != null) {

			Map<Long, List<Long>> equivalidos = new HashMap<>();
			Set<Long> equivalentes = equivalencias.keySet();

			for (Long tipoDocumentoId : equivalentes) {
				List<Long> equivalidosId = equivalencias.get(tipoDocumentoId);
				for(Long equivalidoId : equivalidosId) {
					List<Long> equivalidos2 = equivalidos.get(equivalidoId);
					equivalidos2 = equivalidos2 != null ? equivalidos2 : new ArrayList<>();
					equivalidosMap.put(equivalidoId, equivalidos2);
					equivalidos2.add(tipoDocumentoId);
				}
			}
		}
	}

	public boolean possuiPadraoByTipoProcessoId(Long tipoProcessoId) {
		return campanhaRepository.possuiPadraoByTipoProcessoId(tipoProcessoId);
	}

	public Campanha getByConsultaLinhaTempoSia(ConsultaInscricoesVO consultaVO, Long tipoProcessoId){
		Date data = new Date();
		String instituicao = consultaVO.getCodInstituicao() != null ? consultaVO.getCodInstituicao().toString() : null;
		String curso = consultaVO.getCodCurso() != null ? consultaVO.getCodCurso().toString() : null;
		String campus = consultaVO.getCodCampus() != null ? consultaVO.getCodCampus().toString() : null;
		return getCampanha(tipoProcessoId, data, instituicao, campus, curso);
	}

	public Campanha getByProcesso(Processo processo){
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Date data = processo.getDataCriacao();
		Date dataEnvioAnalise = processo.getDataEnvioAnalise();
		if (dataEnvioAnalise != null) {
			data = dataEnvioAnalise;
		}
		String instituicaoStr = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.INSTITUICAO);
		String campusStr = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CAMPUS);
		String cursoStr = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.CURSO);

		return getCampanha(tipoProcessoId, data, instituicaoStr, campusStr, cursoStr);
	}

	public Campanha getCampanha(Long tipoProcessoId, Date data, String instituicaoStr, String campusStr, String cursoStr) {
		CampanhaFiltro filtro = new CampanhaFiltro();
		filtro.setTipoProcessoId(tipoProcessoId);
		filtro.setInstituicao(instituicaoStr);
		filtro.setCampus(campusStr);
		filtro.setCurso(cursoStr);
		filtro.setData(data);
		Campanha campanha = getByFiltro(filtro);

		if(campanha == null){
			if(StringUtils.isNotBlank(instituicaoStr) || StringUtils.isNotBlank(campusStr)) {
				filtro = new CampanhaFiltro();
				filtro.setTipoProcessoId(tipoProcessoId);
				filtro.setInstituicao(instituicaoStr);
				filtro.setCampus(campusStr);
				filtro.setData(data);
				campanha = getByFiltro(filtro);

				if(campanha == null) {
					filtro = new CampanhaFiltro();
					filtro.setTipoProcessoId(tipoProcessoId);
					filtro.setInstituicao(instituicaoStr);
					filtro.setData(data);
					campanha = getByFiltro(filtro);

				}
			}
		}

		if(campanha != null) {
			return campanha;
		}else{
			filtro = new CampanhaFiltro();
			filtro.setTipoProcessoId(tipoProcessoId);
			filtro.setData(data);
			filtro.setPadrao(false);
			campanha = getByFiltro(filtro);

			if(campanha == null) {
				filtro = new CampanhaFiltro();
				filtro.setTipoProcessoId(tipoProcessoId);
				filtro.setPadrao(true);
				campanha = getByFiltro(filtro);
			}
			return campanha;
		}
	}
}
