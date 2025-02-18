package net.wasys.getdoc.domain.enumeration;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.util.ddd.Entity;

public enum TipoRegistro {

	USUARIO(Usuario.class),
	FERIADO(Feriado.class),
	IRREGULARIDADE(Irregularidade.class),
	AREA(Area.class),
	SUBAREA(Subarea.class),
	TIPO_EVIDENCIA(TipoEvidencia.class),
	TIPO_PROCESSO(TipoProcesso.class),
	TIPO_DOCUMENTO(TipoDocumento.class),
	TIPO_CAMPO(TipoCampo.class),
	TIPO_CAMPO_GRUPO(TipoCampoGrupo.class),
	SITUACAO(Situacao.class),
	BASE_INTERNA(BaseInterna.class),
	MODELO_OCR(ModeloOcr.class),
	MODELO_DOCUMENTO(ModeloDocumento.class),
	SUBPERFIL(Subperfil.class),
	TEXTO_PADRAO(TextoPadrao.class),
	REGRA(Regra.class),
	SUB_REGRA(SubRegra.class),
	FILA_CONFIGURACAO(FilaConfiguracao.class),
	ALUNO(Aluno.class),
	CAMPO_GRUPO(CampoGrupo.class),
	DOCUMENTO(Documento.class),
	CAMPANHA(Campanha.class),
	ETAPAS(Etapa.class),
	CONFIGURACAO_LOGIN_AZURE(ConfiguracaoLoginAzure.class),
	CALENDARIO(Calendario.class),
	CATEGORIA_DOCUMENTO(CategoriaDocumento.class),
	CONFIGURACAO_GERACAO_RELATORIO(ConfiguracaoGeracaoRelatorio.class),
	STATUS_LABORAL(StatusLaboral.class),
	CATEGORIA_GRUPO_MODELO_DOCUMENTO(CategoriaGrupoModeloDocumento.class),
	GRUPO_MODELO_DOCUMENTO(GrupoModeloDocumento.class),
	MODELO_DOCUMENTO_GRUPO_MODELO_DOCUMENTO(ModeloDocumentoGrupoModeloDocumento.class),
	CAMPO_MODELO_OCR(CampoModeloOcr.class),
	CAMPO_OCR_TIPO_CAMPO(CampoOcrTipoCampo.class),
	CONFIGURACAO_OCR(ConfiguracaoOCR.class),
	CONFIGURACAO_OCR_INSTITUICAO(ConfiguracaoOCRInstituicao.class)
	;

	private Class<? extends Entity> registroClass;

	private TipoRegistro(Class<? extends Entity> registroClass) {
		this.registroClass = registroClass;
	}

	public Class<? extends Entity> getRegistroClass() {
		return registroClass;
	}

	public static TipoRegistro getTipoRegistro(Entity entity) {

		TipoRegistro[] values = values();
		for (TipoRegistro tipoRegistro : values) {

			Class<? extends Entity> class1 = entity.getClass();
			Class<? extends Entity> class2 = tipoRegistro.getRegistroClass();
			if(class2.isAssignableFrom(class1)) {
				return tipoRegistro;
			}
		}

		return null;
	}
}
