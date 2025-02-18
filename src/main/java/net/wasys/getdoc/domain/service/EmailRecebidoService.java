package net.wasys.getdoc.domain.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.EmailRecebidoAnexo;
import net.wasys.getdoc.domain.repository.EmailRecebidoRepository;

@Service
public class EmailRecebidoService {

	@Autowired private EmailRecebidoRepository emailRecebidoRepository;
	@Autowired private EmailRecebidoAnexoService emailRecebidoAnexoService;

	@Transactional(rollbackFor = Exception.class)
	public void save(EmailRecebido er) {

		er.setData(new Date());

		emailRecebidoRepository.save(er);

		Set<EmailRecebidoAnexo> anexos = er.getAnexos();
		for (EmailRecebidoAnexo anexo : anexos) {

			emailRecebidoAnexoService.save(anexo);
		}
	}

	public boolean existsByMessageUid(String uid) {
		return emailRecebidoRepository.existsByMessageUid(uid);
	}

	public EmailRecebido getByMessageUid(String messageUid) {
		return emailRecebidoRepository.getByMessageUid(messageUid);
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdate(EmailRecebido emailRecebido) {
		emailRecebidoRepository.saveOrUpdate(emailRecebido);
	}
	
	public EmailRecebido get(Long id) {
		return emailRecebidoRepository.get(id);
	}

	public int countNaoLidos(Long processoId) {
		return emailRecebidoRepository.countNaoLidos(processoId);
	}

	public Map<Long, Boolean> getNaoLidos(List<Long> processosIds) {
		return emailRecebidoRepository.getNaoLidos(processosIds);
	}

	public List<EmailRecebido> getByConversationId(String conversationId){
		return emailRecebidoRepository.findByConversationId(conversationId);
	}
}
