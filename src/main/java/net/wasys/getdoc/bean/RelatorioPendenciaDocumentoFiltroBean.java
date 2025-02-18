package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.BaseRegistroService;
import net.wasys.getdoc.domain.service.CampoService;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.AlunoVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ManagedBean
@ViewScoped
public class RelatorioPendenciaDocumentoFiltroBean extends AbstractBean {

	@Autowired private CampoService campoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private SituacaoService situacaoService;

	private List<RegistroValorVO> regionais = new ArrayList<>(0);
	private List<RegistroValorVO> campus = new ArrayList<>(0);
	private List<RegistroValorVO> cursos = new ArrayList<>(0);
	private List<RegistroValorVO> modalidades = new ArrayList<>(0);
	private List<RegistroValorVO> formasIngressos = new ArrayList<>(0);
	private List<AlunoVO> alunos = new ArrayList<>(0);
	private List<Situacao> situacoes = new ArrayList<>(0);
	private List<TipoProcesso> tiposProcesso = new ArrayList<>(0);
	private List<String> periodoIngresso = new ArrayList<>(0);
	private List<StatusProcesso> statusProcessos = new ArrayList<>(0);

	@Override
	protected void initBean() {

		regionais = baseRegistroService.findByBaseInterna(BaseInterna.REGIONAL_ID);

		if (regionais.isEmpty()) {
			regionais = new ArrayList<>();
		}

		modalidades = baseRegistroService.findByBaseInterna(BaseInterna.MODALIDADE_ENSINO_ID);

		if (modalidades.isEmpty()) {
			modalidades = new ArrayList<>();
		}

		if (situacoes.isEmpty()) {
			situacoes = situacaoService.findAtivas(null);
			Collections.sort(situacoes, new SuperBeanComparator<>("tipoProcesso.nome, nome"));
		}

		formasIngressos = baseRegistroService.findByBaseInterna(BaseInterna.FORMA_INGRESSO_ID);

		if (formasIngressos.isEmpty()) {
			formasIngressos = new ArrayList<>();
		}

		periodoIngresso = campoService.findValoresByCampo(CampoMap.CampoEnum.PERIODO_DE_INGRESSO);

		if (periodoIngresso.isEmpty()) {
			periodoIngresso = new ArrayList<>();
		}
		else {
			periodoIngresso.sort(Collections.reverseOrder());
			periodoIngresso.removeIf(StringUtils::isBlank);
		}

		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tiposProcesso = tipoProcessoService.findAtivosAndInitialize(permissoes);

		statusProcessos = Arrays.asList(StatusProcesso.values());
	}

	public void findCampus(List<String> regionais) {

		if (cursos != null) cursos.clear();

		campus = baseRegistroService.findByRelacionamentoChaveUnicidade(BaseInterna.CAMPUS_ID, regionais, BaseRegistro.COD_REGIONAL);
	}

	public void findCursos(List<String> campus) {
		cursos = baseRegistroService.findByRelacionamentoChaveUnicidade(BaseInterna.CURSO_ID, campus, BaseRegistro.COD_CAMPUS);
	}

	public List<AlunoVO> alunoAutoComplete(String valorPesquisado) {

		valorPesquisado = valorPesquisado.toUpperCase();

		List<AlunoVO> sugestoes = new ArrayList<>();
		for (AlunoVO a : this.alunos) {
			String nomeAluno = a.getNomeAluno();
			String cpf = a.getCpf();
			String matricula = a.getMatricula();
			if (nomeAluno.startsWith(valorPesquisado) || cpf.startsWith(valorPesquisado) || matricula.startsWith(valorPesquisado)) {
				sugestoes.add(a);
			}
		}

		return sugestoes;
	}

	public CampoService getCampoService() {
		return campoService;
	}

	public TipoProcessoService getTipoProcessoService() {
		return tipoProcessoService;
	}

	public BaseRegistroService getBaseRegistroService() {
		return baseRegistroService;
	}

	public SituacaoService getSituacaoService() {
		return situacaoService;
	}

	public List<RegistroValorVO> getRegionais() {
		return regionais;
	}

	public List<RegistroValorVO> getCampus() {
		return campus;
	}

	public List<RegistroValorVO> getCursos() {
		return cursos;
	}

	public List<RegistroValorVO> getModalidades() {
		return modalidades;
	}

	public List<RegistroValorVO> getFormasIngressos() {
		return formasIngressos;
	}

	public List<AlunoVO> getAlunos() {
		return alunos;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public List<TipoProcesso> getTiposProcesso() {
		return tiposProcesso;
	}

	public List<String> getPeriodoIngresso() {
		return periodoIngresso;
	}

	public List<StatusProcesso> getStatusProcessos() {
		return statusProcessos;
	}
}