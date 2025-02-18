package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.Etapa;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.util.other.HorasUteisCalculator;

import java.math.BigDecimal;
import java.util.*;

public class EtapaVO {

    private Date inicio;
    private Date fim;
    private Date prazoLimite;
    private HorasUteisCalculator huc;
    private BigDecimal sla;
    private int tratativasNaoContada = 0;
    private boolean slaAtendido;
    private Long processoLogIdInicial;
    private Map<Long, String> usuariosMap = new LinkedHashMap<>();
    private Etapa etapa;
    private ProcessoLog processoLogCorrente;

    private Situacao situacaoDe;
    private Situacao situacaoPara;

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public Date getPrazoLimite() {
        return prazoLimite;
    }

    public void setPrazoLimite(Date prazoLimite) {
        this.prazoLimite = prazoLimite;
    }

    public HorasUteisCalculator getHuc() {
        return huc;
    }

    public void setHuc(HorasUteisCalculator huc) {
        this.huc = huc;
    }

    public BigDecimal getSla() {
        return sla;
    }

    public void setSla(BigDecimal sla) {
        this.sla = sla;
    }

    public void addTimeSla(Date date) {
        sla = huc.getHoras(inicio, date);
        setFim(date);
    }

    public void addTratativasNaoContada() {
        tratativasNaoContada += 1;
     }

    public int getTratativasNaoContada() {
        return tratativasNaoContada;
    }

    public boolean getSlaAtendido() {
        return slaAtendido;
    }

    public void setSlaAtendido(boolean slaAtendido) {
        this.slaAtendido = slaAtendido;
    }

    public Long getProcessoLogIdInicial() {
        return processoLogIdInicial;
    }

    public void setProcessoLogIdInicial(Long processoLogIdInicial) {
        this.processoLogIdInicial = processoLogIdInicial;
    }

    public Map<Long, String> getUsuariosMap() {
        return usuariosMap;
    }

    public void setUsuariosMap(Map<Long, String> usuariosMap) {
        this.usuariosMap = usuariosMap;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    public ProcessoLog getProcessoLogCorrente() {
        return processoLogCorrente;
    }

    public void setProcessoLogCorrente(ProcessoLog processoLogCorrente) {
        this.processoLogCorrente = processoLogCorrente;
    }

    public Situacao getSituacaoDe() {
        return situacaoDe;
    }

    public void setSituacaoDe(Situacao situacaoDe) {
        this.situacaoDe = situacaoDe;
    }

    public Situacao getSituacaoPara() {
        return situacaoPara;
    }

    public void setSituacaoPara(Situacao situacaoPara) {
        this.situacaoPara = situacaoPara;
    }
}
