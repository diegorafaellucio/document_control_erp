package net.wasys.getdoc.domain.service;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.RelatorioGeral;
import net.wasys.getdoc.domain.entity.RelatorioGeralDocumento;
import net.wasys.getdoc.domain.entity.RelatorioGeralSituacao;
import net.wasys.getdoc.domain.repository.RelatorioGeralDocumentoRepository;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.HorasUteisCalculator;

@Service
public class RelatorioGeralDocumentoService {

	@Autowired private DocumentoService documentoService;

	@Autowired private RelatorioGeralDocumentoRepository relatorioGeralDocumentoRepository;

	public RelatorioGeralDocumento get(Long id) {
		return relatorioGeralDocumentoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(RelatorioGeralDocumento rg) throws MessageKeyException {
		relatorioGeralDocumentoRepository.saveOrUpdate(rg);
	}

	public List<RelatorioGeralDocumento> findByProcessosIds(List<Long> processosIds) {
		List<RelatorioGeralDocumento> list = relatorioGeralDocumentoRepository.findByProcessosIds(processosIds);
		return list;
	}


	public void criaRelatorioGeral(Processo processo, RelatorioGeral rg, Map<Date, RelatorioGeralDocumento> mapRelatorioDocumento, HorasUteisCalculator huc) {

		Long processoId = processo.getId();

		Set<RelatorioGeralDocumento> set = new LinkedHashSet<>();

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentoList = documentoService.findByFiltro(filtro, null, null);

		for (Documento documento : documentoList) {
			RelatorioGeralDocumento  rga = new RelatorioGeralDocumento();

			rga.setRelatorioGeral(rg);
			rga.setDocumento(documento);
			rga.setData(new Date());
			set.add(rga);

			set.add(rga);
		}

		for (RelatorioGeralDocumento rga2 : set) {
			relatorioGeralDocumentoRepository.saveOrUpdateWithoutFlush(rga2);
		}
	}

	public Map<Date, RelatorioGeralDocumento> findByProcessosIdsMap(List<Long> processosIds2) {

		List<RelatorioGeralDocumento> list = relatorioGeralDocumentoRepository.findByProcessosIds(processosIds2);
		Map<Date, RelatorioGeralDocumento> map = new HashMap<>();

		for (RelatorioGeralDocumento rga : list) {
			map.put(rga.getData(), rga);
		}

		return map;
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {
		return relatorioGeralDocumentoRepository.findIdsByFiltro(filtro);
	}

	public List<RelatorioGeralDocumento> findByIds(List<Long> ids) {
		return relatorioGeralDocumentoRepository.findByIds(ids);
	}
}
