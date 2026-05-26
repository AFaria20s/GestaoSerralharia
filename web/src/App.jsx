import { useEffect, useMemo, useState } from 'react';

const NAV_ITEMS = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'equipa', label: 'Minha Equipa' },
  { key: 'tarefas', label: 'Minhas Tarefas' },
  { key: 'materiais', label: 'Materiais' },
  { key: 'problemas', label: 'Reportar Problema' },
  { key: 'progresso', label: 'Registar Progresso' },
  { key: 'equipaInfo', label: 'Info da Equipa' },
];

const TASK_FILTERS = [
  { key: 'all', label: 'Todas' },
  { key: 'today', label: 'Hoje' },
  { key: 'week', label: '7 dias' },
  { key: 'month', label: '30 dias' },
  { key: 'overdue', label: 'Atrasadas' },
];

async function api(path, options = {}) {
  const res = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Erro ${res.status}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

function toDate(value) {
  return value ? new Date(value) : null;
}

function filterTasks(tasks, mode) {
  const now = new Date();
  const oneDay = 24 * 60 * 60 * 1000;
  return tasks.filter((t) => {
    const deadline = toDate(t.dataLimite);
    if (!deadline) return mode === 'all';
    const diff = deadline.setHours(0, 0, 0, 0) - new Date(now).setHours(0, 0, 0, 0);
    if (mode === 'today') return diff === 0;
    if (mode === 'week') return diff >= 0 && diff <= 7 * oneDay;
    if (mode === 'month') return diff >= 0 && diff <= 30 * oneDay;
    if (mode === 'overdue') return diff < 0;
    return true;
  });
}

export default function App() {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('funcionario');
    return raw ? JSON.parse(raw) : null;
  });
  const [active, setActive] = useState('dashboard');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [tasks, setTasks] = useState([]);
  const [allTeams, setAllTeams] = useState([]);
  const [budgetLines, setBudgetLines] = useState([]);
  const [gravidades, setGravidades] = useState([]);
  const [filter, setFilter] = useState('all');
  const [selectedObraMaterials, setSelectedObraMaterials] = useState('');
  const [selectedTaskMaterials, setSelectedTaskMaterials] = useState('');
  const [problemForm, setProblemForm] = useState({ idTarefa: '', idGravidade: '', descricao: '' });
  const [progressForm, setProgressForm] = useState({ idObra: '', notas: '' });
  const [teamMembers, setTeamMembers] = useState([]);
  const [teamsWithMembers, setTeamsWithMembers] = useState([]);

  const myTeam = useMemo(() => {
    const t = tasks.find((task) => task.idEquipa);
    return t?.idEquipa || null;
  }, [tasks]);
  const employeeTeams = useMemo(
    () => teamsWithMembers.filter((tw) => tw.members.some((m) => m.idFuncionario?.id === user?.id)),
    [teamsWithMembers, user?.id],
  );

  const myTasksFiltered = useMemo(() => filterTasks(tasks, filter), [tasks, filter]);
  const myWorkIds = useMemo(() => [...new Set(tasks.map((t) => t.idObra?.id).filter(Boolean))], [tasks]);
  const tasksBySelectedObra = useMemo(
    () => tasks.filter((t) => String(t.idObra?.id || '') === String(selectedObraMaterials || '')),
    [tasks, selectedObraMaterials],
  );

  const materialsByObraLines = useMemo(() => {
    const selectedLines = budgetLines.filter(
      (l) => String(l.idOrcamento?.idObra?.id || '') === String(selectedObraMaterials || '') && l.idMaterial?.id,
    );

    if (!selectedTaskMaterials) return selectedLines;

    const selectedTask = tasks.find((t) => String(t.id) === String(selectedTaskMaterials));
    if (!selectedTask?.descricao) return selectedLines;

    const tokens = selectedTask.descricao
      .toLowerCase()
      .split(/\s+/)
      .map((w) => w.trim())
      .filter((w) => w.length >= 4);

    if (tokens.length === 0) return selectedLines;

    const filtered = selectedLines.filter((line) => {
      const text = `${line.nome || ''} ${line.idMaterial?.nome || ''}`.toLowerCase();
      return tokens.some((token) => text.includes(token));
    });
    return filtered.length > 0 ? filtered : selectedLines;
  }, [budgetLines, selectedObraMaterials, selectedTaskMaterials, tasks]);

  async function loadData(funcionarioId) {
    setLoading(true);
    setError('');
    try {
      const [t, equipas, grav, linhas] = await Promise.all([
        api(`/tarefas/funcionario/${funcionarioId}`),
        api('/equipas'),
        api('/lookups/gravidades-problema'),
        api('/linhas-orcamento'),
      ]);
      setTasks(t);
      setAllTeams(equipas);
      setGravidades(grav);
      setBudgetLines(linhas);
    } catch (e) {
      setError(`Falha ao carregar dados: ${e.message}`);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (user?.id) loadData(user.id);
  }, [user?.id]);

  useEffect(() => {
    if (!myTeam?.id) return;
    api(`/equipas/${myTeam.id}/membros`)
      .then(setTeamMembers)
      .catch(() => setTeamMembers([]));
  }, [myTeam?.id]);

  useEffect(() => {
    if (!allTeams.length || !user?.id) return;
    Promise.all(
      allTeams.map(async (t) => {
        const members = await api(`/equipas/${t.id}/membros`).catch(() => []);
        return { team: t, members };
      }),
    )
      .then(setTeamsWithMembers)
      .catch(() => setTeamsWithMembers([]));
  }, [allTeams, user?.id]);

  useEffect(() => {
    if (!myWorkIds.length) return;
    if (!selectedObraMaterials || !myWorkIds.includes(Number(selectedObraMaterials))) {
      setSelectedObraMaterials(String(myWorkIds[0]));
      setSelectedTaskMaterials('');
    }
  }, [myWorkIds, selectedObraMaterials]);

  async function onLogin(event) {
    event.preventDefault();
    setError('');
    const form = new FormData(event.currentTarget);
    const email = String(form.get('email') || '').trim();
    const password = String(form.get('password') || '');
    try {
      const funcionario = await api('/funcionarios/autenticar', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      localStorage.setItem('funcionario', JSON.stringify(funcionario));
      setUser(funcionario);
    } catch {
      setError('Credenciais inválidas.');
    }
  }

  async function submitProblem(event) {
    event.preventDefault();
    if (!problemForm.idTarefa || !problemForm.idGravidade || !problemForm.descricao.trim()) {
      setError('Preenche tarefa, gravidade e descrição.');
      return;
    }
    const task = tasks.find((t) => t.id === Number(problemForm.idTarefa));
    if (!task?.idObra?.id) return setError('Tarefa sem obra associada.');
    await api('/problemas', {
      method: 'POST',
      body: JSON.stringify({
        idObra: { id: task.idObra.id },
        idTarefa: { id: Number(problemForm.idTarefa) },
        idGravidade: { id: Number(problemForm.idGravidade) },
        descricao: problemForm.descricao.trim(),
      }),
    });
    setProblemForm({ idTarefa: '', idGravidade: '', descricao: '' });
    setError('');
    alert('Problema reportado com sucesso.');
  }

  async function submitProgress(event) {
    event.preventDefault();
    if (!progressForm.idObra || !progressForm.notas.trim()) {
      setError('Seleciona a obra e escreve as notas.');
      return;
    }
    await api('/visitas', {
      method: 'POST',
      body: JSON.stringify({
        idObra: { id: Number(progressForm.idObra) },
        dataVisita: new Date().toISOString(),
        notasMedicoes: progressForm.notas.trim(),
      }),
    });
    setProgressForm({ idObra: '', notas: '' });
    setError('');
    alert('Progresso registado.');
  }

  function logout() {
    localStorage.removeItem('funcionario');
    setUser(null);
    setTasks([]);
    setAllTeams([]);
    setTeamMembers([]);
  }

  if (!user) {
    return (
      <div className="login-root">
        <form className="login-card" onSubmit={onLogin}>
          <h1>Gestão Serralharia</h1>
          <p>Área do Funcionário</p>
          <label htmlFor="email">Email</label>
          <input id="email" name="email" type="email" required />
          <label htmlFor="password">Password</label>
          <input id="password" name="password" type="password" required />
          {error && <div className="error">{error}</div>}
          <button type="submit">Entrar</button>
        </form>
      </div>
    );
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <h2>Funcionário</h2>
          <small>{user.nome}</small>
        </div>
        <nav>
          {NAV_ITEMS.map((item) => (
            <button key={item.key} className={active === item.key ? 'nav-btn active' : 'nav-btn'} onClick={() => setActive(item.key)}>
              {item.label}
            </button>
          ))}
        </nav>
        <button className="logout-btn" onClick={logout}>
          Terminar sessão
        </button>
      </aside>
      <main className="content">
        <header className="content-header">
          <h1>{NAV_ITEMS.find((n) => n.key === active)?.label}</h1>
          {loading && <span>A carregar...</span>}
        </header>
        {error && <div className="error">{error}</div>}

        {active === 'equipa' && (
          <section className="panel">
            <h3>Informação principal</h3>
            <p><strong>Funcionário:</strong> {user.nome}</p>
            <p><strong>Total de equipas:</strong> {employeeTeams.length}</p>
            <p><strong>Equipa ativa atual:</strong> {myTeam?.nomeEquipa || 'Sem equipa ativa nas tarefas atuais'}</p>
            <h3>Equipas em que estás</h3>
            <div className="list">
              {employeeTeams.map(({ team }) => (
                <article className="list-item" key={team.id}>
                  <h4>{team.nomeEquipa}</h4>
                  <p>Obra: {team.idObra?.descricao || '-'}</p>
                  <p>Local: {team.idObra ? `${team.idObra?.rua || ''}, ${team.idObra?.nporta || ''} - ${team.idObra?.localidade || ''}` : '-'}</p>
                  <p>Código postal: {team.idObra?.idCodpostal?.codpostal || '-'}</p>
                </article>
              ))}
              {employeeTeams.length === 0 && <p>Sem equipas associadas.</p>}
            </div>
          </section>
        )}

        {active === 'dashboard' && (
          <section className="panel">
            <h3>Resumo diário</h3>
            <div className="list">
              <article className="list-item">
                <h4>Tarefas de hoje</h4>
                <p>{filterTasks(tasks, 'today').length}</p>
              </article>
              <article className="list-item">
                <h4>Tarefas atrasadas</h4>
                <p>{filterTasks(tasks, 'overdue').length}</p>
              </article>
              <article className="list-item">
                <h4>Obras ativas contigo</h4>
                <p>{myWorkIds.length}</p>
              </article>
              <article className="list-item">
                <h4>Problemas por reportar</h4>
                <p>Usa o separador "Reportar Problema" quando houver incidente em obra.</p>
              </article>
            </div>
          </section>
        )}

        {active === 'tarefas' && (
          <section className="panel">
            <div className="chips">
              {TASK_FILTERS.map((f) => (
                <button key={f.key} className={filter === f.key ? 'chip active' : 'chip'} onClick={() => setFilter(f.key)}>
                  {f.label}
                </button>
              ))}
            </div>
            <div className="list">
              {myTasksFiltered.map((t) => (
                <article className="list-item" key={t.id}>
                  <h4>{t.descricao || `Tarefa #${t.id}`}</h4>
                  <p>Limite: {t.dataLimite || '-'}</p>
                  <p>Estado: {t.idEstadoTarefa?.nomeEstado || '-'}</p>
                  <p>Obra: {t.idObra?.descricao || '-'}</p>
                  <p>Local: {t.idObra ? `${t.idObra?.rua || ''}, ${t.idObra?.nporta || ''} - ${t.idObra?.localidade || ''}` : '-'}</p>
                </article>
              ))}
              {myTasksFiltered.length === 0 && <p>Sem tarefas neste filtro.</p>}
            </div>
          </section>
        )}

        {active === 'materiais' && (
          <section className="panel">
            <div className="form-grid" style={{ marginBottom: 12 }}>
              <label>Obra</label>
              <select value={selectedObraMaterials} onChange={(e) => { setSelectedObraMaterials(e.target.value); setSelectedTaskMaterials(''); }}>
                <option value="">Selecionar...</option>
                {myWorkIds.map((id) => {
                  const desc = tasks.find((t) => t.idObra?.id === id)?.idObra?.descricao;
                  return <option key={id} value={id}>#{id} - {desc || 'Sem descrição'}</option>;
                })}
              </select>
              <label>Tarefa (filtro)</label>
              <select value={selectedTaskMaterials} onChange={(e) => setSelectedTaskMaterials(e.target.value)} disabled={!selectedObraMaterials}>
                <option value="">Todas as tarefas da obra</option>
                {tasksBySelectedObra.map((t) => (
                  <option key={t.id} value={t.id}>#{t.id} - {t.descricao || 'Sem descrição'}</option>
                ))}
              </select>
            </div>
            <div className="list">
              {materialsByObraLines.map((line) => (
                <article className="list-item" key={line.id}>
                  <h4>{line.idMaterial.nome}</h4>
                  <p>Quantidade: {line.quantidade ?? '-'}</p>
                </article>
              ))}
              {materialsByObraLines.length === 0 && <p>Sem materiais de orçamento para esta obra.</p>}
            </div>
          </section>
        )}

        {active === 'problemas' && (
          <section className="panel">
            <form className="form-grid" onSubmit={submitProblem}>
              <label>Tarefa</label>
              <select value={problemForm.idTarefa} onChange={(e) => setProblemForm((s) => ({ ...s, idTarefa: e.target.value }))}>
                <option value="">Selecionar...</option>
                {tasks.map((t) => (
                  <option key={t.id} value={t.id}>
                    #{t.id} - {t.descricao || 'Sem descrição'}
                  </option>
                ))}
              </select>
              <label>Gravidade</label>
              <select value={problemForm.idGravidade} onChange={(e) => setProblemForm((s) => ({ ...s, idGravidade: e.target.value }))}>
                <option value="">Selecionar...</option>
                {gravidades.map((g) => (
                  <option key={g.id} value={g.id}>{g.nomeGravidade}</option>
                ))}
              </select>
              <label>Descrição</label>
              <textarea rows="4" value={problemForm.descricao} onChange={(e) => setProblemForm((s) => ({ ...s, descricao: e.target.value }))} />
              <button type="submit">Enviar Problema</button>
            </form>
          </section>
        )}

        {active === 'progresso' && (
          <section className="panel">
            <form className="form-grid" onSubmit={submitProgress}>
              <label>Obra</label>
              <select value={progressForm.idObra} onChange={(e) => setProgressForm((s) => ({ ...s, idObra: e.target.value }))}>
                <option value="">Selecionar...</option>
                {myWorkIds.map((id) => {
                  const desc = tasks.find((t) => t.idObra?.id === id)?.idObra?.descricao;
                  return <option key={id} value={id}>#{id} - {desc || 'Sem descrição'}</option>;
                })}
              </select>
              <label>Notas</label>
              <textarea rows="6" value={progressForm.notas} onChange={(e) => setProgressForm((s) => ({ ...s, notas: e.target.value }))} />
              <button type="submit">Registar</button>
            </form>
          </section>
        )}

        {active === 'equipaInfo' && (
          <section className="panel">
            {myTeam ? (
              <>
                <p><strong>{myTeam.nomeEquipa}</strong> - {myTeam.idObra?.descricao || 'Sem obra'}</p>
                <p>Local da obra: {myTeam.idObra ? `${myTeam.idObra?.rua || ''}, ${myTeam.idObra?.nporta || ''} - ${myTeam.idObra?.localidade || ''}` : '-'}</p>
                <h4>Membros</h4>
                <ul>
                  {teamMembers.map((m) => (
                    <li key={m.idFuncionario?.id}>{m.idFuncionario?.nome} ({m.idFuncionario?.email})</li>
                  ))}
                </ul>
                <h4>Tarefas da Equipa</h4>
                <ul>
                  {tasks.filter((t) => t.idEquipa?.id === myTeam.id).map((t) => (
                    <li key={t.id}>{t.descricao || `Tarefa #${t.id}`} - {t.idEstadoTarefa?.nomeEstado || '-'}</li>
                  ))}
                </ul>
              </>
            ) : (
              <p>Sem equipa associada.</p>
            )}
          </section>
        )}
      </main>
    </div>
  );
}
