package net.wasys.getdoc.domain.service;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.*;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.DocumentoRepository;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ImagemMetaFiltro;
import net.wasys.getdoc.domain.vo.filtro.TaxonomiaFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPastaVermelhaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.image.ImageShrinker;
import net.wasys.util.other.PDFConverter;
import net.wasys.util.other.PDFCreator;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmlbeans.impl.piccolo.xml.EntityManager;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Arrays.asList;
import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class DocumentoService {

	@Autowired private DocumentoRepository documentoRepository;
	@Autowired private ImagemService imagemService;
	@Autowired private ResourceService resourceService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private ProcessoService processoService;
	@Autowired private PendenciaService pendenciaService;
	@Autowired private AdvertenciaService advertenciaService;
	@Autowired private TipificacaoDarknetService tipificacaoDarknetService;
	@Autowired private LogOcrService logOcrService;
	@Autowired private CampoOcrService campoOcrService;
	@Autowired private FaceRecognitionService faceRecognitionService;
	@Autowired private ProcessoAjudaService processoAjudaService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ExceptionService exceptionService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private ImagemMetaService imagemMetaService;
	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private ParametroService parametroService;
	@Autowired private TipificacaoService tipificacaoService;
	@Autowired private CampanhaService campanhaService;
	@Autowired private ModeloDocumentoService modeloDocumentoService;
	@Autowired private SubperfilService subperfilService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private IrregularidadeTipoDocumentoService irregularidadeTipoDocumentoService;
	@Autowired private OcrService ocrService;
	@Autowired private MessageService messageService;
	@Autowired private IrregularidadeService irregularidadeService;
	@Autowired private CalendarioService calendarioService;
	@Autowired private ProcessoRegraService processoRegraService;

	public Documento get(Long id) {
		return documentoRepository.get(id);
	}

	public List<Documento> findByFiltro(DocumentoFiltro filtro, Integer inicio, Integer max) {
		return documentoRepository.findByFiltro(filtro, inicio, max);
	}

	public boolean existsByFiltro(DocumentoFiltro filtro) {
		return documentoRepository.existsByFiltro(filtro);
	}

	public boolean existsToNotificarAtila(Long processoId) {
		List<Pendencia> toNotificar = pendenciaService.findToNotificar(processoId);
		return !toNotificar.isEmpty();
	}

	public int countByFiltro(DocumentoFiltro filtro) {
		return documentoRepository.countByFiltro(filtro);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Documento documento) {
		documentoRepository.saveOrUpdate(documento);
	}

	@Transactional(rollbackFor=Exception.class)
	public void digitalizarImagens(ImagemTransaction transaction, Usuario usuario, Documento documento, List<FileVO> arquivos, Origem origem) throws MessageKeyException, IOException {
		digitalizarImagens(transaction, usuario, documento, arquivos, AcaoDocumento.DIGITALIZOU, origem, true);
	}

	@Transactional(rollbackFor=Exception.class)
	public void digitalizarImagens(ImagemTransaction transaction, Usuario usuario, Documento documento, List<FileVO> arquivos, AcaoDocumento acao, Origem origem) throws MessageKeyException, IOException {
		digitalizarImagens(transaction, usuario, documento, arquivos, acao, origem, true);
	}

	@Transactional(rollbackFor=Exception.class)
	public void digitalizarImagens(ImagemTransaction transaction, Usuario usuario, Documento documento, List<FileVO> arquivos, AcaoDocumento acao, Origem origem, boolean sobrescrever) throws MessageKeyException, IOException {

		String dir = resourceService.getValue(ResourceService.IMAGENS_PATH);

		Integer versaoAtual = documento.getVersaoAtual();
		gravaDadosDeVersaoAnterior(documento, versaoAtual);

		Long documentoId = documento.getId();
		documento.setOrigem(origem);
		String documentoNome = documento.getNome();
		boolean isTipificando = Documento.NOME_TIFICANDO.equals(documentoNome);
		boolean isOutros = Documento.NOME_OUTROS.equals(documentoNome);
		Processo processo = documento.getProcesso();
		StatusProcesso status = processo.getStatus();
		if(!StatusProcesso.RASCUNHO.equals(status) || versaoAtual == 0) {
			versaoAtual = isOutros ? 1 : versaoAtual + 1;
			documento.setVersaoAtual(versaoAtual);
		}
		else if(sobrescrever && StatusProcesso.RASCUNHO.equals(status) && !isOutros) {
			imagemService.excluirByDocumento(documentoId);
		}

		StatusDocumento statusDoc = documento.getStatus();
		if(StatusDocumento.EXCLUIDO.equals(statusDoc)) {
			adicionarDocumento(documento, usuario);
		}

		boolean first = true;
		PDFConverter pdfConverter = new PDFConverter();
		for (FileVO vo : arquivos) {

			File tempFile = vo.getFile();
			String fileName = vo.getName();
			if(fileName == null){
				fileName = tempFile.getName();
				fileName = fileName.substring(fileName.lastIndexOf("/")+1, fileName.length());
			}
			String fullText = vo.getText();
			String extensao = DummyUtils.getExtensao(fileName);
			Imagem imagem = null;

			parametroService.validarArquivoPermitido(fileName);

			if((isOutros || isTipificando) && StringUtils.equals(extensao, "pdf")){
				List<File> files = pdfConverter.getImagens(tempFile);
				for (File file : files) {
					String imageName = file.getName();
					String extensaoImagem = DummyUtils.getExtensao(imageName);

					FileVO vo1 = vo.clone();
					vo1.setFile(file);

					imagem = digitalizarImagem(transaction, documento, dir, vo1, extensaoImagem, fullText);
					String absolutePath = file.getAbsolutePath();
					transaction.addToDeleteOnCommit(absolutePath);
				}
			} else {
				imagem = digitalizarImagem(transaction, documento, dir, vo, extensao, fullText);
			}

			transaction.isDigitalizadasExistem();

			ImagemMeta imagemMeta = vo.getImagemMeta();
			if (imagemMeta != null) {
				sobrescreverImagemMeta(imagem, imagemMeta);
			}

			List<CampoOcr> camposOcr = vo.getCamposOcr();
			if (camposOcr != null && first) {
				for (CampoOcr campoOcr : camposOcr) {
					campoOcr.setImagem(imagem);
					campoOcrService.saveOrUpdate(campoOcr);
				}
			}

			if (StringUtils.isBlank(fullText)) {
				imagem.setAguardandoFulltext(true);
				imagemService.saveOrUpdate(imagem);
			}

			transaction.isDigitalizadasExistem();

			String absolutePath = tempFile.getAbsolutePath();
			transaction.addToDeleteOnCommit(absolutePath);

			first = false;
		}

		agendarOcr(documento, usuario);
		transaction.isDigitalizadasExistem();

		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		boolean sempreTipificar = tipoDocumento != null ? tipoDocumento.getSempreTipificar() : false;
		if(sempreTipificar) {
			documento.setModeloDocumento(null);
		}
		documento.setGrupoModeloDocumento(null);

		if(!Documento.NOME_TIFICANDO.equals(documentoNome)) {
			documento.setStatus(StatusDocumento.DIGITALIZADO);
		}else if(StatusDocumento.PENDENTE.equals(statusDoc) && (Origem.PORTAL_GRADUCAO.equals(origem) || Origem.PORTAL_POS_GRADUACAO.equals(origem))){
			documento.setStatus(StatusDocumento.DIGITALIZADO);
		}
		documento.setDataDigitalizacao(new Date());
		documentoLogService.criaLog(documento, usuario, acao);

		Advertencia advertencia = documento.getAdvertencia();
		if(advertencia != null) {
			atualizarAdvertencia(documento, advertencia);
		}
		documentoRepository.saveOrUpdate(documento);

		processoRegraService.reprocessaRegrasAoAtualizarDocumentos(processo, usuario);
	}

	private void sobrescreverImagemMeta(Imagem imagem, ImagemMeta imagemMeta) {

		if (imagem != null) {

			Long imagemId = imagem.getId();
			ImagemMeta imagemMetaOriginal = imagemMetaService.getByImagem(imagemId);
			imagemMetaService.excluir(imagemMetaOriginal);

			imagemMeta.setImagemId(imagemId);
			imagemMetaService.saveOrUpdate(imagemMeta);
		}
	}

	private void gravaDadosDeVersaoAnterior(Documento documento, Integer versao) throws MessageKeyException, IOException {
		String jsonFormatado = criaDadosDaVersaoDoDocumento(documento);

		Long documentoId = documento.getId();
		List<Imagem> imagensVersaoAnterior = imagemService.findByDocumentoVersao(documentoId, versao);
		for (Imagem imagem : imagensVersaoAnterior) {
			Long imagemId = imagem.getId();
			ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);
			if(imagemMeta == null) {
				Date dataDigitalizacao = documento.getDataDigitalizacao();
				String caminho = imagem.getCaminho();
				File file = new File(caminho);
				long length = file.exists() ? file.length() : 0;
				Integer numeroPaginasPdf = null;
				String extensao = imagem.getExtensao();
				if(StringUtils.equals(extensao, "pdf")){
					File file1 = file.getAbsoluteFile();
					InputStream targetStream = new FileInputStream(file1);
					PDDocument doc = PDDocument.load(targetStream);
					numeroPaginasPdf = doc.getNumberOfPages();
				}

				imagemMeta = new ImagemMeta();
				imagemMeta.setImagemId(imagemId);
				imagemMeta.setTamanho(length);
				imagemMeta.setDataDigitalizacao(dataDigitalizacao);
				imagemMeta.setPaginas(numeroPaginasPdf);
				imagemMetaService.saveOrUpdate(imagemMeta);
			}

			String metaDados = imagemMeta.getMetaDados();
			Map<String, String> map = (Map<String, String>) DummyUtils.jsonStringToMap(metaDados);
			map = map == null ? new HashMap<>() : map;
			map.put(CamposMetadadosTipificacao.REGISTRO_DE_DIGITALIZACAO.getCampo(), jsonFormatado);
			String metadadosStr = DummyUtils.objectToJson(map);
			Long imagemMetaId = imagemMeta.getId();
			imagemMetaService.atualizarMetadados(imagemMetaId, metadadosStr);
		}
	}

	private String criaDadosDaVersaoDoDocumento(Documento documento) {
		Long documentoId = documento.getId();
		StatusDocumento status = documento.getStatus();
		String statusStr = messageService.getValue("StatusDocumento."+ status.name() +".label");
		ModeloDocumento modeloDocumento = documento.getModeloDocumento();
		String modeloDocumentoDescricao = modeloDocumento != null ? modeloDocumento.getDescricao() : "";
		Date dataDigitalizacao = documento.getDataDigitalizacao();
		Origem docOrigem = documento.getOrigem();

		String motivoRejeiteObs = "";
		String motivoRejeite = "";
		boolean pastaAmarela = false;
		Advertencia advertencia = documento.getAdvertencia();
		if(advertencia != null) {
			motivoRejeiteObs = advertencia.getObservacao();
			Irregularidade irregularidade = advertencia.getIrregularidade();
			if(irregularidade != null) {
				Long irregularidadeId = irregularidade.getId();
				irregularidade = irregularidadeService.get(irregularidadeId);
				motivoRejeite = irregularidade.getNome();
				pastaAmarela = irregularidade.getIrregularidadePastaAmarela();
			}
		}
		else if(StatusDocumento.PENDENTE.equals(status)) {

			//FIXME: isso está sendo chamado duas vezes por documento ao carregar a página
			Pendencia pendencia = pendenciaService.getLast(documentoId);
			if(pendencia != null) {
				motivoRejeiteObs = pendencia.getObservacao();
				Irregularidade irregularidade = pendencia.getIrregularidade();
				if(irregularidade != null) {
					motivoRejeite = irregularidade.getNome();
					pastaAmarela = irregularidade.getIrregularidadePastaAmarela();
				}
			}
		}

		Map<String, String> registrosBkpMap = new HashMap<>();
		registrosBkpMap.put(RegistrosDeDigitalizacaoVO.DATA_GIGITALIZACAO, DummyUtils.formatDateTime3(dataDigitalizacao));
		registrosBkpMap.put(RegistrosDeDigitalizacaoVO.ORIGEM, docOrigem != null ? docOrigem.name() : "");
		registrosBkpMap.put(RegistrosDeDigitalizacaoVO.STATUS, statusStr);
		registrosBkpMap.put(RegistrosDeDigitalizacaoVO.MOTIVO_REJEITO, motivoRejeite);
		registrosBkpMap.put(RegistrosDeDigitalizacaoVO.MOTIVO_REJEITO_OBS, motivoRejeiteObs);
		registrosBkpMap.put(RegistrosDeDigitalizacaoVO.PASTA_AMARELA, pastaAmarela ? "Sim" : "Não");
		registrosBkpMap.put(RegistrosDeDigitalizacaoVO.MODELO_DOCUMENTO, modeloDocumentoDescricao);

		String jsonStr = DummyUtils.objectToJson(registrosBkpMap);
		return DummyUtils.stringToJson(jsonStr);
	}

	public void agendarOcr(Documento documento, Usuario usuario) {

		ModeloOcr modeloOcr = documento.getModeloOcr();
		if(modeloOcr == null) {
			return;
		}

		Long documentoId = documento.getId();
		Imagem imagem = imagemService.getPrimeiraImagem(documentoId);

		logOcrService.agendarOcr(usuario, modeloOcr, imagem);
	}

	private Imagem digitalizarImagem(ImagemTransaction transaction, Documento documento, String dir, FileVO vo, String extensao, String fullText) throws IOException {

		extensao = extensao != null ? extensao : GetdocConstants.EXTENSAO_DEFINICAO_IMAGEM;

		Map<String, String> metadadosMap = vo.getMetadados();
		metadadosMap = metadadosMap != null ? metadadosMap : new LinkedHashMap<>();
		Date dataDigitalizacao = new Date();

		File tempFile = vo.getFile();
		long tamanhoArquivoByte = tempFile.length();
		metadadosMap.put("tamanhoOriginal", String.valueOf(tamanhoArquivoByte));
		metadadosMap.put("dataDigitalizacao", DummyUtils.formatDateTime3(dataDigitalizacao));

		String value = parametroService.getValor(ParametroService.P.COMPRIMIR_IMAGEM);
		boolean isComprimirImagem = StringUtils.isNotBlank(value) && Boolean.valueOf(value);

		if(GetdocConstants.IMAGEM_EXTENSOES.contains(extensao) && isComprimirImagem) {
			int limiteKb = GetdocConstants.TAMANHO_MAXIMO_IMAGEM_KB;
			int limiteByte = limiteKb * 1024;
			if(tamanhoArquivoByte > limiteByte) {
				ImageShrinker shrinker = new ImageShrinker(tempFile);
				shrinker.shrink(limiteKb);
				boolean isTempFile = DummyUtils.isTempFile(tempFile);
				if(isTempFile) {
					DummyUtils.deleteFile(tempFile);
				}
				tempFile = shrinker.getFile();
			}
		}

		Integer numeroPaginasPdf = null;
		if(StringUtils.equals(extensao, "pdf")){
			File file = tempFile.getAbsoluteFile();
			InputStream targetStream = new FileInputStream(file);
			PDDocument doc = PDDocument.load(targetStream);
			numeroPaginasPdf = doc.getNumberOfPages();
		}

		long tamanhoFinalByte = tempFile.length();
		metadadosMap.put("tamanhoFinal", String.valueOf(tamanhoFinalByte));

		Origem origemDocumento = documento.getOrigem();
		Imagem imagem = new Imagem();

		imagem.setDocumento(documento);
		imagem.setPreparada(false);
		imagem.setOrigem(origemDocumento);
		Integer versaoAtual = documento.getVersaoAtual();
		imagem.setVersao(versaoAtual);
		imagem.setExtensao(extensao);

		imagem.setHashChecksum("");
		imagemService.saveOrUpdate(imagem);

		Long imagemId = imagem.getId();
		long length = tempFile.length();

		ImagemMeta imagemMeta = new ImagemMeta();
		imagemMeta.setImagemId(imagemId);
		imagemMeta.setTamanho(length);
		imagemMeta.setDataDigitalizacao(dataDigitalizacao);
		imagemMeta.setPaginas(numeroPaginasPdf);
		imagemMetaService.saveOrUpdate(imagemMeta);

		if(fullText != null && !fullText.isEmpty()) {
			fullText = DummyUtils.substituirCaracteresEspeciais(fullText);
			fullText = StringUtils.trim(fullText);
			fullText = StringUtils.upperCase(fullText);

			imagemMetaService.updateFullText(imagemId, fullText);
		}

		String caminho = Imagem.gerarCaminho(dir, imagem);
		imagem.setCaminho(caminho);

		String dir_bkp = resourceService.getValue(ResourceService.CACHE_PATH);
		if(!dir_bkp.contains("imgfiles")){
			dir_bkp = dir_bkp + "imgfiles" + File.separator;
		}
		String caminho_bkp = Imagem.gerarCaminho(dir_bkp, imagem);

		File file = new File(caminho);
		File file_bkp = new File(caminho_bkp);
		try {
			FileUtils.copyFile(tempFile, file);
			FileUtils.copyFile(tempFile, file_bkp);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String hashChecksum1 = DummyUtils.getHashChecksum(tempFile);
		String hashChecksum2 = DummyUtils.getHashChecksum(file);
		if(!hashChecksum2.equals(hashChecksum1)) {
			throw new RuntimeException("Falha ao verificar o hash: ");
		}

		imagem.setHashChecksum(hashChecksum1);
		imagemService.saveOrUpdate(imagem);

		transaction.addDigitalizadas(caminho);
		transaction.isDigitalizadasExistem();

		String tipificadoDarknetStr = metadadosMap.get(CamposMetadadosTipificacao.DN_IMAGEM_TIPIFICADA.getCampo());
		String tipificadoVisionStr = metadadosMap.get(CamposMetadadosTipificacao.DN_IMAGEM_TIPIFICADA.getCampo());
		boolean tipificado = "true".equals(tipificadoDarknetStr) || "true".equals(tipificadoVisionStr);
		imagemMeta.setTipificado(tipificado);

		if (!tipificado) {
			imagemMeta.setPrecisaTipificar(precisaTipificarImagem(documento));
		}

		String json = DummyUtils.objectToJson(metadadosMap);
		imagemMeta.setMetaDados(json);
		imagemMetaService.saveOrUpdate(imagemMeta);

		transaction.addToDeleteOnRollback(caminho);

		return imagem;
	}

	private boolean precisaTipificarImagem(Documento documento) {

		ModeloDocumento modeloDocumento = documento.getModeloDocumento();
		TipoDocumento tipoDocumento = documento.getTipoDocumento();

		boolean tipoDocumentoSempreTipificar = tipoDocumento != null && tipoDocumento.getSempreTipificar();

		return modeloDocumento == null && (tipoDocumentoSempreTipificar || "OUTROS".equals(documento.getNome()));
	}

	public DownloadVO getDownload(Long documentoId, Integer versao) throws IOException {
		return getDownload(documentoId, versao, false);
	}

	public DownloadVO getDownload(Long documentoId, Integer versao, boolean atualizar) throws IOException {

		boolean unicaExtensao = imagemService.isUnicaExtensao(documentoId, versao, GetdocConstants.IMAGEM_EXTENSOES_DOWNLOAD);
		String extensaoDoc = unicaExtensao ? GetdocConstants.EXTENSAO_DEFINICAO_PDF : GetdocConstants.EXTENSAO_DEFINICAO_ZIP;
		String downloadPath = resourceService.getValue(ResourceService.DOCUMENTOS_PATH);
		Documento documento = documentoRepository.get(documentoId);
		downloadPath = Documento.gerarCaminho(downloadPath, extensaoDoc, documento, versao);

		File download = new File(downloadPath);
		Integer versaoAtual = documento.getVersaoAtual();

		if(versaoAtual.equals(versao) && !atualizar) {
			long lastModified = download.lastModified();
			Date ultimaModificacaoDoc = new Date(lastModified);
			Date lastDataAlteracao = documentoLogService.getLastDataAlteracao(documentoId);
			if(ultimaModificacaoDoc.before(lastDataAlteracao)){
				atualizar = true;
			}
		}

		String documentoNome = documento.getNome();
		boolean exists = download.exists();

		if(exists && !atualizar) {
			DownloadVO vo = new DownloadVO();
			vo.setFile(download);
			vo.setFileName(documentoNome + "." + extensaoDoc);

			return vo;
		} else {
			List<Imagem> imagens = imagemService.findByDocumentoVersao(documentoId, versao);

			if(unicaExtensao) {
				criaDocumentoPdf(download, imagens);
			} else {
				criaDocumentoZip(download, imagens);
			}

			DownloadVO vo = new DownloadVO();
			vo.setFile(download);
			vo.setFileName(documentoNome + "." + extensaoDoc);

			return vo;
		}
	}

	private void criaDocumentoPdf(File download, List<Imagem> imagens) throws IOException {

		List<String> caminhosImagens = new ArrayList<>();
		List<String> caminhosPdfs = new ArrayList<>();

		for (Imagem imagem : imagens) {
			String extensao = imagem.getExtensao();
			extensao = extensao.toLowerCase();
			File file = imagemService.getFile(imagem);
			if (GetdocConstants.EXTENSAO_DEFINICAO_PDF.equals(extensao)) {
				String caminho = file.getAbsolutePath();
				caminhosPdfs.add(caminho);
			} else {
				String caminho = file.getAbsolutePath();
				caminhosImagens.add(caminho);
			}
		}

		if(!caminhosImagens.isEmpty()) {
			Map<String, Object> model = new HashMap<>(1);
			model.put("caminhos", caminhosImagens);

			PDFCreator pdfCreator = new PDFCreator("/net/wasys/getdoc/pdf/pdf-documentos.htm", model);
			File pdf = pdfCreator.toFile();
			String pdfAbsolutePath = pdf.getAbsolutePath();
			caminhosPdfs.add(pdfAbsolutePath);
		}

		File fileTmp = File.createTempFile("merge-pdf", "." + GetdocConstants.EXTENSAO_DEFINICAO_PDF);
		String fileTmpAbsolutePath = fileTmp.getAbsolutePath();
		DummyUtils.mergePdf(caminhosPdfs, fileTmpAbsolutePath);
		FileUtil.copyFile(fileTmp, download);
		FileUtils.forceDelete(fileTmp);
	}

	private void criaDocumentoZip(File download, List<Imagem> imagens) throws IOException {

		List<String> caminhosArquivos = new ArrayList<>();

		for (Imagem imagem : imagens) {
			File file = imagemService.getFile(imagem);
			String caminho = file.getAbsolutePath();
			caminhosArquivos.add(caminho);
		}

		File fileTmp = File.createTempFile("compactado", "." + GetdocConstants.EXTENSAO_DEFINICAO_ZIP);
		String fileTmpAbsolutePath = fileTmp.getAbsolutePath();
		DummyUtils.compactarParaZip(fileTmpAbsolutePath, caminhosArquivos);
		FileUtil.copyFile(fileTmp, download);
		FileUtils.forceDelete(fileTmp);
	}

	public void cropImagem(Usuario usuario, Long documentoId, Long imagemId, Origem origem, int[] coordenadas) throws IOException {

		Imagem imagem = imagemService.get(imagemId);
		ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);

		File file = imagemService.getFile(imagem);

		BufferedImage imageAtual = ImageIO.read(file);
		int x = coordenadas[0];
		int y = coordenadas[1];
		int w = coordenadas[2];
		int h = coordenadas[3];
		BufferedImage imagemCortada = imageAtual.getSubimage(x, y, w, h);

		Documento documento = get(documentoId);

		Imagem imagem2 = new Imagem();

		imagem2.setDocumento(documento);
		imagem2.setExtensao("jpg");
		String hashChecksum = DummyUtils.getHashChecksum(file);
		imagem2.setHashChecksum(hashChecksum);

		imagemService.saveOrUpdate(imagem2);

		ImagemMeta imagemMeta2 = imagemMeta.clone();
		Long imagemId2 = imagem2.getId();
		imagemMeta2.setImagemId(imagemId2);

		imagemMetaService.saveOrUpdate(imagemMeta2);

		String caminho2 = imagemService.gerarCaminho(imagem2);
		imagem2.setCaminho(caminho2);
		File file2 = new File(caminho2);

		try {
			ImageIO.write(imagemCortada, "jpg", file2);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		String hashChecksum2 = DummyUtils.getHashChecksum(file2);
		imagem2.setHashChecksum(hashChecksum2);
		imagemService.saveOrUpdate(imagem2);

		documentoLogService.criaLog(documento, usuario, AcaoDocumento.RECORTE_IMAGEM);
	}

	@Transactional(rollbackFor=Exception.class)
	public void copiarImagem(Imagem imagem, Documento destino, Usuario usuario, boolean mover) {
		copiarImagem(imagem, destino, usuario, mover, false, true);
	}

	@Transactional(rollbackFor=Exception.class)
	public void copiarImagem(Imagem imagem, Documento destino, Usuario usuario, boolean mover, boolean ignorarImagemInexistente, boolean permitirTrocarStatus) {

		String hashChecksum = imagem.getHashChecksum();
		File file = imagemService.getFile(imagem);
		String extensao = imagem.getExtensao();
		Origem origem = imagem.getOrigem();

		Long imagemId = imagem.getId();
		ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);

		Imagem imagem2 = new Imagem();
		imagem2.setDocumento(destino);
		imagem2.setHashChecksum(hashChecksum);
		imagem2.setExtensao(extensao);
		imagem2.setOrigem(origem);

		Date dataDigitalizacao = destino.getDataDigitalizacao();
		if(dataDigitalizacao == null) {
			destino.setDataDigitalizacao(new Date());
		}
		Integer versaoAtual = destino.getVersaoAtual();
		if(versaoAtual == null || versaoAtual.equals(0)) {
			versaoAtual = 1;
			destino.setVersaoAtual(1);
		} else if (!permitirTrocarStatus){
			versaoAtual += 1;
			destino.setVersaoAtual(versaoAtual);
		}
		imagem2.setVersao(versaoAtual);

		StatusDocumento status = destino.getStatus();
		if(permitirTrocarStatus && (StatusDocumento.EXCLUIDO.equals(status) || StatusDocumento.INCLUIDO.equals(status) || StatusDocumento.APROVADO.equals(status))) {
			destino.setStatus(StatusDocumento.DIGITALIZADO);
		}
		destino.setOrigem(origem);
		documentoRepository.saveOrUpdate(destino);

		imagemService.saveOrUpdate(imagem2);

		ImagemMeta imagemMeta2 = imagemMeta.clone();
		Long imagemId2 = imagem2.getId();
		imagemMeta2.setImagemId(imagemId2);
		imagemMetaService.saveOrUpdate(imagemMeta2);

		String caminho2 = imagemService.gerarCaminho(imagem2);
		imagem2.setCaminho(caminho2);
		File file2 = new File(caminho2);

		if (ignorarImagemInexistente && !file.exists()) {
			systraceThread("Arquivo inexistente ignorado. Caminho=" + imagem.getCaminho());
		}
		else {
			try {
				FileUtils.copyFile(file, file2);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}

			if (StringUtils.isNotBlank(hashChecksum)) {
				String hashChecksum2 = DummyUtils.getHashChecksum(file2);
				if (!hashChecksum2.equals(hashChecksum)) {
					throw new RuntimeException("Falha ao verificar o hash");
				}
			}
		}

		imagemService.saveOrUpdate(imagem2);

		if(mover) {
			excluirImagem(imagem, usuario, false);

			Documento documento = imagem.getDocumento();
			documentoLogService.criaLog(documento, usuario, AcaoDocumento.MOVEU_IMAGEM);
			Advertencia advertencia = documento.getAdvertencia();
			if(advertencia != null) {
				atualizarAdvertencia(documento, advertencia);
				documentoRepository.saveOrUpdate(documento);
			}
		}

		documentoLogService.criaLog(destino, usuario, AcaoDocumento.COPIOU_IMAGEM);

		Processo processo = destino.getProcesso();
		processoRegraService.reprocessaRegrasAoAtualizarDocumentos(processo, usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void copiarImagensIsencaoDisciplina(List<Imagem> imagens, Documento destino, Usuario usuario, boolean ignorarImagemInexistente, Integer versaoAtual) {

		for(Imagem imagem : imagens) {
			Documento documentoOrigem = imagem.getDocumento();
			String hashChecksum = imagem.getHashChecksum();
			File file = imagemService.getFile(imagem);
			String extensao = imagem.getExtensao();
			Origem origem = imagem.getOrigem();

			Long imagemId = imagem.getId();
			ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);

			Imagem imagem2 = new Imagem();
			imagem2.setDocumento(destino);
			imagem2.setHashChecksum(hashChecksum);
			imagem2.setExtensao(extensao);
			imagem2.setOrigem(origem);

			Date dataDigitalizacao = documentoOrigem.getDataDigitalizacao();
			destino.setDataDigitalizacao(dataDigitalizacao);
			imagem2.setVersao(versaoAtual);
			destino.setOrigem(origem);
			documentoRepository.saveOrUpdate(destino);

			imagemService.saveOrUpdate(imagem2);

			ImagemMeta imagemMeta2 = imagemMeta.clone();
			Long imagemId2 = imagem2.getId();
			imagemMeta2.setImagemId(imagemId2);
			imagemMetaService.saveOrUpdate(imagemMeta2);

			String caminho2 = imagemService.gerarCaminho(imagem2);
			imagem2.setCaminho(caminho2);
			File file2 = new File(caminho2);

			if (ignorarImagemInexistente && !file.exists()) {
				systraceThread("Arquivo inexistente ignorado. Caminho=" + imagem.getCaminho());
			} else {
				try {
					FileUtils.copyFile(file, file2);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				if (StringUtils.isNotBlank(hashChecksum)) {
					String hashChecksum2 = DummyUtils.getHashChecksum(file2);
					if (!hashChecksum2.equals(hashChecksum)) {
						throw new RuntimeException("Falha ao verificar o hash");
					}
				}
			}

			imagemService.saveOrUpdate(imagem2);

			documentoLogService.criaLog(destino, usuario, AcaoDocumento.COPIOU_IMAGEM);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluirRegistroDeImagemFerrada(Imagem imagem, Usuario usuario, String observacao) {
		Documento documento = imagem.getDocumento();
		Integer versao = imagem.getVersao();

		StringBuilder sb = new StringBuilder();
		sb.append(observacao);
		sb.append("\n\n ### Imagem excluída na versão: ").append(versao).append(" ###");
		documentoLogService.criaLog(documento, usuario, AcaoDocumento.EXCLUIU_IMAGEM, sb.toString());

		imagemService.excluir(imagem);

		Long documentoId = documento.getId();
		Integer countImagens = imagemService.countByDocumento(documentoId);
		if (countImagens == 0) {
			documento.setStatus(StatusDocumento.INCLUIDO);
			saveOrUpdate(documento);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluirImagem(Imagem imagem, Usuario usuario, boolean processarRegras) {

		systraceThread("Excludindo imagem: " + imagem);
		Documento documento = imagem.getDocumento();
		Integer versao = imagem.getVersao();
		documentoLogService.criaLog(documento, usuario, AcaoDocumento.EXCLUIU_IMAGEM);

		File file = imagemService.getFile(imagem);

		imagemService.excluir(imagem);

		Long documentoId = documento.getId();
		Integer versaoAtual = documento.getVersaoAtual();

		Integer countImagens = imagemService.countByDocumento(documentoId, versaoAtual);

		if(versao.equals(versaoAtual)) {
			if (countImagens > 0) {
				documento.setStatus(StatusDocumento.DIGITALIZADO);
				saveOrUpdate(documento);
			} else if(countImagens == 0) {
				documento.setStatus(StatusDocumento.INCLUIDO);
				saveOrUpdate(documento);
			}
		}

		DummyUtils.deleteFile(file);

		if(processarRegras) {
			Processo processo = documento.getProcesso();
			processoRegraService.reprocessaRegrasAoAtualizarDocumentos(processo, usuario);
		}
	}

	public DocumentoVO createVOBy(Documento documento, Usuario usuario, String imageRootPath) {

		Long documentoId = documento.getId();
		String nome = documento.getNome();
		Long processoId = documento.getProcesso().getId();
		StatusDocumento status = documento.getStatus();
		boolean obrigatorio = documento.getObrigatorio();
		Date dataDigitalizacao = documento.getDataDigitalizacao();
		Date validadeExpiracao = documento.getValidadeExpiracao();
		Integer versaoAtual = documento.getVersaoAtual();
		StatusOcr statusOcr = documento.getStatusOcr();
		StatusFacial statusFacial = documento.getStatusFacial();
		boolean reconhecimentoFacial = documento.getReconhecimentoFacial();
		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		Advertencia advertencia = documento.getAdvertencia() != null ? documento.getAdvertencia() : null;
		boolean sempreTipificar = tipoDocumento != null ? tipoDocumento.getSempreTipificar() : false;
		Map<Long, String> modelosDocumentosMap = new LinkedHashMap<>();
		Map<Long, String> modelosDocumentosToRequisitarExpiracaoMap = new LinkedHashMap<>();
		if(sempreTipificar) {
			Long tipoDocumentoId = tipoDocumento.getId();
			List<ModeloDocumento> modelosDocumentos = tipoDocumentoService.findModelosDocumentos(tipoDocumentoId);
			List<ModeloDocumento> modelosDocumentoToRequisitarExpiracao = tipoDocumentoService.findModelosDocumentoToRequisitarExpiracao(tipoDocumentoId);
			boolean requisitarDataExpiracaoPorModeloDocumento = tipoDocumento.getRequisitarDataPorModeloDocumento();
			boolean ehRequisitarDataExpiracaoPraTodosModelos = modelosDocumentoToRequisitarExpiracao.isEmpty() && requisitarDataExpiracaoPorModeloDocumento;
			for (ModeloDocumento md : modelosDocumentos) {
				Long modeloDocumentoId = md.getId();
				String modeloDocumentoDescricao = md.getDescricao();
				modelosDocumentosMap.put(modeloDocumentoId, modeloDocumentoDescricao);

				if(ehRequisitarDataExpiracaoPraTodosModelos || modelosDocumentoToRequisitarExpiracao.remove(md)) {
					modelosDocumentosToRequisitarExpiracaoMap.put(modeloDocumentoId, modeloDocumentoDescricao);
				}
			}
		}
		ModeloDocumento modeloDocumento = documento.getModeloDocumento();
		Long modeloDocumentoId = modeloDocumento != null ? modeloDocumento.getId() : null;
		Origem origem = documento.getOrigem();
		boolean grupoRelacionadoFoiApagado = grupoRelacionadoFoiApagado(documento);
		boolean isDocumentoOutros = documento.isOutros();

		List<Imagem> imagens = imagemService.findByDocumento(documentoId);

		boolean justificavel = podeJustificar(documento, usuario);
		boolean digitalizavel = podeDigitalizar(documento, usuario);
		boolean aprovavel = podeAprovar(documento, usuario);
		boolean rejeitavel = podeRejeitar(documento, usuario);
		boolean reindexavel = podeReindexar(documento, usuario, imagens);
		boolean podeCopiar = true;
		boolean podeMover = true;
		boolean podeExcluir = podeExcluir(usuario);
		boolean podeCortar = true;
		boolean requisitarDataValidadeExpiracao = tipoDocumento == null? false : tipoDocumento.getRequisitarDataValidadeExpiracao();
		boolean requisitarDataEmissao = tipoDocumento == null? false : tipoDocumento.getRequisitarDataEmissao();
		boolean requisitarDataValidadeExpiracaoPorModelo = tipoDocumento == null ? false : tipoDocumento.getRequisitarDataPorModeloDocumento();
		boolean possuiFulltext = false;

		if(reindexavel) {
			if(imagens.size() > 1) {
				if(usuario.isAdminRole()) {
					podeExcluir = true;
				}
			}
		}

		Map<Long, ImagemMeta> imagemMetaMap = new HashMap<>();
		ImagemMetaFiltro filro = new ImagemMetaFiltro();
		filro.setDocumentoId(documentoId);
		List<ImagemMeta> imagensMeta = imagemMetaService.findByFiltro(filro);
		for (ImagemMeta imagemMeta : imagensMeta) {
			Long imagemId = imagemMeta.getImagemId();
			imagemMetaMap.put(imagemId, imagemMeta);
		}

		for (Imagem imagem : imagens) {
			Long id = imagem.getId();
			ImagemMeta imagemMeta = imagemMetaMap.get(id);
			String fullText = imagemMeta != null ? imagemMeta.getFullText() : null;
			if (StringUtils.isNotBlank(fullText)) {
				possuiFulltext = true;
			}
		}

		boolean salaMatriculaRole = usuario.isSalaMatriculaRole();
		if(salaMatriculaRole) {
			podeCopiar = false;
			podeMover = false;
		}

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		Long subperfilAtivoId = subperfilAtivo != null ? subperfilAtivo.getId() : null;

		if(subperfilAtivoId != null) {
			if (validaSubperfilRestritos(subperfilAtivoId)) {
				podeCopiar = false;
				podeMover = false;
			}
		}

		Map<Integer, List<ImagemVO>> imagens2 = new TreeMap<>();
		for (Imagem imagem : imagens) {

			String caminho = Imagem.gerarCaminho(imageRootPath, imagem, "/");
			String extensao = imagem.getExtensao();
			if("pdf".equals(extensao)) {
				podeCortar = false;
			}

			Long imagemId = imagem.getId();
			Integer versao = imagem.getVersao();
			String hash = imagem.getHashChecksum();
			Boolean existente = imagem.isExistente();
			existente = existente == null ? true : existente;

			ImagemMeta imagemMeta = imagemMetaMap.get(imagemId);
			RegistrosDeDigitalizacaoVO registrosDeDigitalizacaoVO = getRegistrosDeDigitalizacaoVO(imagemMeta);
			if(versao == versaoAtual) {
				String registrosDeDigitalizacao = criaDadosDaVersaoDoDocumento(documento);
				registrosDeDigitalizacaoVO = StringUtils.isNotBlank(registrosDeDigitalizacao) ? DummyUtils.jsonToObject(registrosDeDigitalizacao, RegistrosDeDigitalizacaoVO.class) : null;
			}

			ImagemVO imagemVO = new ImagemVO();
			imagemVO.setId(imagemId);
			imagemVO.setCaminho(caminho);
			imagemVO.setVersao(versao);
			imagemVO.setHash(hash);
			imagemVO.setExistente(existente);
			imagemVO.setRegistrosDeDigitalizacao(registrosDeDigitalizacaoVO);

			List<ImagemVO> list2 = imagens2.get(versao);
			list2 = list2 != null ? list2 : new ArrayList<ImagemVO>();
			imagens2.put(versao, list2);
			list2.add(imagemVO);
		}

		DocumentoVO vo = new DocumentoVO();
		vo.setId(documentoId);
		vo.setNome(nome);
		vo.setProcessoId(processoId);
		vo.setStatus(status);
		vo.setObrigatorio(obrigatorio);
		vo.setJustificavel(justificavel);
		vo.setDigitalizavel(digitalizavel);
		vo.setAprovavel(aprovavel);
		vo.setRejeitavel(rejeitavel);
		vo.setPodeCopiar(podeCopiar);
		vo.setPodeCortar(podeCortar);
		vo.setPodeMover(podeMover);
		vo.setPodeExcluir(podeExcluir);
		vo.setDataDigitalizacao(dataDigitalizacao);
		vo.setValidadeExpiracao(validadeExpiracao);
		vo.setVersaoAtual(versaoAtual);
		vo.setStatusOcr(statusOcr);
		vo.setStatusFacial(statusFacial);
		vo.setFaceReconhecida(reconhecimentoFacial);
		vo.setTipoDocumento(tipoDocumento);
		vo.setImagens(imagens2);
		vo.setSempreTipificar(sempreTipificar);
		vo.setPossiveisModelosDocumento(modelosDocumentosMap);
		vo.setModeloDocumentoId(modeloDocumentoId);
		vo.setOrigem(origem);
		vo.setGrupoRelacionadoApagado(grupoRelacionadoFoiApagado);
		vo.setRequisitarDataValidadeExpiracao(requisitarDataValidadeExpiracao);
		vo.setRequisitarDataEmissao(requisitarDataEmissao);
		vo.setRequisitarDataPorModelo(requisitarDataValidadeExpiracaoPorModelo);
		vo.setModelosDocumentoValidarExpiracao(modelosDocumentosToRequisitarExpiracaoMap);
		vo.setPossuiFullText(possuiFulltext);
		vo.setDocumentoOutros(isDocumentoOutros);

		//FIXME: isso está sendo chamado duas vezes por documento ao carregar a página
		Pendencia pendencia = pendenciaService.getLast(documentoId);
		if(pendencia != null) {

			Irregularidade irregularidade = pendencia.getIrregularidade();
			Long irregularidadeId = irregularidade.getId();
			String irregularidadeNome = irregularidade.getNome();
			String pendenciaObservacao = pendencia.getObservacao();
			String pendenciaJustificativa = pendencia.getJustificativa();

			vo.setPendenciaObservacao(pendenciaObservacao);
			vo.setIrregularidadeId(irregularidadeId);
			vo.setIrregularidadeNome(irregularidadeNome);
			vo.setPendenciaJustificativa(pendenciaJustificativa);
		}

		if(advertencia != null) {
			String irregularidadeNome = advertencia.getIrregularidade().getNome();
			vo.setIrregularidadeNome(irregularidadeNome);
			vo.setExisteAdvertencia(true);
		}

		return vo;
	}

	private RegistrosDeDigitalizacaoVO getRegistrosDeDigitalizacaoVO(ImagemMeta imagemMeta) {
		RegistrosDeDigitalizacaoVO registrosDeDigitalizacaoVO = null;
		if(imagemMeta != null) {
			String metaDados = imagemMeta.getMetaDados();
			Map<String, String> metadadosMap = (Map<String, String>) DummyUtils.jsonStringToMap(metaDados);
			String registrosDeDigitalizacao = metadadosMap != null ? metadadosMap.get(CamposMetadadosTipificacao.REGISTRO_DE_DIGITALIZACAO.getCampo()) : null;
			registrosDeDigitalizacaoVO = StringUtils.isNotBlank(registrosDeDigitalizacao) ? DummyUtils.jsonToObject(registrosDeDigitalizacao, RegistrosDeDigitalizacaoVO.class) : null;
		}
		return registrosDeDigitalizacaoVO;
	}

	private boolean grupoRelacionadoFoiApagado(Documento documento) {

		boolean grupoRelacionadoFoiApagado = false;

		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		if (tipoDocumento != null) {

			Long tipoDocumentoId = tipoDocumento.getId();
			String nome = documento.getNome();
			if ((TipoDocumento.DOCUMENTOS_MEMBRO_FAMILIAR_PROUNI.contains(tipoDocumentoId)
					|| TipoDocumento.DOCUMENTOS_MEMBRO_FAMILIAR_FIES.contains(tipoDocumentoId)
						|| TipoDocumento.DOCUMENTOS_MEMBRO_FAMILIAR_TE_FIES.contains(tipoDocumentoId)
							|| TipoDocumento.DOCUMENTOS_MEMBRO_FAMILIAR_TE_PROUNI.contains(tipoDocumentoId))
					&& StringUtils.contains(nome, "MEMBRO FAMILIAR")) {
				grupoRelacionadoFoiApagado = campoGrupoService.grupoRelacionadoFoiApagado(documento);
			}
		}

		return grupoRelacionadoFoiApagado;
	}

	public List<DocumentoVO> findVOsByProcesso(Long processoId, Usuario usuario, String imageRootPath) {

		Processo processo = processoService.get(processoId);

		DocumentoFiltro filtro = new DocumentoFiltro();
		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		if(subperfilAtivo != null) {
			Long subperfilAtivoId = subperfilAtivo.getId();
			subperfilAtivo = subperfilService.get(subperfilAtivoId);
			List<Long> tiposDocumentosIds = tipoDocumentoService.findIdsBySuperfilAndProcessoId(subperfilAtivo, processoId);
			filtro.setTipoDocumentoIdList(tiposDocumentosIds);
		}

		filtro.setProcesso(processo);
		filtro.setFetch(Arrays.asList(DocumentoFiltro.Fetch.TIPO_DOCUMENTO));
		List<Documento> documentos = documentoRepository.findByFiltro(filtro, null, null);

		List<DocumentoVO> list = new ArrayList<>();
		for (Documento documento : documentos) {
			DocumentoVO vo = createVOBy(documento, usuario, imageRootPath);
			if(isAtivoTipoDocumento(vo)){
				list.add(vo);
			}
		}

		return list;
	}

	@Transactional(rollbackFor=Exception.class)
	public List<Documento> deletarDocumentosDinamicos(ImagemTransaction transaction, CampoGrupo grupo) {

		List<Documento> documentosRemovidos = new ArrayList<>();

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setGrupoRelacionado(grupo);
		List<Documento> documentosRelacionados = findByFiltro(filtro, null, null);
		for(Documento documento : documentosRelacionados) {

			excluirPermantentemente(transaction, documento);
			documentosRemovidos.add(documento);
		}

		return documentosRemovidos;
	}

	public List<Documento> findNaoAprovadosByProcesso(Long processoId) {
		return documentoRepository.findNaoAprovadosByProcesso(processoId);
	}

	public List<TipoDocumento> getByProcessoTipoDocumento(Long processoId, List<Long> tipoDocumentoIdList) {
		return documentoRepository.getByProcessoTipoDocumento(processoId, tipoDocumentoIdList);
	}

    public Documento getFirstByTipoDocumentoId(Long tipoDocumentoId, Long processoId){
	    return documentoRepository.getFirstByTipoDocumentoId(tipoDocumentoId, processoId);
    }

	public Documento getFirstByTipoDocumentoIdMembroFamiliar(Long tipoDocumentoId, Long processoId, String membroFamiliar){
		return documentoRepository.getFirstByTipoDocumentoIdMembroFamiliar(tipoDocumentoId, processoId, membroFamiliar);
	}

	public boolean temPendenteDigitalizado(List<Long> processosId) {
		return documentoRepository.temPendenteDigitalizado(processosId);
	}

	public boolean temNaoAprovado(List<Long> processosId) {
		return documentoRepository.temNaoAprovado(processosId);
	}

	public boolean temPendente(List<Long> processosId) {
		return documentoRepository.temPendente(processosId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void aprovar(Documento documento, Usuario usuario) throws MessageKeyException {

		ModeloDocumento modeloDocumento = documento.getModeloDocumento();
		aprovar(documento, modeloDocumento, usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void aprovar(Long documentoId, Long modeloDocumentoId, Usuario usuario) throws MessageKeyException {

		Documento documento = get(documentoId);
		ModeloDocumento modeloDocumento = modeloDocumentoId != null ? modeloDocumentoService.get(modeloDocumentoId) : null;

		aprovar(documento, modeloDocumento, usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void aprovar(Documento documento, ModeloDocumento modeloDocumento, Usuario usuario) {

		if(!podeAprovar(documento, usuario)) {
			throw new MessageKeyException("documentoNaoAprovavel.error");
		}

		String observacao = null;
		ModeloDocumento modeloDocumentoOld = documento.getModeloDocumento();
		if(modeloDocumento != null && !DummyUtils.equals(modeloDocumentoOld, modeloDocumento)) {
			String modeloDocumentoDescricaoOld = modeloDocumentoOld != null ? modeloDocumentoOld.getDescricao() : "null";
			String modeloDocumentoDescricao = modeloDocumento != null ? modeloDocumento.getDescricao() : "null";
			observacao = "Alterou Modelo de Documento\nde: " + modeloDocumentoDescricaoOld + "\npara: " + modeloDocumentoDescricao + ".";

			documento.setModeloDocumento(modeloDocumento);
		}

		documentoLogService.criaLog(documento, usuario, AcaoDocumento.APROVOU, observacao);

		Long documentoId = documento.getId();
		Pendencia pendencia = pendenciaService.getLast(documentoId);
		if(pendencia != null) {
			pendencia.setDataFinalizacao(new Date());
			pendenciaService.saveOrUpdate(pendencia);
		}

		Advertencia advertencia = documento.getAdvertencia();
		if(advertencia != null) {
			advertencia.setDataFinalizacao(new Date());
			advertenciaService.saveOrUpdate(advertencia);
			documento.setAdvertencia(null);
		}

		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		int validadeExpiracao = (tipoDocumento == null? 0 : tipoDocumento.getValidadeExpiracao());
		Date dataEmissao = documento.getDataEmissao();
		//Adição de data de expiração do documento baseado na validade fixa cadastrada pelo usuário
		if(tipoDocumento != null && validadeExpiracao > 0 && dataEmissao != null) {
			Date dataExpiracao;
			dataExpiracao = DateUtils.addMonths(dataEmissao, validadeExpiracao);
			documento.setValidadeExpiracao(dataExpiracao);
		}

		documento.setStatus(StatusDocumento.APROVADO);
		documentoRepository.saveOrUpdate(documento);

		Processo processo = documento.getProcesso();
		processoRegraService.reprocessaRegrasAoAtualizarDocumentos(processo, usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void aprovarComAvertencia(Long documentoId, Usuario usuario, String observacaoIrregularidade, Irregularidade irregularidade) {

		Documento documento = get(documentoId);
		if(!podeRejeitar(documento, usuario)) {
			throw new MessageKeyException("documentoNaoAprovavel.error");
		}

		Advertencia advertenciaAnterior = documento.getAdvertencia();
		if(advertenciaAnterior != null) {
			atualizarAdvertencia(documento, advertenciaAnterior);
		}

		Advertencia advertencia = new Advertencia();
		advertencia.setDataCriacao(new Date());
		advertencia.setIrregularidade(irregularidade);
		advertencia.setObservacao(observacaoIrregularidade);

		advertenciaService.saveOrUpdate(advertencia);

		String observacao;

		observacao = "Rejeito: " + irregularidade.getNome();

		documentoLogService.criaLog(documento, usuario, AcaoDocumento.APROVOU_PARCIALMENTE, observacao);

		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		Long tipoDocumentoId = tipoDocumento.getId();
		tipoDocumento = tipoDocumentoService.get(tipoDocumentoId);

		documento.setAdvertencia(advertencia);
		documento.setStatus(StatusDocumento.APROVADO);
		documentoRepository.saveOrUpdate(documento);
	}

	public void justificar(Long documentoId, Usuario usuario, String observacao) throws MessageKeyException {

		Documento documento = get(documentoId);
		if(usuario != null && !podeJustificar(documento, usuario)) {
			throw new MessageKeyException("documentoNaoJustificavel.error");
		}

		Pendencia pendencia = pendenciaService.getLast(documentoId);
		pendencia.setJustificativa(observacao);
		pendenciaService.saveOrUpdate(pendencia);

		documentoLogService.criaLog(pendencia, usuario, AcaoDocumento.JUSTIFICOU);

		Integer countImagens = imagemService.countByDocumento(documentoId);
		documento.setStatus(countImagens == 0 ? StatusDocumento.INCLUIDO : StatusDocumento.DIGITALIZADO);

		documentoRepository.saveOrUpdate(documento);
	}

	public void alterarObrigatoriedade(Long documentoId, Usuario usuario, boolean obrigatoriedadeDocumento) throws MessageKeyException {

		Documento documento = get(documentoId);
		documento.setObrigatorio(obrigatoriedadeDocumento);

		StringBuilder observacao = new StringBuilder();
		if(obrigatoriedadeDocumento) {
			observacao.append(" Alterado obrigatoriedade de NÃO OBRIGATÓRIO para OBRIGATÓRIO <br>");
		} else {
			observacao.append(" Alterado obrigatoriedade de OBRIGATÓRIO para NÃO OBRIGATÓRIO <br>");
		}

		Integer countImagens = imagemService.countByDocumento(documentoId);
		if(countImagens == 0  && !obrigatoriedadeDocumento) {
			documento.setStatus(StatusDocumento.EXCLUIDO);
			observacao.append(" Alterado Status de " + StatusDocumento.INCLUIDO + " para " + StatusDocumento.EXCLUIDO).append(" <br> ");
		}

		documentoLogService.criaLog(documento, usuario, AcaoDocumento.ALTERACAO_OBRIGATORIEDADE, observacao.toString());
		documentoRepository.saveOrUpdate(documento);
	}

	@Transactional(rollbackFor=Exception.class)
	public void rejeitar(Long documentoId, Irregularidade irregularidade, Usuario usuario, String observacao) throws MessageKeyException {

		Documento documento = get(documentoId);
		if(!podeRejeitar(documento, usuario)) {
			throw new MessageKeyException("documentoNaoRejeitavel.error");
		}

		rejeitarSemValidacao(documento, irregularidade, observacao, usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void rejeitarSemValidacao(Documento documento, Irregularidade irregularidade, String observacao, Usuario usuario) throws MessageKeyException {

		StringBuilder obs = new StringBuilder();
		if(StringUtils.isNotBlank(observacao)) {
			String nome = irregularidade.getNome();
			obs.append(nome).append(" \n\n");
			obs.append("Observação: \n").append(observacao);
		}

		Pendencia pendencia = new Pendencia();
		pendencia.setDataCriacao(new Date());
		pendencia.setDocumento(documento);
		pendencia.setIrregularidade(irregularidade);
		pendencia.setObservacao(observacao);
		pendencia.setNotificadoAtila(false);
		pendenciaService.saveOrUpdate(pendencia);

		Advertencia advertencia = documento.getAdvertencia();
		if(advertencia != null) {
			atualizarAdvertencia(documento, advertencia);
		}

		documentoLogService.criaLog(pendencia, usuario, AcaoDocumento.REJEITOU);

		documento.setStatus(StatusDocumento.PENDENTE);
		documentoRepository.saveOrUpdate(documento);

		Processo processo = documento.getProcesso();
		processoRegraService.reprocessaRegrasAoAtualizarDocumentos(processo, usuario);
	}

	private void atualizarAdvertencia(Documento documento, Advertencia advertencia) {
		advertencia.setDataFinalizacao(new Date());
		advertenciaService.saveOrUpdate(advertencia);
		documento.setAdvertencia(null);
	}

	public boolean podeExcluir(Usuario usuario) {

		if (usuario == null) {
			return false;
		}

		if(usuario.isPodeExcluirImagem() || usuario.isAdminRole()) {
			return true;
		}
		return false;
	}


	public boolean podeAprovar(Documento documento, Usuario usuario) {

		if (usuario == null) {
			return false;
		}

		boolean salaMatriculaRole = usuario.isSalaMatriculaRole();
		if(salaMatriculaRole) {
			return false;
		}

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		Long subperfilAtivoId = subperfilAtivo != null ? subperfilAtivo.getId() : null;
		if(subperfilAtivoId != null) {
			if (validaSubperfilRestritos(subperfilAtivoId)) {
				return false;
			}
		}

		Integer versaoAtual = documento.getVersaoAtual();
		if(versaoAtual == 0){
			return false;
		}

		Processo processo = documento.getProcesso();

		StatusDocumento statusDocumento = documento.getStatus();
		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		boolean aceiteContrato = tipoDocumento != null ? tipoDocumento.getAceiteContrato() : false;
		if(aceiteContrato && !StatusDocumento.APROVADO.equals(statusDocumento)) {
			return true;
		}

		if (usuario.isAdminRole() && !StatusDocumento.APROVADO.equals(statusDocumento)) {
			return true;
		}

		StatusProcesso statusProcesso = processo.getStatus();
		if(StatusProcesso.RASCUNHO.equals(statusProcesso)
				|| StatusProcesso.CONCLUIDO.equals(statusProcesso)
				|| StatusProcesso.CANCELADO.equals(statusProcesso)) {
			return false;
		}

		if(processo.isSisFiesOrSisProuni() && (Subperfil.SUBPERFIS_FIES_PROUNI_IDS.contains(subperfilAtivoId) || usuario.isAdminRole()) && !StatusDocumento.APROVADO.equals(statusDocumento)) {
			return true;
		}

		Advertencia advertencia = documento.getAdvertencia();
		if(advertencia != null && statusDocumento.equals(StatusDocumento.APROVADO)) {
			return true;
		}

		if(!StatusDocumento.DIGITALIZADO.equals(statusDocumento)
				&& !StatusDocumento.PENDENTE.equals(statusDocumento)) {
			return false;
		}

		if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeJustificar(Documento documento, Usuario usuario) {

		return false;

		/*COMENTADO CASO TENHA QUE SER FEITO ROLLBACK NA LÓGICA DE JUSTIFICAR

		Processo processo = documento.getProcesso();
		StatusProcesso statusProcesso = processo.getStatus();
		if(StatusProcesso.RASCUNHO.equals(statusProcesso)
				|| StatusProcesso.CONCLUIDO.equals(statusProcesso)
				|| StatusProcesso.CANCELADO.equals(statusProcesso)) {
			return false;
		}

		StatusDocumento status = documento.getStatus();
		if(!StatusDocumento.PENDENTE.equals(status)) {
			return false;
		}

		if(usuario != null && Usuario.CLIENTE_ID.equals(usuario.getId())) {
			return true;
		}
		if(usuario != null && usuario.isAdminRole()) {
			return true;
		}
		else if(usuario != null && usuario.isGestorRole()) {
			return true;
		}
		else if(usuario != null && usuario.isAnalistaRole()) {
			return true;
		}
		else if(usuario == null || usuario.isRequisitanteRole() || usuario.isAreaRole()) {

			Usuario autor = processo.getAutor();
			if(usuario != null && !usuario.equals(autor)) {
				return false;
			}

			if(StatusProcesso.RASCUNHO.equals(statusProcesso)) {
				return true;
			}
			else if(StatusProcesso.PENDENTE.equals(statusProcesso)) {
				return true;
			}
		}

		return false;*/
	}

	public boolean podeRejeitar(Documento documento, Usuario usuario) {

		if (usuario == null) {
			return false;
		}

		boolean salaMatriculaRole = usuario.isSalaMatriculaRole();
		if(salaMatriculaRole) {
			return false;
		}

		Subperfil subperfilAtivo = usuario.getSubperfilAtivo();
		Long subperfilAtivoId = subperfilAtivo != null ? subperfilAtivo.getId() : null;
		if(subperfilAtivoId != null) {
			if (validaSubperfilRestritos(subperfilAtivoId)) {
				return false;
			}
		}

		Processo processo = documento.getProcesso();

		StatusDocumento status = documento.getStatus();
		if(StatusDocumento.INCLUIDO.equals(status)) {
			Campanha campanha = processo.getCampanha();
			Long campanhaId = campanha.getId();
			campanha = campanhaService.get(campanhaId);
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if(tipoDocumento == null) {
				return false;
			}
			Long tipoDocumentoId = tipoDocumento.getId();
			Map<Long, List<Long>> equivalenciasMap = new HashMap<>();
			campanhaService.carregaObrigatoriosAndEquivalencias(campanha, null, null, equivalenciasMap);
			List<StatusDocumento> statusInicial = Arrays.asList(StatusDocumento.INCLUIDO, StatusDocumento.EXCLUIDO);
			Set<Long> equivalencias = equivalenciasMap.keySet();
			for (Long equivalenciasId : equivalencias) {
				List<Long> equivalencia = equivalenciasMap.get(equivalenciasId);
				for (Long equivalenciaId : equivalencia) {
					if (equivalenciaId.equals(tipoDocumentoId)) {
						Long processoId = processo.getId();
						Documento documento1 = getFirstByTipoDocumentoId(equivalenciaId, processoId);
						StatusDocumento documento1Status = documento1.getStatus();
						if (statusInicial.contains(documento1Status)) {
							return false;
						}
					}
				}
			}
		}

		StatusProcesso statusProcesso = processo.getStatus();
		if(StatusProcesso.RASCUNHO.equals(statusProcesso)
				|| StatusProcesso.CONCLUIDO.equals(statusProcesso)
				|| StatusProcesso.CANCELADO.equals(statusProcesso)) {
			return false;
		}

		if(!StatusDocumento.DIGITALIZADO.equals(status)
				&& !StatusDocumento.INCLUIDO.equals(status)
				&& !StatusDocumento.PENDENTE.equals(status)
				&& !StatusDocumento.APROVADO.equals(status)) {
			return false;
		}

		if(processo.isSisFiesOrSisProuni()
				&& Subperfil.SUBPERFIS_FIES_PROUNI_IDS.contains(subperfilAtivoId)
				&& StatusDocumento.APROVADO.equals(status)
				|| StatusDocumento.DIGITALIZADO.equals(status)) {
			return true;
		}

		if(usuario.isAdminRole()) {
			return true;
		}
		else if(usuario.isGestorRole()) {
			return true;
		}
		else if(usuario.isAnalistaRole()) {
			return true;
		}

		return false;
	}

	public boolean podeReindexar(Documento documento, Usuario usuario, List<Imagem> imagens) {

		if (usuario == null) {
			return false;
		}
		Processo processo = documento.getProcesso();
		StatusProcesso statusProcesso = processo.getStatus();
		if(StatusProcesso.CONCLUIDO.equals(statusProcesso)
				|| StatusProcesso.CANCELADO.equals(statusProcesso)) {
			return false;
		}

		Long documentoId = documento.getId();
		boolean apenasImagens = imagemService.isUnicaExtensao(imagens, GetdocConstants.IMAGEM_EXTENSOES);
		if (!apenasImagens) {
			return false;
		}

		if(usuario.isAdminRole()) {
			return true;
		}
		else if(usuario.isGestorRole()) {
			return true;
		}
		else if(usuario.isAnalistaRole()) {
			return true;
		}
		else if(usuario.isRequisitanteRole() || usuario.isAreaRole()) {

			Usuario autor = processo.getAutor();
			if(!usuario.equals(autor)) {
				return false;
			}

			StatusProcesso status = processo.getStatus();
			if(StatusProcesso.RASCUNHO.equals(status)) {
				return true;
			}
			else if(StatusProcesso.PENDENTE.equals(status)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeDigitalizar(Documento documento, Usuario usuario) {

		if (usuario == null) {
			return false;
		}
		Processo processo = documento.getProcesso();
		StatusProcesso statusProcesso = processo.getStatus();
		if(StatusProcesso.CANCELADO.equals(statusProcesso)) {
			return false;
		}

		StatusDocumento status = documento.getStatus();
		if(!StatusDocumento.DIGITALIZADO.equals(status)
				&& !StatusDocumento.INCLUIDO.equals(status)
				&& !StatusDocumento.PENDENTE.equals(status)
				&& !StatusDocumento.APROVADO.equals(status)) {
			return false;
		}

		if(usuario.isAdminRole()) {
			return true;
		}
		else if(usuario.isGestorRole()) {
			return true;
		}
		else if(usuario.isAnalistaRole() || usuario.isSalaMatriculaRole()) {
			return true;
		}
		else if(usuario.isRequisitanteRole() || usuario.isAreaRole()) {

			Usuario autor = processo.getAutor();
			if(!usuario.equals(autor)) {
				return false;
			}

			if(StatusProcesso.RASCUNHO.equals(statusProcesso)) {
				return true;
			}
			else if(StatusProcesso.PENDENTE.equals(statusProcesso)) {
				return true;
			}
		}

		return false;
	}

	@Transactional(rollbackFor=Exception.class)
	public void adicionarDocumento(List<Long> novosDocumentosId, Usuario usuario) {
		for (Long documentoId : novosDocumentosId) {
			adicionarDocumento(get(documentoId), usuario);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void adicionarDocumento(Documento documento, Usuario usuario) {

		StatusDocumento status = documento.getStatus();
		if(!StatusDocumento.EXCLUIDO.equals(status)) {
			return;
		}

		Processo processo = documento.getProcesso();
		if (processo.isSisFiesOrSisProuni()) {
			corrigirObrigatoriedadeMenorOuMaiorDeIdade(documento, processo);
		}

		documento.setStatus(StatusDocumento.INCLUIDO);
		documentoRepository.saveOrUpdate(documento);

		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		if(tipoDocumento != null){
			List<ProcessoAjuda> ajudas = processoAjudaService.getByProcesso(processo.getId(), tipoDocumento.getId());
			for (ProcessoAjuda processoAjuda : ajudas) {
				processoAjuda.setAtivo(true);
				processoAjudaService.salvarSimples(processoAjuda);
			}
		}

		documentoLogService.criaLog(documento, usuario, AcaoDocumento.INCLUSAO);
	}

	private void corrigirObrigatoriedadeMenorOuMaiorDeIdade(Documento documento, Processo processo) {

		CampoGrupo grupoRelacionado = documento.getGrupoRelacionado();
		if (grupoRelacionado != null) {

			Date dataCadastro = processoService.getDataCadastroOuCriacao(processo, null);
			Boolean isMenorDeIdade = processoService.membroFamiliarEhMenorDeIdade(processo, dataCadastro, grupoRelacionado.getId());

			if (isMenorDeIdade != null) {
				documento.setObrigatorio(!isMenorDeIdade);
			}
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long documentoId, Usuario usuario) throws MessageKeyException {

		Documento documento = get(documentoId);

		logAlteracaoService.registrarAlteracao(documento, usuario, TipoAlteracao.EXCLUSAO);

		try {
			documentoRepository.deleteById(documentoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		Processo processo = documento.getProcesso();
		processoRegraService.reprocessaRegrasAoAtualizarDocumentos(processo, usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluirDocumento(ImagemTransaction imagemTransaction, Documento documento, Usuario usuario) {

		boolean excecaoGrupoRelacionadoFoiApagado = grupoRelacionadoFoiApagado(documento);
		if (documento.getObrigatorio() && !excecaoGrupoRelacionadoFoiApagado) {
			return;
		}

		StatusDocumento status = documento.getStatus();
		if (StatusDocumento.EXCLUIDO.equals(status)) {
			return;
		}

		if (excecaoGrupoRelacionadoFoiApagado) {

			Processo processo = documento.getProcesso();
			excluirPermantentemente(imagemTransaction, documento);
			ProcessoLog processoLog = processoService.criarProcessoLogDeDocumentosDinamicos(processo, usuario, Collections.singletonList(documento), AcaoProcesso.DELETOU_DOCUMENTO);
			processoLogService.saveOrUpdate(processoLog);
		}
		else {

			documento.setStatus(StatusDocumento.EXCLUIDO);
			documentoRepository.saveOrUpdate(documento);
			documentoLogService.criaLog(documento, usuario, AcaoDocumento.EXCLUSAO);
		}
	}

	private void excluirPermantentemente(ImagemTransaction transaction, Documento documento) {

		Long documentoId = documento.getId();
		List<Imagem> imagensDocumento = imagemService.findByDocumento(documentoId);
		if (transaction != null) {
			imagensDocumento.forEach(d -> transaction.addToDeleteOnCommit(d.getCaminho()));
		}
		else {
			for (Imagem imagem : imagensDocumento) {

				String caminho = imagem.getCaminho();
				File file = new File(caminho);
				DummyUtils.deleteFile(file);
			}
		}

		documentoRepository.deleteById(documentoId);
	}

	public ComparacaoFacesVO compararFaces(Documento documentoBase) {

		Long documentoId = documentoBase.getId();
		Imagem imagemBase = imagemService.getPrimeiraImagem(documentoId);

		Processo processo = documentoBase.getProcesso();
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentosList = findByFiltro(filtro, null, null);
		List<Imagem> imagemList = new ArrayList<>();

		for (Documento documento2 : documentosList) {

			boolean reconhecimentoFacial = documento2.getReconhecimentoFacial();
			if(reconhecimentoFacial) {

				StatusDocumento status = documento2.getStatus();
				if(StatusDocumento.INCLUIDO.equals(status) || documento2.equals(documentoBase)) {
					continue;
				}

				Long documento2Id = documento2.getId();
				Imagem imagem = imagemService.getPrimeiraImagem(documento2Id);
				if(imagem != null) {
					imagemList.add(imagem);
				}
			}
		}

		ComparacaoFacesVO cf = faceRecognitionService.comparar(imagemBase, imagemList);
		return cf;
	}

	public List<Long> findIdsToReconhecimentoFacial() {
		return documentoRepository.findIdsToReconhecimentoFacial();
	}

	@Transactional(rollbackFor=Exception.class)
	public void fillReconhecimentoFacial(List<Documento> documentos) {

		for (Documento documento : documentos) {

			compararFaces(documento);
			documento.setAguardandoReconhecimentoFacial(false);
			documentoRepository.saveOrUpdate(documento);
		}
	}

	public List<Documento> findByIds(List<Long> ids) {
		return documentoRepository.findByIds(ids);
	}

	@Transactional(rollbackFor=Exception.class)
	public Documento makeTipificando(Processo processo) {

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		boolean temTipificavel = tipoDocumentoService.temTipificavel(tipoProcessoId);
		if(!temTipificavel) {
			return null;
		}

		boolean isHabilitada = tipificacaoService.isHabilitada();
		if(!isHabilitada) {
			return null;
		}

		Long processoId = processo.getId();
		Documento tipificando = documentoRepository.getTipificando(processoId);

		if(tipificando == null) {
			tipificando = new Documento();
			tipificando.setNome(Documento.NOME_TIFICANDO);
			tipificando.setStatus(StatusDocumento.PROCESSANDO);
			tipificando.setObrigatorio(false);
			tipificando.setOrdem(-10);
			tipificando.setProcesso(processo);
			tipificando.setTaxaCompressao((short) 100);
			tipificando.setVersaoAtual(1);
			documentoRepository.saveOrUpdate(tipificando);
		}

		StatusDocumento status = tipificando.getStatus();
		if(!StatusDocumento.PROCESSANDO.equals(status)) {
			tipificando.setStatus(StatusDocumento.PROCESSANDO);
			documentoRepository.saveOrUpdate(tipificando);
		}

		return tipificando;
	}

	public Documento makeOutros(Processo processo) {

		Long processoId = processo.getId();
		Documento outros = documentoRepository.getByNome(processoId, Documento.NOME_OUTROS);

		if(outros == null) {
			outros = new Documento();
			outros.setNome(Documento.NOME_OUTROS);
			outros.setStatus(StatusDocumento.DIGITALIZADO);
			outros.setObrigatorio(false);
			outros.setOrdem(-5);
			outros.setProcesso(processo);
			outros.setTaxaCompressao((short) 100);
			outros.setVersaoAtual(1);
			documentoRepository.saveOrUpdate(outros);
		}

		return outros;
	}

	public List<Long> findIdsToTipificacao() {
		return documentoRepository.findIdsToTipificacao();
	}

	public MultiValueMap<Long, Long> findIdsToTipificacao2() {

		List<Object[]> list = documentoRepository.findIdsToTipificacaoUsandoImagemMeta();
		MultiValueMap<Long, Long> map = new LinkedMultiValueMap<>();
		for (Object object[] : list) {
			Long documentoId = (Long) object[0];
			Long imagemId = (Long) object[1];
			map.add(documentoId, imagemId);
		}

		return map;
	}

	public MultiValueMap<Long, Long> findIdsToTipificacao3(DocumentoFiltro filtro) {

		List<Object[]> list = documentoRepository.findIdsToTipificacaoByFiltro(filtro);
		MultiValueMap<Long, Long> map = new LinkedMultiValueMap<>();
		for (Object object[] : list) {
			Long documentoId = (Long) object[0];
			Long imagemId = (Long) object[1];
			map.add(documentoId, imagemId);
		}

		return map;
	}

	public List<Long> findIdsToTipificacaoByProcesso(Processo processo) {
		return documentoRepository.findIdsToTipificacaoByProcesso(processo);
	}

	public  MultiValueMap<Long, Long> findIdsToTipificacao2ByProcesso(Processo processo) {

		List<Object[]> list = documentoRepository.findIdsToTipificacao2ByProcesso(processo);
		MultiValueMap<Long, Long> map = new LinkedMultiValueMap<>();
		for (Object object[] : list) {
			Long documentoId = (Long) object[0];
			Long imagemId = (Long) object[1];
			map.add(documentoId, imagemId);
		}

		return map;
	}

	@Transactional(rollbackFor=Exception.class)
	public Documento tipificar(Documento documento) throws Exception {

		Long documentoId = documento.getId();

		Processo processo = documento.getProcesso();
		Long processoId = processo != null ? processo.getId() : null;
		systraceThread("processo: " + processoId + " documentoId: " + documentoId);

		ImagemTransaction transaction = new ImagemTransaction();
		try {
			List<FileVO> list = getFileVOByDocumentoId(documentoId, transaction);

			List<FileVO> naoTipificou = new ArrayList<>();

			Map<Long, List<FileVO>> map = tipificacaoService.tipificar(processoId, documento, list, naoTipificou);

			if(!naoTipificou.isEmpty()) {
				mapearOutros(processoId, map, list);
			}

			Origem origem = documento.getOrigem();
			for (Long documentoId2 : map.keySet()) {
				Documento documento2 = get(documentoId2);
				List<FileVO> list2 = map.get(documentoId2);
				if (!list2.isEmpty()) {
					Origem origem2 = list2.get(0).getOrigem();
					origem2 = origem2 != null ? origem2 : origem;
					digitalizarImagens(transaction, null, documento2, list2, AcaoDocumento.TIPIFICOU, origem2, false);
				}
			}

			documento.setStatus(StatusDocumento.EXCLUIDO);

			documentoRepository.saveOrUpdate(documento);
			processoService.agendarFacialRecognition(processo);

			transaction.commit();

			return documento;
		}
		catch (Exception e) {
			String message = exceptionService.getMessage(e);
			DummyUtils.systraceThread("~~ documentoId: " + documentoId + ": " + message);
			e.printStackTrace();

			transaction.rollback();

			LogAcesso log = LogAcessoFilter.getLogAcesso();
			String exception = log.getException();
			exception = exception != null ? exception + "\n\n" : "";
			exception += message + "\n";
			String stackTrace = ExceptionUtils.getStackTrace(e);
			exception += stackTrace;
			log.setException(exception);

			processoLogService.criaLogErro(processo, null, AcaoProcesso.ERRO_TIPIFICACAO, null, "Erro ao tipificar documento: " + message + " #a-" + log.getId());
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void tipificar2(Documento documento, List<Long> imagensIds) {

		List<FileVO> list = new ArrayList<>();
		Map<Imagem, FileVO> mapFileVO = new LinkedHashMap<>();
		Map<Imagem, ImagemMeta> mapImagemMetaVO = new LinkedHashMap<>();

		List<Imagem> imagens = imagemService.findByIds(imagensIds);
		for (Imagem imagem : imagens) {
			FileVO fileVO = new FileVO();
			File file = imagemService.getFile(imagem);
			if(!file.exists()) {
				imagem.setExistente(false);
				imagemService.saveOrUpdate(imagem);
			}
			else {
				fileVO.setFile(file);
				Long imagemId = imagem.getId();
				ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);
				String metaDados = imagemMeta.getMetaDados();
				Map<String, String> map1 = (Map<String, String>) DummyUtils.jsonStringToMap(metaDados);
				fileVO.setMetadados(map1);
				list.add(fileVO);
				mapFileVO.put(imagem, fileVO);
				mapImagemMetaVO.put(imagem, imagemMeta);
			}
		}

		List<FileVO> naoTipificou = new ArrayList<>();
		tipificacaoDarknetService.tipificar(documento, list, asList(documento), naoTipificou);

		ModeloDocumento modeloDocumento = null;
		Integer versaoAtual = documento.getVersaoAtual();
		for (Imagem imagem : imagens) {
			Integer versao = imagem.getVersao();
			if(versao.equals(versaoAtual)) {
				FileVO fileVO = mapFileVO.get(imagem);
				modeloDocumento = fileVO.getModeloTipificacao();
				break;
			}
		}
		atualizarModeloDocumento(documento, modeloDocumento);

		for (Imagem imagem : mapFileVO.keySet()) {
			FileVO fileVO = mapFileVO.get(imagem);
			Map<String, String> metadados = fileVO.getMetadados();
			String metadadosStr = DummyUtils.objectToJson(metadados);
			ImagemMeta imagemMeta = mapImagemMetaVO.get(imagem);
			imagemMeta.setMetaDados(metadadosStr);
			imagemMeta.setTipificado(true);
			imagemMetaService.saveOrUpdate(imagemMeta);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarModeloDocumento(Documento documento, ModeloDocumento modeloDocumento) {

		String documentoNome = documento.getNome();
		boolean isTipificando = Documento.NOME_TIFICANDO.equals(documentoNome);
		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		if(tipoDocumento == null || isTipificando) return;

		Long tipoDocumentoId = tipoDocumento.getId();
		List<ModeloDocumento> modelosDocumentos = tipoDocumentoService.findModelosDocumentos(tipoDocumentoId);
		if(!modelosDocumentos.contains(modeloDocumento)) {
			//modelo não aplicável ao documento... não faz nada
			return;
		}

		Long documentoId = documento.getId();
		Long modeloDocumentoId = modeloDocumento.getId();
		documentoRepository.atualizarModeloDocumento(documentoId, modeloDocumentoId);
	}

	private void mapearOutros(Long processoId, Map<Long, List<FileVO>> map, List<FileVO> files) {

		Processo processo = processoService.get(processoId);
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = findByFiltro(filtro, null, null);

		Documento documento = null;
		for (Documento documento2 : documentos) {
			String nome = documento2.getNome();
			processo = documento2.getProcesso();
			if(Documento.NOME_OUTROS.equals(nome)) {
				documento = documento2;
				break;
			} else if(Documento.NOME_FOTOS.equals(nome)) {
				documento = documento2;
				break;
			}
		}

		if(documento == null) {
			//documento = documentos.get(documentos.size() - 1);
			documento = makeOutros(processo);
		}

		Long documentoId = documento.getId();
		map.put(documentoId, files);
	}

	private List<FileVO> getFileVOByDocumentoId(Long documentoId, ImagemTransaction transaction) {
		List<Imagem> imagens = imagemService.findByDocumento(documentoId);
		List<FileVO> list = new ArrayList<>();
		for (Imagem imagem : imagens) {

			Long imagemId = imagem.getId();
			File file = imagemService.getFile(imagem);
			FileVO vo = new FileVO();
			vo.setFile(file);
			if(tipificacaoService.isVisionHabilitada()) {
				String text = imagemMetaService.getFullText(imagemId);
				vo.setText(text);
			}
			list.add(vo);

			String caminho = file.getAbsolutePath();
			transaction.addToDeleteOnCommit(caminho);
			imagemService.excluir(imagem);
		}
		return list;
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizaStatusOcr(Long documentoId) {

		Documento documento = documentoRepository.get(documentoId);
		LogOcr logOcr = logOcrService.getLastLog(documentoId);
		StatusLogOcr statusLogOcr = logOcr != null ? logOcr.getStatusOcr() : null;

		if(StatusLogOcr.PRE_AGENDADO.equals(statusLogOcr) || StatusLogOcr.AGENDADO.equals(statusLogOcr)) {
			documento.setStatusOcr(StatusOcr.PROCESSANDO);
		}
		else if(StatusLogOcr.ERRO.equals(statusLogOcr)) {
			documento.setStatusOcr(StatusOcr.ERRO);
		}
		else {

			int countByDocumento = campoOcrService.countByDocumento(documentoId);
			if(countByDocumento == 0) {
				documento.setStatusOcr(StatusOcr.ERRO);
			}
			else {

				boolean temInconsistencia = campoOcrService.temInconsistenciaByDocumento(documentoId);
				if(temInconsistencia) {
					documento.setStatusOcr(StatusOcr.INCONSISTENTE);
				}
				else {
					documento.setStatusOcr(StatusOcr.TUDO_OK);
				}
			}
		}

		documentoRepository.saveOrUpdate(documento);

		Processo processo = documento.getProcesso();
		processoService.atualizarStatusOcr(processo);
	}

	@Transactional(rollbackFor=Exception.class)
	public List<String> getFullTexts(Documento documento, Long imagemId, Integer versao) {

		List<String> list = new ArrayList<>();
		if(imagemId != null) {
			String fullText = imagemMetaService.getFullText(imagemId);
			if (StringUtils.isNotBlank(fullText)) {
				list.add(fullText);
			}
			return list;
		}

		Long documentoId = documento.getId();
		List<Imagem> imagens = imagemService.findByDocumentoVersao(documentoId, versao);
		for (Imagem imagem : imagens) {
			Long imagemId2 = imagem.getId();
			String fullText = imagemMetaService.getFullText(imagemId2);
			list.add(fullText);
		}

		return list;
	}

	public int countByMetaDados(Map<String, Object> map) {
		return documentoRepository.countByMetaDados(map);
	}

	public Integer getMaxOrdemFromProcesso(Long processoId) {
		return documentoRepository.getMaxOrdemFromProcesso(processoId);
	}

	public Documento getByMetaDados(Map<String, Object> map){
		return documentoRepository.getByMetaDados(map);
	}

	public List<Documento> findDocumentosByAlunoFiltro(TaxonomiaFiltro filtro) {
		return documentoRepository.findDocumentosByAlunoFiltro(filtro);
	}

	public Map<StatusDocumento, Long> countByStatusMap(List<Long> processosId ) {
		return documentoRepository.countByStatusMap(processosId);
	}

	public Long getQuantidadeObrigatorio(List<Long> processosId) {
		return documentoRepository.getQuantidadeObrigatorio(processosId);
	}

	private boolean isAtivoTipoDocumento(DocumentoVO documentoVO){
		TipoDocumento tipoDocumento = documentoVO.getTipoDocumento();
		StatusDocumento status = documentoVO.getStatus();
		boolean ativo = tipoDocumento != null && tipoDocumento.isAtivo();
		if (!StatusDocumento.EXCLUIDO.equals(status)) {
			ativo = true;
		}
		return ativo;
	}

	public Integer[] getQuantidadesObrigatoriosEquivalencias(List<Long> processosIds) {

		DocumentoFiltro filtro1 = new DocumentoFiltro();
		filtro1.setDiferenteDeOutros(true);
		filtro1.setDiferenteDeTipificando(true);
		filtro1.setProcessoList(processosIds);
		filtro1.setObrigatorio(true);
		filtro1.setStatusDocumentoList(Arrays.asList(StatusDocumento.INCLUIDO));
		List<Long> obrigatorioNaoDigitalizados = documentoRepository.findTiposDocumentosIdsByFiltro(filtro1, null, null);
		List<StatusDocumento> statusEquivalencia1 = Arrays.asList(StatusDocumento.DIGITALIZADO, StatusDocumento.APROVADO, StatusDocumento.PENDENTE);
		Integer qtdeObrigatorioNaoDigitalizados = 0;
		if(!obrigatorioNaoDigitalizados.isEmpty()) {
			qtdeObrigatorioNaoDigitalizados = considerarEquivalencia(processosIds, obrigatorioNaoDigitalizados, statusEquivalencia1);
		}

		DocumentoFiltro filtro2 = new DocumentoFiltro();
		filtro2.setDiferenteDeOutros(true);
		filtro2.setDiferenteDeTipificando(true);
		filtro2.setObrigatorio(true);
		filtro2.setProcessoList(processosIds);
		filtro2.setStatusDocumentoList(Arrays.asList(StatusDocumento.PENDENTE));
		List<Long> obrigatorioPendentes = documentoRepository.findTiposDocumentosIdsByFiltro(filtro2, null, null);
		List<StatusDocumento> statusEquivalencia2 = Arrays.asList(StatusDocumento.APROVADO);
		Integer qtdeObrigatorioPendentes = 0;
		if(!obrigatorioPendentes.isEmpty()) {
			qtdeObrigatorioPendentes = considerarEquivalencia(processosIds, obrigatorioPendentes, statusEquivalencia2);
		}

		return new Integer[]{qtdeObrigatorioNaoDigitalizados, qtdeObrigatorioPendentes};
	}

	private Integer considerarEquivalencia(List<Long> processosId, List<Long> documentosIds, List<StatusDocumento> statusEquivalencia) {

		List<Long> obrigatorioEquivalidos = new ArrayList<>(documentosIds);

		for(Long processoId : processosId){
			Processo processo = processoService.get(processoId);
			Campanha campanha = processo.getCampanha();

			if(campanha == null) {
				continue;
			}

			Map<Long, List<Long>> equivalentesMap = new LinkedHashMap<>();
			campanhaService.carregaObrigatoriosAndEquivalencias(campanha, null, equivalentesMap, null);
			List<Long> equivalentes = new ArrayList<>(equivalentesMap.keySet());
			for(Long equivalenteId : equivalentes){
				List<Long> equivalidos =  equivalentesMap.get(equivalenteId);
				Documento doc = getFirstByTipoDocumentoId(equivalenteId, processoId);
				if (doc != null) {
					StatusDocumento statusDocumento = doc.getStatus();
					if(statusEquivalencia.contains(statusDocumento)){
						for(Long obrigatorioNaoDigitalizadoId : documentosIds){
							if(equivalidos.contains(obrigatorioNaoDigitalizadoId)){
								obrigatorioEquivalidos.remove(obrigatorioNaoDigitalizadoId);
							}
						}
					}
				}
			}
		}

		return obrigatorioEquivalidos.size();
	}

	public boolean isTodosDigitalizados(List<Long> processosId, List<Long> tipoDocIds){
		return documentoRepository.isTodosDigitalizados(processosId, tipoDocIds);
	}

	public List<Long> findToNotificacaoSia() {
		return documentoRepository.findToNotificacaoSia();
	}

	public void girarPdf(Usuario usuario, Documento documento, Long imagemId, int rotacao) {

		Imagem imagem = imagemService.get(imagemId);
		File file = imagemService.getFile(imagem);
		String caminho = file.getAbsolutePath();
		int tamanho = caminho.length();

		String extensao = caminho.substring(tamanho-4, tamanho);
		String dir = caminho.substring(0, tamanho-4);
		dir = dir + "_rotate" + extensao;

		try {
			PdfReader reader = new PdfReader(caminho);
			int n = reader.getNumberOfPages();
			PdfDictionary page;
			for (int p = 1; p <= n; p++) {
				page = reader.getPageN(p);
				int r = reader.getPageRotation(p);
				page.put(PdfName.ROTATE, new PdfNumber(r + rotacao));
			}

			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dir));
			stamper.close();
			reader.close();

			DummyUtils.deleteFile(file);

			String cache = caminho.replace("imagens", "cache"+File.separator+"imgfiles");
			File fileCache = new File(cache);
			if(fileCache.exists()) {
				DummyUtils.deleteFile(fileCache);
			}

			Path from = Paths.get(dir);
			Path to = Paths.get(caminho);

			Files.move(from, to);

			File file2 = new File(caminho);
			String hashChecksum = DummyUtils.getHashChecksum(file2);
			imagem.setHashChecksum(hashChecksum);

			imagemService.saveOrUpdate(imagem);

			documentoLogService.criaLog(documento, usuario, AcaoDocumento.MOVEU_IMAGEM);
		}
		catch (IOException | DocumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private boolean validaSubperfilRestritos (Long subperfilId) {
		if(Subperfil.SUBPERFIS_CSC_IDS.contains(subperfilId) ||
				Subperfil.IBMEC.equals(subperfilId) ||
				Subperfil.SUBPERFIS_POLOS_PARCEIRO_IDS.contains(subperfilId) ||
				Subperfil.SUBPERFIL_UNIDADES_ID.equals(subperfilId) ||
				Subperfil.SUBPERFIS_MEDICINA_IDS.contains(subperfilId)) {
			return true;
		} else {
			return false;
		}
	}

	public int findMaxOrdemByProcesso(Processo processo) {
		return documentoRepository.findMaxOrdemByProcesso(processo);
	}

	public void atualizarUsaTermo(Date dataInicio, Date dataFim, Long processoId){
		documentoRepository.atualizarUsaTermo(dataInicio, dataFim, processoId);
	}

	public Map<Long, List<Irregularidade>> getMapIrregularidadesPorDocumento(List<DocumentoVO> documentos) {

		Map<Long, List<Irregularidade>> listMap = new LinkedHashMap<>();
		List<Irregularidade> voList;
		for(DocumentoVO documentoVO : documentos) {
			voList = listMap.get(documentoVO.getProcessoId());
			if (voList == null) {
				voList = new ArrayList<>();
			}

			TipoDocumento tipoDocumento = documentoVO.getTipoDocumento();
			if (!documentoVO.isExcluido() && tipoDocumento != null) {
				Long tipoDocumentoId = tipoDocumento.getId();
				List<Irregularidade> irregularidadeList = irregularidadeTipoDocumentoService.findIrregularidadesByTipoDocumentoId(tipoDocumentoId);
				if(irregularidadeList != null && !irregularidadeList.isEmpty()) {
					voList.addAll(irregularidadeList);
					listMap.put(documentoVO.getId(), voList);
				}
			}
		}

		return listMap;
	}

	public boolean validaMesmaVersãoDocumentos(Processo processoIsencaoDisciplinas, Processo processoPai) {
		DocumentoFiltro filtro = new DocumentoFiltro();
		DocumentoFiltro filtro2 = new DocumentoFiltro();
		filtro.setProcesso(processoIsencaoDisciplinas);
		List<Documento> documentosIsencao = findByFiltro(filtro, null, null);
		for (Documento documento : documentosIsencao) {
			Long codOrigem = documento.getTipoDocumento().getCodOrigem();
			if(codOrigem != null) {
				filtro2.setProcesso(processoPai);
				filtro2.setCodsOrigem(Arrays.asList(codOrigem));
				List<Documento> documentosProcessoAtual = findByFiltro(filtro2, null, null);
				for (Documento documentoProcessoAtual : documentosProcessoAtual) {
					Integer versaoAtual = documentoProcessoAtual.getVersaoAtual();
					Integer versaoAtual1 = documento.getVersaoAtual();
					if(versaoAtual.equals(versaoAtual1)) {
						return true;
					}
				}
			}
		}
		return false;
	}

    public List<Documento> findByProcessoIdAndCategoriaDocumento(Long processoId, String chaveCategoria) {
		return documentoRepository.findByProcessoIdAndCategoriaDocumento(processoId, chaveCategoria);
    }

	public List<Documento> findNotificadosSia(Date dataInicioAprovacao, Date dataFimAprovacao) {
		return documentoRepository.findNotificadosSia(dataInicioAprovacao, dataFimAprovacao);
	}

	public int countRelatorioPastaVermelhaByFiltro(RelatorioPastaVermelhaFiltro filtro) {
		return documentoRepository.countRelatorioPastaVermelhaByFiltro(filtro);
	}

	public List<RelatorioPastaVermelhaVO> findRelatorioPastaVermelhaByFiltro(RelatorioPastaVermelhaFiltro filtro, Integer inicio, Integer max) {
		return documentoRepository.findRelatorioPastaVermelhaByFiltro(filtro, inicio, max);
	}

	public void atualizarNotificadoSia(Long documentoId, boolean notificadoSia) {
		documentoRepository.atualizarNotificadoSia(documentoId, notificadoSia);
	}

	public void alterarStatus(ManutencaoDocumentoVO vo, Documento documento, Usuario usuario) {
		StatusDocumento statusAnterior = documento.getStatus();
		StatusDocumento novoStatus = vo.getStatus();
		String observacao = vo.getObservacao();
		observacao = observacao.concat("\n\n Alterado de " + statusAnterior + " para " + novoStatus);
		documentoLogService.criaLog(documento, usuario, AcaoDocumento.ALTERACAO_STATUS, observacao);
		documento.setStatus(novoStatus);
		saveOrUpdate(documento);
	}

	public  MultiValueMap<Long, Long>  findIdsToTipificacaoFluxoAprovacao(Long processoId) {

		List<Object[]> list = documentoRepository.findIdsToTipificacaoFluxoAprovacao(processoId);
		MultiValueMap<Long, Long> map = new LinkedMultiValueMap<>();
		for (Object object[] : list) {
			Long documentoId = (Long) object[0];
			Long imagemId = (Long) object[1];
			map.add(documentoId, imagemId);
		}

		return map;
	}

	@Transactional(rollbackFor=Exception.class)
	public void tipificarFluxoAprovacao(Processo processo, Documento documento) throws Exception {

		try {
			List<FileVO> imageList = getFileVOByDocumentoId(documento);
			Map<Long, List<FileVO>> map = tipificacaoService.tipificarFluxoAprovacao(documento, imageList);

			for (Long documentoTipificadoId : map.keySet()) {
				List<FileVO> listTipificados = map.get(documentoTipificadoId);
				if (CollectionUtils.isNotEmpty(listTipificados)){
					FileVO fileVO = listTipificados.get(0);
					Map<String, String> metadados = fileVO.getMetadados();
					ImagemMeta imagemMeta = fileVO.getImagemMeta();
					String json = DummyUtils.objectToJson(metadados);
					imagemMeta.setMetaDados(json);
					imagemMetaService.saveOrUpdate(imagemMeta);
					List<CampoOcr> camposOcr = fileVO.getCamposOcr();
					if (CollectionUtils.isNotEmpty(camposOcr)) {
						for (CampoOcr campoOcr : camposOcr) {
							campoOcrService.saveOrUpdate(campoOcr);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			String message = exceptionService.getMessage(e);

			LogAcesso log = LogAcessoFilter.getLogAcesso();
			String exception = log.getException();
			exception = exception != null? exception + "\n\n" : "";
			exception += message + "\n";
			String stackTrace = ExceptionUtils.getStackTrace(e);
			exception += stackTrace;
			log.setException(exception);

			processoLogService.criaLogErro(processo, null, AcaoProcesso.ERRO_TIPIFICACAO, null, "Erro ao tipificar documento: " + message + " #b-" + log.getId());
			throw e;
		}
	}

	private List<FileVO> getFileVOByDocumentoId(Documento documento) {
		List<Imagem> imagens = imagemService.findVersaoAtualByDocumento(documento.getId());
		List<FileVO> list = new ArrayList<>();
		for (Imagem imagem : imagens) {
			Long imagemId = imagem.getId();
			File file = imagemService.getFile(imagem);
			FileVO vo = new FileVO();
			vo.setFile(file);
			vo.setImagem(imagem);
			ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);
			String metaDados = imagemMeta.getMetaDados();
			Map<String, String> map1 = (Map<String, String>) DummyUtils.jsonStringToMap(metaDados);
			vo.setMetadados(map1);
			vo.setImagemMeta(imagemMeta);
			if(tipificacaoService.isVisionHabilitada()) {
				String text = imagemMetaService.getFullText(imagemId);
				vo.setText(text);
			}
			list.add(vo);
		}
		return list;
	}

	public  MultiValueMap<Long, Long> findIdsToOcrFluxoAprovacao(Long processoId) {

		List<Object[]> list = documentoRepository.findIdsToOCrFluxoAprovacao(processoId);
		MultiValueMap<Long, Long> map = new LinkedMultiValueMap<>();
		for (Object object[] : list) {
			Long documentoId = (Long) object[0];
			Long imagemId = (Long) object[1];
			map.add(documentoId, imagemId);
		}

		return map;
	}

	@Transactional(rollbackFor=Exception.class)
	public void extrairOCR(Processo processo, Documento documento) throws Exception {

		Long documentoId = documento.getId();

		Long processoId = processo != null ? processo.getId() : null;
		systraceThread("processo: " + processoId + " documentoId: " + documentoId);

		try {
			List<FileVO> list = getFileVOByDocumentoIdToOCR(documentoId);
			ocrService.extrairOcr(documento, list);
			if (extraiuOCR(list)){
				documento.setStatusOcr(StatusOcr.INCONSISTENTE);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			String message = exceptionService.getMessage(e);

			LogAcesso log = LogAcessoFilter.getLogAcesso();
			String exception = log.getException();
			exception = exception != null? exception + "\n\n" : "";
			exception += message + "\n";
			String stackTrace = ExceptionUtils.getStackTrace(e);
			exception += stackTrace;
			log.setException(exception);

			processoLogService.criaLogErro(processo, null, AcaoProcesso.ERRO_TIPIFICACAO, null, "Erro ao tipificar documento: " + message + " #c-" + log.getId());
			//throw e;
		}
	}

	private boolean extraiuOCR(List<FileVO> list) {
		for (FileVO fileVO: list) {
			if (fileVO.isExtraiuOCR())
				return true;
		}

		return false;
	}

	private List<FileVO> getFileVOByDocumentoIdToOCR(Long documentoId) {
		List<Imagem> imagens = imagemService.findVersaoAtualByDocumento(documentoId);
		List<FileVO> list = new ArrayList<>();
		for (Imagem imagem : imagens) {

			Long imagemId = imagem.getId();
			File file = imagemService.getFile(imagem);
			FileVO vo = new FileVO();
			vo.setModeloTipificacao(imagem.getModeloDocumento());
			List<CampoOcr> camposOcr = campoOcrService.findByDocumento(documentoId);

			vo.setCampos(camposOcr);

			vo.setFile(file);
			vo.setImagem(imagem);
			String text = imagemMetaService.getFullText(imagemId);
			vo.setText(text);
			ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);
			String metaDados = imagemMeta.getMetaDados();
			Map<String, String> metadados = (Map<String, String>)DummyUtils.jsonStringToMap(metaDados);
			vo.setMetadados(metadados);

			list.add(vo);

		}
		return list;
	}

	public List<Long> findIdsByFiltro(DocumentoFiltro filtro, Integer inicio, Integer max) {
		return documentoRepository.findIdsByFiltro(filtro, inicio, max);
	}

	public boolean atualizarPermissaoEdicaoDocumentoProUni(Processo processo, List<DocumentoVO> documentos) {
		boolean podeReceberDocumentosSisProuniNoCalendario = true;
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Situacao situacao = processo.getSituacao();
		Long situacaoId = situacao.getId();
		if(Arrays.asList(TipoProcesso.SIS_PROUNI, TipoProcesso.TE_PROUNI).contains(tipoProcessoId) && !Situacao.SOLICITAR_EMISSAO_TCB_TR.equals(situacaoId)) {
			boolean permiteEditarDocumentos = situacao.isPermiteEditarDocumentos();
			if(permiteEditarDocumentos) {
				podeReceberDocumentosSisProuniNoCalendario = calendarioService.podeReceberDocumentos(processo);
				for (DocumentoVO documento : documentos) {
					TipoDocumento tipoDocumento = documento.getTipoDocumento();
					Long tipoDocumentoId = tipoDocumento != null ? tipoDocumento.getId() : null;
					if(TipoDocumento.TERMO_DE_RECUSA_TR_ID.equals(tipoDocumentoId)) continue;
					documento.setDigitalizavel(podeReceberDocumentosSisProuniNoCalendario);
				}
			}
		}

		return podeReceberDocumentosSisProuniNoCalendario;
	}
}
