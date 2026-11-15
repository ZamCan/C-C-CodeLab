/* CodeLab offline Clang pipeline. Also exercised by the Node integration harness. */
async function compileProject(request, readBuffer, emit) {
  const files = request.files;
  if (!files || Object.keys(files).length > 32) throw Error('Maximum 32 project files');
  let total = 0;
  for (const [name, text] of Object.entries(files)) {
    if (!/^[A-Za-z0-9_-][A-Za-z0-9_.-]*\.(c|cpp|h|hpp)$/.test(name) || name.includes('..')) throw Error('Invalid source filename');
    if (typeof text !== 'string') throw Error('Invalid source');
    total += new TextEncoder().encode(text).length;
  }
  if (total > 1048576 || new TextEncoder().encode(request.stdin || '').length > 65536) throw Error('Project or input exceeds limit');
  const units = Object.keys(files).filter(n => /\.(c|cpp)$/.test(n));
  if (!units.length) throw Error('Select a C or C++ source file');
  let phase = 'compile', count = 0;
  const api = new API({
    readBuffer,
    compileStreaming: async name => WebAssembly.compile(await readBuffer(name)),
    clang: 'clang22-noeh', lld: 'lld22-noeh', sysroot: 'sysroot22.tar',
    clangResourceInclude: '/lib/clang/22/include',
    clangExtraArgs: ['-internal-isystem', '/include/wasm32-wasip1/noeh/c++/v1', '-internal-isystem', '/include/wasm32-wasip1'],
    hostWrite: (text, fd) => {
      if (fd === undefined) return; // runtime bookkeeping is not student output
      count += new TextEncoder().encode(text).length;
      if (count > 65536) throw Error('Output limit exceeded (64 KiB)');
      emit({type:'output', stream:phase === 'run' ? (fd === 2 ? 'stderr' : 'stdout') : 'diagnostics', text});
    }
  });
  api.hostLogAsync = async (_label, promise) => promise;
  api.hostLog = () => {};
  emit({type:'phase', phase:'compile'});
  await api.ready;
  for (const [name, text] of Object.entries(files)) api.memfs.addFile(name, text);
  const clang = await api.getModule(api.clangFilename);
  const objects = [];
  for (const name of units) {
    const cpp = name.endsWith('.cpp');
    const object = name + '.o'; objects.push(object);
    await api.run(clang, 'clang', '-cc1', '-triple', 'wasm32-unknown-wasi', '-emit-obj',
      ...api.clangCommonArgs.filter(a => a !== '-fcolor-diagnostics'),
      '-O0', '-std=' + (cpp ? 'c++17' : 'c17'), '-I.', '-o', object, '-x', cpp ? 'c++' : 'c', name);
  }
  emit({type:'phase', phase:'link'});
  const lld = await api.getModule(api.lldFilename);
  await api.run(lld, 'wasm-ld', '-z', 'stack-size=1048576', '--max-memory=67108864',
    '-Llib/wasm32-wasip1', 'lib/wasm32-wasip1/crt1.o', ...objects,
    '-lc++', '-lc++abi', '-lc', '-lclang_rt.builtins', '-o', 'program.wasm');
  const module = await WebAssembly.compile(api.memfs.getFileContents('program.wasm'));
  for (const entry of WebAssembly.Module.imports(module)) {
    if (entry.module !== 'wasi_snapshot_preview1' || entry.kind !== 'function') throw Error('Unsupported program import: ' + entry.module + '.' + entry.name);
  }
  // Fresh virtual filesystem: the program cannot inspect compiler headers or project sources.
  const runtime = new API({readBuffer, compileStreaming:async name => WebAssembly.compile(await readBuffer(name)),
    sysroot:'empty.tar', hostWrite:api.hostWrite});
  runtime.hostLogAsync = async (_label, promise) => promise;
  runtime.hostLog = () => {};
  await runtime.ready;
  runtime.memfs.setStdinStr(request.stdin || '');
  phase = 'run'; count = 0;
  emit({type:'phase', phase:'run'});
  let exitCode = 0, trap = null;
  try { await runtime.run(module, 'program.wasm'); }
  catch (error) { if (typeof error.code === 'number') exitCode = error.code; else {exitCode = null; trap = error.message;} }
  emit({type:'result', exitCode, trap});
}
