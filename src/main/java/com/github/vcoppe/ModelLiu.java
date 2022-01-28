package com.github.vcoppe;

import gurobi.*;

import java.util.ArrayList;

public class ModelLiu extends Model {

    private GRBVar[] x;
    private GRBVar[][] d, y, a;

    public ModelLiu(int n, int[] l, int[][] c, ArrayList<Pair> p, ArrayList<Pair> o, ArrayList<Pair> r) throws GRBException {
        super();

        int L = 0;
        for (int i=0; i<n; i++) L += l[i];

        x = new GRBVar[n];
        d = new GRBVar[n][n];
        y = new GRBVar[n][n];
        a = new GRBVar[n][n];

        for (int i=0; i<n; i++) {
            x[i] = model.addVar(0.5 * l[i], L - 0.5 * l[i], 0, GRB.CONTINUOUS, "x_"+i);
            for (int j=0; j<n; j++) {
                if (i < j) {
                    d[i][j] = model.addVar(0, L, c[i][j], GRB.CONTINUOUS, "d_" + i + "_" + j);
                } else {
                    d[i][j] = model.addVar(0, L, 0, GRB.CONTINUOUS, "d_" + i + "_" + j);
                }
                y[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "y_"+i+"_"+j);
                a[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "a_"+i+"_"+j);
            }
        }

        GRBLinExpr expr1, expr2;
        for (int i=0; i<n; i++) {
            expr1 = new GRBLinExpr();
            expr2 = new GRBLinExpr();
            for (int j=0; j<n; j++) {
                expr1.addTerm(1, y[i][j]);
                expr2.addTerm(1, y[j][i]);
            }
            model.addConstr(expr1, GRB.EQUAL, 1, "mapping_a_"+i);
            model.addConstr(expr2, GRB.EQUAL, 1, "mapping_b_"+i);
        }

        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) if (i != j) {
                expr1 = new GRBLinExpr();
                expr2 = new GRBLinExpr();
                for (int k=0; k<n; k++) {
                    expr1.addTerm(k+1, y[i][k]);
                    expr2.addTerm(k+1, y[j][k]);
                }
                expr1.addTerm(n-1, a[i][j]);
                model.addConstr(expr1, GRB.GREATER_EQUAL, expr2, "a_constraint_"+i+"_"+j);
            }
        }

        for (int i=0; i<n; i++) {
            for (int j=i+1; j<n; j++) {
                expr1 = new GRBLinExpr();
                expr1.addTerm(1, a[i][j]);
                expr1.addTerm(1, a[j][i]);
                model.addConstr(expr1, GRB.EQUAL, 1, "a_exclusive_"+i+"_"+j);

                expr1 = new GRBLinExpr();
                expr1.addTerm(1, x[i]);
                expr1.addTerm(-1, x[j]);
                model.addConstr(d[i][j], GRB.GREATER_EQUAL, expr1, "d_constraint_"+i+"_"+j);

                expr1 = new GRBLinExpr();
                expr1.addTerm(1, x[j]);
                expr1.addTerm(-1, x[i]);
                model.addConstr(d[i][j], GRB.GREATER_EQUAL, expr1, "d_constraint_"+i+"_"+j);
            }
        }

        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) if (i != j) {
                expr1 = new GRBLinExpr();
                expr2 = new GRBLinExpr();
                expr1.addTerm(1, x[i]);
                expr1.addConstant(0.5 * (l[i] + l[j]));
                expr2.addTerm(1, x[j]);
                expr2.addConstant(L);
                expr2.addTerm(-L, a[i][j]);
                model.addConstr(expr1, GRB.LESS_EQUAL, expr2, "no_overlap_"+i+"_"+j);
            }
        }

        // positional constraints
        for (Pair pair : p) {
            int dep = pair.first;
            int pos = pair.second;

            model.addConstr(y[dep][pos], GRB.EQUAL, 1, "pos_"+dep);
        }

        // ordering constraints
        for (Pair pair : o) {
            int dep1 = pair.first;
            int dep2 = pair.second;

            model.addConstr(a[dep1][dep2], GRB.EQUAL, 1, "ord_a_"+dep1+"_"+dep2);

            expr1 = new GRBLinExpr();
            for (int k=0; k<n; k++) {
                expr1.addTerm(k+1, y[dep2][k]);
                expr1.addTerm(-(k+1), y[dep1][k]);
            }
            model.addConstr(expr1, GRB.GREATER_EQUAL, 1, "ord_b_"+dep1+"_"+dep2);
        }

        // relation constraints
        for (Pair pair : r) {
            int dep1 = pair.first;
            int dep2 = pair.second;

            expr1 = new GRBLinExpr();
            for (int k=0; k<n; k++) {
                expr1.addTerm(k+1, y[dep2][k]);
                expr1.addTerm(-(k+1), y[dep1][k]);
            }
            model.addConstr(expr1, GRB.EQUAL, 1, "rel_"+dep1+"_"+dep2);
        }
    }

}
