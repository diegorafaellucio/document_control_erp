package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.AdminAjuda;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.repository.AdminAjudaRepository;
import net.wasys.getdoc.domain.vo.DownloadVO;
import net.wasys.getdoc.domain.vo.filtro.AdminAjudaFiltro;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class AdminAjudaService {

	@Autowired private AdminAjudaRepository adminAjudaRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private ResourceService resourceService;

	public class AdminAjudaCriadaException extends RuntimeException {
		private AdminAjuda adminAjuda;

		public AdminAjudaCriadaException(AdminAjuda adminAjuda, Exception e) {
			super(e);
			this.adminAjuda = adminAjuda;
		}

		public AdminAjuda getAdminAjuda() {
			return adminAjuda;
		}
	}

	public AdminAjuda get(Long id) {
		return adminAjudaRepository.get(id);
	}

	@Transactional(rollbackFor = Exception.class)
	public void excluir(Long adminAjudaId, Usuario usuario) throws ProcessoRestException {

		try {

			AdminAjuda adminAjuda = get(adminAjudaId);
			String caminho = adminAjuda.getCaminho();
			Long usuarioId = usuario.getId();
			String usuarioNome = usuario.getNome();
			systraceThread("Usuário: " + usuarioNome + " #" + usuarioId + ". Ajuda id: " + adminAjudaId);

			adminAjudaRepository.deleteById(adminAjudaId);

			File file = new File(caminho);
			file.delete();
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(AdminAjuda adminAjuda, Usuario usuario) throws MessageKeyException {

		try {
			adminAjudaRepository.saveOrUpdate(adminAjuda);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

	}

	public List<AdminAjuda> findByFiltro(AdminAjudaFiltro filtro, Integer inicio, Integer max) {
		return adminAjudaRepository.findByFiltro(filtro, inicio, max);
	}

	public int countByFiltro(AdminAjudaFiltro filtro) {
		return adminAjudaRepository.countByFiltro(filtro);
	}

	public String gerarCaminho(AdminAjuda adminAjuda) {
		String dir = resourceService.getValue(ResourceService.ADMINAJUDA_PATH);
		String caminho = AdminAjuda.gerarCaminho(dir, adminAjuda);
		return caminho;
	}

	public File getFile(AdminAjuda adminAjuda) {

		if(adminAjuda == null) {
			return null;
		}

		String caminho = adminAjuda.getCaminho();
		File file = new File(caminho);

		if(!file.exists()) {
			caminho = gerarCaminho(adminAjuda);
			file = new File(caminho);
		}

		return file;
	}

	public DownloadVO getDownload(Long ajudaId) {

		AdminAjuda adminAjuda = get(ajudaId);
		String nome = adminAjuda.getNome();
		String extensao = adminAjuda.getExtensao();
		String idString = ajudaId.toString();

		File file = getFile(adminAjuda);
		DownloadVO vo = new DownloadVO();
		vo.setFile(file);
		vo.setFileName(nome + "-" + idString + "." + extensao);

		return vo;
	}

}

