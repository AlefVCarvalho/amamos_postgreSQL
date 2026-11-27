# Sistema de Avaliações

Este projeto foi desenvolvido para a disciplina de Banco de Dados I. A aplicação permite cadastrar questões, montar avaliações, indicar os alunos que devem realizá-las, registrar respostas e acompanhar os resultados.

Escolhemos trabalhar com avaliações de aprendizagem. Cada questão tem um valor definido dentro da avaliação. As questões objetivas recebem nota automaticamente, enquanto as questões subjetivas ficam aguardando a correção do professor. A nota total é calculada a partir das notas das questões.

## Integrantes

- Alef Vinicius Caires Carvalho
- Emilly Victoria Zamperlini

**Curso:** Ciência de Dados e Inteligência Artificial  
**Disciplina:** Banco de Dados I  
**Natureza:** Avaliação de aprendizagem

## Tecnologias

- Java 17
- Spring Boot 3.5.7
- Spring JDBC e JdbcTemplate
- PostgreSQL
- HTML, CSS e JavaScript
- Chart.js
- Maven

O acesso ao banco é feito por JDBC e pelas classes DAO do projeto. Não utilizamos JPA ou Hibernate.

## Organização das pastas

```text
amamos_postgreSQL/
├── README.md
├── pom.xml
├── .gitignore
├── database/
│   ├── schema.sql
│   ├── carga.sql
│   └── relatorios.sql
├── docs/
│   ├── DER.png
│   ├── DER.md
│   └── esquema-relacional.md
└── src/main/
    ├── java/br/uel/avaliacoes/
    │   ├── config/
    │   ├── controller/
    │   ├── dao/
    │   ├── model/
    │   └── service/
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html
            ├── css/
            └── js/
```

## Pré-requisitos

- JDK 17
- Maven 3.6 ou superior
- PostgreSQL
- Navegador atualizado

É necessário ter o PostgreSQL em execução antes de iniciar a aplicação.

## Criar e carregar o banco

Crie um banco chamado `avaliacoes`. Pelo terminal do PostgreSQL, por exemplo:

```sql
CREATE DATABASE avaliacoes;
```

Conecte-se ao banco e execute os scripts nesta ordem:

```bash
psql -U postgres -d avaliacoes -f database/schema.sql
psql -U postgres -d avaliacoes -f database/carga.sql
```

O `schema.sql` cria as tabelas, chaves, restrições, gatilhos e a visão `Nota_Total`. Ele recria as estruturas do projeto; por isso, deve ser executado somente em um banco de estudo ou quando os dados existentes puderem ser apagados.

O `carga.sql` insere dados para demonstração: 20 usuários, sendo 2 professores e 18 alunos, 3 matérias, 30 questões e 5 avaliações, além de designações e respostas para os relatórios.

## Configurar a conexão

Abra `src/main/resources/application.properties`. Por padrão, a aplicação usa:

- Banco: `avaliacoes`
- Usuário: `postgres`
- Senha: `postgres`
- Porta: `5432`

A conexão pode ser configurada por variáveis de ambiente, sem alterar o arquivo:

```bash
DB_URL=jdbc:postgresql://localhost:5432/avaliacoes
DB_USER=postgres
DB_PASSWORD=sua_senha
```

Ajuste os valores de acordo com a instalação local do PostgreSQL.

## Iniciar a aplicação

Na pasta principal do projeto, execute:

```bash
mvn spring-boot:run
```

Também é possível gerar o pacote:

```bash
mvn clean package
java -jar target/sistema-avaliacoes-1.0.0.jar
```

