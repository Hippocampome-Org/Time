/*! Multi compartment Layout
 * O = =
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

int MCNeuronSim::k_idx[] = {0, 1, 2};
int MCNeuronSim::a_idx[] = {3,4,5};
int MCNeuronSim::b_idx[] ={6,7,8};
int MCNeuronSim::d_idx[] = {9,10,11};
int MCNeuronSim::C_idx[]= {12,13,14};
int MCNeuronSim::vr_idx = 15;
int MCNeuronSim::vt_idx[]={16,17,18};
int MCNeuronSim::vmin_idx[]={19,20,21};
int MCNeuronSim::vpeak_idx[] ={22,23,24};

int MCNeuronSim::G_idx[] = {25,26};
int MCNeuronSim::P_idx[] = {27,28};
int MCNeuronSim::W_idx[] = {29,30};

int MCNeuronSim::I_start_idx =31;

int MCNeuronSim::time_step =100;

namespace CARLsim_PTI {
class TuneIzh9p3c1Wrapper : public Experiment {
private:
  const unsigned int _deviceID;
public:
  TuneIzh9p3c1Wrapper(const unsigned int deviceID) : _deviceID(deviceID) {}


	void run(const ParameterInstances &parameters, std::ostream &outputStream) const {

		const int compCnt = 3;
			int conn_layout[compCnt] = {0,0,1};

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

						mc->determineRheo(10.0f, 1000.0f, 0.1f, 1);
						mc->measureVoltageDeflection(-500, 999, 900);
					//	mc->determinePropRate(1000,2000,250,100);
						mc->determinePropRate(200);
						mc->measureSomaticEPSP(100);

			mc->writePhenotype(outputStream);

			delete mc;
		}

    };
}
int main(int argc, char* argv[]) {
	/*FILE *fp;

					fp = fopen("Debug.txt", "a");
					fprintf(fp,"entry - main :\t");
					fclose(fp);
					delete fp;
*/
	const int deviceID = getIntegerArgument("-device", argc, argv, 0);

		const TuneIzh9p3c1Wrapper tune3c1(deviceID);
		const PTI pti(argc, argv, std::cout, std::cin);
		pti.runExperiment(tune3c1);

		return 0;
}

