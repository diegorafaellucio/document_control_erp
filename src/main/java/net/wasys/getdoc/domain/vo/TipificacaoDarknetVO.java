package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class TipificacaoDarknetVO extends WebServiceClientVO {

    @Override
    public TipoConsultaExterna getTipoConsultaExterna() {
        return TipoConsultaExterna.TIPIFICACAO_DARKNET;
    }
}
