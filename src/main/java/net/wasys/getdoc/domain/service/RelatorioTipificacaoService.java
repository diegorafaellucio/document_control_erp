package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CamposMetadadosTipificacao;
import net.wasys.getdoc.domain.vo.filtro.ImagemMetaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioTipificacaoService {

    @Autowired private ImagemMetaService imagemMetaService;
    @Autowired private ImagemService imagemService;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private TipoDocumentoService tipoDocumentoService;



    public File render(ImagemMetaFiltro filtro) {

        try {
            File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/relatorio-tipificacao.xlsx");

            File file = File.createTempFile("relatorio-tipificacao1", ".xlsx");
            DummyUtils.deleteOnExitFile(file);
            FileUtils.copyFile(fileOrigem, file);

            ExcelWriter ew = new ExcelWriter();
            ew.abrirArquivo(file);
            Workbook workbook = ew.getWorkbook();
            ExcelFormat ef = new ExcelFormat(workbook);
            ew.setExcelFormat(ef);

            ew.selecionarPlanilha("Plan1");

            renderRows(filtro, ew);

            File fileDestino = File.createTempFile("relatorio-tipificacao2", ".xlsx");
            DummyUtils.deleteOnExitFile(fileDestino);

            FileOutputStream fos = new FileOutputStream(fileDestino);
            workbook.write(fos);
            workbook.close();
            DummyUtils.deleteFile(file);

            return fileDestino;
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderRows(ImagemMetaFiltro filtro, ExcelWriter ew) {

        filtro.setTipificado(true);
        final List<ImagemMeta> list = imagemMetaService.findByFiltro(filtro);

        int rowNum = 1;
        for (int i = 0; i < list.size(); i++) {

            ImagemMeta imagemMeta = list.get(i);
            String metaDados = imagemMeta.getMetaDados();

            if(StringUtils.isNotBlank(metaDados)) {
                ew.criaLinha(rowNum++);

                renderBody(ew, imagemMeta);
            }

            try {
                Thread.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Session session = sessionFactory.getCurrentSession();
        session.clear();
    }

    private String tipificouCorretamente(List<ModeloDocumento> modelosDocumento, String label){
        for (ModeloDocumento modeloDocumento: modelosDocumento) {
            if (label.contains(modeloDocumento.getLabelDarknet())){
                return "Sim";
            }

        }
        return "Não";
    }

    private void renderBody(ExcelWriter ew, ImagemMeta imagemMeta) {

        Map<String, String> metadados = (Map<String, String>) DummyUtils.jsonStringToMap(imagemMeta.getMetaDados());
        if (metadados == null) {
            return;
        }

        Imagem imagem = imagemService.get(imagemMeta.getImagemId());
        Documento documento = imagem.getDocumento();

        TipoDocumento tipoDocumento = documento.getTipoDocumento();

        List<ModeloDocumento> modeloDocumentos = tipoDocumento != null ? tipoDocumentoService.findModelosDocumentos(tipoDocumento.getId()) : new ArrayList<>();

        Processo processo = documento != null ? documento.getProcesso() : null;

        Long processoId = processo != null ? processo.getId() : 0;
        ew.escrever(processoId);

        TipoProcesso tipoProcesso = processo != null ? processo.getTipoProcesso() : null;
        String nomeStr = tipoProcesso != null ? tipoProcesso.getNome() : "";
        ew.escrever(nomeStr);

        Situacao situacao = processo != null ? processo.getSituacao() : null;
        String nomeSituacaoStr = situacao != null ? situacao.getNome() : "";
        ew.escrever(nomeSituacaoStr);

        String nomeDocumentoStr = documento != null ? documento.getNome() : "";
        ew.escrever(nomeDocumentoStr);

        String obrigatorioStr = tipoDocumento != null ? tipoDocumento.getObrigatorio() ? "Sim" : "Não" : "";
        ew.escrever(obrigatorioStr);

        ModeloDocumento modeloDocumento = documento != null ? documento.getModeloDocumento() : null;
        String nomeModeloDocumentoStr = modeloDocumento != null ? modeloDocumento.getDescricao() : "";
        ew.escrever(nomeModeloDocumentoStr);

        Date dataDigitalizacao = documento != null ? documento.getDataDigitalizacao() : null;
        String dataDigitalizacaoStr = dataDigitalizacao != null ? DummyUtils.formatDateTime(dataDigitalizacao) : "";
        ew.escrever(dataDigitalizacaoStr);
        String labelStr = null;
        String todasAsLabelsStr = null;
		String tipificacaoCorretaStr = null;
		String percentualDeConfiancaStr = null;

        if (metadados.containsKey(CamposMetadadosTipificacao.DN_LABEL.getCampo())) {
            labelStr = metadados.get(CamposMetadadosTipificacao.DN_LABEL.getCampo());
            todasAsLabelsStr = metadados.get(CamposMetadadosTipificacao.DN_TODAS_LABELS.getCampo());


            percentualDeConfiancaStr = metadados.get(CamposMetadadosTipificacao.DN_PERCENTUAL_ACERTO.getCampo());
            percentualDeConfiancaStr = percentualDeConfiancaStr != null ? percentualDeConfiancaStr : "";

            tipificacaoCorretaStr = tipificouCorretamente(modeloDocumentos, labelStr);

        }
        else{
            labelStr = "";
            todasAsLabelsStr =  "";
            percentualDeConfiancaStr = metadados.get(CamposMetadadosTipificacao.GV_PERCENTUAL_ACERTO.getCampo());
        }

		ew.escrever(labelStr);
		ew.escrever(todasAsLabelsStr);
		ew.escrever(tipificacaoCorretaStr);
		ew.escrever(percentualDeConfiancaStr);
    }
}