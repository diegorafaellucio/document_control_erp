package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.ColunaProcessoSeletivo;
import net.wasys.getdoc.domain.enumeration.TipoProcessoSeletivo;
import net.wasys.getdoc.domain.repository.ColunaProcessoSeletivoRepository;
import net.wasys.getdoc.domain.service.factory.ProcessadorArquivoFactory;
import net.wasys.getdoc.domain.vo.LinhaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class ColunaProcessoSeletivoService {

	private static final String FIES = "fies";

	@Autowired private ColunaProcessoSeletivoRepository colunaProcessoSeletivoRepository;

	public ColunaProcessoSeletivo get(Long id) {
		return colunaProcessoSeletivoRepository.get(id);
	}

	public void atualizarDados(File arquivo, String nomeArquivo) {
		ProcessadorArquivo processador = ProcessadorArquivoFactory.getProcessador(arquivo, new ArrayList<String>());
		List<LinhaVO> linhas = processador.processar();

		for (Iterator<LinhaVO> iterator = linhas.iterator(); iterator.hasNext(); ) {
			LinhaVO linha = iterator.next();

			Map<String, String> colunaValor = linha.getColunaValor();

			Set<String> colunas = colunaValor.keySet();
			for (String coluna : colunas) {
				String valor = colunaValor.get(coluna);
				String colunaUp = coluna.toUpperCase();
				int numeroLinha = linha.getNumeroLinha();

				ColunaProcessoSeletivo colunaProcessoSeletivo = colunaProcessoSeletivoRepository.getByColunaAndNumeroLinhaAndNomeArquivoImportacao(colunaUp, numeroLinha, nomeArquivo);

				colunaProcessoSeletivo =  colunaProcessoSeletivo != null ? colunaProcessoSeletivo : new ColunaProcessoSeletivo();

				colunaProcessoSeletivo.setNome(colunaUp);
				colunaProcessoSeletivo.setValor(valor);
				colunaProcessoSeletivo.setNumeroLinha(numeroLinha);
				colunaProcessoSeletivo.setNomeArquivoImportacao(nomeArquivo);

				if (nomeArquivo.contains(FIES)) {
					colunaProcessoSeletivo.setTipoProcessoSeletivo(TipoProcessoSeletivo.PROCESSO_SELETIVO_FIES);
				} else {
					colunaProcessoSeletivo.setTipoProcessoSeletivo(TipoProcessoSeletivo.PROCESSO_SELETIVO_PROUNI);
				}

				Long colunaProcessoSeletivoId = colunaProcessoSeletivo.getId();
				if(colunaProcessoSeletivoId != null){
					colunaProcessoSeletivoRepository.merge(colunaProcessoSeletivo);
				} else {
					colunaProcessoSeletivoRepository.save(colunaProcessoSeletivo);
				}
			}
		}
	}
}
