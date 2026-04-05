<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
<title>Dungeon Realm</title>
<link href="https://fonts.googleapis.com/css2?family=Cinzel+Decorative:wght@700&family=Cinzel:wght@400;600&family=Crimson+Text:ital,wght@0,400;1,400&display=swap" rel="stylesheet">
<style>
*,*::before,*::after{box-sizing:border-box;margin:0;padding:0}

:root{
  --gold:#c9a84c;
  --danger:#ff3030;
  --success:#00e070;
  --bg:#060810;
  --panel:#0d1117;
  --border:#1e2a3a;
  --border-gold:rgba(201,168,76,.35);
  --text:#c0ccdc;
  --muted:#4a5a6a;
}

html,body{
  height:100%;
  overflow:hidden;
  background:var(--bg);
  color:var(--text);
  font-family:'Crimson Text',serif;
}

/* full-screen centered flex */
body{
  display:flex;
  align-items:center;
  justify-content:center;
}

/* subtle radial glow */
body::before{
  content:'';
  position:fixed;inset:0;
  background:radial-gradient(ellipse 70% 60% at 50% 100%,rgba(0,70,110,.2) 0%,transparent 70%);
  pointer-events:none;
}

/* ── wrapper: caps height so nothing overflows ── */
.wrap{
  position:relative;
  width:100%;
  max-width:420px;
  max-height:100vh;
  overflow-y:auto;
  padding:1.2rem 1rem;
  display:flex;
  flex-direction:column;
  gap:.9rem;
}

/* scrollbar hidden */
.wrap::-webkit-scrollbar{width:0}

/* ── logo ── */
.logo{text-align:center}
.skull{
  font-size:2.8rem;
  display:block;
  filter:drop-shadow(0 0 16px rgba(201,168,76,.6));
  animation:skullPulse 3s ease-in-out infinite;
  line-height:1.1;
}
@keyframes skullPulse{
  0%,100%{filter:drop-shadow(0 0 16px rgba(201,168,76,.5));transform:scale(1)}
  50%{filter:drop-shadow(0 0 28px rgba(240,192,64,.9));transform:scale(1.04)}
}
.logo-title{
  font-family:'Cinzel Decorative',cursive;
  font-size:1.7rem;
  color:var(--gold);
  text-shadow:0 0 20px rgba(201,168,76,.4);
  letter-spacing:.04em;
  margin-top:.2rem;
}
.logo-sub{
  font-family:'Cinzel',serif;
  font-size:.65rem;
  letter-spacing:.3em;
  text-transform:uppercase;
  color:var(--muted);
  margin-top:.25rem;
}

/* ── panel ── */
.panel{
  background:var(--panel);
  border:1px solid var(--border);
  border-top:2px solid var(--gold);
  border-radius:4px;
  padding:1.4rem 1.4rem 1.2rem;
  box-shadow:0 0 40px rgba(0,0,0,.7);
}

/* ── tabs ── */
.tabs{
  display:flex;
  border-bottom:1px solid var(--border);
  margin-bottom:1.2rem;
}
.tab{
  flex:1;
  background:none;
  border:none;
  border-bottom:2px solid transparent;
  padding:.55rem .5rem;
  font-family:'Cinzel',serif;
  font-size:.72rem;
  letter-spacing:.15em;
  text-transform:uppercase;
  color:var(--muted);
  cursor:pointer;
  transition:all .25s;
}
.tab.active{color:var(--gold);border-bottom-color:var(--gold)}
.tab:hover:not(.active){color:var(--text)}

/* ── forms ── */
.form{display:none}
.form.active{display:block}

.field{margin-bottom:.85rem}
label{
  display:block;
  font-family:'Cinzel',serif;
  font-size:.6rem;
  letter-spacing:.2em;
  text-transform:uppercase;
  color:var(--muted);
  margin-bottom:.3rem;
}
input[type=text],input[type=password],input[type=email]{
  width:100%;
  background:rgba(0,0,0,.5);
  border:1px solid var(--border);
  border-radius:3px;
  padding:.6rem .85rem;
  color:var(--text);
  font-family:'Crimson Text',serif;
  font-size:.95rem;
  outline:none;
  transition:border-color .25s,box-shadow .25s;
}
input:focus{border-color:var(--gold);box-shadow:0 0 0 2px rgba(201,168,76,.15)}

