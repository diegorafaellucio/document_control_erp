package net.wasys.util.image;

import net.wasys.util.DummyUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ImageResizer extends AbstractImageUtil {

	public ImageResizer(String url) {
		super(url);
	}

	public ImageResizer(File file) {
		super(file);
	}

	public ImageResizer(BufferedImage img) {
		super(img);
	}

	public void resizeProporcionalH(Integer width, Integer height) {

		resizeProporcional(width, height, false);
	}

	public void resizeProporcionalW(Integer width, Integer height) {

		resizeProporcional(width, height, true);
	}

	private void resizeProporcional(Integer width, Integer height, boolean manterWidth) {

		width = width != null ? width : 0;
		height = height != null ? height : 0;

		int widthOld = img.getWidth();
		int heightOld = img.getHeight();

		if(width == widthOld || height == heightOld) {
			return;
		}

		if(width != 0 && height != 0) {

			BigDecimal widthOldBD = new BigDecimal(widthOld);
			BigDecimal heightOldBD = new BigDecimal(heightOld);
			BigDecimal widthBD = new BigDecimal(width);
			BigDecimal heightBD = new BigDecimal(height);

			BigDecimal proporcao1 = widthOldBD.divide(heightOldBD, 2, RoundingMode.CEILING);
			BigDecimal proporcao2 = widthBD.divide(heightBD, 2, RoundingMode.CEILING);

			if(!proporcao1.equals(proporcao2)) {

				if(manterWidth) {
					height = 0;
				} else {
					width = 0;
				}
			}
		}

		resize(width, height);
	}

	public void resize(int width, int height) {

		if ((width > 0 && height == 0) || (width == 0 && height > 0)) {

			int size = (width > 0) ? width : height;
			float fRatio = (float)img.getWidth() / (float)img.getHeight();

			if (height == 0) {
				height = (int)(size / fRatio);
			}

			if (width == 0) {
				width = (int)(size * fRatio);
			}
		}

		int type = img.getType();
		type = type == 0 ? BufferedImage.TYPE_INT_RGB : type;
		BufferedImage imgNew = new BufferedImage(width, height, type);
		Graphics2D grapImg = imgNew.createGraphics();
		grapImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		AffineTransform xform = AffineTransform.getScaleInstance((double)width/img.getWidth(), (double)height/img.getHeight());
		grapImg.drawRenderedImage(img, xform);
		grapImg.dispose();
		img = imgNew;
	}

	public void reduzirParaTamanho(long tamanhoMaximoKb) {

		//o ideal é começar com o mínimo, muitas vezes isso já altera drasticamente o tamanho da imagem
		float reducaoIdeal = 0.1f;

		reduzir(tamanhoMaximoKb, reducaoIdeal);
	}

	private void reduzir(long tamanhoMaximoKb, float reducao) {

		reduzirProporcional(reducao);

		File file = getFile();
		long fileSizeKb = file.length() / 1024;
		DummyUtils.deleteFile(file);

		if(fileSizeKb > tamanhoMaximoKb) {

			System.gc();

			float reducao2 = getReducaoIdeal(tamanhoMaximoKb, fileSizeKb);

			reduzir(tamanhoMaximoKb, reducao2);
		}
	}

	public void reduzirProporcional(float reducao) {

		float proporcao = 1 - reducao;
		int height = (int) (getHeight() * proporcao);
		int width = (int) (getWidth() * proporcao);

		resize(width, height);
	}

	private float getReducaoIdeal(long tamanhoMaximoBytes, long fileSize) {

		float reducao2 = 1f - ((float) tamanhoMaximoBytes / (float) fileSize);
		reducao2 -= 0.3;
		if(reducao2 > 0.5f) {
			reducao2 = 0.5f;
		}
		else if(reducao2 < 0 || reducao2 < 0.2f) {
			reducao2 = 0.1f;
		}
		else if(reducao2 < 0.3) {
			reducao2 = 0.2f;
		}
		else if(reducao2 < 0.4) {
			reducao2 = 0.3f;
		}
		else if(reducao2 < 0.5) {
			reducao2 = 0.4f;
		}

		return reducao2;
	}
}
