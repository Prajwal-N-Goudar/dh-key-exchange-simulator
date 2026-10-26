import { useEffect, useState } from 'react';
import { ArrowLeftRight, CheckCircle2, Clock3, KeyRound, Network, RefreshCw, ShieldAlert, ShieldCheck } from 'lucide-react';

const API = 'http://localhost:8080/api';
const defaults = { p: '23', g: '5', alicePrivate: '6', bobPrivate: '15' };
const mitmDefaults = { ...defaults, attackerPrivateForAlice: '7', attackerPrivateForBob: '11' };

async function post(path, payload) {
  const response = await fetch(`${API}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(Object.fromEntries(Object.entries(payload).map(([k, v]) => [k, v === '' ? null : Number(v)])))
  });
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.message || body.detail || message;
    } catch {}
    throw new Error(message);
  }
  return response.json();
}

function Field({ label, value, onChange, hint }) {
  return <label className="field"><span>{label}</span><input inputMode="numeric" type="number" value={value} onChange={e => onChange(e.target.value)} /><small>{hint}</small></label>;
}

function Metric({ label, value, accent = '' }) {
  return <div className={`metric ${accent}`}><span>{label}</span><strong>{String(value ?? '—')}</strong></div>;
}

export default function App() {
  const [mode, setMode] = useState('normal');
  const [normal, setNormal] = useState(defaults);
  const [mitm, setMitm] = useState(mitmDefaults);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [history, setHistory] = useState([]);
  const [historyError, setHistoryError] = useState('');

  const current = mode === 'normal' ? normal : mitm;
  const setter = mode === 'normal' ? setNormal : setMitm;
  const update = (key, value) => setter(old => ({ ...old, [key]: value }));

  async function loadHistory() {
    try {
      const res = await fetch(`${API}/simulations/history`);
      if (!res.ok) throw new Error('Could not load history');
      setHistory(await res.json());
      setHistoryError('');
    } catch (e) {
      setHistoryError('History unavailable. Start the Spring Boot API and connect MySQL.');
    }
  }

  useEffect(() => { loadHistory(); }, []);

  async function runSimulation(e) {
    e.preventDefault();
    setBusy(true); setError(''); setResult(null);
    try {
      const path = mode === 'normal' ? '/simulations/diffie-hellman' : '/simulations/mitm';
      const data = await post(path, current);
      setResult(data);
      await loadHistory();
    } catch (e) {
      setError(e.message || 'Unable to reach the API. Check that Spring Boot is running.');
    } finally {
      setBusy(false);
    }
  }

  function reset() {
    if (mode === 'normal') setNormal(defaults);
    else setMitm(mitmDefaults);
    setResult(null); setError('');
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#"><span className="brand-icon"><KeyRound size={19}/></span><span>CRYPTOLAB <small>CNS SIMULATOR</small></span></a>
        <div className="status"><span className="status-dot"/> EDUCATIONAL ENVIRONMENT</div>
      </header>

      <main className="container">
        <section className="hero">
          <div className="eyebrow"><Network size={15}/> CRYPTOGRAPHY & NETWORK SECURITY</div>
          <h1>Diffie–Hellman <span>Key Exchange</span></h1>
          <p>Explore how two participants establish a shared secret over a public channel — and why authentication matters.</p>
          <div className="hero-tags"><span><ShieldCheck size={14}/> Key agreement</span><span><ArrowLeftRight size={14}/> Public channel</span><span><ShieldAlert size={14}/> MITM demo</span></div>
        </section>

        <div className="mode-tabs">
          <button className={mode === 'normal' ? 'tab active' : 'tab'} onClick={() => { setMode('normal'); setResult(null); setError(''); }}> <KeyRound size={16}/> Key exchange</button>
          <button className={mode === 'mitm' ? 'tab active danger-tab' : 'tab'} onClick={() => { setMode('mitm'); setResult(null); setError(''); }}><ShieldAlert size={16}/> MITM demonstration</button>
        </div>

        <div className="main-grid">
          <section className="panel config-panel">
            <div className="panel-heading"><div><span className="step-label">01 / CONFIGURATION</span><h2>{mode === 'normal' ? 'Exchange parameters' : 'Attack scenario'}</h2></div><button className="icon-button" onClick={reset} title="Reset values"><RefreshCw size={16}/></button></div>
            <form onSubmit={runSimulation}>
              <div className="field-grid">
                <Field label="Prime number (p)" value={current.p} onChange={v => update('p', v)} hint="Public parameter · e.g. 23"/>
                <Field label="Generator (g)" value={current.g} onChange={v => update('g', v)} hint="Public parameter · e.g. 5"/>
              </div>
              <div className="section-caption">PARTICIPANT PRIVATE EXPONENTS</div>
              <div className="field-grid">
                <Field label="Alice private (a)" value={current.alicePrivate} onChange={v => update('alicePrivate', v)} hint="Leave blank to randomize"/>
                <Field label="Bob private (b)" value={current.bobPrivate} onChange={v => update('bobPrivate', v)} hint="Leave blank to randomize"/>
              </div>
              {mode === 'mitm' && <>
                <div className="section-caption attack-caption">ATTACKER PRIVATE EXPONENTS</div>
                <div className="field-grid">
                  <Field label="Attacker for Alice (x)" value={current.attackerPrivateForAlice} onChange={v => update('attackerPrivateForAlice', v)} hint="Attacker's first exchange"/>
                  <Field label="Attacker for Bob (y)" value={current.attackerPrivateForBob} onChange={v => update('attackerPrivateForBob', v)} hint="Attacker's second exchange"/>
                </div>
              </>}
              <div className="hint-box"><span className="hint-icon">i</span><p>Use small values for classroom demonstrations only. This simulator limits p to a 31-bit prime and is <b>not suitable for real secrets</b>.</p></div>
              {error && <div className="error-box" role="alert">{error}</div>}
              <button className={`primary-button ${mode === 'mitm' ? 'attack-button' : ''}`} type="submit" disabled={busy}>{busy ? 'Calculating…' : mode === 'normal' ? 'Run key exchange' : 'Run MITM simulation'} <ArrowLeftRight size={17}/></button>
            </form>
          </section>

          <section className="panel result-panel">
            <div className="panel-heading"><div><span className="step-label">02 / SIMULATION OUTPUT</span><h2>Exchange visualization</h2></div><span className="live-label"><span/> LIVE RESULT</span></div>
            {!result && <div className="empty-state"><div className="empty-icon"><Network size={30}/></div><h3>Ready to simulate</h3><p>Choose your parameters, then run the simulation to see public values, derived secrets, and the algorithm's result.</p><div className="empty-steps"><span>1 <small>Configure</small></span><i/><span>2 <small>Calculate</small></span><i/><span>3 <small>Inspect</small></span></div></div>}
            {result && mode === 'normal' && <div className="results">
              <div className="success-banner"><CheckCircle2 size={19}/><div><b>{result.secretsMatch ? 'Shared secrets match' : 'Shared secrets do not match'}</b><small>{result.secretsMatch ? 'Alice and Bob derived the same value.' : 'Check the parameters and calculations.'}</small></div></div>
              <div className="participant-grid">
                <div className="participant-card alice"><div className="person-head"><span className="avatar">A</span><b>Alice</b><span className="tiny-pill">PARTICIPANT 01</span></div><Metric label="Private exponent (a)" value={result.alicePrivate}/><Metric label="Public value A = gᵃ mod p" value={result.alicePublic}/><Metric label="Derived shared secret" value={result.aliceSharedSecret} accent="secret-metric"/></div>
                <div className="participant-card bob"><div className="person-head"><span className="avatar bob-avatar">B</span><b>Bob</b><span className="tiny-pill">PARTICIPANT 02</span></div><Metric label="Private exponent (b)" value={result.bobPrivate}/><Metric label="Public value B = gᵇ mod p" value={result.bobPublic}/><Metric label="Derived shared secret" value={result.bobSharedSecret} accent="secret-metric"/></div>
              </div>
              <div className="public-channel"><ArrowLeftRight size={18}/><div><b>Public communication channel</b><small>Only public values A and B are exchanged.</small></div><span className="channel-badge">PUBLIC</span></div>
              <div className="math-box"><b>Mathematical verification</b><code>Bᵃ mod p = {result.bobPublic}^{result.alicePrivate} mod {result.p} = {result.aliceSharedSecret}</code><code>Aᵇ mod p = {result.alicePublic}^{result.bobPrivate} mod {result.p} = {result.bobSharedSecret}</code></div>
              <p className="warning-note">{result.warning}</p>
            </div>}
            {result && mode === 'mitm' && <div className="results">
              <div className="danger-banner"><ShieldAlert size={19}/><div><b>Unauthenticated exchange intercepted in the model</b><small>The attacker establishes two separate shared secrets.</small></div></div>
              <div className="mitm-flow">
                <div className="flow-person"><span className="avatar">A</span><b>Alice</b><small>Public A: {result.alicePublic}</small><strong>Secret: {result.aliceSecretWithAttacker}</strong></div>
                <div className="flow-mid"><span className="flow-line"/><span className="attacker-chip"><ShieldAlert size={16}/> ATTACKER</span><span className="flow-line"/><small>Two independent exchanges</small></div>
                <div className="flow-person"><span className="avatar bob-avatar">B</span><b>Bob</b><small>Public B: {result.bobPublic}</small><strong>Secret: {result.bobSecretWithAttacker}</strong></div>
              </div>
              <div className="attacker-results">
                <Metric label="Attacker's secret with Alice" value={result.attackerSecretWithAlice}/>
                <Metric label="Attacker's secret with Bob" value={result.attackerSecretWithBob}/>
              </div>
              <div className="danger-explanation"><b>{result.attackerDerivesBothLegSecrets ? 'The attacker can derive both leg secrets in this simulation.' : 'The simulation did not produce the expected attacker result.'}</b><p>{result.explanation}</p><small>Alice's and Bob's secrets matching is not required for this attack; each party believes it is exchanging with the other.</small></div>
              <p className="warning-note">{result.warning}</p>
            </div>}
          </section>
        </div>

        <section className="concept-grid">
          <article className="concept-card"><span className="concept-number">01</span><KeyRound size={20}/><h3>Key agreement</h3><p>Both parties derive the same secret from their private exponent and the other party's public value.</p></article>
          <article className="concept-card"><span className="concept-number">02</span><Network size={20}/><h3>Public parameters</h3><p>The prime p, generator g, and public values can travel openly; private exponents should remain private.</p></article>
          <article className="concept-card"><span className="concept-number">03</span><ShieldAlert size={20}/><h3>Authentication matters</h3><p>Basic Diffie–Hellman does not prove who is on the other end. Real systems authenticate the exchange.</p></article>
        </section>

        <section className="panel history-panel">
          <div className="panel-heading"><div><span className="step-label">03 / PERSISTED RECORDS</span><h2>Simulation history</h2></div><button className="secondary-button" onClick={loadHistory}><RefreshCw size={14}/> Refresh</button></div>
          {historyError && <p className="muted">{historyError}</p>}
          {!historyError && history.length === 0 && <p className="muted">No saved simulations yet. Run a simulation to add a record.</p>}
          {history.length > 0 && <div className="table-wrap"><table><thead><tr><th>Mode</th><th>p</th><th>g</th><th>Alice public</th><th>Bob public</th><th>Secrets match</th><th>Time</th></tr></thead><tbody>{history.map(item => <tr key={item.id}><td><span className={item.mode === 'MITM' ? 'mode-pill mode-mitm' : 'mode-pill'}>{item.mode}</span></td><td>{item.p}</td><td>{item.g}</td><td>{item.alicePublic}</td><td>{item.bobPublic}</td><td>{item.secretsMatch ? 'Yes' : 'No'}</td><td><Clock3 size={13}/> {new Date(item.createdAt).toLocaleString()}</td></tr>)}</tbody></table></div>}
          <p className="privacy-note">History stores public parameters and public values only. Private exponents and shared secrets are not persisted.</p>
        </section>

        <footer><span>CRYPTOLAB · CNS MINI PROJECT</span><span>Educational simulation — not for production cryptography</span></footer>
      </main>
    </div>
  );
}
