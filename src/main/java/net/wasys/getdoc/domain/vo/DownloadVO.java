package net.wasys.getdoc.domain.vo;

import java.io.File;

public class DownloadVO {

	private File file;
	private String fileName;


	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
