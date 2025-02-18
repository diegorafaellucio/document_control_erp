package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.entity.ConfiguracaoGeracaoRelatorio;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoConfiguracaoRelatorio;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.HorasUteisCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GerarRelatoriosService {

	public static List<Long> IDS_CONFIGS_EM_EXECUCAO = new ArrayList<>();

	@Autowired private GerarRelatorioDocumentosPendentesService gerarRelatorioDocumentosPendentesService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private ParametroService parametroService;

	public void iniciarGeracaoRelatoriosAutomatico(List<ConfiguracaoGeracaoRelatorio> configs, Usuario usuario) {
		iniciarGeracaoRelatorios(configs, usuario, false);
	}

	public void iniciarGeracaoRelatoriosManual(List<ConfiguracaoGeracaoRelatorio> configs, Usuario usuario) {
		iniciarGeracaoRelatorios(configs, usuario, true);
	}

	private void iniciarGeracaoRelatorios(List<ConfiguracaoGeracaoRelatorio> configs, Usuario usuario, boolean execucaoManual) {

		for (ConfiguracaoGeracaoRelatorio config : configs) {
			Boolean podeExecutar = vericaSePodeExecutarEmHorarioDeExpediente(config, execucaoManual);
			if(!podeExecutar) {
				return;
			}

			TipoConfiguracaoRelatorio tipo = config.getTipo();
			if (TipoConfiguracaoRelatorio.RELATORIO_PENDENCIA_DOCUMENTO.equals(tipo)) {

				Long configId = config.getId();
				if (!IDS_CONFIGS_EM_EXECUCAO.contains(configId) || execucaoManual) {

					TransactionWrapper tw = new TransactionWrapper(applicationContext);

					tw.setRunnable(() -> {

						gerarRelatorioDocumentosPendentesService.gerar(config, usuario, execucaoManual);
						IDS_CONFIGS_EM_EXECUCAO.remove(configId);
					});

					tw.startThread();
					IDS_CONFIGS_EM_EXECUCAO.add(configId);
				}
				else {
					throw new MessageKeyException("configuracaoRelatorioJaEstaSendoExecutada.error", configId);
				}
			}
		}
	}

	private Boolean vericaSePodeExecutarEmHorarioDeExpediente(ConfiguracaoGeracaoRelatorio config, boolean execucaoManual) {
		Boolean executaEmExpediente = config.getExecutaEmExpediente();
		if(executaEmExpediente || execucaoManual) {
			return true;
		}

		String[] expedienteArray = parametroService.getExpediente();
		HorasUteisCalculator.Expediente expediente = new HorasUteisCalculator.Expediente(expedienteArray);
		boolean horarioExpediente = expediente.isHorarioExpediente(new Date(), false);
		String expedienteStr = "";
		for (String ex : expedienteArray) {
			expedienteStr = expedienteStr.concat(ex).concat(" ");
		}

		return !horarioExpediente;
	}

	public boolean estaExecutando(Long configuracaoGeracaoRelatorioId) {
		return IDS_CONFIGS_EM_EXECUCAO.contains(configuracaoGeracaoRelatorioId);
	}
}
