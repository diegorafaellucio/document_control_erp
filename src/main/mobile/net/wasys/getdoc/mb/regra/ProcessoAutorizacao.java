package net.wasys.getdoc.mb.regra;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.ItemPendente;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.ProcessoService;

public class ProcessoAutorizacao {

	@Autowired private ProcessoService processoService;

	private Usuario usuario;
	private Processo processo;

	public ProcessoAutorizacao(Usuario usuario, Processo processo, ProcessoService processoService) {
		this.usuario = usuario;
		this.processo = processo;
		this.processoService = processoService;
	}

	public boolean podeEditar() {
		StatusProcesso status = processo.getStatus();
		List<StatusProcesso> statusList = Arrays.asList(new StatusProcesso[] {
				StatusProcesso.CONCLUIDO, 
				StatusProcesso.CANCELADO, 
				StatusProcesso.AGUARDANDO_ANALISE
		});
		return !statusList.contains(status);
	}

	public boolean podeExcluir() {
		Usuario autor = processo.getAutor();
		StatusProcesso status = processo.getStatus();
		if (usuario.equals(autor) && StatusProcesso.RASCUNHO.equals(status)) {
			return true;
		}
		if (usuario.isAdminRole()) {
			return true;
		}
		return false;
	}

	public boolean podeEnviar() {
		StatusProcesso status = processo.getStatus();
		if (!StatusProcesso.RASCUNHO.equals(status)) {
			return false;
		}
		if (usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isRequisitanteRole()) {
			ItemPendente itemPendente = processoService.getItemPendenteEnviarProcesso(processo, usuario);
			if (itemPendente == null) {
				return true;
			}
			return false;
		}
		return false;
	}

	public boolean podeReenviar() {
		StatusProcesso status = processo.getStatus();
		if (!StatusProcesso.PENDENTE.equals(status)) {
			return false;
		}
		if (usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isRequisitanteRole()) {
			ItemPendente itemPendente = processoService.getItemPendenteResponderPendencia(processo, usuario);
			if (itemPendente == null) {
				return true;
			}
			return false;
		}
		return false;
	}

	public boolean podeRegistrarEvidencia() {
		if (!podeEditar()) {
			return false;
		}
		if (usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		} else if (usuario.isRequisitanteRole()) {
			Usuario autor = processo.getAutor();
			if(!usuario.equals(autor)) {
				return false;
			}
			StatusProcesso status = processo.getStatus();
			if (StatusProcesso.PENDENTE.equals(status)) {
				return true;
			}
		}
		return false;
	}

	public boolean podeAdicionarDocumento() {
		if (!podeEditar()) {
			return false;
		}
		if (usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole() || usuario.isRequisitanteRole()) {
			return true;
		}
		if (usuario.isAreaRole()) {
			Usuario autor = processo.getAutor();
			if (usuario.equals(autor)) {
				return true;
			}
		}

		return false;
	}

	public boolean podeDigitalizarDocumento() {
		if (!podeEditar()) {
			return false;
		}
		if (usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {
			return true;
		}
		else if (usuario.isRequisitanteRole() || usuario.isAreaRole()) {
			Usuario autor = processo.getAutor();
			if (!usuario.equals(autor)) {
				return false;
			}
			StatusProcesso status = processo.getStatus();
			if (StatusProcesso.RASCUNHO.equals(status)) {
				return true;
			}
			else if (StatusProcesso.PENDENTE.equals(status)) {
				return true;
			}
		}
		return false;
	}
}
