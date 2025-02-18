package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.TipoCampo;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.restws.dto.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.other.RepeatTry;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import net.wasys.getdoc.domain.entity.BaseInterna;

import java.util.*;

@Service
public class SincronizaBaseJob extends TransactionWrapperJob {

	public static final String CODIGO = "codigo";
	public static final String NOME = "descricao";
	public static final String COD_INSTITUICAO = "codigoInstituicao";
	public static final String POLO_PARCEIRO = "poloParceiro";
	public static final String COD_REGIONAL = "codigoRegional";
	public static final String COD_TIPO_CURSO = "codigoTipoCurso";
	public static final String COD_CAMPUS = "codigoCampus";
	public static final String NOM_MANTENEDORA = "nomeMantenedora";
	public static final String COD_CURRICULO_CURSO = "codigoCurriculoCurso";
	public static final String USUARIO = "usuarioUltimaAtualizacaoId";

	@Autowired private GeralService geralService;
	@Autowired private FeriadoService feriadoService;
	@Autowired private AreaService areaService;
	@Autowired private SubareaService subareaService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private ResourceService resourceService;

	@Override
	@Scheduled(cron="0 4/3 * * * ?")//a cada 3 minutos
	//@Scheduled(cron="0/30 * * * * ?")//a cada 30 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		String executarOcr = System.getProperty("getdoc.executarSincronismo");
		if("false".equals(executarOcr)) {
			DummyUtils.systraceThread("FIM (executarSincronismo false)");
			return;
		}

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

		if(!geralService.isSincronismoHabilitado()) {
			DummyUtils.systraceThread("job finalizado, endpoint do geral não configurado.");
			return;
		} else {
			String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
			DummyUtils.systraceThread("sincronizando com " + geralEndpoint);
		}

		String sincronizar = System.getProperty("getdoc.sincronizar");
		if("false".equals(sincronizar)) {
			DummyUtils.systraceThread("sincronização desabilitada");
			return;
		}

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			RepeatTry<Object> rt = new RepeatTry<>(3, 30 * 1000);
			rt.setToTry(() -> sincronizarBase());
			rt.execute();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private Boolean sincronizarBase() throws Exception {

		atualizarFeriados();
		atualizarAreas();
		atualizarSubareas();
		atualizarUsuarios();
		atualizarOutrasBases();

		return true;
	}

	private void atualizarFeriados() throws Exception {

		DummyUtils.systraceThread("...");

		Date ultimaDataAtualizacao = feriadoService.getUltimaDataAtualizacao();
		ultimaDataAtualizacao = ultimaDataAtualizacao != null ? ultimaDataAtualizacao : DummyUtils.parseDate("01/01/2010");
		FeriadoDTO[] result = geralService.findFeriados(ultimaDataAtualizacao);

		feriadoService.atualizar(result);
	}

	private void atualizarAreas() throws Exception {

		DummyUtils.systraceThread("...");

		Date ultimaDataAtualizacao = areaService.getUltimaDataAtualizacao();
		ultimaDataAtualizacao = ultimaDataAtualizacao != null ? ultimaDataAtualizacao : DummyUtils.parseDate("01/01/2010");
		AreaDTO[] result = geralService.findAreas(ultimaDataAtualizacao);

		areaService.atualizar(result);
	}

	private void atualizarSubareas() throws Exception {

		DummyUtils.systraceThread("...");

		Date ultimaDataAtualizacao = subareaService.getUltimaDataAtualizacao();
		ultimaDataAtualizacao = ultimaDataAtualizacao != null ? ultimaDataAtualizacao : DummyUtils.parseDate("01/01/2010");
		SubareaDTO[] result = geralService.findSubareas(ultimaDataAtualizacao);

		subareaService.atualizar(result);
	}

	private void atualizarUsuarios() throws Exception {

		DummyUtils.systraceThread("...");

		Date ultimaDataSincronizacao = usuarioService.getUltimaDataSincronizacao();
		ultimaDataSincronizacao = ultimaDataSincronizacao != null ? ultimaDataSincronizacao : DummyUtils.parseDate("01/01/2010");
		UsuarioDTO[] result = geralService.findUsuarios(ultimaDataSincronizacao);

		usuarioService.atualizar(result);

		Date dataCorte = DateUtils.addMinutes(ultimaDataSincronizacao, -20);
		Map<Long, Date> map = usuarioService.getDatasUltimosAcessos(dataCorte);

		int size = map.size();
		DataUltimoAcessoDTO[] datasUltimosAcessos = new DataUltimoAcessoDTO[size];
		List<Long> usuariosIds = new ArrayList<Long>(map.keySet());
		for (int i = 0; i < usuariosIds.size(); i++) {

			Long usuarioId = usuariosIds.get(i);
			Date data = map.get(usuarioId);

			DataUltimoAcessoDTO duaws = new DataUltimoAcessoDTO();
			duaws.setUsuarioId(usuarioId);
			duaws.setData(data);
			datasUltimosAcessos[i] = duaws;
		}

		if(datasUltimosAcessos.length > 0) {
			geralService.atualizarDatasUltimosAcessos(datasUltimosAcessos);
		}
	}

