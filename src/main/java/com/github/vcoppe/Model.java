package com.github.vcoppe;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;

public abstract class Model {

    private final GRBEnv env;
    protected GRBModel model;

    public Model() throws GRBException {
        env = new GRBEnv("srflp.log");
        model = new GRBModel(env);
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
