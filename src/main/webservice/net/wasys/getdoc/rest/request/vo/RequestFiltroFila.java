package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.rest.response.vo.SituacaoResponse;
import net.wasys.getdoc.rest.response.vo.StatusPrazoResponse;
import net.wasys.getdoc.rest.response.vo.StatusProcessoResponse;
import net.wasys.getdoc.rest.response.vo.TipoProcessoResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * VO que vai ser recebido via HTTPRequest, e será convertido em um ProcessoFiltro.
 *
 * @author jonas.baggio@wasys.com.br
 * @create 25 de jul de 2018 17:17:31
 */
@ApiModel(value = "RequestFiltroFila")
public class RequestFiltroFila extends SuperVo {

    @ApiModelProperty(notes = "ID do analista.")
    private Long analistaId;

    @ApiModelProperty(notes = "Texto para pesquisa.")
    private String texto;

    @ApiModelProperty(notes = "Tipos de processo.")
    private List<TipoProcessoResponse> tiposProcesso;

    @ApiModelProperty(notes = "SLAs / StatusPrazo.")
    private List<StatusPrazoResponse> slas;

    @ApiModelProperty(notes = "Email não lido.")
    private boolean emailNaoLido;

    @ApiModelProperty(notes = "Situações.")
    private List<SituacaoResponse> situacoes;

    @ApiModelProperty(notes = "Status.")
    private List<StatusProcessoResponse> status;

    @ApiModelProperty(notes = "regional.")
    private String regional;

    @ApiModelProperty(notes = "Lista de marcas.")
    private List<String> marcasList;

    @ApiModelProperty(notes = "Número da proposta.")
    private String numeroProposta;

    @ApiModelProperty(notes = "Número do Orçamento/Aprovação.")
    private String orcamentoAprovacao;

    @ApiModelProperty(notes = "Lista de tipos de venda.")
    private List<String> tipoVendaList;

    @ApiModelProperty(notes = "Lista de tipos de produtos.")
    private List<String> produtosList;

    @ApiModelProperty(notes = "Veículos zero KM.")
    private Boolean veiculoZeroKm;

    @ApiModelProperty(notes = "Veículos usados.")
    private Boolean veiculoUsado;

    @ApiModelProperty(notes = "Critério para data.")
    private ProcessoFiltro.ConsiderarData considerarData;

    @ApiModelProperty(notes = "Data de inicio.")
    private Date dataInicio;

    @ApiModelProperty(notes = "Data final.")
    private Date dataFim;

    public String getRegional() {
        return regional;
    }

    public void setRegional(String regional) {
        this.regional = regional;
    }

    public Long getAnalistaId() {
        return analistaId;
    }

    public void setAnalistaId(Long analistaId) {
        this.analistaId = analistaId;
    }

    public String getTexto() { return texto; }

    public void setTexto(String texto) { this.texto = texto; }

    public List<TipoProcessoResponse> getTiposProcesso() {
        return tiposProcesso;
    }

    public void setTiposProcesso(List<TipoProcessoResponse> tiposProcesso) {
        this.tiposProcesso = tiposProcesso;
    }

    public List<StatusPrazoResponse> getSlas() {
        return slas;
    }

    public void setSlas(List<StatusPrazoResponse> slas) {
        this.slas = slas;
    }

    public boolean isEmailNaoLido() {
        return emailNaoLido;
    }

    public void setEmailNaoLido(boolean emailNaoLido) {
        this.emailNaoLido = emailNaoLido;
    }

    public List<SituacaoResponse> getSituacoes() {
        return situacoes;
    }

    public void setSituacoes(List<SituacaoResponse> situacoes) {
        this.situacoes = situacoes;
    }

    public List<StatusProcessoResponse> getStatus() {
        return status;
    }

    public void setStatus(List<StatusProcessoResponse> status) {
        this.status = status;
    }

    public List<String> getMarcasList() { return marcasList; }

    public void setMarcasList(List<String> marcasList) { this.marcasList = marcasList; }

    public String getNumeroProposta() { return numeroProposta; }

    public void setNumeroProposta(String numeroProposta) { this.numeroProposta = numeroProposta; }

    public String getOrcamentoAprovacao() { return orcamentoAprovacao; }

    public void setOrcamentoAprovacao(String orcamentoAprovacao) { this.orcamentoAprovacao = orcamentoAprovacao; }

    public List<String> getTipoVendaList() { return tipoVendaList; }

    public void setTipoVendaList(List<String> tipoVendaList) { this.tipoVendaList = tipoVendaList; }

    public List<String> getProdutosList() { return produtosList; }

    public void setProdutosList(List<String> produtosList) { this.produtosList = produtosList; }

    public Boolean getVeiculoZeroKm() { return veiculoZeroKm; }

    public void setVeiculoZeroKm(Boolean veiculoZeroKm) { this.veiculoZeroKm = veiculoZeroKm; }

    public Boolean getVeiculoUsado() { return veiculoUsado; }

    public void setVeiculoUsado(Boolean veiculoUsado) { this.veiculoUsado = veiculoUsado; }

    public ProcessoFiltro.ConsiderarData getConsiderarData() { return considerarData; }

    public void setConsiderarData(ProcessoFiltro.ConsiderarData considerarData) { this.considerarData = considerarData; }

    public Date getDataInicio() { return dataInicio; }

    public void setDataInicio(Date dataInicio) { this.dataInicio = dataInicio; }

    public Date getDataFim() { return dataFim; }

    public void setDataFim(Date dataFim) { this.dataFim = dataFim; }

    public List<StatusProcesso> getStatusList(){
        if(this.status == null){
            return null;
        }

        List<StatusProcesso> list = new ArrayList<>();
        for(StatusProcessoResponse spr : this.status){
            list.add(spr.getId());
        }
        return list;
    }

    public List<TipoProcesso> getTiposProcessoList() {
        if(this.tiposProcesso == null){
            return null;
        }

        List<TipoProcesso> list = new ArrayList<>();
        for(TipoProcessoResponse tpr : this.tiposProcesso){
            TipoProcesso tP = new TipoProcesso();
            tP.setId(tpr.getId());
            list.add(tP);
        }
        return list;
    }

    public List<StatusPrazo> getStatusPrazoList() {
        if(this.slas == null){
            return null;
        }

        List<StatusPrazo> list = new ArrayList<>();
        for(StatusPrazoResponse spr : this.slas){
            list.add(spr.getId());
        }
        return list;
    }
}
