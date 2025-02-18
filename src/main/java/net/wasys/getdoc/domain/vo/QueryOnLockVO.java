package net.wasys.getdoc.domain.vo;

import java.math.BigDecimal;

public class QueryOnLockVO {

    private Integer blockedPid;
    private String blockedUser;
    private BigDecimal blockedExecucaoSeg;
    private String blockedQuery;
    private Integer blockingPid;
    private String blockingUser;
    private BigDecimal blockingExecucaoSeg;
    private String blockingQuery;

    public QueryOnLockVO (Integer blockedPid, String blockedUser, BigDecimal blockedExecucaoSeg, String blockedQuery, Integer blockingPid, String blockingUser, BigDecimal blockingExecucaoSeg, String blockingQuery) {
        this.blockedPid = blockedPid;
        this.blockedUser = blockedUser;
        this.blockedExecucaoSeg = blockedExecucaoSeg;
        this.blockedQuery = blockedQuery;
        this.blockingPid = blockingPid;
        this.blockingUser = blockingUser;
        this.blockingExecucaoSeg = blockingExecucaoSeg;
        this.blockingQuery = blockingQuery;
    }

    public Integer getBlockedPid() { return blockedPid; }

    public String getBlockedUser() { return blockedUser; }

    public BigDecimal getBlockedExecucaoSeg() { return blockedExecucaoSeg; }

    public String getBlockedQuery() { return blockedQuery; }

    public Integer getBlockingPid() { return blockingPid; }

    public String getBlockingUser() { return blockingUser; }

    public BigDecimal getBlockingExecucaoSeg() { return blockingExecucaoSeg; }

    public String getBlockingQuery() { return blockingQuery; }
}
