package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.ExecucaoGeracaoRelatorio;
import net.wasys.getdoc.domain.repository.ExecucaoGeracaoRelatorioRepository;
import net.wasys.getdoc.domain.vo.ExecucaoGeracaoRelatorioVO;
import net.wasys.getdoc.domain.vo.filtro.ExecucaoGeracaoRelatorioFiltro;
import net.wasys.util.DummyUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class ExecucaoGeracaoRelatorioService {

	@Autowired private ExecucaoGeracaoRelatorioRepository repository;
	@Autowired private GerarRelatoriosService gerarRelatoriosService;

	@Transactional
	public void saveOrUpdate(ExecucaoGeracaoRelatorio execucao) {
		repository.saveOrUpdate(execucao);
	}

	public int countByFiltro(ExecucaoGeracaoRelatorioFiltro filtro) {
		return repository.countByFiltro(filtro);
	}

	public List<ExecucaoGeracaoRelatorioVO> findVOsByFiltro(ExecucaoGeracaoRelatorioFiltro filtro, Integer first, Integer pageSize) {

		List<ExecucaoGeracaoRelatorioVO> byFiltro = repository.findVOsByFiltro(filtro, first, pageSize);

		for (ExecucaoGeracaoRelatorioVO execucaoVO : byFiltro) {

			if (execucaoVO.getFim() == null) {

				Long configuracaoGeracaoRelatorioId = execucaoVO.getConfiguracaoGeracaoRelatorioId();
				boolean estaExecutando = gerarRelatoriosService.estaExecutando(configuracaoGeracaoRelatorioId);
				execucaoVO.setExecutando(estaExecutando);
			}
		}

		return byFiltro;
	}

	public File findArquivoTemporarioByExecucaoId(Long execucaoGeracaoRelatorioId) throws IOException {

		ExecucaoGeracaoRelatorio execucaoGeracaoRelatorio = repository.get(execucaoGeracaoRelatorioId);

		String caminhoArquivo = execucaoGeracaoRelatorio.getCaminhoArquivo();
		String extensao = DummyUtils.getExtensao(caminhoArquivo);
		extensao = "." + extensao;

		File tempFile = File.createTempFile(caminhoArquivo.replace(extensao, ""), extensao);
		FileUtils.copyFile(new File(caminhoArquivo), tempFile);

		return tempFile;
	}

	public int excluirAnteriores(Date dataCorte) {

		systraceThread("Excluindo registros anterios à " + dataCorte);

		int countRegistrosExcluidos = 0;

		ExecucaoGeracaoRelatorioFiltro filtro = new ExecucaoGeracaoRelatorioFiltro();
		filtro.setDataInicioMenorQue(dataCorte);

		List<ExecucaoGeracaoRelatorio> execucoes = repository.findByFiltro(filtro, null, null);
		systraceThread("Foram encontrados " + execucoes.size() + " registros para exclusão.");

		for (ExecucaoGeracaoRelatorio execucao : execucoes) {

			String infoExecucao = "ExecucaoGeracaoRelatorioId=" + execucao.getId() + ", caminho=" + execucao.getCaminhoArquivo();

			try {

				String caminhoArquivo = execucao.getCaminhoArquivo();

				File file = new File(caminhoArquivo);
				if (file.exists()) {

					// deixei só pra passar pelo metodo padrão, mesmo com exclusão bloqueada no momento
					DummyUtils.deleteFile(file);
					boolean deletou = file.delete();
					if (!deletou) {
						systraceThread("Não foi possível deletar o arquivo. " + infoExecucao);
					}
					else {
						repository.deleteById(execucao.getId());
						systraceThread("Registro excluído com sucesso. " + infoExecucao);
						countRegistrosExcluidos++;
					}
				}
				else {
					systraceThread("Não foi encontrado o arquivo. " + infoExecucao);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return countRegistrosExcluidos;
	}
}
