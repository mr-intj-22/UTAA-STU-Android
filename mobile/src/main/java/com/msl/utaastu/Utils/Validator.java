package com.msl.utaastu.Utils;

import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Malek Shefat on 6/14/2017.
 */

public class Validator {

    public static boolean PasswordStrong(String pass) {
        String expression = "^(?=.*\\d)(?=.*[a-zA-Z]).{8,}$";
        Pattern patron = Pattern.compile(expression);
        return patron.matcher(pass).matches();
    }

    public static boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url.toLowerCase());
        return m.matches();
    }

    public static boolean isValidEmail(String email) {
        Pattern p = Patterns.EMAIL_ADDRESS;
        Matcher m = p.matcher(email.toLowerCase());
        return m.matches();
    }
}