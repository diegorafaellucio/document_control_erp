package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.ConfiguracaoGeracaoRelatorio;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.ConfiguracaoGeracaoRelatorioRepository;
import net.wasys.getdoc.domain.vo.filtro.ConfiguracaoGeracaoRelatorioFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.TransactionWrapper;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.wasys.util.DummyUtils.objectToJson;
import static net.wasys.util.DummyUtils.systraceThread;

import lombok.extern.slf4j.Slf4j;

@Service
public class ConfiguracaoGeracaoRelatorioService {

	private final Pattern NUM_VERSAO_REGEXP = Pattern.compile(".* \\((\\d)\\)$");

	@Autowired private ConfiguracaoGeracaoRelatorioRepository repository;
	@Autowired private GerarRelatoriosService gerarRelatoriosService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private LogAlteracaoService logAlteracaoService;

	public void salvarConfiguracao(ConfiguracaoGeracaoRelatorio configuracao, RelatorioPendenciaDocumentoFiltro filtro, Usuario usuarioLogado) {

		if (filtro != null) {

			String opcoes = objectToJson(filtro);
			configuracao.setOpcoes(opcoes);
		}

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(configuracao);

		try {
			repository.saveOrUpdate(configuracao);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(configuracao, usuarioLogado, tipoAlteracao);
	}

	public List<ConfiguracaoGeracaoRelatorio> findAtivosEntreHorarios(String inicio, String fim, boolean isViradaDeDia) {
		return repository.findAtivosEntreHorarios(inicio, fim, isViradaDeDia);
	}

	public int countByFiltro(ConfiguracaoGeracaoRelatorioFiltro filtro) {
		return repository.countByFiltro(filtro);
	}

	public List<ConfiguracaoGeracaoRelatorio> findByFiltro(ConfiguracaoGeracaoRelatorioFiltro filtro, Integer inicio, Integer max) {
		return repository.findByFiltro(filtro, inicio, max);
	}

	public void gerarRelatorio(ConfiguracaoGeracaoRelatorio config, Usuario usuarioLogado) {

		systraceThread("Iniciou execução manual. Usuário=" + usuarioLogado);

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> gerarRelatoriosService.iniciarGeracaoRelatoriosManual(Collections.singletonList(config), usuarioLogado));
		tw.startThread();
	}

	@Transactional(rollbackFor = Exception.class)
	public void excluir(Long configId, Usuario usuarioLogado) {

		ConfiguracaoGeracaoRelatorio config = repository.get(configId);

		logAlteracaoService.registrarAlteracao(config, usuarioLogado, TipoAlteracao.EXCLUSAO);

		try {
			repository.deleteById(configId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public void duplicar(ConfiguracaoGeracaoRelatorio original, Usuario usuario) {

		ConfiguracaoGeracaoRelatorio copia = new ConfiguracaoGeracaoRelatorio();

		try {

			BeanUtils.copyProperties(copia, original);

			String nomeOriginal = original.getNome();
			String nomeCopia = novoNomeDuplicado(nomeOriginal);

			copia.setId(null);
			copia.setNome(nomeCopia);
			copia.setAtivo(false);

			salvarConfiguracao(copia, null, usuario);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public String novoNomeDuplicado(String nome) {

		String novoNome = "";

		int num = 1;
		Matcher matcher = NUM_VERSAO_REGEXP.matcher(nome);
		if (matcher.find()) {
			String group = matcher.group(1);
			num = Integer.parseInt(group);
			nome = nome.substring(0, nome.length() - 4);
		}
		num++;
		boolean exists;
		do {
			novoNome = nome + " (" + num++ + ")";
			exists = repository.existsByNome(novoNome);
		}
		while (exists);

		return novoNome;
	}
}
