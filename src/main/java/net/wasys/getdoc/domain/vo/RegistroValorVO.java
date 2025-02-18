package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.BaseRegistroValor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.wasys.util.DummyUtils.*;

public class RegistroValorVO implements Serializable {

	private Long registroId;
	private BaseRegistro baseRegistro;
	private Map<String, BaseRegistroValor> mapColunaRegistroValor = new LinkedHashMap<>();

	public Map<String, BaseRegistroValor> getMapColunaRegistroValor() {
		return mapColunaRegistroValor;
	}

	public void setMapColunaRegistroValor(Map<String, BaseRegistroValor> mapColunaRegistroValor) {
		this.mapColunaRegistroValor = mapColunaRegistroValor;
	}

	public Long getRegistroId() {
		return registroId;
	}

	public void setRegistroId(Long registroId) {
		this.registroId = registroId;
	}

	public BaseRegistro getBaseRegistro() {
		return baseRegistro;
	}

	public void setBaseRegistro(BaseRegistro baseRegistro) {
		this.baseRegistro = baseRegistro;
	}

	public String getLabel() {
		BaseInterna baseInterna = baseRegistro.getBaseInterna();
		String colunaLabel = baseInterna.getColunaLabel();
		BaseRegistroValor label = getMapColunaRegistroValor().get(colunaLabel);
 		return label != null ? label.getValor() : getValorLabelFormatada(colunaLabel);
	}

	public String getLabelWithChaveUnicidade() {
		String label = getLabel();
		String chaveUnicidade = getChaveUnicidade();
		return label.concat(" - (").concat(chaveUnicidade).concat(")");
	}

	public String getChaveUnicidade() {

		String chaveUnicidade = baseRegistro.getChaveUnicidade();
		return chaveUnicidade == null ? null : limparCharsChaveUnicidade(chaveUnicidade);
	}

	public String getValor(String nomeColuna) {
		Map<String, BaseRegistroValor> map = getMapColunaRegistroValor();
		BaseRegistroValor brv = map.get(nomeColuna);
		return brv != null ? brv.getValor() : null;
	}

	private String getValorLabelFormatada(String colunaLabel){
		String label = "";
		for (Map.Entry<String, BaseRegistroValor> entry : mapColunaRegistroValor.entrySet()){
			if( colunaLabel.contains(entry.getKey()) ) {
				String valor = entry.getValue().getValor();
				colunaLabel = colunaLabel.replace(entry.getKey(), valor);
				label = colunaLabel;
			}
		}
		return label;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + getRegistroId() + getMapColunaRegistroValor();
	}
}
