package net.wasys.util.image;

import net.wasys.util.DummyUtils;
import org.apache.commons.io.FileUtils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import static net.wasys.util.DummyUtils.systraceThread;

public class ImageShrinker {

	private BufferedImage img;
	private long fileSize;

	public ImageShrinker(File file) throws IOException {
		this.img = ImageIO.read(file);
		this.fileSize = file.length();
	}

	public void shrink(long sizeK) throws IOException {

		long fileSizeK = fileSize / 1024;
		float reducao = getReducao(sizeK, fileSizeK);

		this.img = reduzirImagem(null, img, sizeK, reducao);
	}

	private BufferedImage reduzirImagem(File tempFile, BufferedImage imagem, long sizeK, float reducao) throws IOException {

		if(imagem == null) {
			imagem = ImageIO.read(tempFile);
		}

		ImageResizer helper = new ImageResizer(imagem);
		float proporcao = 1 - reducao;
		int height = (int) (helper.getHeight() * proporcao);
		int width = (int) (helper.getWidth() * proporcao);

		helper.resize(width, height);
		Image image2 = helper.getImage();

		int width2 = image2.getWidth(null);
		int height2 = image2.getHeight(null);

		BufferedImage bimage = new BufferedImage(width2, height2, BufferedImage.TYPE_INT_RGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(image2, 0, 0, null);
		bGr.dispose();

		long tamanhoKb;
		if(tempFile != null) {

			ImageIO.write(bimage, "jpg", tempFile);
			tamanhoKb = tempFile.length() / 1024;
		}
		else {
			tamanhoKb = getSizeKb(bimage);
		}

		long fileSizeK = tamanhoKb;
		if(fileSizeK > sizeK) {

			//para desalocar memória
			imagem = null;
			helper = null;
			image2 = null;
			bGr = null;

			float reducao2 = getReducao(sizeK, fileSizeK);

			System.gc();

			return reduzirImagem(tempFile, bimage, sizeK, reducao2);
		}

		return bimage;
	}

	public long getSizeKb(BufferedImage page) {

		try {

			ByteArrayOutputStream tmp = new ByteArrayOutputStream();
			ImageIO.write(page, "JPG", tmp);
			tmp.close();
			int tamanhoKb = tmp.size() / 1024;
			return tamanhoKb;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private float getReducao(long sizeK, long fileSizeK) {

		float reducao2 = 1f - ((float)sizeK / fileSizeK);
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

	public BufferedImage getBufferedImage() {
		return img;
	}

	public File getFile() {

		try {
			File file = File.createTempFile("image-shrinker", ".jpg");
			DummyUtils.deleteOnExitFile(file);
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

	public static void main(String[] args) throws IOException {

		long inicio = System.currentTimeMillis();

		File file1 = new File("D:\\Google Drive\\Desktop\\TESTE.jpg");
		systraceThread((file1.length() / 1024) + "kb");

		ImageShrinker is = new ImageShrinker(file1);
		int limiteByte = 1 * 1024;
		is.shrink(limiteByte);
		File file = is.getFile();
		FileUtils.copyFile(file, new File("D:\\Google Drive\\Desktop\\TESTE-ok.jpg"));

		systraceThread((file.length() / 1024) + "kb");

		systraceThread(System.currentTimeMillis() - inicio + "ms");
		systraceThread("");
	}
}
