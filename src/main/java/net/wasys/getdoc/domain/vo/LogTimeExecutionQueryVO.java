package net.wasys.getdoc.domain.vo;

import java.math.BigDecimal;

public class LogTimeExecutionQueryVO {

	private Integer pid;
	private String datname;
	private BigDecimal tempoExecucaoSeg;
	private String clientAddr;
	private String query;

	public LogTimeExecutionQueryVO(Integer pid, String datname, BigDecimal tempoExecucaoSeg, String clientAddr, String query) {
		this.pid = pid;
		this.datname = datname;
		this.tempoExecucaoSeg = tempoExecucaoSeg;
		this.clientAddr = clientAddr;
		this.query = query;
	}

	public Integer getPid() {
		return pid;
	}

	public String getDatname() {
		return datname;
	}

	public BigDecimal getTempoExecucaoSeg() {
		return tempoExecucaoSeg;
	}

	public String getClientAddr() {
		return clientAddr;
	}

	public String getQuery() {
		return query;
	}

}