	private void atualizarOutrasBases() throws Exception {

		DummyUtils.systraceThread("...");

		Map<Long, String> basesSincronismo = new LinkedHashMap<>();
		basesSincronismo.put(BaseInterna.MANTENEDORA_ID, "sincronismo/find-mantenedoras/");
		basesSincronismo.put(BaseInterna.INSTITUICAO_ID, "sincronismo/find-instituicoes/");
		basesSincronismo.put(BaseInterna.REGIONAL_ID, "sincronismo/find-regionais/");
		basesSincronismo.put(BaseInterna.MODALIDADE_ENSINO_ID, "sincronismo/find-modalidades-ensino/");
		basesSincronismo.put(BaseInterna.CURRICULO_CURSO_ID, "sincronismo/find-curriculos-cursos/");
		basesSincronismo.put(BaseInterna.TURNO_ID, "sincronismo/find-turnos/");
		basesSincronismo.put(BaseInterna.TIPO_CURSO_ID, "sincronismo/find-tipos-cursos/");
		basesSincronismo.put(BaseInterna.FORMA_INGRESSO_ID, "sincronismo/find-formas-ingresso/");
		basesSincronismo.put(BaseInterna.CAMPUS_ID, "sincronismo/find-campus/");
		basesSincronismo.put(BaseInterna.CURSO_ID, "sincronismo/find-cursos/");
		basesSincronismo.put(BaseInterna.BANCOS_FINANCIAMENTOS_ID, "sincronismo/find-bancos-financiamentos/");
		basesSincronismo.put(BaseInterna.TIPO_BOLSA_PROUNI_ID, "sincronismo/find-tipos-bolsas-prouni/");
		for (Long baseInternaId : basesSincronismo.keySet()) {
			Date ultimaDataAtualizacao = baseRegistroService.getUltimaDataAtualizacao(baseInternaId);
			ultimaDataAtualizacao = ultimaDataAtualizacao != null ? ultimaDataAtualizacao : DummyUtils.parseDate("01/01/2010");

			String endpoint = basesSincronismo.get(baseInternaId);
			List<Map<String, String>> result = geralService.findOutrasBases(endpoint, ultimaDataAtualizacao);
			result = normalizaDados(baseInternaId, result);
			baseRegistroService.atualizar(baseInternaId, result);
		}
	}

	private List<Map<String, String>> normalizaDados(Long baseInternaId, List<Map<String, String>> dados){
		if( baseInternaId.equals(BaseInterna.MANTENEDORA_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.NOM_MANTENEDORA, item.remove(NOME));
			});
		} else if  (baseInternaId.equals(BaseInterna.INSTITUICAO_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_INSTITUICAO, item.remove(CODIGO));
				item.put(TipoCampo.NOM_INSTITUICAO, item.remove(NOME));
				item.put(TipoCampo.COD_REGIONAL, item.remove(COD_REGIONAL));
			});
		}  else if  (baseInternaId.equals(BaseInterna.REGIONAL_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_REGIONAL, item.remove(CODIGO));
				item.put(TipoCampo.NOM_REGIONAL, item.remove(NOME));
				item.put(TipoCampo.COD_INSTITUICAO, item.remove(COD_INSTITUICAO));
			});
		} else if( baseInternaId.equals(BaseInterna.CAMPUS_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_CAMPUS, item.remove(CODIGO));
				item.put(TipoCampo.NOM_CAMPUS, item.remove(NOME));
				item.put(TipoCampo.COD_REGIONAL, item.remove(COD_REGIONAL));
				item.put(TipoCampo.COD_INSTITUICAO, item.remove(COD_INSTITUICAO));
				item.put(TipoCampo.POLO_PARCEIRO, item.remove(POLO_PARCEIRO));
			});
		} else if  (baseInternaId.equals(BaseInterna.CURSO_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_CURSO, item.remove(CODIGO));
				item.put(TipoCampo.NOM_CURSO, item.remove(NOME));
				item.put(TipoCampo.COD_TIPO_CURSO, item.remove(COD_TIPO_CURSO));
				item.put(TipoCampo.ID_CURRICULO_CURSO, item.remove(COD_CURRICULO_CURSO));
				item.put(TipoCampo.COD_CAMPUS, item.remove(COD_CAMPUS));
			});
		} else if  (baseInternaId.equals(BaseInterna.TURNO_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_TURNO, item.remove(CODIGO));
				item.put(TipoCampo.NOM_TURNO, item.remove(NOME));
			});
		} else if  (baseInternaId.equals(BaseInterna.TIPO_CURSO_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_TIPO_CURSO, item.remove(CODIGO));
				item.put(TipoCampo.NOM_TIPO_CURSO, item.remove(NOME));
			});
		} else if  (baseInternaId.equals(BaseInterna.TIPO_BOLSA_PROUNI_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_TIPO_BOLSA, item.remove(CODIGO));
				item.put(TipoCampo.NOM_TIPO_BOLSA, item.remove(NOME));
			});
		} else if  (baseInternaId.equals(BaseInterna.MODALIDADE_ENSINO_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.TXT_MODALIDADE_ENSINO, item.remove(NOME));
			});
		} else if  (baseInternaId.equals(BaseInterna.FORMA_INGRESSO_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_FORMA_INGRESSO, item.remove(CODIGO));
				item.put(TipoCampo.NOM_FORMA_INGRESSO, item.remove(NOME));
			});
		} else if  (baseInternaId.equals(BaseInterna.BANCOS_FINANCIAMENTOS_ID) ){
			dados.forEach(item -> {
				item.put(TipoCampo.COD_FINANCIAMENTO, item.remove(CODIGO));
				item.put(TipoCampo.NOM_FINANCIAMENTO, item.remove(NOME));
			});
		}

		return dados;
	}
}
