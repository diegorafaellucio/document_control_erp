package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 *
 * VO que vai ser recebido via HTTPRequest, e será convertido em um ProcessoFiltro.
 *
 * @author jonas.baggio@wasys.com.br
 * @create 25 de jul de 2018 17:17:31
 */
@ApiModel(value = "RequestFiltroFila")
public class RequestGerarProtocoloPac extends SuperVo {

    @ApiModelProperty(notes = "IDs dos processos.")
    private List<Long> processosIds;

    @ApiModelProperty(notes = "Código do PAC.")
    private String codigoPac;

    public List<Long> getProcessosIds() { return processosIds; }

    public void setProcessosIds(List<Long> processosIds) { this.processosIds = processosIds; }

    public String getCodigoPac() { return codigoPac; }

    public void setCodigoPac(String codigoPac) { this.codigoPac = codigoPac; }
}
