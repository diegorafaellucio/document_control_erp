package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.restws.dto.SubareaDTO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.SubareaRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class SubareaService {

	@Autowired private SubareaRepository subareaRepository;
	@Autowired private AreaService areaService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private LogAlteracaoService logAlteracaoService;

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Subarea subarea, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(subarea);

		try {
			subareaRepository.saveOrUpdate(subarea);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(subarea, usuario, tipoAlteracao);
	}

	public Date getUltimaDataAtualizacao() {
		return subareaRepository.getUltimaDataAtualizacao();
	}

	public Subarea getByGeralId(Long geralId) {
		return subareaRepository.getByGeralId(geralId);
	}

	public List<Subarea> findByArea(Long areaId) {
		return subareaRepository.findByArea(areaId);
	}

	public List<Subarea> findAtivasByArea(Long areaId) {
		return subareaRepository.findAtivasByArea(areaId);
	}

	public Subarea getById(Long subAreaId) {
		return subareaRepository.get(subAreaId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizar(SubareaDTO[] subareasDTO) throws MessageKeyException {

		if(subareasDTO == null) {
			return;
		}

		for (SubareaDTO dto : subareasDTO) {

			Long geralId = dto.getId();
			String descricao = dto.getDescricao();
			String email = dto.getEmail();
			Date dataAtualizacao = dto.getDataAtualizacao();
			Long areaId = dto.getAreaId();
			Long usuarioUltimaAtualizacaoId = dto.getUsuarioUltimaAtualizacaoId();
			Usuario usuario = null;
			if (usuarioUltimaAtualizacaoId != null) {
				usuario = usuarioService.getByGeralId(usuarioUltimaAtualizacaoId);
			}

			Subarea subarea = subareaRepository.getByGeralId(geralId);
			subarea = subarea != null ? subarea : new Subarea();

			Area area = areaService.getByGeralId(areaId);
			if(area == null) {
				DummyUtils.systraceThread("ERRO ao sincronizar subárea " + descricao + ": área não encontrada para geralId " + geralId + ".", LogLevel.ERROR);
				return;
			}

			subarea.setArea(area);

			subarea.setDataAtualizacao(dataAtualizacao);
			subarea.setDescricao(descricao);
			subarea.setEmails(email);
			subarea.setGeralId(geralId);

			Boolean ativo = dto.getAtivo();
			subarea.setAtivo(ativo);

			saveOrUpdate(subarea, usuario);
		}
	}

	public void excluir(Subarea subarea) {
		Long subareaId = subarea.getId();
		subareaRepository.deleteById(subareaId);
	}
}
