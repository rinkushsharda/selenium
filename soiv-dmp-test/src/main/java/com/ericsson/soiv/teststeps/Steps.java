package com.ericsson.soiv.teststeps;

import com.ericsson.soiv.teststeps.billing.StepsBillGeneration;
import com.ericsson.soiv.teststeps.charging.StepsChargingOnline;
import com.ericsson.soiv.teststeps.charging.offline.StepsChargingOffline;
import com.ericsson.soiv.teststeps.charging.offline.raw.StepsChargingRawOffline;
import com.ericsson.soiv.teststeps.charging.online.raw.StepsChargingRaw;
import com.ericsson.soiv.teststeps.protocols.StepsProtocols;
import com.ericsson.soiv.teststeps.provision.StepsProvision;

public class Steps {
public final static StepsProvision PROVISION = new StepsProvision();
public final static StepsProtocols PROTOCOLS = new StepsProtocols();
public final static StepsChargingOnline CHARGING_ONLINE = new StepsChargingOnline();
public final static StepsBillGeneration BILL_GENERATION = new StepsBillGeneration();
public final static StepsChargingOffline CHARGING_OFFLINE = new StepsChargingOffline();
public final static StepsChargingRaw OnlineChargingRaw = new StepsChargingRaw();
public final static StepsChargingRawOffline OfflineChargingBscsRaw = new StepsChargingRawOffline();
}
