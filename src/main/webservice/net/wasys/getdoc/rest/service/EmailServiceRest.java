package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.EmailEnviadoService;
import net.wasys.getdoc.domain.service.EmailRecebidoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.vo.EmailVO;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.response.vo.EmailEnviadoResponse;
import net.wasys.getdoc.rest.response.vo.ListaEmailResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class EmailServiceRest extends SuperServiceRest {

    @Autowired
    private ProcessoService processoService;
    @Autowired
    private EmailEnviadoService emailEnviadoService;
    @Autowired
    private EmailRecebidoService emailRecebidoService;


    public ListaEmailResponse getEmails(Usuario usuario, Long processoId) {
        Processo processo = processoService.get(processoId);
        if (processo == null) {
            throw new ProcessoRestException("processo.nao.localizado");
        }

        ListaEmailResponse listaEmailResponse = new ListaEmailResponse();
        listaEmailResponse.setBadgeEmailsPendente(temAlertarEmails(usuario, processo));


        List<EmailVO> vosByProcesso = emailEnviadoService.findVosByProcesso(processo.getId());
        if (CollectionUtils.isEmpty(vosByProcesso)){
            return null;
        }

        List<EmailEnviadoResponse> listEmails = new ArrayList<>();
        vosByProcesso.forEach(emailVO -> {
            listEmails.add(new EmailEnviadoResponse(emailVO));
        });

        listaEmailResponse.setEmails(listEmails);
        return listaEmailResponse;
    }

    public int temAlertarEmails(Usuario usuario, Processo processo) {

        if(usuario.isAdminRole() || usuario.isGestorRole() || usuario.isAnalistaRole()) {

            Long processoId = processo.getId();
            return emailRecebidoService.countNaoLidos(processoId);
        }
        else {
            return 0;
        }
    }
}