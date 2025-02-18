package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.vo.RelatorioLicenciamentoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RelatorioLicenciamentoService {

	@Autowired private ProcessoService processoService;
	@Autowired private LoginLogService loginLogService;

	public List<RelatorioLicenciamentoVO> findProcessos(RelatorioLicenciamentoFiltro filtro) {

		List<Object[]> list = processoService.findRelatorioLicenciamento(filtro);
		List<RelatorioLicenciamentoVO> processos = new ArrayList<RelatorioLicenciamentoVO>();

		for (Object[] item : list) {

			Long id = ((Number) item[0]).longValue();
			String nome = ((String) item[1]);

			List<Long> qtdPorMes = new ArrayList<>();
			for (int i = 0; i < filtro.getMeses().size(); i++){
				qtdPorMes.add(((Number) item[2+i]).longValue());
			}

			RelatorioLicenciamentoVO vo = new RelatorioLicenciamentoVO();
			vo.setId(id);
			vo.setNome(nome);
			vo.setQtdPorMes(qtdPorMes);
			processos.add(vo);
		}
		return processos;
	}

	public List<RelatorioLicenciamentoVO> findLogins(RelatorioLicenciamentoFiltro filtro) {
		List<RelatorioLicenciamentoVO> logins = loginLogService.findRelatorioLicenciamento(filtro);
		return logins;
	}
}
