import java.util.*;

/** The two election algorithms. Each one writes its message trace and returns the winner. */
public class Election {

    // ------------------------------------------------------------------ BULLY
    static int bully(int n, boolean[] active, int initiator, List<String> trace, int[] messages) {
        int winner = bullyElection(n, active, initiator, trace, messages, new HashSet<>());
        trace.add("Process " + winner + " becomes the new COORDINATOR.");
        for (int i = 1; i <= n; i++) {
            if (i != winner && active[i]) {
                trace.add("Process " + winner + " -> COORDINATOR -> Process " + i);
                messages[0]++;
            }
        }
        return winner;
    }

    private static int bullyElection(int n, boolean[] active, int p, List<String> trace,
                                     int[] messages, Set<Integer> started) {
        started.add(p);
        trace.add("Process " + p + " starts an election.");
        List<Integer> answered = new ArrayList<>();

        for (int j = p + 1; j <= n; j++) {
            trace.add("Process " + p + " -> ELECTION -> Process " + j);
            messages[0]++;
            if (active[j]) {
                trace.add("Process " + j + " -> OK -> Process " + p + "      (higher process is alive, " + p + " steps back)");
                messages[0]++;
                answered.add(j);
            } else {
                trace.add("Process " + j + " does not answer (failed) - timeout");
            }
        }

        if (answered.isEmpty()) {
            trace.add("No higher process answered, so Process " + p + " wins the election.");
            return p;
        }
        int winner = p;
        for (int j : answered) {
            if (!started.contains(j)) {
                winner = Math.max(winner, bullyElection(n, active, j, trace, messages, started));
            }
        }
        return winner;
    }

    // ------------------------------------------------------------------- RING
    static int ring(int n, boolean[] active, int initiator, List<String> trace, int[] messages) {
        List<Integer> collected = new ArrayList<>();
        collected.add(initiator);
        trace.add("Process " + initiator + " starts an election, ELECTION" + collected);

        int current = initiator;
        while (true) {
            int next = current;
            do {
                next = next % n + 1;
                if (next != initiator && !active[next]) {
                    trace.add("Process " + next + " is down - skipped");
                }
            } while (next != initiator && !active[next]);

            trace.add("Process " + current + " -> ELECTION" + collected + " -> Process " + next);
            messages[0]++;
            if (next == initiator) break;
            collected.add(next);
            current = next;
        }

        trace.add("The message is back at Process " + initiator + ". Collected ids: " + collected);
        int winner = Collections.max(collected);
        trace.add("Highest id in the list = " + winner + ", so Process " + winner + " becomes the new COORDINATOR.");

        trace.add("Process " + initiator + " circulates COORDINATOR(" + winner + ") around the ring:");
        current = initiator;
        for (int i = 1; i < collected.size(); i++) {
            int next = collected.get(i);
            trace.add("Process " + current + " -> COORDINATOR(" + winner + ") -> Process " + next);
            messages[0]++;
            current = next;
        }
        trace.add("Process " + current + " -> COORDINATOR(" + winner + ") -> Process " + initiator + "   (back at the start, done)");
        messages[0]++;
        return winner;
    }
}
