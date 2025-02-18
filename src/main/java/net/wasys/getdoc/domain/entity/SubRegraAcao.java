package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.SubRegraAcoes;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name="SUB_REGRA_ACAO")
public class SubRegraAcao extends net.wasys.util.ddd.Entity {

	private Long id;
	private SubRegraAcoes acao;
	private StatusDocumento statusDocumento;

	private SubRegra subRegra;
	private String tipoDocumentoIds;
	private String tipoGrupoIds;
	private Boolean todosDocumentosAprovados;
	private List<Long> tipoDocumentosIds;
	private List<Long> tipoGruposIds;
	private Boolean obrigatoriedadeDocumento;
	private String novoValorCampo;
	private TipoCampo tipoCampo;
	private Boolean obrigatoriedadeCampo;
	private List<Long> tipoCampoIdList;
	private String tipoCampoObrigatorioIds;

	@Id
	@Column(name="id", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Enumerated(EnumType.STRING)
	@Column(name="ACAO", nullable=false)
	public SubRegraAcoes getAcao() {
		return acao;
	}

	public void setAcao(SubRegraAcoes acao) {
		this.acao = acao;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_DOCUMENTO")
	public StatusDocumento getStatusDocumento() {
		return statusDocumento;
	}

	public void setStatusDocumento(StatusDocumento statusDocumento) {
		this.statusDocumento = statusDocumento;
	}

	@Column(name="TODOS_DOCUMENTOS_APROVADOS")
	public Boolean getTodosDocumentosAprovados() {
		return todosDocumentosAprovados;
	}

	public void setTodosDocumentosAprovados(Boolean todosDocumentosAprovados) {
		this.todosDocumentosAprovados = todosDocumentosAprovados;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUB_REGRA_ID", nullable=false)
	public SubRegra getSubRegra() {
		return subRegra;
	}

	public void setSubRegra(SubRegra subRegra) {
		this.subRegra = subRegra;
	}

	@Column(name="TIPO_DOCUMENTO_IDS")
	public String getTipoDocumentoIds() {
		return tipoDocumentoIds;
	}

	public void setTipoDocumentoIds(String tipoDocumentoIds) {
		this.tipoDocumentoIds = tipoDocumentoIds;
	}

	@Column(name="TIPO_GRUPO_IDS")
	public String getTipoGrupoIds() {
		return tipoGrupoIds;
	}

	public void setTipoGrupoIds(String tipoGrupoIds) {
		this.tipoGrupoIds = tipoGrupoIds;
	}

	@Column(name="OBRIGATORIEDADE_DOCUMENTO")
	public Boolean getObrigatoriedadeDocumento() {
		return obrigatoriedadeDocumento;
	}

	public void setObrigatoriedadeDocumento(Boolean obrigatoriedadeDocumento) {
		this.obrigatoriedadeDocumento = obrigatoriedadeDocumento;
	}

	@Column(name = "NOVO_VALOR_CAMPO")
	public String getNovoValorCampo() {
		return novoValorCampo;
	}

	public void setNovoValorCampo(String novoValorCampo) {
		this.novoValorCampo = novoValorCampo;
	}

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="TIPO_CAMPO_ID")
	public TipoCampo getTipoCampo() {
		return tipoCampo;
	}

	public void setTipoCampo(TipoCampo tipoCampo) {
		this.tipoCampo = tipoCampo;
	}


	@Transient
	public List<Long> getTipoDocumentosIds() {
		List<Long> ids = new ArrayList<>();
		if(StringUtils.isNotBlank(this.tipoDocumentoIds)) {
 			String[] split = this.tipoDocumentoIds.split(",");
			for (int i = 0; i < split.length; i++) {
				String s = split[i].replaceAll("\\[","").replaceAll("]", "").replaceAll("\"","");
				ids.add(Long.parseLong(s));
			}
		}
		return ids;
	}

	@Transient
	public void setTipoDocumentosIds(List<Long> tipoDocumentosIds) {
		this.tipoDocumentosIds = tipoDocumentosIds;
		this.tipoDocumentoIds = DummyUtils.objectToJson(this.tipoDocumentosIds);
	}

	@Transient
	public List<Long> getTipoGruposIds() {
		List<Long> ids = new ArrayList<>();
		if(StringUtils.isNotBlank(this.tipoGrupoIds)) {
 			String[] split = this.tipoGrupoIds.split(",");
			for (int i = 0; i < split.length; i++) {
				String s = split[i].replaceAll("\\[","").replaceAll("]", "").replaceAll("\"","");
				ids.add(Long.parseLong(s));
			}
		}
		return ids;
	}

	@Transient
	public void setTipoGruposIds(List<Long> tipoGruposIds) {
		this.tipoGruposIds = tipoGruposIds;
		this.tipoGrupoIds = DummyUtils.objectToJson(this.tipoGruposIds);
	}

	@Transient
	public void setTipoCampoIdList(List<Long> tipoCampoIdList) {
		this.tipoCampoIdList = tipoCampoIdList;
		this.tipoCampoObrigatorioIds = DummyUtils.objectToJson(this.tipoCampoIdList);
	}

	@Transient
	public List<Long> getTipoCampoIdList() {
		List<Long> ids = new ArrayList<>();
		if(StringUtils.isNotBlank(this.tipoCampoObrigatorioIds) && !this.tipoCampoObrigatorioIds.equals("[]")) {
			String[] split = this.tipoCampoObrigatorioIds.split(",");
			for (int i = 0; i < split.length; i++) {
				String s = split[i].replaceAll("\\[","").replaceAll("]", "").replaceAll("\"","");
				ids.add(Long.parseLong(s));
			}
		}
		return ids;
	}

	@Column(name="OBRIGATORIEDADE_CAMPO")
	public Boolean getObrigatoriedadeCampo() {
		return obrigatoriedadeCampo;
	}

	public void setObrigatoriedadeCampo(Boolean obrigatoriedadeCampo) {
		this.obrigatoriedadeCampo = obrigatoriedadeCampo;
	}

	@Column(name = "TIPO_CAMPO_OBRIGATORIO_IDS")
	public String getTipoCampoObrigatorioIds() {
		return tipoCampoObrigatorioIds;
	}

	public void setTipoCampoObrigatorioIds(String tipoCampoIds) {
		this.tipoCampoObrigatorioIds = tipoCampoIds;
	}
}