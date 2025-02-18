package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.util.DummyUtils;
import net.wasys.util.servlet.LogAcessoFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImagemTransaction {

	private Set<String> deleteOnCommit = new HashSet<>();
	private Set<String> imagensDigitalizadas = new HashSet<>();
	private Set<String> deleteOnRollback = new HashSet<>();

	public void rollback() {

		for (String caminho : deleteOnRollback) {
			File file = new File(caminho);
			DummyUtils.deleteFile(file);
		}

		isDigitalizadasExistem();
	}

	public void commit() {

		for (String caminho : deleteOnCommit) {
			File file = new File(caminho);
			DummyUtils.deleteFile(file);
		}

		isDigitalizadasExistem();
	}

	public void addToDeleteOnCommit(String caminho) {
		deleteOnCommit.add(caminho);
	}

	public void addDigitalizadas(String caminho) {
		imagensDigitalizadas.add(caminho);
	}

	public void addToDeleteOnRollback(String caminho) {
		deleteOnRollback.add(caminho);
	}

	public Set<String> getImagensDigitalizadas() {
		return imagensDigitalizadas;
	}

	public void setImagensDigitalizadas(Set<String> imagensDigitalizadas) {
		this.imagensDigitalizadas = imagensDigitalizadas;
	}

	public void isDigitalizadasExistem() {
		isDigitalizadasExistem(null);
	}

	public void isDigitalizadasExistem(Integer index) {

		index = index == null ? 3 : index;

		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();

		StackTraceElement ste = Thread.currentThread().getStackTrace()[index];
		String className = ste.getClassName();
		int lineNumber = ste.getLineNumber();
		String codLocation = className + ":" + lineNumber;

		ImagemTransactionVO vo = new ImagemTransactionVO();
		List<ImagemTransactionVO> list = new ArrayList<>();
		for (String path : imagensDigitalizadas) {
			File file = new File(path);
			boolean exists = file.exists();
			vo.setExists(exists);
			vo.setCaminho(path);
			vo.setCodLocation(codLocation);
			list.add(vo);
		}

		String json = DummyUtils.objectToJson(list);
		DummyUtils.addParameter(logAcesso, codLocation, json);
		DummyUtils.systraceThread(json);

	}

	private class ImagemTransactionVO {
		private boolean exists;
		private String caminho;
		private String codLocation;

		public void setExists(boolean exists) {
			this.exists = exists;
		}

		public boolean isExists() {
			return exists;
		}

		public void setCaminho(String caminho) {
			this.caminho = caminho;
		}

		public String getCaminho() {
			return caminho;
		}

		public void setCodLocation(String codLocation) {
			this.codLocation = codLocation;
		}

		public String getCodLocation() {
			return codLocation;
		}
	}
}
