package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.CampanhaEquivalenciaVO;
import net.wasys.getdoc.domain.vo.CampoOcrVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.transaction.annotation.Transactional;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class FluxoAprovacaoAnaliseService {

    @Autowired private ProcessoService processoService;
    @Autowired private DocumentoService documentoService;
    @Autowired private CampoOcrService campoOcrService;
    @Autowired private PendenciaService pendenciaService;
    @Autowired private DocumentoLogService documentoLogService;
    @Autowired private IrregularidadeService irregularidadeService;
    @Autowired private SituacaoService situacaoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private ConfiguracaoOCRService configuracaoOCRService;
    @Autowired private ImagemMetaService imagemMetaService;
    @Autowired private CampanhaService campanhaService;
    @Autowired private LogAnaliseIAService logAnaliseIAService;
    @Autowired private ImagemService imagemService;
    @Autowired private BaseRegistroValorService baseRegistroValorService;
    @Autowired private BaseRegistroService baseRegistroService;

    private double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return (longerLength - levenshteinDistance.apply(longer.toUpperCase(), shorter.toUpperCase())) / (double) longerLength;
    }

    public void pendenciarDocumento(Documento documento, Irregularidade irregularidade, Usuario usuario, String mensagem){
        Pendencia pendencia = new Pendencia();
        pendencia.setDataCriacao(new Date());
        pendencia.setDocumento(documento);
        pendencia.setIrregularidade(irregularidade);
        pendencia.setObservacao(mensagem);
        pendencia.setNotificadoAtila(false);
        pendenciaService.saveOrUpdate(pendencia);

        documentoLogService.criaLog(pendencia, usuario, AcaoDocumento.REJEITOU);

        documento.setStatus(StatusDocumento.PENDENTE);
        documentoService.saveOrUpdate(documento);
    }

    public void validarResultadoOCR(Processo processo, boolean reprocessamento) throws Exception {

        boolean aprovar = configuracaoOCRService.isFluxoAprovacaoDocumentos(processo.getId(), processo.getTipoProcesso().getId());
        if (reprocessamento)
            aprovar = false;

        DocumentoFiltro filtro = new DocumentoFiltro();
        filtro.setProcesso(processo);
        filtro.setStatusDocumentoList(Arrays.asList(StatusDocumento.DIGITALIZADO));

        List<Long> documentosId = documentoService.findIdsByFiltro(filtro, 0, 200);

        for (Long documentoId : documentosId) {
            systraceThread("Iniciando analise do documento " + documentoId);
            Documento documento = documentoService.get(documentoId);
            validarCampoOCR(processo, documento, aprovar);
            //documento.setStatusOcr(StatusOcr.TUDO_OK);
        }

        validarAprovacaoProcesso(processo, aprovar);
    }

    public void relatarIAInativa(Processo processo){
        DocumentoFiltro filtro = new DocumentoFiltro();
        filtro.setProcesso(processo);
        filtro.setStatusDifetenteDeList(Arrays.asList(StatusDocumento.EXCLUIDO));

        List<Long> documentosId = documentoService.findIdsByFiltro(filtro, 0, 200);
        for (Long documentoId : documentosId) {
            Documento documento = documentoService.get(documentoId);
            salvarLog(processo, documento, "IA DESLIGADA PARA O PROCESSO", "IA DESLIGADA PARA O PROCESSO", false, "");
        }
    }

    private void salvarLog(Processo processo, Documento documento, String statusDocumento, String motivoDocumento, boolean extraiuOCR, String metadados) {
        LogAnaliseIA logAnaliseIA = logAnaliseIAService.findByProcessoAndDocumento(processo, documento);
        logAnaliseIA.setData(new Date());
        logAnaliseIA.setDocumento(documento);
        logAnaliseIA.setProcesso(processo);
        logAnaliseIA.setTipoProcesso(processo.getTipoProcesso());
        logAnaliseIA.setMetadados(metadados);
        logAnaliseIA.setStatusDocumento(StringUtils.isEmpty(statusDocumento) ? documento.getStatus().toString() : statusDocumento);
        logAnaliseIA.setMotivoDocumento(motivoDocumento);
        logAnaliseIA.setStatusProcesso(null);
        logAnaliseIA.setMotivoProcesso(null);
        GrupoModeloDocumento grupoModeloDocumento = documento.getGrupoModeloDocumento();
        if (grupoModeloDocumento != null)
            logAnaliseIA.setTipificou(true);
        logAnaliseIA.setOcr(extraiuOCR);

        logAnaliseIAService.saveOrUpdate(logAnaliseIA);

    }

    private void validarAprovacaoProcesso(Processo processo, boolean aprovar) throws Exception {
        DocumentoFiltro filtro = new DocumentoFiltro();
        filtro.setProcesso(processo);
        filtro.setStatusDifetenteDeList(Arrays.asList(StatusDocumento.EXCLUIDO));

        List<Documento> documentoList = documentoService.findByFiltro(filtro, null, null);
        long totalDocumentos = documentoList.size();

        long documentosAprovado = documentoList.stream().filter(documento -> documento.getStatus().equals(StatusDocumento.APROVADO)).count();
        long documentosDigitalizadosEquivalidos = getTotalDocumentosEquivalidos(processo, documentoList.stream().filter(documento -> documento.getStatus().equals(StatusDocumento.DIGITALIZADO)).collect(Collectors.toList()), documentoList);
        long documentosIncluidoEquivalidos = getTotalDocumentosEquivalidos(processo, documentoList.stream().filter(documento -> documento.getStatus().equals(StatusDocumento.INCLUIDO)).collect(Collectors.toList()), documentoList);
        long documentosDigitalizados = documentoList.stream().filter(documento -> documento.getStatus().equals(StatusDocumento.DIGITALIZADO)).count() - documentosDigitalizadosEquivalidos;
        long documentosIncluidos = documentoList.stream().filter(documento -> documento.getStatus().equals(StatusDocumento.INCLUIDO)).count() - documentosIncluidoEquivalidos;

        if (documentosDigitalizados == 0){
            //Sem documentos digitalizados e todos os outros estão aprovados ou equivalidos
            if ((documentosAprovado + documentosIncluidoEquivalidos) >= totalDocumentos){
                if (aprovar) {
                    Long tipoProcessoId = processo.getTipoProcesso().getId();
                    Situacao situacao = situacaoService.getByNome(tipoProcessoId, Situacao.DOCUMENTACAO_APROVADA);
                    processoService.concluirSituacao(processo, null, situacao, null, null);
                }
                logAnaliseIAService.updateStatusMotivoProcesso(processo, Situacao.DOCUMENTACAO_APROVADA, "Processo com todos os documentos APROVADOS ou EQUIVALIDOS");
                return;
            } else {
                //Nesse caso não tem nada para analise e deve ser encaminhado para Pendencia
                if (aprovar) {
                    Long tipoProcessoId = processo.getTipoProcesso().getId();
                    Situacao situacao = situacaoService.getByNome(tipoProcessoId, Situacao.DOCUMENTACAO_PENDENTE);
                    processoService.concluirSituacao(processo, null, situacao, null, null);
                }
                logAnaliseIAService.updateStatusMotivoProcesso(processo, Situacao.DOCUMENTACAO_PENDENTE, "Processo sem documentos para análise.");
                return;
            }
        } else {
            if (aprovar) {
                Long tipoProcessoId = processo.getTipoProcesso().getId();
                Situacao situacao = situacaoService.getByNome(tipoProcessoId, Situacao.EM_ANALISE);
                processoService.concluirSituacao(processo, null, situacao, null, null);
            }
            logAnaliseIAService.updateStatusMotivoProcesso(processo, Situacao.EM_ANALISE, "Processo com documentos DIGITALIZADOS.");
            return;
        }

    }

    private long getTotalDocumentosEquivalidos(Processo processo, List<Documento> documentoStatusList, List<Documento> documentoList) {
        Long retorno = 0L;

        for (Documento documento: documentoStatusList) {
            if (isDocumentoEquivalido(processo, documento, documentoList)) {
                retorno++;
            }

        }

        return retorno;

    }

    private boolean isDocumentoEquivalido(Processo processo, Documento documento, List<Documento> documentosProcesso){
        Campanha campanha = campanhaService.getByProcesso(processo);
        String equivalencias = campanha.getEquivalencias();

        List<CampanhaEquivalenciaVO> equivalenciasObj = campanhaService.getEquivalenciasObj(equivalencias);
        StatusDocumento statusDocumento = documento.getStatus();
        //Somente verifica documentos INCLUIDO ou DIGITALIZADO
        if (statusDocumento.equals(StatusDocumento.INCLUIDO)){
            //Busca a campanha do processo e as suas equivalencias
            for (CampanhaEquivalenciaVO campanhaEquivalenciaVO : equivalenciasObj) {
                List<TipoDocumento> tipoDocumentos = campanhaEquivalenciaVO.getDocumentosEquivalidos();
                for (TipoDocumento tipoDocumento: tipoDocumentos) {
                    //Percorre os documentos equivalidos e identifica se algum desses documento que está sendo analisado
                    if (tipoDocumento.equals(documento.getTipoDocumento())){
                        //Filtra a lista para buscar o documento que valida esse
                        Optional<Documento> optionalDocumento = documentosProcesso.stream().filter(documentoProcesso -> documentoProcesso.getTipoDocumento().equals(campanhaEquivalenciaVO.getDocumentoEquivalente())).findAny();
                        if (optionalDocumento.isPresent()){
                            Documento documentoEquivalente = optionalDocumento.get();
                            //Caso o documento equivalente esteja aprovado, incrementar.
                            if (documentoEquivalente.getStatus().equals(StatusDocumento.APROVADO)){
                                LogAnaliseIA logAnaliseIA = logAnaliseIAService.findByProcessoAndDocumento(processo, documento);
                                logAnaliseIA.setStatusDocumento("EQUIVALIDO");
                                logAnaliseIA.setMotivoDocumento("Documento EQUIVALIDO pelo documento " + documentoEquivalente.getNome());

                                logAnaliseIAService.saveOrUpdate(logAnaliseIA);
                                return true;
                            }
                        }
                    }
                }
            }
        }


        return false;
    }

    private void validarCampoOCR(Processo processo, Documento documento, boolean aprovarDocumento) {
        try {
            String statusDocumento = "";
            String motivoDocumento = "";
            Map<String, Object> metadadosMap = new LinkedHashMap<>();
            Usuario usuario = usuarioService.getByLogin(Usuario.LOGIN_ADMIN);

            Aluno aluno = processo.getAluno();

            if (aluno == null) {
                Irregularidade irregularidade = irregularidadeService.get(Irregularidade.DOCUMENTO_DIVERGENTE_ID);
                pendenciarDocumento(documento, irregularidade, usuario, "Não há candidato cadastrado para o processo atual.");
                return;
            }

            List<CampoOcr> campoOcrList = campoOcrService.findByDocumento(documento.getId());
            Long totalCampos = 0L;
            Long camposAprovados = 0L;
            List<CampoOcr.ResultadoComparacaoOCR> campoMetadados = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(campoOcrList)) {
                for (CampoOcr campoOcr : campoOcrList) {
                    if (validarOCR(campoOcr)) {
                        totalCampos++;
                    } else {
                        continue;
                    }

                    CampoOcr.ResultadoComparacaoOCR resultadoComparacaoOCR = validarOCR(campoOcr, aluno);
                    if (resultadoComparacaoOCR != null && resultadoComparacaoOCR.isAprovado()) {
                        camposAprovados++;
                    }

                    campoMetadados.add(resultadoComparacaoOCR);
                }
            }

            metadadosMap.put("FlagAnaliseIA", aprovarDocumento);
            if (camposAprovados > 0 && camposAprovados >= totalCampos) {
                if (aprovarDocumento && !documento.getStatus().equals(StatusDocumento.APROVADO)) {
                    documentoService.aprovar(documento, usuario);
                }
                metadadosMap.put("ResultadoAnaliseIA", "APROVADO");
                statusDocumento = (String) metadadosMap.get("ResultadoAnaliseIA");
                motivoDocumento = "Documento aprovado. OCR convergente.";
                documento.setStatusOcr(StatusOcr.TUDO_OK);
            } else {
                metadadosMap.put("ResultadoAnaliseIA", documento.getStatus().toString());
            }

            metadadosMap.put("totalCamposOCRValidados", totalCampos);
            metadadosMap.put("totalCamposOCRAprovados", camposAprovados);
            metadadosMap.put("camposOCR", campoMetadados);

            JSONObject metadadosJson = new JSONObject(metadadosMap);
            String metadadosStr = metadadosJson.toString();
            documentoService.saveOrUpdate(documento);
            salvarLog(processo, documento, statusDocumento, motivoDocumento, CollectionUtils.isNotEmpty(campoMetadados), metadadosStr);
        }
        catch ( Exception e){
            String obs = e.getMessage();
            if(StringUtils.isBlank(obs)){
                obs = e.getCause().toString();
            }
            salvarLog(processo, documento, "", "", false, obs);
        }
    }

    private boolean validarOCR(CampoOcr campoOcr) {
        CampoModeloOcr campoModeloOcr = campoOcr.getCampoModeloOcr();
        String tipo = campoModeloOcr.getValorTipo();
        String valorAprovacao = campoModeloOcr.getValorAprovacao();
        String valorOperador = campoModeloOcr.getValorOperador();

        if (StringUtils.isEmpty(valorOperador) || StringUtils.isEmpty(valorAprovacao) || StringUtils.isEmpty(tipo)) {
            return false;
        } else {
            return true;
        }
    }

    private CampoOcr.ResultadoComparacaoOCR validarOCR(CampoOcr campoOcr, Aluno aluno) {

        CampoModeloOcr campoModeloOcr = campoOcr.getCampoModeloOcr();
        CampoModeloOcr.TipoComparacao tipoComparacao = CampoModeloOcr.TipoComparacao.valueOf(campoModeloOcr.getValorTipo());
        CampoOcr.ResultadoComparacaoOCR resultadoComparacaoOCR = null;

        String campoAnalise = getCampoAnalise(campoOcr);

        if (tipoComparacao.equals(CampoModeloOcr.TipoComparacao.SIMILARIDADE)) {
            CampoModeloOcr.ValorComparacaoSimilaridade valorComparacaoSimilaridade = CampoModeloOcr.ValorComparacaoSimilaridade.valueOf(campoModeloOcr.getCampoProcesso());
            if (valorComparacaoSimilaridade.equals(CampoModeloOcr.ValorComparacaoSimilaridade.NOME)) {
                resultadoComparacaoOCR = compararResultadoOCRSimilaridade(campoAnalise, aluno.getNome(), campoModeloOcr.getValorOperador(), campoModeloOcr.getValorAprovacao(), campoModeloOcr.getNome(), campoModeloOcr.isFulltext());
            } else {
                if (valorComparacaoSimilaridade.equals(CampoModeloOcr.ValorComparacaoSimilaridade.CPF)) {
                    String cpf = aluno.getCpf();
                    cpf = DummyUtils.getCpfCnpjDesformatado(cpf);
                    resultadoComparacaoOCR = compararResultadoOCRSimilaridade(campoAnalise, cpf, campoModeloOcr.getValorOperador(), campoModeloOcr.getValorAprovacao(), campoModeloOcr.getNome(), campoModeloOcr.isFulltext());
                }
            }

            return resultadoComparacaoOCR;
        }

        if (tipoComparacao.equals(CampoModeloOcr.TipoComparacao.INTEGER)) {
            resultadoComparacaoOCR = compararResultadoOCRInteger(campoAnalise, campoModeloOcr.getValorOperador(), campoModeloOcr.getValorAprovacao(), campoModeloOcr.getNome());
            return resultadoComparacaoOCR;
        }

        if (tipoComparacao.equals(CampoModeloOcr.TipoComparacao.DOUBLE)) {
            resultadoComparacaoOCR = compararResultadoOCRDouble(campoAnalise, campoModeloOcr.getValorOperador(), campoModeloOcr.getValorAprovacao(), campoModeloOcr.getNome());
            return resultadoComparacaoOCR;
        }

        if (tipoComparacao.equals(CampoModeloOcr.TipoComparacao.BASE_INTERNA)) {

            Long baseInternaId = Long.parseLong(campoModeloOcr.getCampoProcesso());

            List<RegistroValorVO> valores = baseRegistroService.findByBaseInterna(baseInternaId, false);

            for (RegistroValorVO registroValorVO : valores) {
                String frase = registroValorVO.getBaseRegistro().getChaveUnicidade();

                resultadoComparacaoOCR = compararResultadoOCRSimilaridade(campoAnalise, DummyUtils.limparCharsChaveUnicidade(frase), campoModeloOcr.getValorOperador(), campoModeloOcr.getValorAprovacao(), campoModeloOcr.getNome(), campoModeloOcr.isFulltext());
                if (resultadoComparacaoOCR.isAprovado())
                    break;
            }
            return resultadoComparacaoOCR;
        }

        return resultadoComparacaoOCR;
    }

    private CampoOcr.ResultadoComparacaoOCR compararResultadoOCRDouble(String valor, String valorOperador, String valorAprovacao, String nomeCampo) {
        CampoOcr.ResultadoComparacaoOCR resultadoComparacaoOCR = new CampoOcr.ResultadoComparacaoOCR();
        boolean aprovado = false;

        try {

            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
            Number valorNumber = format.parse(valor);
            Number valorAprovacaoNumber = format.parse(valorAprovacao);

            double valorDouble = valorNumber.doubleValue();
            double valorAprovacaoDouble = valorAprovacaoNumber.doubleValue();

            CampoModeloOcr.Operador operador = CampoModeloOcr.Operador.valueOf(valorOperador);

            switch (operador){
                case IGUAL:
                    aprovado = valorDouble == valorAprovacaoDouble;
                    break;
                case DIFERENTE:
                    aprovado = valorDouble != valorAprovacaoDouble;
                    break;
                case MAIOR:
                    aprovado = valorDouble > valorAprovacaoDouble;
                    break;
                case MENOR:
                    aprovado = valorDouble < valorAprovacaoDouble;
                    break;
                case MAIOR_IGUAL:
                    aprovado = valorDouble >= valorAprovacaoDouble;
                    break;
                default:
                    aprovado = false;
            }

        } catch (Exception e){
            //Ocorreu algum erro no parse do valor do OCR ou do Cadastro.
            aprovado = false;
        }

        resultadoComparacaoOCR.setAprovado(aprovado);
        resultadoComparacaoOCR.setNomeCampo(nomeCampo);
        resultadoComparacaoOCR.setValorOCR(valor);
        resultadoComparacaoOCR.setValorProcesso(valorAprovacao);

        return resultadoComparacaoOCR;

    }

    private String getCampoAnalise(CampoOcr campoOcr) {
        CampoModeloOcr campoModeloOcr = campoOcr.getCampoModeloOcr();
        if (campoModeloOcr.isFulltext()){
            String fullText = imagemMetaService.getFullText(campoOcr.getImagem().getId());
            return fullText;
        } else {
            return campoOcr.getValor();
        }
    }

    private CampoOcr.ResultadoComparacaoOCR compararResultadoOCRInteger(String valor, String valorOperador, String valorAprovacao, String nomeCampo) {

        CampoOcr.ResultadoComparacaoOCR resultadoComparacaoOCR = new CampoOcr.ResultadoComparacaoOCR();
        boolean aprovado = false;

        try {

            Integer valorInteger = Integer.parseInt(valor);
            Integer valorAprovacaoInteger = Integer.parseInt(valorAprovacao);

            CampoModeloOcr.Operador operador = CampoModeloOcr.Operador.valueOf(valorOperador);

            switch (operador){
                case IGUAL:
                    aprovado = valorInteger == valorAprovacaoInteger;
                    break;
                case DIFERENTE:
                    aprovado = valorInteger != valorAprovacaoInteger;
                    break;
                case MAIOR:
                    aprovado = valorInteger > valorAprovacaoInteger;
                    break;
                case MENOR:
                    aprovado = valorInteger < valorAprovacaoInteger;
                    break;
                case MAIOR_IGUAL:
                    aprovado = valorInteger >= valorAprovacaoInteger;
                    break;
                default:
                    aprovado = false;
            }

        } catch (Exception e){
            //Ocorreu algum erro no parse do valor do OCR ou do Cadastro.
            aprovado = false;
        }

        resultadoComparacaoOCR.setAprovado(aprovado);
        resultadoComparacaoOCR.setNomeCampo(nomeCampo);
        resultadoComparacaoOCR.setValorOCR(valor);
        resultadoComparacaoOCR.setValorProcesso(valorAprovacao);

        return resultadoComparacaoOCR;
    }

    private CampoOcr.ResultadoComparacaoOCR compararResultadoOCRSimilaridade(String valor, String valorEsperado, String valorOperador, String valorAprovacao, String nomeCampo, boolean fulltext) {
        CampoOcr.ResultadoComparacaoOCR resultadoComparacaoOCR = new CampoOcr.ResultadoComparacaoOCR();

        double similarity = 0;
        if (fulltext) {
            similarity = this.similarityFulltext(valor, valorEsperado) * 100;
        } else {
            similarity = this.similarity(valor, valorEsperado) * 100;
        }

        boolean aprovado = compararSimilaridade(similarity, valorOperador, valorAprovacao);

        resultadoComparacaoOCR.setAprovado(aprovado);
        resultadoComparacaoOCR.setNomeCampo(nomeCampo);
        resultadoComparacaoOCR.setPercentualValidacao(similarity);
        resultadoComparacaoOCR.setValorOCR(valor);
        resultadoComparacaoOCR.setValorProcesso(valorEsperado);

        return resultadoComparacaoOCR;
    }

    private double similarityFulltext(String valor, String valorEsperado) {
        valor = valor != null ? valor : "";
        valorEsperado = valorEsperado != null ? valorEsperado : "";

        int letrasTotal = 0;
        int letrasEncontradas = 0;
        for (String palavra : valorEsperado.trim().split(" ")) {
            if (StringUtils.isNotBlank(palavra)) {
                palavra = StringUtils.trim(palavra);
                palavra = StringUtils.upperCase(palavra);
                palavra = DummyUtils.substituirCaracteresEspeciais(palavra);
                valor = DummyUtils.substituirCaracteresEspeciais(valor);
                letrasTotal+= palavra.length();
                if (valor.matches(".*[^\\w]" + palavra + "[A-Z]*[^\\w].*")) {
                    letrasEncontradas+= palavra.length();
                }
            }
        }
        return (double) letrasEncontradas / (double) letrasTotal;
    }

    private boolean compararSimilaridade(double similarity, String valorOperador, String valorAprovacao) {
        double valorEsperado = Double.parseDouble(valorAprovacao);
        boolean retorno = false;

        CampoModeloOcr.Operador operador = CampoModeloOcr.Operador.valueOf(valorOperador);

        switch (operador){
            case IGUAL:
                retorno = similarity == valorEsperado;
                break;
            case DIFERENTE:
                retorno = similarity != valorEsperado;
                break;
            case MAIOR:
                retorno = similarity > valorEsperado;
                break;
            case MENOR:
                retorno = similarity < valorEsperado;
                break;
            case MAIOR_IGUAL:
                retorno = similarity >= valorEsperado;
                break;
            default:
                return false;
        }

        return retorno;
    }
}