Depois, abra [http://localhost:8080](http://localhost:8080) no navegador.

## Como usar

### Identificação

Na tela inicial, informe o registro de um usuário que esteja cadastrado no banco. O sistema consulta o registro no back-end e identifica seu papel. Registros inexistentes não entram no sistema.

Quando o usuário é aluno, a área de resposta mostra apenas as avaliações para as quais ele foi designado e que ainda não foram totalmente respondidas. O registro usado nas operações de resposta vem da sessão mantida pelo servidor; não é escolhido em um campo da tela. Um professor identificado acessa as áreas de cadastro, correção e relatórios.

Para testar com os dados de carga, os registros `3` a `20` são alunos e `1` e `2` são professores.

### Cadastrar questões

Na área **Questões**, informe o enunciado e escolha o tipo:

- **Múltipla escolha:** cadastre as alternativas e selecione a correta.
- **Texto livre:** informe a expectativa de resposta, que serve de referência para a correção do professor.

### Montar avaliações

Na área **Avaliações**, preencha título, matéria, professor, data e horários. Depois, selecione as questões, informe o valor de cada uma e marque os alunos que deverão responder. A mesma questão pode ser reutilizada em avaliações diferentes.

### Responder e corrigir

O aluno entra com seu próprio registro, abre uma avaliação disponível e envia uma resposta para cada questão. O servidor confere a sessão, a designação do aluno e se as questões enviadas pertencem à avaliação.

As questões objetivas são corrigidas automaticamente. Nas subjetivas, a nota permanece sem preenchimento até a correção do professor. Isso permite diferenciar uma resposta ainda pendente de uma resposta corrigida com nota zero. O professor encontra as respostas pendentes na área **Correção** e informa uma nota entre zero e o valor da questão.

A visão `Nota_Total` soma as notas já atribuídas às respostas de cada aluno em cada avaliação.

## Relatórios

A área **Relatórios** apresenta tabelas e gráficos para os três relatórios principais. As consultas também estão organizadas em `database/relatorios.sql`.

### 1. Média por avaliação

**Objetivo:** mostrar a média das notas e a quantidade de alunos designados para cada avaliação.

**SQL utilizado:** `LEFT JOIN` entre avaliação, designações e visão de notas; `AVG`, `COUNT(DISTINCT ...)`, `SUM`, `GROUP BY`, `WHERE` com período e `ORDER BY`.

A contagem considera os alunos efetivamente designados, inclusive quando a avaliação ainda não tem respostas.

### 2. Desempenho do aluno em comparação com a turma

**Objetivo:** apresentar a nota total do aluno e a média dos alunos da mesma avaliação.

**SQL utilizado:** `JOIN`, subconsulta correlacionada com `AVG`, `IN`, filtro por datas e faixa de nota, `GROUP BY`, `HAVING` e `ORDER BY`.

### 3. Questões objetivas com maior percentual de erro

**Objetivo:** identificar as questões objetivas com maior proporção de respostas incorretas.

**SQL utilizado:** `JOIN`, `LEFT JOIN`, `COUNT`, `FILTER`, `GROUP BY`, `HAVING`, `NULLIF`, `ORDER BY` e `FETCH FIRST`.

A consulta conta apenas respostas existentes. Questões sem respostas não entram no cálculo do percentual.

### Consultas adicionais no arquivo SQL

O arquivo também contém a consulta de alunos designados que ainda não responderam todas as questões. Ela compara a quantidade de respostas registradas com a quantidade de questões da avaliação, usando `LEFT JOIN`, subconsulta e `HAVING`.

Há ainda uma consulta de busca de avaliações pelo título com `LIKE` (`ILIKE` no PostgreSQL).

## Testes

Não há uma suíte de testes automatizados configurada no projeto. Para conferir a instalação:

1. Crie um banco vazio e execute `schema.sql`.
2. Execute `carga.sql` logo depois e confira se os dados foram inseridos.
3. Inicie a aplicação com Maven.
4. Entre com o registro `3` e confira se aparecem somente as avaliações designadas para esse aluno.
5. Tente entrar com um registro inexistente e com um registro de professor na área de aluno; o acesso de aluno deve ser recusado.
6. Responda uma avaliação designada e confira o registro das respostas no banco.
7. Entre com um registro de professor, confira as respostas subjetivas pendentes e atribua uma nota zero a uma delas. Depois de salvar, ela não deve continuar na lista de pendências.
8. Consulte `Nota_Total` e confira a soma das notas.
9. Abra a área de relatórios e confira as tabelas e os gráficos.
10. Execute as consultas de `database/relatorios.sql` no PostgreSQL.

Para compilar o projeto, use `mvn clean package`.


## Atualização das melhorias da interface

Se o banco já estava criado antes desta versão, execute também `database/atualizacao_melhorias.sql` no Query Tool do pgAdmin, conectado ao banco `avaliacoes`. Esse script adiciona o controle de avaliação respondida uma única vez e a descrição das alternativas. Para bancos criados do zero, execute `schema.sql` e depois `carga.sql`.

Ao cadastrar uma questão objetiva, informe uma alternativa por linha no formato `A) Texto da alternativa`, `B) Texto da alternativa` etc. O código (A, B, C ou D) é usado na correção automática e o texto é mostrado ao aluno.
