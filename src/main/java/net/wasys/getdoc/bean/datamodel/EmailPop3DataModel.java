package net.wasys.getdoc.bean.datamodel;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.sun.mail.pop3.POP3Folder;

import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.service.EmailPop3Service;

public class EmailPop3DataModel extends LazyDataModel<EmailRecebido> {

	private EmailPop3Service emailPop3Service;
	private List<EmailRecebido> list;
	private POP3Folder folderPop3;

	@Override
	public List<EmailRecebido> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {

		try {
			int count = emailPop3Service.countMessages(folderPop3);
			setRowCount(count);

			if(count == 0) {
				return null;
			}

			int last = first + pageSize;
			last = last > count ? count : last;

			list = emailPop3Service.listMessages(folderPop3, first, last);

			return list;
		}
		catch (RuntimeException e) {
			handleException(e);
			throw e;
		}
	}

	private void handleException(RuntimeException e) {

		//TODO!!
	}

	public void setService(EmailPop3Service emailPop3Service) {
		this.emailPop3Service = emailPop3Service;
	}

	public void setFolderPop3(POP3Folder folderPop3) {
		this.folderPop3 = folderPop3;
	}
}
