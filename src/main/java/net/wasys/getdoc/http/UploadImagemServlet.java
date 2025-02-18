package net.wasys.getdoc.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import net.wasys.getdoc.domain.service.EmailRecebidoAnexoService;
import net.wasys.getdoc.domain.service.ParametroService;
import org.apache.commons.io.IOUtils;

import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.image.ImageResizer;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@MultipartConfig(maxFileSize=10*1024*1024, maxRequestSize=10*1024*1024, fileSizeThreshold=1024)
public class UploadImagemServlet extends HttpServlet {

	private static final Pattern FILENAME_PATTERN = Pattern.compile(".*filename=\"(.*)\"");

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		ServletContext servletContext = req.getSession().getServletContext();
		WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

		ParametroService parametroService = appContext.getBean(ParametroService.class);
		String valor = parametroService.getValor(ParametroService.P.COMPRIMIR_IMAGEM);
		boolean isComprimirImagem = StringUtils.isNotBlank(valor) && Boolean.valueOf(valor);

		String chaveStr = req.getParameter("chave");
		Integer chave = new Integer(chaveStr);

		UploadSessionHandler ush = UploadSessionHandler.getIntance(servletContext);

		HttpSession session = req.getSession();

		List<FileVO> list = ush.getList(session, chave);

		Collection<Part> parts = req.getParts();
		for (Part part : parts) {

			String header = part.getHeader("content-disposition");
			Matcher matcher = FILENAME_PATTERN.matcher(header);
			matcher.matches();
			String fileName = matcher.group(1);

			File tempFile = File.createTempFile("upload_", ".jpg");
			DummyUtils.deleteOnExitFile(tempFile);

			FileOutputStream fos = new FileOutputStream(tempFile);

			InputStream is = part.getInputStream();
			IOUtils.copy(is, fos);

			//reduz para 90% do tamanho original. Isso representa uma grande redução no tamanho final do arquivo
			//removido pq sobe muito o processador
			if(isComprimirImagem) {
				ImageResizer ir = new ImageResizer(tempFile);
				ir.reduzirProporcional(0.1f);
				ir.writeToFile(tempFile);
			}

			//String hashChecksum = DummyUtils.getHashChecksum(tempFile);

			FileVO vo = new FileVO();
			vo.setFile(tempFile);
			//vo.setHash(hashChecksum);
			vo.setName(fileName);

			list.add(vo);
		}
	}
}
