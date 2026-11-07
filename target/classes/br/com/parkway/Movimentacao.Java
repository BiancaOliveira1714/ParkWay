
package br.com.parkway;

import java.sql.Timestamp;

public class Movimentacao {

    private int id;
    private int clienteId;
    private int veiculoId;
    private int estacionamentoId;
    private int vagaId;
    private Timestamp entrada;
    private Timestamp saida;

    public Movimentacao() {
    }

    public Movimentacao(
            int id,
            int clienteId,
            int veiculoId,
            int estacionamentoId,
            int vagaId,
            Timestamp entrada,
            Timestamp saida) {

        this.id = id;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.estacionamentoId = estacionamentoId;
        this.vagaId = vagaId;
        this.entrada = entrada;
        this.saida = saida;
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

    public int getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(int veiculoId) {
        this.veiculoId = veiculoId;
    }

    public int getEstacionamentoId() {
        return estacionamentoId;
    }

    public void setEstacionamentoId(int estacionamentoId) {
        this.estacionamentoId = estacionamentoId;
    }

    public int getVagaId() {
        return vagaId;
    }

    public void setVagaId(int vagaId) {
        this.vagaId = vagaId;
    }

    public Timestamp getEntrada() {
        return entrada;
    }

    public void setEntrada(Timestamp entrada) {
        this.entrada = entrada;
    }

    public Timestamp getSaida() {
        return saida;
    }

    public void setSaida(Timestamp saida) {
        this.saida = saida;
    }
}
