package gitlet;
import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Lane Xie
 */
public class Main {
    public File Editing = null;


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        Repository r = new Repository();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                r.init();
                Commit firstCommit = new Commit("initial commit");
                r.addCommit("initial commit");
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                File addFile = new File(args[1]);
                addAndStage.add(addFile);
                break;
            // TODO: FILL THE REST IN
            case "rm":
                File name = new File(args[1]);
                r.remove(name);
                break;
            case "global-log":
                r.globalLog();
                break;
            case "branch":
                if (args.length ==2){
                    r.branch(args[1]);
                }
                break;
            case "commit":
                if (args.length == 2) {
                    if (args[1].equals("")){
                        System.out.println("Please enter a commit message.");
                        break;
                    }
                    r.addCommit(args[1]);}
                else {
                    System.out.println("Please enter a commit message.");
                }
                break;
            case "status" :
                r.status();
                break;
            case "merge" :
                if (args.length==2) {
                    r.merge(args[1]);
                }
                break;
            case "reset" :
                if (args.length==2) {
                r.reset(args[1]);
                break;
                }
            case "rm-branch":
                if (args.length==2) {
                    r.rmBranch(args[1]);
                    break;
                }
            case "switch":
                if (args.length==2) {
                    r.switcher(args[1]);
                    break;
                }
            case "restore" :
                if (args.length == 3){
                File target = new File(args[2]);
                r.restore(target);
                break;
                }
                else if (args.length == 4) {
                    if (args[2].equals("--")==false){
                        System.out.println("Incorrect operands.");
                    }
                    File target = new File(args[3]);
                    String id = args[1];
                    r.restore(id, target);
                    break;
                }
            case "find" :
                if (args.length == 2) {
                    String message = args[1];
                    r.find(message);
                }
                break;
            case "log" :
                r.log();
                break;
            default:
                System.out.println("No command with that name exists.");
        }

    }
}
