package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.vo.RelatorioLicenciamentoOCRVO;
import net.wasys.getdoc.domain.vo.RelatorioLicenciamentoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoOCRFiltro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RelatorioLicenciamentoOCRService {

	@Autowired private LogAnaliseIAService logAnaliseIAService;

	public List<RelatorioLicenciamentoOCRVO> findProcessos(RelatorioLicenciamentoOCRFiltro filtro) {

		List<Object[]> list = logAnaliseIAService.findRelatorioLicenciamento(filtro);
		List<RelatorioLicenciamentoOCRVO> processos = new ArrayList<>();

		for (Object[] item : list) {

			Long id = ((Number) item[0]).longValue();
			String nome = ((String) item[1]);

			List<Long> qtdPorMes = new ArrayList<>();
			for (int i = 0; i < filtro.getMeses().size(); i++){
				qtdPorMes.add(((Number) item[2+i]).longValue());
			}

			RelatorioLicenciamentoOCRVO vo = new RelatorioLicenciamentoOCRVO();
			vo.setId(id);
			vo.setNome(nome);
			vo.setQtdPorMes(qtdPorMes);
			processos.add(vo);
		}
		return processos;
	}

}
