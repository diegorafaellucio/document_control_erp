package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.ProcessoVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RegraFiltro;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.exception.TipoEvidenciaRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoEvidencia;
import net.wasys.getdoc.rest.request.vo.RequestConcluirProcessos;
import net.wasys.getdoc.rest.request.vo.RequestFiltroFila;
import net.wasys.getdoc.rest.request.vo.RequestTrocarAnalista;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class TipoEvidenciaServiceRest extends SuperServiceRest{

    @Autowired
    private TipoEvidenciaService tipoEvidenciaService;

    public List<TipoEvidenciaResponse> getAtivas(Usuario usuario) {
        List<TipoEvidencia> tipoEvidencias = tipoEvidencias = tipoEvidenciaService.findAtivas();
        List<TipoEvidenciaResponse> list = parser(tipoEvidencias);
        return list;
    }

    public List<TipoEvidenciaResponse> getAll(Usuario usuario, int min, int max) {
        List<TipoEvidencia> all = tipoEvidenciaService.findAll(min, max);
        List<TipoEvidenciaResponse> parser = parser(all);
        return parser;
    }

    private List<TipoEvidenciaResponse> parser(List<TipoEvidencia> ativas) {
        List<TipoEvidenciaResponse> list = null;
        if(ativas != null && ativas.size() > 0){
            list = new ArrayList<>();
            for(TipoEvidencia te : ativas){
                list.add(new TipoEvidenciaResponse(te));
            }
        }
        return list;
    }

    public TipoEvidenciaResponse detalhar(Usuario usuario, Long id) {
        TipoEvidencia tipoEvidencia = tipoEvidenciaService.get(id);
        if(tipoEvidencia == null){
            throw new TipoEvidenciaRestException("tipoevidencia.nao.localizado.id", id);
        }
        return new TipoEvidenciaResponse(tipoEvidencia);
    }
    public boolean excluir(Usuario usuario, Long id) {
        TipoEvidencia tipoEvidencia = tipoEvidenciaService.get(id);
        if(tipoEvidencia == null){
            throw new TipoEvidenciaRestException("tipoevidencia.nao.localizado.id", id);
        }
        tipoEvidenciaService.excluir(tipoEvidencia.getId(), usuario);
        return true;
    }

    public TipoEvidenciaResponse cadastrar(Usuario usuario, RequestCadastroTipoEvidencia requestCadastroTipoEvidencia) {
        TipoEvidencia tipoEvidencia = new TipoEvidencia();
        return saveOrUpdate(usuario, requestCadastroTipoEvidencia, tipoEvidencia);
    }

    public TipoEvidenciaResponse editar(Usuario usuario, Long id, RequestCadastroTipoEvidencia requestCadastroTipoEvidencia) {
        TipoEvidencia tipoEvidencia = tipoEvidenciaService.get(id);
        if(tipoEvidencia == null){
            throw new TipoEvidenciaRestException("tipoevidencia.nao.localizado.id", id);
        }
        return saveOrUpdate(usuario, requestCadastroTipoEvidencia, tipoEvidencia);
    }

    private TipoEvidenciaResponse saveOrUpdate(Usuario usuario, RequestCadastroTipoEvidencia requestCadastroTipoEvidencia, TipoEvidencia tipoEvidencia) {
        tipoEvidencia.setAtivo(requestCadastroTipoEvidencia.isAtivo());
        tipoEvidencia.setDescricao(requestCadastroTipoEvidencia.getDescricao());

        //FIXME Adicionar selecao dos subperfis que poderam visualizar as evidências no acompanhamento.

        tipoEvidenciaService.saveOrUpdate(tipoEvidencia, usuario);
        return new TipoEvidenciaResponse(tipoEvidencia);
    }
}