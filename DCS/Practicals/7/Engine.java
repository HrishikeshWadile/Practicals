import java.util.*;

/**
 * State of the distributed computation and the Dijkstra-Scholten rules.
 *   parent[p]  - the process that engaged p (0 = not engaged; the root is always engaged)
 *   deficit[p] - WORK messages sent by p that are not acknowledged yet
 *
 *   1. p sends WORK            -> deficit[p]++
 *   2. WORK reaches a passive, not engaged process q from s -> parent[q] = s, no ACK yet
 *   3. WORK reaches an engaged process (or the root)        -> ACK goes back at once
 *   4. ACK reaches p           -> deficit[p]--
 *   5. non-root p passive and deficit[p] == 0 -> p sends the pending ACK to its parent and
 *      leaves the tree (parent[p] = 0)
 *   6. root passive and deficit == 0 -> TERMINATION is announced
 */
public class Engine {

    static final int ROOT = 1;

    int n;
    boolean[] active;
    int[] parent;
    int[] deficit;
    int[] workSent;
    int ackCount, workCount;
    boolean terminated;
    LinkedList<int[]> inTransit = new LinkedList<>();     // {from, to}

    Engine(int n) { reset(n); }

    void reset(int n) {
        this.n = n;
        active = new boolean[n + 1];
        parent = new int[n + 1];
        deficit = new int[n + 1];
        workSent = new int[n + 1];
        inTransit.clear();
        terminated = false;
        ackCount = 0;
        workCount = 0;
        active[ROOT] = true;                               // diffusing computation: only the root starts active
    }

