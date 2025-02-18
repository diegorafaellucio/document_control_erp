package net.wasys.getdoc.domain.service;

import java.io.File;
import java.io.IOException;
import java.util.*;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoImportacao;
import net.wasys.util.DummyUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.BaseInternaRepository;
import net.wasys.getdoc.domain.vo.ResultVO;
import net.wasys.getdoc.domain.vo.filtro.BaseInternaFiltro;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class BaseInternaService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private ResourceService resourceService;
	@Autowired private LogImportacaoService logImportacaoService;
	@Autowired private BaseRelacionamentoService baseRelacionamentoService;
	@Autowired private BaseInternaRepository baseInternaRepository;

	public BaseInterna get(Long id) {
		return baseInternaRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(BaseInterna baseInterna, List<BaseRelacionamento> relacionamentosSelecionados, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(baseInterna);
		baseInterna.setDataAlteracao(new Date());

		if(relacionamentosSelecionados != null) {
			Set<BaseRelacionamento> relacionamentos = baseInterna.getRelacionamentos();
			for (BaseRelacionamento relacionamento : new ArrayList<>(relacionamentos)) {
				if(!relacionamentosSelecionados.contains(relacionamento)) {
					Long baseRelacionamentoId = relacionamento.getId();
					baseRelacionamentoService.excluir(baseRelacionamentoId);
					relacionamentos.remove(relacionamento);
				}
			}
			List<BaseRelacionamento> relacionamentosNovos = new ArrayList<>(relacionamentosSelecionados);
			for (BaseRelacionamento relacionamento : relacionamentosSelecionados) {
				for (BaseRelacionamento relacionamento2 : relacionamentos) {
					if(relacionamento.equals(relacionamento2)) {
						BaseInterna baseExtrangeira = relacionamento.getBaseExtrangeira();
						String chaveExtrangeira = relacionamento.getChaveExtrangeira();
						relacionamento2.setBaseExtrangeira(baseExtrangeira);
						relacionamento2.setChaveExtrangeira(chaveExtrangeira);
						baseRelacionamentoService.saveOrUpdate(relacionamento2);
						relacionamentosNovos.remove(relacionamento);
						break;
					}
				}
			}
			for (BaseRelacionamento relacionamentoNovo : relacionamentosNovos) {
				baseRelacionamentoService.saveOrUpdate(relacionamentoNovo);
			}
		}

		try {
			baseInternaRepository.saveOrUpdate(baseInterna);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(baseInterna, usuario, tipoAlteracao);
	}

	@Transactional(rollbackFor=Exception.class)
	public void finalizarImportacao(BaseInterna baseInterna, Usuario usuario, ResultVO resultVO, File file, String fileName) throws MessageKeyException {

		LogImportacao log = logImportacaoService.criarLogImportacao(TipoImportacao.BASE_INTERNA, usuario, resultVO, file, null, null);
		log.setBaseInterna(baseInterna);
		log.setNomeArquivo(fileName.length() > 100 ? fileName.substring(0, 99) + "~" : fileName);
		log.setTamanhoArquivo(file.length());

		String path = log.criarPath(resourceService.getValue(ResourceService.HISTORICO_BASES_PATH), fileName);

		salvarArquivo(file, path);

		log.setPathArquivo(path);
		logImportacaoService.saveOrUpdate(log);
	}

	public void salvarArquivo(File file, String path) {

		if(file == null) {
			return;
		}

		File newFile = new File(path);
		if(newFile.exists()) {
			DummyUtils.deleteFile(newFile);
		}

		try {
			FileUtils.copyFile(file, newFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long id, Usuario usuario) throws MessageKeyException {

		BaseInterna baseInterna = get(id);
		logAlteracaoService.registrarAlteracao(baseInterna, usuario, TipoAlteracao.EXCLUSAO);

		try {
			baseInternaRepository.deleteById(id);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	public int countByFiltro(BaseInternaFiltro filtro) {
		return baseInternaRepository.countByFiltro(filtro);
	}

	public List<BaseInterna> findByFiltro(BaseInternaFiltro filtro, Integer first, Integer pageSize) {
		return baseInternaRepository.findByFiltro(filtro, first, pageSize);
	}

	public List<BaseInterna> findAtivos() {
		return baseInternaRepository.findAtivos();
	}

	public Set<String> findCamposById(Long baseInternaId) {
		return baseInternaRepository.findCamposById(baseInternaId);
	}
}