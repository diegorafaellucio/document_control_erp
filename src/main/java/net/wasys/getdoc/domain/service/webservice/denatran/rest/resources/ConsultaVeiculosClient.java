package net.wasys.getdoc.domain.service.webservice.denatran.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("v1/condutores")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public interface ConsultaVeiculosClient {

    @GET
    @Path("/cpf/{cpf}")
    Response consultarPorCpf(
            @HeaderParam(value="x-cpf-usuario") String cpfUsuario,
            @PathParam("cpf") String cpf,
            @QueryParam("idUltimoRegistro") Long idUltimoRegistro);
}