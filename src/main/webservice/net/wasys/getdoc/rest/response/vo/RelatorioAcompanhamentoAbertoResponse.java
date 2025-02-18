package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import net.wasys.getdoc.domain.vo.RelatorioAcompanhamentoVO;

@ApiModel(value = "RelatorioAcompanhamentoAbertoResponse")
public class RelatorioAcompanhamentoAbertoResponse extends RelatorioAcompanhamentoResponse {

    public RelatorioAcompanhamentoAbertoResponse(){
        super();
    }

    public RelatorioAcompanhamentoAbertoResponse(RelatorioAcompanhamentoVO rVo) {
       super(rVo);
    }
}