import StudentModule.Student;
import StudentModule.StudentHelper;
import StudentModule.StudentOperations;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

public class StudentServer {

    // Implementation of the remote Student interface
    static class StudentImpl extends Servant
            implements StudentOperations,
            org.omg.CORBA.portable.InvokeHandler {

        // rollNo -> {name, branch}, in the order received
        private final java.util.Map<Integer, String[]> students =
                new java.util.LinkedHashMap<>();

        @Override
        public synchronized String getStudentName(int rollNo) {
            String[] s = students.get(rollNo);
            return s == null ? "Student Not Found" : s[0];
        }

        @Override
        public synchronized String getBranch(int rollNo) {
            String[] s = students.get(rollNo);
            return s == null ? "Branch Not Found" : s[1];
        }

        @Override
        public synchronized void addStudent(int rollNo, String name, String branch) {
            students.put(rollNo, new String[] { name, branch });
        }

        @Override
        public synchronized void showAll() {
            System.out.println();
            System.out.println("=============== Student Details Received ===============");
            System.out.printf("%-6s | %-25s | %s%n", "Roll", "Name", "Branch");
            System.out.println("--------------------------------------------------------");
            for (java.util.Map.Entry<Integer, String[]> e : students.entrySet()) {
                System.out.printf("%-6d | %-25s | %s%n",
                        e.getKey(), e.getValue()[0], e.getValue()[1]);
            }
            System.out.println("--------------------------------------------------------");
            System.out.println("Total students: " + students.size());
            System.out.println();
        }

        @Override
        public OutputStream _invoke(
                String method,
                InputStream input,
                org.omg.CORBA.portable.ResponseHandler handler) {

            switch (method) {

                case "getStudentName": {

                    int rollNo = input.read_long();

                    String result = getStudentName(rollNo);

                    OutputStream out = handler.createReply();

                    out.write_string(result);

                    return out;
                }

                case "getBranch": {

                    int rollNo = input.read_long();

                    String result = getBranch(rollNo);

                    OutputStream out = handler.createReply();

                    out.write_string(result);

                    return out;
                }

                case "addStudent": {

                    int rollNo = input.read_long();
                    String name = input.read_string();
                    String branch = input.read_string();

                    addStudent(rollNo, name, branch);

                    return handler.createReply();
                }

                case "showAll": {

                    showAll();

                    return handler.createReply();
                }

                default:

                    throw new org.omg.CORBA.BAD_OPERATION(
                            0,
                            org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
            }
        }

        @Override
        public String[] _all_interfaces(
                POA poa,
                byte[] objectId) {

            return new String[] {
                    "IDL:StudentModule/Student:1.0"
            };
        }
    }

    public static void main(String[] args) {

        try {

            // Create ORB
            ORB orb = ORB.init(args, null);

            // Get Root POA
            POA rootPOA = POAHelper.narrow(
                    orb.resolve_initial_references("RootPOA"));

            // Activate POA
            rootPOA.the_POAManager().activate();

            // Create implementation
            StudentImpl studentImpl = new StudentImpl();

            // Convert servant into CORBA object
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(studentImpl);

            Student studentRef = StudentHelper.narrow(ref);

            // Store object reference
            String objectReference = orb.object_to_string(studentRef);

            java.nio.file.Files.write(
                    java.nio.file.Paths.get("student.ref"),
                    objectReference.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("CORBA Server started...");
            System.out.println("Student service is ready.");
            System.out.println("Waiting for clients...");

            // Keep server running
            orb.run();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}