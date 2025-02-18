package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Solicitacao;
import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.AreaService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.SolicitacaoService;
import net.wasys.getdoc.domain.service.SubareaService;
import net.wasys.getdoc.domain.vo.SolicitacaoVO;
import net.wasys.getdoc.rest.exception.*;
import net.wasys.getdoc.rest.request.vo.RequestCadastroSolicitacao;
import net.wasys.getdoc.rest.request.vo.RequestRecusarSolicitacao;
import net.wasys.getdoc.rest.request.vo.RequestRespostaSolicitacao;
import net.wasys.getdoc.rest.request.vo.UploadArquivo;
import net.wasys.getdoc.rest.response.vo.ListaSolicitacaoResponse;
import net.wasys.getdoc.rest.response.vo.SolicitacaoResponse;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections4.CollectionUtils;
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
public class SolicitacaoServiceRest extends SuperServiceRest {

    @Autowired
    private SolicitacaoService solicitacaoService;
    @Autowired
    private UploadMultipartServiceRest uploadMultipartServiceRest;
    @Autowired
    private ProcessoService processoService;
    @Autowired
    private SubareaService subareaService;
    @Autowired
    private AreaService areaService;

    public ListaSolicitacaoResponse getSolicitacoes(Usuario usuario, Long processoId) {

        RoleGD roleGD = usuario.getRoleGD();
        List<SolicitacaoVO> vosPendentesByProcesso = solicitacaoService.findVosPendentesByProcesso(processoId, roleGD);

        List<SolicitacaoVO> vosByProcesso = solicitacaoService.findVosByProcesso(processoId);
        List<SolicitacaoResponse> list = parser(vosByProcesso);

        ListaSolicitacaoResponse listaSolicitacaoResponse = new ListaSolicitacaoResponse();
        listaSolicitacaoResponse.setBadgeSolicitacaoPendente(vosPendentesByProcesso != null ? vosPendentesByProcesso.size() : 0);
        listaSolicitacaoResponse.setSolicitacoes(list);

        return listaSolicitacaoResponse;
    }

    private List<SolicitacaoResponse> parser(List<SolicitacaoVO> vosByProcess) {
        List<SolicitacaoResponse> list = null;
        if (vosByProcess != null && vosByProcess.size() > 0) {
            list = new ArrayList<>();
            for (SolicitacaoVO te : vosByProcess) {
                list.add(new SolicitacaoResponse(te));
            }
        }
        return list;
    }

