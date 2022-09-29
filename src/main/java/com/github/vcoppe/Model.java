package com.github.vcoppe;

import gurobi.*;

public class Model {

    private GRBEnv env;
    private GRBModel model;
    private GRBVar[][][] z;

    public Model(int n, int[] l, int[][] c) throws GRBException {
        env = new GRBEnv("srflp.log");
        model = new GRBModel(env);

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
            for (int j=i+1; j<n; j++) {
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
    }

    public void solve(double timeLimit, int threads) throws GRBException {
        model.set(GRB.IntParam.OutputFlag, 0);
        model.set(GRB.DoubleParam.FeasibilityTol, 1e-9);
        model.set(GRB.DoubleParam.IntFeasTol, 1e-9);
        model.set(GRB.DoubleParam.OptimalityTol, 1e-9);
        model.set(GRB.DoubleParam.TimeLimit, timeLimit);
        if (threads > 0) model.set(GRB.IntParam.Threads, threads);
        model.optimize();
    }

    public double gap() throws GRBException {
        return model.get(GRB.DoubleAttr.MIPGap);
    }

    public double runTime() throws GRBException {
        return model.get(GRB.DoubleAttr.Runtime);
    }

    public double objVal() throws GRBException {
        return model.get(GRB.DoubleAttr.ObjVal);
    }

    public double lowerBound() throws GRBException {
        return model.get(GRB.DoubleAttr.ObjBound);
    }

    public boolean hasProved() throws GRBException {
        return model.get(GRB.IntAttr.Status) == GRB.OPTIMAL;
    }

    public void dispose() throws GRBException {
        model.dispose();
        env.dispose();
    }
}
