import CalculatorModule.Calculator;
import CalculatorModule.CalculatorHelper;
import CalculatorModule.CalculatorOperations;
import CalculatorModule.DivideByZero;
import CalculatorModule.DivideByZeroHelper;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

public class CalculatorServer {

    // Implementation of the remote Calculator interface
    static class CalculatorImpl extends Servant
            implements CalculatorOperations,
            org.omg.CORBA.portable.InvokeHandler {

        @Override
        public double add(double a, double b) {
            System.out.println("add(" + a + ", " + b + ") called");
            return a + b;
        }

        @Override
        public double subtract(double a, double b) {
            System.out.println("subtract(" + a + ", " + b + ") called");
            return a - b;
        }

        @Override
        public double multiply(double a, double b) {
            System.out.println("multiply(" + a + ", " + b + ") called");
            return a * b;
        }

        @Override
        public double divide(double a, double b) throws DivideByZero {
            System.out.println("divide(" + a + ", " + b + ") called");
            if (b == 0) {
                throw new DivideByZero("Cannot divide by zero");
            }
            return a / b;
        }

        // Dispatches an incoming request to the right method
        @Override
        public OutputStream _invoke(
                String method,
                InputStream input,
                org.omg.CORBA.portable.ResponseHandler handler) {

            OutputStream out;

            switch (method) {

                case "add": {
                    double a = input.read_double();
                    double b = input.read_double();
                    out = handler.createReply();
                    out.write_double(add(a, b));
                    return out;
                }

                case "subtract": {
                    double a = input.read_double();
                    double b = input.read_double();
                    out = handler.createReply();
                    out.write_double(subtract(a, b));
                    return out;
                }

                case "multiply": {
                    double a = input.read_double();
                    double b = input.read_double();
                    out = handler.createReply();
                    out.write_double(multiply(a, b));
                    return out;
                }

                case "divide": {
                    double a = input.read_double();
                    double b = input.read_double();
                    try {
                        double result = divide(a, b);
                        out = handler.createReply();
                        out.write_double(result);
                    } catch (DivideByZero ex) {
                        // Send the user exception back to the client
                        out = handler.createExceptionReply();
                        DivideByZeroHelper.write(out, ex);
                    }
                    return out;
                }

                default:
                    throw new org.omg.CORBA.BAD_OPERATION(
                            0,
                            org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
            }
        }

        @Override
        public String[] _all_interfaces(POA poa, byte[] objectId) {
            return new String[] {
                    "IDL:CalculatorModule/Calculator:1.0"
            };
        }
    }

    public static void main(String[] args) {

        try {

            

            // Create ORB
            ORB orb = ORB.init(args, null);

            // Get Root POA and activate it
            POA rootPOA = POAHelper.narrow(
                    orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // Create implementation and turn it into a CORBA object
            CalculatorImpl calculatorImpl = new CalculatorImpl();
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(calculatorImpl);
            Calculator calculatorRef = CalculatorHelper.narrow(ref);

            // Store the stringified object reference (IOR) in a file
            String objectReference = orb.object_to_string(calculatorRef);
            java.nio.file.Files.write(
                    java.nio.file.Paths.get("calculator.ref"),
                    objectReference.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            System.out.println("CORBA Server started...");
            System.out.println("Calculator service is ready.");
            System.out.println("Waiting for clients...");

            // Keep server running
            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
