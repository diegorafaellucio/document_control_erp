package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.util.ddd.AbstractProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
@Scope("prototype")
public class BaseInternaExporter extends AbstractProcessor {

	@Autowired private BaseRegistroService baseRegistroService;

	private String fileName;
	private File file;

	private BaseRegistroFiltro filtro;

	@Override
	protected void execute2() throws Exception {

		String tipo = "Base Interna";
		systraceThread("tipo: " + tipo);

		file = baseRegistroService.render(filtro);
		fileName = "base-interna.xlsx";

		systraceThread("end");
	}

	public String getFileName() {
		return fileName;
	}

	public File getFile() {
		return file;
	}

	public void setFiltro(BaseRegistroFiltro filtro) {
		this.filtro = filtro;
	}
}
