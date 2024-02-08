package gitlet;
import edu.princeton.cs.algs4.ST;
import net.sf.saxon.trans.SymbolicName;
import org.apache.logging.log4j.core.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;
// TODO: any imports you need here
/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author LaneXie
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGE = Utils.join(Repository.GITLET_DIR, "stage");
    public static final File commitFile = Utils.join(GITLET_DIR,"commit");
    public static final File pointerFile = Utils.join(Repository.GITLET_DIR, "pointer");
    public static final File bolbFile = Utils.join(Repository.GITLET_DIR, "bolb");
    public static final File STATUS = Utils.join(Repository.GITLET_DIR, "status");
    public static final File branchFile = Utils.join(Repository.GITLET_DIR,"branch");
    public static final File stagedFile = Utils.join(STATUS,"staged");
    public static final File removedFile = Utils.join(GITLET_DIR, "removed");
    public Commit pointer;
    public Repository() {
        if (pointerFile.exists() && branchFile.exists()) {
            String route = readContentsAsString(Repository.pointerFile);
            TreeMap<String, Commit> mymap = readObject(Repository.branchFile, TreeMap.class);
            pointer = mymap.get(route);
        } else {
            pointer = null;
        }
    }
    public void rmBranch(String name) {
        String current = readContentsAsString(pointerFile);
        if (name.equals(current)){
            System.out.println("Cannot remove the current branch.");
            return;
        }
        TreeMap<String, Commit> branchMap = readObject(branchFile, TreeMap.class);
        if (!branchMap.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        branchMap.remove(name);
        Utils.writeObject(branchFile,branchMap);
    }
    public void branch(String name) {
        TreeMap myMap = readObject(branchFile, TreeMap.class);
        if (myMap.containsKey(name)){
            System.out.println("A branch with that name already exists.");
            return;
        }
        myMap.put(name, pointer);
        writeObject(branchFile, myMap);
    }
    public void init() {
        if (GITLET_DIR.exists() == false) {
            GITLET_DIR.mkdir();
        } else {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        }
        if (!removedFile.exists()) {
            removedFile.mkdir();
        }
        if (!commitFile.exists()) {
            commitFile.mkdir();
        }
        if (STATUS.exists() == false) {
            STATUS.mkdir();
            File untrackedFile = Utils.join(STATUS, "untracked");
            Utils.writeContents(untrackedFile,"");
        }
        if (STAGE.exists() == false) {
            STAGE.mkdir();
        }
        if (bolbFile.exists() == false) {
            bolbFile.mkdir();
        }
        addCommit("initial commit");
        TreeMap branchMap = new TreeMap<String, Commit>();
        branchMap.put("main", pointer);
        Utils.writeObject(branchFile, branchMap);
        Repository.STAGE.mkdir();
        Utils.writeContents(pointerFile, "main");
    }

    public void restore(File name) {
        Commit temp = pointer;
        String target1 = this.pointer.fileMap.get(name.getName());
        File finalTarget = Utils.join(bolbFile, target1);
        Utils.writeContents(name, readContentsAsString(finalTarget));
        pointer = temp;
    }
    public void reset(String commitId) {
        TreeMap<String, String> presence = pointer.fileMap;
        File[] currentCWD = Repository.CWD.listFiles();
        for (File cwd: currentCWD) {
            if (!cwd.isDirectory()){
                if (!cwd.getName().equals("r") ){
                    if (!pointer.fileMap.containsKey(cwd.getName()) &&!join(STAGE,cwd.getName()).exists()) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                }
            }
        }
        Commit temp = pointer;
        while (pointer != null) {
            if (pointer.sha1.equals(commitId)) {
                break;
            } else {
                pointer = pointer.parent;
            }
        }
        File myCommit = join(commitFile,commitId);
        if (myCommit.exists()) {
            pointer = readObject(myCommit, Commit.class);
        }
        else {
            pointer = null;
        }
        if (pointer == null) {
            System.out.println("No commit with that id exists.");
            pointer = temp;
            return;
        }
        File[] listOfFilesCWD = Repository.CWD.listFiles();
        for (File my :listOfFilesCWD) {
            if (!my.isDirectory() && my.getName()!="r") {
                my.delete();
            }
        }
        for (String key:pointer.fileMap.keySet()) {
            File bolb =join(bolbFile, pointer.fileMap.get(key));
            File target = join(CWD, key);
            writeContents(target, readContentsAsString(bolb));
        }
        File[] listOfFiles = Repository.STAGE.listFiles();
        for (File file : listOfFiles) {
            file.delete();
            File newFile = join(CWD, file.getName());
            if (newFile.exists()) {
                newFile.delete();
            }
        }
        String branch = readContentsAsString(pointerFile);
        TreeMap<String, Commit> myMap = readObject(branchFile, TreeMap.class);
        myMap.put(branch, pointer);
        Utils.writeObject(branchFile, myMap);
    }

    public void restore(String id, File name) {
        Commit temp = pointer;
        while (pointer != null &&id.length()==40) {
            if (pointer.sha1.equals(id)) {
                break;
            }
            else {
                pointer = pointer.parent;
            }
        }
        while (pointer != null &&id.length()<40) {
            String temps = pointer.sha1.substring(0,id.length());
            if (temps.equals(id)) {
                break;}
            else {
                pointer = pointer.parent;
            }
        }
        if (pointer == null) {
            System.out.println("No commit with that id exists.");
            pointer = temp;
            return;
        }
        String target1 = this.pointer.fileMap.get(name.getName());
        if (target1==null) {
            System.out.println("File does not exist in that commit.");
        }
        else {File finalTarget = Utils.join(bolbFile, target1);
            writeContents(name, readContentsAsString(finalTarget));
        }
        pointer = temp;
    }
    public void log() {
        Commit cur = pointer;
        while (cur != null) {
            System.out.println("===");
            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZZZZ", Locale.US);
            System.out.println("commit " + cur.sha1);
            if (cur.getClass() == MergeCommit.class) {
                String first = cur.parent.sha1.substring(0,7);
                String second = ((MergeCommit) cur).parent2.sha1.substring(0,7);
                System.out.println("Merge: "+ first + " " +second);
            }
            System.out.println("Date: " + formatter.format(cur.date));
            System.out.println(cur.message);
            System.out.println("");
            cur = cur.parent;
        }
    }
    public void status() throws IOException {
        if (!STATUS.exists()||!STAGE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        ArrayList<String> showlist = new ArrayList<String>();
        File[] listOfFiles = Repository.STAGE.listFiles();
        int index = 0;
        for (File file : listOfFiles) {
            FileWriter fr = new FileWriter(stagedFile, true);
            fr.write(file.getName());
            fr.write("\n");
            fr.close();
            showlist.add(file.getName());
        }
        System.out.println("=== Branches ===");
        TreeMap<String, Commit> branchMap = readObject(branchFile, TreeMap.class);
        for (String branch: branchMap.keySet()) {
            if (readContentsAsString(pointerFile).equals(branch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        if (stagedFile.exists()) {
            Comparator<String> c = Comparator.comparing(String::toString);
            showlist.sort(c);
            for (String name:showlist) {
                System.out.println(name);
            }
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        File[] listFile = Repository.removedFile.listFiles();
        for (File myFile: listFile) {
            System.out.println(myFile.getName());
        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        System.out.println("");
    }
    public void find(String message) {
        Commit cur = pointer;
        File[] mylist = commitFile.listFiles();
        boolean found = false;
        for (File commitFile : mylist) {
            Commit myCommit = readObject(commitFile, Commit.class);
            if (myCommit.message.equals(message)) {
                System.out.println(myCommit.sha1);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }
    public void switcher(String name) {
        if (name.equals(readContentsAsString(pointerFile))){
            System.out.println("No need to switch to the current branch.");
            return;
        }
        TreeMap<String, Commit> myMap = readObject(branchFile,TreeMap.class);
        if (!myMap.containsKey(name)) {
            System.out.println("No such branch exists.");
            return;
        }
        TreeMap<String, String> presence = myMap.get(name).fileMap;
        File[] currentCWD = Repository.CWD.listFiles();
        for (File cwd: currentCWD) {
            if (!cwd.isDirectory()){
                if (!cwd.getName().equals("r") ){
                    if (!pointer.fileMap.containsKey(cwd.getName())) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }

                }
            }
        }
        for (String fileName: pointer.fileMap.keySet()) {
            if (!presence.containsKey(fileName)){
                File deleteFile = join(CWD, fileName);
                deleteFile.delete();
            }
            for (String changeFile : presence.keySet()) {
                File target = join(CWD, changeFile);
                File from = join(bolbFile, presence.get(changeFile));
                writeContents(target, readContentsAsString(from));
            }

        }
        pointer = myMap.get(name);
        Utils.writeContents(pointerFile,name);
    }
    public void remove(File file) throws IOException {
        File fileInTheStage = Utils.join(Repository.STAGE, file.getPath());
        boolean found1 = false;
        boolean found2 = false;
        if (fileInTheStage.exists()) {
            fileInTheStage.delete();
            found1 = true;
        }
        Commit cur = pointer;
        if (cur.fileMap.get(file.getName()) != null) {
            found2 = true;
            if(file.exists()) {
                File temp = join(removedFile,file.getPath());
                writeContents(temp,Utils.readContentsAsString(file));
                restrictedDelete(file);
            }
            else {
                File temp = join(removedFile,file.getPath());
                writeContents(temp,"");
            }
        }
        if (!found1 && !found2) {
            System.out.println("No reason to remove the file.");
        }
    }
    public void globalLog() {
        File[] a = commitFile.listFiles();
        for (File commitfile: a) {
            Commit cur = readObject(commitfile, Commit.class);
            System.out.println("===");
            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZZZZ", Locale.US);
            System.out.println("commit " + cur.sha1);
            System.out.println("Date: " + formatter.format(cur.date));
            System.out.println(cur.message);
            System.out.println("");
        }
    }
    public void addCommit(String string) {
        if (string.equals("initial commit")) {
            Commit myCommit = new Commit(string);
            myCommit.parent = null;
            myCommit.date = new Date(0);
            myCommit.sha1 = sha1(serialize(myCommit));
            for(File file: STAGE.listFiles()) {
                file.delete();
            }
            pointer = myCommit;
            File myFile = join(commitFile, sha1(serialize(myCommit)));
            writeObject(myFile, myCommit);
            return;
        }
        Commit myCommit = new Commit(string);
        File[] currentCWD = Repository.CWD.listFiles();
        boolean removed = false;
        for (String a:pointer.fileMap.keySet()) {
            File file = join(CWD,a);
            if (!file.exists()) {
                removed = true;
            }
            else {
                myCommit.fileMap.put(a,pointer.fileMap.get(a));
            }
        }
        File[] listOfFiles = Repository.STAGE.listFiles();
        if (listOfFiles.length == 0 && !removed) {
            System.out.println("No changes added to the commit. ");
            return;
        }
        for (File file : listOfFiles) {
            myCommit.fileMap.put(file.getName(), sha1(serialize(readContentsAsString(file))));
        }
        myCommit.date = new Date();
        myCommit.message = string;
        myCommit.parent = pointer;
        String commitID = sha1(serialize(myCommit));
        myCommit.sha1 = commitID;
        File myFile = join(commitFile, commitID);
        writeObject(myFile, myCommit);
        for (File file:listOfFiles) {
            file.delete();
        }
        File[] listOfFiles2 = Repository.removedFile.listFiles();
        for (File file:listOfFiles2) {
            file.delete();
        }
        pointer = myCommit;
        TreeMap<String, Commit> myMap = readObject(branchFile, TreeMap.class);
        myMap.put(readContentsAsString(pointerFile), myCommit);
        writeObject(branchFile, myMap);
    }
    public void merge(String branch) {
        if (readContentsAsString(pointerFile).equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        TreeMap<String, Commit> myMap = readObject(branchFile, TreeMap.class);
        if (!myMap.containsKey(branch)){
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File[] currentCWD = Repository.CWD.listFiles();
        for (File cwd: currentCWD) {
            if (!cwd.isDirectory()) {
                if (!cwd.getName().equals("r")) {
                    if (!pointer.fileMap.containsKey(cwd.getName()) && !join(STAGE, cwd.getName()).exists()) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                }
            }
        }
        if (STAGE.listFiles().length != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (removedFile.listFiles().length != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        /**ERROR CASE FINISHED**/
        MergeCommit mergeCommit =new MergeCommit("Merged "+branch+" into "+ readContentsAsString(pointerFile) +"\\.");
        Commit givenBranch= myMap.get(branch);
        Commit temp = givenBranch;
        ArrayList<String> listOfCommit = new ArrayList<>();
        while (givenBranch!=null) {
            listOfCommit.add(givenBranch.sha1);
            givenBranch = givenBranch.parent;
        }
        givenBranch = temp;
        Commit temp2 = pointer;
        while (pointer!=null) {
            if (listOfCommit.contains(pointer.sha1)) {
                break;
            }
            if (pointer.getClass()==MergeCommit.class) {
                pointer = ((MergeCommit)pointer).parent2;
            }
            else {
                pointer = pointer.parent;}
        }
        Commit splitPoint = pointer;
        pointer = temp2;
        Set<String> filesInSplitPoint = splitPoint.fileMap.keySet();
        Set<String> filesInGivenBranch = givenBranch.fileMap.keySet();
        Set<String> filesInCurrent = pointer.fileMap.keySet();
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(filesInCurrent);
        allFiles.addAll(filesInGivenBranch);
        allFiles.addAll(filesInSplitPoint);
        /** SETUP FILE SETS**/
        for (String file: allFiles) {
            if (filesInCurrent.contains(file) && filesInSplitPoint.contains(file) && filesInGivenBranch.contains(file)) {
                /**the file exists in all commits**/
                if (pointer.fileMap.get(file).equals(splitPoint.fileMap.get(file))) {
                    if (!givenBranch.fileMap.get(file).equals(splitPoint.fileMap.get(file))) {
                        mergeCommit.fileMap.put(file, givenBranch.fileMap.get(file));
                        File target = join(CWD, file);
                        File des = join(bolbFile, givenBranch.fileMap.get(file));
                        writeContents(target, readContentsAsString(des));
                    }/**1**/
                }
                else if (!pointer.fileMap.get(file).equals(splitPoint.fileMap.get(file)))  {
                    if (!pointer.fileMap.get(file).equals(givenBranch.fileMap.get(file))) {
                        if (!givenBranch.fileMap.get(file).equals(splitPoint.fileMap.get(file))) { /**8.1**/
                            System.out.println("Encountered a merge conflict.");
                            String Content = "<<<<<<< HEAD\n";
                            File target1 = join(bolbFile, pointer.fileMap.get(file));
                            String string1 = readContentsAsString(target1);
                            String string2 = "=======\n";
                            File target2 = join(bolbFile, givenBranch.fileMap.get(file));
                            String string3 = readContentsAsString(target2);
                            String string4 = ">>>>>>>\n";
                            Content = Content+string1+string2+string3+string4;
                            File des = join(CWD, file);
                            writeContents(des, Content);
                            File newFile2 = Utils.join(Repository.bolbFile, Utils.sha1(serialize(readContentsAsString(des))));
                            Utils.writeContents(newFile2, readContentsAsString(des));
                            mergeCommit.fileMap.put(des.getName(), sha1(serialize(readContentsAsString(des))));
                        }
                    }
                    else {/**3(Modified in same way)**/
                        mergeCommit.fileMap.put(file, pointer.fileMap.get(file));
                    }
                }
            }
            else if (!filesInCurrent.contains(file) && filesInSplitPoint.contains(file) && filesInGivenBranch.contains(file)) {
                if (givenBranch.fileMap.get(file).equals(splitPoint.fileMap.get(file))) {
                    continue;
                }
                /**7**/
            }
            else if (!filesInCurrent.contains(file) && !filesInSplitPoint.contains(file) && filesInGivenBranch.contains(file)) {
                String targetSha1 = givenBranch.fileMap.get(file);
                File targetFile = join(bolbFile, targetSha1);
                File destination = join(CWD, file);
                writeContents(destination, readContentsAsString(targetFile));
                mergeCommit.fileMap.put(file, givenBranch.fileMap.get(file));/**5**/
            }
            else if (filesInCurrent.contains(file) && !filesInSplitPoint.contains(file) && !filesInGivenBranch.contains(file)) {
                mergeCommit.fileMap.put(file, pointer.fileMap.get(file));/**4**/
            }
            else if (filesInCurrent.contains(file) && filesInSplitPoint.contains(file) && !filesInGivenBranch.contains(file)) {
                if (pointer.fileMap.get(file).equals(splitPoint.fileMap.get(file))) {
                    File target = join(CWD, file);
                    if(target.exists()) {
                        target.delete();
                    }
                    ;/**6**/
                }
                else {
                    System.out.println("Encountered a merge conflict.");
                    String Content = "<<<<<<< HEAD\n";
                    File target1 = join(bolbFile, pointer.fileMap.get(file));
                    String string1 = readContentsAsString(target1);
                    String string2 = "=======\n";
                    String string4 = ">>>>>>>\n";
                    Content = Content+string1+string2+string4;
                    File des = join(CWD, file);
                    writeContents(des, Content);
                    File newFile2 = Utils.join(Repository.bolbFile, Utils.sha1(serialize(readContentsAsString(des))));
                    Utils.writeContents(newFile2, readContentsAsString(des));
                    mergeCommit.fileMap.put(des.getName(), sha1(serialize(readContentsAsString(des))));

                }
            }
            else if (!filesInCurrent.contains(file) && filesInSplitPoint.contains(file) && !filesInGivenBranch.contains(file)) {
                continue;/**3(both removed)**/
            }
        }
        if (!splitPoint.sha1.equals(pointer.sha1) && !splitPoint.sha1.equals(givenBranch.sha1)) {
            String nameOfMain = readContentsAsString(pointerFile);
            mergeCommit.parent = myMap.get(nameOfMain);
            mergeCommit.parent2 = myMap.get(branch);
            mergeCommit.sha1 = sha1(serialize(mergeCommit));
            mergeCommit.date = new Date();
            myMap.put(branch, mergeCommit);
            myMap.put(nameOfMain, mergeCommit);
            writeObject(branchFile, myMap);
            pointer = mergeCommit;
        } else if (splitPoint.sha1.equals(givenBranch.sha1)) {
            System.out.println("Given branch is an ancestor of the current branch.");
        } else if (splitPoint.sha1.equals(pointer.sha1)) {
            System.out.println("Current branch fast-forwarded.");
        }
    }

}