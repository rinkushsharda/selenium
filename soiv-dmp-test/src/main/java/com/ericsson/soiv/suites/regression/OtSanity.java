package com.ericsson.soiv.suites.regression;

import com.ericsson.jive.core.execution.ConfigurableJiveSuite;
import com.ericsson.jive.core.frameworkconfiguration.JiveSuiteConfiguration;

public class OtSanity extends ConfigurableJiveSuite {

    @Override
    protected JiveSuiteConfiguration getJiveSuiteConfiguration() { //NOSONAR
        return new JiveSuiteConfiguration() {

            @Override
            public String getJivePackageBase() {
                return "com.ericsson.soiv";
            }

            @Override
            public String getJivePackageInclude() {
                return "testcases";
            }

            @Override
            public String getJivePackageExclude() {
                return null;
            }

            @Override
            public String getJiveTestClassInclude() {
                return null;
            }

            @Override
            public String getJiveTestClassExclude() {
                return null;
            }

            @Override
            public String getJiveTestCaseIdInclude() {
                return "00000003,00000008,00000016,00000022,00000025,00000029,00000032,00000035,00000054,00000059,00000060,00000062,00000066,00000071,00000076";
            }

            @Override
            public String getJiveTestCaseIdExclude() {
                return null;
            }

            @Override
            public String getJiveTestCaseNameInclude() {
                return null;
            }

            @Override
            public String getJiveTestCaseNameExclude() {
                return null;
            }

            @Override
            public String getJiveTestCaseTagInclude() {return null;}

            @Override
            public String getJiveTestCaseTagExclude() {
                return "PENDING-Update";
            }

            @Override
            public String getSuiteName() {
                return "OtSanity";
            }
        };
    }



}
