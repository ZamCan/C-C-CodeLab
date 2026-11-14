package com.zamcan.codelab;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import java.io.*;
public class WorkspaceRepositoryTest{
    @Rule public TemporaryFolder temp=new TemporaryFolder();
    @Test public void projectsKeepSameFilenameSeparate()throws IOException{WorkspaceRepository projects=new WorkspaceRepository(temp.getRoot());projects.create("One");projects.create("Two");projects.open("One").write("main.c","one");projects.open("Two").write("main.c","two");assertEquals("one",projects.open("One").read("main.c"));assertEquals("two",projects.open("Two").read("main.c"));}
    @Test public void legacyWorkspaceIsPreserved()throws IOException{new ProjectStore(new File(temp.getRoot(),"workspace")).write("main.c","legacy");assertEquals("legacy",new WorkspaceRepository(temp.getRoot()).open("Practice").read("main.c"));}
    @Test public void refusesDuplicateAndTraversal()throws IOException{WorkspaceRepository projects=new WorkspaceRepository(temp.getRoot());projects.create("One");for(String name:new String[]{"One","../outside","/tmp","Practice"}){try{projects.create(name);fail(name);}catch(IOException expected){}}}
}
