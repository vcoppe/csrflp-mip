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

## Data

The `data` folder contains benchmark instances for the Single-Row Facility Layout Problem (SRFLP), the Single Row Equidistant Facility Layout Problem (SREFLP) and the Minimum Linear Arrangement Problem (MinLA). The latter ones are special cases of the SRFLP. It also contains constraint files to transform any instance in an instance of the cSRFLP.

The file format for the SRFLP, SREFLP and MinLA instances is the following:
```
n
l_1  l_2  ... l_n
c_11 c_12 ... c_1n
c_21 c_22 ... c_2n
...  ...  ... ...
c_n1 c_n2 ... c_nn
```
Values can also be separated with commas.
The file format for the constraints is:

```
p o r
a_1 b_1 // position(a_1) = b_1
a_2 b_2
...
a_p b_p
c_1 d_1 // c_1 \in predecessors(d_1)
c_2 d_2
...
c_o d_o
e_1 f_1 // e_1 = previous(f_1)
e_2 f_2
...
e_r f_r
```
where `p`, `o` and `r` respectively denote the number of *positioning*, *ordering* and *relation* constraints.
The following lines contain unique constraints on departments and positions, both 0-indexed.
