package net.wasys.getdoc.domain.enumeration;

public enum Resposta {

    OK("Ok"),
    SIM("Sim"),
    NAO("Não"),
    TODOS("Todos");

    private String nome;
    Resposta(String nome) {
        this.nome = nome;
    }
    public String getNome() {
        return nome;
    }
}
