package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.service.RelatorioLicenciamentoOCRService;
import net.wasys.getdoc.domain.vo.RelatorioLicenciamentoOCRVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoOCRFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

@ManagedBean
@ViewScoped
public class RelatorioLicenciamentoOCRBean extends AbstractBean{

	@Autowired private RelatorioLicenciamentoOCRService relatorioLicenciamentoOCRService;

	private RelatorioLicenciamentoOCRFiltro filtro = new RelatorioLicenciamentoOCRFiltro();
	private List<RelatorioLicenciamentoOCRVO> processos;
	private List<RelatorioLicenciamentoOCRVO> logins;
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
		processos = relatorioLicenciamentoOCRService.findProcessos(filtro);
	}

	public List<Long> getTotal(List <RelatorioLicenciamentoOCRVO> lista){
		List<Long> mesesTotal =  Arrays.asList(new Long[exibicao]);
		if (CollectionUtils.isEmpty(lista)){
			return mesesTotal;
		}
		long[] soma = new long[exibicao];
		for (RelatorioLicenciamentoOCRVO vo : lista){
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

	public RelatorioLicenciamentoOCRFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(RelatorioLicenciamentoOCRFiltro filtro) {
		this.filtro = filtro;
	}

	public List<RelatorioLicenciamentoOCRVO> getProcessos() {
		return processos;
	}

	public void setProcessos(List<RelatorioLicenciamentoOCRVO> processos) {
		this.processos = processos;
	}

	public List<RelatorioLicenciamentoOCRVO> getLogins() {
		return logins;
	}

	public void setLogins(List<RelatorioLicenciamentoOCRVO> logins) {
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