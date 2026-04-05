<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Error — Dungeon Realm</title>
<link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@400;600&family=Crimson+Text&display=swap" rel="stylesheet">
<style>
  body{background:#05080f;color:#c0ccdc;font-family:'Crimson Text',serif;display:flex;
       align-items:center;justify-content:center;min-height:100vh;text-align:center}
  h1{font-family:'Cinzel',serif;color:#c9a84c;font-size:2rem;margin-bottom:1rem}
  p{color:#5a6a7a;margin-bottom:1.5rem}
  a{color:#00d4ff;text-decoration:none}a:hover{text-decoration:underline}
</style>
</head>
<body>
<div>
  <div style="font-size:4rem">💀</div>
  <h1>The Dungeon Claims Another</h1>
  <p>Something went wrong in the dark depths...</p>
  <p><small><%= exception != null ? exception.getMessage() : "Unknown error" %></small></p>
  <a href="/">← Return to Safety</a>
</div>
</body>
</html>
