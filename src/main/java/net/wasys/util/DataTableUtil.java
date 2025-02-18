package net.wasys.util;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.component.datatable.DataTable;

import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class DataTableUtil extends AbstractBean {

	public final static int QTDE_REGISTROS_PAGINA = 10;
	private DataTable lazyDataTable;
	
	/**
	 * Retorna um datable com lazy loading e as configurações definidas para o sistema.
	 *
	 * @return DataTable
	 */
	public DataTable getLazyDataTable() {
		return lazyDataTable;
	}

	/**
	 * Configura um datable com lazy loading e as configurações definidas para o sistema.
	 *
	 * @param dataTable
	 */
	public void setLazyDataTable(DataTable lazyDataTable) {
		lazyDataTable.setLazy(true);
		configurarDataTable(lazyDataTable);
	}
	
	private void configurarDataTable(DataTable dataTable) {
		dataTable.setPaginator(true);
		dataTable.setPaginatorAlwaysVisible(true);
		dataTable.setRows(QTDE_REGISTROS_PAGINA);
		dataTable.setLazy(Boolean.TRUE);
		dataTable.setPaginatorPosition("bottom");
		dataTable.setEmptyMessage(getMessage("emptyMessage.label"));
		dataTable.setCurrentPageReportTemplate("Total: {totalRecords} | {currentPage} de {totalPages} ");
		dataTable.setPaginatorTemplate("{CurrentPageReport} {FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink}");
	}

	@Override
	protected void initBean() {
	}
}
