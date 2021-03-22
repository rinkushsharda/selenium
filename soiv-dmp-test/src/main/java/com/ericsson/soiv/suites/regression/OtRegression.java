package com.ericsson.soiv.suites.regression;

import com.ericsson.jive.core.execution.ConfigurableJiveSuite;
import com.ericsson.jive.core.frameworkconfiguration.JiveSuiteConfiguration;

public class OtRegression extends ConfigurableJiveSuite {
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
                return null;
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
            public String getJiveTestCaseTagInclude() {
                return "OT-Regression";
            }

            @Override
            public String getJiveTestCaseTagExclude() {
                return "PENDING-Update";
            }

            @Override
            public String getSuiteName() {
                return "OtRegression";
            }
        };
    }
}
