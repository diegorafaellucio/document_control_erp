package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.service.webservice.aluno.GetDocAlunoService;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.rest.response.vo.AlunoDocumentosDigitalizados;
import net.wasys.getdoc.rest.response.vo.AlunoDocumentosDigitalizadosList;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;

@Service
public class ReaproveitamentoDigitalizadosJob extends TransactionWrapperJob {

	private final static int maxAmostragem = 5;

	@Autowired private DocumentoService documentoService;
	@Autowired private GetDocAlunoService getDocAlunoService;
	@Autowired private ProcessoService processoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 0/30 * * * ?")//a cada 30 minutos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("[INICIO] ReaproveitamentoDigitalizadosJob " + DummyUtils.formatDateTime(new Date()));
		LogAcesso log = null;

		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			long inicio2 = System.currentTimeMillis();

			Usuario usuario = usuarioService.getByLogin("getdoc.estacio");

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				AlunoDocumentosDigitalizadosList alunoDocumentosDigitalizadosList = getDocAlunoService.consultarDocumentosDigitalizados(maxAmostragem);
				List<AlunoDocumentosDigitalizados> documentos = alunoDocumentosDigitalizadosList.getDocumentos();
				for(AlunoDocumentosDigitalizados alunoDocumentosDigitalizados : documentos){
					String cpfAluno = alunoDocumentosDigitalizados.getCpfAluno();
					cpfAluno = DummyUtils.getCpf(cpfAluno);
					ProcessoFiltro filtro = new ProcessoFiltro();
					filtro.setCpfCnpj(cpfAluno);
					String modeloDocumento = alunoDocumentosDigitalizados.getModeloDocumento();
					boolean composicaoFamiliar = alunoDocumentosDigitalizados.isComposicaoFamiliar();
					String documentosDigitalizadosNomeDocumento = alunoDocumentosDigitalizados.getNomeDocumento();
					List<Processo> processos = processoService.findByFiltro(filtro, null, null);
					for(Processo processo : processos) {
						DocumentoFiltro filtroDoc = new DocumentoFiltro();
						filtroDoc.setProcesso(processo);
						filtroDoc.setStatusDocumentoList(Arrays.asList(StatusDocumento.INCLUIDO, StatusDocumento.PENDENTE));
						filtroDoc.setDiferenteDeOutros(true);
						List<Documento> documentosProcesso = documentoService.findByFiltro(filtroDoc, null, null);
						for(Documento documento : documentosProcesso) {
							TipoDocumento tipoDocumento = documento.getTipoDocumento();
							Long tipoDocumentoId = tipoDocumento.getId();
							List<ModeloDocumento> modelosDocumento = tipoDocumentoService.findModelosDocumentos(tipoDocumentoId);
							List<String> labelsDarknet = new ArrayList<>();
							for (ModeloDocumento modelo : modelosDocumento) {
								String labelDarknet = modelo.getLabelDarknet();
								labelsDarknet.add(labelDarknet);
							}
							if (labelsDarknet.contains(modeloDocumento)) {
								List<AlunoDocumentosDigitalizados.DadosUploadDocumentoAlunoRequestVO> arquivos = alunoDocumentosDigitalizados.getArquivos();
								List<FileVO> fileVOS = new ArrayList<>();
								for(AlunoDocumentosDigitalizados.DadosUploadDocumentoAlunoRequestVO vo : arquivos){
									String nomeArquivo = vo.getNomeArquivo();
									String path = vo.getPath();
									File file = new File(path);

									FileVO fileVO = new FileVO();
									fileVO.setFile(file);
									fileVO.setName(nomeArquivo);

									fileVOS.add(fileVO);
								}

								Documento documento1 = documento;
								if(composicaoFamiliar){
									Long processoId = processo.getId();
									String membroFamiliar = documentosDigitalizadosNomeDocumento.substring(documentosDigitalizadosNomeDocumento.indexOf(Documento.POSFIX_MEMBRO_FAMILIAR));
									String numMembro = membroFamiliar.replaceAll("[^\\d.]", "");
									String strMembro = Documento.POSFIX_MEMBRO_FAMILIAR + " (" + numMembro + ")";
									documento1 = documentoService.getFirstByTipoDocumentoIdMembroFamiliar(tipoDocumentoId, processoId, strMembro);
								}

								ImagemTransaction imagemTransaction = new ImagemTransaction();
								documentoService.digitalizarImagens(imagemTransaction, usuario, documento1, fileVOS, Origem.GETDOC_ALUNO);
							}
						}
					}
				}
			});
			tw.startThread();

			int tempoAtualizacaoMinutos = (int) ((System.currentTimeMillis() - inicio2) / 1000 / 60);

			DummyUtils.systraceThread("[FIM] ReaproveitamentoDigitalizadosJob. TempoAtualizacaoMinutos: " + tempoAtualizacaoMinutos);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}