package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.service.*;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.*;

@ManagedBean
@SessionScoped
public class RelatorioProcessoListagemBean extends AbstractBean {

	@Autowired private ProcessoService processoService;
	@Autowired private RelatorioGeralSolicitacaoService relatorioGeralSolicitacaoService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoProcessoService tipoProcessoService;


	public enum Tipo{
		RELATORIO_EFICIENCIA_AREA
	}

	private List<Processo> listagem;

	protected void initBean() {

		Tipo tipo = (Tipo) getFlashAttribute("tipo");
		if (Tipo.RELATORIO_EFICIENCIA_AREA.equals(tipo)) {

			Date dataInicio = (Date) getFlashAttribute("dataInicio");
			Date dataFim = (Date) getFlashAttribute("dataFim");
			Area area  = (Area) getFlashAttribute("area");
			String mesAno  = (String) getFlashAttribute("mesAno");
			String[] split = mesAno.split("[/]");
			int mes = Integer.parseInt(split[0]);
			int ano = Integer.parseInt(split[1]);
			listagem = relatorioGeralSolicitacaoService.findProcessos(area, mes, ano, dataInicio, dataFim);
		}

	}

	public void limpar() {
		initBean();
	}

	public List<Processo> getListagem() {
		return listagem;
	}

}
