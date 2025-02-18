package net.wasys.getdoc.mb.faces.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.apache.commons.lang.StringUtils;


/**
 * 
 * PaginDataModel
 * 23/11/2015 10:37:18
 * @author Everton Luiz Pascke
 */
public abstract class PaginDataModel<T> extends ListDataModel<T> {

	private int qtde;
	private int page;
	private int count;
	private int limit = 20;
	private String sort;
	private boolean asc;
	private List<Integer> pages;
	
	public PaginDataModel() {
		
	}
	
	public PaginDataModel(int limit) {
		this.limit = limit;
	}
	
	@Override
	public boolean isRowAvailable() {
		Object data = getWrappedData();
		if (data == null) {
			load();
		}
		return super.isRowAvailable();
	}
	
	public void sort(String name) {
		if (StringUtils.equals(sort, name)) {
			asc = !asc;
		}
		else {
			asc = true;
			sort = name;
		}
		load();
	}
	
	public boolean hasNext() {
        return page < (qtde - 1);
    }

    public boolean hasPrevious() {
        return page > 0;
    }
	
	public void load() {
		count = count();
		if (count > 0) {
			qtde = count / limit;
			if (count % limit > 0) {
				qtde++;
			}
			pages = new LinkedList<Integer>();
			for (int j = 0; j < qtde; j++) {
				pages.add(j);
			}
			int first = page * limit;
			List<T> data = load(sort, asc, first, limit);
			setWrappedData(data);
		}
		else {
			setWrappedData(new ArrayList<T>());
		}
	}
	
	public void next() {
		if (hasNext()) {
			this.page ++;
			load();
		}
	}

	public void previous() {
		if (hasPrevious()) {
			this.page --;
			load();
		}
	}

	public void paging(int page) {
		this.page = page;
		load();
	}
	
	// Getters ----------------------------------------------------------------------
	public int getQtde() {
		return qtde;
	}
	
	public int getPage() {
		return page;
	}
	
	public List<Integer> getPages() {
		return pages;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	// Abstract ---------------------------------------------------------------------
	protected abstract int count();
	protected abstract List<T> load(String sort, boolean asc, int first, int limit);
}
