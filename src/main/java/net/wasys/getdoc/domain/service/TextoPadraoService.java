package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.enumeration.TipoEmail;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.domain.repository.TextoPadraoRepository;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.getdoc.domain.vo.filtro.TextoPadraoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.StringWriter;
import java.util.*;

@Service
public class TextoPadraoService {

	@Autowired private TextoPadraoRepository textoPadraoRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private ProcessoService processoService;
	@Autowired private EmailSmtpService emailSmtpService;

	public TextoPadrao get(Long id) {
		return textoPadraoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(TextoPadrao textoPadrao, Usuario usuario, List<TipoProcesso> tiposProcessosSelecionados) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(textoPadrao);

		atualizarTiposProcessos(textoPadrao, tiposProcessosSelecionados);

		try {
			textoPadraoRepository.saveOrUpdate(textoPadrao);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(textoPadrao, usuario, tipoAlteracao);
	}

	private void atualizarTiposProcessos(TextoPadrao textoPadrao, List<TipoProcesso> tiposProcessosSelecionados) {

		Set<TipoProcesso> presentes = new HashSet<>();
		Set<TextoPadraoTipoProcesso> tptps = textoPadrao.getTiposProcessos();
		List<TextoPadraoTipoProcesso> list = new ArrayList<>(tptps);
		for (TextoPadraoTipoProcesso tptp : list) {
			TipoProcesso tipoProcesso = tptp.getTipoProcesso();
			if(!tiposProcessosSelecionados.contains(tipoProcesso)) {
				tptps.remove(tptp);
			}
			presentes.add(tipoProcesso);
		}

		for (TipoProcesso tipoProcesso : tiposProcessosSelecionados) {
			if(!presentes.contains(tipoProcesso)) {
				TextoPadraoTipoProcesso tptp = new TextoPadraoTipoProcesso();
				tptp.setTextoPadrao(textoPadrao);
				tptp.setTipoProcesso(tipoProcesso);
				tptps.add(tptp);
			}
		}
	}

	public List<TextoPadrao> findByFiltro(TextoPadraoFiltro filtro, Integer inicio, Integer max) {
		return textoPadraoRepository.findByFiltro(filtro, inicio, max);
	}

	public Map<Long, TextoPadrao> findByIds(List<Long> ids) {
		TextoPadraoFiltro filtro = new TextoPadraoFiltro();
		filtro.setTextoPadraoIds(ids);
		List<TextoPadrao> textoPadroes = textoPadraoRepository.findByFiltro(filtro, null, null);

		Map<Long, TextoPadrao> map = new HashMap<>();
		for(TextoPadrao textoPadrao : textoPadroes){
			Long textoPadraoId = textoPadrao.getId();
			map.put(textoPadraoId, textoPadrao);
		}
		return map;
	}

	public int count(TextoPadraoFiltro filtro) {
		return textoPadraoRepository.count(filtro);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long textoPadraoId, Usuario usuario) throws MessageKeyException {

		TextoPadrao textoPadrao = get(textoPadraoId);

		logAlteracaoService.registrarAlteracao(textoPadrao, usuario, TipoAlteracao.EXCLUSAO);

		try {
			textoPadraoRepository.deleteById(textoPadraoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public String renderizar(TextoPadrao tp, Processo processo, List<ProcessoAjuda> pendenciasCheckList, Usuario usuario) {

		String texto = tp.getTexto();
		Long processoId = processo.getId();
		processo = processoService.get(processoId);

		Map<String, String> model = new HashMap<>();
		if(pendenciasCheckList != null) {
			StringBuilder pendenciasCheckListSB = new StringBuilder();
			for (ProcessoAjuda pa : pendenciasCheckList) {
				String texto2 = pa.getTexto();
				String solucao = pa.getSolucao();
				pendenciasCheckListSB.append(" • ").append(texto2).append("\n");
				pendenciasCheckListSB.append(" Solução a ser aplicada: ").append(solucao).append("\n");
			}
			String pendenciasCheckListStr = pendenciasCheckListSB.toString();
			pendenciasCheckListStr = pendenciasCheckListStr.replaceAll("\n$", "");
			model.put("pendenciasCheckList", pendenciasCheckListStr);
		}

		Set<CampoGrupo> grupos = processo.getGruposCampos();
		for (CampoGrupo grupo : grupos) {
			String nomeGrupo = grupo.getNome();
			nomeGrupo = StringUtils.trim(nomeGrupo);
			nomeGrupo = DummyUtils.substituirCaracteresEspeciais(nomeGrupo);
			nomeGrupo = nomeGrupo.replace(" ", "");
			nomeGrupo = nomeGrupo.replace("/", "");
			Set<Campo> campos = grupo.getCampos();
			for (Campo campo : campos) {
				String nomeCampo = campo.getNome();
				nomeCampo = StringUtils.trim(nomeCampo);
				nomeCampo = DummyUtils.substituirCaracteresEspeciais(nomeCampo);
				nomeCampo = nomeCampo.replace(" ", "");
				nomeCampo = nomeCampo.replace("/", "");
				nomeCampo = nomeCampo.replace("-", "");
				String valor = campo.getValor();
				if (campo.getTipo().equals(TipoEntradaCampo.COMBO_BOX_ID)) {

					BaseInterna baseInterna = campo.getBaseInterna();
					String chaveUnicidade = campo.getValor();

					BaseRegistroFiltro baseRegistroFiltro = new BaseRegistroFiltro();
					baseRegistroFiltro.setBaseInterna(baseInterna);
					baseRegistroFiltro.setChaveUnicidade(chaveUnicidade);

					String descricao = "";

					List<RegistroValorVO> byFiltro = baseRegistroService.findByFiltro(baseRegistroFiltro, 0, 1);
					for(RegistroValorVO v: byFiltro) {
						descricao = v.getValor("descricao");
					}
					valor = descricao;
				}
				model.put(nomeGrupo + "_" + nomeCampo, valor);
			}
		}

		String content = render(texto, model);

		return content;
	}

	public String renderizar(TextoPadrao tp, Processo processo) {

		Map<String, String> model = new HashMap<>();

		String texto = tp.getTexto();
		String content = render(texto, model);

		return content;
	}

	private String render(String texto, Map<String, String> model) {

		VelocityEngine engine = new VelocityEngine();
		engine.setProperty(Velocity.RESOURCE_LOADER, "string");
		engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
		engine.addProperty("string.resource.loader.repository.static", "false");
		engine.init();

		String keyTemplate = "template-" + Math.random();

		StringResourceRepository rep = (StringResourceRepository) engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
		rep.putStringResource(keyTemplate, texto);

		VelocityContext context = new VelocityContext();
		for (String key : model.keySet()) {
			String value = model.get(key);
			context.put(key, value);
		}

		StringWriter writer = new StringWriter();
		Template template = engine.getTemplate(keyTemplate, "UTF-8");
		template.merge(context, writer);

		String content = String.valueOf(writer);
		return content;
	}

	public String getBodyExemplo(Long textoPadraoId){

		String nomeTemplate = TipoEmail.getNomeTemplate(textoPadraoId);

		if(nomeTemplate == null){
			return null;
		}

		TextoPadrao textoPadrao = get(textoPadraoId);
		String texto = textoPadrao.getTexto();

		List<Documento> list = new ArrayList<>();
		Map<String, String> map = new LinkedHashMap<>(

		);
		for(int i = 1; i < 4; i++ ) {
			Documento documento = new Documento();
			documento.setNome("Documento Exemplo " + i);
			TipoDocumento tipoDocumento = new TipoDocumento();
			tipoDocumento.setObservacaoParaNotificacao("Exemplo Observação " + i);
			documento.setTipoDocumento(tipoDocumento);
			list.add(documento);
			map.put(documento.getNome(), "Status Documento");
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("texto", texto);
		model.put("nome", "Aluno Exemplo");
		model.put("nomeAluno", "Aluno Exemplo");
		model.put("numInscricao", "123456");
		model.put("curso", "Curso Exemplo");
		model.put("documentos", map);
		model.put("documentosObrigatorios", new ArrayList<>(list));
		model.put("documentosEspeciais", new ArrayList<>(list));
		model.put("documentosComposicaoFamiliar", new ArrayList<>(list));
		model.put("documentosCasosEspeciais", new ArrayList<>(list));
		model.put("link", "portaldocandidato.estacio.br");
		model.put("localOferta", "Local de Oferta Exemplo");
		model.put("nacionalidade", "Nacionalidade");
		model.put("endereco", "R. Exemplo");
		model.put("estadoCivil", "Solteiro");
		model.put("profissao", "Estudante");
		model.put("cpf", "000.000.000-00");

		Map<String, File> attachments = new HashMap<>();

		return emailSmtpService.getBody(nomeTemplate, model, attachments);
	}
}
