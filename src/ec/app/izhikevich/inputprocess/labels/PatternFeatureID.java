package ec.app.izhikevich.inputprocess.labels;

public enum PatternFeatureID {
	/*
	 * non constraints
	 */
	IDX_NOTE,
	INCLUDE,
	pattern_class,
	compartment,
	type,
	current,
	time_min,
	current_duration,
	eval,  
	pattern_weight,
	/*
	 * Constraints
	 */
	n_spikes, 
	//isis,	// currently, no range allowed
	avg_isi, 
	fsl, 
	pss, 
	
	sfa_linear_m0, // currently, no range allowed
	sfa_linear_b0, // currently, no range allowed
	n_sfa_isis0,  // currently, no range allowed
	
	sfa_linear_m1, // currently, no range allowed
	sfa_linear_b1, // currently, no range allowed
	n_sfa_isis1,  // currently, no range allowed
	
	sfa_linear_m2, // currently, no range allowed
	sfa_linear_b2, // currently, no range allowed
	n_sfa_isis2,  // currently, no range allowed
	
	non_sfa_avg_isi, 
	sub_ss_voltage, 
	sub_ss_voltage_sd,
	vmin_offset,
	time_const,
	valid_max_v,
	
	nbursts,
	bursts,
	stuts,
	
	dendMirrorSpikes,
	
	rebound_VMax,
	swa,
	
	period, // for chaos
	
	b_w,
	pbi,
	b_nspikes// final analysis
}
