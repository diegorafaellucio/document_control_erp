package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CamposMetadadosTipificacao;
import net.wasys.getdoc.domain.enumeration.TipoErroBacalhau;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoBacalhau;
import net.wasys.getdoc.domain.vo.BacalhauArquivoImagemVO;
import net.wasys.getdoc.domain.vo.BacalhauArquivoVO;
import net.wasys.util.ddd.TransactionWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.repository.ImagemRepository;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.image.ImageRotate;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.util.ExecutorTimeoutUtil;
import net.wasys.util.ddd.ExecutorShutdownException;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.collections4.CollectionUtils;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class ImagemService {

	@Autowired private ImagemRepository imagemRepository;
	@Autowired private ResourceService resourceService;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private ImagemMetaService imagemMetaService;
	@Autowired private OcrSpaceApiService ocrSpaceApiService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private BacalhauService bacalhauService;

	public Imagem get(Long id) {
		return imagemRepository.get(id);
	}

	public List<Imagem> findByDocumento(Long documentoId) {
		return imagemRepository.findByDocumento(documentoId);
	}

	public Integer countByDocumento(Long documentoId) {
		return imagemRepository.countByDocumento(documentoId);
	}

	public Integer countByDocumento(Long documentoId, Integer versao) {
		return imagemRepository.countByDocumento(documentoId, versao);
	}

	public Integer countByProcessoId(Long processoId) {
		return imagemRepository.countByProcessoId(processoId);
	}

	public List<Imagem> findByDocumentoVersao(Long documentoId, Integer versao) {
		return imagemRepository.findByDocumentoVersao(documentoId, versao);
	}

	public List<Imagem> findVersaoAtualByDocumento(Long documentoId) {
		return imagemRepository.findVersaoAtualByDocumento(documentoId);
	}

	public Imagem findVersaoAtualUnicaByDocumento(Long documentoId) {
		return imagemRepository.findVersaoAtualUnicaByDocumento(documentoId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Imagem imagem) {
		Long imagemId = imagem.getId();
		imagemRepository.deleteById(imagemId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Imagem imagem) {
		imagemRepository.saveOrUpdate(imagem);
	}

	@Transactional(rollbackFor=Exception.class)
	public void rotacionarImagem(Long imagemId, Integer rotacao) {

		if(rotacao == null || rotacao == 0) {
			return;
		}

		Imagem imagem = imagemRepository.get(imagemId);

		try {
			File file = getFile(imagem);
			if(!file.exists()) {
				new MessageKeyException("arquivoNaoEncontrado.error", imagemId);
			}

			ImageRotate ir = new ImageRotate(file);
			ir.rotacionarImagem(rotacao);

			File file2 = ir.getFile();
			String hashChecksum1 = DummyUtils.getHashChecksum(file2);

			FileUtils.copyFile(file2, file);
			DummyUtils.deleteFile(file2);

			String hashChecksum2 = DummyUtils.getHashChecksum(file);

			if(!hashChecksum2.equals(hashChecksum1)) {
				throw new RuntimeException("Falha ao verificar o hash");
			}

			imagem.setHashChecksum(hashChecksum2);
			long length = file.length();
			imagemRepository.saveOrUpdate(imagem);

			ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);
			imagemMeta.setTamanho(length);
			imagemMetaService.saveOrUpdate(imagemMeta);

		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public File getFile(Imagem imagem) {

		if(imagem == null) {
			return null;
		}

		String caminho = imagem.getCaminho();
		File file = null;
		if(StringUtils.isNotBlank(caminho)) {
			file = new File(caminho);
		}

		if(file == null || !file.exists()) {

			BacalhauArquivoVO vo = new BacalhauArquivoImagemVO(imagem) {
				@Override
				public String criaCaminho() {
					return gerarCaminho(imagem);
				}
			};

			Boolean existente = imagem.isExistente();
			if(existente == null || existente) {
				imagem.setExistente(false);
				saveOrUpdate(imagem);

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					bacalhauService.salvarDadosImagemFerrada(vo, imagem, TipoExecucaoBacalhau.TIPIFICACAO, TipoErroBacalhau.INEXISTENTE);
				});
				tw.runNewThread();
			}

			caminho = vo.getCaminho();
			if(StringUtils.isNotBlank(caminho)) {
				file = new File(caminho);
			}
		}

		return file;
	}

	public String gerarCaminho(Imagem imagem) {
		String dir = resourceService.getValue(ResourceService.IMAGENS_PATH);
		String caminho = Imagem.gerarCaminho(dir, imagem);
		return caminho;
	}

	public String gerarCaminhoFace(Imagem imagem) {
		String caminho = imagem.getCaminho();
		String camimhoFacial = caminho.replaceAll("\\.(.+)$", "-face.$1");
		return camimhoFacial;
	}

	public List<Long> findIdsByDataDigitalizacao(Date dataInicio, Date dataFim) {
		return imagemRepository.findIdsByDataDigitalizacao(dataInicio, dataFim);
	}

	public List<Imagem> findByIds(List<Long> ids) {
		return imagemRepository.findByIds(ids);
	}

	public boolean isUnicaExtensao(Long documentoId, Integer versao, List<String> imagemExtensoes) {
		return imagemRepository.isUnicaExtensao(documentoId, versao, imagemExtensoes);
	}

	public boolean isUnicaExtensao(List<Imagem> imagens, List<String> extensoes) {
		for (Imagem imagem : imagens) {
			String extensao = imagem.getExtensao();
			if(!extensoes.contains(extensao)) {
				return false;
			}
		}
		return true;
	}

	public boolean isImagem(Long documentoId, Integer versao, Long imagemId) {
		return imagemRepository.isImagem(documentoId, versao, imagemId);
	}

	public Imagem getPrimeiraImagem(Long documentoId) {
		return imagemRepository.getPrimeiraImagem(documentoId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarInfoImagem(Imagem imagem) {

		Long imagemId = imagem.getId();
		String caminho = imagem.getCaminho();
		File file = new File(caminho);

		imagemMetaService.atualizarInfoImagemMeta(imagemId, file);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizaCaminho(Imagem imagem) {

		boolean salvar = false;

		String caminho = imagem.getCaminho();
		if(caminho == null || !new File(caminho).exists()) {
			String dir = resourceService.getValue(ResourceService.IMAGENS_PATH);
			caminho = Imagem.gerarCaminho(dir, imagem);
			imagem.setCaminho(caminho);
			salvar = true;
		}

		Long imagemId = imagem.getId();
		ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);
		Long tamanho = imagemMeta.getTamanho();
		Integer width = imagemMeta.getWidth();
		Integer height = imagemMeta.getHeight();

		if(width == null || height == null || tamanho == null) {
			imagemMetaService.atualizarInfoImagemMeta(imagemId, caminho);
			salvar = true;
		}

		if(salvar) {
			Session session = sessionFactory.getCurrentSession();
			Transaction transaction = session.getTransaction();
			if(transaction != null && TransactionStatus.ACTIVE.equals(transaction.getStatus())) {
				saveOrUpdate(imagem);
			}
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluirByDocumento(Long documentoId) {
		imagemRepository.excluirByDocumento(documentoId);
	}

	public List<Long> findIdsToPreparacao() {
		return imagemRepository.findIdsToPreparacao();
	}

	public List<Long> findIdsToFullText() {
		return imagemRepository.findIdsToFullText();
	}

	public String getFullText(Imagem imagem) throws Exception {

		Long imagemId = imagem.getId();
		String fullText = imagemMetaService.getFullText(imagemId);
		if(fullText == null) {

			String caminho = imagem.getCaminho();
			File file = new File(caminho);

			if(!file.exists()) {
				return null;
			}
			else {
				fullText = ocrSpaceApiService.getOcrSpaceFullText(imagemId);
				if(fullText != null && !fullText.isEmpty()) {
					imagemMetaService.updateFullText(imagemId, fullText);
				}
				imagem.setAguardandoFulltext(false);
			}
		}

		return fullText;
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizaPreparacao(Long documentoId, boolean preparada) {
		imagemRepository.atualizaPreparacao(documentoId, preparada);
	}

	public List<Imagem> findByTipoDocumentoToAmostragem(TipoDocumento tipoDocumento, int max) {
		return imagemRepository.findByTipoDocumentoToAmostragem(tipoDocumento, max);
	}

	public boolean campoGrupoTemImagensRelacionadas(CampoGrupo grupoExcluir) {
		return imagemRepository.campoGrupoTemImagensRelacionadas(grupoExcluir);
	}

	public List<Long> findToVerificarGeral(Long apartirDe, int quantidade) {
		return imagemRepository.findToVerificarGeral(apartirDe, quantidade);
	}

	public Long getFirstRegistro() {
		return imagemRepository.getFirstRegistro();
	}

	public boolean exists(Long imagemId) {
		return imagemRepository.exists(imagemId);
	}

	public static void main(String[] args) {
		Long tipoDocumentoId = 2360L;
		String tipoDocumentoComMembro = String.valueOf(tipoDocumentoId);
		//TODO não entendi essa lógica, gambi para ajustar o problema em prod
		systraceThread("#bg# - tipoDocumentoComMembro: " + tipoDocumentoComMembro );
		//tipoDocumentoComMembro = tipoDocumentoComMembro.length() == 1 ? "00".concat(tipoDocumentoComMembro) : tipoDocumentoComMembro;
		//tipoDocumentoComMembro = tipoDocumentoComMembro.length() == 2 ? "0".concat(tipoDocumentoComMembro) : tipoDocumentoComMembro;
		systraceThread("#bg# - tipoDocumentoComMembro2: " + tipoDocumentoComMembro );
		String strNumMembro = tipoDocumentoComMembro.substring(3);
		systraceThread("#bg# - strNumMembro" + strNumMembro );
		String strMembro = Documento.POSFIX_MEMBRO_FAMILIAR + " (" + String.valueOf(Integer.parseInt(strNumMembro)) + ")";
		systraceThread("#bg# - strMembro" + strMembro );
	}


	private List<Long> findImagensInexistentes() {
		return imagemRepository.findImagensInexistentes();
	}

	public void validarImagensInexistentes() {

		List<Long> imagensId = findImagensInexistentes();
		if (CollectionUtils.isEmpty(imagensId)) {
			return;
		}

		int size = imagensId.size();
		int nThreads = Math.min(size, 50);
		int threadTimeout = 1000 * 5;
		int executorTimeoutMillis = (size * threadTimeout) / nThreads;

		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
		ExecutorTimeoutUtil executorTimeout = new ExecutorTimeoutUtil(executorService, threadTimeout, executorTimeoutMillis, nThreads);
		AtomicInteger existentes = new AtomicInteger();
		AtomicInteger erros = new AtomicInteger();

		for (Long imagemId : imagensId) {

			TransactionWrapper tw = new TransactionWrapper();
			tw.setApplicationContext(applicationContext);
			tw.setRunnable(() -> {
				Imagem imagem = get(imagemId);
				processarImagem(existentes, imagem);
			});
			executorService.submit(tw);
			try {
				tw.throwException();
			} catch (Exception e) {
				e.printStackTrace();
				erros.incrementAndGet();
			}
		}

		try {
			executorTimeout.esperarTerminarFuturesOuCancelar();
		}
		catch (ExecutorShutdownException e) {
			e.printStackTrace();
		}
		finally {
			LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
			DummyUtils.addParameter(logAcesso, "imagensExistentes", existentes.get());
			DummyUtils.addParameter(logAcesso, "erros", erros.get());
			DummyUtils.addParameter(logAcesso, "totalImagens", size);
		}
	}

	private void processarImagem(AtomicInteger existentes, Imagem imagem) {
		String caminho = imagem.getCaminho();
		File file = new File(caminho);
		if (file.exists()) {
			imagem.setExistente(true);
			imagemRepository.saveOrUpdate(imagem);
			existentes.incrementAndGet();
		}
	}
}
