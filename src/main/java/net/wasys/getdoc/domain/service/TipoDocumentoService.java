package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.TipoDocumentoRepository;
import net.wasys.getdoc.domain.vo.filtro.TipoDocumentoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TipoDocumentoService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private TipoDocumentoRepository tipoDocumentoRepository;
	@Autowired private CampanhaService campanhaService;
	@Autowired private IrregularidadeTipoDocumentoService irregularidadeTipoDocumentoService;

	public TipoDocumento get(Long id) {
		return tipoDocumentoRepository.get(id);
	}

	public int countByFiltro(TipoDocumentoFiltro filtro) {
		return tipoDocumentoRepository.countByFiltro(filtro);
	}

	public List<TipoDocumento> findByFiltro(TipoDocumentoFiltro filtro, Integer inicio, Integer max) {
		return tipoDocumentoRepository.findByFiltro(filtro, inicio, max);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(TipoDocumento tipoDocumento, Usuario usuario, List<ModeloDocumento> modelosDocumentos, List<ModeloDocumento> modelosDocumentosSelecionadosParaExpiracao, List<CategoriaDocumento> categoriasDocumento, List<Irregularidade> irregularidadeSelecionadas, List<GrupoModeloDocumento> gruposModeloDocumentoSelecionados, List<GrupoModeloDocumento> gruposModeloDocumentosSelecionadosParaExpiracao) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(tipoDocumento);

		Integer ordem = tipoDocumento.getOrdem();
		if(ordem == null) {
			TipoProcesso tipoProcesso = tipoDocumento.getTipoProcesso();
			Long tipoProcessoId = tipoProcesso.getId();
			List<TipoDocumento> tiposDocumentos = tipoDocumentoRepository.findByTipoProcesso(tipoProcessoId, null, null);

			ordem = 0;
			for (TipoDocumento td : tiposDocumentos) {
				Integer ordem2 = td.getOrdem();
				ordem = Math.max(ordem, ordem2);
			}
			ordem++;
			tipoDocumento.setOrdem(ordem);
		}

		try {
			tipoDocumentoRepository.saveOrUpdate(tipoDocumento);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		if(modelosDocumentos != null) {
			Long tipoDocumentoId = tipoDocumento.getId();
			List<ModeloDocumento> modelosAtuais = tipoDocumentoId != null ? tipoDocumentoRepository.findModelosDocumentos(tipoDocumentoId) : new ArrayList();
			for (ModeloDocumento md : modelosDocumentos) {
				boolean requisitarDataValidadeExpiracao = (modelosDocumentosSelecionadosParaExpiracao!=null && modelosDocumentosSelecionadosParaExpiracao.remove(md)) ? true : false;
				if(!modelosAtuais.remove(md)) {
					TipoDocumentoModelo tdm = new TipoDocumentoModelo();
					tdm.setTipoDocumento(tipoDocumento);
					tdm.setModeloDocumento(md);
					tdm.setRequisitarDataValidadeExpiracao(requisitarDataValidadeExpiracao);

					tipoDocumentoRepository.saveOrUpdateModeloDocumento(tdm);
				}
				else{
					TipoDocumentoModelo tdm = tipoDocumentoRepository.findByTipoDocumentoAndModeloDocumento(tipoDocumento, md);
					tdm.setRequisitarDataValidadeExpiracao(requisitarDataValidadeExpiracao);

					tipoDocumentoRepository.saveOrUpdateModeloDocumento(tdm);
				}
			}
			for (ModeloDocumento modeloAtual : modelosAtuais) {
				Long tipoDocumentoModeloId = modeloAtual.getId();
				tipoDocumentoRepository.deleteModeloDocumentoById(tipoDocumentoId, tipoDocumentoModeloId);
			}
		}

		if(gruposModeloDocumentoSelecionados != null) {
			Long tipoDocumentoId = tipoDocumento.getId();
			List<GrupoModeloDocumento> modelosAtuais = tipoDocumentoId != null ? tipoDocumentoRepository.findGruposModeloDocumentos(tipoDocumentoId) : new ArrayList();
			for (GrupoModeloDocumento grupoModeloDocumentoAtual : modelosAtuais) {
				Long grupoModeloDocumentoId = grupoModeloDocumentoAtual.getId();
				tipoDocumentoRepository.deleteGrupoModeloDocumentoById(tipoDocumentoId, grupoModeloDocumentoId);
			}
			for (GrupoModeloDocumento gmd : gruposModeloDocumentoSelecionados) {
				TipoDocumentoGrupoModeloDocumento tdgmd = new TipoDocumentoGrupoModeloDocumento();
				tdgmd.setTipoDocumento(tipoDocumento);
				tdgmd.setGrupoModeloDocumento(gmd);
				tipoDocumentoRepository.saveOrUpdateGrupoModeloDocumento(tdgmd);
			}
		}

		if(categoriasDocumento != null) {
			Long tipoDocumentoId = tipoDocumento.getId();
			List<CategoriaDocumento> categoriasAtuais = tipoDocumentoId != null ? tipoDocumentoRepository.findCategoriasDocumento(tipoDocumentoId) : new ArrayList();
			for (CategoriaDocumento cd : categoriasDocumento) {
				if(!categoriasAtuais.remove(cd)) {
					TipoDocumentoCategoria tdc = new TipoDocumentoCategoria();
					tdc.setTipoDocumento(tipoDocumento);
					tdc.setCategoriaDocumento(cd);
					tipoDocumentoRepository.saveOrUpdateCategoriaDocumento(tdc);
				}
			}
			for (CategoriaDocumento categoriaAtual : categoriasAtuais) {
				Long tipoDocumentoModeloId = categoriaAtual.getId();
				tipoDocumentoRepository.deleteCategoriaDocumentoById(tipoDocumentoId, tipoDocumentoModeloId);
			}
		}

		if(irregularidadeSelecionadas != null) {
			Long tipoDocumentoId = tipoDocumento.getId();
			List<Irregularidade> itpAtuaisList = tipoDocumentoId != null ? irregularidadeTipoDocumentoService.findIrregularidadesByTipoDocumentoId(tipoDocumentoId) : new ArrayList();
			for (Irregularidade irregularidade : irregularidadeSelecionadas) {
				if(!itpAtuaisList.remove(irregularidade)) {
					IrregularidadeTipoDocumento irregularidadeTipoDocumento = new IrregularidadeTipoDocumento();
					irregularidadeTipoDocumento.setTipoDocumento(tipoDocumento);
					irregularidadeTipoDocumento.setIrregularidade(irregularidade);
					irregularidadeTipoDocumentoService.saveOrUpdate(irregularidadeTipoDocumento);
					continue;
				}

			}
			for (Irregularidade irregularidade : itpAtuaisList) {
				IrregularidadeTipoDocumento irregularidadeTipoDocumento = irregularidadeTipoDocumentoService.findIrregularidadesByTipoDocumentoIdAndIrregularidade(tipoDocumentoId, irregularidade);
				Long irregularidadeTipoDocumentoId = irregularidadeTipoDocumento.getId();
				irregularidadeTipoDocumentoService.excluir(irregularidadeTipoDocumentoId);
			}
		}

		logAlteracaoService.registrarAlteracao(tipoDocumento, usuario, tipoAlteracao);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long tipoDocumentoId, Usuario usuario) throws MessageKeyException {

		TipoDocumento tipoDocumento = get(tipoDocumentoId);

		logAlteracaoService.registrarAlteracao(tipoDocumento, usuario, TipoAlteracao.EXCLUSAO);

		try {
			tipoDocumentoRepository.deleteById(tipoDocumentoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<TipoDocumento> findByTipoProcesso(Long tipoProcessoId, Integer inicio, Integer max) {
		return tipoDocumentoRepository.findByTipoProcesso(tipoProcessoId, inicio, max);
	}

	public int countByTipoProcesso(Long tipoProcessoId) {
		return tipoDocumentoRepository.countByTipoProcesso(tipoProcessoId);
	}

	public void subirOrdem(TipoDocumento tipoDocumento, Usuario usuario) {

		Integer ordem1 = tipoDocumento.getOrdem();
		TipoProcesso tipoProcesso = tipoDocumento.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		TipoDocumento anterior = tipoDocumentoRepository.getAnterior(tipoProcessoId, ordem1);

		if(anterior == null) {
			return;
		}

		Integer ordem2 = anterior.getOrdem();

		anterior.setOrdem(ordem1);
		tipoDocumento.setOrdem(ordem2);

		tipoDocumentoRepository.saveOrUpdate(anterior);
		tipoDocumentoRepository.saveOrUpdate(tipoDocumento);

		logAlteracaoService.registrarAlteracao(anterior, usuario, TipoAlteracao.ATUALIZACAO);
		logAlteracaoService.registrarAlteracao(tipoDocumento, usuario, TipoAlteracao.ATUALIZACAO);
	}

	public void descerOrdem(TipoDocumento tipoDocumento, Usuario usuario) {

		Integer ordem1 = tipoDocumento.getOrdem();
		TipoProcesso tipoProcesso = tipoDocumento.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		TipoDocumento proximo = tipoDocumentoRepository.getProximo(tipoProcessoId, ordem1);

		if(proximo == null) {
			return;
		}

		Integer ordem2 = proximo.getOrdem();

		proximo.setOrdem(ordem1);
		tipoDocumento.setOrdem(ordem2);

		tipoDocumentoRepository.saveOrUpdate(proximo);
		tipoDocumentoRepository.saveOrUpdate(tipoDocumento);

		logAlteracaoService.registrarAlteracao(proximo, usuario, TipoAlteracao.ATUALIZACAO);
		logAlteracaoService.registrarAlteracao(tipoDocumento, usuario, TipoAlteracao.ATUALIZACAO);
	}

	public boolean temTipificavel(Long tipoProcessoId) {
		return tipoDocumentoRepository.temTipificavel(tipoProcessoId);
	}

	public TipoDocumento getByCodOrigem(Long tipoProcessoId, Long codSia){
		return tipoDocumentoRepository.getByCodOrigem(tipoProcessoId, codSia);
	}

	public Map<Long, Boolean> findObrigatoriedadeByProcessoAndTipoProcessoId(Processo processo, Long tipoProcessoId) {

		Campanha campanha = processo.getCampanha();
		List<Long> obrigatorios = new ArrayList<>();

		if(campanha == null) {
			campanha = campanhaService.getByProcesso(processo);
		}

		List<TipoDocumento> tiposDocumentos = findByTipoProcesso(tipoProcessoId, null, null);
		Map<Long, Boolean> obrigatoriedade = new HashMap<>();
		if (campanha != null) {
			campanhaService.carregaObrigatoriosAndEquivalencias(campanha, obrigatorios, null, null);
			for(TipoDocumento tipoDocumento : tiposDocumentos){
				Long tipoDocumentoId = tipoDocumento.getId();
				if(obrigatorios.contains(tipoDocumentoId)){
					obrigatoriedade.put(tipoDocumentoId, true);
				}else{
					obrigatoriedade.put(tipoDocumentoId, false);
				}
			}
			return obrigatoriedade;
		}
		else{
			for(TipoDocumento tipoDocumento : tiposDocumentos){
				Long tipoDocumentoId = tipoDocumento.getId();
				if(obrigatorios.contains(tipoDocumentoId)){
					obrigatoriedade.put(tipoDocumentoId, true);
				}else{
					obrigatoriedade.put(tipoDocumentoId, false);
				}
			}
			return obrigatoriedade;
		}
	}

	public List<ModeloDocumento> findModelosDocumentos(Long tipoDocumentoId) {
		return tipoDocumentoRepository.findModelosDocumentos(tipoDocumentoId);
	}

	public List<TipoDocumento> findByModeloDocumento(Long modeloDocumentoId) {
		return tipoDocumentoRepository.findByModeloDocumento(modeloDocumentoId);
	}

	public List<TipoDocumento> findByReaproveitavel(Long tipoProcessoId) {
		return tipoDocumentoRepository.findByReaproveitavel(tipoProcessoId);
	}

	public List<Long> findIdsBySuperfilAndProcessoId(Subperfil subperfil, Long processoId) {
		return tipoDocumentoRepository.findIdsBySuperfilAndProcessoId(subperfil, processoId);
	}

	public List<Long> findCodSia(List<Long> tiposDocumentosIds) {

		if(tiposDocumentosIds == null || tiposDocumentosIds.isEmpty()) {
			return new ArrayList<>();
		}

		return tipoDocumentoRepository.findCodSia(tiposDocumentosIds);
	}

	public List<TipoDocumento> findByIds(List<Long> tiposDocumentosIds) {
		return tipoDocumentoRepository.findByIds(tiposDocumentosIds);
	}

	public List<ModeloDocumento> findModelosDocumentoToRequisitarExpiracao(Long tipoDocumentoId) {
		return tipoDocumentoRepository.findModelosDocumentoToRequisitarExpiracao(tipoDocumentoId);
	}

	public List<CategoriaDocumento> findCategoriasDocumento(Long tipoDocumentoId) {
		return tipoDocumentoRepository.findCategoriasDocumento(tipoDocumentoId);
	}

	public List<TipoDocumento> findByCategoriaDocumento(String chaveCategoria) {
		return tipoDocumentoRepository.findByCategoriaDocumento(chaveCategoria);
	}

	public List<Long> findIdsByGrupoRelacionadoIdAndProcessoId(Long grupoRelacionadoId, Long processoId) {
		return tipoDocumentoRepository.findIdsByGrupoRelacionadoIdAndProcessoId(grupoRelacionadoId, processoId);
	}

	public List<GrupoModeloDocumento> findGruposModeloDocumento(Long tipoDocumentoId) {
		return tipoDocumentoRepository.findGruposModeloDocumentos(tipoDocumentoId);
	}

	public List<GrupoModeloDocumento> findGruposModeloDocumentoToRequisitarExpiracao(Long tipoDocumentoId) {
		return tipoDocumentoRepository.findGruposModeloDocumentoToRequisitarExpiracao(tipoDocumentoId);
	}
}
