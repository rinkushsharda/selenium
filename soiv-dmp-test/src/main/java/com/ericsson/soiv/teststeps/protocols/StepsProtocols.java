package com.ericsson.soiv.teststeps.protocols;

import com.ericsson.soiv.teststeps.protocols.air.raw.StepsAirRaw;
import com.ericsson.soiv.teststeps.protocols.bscs.businesslogic.StepsBusinessLogicBSCS;
import com.ericsson.soiv.teststeps.protocols.bscs.raw.StepsBscsRaw;
import com.ericsson.soiv.teststeps.protocols.cs.buisnesslogic.StepsBusinessLogicCS;
import com.ericsson.soiv.teststeps.protocols.cs.raw.StepsCsRaw;
import com.ericsson.soiv.teststeps.protocols.eoc.businesslogic.StepsBusinessLogicEOC;
import com.ericsson.soiv.teststeps.protocols.eoc.raw.StepsEocRaw;

public class StepsProtocols {
    public StepsBscsRaw bscs = new StepsBscsRaw();
    public StepsEocRaw eoc = new StepsEocRaw();
    public StepsCsRaw cs = new StepsCsRaw();
    public StepsBusinessLogicEOC businessLogicEOC = new StepsBusinessLogicEOC();
    public StepsBusinessLogicBSCS businessLogicBSCS = new StepsBusinessLogicBSCS();
    public StepsBusinessLogicCS businessLogicCS = new StepsBusinessLogicCS();
    public StepsAirRaw air = new StepsAirRaw();

}
