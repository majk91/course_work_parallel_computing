package com.course.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MenuUtil {

    public static int getHandlerSize(){
        System.out.println("Print count of treads: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String size = in.readLine();
            return Integer.parseInt(size);
        } catch (Exception e) {
            System.out.println("ERROR! "+e.getMessage());
            return getHandlerSize();
        }
    }

    public static String getSearchPhrase() {
        System.out.println("Print search phrase: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println("ERROR! "+e.getMessage());
            return getSearchPhrase();
        }
    }
}
