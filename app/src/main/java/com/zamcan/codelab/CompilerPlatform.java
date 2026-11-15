package com.zamcan.codelab;

import android.os.Build;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Device compatibility assessment. Does not imply that a compiler binary is bundled. */
public final class CompilerPlatform {
    private static final List<String> SUPPORTED = Collections.unmodifiableList(
        Arrays.asList("arm64-v8a", "armeabi-v7a", "x86_64", "x86"));
    private CompilerPlatform() {}

    public static final class Profile {
        public final int sdk;
        public final String abi;
        public final boolean supported;
        public final String explanation;
        Profile(int sdk,String abi,boolean supported,String explanation) {
            this.sdk=sdk;this.abi=abi;this.supported=supported;this.explanation=explanation;
        }
    }

    public static Profile detect() {
        return assess(Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS);
    }

    public static Profile assess(int sdk,String[] abis) {
        if (sdk < 26)
            return new Profile(sdk,"",false,"CodeLab requires Android 8.0 (API 26) or newer.");
        if (abis == null || abis.length == 0)
            return new Profile(sdk,"",false,"No CPU ABI was reported by Android.");
        for (String abi : abis) {
            if (SUPPORTED.contains(abi))
                return new Profile(sdk,abi,true,"Compatible Android CPU ABI detected. Compiler availability must be checked separately.");
        }
        return new Profile(sdk,abis[0],false,"No supported native toolchain ABI for this device.");
    }

    public static boolean hasPackagedToolchain(Profile profile, String[] packagedAbis) {
        if (!profile.supported || packagedAbis == null) return false;
        for (String abi : packagedAbis) if (profile.abi.equals(abi)) return true;
        return false;
    }
}
