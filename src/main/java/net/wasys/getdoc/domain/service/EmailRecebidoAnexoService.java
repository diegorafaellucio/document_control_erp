package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.entity.EmailRecebidoAnexo;
import net.wasys.getdoc.domain.repository.EmailRecebidoAnexoRepository;
import net.wasys.util.DummyUtils;

@Service
public class EmailRecebidoAnexoService {

	@Autowired private EmailRecebidoAnexoRepository emailRecebidoAnexoRepository;
	@Autowired private ResourceService resourceService;

	public EmailRecebidoAnexo get(Long id) {
		return emailRecebidoAnexoRepository.get(id);
	}

	public void save(EmailRecebidoAnexo anexo) {

		String filePath = anexo.getPath();
		File tmpFile = new File(filePath);

		long length = tmpFile.length();
		String hashChecksum = DummyUtils.getHashChecksum(tmpFile);

		anexo.setTamanho(length);
		anexo.setHashChecksum(hashChecksum);

		emailRecebidoAnexoRepository.save(anexo);

		String path = gerarPath(anexo);
		anexo.setPath(path);

		try {
			FileUtils.copyFile(tmpFile, new File(path));

			DummyUtils.deleteFile(tmpFile);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		emailRecebidoAnexoRepository.saveOrUpdate(anexo);
	}

	public String gerarPath(EmailRecebidoAnexo anexo) {

		String imagemDir = resourceService.getValue(ResourceService.ANEXO_EMAIL_PATH);
		String separador = File.separator;

		return EmailRecebidoAnexo.criaPath(anexo, imagemDir, separador);
	}

	public File getFile(EmailRecebidoAnexo anexo) {

		String path = anexo.getPath();
		File file = new File(path);

		if(!file.exists()) {

			path = gerarPath(anexo);
			file = new File(path);
		}

		return file;
	}

	public List<Long> findIdsByDataDigitalizacao(Date dataInicio, Date dataFim) {
		return emailRecebidoAnexoRepository.findIdsByDataDigitalizacao(dataInicio, dataFim);
	}

	public List<EmailRecebidoAnexo> findByIds(List<Long> ids) {
		return emailRecebidoAnexoRepository.findByIds(ids);
	}
}
