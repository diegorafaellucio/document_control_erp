package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.ImagemMeta;
import net.wasys.getdoc.domain.repository.ImagemMetaRepository;
import net.wasys.getdoc.domain.vo.filtro.ImagemMetaFiltro;
import net.wasys.util.DummyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ImagemMetaService {

	@Autowired private ImagemMetaRepository imagemMetaRepository;

	public ImagemMeta get(Long id) {
		return imagemMetaRepository.get(id);
	}

	public ImagemMeta getByImagem(Long imagemId) {
		return imagemMetaRepository.getByImagem(imagemId);
	}

	public List<ImagemMeta> findByFiltro(ImagemMetaFiltro filtro) {
		return imagemMetaRepository.findByFiltro(filtro);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(ImagemMeta imagemMeta) {
		Long imagemMetaId = imagemMeta.getId();
		imagemMetaRepository.deleteById(imagemMetaId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(ImagemMeta imagemMeta) {
		imagemMetaRepository.saveOrUpdate(imagemMeta);
	}

	@Transactional(rollbackFor=Exception.class)
	public void updateFullText(Long imagemId, String fullText) {
		imagemMetaRepository.updateFullText(imagemId, fullText);
	}

	@Transactional(rollbackFor = Exception.class)
	public String getFullText(Long imagemId) {
		return imagemMetaRepository.getFullText(imagemId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarInfoImagemMeta(Long imagemId, String caminho) {

		File file = new File(caminho);
		atualizarInfoImagemMeta(imagemId, file);
	}

	@Transactional(rollbackFor=Exception.class)
	public void atualizarInfoImagemMeta(Long imagemId, File file) {

		String name = file.getName();
		String extensao = DummyUtils.getExtensao(name);
		if(GetdocConstants.IMAGEM_EXTENSOES.contains(extensao)) {

			long tamanho = file.length();
			Integer width = null;
			Integer height = null;

			try {
				BufferedImage image = ImageIO.read(file);
				if(image != null) {
					width = image.getWidth();
					height = image.getHeight();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			imagemMetaRepository.atualizarInfoImagemMeta(imagemId, tamanho, width, height);
		}
	}

    public ImagemMeta getPrimeiraImagem(Long documentoId) {
		return imagemMetaRepository.getPrimeiraImagem(documentoId);
    }

	public void atualizarMetadados(Long imagemMetaId, String metaDados) {
		imagemMetaRepository.atualizarMetadados(imagemMetaId, metaDados);
	}
}
