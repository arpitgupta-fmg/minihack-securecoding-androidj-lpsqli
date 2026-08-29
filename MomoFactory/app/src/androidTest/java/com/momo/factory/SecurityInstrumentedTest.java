package com.momo.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.momo.factory.controller.AuthController;
import com.momo.factory.model.DatabaseHelper;
import com.momo.factory.model.User;
import com.momo.factory.view.AdminActivity;
import com.momo.factory.view.DashboardActivity;
import com.momo.factory.view.LoginActivity;
import com.momo.factory.view.MainActivity;
import com.momo.factory.view.SignupActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Local equivalent of the lab's hidden grader tests. Each test asserts that one
 * of the four vulnerability classes from the challenge is fixed. Runs on a
 * connected emulator/device via `connectedAndroidTest`.
 */
@RunWith(AndroidJUnit4.class)
public class SecurityInstrumentedTest {

    private Context ctx() {
        return ApplicationProvider.getApplicationContext();
    }

    // ---- 1. SQL injection in login must NOT bypass authentication ----
    @Test
    public void loginSqlInjectionIsBlocked() {
        AuthController auth = new AuthController(ctx());
        String[] payloads = {
                "' OR '1'='1' --",
                "' OR '1'='1",
                "admin' --",
                "' OR 1=1 --"
        };
        for (String payload : payloads) {
            User u = auth.loginUser(payload, "anything");
            assertEquals("SQL injection payload logged in: " + payload, null, u);
        }
    }

    // ---- positive control: a normally registered user CAN log in ----
    @Test
    public void legitimateLoginStillWorks() {
        AuthController auth = new AuthController(ctx());
        String username = "user_" + System.currentTimeMillis();
        String password = "secret123";

        assertTrue("registration failed",
                auth.registerUser(username, password, "1 Test Street"));

        User u = auth.loginUser(username, password);
        assertNotNull("valid credentials were rejected", u);
        assertEquals(username, u.getUsername());
        assertFalse("new user must not be Pro", u.isProUser());
    }

    // ---- 2. Signup SQL injection must NOT grant Pro membership ----
    @Test
    public void signupCannotEscalateToPro() {
        int proBefore = countProUsers();

        AuthController auth = new AuthController(ctx());
        // Classic attempt to close the VALUES list early and force isPro = 1.
        String malicious = "hax_" + System.currentTimeMillis() + "', 'p', 'a', 1) --";
        auth.registerUser(malicious, "pw12", "addr");

        int proAfter = countProUsers();
        assertEquals("signup was able to create/inject a Pro user", proBefore, proAfter);
    }

    private int countProUsers() {
        DatabaseHelper helper = new DatabaseHelper(ctx());
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM users WHERE isPro = 1", null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        db.close();
        return count;
    }

    // ---- 3. Internal activities must not be exported ----
    @Test
    public void internalActivitiesAreNotExported() {
        assertFalse("AdminActivity is exported", isExported(AdminActivity.class));
        assertFalse("LoginActivity is exported", isExported(LoginActivity.class));
        assertFalse("SignupActivity is exported", isExported(SignupActivity.class));
        assertFalse("DashboardActivity is exported", isExported(DashboardActivity.class));
        // The launcher entry point is allowed to be exported.
        assertTrue("MainActivity should stay exported (launcher)", isExported(MainActivity.class));
    }

    private boolean isExported(Class<?> activity) {
        try {
            ActivityInfo info = ctx().getPackageManager()
                    .getActivityInfo(new ComponentName(ctx(), activity), 0);
            return info.exported;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // ---- 4. Dangerous / unneeded permissions must be removed ----
    @Test
    public void dangerousPermissionsAreRemoved() {
        Set<String> requested = requestedPermissions();

        String[] mustBeAbsent = {
                "android.permission.READ_CONTACTS",
                "android.permission.RECORD_AUDIO",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_PHONE_STATE"
        };
        for (String perm : mustBeAbsent) {
            assertFalse("unneeded permission still present: " + perm, requested.contains(perm));
        }
        assertTrue("INTERNET permission is required but missing",
                requested.contains("android.permission.INTERNET"));
    }

    private Set<String> requestedPermissions() {
        try {
            PackageInfo pi = ctx().getPackageManager()
                    .getPackageInfo(ctx().getPackageName(), PackageManager.GET_PERMISSIONS);
            if (pi.requestedPermissions == null) {
                return new HashSet<>();
            }
            return new HashSet<>(Arrays.asList(pi.requestedPermissions));
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
