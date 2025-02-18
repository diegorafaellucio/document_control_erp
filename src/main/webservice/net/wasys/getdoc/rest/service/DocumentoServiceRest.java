package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.DigitalizacaoVO;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.getdoc.mb.enumerator.DeviceSO;
import net.wasys.getdoc.rest.exception.AnexoRestException;
import net.wasys.getdoc.rest.exception.DocumentoRestException;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.request.RequestJustificarDocumento;
import net.wasys.getdoc.rest.request.vo.RequestImagensDocumento;
import net.wasys.getdoc.rest.request.vo.UploadArquivo;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class DocumentoServiceRest extends SuperServiceRest {

    @Autowired private UploadMultipartServiceRest uploadMultipartServiceRest;
    @Autowired private ProcessoService processoService;
    @Autowired private DocumentoService documentoService;
    @Autowired private ImagemService imagemService;
    @Autowired private DownloadFileService downloadFileService;
    @Autowired private ImagemMetaService imagemMetaService;



    public Set<String> passo1UploadDocumento(Usuario usuario, Long documentoId, MultipartFile[] multipartFiles) throws MessageKeyException {

        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Documento documento = documentoService.get(documentoId);
        if (documento == null) {
            throw new DocumentoRestException("documento.nao.localizado.com.id", documentoId);
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, documento.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_DOCUMENTO, multipartFiles);
        return anexos;
    }

    public DocumentoUploadResponse passo2ConfirmaArquivos(Usuario usuario, Long documentoId, List<String> keys, Origem origem) throws Exception {

        if (keys == null || keys.size() <= 0) {
            throw new DocumentoRestException("documento.keys.null");
        }

        Documento documento = documentoService.get(documentoId);
        if (documento == null) {
            throw new DocumentoRestException("documento.nao.localizado.com.id", documentoId);
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, documento.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_DOCUMENTO);

        Processo processo = documento.getProcesso();

        Map<String, UploadArquivo> mapChecksumFilePath = uploadMultipartServiceRest.get(key);
        if (mapChecksumFilePath == null || mapChecksumFilePath.keySet() == null || mapChecksumFilePath.keySet().size() <= 0) {
            throw new DocumentoRestException("documento.mapChecksumFilePath.null", documentoId, documento.getNome());
        }

        /**
         * Objeto retorno ainda não processou nenhuma imagem, mantém como null...
         */
        Map<String, String> mapStatusUpload = new HashMap<>();
        keys.forEach(checksum -> {
            mapStatusUpload.put(checksum, null);
        });

        DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
        digitalizacaoVO.setDocumento(documento);
        digitalizacaoVO.setProcesso(processo);
        digitalizacaoVO.setOrigem(origem);

        Set<String> listCheckSum = mapChecksumFilePath.keySet();
        for (String keyChecksum : listCheckSum) {
            UploadArquivo uploadArquivo = mapChecksumFilePath.get(keyChecksum);

            File tmpFile = FileUtils.getFile(uploadArquivo.getPath());
            if (!tmpFile.exists()) {
                throw new DocumentoRestException("documento.file.null", tmpFile.getAbsolutePath());
            }
            DummyUtils.deleteOnExitFile(tmpFile);
            digitalizacaoVO.addAnexo(uploadArquivo.getNome(), tmpFile);

            //se chegou até aqui, atualiza o status com sucesso.
            mapStatusUpload.put(keyChecksum, uploadArquivo.getNome());
        }

        processoService.digitalizarImagens(usuario, digitalizacaoVO);

        //Após ter digitalizado, recupera o status atual desse documento.
        documento = documentoService.get(documento.getId());

        DocumentoUploadResponse documentoResponse = new DocumentoUploadResponse();
        documentoResponse.setId(documento.getId());
        documentoResponse.setNome(documento.getNome());
        documentoResponse.setStatus(documento.getStatus());
        documentoResponse.setMapImagens(mapStatusUpload);

        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.

        return documentoResponse;
    }

    public List<DocumentoExcluidoResponse> getDocumentosExcluidos(Usuario usuario, Long processoId, String imagePath) {
        List<DocumentoVO> documentos = documentoService.findVOsByProcesso(processoId, usuario, imagePath);

        Map<Long, DocumentoVO> documentosMap = new LinkedHashMap<Long, DocumentoVO>();
        ArrayList<DocumentoVO> documentosExcluidos = new ArrayList<>();

        for (DocumentoVO vo : documentos) {
            Long documentoId = vo.getId();
            documentosMap.put(documentoId, vo);

            StatusDocumento status = vo.getStatus();
            if (StatusDocumento.EXCLUIDO.equals(status)) {
                documentosExcluidos.add(vo);
            }
//            else {
//                String nome = vo.getNome();
//                if ("TIPIFICANDO...".equals(nome)) {
//
//                }
//            }
        }

        List<DocumentoExcluidoResponse> list = null;

        if(documentosExcluidos != null && documentosExcluidos.size() > 0){
            list = new ArrayList<>();
            for(DocumentoVO docVo : documentosExcluidos){
                list.add(new DocumentoExcluidoResponse(docVo));
            }
        }
        return list;
    }


    /**
     * Realiza o download de uma imagem específica.
     * @param usuario
     * @param imagenId
     * @return
     */
    public DownloadAnexoResponse download(Usuario usuario, Long imagenId) {

        Imagem imagem = imagemService.get(imagenId);
        ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagenId);

        if(imagem == null){
            throw new DocumentoRestException("documento.imagem.nao.localizada", imagenId);
        }

        File file = new File(imagem.getCaminho());
        if(!file.exists()){
            throw new DocumentoRestException("documento.imagem.nao.localizada.disco", imagem.getId(), imagem.getDocumento().getNome());
        }

        ArquivoDownload arquivoDownload = new ArquivoDownload();
        arquivoDownload.setId(imagem.getId());
        arquivoDownload.setPath(imagem.getCaminho());
        arquivoDownload.setNome(imagem.getDocumento().getNome());
        arquivoDownload.setExtensao(imagem.getExtensao());
        arquivoDownload.setTamanho(imagemMeta.getTamanho());

        DownloadAnexoResponse download = downloadFileService.download(arquivoDownload);
        if(download == null){
            throw new AnexoRestException("anexo.response.null");
        }
        return download;
    }

    /**
     * Recupera os ID das imagens do documento.
     * @param usuario
     * @param documentoId
     * @return
     */
    public ImagenDocumentoResponse getImagens(Usuario usuario, Long documentoId) {
        Documento documento = documentoService.get(documentoId);
        if (documento == null) {
            throw new DocumentoRestException("documento.nao.localizado.com.id", documentoId);
        }

        List<Imagem> byDocumento = imagemService.findByDocumentoVersao(documento.getId(), documento.getVersaoAtual());
        ImagenDocumentoResponse imagenDocumentoResponse = new ImagenDocumentoResponse();
        imagenDocumentoResponse.setDocumentoId(documento.getId());
        imagenDocumentoResponse.setDocumentoNome(documento.getNome());

        if(byDocumento != null && byDocumento.size() > 0) {
            List<Long> listImgId = new ArrayList<>();
            for (Imagem imagem : byDocumento) {
                listImgId.add(imagem.getId());
            }
            imagenDocumentoResponse.setImagensId(listImgId);
        }

        return imagenDocumentoResponse;
    }

    public boolean passo2MobileConfirmaArquivos(Usuario usuario, Long documentoId, RequestImagensDocumento requestImagensDocumento) throws Exception {
        Documento documento = documentoService.get(documentoId);
        if (documento == null) {
            throw new DocumentoRestException("documento.nao.localizado.com.id", documentoId);
        }
        Origem origemMobile = getOrigemMobileUsuario(usuario);

        List<String> listHash = requestImagensDocumento.getListHash();
        if(listHash != null && listHash.size() > 0) {
            passo2ConfirmaArquivos(usuario, documentoId, listHash, origemMobile);
        }

        List<Long> excluir = requestImagensDocumento.getExcluir();
        if(excluir != null && excluir.size() > 0){
            List<Imagem> byIds = imagemService.findByIds(excluir);
            if(byIds != null && byIds.size() > 0){
                for (Imagem byId : byIds) {
                    if(byId != null) {
                        documentoService.excluirImagem(byId, usuario, true);
                    }
                }
            }
        }

        return true;

    }

    private Origem getOrigemMobileUsuario(Usuario usuario) {
        Origem origem = Origem.ANDROID;
        DeviceSO deviceSO = usuario.getDeviceSO();
        if (deviceSO != null){
            origem = deviceSO == DeviceSO.ANDROID ? Origem.ANDROID : Origem.IOS;
        }
        return origem;
    }

    public Boolean justificar(Usuario usuario, Long documentoId, Long processoId, RequestJustificarDocumento requestJustificarDocumento, String imagePath) {

        Documento documento = documentoService.get(documentoId);
        if (documento == null) {
            throw new DocumentoRestException("documento.nao.localizado.com.id", documentoId);
        }

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        documentoService.justificar(documentoId,usuario,requestJustificarDocumento.getJustificativa());

        return true;
    }
}
