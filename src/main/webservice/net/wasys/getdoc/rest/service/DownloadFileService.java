package net.wasys.getdoc.rest.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.rest.exception.AnexoRestException;
import net.wasys.getdoc.rest.response.vo.ArquivoDownload;
import net.wasys.getdoc.rest.response.vo.DownloadAnexoResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;

/**
 * Centraliza o download dos arquivos nesse service.
 */
@Service
public class DownloadFileService extends SuperServiceRest{

    /**
     *
     * @param arquivoDownload
     * @return
     */
    public DownloadAnexoResponse download(ArquivoDownload arquivoDownload) throws AnexoRestException {

        String path = arquivoDownload.getPath();
        File file = new File(path);
        String nome = arquivoDownload.getNome();
        Long tamanho = arquivoDownload.getTamanho();
        String extensao = arquivoDownload.getExtensao();
        if(!file.exists()){
            Long id = arquivoDownload.getId();
            throw new AnexoRestException("anexo.nao.localizado.disco", id + " / " + nome);
        }

        DownloadAnexoResponse downloadAnexoResponse = new DownloadAnexoResponse();

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentLength(tamanho);
        respHeaders.setContentDispositionFormData("attachment", nome);

        switch (extensao){
            case "png":{
                respHeaders.setContentType(MediaType.IMAGE_PNG);
                break;
            }
            case "pdf":{
                respHeaders.setContentType(MediaType.valueOf("application/pdf"));
                break;
            }

            case "jpg":
            case "jpeg":
                respHeaders.setContentType(MediaType.IMAGE_JPEG);
                break;

            case "gif": {
                respHeaders.setContentType(MediaType.IMAGE_GIF);
                break;
            }
            case "zip": {
                respHeaders.put("Content-Disposition", Collections.singletonList("attachment; filename=" + nome + "." + extensao));
                break;
            }

            default:{
                throw new AnexoRestException("anexo.extensao.nao.tratada", extensao);
            }
        }

        try {
            InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
            downloadAnexoResponse.setIsr(isr);
            downloadAnexoResponse.setRespHeaders(respHeaders);
            return downloadAnexoResponse;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}