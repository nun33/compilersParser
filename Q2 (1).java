import java.util.ArrayList;
import java.util.Scanner;

public class Q2 {

    Q1 q1;
    Scanner sc = new Scanner(System.in);

    int numP;
    String[] leftP;
    int[] lenP;
    String[][] rightP;

    public Q2(Q1 x) {
        q1 = x;
    }

    public void readP() {

        System.out.print("\nEnter number of productions: ");
        numP = sc.nextInt();
        sc.nextLine();

        leftP = new String[numP];
        lenP = new int[numP];
        rightP = new String[numP][];

        System.out.println("Enter productions in the form: A n symbols");

        for (int i = 0; i < numP; i++) {

            System.out.print("Production " + (i + 1) + ": ");
            String line = sc.nextLine().trim();

            String[] p = line.split("\\s+");

            leftP[i] = p[0];
            lenP[i] = Integer.parseInt(p[1]);

            rightP[i] = new String[lenP[i]];

            for (int j = 0; j < lenP[i]; j++) {
                rightP[i][j] = p[2 + j];
            }
        }
    }

    private int getT(String a) {
        for (int i = 0; i < q1.numTerminal; i++) {
            if (q1.terminalNames[i].equals(a))
                return i;
        }
        return -1;
    }

    private int getNT(String A) {
        for (int i = 0; i < q1.numNT; i++) {
            if (q1.nonTerminalNames[i].equals(A))
                return i;
        }
        return -1;
    }

    private void printS(ArrayList<Integer> st, ArrayList<String> IL, String act) {

        StringBuilder stackStr = new StringBuilder();
        for (int i : st)
            stackStr.append(i).append(" ");

        StringBuilder inputStr = new StringBuilder();
        for (String s : IL)
            inputStr.append(s);

        String out = "-";

        if (act.startsWith("reduce")) {
            int r = Integer.parseInt(act.replace("reduce", "").trim()) - 1;

            StringBuilder g = new StringBuilder();
            g.append(leftP[r]).append(" -> ");

            for (String s : rightP[r])
                g.append(s).append(" ");

            out = g.toString().trim();
        }

        System.out.printf("%-15s %-15s %-15s %-25s\n",
                stackStr, inputStr, act, out);
    }

    public void LR_Parse(String IS) {

        ArrayList<String> IL = new ArrayList<>();
        String[] p = IS.trim().split("\\s+");

        for (String x : p)
            IL.add(x);
        IL.add("$");

        ArrayList<Integer> st = new ArrayList<>();

        int start = q1.group[0] - 1;
        if (start < 0)
            start = 0;
        st.add(start);

        System.out.printf("\n%-15s %-15s %-15s %-15s\n",
                "Stack", "Input", "Action", "Output");

        while (true) {

            int state = st.get(st.size() - 1);
            String a = IL.get(0);

            int tIndex = getT(a);
            if (tIndex == -1) {
                printS(st, IL, "ERROR");
                System.out.println("NOT ACCEPTED");
                return;
            }

            String action = q1.LALR_ACTION[state][tIndex];

            if (action == null) {
                printS(st, IL, "ERROR");
                System.out.println("The String is NOT ACCEPTED");
                return;
            }

            if (action.startsWith("s")) {

                int next = Integer.parseInt(action.substring(1));

                // ✅ تحويله إلى اندكس LALR الحقيقي
                if (next >= q1.group.length) {
                    printS(st, IL, "ERROR");
                    System.out.println("The String is NOT ACCEPTED");
                    return;
                }

                next = q1.group[next] - 1; // ✅ هذا هو السطر الذهبي

                printS(st, IL, "shift " + next);
                st.add(next);
                IL.remove(0);
            }

            else if (action.startsWith("r")) {

                if (action.contains("/")) {
                    System.out.println("Reduce/Reduce Conflict: " + action + " -> choosing first reduce");
                    action = action.split("/")[0];
                }

                int r = Integer.parseInt(action.substring(1)) - 1;

                String A = leftP[r];
                int len = lenP[r];

                printS(st, IL, "reduce " + (r + 1));

                for (int i = 0; i < len; i++)
                    st.remove(st.size() - 1);

                int top = st.get(st.size() - 1);
                int ntIndex = getNT(A);

                int go = q1.LALR_GOTO[top][ntIndex];

                if (go == -1) {
                    printS(st, IL, "ERROR");
                    System.out.println("The String is NOT ACCEPTED");
                    return;
                }

                st.add(go);
            }

            else if (action.equals("acc")) {
                printS(st, IL, "ACCEPT");
                System.out.println("\nThe String is ACCEPTED");
                return;
            }
        }
    }
}
