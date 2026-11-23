Carga de dados (uma unica transacao)
BEGIN;

Usuarios: 1 professor e 2 alunos
INSERT INTO Usuario (REGISTRO, NOME) VALUES
    (1, 'Ana Souza'),
    (2, 'Bruno Lima'),
    (3, 'Carla Dias');
INSERT INTO Professor (REGISTRO) VALUES (1);
INSERT INTO Aluno     (REGISTRO) VALUES (2), (3);

Materia e vinculos
INSERT INTO Materia (SIGLA, NOME) VALUES ('BD', 'Banco de Dados');
INSERT INTO Possui  (SIGLA, REGISTRO) VALUES ('BD', 1), ('BD', 2), ('BD', 3);

Questoes: 2 objetivas e 1 subjetiva
INSERT INTO Questao (NUMERO, ENUNCIADO) VALUES
    (1, 'Qual comando SQL cria uma tabela?'),
    (2, 'Qual restricao garante valores unicos e nao nulos?'),
    (3, 'Explique a diferenca entre chave primaria e chave estrangeira.');

INSERT INTO Objetiva (NUMERO, CORRETA) VALUES
    (1, 'CREATE TABLE'),
    (2, 'PRIMARY KEY');

INSERT INTO Alternativas (NUMERO, ALTERNATIVA) VALUES
    (1, 'CREATE TABLE'), (1, 'INSERT INTO'), (1, 'DROP TABLE'),
    (2, 'PRIMARY KEY'),  (2, 'CHECK'),       (2, 'DEFAULT');

INSERT INTO Subjetiva (NUMERO, EXPECTATIVA) VALUES
    (3, 'A PK identifica unicamente cada linha; a FK referencia a PK de outra tabela.');

Avaliacao aplicada pela professora Ana (registro 1), da materia BD
INSERT INTO Avaliacao (CODIGO, TITULO, DESCRICAO, INICIO, FIM, DATA, REGISTRO, SIGLA)
VALUES (1, 'Prova 1 - SQL', 'Introducao a SQL', '08:00', '10:00', '2026-12-01', 1, 'BD');

Montagem da avaliacao (valor de cada questao; soma = 10)
INSERT INTO Contem (CODIGO, NUMERO, VALOR) VALUES
    (1, 1, 3.00),
    (1, 2, 3.00),
    (1, 3, 4.00);

Quem deve fazer a avaliacao
INSERT INTO Faz (REGISTRO, CODIGO) VALUES (2, 1), (3, 1);

Respostas. NOTA nao e informada:
objetivas: o trigger calcula a nota
subjetiva: fica 0 ate o professor corrigir
INSERT INTO Resposta (REGISTRO, CODIGO, NUMERO, RESPOSTA) VALUES
    (2, 1, 1, 'CREATE TABLE'),
    (2, 1, 2, 'CHECK'),
    (2, 1, 3, 'A PK identifica a linha e a FK liga duas tabelas.'),
    (3, 1, 1, 'CREATE TABLE'),
    (3, 1, 2, 'PRIMARY KEY'),
    (3, 1, 3, 'PK e unica por linha; FK aponta para a PK de outra tabela.');

COMMIT;

Verificacoes 
Respostas: objetivas ja corrigidas; questao 3 ainda com NOTA 0
Esperado: Bruno Q1=3, Q2=0, Q3=0 e Carla Q1=3, Q2=3, Q3=0
SELECT * FROM Resposta ORDER BY REGISTRO, NUMERO;

Nota total parcial (subjetivas ainda nao corrigidas)
Esperado: Bruno = 3.00 e Carla = 6.00
SELECT * FROM Nota_Total ORDER BY REGISTRO;

A professora corrige a subjetiva
UPDATE Resposta SET NOTA = 3.5
 WHERE REGISTRO = 2 AND CODIGO = 1 AND NUMERO = 3;
UPDATE Resposta SET NOTA = 4
 WHERE REGISTRO = 3 AND CODIGO = 1 AND NUMERO = 3;

Nota total final
Esperado: Bruno = 6.50 e Carla = 10.00
SELECT * FROM Nota_Total ORDER BY REGISTRO;

Avaliacao com professor, materia, questoes e valores
SELECT a.CODIGO, a.TITULO, u.NOME AS PROFESSOR, m.NOME AS MATERIA,
       q.NUMERO AS QUESTAO, q.ENUNCIADO, c.VALOR
  FROM Avaliacao a
  JOIN Usuario  u ON u.REGISTRO = a.REGISTRO
  JOIN Materia  m ON m.SIGLA    = a.SIGLA
  JOIN Contem   c ON c.CODIGO   = a.CODIGO
  JOIN Questao  q ON q.NUMERO   = c.NUMERO
 ORDER BY q.NUMERO;

Testes de erro (cada comando DEVE falhar)

Nota maior que o valor da questao (4,00) -> erro
-- UPDATE Resposta SET NOTA = 5
--  WHERE REGISTRO = 2 AND CODIGO = 1 AND NUMERO = 3;

Resposta que nao e uma alternativa -> erro
-- UPDATE Resposta SET RESPOSTA = 'XYZ'
--  WHERE REGISTRO = 2 AND CODIGO = 1 AND NUMERO = 1;

Usuario que nao e professor nem aluno -> erro (no commit)
-- INSERT INTO Usuario (REGISTRO, NOME) VALUES (99, 'Sem papel');

Professor que tambem e aluno -> erro
-- INSERT INTO Aluno (REGISTRO) VALUES (1);

Questao que nao e objetiva nem subjetiva -> erro (no commit)
-- INSERT INTO Questao (NUMERO, ENUNCIADO) VALUES (99, 'Questao sem tipo');

Horario de fim antes do inicio -> erro
-- INSERT INTO Avaliacao (CODIGO, TITULO, INICIO, FIM, DATA, REGISTRO, SIGLA)
-- VALUES (2, 'Prova invalida', '10:00', '08:00', '2026-12-02', 1, 'BD');
