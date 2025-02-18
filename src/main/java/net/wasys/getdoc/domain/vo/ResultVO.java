package net.wasys.getdoc.domain.vo;

public class ResultVO {

	private int inserts;
	private int updates;
	private int deletes;
	private int cancelados;

	public void addInsert() {
		inserts++;
	}

	public void addUpdate() {
		updates++;
	}

	public void addCancelado() {
		cancelados++;
	}

	public int getInserts() {
		return inserts;
	}

	public int getUpdates() {
		return updates;
	}

	public int getDeletes() {
		return deletes;
	}

	public int getCancelados() {
		return cancelados;
	}

	public void addInserts(int insert) {
		this.inserts =+ insert;
	}

	public void addUpdates(int updates) {
		this.updates += updates;
	}

	public void addDeletes(int deletes) {
		this.deletes += deletes;
	}

	@Override
	public String toString() {
		return "Inserts: " + getInserts() + ". Updates: " + getUpdates() + ". Deletes: " + getDeletes();
	}
}