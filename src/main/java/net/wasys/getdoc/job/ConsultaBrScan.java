package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsBrScanVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoRegraFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.rest.RestClient;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class ConsultaBrScan extends TransactionWrapperJob {

	@Autowired private ParametroService parametroService;
	@Autowired private ProcessoRegraService processoRegraService;
	@Autowired private ConsultaExternaService consultaExternaService;
	@Autowired private ConsultaExternaLogService consultaExternaLogService;
	@Autowired private RegraService regraService;
	@Autowired private SubRegraService subRegraService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	//@Scheduled(cron="9/30 * * * * ?")//a cada 30 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
		filtro.setTipoConsultaExterna(TipoConsultaExterna.BRSCAN);
		filtro.setStatusList(Arrays.asList(StatusProcessoRegra.PROCESSANDO));
		List<ProcessoRegra> lasts = processoRegraService.findLasts(filtro);
		ConfiguracoesWsBrScanVO cwbs = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_BRSCAN, ConfiguracoesWsBrScanVO.class);

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			for(ProcessoRegra pr: lasts){

				Long subRegraFinalId = pr.getSubRegraFinalId();
				if(subRegraFinalId == null){
					continue;
				}
				SubRegra subRegra = subRegraService.get(subRegraFinalId);
				List<DeparaParam> deparaParams = subRegra.getDeparaParams();
				DeparaParam deparaParam = deparaParams.get(0);
				String origem = deparaParam.getOrigem();
				String[] split = origem.split("']\\['");
				String grupo = split[0].replaceAll("\\['","");
				String campo = split[1].replaceAll("']","");

				Processo processo = pr.getProcesso();
				String cpf = DummyUtils.getCampoProcessoValor(processo, grupo, campo);
				Map consultar = consultar(cwbs, cpf);

				ArrayList aReturn = (ArrayList) consultar.get("return");
				Map retornoConsulta = aReturn != null ? (Map) aReturn.get(0) : null;
				String statusRegistro = retornoConsulta != null ? (String) retornoConsulta.get("StatusRegistro") : null;

				if(StringUtils.isNotBlank(statusRegistro) && statusRegistro.equals("Concluído")){
					JSONObject jsonRet = new JSONObject(consultar);
					ConsultaExterna consultaExterna = new ConsultaExterna();
					consultaExterna.setTipo(TipoConsultaExterna.BRSCAN);
					consultaExterna.setResultado(jsonRet.toString());
					consultaExterna.setData(new Date());

					consultaExternaService.saveOrUpdate(consultaExterna);

					ConsultaExternaLog consultaExternaLog = new ConsultaExternaLog();
					consultaExternaLog.setProcesso(processo);
					consultaExternaLog.setConsultaExterna(consultaExterna);
					consultaExternaLog.setData(new Date());

					consultaExternaLogService.saveOrUpdate(consultaExternaLog);

					regraService.continueExecutarRegra(pr, null);
				}

			}
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private static Map consultar(ConfiguracoesWsBrScanVO cwbs, String cpf) throws Exception {

		String endpointConsultar = cwbs.getEndpointConsultar();
		RestClient rc1 = new RestClient(endpointConsultar);
		rc1.setRepeatTimes(1);

		Map<String, String> headers1 = new LinkedHashMap<>();
		headers1.put("Content-Type", "application/json");
		rc1.setHeaders(headers1);

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("codLogin", cwbs.getUsuario());
		map.put("desSenha", cwbs.getSenha());
		map.put("cpfcnpj", cpf);

		String json = DummyUtils.objectToJson(map);

		ByteArrayEntity entity2 = new ByteArrayEntity(json.getBytes());

		DummyUtils.systraceThread("acessando url de consulta...");
		Map map2 = rc1.execute(entity2, Map.class);

		DummyUtils.systraceThread(String.valueOf(map2));

		return map2;
	}
}