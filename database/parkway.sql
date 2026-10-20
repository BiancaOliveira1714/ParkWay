-- Criando o banco do ParkWay
CREATE DATABASE IF NOT EXISTS parkway;

USE parkway;

-- Tabela que guarda as vagas do estacionamento
CREATE TABLE IF NOT EXISTS vagas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL'
);

-- Criando algumas vagas para começar os testes
INSERT INTO vagas (numero, status) VALUES
(1, 'DISPONIVEL'),
(2, 'DISPONIVEL'),
(3, 'OCUPADA'),
(4, 'DISPONIVEL'),
(5, 'OCUPADA'),
(6, 'DISPONIVEL'),
(7, 'DISPONIVEL'),
(8, 'OCUPADA'),
(9, 'DISPONIVEL'),
(10, 'DISPONIVEL');