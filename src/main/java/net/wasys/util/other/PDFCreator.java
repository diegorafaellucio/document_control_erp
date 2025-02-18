package net.wasys.util.other;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.css.value.PageSize;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.ImageResource;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;

import net.wasys.util.DummyUtils;

public class PDFCreator {

	private final String template;
	private final Map<String, Object> model;
	private HttpServletRequest request;
	private boolean portrait;
	private boolean textoDireto = false;

	public PDFCreator(String template, Map<String, Object> model) {
		this.template = template;
		this.model = model;
	}

	public PDFCreator(String template, boolean textoDireto) {
		this.model = null;
		this.template = template;
		this.textoDireto = textoDireto;
	}

	public byte[] toByteArray() {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		render(baos);

		return baos.toByteArray();
	}

	public File toFile() {

		try {
			File file = File.createTempFile("topdf", ".pdf");
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			render(bos);

			bos.flush();
			bos.close();

			return file;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected void render(OutputStream os) {

		StringBuilder html = new StringBuilder();

		html.append("\n<html>");
		html.append("\n<head>");
		html.append("\n	<style type='text/css'>");
		html.append("\n		@page {");
		if (isPortrait()) {
			html.append("\n		size: " + PageSize.A4.getPageHeight() + " " + PageSize.A4.getPageWidth() + ";");
		} else {
			html.append("\n		size: " + PageSize.A4.getPageWidth() + " " + PageSize.A4.getPageHeight() + ";");
		}
		html.append("\n			size: A4;");
		html.append("\n			margin-right: 0;");
		html.append("\n			margin-left: 0;");
		html.append("\n			margin-top: 0;");
		html.append("\n			margin-bottom: 0;");
		html.append("\n			-fs-flow-top: \"header\";");
		html.append("\n			-fs-flow-bottom: \"footer\";");
		html.append("\n			-fs-flow-left: \"left\";");
		html.append("\n			-fs-flow-right: \"right\";");
		html.append("\n			padding: 0;");
		html.append("\n		}");
		html.append("\n	</style>");
		html.append("\n</head>");
		html.append("\n");

		if(request != null) {
			model.put("context", request.getContextPath());
		}

		if (textoDireto) {
			String aux = DummyUtils.stringToHTML(template);
			html.append("<div style='padding: 2em'>").append(aux).append("</div>");
		}
		else {
			StringWriter writer = new StringWriter();

			File fileFromResource = DummyUtils.getFileFromResource(template);
			try {
				String aux = FileUtils.readFileToString(fileFromResource, "UTF-8");

				String aux2 = VelocityEngineUtils.merge(model, aux);
				html.append(aux2);
				//VelocityEngineUtils.merge(template, writer, model);
				//html.append(writer);
			}
			catch (IOException e) {
				throw  new RuntimeException(e);
			}
		}

		html.append("\n<html>");

		Tidy tidy = new Tidy();
		tidy.setInputEncoding("UTF-8");
		tidy.setOutputEncoding("UTF-8");
		tidy.setShowWarnings(false);
		tidy.setShowErrors(0);
		tidy.setQuiet(true);

		Document doc = tidy.parseDOM(new ByteArrayInputStream(html.toString().getBytes()), null);

		ITextRenderer renderer = new ITextRenderer();

		SharedContext sharedContext = renderer.getSharedContext();
		sharedContext.setUserAgentCallback(new ImageFileITextUserAgent(renderer.getOutputDevice(), renderer.getSharedContext()));

		renderer.setDocument(doc, null);
		renderer.layout();

		try {
			renderer.createPDF(os);
		}
		catch (DocumentException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * classe que trata a recupera��o de imagens que pode ser referencia para um arquivo e n�o para uma url, por exmplo:
	 * 
	 * <img src='C:\pasta\imagem.jpg'/>
	 * 
	 * caso a imagem seja gerada dinamicamente, ela pode ser enviada para um arquivo tempor�rio e usado como no exemplo acima
	 * 
	 * @author Felipe Maschio
	 * @created 12/12/2009
	 */
	private class ImageFileITextUserAgent extends ITextUserAgent {

		public ImageFileITextUserAgent(ITextOutputDevice outputDevice, SharedContext sharedContext) {
			super(outputDevice);

			setSharedContext(sharedContext);
		}

		@Override
		public ImageResource getImageResource(String uri) {

			if(uri.startsWith("net")) {
				File file = DummyUtils.getFileFromResource(uri);
				if(file != null) {
					return toImageResource(uri, file);
				}
			}

			String uriFile = uri.replace('/', File.separatorChar);
			File imagemFile = new File(uriFile);
			if(imagemFile.exists()) {
				return toImageResource(uriFile, imagemFile);
			}

			if(request != null) {

				try {
					ServletContext servletContext = request.getSession().getServletContext();
					URL resource = servletContext.getResource(uri);

					if(resource == null) {

						String contextPath = request.getContextPath();

						if(uri.startsWith(contextPath)) {
							resource = servletContext.getResource(uri.replaceFirst(contextPath, ""));
						}
					}

					if(resource != null) {

						return super.getImageResource(resource.toString());
					}
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

			return super.getImageResource(uri);
		}

		private ImageResource toImageResource(String uriFile, File file) {
			try {
				byte[] bytes = FileUtils.readFileToByteArray(file);
				Image image = Image.getInstance(bytes);
				return new ImageResource(uriFile, new ITextFSImage(image));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public boolean isPortrait() {
		return portrait;
	}

	public void setPortrait(boolean portrait) {
		this.portrait = portrait;
	}
}
