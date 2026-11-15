# DCS practicals - how to run

Every practical works the same way:

1. Open **two** terminals in the practical's folder.
2. Terminal 1 - compile once (`javac *.java`) and start the **server**.
3. Terminal 2 - start the **client**, type in the client window.
4. Look at the **server** window: it shows everything the client sent and what the server did.

Start the server first, always. Press `Ctrl+C` in the server window when finished.
Needs only a JDK (17 or 21 recommended); no other libraries for practicals 1, 2, 4-8.

| # | Title | Server | Client | Port | Type this in the client |
|---|-------|--------|--------|------|-------------------------|
| 1 | Echo Server (concurrent) | `java EchoServer` | `java EchoClient` | 5000 | any text, `exit` to quit. Start 2-3 clients to see concurrency |
| 2 | Multi-threaded client/server using RMI | `java RMIServer` | `java RMIClient` | 1099 | menu 5 = all operations, menu 6 = 5 concurrent calls |
| 3 | CORBA (Java IDL, Maven project) | see `3/README.md` | see `3/README.md` | - | number of students, then roll no / name / branch |
| 4 | Berkeley clock synchronisation | `java BerkeleyServer` | `java BerkeleyClient` | 6000 | server: master clock + slave count; client: slave count + each clock |
| 5 | Leader election - Bully and Ring | `java ElectionServer` | `java ElectionClient` | 7000 | algorithm, number of processes, failed process(es), detecting process |
| 6 | Word count using MapReduce | `java MapReduceServer` | `java MapReduceClient` | 8000 | menu 2 = sample documents, menu 1 = your own lines |
| 7 | Termination detection (Dijkstra-Scholten) | `java TerminationServer` | `java TerminationClient` | 9000 | `DEMO`, or `SEND 1 2`, `RECEIVE 2`, `FINISH 1`, `STATUS` ... |
| 8 | Name Server | `java NameServer` | `java Client` | 5000 | menu 1 = lookup `google`, menu 2 = register a name |
| 9 | Publish/Subscribe with JMS (Maven project) | `MessageBroker` | `Publisher`, `Subscriber` | 61616 | unchanged - see `9/pubsubpractical/README.md` |

Practicals 1 and 8 both use port 5000, so do not run them at the same time.
On another machine pass the server's address: `java EchoClient 192.168.1.10`.

## Quick examples

**Berkeley** - server: master clock `30`, `4` slaves. Client: `4` slaves, clocks `12`, `4`, `72`, `3`.
Result: every clock becomes `24.20` (average of the five clocks' differences added to the master).

**Bully / Ring** - client: `5` processes, failed `5`, detecting process `2` -> Process 4 becomes coordinator.
Failed list may hold several processes, e.g. `4,5`.

**Termination detection** - type `DEMO` for a complete worked example with WORK/ACK messages,
the engagement tree and the final "TERMINATION DETECTED" message.
