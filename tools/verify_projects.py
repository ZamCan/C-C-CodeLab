from pathlib import Path
import json,subprocess,tempfile
root=Path(__file__).resolve().parents[1]/'app/src/main/assets/projects'
cases=json.loads((root/'tests.json').read_text())
with tempfile.TemporaryDirectory(prefix='zamcan-projects-') as tmp:
    for case in cases:
        for ext,compiler,standard in [('c','cc','c11'),('cpp','c++','c++17')]:
            work=Path(tmp)/(case['id']+'-'+ext);work.mkdir();exe=work/'program'
            subprocess.run([compiler,'-std='+standard,'-Wall','-Wextra','-Werror',str(root/(case['id']+'.'+ext)),'-o',str(exe)],check=True,timeout=45)
            result=subprocess.run([str(exe)],cwd=work,input=case['stdin'],text=True,capture_output=True,timeout=3)
            assert result.returncode==0,(case['id'],result.stderr)
            assert result.stdout==case['stdout'],(case['id'],result.stdout,case['stdout'])
            if case['id']=='calculator':
                for source,status in [('5 / 0\n',2),('invalid\n',1),('2 + 3 garbage\n',1)]:
                    failure=subprocess.run([str(exe)],input=source,text=True,capture_output=True,timeout=3)
                    assert failure.returncode==status and failure.stderr and not failure.stdout
            if case['id'] in ('library','grades'):
                filename='library.txt' if case['id']=='library' else 'grades.txt'
                (work/filename).write_text('corrupt data\n')
                failure=subprocess.run([str(exe)],cwd=work,input='load\nquit\n',text=True,capture_output=True,timeout=3)
                assert 'Load failed' in failure.stdout
    print('PASS: ten C/C++ reference builds and exact transcripts; calculator error statuses and corrupt record rejection verified on host.')
