-- =====================================================
-- Sistema de Avaliacoes - Script de criacao (PostgreSQL)
-- Versao final, alinhada ao DER:
--  * Avaliacao (CODIGO) aplicada por Professor e pertencente a Materia
--  * Faz (Aluno N:N Avaliacao) e Contem (Avaliacao N:N Questao)
--  * Resposta (relacionamento entre Faz e Contem), com RESPOSTA e NOTA
--  * Especializacoes disjuntas e totais (Usuario e Questao)
--  * Nota automatica nas objetivas e view de nota total
--
-- ATENCAO: o script APAGA e recria todas as tabelas do banco
-- (ver bloco "Limpeza"). Rode-o em um banco de estudo/teste.
-- =====================================================

BEGIN;

-- -----------------------------------------------------
-- Limpeza (permite rodar o script mais de uma vez)
-- -----------------------------------------------------
DROP VIEW  IF EXISTS Nota_Total   CASCADE;
DROP TABLE IF EXISTS Resposta     CASCADE;
DROP TABLE IF EXISTS Possui       CASCADE;
DROP TABLE IF EXISTS Contem       CASCADE;
DROP TABLE IF EXISTS Faz          CASCADE;
DROP TABLE IF EXISTS Avaliacao    CASCADE;
DROP TABLE IF EXISTS Subjetiva    CASCADE;
DROP TABLE IF EXISTS Alternativas CASCADE;
DROP TABLE IF EXISTS Objetiva     CASCADE;
DROP TABLE IF EXISTS Questao      CASCADE;
DROP TABLE IF EXISTS Materia      CASCADE;
DROP TABLE IF EXISTS Aluno        CASCADE;
DROP TABLE IF EXISTS Professor    CASCADE;
DROP TABLE IF EXISTS Usuario      CASCADE;
DROP FUNCTION IF EXISTS fn_resposta_nota()    CASCADE;
DROP FUNCTION IF EXISTS fn_usuario_disjunto() CASCADE;
DROP FUNCTION IF EXISTS fn_usuario_total()    CASCADE;
DROP FUNCTION IF EXISTS fn_questao_disjunta() CASCADE;
DROP FUNCTION IF EXISTS fn_questao_total()    CASCADE;

-- =====================================================
-- TABELAS
-- =====================================================

-- Usuario
CREATE TABLE Usuario (
    REGISTRO  INTEGER      NOT NULL,
    NOME      VARCHAR(150) NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (REGISTRO)
);

