package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.TextoPadrao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.TextoPadraoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.filtro.TextoPadraoFiltro;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.exception.TextoPadraoRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTextoPadrao;
import net.wasys.getdoc.rest.response.vo.TextoPadraoResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class TextoPadraoServiceRest extends SuperServiceRest {

    @Autowired
    private TextoPadraoService textoPadraoService;
    @Autowired
    private ProcessoService processoService;
    @Autowired
    private TipoProcessoService tipoProcessoService;

    /**
     * @param usuario
     * @param processoId
     * @param min
     * @param max
     * @return
     */
    public List<TextoPadraoResponse> getAtivosByProcesso(Usuario usuario, Long processoId, int min, int max) throws ProcessoRestException {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        TextoPadraoFiltro filtro = new TextoPadraoFiltro();
        filtro.setAtivo(true);
        filtro.setTipoProcessoId(processo.getTipoProcesso().getId());

        List<TextoPadrao> listTextoPadrao = textoPadraoService.findByFiltro(filtro, min, max);
        List<TextoPadraoResponse> list = parser(listTextoPadrao);
        return list;
    }

    /**
     * @param usuario
     * @param processoId
     * @param min
     * @param max
     * @return
     */
    public List<TextoPadraoResponse> getAllByProcesso(Usuario usuario, Long processoId, int min, int max) throws ProcessoRestException {

        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        TextoPadraoFiltro filtro = new TextoPadraoFiltro();
        filtro.setTipoProcessoId(processo.getTipoProcesso().getId());

        List<TextoPadrao> listTextoPadrao = textoPadraoService.findByFiltro(filtro, min, max);
        List<TextoPadraoResponse> list = parser(listTextoPadrao);
        return list;
    }


    private List<TextoPadraoResponse> parser(List<TextoPadrao> ativas) {
        List<TextoPadraoResponse> list = null;
        if (ativas != null && ativas.size() > 0) {
            list = new ArrayList<>();
            for (TextoPadrao te : ativas) {
                list.add(new TextoPadraoResponse(te));
            }
        }
        return list;
    }

    public List<TextoPadraoResponse> getAll(Usuario usuario, int min, int max) {
        TextoPadraoFiltro filtro = new TextoPadraoFiltro();
        List<TextoPadrao> listTextoPadrao = textoPadraoService.findByFiltro(filtro, min, max);
        List<TextoPadraoResponse> list = parser(listTextoPadrao);
        return list;
    }

    public TextoPadraoResponse detalhar(Usuario usuario, Long id) throws TextoPadraoRestException {
        TextoPadrao textoPadrao = textoPadraoService.get(id);
        if (textoPadrao == null) {
            throw new TextoPadraoRestException("textopadrao.nao.localizado.id", id);
        }
        return new TextoPadraoResponse(textoPadrao);
    }

    public boolean excluir(Usuario usuario, Long id) {
        TextoPadrao textoPadrao = textoPadraoService.get(id);
        if (textoPadrao == null) {
            throw new TextoPadraoRestException("textopadrao.nao.localizado.id", id);
        }
        textoPadraoService.excluir(textoPadrao.getId(), usuario);
        return true;
    }

    public TextoPadraoResponse cadastrar(Usuario usuario, RequestCadastroTextoPadrao requestCadastroTextoPadrao) throws TipoProcessoRestException {
        validaRequestParameters(requestCadastroTextoPadrao);

        TextoPadrao textoPadrao = new TextoPadrao();
        return saveOrUpdate(usuario, requestCadastroTextoPadrao, textoPadrao);
    }

    public TextoPadraoResponse editar(Usuario usuario, Long id, RequestCadastroTextoPadrao requestCadastroTextoPadrao) throws TipoProcessoRestException, TextoPadraoRestException {
        validaRequestParameters(requestCadastroTextoPadrao);

        TextoPadrao textoPadrao = textoPadraoService.get(id);
        if(textoPadrao == null){
            throw new TextoPadraoRestException("textopadrao.nao.localizado.id", requestCadastroTextoPadrao.getTipoProcessoId());
        }

        return saveOrUpdate(usuario, requestCadastroTextoPadrao, textoPadrao);
    }

    private TextoPadraoResponse saveOrUpdate(Usuario usuario, RequestCadastroTextoPadrao requestCadastroTextoPadrao, TextoPadrao textoPadrao) throws TipoProcessoRestException {

        TipoProcesso tipoProcesso = tipoProcessoService.get(requestCadastroTextoPadrao.getTipoProcessoId());
        if(tipoProcesso == null){
            throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", requestCadastroTextoPadrao.getTipoProcessoId());
        }

        //textoPadrao.setTipoProcesso(tipoProcesso);
        textoPadrao.setTexto(requestCadastroTextoPadrao.getTexto());
        textoPadrao.setAtivo(requestCadastroTextoPadrao.isAtivo());
        textoPadrao.setNome(requestCadastroTextoPadrao.getNome());

        List<String> permissoesUso = new ArrayList<>();
        if(requestCadastroTextoPadrao.isSolicitacaoArea()){
            permissoesUso.add(TextoPadrao.SOLICITACAO_AREA);
        }

        if(requestCadastroTextoPadrao.isEnvioEmail()){
            permissoesUso.add(TextoPadrao.ENVIO_EMAIL);
        }

        if(requestCadastroTextoPadrao.isRegistroEvidencia()){
            permissoesUso.add(TextoPadrao.REGISTRO_EVIDENCIA);
        }

        textoPadrao.setPermissoesDeUso(permissoesUso.toString());

        //textoPadraoService.saveOrUpdate(textoPadrao, usuario);
        return new TextoPadraoResponse(textoPadrao);
    }
}