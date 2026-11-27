-- Relatório 1: média por avaliação e quantidade de alunos designados.
-- LEFT JOIN mantém avaliações sem participantes; COUNT DISTINCT conta apenas alunos designados.
SELECT a.CODIGO,a.TITULO,ROUND(AVG(nt.NOTA_TOTAL),2) AS MEDIA,
       COUNT(DISTINCT f.REGISTRO) AS PARTICIPANTES,
       COALESCE(SUM(nt.NOTA_TOTAL),0) AS SOMA_NOTAS
FROM Avaliacao a
LEFT JOIN Faz f ON f.CODIGO=a.CODIGO
LEFT JOIN Nota_Total nt ON nt.CODIGO=a.CODIGO AND nt.REGISTRO=f.REGISTRO
WHERE a.DATA BETWEEN DATE '2026-01-01' AND DATE '2027-12-31'
GROUP BY a.CODIGO,a.TITULO,a.DATA
ORDER BY a.DATA;

-- Relatório 2: nota do aluno comparada à média dos alunos da mesma avaliação.
-- Usa JOIN, subconsulta, AVG, GROUP BY, HAVING, IN, faixa de datas e ORDER BY.
SELECT nt.REGISTRO,nt.NOME,nt.CODIGO,nt.TITULO,nt.NOTA_TOTAL,
       (SELECT ROUND(AVG(nt2.NOTA_TOTAL),2) FROM Nota_Total nt2 WHERE nt2.CODIGO=nt.CODIGO) AS MEDIA_TURMA
FROM Nota_Total nt
JOIN Faz f ON f.REGISTRO=nt.REGISTRO AND f.CODIGO=nt.CODIGO
WHERE nt.CODIGO IN (SELECT CODIGO FROM Avaliacao WHERE DATA BETWEEN DATE '2026-01-01' AND DATE '2027-12-31')
  AND nt.NOTA_TOTAL BETWEEN 0 AND 100
GROUP BY nt.REGISTRO,nt.NOME,nt.CODIGO,nt.TITULO,nt.NOTA_TOTAL
HAVING SUM(nt.NOTA_TOTAL)>=0
ORDER BY nt.CODIGO,nt.NOTA_TOTAL DESC;

-- Relatório 3: questões objetivas com maior percentual de erro.
-- Conta somente respostas existentes de questões objetivas, sem contar linhas vazias do LEFT JOIN.
SELECT q.NUMERO,q.ENUNCIADO,COUNT(r.REGISTRO) AS TOTAL_RESPOSTAS,
       COUNT(r.REGISTRO) FILTER (WHERE r.RESPOSTA<>o.CORRETA) AS ERROS,
       ROUND(100.0*COUNT(r.REGISTRO) FILTER (WHERE r.RESPOSTA<>o.CORRETA)
       / NULLIF(COUNT(r.REGISTRO),0),2) AS PERCENTUAL_ERRO
FROM Questao q
JOIN Objetiva o ON o.NUMERO=q.NUMERO
LEFT JOIN Contem c ON c.NUMERO=q.NUMERO
LEFT JOIN Resposta r ON r.CODIGO=c.CODIGO AND r.NUMERO=c.NUMERO
GROUP BY q.NUMERO,q.ENUNCIADO
HAVING COUNT(r.REGISTRO)>0
ORDER BY PERCENTUAL_ERRO DESC NULLS LAST
FETCH FIRST 10 ROWS ONLY;

-- Relatório 4: alunos designados que ainda não responderam todas as questões.
-- A comparação considera a quantidade de respostas e a quantidade de questões da avaliação.
SELECT u.REGISTRO,u.NOME,a.CODIGO,a.TITULO,
       COUNT(r.NUMERO) AS QUESTOES_RESPONDIDAS,
       (SELECT COUNT(*) FROM Contem c WHERE c.CODIGO=a.CODIGO) AS TOTAL_QUESTOES
FROM Faz f
JOIN Usuario u ON u.REGISTRO=f.REGISTRO
JOIN Avaliacao a ON a.CODIGO=f.CODIGO
LEFT JOIN Resposta r ON r.REGISTRO=f.REGISTRO AND r.CODIGO=f.CODIGO
GROUP BY u.REGISTRO,u.NOME,a.CODIGO,a.TITULO
HAVING COUNT(r.NUMERO)<(SELECT COUNT(*) FROM Contem c WHERE c.CODIGO=a.CODIGO)
ORDER BY a.CODIGO,u.NOME;

-- Consulta de apoio: localizar avaliações pelo título.
-- Demonstra o uso de LIKE para pesquisa textual.
SELECT CODIGO,TITULO,DATA
FROM Avaliacao
WHERE TITULO ILIKE '%SQL%'
ORDER BY DATA;
