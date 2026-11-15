package com.zamcan.codelab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Deterministic code-order exercises with per-position corrective feedback. */
public final class SnippetExercise {
    public static final class Line {
        public final int id;
        public final String code;
        public Line(int id,String code) {
            if (id < 1 || code == null) throw new IllegalArgumentException("Invalid line");
            this.id=id;this.code=code;
        }
    }
    public static final class Feedback {
        public final int position;
        public final int selectedId;
        public final int correctId;
        public final boolean correct;
        public final String explanation;
        Feedback(int position,int selectedId,int correctId,boolean correct,String explanation) {
            this.position=position;this.selectedId=selectedId;this.correctId=correctId;
            this.correct=correct;this.explanation=explanation;
        }
    }
    public final String id,title,language;
    public final List<Line> snippets;
    public final List<Integer> answer;
    public final List<String> explanations;

    public SnippetExercise(String id,String title,String language,List<Line> snippets,
                           List<Integer> answer,List<String> explanations) {
        if (id==null || id.isEmpty() || title==null || title.isEmpty()
            || !("c".equals(language)||"cpp".equals(language)))
            throw new IllegalArgumentException("Invalid exercise metadata");
        if (snippets==null || answer==null || explanations==null
            || snippets.size()<2 || snippets.size()!=answer.size()
            || snippets.size()!=explanations.size())
            throw new IllegalArgumentException("Exercise requires matching lines and explanations");
        Set<Integer> ids=new HashSet<>();
        for(Line line:snippets) if(!ids.add(line.id)) throw new IllegalArgumentException("Duplicate snippet id");
        if(!ids.equals(new HashSet<>(answer)) || new HashSet<>(answer).size()!=answer.size())
            throw new IllegalArgumentException("Answer must be a permutation of snippet ids");
        this.id=id;this.title=title;this.language=language;
        this.snippets=Collections.unmodifiableList(new ArrayList<>(snippets));
        this.answer=Collections.unmodifiableList(new ArrayList<>(answer));
        this.explanations=Collections.unmodifiableList(new ArrayList<>(explanations));
    }

    public List<Feedback> check(List<Integer> proposed) {
        if(proposed==null || proposed.size()!=answer.size()
            || !new HashSet<>(proposed).equals(new HashSet<>(answer))
            || new HashSet<>(proposed).size()!=proposed.size())
            throw new IllegalArgumentException("Submit every snippet exactly once");
        List<Feedback> result=new ArrayList<>();
        for(int i=0;i<answer.size();i++) {
            int actual=proposed.get(i), expected=answer.get(i);
            result.add(new Feedback(i+1,actual,expected,actual==expected,explanations.get(i)));
        }
        return Collections.unmodifiableList(result);
    }

    public int score(List<Integer> proposed) {
        int count=0;
        for(Feedback feedback:check(proposed)) if(feedback.correct) count++;
        return count;
    }

    public String correctedSource() {
        Map<Integer,String> byId=new HashMap<>();
        for(Line line:snippets) byId.put(line.id,line.code);
        StringBuilder source=new StringBuilder();
        for(Integer id:answer) source.append(byId.get(id)).append('\n');
        return source.toString();
    }

    public static List<SnippetExercise> starterExercises() {
        List<SnippetExercise> items=new ArrayList<>();
        items.add(new SnippetExercise("c-hello","Hello, World!","c",
            Arrays.asList(new Line(3,"    printf(\"Hello, World!\\n\");"),
                new Line(1,"#include <stdio.h>"),
                new Line(5,"}"),
                new Line(2,"int main(void) {"),
                new Line(4,"    return 0;")),
            Arrays.asList(1,2,3,4,5),
            Arrays.asList("Import the declarations for printf.",
                "Define the C program entry point.",
                "Print the greeting using printf.",
                "Return a successful exit status.",
                "Close the main function.")));
        items.add(new SnippetExercise("cpp-hello","C++ output","cpp",
            Arrays.asList(new Line(4,"    return 0;"),
                new Line(2,"int main() {"),
                new Line(1,"#include <iostream>"),
                new Line(5,"}"),
                new Line(3,"    std::cout << \"Hello, World!\\n\";")),
            Arrays.asList(1,2,3,4,5),
            Arrays.asList("Include the standard stream declarations.",
                "Define the C++ entry point.",
                "Send text to standard output.",
                "Report successful completion.",
                "Close the function.")));
        items.add(new SnippetExercise("c-temperature","Celsius to Fahrenheit","c",
            Arrays.asList(new Line(3,"    double celsius = 25.0;"),
                new Line(5,"    printf(\"%.1f\\n\", fahrenheit);"),
                new Line(1,"#include <stdio.h>"),
                new Line(7,"}"),
                new Line(4,"    double fahrenheit = celsius * 9.0 / 5.0 + 32.0;"),
                new Line(2,"int main(void) {"),
                new Line(6,"    return 0;")),
            Arrays.asList(1,2,3,4,5,6,7),
            Arrays.asList("Include printf's header.","Start main.",
                "Declare the Celsius input.","Calculate the Fahrenheit result.",
                "Display the calculated value.","Return success.","Close main.")));
        return Collections.unmodifiableList(items);
    }
}
