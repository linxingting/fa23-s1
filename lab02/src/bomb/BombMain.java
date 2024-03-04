package bomb;

import common.IntList;

public class BombMain {
    public static void main(String[] args) {
        int phase = 2;
        if (args.length > 0) {
            phase = Integer.parseInt(args[0]);
        }
        // TODO: Find the correct inputs (passwords) to each phase using debugging techniques
        Bomb b = new Bomb();
        if (phase >= 0) {
            b.phase0("39291226");
        }
        if (phase >= 1) {
            b.phase1(IntList.of(0, 9, 3, 0, 8)); // Figure this out too
        }
        if (phase >= 2) {
            String x = "0";
            for (int i = 1; i < 1337; i++){
                x = x + " 0";
            }
            x = x + " -81201430";
            b.phase2(x);
        }
    }
}
