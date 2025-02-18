package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoPermissaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.TipoProcessoVO;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.SituacaoRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoProcesso;
import net.wasys.getdoc.rest.response.vo.ListaTipoProcessoResponse;
import net.wasys.getdoc.rest.response.vo.TipoDocumentoResponse;
import net.wasys.getdoc.rest.response.vo.TipoProcessoResponse;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class TipoProcessoServiceRest extends SuperServiceRest{

    @Autowired
    private TipoProcessoService tipoProcessoService;
    @Autowired
    private SituacaoService situacaoService;
    @Autowired
    private TipoProcessoPermissaoService tipoProcessoPermissaoService;

    public List<TipoProcessoResponse> getInformacaoResumida(Usuario usuario) {
        List<TipoProcessoResponse> tipoProcessoResponseList = new ArrayList<>();

        List<TipoProcesso> listTiposProcesso = tipoProcessoService.findByUsuario(usuario);
        if (listTiposProcesso != null && CollectionUtils.isNotEmpty(listTiposProcesso)) {
            listTiposProcesso.forEach(tipoProcesso -> {
                tipoProcessoResponseList.add(new TipoProcessoResponse(tipoProcesso));
            });
        }
        return tipoProcessoResponseList.size() <= 0 ? null : tipoProcessoResponseList;
    }

    /**
     *
     * @param usuario
     * @param min
     * @param max
     * @return
     */
    public ListaTipoProcessoResponse getLista(Usuario usuario, int min, int max) {
        ListaTipoProcessoResponse listaTipoProcessoResponse = new ListaTipoProcessoResponse();

        int count = tipoProcessoService.count();
        listaTipoProcessoResponse.setCount(count);
        if(count == 0){
            return listaTipoProcessoResponse;
        }

        List<TipoProcessoVO> lista = tipoProcessoService.findAll(min, max, "ID", SortOrder.ASCENDING);
        if (lista != null && CollectionUtils.isNotEmpty(lista)) {
            lista.forEach(tipoProcessoVo -> {
                listaTipoProcessoResponse.add(new TipoProcessoResponse(tipoProcessoVo));
            });
        }
        return listaTipoProcessoResponse.getCount() > 0 ? listaTipoProcessoResponse : null;
    }

    /**
     * Cria um novo tipo de processo.
     * @param usuario
     * @param requestCadastroTipoProcesso
     * @return
     * @throws MessageKeyException
     */
    public TipoProcessoResponse cadastrar(Usuario usuario, RequestCadastroTipoProcesso requestCadastroTipoProcesso) throws DadosObrigatorioRequestException, SituacaoRestException, TipoProcessoRestException {
        validaRequestParameters(requestCadastroTipoProcesso);
       return saveOrUpdate(usuario, requestCadastroTipoProcesso, new TipoProcesso());
    }

    /**
     * Editar os dados de um tipo processo existente.
     * @param usuario
     * @param id
     * @param requestCadastroTipoProcesso
     * @return
     * @throws TipoProcessoRestException
     * @throws DadosObrigatorioRequestException
     * @throws SituacaoRestException
     */
    public TipoProcessoResponse editar(Usuario usuario, Long id, RequestCadastroTipoProcesso requestCadastroTipoProcesso) throws TipoProcessoRestException, DadosObrigatorioRequestException, SituacaoRestException {
        validaRequestParameters(requestCadastroTipoProcesso);

        TipoProcesso tipoProcesso = tipoProcessoService.get(id);
        if(tipoProcesso == null) {
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", id);
        }
        return saveOrUpdate(usuario, requestCadastroTipoProcesso, tipoProcesso);
    }

    /**
     * Centraliza o cadastro ou edição do objeto.
     * @param usuario
     * @param requestCadastroTipoProcesso
     * @param tipoProcesso
     * @return
     * @throws SituacaoRestException
     * @throws TipoProcessoRestException
     */
    private TipoProcessoResponse saveOrUpdate(Usuario usuario, RequestCadastroTipoProcesso requestCadastroTipoProcesso, TipoProcesso tipoProcesso) throws SituacaoRestException, TipoProcessoRestException {
        /**
         * Situação inicial não é obrigatório, mas se veio ID, ela tem que existir no banco de dados.
         */
        Situacao situacaoInicial = null;
        if(requestCadastroTipoProcesso.getSituacaoInicialId() != null){
            situacaoInicial = situacaoService.get(requestCadastroTipoProcesso.getSituacaoInicialId());
            if(situacaoInicial == null) {
                throw new SituacaoRestException("situacao.nao.localizada.com.id", requestCadastroTipoProcesso.getSituacaoInicialId());
            }
        }

        List<String> permissoes = new ArrayList<>();
        Set<PermissaoTP> permissoesSelecionadas = requestCadastroTipoProcesso.getPermissoes();
        permissoesSelecionadas.forEach(permissaoSelec -> {
            permissoes.add(permissaoSelec.name());
        });

        tipoProcesso.setAtivo(requestCadastroTipoProcesso.isAtivo());
        tipoProcesso.setNome(requestCadastroTipoProcesso.getNome());
        tipoProcesso.setHorasPrazo(requestCadastroTipoProcesso.getHorasPrazo());
        tipoProcesso.setDica(requestCadastroTipoProcesso.getDica());
        tipoProcesso.setPreencherViaOcr(requestCadastroTipoProcesso.isPreencherViaOcr());
        tipoProcesso.setSituacaoInicial(situacaoInicial);

        try {
            tipoProcessoService.saveOrUpdate(tipoProcesso, permissoes, usuario);
            return new TipoProcessoResponse(tipoProcesso);

        } catch (MessageKeyException e) {
            throw new TipoProcessoRestException(e.getKey(), e.getArgs());
        }
    }

    /**
     *
     * @param usuario
     * @param id
     * @return
     */
    public boolean excluir(Usuario usuario, Long id) {
        TipoProcesso tipoProcesso = tipoProcessoService.get(id);
        if(tipoProcesso == null) {
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", id);
        }
        tipoProcessoService.excluir(id, usuario);
        return true;
    }

    public TipoProcessoResponse detalhar(Usuario usuario, Long id) {
        TipoProcesso tipoProcesso = tipoProcessoService.get(id);
        if(tipoProcesso == null) {
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", id);
        }

        return new TipoProcessoResponse(tipoProcesso);
    }
}