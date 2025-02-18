package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.vo.ModeloDocumentoReaproveitamentoVO;
import net.wasys.getdoc.domain.vo.ReaproveitamentoDocumentoVO;
import net.wasys.getdoc.domain.vo.ReaproveitamentoImagemVO;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.rest.exception.DocumentoRestException;
import net.wasys.util.DummyUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class ReaproveitarDocumentoService {

	@Autowired private ResourceService resourceService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ImagemService imagemService;
	@Autowired private DocumentoLogService documentoLogService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private ImagemMetaService imagemMetaService;
	@Autowired private PendenciaService pendenciaService;
	@Autowired private ModeloDocumentoService modeloDocumentoService;

	public List<ReaproveitamentoDocumentoVO> buscarDocumentosReaproveitaveis(Processo processo, boolean ignorarArquivos) throws IOException {

		List<ReaproveitamentoDocumentoVO> vos = new ArrayList<>();

		List<Documento> documentos = filtrarDocumentosParaReaproveitar(processo);

		for (Documento documento : documentos) {
			ReaproveitamentoDocumentoVO vo = makeReaproveitamentoDocumentoVO(documento, ignorarArquivos);
			vos.add(vo);
		}

		return vos;
	}

	private ReaproveitamentoDocumentoVO makeReaproveitamentoDocumentoVO(Documento documento, boolean ignorarArquivos) throws IOException {

		List<ModeloDocumento> modelosDocumento = getModelosDocumento(documento);
		List<Imagem> imagens = getImagens(documento);

		List<ReaproveitamentoImagemVO> imagensVOs = makeReaproveitamentoImagemVOs(imagens, modelosDocumento, ignorarArquivos);

		return makeReaproveitamentoDocumentoVO(documento, modelosDocumento, imagensVOs);
	}

	private ReaproveitamentoDocumentoVO makeReaproveitamentoDocumentoVO(Documento documento, List<ModeloDocumento> modelosDocumento, List<ReaproveitamentoImagemVO> imagensVOs) {

		Long documentoId = documento.getId();
		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		Long tipoDocumentoId = tipoDocumento != null ? tipoDocumento.getId() : null;
		Long codOrigem = tipoDocumento != null ? tipoDocumento.getCodOrigem() : null;
		Processo processo = documento.getProcesso();
		Long processoId = processo.getId();
		StatusDocumento status = documento.getStatus();
		String posfixo = getPosfixoDocumento(documento);
		boolean obrigatorio = documento.getObrigatorio();
		String nome = documento.getNome();
		boolean termoPastaVermelha = tipoDocumento != null && tipoDocumento.getTermoPastaVermelha();
		Integer versaoAtual = documento.getVersaoAtual();

		ReaproveitamentoDocumentoVO vo = new ReaproveitamentoDocumentoVO();

		preencherLabelsDarknet(documento, modelosDocumento, vo);

		String metadados = documento.getMetaDados();
		metadados = metadados == null ? "{}" : metadados;
		JSONObject tempJSON = new JSONObject(metadados);
		tempJSON.put("termoPastaVermelha", termoPastaVermelha);
		tempJSON.put("processoId", processoId);
		tempJSON.put("documentoIdCaptacao", documentoId);
		tempJSON.put("tipoDocumentoIdCaptacao", tipoDocumentoId);
		tempJSON.put("labelsDarknet", new JSONArray(vo.getLabelsDarknet()));
		String metadadosJson = tempJSON.toString();

		vo.setImagens(imagensVOs);
		vo.setValidade(null);
		vo.setCodOrigem(String.valueOf(codOrigem));
		vo.setMetadados(metadadosJson);
		vo.setStatusDocumento(status);
		vo.setPosfixo(posfixo);
		vo.setDocumentoCaptacaoId(documentoId);
		vo.setObrigatorio(obrigatorio);
		vo.setNome(nome);
		vo.setVersaoAtual(versaoAtual);

		if(StatusDocumento.PENDENTE.equals(status)) {
			Pendencia ultimaPendencia = pendenciaService.getLast(documentoId);
			if(ultimaPendencia != null) {
				Irregularidade irregularidade = ultimaPendencia.getIrregularidade();
				String irregularidadeNome = irregularidade.getNome();
				String observacao = ultimaPendencia.getObservacao();
				vo.setMotivoPendencia(irregularidadeNome);
				vo.setObservacaoPendencia(observacao);
			}
		}

		Advertencia advertencia = documento.getAdvertencia();
		if(advertencia != null) {
			Irregularidade irregularidade = advertencia.getIrregularidade();
			String motivoAdvertencia = irregularidade.getNome();
			String observacaoAdvertencia = advertencia.getObservacao();
			vo.setMotivoAdvertencia(motivoAdvertencia);
			vo.setObservacaoAdvertencia(observacaoAdvertencia);
		}

		preencherUsuarioAcao(vo, documento);

		return vo;
	}

	private void preencherLabelsDarknet(Documento documento, List<ModeloDocumento> modelosDocumento, ReaproveitamentoDocumentoVO vo) {

		if (isNotEmpty(modelosDocumento)) {

			List<String> labelsDarknet = isEmpty(vo.getLabelsDarknet()) ? new ArrayList<>() : vo.getLabelsDarknet();

			for (ModeloDocumento modeloDocumento : modelosDocumento) {

				String labelDarknet = modeloDocumento.getLabelDarknet();
				if (isNotBlank(labelDarknet)) {
					labelsDarknet.add(labelDarknet);
				}
				vo.setLabelsDarknet(labelsDarknet);

				preencherValidade(documento, vo, labelDarknet);
			}
		}
	}

	private List<Imagem> getImagens(Documento documento) {

		StatusDocumento status = documento.getStatus();
		if(StatusDocumento.EXCLUIDO.equals(status)) {
			return new ArrayList<>();
		}

		Long documentoId = documento.getId();
		List<Imagem> imagens = imagemService.findByDocumento(documentoId);

		if (DummyUtils.isHomolog() || DummyUtils.isDev()) {
			colocarImagensFalsasCasoNaoExista(imagens);
		}

		return imagens;
	}

	private void colocarImagensFalsasCasoNaoExista(List<Imagem> imagens) {

		for (Imagem imagem : imagens) {

			String imagemCaminho = imagem.getCaminho();
			if (!new File(imagemCaminho).exists()) {

				File temp = DummyUtils.getFileFromResource("/net/wasys/getdoc/imagens/image_test.jpg");
				imagem.setCaminho(temp.getAbsolutePath());
			}
		}
	}

	private List<ModeloDocumento> getModelosDocumento(Documento documento) {

		List<ModeloDocumento> modelosDocumento = null;
		TipoDocumento tipoDocumento = documento.getTipoDocumento();

		// se "sempreTipificar = false" então não dá pra confiar no modelo_documento do documento
		if (tipoDocumento != null && !tipoDocumento.getSempreTipificar()) {

			Long tipoDocumentoId = tipoDocumento.getId();
			modelosDocumento = tipoDocumentoService.findModelosDocumentos(tipoDocumentoId);
		}
		else {

			ModeloDocumento modeloDocumento = documento.getModeloDocumento();

			String documentoNome = documento.getNome();
			if (tipoDocumento != null && modeloDocumento == null) {

				Long tipoDocumentoId = tipoDocumento.getId();
				Long codOrigem = tipoDocumento.getCodOrigem();

				// para tratar casos que foi rejeitado e não tipificado os 2 documentos especificos.
				if(TipoDocumento.COMPROVANTE_ENSINO_MEDIO_COD_SIA.equals(codOrigem)){
					modelosDocumento = modeloDocumentoService.findByLabelDarknet(ModeloDocumento.CONCLUSAO_ENSINO_MEDIO_LABEL_DARKNET);
				} else if (TipoDocumento.DIPLOMA_ENSINO_SUPERIOR_COD_SIA.contains(codOrigem)){
					modelosDocumento = modeloDocumentoService.findByLabelDarknet(ModeloDocumento.DIPLOMA_ENSINO_SUPERIOR_LABEL_DARKNET);
				} else {
					modelosDocumento = tipoDocumentoService.findModelosDocumentos(tipoDocumentoId);
				}
			}
			else if (modeloDocumento != null && !equalsIgnoreCase(documentoNome, Documento.NOME_OUTROS) && !equalsIgnoreCase(documentoNome, Documento.NOME_TIFICANDO)) {
				modelosDocumento = singletonList(modeloDocumento);
			}
		}

		return modelosDocumento;
	}

	private String getPosfixoDocumento(Documento documento) {

		String posfixo = null;

		String nome = documento.getNome();
		String[] split = nome.split("-");
		if (split.length > 1) {
			posfixo = split[1].trim();
		}

		return posfixo;
	}

	private void preencherValidade(Documento documento, ReaproveitamentoDocumentoVO vo, String labelDarknet) {

		if ("documento_comprobatorio_de_escolaridade_do_ensino_superior".equals(labelDarknet)) {

			Date dataDigitalizacao = documento.getDataDigitalizacao();
			if (dataDigitalizacao != null) {

				Calendar c = Calendar.getInstance();
				c.setTime(dataDigitalizacao);
				//FIXME pq tem esse 90 fixo??
				c.add(Calendar.DATE, +90);
				Date dataValidade = c.getTime();
				vo.setValidade(dataValidade);
			}
		}
		else {
			vo.setValidade(null);
		}
	}

	private List<ReaproveitamentoImagemVO> makeReaproveitamentoImagemVOs(List<Imagem> imagens, List<ModeloDocumento> modelosDocumento, boolean ignorarArquivos) throws IOException {

		List<ReaproveitamentoImagemVO> vos = new ArrayList<>();

		for (Imagem imagem : imagens) {
			Boolean existente = imagem.isExistente();

			if(existente != null && !existente) {
				continue;
			}

			Documento doc = imagem.getDocumento();
			Long docId = doc.getId();
			Long imagemId = imagem.getId();

			File arquivoTemporario = null;
			if(!ignorarArquivos) {
				File arquivoOriginal = imagemService.getFile(imagem);
				if (!arquivoOriginal.exists()) {
					String docNome = doc.getNome();
					throw new DocumentoRestException("documento.imagem.nao.localizada.disco", imagemId, docNome);
				}

				arquivoTemporario = copiarImagemParaArquivoTemporario(imagem, arquivoOriginal);
			}

			TipoDocumento tipoDocumento = doc.getTipoDocumento();
			List<ModeloDocumentoReaproveitamentoVO> detalhesModelosDocumento = null;
			if (isNotEmpty(modelosDocumento)) {
				detalhesModelosDocumento = modelosDocumento
						.stream().map(md -> new ModeloDocumentoReaproveitamentoVO(md.getId(), md.getLabelDarknet())).collect(Collectors.toList());
			}

			ImagemMeta imagemMeta = imagemMetaService.getByImagem(imagemId);
			imagemMeta = imagemMeta != null ? imagemMeta : new ImagemMeta();

			Map<String, String> metadadosMap = (Map<String, String>) DummyUtils.jsonStringToMap(imagemMeta.getMetaDados());
			metadadosMap = metadadosMap != null ? metadadosMap : new LinkedHashMap<>();

			metadadosMap.put("imagemCaptacaoId", imagemId.toString());
			metadadosMap.put("documentoCaptacaoId", docId.toString());
			metadadosMap.put("tipoDocumentoCaptacaoId", tipoDocumento != null ? tipoDocumento.getId().toString() : null);
			List<Long> modelosDocumentosIds = new ArrayList<>();
			if(detalhesModelosDocumento != null) {
				for (ModeloDocumentoReaproveitamentoVO mdrVO : detalhesModelosDocumento) {
					Long modeloDocumentoId = mdrVO.getModeloDocumentoId();
					modelosDocumentosIds.add(modeloDocumentoId);
				}
			}
			metadadosMap.put("modelosDocumentos", modelosDocumentosIds.toString());

			String hashChecksum = imagem.getHashChecksum();

			ReaproveitamentoImagemVO reaproveitamentoVO = new ReaproveitamentoImagemVO();
			reaproveitamentoVO.setCaminho(arquivoTemporario != null ? arquivoTemporario.getAbsolutePath() : null);
			reaproveitamentoVO.setMetadados(DummyUtils.objectToJson(metadadosMap));
			reaproveitamentoVO.setVersao(imagem.getVersao());
			reaproveitamentoVO.setHash(hashChecksum);

			vos.add(reaproveitamentoVO);
		}

		return vos;
	}

	private File copiarImagemParaArquivoTemporario(Imagem imagem, File arquivoOriginal) throws IOException {
		int tentativa = 0;
		IOException ex = null;
		do {
			try {
				String storageTmPath = resourceService.getValue(ResourceService.STORAGE_TMP_PATH);
				String arquivoOriginalName = arquivoOriginal.getName();
				String extensao = DummyUtils.getExtensao(arquivoOriginalName);
				String prefix = DummyUtils.SYSPREFIX + "/reaproveitamento/";
				prefix = prefix.replace("[", "").replace("]", "").replace(" ", "");

				Documento documento = imagem.getDocumento();
				Date dataDigitalizacao = documento.getDataDigitalizacao();
				Calendar c = Calendar.getInstance();
				c.setTime(dataDigitalizacao);
				int ano = c.get(Calendar.YEAR);
				int mes = c.get(Calendar.MONTH) + 1;
				String mesStr = mes <= 9 ? "0" + mes : String.valueOf(mes);
				int dia = c.get(Calendar.DAY_OF_MONTH);
				String diaStr = dia <= 9 ? "0" + dia : String.valueOf(dia);

				Processo processo = documento.getProcesso();
				Long processoId = processo.getId();
				String processoIdStr = processoId.toString();
				int length = processoIdStr.length();
				String agrupador = processoIdStr.substring(length - 3, length);

				String rootDir = storageTmPath + File.separator + prefix + File.separator;
				StringBuilder caminho = new StringBuilder(rootDir);
				caminho.append(ano).append(File.separator);
				caminho.append(mesStr).append(File.separator);
				caminho.append(diaStr).append(File.separator);
				caminho.append(agrupador).append(File.separator);

				Long imagemId = imagem.getId();
				File tmp = new File(caminho.toString(), processoId + "_" + imagemId + "." + extensao);
				FileUtils.copyFile(arquivoOriginal, tmp);
				return tmp;
			}
			catch (IOException e) {
				DummyUtils.sleep(1000);
				ex = e;
				tentativa++;
			}
		}
		while(tentativa < 3);

		throw ex;
	}

	private void preencherUsuarioAcao(ReaproveitamentoDocumentoVO vo, Documento documento) {

		String usuarioDigitalizou = "", usuarioAprovou = "";

		StatusDocumento documentoStatus = documento.getStatus();
		if (StatusDocumento.DIGITALIZADO.equals(documentoStatus) || StatusDocumento.APROVADO.equals(documentoStatus)) {

			Long documentoId = documento.getId();
			DocumentoLog docLog = documentoLogService.findLastByDocumentoAndAcaoDocumento(documentoId, Arrays.asList(AcaoDocumento.DIGITALIZOU, AcaoDocumento.APROVOU));
			if (docLog != null) {

				AcaoDocumento acao = docLog.getAcao();
				Usuario usuario = docLog.getUsuario();
				String login = usuario != null ? usuario.getLogin() : null;

				if (acao.equals(AcaoDocumento.DIGITALIZOU)) {
					usuarioDigitalizou = login;
				}
				if (acao.equals(AcaoDocumento.APROVOU)) {
					usuarioAprovou = login;
				}
			}
		}

		vo.setUsuarioAprovou(usuarioAprovou);
		vo.setUsuarioDigitalizou(usuarioDigitalizou);
	}

	private List<Documento> filtrarDocumentosParaReaproveitar(Processo processo) {

		DocumentoFiltro filtroDoc = new DocumentoFiltro();
		filtroDoc.setProcesso(processo);
		filtroDoc.setFetch(Arrays.asList(DocumentoFiltro.Fetch.TIPO_DOCUMENTO, DocumentoFiltro.Fetch.MODELO_DOCUMENTO));
		filtroDoc.setStatusDifetenteDeList(Arrays.asList(StatusDocumento.EXCLUIDO));

		return documentoService.findByFiltro(filtroDoc, null, null);
	}
}
