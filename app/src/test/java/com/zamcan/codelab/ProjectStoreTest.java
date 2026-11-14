package com.zamcan.codelab;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.*;
import static org.junit.Assert.*;
public class ProjectStoreTest {
 @Rule public TemporaryFolder folder=new TemporaryFolder();
 private ProjectStore store;
 @Before public void setup() throws IOException {store=new ProjectStore(folder.newFolder("sources"));}
 @Test public void survivesReopeningAndPreservesUnicode() throws IOException {
  File root=folder.newFolder("restart"); ProjectStore first=new ProjectStore(root);
  first.write("sample.cpp","// café 日本語");
  assertEquals("// café 日本語",new ProjectStore(root).read("sample.cpp"));
 }
 @Test public void rejectsTraversalAndUnexpectedExtensions() throws IOException {
  for(String name:new String[]{"../escape.c","/tmp/test.c","a/b.c",".c","x.sh","x.c/../a.c","hello world.c"}) {
   try{store.write(name,"bad");fail(name);}catch(IOException expected){}
  }
 }
 @Test public void rejectedOversizeWriteKeepsPreviousSource() throws IOException {
  store.write("main.c","original");
  try{store.write("main.c",new String(new char[ProjectStore.MAX_BYTES+1]));fail();}catch(IOException expected){}
  assertEquals("original",store.read("main.c"));
 }
 @Test public void replacesWholeFileAndListsOnlySources() throws IOException {
  store.write("main.c","long original source");store.write("main.c","short");store.write("api.hpp","// header");
  assertEquals("short",store.read("main.c"));assertEquals(java.util.Arrays.asList("api.hpp","main.c"),store.list());
 }
 @Test public void utf8LimitUsesBytesNotCharacters() throws IOException {
  try{store.write("main.c","界".repeat(100000));fail();}catch(IOException expected){}
  assertFalse(store.exists("main.c"));
 }
}
