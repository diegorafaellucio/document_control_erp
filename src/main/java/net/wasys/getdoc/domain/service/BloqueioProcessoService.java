package net.wasys.getdoc.domain.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.entity.BloqueioProcesso;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.repository.BloqueioProcessoRepository;
import net.wasys.getdoc.domain.vo.BloqueioErrorVO;
import net.wasys.getdoc.domain.vo.VerificacaoBloqueioVO;

@Service
public class BloqueioProcessoService {

	@Autowired private BloqueioProcessoRepository bloqueioProcessoRepository;
	@Autowired private ProcessoService processoService;
	@Autowired private UsuarioService usuarioService;

	public BloqueioProcesso get(Long id) {
		return bloqueioProcessoRepository.get(id);
	}

	public BloqueioProcesso getByProcesso(Long processoId) {

		BloqueioProcesso bloqueio = bloqueioProcessoRepository.getLastBoqueioByProcesso(processoId);
		if(bloqueio != null && isValid(bloqueio)) {
			return bloqueio;
		} else {
			return null;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public BloqueioErrorVO bloquear(Long processoId, Usuario usuario) {

		Long usuarioId = usuario.getId();

		//se está bloqueando outro, desbloqueia o que tinha aberto anteriormente
		BloqueioProcesso lastBoqueio = bloqueioProcessoRepository.getLastBoqueioByUsuario(usuarioId);
		if(lastBoqueio != null) {

			Long processoId2 = lastBoqueio.getProcessoId();
			if(processoId2.equals(processoId) && isValid(lastBoqueio)) {
				return null;
			}

			desbloquear(lastBoqueio);
		}

		BloqueioProcesso bloqueio = bloqueioProcessoRepository.getLastBoqueioByProcesso(processoId);
		if(bloqueio == null || !isValid(bloqueio)) {

			Processo processo = processoService.get(processoId);
			StatusProcesso status = processo.getStatus();
			List<StatusProcesso> statusEmAndamento = StatusProcesso.getStatusEmAndamento();

			if(!statusEmAndamento.contains(status)) {
				return null;
			}

			BloqueioProcesso novo = bloqueio != null ? bloqueio : new BloqueioProcesso();
			novo.setData(new Date());
			novo.setProcessoId(processoId);
			novo.setUsuarioId(usuarioId);

			bloqueioProcessoRepository.saveOrUpdate(novo);

			return null;
		}
		else {

			Long usuarioId2 = bloqueio.getUsuarioId();
			Usuario usuario2 = usuarioService.get(usuarioId2);
			String usuario2Nome = usuario2.getNome();
			return new BloqueioErrorVO("processoBloqueado.error", usuario2Nome);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	private void desbloquear(BloqueioProcesso bloqueio) {
		Long bloqueioId = bloqueio.getId();
		bloqueioProcessoRepository.deleteById(bloqueioId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void desbloquearByUsuario(Long usuarioId) {
		bloqueioProcessoRepository.deleteByUsuarioId(usuarioId);
	}

	@Transactional(rollbackFor=Exception.class)
	public int limparVencidos() {

		Date timeoutDate = getTimeoutDate();
		int deletados = bloqueioProcessoRepository.limparVencidos(timeoutDate);
		return deletados;
	}

	private boolean isValid(BloqueioProcesso bloqueio) {

		Date timeoutDate = getTimeoutDate();
		boolean valid = bloqueio.getData().after(timeoutDate);

		if(valid) {

			Long processoId = bloqueio.getProcessoId();
			BloqueioProcesso lastBloqueio = bloqueioProcessoRepository.getLastBoqueioByProcesso(processoId);
			if(!bloqueio.equals(lastBloqueio)) {
				return false;
			}
		}

		return valid;
	}

	private Date getTimeoutDate() {

		Integer timeout = 15;

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -timeout);
		Date timeoutDate = calendar.getTime();

		return timeoutDate;
	}

	public List<VerificacaoBloqueioVO> findVerificacoesBloqueios(Collection<Long> ids, boolean outher) {

		List<VerificacaoBloqueioVO> result = new ArrayList<>();
		if(ids.isEmpty()) {
			return result;
		}

		List<VerificacaoBloqueioVO> list = bloqueioProcessoRepository.findVerificacoesBloqueios(ids, outher);

		for (VerificacaoBloqueioVO vo : list) {

			BloqueioProcesso bloqueio = vo.getBloqueio();
			if(bloqueio == null || isValid(bloqueio)) {
				result.add(vo);
			}
		}

		return result;
	}
}