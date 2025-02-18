package net.wasys.getdoc.domain.service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.SunUnsafeReflectionProvider;
import com.thoughtworks.xstream.core.ReferenceByIdMarshallingStrategy;
import com.thoughtworks.xstream.mapper.Mapper;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.TipoAlteracao;
import net.wasys.getdoc.domain.enumeration.TipoRegistro;
import net.wasys.getdoc.domain.repository.LogAlteracaoRepository;
import net.wasys.getdoc.domain.vo.filtro.LogAlteracaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.Entity;
import org.hibernate.Hibernate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class LogAlteracaoService {

    @Autowired private LogAlteracaoRepository logAlteracaoRepository;

    public LogAlteracao get(Long id) {
        return logAlteracaoRepository.get(id);
    }

    @Transactional(rollbackFor=Exception.class)
    public void registrarAlteracao(Entity entity, Usuario usuario, TipoAlteracao tipoAlteracao) {

        String dados = getDados(entity);

        TipoRegistro tipoRegistro = TipoRegistro.getTipoRegistro(entity);
        Long registroId = entity.getId();

        LogAlteracao log = new LogAlteracao();
        log.setTipoAlteracao(tipoAlteracao);
        log.setTipoRegistro(tipoRegistro);
        log.setUsuario(usuario);
        log.setRegistroId(registroId);
        log.setDados(dados);

        logAlteracaoRepository.saveOrUpdate(log);
    }

    private String getDados(Entity entity) {

        XStream xStream = new XStream();

        xStream.setMarshallingStrategy(new ReferenceByIdMarshallingStrategy());

        if(entity instanceof Usuario) {

            Usuario usuario = (Usuario) entity;
            Hibernate.initialize(usuario);

            Set<Role> roles = usuario.getRoles();
            Hibernate.initialize(roles);
            for (Role role : roles) {
                Hibernate.initialize(role);
            }

            xStream.omitField(Usuario.class, "senha");
            xStream.omitField(Usuario.class, "senhasAnteriores");
            xStream.omitField(Usuario.class, "dataExpiracaoBloqueio");
            xStream.omitField(Usuario.class, "logoffListener");
            xStream.omitField(Role.class, "usuario");
        }
        else if(entity instanceof TipoCampoGrupo) {

            xStream.omitField(GrupoAbstract.class, "campos");
            xStream.omitField(TipoProcesso.class, "horasPrazo");
            xStream.omitField(TipoProcesso.class, "ativo");
            xStream.omitField(TipoProcesso.class, "dica");
            xStream.omitField(TipoProcesso.class, "preencherViaOcr");
            xStream.omitField(TipoProcesso.class, "situacaoInicial");
            xStream.omitField(TipoProcesso.class, "permissoes");
        }
        else if(entity instanceof TipoCampo) {

            TipoCampo tipoCampo = (TipoCampo) entity;
            TipoCampoGrupo grupo = tipoCampo.getGrupo();
            Hibernate.initialize(grupo);
            TipoProcesso tipoProcesso = grupo.getTipoProcesso();
            Hibernate.initialize(tipoProcesso);
            xStream.omitField(GrupoAbstract.class, "campos");
            xStream.omitField(GrupoAbstract.class, "criacaoProcesso");
            xStream.omitField(GrupoAbstract.class, "ordem");
            xStream.omitField(GrupoAbstract.class, "abertoPadrao");
            xStream.omitField(TipoProcesso.class, "horasPrazo");
            xStream.omitField(TipoProcesso.class, "ativo");
            xStream.omitField(TipoProcesso.class, "dica");
            xStream.omitField(TipoProcesso.class, "preencherViaOcr");
            xStream.omitField(TipoProcesso.class, "situacaoInicial");
            xStream.omitField(TipoProcesso.class, "permissoes");
        }
        else if(entity instanceof SubRegra) {

            SubRegra subRegra = (SubRegra) entity;
            Hibernate.initialize(subRegra);
            List<DeparaParam> deparaParams = subRegra.getDeparaParams();
            Hibernate.initialize(deparaParams);
            xStream.omitField(Regra.class, "tiposProcessos");
            xStream.omitField(Regra.class, "ativa");
            xStream.omitField(Regra.class, "dataAlteracao");
            xStream.omitField(Regra.class, "descricao");
            xStream.omitField(BaseInterna.class, "ativa");
            xStream.omitField(BaseInterna.class, "descricao");
            xStream.omitField(BaseInterna.class, "colunasUnicidade");
            xStream.omitField(SubRegra.class, "linha");
        }

        Mapper mapper = xStream.getMapper();
        SunUnsafeReflectionProvider reflectionProvider = new SunUnsafeReflectionProvider();
        xStream.registerConverter(new ReflectionConverter(mapper, reflectionProvider), XStream.PRIORITY_VERY_LOW);

        String xml = xStream.toXML(entity);

        return xml;
    }

    public List<LogAlteracao> findByFiltro(LogAlteracaoFiltro filtro, Integer inicio, Integer max) {
        return logAlteracaoRepository.findByFiltro(filtro, inicio, max);
    }

    public int countByFiltro(LogAlteracaoFiltro filtro) {
        return logAlteracaoRepository.countByFiltro(filtro);
    }

    public LogAlteracao getPrevio(LogAlteracao log) {
        return logAlteracaoRepository.getPrevio(log);
    }

    public LogAlteracao getProximo(LogAlteracao log) {
        return logAlteracaoRepository.getProximo(log);
    }
}
