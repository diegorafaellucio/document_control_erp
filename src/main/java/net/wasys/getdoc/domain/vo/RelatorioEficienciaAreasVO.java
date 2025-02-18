package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.RelatorioGeralSolicitacao;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.HorasUteisCalculator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class RelatorioEficienciaAreasVO {

    private Area area;
    private int qtdeRetrabalho;
    private int qtdeAtraso;
    private Set<RelatorioGeralSolicitacao> solicitacoes = new HashSet<RelatorioGeralSolicitacao>();
    private Set<BigDecimal> tempos = new HashSet<BigDecimal>();
    private Set<BigDecimal> temposAtrasos = new HashSet<BigDecimal>();
    private Integer total;
    private BigDecimal mediaTempoArea;
    private BigDecimal mediaAtraso;
    private HorasUteisCalculator calculator;

    public void add(RelatorioGeralSolicitacao rga) {

        if(!solicitacoes.contains(rga)) {

            solicitacoes.add(rga);

            Integer numRetrabalhos = rga.getNumeroRetrabalhos();
            qtdeRetrabalho += numRetrabalhos;

            Date prazoLimite = rga.getPrazoLimite();
            Date dataFinalizacao = rga.getDataFinalizacao();
            if(dataFinalizacao != null && dataFinalizacao.after(prazoLimite)) {

                qtdeAtraso++;

                BigDecimal horasAtraso = calculator.getHoras(prazoLimite, dataFinalizacao);
                temposAtrasos.add(horasAtraso);
            }

            BigDecimal tempoComArea = rga.getTempoComArea();
            if (tempoComArea == null) {
                tempos.add(BigDecimal.ZERO);
            }
            else {
                tempos.add(tempoComArea);
            }
        }
    }

    public int getTotal() {

        if(total != null) {
            return total;
        }

        return solicitacoes.size();
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Set<BigDecimal> getTempos() {
        return tempos;
    }

    public BigDecimal getMediaAtraso() {
        if(mediaAtraso != null) {
            return mediaAtraso;
        }
        BigDecimal media = DummyUtils.getMedia1(temposAtrasos.size(), temposAtrasos);
        return media;
    }

    public String getMediaAtrasoStr() {
        BigDecimal horasExpediente = calculator.getHorasExpediente();

        BigDecimal mediaAtraso = getMediaAtraso();
        String mediaAtrasoStr = DummyUtils.getHoras(horasExpediente, mediaAtraso, false);
        return mediaAtrasoStr;
    }

    public BigDecimal getMediaTempoArea() {
        if(mediaTempoArea != null) {
            return mediaTempoArea;
        }
        Integer total = getTotal();
        BigDecimal media = DummyUtils.getMedia1(total, tempos);
        return media;
    }

    public void setMediaTempoArea(BigDecimal mediaTempoArea) {
        this.mediaTempoArea = mediaTempoArea;
    }

    public String getMediaTempoAreaStr() {
        BigDecimal horasExpediente = calculator.getHorasExpediente();
        BigDecimal mediaTempoArea = getMediaTempoArea();
        String mediaTempoAreaStr = DummyUtils.getHoras(horasExpediente, mediaTempoArea, false);
        return mediaTempoAreaStr;
    }

    public int getQtdeAtraso() {
        return qtdeAtraso;
    }

    public String getQtdeAtrasoStr() {
        BigDecimal qtdeAtrasoBD = new BigDecimal(getQtdeAtraso());
        double percent = getPercentAtraso();
        return qtdeAtrasoBD + " (" + percent + "%)";
    }

    public double getPercentAtraso() {
        BigDecimal qtdeAtrasoBD = new BigDecimal(getQtdeAtraso());
        BigDecimal totalBD = new BigDecimal(getTotal());
        BigDecimal percent = DummyUtils.getPercent(qtdeAtrasoBD, totalBD, 1);
        return percent.doubleValue();
    }

    public void setQtdeAtraso(int qtdeAtraso) {
        this.qtdeAtraso = qtdeAtraso;
    }

    public int getQtdeRetrabalho() {
        return qtdeRetrabalho;
    }

    public String getQtdeRetrabalhoStr() {
        BigDecimal qtdeRetrabalhoBD = new BigDecimal(getQtdeRetrabalho());
        BigDecimal totalBD = new BigDecimal(getTotal());
        BigDecimal percent = DummyUtils.getPercent(qtdeRetrabalhoBD, totalBD, 0);
        return qtdeRetrabalhoBD + " (" + percent + "%)";
    }

    public void setQtdeRetrabalho(int qtdeRetrabalho) {
        this.qtdeRetrabalho = qtdeRetrabalho;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public void setCalculator(HorasUteisCalculator calculator) {
        this.calculator = calculator;
    }

}
