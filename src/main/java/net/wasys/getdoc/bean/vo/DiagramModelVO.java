package net.wasys.getdoc.bean.vo;

import java.util.List;

public class DiagramModelVO {

	private String classe;
	private List<NodeDataArrayVO> nodeDataArray;
	private List<LinkDataArrayVO> linkDataArray;

	public DiagramModelVO() {
		super();
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public List<NodeDataArrayVO> getNodeDataArray() {
		return nodeDataArray;
	}

	public void setNodeDataArray(List<NodeDataArrayVO> nodeDataArray) {
		this.nodeDataArray = nodeDataArray;
	}

	public List<LinkDataArrayVO> getLinkDataArray() {
		return linkDataArray;
	}

	public void setLinkDataArray(List<LinkDataArrayVO> linkDataArray) {
		this.linkDataArray = linkDataArray;
	}
}
