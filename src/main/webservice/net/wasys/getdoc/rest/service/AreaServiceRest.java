package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.AreaService;
import net.wasys.getdoc.domain.service.SubareaService;
import net.wasys.getdoc.rest.exception.AreaRestException;
import net.wasys.getdoc.rest.exception.SubAreaRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastrarArea;
import net.wasys.getdoc.rest.request.vo.RequestCadastrarSubArea;
import net.wasys.getdoc.rest.response.vo.AreaResponse;
import net.wasys.getdoc.rest.response.vo.DetalhesAreaResponse;
import net.wasys.getdoc.rest.response.vo.SubAreaResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class AreaServiceRest extends SuperServiceRest{

    @Autowired
    private AreaService areaService;
    @Autowired
    private SubareaService subAreaService;

    public List<AreaResponse> getAtivas(Usuario usuario) {
        List<Area> areasAtivas = areaService.findAtivas();
        List<AreaResponse> list = parser(areasAtivas);
        return list;
    }

    public List<AreaResponse> getAll(Usuario usuario, int min, int max) {
        List<Area> all = areaService.findAll(min, max);
        List<AreaResponse> parser = parser(all);
        return parser;
    }

    private List<AreaResponse> parser(List<Area> ativas) {
        List<AreaResponse> list = null;
        if(ativas != null && ativas.size() > 0){
            list = new ArrayList<>();
            for(Area te : ativas){
                list.add(new AreaResponse(te));
            }
        }
        return list;
    }

    /**
     * Retorna as subáreas de uma determinada área.
     * @param usuario
     * @param areaId
     * @return
     */
    public List<SubAreaResponse> getSubAreas(Usuario usuario, Long areaId) throws AreaRestException {

        Area area = areaService.get(areaId);
        if(area == null){
            throw new AreaRestException("area.nao.localizada");
        }

        List<SubAreaResponse> list = null;
        List<Subarea> subareas = subAreaService.findByArea(area.getId());
        if(subareas != null && subareas.size() > 0){
            list = new ArrayList<>();
            for(Subarea sA : subareas){
                list.add(new SubAreaResponse(sA));
            }
        }
        return list;
    }

    public DetalhesAreaResponse cadastrar(Usuario usuario, RequestCadastrarArea requestCadastrarArea) throws AreaRestException {
        Area area = new Area();
        return saveOrUpdate(usuario, requestCadastrarArea, area);
    }

    public DetalhesAreaResponse editar(Usuario usuario, Long id, RequestCadastrarArea requestCadastrarArea) throws AreaRestException {
        Area area = areaService.get(id);
        if(area == null){
            throw new AreaRestException("area.nao.localizada");
        }
        return saveOrUpdate(usuario, requestCadastrarArea, area);
    }

    private DetalhesAreaResponse saveOrUpdate(Usuario usuario, RequestCadastrarArea requestCadastrarArea, Area area) {
        validaRequestParameters(requestCadastrarArea);
        validaRequestParameters(requestCadastrarArea.getSubAreas());

        area.setAtivo(requestCadastrarArea.isAtiva());
        area.setDescricao(requestCadastrarArea.getNome());
        area.setHorasPrazo(requestCadastrarArea.getPrazoHoras());


        List<Subarea> listSubarea = new ArrayList<>();

        List<RequestCadastrarSubArea> subAreas = requestCadastrarArea.getSubAreas();
        if(CollectionUtils.isNotEmpty(subAreas)){
            subAreas.forEach(requestCadastrarSubArea -> {

                Subarea subarea = new Subarea();
                if(requestCadastrarSubArea.getId() != null){
                    subarea = subAreaService.getById(requestCadastrarSubArea.getId());
                    if(subarea == null){
                        throw new SubAreaRestException("subarea.nao.localizado.id", requestCadastrarSubArea.getId());
                    }
                }

                subarea.setArea(area);
                subarea.setDataAtualizacao(new Date());
                subarea.setAtivo(requestCadastrarSubArea.isAtiva());
                subarea.setDescricao(requestCadastrarSubArea.getNome());
                subarea.setEmails(requestCadastrarSubArea.getEmails());
                listSubarea.add(subarea);

            });
        }

        areaService.saveOrUpdate(area, listSubarea, usuario);

        DetalhesAreaResponse detalhesAreaResponse = new DetalhesAreaResponse(area);

        List<SubAreaResponse> subAreasSalva = getSubAreas(usuario, area.getId());
        if(CollectionUtils.isNotEmpty(subAreasSalva)){
            subAreasSalva.forEach(subAreaResponse -> {
                detalhesAreaResponse.addSubarea(subAreaResponse);
            });
        }

        return detalhesAreaResponse;
    }

    public boolean excluir(Usuario usuario, Long id) throws AreaRestException {
        Area area = areaService.get(id);
        if(area == null){
            throw new AreaRestException("area.nao.localizada");
        }
        areaService.excluir(area.getId(), usuario);
        return true;
    }

    public DetalhesAreaResponse detalhar(Usuario usuario, Long id) throws AreaRestException {
        Area area = areaService.get(id);
        if(area == null){
            throw new AreaRestException("area.nao.localizada");
        }

        DetalhesAreaResponse detalhesAreaResponse = new DetalhesAreaResponse(area);

        List<SubAreaResponse> subAreas = getSubAreas(usuario, area.getId());
        if(CollectionUtils.isNotEmpty(subAreas)){
            subAreas.forEach(subAreaResponse -> {
                detalhesAreaResponse.addSubarea(subAreaResponse);
            });
        }

        return detalhesAreaResponse;
    }
}