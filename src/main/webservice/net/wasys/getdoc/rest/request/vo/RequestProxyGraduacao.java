package net.wasys.getdoc.rest.request.vo;

public class RequestProxyGraduacao {

    private Long processoId;
    private Long tipoDocumentoId;
    private DadosUploadDocumentoCaptacaoRequestVO[] arquivos;

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public Long getTipoDocumentoId() {
        return tipoDocumentoId;
    }

    public void setTipoDocumentoId(Long tipoDocumentoId) {
        this.tipoDocumentoId = tipoDocumentoId;
    }

    public DadosUploadDocumentoCaptacaoRequestVO[] getArquivos() {
        return arquivos;
    }

    public void setArquivos(DadosUploadDocumentoCaptacaoRequestVO[] arquivos) {
        this.arquivos = arquivos;
    }

    public static class DadosUploadDocumentoCaptacaoRequestVO {

        private String nomeArquivo;
        private String path;

        public String getNomeArquivo() {
            return nomeArquivo;
        }

        public void setNomeArquivo(String nomeArquivo) {
            this.nomeArquivo = nomeArquivo;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
