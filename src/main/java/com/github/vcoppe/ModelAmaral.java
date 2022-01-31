package com.github.vcoppe;

import gurobi.*;

import java.util.ArrayList;

public class ModelAmaral extends Model {

    private final GRBVar[][][] z;

    private final int START, END;

    public ModelAmaral(int n, int[] lengths, int[][] costs, ArrayList<Pair> p, ArrayList<Pair> o, ArrayList<Pair> r) throws GRBException {
        super();

        // create 2 dummy variables for start and end departments
        START = n;
        END = n+1;

        int[] l = new int[n+2];
        int[][] c = new int[n+2][n+2];

        for (int i=0; i<n; i++) {
            l[i] = lengths[i];
            for (int j=0; j<n; j++) {
                c[i][j] = costs[i][j];
            }
        }
        n = n + 2;

        z = new GRBVar[n][n][n];

        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                if (i != j) model.addVar(1, 1, 0.5 * l[i] * c[i][j], GRB.BINARY, "K_"+i+"_"+j);
                for (int k=0; k<n; k++) {
                    if (i == j || i == k || j == k) {
                        z[i][j][k] = model.addVar(0, 0, 0, GRB.BINARY, "z_" + i + "_" + j + "_" + k);
                    } else if (i < j) {
                        z[i][j][k] = model.addVar(0, 1, c[i][j] * l[k], GRB.BINARY, "z_" + i + "_" + j + "_" + k);
                    } else {
                        z[i][j][k] = model.addVar(0, 1, 0, GRB.BINARY, "z_" + i + "_" + j + "_" + k);
                    }
                }
            }
        }

        GRBLinExpr expr;
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) if (i != j) {
                for (int k=0; k<n; k++) if (k != i && k != j) {
                    model.addConstr(z[i][j][k], GRB.EQUAL, z[j][i][k], "equal_"+i+"_"+j+"_"+k);

                    expr = new GRBLinExpr();
                    expr.addConstant(1);
                    expr.addTerm(-1, z[i][k][j]);
                    expr.addTerm(-1, z[j][k][i]);
                    model.addConstr(z[i][j][k], GRB.EQUAL, expr, "transform_equal_"+i+"_"+j+"_"+k);

                    expr = new GRBLinExpr();
                    expr.addTerm(1, z[i][j][k]);
                    expr.addTerm(1, z[i][k][j]);
                    expr.addTerm(1, z[j][k][i]);
                    model.addConstr(expr, GRB.EQUAL, 1, "exclusive_"+i+"_"+j+"_"+k);

                    for (int d=0; d<n; d++) if (d != i && d != j && d != k) {
                        expr = new GRBLinExpr();
                        expr.addTerm(1, z[i][j][d]);
                        expr.addTerm(1, z[j][k][d]);
                        expr.addTerm(-1, z[i][k][d]);
                        model.addConstr(expr, GRB.GREATER_EQUAL, 0, "forced_"+i+"_"+j+"_"+k+"_"+d);

                        expr = new GRBLinExpr();
                        expr.addTerm(1, z[i][j][d]);
                        expr.addTerm(1, z[j][k][d]);
                        expr.addTerm(1, z[i][k][d]);
                        model.addConstr(expr, GRB.LESS_EQUAL, 2, "exclusive_"+i+"_"+j+"_"+k+"_"+d);
                    }
                }
            }
        }

        // add constraints to start and end departments
        for (int i=0; i<START; i++) {
            for (int j=i+1; j<START; j++) if (i != j) {
                model.addConstr(z[i][j][START], GRB.EQUAL, 0, "start_not_between_"+i+"_"+j);
                model.addConstr(z[i][j][END], GRB.EQUAL, 0, "end_not_between_"+i+"_"+j);

                /*model.addConstr(z[START][i][j], GRB.EQUAL, z[j][END][i], "triangle_1_"+i+"_"+j);

                expr = new GRBLinExpr();
                expr.addTerm(1, z[START][i][j]);
                expr.addTerm(1, z[i][END][j]);
                model.addConstr(expr, GRB.EQUAL, 1, "triangle_2_"+i+"_"+j);*/
            }

            model.addConstr(z[START][END][i], GRB.EQUAL, 1, "between_start_end_"+i);
            model.addConstr(z[END][START][i], GRB.EQUAL, 1, "between_end_start_"+i);

            /*model.addConstr(z[START][i][END], GRB.EQUAL, 0, "end_not_between_start_"+i);
            model.addConstr(z[i][START][END], GRB.EQUAL, 0, "end_not_between_"+i+"_start");

            model.addConstr(z[END][i][START], GRB.EQUAL, 0, "start_not_between_end_"+i);
            model.addConstr(z[i][END][START], GRB.EQUAL, 0, "start_not_between_"+i+"_end");*/
        }

        // add positional constraints
        for (Pair pair : p) {
            int dep = pair.first;
            int pos = pair.second;

            expr = new GRBLinExpr();
            for (int k=0; k<START; k++) {
                expr.addTerm(1, z[START][dep][k]);
            }
            model.addConstr(expr, GRB.EQUAL, pos, "pos_start_"+dep);

            expr = new GRBLinExpr();
            for (int k=0; k<START; k++) {
                expr.addTerm(1, z[dep][END][k]);
            }
            model.addConstr(expr, GRB.EQUAL, (n-2)-pos-1, "pos_end_"+dep);
        }

        // add ordering constraints
        for (Pair pair : o) {
            int dep1 = pair.first;
            int dep2 = pair.second;

            model.addConstr(z[START][dep1][dep2], GRB.EQUAL, 0, "ord_start_a_"+dep1+"_"+dep2);
            model.addConstr(z[START][dep2][dep1], GRB.EQUAL, 1, "ord_start_b_"+dep1+"_"+dep2);
            model.addConstr(z[dep1][END][dep2], GRB.EQUAL, 1, "ord_end_a_"+dep1+"_"+dep2);
            model.addConstr(z[dep2][END][dep1], GRB.EQUAL, 0, "ord_end_b_"+dep1+"_"+dep2);
        }

        // add relation constraints
        for (Pair pair : r) {
            int dep1 = pair.first;
            int dep2 = pair.second;

            // same than ordering
            model.addConstr(z[START][dep1][dep2], GRB.EQUAL, 0, "rel_start_a_"+dep1+"_"+dep2);
            model.addConstr(z[START][dep2][dep1], GRB.EQUAL, 1, "rel_start_b_"+dep1+"_"+dep2);
            model.addConstr(z[dep1][END][dep2], GRB.EQUAL, 1, "rel_end_a_"+dep1+"_"+dep2);
            model.addConstr(z[dep2][END][dep1], GRB.EQUAL, 0, "rel_end_b_"+dep1+"_"+dep2);

            for (int k=0; k<START; k++) if (k != dep1 && k != dep2) {
                model.addConstr(z[dep1][dep2][k], GRB.EQUAL, 0, "rel_between_"+dep1+"_"+dep2);
            }
        }
    }
}
