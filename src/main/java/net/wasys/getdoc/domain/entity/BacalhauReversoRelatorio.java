package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="BACALHAU_REVERSO_RELATORIO")
public class BacalhauReversoRelatorio extends net.wasys.util.ddd.Entity {

    private Long id;
    private Date dataExecucao;
    private Date mesReferencia;
    private String estatisticasPorDiretorio;
    private String estatisticasPorDigitalizacao;
    private String estatisticasPorUltimaAlteracao;
    private String estatisticasPorUltimoAcesso;

    @Id
    @Override
    @Column(name="ID", unique=true, nullable=false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="MES_REFERENCIA")
    @Temporal(TemporalType.DATE)
    public Date getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(Date mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    @Column(name="DATA_EXECUCAO")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDataExecucao() {
        return dataExecucao;
    }

    public void setDataExecucao(Date dataExecucao) {
        this.dataExecucao = dataExecucao;
    }

    @Column(name="ESTATISTICAS_POR_DIRETORIO")
    public String getEstatisticasPorDiretorio() {
        return estatisticasPorDiretorio;
    }

    public void setEstatisticasPorDiretorio(String estatisticasPorMesDiretorio) {
        this.estatisticasPorDiretorio = estatisticasPorMesDiretorio;
    }

    @Column(name="ESTATISTICAS_POR_DIGITALIZACAO")
    public String getEstatisticasPorDigitalizacao() {
        return estatisticasPorDigitalizacao;
    }

    public void setEstatisticasPorDigitalizacao(String estatisticasPorMesCriacao) {
        this.estatisticasPorDigitalizacao = estatisticasPorMesCriacao;
    }

    @Column(name="ESTATISTICAS_POR_ULTIMA_ALTERACAO")
    public String getEstatisticasPorUltimaAlteracao() {
        return estatisticasPorUltimaAlteracao;
    }

    public void setEstatisticasPorUltimaAlteracao(String estatisticasPorMesUltimaAlteracao) {
        this.estatisticasPorUltimaAlteracao = estatisticasPorMesUltimaAlteracao;
    }

    @Column(name="ESTATISTICAS_POR_ULTIMO_ACESSO")
    public String getEstatisticasPorUltimoAcesso() {
        return estatisticasPorUltimoAcesso;
    }

    public void setEstatisticasPorUltimoAcesso(String estatisticasPorMesUltimoAcesso) {
        this.estatisticasPorUltimoAcesso = estatisticasPorMesUltimoAcesso;
    }
}
