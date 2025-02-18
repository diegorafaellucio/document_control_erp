package net.wasys.getdoc.mb.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadResultModel {

	public List<String> names;
	public File file;
	public String fileName;
	public String path;

	
	public void add(String name) {
		if (names == null) {
			names = new ArrayList<>();
		}
		names.add(name);
	}

	public String getPath() {
		return path;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public File getFile() {
		return file;
	}
}
