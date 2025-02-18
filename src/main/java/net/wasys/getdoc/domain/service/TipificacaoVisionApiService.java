package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.enumeration.CamposMetadadosTipificacao;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.util.DummyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class TipificacaoVisionApiService {

	public static final int PERCENTUAL_MINIMO_TIPIFICACAO_DEFAULT = 10;

	@Autowired private VisionApiService visionApiService;
	@Autowired private RelatorioTipificacaoService relatorioTipificacaoService;
	@Autowired private ResourceService resourceService;
	@Autowired private TipoDocumentoService tipoDocumentoService;

	public boolean isHabilitada() {
		String visionKey = resourceService.getValue(ResourceService.VISION_PROCESSOR_KEY);
		return StringUtils.isNotBlank(visionKey);
	}

	public Map<Long, List<FileVO>> tipificar(List<Documento> documentos , List<FileVO> arquivos) {

		long inicio = System.currentTimeMillis();
		Map<Long, List<FileVO>> map = new LinkedHashMap<>();

		if(!isHabilitada()) {
			systraceThread("fim. vision não habilitado");
			return map;
		}

		List<FileVO> arquivos2 = new ArrayList<>();
		for (FileVO fileVO : arquivos) {
			String text = fileVO.getText();
			if(StringUtils.isBlank(text)) {
				arquivos2.add(fileVO);
			}
		}

		visionApiService.detectingText(arquivos2);

		for (FileVO fileVO : new ArrayList<>(arquivos)) {

			Documento documento = null;
			ModeloDocumento modeloDocumento = null;
			String text = fileVO.getText();

			text = StringUtils.trim(text);
			if(StringUtils.isNotBlank(text)) {

				text = StringUtils.upperCase(text);
				text = DummyUtils.substituirCaracteresEspeciais(text);
				text = " " + text.replace("\n", " ") + " ";

				Map<Integer, Object[]> opcoes = new TreeMap<>();

				boolean achou = false;
				for (int i = 0; i < documentos.size() && !achou; i++) {

					Documento documento2 = documentos.get(i);

					TipoDocumento tipoDocumento = documento.getTipoDocumento();
					Long tipoDocumentoId = tipoDocumento.getId();

					List<ModeloDocumento> modelosDocumentos = tipoDocumentoService.findModelosDocumentos(tipoDocumentoId);
					for (ModeloDocumento modeloDocumento2 : modelosDocumentos) {

						boolean visionApiHabilitada = modeloDocumento2.getVisionApiHabilitada();
						if(!visionApiHabilitada	) continue;

						boolean contemPalavraExcludente = contemPalavraExcludente(text, documento2, modeloDocumento2);
						if(contemPalavraExcludente) continue;

						String palavrasEsperadas = modeloDocumento2.getPalavrasEsperadas();
						palavrasEsperadas = palavrasEsperadas != null ? palavrasEsperadas : "";
						String[] palavrasSplit = palavrasEsperadas.split(";");
						int palavrasCount = 0;
						int palavrasOk = 0;
						for (String palavra : palavrasSplit) {
							if(StringUtils.isNotBlank(palavra)) {

								palavrasCount++;

								palavra = StringUtils.trim(palavra);
								palavra = StringUtils.upperCase(palavra);
								palavra = DummyUtils.substituirCaracteresEspeciais(palavra);

								boolean matches = text.matches(".*[^\\w]"  + palavra + "[^\\w].*");
								if(matches) {
									//DummyUtils.systraceThread("Encontrou " + palavra);
									palavrasOk++;
								}
							}
						}

						double percent = (double) palavrasOk / (double) palavrasCount * 100;
						int percent2 = (int) percent;
						while(opcoes.containsKey(percent2)) {
							percent2--;
						}
						opcoes.put(percent2, new Object[]{documento2, modeloDocumento2});

						if(percent == 100) {
							achou = true;
							documento = documento2;
							modeloDocumento = modeloDocumento2;
							break;
						}
					}
				}

				Map<String, String> metadados = fileVO.getMetadados();
				metadados = metadados != null ? metadados : new LinkedHashMap<>();
				fileVO.setMetadados(metadados);
				metadados.put(CamposMetadadosTipificacao.GV_IMAGEM_TIPIFICADA.getCampo(), "true");

				if(achou) {
					systraceThread("achouTudoOk. " + documento);
					metadados.put(CamposMetadadosTipificacao.GV_PERCENTUAL_ACERTO.getCampo(), "100");
				}
				else if (!opcoes.isEmpty()) {
					systraceThread("pegando melhor opção. " + opcoes);

					Set<Integer> porcentagensSet = opcoes.keySet();
					List<Integer> porcentagensList = new ArrayList<>(porcentagensSet);
					Integer melhorPorcentagem = porcentagensList.get(porcentagensList.size() - 1);
					Object[] objects = opcoes.get(melhorPorcentagem);
					Documento documentoMelhorPorcentagem = (Documento) objects[0];
					ModeloDocumento modeloDocumento2 = (ModeloDocumento) objects[1];
					int percentualMinimoTipificacao = modeloDocumento2 != null ? modeloDocumento2.getPercentualMininoTipificacao() : PERCENTUAL_MINIMO_TIPIFICACAO_DEFAULT;
					if(melhorPorcentagem > percentualMinimoTipificacao) {
						documento = documentoMelhorPorcentagem;
						modeloDocumento = modeloDocumento2;
						metadados.put(CamposMetadadosTipificacao.GV_PERCENTUAL_ACERTO.getCampo(), String.valueOf(percentualMinimoTipificacao));
					}
				}

				if(documento != null) {
					Long documentoId = documento.getId();
					List<FileVO> list2 = map.get(documentoId);
					list2 = list2 != null ? list2 : new ArrayList<>();
					map.put(documentoId, list2);
					list2.add(fileVO);
					fileVO.setModeloTipificacao(modeloDocumento);

					arquivos.remove(fileVO);
				}
			}
		}

		return map;
	}

	private boolean contemPalavraExcludente(String text, Documento documento, ModeloDocumento modeloDocumento) {

		String palavrasExcludentes = modeloDocumento.getPalavrasExcludentes();

		if(StringUtils.isBlank(palavrasExcludentes)) {
			return false;
		}

		palavrasExcludentes = palavrasExcludentes != null ? palavrasExcludentes : "";
		String[] palavrasExcludentesSplit = palavrasExcludentes.split(";");
		List<String> palavrasExcludentesList = Arrays.asList(palavrasExcludentesSplit);

		for(String palavra : palavrasExcludentesList) {

			palavra = StringUtils.upperCase(palavra);
			palavra = DummyUtils.substituirCaracteresEspeciais(palavra);

			if(text.contains(palavra)) {
				return true;
			}
		}

		return false;
	}
}
