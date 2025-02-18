package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.CampoGrupoRepository;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CampoGrupoService {

	private static final Pattern PATTERN_NUMERO_ENTRE_PARENTESES = Pattern.compile("(\\d+)");

    @Autowired private CampoGrupoRepository campoGrupoRepository;
    @Autowired private LogAlteracaoService logAlteracaoService;

    public CampoGrupo get(Long id) {
        return campoGrupoRepository.get(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(CampoGrupo grupo) {
        campoGrupoRepository.saveOrUpdate(grupo);
    }

    public List<CampoGrupo> findByProcesso(Long processoId) {
        return campoGrupoRepository.findByProcesso(processoId);
    }

    public int getMaxOrdemGrupoByProcessoAndTipoCampoGrupo(Long processoId, Long tipoCampoGrupoId) {
        return campoGrupoRepository.getMaxOrdemGrupoByProcessoAndTipoCampoGrupo(processoId, tipoCampoGrupoId);
    }

    @Transactional(rollbackFor=Exception.class)
    public void excluir(Long grupoId, Usuario usuario) throws MessageKeyException {

        CampoGrupo campoGrupo = get(grupoId);

        logAlteracaoService.registrarAlteracao(campoGrupo, usuario, TipoAlteracao.EXCLUSAO);

        try {
            campoGrupoRepository.deleteById(grupoId);
        }
        catch (RuntimeException e) {
            HibernateRepository.verifyConstrantViolation(e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void removerGrupoDinamico(CampoGrupo grupo, Usuario usuario) {
        Long grupoId = grupo.getId();
        excluir(grupoId, usuario);
    }

	public CampoGrupo criaGrupo(Processo processo, TipoCampoGrupo tipoGrupo) {

		Long tipoCampoGrupoId = tipoGrupo.getId();
		String nome2 = tipoGrupo.getNome();
		Integer ordem = tipoGrupo.getOrdem();
		Boolean abertoPadrao = tipoGrupo.getAbertoPadrao();
		Boolean grupoDinamico = tipoGrupo.getGrupoDinamico();
		TipoCampoGrupo subGrupo = tipoGrupo.getSubgrupo();

		CampoGrupo grupo = new CampoGrupo();
		grupo.setNome(nome2);
		grupo.setOrdem(ordem);
		grupo.setProcesso(processo);
		grupo.setAbertoPadrao(abertoPadrao);
		grupo.setGrupoDinamico(grupoDinamico);
		grupo.setTipoCampoGrupoId(tipoCampoGrupoId);
		grupo.setTipoSubgrupo(subGrupo);

		return grupo;
	}

	public boolean existByProcesso(Long processoId, Long tipoGrupoId) {
        return campoGrupoRepository.existByProcesso(processoId, tipoGrupoId);
	}

	public List<CampoGrupo> findGruposSemCamposByProcessoId(Long processoId) {
		return campoGrupoRepository.findGruposSemCamposByProcessoId(processoId);
	}

	public int getMaiorIndiceGrupoDinamico(Map<CampoGrupo, List<Campo>> campos, Long tipoCampoGrupoId, boolean considerarGruposNaoSalvos) {

		int maiorIndice = 0;

		if (campos != null) {

			Set<CampoGrupo> grupos = campos.keySet();
			Set<CampoGrupo> gruposDoMesmoTipo = grupos.stream().filter(g -> g.getTipoCampoGrupoId().equals(tipoCampoGrupoId)).collect(Collectors.toSet());

			for (CampoGrupo campoGrupo : gruposDoMesmoTipo) {

				Long campoGrupoId = campoGrupo.getId();
				if (campoGrupoId != null || considerarGruposNaoSalvos) {

					String nome = campoGrupo.getNome();
					Matcher matcher = PATTERN_NUMERO_ENTRE_PARENTESES.matcher(nome);
					if (matcher.find()) {

						int numero = Integer.valueOf(matcher.group());
						if (numero > maiorIndice) {
							maiorIndice = numero;
						}
					}
				}
			}
		}

		return maiorIndice;
	}

	public List<CampoGrupo> findByProcessoIdAndNome(Long processoId, String nome) {
		return campoGrupoRepository.findByProcessoIdAndContainsNome(processoId, nome);
	}

	public boolean grupoRelacionadoFoiApagado(Documento documento) {
		return campoGrupoRepository.grupoRelacionadoFoiApagado(documento);
	}
}
