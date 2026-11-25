// Live Call Translator – relay server
// Phones join a room with a code; recognized speech text is forwarded to the other phones.
const http = require('http');
const { WebSocketServer } = require('ws');

const PORT = process.env.PORT || 8080;
const MAX_PER_ROOM = 8;
const rooms = new Map(); // room -> Set<ws>

const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'text/plain' });
  res.end('Live Call Translator relay is running');
});
const wss = new WebSocketServer({ server });

const send = (ws, obj) => { if (ws.readyState === 1) ws.send(JSON.stringify(obj)); };
const others = (ws) => [...(rooms.get(ws.room) || [])].filter((p) => p !== ws);

function leave(ws) {
  const set = rooms.get(ws.room);
  if (!set) return;
  set.delete(ws);
  others(ws).forEach((p) => send(p, { type: 'peer_left', name: ws.name }));
  if (set.size === 0) rooms.delete(ws.room);
  ws.room = null;
}

wss.on('connection', (ws) => {
  ws.isAlive = true;
  ws.on('pong', () => (ws.isAlive = true));

  ws.on('message', (raw) => {
    let m;
    try { m = JSON.parse(raw.toString()); } catch { return; }

    if (m.type === 'join') {
      leave(ws);
      const room = String(m.room || '').slice(0, 32);
      if (!room) return;
      const set = rooms.get(room) || new Set();
      if (set.size >= MAX_PER_ROOM) return send(ws, { type: 'error', message: 'Room full' });
      ws.room = room;
      ws.name = String(m.name || 'Dost').slice(0, 40);
      ws.lang = String(m.lang || 'hi-IN').slice(0, 10);
      set.add(ws);
      rooms.set(room, set);
      send(ws, { type: 'joined', peers: others(ws).map((p) => ({ name: p.name, lang: p.lang })) });
      others(ws).forEach((p) => send(p, { type: 'peer_joined', name: ws.name, lang: ws.lang }));
    } else if (m.type === 'msg' && ws.room) {
      const text = String(m.text || '').slice(0, 2000);
      if (!text) return;
      others(ws).forEach((p) => send(p, { type: 'msg', name: ws.name, lang: ws.lang, text }));
    }
  });

  ws.on('close', () => leave(ws));
});

setInterval(() => {
  wss.clients.forEach((ws) => {
    if (!ws.isAlive) return ws.terminate();
    ws.isAlive = false;
    ws.ping();
  });
}, 30000);

server.listen(PORT, () => console.log(`Relay listening on :${PORT}`));
