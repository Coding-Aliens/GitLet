package gitlet;

import java.util.Date;

public class MergeCommit extends Commit {
    public Commit parent2;

    public MergeCommit(String string) {
        super(string);
        MergeCommit.super.date = new Date();
    }
}
