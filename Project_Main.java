
package com.mycompany.mavenproject2;

import java.util.Scanner;

public class Project_Main {

    public static void main(String[] args) {

         
System.out.println("Q1 - Canonical LR(1) Data:\n"
                + "1) Enter number and name for terminals and non-terminals.\n,then enter number of states"
                + "2) For each state, enter the items in this form:\n"
                + "   Example: S' -> . S , $\n"
                + "3) Enter the ACTION table row by row,\n"
                + "   use sX for shift, rY for reduce, acc for accept, and - for empty.\n"
                + "4) Enter the GOTO table row by row,\n"
                + "   use -1 when there is no transition.\n");

        Scanner sc = new Scanner(System.in);

        Q1 q1 = new Q1();
        q1.sc = sc;   

        Q2 p = new Q2(q1);
        p.sc = sc;   

        q1.q2 = p;

        
        System.out.println("Q1: Enter LR(1) items, ACTION, and GOTO");
        q1.readInput();
        q1.makeCore();
        q1.mergeStates();
        q1.makeLALR();
        q1.printLALR();

       
        sc.nextLine();

        
        if (q1.shiftReduceConflict || q1.reduceReduceConflict) {
            System.out.println("\nCannot continue because grammar is NOT LALR.");   
            return;
        }

     
        System.out.println("\nQ2 - Parsing Data:\n"
                + "3) Enter number of productions, then each rule as:\n"
                + "   A n symbols  (Example:  S 3 a B c)\n"
                + "   Use 0 when the right-hand side is epsilon (Example: S 0).\n"
                + "4) Finally, enter the input string tokens separated by spaces.\n"
                + "   Example:  id + id  OR  a a b b");

        System.out.println("Enter productions:");
        p.readP();

        System.out.println("\nEnter input string to parse:");
        String s = sc.nextLine();

        p.LR_Parse(s);
    }
}
