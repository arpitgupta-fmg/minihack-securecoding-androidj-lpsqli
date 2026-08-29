# Android Secure-Coding Minihack — Cheat Sheet

Playbook + fixes from the MomoFactory challenge, kept for future rounds.

---

## 1. Round workflow (lab)
1. Open Android Studio → **File > Open** the `MomoFactory` folder (not the parent).
2. Wait for Gradle sync.
3. **Device Manager** → boot the **Pixel 7** AVD; confirm `adb devices`.
4. Apply the fixes (below) in the **lab copy**.
5. Terminal: `run tests` → aim for full pass. Re-run as often as you like.
6. Submit via the lab's Submit button (final score = last run).

---

## 2. The four recurring vulnerability classes

### A. SQL Injection (login & signup) — the headline bug
**Never build SQL by concatenating user input.** Bind it as parameters.

Login — `DatabaseHelper.checkLogin()`:
```java
// VULNERABLE:
String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
Cursor c = db.rawQuery(sql, null);

// FIXED:
String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
Cursor c = db.rawQuery(sql, new String[]{username, password});
```
Test payloads that must NOT log in: `' OR '1'='1' --`, `admin' --`, `' OR 1=1 --`.

Signup — `DatabaseHelper.addUser()` (injection here can set `isPro=1` = free Pro):
```java
// VULNERABLE:
String sql = "INSERT INTO users(username,password,address) VALUES ('" + u + "','" + p + "','" + a + "')";
db.execSQL(sql);

// FIXED:
ContentValues v = new ContentValues();
v.put("username", u); v.put("password", p); v.put("address", a);
long rowId = db.insertOrThrow("users", null, v);
return rowId != -1;
```
Rule of thumb: `rawQuery(sql, args)`, `ContentValues` + `insert/insertOrThrow`,
`SQLiteStatement.bindString`, or `query(...)` with `selectionArgs`. Never `execSQL`
/ `rawQuery` with a string that contains user input.

### B. Exported components
Every non-launcher `<activity>` (also services/receivers/providers) should be
`android:exported="false"`. Only the launcher activity stays `true`.
```xml
<activity android:name=".view.MainActivity"  android:exported="true"> ...launcher... </activity>
<activity android:name=".view.AdminActivity" android:exported="false" />
```
Why it matters: an exported `AdminActivity` can be launched by any other app
(`adb shell am start -n com.momo.factory/.view.AdminActivity`).

### C. Over-privileged manifest
Request **only** permissions the app actually uses. A delivery app ≈ internet +
location. Remove dangerous/unused ones:
```
REMOVE: READ_CONTACTS, RECORD_AUDIO, READ/WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE, (CAMERA)
KEEP:   INTERNET, ACCESS_NETWORK_STATE, ACCESS_COARSE_LOCATION
```

### D. Common bonus hardening (often rewarded)
- `android:allowBackup="false"` — stop `adb backup` from pulling app data.
- `android:usesCleartextTraffic="false"` — force HTTPS.
- Don't store passwords in plaintext or **Base64** (Base64 is encoding, NOT
  encryption). Use a real password hash (e.g. bcrypt/scrypt/Argon2) if asked.
- Don't `Log.d(...)` SQL queries or credentials (info leak via logcat).

---

## 3. Local practice rig (this Mac)
Everything to reproduce the lab is set up:
- **Android Studio** + SDK 34, Pixel 7 **arm64** emulator (Apple Silicon).
- `run-tests.sh` — local clone of the grader (runs `connectedAndroidTest`,
  parses `app/build/outputs/androidTest-results/connected/*.xml`).
- `~/bin/run` shim → type **`run tests`** just like the lab.
- Equivalent instrumented tests: `app/src/androidTest/java/com/momo/factory/SecurityInstrumentedTest.java`.

Run locally:
```bash
# boot an emulator first (Device Manager ▶), then:
adb devices
run tests            # or: bash run-tests.sh
```

Sanity-check the harness catches regressions:
```
Set AdminActivity exported="true" -> run tests -> 4/5 (fails)
Restore exported="false"          -> run tests -> 5/5
```

Note: local test *assertions* approximate the lab's hidden ones; the **flow and
vulnerability classes** are identical, which is what matters for practice.

---

## 4. Quick grep audit (spot bugs fast)
```bash
# concatenated SQL:
grep -rn "rawQuery\|execSQL" app/src/main/java | grep -v "?"
# exported activities:
grep -n 'android:exported="true"' app/src/main/AndroidManifest.xml
# permissions:
grep -o 'android.permission.[A-Z_]*' app/src/main/AndroidManifest.xml
# leaked secrets/logs:
grep -rn "Log\.\|Base64\|password" app/src/main/java
```
