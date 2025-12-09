import java.util.ArrayList;
import java.util.Scanner;

public class Q1 {

    Scanner sc;

    int numStates;
    int numTerminal;
    int numNT;

    String[] terminalNames;
    String[] nonTerminalNames;

    ArrayList<String>[] I;

    String[][] ACTION;
    int[][] GOTO;

    String[][] LALR_ACTION;
    int[][] LALR_GOTO;

    String[] core;

    int[] group;

    boolean shiftReduceConflict = false;
    boolean reduceReduceConflict = false;

    Q2 q2;

    public void readInput() {

        System.out.print("Enter number of terminals: ");
        numTerminal = sc.nextInt();
        sc.nextLine();

        terminalNames = new String[numTerminal];

        System.out.println("Enter terminal names:");
        for (int i = 0; i < numTerminal; i++) {
            System.out.print("Terminal " + i + ": ");
            terminalNames[i] = sc.nextLine().trim();
        }

        System.out.print("Enter number of non-terminals: ");
        numNT = sc.nextInt();
        sc.nextLine();

        nonTerminalNames = new String[numNT];

        System.out.println("Enter non-terminal names:");
        for (int i = 0; i < numNT; i++) {
            System.out.print("Non-terminal " + i + ": ");
            nonTerminalNames[i] = sc.nextLine().trim();
        }

        System.out.print("Enter number of states: ");
        numStates = sc.nextInt();
        sc.nextLine();

        I = new ArrayList[numStates];
        core = new String[numStates];

        for (int i = 0; i < numStates; i++) {

            System.out.print("Enter number of items in state " + i + ": ");
            int c = sc.nextInt();
            sc.nextLine();

            I[i] = new ArrayList<>();

            System.out.println("Enter the items:");
            for (int j = 0; j < c; j++) {
                String it = sc.nextLine().trim();
                I[i].add(it);
            }
        }

        ACTION = new String[numStates][numTerminal];

        System.out.println("\nEnter ACTION table:");
        for (int i = 0; i < numStates; i++) {
            for (int t = 0; t < numTerminal; t++) {
                String a = sc.next();
                if (a.equals("-"))
                    a = null;
                ACTION[i][t] = a;
            }
        }
        sc.nextLine();

        GOTO = new int[numStates][numNT];

        System.out.println("\nEnter GOTO table:");
        for (int i = 0; i < numStates; i++) {
            for (int n = 0; n < numNT; n++) {
                GOTO[i][n] = sc.nextInt();
            }
        }
    }

    public void makeCore() {

        for (int s = 0; s < numStates; s++) {

            ArrayList<String> items = new ArrayList<>();

            for (String x : I[s]) {

                int p = x.indexOf(',');

                if (p >= 0)
                    x = x.substring(0, p).trim();

                items.add(x);
            }

            items.sort(String::compareTo);

            StringBuilder c1 = new StringBuilder();
            for (String it : items) {
                c1.append(it).append(" | ");
            }

            core[s] = c1.toString();
        }
    }

    public void mergeStates() {

        group = new int[numStates];
        int gCnt = 0;

        for (int i = 0; i < numStates; i++) {
            if (core[i] == null || core[i].isEmpty())
                continue;

            if (group[i] == 0) {
                gCnt++;
                group[i] = gCnt;

                for (int j = i + 1; j < numStates; j++) {
                    if (core[j] == null || core[j].isEmpty())
                        continue;
                    if (core[i].equals(core[j])) {
                        group[j] = gCnt;
                    }
                }
            }
        }

        for (int i = 0; i < numStates; i++) {
            if (group[i] == 0)
                continue;

            for (int j = i + 1; j < numStates; j++) {
                if (group[i] == group[j] && i != j) {

                    for (String item : I[j]) {
                        if (!I[i].contains(item)) {
                            I[i].add(item);
                        }
                    }

                    I[j].clear();
                    core[j] = "";
                }
            }
        }
    }

