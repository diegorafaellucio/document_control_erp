package net.wasys.getdoc.http;

import static net.wasys.util.DummyUtils.systraceThread;
import static net.wasys.util.faces.AbstractBean.USUARIO_SESSION_KEY;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.wasys.getdoc.domain.service.*;
import net.wasys.util.LogLevel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.EmailRecebidoAnexo;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.util.DummyUtils;

public class ImagemFilter implements Filter {

	private static int BUFFER_SIZE = 8192;
	public static final String PATH = "/imgfiles/";
	public static final String ANEXO_PL = "anexo_pl/";
	public static final String ANEXO_ER = "anexo_er/";
	public static final String IMAGEM_LAYOUT = "imagem_layout/";
	private HashMap<String, String> resourceHashMap = new HashMap<>();

	@Override
	public void init(FilterConfig arg0) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request1, ServletResponse response1, FilterChain chain) throws ServletException, IOException {

		BufferedInputStream bis = null;
		try {

			HttpServletRequest request = (HttpServletRequest) request1;
			HttpServletResponse response = (HttpServletResponse) response1;
			ServletContext servletContext = request.getSession().getServletContext();
			HttpSession session = request.getSession();

			String requestURI = request.getRequestURI();
			String contextPath = request.getContextPath();
			String pathImagem = requestURI.replace(contextPath, "");
			String nomeImagem = pathImagem.substring(pathImagem.lastIndexOf('/') + 1, pathImagem.length());
			String extensao = DummyUtils.getExtensao(nomeImagem);

			if(!pathImagem.contains(IMAGEM_LAYOUT) && session.getAttribute(USUARIO_SESSION_KEY) == null){
				response.sendError(403);
				return;
			}

			boolean isPdf = "pdf".equals(extensao);
			if(!GetdocConstants.IMAGEM_EXTENSOES.contains(extensao) && !isPdf) {
				response.sendRedirect(contextPath + "/resources/images/arquivo-nao-imagem.png");
				return;
			}

			int index1 = nomeImagem.lastIndexOf("_");
			int index2 = nomeImagem.lastIndexOf(".");
			String idStr = nomeImagem.substring(index1 + 1, index2);

			if(StringUtils.isBlank(idStr)) {
				systraceThread("id não encontrado na url " + requestURI);
				response.sendError(404);
				return;
			}

			boolean face = idStr.contains("-face");
			idStr = idStr.replace("-face", "");
			Long id = StringUtils.isNumeric(idStr) ? new Long(idStr) : null;

			response.setContentType(isPdf ? "application/pdf" : "image/jpeg");
			response.setHeader("cache-control", "604800");//604800 segundos = 7 dias

			WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			Object objImg = getObjetoImagem(servletContext, appContext, pathImagem, id);
			if(objImg == null) {
				systraceThread("objeto não encontrado para id " + id + " na url " + requestURI);
				response.sendError(404);
				return;
			}

			String hashChecksumHeader = request.getHeader("If-None-Match");
			String hashChecksum = getHashChecksum(servletContext, appContext, pathImagem, objImg);
			if(hashChecksum.equals(hashChecksumHeader)) {
				response.sendError(304);
				return;
			}

			File file = getFileDeployed(appContext, pathImagem, objImg, face, request, hashChecksum);
			if(file == null) {
				return;
			}

			response.setContentLength((int) file.length());
			response.setHeader("ETag", hashChecksum);
			response.setBufferSize(BUFFER_SIZE);

			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				systraceThread("arquivo inexistente para id " + id + " na url " + requestURI + ": " + file.getAbsolutePath(), LogLevel.ERROR);
				response.sendError(404);
				return;
			}

			bis = new BufferedInputStream(fis, BUFFER_SIZE);
			ServletOutputStream os = response.getOutputStream();

			try {
				copy(bis, os, BUFFER_SIZE);
			}
			catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		finally {

			if(bis != null) {
				bis.close();
			}
		}
	}

	private void copy(BufferedInputStream is, ServletOutputStream os, int bufferSize) throws IOException {

		byte buffer[] = new byte[bufferSize];
		int len = buffer.length;
		while (true) {
			len = is.read(buffer);
			if (len == -1) break;
			os.write(buffer, 0, len);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getObjetoImagem(ServletContext servletContext, WebApplicationContext appContext, String pathImagem, Long id) throws IOException {

		if(pathImagem.contains(ANEXO_PL)) {

			ProcessoLogAnexoService processoLogAnexoService = (ProcessoLogAnexoService) appContext.getBean(ProcessoLogAnexoService.class);
			ProcessoLogAnexo anexo = processoLogAnexoService.get(id);
			return (T) anexo;
		}
		else if(pathImagem.contains(ANEXO_ER)) {

			EmailRecebidoAnexoService emailRecebidoAnexoService = appContext.getBean(EmailRecebidoAnexoService.class);
			EmailRecebidoAnexo anexo = emailRecebidoAnexoService.get(id);
			return (T) anexo;
		}
		else if(pathImagem.contains(IMAGEM_LAYOUT)) {

			return (T) new Object();
		}
		else {

			ImagemService imagemService = (ImagemService) appContext.getBean(ImagemService.class);
			Imagem imagem = imagemService.get(id);
			return (T) imagem;
		}
	}

	private String getHashChecksum(ServletContext servletContext, WebApplicationContext appContext, String pathImagem, Object obj) throws IOException {

		if(pathImagem.contains(ANEXO_PL)) {

			ProcessoLogAnexo anexo = (ProcessoLogAnexo) obj;
			return anexo.getHashChecksum();
		}
		else if(pathImagem.contains(ANEXO_ER)) {

			EmailRecebidoAnexo anexo = (EmailRecebidoAnexo) obj;
			return anexo.getHashChecksum();
		}
		else if(pathImagem.contains(IMAGEM_LAYOUT)) {

			ParametroService parametroService = (ParametroService) appContext.getBean(ParametroService.class);
			return parametroService.getHashLogo();
		}
		else {

			Imagem imagem = (Imagem) obj;
			return imagem.getHashChecksum();
		}
	}

	private File getFileDeployed(WebApplicationContext appContext, String pathImagem, Object obj, boolean face, HttpServletRequest request, String hashChecksum) throws IOException {

		try {
			ResourceService resourceService = (ResourceService) appContext.getBean(ResourceService.class);
			String realTargetDir = resourceService.getValue(ResourceService.CACHE_PATH);

			File fileDeployed = new File(realTargetDir, pathImagem);

			if(fileDeployed.exists()) {
				String resourceHash = resourceHashMap.get(pathImagem);

				if (fileDeployed.exists() && StringUtils.isBlank(resourceHash)) {
					Imagem imagem = (Imagem) obj;
					resourceHash = imagem.getHashChecksum();
					resourceHashMap.put(pathImagem, resourceHash);
				}

				if (hashChecksum.equals(resourceHash)) {
					return fileDeployed;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		File file = getFileOriginal(appContext, pathImagem, obj, face, request);
		return deployFile(file, pathImagem, appContext, hashChecksum);
	}

	private File getFileOriginal(WebApplicationContext appContext, String pathImagem, Object obj, boolean face, HttpServletRequest request) {

		if(pathImagem.contains(ANEXO_PL)) {

			ProcessoLogAnexo anexo = (ProcessoLogAnexo) obj;
			ProcessoLogAnexoService processoLogAnexoService = (ProcessoLogAnexoService) appContext.getBean(ProcessoLogAnexoService.class);
			return processoLogAnexoService.getFile(anexo);
		}
		else if(pathImagem.contains(ANEXO_ER)) {

			EmailRecebidoAnexo anexo = (EmailRecebidoAnexo) obj;
			EmailRecebidoAnexoService emailRecebidoAnexoService = appContext.getBean(EmailRecebidoAnexoService.class);
			return emailRecebidoAnexoService.getFile(anexo);
		}
		else if(pathImagem.contains(IMAGEM_LAYOUT)) {

			ParametroService parametroService = (ParametroService) appContext.getBean(ParametroService.class);
			File file = parametroService.getLogo();

			String hashChecksumHeader = request.getHeader("If-None-Match");
			if(StringUtils.isNotBlank(hashChecksumHeader)) {
				String hashLogo = parametroService.getHashLogo();
				if(!hashChecksumHeader.equals(hashLogo)) {
					hashLogo = DummyUtils.getHashChecksum(file);
					parametroService.setValor(P.HASH_LOGO, hashLogo);
				}
			}

			return file;
		}
		else {

			Imagem imagem = (Imagem) obj;
			ImagemService imagemService = (ImagemService) appContext.getBean(ImagemService.class);
			if(face) {
				String caminhoFacial = imagem.getCaminhoFacial();
				return new File(caminhoFacial);
			} else {
				return imagemService.getFile(imagem);
			}
		}
	}

	private File deployFile(File file, String pathImagem, WebApplicationContext appContext, String hashChecksum) throws IOException {

		if(file == null) {
			return null;
		}

		ResourceService resourceService = (ResourceService) appContext.getBean(ResourceService.class);
		String realTargetDir = resourceService.getValue(ResourceService.CACHE_PATH);

		File destinoFile = new File(realTargetDir, pathImagem);
		if(destinoFile.exists()) {
			DummyUtils.deleteFile(destinoFile);
		}

		FileUtils.copyFile(file, destinoFile);

		resourceHashMap.put(pathImagem, hashChecksum);

		return destinoFile;
	}

	@Override
	public void destroy() {
	}
}
