package ec.app.izhikevich.inputprocess.labels;

public enum MCConstraintAttributeID {
	type,
	INCLUDE,
	//excitability
	current_min,
	current_max,
	current_duration,
	current_step,
	rheo_diff,
	
	//input resistance	
//	current_duration,
	current,
	v_at_time,	
		
	//forward propagation
	dend_current_min,
	dend_current_max,
	dend_current_time_min,
	dend_current_duration,
	dend_current_step,
	dend_target_spike_freq,
	spike_prop_rate_min,
	
	//syn stimulated epsp
	sim_duration,
	ampa_epsp,
	ampa_tau,
	nmda_epsp,
	nmda_tau,
	
	//common
	cons_weight
}
