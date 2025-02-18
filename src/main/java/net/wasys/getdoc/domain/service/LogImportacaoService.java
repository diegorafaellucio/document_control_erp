package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.enumeration.StatusImportacao;
import net.wasys.getdoc.domain.enumeration.TipoImportacao;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.LogImportacao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.repository.LogImportacaoRepository;
import net.wasys.getdoc.domain.vo.ResultVO;
import net.wasys.getdoc.domain.vo.filtro.LogImportacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class LogImportacaoService {

	@Autowired private LogImportacaoRepository logImportacaoRepository;
	@Autowired private ResourceService resourceService;

	public LogImportacao get(Long id) {
		return logImportacaoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(LogImportacao logImportacao) throws MessageKeyException {

		try {
			logImportacaoRepository.saveOrUpdate(logImportacao);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	public LogImportacao criarLogImportacao(TipoImportacao tipoImportacao, Usuario usuario, ResultVO resultVO, File file, String nomeOriginalFile, StatusImportacao status) throws MessageKeyException {

		LogImportacao log = new LogImportacao();
		log.setTipo(tipoImportacao);
		log.setUsuario(usuario);
		log.setInserts(resultVO.getInserts());
		log.setUpdates(resultVO.getUpdates());
		log.setDeletes(resultVO.getDeletes());
		log.setStatus(status);
		log.setData(new Date());

		String logImportacaoPath = resourceService.getValue(ResourceService.LOG_IMPORTACAO_PATH);
		String path = logImportacaoPath + File.separator + tipoImportacao.name().toLowerCase();
		if(TipoImportacao.PROCESSO.equals(tipoImportacao) || TipoImportacao.MODELO.equals(tipoImportacao)) {
			path = getPathWithDate(path);
		}
		File dir = new File(path);
		String fileName = StringUtils.isBlank(nomeOriginalFile) ? file.getName() : nomeOriginalFile;
		File fileDestino = DummyUtils.getFileDestino(dir, fileName);
		try {
			FileUtils.copyFile(file, fileDestino);
			String absolutePath = fileDestino.getAbsolutePath();
			log.setPathArquivo(absolutePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.setNomeArquivo(fileName);

		saveOrUpdate(log);

		return log;
	}

	private String getPathWithDate(String path) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int ano = c.get(Calendar.YEAR);
		int mes = c.get(Calendar.MONTH) + 1;
		String mesStr = mes <= 9 ? "0" + mes : String.valueOf(mes);
		int dia = c.get(Calendar.DAY_OF_MONTH);
		String diaStr = dia <= 9 ? "0" + dia : String.valueOf(dia);

		String separador = File.separator;
		StringBuilder caminho = new StringBuilder(path);
		caminho.append(separador);
		caminho.append(ano).append(separador);
		caminho.append(mesStr).append(separador);
		caminho.append(diaStr).append(separador);

		path = caminho.toString();
		return path;
	}

	public int countByFiltro(LogImportacaoFiltro filtro) {
		return logImportacaoRepository.countByFiltro(filtro);
	}

	public List<LogImportacao> findByFiltro(LogImportacaoFiltro filtro, Integer first, Integer pageSize) {
		return logImportacaoRepository.findByFiltro(filtro, first, pageSize);
	}

	public LogImportacao getLastByFiltro(LogImportacaoFiltro filtro) {
		return logImportacaoRepository.getLastByFiltro(filtro);
	}

	public boolean temImportacaoEmProcessamento() {
		return logImportacaoRepository.temImportacaoEmProcessamento();
	}
}