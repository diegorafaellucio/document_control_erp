package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.BacalhauReversoRelatorio;
import net.wasys.getdoc.domain.repository.BacalhauReversoRelatorioRepository;
import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BacalhauReversoRelatorioService {

    @Autowired
    private BacalhauReversoRelatorioRepository bacalhauReversoRelatorioRepository;

    @Transactional(rollbackFor=Exception.class)
    public void registrar(Date dataExecucao, BacalhauReversoRelatorioBuilder builder) {

        MultiValueMap<String, BacalhauReversoRelatorioBuilder.FileAuxVO> mesDir = builder.getMesDir();
        Map<Date, BacalhauReversoRelatorioJson> estatisticasPorMesDiretorio = getEstatisticas(mesDir);

        MultiValueMap<String, BacalhauReversoRelatorioBuilder.FileAuxVO> mesDigitalizacao = builder.getMesDigitalizacao();
        Map<Date, BacalhauReversoRelatorioJson> estatisticasPorMesDigitalizacao = getEstatisticas(mesDigitalizacao);

        MultiValueMap<String, BacalhauReversoRelatorioBuilder.FileAuxVO> mesDataUltimaAlteracao = builder.getMesDataUltimaAlteracao();
        Map<Date, BacalhauReversoRelatorioJson> estatisticasPorMesUltimaAlteracao = getEstatisticas(mesDataUltimaAlteracao);

        MultiValueMap<String, BacalhauReversoRelatorioBuilder.FileAuxVO> mesDataUltimoAcesso = builder.getMesDataUltimoAcesso();
        Map<Date, BacalhauReversoRelatorioJson> estatisticasPorMesUltimoAcesso = getEstatisticas(mesDataUltimoAcesso);

        TreeSet<Date> mesesReferencia = new TreeSet<>();
        mesesReferencia.addAll(estatisticasPorMesDiretorio.keySet());
        mesesReferencia.addAll(estatisticasPorMesDigitalizacao.keySet());
        mesesReferencia.addAll(estatisticasPorMesUltimaAlteracao.keySet());
        mesesReferencia.addAll(estatisticasPorMesUltimoAcesso.keySet());

        for (Date mesReferencia : mesesReferencia) {

            BacalhauReversoRelatorioJson estatisticasPorMesReferenciaDiretorio = estatisticasPorMesDiretorio.get(mesReferencia);
            estatisticasPorMesReferenciaDiretorio = estatisticasPorMesReferenciaDiretorio != null ? estatisticasPorMesReferenciaDiretorio : new BacalhauReversoRelatorioJson();
            String estatisticasPorMesDiretorioJson = DummyUtils.objectToJson(estatisticasPorMesReferenciaDiretorio);

            BacalhauReversoRelatorioJson estatisticasPorMesReferenciaDigitalizacao = estatisticasPorMesDigitalizacao.get(mesReferencia);
            estatisticasPorMesReferenciaDigitalizacao = estatisticasPorMesReferenciaDigitalizacao != null ? estatisticasPorMesReferenciaDigitalizacao : new BacalhauReversoRelatorioJson();
            String estatisticasPorMesDigitalizacaoJson = DummyUtils.objectToJson(estatisticasPorMesReferenciaDigitalizacao);

            BacalhauReversoRelatorioJson estatisticasPorMesReferenciaUltimaAlteracao = estatisticasPorMesUltimaAlteracao.get(mesReferencia);
            estatisticasPorMesReferenciaUltimaAlteracao = estatisticasPorMesReferenciaUltimaAlteracao != null ? estatisticasPorMesReferenciaUltimaAlteracao : new BacalhauReversoRelatorioJson();
            String estatisticasPorMesUltimaAlteracaoJson = DummyUtils.objectToJson(estatisticasPorMesReferenciaUltimaAlteracao);

            BacalhauReversoRelatorioJson estatisticasPorMesReferenciaUltimoAcesso = estatisticasPorMesUltimoAcesso.get(mesReferencia);
            estatisticasPorMesReferenciaUltimoAcesso = estatisticasPorMesReferenciaUltimoAcesso != null ? estatisticasPorMesReferenciaUltimoAcesso : new BacalhauReversoRelatorioJson();
            String estatisticasPorMesUltimoAcessoJson = DummyUtils.objectToJson(estatisticasPorMesReferenciaUltimoAcesso);

            BacalhauReversoRelatorio brr = new BacalhauReversoRelatorio();
            brr.setDataExecucao(dataExecucao);
            brr.setMesReferencia(mesReferencia);
            brr.setEstatisticasPorDiretorio(estatisticasPorMesDiretorioJson);
            brr.setEstatisticasPorDigitalizacao(estatisticasPorMesDigitalizacaoJson);
            brr.setEstatisticasPorUltimaAlteracao(estatisticasPorMesUltimaAlteracaoJson);
            brr.setEstatisticasPorUltimoAcesso(estatisticasPorMesUltimoAcessoJson);

            bacalhauReversoRelatorioRepository.saveOrUpdate(brr);
        }
    }

    private Map<Date, BacalhauReversoRelatorioJson> getEstatisticas(MultiValueMap<String, BacalhauReversoRelatorioBuilder.FileAuxVO> dados) {

        Map<Date, BacalhauReversoRelatorioJson> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<BacalhauReversoRelatorioBuilder.FileAuxVO>> entry : dados.entrySet()) {

            String mesStr = entry.getKey();
            List<BacalhauReversoRelatorioBuilder.FileAuxVO> fileAuxVOList = entry.getValue();

            Map<String, AtomicInteger> countExtensaoMap = new TreeMap<>();
            Set<String> processos = new HashSet<>();
            long tamanhoTotal = 0;
            long tamanhoMax = 0;
            int count = 0;

            Map<String, AtomicInteger> countExtensaoMapInexistente = new TreeMap<>();
            Set<String> processosInexistente = new HashSet<>();
            long tamanhoTotalInexistente = 0;
            long tamanhoMaxInexistente = 0;
            int countInexistente = 0;

            for (BacalhauReversoRelatorioBuilder.FileAuxVO auxVO : fileAuxVOList) {

                boolean temCorrespondencia = auxVO.temCorrespondencia;

                if(temCorrespondencia) {
                    count++;
                } else {
                    countInexistente++;
                }

                String processoId = auxVO.processoId;
                if(temCorrespondencia) {
                    processos.add(processoId);
                } else {
                    processosInexistente.add(processoId);
                }

                Long size = auxVO.size;
                if(temCorrespondencia) {
                    tamanhoTotal += size;
                    tamanhoMax = Math.max(tamanhoMax, size);
                } else {
                    tamanhoTotalInexistente += size;
                    tamanhoMaxInexistente = Math.max(tamanhoMaxInexistente, size);
                }

                String extensao = auxVO.extensao;
                if(temCorrespondencia) {
                    AtomicInteger countExtensao = countExtensaoMap.get(extensao);
                    countExtensao = countExtensao != null ? countExtensao : new AtomicInteger();
                    countExtensao.incrementAndGet();
                    countExtensaoMap.put(extensao, countExtensao);
                } else {
                    AtomicInteger countExtensao = countExtensaoMapInexistente.get(extensao);
                    countExtensao = countExtensao != null ? countExtensao : new AtomicInteger();
                    countExtensao.incrementAndGet();
                    countExtensaoMapInexistente.put(extensao, countExtensao);
                }
            }

            BacalhauReversoRelatorioJson json = new BacalhauReversoRelatorioJson();

            json.setProcessos(processos.size());
            json.setArquivos(count);
            json.setTamanhoTotal(tamanhoTotal);
            long tamanhoMedio = count > 0 ? tamanhoTotal / count : 0;
            json.setTamanhoMedio(tamanhoMedio);
            json.setTamanhoMax(tamanhoMax);
            json.setCountExtensao(countExtensaoMap);

            BacalhauReversoRelatorioJson jsonInexistente = new BacalhauReversoRelatorioJson();
            jsonInexistente.setArquivos(countInexistente);
            jsonInexistente.setProcessos(processosInexistente.size());
            jsonInexistente.setTamanhoTotal(tamanhoTotalInexistente);
            long tamanhoMedioInexistente = countInexistente > 0 ? tamanhoTotalInexistente / countInexistente : 0;
            jsonInexistente.setTamanhoMedio(tamanhoMedioInexistente);
            jsonInexistente.setTamanhoMax(tamanhoMaxInexistente);
            jsonInexistente.setCountExtensao( countExtensaoMapInexistente);
            json.setInexistentes(jsonInexistente);

            Date mes = DummyUtils.parse(mesStr, "yyyy/MM");
            result.put(mes, json);
        }

        return result;
    }

    public static class BacalhauReversoRelatorioJson {

        private int arquivos = 0;
        private int processos = 0;
        private long tamanhoTotal = 0;
        private long tamanhoMedio = 0;
        private long tamanhoMax = 0;
        private Map<String, ? extends Object> countExtensao;
        private BacalhauReversoRelatorioJson inexistentes;

        public int getProcessos() {
            return processos;
        }

        public void setProcessos(int processos) {
            this.processos = processos;
        }

        public long getTamanhoTotal() {
            return tamanhoTotal;
        }

        public void setTamanhoTotal(long tamanhoTotal) {
            this.tamanhoTotal = tamanhoTotal;
        }

        public long getTamanhoMedio() {
            return tamanhoMedio;
        }

        public void setTamanhoMedio(long tamanhoMedio) {
            this.tamanhoMedio = tamanhoMedio;
        }

        public long getTamanhoMax() {
            return tamanhoMax;
        }

        public void setTamanhoMax(long tamanhoMax) {
            this.tamanhoMax = tamanhoMax;
        }

        public Map<String, ? extends Object> getCountExtensao() {
            return countExtensao;
        }

        public void setCountExtensao(Map<String, ? extends Object> countExtensao) {
            this.countExtensao = countExtensao;
        }

        public BacalhauReversoRelatorioJson getInexistentes() {
            return inexistentes;
        }

        public void setInexistentes(BacalhauReversoRelatorioJson inexistentes) {
            this.inexistentes = inexistentes;
        }

        public void setArquivos(int arquivos) {
            this.arquivos = arquivos;
        }

        public int getArquivos() {
            return arquivos;
        }
    }

    public static class BacalhauReversoRelatorioBuilder {

        private String currentDir;
        private MultiValueMap<String, FileAuxVO> mesDir = new LinkedMultiValueMap<>();
        private MultiValueMap<String, FileAuxVO> mesDigitalizacao = new LinkedMultiValueMap<>();
        private MultiValueMap<String, FileAuxVO> mesDataUltimaAlteracao = new LinkedMultiValueMap<>();
        private MultiValueMap<String, FileAuxVO> mesDataUltimoAcesso = new LinkedMultiValueMap<>();

        public MultiValueMap<String, FileAuxVO> getMesDir() {
            return mesDir;
        }

        public MultiValueMap<String, FileAuxVO> getMesDigitalizacao() {
            return mesDigitalizacao;
        }

        public MultiValueMap<String, FileAuxVO> getMesDataUltimaAlteracao() {
            return mesDataUltimaAlteracao;
        }

        public MultiValueMap<String, FileAuxVO> getMesDataUltimoAcesso() {
            return mesDataUltimoAcesso;
        }

        public void addDir(String dir) {
            this.currentDir = dir;
        }

        public void addFile(String processoId, Date dataDigitalizacao, Date dataUltimaAlteracao, Date dataUltimoAcesso, Long size, String extensao, boolean temCorrespondencia) {
            this.mesDir.add(currentDir, new FileAuxVO(processoId, size, extensao, temCorrespondencia));

            String mesDigitalizacao = DummyUtils.format(dataDigitalizacao, "yyyy/MM");
            this.mesDigitalizacao.add(mesDigitalizacao, new FileAuxVO(processoId, size, extensao, temCorrespondencia));

            String mesDataUltimaAlteracao = DummyUtils.format(dataUltimaAlteracao, "yyyy/MM");
            this.mesDataUltimaAlteracao.add(mesDataUltimaAlteracao, new FileAuxVO(processoId, size, extensao, temCorrespondencia));

            String mesDataUltimoAcesso = DummyUtils.format(dataUltimoAcesso, "yyyy/MM");
            this.mesDataUltimoAcesso.add(mesDataUltimoAcesso, new FileAuxVO(processoId, size, extensao, temCorrespondencia));
        }

        public static class FileAuxVO {

            private final String processoId;
            private final Long size;
            private final String extensao;
            private final boolean temCorrespondencia;

            public FileAuxVO(String processoId, Long size, String extensao, boolean temCorrespondencia) {
                this.processoId = processoId;
                this.size = size;
                this.extensao = extensao;
                this.temCorrespondencia = temCorrespondencia;
            }
        }
    }

    public void registrarEstatisticas(BacalhauReversoRelatorioService.BacalhauReversoRelatorioBuilder builder, File imagemFile, boolean temCorrespondencia) throws IOException {

        String fileName = imagemFile.getName();
        String parent = imagemFile.getParent();
        Path path = FileSystems.getDefault().getPath(parent, fileName);

        int indexUnderline = fileName.indexOf("_");
        int indexDot = fileName.indexOf(".");
        String processoId = fileName.substring(0, indexUnderline);
        String imagemId = fileName.substring(indexUnderline + 1, indexDot);
        String extensao = fileName.substring(indexDot + 1);

        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

        FileTime creationTimeFT = attr.creationTime();
        Date creationTime = creationTimeFT != null ? new Date(creationTimeFT.toMillis()) : null;
        FileTime lastModifiedTimeFT = attr.lastModifiedTime();
        Date lastModifiedTime = lastModifiedTimeFT != null ? new Date(lastModifiedTimeFT.toMillis()) : null;
        FileTime lastAccessTimeFT = attr.lastAccessTime();
        Date lastAccessTime = lastAccessTimeFT != null ? new Date(lastAccessTimeFT.toMillis()) : null;
        Long size = attr.size();
        builder.addFile(processoId, creationTime, lastModifiedTime, lastAccessTime, size, extensao, temCorrespondencia);
    }
}
