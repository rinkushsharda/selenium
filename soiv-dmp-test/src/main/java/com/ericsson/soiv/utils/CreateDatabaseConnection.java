package com.ericsson.soiv.utils;

import com.ericsson.jive.core.execution.Jive;
import com.ericsson.soiv.fixtures.SoivFixture;

import java.sql.*;
import java.util.Arrays;

import static com.ericsson.jive.core.execution.Jive.getFixture;

// Created by ZMUKMAN

public class CreateDatabaseConnection {

    private static Statement statement = null;
    private static Connection connection = null;

    public static String runQuery(String query) throws SQLException {

        try {
            connection = DriverManager.getConnection(
                "jdbc:oracle:thin:"+
                        (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("bscsadm-username"))+
                        "/"+
                        (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("bscsadm-password"))+
                        "\"@"+
                        (((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("bscsadm-host"))
                        +":"+(((SoivFixture) getFixture()).getConfigurationPropertyFailIfNotSet("bscsadm-port"))
                        +":bscsdb");
            
            statement = connection.createStatement();
            Jive.log("INFO : Database Connection Is created Successfully!");
        }
        catch (SQLException e){
            Jive.fail("FAILED : Database Exception : Connection Is not created "+e.getErrorCode()
                    + " \n "+ Arrays.toString(e.getStackTrace()));
        }

        return executeQueryStatement(query);
    }

    private static String executeQueryStatement(String query) throws SQLException {
        String output = "";
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery(query);
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnNumber = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnNumber; i++) {
                    if (i > 1) {
                        Jive.log(",  ");
                    }
                    output = resultSet.getString(i);
                }
                Jive.log("INFO : Query : " +query +" : is fetched Successfully from the database and Output : " + output);
                assert output != null;
                return output;
            }

        }catch (SQLException e){
            Jive.log("Exception in SQL " +e);
            Jive.fail("FAILED : Exception in Database query");
        }
        finally {
            assert resultSet != null;
            resultSet.close();
            assert statement != null;
            statement.close();
            assert connection != null;
            connection.close();
            Jive.log("INFO : Database Connection is Going to Closed");
        }
        assert output != null;
        return output;
    }
}




