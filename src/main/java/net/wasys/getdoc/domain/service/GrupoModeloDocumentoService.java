package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.GrupoModeloDocumento;
import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.ModeloDocumentoGrupoModeloDocumento;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.GrupoModeloDocumentoRepository;
import net.wasys.getdoc.domain.vo.GrupoModeloDocumentoVO;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GrupoModeloDocumentoService {

	@Autowired
    private LogAlteracaoService logAlteracaoService;
	@Autowired
    private GrupoModeloDocumentoRepository grupoModeloDocumentoRepository;

	public GrupoModeloDocumento get(Long id) {
		return grupoModeloDocumentoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(GrupoModeloDocumento grupoModeloDocumento, Usuario usuario, List<ModeloDocumento> modelosDocumento) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(grupoModeloDocumento);

		grupoModeloDocumento.setDataAlteracao(new Date());
		try {
			grupoModeloDocumentoRepository.saveOrUpdate(grupoModeloDocumento);

			if(modelosDocumento != null) {
				Long modeloDocumentoGrupoId = grupoModeloDocumento.getId();
				List<ModeloDocumento> modelosAtuais = modeloDocumentoGrupoId != null ? grupoModeloDocumentoRepository.findModelosDocumento(modeloDocumentoGrupoId) : new ArrayList();
				for (ModeloDocumento md : modelosDocumento) {
					if(!modelosAtuais.remove(md)) {
						ModeloDocumentoGrupoModeloDocumento modeloDocumentoGrupoModeloDocumento = new ModeloDocumentoGrupoModeloDocumento();
						modeloDocumentoGrupoModeloDocumento.setGrupoModeloDocumento(grupoModeloDocumento);
						modeloDocumentoGrupoModeloDocumento.setModeloDocumento(md);

						grupoModeloDocumentoRepository.saveOrUpdateModeloDocumento(modeloDocumentoGrupoModeloDocumento);
					}
					else {
						ModeloDocumentoGrupoModeloDocumento modeloDocumentoGrupoModeloDocumento = grupoModeloDocumentoRepository.findByModeloDocumentoGrupoAndModeloDocumento(grupoModeloDocumento, md);

						grupoModeloDocumentoRepository.saveOrUpdateModeloDocumento(modeloDocumentoGrupoModeloDocumento);
					}
				}
				for (ModeloDocumento modeloAtual : modelosAtuais) {
					Long tipoDocumentoModeloId = modeloAtual.getId();
					grupoModeloDocumentoRepository.deleteModeloDocumentoById(modeloDocumentoGrupoId, tipoDocumentoModeloId);
				}
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(grupoModeloDocumento, usuario, tipoAlteracao);
	}

	public List<GrupoModeloDocumento> findAll() {
		return grupoModeloDocumentoRepository.findAll();
	}

	public int count() {
		return grupoModeloDocumentoRepository.count();
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long modeloDocumentoId, Usuario usuario) throws MessageKeyException {

		GrupoModeloDocumento grupoModeloDocumento = get(modeloDocumentoId);

		logAlteracaoService.registrarAlteracao(grupoModeloDocumento, usuario, TipoAlteracao.EXCLUSAO);

		try {
			grupoModeloDocumentoRepository.deleteById(modeloDocumentoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}



	public GrupoModeloDocumentoVO createVOBy(GrupoModeloDocumento grupoModeloDocumento) {
		GrupoModeloDocumentoVO vo = new GrupoModeloDocumentoVO();
		vo.setId(grupoModeloDocumento.getId());
		vo.setNome(grupoModeloDocumento.getDescricao());
		vo.setDataAlteracao(grupoModeloDocumento.getDataAlteracao());

		return vo;
	}

	public List<GrupoModeloDocumentoVO> findVOsByProcesso() {

		List<GrupoModeloDocumento> grupoModeloDocumentos = grupoModeloDocumentoRepository.findAll();

		List<GrupoModeloDocumentoVO> list = new ArrayList<>();
		for (GrupoModeloDocumento grupoModeloDocumento : grupoModeloDocumentos) {

			GrupoModeloDocumentoVO vo = createVOBy(grupoModeloDocumento);
			list.add(vo);

		}

		return list;
	}

	public List<ModeloDocumento> findModelosDocumento(Long modeloDocumentoGupoId) {
		return grupoModeloDocumentoRepository.findModelosDocumento(modeloDocumentoGupoId);
	}


	public ModeloDocumentoGrupoModeloDocumento findByTipoDocumentoAndModeloDocumento(GrupoModeloDocumento grupoModeloDocumento, ModeloDocumento modeloDocumento) {
		return grupoModeloDocumentoRepository.findByModeloDocumentoGrupoAndModeloDocumento(grupoModeloDocumento, modeloDocumento);
	}


}
