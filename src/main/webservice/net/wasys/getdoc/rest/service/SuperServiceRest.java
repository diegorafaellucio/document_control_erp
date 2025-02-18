package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.service.MessageService;
import net.wasys.getdoc.rest.annotations.NotNullRunner;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuperServiceRest {

    @Autowired
    public MessageService messageService;

    @Autowired
    public NotNullRunner notNullRunner;

    /**
     * Responsável por validar os parâmetros da request.
     *
     * @param obj
     * @throws Exception
     */
    protected void validaRequestParameters(Object obj) throws DadosObrigatorioRequestException {
        notNullRunner.run(obj);
    }
}
