package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.FilaConfiguracao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.FilaConfiguracaoService;
import net.wasys.getdoc.rest.exception.FilaRestException;
import net.wasys.getdoc.rest.request.vo.RequestFilaConfiguracao;
import net.wasys.getdoc.rest.response.vo.FilaConfiguracaoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class FilaServiceRest extends SuperServiceRest {


    @Autowired
    private FilaConfiguracaoService filaConfiguracaoService;


    public List<FilaConfiguracaoResponse> getFilaTrabalho(Usuario usuario) {
        List<FilaConfiguracao> filaConfiguracaoList = filaConfiguracaoService.findAll();

        if (filaConfiguracaoList == null || filaConfiguracaoList.size() == 0) {
            throw new FilaRestException("fila.null");
        }

        List<FilaConfiguracaoResponse> filaConfiguracaoResponseList = new ArrayList<>();

        for (FilaConfiguracao fc : filaConfiguracaoList) {
            if (fc != null) {
                FilaConfiguracaoResponse filaConfiguracaoResponse = new FilaConfiguracaoResponse(fc);
                filaConfiguracaoResponseList.add(filaConfiguracaoResponse);
            }
        }
        return filaConfiguracaoResponseList;
    }

    public FilaConfiguracaoResponse cadastrar(Usuario usuario, RequestFilaConfiguracao requestFilaConfiguracao) {
        if (requestFilaConfiguracao == null) {
            throw new FilaRestException("fila.request.null");
        }

        /*
         * valida campos em branco
         */
        validaCamposEmBranco(requestFilaConfiguracao);

        /*
         * converte e salva o objeto na base
         */
        FilaConfiguracao filaConfiguracao = new FilaConfiguracao(requestFilaConfiguracao);
        filaConfiguracaoService.saveOrUpdateAngular(filaConfiguracao, usuario);

        /*
         * converte o objeto para o retorno response
         */

        return new FilaConfiguracaoResponse(requestFilaConfiguracao);

    }

    public FilaConfiguracaoResponse editar(Usuario usuario, Long id, RequestFilaConfiguracao requestFilaConfiguracao) {
        if (requestFilaConfiguracao == null) {
            throw new FilaRestException("fila.request.null");
        }
        /*
         * valida campos em branco
         */
        validaCamposEmBranco(requestFilaConfiguracao);

        if (id == null) {
            throw new FilaRestException("fila.campo.vazio", "Id");
        }

        FilaConfiguracao fila = filaConfiguracaoService.get(id);
        if (fila == null){
            throw new FilaRestException("fila.null");
        }

        fila.setStatus(requestFilaConfiguracao.getStatus());
        fila.setExibirAssociadosOutros(requestFilaConfiguracao.isExibirAssociadosOutros());
        fila.setExibirNaoAssociados(requestFilaConfiguracao.isExibirNaoAssociados());
        fila.setPermissaoEditarOutros(requestFilaConfiguracao.isPermissaoEditarOutros());
        fila.setVerificarProximaRequisicao(requestFilaConfiguracao.isVerificarProximaRequisicao());
        fila.setDescricao(requestFilaConfiguracao.getDescricao());

        /*
         * salva o objeto na base
         */
        filaConfiguracaoService.saveOrUpdateAngular(fila, usuario);

        /*
         * converte o objeto para o retorno response
         */

        return new FilaConfiguracaoResponse(requestFilaConfiguracao);

    }

    public boolean excluir(Usuario usuario, Long id){
        if (id == null) {
            throw new FilaRestException("fila.campo.vazio", "Id");
        }

        FilaConfiguracao fila = filaConfiguracaoService.get(id);
        if (fila == null){
            throw new FilaRestException("fila.null");
        }

        filaConfiguracaoService.excluir(id, usuario);
        return true;

    }

    private void validaCamposEmBranco(RequestFilaConfiguracao requestFilaConfiguracao) {
        if (requestFilaConfiguracao.getDescricao().isEmpty()) {
            throw new FilaRestException("fila.campo.vazio", "Descrição");
        }
        if (requestFilaConfiguracao.getStatus().isEmpty()) {
            throw new FilaRestException("fila.campo.vazio", "Status");
        }

    }

}