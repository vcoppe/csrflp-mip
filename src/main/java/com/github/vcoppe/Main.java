package com.github.vcoppe;

import gurobi.GRBException;

import java.io.File;
import java.util.ArrayList;
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

        if (args[0].contains("Cl")) {
            for (int i=0; i<n; i++) {
                l[i] += 10;
            }
        }

        Model mip = modelAmaral ? new ModelAmaral(n, l, c, p, o, r) : new ModelLiu(n, l, c, p, o, r);

        System.out.println("MIP model created");
        System.out.println("Solving...");

        mip.solve(timeLimit, threads);

        System.out.println("runTime          : " + mip.runTime());
        System.out.println("objValue         : " + mip.objVal());
        System.out.println("gap              : " + mip.gap());

        mip.dispose();
    }

}
