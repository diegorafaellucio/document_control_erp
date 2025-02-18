package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.entity.FilaConfiguracao;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.FilaConfiguracaoRepository;
import net.wasys.getdoc.domain.vo.ColunaConfigVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FilaConfiguracaoService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private FilaConfiguracaoRepository filaConfiguracaoRepository;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private SubperfilService subperfilService;

	public FilaConfiguracao get(Long id) {
		return filaConfiguracaoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(FilaConfiguracao filaConfiguracao, Usuario usuario, StatusProcesso[] statusSelecionados) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(filaConfiguracao);

		String statusFila = DummyUtils.listToString(Arrays.asList(statusSelecionados));

		try {

			filaConfiguracao.setStatus(statusFila);

			filaConfiguracaoRepository.saveOrUpdate(filaConfiguracao);

			logAlteracaoService.registrarAlteracao(filaConfiguracao, usuario, tipoAlteracao);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdateAngular(FilaConfiguracao filaConfiguracao, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(filaConfiguracao);

		try {

			filaConfiguracaoRepository.saveOrUpdate(filaConfiguracao);

			logAlteracaoService.registrarAlteracao(filaConfiguracao, usuario, tipoAlteracao);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long id, Usuario usuario) throws MessageKeyException {

		FilaConfiguracao entity = get(id);
		logAlteracaoService.registrarAlteracao(entity, usuario, TipoAlteracao.EXCLUSAO);

		try {
			filaConfiguracaoRepository.deleteById(id);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	public List<String> findCamposPossiveis(){
		return tipoCampoService.findNomesGrupoAndCampo();
	}

	public String getColunasJson(List<ColunaConfigVO> colunas){

		for(ColunaConfigVO coluna : colunas){
			List<String> tipoCampoNomes = coluna.getCampos();

			if(tipoCampoNomes.size() > 0) {
				List<Long> tipoCampoIds = tipoCampoService.findIdsByNomesGrupoAndCampo(tipoCampoNomes);
				List<String> tipoCampoIdsStr = Lists.transform(tipoCampoIds, Functions.toStringFunction());

				coluna.setCampos(tipoCampoIdsStr);
			}
		}

		return DummyUtils.objectToJson(colunas);
	}

	public List<ColunaConfigVO> getColunasObj(String colunasJson){

		if(colunasJson != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				List<ColunaConfigVO> colunas = mapper.readValue(colunasJson, new TypeReference<List<ColunaConfigVO>>(){});

				for (ColunaConfigVO coluna : colunas) {
					List<String> tipoCampoIdsStr = coluna.getCampos();

					if(tipoCampoIdsStr != null && tipoCampoIdsStr.size() > 0) {
						List<Long> tipoCampoIds = new ArrayList<>();
						for (String tipoCampoIdStr : tipoCampoIdsStr) {
							long tipoCampoId = Long.valueOf(tipoCampoIdStr);
							tipoCampoIds.add(tipoCampoId);
						}

						List<String> campoNomes = tipoCampoService.findNomesGrupoAndCampoByIds(tipoCampoIds);
						coluna.setCampos(campoNomes);
					}
				}
				return colunas;
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		else {
			return new ArrayList<>();
		}
	}

	public void restaurarPadraoFilaAnalista(FilaConfiguracao fila) {

		if(fila == null) {
			return;
		}

		fila.setExibirNaoAssociados(false);
		fila.setExibirAssociadosOutros(false);
		fila.setPermissaoEditarOutros(true);
		fila.setVerificarProximaRequisicao(true);

		StatusProcesso[] statusProcessos = {StatusProcesso.AGUARDANDO_ANALISE, StatusProcesso.EM_ANALISE,
				StatusProcesso.PENDENTE, StatusProcesso.EM_ACOMPANHAMENTO};
		String statusFila = DummyUtils.listToString(Arrays.asList(statusProcessos));
		fila.setStatus(statusFila);
		fila.setColunas("[]");
	}

	public List<FilaConfiguracao> findAll() {
		return filaConfiguracaoRepository.findAll(0, Integer.MAX_VALUE);
	}

	public List<FilaConfiguracao> findAll(Integer inicio, Integer max) {
		return filaConfiguracaoRepository.findAll(inicio, max);
	}

	public int count() {
		return filaConfiguracaoRepository.count();
	}

	public boolean possuiPadrao() {
		return filaConfiguracaoRepository.possuiPadrao();
	}

	public FilaConfiguracao getPadrao() {
		return filaConfiguracaoRepository.getPadrao();
	}

	public FilaConfiguracao carregarConfiguracoesFila(Usuario usuario) {

		FilaConfiguracao filaConfiguracao;
		Subperfil subperfil = usuario.getSubperfilAtivo();
		if (subperfil != null) {
			subperfil = subperfilService.get(subperfil.getId());
			filaConfiguracao = subperfil.getFilaConfiguracao();
		}
		else {
			filaConfiguracao = getPadrao();
		}

		return filaConfiguracao;
	}
}