package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.rest.response.vo.PermissaoTipoProcessoResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class PermissaoServiceRest extends SuperServiceRest {

    public List<PermissaoTipoProcessoResponse> get(Usuario usuario) {

        List<PermissaoTipoProcessoResponse> list = new ArrayList<>();
        for (PermissaoTP value : PermissaoTP.values()) {
            PermissaoTipoProcessoResponse ptpr = new PermissaoTipoProcessoResponse();
            ptpr.setPermissao(value);
            ptpr.setDescricao(messageService.getValue("PermissaoTP." + value.name() + ".label"));
            list.add(ptpr);
        }
        return list;
    }
}