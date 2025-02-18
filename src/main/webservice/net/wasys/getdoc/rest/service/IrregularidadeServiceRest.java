package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.IrregularidadeService;
import net.wasys.getdoc.rest.exception.IrregularidadeRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroIrregularidade;
import net.wasys.getdoc.rest.response.vo.IrregularidadeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class IrregularidadeServiceRest extends SuperServiceRest{

    @Autowired
    private IrregularidadeService irregularidadeService;

    public List<IrregularidadeResponse> getAtivas(Usuario usuario) {
        List<Irregularidade> irregularidadeServiceAtivas = irregularidadeService.findAtivas();
        List<IrregularidadeResponse> list = parser(irregularidadeServiceAtivas);
        return list;
    }

    public List<IrregularidadeResponse> getAll(Usuario usuario, int min, int max) {
        List<Irregularidade> all = irregularidadeService.findAll(min, max);
        List<IrregularidadeResponse> parser = parser(all);
        return parser;
    }

    private List<IrregularidadeResponse> parser(List<Irregularidade> ativas) {
        List<IrregularidadeResponse> list = null;
        if(ativas != null && ativas.size() > 0){
            list = new ArrayList<>();
            for(Irregularidade te : ativas){
                list.add(new IrregularidadeResponse(te));
            }
        }
        return list;
    }

    public IrregularidadeResponse detalhar(Usuario usuario, Long id) {
        Irregularidade irregularidade = irregularidadeService.get(id);
        if(irregularidade == null){
            throw new IrregularidadeRestException("irregularidade.nao.localizada.id", id);
        }
        return new IrregularidadeResponse(irregularidade);
    }

    public boolean excluir(Usuario usuario, Long id) {
        Irregularidade irregularidade = irregularidadeService.get(id);
        if(irregularidade == null){
            throw new IrregularidadeRestException("irregularidade.nao.localizada.id", id);
        }

        irregularidadeService.excluir(irregularidade.getId(), usuario);
        return true;
    }

    public IrregularidadeResponse editar(Usuario usuario, Long id,  RequestCadastroIrregularidade requestCadastroIrregularidade) {
        Irregularidade irregularidade = irregularidadeService.get(id);
        if(irregularidade == null){
            throw new IrregularidadeRestException("irregularidade.nao.localizada.id", id);
        }
        return saveOrUpdate(usuario, requestCadastroIrregularidade, irregularidade);
    }

    public IrregularidadeResponse cadastrar(Usuario usuario, RequestCadastroIrregularidade requestCadastroIrregularidade) {
        Irregularidade irregularidade = new Irregularidade();
        return saveOrUpdate(usuario, requestCadastroIrregularidade, irregularidade);
    }

    private IrregularidadeResponse saveOrUpdate(Usuario usuario, RequestCadastroIrregularidade requestCadastroIrregularidade, Irregularidade irregularidade) {
        validaRequestParameters(requestCadastroIrregularidade);

        irregularidade.setAtiva(requestCadastroIrregularidade.isAtivo());
        irregularidade.setNome(requestCadastroIrregularidade.getDescricao());

        irregularidadeService.saveOrUpdate(irregularidade, usuario);
        return new IrregularidadeResponse(irregularidade);
    }
}