package net.wasys.getdoc.domain.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.util.DummyUtils;

public class ComparacaoFacesVO {

	private Imagem imagemBase;
	private List<Imagem> imagens = new ArrayList<>();
	private String imagePath;

	public void setImagemBase(Imagem imagemBase) {
		this.imagemBase = imagemBase;
	}

	public Imagem getImagemBase() {
		return imagemBase;
	}

	public List<Imagem> getImagens() {

		Collections.sort(imagens, new Comparator<Imagem>() {
			@Override
			public int compare(Imagem o1, Imagem o2) {
				BigDecimal sf1 = o1.getSimilaridadeFacial();
				BigDecimal sf2 = o2.getSimilaridadeFacial();
				return DummyUtils.compare(sf2, sf1);
			}
		});
		return imagens;
	}

	public void addImagem(Imagem imagem) {
		imagens.add(imagem);
	}

	public String getCaminhoImagemBase() {

		Imagem imagem = getImagemBase();
		return getCaminhoImagem(imagem);
	}

	public String getCaminhoImagem(Imagem imagem) {

		if(imagem == null) {
			return null;
		}

		String path = Imagem.gerarCaminho(imagePath, imagem, "/");

		String caminhoFacial = imagem.getCaminhoFacial();
		if(caminhoFacial != null) {
			path = path.replace("." + GetdocConstants.EXTENSAO_DEFINICAO_IMAGEM, "-face." + GetdocConstants.EXTENSAO_DEFINICAO_IMAGEM);
		}

		return path;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}
