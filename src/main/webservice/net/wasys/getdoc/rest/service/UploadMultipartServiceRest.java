package net.wasys.getdoc.rest.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.rest.request.vo.UploadArquivo;
import net.wasys.util.DummyUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * Centraliza a lógica do upload de arquivos multipart.
 */
@Service
public class UploadMultipartServiceRest extends SuperServiceRest{

    /**
     * Deixa em memória as imagens de um documentoId para fazer o upload em duas etapas:
     * 1) Faz o upload dos arquivos para o tmp do servidor. Cria um Map contendo o checksum (key) do arquivo para o arquivo (filepath).
     * 2) O client envia a confirmação (checksum/keys) dos arquivos que deverão ser gravados.
     */
    private static Map<String, Map<String, UploadArquivo>> mapUploadMultipart = new HashMap<>();

    public enum AcaoUpload{
        UPLOAD_ANEXO_RESPOSTA_SOLICITACAO,
        UPLOAD_ANEXO_CADASTRAR_SOLICITACAO,
        UPLOAD_ANEXO_EVIDENCIA,
        UPLOAD_ANEXO_SOLICITACAO_CANCELAMENTO,
        UPLOAD_ANEXO_DOCUMENTO,
        UPLOAD_ANEXO_ENVIO_EMAIL,
        UPLOAD_ANEXO_RECUSA_SOLICITACAO,
        UPLOAD_ANEXO_CONCLUIR_TAREFA,
        UPLOAD_ANEXO_REABRIR_TAREFA,
        UPLOAD_MODELO_OCR,
        UPLOAD_ANEXO_SOLICITACAO_ACOMPANHAMENTO
    }

    /**
     * Como os uploads que imagem vão ficar em um map em memória até que seja confirmado que o upload deve ser salvo,
     * cria uma espécie de 'sessão', cosiderando:
     * ID USUARIO + (ID PROCESSO ou ID EVIDENCIA ou ID SOLICITACAO) + Enum.AcaoUpload, para indicar qual é o tipo de ação que
     * esta sendo realizada. Esse enum é importante para o caso de a mesma imagem (mesmo checksum) ser utilizada pelo mesmo usuário
     * para ser anexada em um processo e uma solicitação, por exemplo. Nesse exemplo, o ID USUARIO + ID poderia dar conflito, o enum
     * 'desempata' essa situação.
     * @param usuario
     * @param id
     * @param acaoUpload
     * @return
     */
    public String getKeyUpload(Usuario usuario, Long id, UploadMultipartServiceRest.AcaoUpload acaoUpload){
        return acaoUpload.name() + "_" + usuario.getId() + "_" + id;
    }

    /**
     * Retorna a lista de cheksum dos anexos salvos no tmpdir. Essa lista de checksum deverá ser enviada novamente como ser anexada
     * ao Entity e ser persistida no banco de dados.
     * @param usuario
     * @param id
     * @param acaoUpload
     * @param multipartFiles
     * @return
     */
    public Set<String> putAnexo(Usuario usuario, Long id, AcaoUpload acaoUpload, MultipartFile[] multipartFiles) {

        String key = getKeyUpload(usuario, id, acaoUpload);

        mapUploadMultipart.clear();

        try {

            for(MultipartFile multipartFile : multipartFiles) {

                String fileName = multipartFile.getOriginalFilename();

                File tmpFile = getTmpFileName(acaoUpload, fileName);
                DummyUtils.deleteOnExitFile(tmpFile);
                InputStream is = multipartFile.getInputStream();
                FileUtils.copyInputStreamToFile(is, tmpFile);

                String keyChecksum = DummyUtils.getHashChecksum(tmpFile);
                String filePath = tmpFile.getAbsolutePath();
                DummyUtils.systraceThread("Checksum " + keyChecksum + " > " + filePath);


                Map<String, UploadArquivo> mapChecksumFilePath = mapUploadMultipart.get(key);
                if(mapChecksumFilePath == null){
                    mapChecksumFilePath = new HashMap<>();
                }

                mapChecksumFilePath.put(keyChecksum, new UploadArquivo(fileName, filePath));
                mapUploadMultipart.put(key, mapChecksumFilePath);
            }

            return mapUploadMultipart.get(key).keySet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getTmpFileName(AcaoUpload acaoUpload, String fileName) throws IOException {
        String extensao = DummyUtils.getExtensao(fileName);;

        switch (acaoUpload){
            case UPLOAD_MODELO_OCR:
                return File.createTempFile("modeloocr-", "." + extensao);
            default:
                return File.createTempFile("anexo-proc-", ".tmp");
        }
    }

    /**
     * Recupera um item que esta em memória no map.
     * @param key
     * @return
     */
    public Map<String, UploadArquivo> get(String key) {
        return mapUploadMultipart.get(key);
    }

    /**
     * Remove um item que esta em memória no map.
     * @param key
     */
    public void remove(String key) {
        mapUploadMultipart.remove(key);
    }

    public void clear(){
        mapUploadMultipart.clear();
    }
}