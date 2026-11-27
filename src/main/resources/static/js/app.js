// Controla a navegação, formulários, comunicação com a API e relatórios.
const api = '/api';
let questoesCache = [];
let alunosCache = [];
let avaliacoesCache = [];
let provaAtual = null;
let graficos = {};
let paginasGraficos = {};
let sessao = null;

async function requisicao(url, opcoes = {}) {
    const resposta = await fetch(api + url, { headers: { 'Content-Type': 'application/json' }, ...opcoes });
    const dados = await resposta.json().catch(() => ({}));
    if (!resposta.ok) throw new Error(dados.erro || 'Não foi possível realizar a operação.');
    return dados;
}
function avisar(texto, erro = false) {
    const caixa = document.getElementById('mensagem');
    caixa.textContent = texto; caixa.className = erro ? 'mensagem erro' : 'mensagem'; caixa.style.display = 'block';
    setTimeout(() => caixa.style.display = 'none', 4000);
}
function escapar(texto) { return String(texto ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
function mudarPagina(id) {
    document.querySelectorAll('.pagina').forEach(p => p.classList.toggle('ativa', p.id === id));
    document.querySelectorAll('.aba').forEach(b => b.classList.toggle('ativa', b.dataset.alvo === id));
    if (id === 'correcao') carregarCorrecoes();
    if (id === 'relatorios') carregarRelatorios();
}
document.querySelectorAll('.aba').forEach(b => b.addEventListener('click', () => mudarPagina(b.dataset.alvo)));

async function carregarBase() {
    try {
        if (sessao.papel === 'ALUNO') {
            avaliacoesCache = await requisicao('/avaliacoes/aluno/minhas');
            preencherSelect('avaliacaoParaResponder', avaliacoesCache, 'codigo', a => `${a.titulo} — ${a.materia}`);
            document.getElementById('alunoIdentificado').textContent = `Aluno: ${sessao.nome} (#${sessao.registro})`;
            document.querySelectorAll('.aba').forEach(b => b.classList.add('oculto'));
            document.querySelectorAll('.pagina').forEach(p => { p.classList.toggle('ativa', p.id === 'responder'); p.classList.toggle('oculto', p.id !== 'responder'); });
            document.querySelector('.navegacao').classList.add('oculto');
            document.getElementById('responder').classList.remove('oculto');
            return;
        }
        const [usuarios, materias, questoes, avaliacoes] = await Promise.all([
            requisicao('/usuarios'), requisicao('/materias'), requisicao('/questoes'),
            requisicao(sessao.papel === 'ALUNO' ? '/avaliacoes/aluno/minhas' : '/avaliacoes')
        ]);
        const professores = usuarios.filter(u => u.papel === 'PROFESSOR');
        alunosCache = usuarios.filter(u => u.papel === 'ALUNO'); questoesCache = questoes; avaliacoesCache = avaliacoes;
        preencherSelect('professorAvaliacao', professores, 'registro', u => u.nome);
        preencherSelect('materiaAvaliacao', materias, 'sigla', m => m.nome);
        preencherSelect('avaliacaoParaResponder', avaliacoesCache, 'codigo', a => `${a.titulo} — ${a.materia}`);
        document.getElementById('alunoIdentificado').textContent = `Aluno: ${sessao.nome} (#${sessao.registro})`;
        renderizarQuestoes(); renderizarSelecaoQuestoes(); renderizarSelecaoAlunos(); renderizarAvaliacoes();
        document.getElementById('totalQuestoes').textContent = questoes.length;
        document.getElementById('totalAvaliacoes').textContent = avaliacoes.length;
        document.querySelectorAll('.aba').forEach(b => b.classList.toggle('oculto', b.dataset.alvo === 'responder'));
        document.querySelectorAll('.pagina').forEach(p => p.classList.toggle('oculto', p.id === 'responder'));
        document.querySelector('.navegacao').classList.remove('oculto');
        await atualizarPendentesResumo();
    } catch (e) { avisar(e.message, true); }
}
function preencherSelect(id, itens, chave, rotulo) {
    const select = document.getElementById(id); select.innerHTML = '';
    itens.forEach(item => { const op = document.createElement('option'); op.value = item[chave]; op.textContent = rotulo(item); select.appendChild(op); });
}
function renderizarListaProgressiva(id, itens, renderItem, quantidade = 30) {
    const caixa = document.getElementById(id);
    caixa.innerHTML = '';
    if (!itens.length) { caixa.textContent = 'Nenhum registro encontrado.'; return; }
    let exibidos = 0;
    const carregar = () => {
        const parte = itens.slice(exibidos, exibidos + quantidade);
        caixa.insertAdjacentHTML('beforeend', parte.map(renderItem).join(''));
        exibidos += parte.length;
        if (botao) botao.remove();
        if (exibidos < itens.length) {
            botao = document.createElement('button');
            botao.type = 'button';
            botao.className = 'botao claro carregar-mais';
            botao.textContent = `Carregar mais (${itens.length - exibidos} restantes)`;
            botao.addEventListener('click', carregar);
            caixa.appendChild(botao);
        }
    };
    let botao = null;
    carregar();
}
function renderizarQuestoes() {
    const caixa = document.getElementById('listaQuestoes');
    if (!questoesCache.length) { caixa.textContent = 'Nenhuma questão cadastrada.'; return; }
    renderizarListaProgressiva('listaQuestoes', questoesCache, q => `<div class="linha-lista"><div><strong>${q.numero}. ${escapar(q.enunciado)}</strong><small>Questão ${String(q.tipo).toLowerCase()}</small></div><span class="tag">${q.tipo}</span></div>`);
}
function renderizarSelecaoQuestoes() {
    renderizarListaProgressiva('selecaoQuestoes', questoesCache, q => `<label class="item-selecao"><input type="checkbox" class="questao-escolhida" value="${q.numero}"><span>${q.numero}. ${escapar(q.enunciado)}</span><input type="number" class="valor-questao" data-numero="${q.numero}" min="0" step="0.5" value="1" title="Valor em pontos"></label>`);
}
function renderizarSelecaoAlunos() {
    renderizarListaProgressiva('selecaoAlunos', alunosCache, a => `<label class="item-selecao"><input type="checkbox" class="aluno-escolhido" value="${a.registro}">${escapar(a.nome)} <small>#${a.registro}</small></label>`);
}
function renderizarAvaliacoes() {
    renderizarListaProgressiva('listaAvaliacoes', avaliacoesCache, a => `<div class="linha-lista"><div><strong>${escapar(a.titulo)}</strong><small>${escapar(a.materia)} · ${a.data} · ${a.total_questoes} questões · Professor: ${escapar(a.professor)}<br>Alunos designados: ${escapar(a.alunos_designados || 'Nenhum')}</small></div><span class="tag">#${a.codigo}</span></div>`);
}

document.getElementById('tipoQuestao').addEventListener('change', e => {
    const objetiva = e.target.value === 'OBJETIVA';
    document.getElementById('camposObjetiva').classList.toggle('oculto', !objetiva);
    document.getElementById('camposSubjetiva').classList.toggle('oculto', objetiva);
});
document.getElementById('alternativas').addEventListener('input', () => {
    const valores = document.getElementById('alternativas').value.split('\n').map(v => v.trim()).filter(Boolean);
    document.getElementById('correta').innerHTML = '<option value="">Selecione a correta</option>' + valores.map(v => {
        const codigo = v.match(/^([A-Z])(?:\)|\s-)/)?.[1] || v;
        return `<option value="${escapar(codigo)}">${escapar(v)}</option>`;
    }).join('');
});
document.getElementById('formQuestao').addEventListener('submit', async e => {
    e.preventDefault();
    const tipo = document.getElementById('tipoQuestao').value;
    const dados = { enunciado: document.getElementById('enunciado').value, tipo };
    if (tipo === 'OBJETIVA') { dados.alternativas = document.getElementById('alternativas').value.split('\n').map(v => v.trim()).filter(Boolean); dados.correta = document.getElementById('correta').value; }
    else dados.expectativa = document.getElementById('expectativa').value;
    try { const r = await requisicao('/questoes', { method: 'POST', body: JSON.stringify(dados) }); avisar(`Questão ${r.numero} cadastrada.`); e.target.reset(); document.getElementById('tipoQuestao').dispatchEvent(new Event('change')); await carregarBase(); }
    catch (err) { avisar(err.message, true); }
});
document.getElementById('atualizarQuestoes').addEventListener('click', carregarBase);
document.getElementById('marcarAlunos').addEventListener('click', () => document.querySelectorAll('.aluno-escolhido').forEach(c => c.checked = true));
document.getElementById('formAvaliacao').addEventListener('submit', async e => {
    e.preventDefault();
    const questoes = [...document.querySelectorAll('.questao-escolhida:checked')].map(c => ({ numero: Number(c.value), valor: Number(document.querySelector(`.valor-questao[data-numero="${c.value}"]`).value) }));
    const alunos = [...document.querySelectorAll('.aluno-escolhido:checked')].map(c => Number(c.value));
    const dados = { titulo: document.getElementById('tituloAvaliacao').value, descricao: document.getElementById('descricaoAvaliacao').value, siglaMateria: document.getElementById('materiaAvaliacao').value, registroProfessor: Number(document.getElementById('professorAvaliacao').value), data: document.getElementById('dataAvaliacao').value, inicio: document.getElementById('inicioAvaliacao').value, fim: document.getElementById('fimAvaliacao').value, questoes, alunos };
    try { const r = await requisicao('/avaliacoes', { method: 'POST', body: JSON.stringify(dados) }); avisar(`Avaliação ${r.codigo} criada com sucesso.`); e.target.reset(); await carregarBase(); }
    catch (err) { avisar(err.message, true); }
});

document.getElementById('carregarProva').addEventListener('click', async () => {
    const codigo = document.getElementById('avaliacaoParaResponder').value;
    const aluno = sessao?.registro;
    if (!codigo || !aluno || sessao.papel !== 'ALUNO') { avisar('Entre como aluno e selecione uma avaliação.', true); return; }
    try {
        provaAtual = await requisicao(`/avaliacoes/${codigo}/aluno/${aluno}`);
        const html = provaAtual.questoes.map(q => {
            const resposta = q.tipo === 'OBJETIVA'
                ? (q.alternativas || []).map(a => `<label class="opcao"><input type="radio" name="q${q.numero}" value="${escapar(a.codigo)}"><span><strong>${escapar(a.codigo)}.</strong> ${escapar(a.texto)}</span></label>`).join('')
                : `<textarea name="q${q.numero}" rows="4" required placeholder="Digite sua resposta"></textarea>`;
            return `<article class="questao-prova"><h3>Questão ${q.numero} — ${q.valor} ponto(s)</h3><p>${escapar(q.enunciado)}</p>${resposta}</article>`;
        }).join('');
        document.getElementById('questoesProva').innerHTML = `<h2>${escapar(provaAtual.titulo)}</h2>${html}`;
        document.getElementById('formRespostas').classList.remove('oculto');
    } catch (err) { avisar(err.message, true); }
});
document.getElementById('formRespostas').addEventListener('submit', async e => {
    e.preventDefault(); if (!provaAtual) return;
    const respostas = provaAtual.questoes.map(q => {
        const campo = e.target.elements[`q${q.numero}`];
        const marcada = e.target.querySelector(`input[name="q${q.numero}"]:checked`);
        return { numero: q.numero, resposta: marcada ? marcada.value : campo.value };
    });
    try { await requisicao(`/avaliacoes/${provaAtual.codigo}/respostas/${sessao.registro}`, { method: 'POST', body: JSON.stringify({ respostas }) }); avisar('Respostas enviadas. As objetivas foram corrigidas automaticamente.'); e.target.classList.add('oculto'); provaAtual = null; await carregarBase(); await atualizarPendentesResumo(); }
    catch (err) { avisar(err.message, true); }
});

async function carregarCorrecoes() {
    try {
        const itens = await requisicao('/correcoes/pendentes');
        document.getElementById('totalPendentes').textContent = itens.length;
        const caixa = document.getElementById('listaCorrecoes');
        renderizarListaProgressiva('listaCorrecoes', itens, i => `<div class="linha-lista"><div><strong>${escapar(i.aluno)} (#${i.registro}) — ${escapar(i.titulo)}</strong><small>Questão ${i.numero}: ${escapar(i.enunciado)}<br>Resposta: ${escapar(i.resposta)}<br>Valor máximo: ${i.valor}</small></div><div style="min-width:150px"><label>Nota<input type="number" min="0" max="${i.valor}" step="0.1" id="nota-${i.registro}-${i.codigo}-${i.numero}" value="0"></label><button class="botao primario corrigir" data-registro="${i.registro}" data-codigo="${i.codigo}" data-numero="${i.numero}" data-valor="${i.valor}">Salvar nota</button></div></div>`);
        caixa.onclick = async evento => {
            const b = evento.target.closest('.corrigir');
            if (!b) return;
            const nota = Number(document.getElementById(`nota-${b.dataset.registro}-${b.dataset.codigo}-${b.dataset.numero}`).value);
            if (nota < 0 || nota > Number(b.dataset.valor)) { avisar('A nota deve estar entre zero e o valor da questão.', true); return; }
            try {
                await requisicao('/correcoes', { method: 'PUT', body: JSON.stringify({ registro: Number(b.dataset.registro), codigo: Number(b.dataset.codigo), numero: Number(b.dataset.numero), nota }) });
                avisar('Nota salva.');
                carregarCorrecoes();
            } catch (err) { avisar(err.message, true); }
        };
    } catch (e) { avisar(e.message, true); }
}

async function atualizarPendentesResumo() { try { const p = await requisicao('/correcoes/pendentes'); document.getElementById('totalPendentes').textContent = p.length; } catch (_) {} }
function tabela(dados, colunas) {
    if (!dados.length) return '<p>Sem dados para exibir.</p>';
    return `<table><thead><tr>${colunas.map(c => `<th>${c.titulo}</th>`).join('')}</tr></thead><tbody>${dados.map(l => `<tr>${colunas.map(c => `<td>${escapar(l[c.campo])}</td>`).join('')}</tr>`).join('')}</tbody></table>`;
}
function desenhar(id, tipo, labels, valores, titulo) {
    if (graficos[id]) graficos[id].destroy();
    if (!window.Chart) return;
    graficos[id] = new Chart(document.getElementById(id), {
        type: tipo,
        data: { labels, datasets: [{ label: titulo, data: valores, borderWidth: 1, backgroundColor: ['#19a78b','#345b7e','#80b9ac','#e4b363','#7995b2','#c58a8a'] }] },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { x: { beginAtZero: true } }
        }
    });
}
function desenharGraficoPaginado(id, itens, rotulo, valor, titulo) {
    const porPagina = 10;
    const estado = paginasGraficos[id] || { pagina: 0 };
    estado.itens = itens;
    estado.rotulo = rotulo;
    estado.valor = valor;
    estado.titulo = titulo;
    estado.porPagina = porPagina;
    estado.totalPaginas = Math.max(1, Math.ceil(itens.length / porPagina));
    estado.pagina = Math.min(estado.pagina, estado.totalPaginas - 1);
    paginasGraficos[id] = estado;

    const inicio = estado.pagina * porPagina;
    const parte = itens.slice(inicio, inicio + porPagina);
    desenhar(id, 'bar', parte.map(rotulo), parte.map(valor), titulo);

    const controles = document.getElementById(`${id}Controles`);
    controles.innerHTML = '';
    if (!itens.length) {
        controles.textContent = 'Sem dados para exibir no gráfico.';
        return;
    }

    const anterior = document.createElement('button');
    anterior.type = 'button';
    anterior.className = 'botao claro';
    anterior.textContent = 'Anterior';
    anterior.disabled = estado.pagina === 0;
    anterior.addEventListener('click', () => {
        estado.pagina--;
        desenharGraficoPaginado(id, estado.itens, estado.rotulo, estado.valor, estado.titulo);
    });

    const proxima = document.createElement('button');
    proxima.type = 'button';
    proxima.className = 'botao claro';
    proxima.textContent = 'Próximos';
    proxima.disabled = estado.pagina >= estado.totalPaginas - 1;
    proxima.addEventListener('click', () => {
        estado.pagina++;
        desenharGraficoPaginado(id, estado.itens, estado.rotulo, estado.valor, estado.titulo);
    });

    const indicador = document.createElement('span');
    indicador.textContent = `Exibindo ${inicio + 1}–${Math.min(inicio + porPagina, itens.length)} de ${itens.length} registros · Página ${estado.pagina + 1} de ${estado.totalPaginas}`;
    controles.append(anterior, indicador, proxima);
}
async function carregarRelatorios() {
    try {
        const [medias, alunos, questoes] = await Promise.all([requisicao('/relatorios/medias'), requisicao('/relatorios/alunos'), requisicao('/relatorios/questoes')]);
        document.getElementById('tabelaMedias').innerHTML = tabela(medias,[{campo:'titulo',titulo:'Avaliação'},{campo:'media',titulo:'Média'},{campo:'participantes',titulo:'Participantes'}]);
        desenharGraficoPaginado('graficoMedias', medias, x => x.titulo, x => Number(x.media || 0), 'Média');

        document.getElementById('tabelaAlunos').innerHTML = tabela(alunos,[{campo:'nome',titulo:'Aluno'},{campo:'titulo',titulo:'Avaliação'},{campo:'nota_total',titulo:'Nota'},{campo:'media_turma',titulo:'Média da turma'}]);
        desenharGraficoPaginado('graficoAlunos', alunos, x => `${x.nome} — ${x.titulo}`, x => Number(x.nota_total || 0), 'Nota total');

        const comDados = questoes.filter(x => x.percentual_erro !== null);
        document.getElementById('tabelaQuestoes').innerHTML = tabela(questoes,[{campo:'numero',titulo:'Questão'},{campo:'enunciado',titulo:'Enunciado'},{campo:'respostas',titulo:'Respostas'},{campo:'erros',titulo:'Erros'},{campo:'percentual_erro',titulo:'% de erros'}]);
        desenharGraficoPaginado('graficoQuestoes', comDados, x => `Q${x.numero} — ${x.enunciado}`, x => Number(x.percentual_erro || 0), '% de erros');
    } catch (e) { avisar(e.message, true); }
}

