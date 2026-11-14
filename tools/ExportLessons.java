import com.zamcan.codelab.*;
import java.nio.file.*;
public final class ExportLessons {
    public static void main(String[] args) throws Exception {
        Path root=Paths.get(args[0]);Files.createDirectories(root);
        for(boolean cpp:new boolean[]{false,true}) {
            Lesson[] lessons=Curriculum.lessons(cpp);
            for(int i=0;i<lessons.length;i++) Files.writeString(root.resolve((cpp?"cpp":"c")+i+(cpp?".cpp":".c")),lessons[i].code);
        }
    }
}
