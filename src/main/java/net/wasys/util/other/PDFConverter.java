package net.wasys.util.other;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.wasys.util.DummyUtils;
import org.jpedal.PdfDecoder;
import org.jpedal.PdfDecoderServer;
import org.jpedal.constants.JPedalSettings;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.FontMappings;

import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.image.ImageResizer;

public class PDFConverter {

	public static final long TIME_TO_SLEEP_DEFAULT = 10;
	public static final long TAMANHO_MAXIMO_KB_DEFAULT = 600;
	public static final int NUMERO_MAXIMO_PAGINAS_DEFAULT = 150;

	private Long timeToSleep = TIME_TO_SLEEP_DEFAULT;//Tempo que a thread vai dormir a cada leitura de página. Para evitar picos de processamento. Ideal no mínimo 1000.
	private Long tamanhoMaximoKb = TAMANHO_MAXIMO_KB_DEFAULT;//Tamanho máximo permitido para cada página. Caso exceda, a imagem será reduzida
	private Integer numeroMaximoPaginas = NUMERO_MAXIMO_PAGINAS_DEFAULT;//Retorna erro se o número de páginas do documento excede este valor.

	public List<File> getImagens(File file) throws MessageKeyException {

		PdfDecoder decoder = new PdfDecoder();
		FontMappings.setFontReplacements();

		try {
			decoder.openPdfFile(file.getAbsolutePath());

			Map<Integer, Object> mapValues = new HashMap<Integer, Object>();
			//do not scale above this figure
			mapValues.put(JPedalSettings.EXTRACT_AT_BEST_QUALITY_MAXSCALING, 2);
			//alternatively secify a page size (aspect ratio preserved so will do best fit)
			//set a page size (JPedal will put best fit to this)
			mapValues.put(JPedalSettings.EXTRACT_AT_PAGE_SIZE, new String[]{"2000", "1600"});
			//which takes priority (default is false)
			mapValues.put(JPedalSettings.PAGE_SIZE_OVERRIDES_IMAGE, Boolean.TRUE);

			PdfDecoderServer.modifyJPedalParameters(mapValues);

			int numPages = decoder.getPageCount();
			if(numeroMaximoPaginas != null && numPages > numeroMaximoPaginas) {
				throw new MessageKeyException("pdf-maxPages.error", numeroMaximoPaginas, file.getName());
			}

			List<File> imagePages = new ArrayList<File>(numPages);
			for (int i = 1; i <= numPages; i++) {

				BufferedImage page = decoder.getPageAsHiRes(i);

				File imageFile = createImage(page, i);
				long tamanhoKb = imageFile.length() / 1024;

				if(tamanhoMaximoKb != null && tamanhoKb > tamanhoMaximoKb) {

					ImageResizer ir = new ImageResizer(page);
					ir.reduzirParaTamanho(tamanhoMaximoKb);
					ir.writeToFile(imageFile);
				}

				imagePages.add(imageFile);

				if(timeToSleep != null) {

					try {
						Thread.sleep(timeToSleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			return imagePages;
		}
		catch (PdfException e) {
			throw new RuntimeException(e);
		}
		finally {
			if(decoder != null) {
				decoder.closePdfFile();
			}
		}
	}

	private File createImage(BufferedImage page, int pg) {

		try {
			File tempFile = File.createTempFile("pdf_to_img", ".jpg");
			DummyUtils.deleteOnExitFile(tempFile);

			ImageIO.write(page, "JPG", tempFile);

			return tempFile;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setNumeroMaximoPaginas(Integer numeroMaximoPaginas) {
		this.numeroMaximoPaginas = numeroMaximoPaginas;
	}

	public void setTamanhoMaximoKb(Long tamanhoMaximoBytes) {
		this.tamanhoMaximoKb = tamanhoMaximoBytes;
	}

	public void setTimeToSleep(Long timeToSleep) {
		this.timeToSleep = timeToSleep;
	}
}
