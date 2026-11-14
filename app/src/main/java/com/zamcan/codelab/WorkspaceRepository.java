package com.zamcan.codelab;
import java.io.*;
import java.util.*;

/** Per-project source directories; legacy workspace is preserved as Practice. */
public final class WorkspaceRepository {
    private final File files, projects;
    public WorkspaceRepository(File files)throws IOException{
        this.files=files;projects=new File(files,"projects");
        if(!projects.isDirectory()&&!projects.mkdirs())throw new IOException("Cannot open projects");
    }
    private File path(String name)throws IOException{
        if(name==null||!name.matches("[A-Za-z][A-Za-z0-9_-]{0,39}"))throw new IOException("Use 1–40 letters, digits, underscores, or hyphens; begin with a letter");
        File root="Practice".equals(name)?new File(files,"workspace"):new File(projects,name);
        File parent="Practice".equals(name)?files:projects;
        if(!root.getCanonicalFile().getParentFile().equals(parent.getCanonicalFile()))throw new IOException("Invalid project path");
        return root;
    }
    public ProjectStore open(String name)throws IOException{return new ProjectStore(path(name));}
    public void create(String name)throws IOException{File root=path(name);if(root.exists()||"Practice".equals(name))throw new IOException("Project already exists");if(!root.mkdir())throw new IOException("Cannot create project");}
    public List<String> list(){List<String> result=new ArrayList<>();result.add("Practice");String[] names=projects.list((dir,name)->name.matches("[A-Za-z][A-Za-z0-9_-]{0,39}")&&new File(dir,name).isDirectory());if(names!=null){Arrays.sort(names);result.addAll(Arrays.asList(names));}return result;}
}
