package com.ericsson.soiv.utils;

import static com.ericsson.soiv.utils.HelperClass.getRandomNumberWithinRange;

import java.math.BigInteger;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.junit.Assert;

import com.ericsson.jive.core.execution.Jive;

public class HelperClass {
    public static int getNumberOfMatchingStringCount(String searchString, String wholeString) {
        Pattern pattern = Pattern.compile(searchString);
        Matcher matcher = pattern.matcher(wholeString);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static void toCheckCommunityId(JSONArray communityInformationCurrent) {
        int getCommunityLength = communityInformationCurrent.length();
        for (int i = 0; i < getCommunityLength; i++) {
            if (getCommunityLength == 2) {
                switch (i) {
                case 0:
                    Assert.assertTrue("{\"communityID\":3}".contains(communityInformationCurrent.get(i).toString()));
                    Jive.log("OK : community id 3 is at element 0");
                    break;
                case 1:
                    Assert.assertTrue("{\"communityID\":1}".contains(communityInformationCurrent.get(i).toString()));
                    Jive.log("OK : community id 1 is at element 1");
                    break;
                }
            } else if (getCommunityLength == 3) {
                switch (i) {
                case 0:
                    Assert.assertTrue("{\"communityID\":3}".contains(communityInformationCurrent.get(i).toString()));
                    Jive.log("OK : community id 3 is at element 0");
                    break;
                case 1:
                    Assert.assertTrue("{\"communityID\":2}".contains(communityInformationCurrent.get(i).toString()));
                    Jive.log("OK : community id 2 is at element 1");
                    break;
                case 2:
                    Assert.assertTrue("{\"communityID\":1}".contains(communityInformationCurrent.get(i).toString()));
                    Jive.log("OK : community id 1 is at element 2");
                    break;
                }
            } else {
                Jive.fail("FAILED : community id's are not fine need to check the community ID's order in GUI");
            }
        }
    }

    public static int getRandomNumberWithinRange(int min, int max) {
        Random rand = new Random(); // NOSONAR
        return rand.nextInt((max - min) + 1) + min;
    }

    public static BigInteger generateRandomBigIntegerFromRange(String mini, String maxi) {
        BigInteger bigInteger = new BigInteger(maxi);// uper limit
        BigInteger min = new BigInteger(mini);// lower limit
        BigInteger bigInteger1 = bigInteger.subtract(min);
        Random random = new Random();
        int maxNumBitLength = bigInteger.bitLength();

        BigInteger aRandomBigInt;

        aRandomBigInt = new BigInteger(maxNumBitLength, random);
        if (aRandomBigInt.compareTo(min) < 0)
        {
            aRandomBigInt = aRandomBigInt.add(min);
        }
        if (aRandomBigInt.compareTo(bigInteger) >= 0)
        {
            aRandomBigInt = aRandomBigInt.mod(bigInteger1).add(min);
        }

        return aRandomBigInt;
    }

    public static String getRandomIP(String originalIP)
    {
    Integer randomNumber = getRandomNumberWithinRange(101, 254);
    StringBuilder randomIP = new StringBuilder(originalIP);

    randomIP.replace(originalIP.length() - 11, originalIP.length() - 8, randomNumber.toString());
    randomNumber = getRandomNumberWithinRange(101, 254);
    randomIP.replace(originalIP.length() - 7, originalIP.toString().length() - 4, randomNumber.toString());
    randomNumber = getRandomNumberWithinRange(101, 254);
    randomIP.replace(originalIP.length() - 3, originalIP.length(), randomNumber.toString());
    
    return randomIP.toString();
    }
    
    
    
    public static String iptoHexaDecimal(String ip) {
        String hexValue = ip;
        StringBuilder sb = new StringBuilder();

        String[] v = hexValue.split("\\.");

        for (int i = 0; i < v.length; i++) {

            Integer j = Integer.parseInt(v[i]);
            if (j <= 15) {
                sb = sb.append("0");
                String f = Integer.toHexString(j);
                sb = sb.append(f);
            } else {
                String f = Integer.toHexString(j);
                sb = sb.append(f);
            }

        }
        return sb.toString().toUpperCase();
    }

}
