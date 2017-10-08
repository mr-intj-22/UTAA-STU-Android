package com.msl.utaastu.Utils;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by Malek Shefat on 9/5/2017.
 * All rights preserved.
 */

public class Intents {

    public static Intent sendEmail(String email, String subject, String message) {
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra("android.intent.extra.EMAIL", new String[]{email});
        intent.putExtra("android.intent.extra.SUBJECT", subject);
        intent.putExtra("android.intent.extra.TEXT", message);
        return intent;
    }

    public static Intent shareLink(String subject, String message) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.SUBJECT", subject);
        intent.putExtra("android.intent.extra.TEXT", message);
        return intent;
    }

}
