package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.CampoAbstract;
import net.wasys.getdoc.domain.entity.GrupoAbstract;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "CampoResponse")
public class CampoResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "ID do Tipo Campo.")
    private Long tipoCampoId;

    @ApiModelProperty(value = "Nome.")
    private String nome;

    @ApiModelProperty(value = "Valor.")
    private String valor;

    @ApiModelProperty(value = "Tipo de entrada esperada para esse campo")
    private TipoEntradaCampo tipo;

    @ApiModelProperty(value = "Dica.")
    private String dica;

    @ApiModelProperty(value = "Indica a obrigatoriedade desse campo.")
    private boolean obrigatorio;

    @ApiModelProperty(value = "Ordem de exibição do campo.")
    private int ordem;

    @ApiModelProperty(value = "Quantidade máxima de carácteres que deve caber nesse campo.")
    private Integer tamanhoMaximo;

    @ApiModelProperty(value = "Quantidade mínima de carácteres que deve caber nesse campo.")
    private Integer tamanhoMinimo;

    @ApiModelProperty(value = "Opções disponíveis para ser utilizado como valor do campo.")
    private String opcoes;

    @ApiModelProperty(value = "Opções disponíveis para ser utilizado como valor do campo.")
    private List<String> opcoesList;

    @ApiModelProperty(value = "Indica se o campo pode ou não ser editável.")
    private boolean editavel;

    @ApiModelProperty(value = "Grupo ao qual o campo pertence.")
    private Long grupoId;

    @ApiModelProperty(value = "Grupo ao qual o campo pertence.")
    private Boolean abertoPadrao;

    @ApiModelProperty(value = "Opções disponíveis para ser utilizado como valor do campo valor da base interna.")
    private List<OpcoesBaseInternaResponse> opcoesBaseInternaList;

    @ApiModelProperty(value = "Opção ID Selecionado")
    private String opcaoId;

    @ApiModelProperty(value = "Base Interna ID")
    private Long baseInternaId;

    @ApiModelProperty(value = "Json de lista de campos pai relacionados a este campo.")
    private String pais;

    @ApiModelProperty(value = "Critérios para exibição deste campo.")
    private String criterioExibicao;

    @ApiModelProperty(value = "Critérios para filtro deste campo.")
    private String criterioFiltro;

    @ApiModelProperty(value = "Alerta de inconsistência do campo.")
    private String alerta;

    public CampoResponse(){}

    public CampoResponse(CampoAbstract c) {
        this.id = c.getId();
        this.tipoCampoId = c.getTipoCampoId();
        this.valor = c.getValor();
        this.nome = c.getNome();
        this.dica = c.getDica();
        this.obrigatorio = c.getObrigatorio();
        this.ordem = c.getOrdem();
        this.tamanhoMaximo = c.getTamanhoMaximo();
        this.tamanhoMinimo = c.getTamanhoMinimo();
        this.opcoesList = c.getOpcoesList();
        this.opcoes = c.getOpcoes();
        this.editavel = c.getEditavel();
        this.tipo = c.getTipo();
        this.pais = c.getPais();
        this.criterioExibicao = c.getCriterioExibicao();
        this.criterioFiltro = c.getCriterioFiltro();
        this.alerta = c.getAlerta();

        GrupoAbstract grupo = c.getGrupo();
        if (grupo != null) {
            this.grupoId = grupo.getId();
            this.abertoPadrao = grupo.getAbertoPadrao();
        }
        BaseInterna baseInterna = c.getBaseInterna();
        if (baseInterna != null) {
            this.baseInternaId = baseInterna.getId();
        }

        if(c.getOpcoesBaseInterna() != null) {
            this.opcoesBaseInternaList = new ArrayList<>();
            for(RegistroValorVO registroValorVO : c.getOpcoesBaseInterna()){
                String label = registroValorVO.getLabel();
                BaseRegistro baseRegistro = registroValorVO.getBaseRegistro();
                String chaveUnicidade = baseRegistro.getChaveUnicidade();
                this.opcoesBaseInternaList.add(new OpcoesBaseInternaResponse(chaveUnicidade, label));
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTipoCampoId() {
        return tipoCampoId;
    }

    public void setTipoCampoId(Long tipoCampoId) {
        this.tipoCampoId = tipoCampoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDica() {
        return dica;
    }

    public void setDica(String dica) {
        this.dica = dica;
    }

    public boolean isObrigatorio() {
        return obrigatorio;
    }

    public void setObrigatorio(boolean obrigatorio) {
        this.obrigatorio = obrigatorio;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public Integer getTamanhoMaximo() {
        return tamanhoMaximo;
    }

    public void setTamanhoMaximo(Integer tamanhoMaximo) {
        this.tamanhoMaximo = tamanhoMaximo;
    }

    public Integer getTamanhoMinimo() {
        return tamanhoMinimo;
    }

    public void setTamanhoMinimo(Integer tamanhoMinimo) {
        this.tamanhoMinimo = tamanhoMinimo;
    }

    public String getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(String opcoes) {
        this.opcoes = opcoes;
    }

    public List<String> getOpcoesList() {
        return opcoesList;
    }

    public void setOpcoesList(List<String> opcoesList) {
        this.opcoesList = opcoesList;
    }

    public boolean isEditavel() {
        return editavel;
    }

    public void setEditavel(boolean editavel) {
        this.editavel = editavel;
    }

    public TipoEntradaCampo getTipo() {
        return tipo;
    }

    public void setTipo(TipoEntradaCampo tipo) {
        this.tipo = tipo;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public List<OpcoesBaseInternaResponse> getOpcoesBaseInternaList() {
        return opcoesBaseInternaList;
    }

    public void setOpcoesBaseInternaList(List<OpcoesBaseInternaResponse> opcoesBaseInternaList) {
        this.opcoesBaseInternaList = opcoesBaseInternaList;
    }

    public String getOpcaoId() {
        return opcaoId;
    }

    public void setOpcaoId(String opcaoId) {
        this.opcaoId = opcaoId;
    }

    public Long getBaseInternaId() {
        return baseInternaId;
    }

    public void setBaseInternaId(Long baseInternaId) {
        this.baseInternaId = baseInternaId;
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

    public Boolean getAbertoPadrao() {
        return abertoPadrao;
    }

    public void setAbertoPadrao(Boolean abertoPadrao) {
        this.abertoPadrao = abertoPadrao;
    }

    public String getAlerta() { return alerta; }

    public void setAlerta(String alerta) { this.alerta = alerta; }
}