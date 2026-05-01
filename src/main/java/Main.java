import com.govsim.govsim.simulation.SimuEngine;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {

        try {
            PrintStream fileOut = new PrintStream(new FileOutputStream("output.txt"));
            PrintStream console = System.out;

            System.setOut(new PrintStream(new java.io.OutputStream() {
                @Override
                public void write(int b) {
                    try {
                        console.write(b);
                        fileOut.write(b);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));

        } catch (Exception e) {
            e.printStackTrace();
        }

        SimuEngine engine = new SimuEngine();
        engine.start();
    }
}