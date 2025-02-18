package net.wasys.getdoc.rest.response.vo;


import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "HelloCinqResponse")
public class HelloCinqResponse {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Nome")
    private String nome;

    public HelloCinqResponse(String nome) {
        if(nome != null) {
            this.id = 1L;
            this.nome = nome;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}