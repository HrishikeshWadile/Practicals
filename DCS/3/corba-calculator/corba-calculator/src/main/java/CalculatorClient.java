import CalculatorModule.Calculator;
import CalculatorModule.CalculatorHelper;
import CalculatorModule.DivideByZero;

import org.omg.CORBA.ORB;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class CalculatorClient {

    private static int readInt(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a whole number.");
            }
        }
    }

    private static double readDouble(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }

    public static void main(String[] args) {

        try {

            ORB orb = ORB.init(args, null);

            // Read server object reference
            byte[] refBytes = Files.readAllBytes(java.nio.file.Paths.get("calculator.ref"));
            String objectReference = new String(refBytes, StandardCharsets.UTF_8).trim();

            // Convert string into CORBA object, then narrow it
            org.omg.CORBA.Object obj = orb.string_to_object(objectReference);
            Calculator calc = CalculatorHelper.narrow(obj);

            System.out.println("=================================");
            System.out.println("      CORBA Calculator Client");
            System.out.println("=================================");
            System.out.println("Connected to CORBA Server.");

            Scanner sc = new Scanner(System.in);

            while (true) {

                System.out.println();
                System.out.println("1. Addition");
                System.out.println("2. Subtraction");
                System.out.println("3. Multiplication");
                System.out.println("4. Division");
                System.out.println("5. Exit");

                int choice = readInt(sc, "Enter your choice: ");

                if (choice == 5) {
                    System.out.println("Exiting client.");
                    break;
                }
                if (choice < 1 || choice > 4) {
                    System.out.println("Invalid choice.");
                    continue;
                }

                double a = readDouble(sc, "Enter first number : ");
                double b = readDouble(sc, "Enter second number: ");

                try {
                    switch (choice) {
                        case 1:
                            System.out.println("Result: " + a + " + " + b + " = " + calc.add(a, b));
                            break;
                        case 2:
                            System.out.println("Result: " + a + " - " + b + " = " + calc.subtract(a, b));
                            break;
                        case 3:
                            System.out.println("Result: " + a + " * " + b + " = " + calc.multiply(a, b));
                            break;
                        case 4:
                            System.out.println("Result: " + a + " / " + b + " = " + calc.divide(a, b));
                            break;
                    }
                } catch (DivideByZero e) {
                    // User exception defined in the IDL
                    System.out.println("Server error: " + e.reason);
                }
            }

        } catch (Exception e) {
            System.out.println("Client Error:");
            e.printStackTrace();
        }
    }
}
