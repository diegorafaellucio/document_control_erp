package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.bean.UtilBean;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.repository.ProcessoLogAnexoRepository;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.filtro.AnexoFiltro;
import net.wasys.util.DummyUtils;

@Service
public class ProcessoLogAnexoService {

	@Autowired private ProcessoLogAnexoRepository processoLogAnexoRepository;
	@Autowired private ResourceService resourceService;
	@Autowired private ParametroService parametroService;

	@Transactional(rollbackFor=Exception.class)
	public void criar(ProcessoLog log, FileVO fileVO) {

		String name = fileVO.getName();
		String extensao = DummyUtils.getExtensao(name);
		File tmpFile = fileVO.getFile();

		parametroService.validarArquivoPermitido(name);

		long length = tmpFile.length();
		String hashChecksum = DummyUtils.getHashChecksum(tmpFile);

		ProcessoLogAnexo anexo = new ProcessoLogAnexo();
		anexo.setProcessoLog(log);
		anexo.setNome(name);
		anexo.setExtensao(extensao);
		anexo.setPath("");
		anexo.setTamanho(length);
		anexo.setHashChecksum(hashChecksum);

		processoLogAnexoRepository.saveOrUpdate(anexo);

		String path = gerarPath(anexo);
		anexo.setPath(path);

		try {
			FileUtils.copyFile(tmpFile, new File(path));
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		processoLogAnexoRepository.saveOrUpdate(anexo);
		
		salvarTamanhoImagem(anexo);

		Set<ProcessoLogAnexo> anexos = log.getAnexos();
		anexos.add(anexo);
	}

	public File getFile(ProcessoLogAnexo anexo) {

		String path = anexo.getPath();
		File file = new File(path);

		if(!file.exists()) {

			path = gerarPath(anexo);
			file = new File(path);
		}

		return file;
	}

	public String gerarPath(ProcessoLogAnexo anexo) {

		String imagemDir = resourceService.getValue(ResourceService.ANEXO_PROCESSO_PATH);
		String separador = File.separator;

		return ProcessoLogAnexo.criaPath(anexo, imagemDir, separador);
	}

	public ProcessoLogAnexo get(Long id) {
		return processoLogAnexoRepository.get(id);
	}

	public List<Long> findIdsByDataDigitalizacao(Date dataInicio, Date dataFim) {
		return processoLogAnexoRepository.findIdsByDataDigitalizacao(dataInicio, dataFim);
	}

	public List<ProcessoLogAnexo> findByIds(List<Long> ids) {
		return processoLogAnexoRepository.findByIds(ids);
	}
	
	public List<ProcessoLogAnexo> findByProcessoAnexos(AnexoFiltro filtro) {
		return processoLogAnexoRepository.findByProcessoAnexos(filtro);
	}
	
	@Transactional(rollbackFor=Exception.class)
	public void salvarTamanhoTodasImagem(){
		List<ProcessoLogAnexo> plas = processoLogAnexoRepository.findAll();
		
		for (ProcessoLogAnexo pla : plas) {
			salvarTamanhoImagem(pla);
		}
	}
	private void salvarTamanhoImagem(ProcessoLogAnexo anexo) {

		UtilBean utilBean = new UtilBean();
		if (utilBean.isExtensaoImagem(anexo.getExtensao())){
			BufferedImage bimg;
			try {
				bimg = ImageIO.read(new File(anexo.getPath()));
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			anexo.setLarguraImagem(bimg.getWidth());
			anexo.setAlturaImagem(bimg.getHeight());
			processoLogAnexoRepository.saveOrUpdate(anexo);
		}
	}
}
