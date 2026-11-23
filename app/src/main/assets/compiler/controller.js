'use strict';
let port, worker, timer;
function stop() { clearTimeout(timer); if (worker) worker.terminate(); worker = null; }
function send(value) { if (port) port.postMessage(JSON.stringify(value)); }
function deadline(ms) { clearTimeout(timer); timer = setTimeout(() => { stop(); send({type:'failure', message:'Time limit exceeded'}); }, ms); }
function startWorker(request) {
  worker = new Worker('worker.js');
  deadline(120000);
  worker.onmessage = event => {
    const value = event.data;
    if (value.type === 'phase' && value.phase === 'run') deadline(3000);
    send(value);
    if (value.type === 'ready' || value.type === 'result' || value.type === 'failure') stop();
  };
  worker.onerror = event => {stop(); send({type:'failure', message:event.message || 'Compiler worker failed'});};
  worker.postMessage(request);
}
window.addEventListener('message', event => {
  if (port || event.data !== 'connect' || !event.ports[0]) return;
  port = event.ports[0];
  port.onmessage = event => {
    stop();
    const request = JSON.parse(event.data);
    if (request.cancel) {send({type:'cancelled'}); return;}
    startWorker(request);
  };
  startWorker({checkStartup:true});
});
