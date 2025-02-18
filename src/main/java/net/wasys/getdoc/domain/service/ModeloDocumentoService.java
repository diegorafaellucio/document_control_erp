package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.ModeloDocumentoRepository;
import net.wasys.getdoc.domain.vo.ModeloDocumentoVO;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ModeloDocumentoService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private ModeloDocumentoRepository modeloDocumentoRepository;

	public ModeloDocumento get(Long id) {
		return modeloDocumentoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ModeloDocumento modeloDocumento, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(modeloDocumento);

		modeloDocumento.setDataAlteracao(new Date());
		try {
			modeloDocumentoRepository.saveOrUpdate(modeloDocumento);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(modeloDocumento, usuario, tipoAlteracao);
	}

	public List<ModeloDocumento> findAll(Integer inicio, Integer max) {
		return modeloDocumentoRepository.findAll(inicio, max);
	}

	public int count() {
		return modeloDocumentoRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long modeloDocumentoId, Usuario usuario) throws MessageKeyException {

		ModeloDocumento feriado = get(modeloDocumentoId);

		logAlteracaoService.registrarAlteracao(feriado, usuario, TipoAlteracao.EXCLUSAO);

		try {
			modeloDocumentoRepository.deleteById(modeloDocumentoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<ModeloDocumento> findAtivos() {
		return modeloDocumentoRepository.findAtivos();
	}

	public List<ModeloDocumento> findByLabelDarknet(String labelDarknet) {
		return modeloDocumentoRepository.findByLabelDarknet(labelDarknet);
	}

	public ModeloDocumentoVO createVOBy(ModeloDocumento modeloDocumento) {


		ModeloDocumentoVO vo = new ModeloDocumentoVO();
		vo.setId(modeloDocumento.getId());
		vo.setDescricao(modeloDocumento.getDescricao());
		vo.setPalavrasEsperadas(modeloDocumento.getPalavrasEsperadas());
		vo.setPalavrasExcludentes(modeloDocumento.getPalavrasExcludentes());
		vo.setAtivo(modeloDocumento.getAtivo());
		vo.setDataAlteracao(modeloDocumento.getDataAlteracao());
		vo.setPercentualMininoTipificacao(modeloDocumento.getPercentualMininoTipificacao());
		vo.setDarknetApiHabilitada(modeloDocumento.getDarknetApiHabilitada());
		vo.setVisionApiHabilitada(modeloDocumento.getVisionApiHabilitada());
		vo.setLabelDarknet(modeloDocumento.getLabelDarknet());



		return vo;
	}

	public List<ModeloDocumentoVO> findVOsByProcesso() {

		List<ModeloDocumento> modeloDocumentos = modeloDocumentoRepository.findAtivos();

		List<ModeloDocumentoVO> list = new ArrayList<>();
		for (ModeloDocumento modeloDocumento : modeloDocumentos) {

			ModeloDocumentoVO vo = createVOBy(modeloDocumento);
			list.add(vo);

		}

		return list;
	}
}