async function iniciarSessao() {
    try {
        sessao = await requisicao('/sessao');
        abrirAplicacao();
    } catch (_) {
        document.getElementById('telaLogin').classList.remove('oculto');
        document.getElementById('aplicacao').classList.add('oculto');
    }
}
function abrirAplicacao() {
    document.getElementById('telaLogin').classList.add('oculto');
    document.getElementById('aplicacao').classList.remove('oculto');
    document.getElementById('nomeLogado').textContent = `${sessao.nome} (${sessao.papel.toLowerCase()})`;
    carregarBase();
}
document.getElementById('formLogin').addEventListener('submit', async e => {
    e.preventDefault();
    const registro = Number(document.getElementById('registroLogin').value);
    try {
        sessao = await requisicao('/login', { method: 'POST', body: JSON.stringify({ registro }) });
        document.getElementById('erroLogin').textContent = '';
        abrirAplicacao();
    } catch (err) { document.getElementById('erroLogin').textContent = err.message; }
});
document.getElementById('sair').addEventListener('click', async () => {
    try { await requisicao('/logout', { method: 'POST' }); } catch (_) {}
    sessao = null;
    document.getElementById('aplicacao').classList.add('oculto');
    document.getElementById('telaLogin').classList.remove('oculto');
    document.getElementById('registroLogin').value = '';
});
iniciarSessao();
