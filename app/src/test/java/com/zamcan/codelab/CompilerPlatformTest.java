package com.zamcan.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public class CompilerPlatformTest {
    @Test public void arm32AndroidEightSupported() {
        CompilerPlatform.Profile p=CompilerPlatform.assess(26,new String[]{"armeabi-v7a"});
        assertTrue(p.supported);
        assertEquals("armeabi-v7a",p.abi);
    }
    @Test public void modernArm64Supported() {
        assertTrue(CompilerPlatform.assess(35,new String[]{"arm64-v8a"}).supported);
    }
    @Test public void emulatorAbisSupported() {
        assertTrue(CompilerPlatform.assess(29,new String[]{"x86_64"}).supported);
        assertTrue(CompilerPlatform.assess(26,new String[]{"x86"}).supported);
    }
    @Test public void oldAndroidRejected() {
        assertFalse(CompilerPlatform.assess(25,new String[]{"arm64-v8a"}).supported);
    }
    @Test public void unsupportedAbiRejected() {
        assertFalse(CompilerPlatform.assess(30,new String[]{"mips"}).supported);
    }
    @Test public void cannotClaimToolchainWithoutPackagedBinary() {
        CompilerPlatform.Profile p=CompilerPlatform.assess(35,new String[]{"arm64-v8a"});
        assertFalse(CompilerPlatform.hasPackagedToolchain(p,new String[]{}));
        assertFalse(CompilerPlatform.hasPackagedToolchain(p,new String[]{"armeabi-v7a"}));
        assertTrue(CompilerPlatform.hasPackagedToolchain(p,new String[]{"arm64-v8a"}));
    }
}
