
/*
    PARKWAY - CLIENTES E HISTÓRICO

    Endpoints necessários no Servidor.java:

    GET  /api/clientes?busca=...
    POST /api/clientes
    GET  /api/clientes/{id}/historico
*/

const formCliente = document.getElementById("formCliente");
const formMensagem = document.getElementById("formMensagem");
const listaClientes = document.getElementById("listaClientes");
const buscaCliente = document.getElementById("buscaCliente");
const historicoCliente = document.getElementById("historicoCliente");

/* ==========================================
   LISTAR CLIENTES
========================================== */

async function carregarClientes(busca = "") {

    if (!listaClientes) return;

    listaClientes.innerHTML = "<p>Carregando clientes...</p>";

    try {

        const resposta = await fetch(
            `/api/clientes?busca=${encodeURIComponent(busca)}`
        );

        if (!resposta.ok) {
            throw new Error("Falha ao consultar clientes");
        }

        const clientes = await resposta.json();

        listaClientes.innerHTML = "";

        if (!clientes.length) {
            listaClientes.innerHTML =
                "<p>Nenhum cliente cadastrado.</p>";
            return;
        }

        clientes.forEach(cliente => {

            const item = document.createElement("div");
            item.className = "cliente-item";

            item.innerHTML = `
                <strong>${escapeHtml(cliente.nome)}</strong>

                <small>
                    ${escapeHtml(cliente.telefone || "Sem telefone")}
                    •
                    ${escapeHtml(cliente.placa || "Sem placa")}
                </small>

                <button class="btn btn-outline" type="button">
                    Ver histórico
                </button>
            `;

            item.querySelector("button").addEventListener(
                "click",
                () => carregarHistorico(cliente.id)
            );

            listaClientes.appendChild(item);

        });

    } catch (erro) {

        console.error(erro);

        listaClientes.innerHTML = `
            <p>
                Não foi possível carregar os clientes.
                Confira se o servidor está funcionando.
            </p>
        `;

    }

}

/* ==========================================
   CADASTRAR CLIENTE E VEÍCULO
========================================== */

if (formCliente) {

    formCliente.addEventListener("submit", async evento => {

        evento.preventDefault();

        formMensagem.textContent = "Cadastrando cliente...";

        const formulario = new FormData(formCliente);

        const dados = Object.fromEntries(formulario.entries());

        dados.placa = dados.placa.trim().toUpperCase();

        try {

            const resposta = await fetch("/api/clientes", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(dados)

            });

            if (!resposta.ok) {
                throw new Error("Cadastro não foi concluído");
            }

            formCliente.reset();

            formMensagem.textContent =
                "Cliente cadastrado com sucesso!";

            await carregarClientes(
                buscaCliente?.value || ""
            );

        } catch (erro) {

            console.error(erro);

            formMensagem.textContent =
                "Erro ao cadastrar. Verifique a API de clientes.";

        }

    });

}

/* ==========================================
   HISTÓRICO DO CLIENTE
========================================== */

async function carregarHistorico(clienteId) {

    historicoCliente.innerHTML =
        "<p>Carregando histórico...</p>";

    try {

        const resposta = await fetch(
            `/api/clientes/${clienteId}/historico`
        );

        if (!resposta.ok) {
            throw new Error("Falha ao consultar histórico");
        }

        const movimentacoes = await resposta.json();

        if (!movimentacoes.length) {

            historicoCliente.innerHTML =
                "<p>Este cliente ainda não possui movimentações.</p>";

            return;
        }

        const linhas = movimentacoes.map(m => {

            const vaga = m.vagaId ?? m.vaga_id ?? "";

            return `
                <tr>
                    <td>${escapeHtml(m.placa || "")}</td>
                    <td>Vaga ${escapeHtml(vaga)}</td>
                    <td>${escapeHtml(m.entrada || "")}</td>
                    <td>${escapeHtml(m.saida || "Em aberto")}</td>
                </tr>
            `;

        }).join("");

        historicoCliente.innerHTML = `
            <table class="historico-table">

                <thead>
                    <tr>
                        <th>Placa</th>
                        <th>Vaga</th>
                        <th>Entrada</th>
                        <th>Saída</th>
                    </tr>
                </thead>

                <tbody>
                    ${linhas}
                </tbody>

            </table>
        `;

    } catch (erro) {

        console.error(erro);

        historicoCliente.innerHTML =
            "<p>Não foi possível carregar o histórico.</p>";

    }

}

/* ==========================================
   BUSCA
========================================== */

if (buscaCliente) {

    buscaCliente.addEventListener("input", () => {

        carregarClientes(buscaCliente.value);

    });

}

/* ==========================================
   SEGURANÇA HTML
========================================== */

function escapeHtml(valor) {

    return String(valor ?? "").replace(/[&<>"']/g, caractere => {

        const entidades = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        };

        return entidades[caractere];

    });

}

/* ==========================================
   INICIALIZAÇÃO
========================================== */

carregarClientes();
