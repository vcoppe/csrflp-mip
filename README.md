# csrflp-mip

Implementation of two MIP models for the Constrained Single-Row Facility Layout Problem (cSRFLP).
Constraints of the cSRFLP were modeled on top of the second model which was initially designed for the Single-Row Facility Layout Problem.

> Liu, Silu, et al. "An improved fireworks algorithm for the constrained single-row facility layout problem." International Journal of Production Research 59.8 (2021): 2309-2327.

> Amaral, André R.S. "A new lower bound for the single row facility layout problem." Discrete Applied Mathematics 157.1 (2009): 183-190.

## Usage

```
Arguments needed :
    <FILENAME>                        // instance path
    [--constraints <FILENAME>]        // constraints path
    [--time <TIME_LIMIT>]             // maximum time allowed
    [--threads <NUMBER_OF_THREADS>]   // maximum number of threads used
    [--liu]                           // switch to model from Liu (default is Amaral)
```


## Dependencies

Currently works with [Gurobi](https://www.gurobi.com/) 9.1.2, all you need is to import the `gurobi.jar` file in your [IntelliJ](https://www.jetbrains.com/fr-fr/idea/) project and grab an Academic License for the solver.
