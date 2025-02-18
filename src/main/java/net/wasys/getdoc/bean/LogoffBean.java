package net.wasys.getdoc.bean;

import javax.faces.bean.ManagedBean;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.LogAtendimentoService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.util.faces.AbstractBean;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;

@ManagedBean
public class LogoffBean extends AbstractBean {

	@Autowired
	LogAtendimentoService logAtendimentoService;
	@Autowired
	UsuarioService usuarioService;

	@Override
	protected void initBean() {
	}

	@Override
	public void logoff() {
		String confirmStr = Faces.getRequestParameter("confirm");

		Usuario usuarioLogado = getUsuarioLogado();
		Long usuarioLogadoId = usuarioLogado.getId();
		usuarioLogado = usuarioService.get(usuarioLogadoId);
		Long processoAtualId = usuarioLogado.getProcessoAtualId();
		if (!"true".equals(confirmStr) && processoAtualId != null) {
			Ajax.data("result", false);
			Ajax.data("message", getMessage("logoffComVilculo.confirm", processoAtualId.toString()));
			return;
		}
		Ajax.data("result", true);

		if (usuarioLogado.isAnalistaRole()) {
			logAtendimentoService.encerrarUltimoLog(usuarioLogado);
			logAtendimentoService.encerrarProgramacao(usuarioLogado);
		}

		super.logoff();
	}
}