-- Professor (especializacao de Usuario)
CREATE TABLE Professor (
    REGISTRO  INTEGER NOT NULL,
    CONSTRAINT pk_professor PRIMARY KEY (REGISTRO),
    CONSTRAINT fk_professor_usuario FOREIGN KEY (REGISTRO)
        REFERENCES Usuario (REGISTRO)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Aluno (especializacao de Usuario)
CREATE TABLE Aluno (
    REGISTRO  INTEGER NOT NULL,
    CONSTRAINT pk_aluno PRIMARY KEY (REGISTRO),
    CONSTRAINT fk_aluno_usuario FOREIGN KEY (REGISTRO)
        REFERENCES Usuario (REGISTRO)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Materia
CREATE TABLE Materia (
    SIGLA  VARCHAR(10)  NOT NULL,
    NOME   VARCHAR(150) NOT NULL,
    CONSTRAINT pk_materia PRIMARY KEY (SIGLA)
);

-- Possui (Usuario N:N Materia)
CREATE TABLE Possui (
    SIGLA     VARCHAR(10) NOT NULL,
    REGISTRO  INTEGER     NOT NULL,
    CONSTRAINT pk_possui PRIMARY KEY (SIGLA, REGISTRO),
    CONSTRAINT fk_possui_materia FOREIGN KEY (SIGLA)
        REFERENCES Materia (SIGLA)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_possui_usuario FOREIGN KEY (REGISTRO)
        REFERENCES Usuario (REGISTRO)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Avaliacao
--   REGISTRO : relacionamento APLICA    (Professor 1 : N Avaliacao)
--   SIGLA    : relacionamento PERTENCE  (Materia   1 : N Avaliacao)
CREATE TABLE Avaliacao (
    CODIGO     INTEGER      NOT NULL,
    TITULO     VARCHAR(150) NOT NULL,
    DESCRICAO  TEXT,
    INICIO     TIME         NOT NULL,
    FIM        TIME         NOT NULL,
    DATA       DATE         NOT NULL,
    REGISTRO   INTEGER      NOT NULL,
    SIGLA      VARCHAR(10)  NOT NULL,
    CONSTRAINT pk_avaliacao PRIMARY KEY (CODIGO),
    CONSTRAINT ck_avaliacao_horario CHECK (FIM > INICIO),
    CONSTRAINT fk_avaliacao_professor FOREIGN KEY (REGISTRO)
        REFERENCES Professor (REGISTRO)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_avaliacao_materia FOREIGN KEY (SIGLA)
        REFERENCES Materia (SIGLA)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Faz (Aluno N:N Avaliacao)
CREATE TABLE Faz (
    REGISTRO  INTEGER NOT NULL,
    CODIGO    INTEGER NOT NULL,
    RESPONDIDA BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_faz PRIMARY KEY (REGISTRO, CODIGO),
    CONSTRAINT fk_faz_aluno FOREIGN KEY (REGISTRO)
        REFERENCES Aluno (REGISTRO)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_faz_avaliacao FOREIGN KEY (CODIGO)
        REFERENCES Avaliacao (CODIGO)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Questao
CREATE TABLE Questao (
    NUMERO     INTEGER NOT NULL,
    ENUNCIADO  TEXT    NOT NULL,
    CONSTRAINT pk_questao PRIMARY KEY (NUMERO)
);

-- Objetiva (especializacao de Questao)
CREATE TABLE Objetiva (
    NUMERO   INTEGER      NOT NULL,
    CORRETA  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_objetiva PRIMARY KEY (NUMERO),
    CONSTRAINT fk_objetiva_questao FOREIGN KEY (NUMERO)
        REFERENCES Questao (NUMERO)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Alternativas (atributo multivalorado de Objetiva)
CREATE TABLE Alternativas (
    NUMERO       INTEGER      NOT NULL,
    ALTERNATIVA  VARCHAR(255) NOT NULL,
    DESCRICAO   VARCHAR(500) NOT NULL DEFAULT '',
    CONSTRAINT pk_alternativas PRIMARY KEY (NUMERO, ALTERNATIVA),
    CONSTRAINT fk_alternativas_objetiva FOREIGN KEY (NUMERO)
        REFERENCES Objetiva (NUMERO)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- A resposta correta precisa ser uma das alternativas.
-- A FK e adiada ate o COMMIT, pois Objetiva e Alternativas se referenciam:
-- cadastre as duas na mesma transacao (BEGIN ... COMMIT).
ALTER TABLE Objetiva
    ADD CONSTRAINT fk_objetiva_correta FOREIGN KEY (NUMERO, CORRETA)
        REFERENCES Alternativas (NUMERO, ALTERNATIVA)
        DEFERRABLE INITIALLY DEFERRED;

-- Subjetiva (especializacao de Questao)
CREATE TABLE Subjetiva (
    NUMERO       INTEGER NOT NULL,
    EXPECTATIVA  TEXT    NOT NULL,
    CONSTRAINT pk_subjetiva PRIMARY KEY (NUMERO),
    CONSTRAINT fk_subjetiva_questao FOREIGN KEY (NUMERO)
        REFERENCES Questao (NUMERO)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Contem (Avaliacao N:N Questao)
-- A mesma questao pode estar em varias avaliacoes.
CREATE TABLE Contem (
    CODIGO  INTEGER      NOT NULL,
    NUMERO  INTEGER      NOT NULL,
    VALOR   NUMERIC(5,2) NOT NULL,
    CONSTRAINT pk_contem PRIMARY KEY (CODIGO, NUMERO),
    CONSTRAINT fk_contem_avaliacao FOREIGN KEY (CODIGO)
        REFERENCES Avaliacao (CODIGO)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_contem_questao FOREIGN KEY (NUMERO)
        REFERENCES Questao (NUMERO)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_contem_valor CHECK (VALOR >= 0)
);

-- Resposta (relacionamento entre Faz e Contem)
-- NOTA nula indica resposta subjetiva ainda nao corrigida. Nota zero e uma correcao valida.
CREATE TABLE Resposta (
    REGISTRO  INTEGER      NOT NULL,
    CODIGO    INTEGER      NOT NULL,
    NUMERO    INTEGER      NOT NULL,
    RESPOSTA  TEXT         NOT NULL,
    NOTA      NUMERIC(5,2),
    CONSTRAINT pk_resposta PRIMARY KEY (REGISTRO, CODIGO, NUMERO),
    CONSTRAINT fk_resposta_faz FOREIGN KEY (REGISTRO, CODIGO)
        REFERENCES Faz (REGISTRO, CODIGO)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_resposta_contem FOREIGN KEY (CODIGO, NUMERO)
        REFERENCES Contem (CODIGO, NUMERO)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT ck_resposta_nota CHECK (NOTA IS NULL OR NOTA >= 0)
);

-- =====================================================
-- REGRAS DO DER: especializacoes disjuntas e totais
-- =====================================================

-- Disjuncao: um usuario nao pode ser professor e aluno ao mesmo tempo
CREATE FUNCTION fn_usuario_disjunto() RETURNS trigger AS $$
BEGIN
    IF TG_TABLE_NAME = 'professor' THEN
        IF EXISTS (SELECT 1 FROM Aluno WHERE REGISTRO = NEW.REGISTRO) THEN
            RAISE EXCEPTION 'Usuario % ja e aluno', NEW.REGISTRO;
        END IF;
    ELSE
        IF EXISTS (SELECT 1 FROM Professor WHERE REGISTRO = NEW.REGISTRO) THEN
            RAISE EXCEPTION 'Usuario % ja e professor', NEW.REGISTRO;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_professor_disjunto
BEFORE INSERT OR UPDATE OF REGISTRO ON Professor
FOR EACH ROW EXECUTE FUNCTION fn_usuario_disjunto();

CREATE TRIGGER trg_aluno_disjunto
BEFORE INSERT OR UPDATE OF REGISTRO ON Aluno
FOR EACH ROW EXECUTE FUNCTION fn_usuario_disjunto();

-- Totalidade: todo usuario precisa ser professor ou aluno (checado no COMMIT)
CREATE FUNCTION fn_usuario_total() RETURNS trigger AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Professor WHERE REGISTRO = NEW.REGISTRO)
       AND NOT EXISTS (SELECT 1 FROM Aluno WHERE REGISTRO = NEW.REGISTRO) THEN
        RAISE EXCEPTION 'Usuario % precisa ser professor ou aluno', NEW.REGISTRO;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_usuario_total
AFTER INSERT ON Usuario
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION fn_usuario_total();

-- Disjuncao: uma questao nao pode ser objetiva e subjetiva ao mesmo tempo
CREATE FUNCTION fn_questao_disjunta() RETURNS trigger AS $$
BEGIN
    IF TG_TABLE_NAME = 'objetiva' THEN
        IF EXISTS (SELECT 1 FROM Subjetiva WHERE NUMERO = NEW.NUMERO) THEN
            RAISE EXCEPTION 'Questao % ja e subjetiva', NEW.NUMERO;
        END IF;
    ELSE
        IF EXISTS (SELECT 1 FROM Objetiva WHERE NUMERO = NEW.NUMERO) THEN
            RAISE EXCEPTION 'Questao % ja e objetiva', NEW.NUMERO;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_objetiva_disjunta
BEFORE INSERT OR UPDATE OF NUMERO ON Objetiva
FOR EACH ROW EXECUTE FUNCTION fn_questao_disjunta();

CREATE TRIGGER trg_subjetiva_disjunta
BEFORE INSERT OR UPDATE OF NUMERO ON Subjetiva
FOR EACH ROW EXECUTE FUNCTION fn_questao_disjunta();

-- Totalidade: toda questao precisa ser objetiva ou subjetiva (checado no COMMIT)
CREATE FUNCTION fn_questao_total() RETURNS trigger AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Objetiva WHERE NUMERO = NEW.NUMERO)
       AND NOT EXISTS (SELECT 1 FROM Subjetiva WHERE NUMERO = NEW.NUMERO) THEN
        RAISE EXCEPTION 'Questao % precisa ser objetiva ou subjetiva', NEW.NUMERO;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_questao_total
AFTER INSERT ON Questao
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION fn_questao_total();

-- =====================================================
-- RESPOSTA: correcao automatica das objetivas e validacao da nota
-- =====================================================
CREATE FUNCTION fn_resposta_nota() RETURNS trigger AS $$
DECLARE
    v_valor    NUMERIC(5,2);
    v_correta  VARCHAR(255);
BEGIN
    -- A questao precisa pertencer a avaliacao
    SELECT VALOR INTO v_valor
      FROM Contem
     WHERE CODIGO = NEW.CODIGO
       AND NUMERO = NEW.NUMERO;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'A questao % nao pertence a avaliacao %',
            NEW.NUMERO, NEW.CODIGO;
    END IF;

    SELECT CORRETA INTO v_correta
      FROM Objetiva
     WHERE NUMERO = NEW.NUMERO;

    IF FOUND THEN
        -- Objetiva: a resposta deve ser uma alternativa; nota automatica
        IF NOT EXISTS (SELECT 1 FROM Alternativas
                        WHERE NUMERO = NEW.NUMERO
                          AND ALTERNATIVA = NEW.RESPOSTA) THEN
            RAISE EXCEPTION 'A resposta "%" nao e uma alternativa da questao %',
                NEW.RESPOSTA, NEW.NUMERO;
        END IF;
        NEW.NOTA := CASE WHEN NEW.RESPOSTA = v_correta THEN v_valor ELSE 0 END;
    ELSIF NEW.NOTA IS NOT NULL AND NEW.NOTA > v_valor THEN
        -- Subjetiva: a nota dada pelo professor nao pode passar do valor da questao
        RAISE EXCEPTION 'Nota (%) maior que o valor da questao (%)', NEW.NOTA, v_valor;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_resposta_nota
BEFORE INSERT OR UPDATE ON Resposta
FOR EACH ROW EXECUTE FUNCTION fn_resposta_nota();

-- =====================================================
-- NOTA TOTAL (atributo derivado de Faz)
-- =====================================================
CREATE VIEW Nota_Total AS
SELECT f.REGISTRO,
       u.NOME,
       f.CODIGO,
       a.TITULO,
       COALESCE(SUM(r.NOTA), 0) AS NOTA_TOTAL
  FROM Faz f
  JOIN Usuario   u ON u.REGISTRO = f.REGISTRO
  JOIN Avaliacao a ON a.CODIGO   = f.CODIGO
  LEFT JOIN Resposta r
         ON r.REGISTRO = f.REGISTRO
        AND r.CODIGO   = f.CODIGO
 GROUP BY f.REGISTRO, u.NOME, f.CODIGO, a.TITULO;

COMMIT;
