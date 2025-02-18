package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.repository.RelatorioOperacaoRepository;
import net.wasys.getdoc.domain.vo.RelatorioOperacaoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioOperacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class RelatorioOperacaoService {

    @Autowired private SessionFactory sessionFactory;
    @Autowired private RelatorioOperacaoRepository relatorioOperacaoRepository;
    @Autowired private MessageService messageService;

    public List<RelatorioOperacaoVO> findRelatorioOperacao(RelatorioOperacaoFiltro filtro){
        return relatorioOperacaoRepository.findRelatorioOperacao(filtro);
    }

    public File renderRelatorioOperacao(RelatorioOperacaoFiltro filtro) {
        try {
            File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/relatorio-operacao.xlsx");

            File file = File.createTempFile("relatorio-relatorio-operacao1", ".xlsx");
            DummyUtils.deleteOnExitFile(file);
            FileUtils.copyFile(fileOrigem, file);

            ExcelWriter ew = new ExcelWriter();
            ew.abrirArquivo(file);
            Workbook workbook = ew.getWorkbook();
            ExcelFormat ef = new ExcelFormat(workbook);
            ew.setExcelFormat(ef);

            ew.selecionarPlanilha("Plan1");
            renderRowsRelatorioOperacao(filtro, ew);

            File fileDestino = File.createTempFile("relatorio-relatorio-operacao2", ".xlsx");
            DummyUtils.deleteOnExitFile(fileDestino);

            FileOutputStream fos = new FileOutputStream(fileDestino);
            workbook.write(fos);
            workbook.close();
            DummyUtils.deleteFile(file);

            return fileDestino;
        }
        catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderRowsRelatorioOperacao(RelatorioOperacaoFiltro filtro, ExcelWriter ew) {

        final List<RelatorioOperacaoVO> vos = findRelatorioOperacao(filtro);

        int total = vos.size();
        if(total > GetdocConstants.EXCEL_MAX_ROWS) {
            throw new MessageKeyException("excelLimiteDeLinhas.error", total);
        }
        systraceThread("iniciando extração. total de registros: " + total);

        int rowNum = 1;
        long inicio = System.currentTimeMillis();

        for (int i = 0; i < vos.size(); i++) {

            RelatorioOperacaoVO vo = vos.get(i);

            ew.criaLinha(rowNum++);

            renderBodyRelatorioOperacao(ew, vo);

            DummyUtils.sleep(1);

            if(rowNum % 1000 == 0) {
                long fim = System.currentTimeMillis();
                systraceThread(rowNum + " de " + total + ". " + (fim - inicio) + "ms.");
            }
        }

        Session session = sessionFactory.getCurrentSession();
        session.clear();
    }

    private void renderBodyRelatorioOperacao(ExcelWriter ew, RelatorioOperacaoVO vo) {


        Long processoId = vo.getProcessoId();
        ew.escrever(processoId);

        String situacao = vo.getSituacao();
        ew.escrever(situacao);

        Origem origem = vo.getOrigem();
        ew.escrever(origem.name());

        String regional = vo.getRegional();
        ew.escrever(regional);

        String campus = vo.getCampus();
        ew.escrever(campus);

        String formaIngresso = vo.getFormaIngresso();
        ew.escrever(formaIngresso);

        String periodoIngresso = vo.getPeriodoIngresso();
        ew.escrever(periodoIngresso);

        Long documentoId = vo.getDocumentoId();
        ew.escrever(documentoId);

        String nomeDocumento = vo.getNomeDocumento();
        ew.escrever(nomeDocumento);

        StatusDocumento statusDocumento = vo.getStatusDocumento();
        String statusDocumentoStr = messageService.getValue("StatusDocumento.".concat(statusDocumento.name()).concat(".label"));
        ew.escrever(statusDocumentoStr);

        Long totalImagens = vo.getTotalImagens();
        ew.escrever(totalImagens);

        String pendencia = vo.getIrregularidade();
        ew.escrever(pendencia);

        String observacao = vo.getObservacao();
        ew.escrever(observacao);

        Date data = vo.getData();
        ew.escreverDate(data);
        ew.escreverTime(data);

        AcaoDocumento acaoDocumento = vo.getAcaoDocumento();
        String acaoDocumentoStr = messageService.getValue("AcaoDocumento.".concat(acaoDocumento.name()).concat(".label"));
        ew.escrever(acaoDocumentoStr);

        String usuario = vo.getUsuario();
        ew.escrever(usuario);

        String usuarioNome = vo.getUsuarioNome();
        ew.escrever(usuarioNome);
    }
}
