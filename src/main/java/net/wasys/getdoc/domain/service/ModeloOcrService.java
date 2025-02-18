package net.wasys.getdoc.domain.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.ModeloOcrRepository;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class ModeloOcrService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private ResourceService resourceService;
	@Autowired private ModeloOcrRepository modeloOcrRepository;

	public ModeloOcr get(Long id) {
		return modeloOcrRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ModeloOcr modeloOcr, Usuario usuario, File file) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(modeloOcr);

		modeloOcr.setDataAlteracao(new Date());

		Long id = modeloOcr.getId();
		if(id == null) {
			modeloOcr.setPathModelo("");
		}

		if(file != null) {
			String hashChecksum = DummyUtils.getHashChecksum(file);
			modeloOcr.setHashChecksum(hashChecksum);
		}

		try {
			modeloOcrRepository.saveOrUpdate(modeloOcr);

			logAlteracaoService.registrarAlteracao(modeloOcr, usuario, tipoAlteracao);

			salvaArquivo(modeloOcr, file);

			modeloOcrRepository.saveOrUpdate(modeloOcr);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	private void salvaArquivo(ModeloOcr modeloOcr, File file) {

		if(file == null) {
			return;
		}

		String modelosPath = resourceService.getValue(ResourceService.MODELOS_PATH);
		String separador = System.getProperty("file.separator");

		String caminho = modeloOcr.criaCaminho(modelosPath, separador);

		File newFile = new File(caminho);
		if(newFile.exists()) {
			DummyUtils.deleteFile(newFile);
		}

		try {
			FileUtils.copyFile(file, newFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		modeloOcr.setPathModelo(caminho);
		modeloOcr.setSizeModelo(file.length());
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long id, Usuario usuario) throws MessageKeyException {

		ModeloOcr tipoEvidencia = get(id);
		logAlteracaoService.registrarAlteracao(tipoEvidencia, usuario, TipoAlteracao.EXCLUSAO);

		try {
			modeloOcrRepository.deleteById(id);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	public List<ModeloOcr> findAtivos() {
		return modeloOcrRepository.findAtivos();
	}

	public List<ModeloOcr> findAll(Integer inicio, Integer max) {
		return modeloOcrRepository.findAll(inicio, max);
	}

	public int count() {
		return modeloOcrRepository.count();
	}
}