
/* ==========================================
   PARKWAY - CONTROLE DE VAGAS
   Integração com API Java
========================================== */

let estacionamentoAtual = 1;

/* ==========================================
   ELEMENTOS DA PÁGINA
========================================== */

const parkingGrid = document.getElementById("parkingGrid");
const estacionamentoSelect = document.getElementById("estacionamentoSelect");
const totalVagasElement = document.getElementById("totalVagas");
const vagasOcupadasElement = document.getElementById("vagasOcupadas");
const vagasLivresElement = document.getElementById("vagasLivres");

/* ==========================================
   CARREGAR VAGAS
========================================== */

async function carregarVagas() {

    if (!parkingGrid) {
        console.error("Elemento parkingGrid não encontrado.");
        return;
    }

    parkingGrid.innerHTML = `
        <p class="map-loading">Carregando vagas...</p>
    `;

    try {

        const resposta = await fetch(
            `/api/vagas?estacionamento=${estacionamentoAtual}`
        );

        if (!resposta.ok) {
            throw new Error("Não foi possível carregar as vagas.");
        }

        const vagas = await resposta.json();

        parkingGrid.innerHTML = "";

        let ocupadas = 0;
        let livres = 0;

        if (!Array.isArray(vagas) || vagas.length === 0) {

            parkingGrid.innerHTML = `
                <p class="map-empty">
                    Nenhuma vaga encontrada neste estacionamento.
                </p>
            `;

            atualizarResumo(0, 0, 0);
            return;
        }

        vagas.forEach(vaga => {

            const ocupada = vaga.status === "OCUPADA";

            if (ocupada) {
                ocupadas++;
            } else {
                livres++;
            }

            const card = document.createElement("div");

            card.className = ocupada
                ? "parking-space occupied"
                : "parking-space free";

            const numeroVaga = String(vaga.numero).padStart(2, "0");

            card.innerHTML = `
                <strong>V${numeroVaga}</strong>

                <span class="vaga-status">
                    ${ocupada ? "Ocupada" : "Disponível"}
                </span>

                <small>
                    ${ocupada
                        ? "Esta vaga está ocupada."
                        : "Esta vaga está livre."}
                </small>

                <button
                    class="botao-vaga"
                    type="button"
                    ${ocupada ? "" : ""}
                >
                    ${ocupada ? "Liberar vaga" : "Ocupar vaga"}
                </button>
            `;

            const botao = card.querySelector(".botao-vaga");

            botao.addEventListener("click", async () => {

                botao.disabled = true;

                if (ocupada) {
                    await liberarVaga(vaga.id);
                } else {
                    await ocuparVaga(vaga.id);
                }

                botao.disabled = false;

            });

            parkingGrid.appendChild(card);

        });

        atualizarResumo(vagas.length, ocupadas, livres);

    } catch (erro) {

        console.error("Erro ao carregar vagas:", erro);

        parkingGrid.innerHTML = `
            <p class="map-error">
                Não foi possível carregar as vagas.
                Verifique se o servidor Java está funcionando.
            </p>
        `;

        atualizarResumo(0, 0, 0);

    }

}

/* ==========================================
   ATUALIZAR CONTADORES
========================================== */

function atualizarResumo(total, ocupadas, livres) {

    if (totalVagasElement) {
        totalVagasElement.textContent = total;
    }

    if (vagasOcupadasElement) {
        vagasOcupadasElement.textContent = ocupadas;
    }

    if (vagasLivresElement) {
        vagasLivresElement.textContent = livres;
    }

}

/* ==========================================
   OCUPAR VAGA
========================================== */

async function ocuparVaga(id) {

    try {

        const resposta = await fetch(`/api/ocupar/${id}`, {
            method: "POST"
        });

        if (!resposta.ok) {
            throw new Error("Não foi possível ocupar a vaga.");
        }

        await carregarVagas();

    } catch (erro) {

        console.error("Erro ao ocupar vaga:", erro);

        alert("Não foi possível ocupar esta vaga.");

    }

}

/* ==========================================
   LIBERAR VAGA
========================================== */

async function liberarVaga(id) {

    try {

        const resposta = await fetch(`/api/liberar/${id}`, {
            method: "POST"
        });

        if (!resposta.ok) {
            throw new Error("Não foi possível liberar a vaga.");
        }

        await carregarVagas();

    } catch (erro) {

        console.error("Erro ao liberar vaga:", erro);

        alert("Não foi possível liberar esta vaga.");

    }

}

/* ==========================================
   SELEÇÃO DE ESTACIONAMENTO
========================================== */

if (estacionamentoSelect) {

    estacionamentoSelect.addEventListener("change", () => {

        estacionamentoAtual = Number(estacionamentoSelect.value);

        carregarVagas();

    });

}

/* ==========================================
   INICIALIZAÇÃO
========================================== */

document.addEventListener("DOMContentLoaded", () => {

    if (estacionamentoSelect) {
        estacionamentoAtual = Number(estacionamentoSelect.value);
    }

    carregarVagas();

});
