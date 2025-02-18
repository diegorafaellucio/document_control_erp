package net.wasys.getdoc.bean.datamodel;

import net.wasys.getdoc.domain.entity.BacalhauEmail;
import net.wasys.getdoc.domain.service.BacalhauEmailService;
import net.wasys.getdoc.domain.service.BacalhauService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.List;
import java.util.Map;

public class BacalhauEmailDataModel extends LazyDataModel<BacalhauEmail> {

	private BacalhauEmailService bacalhauEmailService;
	private List<BacalhauEmail> list;

	@Override
	public List<BacalhauEmail> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		int count = bacalhauEmailService.countEmails();
		setRowCount(count);

		if (count == 0) {
			return null;
		}
		list = bacalhauEmailService.findEmails(first, pageSize);

		return list;
	}

	public void setService(BacalhauEmailService bacalhauEmailService) {
		this.bacalhauEmailService = bacalhauEmailService;
	}

}
