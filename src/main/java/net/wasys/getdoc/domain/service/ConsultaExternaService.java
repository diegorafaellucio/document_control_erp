package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.repository.ConsultaExternaRepository;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.ConsultaExternaFiltro;
import net.wasys.getdoc.domain.vo.filtro.LogAcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.correiows.Exception;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.Bolso;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class ConsultaExternaService {

	@Autowired private ConsultaExternaRepository consultaExternaRepository;
	@Autowired private ParametroService parametroService;
	@Autowired private ConsultaExternaLogService consultaExternaLogService;
	@Autowired private ApplicationContext applicationContext;

	@Transactional(rollbackFor=Exception.class, propagation=Propagation.REQUIRES_NEW)
	public void salvarComLog(ConsultaExterna consultaExterna, WebServiceClientVO vo) {

		saveOrUpdate(consultaExterna);
		consultaExternaLogService.criarConsultaExternaLog(vo, consultaExterna);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ConsultaExterna consultaExterna) {
		consultaExternaRepository.saveOrUpdate(consultaExterna);
	}

	public Integer countByFiltro(ConsultaExternaFiltro filtro) {
		return consultaExternaRepository.countByFiltro(filtro);
	}

	public List<ConsultaExterna> findByFiltro(ConsultaExternaFiltro filtro, Integer first, Integer pageSize) {
		return consultaExternaRepository.findByFiltro(filtro, first, pageSize);
	}

	public ConsultaExterna buscarConsultaAnteriorValida(String parametrosJson, TipoConsultaExterna tipoConsultaExterna) {

		Integer horasValidade = 0;
		if(TipoConsultaExterna.DETRAN_ARN.equals(tipoConsultaExterna)) {
			ConfiguracoesWsDetranArnVO dto = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_ARN, ConfiguracoesWsDetranArnVO.class);
			horasValidade = dto.getValidadeDetran();
		}
		else if(TipoConsultaExterna.DECODE.equals(tipoConsultaExterna) || TipoConsultaExterna.LEILAO.equals(tipoConsultaExterna)) {
			ConfiguracoesWsInfocarVO dto = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_INFOCAR, ConfiguracoesWsInfocarVO.class);
			if(TipoConsultaExterna.DECODE.equals(tipoConsultaExterna)) {
				horasValidade = dto.getValidadeDecode();
			} else {
				horasValidade = dto.getValidadeLeilao();
			}
		}
		else if(TipoConsultaExterna.CRIVO.equals(tipoConsultaExterna)) {
			ConfiguracoesWsCrivoVO dto = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_CRIVO, ConfiguracoesWsCrivoVO.class);
			horasValidade = dto.getValidadeCrivo();
		}
		else if(TipoConsultaExterna.DATA_VALID.equals(tipoConsultaExterna) || TipoConsultaExterna.DATA_VALID_BIOMETRIA.equals(tipoConsultaExterna)) {
			ConfiguracoesWsDataValidVO dto = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_DATAVALID, ConfiguracoesWsDataValidVO.class);
			horasValidade = dto.getValidadeDataValid();
		}
		else if(TipoConsultaExterna.BRSCAN.equals(tipoConsultaExterna)) {
			ConfiguracoesWsBrScanVO dto = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_BRSCAN, ConfiguracoesWsBrScanVO.class);
			horasValidade = dto.getValidade();
		}
		else if(TipoConsultaExterna.SIA_CONSULTA_LINHA_TEMPO.equals(tipoConsultaExterna) || TipoConsultaExterna.SIA_CONSULTA_COMPROVANTE_INSCRICAO.equals(tipoConsultaExterna)) {
			ConfiguracoesWsSiaVO dto = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_SIA, ConfiguracoesWsSiaVO.class);
			horasValidade = dto.getValidadeConsultaLinhaTempo();
		}
		else if(TipoConsultaExterna.SIA_CONSULTA_INSCRICOES.equals(tipoConsultaExterna)) {
			ConfiguracoesWsSiaVO dto = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_SIA, ConfiguracoesWsSiaVO.class);
			horasValidade = dto.getValidadeConsultaLinhaTempo();
		}
		else if(Arrays.asList(TipoConsultaExterna.DOCUMENTOS_DIGITALIZADOS_GETDOC_ALUNO, TipoConsultaExterna.CONSULTA_MATRICULA_GETDOC_ALUNO,
				TipoConsultaExterna.REAPROVEITA_DOCUMENTO_GETDOC_ALUNO, TipoConsultaExterna.CONSULTA_DOCUMENTO_GRADUACAO_GETDOC_ALUNO).contains(tipoConsultaExterna)) {
			return null;
		}

		Date aPartirDe = DateUtils.addHours(new Date(), -horasValidade);
		return consultaExternaRepository.findByParametrosAndConsultaExterna(parametrosJson, tipoConsultaExterna, aPartirDe);
	}

	public ConsultaExterna findLastByFiltro(ConsultaExternaFiltro filtro) {
		return consultaExternaRepository.findLastByFiltro(filtro);
	}

	public List<Object[]> findToMonitoramento(Date date, List<TipoConsultaExterna> tiposConsultaExterna) {
		return consultaExternaRepository.findToMonitoramento(date, tiposConsultaExterna);
	}

    public List<LogPorTempoVO> findWSPorTempo(LogAcessoFiltro filtro, int first, int pageSize) {

		List<Object[]> wsForTime = consultaExternaRepository.findWsPorTempo(filtro, first, pageSize);

		List<LogPorTempoVO> logPorTempoVOList = new ArrayList<>();

		for (Object[] objects : wsForTime) {

			String path = (String) objects[0];
			String horaMin = (String) objects[1];
			Long tempoMedio = objects[2] == null ? null : ((BigDecimal) objects[2]).longValue();
			Long tempoTotal = objects[3] == null ? null : ((BigDecimal) objects[3]).longValue();
			Long tamanhoTotal = objects[4] == null ? null : ((BigInteger) objects[4]).longValue();
			Long acessos =  objects[5] == null ? null : ((BigInteger) objects[5]).longValue();

			LogPorTempoVO logPorTempoVO = new LogPorTempoVO(path, horaMin, tempoMedio, tempoTotal, tamanhoTotal, acessos);

			logPorTempoVOList.add(logPorTempoVO);
		}

		return logPorTempoVOList;
    }

	public int countWSPorTempo(LogAcessoFiltro filtro) {
		return consultaExternaRepository.countWSPorTempo(filtro);
	}

	public List<StatusWSVO> findWsStatus() {

		Date hoje = new Date();
		hoje = DateUtils.addDays(hoje, -5);
		List<Object[]> logsAcessos = consultaExternaRepository.findWsStatus(hoje);
		List<StatusWSVO> logVos = new ArrayList<>();

		MultiValueMap<String, Object[]> logAcessosMap = new LinkedMultiValueMap<>();
		for (Object[] logAcesso : logsAcessos) {
			String tipo = (String) logAcesso[1];
			logAcessosMap.add(tipo, logAcesso);
		}

		for (String tipo : logAcessosMap.keySet()) {

			List<Object[]> logsAcessosTipo = logAcessosMap.get(tipo);
			Date data = null;
			String mensagemErro = null;
			String stackTrace = null;

			boolean temOK = false;
			for (Object[] logAcesso : logsAcessosTipo) {
				data = (Date) logAcesso[2];
				String status = (String) logAcesso[3];
				mensagemErro = (String) logAcesso[4];
				stackTrace = (String) logAcesso[5];
				temOK |= StatusConsultaExterna.SUCESSO.name().equals(status);
			}

			if(temOK) {
				mensagemErro = null;
				stackTrace = null;
			}

			String horaMinuto = DummyUtils.formatTime(data);
			StatusWSVO vo = new StatusWSVO(tipo, horaMinuto, mensagemErro, stackTrace);
			logVos.add(vo);
		}

		return logVos;
	}

	public void expurgarConsultaExterna() throws java.lang.Exception {

		boolean continuar = true;
		do {

			int timeout = 1000 * 60 * 10;//10 minutos
			Bolso<List<Long>> listBolso = new Bolso<>();

			TransactionWrapper tw1 = new TransactionWrapper(applicationContext);
			tw1.setRunnable(() -> {
				List<Long> expurgarList = consultaExternaRepository.findToExpurgo(10000);
				listBolso.setObjeto(expurgarList);
			});
			tw1.runNewThread(timeout);
			tw1.throwException();

			List<Long> list = listBolso.getObjeto();
			if(list.isEmpty()) {
				continuar = false;
			}
			else {

				TransactionWrapper tw2 = new TransactionWrapper(applicationContext);
				tw2.setRunnable(() -> {
					consultaExternaRepository.expurgar(list);
				});
				tw2.runNewThread(timeout);
				tw2.throwException();
			}

			DummyUtils.sleep(1500);
		}
		while (continuar);
	}
}
