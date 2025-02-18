package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.service.RelatorioLicenciamentoService;
import net.wasys.getdoc.domain.vo.RelatorioLicenciamentoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

@ManagedBean
@ViewScoped
public class RelatorioLicenciamentoBean extends AbstractBean{

	@Autowired private RelatorioLicenciamentoService relatorioLicenciamentoService;

	private RelatorioLicenciamentoFiltro filtro = new RelatorioLicenciamentoFiltro();
	private List<RelatorioLicenciamentoVO> processos;
	private List<RelatorioLicenciamentoVO> logins;
	private List<Integer> anos;
	private int exibicao;

	protected void initBean(){
		gerarAnosList();
		Date date = new Date();
		filtro.setAno(DateUtils.getYear(date));
		filtro.setMes(DateUtils.getMonth(date));
		exibicao = 4;
		buscar();
	}

	public void buscar() {
		gerarMesesAnteriores();
		processos = relatorioLicenciamentoService.findProcessos(filtro);
		logins = relatorioLicenciamentoService.findLogins(filtro);
	}

	public List<Long> getTotal(List <RelatorioLicenciamentoVO> lista){
		List<Long> mesesTotal =  Arrays.asList(new Long[exibicao]);
		long[] soma = new long[exibicao];
		for (RelatorioLicenciamentoVO vo : lista){
			for (int i = 0; i <= vo.getQtdPorMes().size()-1; i++){
				soma[i] += vo.getQtdPorMes().get(i);
				mesesTotal.set(i, soma[i]);
			}
		}
		return  mesesTotal;
	}

	public void gerarAnosList(){
		Date date = new Date();
		int anoAtual = DateUtils.getYear(date);
		List<Integer> anos = new ArrayList<Integer>();

		for (int i=2019; i<=anoAtual; i++){
			anos.add(i);
		}
		this.anos = anos;
	}

	public String lastDay(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.MILLISECOND, -1);
		date = cal.getTime();
		Format formatter = new SimpleDateFormat("dd/MM/yyyy");
		return formatter.format(date);
	}

	public String firstDay(Date date){
		Format formatter = new SimpleDateFormat("MM/yyyy");
		return formatter.format(date);
	}

	public void gerarMesesAnteriores() {
		Date date;

		List<Date> mesesList = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.MONTH, filtro.getMes() - exibicao);
		cal.set(Calendar.YEAR, filtro.getAno());

		for (int i=0; i <exibicao ; i++){
			date = cal.getTime();
			mesesList.add(date);
			cal.add(Calendar.MONTH, +1);
		}
		filtro.setMeses(mesesList);
	}

	public RelatorioLicenciamentoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(RelatorioLicenciamentoFiltro filtro) {
		this.filtro = filtro;
	}

	public List<RelatorioLicenciamentoVO> getProcessos() {
		return processos;
	}

	public void setProcessos(List<RelatorioLicenciamentoVO> processos) {
		this.processos = processos;
	}

	public List<RelatorioLicenciamentoVO> getLogins() {
		return logins;
	}

	public void setLogins(List<RelatorioLicenciamentoVO> logins) {
		this.logins = logins;
	}

	public List<Integer> getAnos() {
		return anos;
	}

	public int getExibicao() {
		return exibicao;
	}

	public void setExibicao(int exibicao) {
		this.exibicao = exibicao;
	}
}