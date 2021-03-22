package com.ericsson.soiv.teststeps.provision;

import com.ericsson.soiv.teststeps.provision.bscs.StepsBSCS;
import com.ericsson.soiv.teststeps.provision.eoc.StepsEOC;
import com.ericsson.soiv.teststeps.provision.sdp.StepsSDP;

public class StepsProvision {
    public StepsBSCS bscs = new StepsBSCS();
    public StepsEOC eoc = new StepsEOC();
    public StepsSDP sdp = new StepsSDP();
}
