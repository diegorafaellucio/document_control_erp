package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.DummyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static net.wasys.getdoc.domain.enumeration.StatusDocumento.APROVADO;
import static net.wasys.getdoc.domain.enumeration.StatusDocumento.PENDENTE;
import static net.wasys.util.DummyUtils.systraceThread;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class MigrarProcessoService {

	@Autowired private ProcessoService processoService;
	@Autowired private DocumentoService documentoService;
	@Autowired private CampoService campoService;
	@Autowired private ImagemMetaService imagemMetaService;
	@Autowired private ResourceService resourceService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private PendenciaService pendenciaService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ProcessoLogService processoLogService;

	@Transactional
	public void migrarTipoDeProcesso(Processo processoOrigem, TipoProcesso tipoProcessoDestino, boolean excluirProcessosAposMigracao, Usuario usuarioLogado, Map<String, String> tipoDocumentoEquivalenteParaTipoDocumento) throws Exception {

		systraceThread("Usuario=" + usuarioLogado + " migrando processo=" + processoOrigem + " para tipoProcesso=" + tipoProcessoDestino);

		CriacaoProcessoVO vo = makeCriacaoProcessoVO(processoOrigem, tipoProcessoDestino, usuarioLogado, tipoDocumentoEquivalenteParaTipoDocumento);

		Processo novoProcesso = processoService.criaProcesso(vo);
		systraceThread("Novo processo criado=" + novoProcesso);

		aprovarOuPendenciarDocumentos(novoProcesso, processoOrigem, tipoDocumentoEquivalenteParaTipoDocumento);

		migrarSituacaoProcesso(processoOrigem, novoProcesso);

		if (excluirProcessosAposMigracao) {
			processoService.excluir(processoOrigem.getId(), usuarioLogado);
		}
	}

	private CriacaoProcessoVO makeCriacaoProcessoVO(Processo processoOrigem, TipoProcesso tipoProcessoDestino, Usuario usuarioLogado, Map<String, String> tipoDocumentoEquivalenteParaTipoDocumento) {

		Aluno aluno = processoOrigem.getAluno();
		Long alunoId = aluno != null ? aluno.getId() : null;
		Long processoOrigemId = processoOrigem.getId();

		List<Campo> campos = campoService.findByProcesso(processoOrigemId);
		List<CampoAbstract> camposClonados = campos.stream().map(Campo::clone).collect(Collectors.toList());

		Map<TipoDocumento, DigitalizacaoVO> digitalizacao = montarMapDigitalizacao(processoOrigemId, usuarioLogado, tipoDocumentoEquivalenteParaTipoDocumento);

		CriacaoProcessoVO vo = new CriacaoProcessoVO();
		vo.setTipoProcesso(tipoProcessoDestino);
		vo.setValoresCampos(camposClonados);
		vo.setUsuario(usuarioLogado);
		vo.setOrigem(processoOrigem.getOrigem());
		vo.setAcao(AcaoProcesso.CRIACAO);
		vo.setDigitalizacao(digitalizacao);

		if (alunoId != null) {
			vo.setAluno(new Aluno(alunoId));
		}

		return vo;
	}

	private void migrarSituacaoProcesso(Processo processoOrigem, Processo novoProcesso) throws Exception {

		Situacao processoOrigemSituacao = processoOrigem.getSituacao();
		TipoProcesso novoProcessoTipoProcesso = novoProcesso.getTipoProcesso();

		SituacaoFiltro filtro = new SituacaoFiltro();
		filtro.setNome(processoOrigemSituacao.getNome());
		filtro.setTipoProcessoId(novoProcessoTipoProcesso.getId());

		List<Situacao> situacoesDestino = situacaoService.findByFiltro(filtro, null, null);
		if (CollectionUtils.isNotEmpty(situacoesDestino)) {

			Situacao situacaoDestino = situacoesDestino.get(0);
			systraceThread("Situação processo original=" + processoOrigemSituacao + ", nova situação destino=" + situacoesDestino);
			processoService.concluirSituacao(novoProcesso, null, situacaoDestino, null, null);

			ProcessoLog origemUltimaAlteracao = processoLogService.findLastLogByProcessoAndAcao(processoOrigem.getId(), AcaoProcesso.ALTERACAO_SITUACAO);
			String login = "";
			if (origemUltimaAlteracao != null) {

				Usuario usuario = origemUltimaAlteracao.getUsuario();
				login = usuario != null ? usuario.getLogin() : "";
			}

			processoLogService.criaLog(novoProcesso, null, AcaoProcesso.ALTERACAO_SITUACAO, "Situação alterada antes da migração de Tipo de Processo pelo usuário: \"" + login + "\"");
		}
	}

	private void aprovarOuPendenciarDocumentos(Processo novoProcesso, Processo processoOrigem, Map<String, String> tipoDocumentoEquivalenteParaTipoDocumento) {

		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(novoProcesso);
		List<Documento> documentosProcessoNovo = documentoService.findByFiltro(filtro, null, null);

		filtro = new DocumentoFiltro();
		filtro.setProcesso(processoOrigem);
		List<Documento> documentosProcessoOrigem = documentoService.findByFiltro(filtro, null, null);

		for (Documento docOrigem : documentosProcessoOrigem) {

			StatusDocumento docOrigemStatus = docOrigem.getStatus();
			if (PENDENTE.equals(docOrigemStatus) || APROVADO.equals(docOrigemStatus)) {

				TipoDocumento tipoDocumentoDocOrigem = docOrigem.getTipoDocumento();
				if (tipoDocumentoDocOrigem == null) continue;

				String tipoDocumentoEquivalenteStr = tipoDocumentoEquivalenteParaTipoDocumento.get(tipoDocumentoDocOrigem.getId() + "");

				if (isNotBlank(tipoDocumentoEquivalenteStr)) {

					Optional<Documento> novoDocEquivalenteOpt = documentosProcessoNovo.stream()
							.filter(d -> d.getTipoDocumento() != null)
							.filter(d -> d.getTipoDocumento().getId().equals(Long.valueOf(tipoDocumentoEquivalenteStr)))
							.findFirst();

					if (novoDocEquivalenteOpt.isPresent()) {

						Documento documentoProcessoNovo = novoDocEquivalenteOpt.get();

						DocumentoLog docOrigemAcaoLog = documentoLogService.findLastByDocumentoAndAcaoDocumento(docOrigem.getId(), Arrays.asList(AcaoDocumento.APROVOU, AcaoDocumento.REJEITOU));
						if (docOrigemAcaoLog != null) {

							systraceThread("Migrando status do documento=" + docOrigem + " para documento=" + novoDocEquivalenteOpt);

							AcaoDocumento acao = docOrigemAcaoLog.getAcao();
							Usuario usuario = docOrigemAcaoLog.getUsuario();

							String login = "-";
							if (usuario != null) {
								login = usuario.getLogin();
							}

							if (AcaoDocumento.REJEITOU.equals(acao)) {

								Pendencia pendencia = pendenciaService.getLast(docOrigem.getId());
								Irregularidade irregularidade = pendencia.getIrregularidade();
								String observacao = pendencia.getObservacao();

								documentoService.rejeitarSemValidacao(documentoProcessoNovo, irregularidade, observacao, null);

								documentoLogService.criaLog(documentoProcessoNovo, null, AcaoDocumento.REJEITOU, "Rejeitado antes da migração de Tipo de Processo pelo usuário: \"" + login + "\"");
							}
							else if (AcaoDocumento.APROVOU.equals(acao)) {

								documentoProcessoNovo.setStatus(APROVADO);
								documentoService.saveOrUpdate(documentoProcessoNovo);

								documentoLogService.criaLog(documentoProcessoNovo, null, AcaoDocumento.APROVOU, "Aprovado antes da migração de Tipo de Processo pelo usuário: \"" + login + "\"");
							}
						}
					}
				}
				else {
					systraceThread("Não foi encontrado documento equivalente para o documento=" + docOrigem);
				}
			}
		}
	}

	private Map<TipoDocumento, DigitalizacaoVO> montarMapDigitalizacao(Long processoId, Usuario usuarioLogado, Map<String, String> tipoDocumentoEquivalenteParaTipoDocumento) {

		String dir = resourceService.getValue(ResourceService.IMAGENS_PATH);
		List<DocumentoVO> documentosProcesso = documentoService.findVOsByProcesso(processoId, usuarioLogado, dir);

		Map<TipoDocumento, DigitalizacaoVO> digitalizacao = null;
		for (DocumentoVO doc : documentosProcesso) {
			digitalizacao = mapearDocumentoParaMapDigitalizacao(digitalizacao, doc, tipoDocumentoEquivalenteParaTipoDocumento);
		}

		return digitalizacao;
	}

	private Map<TipoDocumento, DigitalizacaoVO> mapearDocumentoParaMapDigitalizacao(Map<TipoDocumento, DigitalizacaoVO> digitalizacao, DocumentoVO doc, Map<String, String> tipoDocumentoEquivalenteParaTipoDocumento) {

		List<FileVO> filesVOs = new ArrayList<>();

		Map<Integer, List<ImagemVO>> imagensParaVersao = doc.getImagens();
		for (List<ImagemVO> imagens : imagensParaVersao.values()) {

			List<FileVO> arquivosImagens = imagens.stream().map(this::imagemVoParaFileVo).collect(Collectors.toList());
			filesVOs.addAll(arquivosImagens);
		}

		if (!filesVOs.isEmpty()) {

			if (digitalizacao == null) digitalizacao = new HashMap<>();

			Origem origem = doc.getOrigem();
			TipoDocumento tipoDocumento = doc.getTipoDocumento();

			DigitalizacaoVO digitalizacaoVO = new DigitalizacaoVO();
			digitalizacaoVO.setOrigem(origem);
			digitalizacaoVO.setArquivos(filesVOs);
			digitalizacaoVO.setPodeUsarDocumentoOutros(true);

			String tipoDocumentoIdEquivalente = "";
			if (tipoDocumento != null) {

				String tipoDocumentoIdStr = String.valueOf(tipoDocumento.getId());
				tipoDocumentoIdEquivalente = tipoDocumentoEquivalenteParaTipoDocumento.get(tipoDocumentoIdStr);

				systraceThread("tipoDocumentoOriginal=" + tipoDocumentoIdStr + ", tipoDocumentoEquivalente=" + tipoDocumentoIdEquivalente);
			}

			if (isNotBlank(tipoDocumentoIdEquivalente)) {

				TipoDocumento tipoDocumentoEquivalente = new TipoDocumento(Long.valueOf(tipoDocumentoIdEquivalente));
				digitalizacao.put(tipoDocumentoEquivalente, digitalizacaoVO);
				systraceThread("Adicionando no map digitalizacao key=" + tipoDocumentoEquivalente + ", value=" + digitalizacaoVO);
			}
			else {
				digitalizacao.put(tipoDocumento, digitalizacaoVO);
				systraceThread("Adicionando no map digitalizacao key=" + tipoDocumento + ", value=" + digitalizacaoVO);
			}
		}

		return digitalizacao;
	}

	private FileVO imagemVoParaFileVo(ImagemVO i) {

		String nome = i.getNome();
		String hash = i.getHash();
		Long imagemId = i.getId();

		ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);

		File file = new File(i.getCaminho());
		String fileSize = DummyUtils.toFileSize(file.length());

		FileVO fileVO = new FileVO();
		fileVO.setName(nome);
		fileVO.setOrigem(Origem.WEB);
		fileVO.setHash(hash);
		fileVO.setLength(fileSize);
		fileVO.setFile(file);
		fileVO.setImagemMeta(imagemMeta.clone());

		return fileVO;
	}
}
