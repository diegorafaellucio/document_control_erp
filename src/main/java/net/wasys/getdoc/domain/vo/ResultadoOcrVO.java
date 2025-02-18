package net.wasys.getdoc.domain.vo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.entity.CampoOcr;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.entity.LogOcr;

public class ResultadoOcrVO {

	private Map<LogOcr, Imagem> imagemMap = new LinkedHashMap<>();
	private Map<LogOcr, List<CampoOcr>> camposMap = new LinkedHashMap<>();

	public void put(LogOcr logOcr, Imagem imagem) {
		imagemMap.put(logOcr, imagem);
	}

	public void put(LogOcr logOcr, List<CampoOcr> campos) {
		camposMap.put(logOcr, campos);
	}

	public List<CampoOcr> getCampos(LogOcr logOcr) {
		return camposMap.get(logOcr);
	}
}
