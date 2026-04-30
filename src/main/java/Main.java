import com.govsim.govsim.simulation.SimuEngine;

public class Main {
    public static void main(String[] args) {

        SimuEngine engine = new SimuEngine(1000000);

        // Run  months to test monthly income
        engine.runMonth();

    }
}