    public void makeLALR() {

    int mg = 0;
    for (int i = 0; i < numStates; i++) {
        if (group[i] > mg)
            mg = group[i];
    }

    LALR_ACTION = new String[mg][numTerminal];
    LALR_GOTO   = new int[mg][numNT];

    for (int g = 0; g < mg; g++) {
        for (int n = 0; n < numNT; n++) {
            LALR_GOTO[g][n] = -1;
        }
    }

    for (int s = 0; s < numStates; s++) {

        int g = group[s] - 1;
        if (g < 0) continue;

        for (int t = 0; t < numTerminal; t++) {

            String aOld = ACTION[s][t];
            if (aOld == null) continue;

            String aNew = aOld;

 
            if (aOld.contains("/")) {

                String[] parts = aOld.split("/");
                String left  = parts[0];
                String right = parts[1];

                if (left.startsWith("r") && right.startsWith("r")) {
                    reduceReduceConflict = true;
                }
                else if ((left.startsWith("s") && right.startsWith("r")) ||
                         (left.startsWith("r") && right.startsWith("s"))) {
                    shiftReduceConflict = true;
                }
            }


            if (aNew.equals("s") || aNew.equals("r")) continue;
            if ((aNew.startsWith("s") || aNew.startsWith("r")) && aNew.length() == 1) continue;

  
            if (LALR_ACTION[g][t] == null) {

                LALR_ACTION[g][t] = aNew;

            } else {

                String cur = LALR_ACTION[g][t];

                if (!cur.equals(aNew)) {

                    if (cur.startsWith("s") && aNew.startsWith("s")) {

                        if (cur.length() > 1)
                            LALR_ACTION[g][t] = cur;
                        else
                            LALR_ACTION[g][t] = aNew;

                    } else {

               
                        LALR_ACTION[g][t] = cur + "/" + aNew;
                    }

                 
                    if ((cur.startsWith("s") && aNew.startsWith("r")) ||
                        (cur.startsWith("r") && aNew.startsWith("s"))) {
                        shiftReduceConflict = true;
                    }
                    else if (cur.startsWith("r") && aNew.startsWith("r")) {
                        reduceReduceConflict = true;
                    }
                }
            }
        }

     
        for (int n = 0; n < numNT; n++) {

            int go = GOTO[s][n];
            if (go == -1) continue;

            int ng = group[go] - 1;

            if (LALR_GOTO[g][n] == -1) {
                LALR_GOTO[g][n] = ng;
            }
        }
    }
}

    String getMergedStateID(int g) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < numStates; i++) {
            if (group[i] - 1 == g) {
                if (!first)
                    sb.append("");
                sb.append(i);
                first = false;
            }
        }
        return sb.toString();
    }

    String beautify(String action) {

    if (action == null || action.equals("-"))
        return "-";

    // ✅ acc أو أي شيء غير s/r
    if (!(action.startsWith("s") || action.startsWith("r")))
        return action;

    // ✅ لو فيه Conflict (مثل s6/r3 أو r2/r3)
    if (action.contains("/")) {

        String[] parts = action.split("/");

        String left  = parts[0];  // غالبًا s6 أو r3
        String right = parts[1];  // r3 أو r4

        // ✅ لو اليسار Shift → ندمجه
        if (left.startsWith("s") && left.length() > 1) {

            int oldState = Integer.parseInt(left.substring(1));
            String merged = getMergedStateID(oldState);

            if (merged != null && !merged.isEmpty())
                left = "s" + merged;
        }

        // ✅ نعيد الاثنين معًا (الـ r لا يتغير)
        return left + "/" + right;
    }

    // ✅ لو كانت Reduce فقط → نرجعها كما هي
    if (action.startsWith("r"))
        return action;

    // ✅ هنا Shift فقط بدون conflict
    if (action.startsWith("s") && action.length() > 1) {

        int oldState = Integer.parseInt(action.substring(1));
        String merged = getMergedStateID(oldState);

        if (merged == null || merged.isEmpty())
            return action;

        return "s" + merged;
    }

    return action;
}

    public void printLALR() {

        int mg = LALR_ACTION.length;

        System.out.println("\n------ LALR(1) Parsing Table ------\n");

        System.out.printf("%-10s", "STATE");

        for (int t = 0; t < numTerminal; t++)
            System.out.printf("%-10s", terminalNames[t]);

        for (int n = 0; n < numNT; n++)
            System.out.printf("%-10s", nonTerminalNames[n]);

        System.out.println();

        for (int g = 0; g < mg; g++) {

            String merged = getMergedStateID(g);
            if (merged.isEmpty())
                merged = "" + g;

            System.out.printf("%-10s", merged);

            for (int t = 0; t < numTerminal; t++) {
                String a = LALR_ACTION[g][t];
                if (a == null)
                    System.out.printf("%-10s", "-");
                else
                    System.out.printf("%-10s", beautify(a));
            }

            for (int n = 0; n < numNT; n++) {
                int v = LALR_GOTO[g][n];
                if (v == -1)
                    System.out.printf("%-10s", "-");
                else
                    System.out.printf("%-10s", getMergedStateID(v));
            }

            System.out.println();
        }

    }
}
