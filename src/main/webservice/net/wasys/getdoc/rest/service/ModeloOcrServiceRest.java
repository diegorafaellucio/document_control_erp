package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.ModeloOcrService;
import net.wasys.getdoc.rest.exception.ModeloOcrRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroModeloOcr;
import net.wasys.getdoc.rest.request.vo.UploadArquivo;
import net.wasys.getdoc.rest.response.vo.ListaModeloOcrResponse;
import net.wasys.getdoc.rest.response.vo.ModeloOcrResponse;
import net.wasys.util.DummyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class ModeloOcrServiceRest extends SuperServiceRest {

    @Autowired
    private ModeloOcrService modeloOcrService;

    @Autowired
    private UploadMultipartServiceRest uploadMultipartServiceRest;

    public List<ModeloOcrResponse> getAtivos(Usuario usuario) {
        List<ModeloOcr> ativos = modeloOcrService.findAtivos();
        List<ModeloOcrResponse> listModelosOcr = new ArrayList<>();

        ativos.forEach(modeloOcr -> {
            listModelosOcr.add(new ModeloOcrResponse(modeloOcr));
        });

        return listModelosOcr;
    }

    public List<ListaModeloOcrResponse> getAll(Usuario usuario, int min, int max) {
        List<ModeloOcr> all = modeloOcrService.findAll(min, max);

        List<ListaModeloOcrResponse> listModelosOcr = new ArrayList<>();
        all.forEach(modeloOcr -> {
            listModelosOcr.add(new ListaModeloOcrResponse(modeloOcr));
        });

        return listModelosOcr;
    }

    public ListaModeloOcrResponse cadastrar(Usuario usuario, RequestCadastroModeloOcr requestCadastroModeloOcr) throws ModeloOcrRestException {
        validaRequestParameters(requestCadastroModeloOcr);
        ModeloOcr modeloOcr = new ModeloOcr();
        return saveOrUpdate(usuario, requestCadastroModeloOcr, modeloOcr);
    }

    public ListaModeloOcrResponse editar(Usuario usuario, Long id, RequestCadastroModeloOcr requestCadastroModeloOcr) throws ModeloOcrRestException {
        validaRequestParameters(requestCadastroModeloOcr);
        ModeloOcr modeloOcr = modeloOcrService.get(id);

        if(modeloOcr == null){
            throw new ModeloOcrRestException("modeloocr.nao.localizado.id", id);
        }

        return saveOrUpdate(usuario, requestCadastroModeloOcr, modeloOcr);
    }

    private ListaModeloOcrResponse saveOrUpdate(Usuario usuario, RequestCadastroModeloOcr requestCadastroModeloOcr, ModeloOcr modeloOcr) throws ModeloOcrRestException {
        if (CollectionUtils.isEmpty(requestCadastroModeloOcr.getAnexos())) {
            throw new ModeloOcrRestException("modeloocr.keys.null");
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, usuario.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_MODELO_OCR);

        Map<String, UploadArquivo> mapChecksumFilePath = uploadMultipartServiceRest.get(key);
        if (mapChecksumFilePath == null || mapChecksumFilePath.keySet() == null || mapChecksumFilePath.keySet().size() <= 0) {
            throw new ModeloOcrRestException("modeloocr.multipart.null");
        }

        modeloOcr.setAltura(requestCadastroModeloOcr.getAltura());
        modeloOcr.setAtivo(requestCadastroModeloOcr.isAtivo());
        modeloOcr.setDescricao(requestCadastroModeloOcr.getDescricao());
        modeloOcr.setLargura(requestCadastroModeloOcr.getLargura());

        Set<String> listCheckSum = mapChecksumFilePath.keySet();
        for (String keyChecksum : listCheckSum) {
            UploadArquivo uploadArquivo = mapChecksumFilePath.get(keyChecksum);

            File tmpFile = FileUtils.getFile(uploadArquivo.getPath());
            if (!tmpFile.exists()) {
                throw new ModeloOcrRestException("documento.file.null", tmpFile.getAbsolutePath());
            }
            DummyUtils.deleteOnExitFile(tmpFile);

            modeloOcr.setSizeModelo(tmpFile.length());
            modeloOcr.setNomeArquivo(uploadArquivo.getNome());

            modeloOcrService.saveOrUpdate(modeloOcr, usuario, tmpFile);
            uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        }

        ListaModeloOcrResponse modeloOcrResponse = new ListaModeloOcrResponse(modeloOcr);
        return modeloOcrResponse;
    }

    public Set<String> passo1UploadModeloOcr(Usuario usuario, MultipartFile[] multipartFiles) throws ModeloOcrRestException {
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new ModeloOcrRestException("documento.multipart.null");
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, usuario.getId(), UploadMultipartServiceRest.AcaoUpload.UPLOAD_MODELO_OCR, multipartFiles);
        return anexos;
    }

    public boolean excluir(Usuario usuario, Long id) throws ModeloOcrRestException {
        ModeloOcr modeloOcr = modeloOcrService.get(id);

        if(modeloOcr == null){
            throw new ModeloOcrRestException("modeloocr.nao.localizado.id", id);
        }

        modeloOcrService.excluir(modeloOcr.getId(), usuario);
        return true;
    }

    public ListaModeloOcrResponse detalhar(Usuario usuario, Long id) {
        ModeloOcr modeloOcr = modeloOcrService.get(id);

        if(modeloOcr == null){
            throw new ModeloOcrRestException("modeloocr.nao.localizado.id", id);
        }
        return new ListaModeloOcrResponse(modeloOcr);
    }
}