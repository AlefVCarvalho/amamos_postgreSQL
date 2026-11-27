# Sistema de Avaliações

Sistema web desenvolvido para a disciplina de Banco de Dados I. Ele permite cadastrar questões, montar avaliações, indicar quais alunos devem responder, registrar as respostas, corrigir questões discursivas e consultar os resultados em tabelas e gráficos.

## Integrantes

* [Alef Carvalho](https://github.com/AlefVCarvalho)
* [Emilly Zamperlini](https://github.com/emillyzamperlini)

**Curso:** Ciência de Dados e Inteligência Artificial  
**Disciplina:** Banco de Dados I  
**Natureza escolhida:** Avaliação de aprendizagem

Na avaliação de aprendizagem, cada questão tem um valor definido dentro da avaliação. As questões objetivas recebem nota automaticamente. As questões subjetivas são corrigidas pelo professor. O sistema soma as notas das questões para apresentar a nota total do aluno.

## Tecnologias

* Java 17 e Spring Boot
* Spring JDBC e classes DAO
* PostgreSQL
* HTML, CSS e JavaScript
* Chart.js para os gráficos
* Maven para compilação e execução

## Organização do projeto

```text
amamos_postgreSQL/
├── database/
│   ├── schema.sql
│   ├── carga.sql
│   ├── atualizacao_melhorias.sql
│   └── relatorios.sql
├── docs/
│   ├── DER.png
│   └── esquema-relacional.md
├── src/main/
│   ├── java/br/uel/avaliacoes/
│   │   ├── config/      # Configuração do JDBC
│   │   ├── controller/  # Endpoints
│   │   ├── dao/         # Consultas SQL e operações
│   │   ├── model/       # Classes
│   │   └── service/     # Regras
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html
│           ├── css/
│           ├── js/
│           └── logo.png
├── .gitignore
├── pom.xml
└── README.md
```

## Pré-requisitos

Antes de executar, instale:

* JDK 17;
* Maven 3.6 ou superior;
* PostgreSQL;

O PostgreSQL precisa estar iniciado. Os comandos abaixo consideram que o projeto foi aberto no terminal na pasta principal, onde está o arquivo `pom.xml`.

## 1\. Criar o banco de dados

Crie um banco chamado `avaliacoes`:

```sql
CREATE DATABASE avaliacoes;
```

## 2\. Executar os scripts SQL

Execute os arquivos nesta ordem, conectando-se ao banco `avaliacoes`:

1. `database/schema.sql`
2. `database/carga.sql`

Pelo terminal, se o comando `psql` estiver instalado e disponível no PATH:

```powershell
psql -U postgres -d avaliacoes -f database/schema.sql
psql -U postgres -d avaliacoes -f database/carga.sql
```

O `schema.sql` cria as tabelas, chaves primárias e estrangeiras, restrições, funções, gatilhos e a view `Nota_Total`. **Atenção:** ele apaga e recria as estruturas do projeto. Use em um banco vazio ou quando puder perder os dados que já estão nele.

O `carga.sql` insere dados para demonstração: 20 usuários (2 professores e 18 alunos), 3 matérias, 30 questões, 5 avaliações e respostas para alimentar os relatórios.

Também foi mantido a parte a atualização prevista em `database/atualizacao_melhorias.sql`, com algumas pequenas alterações para o banco.

## 3\. Configurar a conexão com o PostgreSQL

Abra `src/main/resources/application.properties` e confira estes campos:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/avaliacoes
spring.datasource.username=postgres
spring.datasource.password=COLOQUE_SUA_SENHA
spring.datasource.driver-class-name=org.postgresql.Driver
server.port=8080
```

Troque `COLOQUE_SUA_SENHA` pela senha definida para o usuário do PostgreSQL na sua máquina. Se o usuário, a porta ou o nome do banco forem diferentes, ajuste os respectivos valores.

## 4\. Iniciar a aplicação

Na pasta principal do projeto, execute:

```powershell
mvn spring-boot:run
```

Para compilar e gerar o pacote, use:

```powershell
mvn clean package
```

Depois que a aplicação iniciar, abra no navegador:

[http://localhost:8080](http://localhost:8080)

Para encerrar, volte ao terminal e pressione `Ctrl + C`.

## Como utilizar o sistema

### Identificação do usuário

Na tela inicial, informe o registro cadastrado no banco. O back-end consulta o registro e identifica se ele pertence a um aluno ou professor. O sistema não utiliza senha; essa identificação simples atende ao escopo acadêmico do projeto.

Com os dados de carga, os registros `1` e `2` são professores, e os registros de `3` a `20` são alunos. Um registro inexistente não consegue entrar. As operações da área do aluno usam o registro guardado na sessão do servidor.

### Cadastrar questões

Na aba **Questões**, informe o enunciado e escolha o tipo:

* **Múltipla escolha:** informe as alternativas, uma por linha, no formato `A) Texto da alternativa`, `B) Texto da alternativa` etc. Depois selecione a alternativa correta.
* **Texto livre:** informe a expectativa de resposta, que serve de referência para a correção do professor.

As questões cadastradas podem ser reutilizadas em mais de uma avaliação.

### Criar uma avaliação

Na aba **Avaliações**, informe título, matéria, professor responsável, data, horário de início, horário de fim e descrição. Em seguida, selecione as questões, informe o valor de cada uma e marque os alunos que devem responder.

A mesma questão pode participar de avaliações diferentes e ter um valor diferente em cada uma. A lista de avaliações também apresenta os alunos designados.

### Responder e corrigir

O aluno visualiza somente as avaliações para as quais foi designado. Ao abrir uma avaliação, responde às questões e envia as respostas. O back-end verifica a identificação da sessão e se as questões pertencem à avaliação. Uma avaliação só pode ser enviada uma vez por aluno.

As questões objetivas são corrigidas automaticamente. As respostas subjetivas ficam com nota nula até o professor corrigir. Na aba **Correção**, o professor consulta as respostas pendentes e atribui uma nota respeitando o valor máximo da questão. Uma nota zero é uma correção válida e não permanece como pendência.

A visão `Nota_Total`, criada no banco, soma as notas já atribuídas para cada aluno em cada avaliação.

## Relatórios

A aba **Relatórios** apresenta três relatórios, cada um com tabela e gráfico. As consultas SQL correspondentes e consultas complementares estão em `database/relatorios.sql`.

### 1\. Média por avaliação

**Objetivo:** mostrar a média das notas e a quantidade de alunos designados em cada avaliação.

**SQL utilizado:** consulta que parte de `Avaliacao` e usa `LEFT JOIN` com `Faz` e `Nota_Total`. Usa `AVG` para a média, `COUNT(DISTINCT ...)` para contar alunos designados, `SUM` para somar as notas, `GROUP BY` para agrupar por avaliação, `WHERE` para limitar o período e `ORDER BY` para ordenar por data.

### 2\. Desempenho do aluno em comparação com a turma

**Objetivo:** mostrar a nota total de cada aluno e a média dos alunos na mesma avaliação.

**SQL utilizado:** `JOIN` entre `Nota\_Total` e `Faz`, subconsulta para calcular a média da avaliação, `IN` para filtrar avaliações dentro do período, `WHERE` para filtrar datas e faixa de notas, `GROUP BY`, `HAVING` e `ORDER BY`.

### 3\. Questões objetivas com maior percentual de erro

**Objetivo:** identificar as questões objetivas com maior proporção de respostas incorretas. O relatório considera apenas respostas existentes, sem contar linhas vazias produzidas pelo `LEFT JOIN`.

**SQL utilizado:** `JOIN` com `Objetiva`, `LEFT JOIN` com `Contem` e `Resposta`, `COUNT` para contar respostas e erros, `FILTER` para contar somente as incorretas, `GROUP BY`, `HAVING`, `ORDER BY` e `FETCH FIRST` para limitar às dez primeiras. `NULLIF` evita divisão por zero.

### Consultas complementares

O arquivo `database/relatorios.sql` também possui uma consulta para encontrar alunos designados que ainda não responderam todas as questões da avaliação. Ela compara a quantidade de respostas registradas com a quantidade de questões da avaliação, usando `LEFT JOIN`, subconsulta, `COUNT`, `GROUP BY`, `HAVING` e `ORDER BY`.

Há também uma consulta de apoio que pesquisa avaliações pelo título com `LIKE`.

## Testes
1. Crie um banco de teste e execute `schema.sql` e `carga.sql`.
2. Inicie a aplicação com `mvn spring-boot:run`.
3. Entre com o registro `3` e confira se aparecem apenas as avaliações designadas para esse aluno.
4. Tente entrar com um registro inexistente e com um registro de professor na área de aluno.
5. Responda uma avaliação designada e confira se as respostas foram gravadas.
6. Tente enviar a mesma avaliação novamente; o sistema deve impedir o segundo envio.
7. Entre como professor, confira as respostas subjetivas pendentes e atribua uma nota a uma resposta.
8. Consulte a visão `Nota_Total` e a soma das notas.
9. Abra a aba **Relatórios** e confira as tabelas, os gráficos e a paginação.
10. Execute as consultas de `database/relatorios.sql` no pgAdmin ou pelo `psql`.
11. Execute `mvn clean package` para verificar a compilação.

## Documentação do banco

* `docs/DER.png`: diagrama entidade-relacionamento.
* `docs/esquema-relacional.md`: tabelas, chaves primárias, chaves estrangeiras e justificativa da terceira forma normal (3FN).
* `database/schema.sql`: criação das tabelas, restrições, funções, gatilhos e visão de nota total.
* `database/carga.sql`: dados utilizados nos testes e demonstrações.
* `database/relatorios.sql`: consultas SQL dos relatórios.

O esquema relacional foi organizado para evitar repetição de dados e dependências indevidas: os relacionamentos muitos-para-muitos ficam em tabelas próprias, e os atributos dependem da chave de sua relação. Essa organização busca atender à terceira forma normal (3FN).