.btn{
  width:100%;
  margin-top:.2rem;
  background:linear-gradient(135deg,#7a5c10,#c9a84c,#7a5c10);
  background-size:200% auto;
  border:1px solid var(--gold);
  border-radius:3px;
  color:#0a0800;
  font-family:'Cinzel',serif;
  font-size:.8rem;
  font-weight:700;
  letter-spacing:.18em;
  text-transform:uppercase;
  padding:.7rem;
  cursor:pointer;
  transition:background-position .4s,box-shadow .25s,transform .15s;
}
.btn:hover{background-position:right center;box-shadow:0 0 16px rgba(201,168,76,.35);transform:translateY(-1px)}
.btn:active{transform:translateY(0)}
.btn:disabled{opacity:.6;cursor:not-allowed;transform:none}

/* ── message ── */
.msg{
  margin-top:.8rem;
  padding:.55rem .85rem;
  border-radius:3px;
  font-size:.88rem;
  border-left:3px solid;
  display:none;
  line-height:1.4;
}
.msg.ok {background:rgba(0,180,80,.08);border-color:var(--success);color:#00d460}
.msg.err{background:rgba(255,48,48,.08);border-color:var(--danger);color:#ff7070}

/* ── tagline ── */
.tagline{
  text-align:center;
  font-size:.8rem;
  color:var(--muted);
  font-style:italic;
}

/* ── spinner ── */
.spin{
  display:inline-block;width:11px;height:11px;
  border:2px solid rgba(0,0,0,.25);
  border-top-color:#0a0800;
  border-radius:50%;
  animation:rot .65s linear infinite;
  vertical-align:middle;
  margin-right:.35rem;
}
@keyframes rot{to{transform:rotate(360deg)}}

/* ── mobile safety ── */
@media(max-height:640px){
  .skull{font-size:2rem}
  .logo-title{font-size:1.3rem}
  .panel{padding:1rem 1.2rem .9rem}
  .field{margin-bottom:.6rem}
}
</style>
</head>
<body>

<div class="wrap">

  <div class="logo">
    <span class="skull">💀</span>
    <div class="logo-title">Dungeon Realm</div>
    <div class="logo-sub">Multiplayer Dark Fantasy RPG</div>
  </div>

  <div class="panel">
    <div class="tabs">
      <button class="tab active" onclick="tab('login')">⚔ Enter</button>
      <button class="tab"        onclick="tab('reg')">📜 New Hero</button>
    </div>

    <!-- LOGIN -->
    <div class="form active" id="fLogin">
      <div class="field">
        <label>Username</label>
        <input type="text" id="l_u" placeholder="Your hero name" autocomplete="username">
      </div>
      <div class="field">
        <label>Password</label>
        <input type="password" id="l_p" placeholder="••••••••" autocomplete="current-password">
      </div>
      <button class="btn" id="btnLogin" onclick="doLogin()">Enter the Dungeon</button>
    </div>

    <!-- REGISTER -->
    <div class="form" id="fReg">
      <div class="field">
        <label>Hero Name</label>
        <input type="text" id="r_u" placeholder="3–20 characters" autocomplete="username">
      </div>
      <div class="field">
        <label>Email</label>
        <input type="email" id="r_e" placeholder="you@email.com" autocomplete="email">
      </div>
      <div class="field">
        <label>Password</label>
        <input type="password" id="r_p" placeholder="Min 6 characters" autocomplete="new-password">
      </div>
      <button class="btn" id="btnReg" onclick="doReg()">Forge Your Legend</button>
    </div>

    <div class="msg" id="msg"></div>
  </div>

  <div class="tagline">"Not all who venture in emerge..."</div>

</div>

<script>
function tab(t){
  document.querySelectorAll('.tab').forEach((b,i)=>b.classList.toggle('active',t==='login'?i===0:i===1));
  document.getElementById('fLogin').classList.toggle('active',t==='login');
  document.getElementById('fReg'  ).classList.toggle('active',t==='reg');
  hide();
}

function show(text,type){
  const el=document.getElementById('msg');
  el.textContent=text;
  el.className='msg '+(type==='ok'?'ok':'err');
  el.style.display='block';
}
function hide(){document.getElementById('msg').style.display='none'}

async function post(params){
  const r=await fetch('auth',{
    method:'POST',
    headers:{'Content-Type':'application/x-www-form-urlencoded'},
    body:new URLSearchParams(params).toString()
  });
  return r.json();
}

async function doLogin(){
  const u=document.getElementById('l_u').value.trim();
  const p=document.getElementById('l_p').value;
  if(!u||!p){show('Fill in all fields.','err');return}
  const btn=document.getElementById('btnLogin');
  btn.innerHTML='<span class="spin"></span>Entering...';
  btn.disabled=true;
  try{
    const d=await post({action:'login',username:u,password:p});
    if(d.success){show('✓ '+d.message,'ok');setTimeout(()=>location.href='game',700)}
    else{show(d.message,'err');btn.textContent='Enter the Dungeon';btn.disabled=false}
  }catch(e){
    show('Connection error — check Railway MySQL is linked.','err');
    btn.textContent='Enter the Dungeon';btn.disabled=false;
  }
}

async function doReg(){
  const u=document.getElementById('r_u').value.trim();
  const e=document.getElementById('r_e').value.trim();
  const p=document.getElementById('r_p').value;
  if(!u||!e||!p){show('Fill in all fields.','err');return}
  const btn=document.getElementById('btnReg');
  btn.innerHTML='<span class="spin"></span>Creating...';
  btn.disabled=true;
  try{
    const d=await post({action:'register',username:u,email:e,password:p});
    if(d.success){show('✓ '+d.message,'ok');setTimeout(()=>location.href='game',700)}
    else{show(d.message,'err');btn.textContent='Forge Your Legend';btn.disabled=false}
  }catch(e){
    show('Connection error — check Railway MySQL is linked.','err');
    btn.textContent='Forge Your Legend';btn.disabled=false;
  }
}

document.addEventListener('keydown',e=>{
  if(e.key!=='Enter')return;
  const active=document.querySelector('.form.active');
  if(active.id==='fLogin')doLogin();
  else doReg();
});
</script>
</body>
</html>
