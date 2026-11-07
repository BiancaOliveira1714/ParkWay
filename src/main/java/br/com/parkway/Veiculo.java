
package br.com.parkway;

public class Veiculo {

    private int id;
    private int clienteId;
    private String modelo;
    private String placa;
    private String cor;

    public Veiculo() {
    }

    public Veiculo(
            int id,
            int clienteId,
            String modelo,
            String placa,
            String cor) {

        this.id = id;
        this.clienteId = clienteId;
        this.modelo = modelo;
        this.placa = placa;
        this.cor = cor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }
}
