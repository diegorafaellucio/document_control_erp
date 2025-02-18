package net.wasys.getdoc.domain.service;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.MessageKeyListException;

@Service
public class ExceptionService {

	@Autowired private MessageService messageService;

	public void getMessage(StringBuilder exceptionsMessages, Throwable exception) {

		if(exception instanceof MessageKeyListException) {
			List<MessageKeyException> exceptions = ((MessageKeyListException) exception).getMessageKeyExceptions();
			getMessage(exceptionsMessages, exceptions);
		}
		else if(exception instanceof MessageKeyException) {
			MessageKeyException mke = (MessageKeyException) exception;
			getMessage(exceptionsMessages, mke);
		}
		else {
			String message = DummyUtils.getExceptionMessage(exception);
			exceptionsMessages.append(message).append("\n");
		}
	}

	public void getMessage(StringBuilder exceptionsMessages, List<MessageKeyException> exceptions) {
		for (MessageKeyException mke : exceptions) {
			getMessage(exceptionsMessages, mke);
		}
	}

	private void getMessage(StringBuilder exceptionsMessages, MessageKeyException mke) {
		String key = mke.getKey();
		Object[] args = mke.getArgs();
		String message = messageService.getValue(key, args);
		if(StringUtils.isBlank(message)) {
			message = DummyUtils.getExceptionMessage(mke);
		}
		exceptionsMessages.append(message).append("\n");
	}

	public String getMessage(Throwable e) {
		StringBuilder sb = new StringBuilder();
		getMessage(sb, e);
		return sb.toString();
	}
}
