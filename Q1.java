

package com.mycompany.mavenproject2;

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
                if (a.equals("-")) a = null;
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
            if (core[i] == null || core[i].isEmpty()) continue;

            if (group[i] == 0) {
                gCnt++;
                group[i] = gCnt;

                for (int j = i + 1; j < numStates; j++) {
                    if (core[j] == null || core[j].isEmpty()) continue;
                    if (core[i].equals(core[j])) {
                        group[j] = gCnt;
                    }
                }
            }
        }

        for (int i = 0; i < numStates; i++) {
            if (group[i] == 0) continue;

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
        LALR_GOTO = new int[mg][numNT];

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

                if (aOld.startsWith("s")) {
                    int oldNext = Integer.parseInt(aOld.substring(1));
                    int newNext = group[oldNext] - 1;
                    aNew = "s" + newNext;
                }

                if (LALR_ACTION[g][t] == null) {
                    LALR_ACTION[g][t] = aNew;
                } else {
                    String cur = LALR_ACTION[g][t];

                    if ((cur.startsWith("s") && aNew.startsWith("r")) ||
                        (cur.startsWith("r") && aNew.startsWith("s"))) {
                        shiftReduceConflict = true;
                    }

                    if (cur.startsWith("r") && aNew.startsWith("r")
                            && !cur.equals(aNew)) {
                        reduceReduceConflict = true;
                    }
                }
            }

            for (int n = 0; n < numNT; n++) {

                int go = GOTO[s][n];
                if (go == -1) continue;

                int ng = group[go] - 1;

                if (LALR_GOTO[g][n] == -1)
                    LALR_GOTO[g][n] = ng;
            }
        }
    }


   
    String getMergedStateID(int g){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numStates; i++) {
            if (group[i] - 1 == g ) {
                sb.append(i);
            }
        }
        return sb.toString();
    }


   
    String beautify(String action) {

        if (action == null) return "-";

        if (action.startsWith("s")) {
            char type = action.charAt(0);
            int next = Integer.parseInt(action.substring(1));
            String merged = getMergedStateID(next);
            return type + merged;
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

        if (shiftReduceConflict)
            System.out.println("\nShift/Reduce conflict → Grammar is NOT LALR(1).");

        if (reduceReduceConflict)
            System.out.println("\nReduce/Reduce conflict → Grammar is NOT LALR(1).");

        if (!shiftReduceConflict && !reduceReduceConflict)
            System.out.println("\nGrammar is LALR(1). No conflicts detected.");
    }
}
