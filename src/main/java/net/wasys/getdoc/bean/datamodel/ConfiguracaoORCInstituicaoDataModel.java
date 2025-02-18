package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.CampoModeloOcr;
import net.wasys.getdoc.domain.entity.ConfiguracaoOCR;
import net.wasys.getdoc.domain.entity.ConfiguracaoOCRInstituicao;
import net.wasys.getdoc.domain.service.CampoModeloOcrService;
import net.wasys.getdoc.domain.service.ConfiguracaoOCRInstituicaoService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class ConfiguracaoORCInstituicaoDataModel extends LazyDataModel<ConfiguracaoOCRInstituicao> {

	private ConfiguracaoOCRInstituicaoService configuracaoOCRInstituicaoService;
	private List<ConfiguracaoOCRInstituicao> list;
	private Long configuracaoOCRId;


	@Override
	public List<ConfiguracaoOCRInstituicao> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		list = configuracaoOCRInstituicaoService.findByConfiguracaoOCR(configuracaoOCRId);
		return list;
	}

	@Override
	public int getRowCount() {

		int count = configuracaoOCRInstituicaoService.countByConfiguracaoOCR(configuracaoOCRId);
		return count;
	}

	public void setService(ConfiguracaoOCRInstituicaoService configuracaoOCRInstituicaoService) {
		this.configuracaoOCRInstituicaoService = configuracaoOCRInstituicaoService;
	}

	public void setConfiguracaoOCRId(Long configuracaoOCRId) {
		this.configuracaoOCRId = configuracaoOCRId;
	}
}
