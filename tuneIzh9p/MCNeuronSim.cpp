#include <carlsim.h>
#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <vector>
#include <math.h>
#include <cassert>
#include <limits>

#include <string>
#include <sstream>
#include <fstream>
#include "core/json.h"
#include "core/phenotype-structures.h"
#include "core/MCNeuronSim.h"
#include <ctime>


	MCNeuronSim::MCNeuronSim(const int comp_cnt, int* _connLayout,
			const int scen_count, const int _popSize_neuronCount, double** _parameters){

		scenCount = scen_count;
		compCount = comp_cnt;
		mcScenCount = 0; //for single compartment
		if(compCount>1){
			mcScenCount = 1+			// rheos
					1+					//IR
					(compCount-1)+		//for proprate sims for all dendritic comps.
					(compCount-1); 		// EPSP
		}
		totalGroupCount = scenCount *compCount; // cuz of n compartments


		popSize_neuronCount = _popSize_neuronCount;
		//std::cout<<popSize_neuronCount<<"\n";
		if(popSize_neuronCount < 50){
			writeAllVs = true;
		}else{
			writeAllVs = false;
		}

		customI = 0;

		Idur_start_idx = I_start_idx+scenCount ;

		connLayout = _connLayout;

		I.reserve(scenCount);
		Idur = new int* [scenCount];

		excGroup = new int* [scenCount+mcScenCount+1]; // +1 for an extra single compartment single neuron group for epsp scenario,
														//and multi neuron spikers group for new spike prop implementation
		excMonitor = new SpikeMonitor** [scenCount+mcScenCount];

		for(int i=0;i<scenCount;i++){
			//I[i] = new float[popSize_neuronCount];

			//I[i].reserve(popSize_neuronCount);

			Idur[i] = new int[popSize_neuronCount];
		}

		for(int i=0;i<scenCount+mcScenCount;i++){
			excGroup[i] = new int[compCount];
			excMonitor[i] = new SpikeMonitor*[compCount];
		}

		nPreSynSpikerGroups = 10;
		excGroup[scenCount+mcScenCount] = new int[nPreSynSpikerGroups * 2]; // single compartment neuron [0] for epsp, single compartment neuronS [1] for prop test.
														// changed from 2 to 20 cuz of post synapses limit.
		parameters = _parameters;

		if(compCount>1){
			rheos = new int* [popSize_neuronCount];
			vDefs = new float* [popSize_neuronCount];
			somEpsps = new float* [popSize_neuronCount];
			epspsAll = new float** [popSize_neuronCount];
			spikePropRates = new float* [popSize_neuronCount];
			for(int i=0;i<popSize_neuronCount;i++){
				rheos[i] = new int[compCount];
				vDefs[i] = new float[compCount];
				spikePropRates[i] = new float[compCount-1];
				somEpsps[i] = new float[compCount-1];
				epspsAll[i] = new float* [compCount-1];
				for(int j=0;j<compCount-1;j++){
					epspsAll[i][j]= new float[compCount];
				}
			}
		}
}

	MCNeuronSim::~MCNeuronSim(){
			//timer 6
			int	start_s = clock();
			/*
			 * clearing up!!!
			 */

			for(int i=0;i<scenCount;i++){
			//	delete I[i];
				delete Idur[i];
			}
			for(int i=0;i<scenCount+mcScenCount;i++){
				delete excGroup[i];
				delete excMonitor[i];
			}
			delete excGroup[scenCount+mcScenCount];

			//delete I;
			delete Idur;
			delete excGroup;
			delete excMonitor;
			//delete in;
			if(compCount>1){
				for(int i=0;i<popSize_neuronCount;i++){
					delete rheos[i];
					delete vDefs[i];
					delete spikePropRates[i];
					delete somEpsps[i];
					for(int j=0;j<compCount-1;j++){
						delete epspsAll[i][j];
					}
					epspsAll[i];
				}
				delete rheos ;
				delete vDefs ;
				delete somEpsps ;
				delete epspsAll;
				delete spikePropRates ;
			}
			delete network;

}

	void MCNeuronSim::initNetwork(const int deviceID){
			/**************
		 * [I] construct a CARLsim network on the heap.
		 ***************/
			network = new CARLsim("MCNeuronSim_core", GPU_MODE, SILENT, deviceID);
	}

	void MCNeuronSim::isCustomI(const int is_custom_I){
		customI = is_custom_I;
	}
	void MCNeuronSim::setupGroups(){
		//int poissonGroup[scenCount];
			/*
			 * (A) first setup groups for somatic scenarios! - connect compartments for MC!
			 */
			float G_up, G_dn;
			for(int k = 0; k < scenCount; k++)
			{
				//std::string poissonGroupName = "poisson_" + patch::to_string(k);
				//poissonGroup[k] = network->createSpikeGeneratorGroup(poissonGroupName, popSize_neuronCount, EXCITATORY_NEURON);
				for(int c=0; c<compCount; c++)
				{
					std::string excGroupName = "exc_" + patch::to_string(k) + patch::to_string(c);
					excGroup[k][c] = network->createGroup(excGroupName, popSize_neuronCount, EXCITATORY_NEURON);
					network->setNeuronParameters(excGroup[k][c], 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f);
			//		network->setExternalCurrent(excGroup[k][c], 0);
					if(compCount>1){
						G_up = 0;
						G_dn = 0;
						network->setCompartmentParameters(excGroup[k][c], G_up, G_dn);
					}
					excMonitor[k][c] = network->setSpikeMonitor(excGroup[k][c], "/dev/null");

					if(c>0){//connect compartments based on layout
						if(compCount>2 && c==1 && connLayout[c]==connLayout[c+1]){ //meaning 2 dendrites (dend 1 and dend2 ) connecting to the same point
							network->connectCompartments(excGroup[k][c], excGroup[k][connLayout[c]]);
						}else{
							network->connectCompartments(excGroup[k][connLayout[c]], excGroup[k][c]);
						}
					}
				}

				//????????????
				//network->connect(poissonGroup[k], excGroup[k][0],"random", RangeWeight(0.0f), 0.5f, RangeDelay(1));
				//????????????
			}
			/*
			 * (B) secondly, setup groups for MC scenarios! - connect compartments depending on scenarios as below
			 *  - 1. rheo group : decoupled
			 *  - 2. IR: decoupled
			 *  - 3. spike prop: Coupled
			 *  - 4. syn ampl: Coupled
			 */
			for(int k = scenCount; k < scenCount+mcScenCount; k++)
				{
				for(int c=0; c<compCount; c++)
					{
						std::string excGroupName = "mcc_exc_" + patch::to_string(k) + patch::to_string(c);
						excGroup[k][c] = network->createGroup(excGroupName, popSize_neuronCount, EXCITATORY_NEURON);
						network->setNeuronParameters(excGroup[k][c], 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f);
						if(k>=scenCount+2){// coupled scenarios for mcc
							G_up = 0;
							G_dn = 0;
							network->setCompartmentParameters(excGroup[k][c], G_up, G_dn);
						}

						excMonitor[k][c] = network->setSpikeMonitor(excGroup[k][c], "/dev/null");

						if(k>=scenCount+2){
							if(c>0){//connect compartments based on layout
								if(compCount>2 && c==1 && connLayout[c]==connLayout[c+1]){ //meaning 2 dendrites (dend 1 and dend2 ) connecting to the same point
									network->connectCompartments(excGroup[k][c], excGroup[k][connLayout[c]]);
								}else{
									network->connectCompartments(excGroup[k][connLayout[c]], excGroup[k][c]);
								}
							}
						}
					}
				}
			//single neuron single compartment for EPSP stimulation
			for(int pssg=0;  pssg<nPreSynSpikerGroups;pssg++){
				excGroup[scenCount+mcScenCount][pssg] = network->createGroup("spiker_"+patch::to_string(pssg), 1, EXCITATORY_NEURON);
				network->setNeuronParameters(excGroup[scenCount+mcScenCount][pssg], 121.0f, 0.5343182f,-61.20764f,-40.00114f,
									0.0336908f, 6.5490165f, 35.965454f,-41.31163f, 7.0f);
			}

			float connProb=0.3f;
			if(writeAllVs){
				connProb=1.0f;
			}
			const int epspGrpRowStartIdx = scenCount + 2+ (compCount-1);
			float _weight = 1.0f;
			for(int kc=0; kc<(compCount-1); kc++){
				/*
				 * new addition for CA1 pyramidal dendrites analysis
				 */
				if(compCount==4 && kc==0)
					_weight = 1.0f;
				if(compCount==4 && kc==1)
					_weight = 1.0f/3.0f;//1.0f/6.0f;
				if(compCount==4 && kc==2)
					_weight = 2.5f*(1.0f/3.0f); //3.0f*(1.0f/3.0f);


				/*if(compCount==4 && kc==2)
					_weight = 1.0f;
					*/
				/*
				 * new addition ends
				 */


				for(int pssg=0;  pssg<nPreSynSpikerGroups;pssg++){
					network->connect(excGroup[scenCount+mcScenCount][pssg], excGroup[epspGrpRowStartIdx + kc][kc+1],
											"random", RangeWeight(_weight), connProb,
											RangeDelay(1),  RadiusRF(-1), SYN_FIXED, 1.0f, 0.0f);
				}
			}

			//multi neuron single compartment spikers for new spike prop implementation via synaptic stimulation
			for(int pssg=nPreSynSpikerGroups;  pssg<nPreSynSpikerGroups*2;pssg++){
				//excGroup[scenCount+mcScenCount][pssg] = network->createGroup("spiker_"+ patch::to_string(pssg), 16, EXCITATORY_NEURON);
				excGroup[scenCount+mcScenCount][pssg] = network->createGroup("spiker_"+ patch::to_string(pssg), 85, EXCITATORY_NEURON);

				network->setNeuronParameters(excGroup[scenCount+mcScenCount][pssg], 121.0f, 0.5343182f,-61.20764f,-40.00114f,
									0.0336908f, 6.5490165f, 35.965454f,-41.31163f, 7.0f);
			}

			const int propRateGrpRowStartIdx = scenCount + 2;

			//_weight =33.0f;
			_weight =2.0f;
			for(int kc=0; kc<(compCount-1); kc++){
				//if(kc+1>1)
				//	_weight = 10.0f;
					if(compCount==4 && kc==0)
						_weight = 1.0f;
					if(compCount==4 && kc==1)
						_weight = 1.0f;	//_weight = 0;			//0, cuz this is not a check for prop in SR. By setting this weight 0, SR only has the I(=700?, {see determine prop rate function})- this is a baseline to test conditional spike propagation
					if(compCount==4 && kc==2)
						_weight = 2.0f;

				for(int pssg=nPreSynSpikerGroups;  pssg<nPreSynSpikerGroups*2;pssg++){
					network->connect(excGroup[scenCount+mcScenCount][pssg], excGroup[propRateGrpRowStartIdx + kc][kc+1],
																"random", RangeWeight(_weight), connProb,
																RangeDelay(1,10),  RadiusRF(-1), SYN_FIXED, 1.0f, 0.0f );
				}
			}

			if(customI==1){
				std::cout<<"pre - in";
				custom_grp_in=network->createSpikeGeneratorGroup("custom_input_0", 1, EXCITATORY_NEURON);
				std::vector<int> spkTimes = {150, 250, 400, 600};
				SpikeGeneratorFromVector SGV(spkTimes);
				network->setSpikeGenerator(custom_grp_in, &SGV);

				int compartment_to_stimulate = 1;
				//create poisson group!
				network->connect(custom_grp_in, excGroup[0][compartment_to_stimulate], "full", RangeWeight(10), 1, RangeDelay(1),RadiusRF(-1),SYN_FIXED);

				std::cout<<"custom_input EXIT ";							//
			}


			//
			/*
			 *  (C) Now, setup network!
			 */
			network->setConductances(true);
			network->setIntegrationMethod(RUNGE_KUTTA4, time_step);
			network->setupNetwork();
		}

	void MCNeuronSim::setupGroups_custom(){

			float G_up, G_dn;
			int k=0;
				//std::string poissonGroupName = "poisson_" + patch::to_string(k);
				//poissonGroup[k] = network->createSpikeGeneratorGroup(poissonGroupName, popSize_neuronCount, EXCITATORY_NEURON);
				for(int c=0; c<compCount; c++)
				{
					std::string excGroupName = "exc_" + patch::to_string(k) + patch::to_string(c);
					excGroup[k][c] = network->createGroup(excGroupName, popSize_neuronCount, EXCITATORY_NEURON);
					network->setNeuronParameters(excGroup[k][c], 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f);
			//		network->setExternalCurrent(excGroup[k][c], 0);
					if(compCount>1){
						G_up = 0;
						G_dn = 0;
						network->setCompartmentParameters(excGroup[k][c], G_up, G_dn);
					}
					excMonitor[k][c] = network->setSpikeMonitor(excGroup[k][c], "/dev/null");

					if(c>0){//connect compartments based on layout
						if(compCount>2 && c==1 && connLayout[c]==connLayout[c+1]){ //meaning 2 dendrites (dend 1 and dend2 ) connecting to the same point
							network->connectCompartments(excGroup[k][c], excGroup[k][connLayout[c]]);
						}else{
							network->connectCompartments(excGroup[k][connLayout[c]], excGroup[k][c]);
						}
					}
				}

				/*
				 *
				 */
				//std::cout<<"pre ";

				//int custom_grp_in2=network->createSpikeGeneratorGroup("input", 10, EXCITATORY_NEURON);
				//std::vector<int> spkTimes = {150, 250, 400, 600};
				//SpikeGeneratorFromVector SGV(spkTimes);
		//		network->setSpikeGenerator(custom_grp_in2, &SGV);

					//std::cout<<"custom_input TRUE ";
					//int compartment_to_stimulate = 1;
					//create poisson group!
					//network->connect(custom_grp_in, excGroup[0][compartment_to_stimulate], "full", RangeWeight(10), 1, RangeDelay(1),RadiusRF(-1),SYN_FIXED);

				//std::cout<<"custom_input EXIT ";

			network->setConductances(true);
			network->setIntegrationMethod(RUNGE_KUTTA4, time_step);
			network->setupNetwork();
	}

	void MCNeuronSim::setupAllNeuronParms(){
			/*
			 * [A] first, setup parms for somatic scenarios!
			 */
			for(unsigned int k = 0; k < scenCount; k++)
				{
					std::vector<float> tempI;
					tempI.reserve(popSize_neuronCount);
					for(unsigned int n = 0; n < popSize_neuronCount; n++)
						{
							setupSingleNeuronParms(k, n, true);
							tempI.push_back(parameters[n][I_start_idx+k]);
							Idur[k][n]= parameters[n][Idur_start_idx+k];
						}
					I.push_back(tempI);
				}

			/*
			 * [A] second, setup parms for mcc scenarios!
			 *  - remember decoupled mcc scenarios for 1, 2
			 *  - coupled mcc scenarios for 3,... (3+n-1),..
			 */
			//timer 3
			for(unsigned int k = scenCount; k < scenCount+mcScenCount; k++)
				{
					for(unsigned int n = 0; n < popSize_neuronCount; n++)
						{
							if(k>=scenCount+2){// coupled scenarios for mcc
								setupSingleNeuronParms(k, n, true);
							}else{//decoupled mcc scenarios
								setupSingleNeuronParms(k, n, false);
							}
						}
				}
		}


	void MCNeuronSim::setupSingleNeuronParms(int grpRowId, int neurId, bool coupledComp){
			for(unsigned int c = 0; c < compCount; c++) // each neuron has compCount compartments
			{
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "C", getCm(neurId, c));
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "k", getK(neurId, c));
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "vr", getVr(neurId));
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "vt", getVt(neurId, c));
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "a", getA(neurId, c));
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "b", getB(neurId, c));
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "vpeak", getVpeak(neurId, c));
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "c", getVmin(neurId, c));
				network->setIzhikevichParameter(excGroup[grpRowId][c], neurId, "d", getD(neurId, c));

				if(coupledComp){
					if(c>0){
						double G = getG(neurId, c); //parameters[neurId][G_idx[c-1]];
						double P = getP(neurId, c);//parameters[neurId][P_idx[c-1]];
						float fwd = G * P;
						float bwd = G * (1-P);
						/*
						 * generally, fwd is carlsim 'down', bwd is carlsim 'up' for the purpose of coupling constant assignment, but,
						 * when there is a dendrite 'below' soma: ****cases 3c2 and 4c2***
						 * up and down are reversed.
						 */
						if(compCount>2 && c==1 && connLayout[c]==connLayout[c+1]){ //meaning 2 dendrites (dend 1 and dend2 ) connecting to the same point
							network->setCouplingConstant(excGroup[grpRowId][connLayout[c]], neurId, "down", bwd);
							network->setCouplingConstant(excGroup[grpRowId][c], neurId, "up", fwd);
						}else{
							network->setCouplingConstant(excGroup[grpRowId][c], neurId, "down", fwd);
							network->setCouplingConstant(excGroup[grpRowId][connLayout[c]], neurId, "up", bwd);
						}
					}
				}
			}
		}

	void MCNeuronSim::setupIandRunNetwork(){

				for(int k = 0; k < scenCount; k++){
					for(int c=0;c<compCount; c++){
						if(writeAllVs){
							for(int i=0;i<popSize_neuronCount;i++){
								std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(k) + "_" + patch::to_string(c);
								network->recordVoltage(excGroup[k][c], i, fileName); // for now just record from first neuron
							}
						}
						network->setExternalCurrent(excGroup[k][c], 0);
					}
				}
		network->runNetwork(0, 100);

			for(int k = 0; k < scenCount; k++){
				network->setExternalCurrent(excGroup[k][0], I[k]);			// external current scenarios are only for somatic compartment
				excMonitor[k][0]->startRecording();
				if(writeAllVs){
						for(int c=0;c<compCount; c++){
							for(int i=0;i<popSize_neuronCount;i++){
								std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(k) + "_" + patch::to_string(c);
								network->recordVoltage(excGroup[k][c], i, fileName); // for now just record from first neuron
							}
						}
				}
			}

			int sec=0;
			int msec=Idur[0][0];
			if(msec>999){
				sec=msec/1000;
				msec=msec-(sec*1000);
			}
		//	std::cout<<"sec: "<<sec<<",   msec:"<<msec;
			network->runNetwork(sec, msec); //remember, Idur has repetitive values (same for all neurons within a group)
			network->setExternalCurrent(excGroup[0][0], 0);
			//network->runNetwork(0, 100);

			for(int k = 1; k < scenCount; k++)
			 {
				sec=0;
				msec=Idur[k][0]-Idur[k-1][0];  // assuming durations are in asc order
				if(msec>999){
					sec=msec/1000;
					msec=msec-(sec*1000);
				}
			//	std::cout<<"sec: "<<sec<<",   msec:"<<msec;
				network->runNetwork(sec, msec);
				network->setExternalCurrent(excGroup[k][0], 0);
			 }

			// if(1000>Idur[scenCount-1][0]){
			//	 network->runNetwork(0, 1000 - Idur[scenCount-1][0]); // complete 1s runtime, if required
			 //}

				for(int k = 0; k < scenCount; k++){
					for(int c=0;c<compCount; c++){
						if(writeAllVs){
							for(int i=0;i<popSize_neuronCount;i++){
								std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(k) + "_" + patch::to_string(c);
								network->recordVoltage(excGroup[k][c], i, fileName); // for now just record from first neuron
							}
						}
						network->setExternalCurrent(excGroup[k][c], 0);
					}
				}
				network->runNetwork(0, 100);


		}



	void MCNeuronSim::setupCustomI_AndRunNetwork(){

		//empty sim for 100ms
		int k=0; // 1 scenario

		excMonitor[k][0]->startRecording();

		for(int c=0;c<compCount; c++){
			if(writeAllVs){
				for(int i=0;i<popSize_neuronCount;i++){
					std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(k) + "_" + patch::to_string(c);
					network->recordVoltage(excGroup[k][c], i, fileName); // for now just record from first neuron
				}
			}
			network->setExternalCurrent(excGroup[k][c], 0);
		}

		network->runNetwork(0, 100);

		//10hz 20hz inputs
		//PoissonRate in(1);
		//in.setRates(10);
		//network->setSpikeRate(custom_grp_in,&in);


		//
		if(writeAllVs){
			for(int c=0;c<compCount; c++){
				for(int i=0;i<popSize_neuronCount;i++){
					std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(k) + "_" + patch::to_string(c);
					network->recordVoltage(excGroup[k][c], i, fileName);
				}
			}
		}

		network->runNetwork(1, 900);
}

	void MCNeuronSim::writePhenotype(std::ostream &outputStream){
		std::vector< std::vector<int> > spikeTimes[scenCount];
			for(int k = 0; k < scenCount; k++)
				{
					excMonitor[k][0]->stopRecording();
					spikeTimes[k] = excMonitor[k][0]->getSpikeVector2D();
					excMonitor[k][0]->clear();
				}

			for(unsigned int i = 0; i < popSize_neuronCount; i++) {
				Json::Value wrapper;
				Json::Value somaPatterns(Json::arrayValue);

				/*
				 * [1] write somatic scenarios!
				 */
				for(unsigned int k = 0; k < scenCount; k++){
					int* spike_times = &spikeTimes[k][i][0];
					int stl = spikeTimes[k][i].size();
					//	float* group_voltage = network->retrieveGroupVoltages(g);
					float vTrace[0];
					int vtl = 0;
					SpikePatternJson* sp = new SpikePatternJson(I[k][i], Idur[k][i], 0.1f, spike_times, stl, vTrace, vtl);
					Json::Value spikePattern = sp->retrieveJson();
					somaPatterns.append(spikePattern);
					delete sp;
					//delete spike_times;
				}
				wrapper["soma_patterns"] = somaPatterns;

				/*
				 * [2] write mcc scenarios!
				 */
				if(compCount>1){
					MultiCompDataJson* mcd = new MultiCompDataJson(rheos[i], vDefs[i], spikePropRates[i], somEpsps[i], compCount, epspsAll[i]);
					Json::Value mcData = mcd->retrieveJson();
					wrapper["multi_comp_sim"] = mcData;
				}

				std::string fileNameStr = std::string("results/")+patch::to_string(i)+std::string("_phenotype");
				const char *fileName = fileNameStr.c_str();
				std::ofstream file(fileName);
				file << wrapper << std::endl;
				file.close();

				outputStream << fileNameStr << std::endl;
			}
			//delete[] spikeTimes;
		}

	void MCNeuronSim::writePhenotype(){
				std::vector< std::vector<int> >* spikeTimes;
				spikeTimes = new std::vector< std::vector<int> > [scenCount];
				for(int k = 0; k < scenCount; k++)
					{
						excMonitor[k][0]->stopRecording();
						spikeTimes[k] = excMonitor[k][0]->getSpikeVector2D();
						excMonitor[k][0]->clear();
					}

				for(unsigned int i = 0; i < popSize_neuronCount; i++) {
					Json::Value wrapper;
					Json::Value somaPatterns(Json::arrayValue);

					/*
					 * [1] write somatic scenarios!
					 */
					for(unsigned int k = 0; k < scenCount; k++){
						int* spike_times = &spikeTimes[k][i][0];
						int stl = spikeTimes[k][i].size();
						//	float* group_voltage = network->retrieveGroupVoltages(g);
						float vTrace[1];
						int vtl = 0;
						SpikePatternJson* sp = new SpikePatternJson(I[k][i], Idur[k][i], 0.1f, spike_times, stl, vTrace, vtl);
						Json::Value spikePattern = sp->retrieveJson();
						somaPatterns.append(spikePattern);
						delete sp;
					}
					wrapper["soma_patterns"] = somaPatterns;

					/*
					 * [2] write mcc scenarios!
					 */
					if(compCount>1){
						MultiCompDataJson* mcd = new MultiCompDataJson(rheos[i], vDefs[i], spikePropRates[i], somEpsps[i], compCount, epspsAll[i]);
						Json::Value mcData = mcd->retrieveJson();
						wrapper["multi_comp_sim"] = mcData;
					}

					std::string fileNameStr = std::string("results/")+patch::to_string(i)+std::string("_phenotype");
					const char *fileName = fileNameStr.c_str();
					std::ofstream file(fileName);
					file << wrapper << std::endl;
					file.close();
				}
				delete[] spikeTimes;
			}
	 /* Determines the excitability of compartments as rheo currents
		 *  - rheo: minimum depolarizing current required to elicit spikes
		 *  - measured for isolated compartments
		 * 	- dendritic rheo is returned only if it is less than somatic, which is not valid,
		 * 		and consequently an error will be added to ECJ fitness, depending on the rheo difference bw soma and dendrite.
		 * 	- the loop (with increasing current) is stopped once all SOMATIC rheos have been identified
		 * 		- therefore, for an undetermined dendritic compartment, its rheo is either higher than the already found-out rheo of soma (valid case)
		 *		- or, this undetermined dendritic compartment is passive (no rheo current could be found at all), which is also a valid case
		 *
	 *		- uses ramp current to save sim time: ***NEW***
		 */
	void MCNeuronSim::determineRheo(float iMin, float iMax, float iStep, int iDur){
			/*int runDur = Idur[scenCount-1][0];
				if(runTime>Idur[scenCount-1][0]){
					 runDur = runTime;
				 }
			 */

				const int grpRowIdx = scenCount+ 0;
				for(int c=0;c<compCount; c++){
					if(writeAllVs){
						for(int i=0;i<popSize_neuronCount;i++){
							std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowIdx) + "_" + patch::to_string(c);
							network->recordVoltage(excGroup[grpRowIdx][c], i, fileName); // for now just record from first neuron
						}
					}
					network->setExternalCurrent(excGroup[grpRowIdx][c], 0);
				}
				network->runNetwork(0, 100);

				bool rheoFound[popSize_neuronCount][compCount];
				// init with default
				for(int i=0;i<popSize_neuronCount;i++){
					for(int j=0;j<compCount;j++){
						rheos[i][j]=99999;
						rheoFound[i][j]=false;
					}
				}


				for(float I=iMin; I<iMax; I+=iStep){
					for(int c=0;c<compCount;c++){
						network->setExternalCurrent(excGroup[grpRowIdx][c], I);
						excMonitor[grpRowIdx][c]->startRecording();
						if(writeAllVs){
							for(int i=0;i<popSize_neuronCount;i++){
								std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowIdx) + "_" + patch::to_string(c);
								network->recordVoltage(excGroup[grpRowIdx][c], i, fileName); // for now just record from first neuron
							}
						}
					}

					//run and retrieve spikevectors

					network->runNetwork(0, iDur);
					//runDur += iDur;

					std::vector< std::vector<int> > spikeTimes[compCount];

					for(int c=0;c<compCount;c++){
						excMonitor[grpRowIdx][c]->stopRecording();
						spikeTimes[c] = excMonitor[grpRowIdx][c]->getSpikeVector2D();
						excMonitor[grpRowIdx][c]->clear();
						/*
						 * reset necessary? shouldn't be!

						network->setExternalCurrent(excGroup[grpRowIdx][c], 0);
						network->runNetwork(0, 100);
						//runDur += 100;
						 *
						 * */
					}

					//set rheobase
					for(int i=0;i<popSize_neuronCount;i++){
						for(int c=0;c<compCount;c++){
							if(!rheoFound[i][c]){
								if(spikeTimes[c][i].size()>0){
									//std::cout<<"comp. "<<c<<"\n";
									//std::cout<<"current. "<<I<<"\n";
									//std::cout<<"st size. "<<spikeTimes[c][i].size()<<"\n";
									//std::cout<<"spike times 0. "<<(spikeTimes[c][i][0]-(runDur-iDur))<<"\n";
									//std::cout<<"sim time. "<<runDur<<"\n";
									rheoFound[i][c]=true;
									rheos[i][c]=I;
								}
							}
						}
					}

					//check for termination
					bool allSomaticRheoFound = true;
					for(int i=0;i<popSize_neuronCount;i++){
						if(!rheoFound[i][0]){
							allSomaticRheoFound = false;
						}
					}
					if(allSomaticRheoFound){
						//break;
					}
				}

				for(int c=0;c<compCount; c++){
					if(writeAllVs){
						for(int i=0;i<popSize_neuronCount;i++){
							std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowIdx) + "_" + patch::to_string(c);
							network->recordVoltage(excGroup[grpRowIdx][c], i, fileName); // for now just record from first neuron
						}
					}
					network->setExternalCurrent(excGroup[grpRowIdx][c], 0);
				}

				network->runNetwork(0, 100);
				//for (int i = 0; i < popSize_neuronCount; i++) {
				//	delete[] * rheoFound;
				//}
			}

	void MCNeuronSim::measureVoltageDeflection(int I, int I_dur,  int V_at_T){
		//	int runDur = Idur[scenCount-1][0];
			const int grpRowIdx = scenCount+ 1;


			for(int c=0;c<compCount; c++){
				if(writeAllVs){
					for(int i=0;i<popSize_neuronCount;i++){
						std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowIdx) + "_" + patch::to_string(c);
						network->recordVoltage(excGroup[grpRowIdx][c], i, fileName); // for now just record from first neuron
					}
				}
				network->setExternalCurrent(excGroup[grpRowIdx][c], 0);
			}

			network->runNetwork(0, 100);

			/*
			 * following is what's necessary for fitness! the above for voltage plots
			 */
			if(writeAllVs){
				for(int c=0;c<compCount; c++){
					for(int i=0;i<popSize_neuronCount;i++){
													std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowIdx) + "_" + patch::to_string(c);
													network->recordVoltage(excGroup[grpRowIdx][c], i, fileName); // for now just record from first neuron
												}
				}
			}

			for(int c=0;c<compCount;c++){
				network->setExternalCurrent(excGroup[grpRowIdx][c], I);
				//excMonitor[grpRowIdx][c]->startRecording();
				for(int i=0;i<popSize_neuronCount;i++){
					std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(c);
					network->recordVoltage(excGroup[grpRowIdx][c], i, fileName);

				}
			}

			network->runNetwork(0, I_dur);


			for(int c=0;c<compCount; c++){
				if(writeAllVs){
					for(int i=0;i<popSize_neuronCount;i++){
						std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowIdx) + "_" + patch::to_string(c);
						network->recordVoltage(excGroup[grpRowIdx][c], i, fileName); // for now just record from first neuron
					}
				}
				network->setExternalCurrent(excGroup[grpRowIdx][c], 0);
			}

			network->runNetwork(0, 100);

			//now, read Vs from the written file!
			for(int c=0;c<compCount;c++){
				for(int i=0;i<popSize_neuronCount;i++){
					std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(c);
					float* vTrace = readVtrace(fileName, I_dur, true);
					vDefs[i][c]=vTrace[V_at_T-1];
					delete vTrace;
				}
			}
		}

	float* MCNeuronSim::readVtrace(std::string fileName, int nLines, bool deleteFile){
			float* vTrace = new float[nLines];

			std::ifstream infile(fileName.c_str());

			std::string file_line;
			std::string delimiter = ";";
			int i=0;
			while (std::getline(infile, file_line))
			{
				if(i>=nLines){
					break;
					//std::cout<<"More lines read than expected from "+patch::to_string(fileName)+"--nlines read:"+patch::to_string(i+1);
				}
			    std::istringstream iss1(file_line);
			    std::string line;
			    iss1 >> line;

			    //tokenize and convert to float
			    std::string token =line.substr(0, line.find(delimiter));
				std::istringstream iss2(token);
				iss2 >> vTrace[i++];
				//std::cout<<vTrace[i-1]<<"\n";
			}
			if(deleteFile)
				std::remove(fileName.c_str());
			return vTrace;
		}

	/*void MCNeuronSim::determinePropRate(int iMin, int iMax, int iStep, int iDur){
		//	int runDur = Idur[scenCount-1][0];
				const int grpRowStartIdx = scenCount+ 2;

				bool thresholdCurrentFound[popSize_neuronCount][compCount-1];
				// init with default
				for(int i=0;i<popSize_neuronCount;i++){
					for(int j=0;j<compCount-1;j++){
						spikePropRates[i][j]=false;
						thresholdCurrentFound[i][j]=false;
					}
				}


				for(unsigned int I=iMin; I<iMax; I+=iStep){
					for(int kc=1;kc<compCount;kc++){//ofset for grprowstartidx; also compartment index
						network->setExternalCurrent(excGroup[grpRowStartIdx+(kc-1)][kc], I);
						excMonitor[grpRowStartIdx+(kc-1)][kc]->startRecording(); //group where I is injected
						excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->startRecording(); //group where prop rate checked
					}
					network->runNetwork(0, iDur);
					//runDur+= iDur;
					std::vector< std::vector<int> > spikeTimes_siteOfI[compCount-1];
					std::vector< std::vector<int> > spikeTimes_siteOfCheck[compCount-1];
					for(int kc=1;kc<compCount;kc++){
						excMonitor[grpRowStartIdx+(kc-1)][kc]->stopRecording();
						excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->stopRecording();
						spikeTimes_siteOfI[kc-1] = excMonitor[grpRowStartIdx+(kc-1)][kc]->getSpikeVector2D();
						spikeTimes_siteOfCheck[kc-1] = excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->getSpikeVector2D();
						excMonitor[grpRowStartIdx+(kc-1)][kc]->clear();
						excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->clear();
					}

					//set threshold and other
					for(int i=0;i<popSize_neuronCount;i++){
						for(int kc=0;kc<compCount-1;kc++){
							if(!thresholdCurrentFound[i][kc]){
								if(spikeTimes_siteOfI[kc][i].size()>0){
									//std::cout<<"comp. site. of I. "<<(kc+1)<<"\n";
									//std::cout<<"current. "<<I<<"\n";
									//std::cout<<"st size. "<<spikeTimes[c][i].size()<<"\n";
									//std::cout<<"spike times 0. "<<(spikeTimes_siteOfI[kc][i][0]-(runDur))<<"\n";
									//std::cout<<"sim time. "<<runDur<<"\n";
									thresholdCurrentFound[i][kc]=true;

									if(spikeTimes_siteOfCheck[kc][i].size()>0){
										spikePropRates[i][kc]=true;
									}
								}
							}
						}
					}

					//check for termination
					bool allThresholdFound = true;
					for(int i=0;i<popSize_neuronCount;i++){
						for(int j=0;j<compCount-1;j++){
							if(!thresholdCurrentFound[i][j]){
								allThresholdFound = false;
							}
						}
					}
					if(allThresholdFound){
						break;
					}
			}
		}
*/
	void MCNeuronSim::determinePropRate(int I_dur){
			//	int runDur = Idur[scenCount-1][0];
			const int grpRowStartIdx = scenCount+ 2;
			const float I_for_epsp = 188;
			for(int pssg=nPreSynSpikerGroups;  pssg<nPreSynSpikerGroups*2;pssg++){
				network->setExternalCurrent(excGroup[scenCount+mcScenCount][pssg], I_for_epsp);
			}

			// init with default
			for(int i=0;i<popSize_neuronCount;i++){
				for(int j=0;j<compCount-1;j++){
					spikePropRates[i][j]=0.0;
					//thresholdCurrentFound[i][j]=false;
				}
			}
			//I_dur = 100;
			for(int kc=1;kc<compCount;kc++){//ofset for grprowstartidx; also compartment index

				/*
				 * temp I for SLM to elicit spike!
				 */

				//network->setExternalCurrent(excGroup[grpRowStartIdx+(kc-1)][kc], 0.0f);
				if(kc==2){ // SR
					network->setExternalCurrent(excGroup[grpRowStartIdx+1][2], 0.0f); // [2] --> I is setup in SR
								// ALSO RESET near line 860
				}
				if(kc==3){ // SLM
					network->setExternalCurrent(excGroup[grpRowStartIdx+2][2], 0.0f); // [2] cuz I is setup in SR
								// ALSO RESET near line 860
				}

				excMonitor[grpRowStartIdx+(kc-1)][kc]->startRecording(); //group where synaptic input received (should have spiked)
				excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->startRecording(); //group where prop rate checked

				if(writeAllVs){
					for(int c=0;c<compCount; c++){
						for(int i=0;i<popSize_neuronCount;i++){
							std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowStartIdx+(kc-1)) + "_" + patch::to_string(c);
							network->recordVoltage(excGroup[grpRowStartIdx+(kc-1)][c], i, fileName); // for now just record from first neuron
						}
					}
				}
			}
			//network->recordVoltage(excGroup[grpRowStartIdx + 0][1], 0, "temp");
			// Pre synaptic spiker spikes at 33, 56 etc. Turn off I after 50 ms, in order to allow only one spike!
			network->runNetwork(0, 40);
			for(int pssg=nPreSynSpikerGroups;  pssg<nPreSynSpikerGroups*2;pssg++){
				network->setExternalCurrent(excGroup[scenCount+mcScenCount][pssg], 0);
			}
			network->setExternalCurrent(excGroup[grpRowStartIdx+1][2], 0.0f);
			network->setExternalCurrent(excGroup[grpRowStartIdx+2][2], 0.0f);

			//loop repeat for vrecording
			for(int kc=1;kc<compCount;kc++){
				if(writeAllVs){
					for(int c=0;c<compCount; c++){
						for(int i=0;i<popSize_neuronCount;i++){
							std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowStartIdx+(kc-1)) + "_" + patch::to_string(c);
							network->recordVoltage(excGroup[grpRowStartIdx+(kc-1)][c], i, fileName); // for now just record from first neuron
						}
					}
				}
			}

			//let the mc neuron run for remaining duration to allow spike generation!
			network->runNetwork(0, I_dur-40);
			//network->setExternalCurrent(excGroup[grpRowStartIdx+compCount-2][compCount-2], 0.0f);
			//runDur+= iDur;
			std::vector< std::vector<int> > spikeTimes_siteOfI[compCount-1];
			std::vector< std::vector<int> > spikeTimes_siteOfCheck[compCount-1];

			for(int kc=1;kc<compCount;kc++){
				excMonitor[grpRowStartIdx+(kc-1)][kc]->stopRecording();
				excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->stopRecording();
				spikeTimes_siteOfI[kc-1] = excMonitor[grpRowStartIdx+(kc-1)][kc]->getSpikeVector2D();
				spikeTimes_siteOfCheck[kc-1] = excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->getSpikeVector2D();
				excMonitor[grpRowStartIdx+(kc-1)][kc]->clear();
				excMonitor[grpRowStartIdx+(kc-1)][connLayout[kc]]->clear();
			}

			if(writeAllVs){
				for(int kc=1;kc<compCount;kc++){
					for(int c=0;c<compCount; c++){
						for(int i=0;i<popSize_neuronCount;i++){
													std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(grpRowStartIdx+(kc-1)) + "_" + patch::to_string(c);
													network->recordVoltage(excGroup[grpRowStartIdx+(kc-1)][c], i, fileName); // for now just record from first neuron
												}
					}
				}
			}
			network->runNetwork(0, 100);
			//set threshold and other
						for(int i=0;i<popSize_neuronCount;i++){
							for(int kc=0;kc<compCount-1;kc++){
									if(spikeTimes_siteOfI[kc][i].size()>0){
										float spikesAtSiteOfCheck = spikeTimes_siteOfCheck[kc][i].size();
										float spikesAtSiteOfInit = spikeTimes_siteOfI[kc][i].size();
										spikePropRates[i][kc]=spikesAtSiteOfCheck/spikesAtSiteOfInit;
									}
								/*		std::cout<<"comp. site. of syn stim. \n";
										std::cout<<"n spikes. "<<spikeTimes_siteOfI[kc][i].size()<<"\n";
										for(int ii=0;ii<spikeTimes_siteOfI[kc][i].size();ii++){
											std::cout<<(spikeTimes_siteOfI[kc][i][ii])<<", ";
										}

										std::cout<<"comp. site. of check. \n";
										std::cout<<"n spikes. "<<spikeTimes_siteOfCheck[kc][i].size()<<"\n";
										for(int ii=0;ii<spikeTimes_siteOfCheck[kc][i].size();ii++){
											std::cout<<(spikeTimes_siteOfCheck[kc][i][ii])<<", ";
										}*/
										/*
										 * here look up the down compartment spike count and set!
										 */
									//if(spikeTimes_siteOfCheck[kc][i].size()>0){

									//}
									//}
							}
						}
						//delete[] spikeTimes_siteOfI;
						//delete[] spikeTimes_siteOfCheck;
			}

	void MCNeuronSim::measureSomaticEPSP(int I_dur){
			const int epspGrpRowStartIdx = scenCount + 2+ (compCount-1);
			const float I_for_epsp = 188;

			for(int pssg=0;  pssg<nPreSynSpikerGroups;pssg++){
				network->setExternalCurrent(excGroup[scenCount+mcScenCount][pssg], I_for_epsp);
			}


			for(int kc=0; kc<(compCount-1); kc++){
				for(int i=0;i<popSize_neuronCount;i++){
						std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(kc+1)+"_";
						network->recordVoltage(excGroup[epspGrpRowStartIdx + kc][0], i, fileName); //always record frm soma - indx = 0
				}
				if(writeAllVs){
					for(int c=0;c<compCount; c++){
						for(int i=0;i<popSize_neuronCount;i++){
													std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(epspGrpRowStartIdx + kc) + "_" + patch::to_string(c);
													network->recordVoltage(excGroup[epspGrpRowStartIdx + kc][c], i, fileName); // for now just record from first neuron
												}
					}
				}
			}
			network->runNetwork(0, 40);

			/*
			 * reset spiker current for v plots
			 */
			for(int pssg=0;  pssg<nPreSynSpikerGroups;pssg++){
				network->setExternalCurrent(excGroup[scenCount+mcScenCount][pssg], 0);
			}
			for(int kc=0; kc<(compCount-1); kc++){
				for(int i=0;i<popSize_neuronCount;i++){
						std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(kc+1)+"_";
						network->recordVoltage(excGroup[epspGrpRowStartIdx + kc][0], i, fileName); //always record frm soma - indx = 0
				}
				if(writeAllVs){
					I_dur +=50;
					for(int c=0;c<compCount; c++){
						for(int i=0;i<popSize_neuronCount;i++){
							std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(epspGrpRowStartIdx + kc) + "_" + patch::to_string(c);
							network->recordVoltage(excGroup[epspGrpRowStartIdx + kc][c], i, fileName); // for now just record from first neuron
						}
					}
				}
			}
			network->runNetwork(0, I_dur-40);

			//now, read Vs from the written file!
			for(int kc=0; kc<(compCount-1); kc++){
				for(int i=0;i<popSize_neuronCount;i++){
					std::string fileName = "V_"+ patch::to_string(i) + "_" + patch::to_string(kc+1)+"_";
					float* vTrace = readVtrace(fileName, I_dur, true);

					float maxVoltage = -1000;
					for(int ii=0;ii<I_dur;ii++){
						if(vTrace[ii]>maxVoltage){
							maxVoltage = vTrace[ii];
						}
					}
					somEpsps[i][kc]=maxVoltage;
					delete vTrace;
				}
				/*
				 * new addition: for mc analysis
				 */
				if(writeAllVs){
					for(int c=0;c<compCount; c++){
						for(int i=0;i<popSize_neuronCount;i++){
							std::string fileName = "results/"+patch::to_string(i)+"allV_"+ patch::to_string(epspGrpRowStartIdx + kc) + "_" + patch::to_string(c);
							float* vTrace = readVtrace(fileName, I_dur, false);

							float maxVoltage = -1000;
							for(int ii=0;ii<I_dur;ii++){
								if(vTrace[ii]>maxVoltage){
									maxVoltage = vTrace[ii];
								}
							}
							epspsAll[i][kc][c]=maxVoltage;
							delete vTrace;
						}
					}
				}
			}
	}

	void MCNeuronSim::writeTime(std::string item, double timeInNanoSec){
		double timeInMs = (timeInNanoSec)/double(CLOCKS_PER_SEC)*1000;
		FILE *fp;
		fp = fopen("Info.txt", "a");
		fprintf(fp, item.c_str());
		fprintf(fp, " :\t");
		fprintf(fp, (patch::to_string(timeInMs)).c_str());
		fprintf(fp,"\n");
		fclose(fp);
	}

	double MCNeuronSim::getK(int neurId, int compId){
		//if(compId == 0)
			return parameters[neurId][k_idx[compId]];
		//else
			//return parameters[neurId][k_idx[0]]+parameters[neurId][k_idx[compId]];
	}
	double MCNeuronSim::getA(int neurId, int compId){
			//if(compId == 0)
				return parameters[neurId][a_idx[compId]];
			//else
			//	return parameters[neurId][a_idx[0]]+parameters[neurId][a_idx[compId]];
	}
	double MCNeuronSim::getB(int neurId, int compId){
			//if(compId == 0)
				return parameters[neurId][b_idx[compId]];
			//else
			//	return parameters[neurId][b_idx[0]]+parameters[neurId][b_idx[compId]];
	}
	double MCNeuronSim::getD(int neurId, int compId){
			//if(compId == 0)
				return parameters[neurId][d_idx[compId]];
			//else
			//	return parameters[neurId][d_idx[0]]+parameters[neurId][d_idx[compId]];
		}
	double MCNeuronSim::getCm(int neurId, int compId){
		//	if(compId == 0)
				return parameters[neurId][C_idx[compId]];
		//	else
		//		return parameters[neurId][C_idx[0]]+parameters[neurId][C_idx[compId]];
		}
	//vr same across all comps
	double MCNeuronSim::getVr(int neurId){
			return parameters[neurId][vr_idx];
		}
	double MCNeuronSim::getVt(int neurId, int compId){
			//if(compId == 0)
				return parameters[neurId][vr_idx] + parameters[neurId][vt_idx[compId]];
			//else
				//return parameters[neurId][vr_idx] + (parameters[neurId][vt_idx[0]]+parameters[neurId][vt_idx[compId]]);
		}
	double MCNeuronSim::getVmin(int neurId, int compId){
			//if(compId == 0)
				return parameters[neurId][vr_idx] +  parameters[neurId][vmin_idx[compId]];
			//else
				//return parameters[neurId][vr_idx] + (parameters[neurId][vmin_idx[0]]+parameters[neurId][vmin_idx[compId]]);
		}
	double MCNeuronSim::getVpeak(int neurId, int compId){
				if(compId == 0)
					return parameters[neurId][vr_idx] +  parameters[neurId][vpeak_idx[compId]];
				else
					return (parameters[neurId][vr_idx] + parameters[neurId][vpeak_idx[0]]) - parameters[neurId][vpeak_idx[compId]];
		}
	// G and P => i-1 idx
	double MCNeuronSim::getG(int neurId, int compId){
		//	if(compId == 1)
				return parameters[neurId][G_idx[compId-1]];
		//	else
		//		return parameters[neurId][G_idx[0]]+parameters[neurId][G_idx[compId-1]];
		}
	double MCNeuronSim::getP(int neurId, int compId){
		return parameters[neurId][P_idx[compId-1]];
	}
	//gene W is currently not used!
	double MCNeuronSim::getW(int neurId, int compId){
		return parameters[neurId][W_idx[compId]];
	}


