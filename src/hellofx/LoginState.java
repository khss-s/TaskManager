package hellofx;

import java.util.prefs.Preferences;

public class LoginState {
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_LOGGED_IN = "loggedIn";
    private static final String KEY_USER_ID = "userId";

    public static void saveLoginState(boolean loggedIn) {
        Preferences prefs = Preferences.userRoot().node(PREFS_NAME);
        prefs.putBoolean(KEY_LOGGED_IN, loggedIn);
    }

    public static boolean getLoginState() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NAME);
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public static void saveUserId(int userId) {
        Preferences prefs = Preferences.userRoot().node(PREFS_NAME);
        prefs.putInt(KEY_USER_ID, userId);
    }

    public static int getUserId() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NAME);
        return prefs.getInt(KEY_USER_ID, -1);
    }
}