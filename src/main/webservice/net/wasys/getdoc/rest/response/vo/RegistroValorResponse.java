package net.wasys.getdoc.rest.response.vo;

import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;

public class RegistroValorResponse extends SuperVo {

    private List<String> colunas;
    private int count;
    private List<RegistroValorVO> dados;
    private List<String> colunasUnicidade;

    public List<String> getColunas() {
        return colunas;
    }

    public void setColunas(List<String> colunas) {
        this.colunas = colunas;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<RegistroValorVO> getDados() {
        return dados;
    }

    public void setDados(List<RegistroValorVO> dados) {
        this.dados = dados;
    }

    public List<String> getColunasUnicidade() {
        return colunasUnicidade;
    }

    public void setColunasUnicidade(List<String> colunasUnicidade) {
        this.colunasUnicidade = colunasUnicidade;
    }
}
