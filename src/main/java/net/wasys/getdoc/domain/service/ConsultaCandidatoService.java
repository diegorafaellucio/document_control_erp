package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.bean.vo.ProcessoSiaVO;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.service.webservice.aluno.GetDocAlunoService;
import net.wasys.getdoc.domain.service.webservice.sia.SiaService;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ConsultaCandidatoService {

	@Autowired private SiaService siaService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private AlunoService alunoService;
	@Autowired private ProcessoService processoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private GetDocAlunoService getDocAlunoService;
	@Autowired private EmailSmtpService emailSmtpService;

	private Set<TipoCampo> preencheCampos(Map<String, String> campoValor, Map<TipoCampoGrupo, List<TipoCampo>> mapCampos) {
		Set<TipoCampo> list = new LinkedHashSet<>();
		for (TipoCampoGrupo grupo : mapCampos.keySet()) {

			String nome = grupo.getNome();
			if(CampoMap.GrupoEnum.DADOS_DE_IMPORTACAO.getNome().equals(nome)) continue;

			Set<TipoCampo> campos = grupo.getCampos();
			for (TipoCampo campo : campos) {
				list.add(campo);
			}
		}

		Set<String> camposNome = campoValor.keySet();

		for (TipoCampo tipoCampo : list) {
			String nomeCampo = tipoCampo.getNome();
			nomeCampo = DummyUtils.substituirCaracteresEspeciais(nomeCampo);
			for (String nome : camposNome) {
				String nomeMap = DummyUtils.substituirCaracteresEspeciais(nome);
				if (nomeMap.equals(nomeCampo)) {
					tipoCampo.setValor(campoValor.get(nome));
				}
			}
		}

		return list;
	}

	private Processo criaProcessoSia(Usuario usuario, ConsultaInscricoesVO consultaVO) throws Exception {

		//FIXME mudar essa busca que está por cpf/nome para cpf/email << TEM QUE RELACIONAR EMAIL DO PROCESSO COM ALUNO
		Aluno aluno = alunoService.saveOrUpdateAluno(consultaVO, usuario);

		Map<String, String> campoValor = getCampoValorMap(consultaVO);

		Long tipoProcessoId = defineTipoProcessoId(consultaVO);

		TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);

		boolean tipoProcessoAtivo = tipoProcesso.getAtivo();
		if(!tipoProcessoAtivo) {
			String tipoProcessoNome = tipoProcesso.getNome();
			throw new MessageKeyException("consultaCandidato.tipoProcessoInativo.error", tipoProcessoNome);
		}

		Map<TipoCampoGrupo, List<TipoCampo>> mapCampos = tipoCampoService.findMapByTipoProcessoCriacaoProcesso(tipoProcessoId);

		Set<TipoCampo> list = preencheCampos(campoValor, mapCampos);

		Set<CampoAbstract> valoresCampos = new LinkedHashSet<>();
		valoresCampos.addAll(list);

		CriacaoProcessoVO vo = new CriacaoProcessoVO();
		vo.setTipoProcesso(tipoProcesso);
		vo.setValoresCampos(valoresCampos);
		vo.setAluno(aluno);
		vo.setUsuario(usuario);

		return processoService.criaProcesso(vo);
	}

	private Long defineTipoProcessoId(ConsultaInscricoesVO consultaVO) {

		Long numFormaIngresso = consultaVO.getCodFormaIngresso();
		String tipoCurso = consultaVO.getNomTipoCurso();
		List<String> tiposCursosMestradoDoutorado = TipoProcessoMestradoDoutorado.getAllTipoCurso();
		Long tipoProcessoPortal = TipoProcessoPortal.getByClassificacao(numFormaIngresso);

		Long codigoCursoMedicina = 51L;
		Long numCurso = consultaVO.getCodCurso();
		if(codigoCursoMedicina.equals(numCurso)) {
			if(TipoProcesso.VESTIBULAR.equals(tipoProcessoPortal)) {
				tipoProcessoPortal = TipoProcesso.MEDICINA_VESTIBULAR;
			}
			else if(TipoProcesso.ENEM.equals(tipoProcessoPortal)) {
				tipoProcessoPortal = TipoProcesso.MEDICINA_ENEM;
			}
			else if(TipoProcesso.TRANSFERENCIA_EXTERNA.equals(tipoProcessoPortal)) {
				tipoProcessoPortal = TipoProcesso.MEDICINA_TE;
			}else if(TipoProcesso.MSV_EXTERNA.equals(tipoProcessoPortal)) {
				tipoProcessoPortal = TipoProcesso.MEDICINA_MSV_EXTERNA;
			}
		}
		else if(tiposCursosMestradoDoutorado.contains(tipoCurso)){
			tipoProcessoPortal = TipoProcessoMestradoDoutorado.getByClassificacaoAndTipoCurso(numFormaIngresso, tipoCurso);
		}

		return tipoProcessoPortal;
	}

	public Processo atualizaProcesso(Processo processo, Usuario usuario, ConsultaInscricoesVO consultaVO) throws Exception {

		//Aluno alunoOld = processo.getAluno();
		Aluno aluno = alunoService.saveOrUpdateAluno(consultaVO, usuario);

		/*if(alunoOld != null && (!alunoOld.equals(aluno))) {
			String alunoOldNome = alunoOld.getNome();
			String alunoNome = aluno.getNome();
			String alunoOldCpf = alunoOld.getCpf();
			String alunoCpf = aluno.getCpf();
			if((!alunoOldNome.equals(alunoNome)) && (!alunoOldCpf.equals(alunoCpf))) {
				emailSmtpService.enviarNotificacaoAlteracaoDadosAluno(alunoOld, aluno, processo);
			}
		}*/
		processo.setAluno(aluno);

		Map<String, String> campoValor = getCampoValorMap(consultaVO);

		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		Map<TipoCampoGrupo, List<TipoCampo>> mapCampos = tipoCampoService.findMapByTipoProcessoCriacaoProcesso(tipoProcessoId);

		Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
		for (CampoGrupo grupo : gruposCampos) {
			Set<Campo> campos = grupo.getCampos();
			for (Campo campo : campos) {
				CampoGrupo tipoGrupo = campo.getGrupo();
				Long tipoCampoGrupoId = tipoGrupo.getTipoCampoGrupoId();
				Long tipoCampoId = campo.getTipoCampoId();
				if(tipoCampoGrupoId == null) continue;
				TipoCampoGrupo tipoCampoGrupo = new TipoCampoGrupo(tipoCampoGrupoId);
				List<TipoCampo> tipoCampos = mapCampos.get(tipoCampoGrupo);
				if(tipoCampos == null) continue;
				for (TipoCampo tipoCampo : tipoCampos) {
					Long tipoCampoId1 = tipoCampo.getId();
					if(tipoCampoId1.equals(tipoCampoId)) {
						String valor = campo.getValor();
						tipoCampo.setValor(valor);
					}
				}
			}
		}
		Set<TipoCampo> list = preencheCampos(campoValor, mapCampos);

		Set<CampoAbstract> valoresCampos = new LinkedHashSet<>();
		valoresCampos.addAll(list);

		List<CampoAbstract> arrayList = new ArrayList<>(valoresCampos);

		Usuario autor = processo.getAutor();
		if(autor == null) {
			processo.setAutor(usuario);
		}

		Processo processoIsencaoDisciplinas = processoService.getProcessoIsencaoDisciplinas(processo);
		if(processoIsencaoDisciplinas != null) {
			processoService.atualizarProcesso(processoIsencaoDisciplinas, usuario, arrayList);
		}

		return processoService.atualizarProcesso(processo, usuario, arrayList, false, false);
	}

	public Map<String, String> getCampoValorMap(ConsultaInscricoesVO consultaVO) {
		String regional = consultaVO.getCodRegional() != null ? consultaVO.getCodRegional().toString() : null;
		String numInstituicao = consultaVO.getCodInstituicao() != null ? consultaVO.getCodInstituicao().toString() : null;
		String numCurso = consultaVO.getCodCurso() != null ? consultaVO.getCodCurso().toString() : null;
		String numCampus = consultaVO.getCodCampus() != null ? consultaVO.getCodCampus().toString() : null;
		String numFormaIngresso = consultaVO.getCodFormaIngresso() != null ? consultaVO.getCodFormaIngresso().toString() : null;
		String numInscricao = consultaVO.getNumInscricao();
		String cpf = DummyUtils.getCpf(consultaVO.getCpf());
		String numCandidato = consultaVO.getNumCandidato();
		String numMatricula = consultaVO.getCodMatricula();
		String nomTurno = consultaVO.getNomTurno();
		String telefone = consultaVO.getDddTelefone() != null ? consultaVO.getDddTelefone()+consultaVO.getNumTelefone() : null;
		String celular = consultaVO.getDddCelular() != null ?consultaVO.getDddCelular()+consultaVO.getNumCelular() : null;
		String nomTipoCurso = consultaVO.getNomTipoCurso();
		String area = DummyUtils.limparCharsChaveUnicidade(siaService.getRelacional(BaseInterna.AREA_ID, numCurso, TipoCampo.COD_CURSO, TipoCampo.NOM_AREA));
		area = area.equals("null") ? "" : area;
		String codIesOrigem = consultaVO.getCodIesOrigem() != null ? "[\""+consultaVO.getCodIesOrigem()+"\"]" : null;
		String situacaoAluno = StringUtils.upperCase(consultaVO.getNomSituacaoAluno());
		Date dataInscricao = consultaVO.getDataInscricao();
		String numTelefone = null;
		if(telefone != null) numTelefone = DummyUtils.getTelefone(telefone);
		String numCelular = null;
		if(celular != null) numCelular = DummyUtils.getTelefone(celular);
		Date dataVinculoSia = consultaVO.getDataVinculoSia();

		Map<String, String> campoValor = new HashMap<>();
		campoValor.put(CampoMap.CampoEnum.REGIONAL.getNome(), "[\"" + regional + "\"]");
		campoValor.put(CampoMap.CampoEnum.INSTITUICAO.getNome(),  "[\"" + numInstituicao + "\"]");
		campoValor.put(CampoMap.CampoEnum.CURSO.getNome(), "[\"" + numCurso + "\"]");
		campoValor.put(CampoMap.CampoEnum.CAMPUS.getNome(), "[\"" + numCampus + "\"]");
		campoValor.put(CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome(), "[\""+ numFormaIngresso +"\"]");
		campoValor.put(CampoMap.CampoEnum.EMAIL.getNome(), consultaVO.getEmail());
		campoValor.put(CampoMap.CampoEnum.PASSAPORTE.getNome(), consultaVO.getTxtIdPassaporte());
		campoValor.put(CampoMap.CampoEnum.NOME_COMPLETO.getNome(), consultaVO.getNomCandidato());
		campoValor.put(CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome(), formatarPeriodo(consultaVO.getPeriodoIngresso()));
		campoValor.put(CampoMap.CampoEnum.CURRICULO.getNome(), consultaVO.getCodCurriculo());
		campoValor.put(CampoMap.CampoEnum.CANDIDATO_CPF.getNome(), cpf);
		campoValor.put(CampoMap.CampoEnum.NUM_CANDIDATO.getNome(), numCandidato);
		campoValor.put(CampoMap.CampoEnum.NUM_INSCRICAO.getNome(), numInscricao);
		campoValor.put(CampoMap.CampoEnum.MATRICULA.getNome(), numMatricula);
		campoValor.put(CampoMap.CampoEnum.TURNO.getNome(), nomTurno);
		campoValor.put(CampoMap.CampoEnum.TIPO_CURSO.getNome(), nomTipoCurso);
		campoValor.put(CampoMap.CampoEnum.AREA.getNome(), area);
		campoValor.put(CampoMap.CampoEnum.IES_ORIGEM.getNome(), codIesOrigem);
		campoValor.put(CampoMap.CampoEnum.TELEFONE.getNome(), numTelefone);
		campoValor.put(CampoMap.CampoEnum.CELULAR.getNome(), numCelular);
		campoValor.put(CampoMap.CampoEnum.SITUACAO_ALUNO.getNome(), situacaoAluno);
		campoValor.put(CampoMap.CampoEnum.DATA_CADASTRO.getNome(), DummyUtils.formatDate(dataInscricao));
		campoValor.put(CampoMap.CampoEnum.DATA_VINCULO_SIA.getNome(), DummyUtils.formatDateTime(dataVinculoSia));

		return campoValor;
	}

	@Transactional(rollbackFor=Exception.class)
	public Processo criaOuAtualizaProcessoSia(Usuario usuario, AlunoFiltro filtro) throws Exception {
		Processo processo;
		AlunoFiltro filtroSia = new AlunoFiltro();
		String numCandidato = filtro.getNumCandidato();
		String numInscricao = filtro.getNumInscricao();
		String cpf = filtro.getCpf();

		if(StringUtils.isNotBlank(numCandidato)){
			filtroSia.setNumCandidato(numCandidato);
		}
		else if(StringUtils.isNotBlank(numInscricao)){
			filtroSia.setNumInscricao(numInscricao);
		}
		else {
			filtroSia.setCpf(cpf);
		}

		ConsultaInscricoesVO consultaVO = siaService.consultaInscricao(filtroSia);

		if (consultaVO == null) {
			filtroSia = new AlunoFiltro();
			if(StringUtils.isNotBlank(numInscricao)){
				filtroSia.setNumInscricao(numInscricao);
			}
			else {
				filtroSia.setCpf(cpf);
			}
			consultaVO = siaService.consultaInscricao(filtroSia);

			if (consultaVO == null) {
				consultaVO = getConsultaInscricoesVO(numInscricao, numCandidato);
			}
		}

		if (consultaVO != null) {
			numInscricao = consultaVO.getNumInscricao();
			numCandidato = consultaVO.getNumCandidato();

			processo = getProcesso(numInscricao, numCandidato);

			if (processo == null) {
				numCandidato = consultaVO.getNumCandidato();
				numInscricao = consultaVO.getNumInscricao();
				processo = getProcesso(numInscricao, numCandidato);
			}

			if (processo != null) {
				atualizaProcesso(processo, usuario, consultaVO);
			} else {
				String matricula = consultaVO.getCodMatricula();
				if(StringUtils.isNotBlank(matricula)) {
					ConsultaExterna ce = getDocAlunoService.consultarMatriculaGetDocAluno(matricula);
					StatusConsultaExterna statusCe = ce != null ? ce.getStatus() : null;
					if(StatusConsultaExterna.SUCESSO.equals(statusCe)) {
						String resultadoJson = ce.getResultado();
						Map<String, String> resultadoMap = (Map<String, String>) DummyUtils.jsonStringToMap(resultadoJson);
						String processoIdStr = resultadoMap.get("processoId");
						if(!processoIdStr.equals("0")) {
							String tipoProcessoNome = resultadoMap.get("tipoProcessoNome");
							throw new MessageKeyException("consultaMatriculaAluno.error", processoIdStr, tipoProcessoNome);
						}
					}
					else {
						String mensagem = ce != null ? ce.getMensagem() : null;
						if(mensagem != null) {
							throw new RuntimeException("Erro inesperado: " + mensagem);
						}
					}
				}
				processo = criaProcessoSia(usuario, consultaVO);
			}
		}
		else {
			processo = getProcesso(numInscricao, numCandidato);
		}

		return processo;
	}

	private Processo getProcesso(String numInscricao, String numCandidato) {

		ProcessoFiltro processoFiltro = new ProcessoFiltro();
		processoFiltro.setDesconsiderarTipoProcessoIds(Arrays.asList(TipoProcesso.ISENCAO_DISCIPLINAS, TipoProcesso.SIS_PROUNI, TipoProcesso.SIS_FIES));
		Processo processo = null;

		if(StringUtils.isNotBlank(numInscricao)) {
			processoFiltro.setNumCandidatoInscricao(numInscricao);
			List<Processo> processos = processoService.findByFiltro(processoFiltro, null, null);
			processo = processos.isEmpty() ? null : processos.get(0);
		}
		if(processo == null && StringUtils.isNotBlank(numCandidato)){
			processoFiltro.setNumCandidatoInscricao(numCandidato);
			List<Processo> processos = processoService.findByFiltro(processoFiltro, null, null);
			processo = processos.isEmpty() ? null : processos.get(0);
		}

		return processo;
	}

	/**
	 * serviços do GED antigo
	 */
	public ConsultaInscricoesVO getConsultaInscricoesVO(String numInscricao, String numCandidato) {

		ConsultaInscricoesVO consultaVO;
		ConsultaLinhaTempoSiaVO vo = siaService.consultaLinhaTempo(numInscricao, numCandidato);
		ConsultaComprovanteInscricaoVO vo2 = siaService.consultaComprovanteInscricao(numInscricao, numCandidato);

		if(vo == null || vo2 == null) {
			return null;
		}

		Long numCampus = vo2.getNumCampus();
		Long numFormaIngresso = vo.getNumFormaIngresso();
		Long numInstituicao = vo.getNumInstituicao();
		Long numCurso = vo.getNumCurso();
		String numMatricula = vo.getNumMatricula();
		String relacional = siaService.getRelacional(BaseInterna.CAMPUS_ID, String.valueOf(numCampus), TipoCampo.COD_CAMPUS, TipoCampo.COD_REGIONAL);
		String cod = DummyUtils.limparCharsChaveUnicidade(relacional);
		Long codRegional = cod.contains("null") ? null : new Long(cod);
		String nomTipoCurso = vo2.getNomTipoCurso();
		String cpfCanditato = vo2.getCpfCanditato();
		String numDDDTel = vo.getNumDDDTel();
		String numTelefone = vo.getNumTelefone();
		String nomTurno = vo2.getNomTurno();
		String dataEmissao = vo.getDataEmissao();
		String email = vo.getEmail();
		String nomCadidato = vo2.getNomCadidato();
		String nomeMae = vo.getNomeMae();
		String nomePai = vo.getNomePai();
		String numCandidato1 = vo.getNumCandidato();
		String numIdentidade = vo.getNumIdentidade();
		String numInscricao1 = vo.getNumInscricao();
		String periodoIngresso = formatarPeriodo(vo2.getPeriodoIngresso());
		String orgaoEmissor = vo.getOrgaoEmissor();
		String ufRG = vo.getUfRG();
		String idPassaporte = vo.getIdPassaporte();
		Date dtEmissaoIdent = null;
		if(dataEmissao != null) {
			long date = Long.parseLong(dataEmissao);
			dtEmissaoIdent = new Date(date);
		}

		consultaVO = new ConsultaInscricoesVO();
		consultaVO.setCodCampus(numCampus);
		consultaVO.setCodFormaIngresso(numFormaIngresso);
		consultaVO.setCodInstituicao(numInstituicao);
		consultaVO.setCodCurso(numCurso);
		consultaVO.setCodMatricula(numMatricula);
		consultaVO.setCodRegional(codRegional);
		consultaVO.setNomTipoCurso(nomTipoCurso);
		consultaVO.setCpf(cpfCanditato);
		consultaVO.setDddTelefone(numDDDTel);
		consultaVO.setNumTelefone(numTelefone);
		consultaVO.setNomTurno(nomTurno);
		consultaVO.setEmail(email);
		consultaVO.setNomCandidato(nomCadidato);
		consultaVO.setNomMae(nomeMae);
		consultaVO.setNomPai(nomePai);
		consultaVO.setNumCandidato(numCandidato1);
		consultaVO.setNumIdentidade(numIdentidade);
		consultaVO.setNumInscricao(numInscricao1);
		consultaVO.setPeriodoIngresso(periodoIngresso);
		consultaVO.setSglOrgaoEmissao(orgaoEmissor);
		consultaVO.setSglUfRg(ufRG);
		consultaVO.setTxtIdPassaporte(idPassaporte);
		consultaVO.setDtEmissaoIdent(dtEmissaoIdent);

		return consultaVO;
	}

	@Transactional(rollbackFor=Exception.class)
	public List<ProcessoSiaVO> consultarInscricoesSIA(AlunoFiltro filtro) throws Exception {

		List<ProcessoSiaVO> list = new ArrayList<>();

		List<ConsultaInscricoesVO> consultaList = siaService.consultaInscricaoPorCpf(filtro);

		List<String> aux = new ArrayList<>();

		if (consultaList != null && !consultaList.isEmpty()) {

			for (ConsultaInscricoesVO consultaInscricoesVO : consultaList) {

				String numInscricao = consultaInscricoesVO.getNumInscricao();
				String numCandidato = consultaInscricoesVO.getNumCandidato();

				Processo processo = getProcesso(numInscricao, numCandidato);

				ProcessoSiaVO processoSiaVO = criaProcessoSiaVO(consultaInscricoesVO, processo);
				if(processoSiaVO != null && (!aux.contains(numInscricao) || !aux.contains(numCandidato))) {
					aux.add(numInscricao);
					aux.add(numCandidato);
					list.add(processoSiaVO);
				}
			}
		}

		return list;
	}

	private ProcessoSiaVO criaProcessoSiaVO(ConsultaInscricoesVO vo, Processo processo) {

		ProcessoSiaVO processoSiaVO = new ProcessoSiaVO();

		Long tipoProcessoId = defineTipoProcessoId(vo);
		TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);

		processoSiaVO.setProcesso(processo);
		processoSiaVO.setTipoProcesso(tipoProcesso);
		processoSiaVO.setVo(vo);

		if(processo == null){
			processoSiaVO.setStatus(StatusProcesso.RASCUNHO);
		} else {
			processoSiaVO.setStatus(processo.getStatus());
		}
		return processoSiaVO;
	}

	@Transactional(rollbackFor=Exception.class)
	public boolean salvarRevisao(Processo processoIsencao, Usuario usuario, Processo processo, List<Campo> camposSituacao, boolean temDigitalizado, boolean adicionarDocumentoRevisao) throws Exception {
		ProcessoLog processoLog = processoLogService.criaLog(processo, usuario, AcaoProcesso.REVISAO_ISENCAO_DISCIPLINAS);
		Long processoIsencaoId = processoIsencao.getId();
		processoIsencao = processoService.get(processoIsencaoId);
		processoService.atualizarProcesso(processoIsencao, usuario, camposSituacao);
		if(adicionarDocumentoRevisao && temDigitalizado){
			processoService.enviarParaAnalise(processo, usuario);
		} else if(adicionarDocumentoRevisao && !temDigitalizado){
			return false;
		} else {
			TipoProcesso tipoProcesso = processo.getTipoProcesso();
			Long tipoProcessoId = tipoProcesso.getId();
			Situacao situacao;
			if(TipoProcesso.TRANSFERENCIA_EXTERNA.equals(tipoProcessoId)){
				situacao = situacaoService.get(Situacao.ISENCAO_DISCIPLINAS_REVISAO_TE_ID);
			} else if (TipoProcesso.MEDICINA_IDS.contains(tipoProcessoId)) {
				situacao = situacaoService.get(Situacao.ISENCAO_DISCIPLINAS_REVISAO_ID);
			} else {
				situacao = situacaoService.get(Situacao.ISENCAO_DISCIPLINAS_REVISAO_ID);
			}
			processoService.concluirSituacao(processoIsencao, usuario, situacao, processoLog, null);
		}
		return true;
	}

	public boolean salvarRevisaoAntigo(EvidenciaVO evidenciaVO, Usuario usuario, Processo processo, boolean temDigitalizado, boolean adicionarDocumentoRevisao) throws Exception{
		Situacao situacao;
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		if(adicionarDocumentoRevisao && temDigitalizado){
			situacao = situacaoService.getByNome(tipoProcessoId, Situacao.EM_ANALISE);
		} else if(adicionarDocumentoRevisao && !temDigitalizado){
			return false;
		} else {
			situacao = situacaoService.getByNome(tipoProcessoId, Situacao.ANALISE_ISENCAO_REVISAO);
		}
		evidenciaVO.setSituacao(situacao);

		processoService.salvarEvidencia(evidenciaVO, processo, usuario);
		return true;
	}

	public String formatarPeriodo(String periodoOriginal) {

		if(StringUtils.isBlank(periodoOriginal)) {
			return null;
		}

		String periodo = periodoOriginal;
		periodo = periodo.replace("-", "");
		periodo = periodo.replace("/", "");
		periodo = periodo.replace(".", "");
		periodo = periodo.replaceAll("(  )+", " ");
		periodo = org.apache.commons.lang3.StringUtils.trim(periodo);

		if(periodo.matches("\\d{5}[^\\d].*") || periodo.matches("\\d{5}") && periodo.length() >= 5) {

			String periodo2 = periodo.substring(0, 4) + "." + periodo.substring(4, 5);

			if(periodo.contains("EAD")) {
				periodo2 += " EAD";
			}
			else if(periodo.matches(".*[^\\w]F")) {
				periodo2 += " F";
			}
			else if(periodo.matches(".*[^\\w]A")) {
				periodo2 += " A";
			}
			else if(periodo.matches(".*[^\\w]D.*")) {
				periodo2 += " D/T";
			}
			else if(periodo.contains("FLEX")) {
				periodo2 += " FLEX";
			}

			return periodo2;
		}
		else {

			if(periodo.matches("\\d{6}")) {
				String periodo2 = periodo.substring(0, 4) + "." + periodo.substring(4, 5);
				return periodo2;
			}

			return periodoOriginal;
		}
	}
}
