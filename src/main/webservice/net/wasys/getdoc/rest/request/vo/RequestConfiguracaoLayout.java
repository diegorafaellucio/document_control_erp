package net.wasys.getdoc.rest.request.vo;

import com.gargoylesoftware.htmlunit.javascript.host.xml.FormData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

@ApiModel(value = "RequestConfiguracaoLayout")
public class RequestConfiguracaoLayout extends SuperVo {

    @ApiModelProperty(notes = "logo.")
    private MultipartFile logo;

    @ApiModelProperty(notes = "navbar.")
    private String navbar;

    @ApiModelProperty(notes = "accent.")
    private String accent;

    public MultipartFile getLogo() {
        return logo;
    }

    public void setLogo(MultipartFile logo) {
        this.logo = logo;
    }

    @NotNull
    public String getNavbar() {
        return navbar;
    }

    public void setNavbar(String navbar) {
        this.navbar = navbar;
    }

    @NotNull
    public String getAccent() {
        return accent;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }
}

