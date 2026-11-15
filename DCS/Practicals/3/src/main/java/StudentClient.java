import StudentModule.Student;
import StudentModule.StudentHelper;

import org.omg.CORBA.ORB;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class StudentClient {

    private static int readInt(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = in.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a whole number.");
            }
        }
    }

    private static String readText(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = in.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  This field cannot be empty.");
        }
    }

    public static void main(String[] args) {

        try {

            ORB orb = ORB.init(args, null);

            // Read server object reference
            String ref = new String(
                    Files.readAllBytes(Paths.get("student.ref")),
                    StandardCharsets.UTF_8);

            Student student = StudentHelper.narrow(orb.string_to_object(ref));

            Scanner in = new Scanner(System.in);

            System.out.println("=================================");
            System.out.println("       CORBA Student Client");
            System.out.println("=================================");
            System.out.println("Connected to CORBA Server.");
            System.out.println();

            int n = 0;
            while (n <= 0) {
                n = readInt(in, "How many students do you want to enter? ");
                if (n <= 0) {
                    System.out.println("  Enter a number greater than 0.");
                }
            }

            int[] rolls = new int[n];
            String[] names = new String[n];
            String[] branches = new String[n];

            // Step 1: take all details from the user
            for (int i = 0; i < n; i++) {
                System.out.println();
                System.out.println("--- Student " + (i + 1) + " of " + n + " ---");
                rolls[i] = readInt(in, "Roll Number : ");
                names[i] = readText(in, "Name        : ");
                branches[i] = readText(in, "Branch      : ");
            }

            // Step 2: once everything is entered, send it all to the server
            for (int i = 0; i < n; i++) {
                student.addStudent(rolls[i], names[i], branches[i]);
            }

            // Step 3: tell the server to print what it received
            student.showAll();

            System.out.println();
            System.out.println("All " + n + " student(s) sent. Check the server window.");

        } catch (Exception e) {

            System.out.println("Client Error:");
            e.printStackTrace();
        }
    }
}
