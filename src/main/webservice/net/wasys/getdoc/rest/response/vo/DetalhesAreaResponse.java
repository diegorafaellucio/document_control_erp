package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.Subarea;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "DetalhesAreaResponse")
public class DetalhesAreaResponse extends AreaResponse {


    @ApiModelProperty(notes = "Lista de subáreas.")
    private List<SubAreaResponse> subAreas;

    public DetalhesAreaResponse(Area area) {
        super(area);
    }

    public DetalhesAreaResponse(Area area, List<SubAreaResponse> subAreas) {
        super(area);
        this.subAreas = subAreas;
    }

    public void addSubarea(SubAreaResponse subarea){
        if(this.subAreas == null){
            this.subAreas = new ArrayList<>();
        }
        this.subAreas.add(subarea);
    }

    public List<SubAreaResponse> getSubAreas() {
        return subAreas;
    }

    public void setSubAreas(List<SubAreaResponse> subAreas) {
        this.subAreas = subAreas;
    }
}