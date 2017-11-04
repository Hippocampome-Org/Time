/*! Multi compartment Layout
 * O =
 */
#include <PTI.h>
#include <carlsim.h>
#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <string>
#include <sstream>
#include <ctime>

#include "core/MCNeuronSim.h"
#include "parsingHelpers.h"

using namespace CARLsim_PTI;

int MCNeuronSim::k_idx[] = {0};int MCNeuronSim::a_idx[] = {1};int MCNeuronSim::b_idx[] ={2};int MCNeuronSim::d_idx[] = {3};int MCNeuronSim::C_idx[]= {4};
int MCNeuronSim::vr_idx = 5;int MCNeuronSim::vt_idx[]={6};int MCNeuronSim::vmin_idx[]={7};int MCNeuronSim::vpeak_idx[] ={8};
int MCNeuronSim::G_idx[] = {-1};int MCNeuronSim::P_idx[] = {-1};int MCNeuronSim::W_idx[] = {-1};int MCNeuronSim::I_start_idx =9;

int MCNeuronSim::time_step =100;

namespace CARLsim_PTI {
class TuneIzh9p1cWrapper : public Experiment {
private:
  const unsigned int _deviceID;
public:
	TuneIzh9p1cWrapper(const unsigned int deviceID) : _deviceID(deviceID) {}

	void run(const ParameterInstances &parameters, std::ostream &outputStream) const {

			const int compCnt = 1;
			int conn_layout[compCnt] = {0};
			int scenCount = (parameters.getNumParameters() - MCNeuronSim::I_start_idx)/2;
			int popSize_neuronCount = parameters.getNumInstances();

			double** _parameters;
			_parameters = new double* [popSize_neuronCount];
			for(int i=0;i<popSize_neuronCount;i++){
				_parameters[i] = new double[parameters.getNumParameters()];
				for(int j=0;j<parameters.getNumParameters();j++){
					_parameters[i][j]=parameters.getParameter(i,j);
				}
			}

			int* _connLayout = new int[compCnt];
			for(int i=0;i<compCnt;i++){
				_connLayout[i] = conn_layout[i];
			}

			MCNeuronSim* mc = new MCNeuronSim(compCnt, _connLayout, scenCount, popSize_neuronCount, _parameters);

			mc->initNetwork(_deviceID);
			mc->setupGroups();
			mc->setupAllNeuronParms();
			mc->setupIandRunNetwork();
	//		mc->determineRheo(10, 1000, 10, 10);

	//		mc->measureVoltageDeflection(-100, 500, 450);
	//		mc->determinePropRate(1000,2000,250,100);
	//		mc->measureSomaticEPSP(50);

			mc->writePhenotype(outputStream);

			delete mc;
		}

    };
}
int main(int argc, char* argv[]) {
	const int deviceID = getIntegerArgument("-device", argc, argv, 0);
	  const TuneIzh9p1cWrapper tune1c(deviceID);
		const PTI pti(argc, argv, std::cout, std::cin);
		pti.runExperiment(tune1c);

		return 0;
}

