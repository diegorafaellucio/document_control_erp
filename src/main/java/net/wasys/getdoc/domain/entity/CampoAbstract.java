package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CampoAbstract extends net.wasys.util.ddd.Entity implements Cloneable {

	private Long id;
	private String nome;
	private Integer tamanhoMinimo;
	private Integer tamanhoMaximo;
	private boolean obrigatorio;
	private Integer ordem;
	private TipoEntradaCampo tipo;
	private String opcoes;
	private String valor;
	private String dica;
	private Boolean editavel = true;
	private Boolean defineUnicidade = false;
	private GrupoAbstract grupo;
	private String opcaoId;
	private String pais;
	private String criterioExibicao;
	private String criterioFiltro;
	private String alerta;

	private List<RegistroValorVO> opcoesBaseInterna;
	private BaseInterna baseInterna;

	public GrupoAbstract getGrupo() {
		return grupo;
	}

	@Transient
	public abstract Long getTipoCampoId();

	public void setGrupo(GrupoAbstract grupo) {
		this.grupo = grupo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = StringUtils.upperCase(nome);
	}

	public Integer getTamanhoMinimo() {
		return tamanhoMinimo;
	}

	public void setTamanhoMinimo(Integer tamanhoMinimo) {
		this.tamanhoMinimo = tamanhoMinimo;
	}

	public Integer getTamanhoMaximo() {
		return tamanhoMaximo;
	}

	public void setTamanhoMaximo(Integer tamanhoMaximo) {
		this.tamanhoMaximo = tamanhoMaximo;
	}

	public boolean getObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public String getDica() {
		return dica;
	}

	public void setDica(String dica) {
		this.dica = dica;
	}

	public TipoEntradaCampo getTipo() {
		return tipo;
	}

	public void setTipo(TipoEntradaCampo tipo) {
		this.tipo = tipo;
	}

	public String getOpcoes() {
		return opcoes;
	}

	public void setOpcoes(String opcoes) {
		this.opcoes = opcoes;
		if(opcoes != null) {
			List<String> opcoesList = getOpcoesList();
			opcoes = opcoesList.toString();
			opcoes = opcoes.replaceAll("^\\[", "");
			opcoes = opcoes.replaceAll("\\]$", "");
			opcoes = opcoes.replaceAll(", ", ",");
			this.opcoes = opcoes;
		}
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@Transient
	public List<String> getOpcoesList() {

		String opcoes = getOpcoes();
		if(StringUtils.isBlank(opcoes)) {
			return null;
		}

		List<String> list = new ArrayList<>();
		String[] opcoesArray = opcoes.split(",");
		for (String opcao : opcoesArray) {

			opcao = StringUtils.trim(opcao);
			list.add(opcao);
		}

		return list;
	}

	public Boolean getEditavel() {
		return editavel;
	}

	public void setEditavel(Boolean editavel) {
		this.editavel = editavel;
	}

	public Boolean getDefineUnicidade() {
		return defineUnicidade;
	}

	public void setDefineUnicidade(Boolean defineUnicidade) {
		this.defineUnicidade = defineUnicidade;
	}

	public CampoAbstract clone() {
		try {
			return (CampoAbstract) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isObrigatorio() {
		return obrigatorio;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getCriterioExibicao() {
		return criterioExibicao;
	}

	public void setCriterioExibicao(String criterioExibicao) {
		this.criterioExibicao = criterioExibicao;
	}

	public String getCriterioFiltro() {
		return criterioFiltro;
	}

	public void setCriterioFiltro(String criterioFiltro) {
		this.criterioFiltro = criterioFiltro;
	}

	public List<RegistroValorVO> getOpcoesBaseInterna() {
		return opcoesBaseInterna;
	}

	public void setOpcoesBaseInterna(List<RegistroValorVO> opcoesBaseInterna) {
		this.opcoesBaseInterna = opcoesBaseInterna;
	}

	public String getOpcaoId() {
		return opcaoId;
	}

	public void setOpcaoId(String opcaoId) {
		this.opcaoId = opcaoId;
	}

	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public void setBaseInterna(BaseInterna baseInterna) {
		this.baseInterna = baseInterna;
	}

	@Transient
	public List<CampoPai> getPaisObject() {
		String pais = getPais();
		if(StringUtils.isBlank(pais)) {
			return new ArrayList<>();
		}
		ObjectMapper objectMapper = new ObjectMapper();
		CampoPai[] camposPai = objectMapper.readValue(pais, CampoPai[].class);
		return new ArrayList<>(Arrays.asList(camposPai));
	}

	@Transient
	public void setPaisObject(List<CampoPai> filhoObject) {
		if(filhoObject == null) {
			this.pais = null;
		} else {
			ObjectMapper objectMapper = new ObjectMapper();
			this.pais = objectMapper.writeValueAsString(filhoObject);
		}
	}

	@Transient
	public String getAlerta() {
		return this.alerta;
	}

	public void setAlerta(String alerta) {
		this.alerta = alerta;
	}

	@Transient
	public String getValorLabel() {
		String valorLabel = getValor();
		valorLabel = valorLabel != null ? valorLabel : "";
		if(StringUtils.isNotBlank(valorLabel) && TipoEntradaCampo.COMBO_BOX_ID.equals(tipo)) {
			List<RegistroValorVO> opcoes = getOpcoesBaseInterna();
			for (RegistroValorVO vo : opcoes) {
				BaseRegistro baseRegistro = vo.getBaseRegistro();
				String chaveUnicidade = baseRegistro.getChaveUnicidade();
				if(valorLabel.equals(chaveUnicidade)) {
					String labelStr = vo.getLabel();
					valorLabel = labelStr;
				}
			}
		}
		return valorLabel;
	}

	@Transient
	public String getChaveFonte() {
		GrupoAbstract grupo = getGrupo();
		String grupoNome = grupo.getNome();
		String campoNome = getNome();
		return "['" + grupoNome + "']['" + campoNome + "']";
	}

	@Override
	public String toString() {
		GrupoAbstract grupo = getGrupo();
		String grupoNome = grupo != null ? grupo.getNome() : "";
		return getClass().getSimpleName() + "#" + getId() + "[" + grupoNome + "][" + getNome() + "]={" + getValor() + "}";
	}

	public static class CampoPai {
		private Long paiId;
		private String nome;
		public Long getPaiId() { return paiId; }
		public void setPaiId(Long paiId) { this.paiId = paiId; }
		public String getNome() { return nome; }
		public void setNome(String nome) { this.nome = nome; }
	}
}
