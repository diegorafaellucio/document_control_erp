package net.wasys.getdoc.rest.response.vo;


import net.wasys.getdoc.rest.request.vo.SuperVo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;

public class DownloadAnexoResponse extends SuperVo {

    private InputStreamResource isr;
    private HttpHeaders respHeaders;

    public InputStreamResource getIsr() {
        return isr;
    }

    public void setIsr(InputStreamResource isr) {
        this.isr = isr;
    }

    public HttpHeaders getRespHeaders() {
        return respHeaders;
    }

    public void setRespHeaders(HttpHeaders respHeaders) {
        this.respHeaders = respHeaders;
    }
}