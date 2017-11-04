/*! \brief
 *
 */
#include <PTI.h>
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
#include "core/json.h"
#include "core/MCNeuronSim.h"
#include <ctime>
#include<iostream>
#include<fstream>

using namespace std;
using namespace CARLsim_PTI;

/*
 * initialize const indices! 1 comp
 */
/*
int MCNeuronSim::k_idx[] = {0};int MCNeuronSim::a_idx[] = {1};int MCNeuronSim::b_idx[] ={2};int MCNeuronSim::d_idx[] = {3};int MCNeuronSim::C_idx[]= {4};
int MCNeuronSim::vr_idx = 5;int MCNeuronSim::vt_idx[]={6};int MCNeuronSim::vmin_idx[]={7};int MCNeuronSim::vpeak_idx[] ={8};
int MCNeuronSim::G_idx[] = {-1};int MCNeuronSim::P_idx[] = {-1};int MCNeuronSim::W_idx[] = {-1};int MCNeuronSim::I_start_idx =9;
*/

/*
 * initialize const indices! 2 comp
 */
 int MCNeuronSim::k_idx[] = {0, 1};int MCNeuronSim::a_idx[] = {2,3};int MCNeuronSim::b_idx[] ={4,5};int MCNeuronSim::d_idx[] = {6,7};
int MCNeuronSim::C_idx[]= {8,9};int MCNeuronSim::vr_idx = 10;int MCNeuronSim::vt_idx[]={11,12};int MCNeuronSim::vmin_idx[]={13,14};int MCNeuronSim::vpeak_idx[] ={15,16};
int MCNeuronSim::G_idx[] = {17};int MCNeuronSim::P_idx[] = {18};int MCNeuronSim::W_idx[] = {19};int MCNeuronSim::I_start_idx =20;

/*
 * initialize const indices! 3 comp
 */
/*
 int MCNeuronSim::k_idx[] = {0, 1, 2};int MCNeuronSim::a_idx[] = {3,4,5};int MCNeuronSim::b_idx[] ={6,7,8};int MCNeuronSim::d_idx[] = {9,10,11};
int MCNeuronSim::C_idx[]= {12,13,14};int MCNeuronSim::vr_idx = 15;int MCNeuronSim::vt_idx[]={16,17,18};int MCNeuronSim::vmin_idx[]={19,20,21};int MCNeuronSim::vpeak_idx[] ={22,23,24};
int MCNeuronSim::G_idx[] = {25,26};int MCNeuronSim::P_idx[] = {27,28};int MCNeuronSim::W_idx[] = {29,30};int MCNeuronSim::I_start_idx =31;
*/
/*
 * initialize const indices! 4 comp
 */

 /*int MCNeuronSim::k_idx[] = {0, 1, 2, 3};int MCNeuronSim::a_idx[] = {4, 5, 6, 7};int MCNeuronSim::b_idx[] ={8, 9, 10, 11};int MCNeuronSim::d_idx[] = {12, 13, 14, 15};
int MCNeuronSim::C_idx[]= {16, 17, 18, 19};int MCNeuronSim::vr_idx = 20;int MCNeuronSim::vt_idx[]={21, 22, 23, 24};int MCNeuronSim::vmin_idx[]={25, 26, 27, 28};int MCNeuronSim::vpeak_idx[] ={29, 30, 31, 32};
int MCNeuronSim::G_idx[] = {33, 34, 35};int MCNeuronSim::P_idx[] = {36, 37, 38};int MCNeuronSim::W_idx[] = {39, 40, 41};int MCNeuronSim::I_start_idx =42;
*/
/*
 * common
 */
//int MCNeuronSim::runTime = 1000;
int MCNeuronSim::time_step =100;

int main(int argc, char* argv[]) {

	FILE *fp;

						fp = fopen("Debug.txt", "a");
						fprintf(fp,"entry - main :\t");
						fclose(fp);



	const int compCnt = 2;
	const int conn_layout[compCnt] = {0,0};
	const int scenCount =1;
	const int popSize_neuronCount = 1;
	const int N_NEURONS = 1;
	const int N_PARMS = (9*compCnt-(compCnt-1)) + (3*(compCnt-1)) + (2*scenCount);

	/*
	 * read parm from a file
	 */
	ifstream infile;
	int i=0;
	char cNum[25];
	double parms[N_PARMS];

	infile.open ("inpGene.txt", ifstream::in);
	                if (infile.is_open())
	                {
	                        while (infile.good())
	                        {
	                                infile.getline(cNum, 256, ',');
	                                parms[i]= stod(cNum) ;
	                                i++ ;
	                        }
	                        infile.close();
	                }
	                else
	                {
	                        cout << "Error opening file";
	                }
	/*double parms[N_PARMS]	=

	{
			1.3759035, 1.2730268, 0.03967973, 0.017186081, -23.001848, -23.87721, 235.0, 830.0, 262.0, 218.0, -57.389076, 23.91178, 24.831375, 8.009567, 8.875533, 94.67751, 5.158985, 133.0, 0.3341655, 8.717381, 99.0, 523

	};*/

	double** _passParms;
	_passParms = new double*[N_NEURONS];
	_passParms[0] = new double[N_PARMS];
	for(int i=0;i<N_PARMS;i++){
		_passParms[0][i] = parms[i];
	}
	fp = fopen("Debug.txt", "a");
								fprintf(fp,"after passparms :\t");
								fclose(fp);
	int* _connLayout = new int[compCnt];
	for(int i=0;i<compCnt;i++){
		_connLayout[i] = conn_layout[i];
	}

	fp = fopen("Debug.txt", "a");
							fprintf(fp,"before mcn :\t");
							fclose(fp);


	MCNeuronSim* mc = new MCNeuronSim(compCnt, _connLayout, scenCount, popSize_neuronCount,_passParms);

	fp = fopen("Debug.txt", "a");
							fprintf(fp,"after mc :\t");
							fclose(fp);


	int start_s=clock();
		mc->initNetwork();
	int stop_s=clock();
	mc->writeTime("instantiation", stop_s-start_s);

	start_s=clock();
		mc->setupGroups();
		mc->setupAllNeuronParms();
	stop_s=clock();
	mc->writeTime("setup Groups and Parms", stop_s-start_s);

	start_s=clock();
		mc->setupIandRunNetwork();
	stop_s=clock();
	mc->writeTime("I and run Network", stop_s-start_s);

	if(compCnt > 1){
		start_s=clock();
				mc->determineRheo(10, 1000, 10, 100);
			stop_s=clock();
			mc->writeTime("determine ramp rheo", stop_s-start_s);

			start_s=clock();
				mc->measureVoltageDeflection(-100, 999, 900);
			stop_s=clock();
			mc->writeTime("measure vDef", stop_s-start_s);

			start_s=clock();
				//mc->determinePropRate(1000,2000,250,100);
				mc->determinePropRate(200); // pregroup neuron spike at 34 ms!
			stop_s=clock();
			mc->writeTime("determine prop rate", stop_s-start_s);

			start_s=clock();
				mc->measureSomaticEPSP(100); // pregroup neuron spike at 34 ms!
			stop_s=clock();
			mc->writeTime("measure epsp", stop_s-start_s);
	}

	start_s=clock();
		mc->writePhenotype();
	stop_s=clock();
	mc->writeTime("write-phenotype", stop_s-start_s);

	delete mc;
}





