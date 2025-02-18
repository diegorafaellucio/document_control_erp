package net.wasys.util.rewrite;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("rewrite")
public class RewriteTag {

	@XStreamAsAttribute private List<JoinTag> joins;

	public List<JoinTag> getJoins() {
		return joins;
	}

	public void setJoins(List<JoinTag> joins) {
		this.joins = joins;
	}
}
