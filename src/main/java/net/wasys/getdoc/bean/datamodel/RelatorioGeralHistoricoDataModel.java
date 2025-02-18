package net.wasys.getdoc.bean.datamodel;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RelatorioGeralHistoricoDataModel extends LazyDataModel<File> {

	private String diretorio;

	@Override
	public List<File> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		File pasta = new File(diretorio);
		File[] arquivosArray = pasta.listFiles();
		int count = arquivosArray != null ? arquivosArray.length : 0;
		setRowCount(count);

		if(count == 0) {
			return null;
		}

		List<File> arquivos = Arrays.asList(arquivosArray);

			Collections.sort(arquivos, Collections.reverseOrder());


		int size = arquivos.size();
		int toPage = first + pageSize;
		if(toPage > size) {
			toPage = size;
		}
		arquivos = arquivos.subList(first, toPage);

		return arquivos;
	}

	public void setDiretorio(String diretorio) {
		this.diretorio = diretorio;
	}
}