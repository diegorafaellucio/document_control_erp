package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.BacalhauImagemPerdidaRepository;
import net.wasys.getdoc.domain.vo.filtro.BacalhauFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BacalhauImagemPerdidaService {

	@Autowired private BacalhauImagemPerdidaRepository bacalhauImagemPerdidaRepository;

	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdate(BacalhauImagemPerdida bacalhauImagemPerdida) throws MessageKeyException {
		try {
			bacalhauImagemPerdidaRepository.saveOrUpdate(bacalhauImagemPerdida);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void deletarRegistroById(Long bacalhauImagemFerradaId) {
		bacalhauImagemPerdidaRepository.deleteById(bacalhauImagemFerradaId);
	}

	public List<Long> findByImagemIdList(List<Long> imagemIds) {
		return bacalhauImagemPerdidaRepository.findByImagemIdList(imagemIds);
	}

	public int countByFiltro(BacalhauFiltro filtro) {
		return bacalhauImagemPerdidaRepository.countByFiltro(filtro);
	}

	public List<BacalhauImagemPerdida> findByFiltro(BacalhauFiltro filtro, Integer first, Integer pageSize) {
		return bacalhauImagemPerdidaRepository.findByFiltro(filtro, first, pageSize);
	}

	public void exportar(PrintWriter writer, BacalhauFiltro filtro) {

		renderHeader(writer);

		filtro.setDataInicio(null);
		filtro.setDataFim(null);
		List<BacalhauImagemPerdida> imagensPerdidasList = findByFiltro(filtro, null, null);

		renderBody(writer, imagensPerdidasList);
	}

	private void renderBody(PrintWriter writer, List<BacalhauImagemPerdida> imagensPerdidasList) {

		for (BacalhauImagemPerdida imagemPerdida : imagensPerdidasList) {

			Imagem imagem = imagemPerdida.getImagem();
			Documento documento = imagem.getDocumento();

			Processo processo = documento.getProcesso();

			Long processoId = processo.getId();
			writer.append(processoId.toString()).append(";");

			String matricula = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.MATRICULA);
			writer.append(matricula).append(";");

			Situacao situacao = processo.getSituacao();
			String situacaoNome = situacao.getNome();
			writer.append(situacaoNome).append(";");

			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			String tipoProcessoNome = tipoProcesso.getNome();
			writer.append(tipoProcessoNome).append(";");

			String documentoNome = documento.getNome();
			writer.append(documentoNome).append(";");

			StatusDocumento documentoStatus = documento.getStatus();
			writer.append(documentoStatus.name()).append(";");

			Integer documentoVersaoAtual = documento.getVersaoAtual();
			writer.append(String.valueOf(documentoVersaoAtual)).append(";");

			Long imagemId = imagem.getId();
			writer.append(imagemId.toString()).append(";");

			Integer versao = imagem.getVersao();
			writer.append(versao.toString()).append(";");

			Origem origem = imagem.getOrigem();
			writer.append(origem != null ? origem.name() : "").append(";");

			TipoArquivoBacalhau tipoArquivo = imagemPerdida.getTipoArquivo();
			writer.append(tipoArquivo.name()).append(";");

			String caminho = imagem.getCaminho();
			writer.append(caminho).append(";");

			String caminhoCache = imagemPerdida.getCaminhoCache();
			writer.append(caminhoCache).append(";");

			Boolean recuperadaDoCache = imagemPerdida.getRecuperadaDoCache();
			writer.append(recuperadaDoCache == null ? "Pendente" : (recuperadaDoCache ? "Sim" : "Não")).append(";");

			TipoExecucaoBacalhau tipoExecucao = imagemPerdida.getTipoExecucao();
			writer.append(tipoExecucao.name()).append(";");

			Date data = imagemPerdida.getData();
			writer.append(DummyUtils.formatDateTime(data)).append(";");

			String erro = imagemPerdida.getErro();
			writer.append(erro).append(";");

			writer.append("\n");
		}
	}

	private void renderHeader(PrintWriter writer) {

		writer.append("ProcessoID").append(";");
		writer.append("Matrícula").append(";");
		writer.append("Situação").append(";");
		writer.append("Tipo Processo").append(";");
		writer.append("Documento").append(";");
		writer.append("Status").append(";");
		writer.append("Versão Atual").append(";");
		writer.append("Imagem").append(";");
		writer.append("Versão").append(";");
		writer.append("Origem").append(";");
		writer.append("Tipo Arquivo").append(";");
		writer.append("Caminho Storage").append(";");
		writer.append("Caminho Cache").append(";");
		writer.append("Recuperado do Cache").append(";");
		writer.append("Tipo Execução").append(";");
		writer.append("Data de Verificação").append(";");
		writer.append("Descrição Erro").append(";");
		writer.append("\n");
	}

	public Map<String, Integer> verificarStatusBacalhauGeral() {
		return bacalhauImagemPerdidaRepository.verificarStatusBacalhauGeral();
	}
}
