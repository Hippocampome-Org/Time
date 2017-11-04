#include "json.h"

namespace patch
{
    template < typename T > std::string to_string( const T& n )
    {
        std::ostringstream stm ;
        stm << n ;
        return stm.str() ;
    }
}

class SpikePatternJson{
	 float I;
	 float Idur;
	 float tStep;
	 int* spikeTimes;
	 int stLength;
	 float* vTrace;
	 int vtLength;

public:
	 SpikePatternJson(float i, int idur, float tstep, int* spike_times, int stlength, float* vtrace, int vtlength){
		 I= i;
		 Idur = idur;
		 tStep = tstep;
		 spikeTimes = new int[stlength];
		 for(int i=0;i<stlength;i++){
			 spikeTimes[i]=spike_times[i];
		 }
		 stLength = stlength;
		 vTrace= new float[vtlength];
		 for(int i=0;i<vtlength;i++){
			 vTrace[i]=vtrace[i];
		 }
		 vtLength=vtlength;
	 }

	 ~SpikePatternJson(){
		 delete vTrace;
		 delete spikeTimes;
	 }
	 Json::Value retrieveJson(){
		 Json::Value spikePattern;
		 Json::Value spike_times(Json::arrayValue);
		 Json::Value v_trace(Json::arrayValue);
		 for(int i=0;i<stLength;i++){
			 spike_times.append(Json::Value(spikeTimes[i]));
		 }
		 for(int i=0;i<vtLength;i++){
			 v_trace.append(Json::Value(vTrace[i]));
		 }
		 spikePattern["I"] = I;
		 spikePattern["I_dur"] = Idur;
		 spikePattern["t_step"] = tStep;
		 spikePattern["v_trace"] = v_trace;
		 spikePattern["spike_times"] = spike_times;
		 return spikePattern;
	 }
};
class MultiCompDataJson{
	 int* rheos;
	 float* vDefs;
	 int compCount;
	 float* spikeProp;
	 float* epsps;

public:
	 MultiCompDataJson( int* _rheos, float* _vDefs, float* _spikeProps, float* _epsps, int _compCount){
		 rheos = _rheos;
		 vDefs = _vDefs;
		 spikeProp = _spikeProps;
		 epsps = _epsps;
		 compCount = _compCount;
	 }
	 ~MultiCompDataJson(){
	 }
	 Json::Value retrieveJson(){
		 Json::Value multiCompData;

		 Json::Value _rheos(Json::arrayValue);
		 for(int i=0;i<compCount;i++){
			 _rheos.append(Json::Value(rheos[i]));
		 }
		 multiCompData["ramp_rheos"] = _rheos;

		 Json::Value _vDefs(Json::arrayValue);
		 for(int i=0;i<compCount;i++){
			 _vDefs.append(Json::Value(vDefs[i]));
		 }
		 multiCompData["v_defs"] = _vDefs;

		 Json::Value _spikeProps(Json::arrayValue);
		 for(int i=0;i<compCount-1;i++){
			_spikeProps.append(Json::Value(spikeProp[i]));
		 }
		 multiCompData["spike_proped"] = _spikeProps;

		 Json::Value _epsps(Json::arrayValue);
		 for(int i=0;i<compCount-1;i++){
			 _epsps.append(Json::Value(epsps[i]));
		 }
		 multiCompData["epsps"] = _epsps;

		 return multiCompData;
	 }
 };
