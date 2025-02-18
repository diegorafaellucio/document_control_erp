package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;

import java.util.Date;
import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestPesquisaProcesso")
public class RequestPesquisaProcesso extends SuperVo {

    @ApiModelProperty(notes = "Período a ser considerado.")
    private ProcessoFiltro.ConsiderarData considerarData;

    @ApiModelProperty(notes = "Data início.")
    private Date dataInicio;

    @ApiModelProperty(notes = "Data fim.")
    private Date dataFim;

    @ApiModelProperty(notes = "Número.")
    private String numero;

    @ApiModelProperty(notes = "ID do analista.")
    private Long analista;

    @ApiModelProperty(notes = "Lista de ID situações.")
    private List<Long> situacao;

    @ApiModelProperty(notes = "Lista de ID tipos de processo.")
    private List<Long> tipoProcesso;

    @ApiModelProperty(notes = "CPF/CNPJ.")
    private String cpfOuCnpj;

    @ApiModelProperty(notes = "Nome do cliente.")
    private String nomeCliente;

    @ApiModelProperty(notes = "Texto logs/solicitacoes/imagens")
    private String texto;

    @ApiModelProperty(notes = "Lista de status")
    private List<StatusProcesso> statusProcesso;

    @ApiModelProperty(notes = "Email ainda não lido")
    private boolean emailNaoLido;

    @ApiModelProperty(notes = "Regional")
    private String regional;

    public ProcessoFiltro.ConsiderarData getConsiderarData() {
        return considerarData;
    }

    public void setConsiderarData(ProcessoFiltro.ConsiderarData considerarData) {
        this.considerarData = considerarData;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public List<Long> getSituacao() {
        return situacao;
    }

    public void setSituacao(List<Long> situacao) {
        this.situacao = situacao;
    }

    public List<Long> getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(List<Long> tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }

    public String getCpfOuCnpj() {
        return cpfOuCnpj;
    }

    public void setCpfOuCnpj(String cpfOuCnpj) {
        this.cpfOuCnpj = cpfOuCnpj;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public List<StatusProcesso> getStatusProcesso() {
        return statusProcesso;
    }

    public void setStatusProcesso(List<StatusProcesso> statusProcesso) {
        this.statusProcesso = statusProcesso;
    }

    public boolean isEmailNaoLido() {
        return emailNaoLido;
    }

    public void setEmailNaoLido(boolean emailNaoLido) {
        this.emailNaoLido = emailNaoLido;
    }

    public Long getAnalista() {
        return analista;
    }

    public void setAnalista(Long analista) {
        this.analista = analista;
    }

    public String getRegional() {
        return regional;
    }

    public void setRegional(String regional) {
        this.regional = regional;
    }
}
