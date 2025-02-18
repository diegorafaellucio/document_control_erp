package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.rest.annotations.NotNull;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoCampoGrupo;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "TipoCampoGrupoResponse")
public class TipoCampoGrupoResponse extends RequestCadastroTipoCampoGrupo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Sempre visível.")
    private int ordem;

    @ApiModelProperty(value = "Nome do processo.")
    private String processoNome;

    public TipoCampoGrupoResponse(TipoCampoGrupo tipoCampoGrupo) {
        super(tipoCampoGrupo);
        this.id = tipoCampoGrupo.getId();
        this.ordem = tipoCampoGrupo.getOrdem();
        this.processoNome = tipoCampoGrupo.getTipoProcesso().getNome();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
}