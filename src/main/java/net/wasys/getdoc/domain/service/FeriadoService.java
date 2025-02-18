package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.Feriado;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.FeriadoRepository;
import net.wasys.getdoc.restws.dto.FeriadoDTO;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.Bolso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class FeriadoService {

	private final static Bolso<List<Date>> DATAS_CACHE = new Bolso<>();

	@Autowired private FeriadoRepository feriadoRepository;
	@Autowired private LogAlteracaoService logAlteracaoService;

	public Feriado get(Long id) {
		return feriadoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Feriado feriado, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(feriado);

		feriado.setDataAtualizacao(new Date());

		try {
			feriadoRepository.saveOrUpdate(feriado);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(feriado, usuario, tipoAlteracao);

		DATAS_CACHE.setObjeto(null);
	}

	public List<Date> findAllDatas() {

		List<Date> result = DATAS_CACHE.getObjeto();
		long finalTime = DATAS_CACHE.getFinalTime();

		long now = System.currentTimeMillis();
		if(result == null || finalTime < now) {

			result = feriadoRepository.findAllDatas();
			DATAS_CACHE.setObjeto(result);
			long timeout = (1000 * 60 * 10);//10 minutos
			DATAS_CACHE.setFinalTime(now + timeout);
		}

		return new ArrayList<>(result);
	}

	public List<Feriado> findAll(Boolean paralizacao, Integer inicio, Integer max) {
		return feriadoRepository.findAll(paralizacao, inicio, max);
	}

	public int count(Boolean paralizacao) {
		return feriadoRepository.count(paralizacao);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long feriadoId, Usuario usuario) throws MessageKeyException {

		Feriado feriado = get(feriadoId);

		logAlteracaoService.registrarAlteracao(feriado, usuario, TipoAlteracao.EXCLUSAO);

		try {
			feriadoRepository.deleteById(feriadoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public Date getUltimaDataAtualizacao() {
		return feriadoRepository.getUltimaDataAtualizacao();
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizar(FeriadoDTO[] feriados) throws MessageKeyException {

		systraceThread("feriados: " + (feriados != null ? feriados.length : "null"));
		if(feriados == null) {
			return;
		}
		for (FeriadoDTO dto : feriados) {
			Long geralId = dto.getId();
			Date data = dto.getData();
			String descricao = dto.getDescricao();
			Date dataAtualizacao = dto.getDataAtualizacao();
			Usuario usuario = null;

			Feriado feriado = feriadoRepository.getByGeralId(geralId);
			if (feriado == null) {
				feriado = feriadoRepository.getByData(data);
			}
			feriado = feriado != null ? feriado : new Feriado();

			feriado.setData(data);
			feriado.setDescricao(descricao);
			feriado.setGeralId(geralId);
			feriado.setDataAtualizacao(dataAtualizacao);
			feriado.setParalizacao(false);

			saveOrUpdate(feriado, usuario);
		}
	}

	public List<Date> findBySituacao(Long situacaoId){
		return feriadoRepository.findBySituacao(situacaoId);
	}
}
