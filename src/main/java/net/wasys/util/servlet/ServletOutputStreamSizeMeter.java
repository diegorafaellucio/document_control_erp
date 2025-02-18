package net.wasys.util.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class ServletOutputStreamSizeMeter extends ServletOutputStream {

	private OutputStream outputStream;
	private int sizeCount;

	public ServletOutputStreamSizeMeter(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
		sizeCount++;
	}

	public int getSizeCount() {
		return sizeCount;
	}
}