package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.TipoCampo;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CamposFiltro {

	private List<TipoProcesso> tiposProcesso;
	private List<CampoDinamicoVO> camposFiltro;

	public List<TipoProcesso> getTiposProcesso() {
		return tiposProcesso;
	}

	public void setTiposProcesso(List<TipoProcesso> tiposProcesso) {
		this.tiposProcesso = tiposProcesso;
	}

	public List<CampoDinamicoVO> getCamposFiltro() {
		return camposFiltro;
	}

	public void setCamposFiltro(CampoMap.CampoEnum campo, List<String> chaves) {
		List<String> chavesUnicidades = new ArrayList<>(chaves);
		for(String chaveUnicicidade : chavesUnicidades){
			if(chaveUnicicidade == null)
				return;
		}
		CampoDinamicoVO campoDinamicoVO = new CampoDinamicoVO(campo, null);
		campoDinamicoVO.setChavesUnicidade(chavesUnicidades);
		camposFiltro = camposFiltro != null ? camposFiltro : new ArrayList<>();
		camposFiltro.add(campoDinamicoVO);
	}

	public void setCamposFiltro(List<CampoDinamicoVO> camposFiltro) {
		this.camposFiltro = camposFiltro;
	}

	public void addCamposFiltro(TipoCampo tc, List<String> chavesUnicidade) {
		CampoDinamicoVO campoDinamicoVO = new CampoDinamicoVO(tc.getNome(), tc.getValor(), tc.getGrupo().getNome());
		campoDinamicoVO.setChavesUnicidade(chavesUnicidade);
		camposFiltro = camposFiltro != null ? camposFiltro : new ArrayList<>();
		camposFiltro.add(campoDinamicoVO);
	}

	public void configuraTipoProcessoCampos(TipoProcesso tipoProcessoCampos, BaseRegistroService baseRegistroService) {

		setCamposFiltro(null);
		setTiposProcesso(Arrays.asList(tipoProcessoCampos));

		Set<TipoCampoGrupo> tipoCampoGrupos = tipoProcessoCampos.getTipoCampoGrupo();
		for (TipoCampoGrupo tcg : tipoCampoGrupos) {

			//Faz filtro na lista de campos e apenas itera os campos com valor preenchido
			List<TipoCampo> result = tcg.getCampos().stream()
					.filter(tipoCampo -> org.apache.commons.lang3.StringUtils.isNotBlank(tipoCampo.getValor()))
					.collect(Collectors.toList());

			for (TipoCampo tc : result) {
				List<String> chavesUnicidade = baseRegistroService.findChaveUnicidadeByTipoCampo(tc.getId(), tc.getValor());
				if(chavesUnicidade != null && chavesUnicidade.size() > 0){
					addCamposFiltro(tc, chavesUnicidade);
				}
				else{
					addCamposFiltro(tc, null);
				}
			}
		}
	}
}
