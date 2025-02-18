package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(value="RequestUploadDocumentoGraduacao")
public class RequestUploadDocumentoGraduacao {

	//{"nome": "teste", "cpf": "", "email": "teste@teste.com", "canalId": "1", "tipoAlunoId": "2", "tipoProcessoId": "3", "classificacaoProcessoId": "4", "alunoCod": "5", "dataCadastro": "11/08/2019 14:52", "cursoCod": "6", "campusCod": "7", "tipoDocumentoId": "8", "nomeArquivo": "teste.jpg"}

    @ApiModelProperty(notes="Nome do Inscrito.", required = true)
    private String nome;

    @ApiModelProperty(notes="CPF do Inscrito.", required = true)
    private String cpf;

    @ApiModelProperty(notes="E-mail do Inscrito.")
    private String email;

    @ApiModelProperty(notes="ID do Canal (2 = CVC).")
    private Long canalId;

    @ApiModelProperty(notes="ID do Tipo de Aluno (1 = Inscrição; 2 = Candidato).", required = true)
    private Long tipoAlunoId;

    @ApiModelProperty(notes="Código do Aluno (número da inscrição ou número do candidato).", required = true)
    private String alunoCod;

    @ApiModelProperty(notes="ID do Tipo de Processo.", required = true)
    private Long tipoProcessoId;

    @ApiModelProperty(notes="ID da Classificação do Processo (1 = Vestibular; 2 = TE; 3 = MSV Externa; 5 = MSV Interna; 7 = Enem).", required = true)
    private Long classificacaoProcessoId;

    @ApiModelProperty(notes="Data que o Cadastro Aconteceu.")
    private Date dataCadastro;

    @ApiModelProperty(notes="Código do Curso.", required = true)
    private Integer cursoCod;

    @ApiModelProperty(notes="Código do Campus.", required = true)
    private Integer campusCod;

    @ApiModelProperty(notes="Código do Tipo do Documento.", required = true)
    private Long tipoDocumentoId;

    @ApiModelProperty(notes="Nome do Arquivo.", required = true)
    private String nomeArquivo;

    public Integer getCampusCod() {
        return campusCod;
    }

    public void setCampusCod(Integer campusCod) {
        this.campusCod = campusCod;
    }

    public Integer getCursoCod() {
        return cursoCod;
    }

    public void setCursoCod(Integer cursoCod) {
        this.cursoCod = cursoCod;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCanalId() {
        return canalId;
    }

    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }

    public Long getTipoAlunoId() {
        return tipoAlunoId;
    }

    public void setTipoAlunoId(Long tipoAlunoId) {
        this.tipoAlunoId = tipoAlunoId;
    }

    public Long getTipoProcessoId() {
        return tipoProcessoId;
    }

    public void setTipoProcessoId(Long tipoProcessoId) {
        this.tipoProcessoId = tipoProcessoId;
    }

    public Long getClassificacaoProcessoId() {
        return classificacaoProcessoId;
    }

    public void setClassificacaoProcessoId(Long classificacaoProcessoId) {
        this.classificacaoProcessoId = classificacaoProcessoId;
    }

    public String getAlunoCod() {
        return alunoCod;
    }

    public void setAlunoCod(String alunoCod) {
        this.alunoCod = alunoCod;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Long getTipoDocumentoId() {
        return tipoDocumentoId;
    }

    public void setTipoDocumentoId(Long tipoDocumentoId) {
        this.tipoDocumentoId = tipoDocumentoId;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }
}
