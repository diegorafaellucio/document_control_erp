package net.wasys.util.image;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import fiji.selection.Select_Bounding_Box;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.plugin.filter.RankFilters;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class ImageBorderCrop {

	private BufferedImage img;

	public ImageBorderCrop(String url) throws IOException {
		img = ImageIO.read(new File(url));
	}

	public ImageBorderCrop(File file) throws IOException {
		img = ImageIO.read(file);
	}

	public ImageBorderCrop( BufferedImage img) {
		this.img = img;
	}

	public int getWidth () {
		return img.getWidth();
	}

	public int getHeight () {
		return img.getHeight();
	}

	public Image getImage() {
		int width = img.getWidth();
		int height = img.getHeight();
		return img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
	}

	public BufferedImage getBufferedImage() {
		return img;
	}

	public File getFile() {

		try {
			File file = File.createTempFile("image-resizer", ".jpg");
			writeToFile(file);

			return file;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeToFile(File file) {

		try {
			BufferedImage img = getBufferedImage();
			ImageIO.write(img, "JPG", file);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setOutputImage(OutputStream out, float resolution) throws IOException {
		ImageIO.write(img, "JPG", out);
	}

	public void recortarBordaProporcional(int width, int height) {

		double proporcaoModelo = (double) height / (double) width;
		BufferedImage img2 = img;

		int width2 = img2.getWidth();
		int height2 = img2.getHeight();

		Map<Double, Rectangle> map = new TreeMap<Double, Rectangle>();
		int minBri = 70;
		int linhaCorte = 0;

		do {
			Rectangle borda = encontrarBordas(img2, minBri);

			double newWidth = borda.getWidth();
			double newHeight = borda.getHeight();

			double proporcao = newWidth < newHeight ? newWidth / newHeight : newHeight / newWidth;
			double diferenca = proporcaoModelo - proporcao;
			diferenca = Math.abs(diferenca);

			if(newWidth / width2 < 0.25) {
				//diminuiu demais
				break;
			}

			map.put(diferenca, borda);

			if(diferenca < 0.0199) {
				if(linhaCorte > 0) {
					break;
				} else {
					linhaCorte++;
				}
			}

			if(minBri > 30) {
				minBri -=  15;
			} else if(minBri > 10) {
				minBri -=  5;
			} else {
				minBri -=  2;
			}
		}
		while(minBri >= 2);

		if(map.isEmpty()) {
			this.img = img2;
			return;
		}

		Set<Double> ks = map.keySet();
		Iterator<Double> it = ks.iterator();
		Double diferenca = it.next();
		Rectangle borda = map.isEmpty() ? null : map.get(diferenca);
		boolean mudouTamanho = borda == null || ((double) borda.width / width2) < 0.9;

		if(diferenca > 0.03 || !mudouTamanho) {

			ImagePlus ip = bufferedImageToImagePlus(img2);
			ColorProcessor cp = new ColorProcessor(img2);
			RankFilters rankFilters = new RankFilters();
			rankFilters.setup("outliers", ip);
			rankFilters.rank(cp, 20d, 5, 1, 120F);
			img2 = cp.getBufferedImage();

			Rectangle borda2 = encontrarBordas(img2, 50);
			boolean mudouTamanho2 = borda2 == null || ((double) borda2.width / width2) < 0.9;
			if(!mudouTamanho && mudouTamanho2) {

				borda = borda2;
			}
			else {

				double proporcao2 = borda2.width < borda2.height ? (double) borda2.width / borda2.height : (double) borda2.height / borda2.width;
				double diferenca2 = proporcaoModelo - proporcao2;
				diferenca2 = Math.abs(diferenca2);
				if(diferenca2 < diferenca) {

					borda = borda2;
				}
			}
		}

		if(borda.width != width2 && borda.height != height2) {
			img2 = img.getSubimage(borda.x, borda.y, borda.width, borda.height);
		}

		this.img = img2;
	}

	private Rectangle encontrarBordas(BufferedImage img, int minBri) {

		int width = img.getWidth();
		int height = img.getHeight();
		ImagePlus imp = bufferedImageToImagePlus(img);

		ColorThresholder colorThresholder = new ColorThresholder();
		colorThresholder.setup(imp);
		colorThresholder.setMinBri(minBri);
		colorThresholder.apply(imp);
		imp.updateAndDraw();
		img = imp.getBufferedImage();

		ColorProcessor cp1 = new ColorProcessor(img);
		Select_Bounding_Box sbb = new Select_Bounding_Box();
		double background = sbb.guessBackground(cp1);
		Rectangle rect = new Rectangle(0, 0, width, height);
		Rectangle boundingBox = Select_Bounding_Box.getBoundingBox(cp1, rect, background);

		return boundingBox;
	}

	private static ImagePlus bufferedImageToImagePlus(BufferedImage imgBi1) {

		ImagePlus imp = new ImagePlus("img.jpg", imgBi1);
		if (imp.getType()==ImagePlus.COLOR_RGB) {
			convertGrayJpegTo8Bits(imp);
		}

		FileInfo fi = new FileInfo();
		fi.fileFormat = FileInfo.GIF_OR_JPG;
		fi.fileName = "img.jpg";
		imp.setFileInfo(fi);
		return imp;
	}

	private static void convertGrayJpegTo8Bits(ImagePlus imp) {

		ImageProcessor ip = imp.getProcessor();
		if (ip.getBitDepth()==24 && ip.isGrayscale()) {
			new ImageConverter(imp).convertToGray8();
		}
	}

}
