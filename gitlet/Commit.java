package gitlet;

// TODO: any imports you need here

import edu.princeton.cs.algs4.ST;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Lane Xie
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    public String message;
    public Date date;
    public Commit parent;
    public TreeMap<String, String> fileMap = new TreeMap<>();
    public String sha1;
    public File removedFile = new File("r");
    /**
     * from name to the files
     */
    /* TODO: fill in the rest of this class. */
    public Commit(String string) {
        if (string.equals("initial commit")) {
            date = new Date(0);
            message = string;
            parent = null;
            Utils.writeContents(removedFile,"");
        } else {
            message = string;
        }
    }
}
