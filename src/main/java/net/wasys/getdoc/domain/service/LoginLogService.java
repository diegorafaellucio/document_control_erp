package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.LoginLog;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.repository.LoginLogRepository;
import net.wasys.getdoc.domain.vo.RelatorioLicenciamentoVO;
import net.wasys.getdoc.domain.vo.filtro.LoginLogFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LoginLogService {

	@Autowired private SessionFactory sessionFactory;
	@Autowired private LoginLogRepository loginLogRepository;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private LogAtendimentoService logAtendimentoService;
	@Autowired private ProcessoLogService processoLogService;

	public LoginLog get(Long id) {
		return loginLogRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(LoginLog loginLog) throws MessageKeyException {
		try {
			loginLogRepository.saveOrUpdate(loginLog);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	public LoginLog criarLog(Usuario usuario) {
		LoginLog log = new LoginLog();
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();

		try {
			BeanUtils.copyProperties(log, logAcesso);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

        log.setId(null);
		log.setDataAcesso(new Date());
		log.setUsuario(usuario);

		saveOrUpdate(log);

		return log;
	}

	public List<RelatorioLicenciamentoVO> findRelatorioLicenciamento(RelatorioLicenciamentoFiltro filtro) {
		return loginLogRepository.findRelatorioLicenciamento(filtro);
	}

	public int getAtivos(Date inicio, Date fim) {
		return loginLogRepository.getAtivos(inicio, fim);
	}

	public LoginLog getLastLoginLogByData(Usuario usuario, Date data) {
		return loginLogRepository.getLastLoginLogByData(usuario, data);
	}

	@Transactional(rollbackFor=Exception.class)
	public void registarLogoff(Long loginLogId) {
		LoginLog loginLog = get(loginLogId);
		Date dataFimAcesso = loginLog.getDataFimAcesso();
		if(dataFimAcesso == null) {
			Usuario usuario = loginLog.getUsuario();
			LogAcesso lastLogAcesso = logAcessoService.getLastLogAcesso(usuario);
			Date inicio = lastLogAcesso.getInicio();
			loginLog.setDataFimAcesso(inicio);
			saveOrUpdate(loginLog);
			logAtendimentoService.encerrarUltimoLogComData(usuario, inicio);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void verificarUltimoLoginLogMenorQueDataDoLoginLogAtual(LoginLog loginLog) {
		Usuario usuario = loginLog.getUsuario();
		Date dataAcesso = loginLog.getDataAcesso();
		LoginLog lastLoginLogByData = getLastLoginLogByData(usuario, dataAcesso);
		if(lastLoginLogByData == null){
			return;
		}
		Date dataFimAcesso = lastLoginLogByData.getDataFimAcesso();
		if(dataFimAcesso == null) {
			ProcessoLog lastProcessoLog = processoLogService.getLastProcessoLogByUsuarioAndData(usuario, dataAcesso);
			if(lastProcessoLog == null) {
				return;
			}

			Date lastData = lastProcessoLog.getData();
			lastLoginLogByData.setDataFimAcesso(lastData);
			saveOrUpdate(lastLoginLogByData);
			logAtendimentoService.encerrarUltimoLogComData(usuario, lastData);
		}
	}

	public int countByFiltro(LoginLogFiltro filtro) {
		return loginLogRepository.countByFiltro(filtro);
	}

	public List<LoginLog> findByFiltro(LoginLogFiltro filtro, Integer first, Integer pageSize) {
		return loginLogRepository.findByFiltro(filtro, first, pageSize);
	}

	public List<Long> findIdsByFiltro(LoginLogFiltro filtro) {
		return loginLogRepository.findIdsByFiltro(filtro);
	}

	public List<LoginLog> findByIds(List<Long> ids) {
		return loginLogRepository.findByIds(ids);
	}

	public File render(LoginLogFiltro filtro) {

		try {
			File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/relatorio-login-log.xlsx");

			File file = File.createTempFile("relatorio-login-log1", ".xlsx");
			DummyUtils.deleteOnExitFile(file);
			FileUtils.copyFile(fileOrigem, file);

			ExcelWriter ew = new ExcelWriter();
			ew.abrirArquivo(file);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);

			ew.selecionarPlanilha("Plan1");
			renderRows(filtro, ew);

			File fileDestino = File.createTempFile("relatorio-login-log2", ".xlsx");
			DummyUtils.deleteOnExitFile(fileDestino);

			FileOutputStream fos = new FileOutputStream(fileDestino);
			workbook.write(fos);
			workbook.close();
			DummyUtils.deleteFile(file);

			return fileDestino;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (InvalidFormatException e) {
			throw new RuntimeException(e);
		}
	}

	private void renderRows(LoginLogFiltro filtro, ExcelWriter ew) {

		final List<Long> ids = findIdsByFiltro(filtro);

		int rowNum = 1;

		do {
			List<Long> ids2 = new ArrayList<Long>();
			for (int i = 0; i < 200 && !ids.isEmpty(); i++) {
				Long id = ids.remove(0);
				ids2.add(id);
			}

			List<LoginLog> list = findByIds(ids2);

			for (int i = 0; i < list.size(); i++) {

				LoginLog ll = list.get(i);

				ew.criaLinha(rowNum++);

				renderBody(ew, ll);

				try {
					Thread.sleep(4);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(!ids.isEmpty());
	}

	private void renderBody(ExcelWriter ew, LoginLog ll) {

		Long id = ll.getId();
		ew.escrever(id);

		Usuario usuario = ll.getUsuario();
		Long usuarioId = usuario.getId();
		ew.escrever(usuarioId);

		String nome = usuario.getNome();
		ew.escrever(nome);

		Date dataInicio = ll.getDataAcesso();
		ew.escreverDate(dataInicio);
		ew.escreverTime(dataInicio);

		Date dataFim = ll.getDataFimAcesso();
		ew.escreverDate(dataFim);
		ew.escreverTime(dataFim);

		String tempo = DummyUtils.calcularTempoEntreDatas(dataInicio, dataFim);
		ew.escrever(tempo);
	}

}
