package net.wasys.getdoc.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.repository.RoleRepository;

@Service
public class RoleService {

	@Autowired private RoleRepository roleRepository;

	@Transactional(rollbackFor=Exception.class)
	public void delete(Long roleId) {
		roleRepository.delete(roleId);
	}
}
