package net.wasys.getdoc.rest.response.vo;

import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;
import java.util.Map;

public class SegurosResponse extends SuperVo {

    private Map<String, String> seguroResponse;

    private List<Map<String, String>> precosVigentes;

    public Map<String, String> getSeguroResponse() { return seguroResponse; }

    public void setSeguroResponse(Map<String, String> seguroResponse) { this.seguroResponse = seguroResponse; }

    public List<Map<String, String>> getPrecosVigentes() { return precosVigentes; }

    public void setPrecosVigentes(List<Map<String, String>> precosVigentes) { this.precosVigentes = precosVigentes; }
}
