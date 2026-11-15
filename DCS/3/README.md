# CORBA Student Client-Server Practical

This project implements a student information service using CORBA. Maven
generates the Java classes from `src/main/idl/Student.idl` and compiles them
together with the server and client.

## Prerequisites

- **JDK 17 or JDK 21** (JDK 8 is not needed)
- Maven 3.6 or newer

Check with:

```powershell
java -version
mvn -version      # the "Java version" line must show 17 or 21
```

`JAVA_HOME` must point to the same JDK 17/21 that you want Maven to use.

### Why the `--add-opens` flags?

CORBA was removed from the JDK in Java 11, so the project uses the GlassFish
CORBA ORB from Maven Central. On Java 17+ that ORB needs access to some
`java.base` internals. The `server` and `client` executions in `pom.xml` start a
separate JVM with the required `--add-opens` flags, so nothing has to be set
manually.

## Compile

Open PowerShell in this project directory:

```powershell
cd "D:\Study\College\B.Tech\DCS\Practicals\3"
mvn clean compile
```

(`.\run-corba.ps1 clean compile` does the same and also checks that Java 17+ is
being used.)

## Run

Start the server in one PowerShell terminal:

```powershell
cd "D:\Study\College\B.Tech\DCS\Practicals\3"
mvn compile exec:exec@server
```

Wait for `Waiting for clients...`. The server writes its CORBA object reference
to `student.ref` in the project directory.

In a second PowerShell terminal, run the client:

```powershell
cd "D:\Study\College\B.Tech\DCS\Practicals\3"
mvn compile exec:exec@client
```

The client asks how many students you want to enter, takes the roll number, name
and branch of each one, sends them all to the server and then asks the server to
print them. **Type in the client window and watch the server window** - the
student table appears there. Keep the server running while using the client;
press `Ctrl+C` in the server terminal when finished.

> Use `exec:exec@server` / `exec:exec@client`. The old
> `exec:java -Dexec.mainClass=...` command runs inside Maven's own JVM and
> will not get the `--add-opens` flags.
