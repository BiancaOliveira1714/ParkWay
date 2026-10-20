package br.com.parkway;

public class Vaga {

    private int id;
    private int numero;
    private String status;

    // Aqui a gente cria uma vaga
    // usando as informações que vieram do banco
    public Vaga(int id, int numero, String status) {

        this.id = id;
        this.numero = numero;
        this.status = status;
    }

    // Pega o ID da vaga
    public int getId() {

        return id;
    }

    // Pega o número da vaga
    public int getNumero() {

        return numero;
    }

    // Pega o status da vaga
    public String getStatus() {

        return status;
    }

    // Altera o status da vaga
    public void setStatus(String status) {

        this.status = status;
    }
}