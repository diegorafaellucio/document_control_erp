package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.FilaConfiguracaoDataModel;
import net.wasys.getdoc.domain.entity.FilaConfiguracao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.ExportacaoPalavrasEEService;
import net.wasys.getdoc.domain.service.FilaConfiguracaoService;
import net.wasys.getdoc.domain.vo.ColunaConfigVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@ViewScoped
public class FilaConfiguracaoCrudBean extends AbstractBean {

	@Autowired private FilaConfiguracaoService filaConfiguracaoService;

	private FilaConfiguracaoDataModel dataModel;
	private FilaConfiguracao filaConfiguracao;

	private boolean possuiPadrao;

	private StatusProcesso[] statusSelecionados;
	private List<StatusProcesso> statusPossiveis;

	private List<String> camposPossiveis;
	private List<ColunaConfigVO> colunas;

	public void initBean() {
		statusPossiveis = StatusProcesso.getStatusEmAndamento();
		statusPossiveis = new ArrayList<>(statusPossiveis);
		statusPossiveis.add(0, StatusProcesso.RASCUNHO);

		camposPossiveis =  filaConfiguracaoService.findCamposPossiveis();

		dataModel = new FilaConfiguracaoDataModel();
		dataModel.setService(filaConfiguracaoService);
	}

	public void salvar() {

		try {
			boolean insert = isInsert(filaConfiguracao);
			Usuario usuario = getUsuarioLogado();

			List<ColunaConfigVO> cloneColunas = cloneColunas();
			String colunasJson = filaConfiguracaoService.getColunasJson(colunas);
			filaConfiguracao.setColunas(colunasJson);

			if(filaConfiguracao.isPadrao()){
				filaConfiguracao.setVerificarProximaRequisicao(true);
				filaConfiguracao.setExibirNaoAssociados(true);
				filaConfiguracao.setExibirAssociadosOutros(true);
				filaConfiguracao.setPermissaoEditarOutros(true);
				filaConfiguracao.setStatus("");
			}

			filaConfiguracaoService.saveOrUpdate(filaConfiguracao, usuario, statusSelecionados);

			colunas = cloneColunas;
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();

		Long filaConfiguracaoId = filaConfiguracao.getId();

		try {
			filaConfiguracaoService.excluir(filaConfiguracaoId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}
	}

	public void adicionarColuna(){
		ColunaConfigVO coluna = new ColunaConfigVO();
		this.colunas.add(coluna);
	}

	public void removerColuna(ColunaConfigVO coluna) {
		this.colunas.remove(coluna);
	}

	public List<ColunaConfigVO> cloneColunas () {

		List<ColunaConfigVO> clone = new ArrayList<>();

		for(ColunaConfigVO vo : this.colunas){
			String nomeColuna = vo.getNome();
			List<String> camposColuna =  vo.getCampos();

			ColunaConfigVO voClone = new ColunaConfigVO();
			voClone.setNome(nomeColuna);
			voClone.setCampos(camposColuna);

			clone.add(voClone);
		}

		return clone;
	}

	public void restaurarPadraoFilaAnalista() {
		try {
			filaConfiguracaoService.restaurarPadraoFilaAnalista(filaConfiguracao);
			String statusFila = filaConfiguracao.getStatus();
			List<StatusProcesso> statusSelecionadosList = DummyUtils.stringToList(statusFila, StatusProcesso.class);
			if(statusSelecionadosList != null) {
				statusSelecionados = new StatusProcesso[statusSelecionadosList.size()];
				statusSelecionados = statusSelecionadosList.toArray(statusSelecionados);
			}

			String colunasJson = filaConfiguracao.getColunas();
			colunas = filaConfiguracaoService.getColunasObj(colunasJson);

		} catch (Exception e) {
			addMessageError(e);
		}
	}

	public FilaConfiguracaoDataModel getDataModel() {
		return dataModel;
	}

	public FilaConfiguracao getFilaConfiguracao() {
		return filaConfiguracao;
	}

	public void setFilaConfiguracao(FilaConfiguracao filaConfiguracao) {
		possuiPadrao = filaConfiguracaoService.possuiPadrao();

		if(filaConfiguracao == null) {
			this.filaConfiguracao = new FilaConfiguracao();
			restaurarPadraoFilaAnalista();
		}
		else {
			String statusFila = filaConfiguracao.getStatus();
			List<StatusProcesso> statusSelecionadosList = DummyUtils.stringToList(statusFila, StatusProcesso.class);
			if(statusSelecionadosList != null) {
				statusSelecionados = new StatusProcesso[statusSelecionadosList.size()];
				statusSelecionados = statusSelecionadosList.toArray(statusSelecionados);
			}

			String colunasJson = filaConfiguracao.getColunas();
			colunas = filaConfiguracaoService.getColunasObj(colunasJson);
			this.filaConfiguracao = filaConfiguracao;
		}
	}

	public boolean getPossuiPadrao() {
		return possuiPadrao;
	}

	public void setPossuiPadrao(boolean possuiPadrao) {
		this.possuiPadrao = possuiPadrao;
	}

	public StatusProcesso[] getStatusSelecionados() {
		return statusSelecionados;
	}

	public void setStatusSelecionados(StatusProcesso[] statusSelecionados) {
		this.statusSelecionados = statusSelecionados;
	}

	public List<StatusProcesso> getStatusPossiveis() {
		return statusPossiveis;
	}

	public List<ColunaConfigVO> getColunas() {
		return colunas;
	}

	public void setColunas(List<ColunaConfigVO> colunas) {
		this.colunas = colunas;
	}

	public List<String> getCamposPossiveis() {
		return camposPossiveis;
	}
}
