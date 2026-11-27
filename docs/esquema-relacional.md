# Esquema relacional



**Usuario**{REGISTRO(PK), NOME}

NOME: NOT NULL



**Professor**{REGISTRO(PK, FK)->Usuario}



**Aluno**{REGISTRO(PK, FK)->Usuario}



**Materia**{SIGLA(PK), NOME}

NOME: NOT NULL



**Avaliacao**{CODIGO(PK), TITULO, REGISTRO(FK)->Professor, SIGLA(FK)->Materia, DESCRICAO, INICIO, FIM, DATA}

TITULO, DATA, INICIO, FIM: NOT NULL



**Questao**{NUMERO(PK), ENUNCIADO}

ENUNCIADO: NOT NULL



**Objetiva**{NUMERO(PK, FK)->Questao}



**Subjetiva**{NUMERO(PK, FK)->Questao, EXPECTATIVA}

EXPECTATIVA: NOT NULL



**Alternativas**{NUMERO(PK, FK)->Objetiva, LETRA(PK), TEXTO, CORRETA}

TEXTO, CORRETA: NOT NULL



**Cursa**{SIGLA(PK, FK)->Materia, REGISTRO(PK, FK)->Aluno}



**Faz**{REGISTRO(PK, FK)->Aluno, CODIGO(PK, FK)->Avaliacao}



**Contem**{CODIGO(PK, FK)->Avaliacao, NUMERO(PK, FK)->Questao, VALOR}

VALOR: NOT NULL



**Resposta**{REGISTRO(PK, FK)->Aluno, CODIGO(PK, FK)->Avaliacao, NUMERO(PK, FK)->Questao, RESPOSTA, NOTA}

RESPOSTA: NOT NULL

