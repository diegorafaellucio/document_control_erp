package net.wasys.getdoc.restws;

import net.wasys.getdoc.domain.service.RelatorioLicenciamentoService;
import net.wasys.getdoc.domain.vo.RelatorioLicenciamentoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.rest.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Controller
@RequestMapping(path="/licenciamento", produces=MediaType.APPLICATION_JSON_VALUE)
public class LicenciamentoController extends AbstractController {

	@Autowired private RelatorioLicenciamentoService relatorioLicenciamentoService;

	@Override
	public boolean ifSecurityOk() {
		return true;
	}

	@RequestMapping(value="/check")
	public ResponseEntity<?> check() {

		systraceThread("");
		try {
			RelatorioLicenciamentoFiltro filtro = new RelatorioLicenciamentoFiltro();
			Date date = new Date();
			filtro.setAno(DateUtils.getYear(date));
			filtro.setMes(DateUtils.getMonth(date));
			Date mes = new Date();
			List<Date> meses = new ArrayList<>();
			meses.add(mes);
			mes = DateUtils.addMonths(mes, -1);
			meses.add(mes);
			mes = DateUtils.addMonths(mes, -1);
			meses.add(mes);
			Collections.reverse(meses);
			filtro.setMeses(meses);

			List<RelatorioLicenciamentoVO> processos = relatorioLicenciamentoService.findProcessos(filtro);
			List<QuantidadeMesesVO> procs = new ArrayList<>();
			for (RelatorioLicenciamentoVO vo : processos) {
				QuantidadeMesesVO vo2 = criaQuantidadeMesesVO(vo);
				procs.add(vo2);
			}
			addTotal(procs);

			List<RelatorioLicenciamentoVO> logins = relatorioLicenciamentoService.findLogins(filtro);
			List<QuantidadeMesesVO> usrs = new ArrayList<>();
			for (RelatorioLicenciamentoVO vo : logins) {
				QuantidadeMesesVO vo2 = criaQuantidadeMesesVO(vo);
				usrs.add(vo2);
			}
			addTotal(usrs);

			Map<String, Object> dto = new LinkedHashMap<>();
			dto.put("procs", procs);
			dto.put("usrs", usrs);

			return new ResponseEntity<Map>(dto, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return handleException(e);
		}
	}

	private QuantidadeMesesVO criaQuantidadeMesesVO(RelatorioLicenciamentoVO vo) {
		List<Long> qtdPorMes = vo.getQtdPorMes();
		Long mesMenosDois = qtdPorMes.get(0);
		Long mesMenosUm = qtdPorMes.get(1);
		Long mesAtual = qtdPorMes.get(2);
		QuantidadeMesesVO vo2 = new QuantidadeMesesVO();
		String nome = vo.getNome();
		vo2.setNome(nome);
		vo2.setMesMenosDois(mesMenosDois);
		vo2.setMesMenosUm(mesMenosUm);
		vo2.setMesAtual(mesAtual);
		return vo2;
	}

	private void addTotal(List<QuantidadeMesesVO> list) {

		QuantidadeMesesVO total = new QuantidadeMesesVO();
		for (QuantidadeMesesVO vo : list) {
			total.setNome("Total");
			total.setMesMenosDois(total.mesMenosDois + vo.getMesMenosDois());
			total.setMesMenosUm(total.mesMenosUm + vo.getMesMenosUm());
			total.setMesAtual(total.mesAtual + vo.getMesAtual());
		}
		list.add(total);
	}

	public static class QuantidadeMesesVO {

		private String nome;
		private Long mesMenosDois = 0l;
		private Long mesMenosUm = 0l;
		private Long mesAtual = 0l;

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public Long getMesMenosDois() {
			return mesMenosDois;
		}

		public void setMesMenosDois(Long mesMenosDois) {
			this.mesMenosDois = mesMenosDois;
		}

		public Long getMesMenosUm() {
			return mesMenosUm;
		}

		public void setMesMenosUm(Long mesMenosUm) {
			this.mesMenosUm = mesMenosUm;
		}

		public Long getMesAtual() {
			return mesAtual;
		}

		public void setMesAtual(Long mesAtual) {
			this.mesAtual = mesAtual;
		}
	}
}
