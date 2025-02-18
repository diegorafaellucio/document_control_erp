package net.wasys.getdoc.rest.response.vo;


import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;

public class ImagenDocumentoResponse extends SuperVo {

    private Long documentoId;
    private String documentoNome;
    private List<Long> imagensId;

    public Long getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(Long documentoId) {
        this.documentoId = documentoId;
    }

    public String getDocumentoNome() {
        return documentoNome;
    }

    public void setDocumentoNome(String documentoNome) {
        this.documentoNome = documentoNome;
    }

    public List<Long> getImagensId() {
        return imagensId;
    }

    public void setImagensId(List<Long> imagensId) {
        this.imagensId = imagensId;
    }
}
