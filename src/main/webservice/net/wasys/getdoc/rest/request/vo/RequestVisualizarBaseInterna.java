package net.wasys.getdoc.rest.request.vo;

import java.util.Map;

public class RequestVisualizarBaseInterna extends SuperVo {

    private Map<String, String> busca;

    public Map<String, String> getBusca() {
        return busca;
    }

    public void setBusca(Map<String, String> busca) {
        this.busca = busca;
    }
}
