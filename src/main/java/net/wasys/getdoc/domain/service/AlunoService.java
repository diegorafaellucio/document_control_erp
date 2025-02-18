package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Aluno;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.Estado;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.repository.AlunoRepository;
import net.wasys.getdoc.domain.vo.ConsultaInscricoesVO;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.TaxonomiaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AlunoService {

    @Autowired private LogAlteracaoService logAlteracaoService;
    @Autowired private SessionFactory sessionFactory;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private AlunoRepository alunoRepository;

    public Aluno get(Long id) {
        return alunoRepository.get(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(Aluno aluno, Usuario usuario) throws MessageKeyException {

        TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(aluno);

        if (TipoAlteracao.CRIACAO.equals(tipoAlteracao)) {
            aluno.setDataCadastro(new Date());
        }

        try {
            alunoRepository.saveOrUpdate(aluno);
        }
        catch (RuntimeException e) {
            HibernateRepository.verifyConstrantViolation(e);
            throw e;
        }

        logAlteracaoService.registrarAlteracao(aluno, usuario, tipoAlteracao);
    }

    public int countByFiltro(AlunoFiltro filtro) {
        return alunoRepository.countByFiltro(filtro);
    }

    public List<Aluno> findByFiltro(AlunoFiltro filtro, Integer inicio, Integer max) {
        return alunoRepository.findByFiltro(filtro, inicio, max);
    }

    @Transactional(rollbackFor = Exception.class)
    public void excluir(Long alunoId, Usuario usuario) throws MessageKeyException {

        Aluno aluno = get(alunoId);

        logAlteracaoService.registrarAlteracao(aluno, usuario, TipoAlteracao.EXCLUSAO);

        try {
            alunoRepository.deleteById(alunoId);
        } catch (RuntimeException e) {
            HibernateRepository.verifyConstrantViolation(e);
            throw e;
        }
    }
    public List<Aluno> findAlunoPorNomeComProcessoByFiltro(TaxonomiaFiltro filtro) {
        return alunoRepository.findAlunoPorNomeComProcessoByFiltro(filtro);
    }

    public Aluno getByCpfNome(String cpf, String nome) {
        return alunoRepository.getByCpfNome(cpf, nome);
    }

    public Aluno getByCpfNomeMae(String cpf, String nome, String nomeMae) {
        return alunoRepository.getByCpfNomeMae(cpf, nome, nomeMae);
    }

    public Aluno saveOrUpdateAluno(ConsultaInscricoesVO consultaVO, Usuario usuario) throws Exception {

        Aluno aluno = null;

        boolean atualizar = false;
        String cpfCandidato = DummyUtils.getCpf(consultaVO.getCpf());
        String nomeCandidato = consultaVO.getNomCandidato();

        if(cpfCandidato != null) {
            aluno = alunoRepository.getByCpfNome(cpfCandidato, nomeCandidato);

            if(aluno == null){
                AlunoFiltro alunoFiltro = new AlunoFiltro();
                alunoFiltro.setCpf(cpfCandidato);
                List<Aluno> alunos = alunoRepository.findByFiltro(alunoFiltro , null, null);
                if(!alunos.isEmpty()){
                    aluno = alunos.get(0);
                }
            }
        } else {
            AlunoFiltro alunoFiltro = new AlunoFiltro();
            alunoFiltro.setEstrangeiro(true);
            alunoFiltro.setNome(nomeCandidato);
            List<Aluno> alunos = alunoRepository.findByFiltro(alunoFiltro , null, null);
            if(!alunos.isEmpty()){
                aluno = alunos.get(0);
            }
        }

        if(aluno == null) {
            aluno = new Aluno();
        }
        else {
            Hibernate.initialize(aluno);
            Session session = sessionFactory.getCurrentSession();
            session.evict(aluno);
            atualizar = true;
        }

        Aluno aluno2 = aluno;
        boolean finalAtualizar = atualizar;

        TransactionWrapper tw = new TransactionWrapper(applicationContext);
        tw.setRunnable(() -> {
            String numIdentidade = consultaVO.getNumIdentidade();
            String orgaoEmissor = consultaVO.getSglOrgaoEmissao();
            Estado estado = DummyUtils.getEnumValue(Estado.class.getSimpleName(), consultaVO.getSglUfRg());
            String nomeMae = consultaVO.getNomMae();
            String nomePai = consultaVO.getNomPai();
            String passaporte = consultaVO.getTxtIdPassaporte();
            Date dataEmissao = consultaVO.getDtEmissaoIdent();
            String nomCadidato = consultaVO.getNomCandidato();

            if(!finalAtualizar) {
                String cpf = DummyUtils.getCpf(consultaVO.getCpf());
                aluno2.setCpf(cpf);
            }

            aluno2.setIdentidade(numIdentidade);
            aluno2.setOrgaoEmissor(orgaoEmissor);
            aluno2.setUfIdentidade(estado);
            aluno2.setMae(nomeMae);
            aluno2.setPai(nomePai);
            aluno2.setPassaporte(passaporte);
            aluno2.setDataEmissao(dataEmissao);
            aluno2.setNome(nomCadidato);
            saveOrUpdate(aluno2, usuario);
        });
        tw.runNewThread();
        tw.throwException();

        return aluno2;
    }


}
