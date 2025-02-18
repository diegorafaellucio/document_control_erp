package net.wasys.util.image;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageRotate extends AbstractImageUtil {

	public ImageRotate(String url) throws IOException {
		super(url);
	}

	public ImageRotate(File file) throws IOException {
		super(file);
	}

	public ImageRotate(BufferedImage img) {
		super(img);
	}

	public void rotacionarImagem(int angulo) {

		int width = getWidth();
		int height = getHeight();

		// Rotation information
		double rotationRequired = Math.toRadians(angulo);
		double meioWidth = width / 2;
		double meioHeight = height / 2;

		AffineTransform at = AffineTransform.getRotateInstance(rotationRequired, meioWidth, meioHeight);

		//Rotaciona as coordenadas dos cantos da imagem
		Point2D[] aCorners = new Point2D[4];
		aCorners[0] = at.transform(new Point2D.Double(0.0, 0.0), null);
		aCorners[1] = at.transform(new Point2D.Double(width, 0.0), null);
		aCorners[2] = at.transform(new Point2D.Double(0.0, height), null);
		aCorners[3] = at.transform(new Point2D.Double(width, height), null);

		// Obtém o valor de translação para cada eixo com um canto "escondido"
		double dTransX = 0;
		double dTransY = 0;
		for(int i = 0; i < 4; i++) {
			if(aCorners[i].getX() < 0 && aCorners[i].getX() < dTransX) {
				dTransX = aCorners[i].getX();
			}
			if(aCorners[i].getY() < 0 && aCorners[i].getY() < dTransY) {
				dTransY = aCorners[i].getY();
			}
		}
		AffineTransform at2 = new AffineTransform();
		at2.translate(-dTransX, -dTransY);
		at.preConcatenate(at2);

		AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

		// Drawing the rotated image at the required drawing locations
		BufferedImage filtered = ato.filter(img, null);

		int type = img.getType();
		type = type == 0 ? BufferedImage.TYPE_INT_RGB : type;
		BufferedImage imgNew = new BufferedImage(filtered.getWidth(), filtered.getHeight(), type);
		Graphics2D grapImg = imgNew.createGraphics();
		grapImg.drawImage(filtered, 0, 0, null);

		grapImg.dispose();
		img = imgNew;
	}
}
