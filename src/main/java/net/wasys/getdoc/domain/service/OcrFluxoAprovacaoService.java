package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.DummyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class OcrFluxoAprovacaoService {

	@Autowired
    private DocumentoService documentoService;
	@Autowired
    private UsuarioService usuarioService;
	@Autowired
    private SituacaoService situacaoService;
	@Autowired
    private ProcessoService processoService;

	@Transactional(rollbackFor=Exception.class)
	public void realizarOcrByProcesso(Processo processo, boolean reprocessamento) throws Exception {
		Usuario usuarioAdmin = usuarioService.getByLogin(Usuario.LOGIN_ADMIN);

		Long processoId = processo.getId();
		MultiValueMap<Long, Long> idsToOcrFluxoAprovacao = documentoService.findIdsToOcrFluxoAprovacao(processoId);

		systraceThread("OCR - Processo Id: " + processoId);
		systraceThread("OCR - Processo possui documentos: " + idsToOcrFluxoAprovacao.size());

		if (idsToOcrFluxoAprovacao.isEmpty() && !reprocessamento) {
			Situacao situacao = getSituacaoAguardandoAnaliseIA(processo);
			if (situacao != null) {
				processoService.concluirSituacao(processo, usuarioAdmin, situacao, null, null);
				processoService.agendarFacialRecognition(processo);
			} else {
				processoService.enviarParaAnalise(processo, usuarioAdmin);
			}
			return;
		}
		systraceThread("OCR - Total de Documentos a serem processados: " + idsToOcrFluxoAprovacao.size() + " do processo " + processoId);

		for (Long documentoId : idsToOcrFluxoAprovacao.keySet()) {
			systraceThread("Tipificacao - Tipificando Documento id: " + documentoId);
			Documento documento = documentoService.get(documentoId);
			documentoService.extrairOCR(processo, documento);
		}

		if (!reprocessamento) {
			Situacao situacao = getSituacaoAguardandoAnaliseIA(processo);
			processo.setSituacao(situacao);
			processoService.concluirSituacao(processo, usuarioAdmin, situacao, null, null);
			processoService.agendarFacialRecognition(processo);
		}
	}

	private Situacao getSituacaoAguardandoAnaliseIA(Processo processo) {
		SituacaoFiltro filtro = new SituacaoFiltro();
		filtro.setTipoProcessoId(processo.getTipoProcesso().getId());
		filtro.setNome(Situacao.AGUARDANDO_ANALISE_IA);
		List<Situacao> list = situacaoService.findByFiltro(filtro, null, null);

		Situacao situacao = CollectionUtils.isEmpty(list) ? null : list.get(0);

		return situacao;
	}


}
