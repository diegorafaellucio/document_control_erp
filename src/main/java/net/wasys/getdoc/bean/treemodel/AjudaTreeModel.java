package net.wasys.getdoc.bean.treemodel;

import java.util.Arrays;
import java.util.List;

import net.wasys.getdoc.domain.enumeration.Resposta;
import org.apache.commons.collections.CollectionUtils;
import org.omnifaces.model.tree.ListTreeModel;
import org.omnifaces.model.tree.TreeModel;

import net.wasys.getdoc.domain.entity.Ajuda;

public class AjudaTreeModel extends ListTreeModel<Ajuda> {

	private Resposta folha;

	public boolean existeInferior(Resposta resposta) {
		Ajuda ajuda = getData();
		List<Ajuda> inferiores = ajuda.getInferiores();
		if (CollectionUtils.isNotEmpty(inferiores)) {
			for (Ajuda inferior : inferiores) {
				if (resposta.equals(inferior.getResposta())) {
					folha = resposta;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<TreeModel<Ajuda>> getChildren() {
		List<TreeModel<Ajuda>> children = super.getChildren();
		if (folha != null) {
			for (TreeModel<Ajuda> child : children) {
				Ajuda ajuda = child.getData();
				if (folha.equals(ajuda.getResposta())) {
					children = Arrays.asList(child);
					folha = null;
					break;
				}
			}
		}
		return children;
	}
}
