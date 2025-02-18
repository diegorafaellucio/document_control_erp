package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.bean.RelatorioAcompanhamentoBean.PadraoLinha;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.RelatoriosService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.RelatorioAcompanhamentoVO;
import net.wasys.getdoc.domain.vo.filtro.LogOcrFiltro;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@ManagedBean
@ViewScoped
public class RelatorioOcrBean extends AbstractBean {

	@Autowired private RelatoriosService relatoriosService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private LogOcrFiltro filtro = new LogOcrFiltro();
	private List<RelatorioAcompanhamentoVO> list;
	private List<TipoProcesso> tiposProcessos;

	protected void initBean(){

		buscar();

		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcessos = tipoProcessoService.findAtivos(permissoes);

		initData();
	}

	private void initData() {
		Calendar c = Calendar.getInstance();
		int dias = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		String mesAno = new SimpleDateFormat("/MM/yyyy").format(new Date());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");	

		try {
			filtro.setDataInicio(sdf.parse("01"+mesAno));
			filtro.setDataFim(sdf.parse(dias+mesAno));
		} catch (ParseException e) {
			e.printStackTrace();
		}		
	}

	public void buscar() {
		if (filtro.getDataInicio()==null && filtro.getDataFim()==null){
			initData();
		}
		list = relatoriosService.getRelatorioOcr(filtro);
	}

	public String getArrayGoogleChart() {
		String array = "['Status', 'Total'],";
		int size = list.size();
		int i = 0;
		for (Iterator<RelatorioAcompanhamentoVO> it = list.iterator(); it.hasNext();) {
			RelatorioAcompanhamentoVO vo = it.next();
			if ((i + 1) < size) {
				array += "['" + vo.getNomeLinha() + "'," + vo.getTotal() + "]";
				i++;
				if ((i + 1) < size) {
					array += ",";
				}
			}

		}
		return array;
	}

	public List<RelatorioAcompanhamentoVO> getList() {
		return list;
	}

	public void setList(List<RelatorioAcompanhamentoVO> list) {
		this.list = list;
	}

	public LogOcrFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(LogOcrFiltro filtro) {
		this.filtro = filtro;
	}

	public PadraoLinha[] getPadroesLinhas() {
		return PadraoLinha.values();
	}

	public List<TipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}
}
