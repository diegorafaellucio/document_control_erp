package net.wasys.util.image;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public abstract class AbstractImageUtil {

	protected BufferedImage img;

	public AbstractImageUtil(String url) {
		try {
			img = ImageIO.read(new File(url));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public AbstractImageUtil(File file) {
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public AbstractImageUtil(BufferedImage img) {
		this.img = img;
	}

	public int getWidth() {
		return img.getWidth();
	}

	public int getHeight() {
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
}
