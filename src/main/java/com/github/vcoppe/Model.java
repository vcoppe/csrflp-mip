package com.github.vcoppe;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;

public abstract class Model {

    private GRBEnv env;
    protected GRBModel model;

    public Model() throws GRBException {
        env = new GRBEnv("srflp.log");
        model = new GRBModel(env);
    }

    public void solve(double timeLimit, int threads) throws GRBException {
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

    public void dispose() throws GRBException {
        model.dispose();
        env.dispose();
    }
}
