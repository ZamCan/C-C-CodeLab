importScripts('shared.js', 'engine.js');
const hashes = {"clang22-noeh": "e01346e33b31a3b3359a317450502bd4feb78cd6ca7fed08aca8601f9ff8834d", "lld22-noeh": "63c397c582e193aacd96fbdeb04b1bd32f47ae505f6dc4a569ed08a80ddfeff7", "sysroot22.tar": "d991c621cdf9b4640c2cd4aa21abb08491c2ae6ef9917cac5c6065553604bcec", "memfs": "fb77c156e0c6c2032f4a4edd0224420debdb5cd071cbfc3daa6881a41d432d93"};
self.onmessage = async event => {
  try {
    await compileProject(event.data, async name => {
      if (!['clang22-noeh','lld22-noeh','sysroot22.tar','memfs','empty.tar'].includes(name)) throw Error('Unknown asset');
      const compressed = ['clang22-noeh','lld22-noeh','sysroot22.tar','memfs'].includes(name);
      const assetPath = compressed ? name + '.gz' : name;
      const response = await fetch(assetPath, {credentials:'omit'});
      if (!response.ok) throw Error('Offline compiler asset unavailable: ' + name);
      let buffer;
      if (compressed) {
        if (typeof DecompressionStream === 'undefined') throw Error('Gzip decompression is unavailable on this WebView');
        buffer = await new Response(response.body.pipeThrough(new DecompressionStream('gzip'))).arrayBuffer();
      } else {
        buffer = await response.arrayBuffer();
      }
      if (hashes[name]) {
        const digest = Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',buffer)),b=>b.toString(16).padStart(2,'0')).join('');
        if(digest!==hashes[name])throw Error('Compiler integrity check failed: '+name);
      }
      return buffer;
    }, message => postMessage(message));
  } catch (error) {
    postMessage({type:'failure', message:error.message, exitCode:typeof error.code === 'number' ? error.code : null});
  }
};
