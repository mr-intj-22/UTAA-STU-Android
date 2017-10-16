package com.msl.utaastu.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.FrameLayout;

import com.msl.utaastu.R;
import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

import java.util.List;

/**
 * Created by jrvansuita on 17/02/17.
 */

public class AboutHelper {

    private Activity activity;

    private AboutHelper(Activity activity) {
        this.activity = activity;
    }

    public static AboutHelper with(Activity activity) {
        return new AboutHelper(activity);
    }

    public AboutHelper init() {
        activity.setTheme(R.style.AboutTheme);

        return this;
    }

    public void loadAbout() {
        FrameLayout flHolder = activity.findViewById(R.id.about);

        AboutBuilder builder = AboutBuilder.with(activity)
                .setAppIcon(R.mipmap.ic_circle)
                .setAppName(R.string.app_name)
                .setPhoto(R.mipmap.mslprofile)
                .setCover(R.mipmap.mslcover)
                .setLinksAnimated(true)
                .setDividerDashGap(13)
                .setName(R.string.full_name)
                .setSubTitle(R.string.subtitle)
                .setLinksColumnsCount(2)
                .setBrief(R.string.brief)
                .addLink(R.mipmap.facebook, R.string.facebook, openFacebookInPage(activity.getString(R.string.facebook_id)))
                .addLink(R.mipmap.linkedin, R.string.linkedin, openLinkedInPage(activity.getString(R.string.linkedin_id)))
                .addLink(R.mipmap.twitter, R.string.twitter, openTwitterInPage(activity.getString(R.string.twitter_id)))
                .addInstagramLink(R.string.instagram_id)
                .addGitHubLink(R.string.github_id)
                .addGooglePlayStoreLink(R.string.playstore_id)
                .addWhatsappLink(R.string.full_name, R.string.whatsapp)
                .addEmailLink(R.string.feedback_email)
                .addFiveStarsAction()
                .addMoreFromMeAction(R.string.playstore_publisher)
                .setVersionNameAsAppSubTitle()
                .setActionsColumnsCount(2)
                .addShareAction(R.string.app_name)
                .addFeedbackAction(R.string.feedback_email)
                .setWrapScrollView(true)
                .setShowAsCard(false);


        AboutView view = builder.build();

        flHolder.addView(view);
    }

    private Intent openLinkedInPage(String linkedId) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://add/%@" + linkedId));
        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.isEmpty()) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.linkedin.com/profile/view?id=" + linkedId));
        }
        return intent;
    }

    private Intent openFacebookInPage(String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = activity.getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    private Intent openTwitterInPage(String username) {
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + username));
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + username));
        }
        return intent;
    }
}