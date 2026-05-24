package com.focusfarm.app.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Known social / entertainment app package names for the Block Apps shortlist.
 * Teammates can add more package IDs here as needed.
 */
public final class SocialMediaAppFilter {

    private static final Set<String> SOCIAL_MEDIA_PACKAGES = new HashSet<>(Arrays.asList(
            "com.instagram.android",
            "com.instagram.lite",
            "com.instagram.barcelona",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.ss.android.ugc.aweme",
            "com.twitter.android",
            "com.facebook.katana",
            "com.facebook.lite",
            "com.facebook.orca",
            "com.snapchat.android",
            "com.reddit.frontpage",
            "com.reddit.frontpage.beta",
            "com.pinterest",
            "com.linkedin.android",
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music",
            "com.whatsapp",
            "com.discord",
            "tv.twitch.android.app",
            "com.tumblr",
            "org.telegram.messenger",
            "org.telegram.messenger.web",
            "com.viber.voip",
            "com.tencent.mm",
            "com.tencent.mobileqq",
            "com.bereal.ft",
            "com.clubhouse.app",
            "com.spotify.music",
            "com.netflix.mediaclient"
    ));

    private SocialMediaAppFilter() {
    }

    public static boolean isSocialMediaApp(String packageName) {
        return packageName != null && SOCIAL_MEDIA_PACKAGES.contains(packageName);
    }

    public static Set<String> getKnownPackages() {
        return new HashSet<>(SOCIAL_MEDIA_PACKAGES);
    }
}
