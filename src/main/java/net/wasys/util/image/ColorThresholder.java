package net.wasys.util.image;
import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.Undo;
import ij.measure.Measurements;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ColorSpaceConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class ColorThresholder {

	private byte[] hSource;
	private byte[] sSource;
	private byte[] bSource;
	private boolean bandPassH = true;
	private boolean bandPassS = true;
	private boolean bandPassB = true;
	private int numPixels;
	private int minHue = 0;
	private int minSat = 0;
	private int minBri = 0;
	private int maxHue = 255;
	private int maxSat = 255;
	private int maxBri = 255;
	private static final int RED=0, WHITE=1, BLACK=2, BLACK_AND_WHITE=3;
	private static int mode = RED;
	private static final int HSB=0, RGB=1, LAB=2, YUV=3;
	private int colorSpace = HSB;
	private static AutoThresholder thresholder = new AutoThresholder();
	private static final int DEFAULT = 0;
	private static String[] methodNames = AutoThresholder.getMethods();
	private static String method = methodNames[DEFAULT];

	public void setMinBri(int minBri) {
		this.minBri = minBri;
	}
	
	public void apply(ImagePlus imp) {

		ImageProcessor fillMaskIP = (ImageProcessor) imp.getProperty("Mask");
		if (fillMaskIP == null)
			return;
		byte[] fillMask = (byte[]) (byte[]) fillMaskIP.getPixels();
		byte fill = -1;
		byte keep = 0;

		if (bandPassH && bandPassS && bandPassB) {

			for (int j = 0; j < this.numPixels; ++j) {
				int hue = this.hSource[j] & 0xFF;
				int sat = this.sSource[j] & 0xFF;
				int bri = this.bSource[j] & 0xFF;
				if ((hue < this.minHue) || (hue > this.maxHue)
						|| (sat < this.minSat) || (sat > this.maxSat)
						|| (bri < this.minBri) || (bri > this.maxBri))
					fillMask[j] = keep;
				else
					fillMask[j] = fill;
			}
		}
		else if ((!bandPassH)
				&& (!bandPassS)
				&& (!bandPassB))
			for (int j = 0; j < this.numPixels; ++j) {
				int hue = this.hSource[j] & 0xFF;
				int sat = this.sSource[j] & 0xFF;
				int bri = this.bSource[j] & 0xFF;
				if (((hue >= this.minHue) && (hue <= this.maxHue))
						|| ((sat >= this.minSat) && (sat <= this.maxSat))
						|| ((bri >= this.minBri) && (bri <= this.maxBri)))
					fillMask[j] = keep;
				else
					fillMask[j] = fill;
			}
		else if (bandPassH && bandPassS
				&& (!bandPassB))
			for (int j = 0; j < this.numPixels; ++j) {
				int hue = this.hSource[j] & 0xFF;
				int sat = this.sSource[j] & 0xFF;
				int bri = this.bSource[j] & 0xFF;
				if ((hue < this.minHue) || (hue > this.maxHue)
						|| (sat < this.minSat) || (sat > this.maxSat)
						|| ((bri >= this.minBri) && (bri <= this.maxBri)))
					fillMask[j] = keep;
				else
					fillMask[j] = fill;
			}
		else if ((!bandPassH)
				&& (!bandPassS)
				&& bandPassB)
			for (int j = 0; j < this.numPixels; ++j) {
				int hue = this.hSource[j] & 0xFF;
				int sat = this.sSource[j] & 0xFF;
				int bri = this.bSource[j] & 0xFF;
				if (((hue >= this.minHue) && (hue <= this.maxHue))
						|| ((sat >= this.minSat) && (sat <= this.maxSat))
						|| (bri < this.minBri) || (bri > this.maxBri))
					fillMask[j] = keep;
				else
					fillMask[j] = fill;
			}
		else if (bandPassH && (!bandPassS)
				&& (!bandPassB))
			for (int j = 0; j < this.numPixels; ++j) {
				int hue = this.hSource[j] & 0xFF;
				int sat = this.sSource[j] & 0xFF;
				int bri = this.bSource[j] & 0xFF;
				if ((hue < this.minHue) || (hue > this.maxHue)
						|| ((sat >= this.minSat) && (sat <= this.maxSat))
						|| ((bri >= this.minBri) && (bri <= this.maxBri)))
					fillMask[j] = keep;
				else
					fillMask[j] = fill;
			}
		else if ((!bandPassH) && bandPassS
				&& bandPassB)
			for (int j = 0; j < this.numPixels; ++j) {
				int hue = this.hSource[j] & 0xFF;
				int sat = this.sSource[j] & 0xFF;
				int bri = this.bSource[j] & 0xFF;
				if (((hue >= this.minHue) && (hue <= this.maxHue))
						|| (sat < this.minSat) || (sat > this.maxSat)
						|| (bri < this.minBri) || (bri > this.maxBri))
					fillMask[j] = keep;
				else
					fillMask[j] = fill;
			}
		else if ((!bandPassH) && bandPassS
				&& (!bandPassB))
			for (int j = 0; j < this.numPixels; ++j) {
				int hue = this.hSource[j] & 0xFF;
				int sat = this.sSource[j] & 0xFF;
				int bri = this.bSource[j] & 0xFF;
				if (((hue >= this.minHue) && (hue <= this.maxHue))
						|| (sat < this.minSat) || (sat > this.maxSat)
						|| ((bri >= this.minBri) && (bri <= this.maxBri)))
					fillMask[j] = keep;
				else
					fillMask[j] = fill;
			}
		else if (bandPassH && (!bandPassS)
				&& bandPassB) {
			for (int j = 0; j < this.numPixels; ++j) {
				int hue = this.hSource[j] & 0xFF;
				int sat = this.sSource[j] & 0xFF;
				int bri = this.bSource[j] & 0xFF;
				if ((hue < this.minHue) || (hue > this.maxHue)
						|| ((sat >= this.minSat) && (sat <= this.maxSat))
						|| (bri < this.minBri) || (bri > this.maxBri))
					fillMask[j] = keep;
				else {
					fillMask[j] = fill;
				}
			}
		}

		ImageProcessor ip = imp.getProcessor();
		if (ip == null)
			return;
		if (mode == 3) {
			int[] pixels = (int[]) (int[]) ip.getPixels();
			int fcolor = (Prefs.blackBackground) ? -1 : -16777216;
			int bcolor = (Prefs.blackBackground) ? -16777216 : -1;
			for (int i = 0; i < this.numPixels; ++i)
				if (fillMask[i] != 0)
					pixels[i] = fcolor;
				else
					pixels[i] = bcolor;
		} else {
			ip.setColor(thresholdColor());
			ip.fill(fillMaskIP);
		}
	}

	boolean setup(ImagePlus imp) {

		ImageProcessor ip;
		int type = imp.getType();
		if (type!=ImagePlus.COLOR_RGB)
			return false;
		ip = imp.getProcessor();

		Undo.reset();
		ImageStack stack = imp.getStack();
		int width = stack.getWidth();
		int height = stack.getHeight();
		numPixels = width * height;

		hSource = new byte[numPixels];
		sSource = new byte[numPixels];
		bSource = new byte[numPixels];

		ImageProcessor mask = new ByteProcessor(width, height);
		imp.setProperty("Mask", mask);

		//Get hsb or rgb from image.
		ColorProcessor cp = (ColorProcessor)ip;
		IJ.showStatus("Converting colour space...");
		if(colorSpace==RGB)
			cp.getRGB(hSource,sSource,bSource);
		else if(colorSpace==HSB)
			cp.getHSB(hSource,sSource,bSource);
		else if(colorSpace==LAB)
			getLab(cp, hSource,sSource,bSource);
		else if(colorSpace==YUV)
			getYUV(cp, hSource,sSource,bSource);

		IJ.showStatus("");

		//Create a spectrum ColorModel for the Hue histogram plot.
		Color c;
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		for (int i=0; i<256; i++) {
			c = Color.getHSBColor(i/255f, 1f, 1f);
			reds[i] = (byte)c.getRed();
			greens[i] = (byte)c.getGreen();
			blues[i] = (byte)c.getBlue();
		}

		autoSetThreshold(imp);
		imp.updateAndDraw();

		return ip != null;
	}

	void autoSetThreshold(ImagePlus imp) {

		ImageProcessor ip = imp.getProcessor();
		ImageStatistics stats = ImageStatistics.getStatistics(ip, Measurements.AREA + Measurements.MODE, null);
		int[] histogram = stats.histogram;
		boolean darkb = true;

		switch (colorSpace) {
		case HSB:

			int threshold = thresholder.getThreshold(method, histogram);
			if (darkb) {
				minBri = threshold+1;
				maxBri = 255;
			} else {
				minBri = 0;
				maxBri = threshold;
			}
			break;
		case RGB:

			threshold = thresholder.getThreshold(method, histogram);
			if (darkb) {
				minHue = threshold+1;
				maxHue = 255;
			} else {
				minHue = 0;
				maxHue = threshold;
			}
			threshold = thresholder.getThreshold(method, histogram);
			if (darkb) {
				minSat = threshold+1;
				maxSat = 255;
			} else {
				minSat = 0;
				maxSat = threshold;
			}
			threshold = thresholder.getThreshold(method, histogram);
			if (darkb) {
				minBri = threshold+1;
				maxBri = 255;
			} else {
				minBri = 0;
				maxBri = threshold;
			}
			break;
		case LAB: case YUV:
			threshold = thresholder.getThreshold(method, histogram);
			if (darkb) {
				minHue = threshold+1;
				maxHue = 255;
			} else {
				minHue = 0;
				maxHue = threshold;
			}
			break;
		}
	}

	public static void getLab(ImageProcessor ip, byte[] L, byte[] a, byte[] b) {
		ColorSpaceConverter converter = new ColorSpaceConverter();
		int[] pixels = (int[])ip.getPixels();
		for (int i=0; i<pixels.length; i++) {
			double[] values = converter.RGBtoLAB(pixels[i]);
			int L1 = (int) (values[0] * 2.55);
			int a1 = (int) (Math.floor((1.0625 * values[1] + 128) + 0.5));
			int b1 = (int) (Math.floor((1.0625 * values[2] + 128) + 0.5));
			L[i] = (byte)((int)(L1<0?0:(L1>255?255:L1)) & 0xff);
			a[i] = (byte)((int)(a1<0?0:(a1>255?255:a1)) & 0xff);
			b[i] = (byte)((int)(b1<0?0:(b1>255?255:b1)) & 0xff);
		}
	}

	public void getYUV(ImageProcessor ip, byte[] Y, byte[] U, byte[] V) {
		// Returns YUV in 3 byte arrays.

		//RGB <--> YUV Conversion Formulas from http://www.cse.msu.edu/~cbowen/docs/yuvtorgb.html
		//R = Y + (1.4075 * (V - 128));
		//G = Y - (0.3455 * (U - 128) - (0.7169 * (V - 128));
		//B = Y + (1.7790 * (U - 128);
		//
		//Y = R *  .299 + G *  .587 + B *  .114;
		//U = R * -.169 + G * -.332 + B *  .500 + 128.;
		//V = R *  .500 + G * -.419 + B * -.0813 + 128.;

		int c, x, y, i=0, r, g, b;
		double yf;

		int width=ip.getWidth();
		int height=ip.getHeight();

		for(y=0;y<height; y++) {
			for (x=0; x< width;x++){
				c = ip.getPixel(x,y);

				r = ((c&0xff0000)>>16);//R
				g = ((c&0x00ff00)>>8);//G
				b = ( c&0x0000ff); //B 

				// Kai's plugin
				yf = (0.299 * r  + 0.587 * g + 0.114 * b);
				Y[i] = (byte)((int)Math.floor(yf + 0.5)) ;
				U[i] = (byte)(128+(int)Math.floor((0.493 *(b - yf))+ 0.5)); 
				V[i] = (byte)(128+(int)Math.floor((0.877 *(r - yf))+ 0.5)); 

				//Y[i] = (byte) (Math.floor( 0.299 * r + 0.587 * g + 0.114  * b)+.5);
				//U[i] = (byte) (Math.floor(-0.169 * r - 0.332 * g + 0.500  * b + 128.0)+.5);
				//V[i] = (byte) (Math.floor( 0.500 * r - 0.419 * g - 0.0813 * b + 128.0)+.5);

				i++;
			}
		}
	}

	private Color thresholdColor() {
		Color color = null;
		switch (mode) {
		case RED: color=Color.red; break;
		case WHITE: color=Color.white; break;
		case BLACK: color=Color.black; break;
		case BLACK_AND_WHITE: color=Color.black; break;
		}
		return color;
	}
}
