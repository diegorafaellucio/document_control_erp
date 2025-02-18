package net.wasys.getdoc.domain.service;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.util.DummyUtils;

@Service
public class ExportacaoUsuarioService {

	@Autowired private ResourceService resourceService;
	@Autowired private UsuarioService usuarioService;

	public void exportar(PrintWriter writer) {

		renderHeader(writer);

		UsuarioFiltro filtro = new UsuarioFiltro();
		//filtro.setStatus(StatusUsuario.ATIVO, StatusUsuario.BLOQUEADO);
		List<Usuario> usuarios = usuarioService.findByFiltro(filtro);

		renderBody(writer, usuarios);
	}

	private void renderHeader(PrintWriter writer) {

		writer.append("LOGIN").append(";");
		writer.append("NOME").append(";");
		writer.append("DATA DO ACESSO CONCEDIDO").append(";");
		writer.append("DATA DO ULTIMO ACESSO").append(";");
		writer.append("PERFIL GERAL BF").append(";");
		writer.append("PERFIL MANIFESTACOES").append(";");
		writer.append("PERFIL AG").append(";");
		writer.append("STATUS").append(";");
		writer.append("\n");
	}

	private void renderBody(PrintWriter writer, List<Usuario> usuarios) {

		for (Usuario usuario : usuarios) {

			String login = usuario.getLogin();
			writer.append(login).append(";");

			String nome = usuario.getNome();
			writer.append(nome).append(";");


			Date dataCadastro = usuario.getDataCadastro();
			String dataCadastroStr = DummyUtils.formatDateTime(dataCadastro);
			writer.append(dataCadastroStr).append(";");

			Date dataUltimoAcesso = usuario.getDataUltimoAcesso();
			String dataUltimoAcessoStr = dataUltimoAcesso != null ? DummyUtils.formatDateTime(dataUltimoAcesso) : "";
			writer.append(dataUltimoAcessoStr).append(";");

			RoleGD roleGD = usuario.getRoleGD();
			if (roleGD != null) {
				String roleAGStr = resourceService.getValue("RoleGD." + roleGD.name() + ".label");
				writer.append(roleAGStr);
			}
			writer.append(";");

			writer.append(usuario.getStatus().name());
			writer.append(";");

			writer.append("\n");
		}
	}

}
