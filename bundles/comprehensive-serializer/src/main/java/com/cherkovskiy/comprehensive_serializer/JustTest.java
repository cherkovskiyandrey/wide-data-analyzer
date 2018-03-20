package com.cherkovskiy.comprehensive_serializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JustTest {


    public static void main(String[] args) {


        Pattern pattern = Pattern.compile("cls=>([^,]+),accessType=>(PUBLIC|PRIVATE)");

        String str = "cls=>Object,accessType=>PUBLIC," +
                "cls=>Class,accessType=>PRIVATE,";

        final Matcher matcher = pattern.matcher(str);

        int start = 0;
        int end = 0;
        while(matcher.find()) {
            start = matcher.start();
            if(end != 0 && (str.charAt(end) != ',' || start != end + 1)) {
                throw new IllegalArgumentException("");
            }
            end = matcher.end();

            System.out.println(matcher.group(1) + " --->>>> " + matcher.group(2));
        }


    }
}
