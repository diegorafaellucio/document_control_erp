package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.CategoriaGrupoModeloDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.CategoriaGrupoDocumentoRepository;
import net.wasys.getdoc.domain.vo.CategoriaGrupoModeloDocumentoVO;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriaGrupoModeloDocumentoService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private CategoriaGrupoDocumentoRepository categoriaGrupoDocumentoRepository;

	public CategoriaGrupoModeloDocumento get(Long id) {
		return categoriaGrupoDocumentoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(CategoriaGrupoModeloDocumento grupoModeloDocumento, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(grupoModeloDocumento);

		try {
			categoriaGrupoDocumentoRepository.saveOrUpdate(grupoModeloDocumento);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(grupoModeloDocumento, usuario, tipoAlteracao);
	}

	public List<CategoriaGrupoModeloDocumento> findAll() {
		return categoriaGrupoDocumentoRepository.findAll();
	}

	public int count() {
		return categoriaGrupoDocumentoRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long modeloDocumentoId, Usuario usuario) throws MessageKeyException {

		CategoriaGrupoModeloDocumento grupoModeloDocumento = get(modeloDocumentoId);

		logAlteracaoService.registrarAlteracao(grupoModeloDocumento, usuario, TipoAlteracao.EXCLUSAO);

		try {
			categoriaGrupoDocumentoRepository.deleteById(modeloDocumentoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}



	public CategoriaGrupoModeloDocumentoVO createVOBy(CategoriaGrupoModeloDocumento grupoModeloDocumento) {


		CategoriaGrupoModeloDocumentoVO vo = new CategoriaGrupoModeloDocumentoVO();
		vo.setId(grupoModeloDocumento.getId());
		vo.setDescricao(grupoModeloDocumento.getDescricao());

		return vo;
	}

	public List<CategoriaGrupoModeloDocumentoVO> findVOsByProcesso() {

		List<CategoriaGrupoModeloDocumento> grupoModeloDocumentos = categoriaGrupoDocumentoRepository.findAll();

		List<CategoriaGrupoModeloDocumentoVO> list = new ArrayList<>();
		for (CategoriaGrupoModeloDocumento categoriaGrupoModeloDocumento : grupoModeloDocumentos) {

			CategoriaGrupoModeloDocumentoVO vo = createVOBy(categoriaGrupoModeloDocumento);
			list.add(vo);

		}

		return list;
	}

}