    List<String> execute(String command) {
        List<String> out = new ArrayList<>();
        String[] p = command.trim().split("\\s+");
        String c = p[0].toUpperCase();
        try {
            switch (c) {
                case "INIT":
                    int count = Integer.parseInt(p[1]);
                    if (count < 2 || count > 9) throw new IllegalArgumentException("number of processes must be 2 to 9");
                    reset(count);
                    out.add("New computation with " + count + " processes. P1 is the root and is ACTIVE; the others are PASSIVE.");
                    break;
                case "SEND":    send(num(p, 1), num(p, 2), out); break;
                case "RECEIVE": receive(num(p, 1), out); break;
                case "FINISH":  finish(num(p, 1), out); break;
                case "STATUS":  status(out); break;
                case "DEMO":    demo(out); break;
                default:
                    out.add("ERROR: unknown command '" + p[0] + "'");
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            out.add("ERROR: " + (e instanceof ArrayIndexOutOfBoundsException ? "missing argument" : e.getMessage()));
        }
        return out;
    }

    int num(String[] p, int i) {
        int v = Integer.parseInt(p[i]);
        if (v < 1 || v > n) throw new IllegalArgumentException("process must be between 1 and " + n);
        return v;
    }

    void checkRunning() {
        if (terminated) throw new IllegalArgumentException("the computation has already terminated (use INIT to start a new one)");
    }

    void send(int a, int b, List<String> out) {
        checkRunning();
        if (a == b) throw new IllegalArgumentException("a process cannot send WORK to itself");
        if (!active[a]) throw new IllegalArgumentException("P" + a + " is PASSIVE - only an active process can send");
        deficit[a]++;
        workSent[a]++;
        workCount++;
        inTransit.add(new int[]{a, b});
        out.add("P" + a + " -> WORK -> P" + b + "   (in transit).  deficit(P" + a + ") = " + deficit[a]);
    }

    void receive(int b, List<String> out) {
        checkRunning();
        int[] message = null;
        for (int[] m : inTransit) if (m[1] == b) { message = m; break; }
        if (message == null) throw new IllegalArgumentException("no WORK message is waiting for P" + b);
        inTransit.remove(message);
        int a = message[0];

        out.add("P" + b + " receives WORK from P" + a + " and becomes ACTIVE.");
        boolean engaged = (b == ROOT) || parent[b] != 0;
        active[b] = true;
        if (engaged) {
            out.add("P" + b + " is already engaged, so it returns the ACK to P" + a + " immediately.");
            ack(a, b, out);
        } else {
            parent[b] = a;
            out.add("P" + b + " was not engaged: it joins the tree with parent P" + a + " (ACK is kept pending).");
        }
    }

    void finish(int p, List<String> out) {
        checkRunning();
        if (!active[p]) throw new IllegalArgumentException("P" + p + " is already PASSIVE");
        active[p] = false;
        out.add("P" + p + " finishes its local work and becomes PASSIVE.");
        tryRelease(p, out);
    }

    /** Rules 5 and 6: a passive process with deficit 0 leaves the tree / the root announces termination. */
    void tryRelease(int p, List<String> out) {
        if (active[p] || deficit[p] != 0) {
            if (!active[p]) out.add("P" + p + " is passive but still waits for " + deficit[p] + " ACK(s) (deficit = " + deficit[p] + ") - stays in the tree.");
            return;
        }
        if (p == ROOT) {
            terminated = true;
            out.add("*** ROOT P1 is passive and its deficit is 0  ->  TERMINATION DETECTED ***");
            out.add("Total WORK messages: " + workCount + ", ACK messages: " + ackCount + " (every WORK is acknowledged exactly once).");
        } else if (parent[p] != 0) {
            int parentProcess = parent[p];
            parent[p] = 0;
            out.add("P" + p + " is passive with deficit 0: sends the pending ACK to its parent P" + parentProcess + " and leaves the tree.");
            ack(parentProcess, p, out);
        }
    }

    void ack(int to, int from, List<String> out) {
        ackCount++;
        deficit[to]--;
        out.add("P" + from + " -> ACK -> P" + to + ".  deficit(P" + to + ") = " + deficit[to]);
        tryRelease(to, out);
    }

    void status(List<String> out) {
        out.add("+---------+---------+----------+--------+---------+");
        out.add("| Process | State   | Engaged  | Parent | Deficit |");
        out.add("+---------+---------+----------+--------+---------+");
        for (int i = 1; i <= n; i++) {
            boolean engaged = (i == ROOT) || parent[i] != 0;
            out.add(String.format("| P%-6d | %-7s | %-8s | %-6s | %-7d |", i, active[i] ? "ACTIVE" : "PASSIVE",
                    engaged ? "yes" : "no", i == ROOT ? "root" : (parent[i] == 0 ? "-" : "P" + parent[i]), deficit[i]));
        }
        out.add("+---------+---------+----------+--------+---------+");

        StringBuilder transit = new StringBuilder();
        for (int[] m : inTransit) transit.append("P").append(m[0]).append("->P").append(m[1]).append("  ");
        out.add("WORK messages in transit : " + (inTransit.isEmpty() ? "none" : transit.toString().trim()));

        boolean allPassive = true;
        for (int i = 1; i <= n; i++) if (active[i]) allPassive = false;
        out.add("All processes passive?  : " + (allPassive ? "yes" : "no")
                + (allPassive && (!inTransit.isEmpty() || deficit[ROOT] > 0) ? "   <- but the computation has NOT terminated (work still in transit)" : ""));
        out.add("Engagement tree         : " + tree(ROOT));
        out.add("State                   : " + (terminated ? "TERMINATED" : "RUNNING"));
    }

    String tree(int p) {
        StringBuilder sb = new StringBuilder("P" + p);
        List<String> children = new ArrayList<>();
        for (int i = 1; i <= n; i++) if (i != ROOT && parent[i] == p) children.add(tree(i));
        if (!children.isEmpty()) sb.append("(").append(String.join(", ", children)).append(")");
        return sb.toString();
    }

    void demo(List<String> out) {
        reset(4);
        String[] steps = {"SEND 1 2", "SEND 1 3", "RECEIVE 2", "RECEIVE 3", "FINISH 1", "STATUS", "SEND 2 4", "RECEIVE 4",
                "FINISH 3", "SEND 2 3", "RECEIVE 3", "FINISH 4", "FINISH 3", "STATUS", "FINISH 2", "STATUS"};
        out.add("DEMO: 4 processes, P1 is the root.");
        for (String step : steps) {
            out.add("");
            out.add(">>> " + step);
            out.addAll(execute(step));
        }
    }
}
