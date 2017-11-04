# Time

This framework uses Evolutionary algorithms (EAs) to optimize Izhikevich model parameters. Models with up to 4-compartments can be tuned to reproduce experimentally recorded spike times.

Required libraries:

1. ECJ (Luke S, et al. (2015). "Ecj: A java-based evolutionary computation research system."
Downloadable versions and documentation can be found at the following url: http://cs. gmu. edu/eclab/projects/ecj.)
2. Apache Commons Mathematics Library (http://commons.apache.org/proper/commons-math/)
3. CARLsim (Beyeler M, Carlson KD, Chou TS, Dutt N, Krichmar JL. (2015). 
A User-Friendly and Highly Optimized Library for the Creation of Neurobiologically Detailed Spiking Neural Networks. In International Joint Conference on Neural Networks.)
http://www.socsci.uci.edu/~jkrichma/CARLsim/
(#3 is required only for multi-compartment models)

The following steps assume you are familiar with the required libraries above.

- Constraints are specified in JSON format under input/ directory
- Specify the .json file path in "primary_input" file (comma separated values. See "primary_input")
- Specify the number of compartments (up to 4), and their layout following the file path in primary_input
e.g. 10_2_3,B4,3-000,1,0 will create a single compartment model using the constraints specified in 
input/10_2_3/B4/3-000.json. 
10_2_3,B4,3-000,4,0,0,0,2 will create a 4-compartment model using the same constraints and additional dendritic constraints.
{0,0,0,2} denotes the 4-compartment layout. index (0-based) represents a compartment, and the value represents the compartment which the index is connected to.
0 always represents soma (compartment-0). compartment-1 and compartment-2 (dendrites denoted by indices 1 and 2) are connected to soma. compartment-3 is connected to compartment-2
- The 2nd line in primary_input specifies which simulator to use. ("int" for Java-based ACM library, "ext" for CARLsim)
- Specify the ECJ parameters in .params file under input/ directory
- Once the modules are setup and input is specified, run src\ec\app\izhikevich\starter\ECJStarterV2.java (Or simply run startEA.sh)
- Outputs are generated under output/ directory
- Utility classes are available under src\ec\app\izhikevich\util package to read ECJ-generated output files
- .cpp files for simulating multi-compartment models using CARLsim are located under tuneIzh9p/

For more details and discussion, please refer to the following article:
(more details coming soon...)
Simple models of diverse intrinsic dynamics in hippocampal neuron types
Siva Venkadesh, Alexander O. Komendantov, Stanislav Listopad, Eric O. Scott, Kenneth De Jong, Jeffrey L. Krichmar, Giorgio A. Ascoli
