package com.github.vcoppe;

import gurobi.GRBException;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static int n = -1;
    private static int[] l;
    private static int[][] c;
    private static final ArrayList<Pair> p = new ArrayList<>(), o = new ArrayList<>(), r = new ArrayList<>();
    private static boolean modelAmaral = true;

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

            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to read input file");
            System.exit(0);
        }
    }

    private static void readConstraints(String path) {
        try {
            Scanner scan = new Scanner(new File(path));

            int np = scan.nextInt();
            int no = scan.nextInt();
            int nr = scan.nextInt();

            int a, b;
            for (int i=0; i<np; i++) {
                a = scan.nextInt();
                b = scan.nextInt();
                p.add(new Pair(a, b));
            }

            for (int i=0; i<no; i++) {
                a = scan.nextInt();
                b = scan.nextInt();
                o.add(new Pair(a, b));
            }

            for (int i=0; i<nr; i++) {
                a = scan.nextInt();
                b = scan.nextInt();
                r.add(new Pair(a, b));
            }

            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to read constraints file");
            System.exit(0);
        }
    }

    public static void main(String[] args) throws GRBException {
        if (args.length < 1) {
            System.out.println("Arguments needed :\n\tfilename\n\t[--time timeLimit]\n\t[--threads threads]\n\t[--constraints filename]\n\t[--liu]");
            return;
        }

        read(args[0]);

        int timeLimit = Integer.MAX_VALUE, threads = 0;
        for (int i=1; i<args.length; i++) {
            if (args[i].equals("--time")) timeLimit = Integer.parseInt(args[i+1]);
            if (args[i].equals("--threads")) threads = Integer.parseInt(args[i+1]);
            if (args[i].equals("--constraints")) readConstraints(args[i+1]);
            if (args[i].equals("--liu")) modelAmaral = false;
        }

        Model mip = modelAmaral ? new ModelAmaral(n, l, c, p, o, r) : new ModelLiu(n, l, c, p, o, r);

        mip.solve(timeLimit, threads);

        Locale.setDefault(Locale.US);

        int objVal = (int) Math.round(mip.objVal());
        int bestBound = (int) Math.round(mip.lowerBound());
        System.out.printf("solver     : %s\n", "mip_" + (modelAmaral ? "amaral" : "liu"));
        System.out.printf("threads    : %d\n", threads);
        System.out.println("width      : 0");
        System.out.println("caching    : false");
        System.out.println("dominance  : false");
        System.out.println("cmpr. bound: false");
        System.out.println("cmpr. heu. : false");
        System.out.println("cmpr. width: 0");
        System.out.println("nb clusters: 0");
        System.out.printf("is exact   : %b\n", mip.hasProved());
        System.out.printf("duration   : %f seconds\n", mip.runTime());
        System.out.printf("best value : %d\n", objVal);
        System.out.printf("best bound : %d\n", bestBound);
        System.out.println("expl. b&b  : 0");
        System.out.println("expl. dd   : 0");
        System.out.println("peak mem.  : 0");

        mip.dispose();
    }

}
