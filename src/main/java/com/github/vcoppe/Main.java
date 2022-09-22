package com.github.vcoppe;

import gurobi.GRBException;

import java.io.File;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static int n = -1;
    private static int[] l;
    private static int[][] c;

    private static void read(String path) {
        try {
            Scanner scan = new Scanner(new File(path));

            int count = 0;
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("c")) continue;

                String[] tokens = line.split("[\\s,]+");
                if (n != -1 && tokens.length != n) continue;

                if (n == -1) {
                    n = Integer.parseInt(tokens[0]);
                    l = new int[n];
                    c = new int[n][n];
                } else if (count < n) {
                    for (; count < n; count++) {
                        l[count] = Integer.parseInt(tokens[count]);
                    }
                } else {
                    int start = count;
                    for (; count < start + n; count++) {
                        c[count/n - 1][count % n] = Integer.parseInt(tokens[count % n]);
                    }
                }
            }

            for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) {
                if (c[i][j] != c[j][i]) {
                    c[i][j] = c[j][i] = c[i][j] + c[j][i];
                }
            }

            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to read input file");
            System.exit(0);
        }
    }

    public static void main(String[] args) throws GRBException {
        if (args.length < 1) {
            System.out.println("Arguments needed :\n\tfilename\n\t[timeLimit]\n\t[threads]");
            return;
        }

        read(args[0]);

        int timeLimit = Integer.MAX_VALUE, threads = 0;
        if (args.length >= 2) timeLimit = Integer.parseInt(args[1]);
        if (args.length == 3) threads = Integer.parseInt(args[2]);

        double K = 0;
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            K += 0.5 * l[i] * c[i][j];
        }

        Model mip = new Model(n, l, c);

        mip.solve(timeLimit, threads);

        String[] split = args[0].split("/");
        String instance = split[split.length - 1];

        Locale.setDefault(Locale.US);

        System.out.printf("%s | mip | %s | %.2f | 0 | %d | %d | %d | %.4f\n",
                instance,
                mip.gap() == 0 ? "Proved" : "Timeout",
                mip.runTime(),
                (int) - (mip.objVal() - K),
                (int) - (mip.objVal() - K),
                (int) - (mip.lowerBound() - K),
                mip.gap()
        );

        mip.dispose();
    }

}
