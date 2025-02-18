package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;


import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.AreaRepository;
import net.wasys.getdoc.restws.dto.AreaDTO;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
public class AreaService {

	@Autowired private AreaRepository areaRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private SubareaService subareaService;

	public Area get(Long id) {
		return areaRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Area area, List<Subarea> subareas, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(area);

		area.setDataAtualizacao(new Date());

		try {
			areaRepository.saveOrUpdate(area);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		if(subareas != null) {
			for (Subarea subarea : subareas) {
				subareaService.saveOrUpdate(subarea, usuario);
			}

			List<Subarea> atuais = subareaService.findByArea(area.getId());
			atuais.removeAll(subareas);
			for (Subarea subarea : atuais) {
				subareaService.excluir(subarea);
			}
		}

		logAlteracaoService.registrarAlteracao(area, usuario, tipoAlteracao);
	}

	public List<Area> findAll(Integer inicio, Integer max) {
		return areaRepository.findAll(inicio, max);
	}

	public int count() {
		return areaRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long areaId, Usuario usuario) throws MessageKeyException {

		Area area = get(areaId);

		logAlteracaoService.registrarAlteracao(area, usuario, TipoAlteracao.EXCLUSAO);

		try {
			areaRepository.deleteById(areaId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public List<Area> findAtivas() {
		return areaRepository.findAtivas();
	}

	public Date getUltimaDataAtualizacao() {
		return areaRepository.getUltimaDataAtualizacao();
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizar(AreaDTO[] areas) throws MessageKeyException {

		DummyUtils.systraceThread("areas: " + (areas != null ? areas.length : "null"));

		if(areas == null) {
			return;
		}

		for (AreaDTO dto : areas) {

			Long geralId = dto.getId();
			String descricao = dto.getDescricao();
			Boolean ativo = dto.getAtivo();
			Date dataAtualizacao = dto.getDataAtualizacao();
			/*Long usuarioUltimaAtualizacaoId = dto.getUsuarioUltimaAtualizacaoId();
			Usuario usuario = null;
			if (usuarioUltimaAtualizacaoId != null) {
				usuario = usuarioService.getByGeralId(usuarioUltimaAtualizacaoId);
			}*/

			Area area = areaRepository.getByGeralId(geralId);
			area = area != null ? area : new Area();
			area.setGeralId(geralId);
			area.setDataAtualizacao(dataAtualizacao);
			area.setDescricao(descricao);
			area.setAtivo(ativo);

			saveOrUpdate(area, null, null);
		}
	}

	public Area getByGeralId(Long geralId) {
		return areaRepository.getByGeralId(geralId);
	}
}
