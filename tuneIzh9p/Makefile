# Makefile for building Tutorial code from the CARLsim library

# NOTE: need to tell CARLsim where to find the user.mk
USER_MK_PATH = ../../
include $(USER_MK_PATH)user.mk.sample

#local_targets := tuneIzh9p_debug
#local_targets := tuneIzh9p_1c_wrapper
#local_targets := tuneIzh9p_2c_wrapper
#local_targets := tuneIzh9p_3c1_wrapper
#local_targets := tuneIzh9p_3c2_wrapper
#local_targets := tuneIzh9p_4c1_wrapper
local_targets := tuneIzh9p_4c2_wrapper

# name of ECJ parameter file.
#ecj_param_file := $(local_targets)Experiment.params
output := *.dot *.dat *.log *.stat results/*

# -----------------------------------------------------------------------------
# You should not need to edit the file beyond this point
# -----------------------------------------------------------------------------

# You should not need to edit the file beyond this point
# ------------------------------------------------------

# we are compiling from lib
CARLSIM_FLAGS += -I$(CARLSIM_LIB_DIR)/include/kernel \
				 -I$(CARLSIM_LIB_DIR)/include/interface \
				 -I$(CARLSIM_LIB_DIR)/include/spike_monitor \
				 -I$(CARLSIM_LIB_DIR)/include/connection_monitor \
				 -I$(CARLSIM_LIB_DIR)/include/spike_generators \
				 -I$(CARLSIM_LIB_DIR)/include/visual_stimulus \
				 -I$(CARLSIM_LIB_DIR)/include/simple_weight_tuner \
				 -I$(CARLSIM_LIB_DIR)/include/stopwatch \
				 -I$(CARLSIM_LIB_DIR)/include/group_monitor
				 
CARLSIM_LIBS  += -L$(CARLSIM_LIB_DIR)/lib -lCARLsim

# carlsim ecj components
ECJ_PTI_FLAGS += -I$(ECJ_PTI_DIR)/include
ECJ_PTI_LIBS  += $(ECJ_PTI_DIR)/lib/libCARLsimPTI.a

local_src  := main_$(local_targets).cpp
local_prog := $(local_targets)
carlsim_prog := carlsim_$(local_targets)
# you can add your own local objects
local_objs := jsoncpp.cpp
local_objs += MCNeuronSim.cpp

.PHONY: all clean distclean
#all: $(local_targets)
# create executable bash script for user to run
#$(local_prog): $(local_src) $(carlsim_prog)
#	@echo "#!/bin/bash" > $(local_prog)
#	@echo "java -cp \"$(ECJ_DIR)/jar/ecj.22.jar:$(ECJ_PTI_DIR)/lib/CARLsim-ECJ.jar\" ecjapp.CARLsimEC -file ./$(ecj_param_file)" >> $(local_prog)
#	@chmod u+x $(local_prog)

# compile from CARLsim lib
$(carlsim_prog): $(local_src) $(local_objs)
	$(NVCC) $(CARLSIM_INCLUDES) $(ECJ_PTI_FLAGS) $(CARLSIM_FLAGS) \
	$(CARLSIM_LFLAGS) $(CARLSIM_LIBS) $(ECJ_PTI_LIBS) $(local_objs) $< -o $@

#without ECJ flags for temporary
#$(carlsim_prog): $(local_src) $(local_objs)
#	$(NVCC) $(CARLSIM_INCLUDES) $(CARLSIM_FLAGS) \
#	$(CARLSIM_LFLAGS) $(CARLSIM_LIBS) $(local_objs) $< -o $@

clean:
	$(RM) $(local_prog) $(carlsim_prog)

distclean:
	$(RM) $(local_prog) $(carlsim_prog) $(output)
