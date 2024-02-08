package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

public class addAndStage {

    static void add(File file) {
        if(!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String route = readContentsAsString(Repository.pointerFile);
        TreeMap<String, Commit> mymap = readObject(Repository.branchFile, TreeMap.class);
        Commit pointer = mymap.get(route);
        File newFile = Utils.join(Repository.STAGE, file.getPath());
        Utils.writeContents(newFile, readContentsAsString(file));
        File removedFile = join(Repository.removedFile, file.getPath());
        if (removedFile.exists()) {
            removedFile.delete();
        }
        String sha =  sha1(serialize(readContentsAsString(newFile)));
        String sha2 = pointer.fileMap.get(file.getName());
        if (sha2!=null){
            if (sha.equals(sha2)) {
                newFile.delete();
                return;
            }
        }
        File newFile2 = Utils.join(Repository.bolbFile, Utils.sha1(serialize(readContentsAsString(newFile))));
        Utils.writeContents(newFile2, readContentsAsString(newFile));
    }
}
