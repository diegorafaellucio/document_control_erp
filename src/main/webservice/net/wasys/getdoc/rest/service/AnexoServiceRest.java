package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.ProcessoLogAnexoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.vo.LogVO;
import net.wasys.getdoc.domain.vo.filtro.AnexoFiltro;
import net.wasys.getdoc.rest.exception.AnexoRestException;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestFiltrarAnexos;
import net.wasys.getdoc.rest.response.vo.AnexoResponse;
import net.wasys.getdoc.rest.response.vo.ArquivoDownload;
import net.wasys.getdoc.rest.response.vo.DownloadAnexoResponse;
import net.wasys.util.other.ReflectionBeanComparator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class AnexoServiceRest extends SuperServiceRest{

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private ProcessoLogAnexoService processoLogAnexoService;
    @Autowired
    private DownloadFileService downloadFileService;

    /**
     * Consulta os anexos de um processo.
     * @param usuario
     * @param processoId
     * @return
     */
    public List<AnexoResponse> getAnexos(Usuario usuario, Long processoId) throws ProcessoRestException {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }


        AnexoFiltro anexoFiltro = new AnexoFiltro();
        anexoFiltro.setProcessoId(processo.getId());

        List<ProcessoLogAnexo> logs1 = processoLogAnexoService.findByProcessoAnexos(anexoFiltro);

        List<LogVO> logs = new ArrayList<>();
        for (ProcessoLogAnexo pla : logs1) {
            LogVO vo = new LogVO(pla);
            logs.add(vo);
            Integer alturaImagem = pla.getAlturaImagem();
            vo.setAlturaImg(alturaImagem);
            Integer larguraImagem = pla.getLarguraImagem();
            vo.setLarguraImg(larguraImagem);
        }

        Collections.sort(logs, new ReflectionBeanComparator<>("data desc, id desc"));

        List<AnexoResponse> list = null;
        if(logs != null && logs.size() > 0){
            list = new ArrayList<>();

            for(LogVO logVo : logs){
                list.add(new AnexoResponse(messageService, logVo));
            }
        }
        return list;
    }

    /**
     *
     * @param usuario
     * @param anexoId
     * @return
     */
    public DownloadAnexoResponse download(Usuario usuario, Long anexoId) throws AnexoRestException {

        ProcessoLogAnexo processoLogAnexo = processoLogAnexoService.get(anexoId);
        if(processoLogAnexo == null){
            throw new AnexoRestException("anexo.nao.localizado.com.id", anexoId);
        }

        File file = new File(processoLogAnexo.getPath());
        if(!file.exists()){
            throw new AnexoRestException("anexo.nao.localizado.disco", processoLogAnexo.getId() + " / " + processoLogAnexo.getNome());
        }

        ArquivoDownload arquivoDownload = new ArquivoDownload();
        arquivoDownload.setId(processoLogAnexo.getId());
        arquivoDownload.setPath(processoLogAnexo.getPath());
        arquivoDownload.setNome(processoLogAnexo.getNome());
        arquivoDownload.setExtensao(processoLogAnexo.getExtensao());
        arquivoDownload.setTamanho(processoLogAnexo.getTamanho());

        DownloadAnexoResponse download = downloadFileService.download(arquivoDownload);
        if(download == null){
            throw new AnexoRestException("anexo.response.null");
        }
        return download;
    }

    public List<AnexoResponse> filtrarAnexos(Usuario usuario, Long processoId, RequestFiltrarAnexos requestFiltrarAnexos) {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        AnexoFiltro anexoFiltro = new AnexoFiltro();
        anexoFiltro.setProcessoId(processo.getId());
        anexoFiltro.setAcoesSelecionadas(requestFiltrarAnexos.getAcao());
        anexoFiltro.setBusca(requestFiltrarAnexos.getTextoBusca());
        anexoFiltro.setDataFim(requestFiltrarAnexos.getDataFim());
        anexoFiltro.setDataInicio(requestFiltrarAnexos.getDataInicio());

        List<ProcessoLogAnexo> logs1 = processoLogAnexoService.findByProcessoAnexos(anexoFiltro);

        List<LogVO> logs = new ArrayList<>();
        for (ProcessoLogAnexo pla : logs1) {
            LogVO vo = new LogVO(pla);
            logs.add(vo);
            Integer alturaImagem = pla.getAlturaImagem();
            vo.setAlturaImg(alturaImagem);
            Integer larguraImagem = pla.getLarguraImagem();
            vo.setLarguraImg(larguraImagem);
        }

        Collections.sort(logs, new ReflectionBeanComparator<>("data desc, id desc"));

        List<AnexoResponse> list = null;
        if(CollectionUtils.isNotEmpty(logs)){
            list = new ArrayList<>();
            for (LogVO log : logs) {
                list.add(new AnexoResponse(messageService, log));
            }
        }
        return list;
    }
}