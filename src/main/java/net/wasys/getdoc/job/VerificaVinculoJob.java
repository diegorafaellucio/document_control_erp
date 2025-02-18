package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Service
public class VerificaVinculoJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private CampoGrupoService campoGrupoService;

	@Override
	@Scheduled(cron="0 00 0/1 * * ?")//a cada 3 horas
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			List<Long> ids = Arrays.asList(TipoProcesso.SIS_PROUNI, TipoProcesso.SIS_FIES);
			List<TipoProcesso> tipoProcessos = tipoProcessoService.findByIds(ids);
			Situacao situacao;
			Situacao situacaoAnterior;

			for (TipoProcesso tipoProcesso : tipoProcessos) {
				ProcessoFiltro filtro = new ProcessoFiltro();
				Long tipoProcessoId = tipoProcesso.getId();

				if(tipoProcessoId.equals(TipoProcesso.SIS_PROUNI)) {
					situacao = new Situacao(Situacao.CONCLUIDO_PENDENTE_VINCULO_SISPROUNI_ID);
				} else {
					situacao= new Situacao(Situacao.CONCLUIDO_PENDENTE_VINCULO_SISFIES_ID);
				}
				List<Situacao> situacoes = Arrays.asList(situacao);
				filtro.setSituacao(situacoes);

				List<Processo> processos = processoService.findByFiltro(filtro, null, null);

				for (Processo processo : processos) {
					Long processoId = processo.getId();
					List<CampoGrupo> camposGrupos = campoGrupoService.findByProcessoIdAndNome(processoId, CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome());
					processo.setGruposCampos(new HashSet<>(camposGrupos));
					String valor = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.DATA_VINCULO_SIA);

					if(StringUtils.isNotBlank(valor)){
						ProcessoLog processoLogAnterior = processoLogService.findSituacaoAnterior(processo, situacao,null);
						Long id = processoLogAnterior.getId();
						DummyUtils.systraceThread("Processo log encontrado: " + id + ". Processo: " + processoId);

						if (processoLogAnterior != null) {
							situacaoAnterior = processoLogAnterior.getSituacaoAnterior();
							DummyUtils.systraceThread("Situacao destino: " + situacaoAnterior.getNome());
							try {
								processoService.concluirEmMassa(Arrays.asList(processo),null, situacaoAnterior,"Alteração de situação job, verificação de vínculo");
							} catch (Exception e) {
								DummyUtils.systraceThread("Erro ao tentar alterar situação de processo. Exception: " + e.getMessage());
								e.printStackTrace();
							}
						}
					}
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
}
