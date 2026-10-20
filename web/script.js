// Aqui a gente busca as vagas que estão no banco
async function carregarVagas() {

    try {

        const resposta =
            await fetch("/api/vagas");


        if (!resposta.ok) {

            throw new Error(
                "Não foi possível buscar as vagas."
            );
        }


        const vagas =
            await resposta.json();


        const lista =
            document.getElementById(
                "listaVagas"
            );


        lista.innerHTML = "";


        let disponiveis = 0;

        let ocupadas = 0;


        // Passa por cada vaga que veio do banco
        vagas.forEach(vaga => {

            const estaOcupada =
                vaga.status === "OCUPADA";


            if (estaOcupada) {

                ocupadas++;

            } else {

                disponiveis++;

            }


            const div =
                document.createElement(
                    "div"
                );


            // Define a aparência da vaga
            if (estaOcupada) {

                div.className =
                    "vaga ocupada-vaga";

            } else {

                div.className =
                    "vaga disponivel-vaga";

            }


            // Monta o conteúdo da vaga
            div.innerHTML = `

                <div class="vaga-numero">

                    Vaga ${vaga.numero}

                </div>


                <div class="status ${
                    estaOcupada
                        ? "ocupada"
                        : "disponivel"
                }">

                    ${
                        estaOcupada
                            ? "Ocupada"
                            : "Disponível"
                    }

                </div>


                <button
                    class="botao-vaga"

                    onclick="${
                        estaOcupada
                            ? `liberarVaga(${vaga.id})`
                            : `ocuparVaga(${vaga.id})`
                    }"
                >

                    ${
                        estaOcupada
                            ? "Liberar vaga"
                            : "Ocupar vaga"
                    }

                </button>

            `;


            lista.appendChild(div);

        });


        atualizarNumeros(
            vagas.length,
            disponiveis,
            ocupadas
        );


    } catch (erro) {

        console.error(
            "Erro ao carregar as vagas:",
            erro
        );


        document.getElementById(
            "listaVagas"
        ).innerHTML = `

            <div class="carregando">

                Não foi possível carregar
                as vagas.

            </div>

        `;

    }

}


// Atualiza os números da tela
function atualizarNumeros(
    total,
    disponiveis,
    ocupadas
) {

    // Cards da parte das vagas
    document.getElementById(
        "totalVagas"
    ).textContent = total;


    document.getElementById(
        "vagasDisponiveis"
    ).textContent = disponiveis;


    document.getElementById(
        "vagasOcupadas"
    ).textContent = ocupadas;


    // Informações do dashboard inicial
    document.getElementById(
        "dashboardOcupadas"
    ).textContent =
        `${ocupadas} ocupadas`;


    document.getElementById(
        "dashboardTotal"
    ).textContent =
        `${total} vagas no total`;


    document.getElementById(
        "textoLivres"
    ).textContent =
        `${disponiveis} vagas livres`;


    // Calcula a porcentagem de ocupação
    let porcentagem = 0;


    if (total > 0) {

        porcentagem =
            Math.round(
                (ocupadas / total) * 100
            );

    }


    document.getElementById(
        "porcentagem"
    ).textContent =
        `${porcentagem}%`;


    // Atualiza a barra
    document.getElementById(
        "barraOcupacao"
    ).style.width =
        `${porcentagem}%`;

}


// Aqui a gente ocupa uma vaga
async function ocuparVaga(id) {

    try {

        const resposta =
            await fetch(
                `/api/ocupar/${id}`
            );


        if (!resposta.ok) {

            throw new Error(
                "Erro ao ocupar a vaga."
            );

        }


        // Atualiza a tela depois da alteração
        await carregarVagas();


    } catch (erro) {

        console.error(
            "Erro ao ocupar a vaga:",
            erro
        );

    }

}


// Aqui a gente libera uma vaga
async function liberarVaga(id) {

    try {

        const resposta =
            await fetch(
                `/api/liberar/${id}`
            );


        if (!resposta.ok) {

            throw new Error(
                "Erro ao liberar a vaga."
            );

        }


        // Atualiza a tela depois da alteração
        await carregarVagas();


    } catch (erro) {

        console.error(
            "Erro ao liberar a vaga:",
            erro
        );

    }

}


// Botões que levam para a área do sistema
function irParaSistema() {

    document
        .getElementById("sistema")
        .scrollIntoView({
            behavior: "smooth"
        });

}


// Quando abrir a página,
// carrega as vagas automaticamente
carregarVagas();