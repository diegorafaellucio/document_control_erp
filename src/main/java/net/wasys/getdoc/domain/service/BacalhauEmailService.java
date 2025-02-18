package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.BacalhauEmail;
import net.wasys.getdoc.domain.repository.BacalhauEmailRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BacalhauEmailService {
	@Autowired
	private BacalhauEmailRepository bacalhauEmailRepository;

	public List<BacalhauEmail> list(int first, int pageSize) {
		return bacalhauEmailRepository.list(first, pageSize);
	}

	public int countEmails() {
		return bacalhauEmailRepository.count();
	}

	public List<BacalhauEmail> findEmails(int first, int pageSize) {
		return bacalhauEmailRepository.list(first, pageSize);
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdateEmail(BacalhauEmail bacalhauEmail) throws MessageKeyException {
		try {
			bacalhauEmailRepository.saveOrUpdate(bacalhauEmail);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteEmailById(Long id) {
		bacalhauEmailRepository.deleteById(id);
	}
}
