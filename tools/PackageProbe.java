import android.os.Build;
import java.io.File;
import java.lang.reflect.*;

/** Read-only Android framework diagnostic. No hidden-API exemptions or privileged operations. */
public final class PackageProbe {
    public static void main(String[] args) throws Exception {
        System.out.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
        System.out.println("Android: " + Build.VERSION.RELEASE + " API " + Build.VERSION.SDK_INT);
        System.out.println("ABI: " + String.join(",", Build.SUPPORTED_ABIS));
        File apk = new File(args[0]);
        System.out.println("Readable APK: " + apk.canRead() + ", bytes: " + apk.length());
        try {
            Class<?> parserClass = Class.forName("android.content.pm.PackageParser");
            Object parser = parserClass.getConstructor().newInstance();
            Object parsed = parserClass.getMethod("parsePackage", File.class, int.class).invoke(parser, apk, 0);
            System.out.println("Framework parsed package: " + parsed.getClass().getField("packageName").get(parsed));
            for (Method method : parserClass.getDeclaredMethods()) {
                if (method.getName().equals("collectCertificates") && method.getParameterCount() == 2 && method.getParameterTypes()[0].equals(parsed.getClass()) && method.getParameterTypes()[1].equals(boolean.class)) {
                    method.invoke(null, parsed, false);
                    System.out.println("Framework certificate verification: passed");
                    return;
                }
            }
            System.out.println("Certificate diagnostic unavailable on this platform; use apksigner verification.");
        } catch (InvocationTargetException failure) {
            failure.getCause().printStackTrace();
            System.exit(2);
        }
    }
}
