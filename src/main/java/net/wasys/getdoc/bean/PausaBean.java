package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.LogAtendimento;
import net.wasys.getdoc.domain.entity.StatusLaboral;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusAtendimento;
import net.wasys.getdoc.domain.service.LogAtendimentoService;
import net.wasys.getdoc.domain.service.StatusLaboralService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ManagedBean
@SessionScoped
public class PausaBean extends AbstractBean {

	private Usuario analista;
	private boolean isPausarStatusLaboral = false;

	@Autowired private UsuarioService usuarioService;
	@Autowired private StatusLaboralService statusLaboralService;
	@Autowired private LogAtendimentoService logAtendimentoService;

	protected void initBean() {}

	public void iniciarPausaBean() {
		Usuario usuarioLogado = getUsuarioLogado();
		if(usuarioLogado != null && usuarioLogado.isAnalistaRole()){
			Date dataAlteracaoOld = usuarioLogado.getDataAlteracao();
			Long id = usuarioLogado.getId();
			this.analista = usuarioService.get(id);
			Date dataAlteracao = analista.getDataAlteracao();
			if(dataAlteracao.after(dataAlteracaoOld)){
				HttpSession session = getSession();
				session.setAttribute(USUARIO_SESSION_KEY, analista);
			}
			this.isPausarStatusLaboral = analista.getDistribuirDemandaAutomaticamente();
		}
	}

	public LogAtendimento criarPausaSistema() {
		StatusLaboral pausaSistema = statusLaboralService.getFixo(StatusAtendimento.PAUSA_SISTEMA);
		return logAtendimentoService.criaLogAtendimento(analista.getId(), pausaSistema);
	}

	public boolean estaDisponivel() {
		StatusLaboral statusLaboral = analista.getStatusLaboral();
		StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
		return StatusAtendimento.DISPONIVEL.equals(statusAtendimento);
	}

	public boolean estaEmAnalise() {
		StatusLaboral statusLaboral = analista.getStatusLaboral();
		StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
		return StatusAtendimento.EM_ANALISE.equals(statusAtendimento);
	}

	public boolean estaEmPausa() {
		if(isPausarStatusLaboral) {
			StatusLaboral statusLaboral = analista.getStatusLaboral();
			StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
			return StatusAtendimento.PAUSA.equals(statusAtendimento);
		} else {
			return false;
		}
	}

	public boolean estaEmPausaSistema() {
		StatusLaboral statusLaboral = analista.getStatusLaboral();
		StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
		return StatusAtendimento.PAUSA_SISTEMA.equals(statusAtendimento);
	}

	public List<StatusLaboral> getSituacaoAtendimentoList() {
		if(isPausarStatusLaboral) {
			return statusLaboralService.findAtivasSelecionaveis();
		}
		return new ArrayList<>();
	}

	public void entrarEmPausa(StatusLaboral statusLaboral) {
		logAtendimentoService.entrarEmPausa(statusLaboral, analista);
	}

	public String getStrSituacaoAtendimentoProgramada(){
		boolean distribuirDemandaAutomaticamente = analista.getDistribuirDemandaAutomaticamente();
		boolean programouStatusLaboral = analista.getProgramouStatusLaboral();

		if (!distribuirDemandaAutomaticamente && !programouStatusLaboral) {
			return "";
		}

		StatusLaboral statusLaboral;

		if (programouStatusLaboral) {

			statusLaboral = analista.getStatusLaboralProgramado();
			String statusLaboralNome = statusLaboral.getNome();

			return "Programou: " + statusLaboralNome;
		}

		statusLaboral = analista.getStatusLaboral();
		StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();

		if (StatusAtendimento.EM_ANALISE.equals(statusAtendimento)){
			return "Programar Próximo Status Laboral";
		}

		return "Trocar Status Laboral";
	}

	public boolean getProgramouSituacaoAtendimento(){
		return analista.getProgramouStatusLaboral();
	}

	public boolean analistaEstaEmPausa() {
		return analista != null
				&& analista.getStatusLaboral() != null
				&& StatusAtendimento.PAUSA.equals(analista.getStatusLaboral().getStatusAtendimento());
	}

	public boolean isPausarStatusLaboral() {
		return isPausarStatusLaboral;
	}

	public Usuario getAnalista() {
		return analista;
	}

	public void entrarSituacaoAtendimentoProgramada() {
		logAtendimentoService.entrarSituacaoAtendimentoProgramada(analista);
	}

	public LogAtendimento getUltimoLogAtendimento() {
		return logAtendimentoService.getUltimoLogAtendimento(analista);
	}

	public boolean getMostrarTempo() {
		StatusLaboral statusLaboral = analista.getStatusLaboral();
		StatusAtendimento statusAtendimento = statusLaboral.getStatusAtendimento();
		List<StatusAtendimento> statusQuePodeVerTempo = Arrays.asList(StatusAtendimento.PAUSA, StatusAtendimento.EM_ANALISE);
		if(statusQuePodeVerTempo.contains(statusAtendimento)){
			return true;
		} else {
			return false;
		}
	}
}
