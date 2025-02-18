package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@ApiModel(value = "ConfiguracaoLayoutResponse")
public class ConfiguracaoLayoutResponse extends SuperVo {

    @ApiModelProperty(value = "logo.")
    private String logo;

    @ApiModelProperty(value = "navbar.")
    private String navbar;

    @ApiModelProperty(value = "accent.")
    private String accent;

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getNavbar() {
        return navbar;
    }

    public void setNavbar(String navbar) {
        this.navbar = navbar;
    }

    public String getAccent() {
        return accent;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }
}