    public Set<String> passo1UploadAnexoSolicitacao(Usuario usuario, Long processoId, MultipartFile[] multipartFiles) throws MessageKeyException {

        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_CADASTRAR_SOLICITACAO, multipartFiles);
        return anexos;
    }


    public boolean cadastrarSolicitacao(Usuario usuario, Long processoId, RequestCadastroSolicitacao requestCadastroSolicitacao) throws Exception {
        validaRequestParameters(requestCadastroSolicitacao);

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        Subarea subarea = subareaService.getById(requestCadastroSolicitacao.getSubAreaId());
        if (subarea == null) {
            throw new SubAreaRestException("subarea.nao.localizado.id", requestCadastroSolicitacao.getSubAreaId());
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, processoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_CADASTRAR_SOLICITACAO);

        SolicitacaoVO solicitacaoVO = new SolicitacaoVO();
        solicitacaoVO.setSolicitacao(new Solicitacao());
        solicitacaoVO.setAcao(AcaoProcesso.SOLICITACAO_CRIACAO);
        solicitacaoVO.getSolicitacao().setSubarea(subarea);
        solicitacaoVO.setObservacaoTmp(requestCadastroSolicitacao.getObservacao());

        List<String> keys = requestCadastroSolicitacao.getAnexos();

        if(CollectionUtils.isNotEmpty(keys)) {
            Map<String, UploadArquivo> mapChecksumFilePath = uploadMultipartServiceRest.get(key);
            if (mapChecksumFilePath == null || mapChecksumFilePath.keySet() == null || mapChecksumFilePath.keySet().size() <= 0) {
                throw new ProcessoRestException("documento.mapChecksumFilePath.null", processo.getId(), processo.getTipoProcesso().getNome());
            }

            /**
             * Objeto retorno ainda não processou nenhuma imagem, mantém como null...
             */
            Map<String, String> mapStatusUpload = new HashMap<>();
            keys.forEach(checksum -> {
                mapStatusUpload.put(checksum, null);
            });

            Set<String> listCheckSum = mapChecksumFilePath.keySet();
            for (String keyChecksum : listCheckSum) {
                UploadArquivo uploadArquivo = mapChecksumFilePath.get(keyChecksum);

                File tmpFile = FileUtils.getFile(uploadArquivo.getPath());
                if (!tmpFile.exists()) {
                    throw new AnexoRestException("anexo.file.null", tmpFile.getAbsolutePath());
                }
                DummyUtils.deleteOnExitFile(tmpFile);
                solicitacaoVO.addAnexo(uploadArquivo.getNome(), tmpFile);

                //se chegou até aqui, atualiza o status com sucesso.
                mapStatusUpload.put(keyChecksum, uploadArquivo.getNome());
            }
        }
        solicitacaoService.salvarSolicitacao(solicitacaoVO, processo, usuario);
        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        return true;
    }

    public Set<String> passo1UploadAnexoRespostaSolicitacao(Usuario usuario, Long solicitacaoId, MultipartFile[] multipartFiles) throws MessageKeyException {

        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Solicitacao solicitacao = solicitacaoService.getById(solicitacaoId);
        if (solicitacao == null) {
            throw new SolicitacaoRestException("solicitacao.nao.localizado.id", solicitacaoId);
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, solicitacaoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_RESPOSTA_SOLICITACAO, multipartFiles);
        return anexos;
    }

    public boolean registrarRespostaSolicitacao(Usuario usuario, Long solicitacaoId, RequestRespostaSolicitacao requestRespostaSolicitacao) throws Exception {
        validaRequestParameters(requestRespostaSolicitacao);

        Solicitacao solicitacao = solicitacaoService.getById(solicitacaoId);
        if (solicitacao == null) {
            throw new SolicitacaoRestException("solicitacao.nao.localizado.id", solicitacaoId);
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, solicitacaoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_RESPOSTA_SOLICITACAO);

        SolicitacaoVO solicitacaoVO = new SolicitacaoVO();
        solicitacaoVO.setSolicitacao(solicitacao);
        solicitacaoVO.setAcao(AcaoProcesso.SOLICITACAO_REGISTRO_RESPOSTA);
        solicitacaoVO.setObservacaoTmp(requestRespostaSolicitacao.getObservacao());

        solicitacaoVO = setAnexos(solicitacaoVO, requestRespostaSolicitacao.getAnexos(), key, solicitacao);
        solicitacaoService.salvarSolicitacao(solicitacaoVO, solicitacao.getProcesso(), usuario);
        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        return true;
    }

    private SolicitacaoVO setAnexos(SolicitacaoVO solicitacaoVO, List<String> keys, String key, Solicitacao solicitacao) {
        if(CollectionUtils.isNotEmpty(keys)) {
            Map<String, UploadArquivo> mapChecksumFilePath = uploadMultipartServiceRest.get(key);
            if (mapChecksumFilePath == null || mapChecksumFilePath.keySet() == null || mapChecksumFilePath.keySet().size() <= 0) {
                throw new SolicitacaoRestException("solicitacao.mapChecksumFilePath.null", solicitacao.getId());
            }

            Set<String> listCheckSum = mapChecksumFilePath.keySet();
            for (String keyChecksum : listCheckSum) {
                UploadArquivo uploadArquivo = mapChecksumFilePath.get(keyChecksum);

                File tmpFile = FileUtils.getFile(uploadArquivo.getPath());
                if (!tmpFile.exists()) {
                    throw new AnexoRestException("anexo.file.null", tmpFile.getAbsolutePath());
                }
                DummyUtils.deleteOnExitFile(tmpFile);
                solicitacaoVO.addAnexo(uploadArquivo.getNome(), tmpFile);
            }
        }
        return solicitacaoVO;
    }

    public Set<String> passo1UploadAnexoRecusaSolicitacao(Usuario usuario, Long solicitacaoId, MultipartFile[] multipartFiles) {
        if (multipartFiles == null || multipartFiles.length == 0) {
            throw new DocumentoRestException("documento.multipart.null");
        }

        Solicitacao solicitacao = solicitacaoService.getById(solicitacaoId);
        if (solicitacao == null) {
            throw new SolicitacaoRestException("solicitacao.nao.localizado.id", solicitacaoId);
        }

        Set<String> anexos = uploadMultipartServiceRest.putAnexo(usuario, solicitacaoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_RECUSA_SOLICITACAO, multipartFiles);
        return anexos;
    }

    public boolean recusarSolicitacao(Usuario usuario, Long solicitacaoId, RequestRecusarSolicitacao requestRecusarSolicitacao) throws Exception {
        validaRequestParameters(requestRecusarSolicitacao);

        Solicitacao solicitacao = solicitacaoService.getById(solicitacaoId);
        if (solicitacao == null) {
            throw new SolicitacaoRestException("solicitacao.nao.localizado.id", solicitacaoId);
        }

        String key = uploadMultipartServiceRest.getKeyUpload(usuario, solicitacaoId, UploadMultipartServiceRest.AcaoUpload.UPLOAD_ANEXO_RECUSA_SOLICITACAO);

        SolicitacaoVO solicitacaoVO = new SolicitacaoVO();
        solicitacaoVO.setSolicitacao(solicitacao);
        solicitacaoVO.setAcao(AcaoProcesso.SOLICITACAO_RECUSA_SOLICITACAO);
        solicitacaoVO.setObservacaoTmp(requestRecusarSolicitacao.getObservacao());

        solicitacaoVO = setAnexos(solicitacaoVO, requestRecusarSolicitacao.getAnexos(), key, solicitacao);
        solicitacaoService.salvarSolicitacao(solicitacaoVO, solicitacao.getProcesso(), usuario);
        uploadMultipartServiceRest.remove(key);//zera o map pois já fez upload de tudo.
        return true;
    }
}