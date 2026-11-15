"""Read-only framework parsing probe for the current Termux Android host."""
import os, pathlib, shutil, subprocess, tempfile, zipfile, sys
root=pathlib.Path(__file__).resolve().parents[1]
sdk=pathlib.Path('/data/data/com.termux/files/home/android-sdk')
archive=pathlib.Path('/data/data/com.termux/files/home/.cache/zamcan-package-probe.jar')
apk=sys.argv[1] if len(sys.argv)>1 else '/sdcard/Download/ZamCan-Academy-0.1.0-preview.apk'
def run(args,**kwargs):return subprocess.run([str(x) for x in args],check=True,timeout=90,**kwargs)
with tempfile.TemporaryDirectory(prefix='zamcan-probe-') as directory:
    work=pathlib.Path(directory);(work/'dex').mkdir()
    run(['javac','-source','8','-target','8','-cp',sdk/'platforms/android-35/android.jar','-d',work,root/'tools/PackageProbe.java'])
    run(['java','-cp',sdk/'build-tools/34.0.0/lib/d8.jar','com.android.tools.r8.D8','--lib',sdk/'platforms/android-35/android.jar','--min-api','26','--output',work/'dex',work/'PackageProbe.class'])
    with zipfile.ZipFile(work/'probe.jar','w') as output:output.write(work/'dex/classes.dex','classes.dex')
    archive.parent.mkdir(parents=True,exist_ok=True)
    if archive.exists():archive.chmod(0o600)
    shutil.copyfile(work/'probe.jar',archive);archive.chmod(0o444)
    env=dict(os.environ);env.pop('LD_LIBRARY_PATH',None);env.pop('LD_PRELOAD',None);env['CLASSPATH']=str(archive)
    run(['/system/bin/app_process','-Xnoimage-dex2oat','/','PackageProbe',apk],env=env)
