
// include CARLsim user interface
#include <carlsim.h>
#include <stopwatch.h>
#include <sstream>
#include <string>

int main() {

	int randSeed = 1505420798;
	
	float exc_weight = 8;
	float inh_weight = 1.5;

	CARLsim* network = new CARLsim("synseg", GPU_MODE, SILENT, 0, randSeed);

	/*
	 * arbitraty presynaptic spiketrains
	 */
	int inh_input=network->createSpikeGeneratorGroup("input", 1, INHIBITORY_NEURON);
	std::vector<int> spkTimes = {140, 142,  150,155, 160, 163, 169,	 280,285,  295,299 };
	SpikeGeneratorFromVector SGV_inh(spkTimes);
	network->setSpikeGenerator(inh_input, &SGV_inh);

	int exc_input=network->createSpikeGeneratorGroup("input", 1, EXCITATORY_NEURON);
	std::vector<int> spkTimes2 = {95, 99, 103, 159,164, 305, 309, 320, 325, 329};
	SpikeGeneratorFromVector SGV_exc(spkTimes2);
	network->setSpikeGenerator(exc_input, &SGV_exc);

	int	mc_soma1=network->createGroup("mc_soma1", 1, EXCITATORY_NEURON);
	int	mc_dend1=network->createGroup("mc_dend1", 1, EXCITATORY_NEURON);
	int	mc_soma2=network->createGroup("mc_soma2", 1, EXCITATORY_NEURON);
	int	mc_dend2=network->createGroup("mc_dend2", 1, EXCITATORY_NEURON);

	int sc_soma=network->createGroup("sc_soma", 1, EXCITATORY_NEURON);

	/*
	 * 2-compartment model: 4-006-1 (CA1-PPA)
	 */
	float k0 = 4.496187471;
	float a0 = 0.002010625;
	float b0 = 3.419040616;
	float d0 = 61;
	float C0 = 282;
	float vr0 = -55.32395127;
	float vt0 = -46.55491017;
	float vpeak0 = -5.237704808;
	float c0 = -50.74194412;

	float k1= 0.668732356;
	float a1= 0.009238603;
	float b1=16.52010734;
	float d1=425;
	float C1=682;
	float vr1=-55.32395127;
	float vt1=-26.70125197	;
	float vpeak1=-12.95567579;
	float c1=-50.41480868;

	float G = 173;
	float p = 0.195538505;

	network->setNeuronParameters(mc_soma1, C0, k0, vr0, vt0, a0, b0, vpeak0, c0, d0);
	network->setNeuronParameters(mc_dend1, C1, k1, vr1, vt1, a1, b1, vpeak1, c1, d1);
	network->setCompartmentParameters(mc_soma1, G*(1-p), 0);
	network->setCompartmentParameters(mc_dend1, 0, G*p);
	network->connectCompartments(mc_soma1, mc_dend1);

	network->setNeuronParameters(mc_soma2, C0, k0, vr0, vt0, a0, b0, vpeak0, c0, d0);
	network->setNeuronParameters(mc_dend2, C1, k1, vr1, vt1, a1, b1, vpeak1, c1, d1);
	network->setCompartmentParameters(mc_soma2, G*(1-p), 0);
	network->setCompartmentParameters(mc_dend2, 0, G*p);
	network->connectCompartments(mc_soma2, mc_dend2);

	/*
	 * 1-c model: 4-006-1 (CA1 PPA)
	 */

	float k = 4.100487678;
	float a = 7.26E-05;
	float b = 5.046796684;
	float d = 47;
	float C = 233;
	float vr = -55.46848131;
	float vt = -46.10308834;
	float vpeak = -5.272886261;
	float c = -51.04148064;

	network->setNeuronParameters(sc_soma, C, k, vr, vt, a, b, vpeak, c, d);

	/******************
	 * END MODEL DEF
	 ******************
	 ******************/

	/*
	 * MC scenario1: excitatory input to dendrite
	 * 				 inhibitory input to "soma"
	 */
	network->connect(exc_input, mc_dend1, "full", RangeWeight(4*exc_weight), 1, RangeDelay(1),RadiusRF(-1),SYN_FIXED);
	network->connect(inh_input, mc_soma1, "full", RangeWeight(inh_weight), 1, RangeDelay(1),RadiusRF(-1),SYN_FIXED);

	/*
	 * MC scenario2: excitatory input to soma
	 * 				 inhibitory input to soma
	 */
	network->connect(exc_input, mc_soma2, "full", RangeWeight(exc_weight), 1, RangeDelay(1),RadiusRF(-1),SYN_FIXED);
	network->connect(inh_input, mc_dend2, "full", RangeWeight(4*inh_weight), 1, RangeDelay(1),RadiusRF(-1),SYN_FIXED);

	 /* SC scenario: excitatory input to point
	 * 				 inhibitory input to the same point
	 */
	network->connect(exc_input, sc_soma, "full", RangeWeight(exc_weight), 1, RangeDelay(1),RadiusRF(-1),SYN_FIXED);
	network->connect(inh_input, sc_soma, "full", RangeWeight(inh_weight), 1, RangeDelay(1),RadiusRF(-1),SYN_FIXED);

	network->setConductances(true);
	network->setIntegrationMethod(RUNGE_KUTTA4, 100);
	network->setupNetwork();

	std::string fileName1s = "results/syn_pair/soma1_V";
	std::string fileName1d = "results/syn_pair/dend1_V";
	std::string fileName2s = "results/syn_pair/soma2_V";
	std::string fileName2d = "results/syn_pair/dend2_V";

	std::string fileName3 = "results/syn_pair/sc_V";

	network->setExternalCurrent(mc_soma1, 0);
	network->setExternalCurrent(mc_dend1, 0);
	network->setExternalCurrent(mc_soma2, 0);
	network->setExternalCurrent(mc_dend2, 0);

	network->setExternalCurrent(sc_soma, 0);

	network->recordVoltage(mc_soma1, 0, fileName1s);
	network->recordVoltage(mc_dend1, 0, fileName1d);
	network->recordVoltage(mc_soma2, 0, fileName2s);
	network->recordVoltage(mc_dend2, 0, fileName2d);

	network->recordVoltage(sc_soma, 0, fileName3);
	network->runNetwork(1,0);


	return 0;
}
