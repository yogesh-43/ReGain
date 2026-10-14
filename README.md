# ReGain / FocusFlow — Offline Android Focus Timer & App Blocker

An **open-source, local-first Android study assistant** built with **Kotlin and Jetpack Compose**. The GitHub repository is named **ReGain**, while the current Android application label and package are **FocusFlow** (`com.focusflow.app`). It is an independent project, not the proprietary Regain app.

> **Project status: early prototype; compilation and real-device blocking have not been verified.** Android can restrict what accessibility-based blockers do. This app cannot forcibly stop arbitrary apps or make itself impossible to uninstall.

## What it does

The main workflow is: choose apps to restrict → configure a focus session → optionally enable Strict Mode → start a timer → when one of the chosen apps opens, an Accessibility Service attempts to send you Home and display a restriction screen. History and settings are saved locally.

| Feature | Present in source | What actually happens / limitations |
| --- | --- | --- |
| Focus countdown | Yes | Choose 5–180 minutes in the UI; the deadline is stored using Android elapsed realtime. |
| Study subject | Yes | Enter free-text subject for each session. |
| Pause/resume | **No** | Running sessions cannot be paused. |
| Strict Mode | Yes, in app | Hides the early-end control. Does **not** prevent disabling permissions, clearing data or uninstalling. |
| Pick blocked apps | Yes | Lists launcher apps and saves selected package names locally. Android package visibility may affect the list. |
| Open-app interception | Best effort | Accessibility event triggers Home action and attempts to launch `BlockedActivity`. Some Android versions block background activity starts. |
| Restricted screen | Yes | Shows an explanation and remaining time; may not launch on all devices. |
| Foreground timer service | Yes | Ongoing notification and a coroutine to watch the deadline; not a guarantee against process termination. |
| Local session history | Basic | Completed/expired sessions appended to JSON stored in SharedPreferences (max 500). |
| Study stats | Basic | Total completed sessions, today's minutes and approximate UsageStats foreground time. |
| Goal setting | Basic | Daily-minute target saved; no goal alert/advanced charts. |
| Daily reminder | Basic | One daily AlarmManager reminder, with reboot rescheduling. Notification permission may be required. |
| Full Pomodoro cycles | **No** | Manual countdown only; no automated breaks. |
| Focus music/sounds | **No** | Not in the GitHub source. |
| Website filtering / Shorts blocking | **No** | Use the separate StopScroll repository for a prototype short-video feed detector. |
| Multiplayer / cloud / account | **No, by design** | Runs locally on one device. |
| Force-stop background apps / true kiosk lock | **No** | Not possible for a normal third-party app with these permissions. |

## Android permissions and services

- **Accessibility Service** (`FocusAccessibilityService`): manually enabled under Android's Accessibility settings; receives window-change events and checks the foreground app package against your block list. Only active during a saved focus session. This is a sensitive permission; review the source before enabling it.
- **Usage Access** (`PACKAGE_USAGE_STATS`): user grants special access in Android settings to display approximate app-usage time; does not enable app blocking.
- **Foreground service** (`FocusNotificationService`): supports active-session notification and deadline monitoring using `foregroundServiceType="specialUse"`. Google Play may require justification/review for this service type.
- **Notifications** (`POST_NOTIFICATIONS`): declared for Android 13+; the prototype does not include a dedicated runtime notification permission prompt.
- **Alarm / reboot**: `FocusAlarmReceiver` schedules reminders; `FocusBootReceiver` reschedules them after boot. Session enforcement itself is **not guaranteed after a reboot**.

### Focus-state flow

```text
MainActivity -> FocusScreen -> FocusState.begin()
                         -> FocusNotificationService
Selected package opens -> FocusAccessibilityService
                       -> verify FocusState.active() + package allow/block list
                       -> GLOBAL_ACTION_HOME
                       -> attempt BlockedActivity
Timer expires -> FocusState.clearExpiredAndLog()
              -> StudyStore.save() -> StatsScreen
```

Deadline logic uses `SystemClock.elapsedRealtime()`, plus a stored boot/wall-clock offset to avoid treating a stale deadline as active after a reboot. Time is **not** a security boundary; users retain ultimate control of their phones.

## Project structure

```text
app/src/main/
  AndroidManifest.xml
  java/com/focusflow/app/
    MainActivity.kt                 Compose screens: Focus, Block, Stats, Settings
    FocusState.kt                   Session deadline, Strict Mode, blocked package preferences
    FocusAccessibilityService.kt    Foreground-app detection and redirect attempt
    FocusNotificationService.kt     Foreground notification and timer monitoring
    BlockedActivity.kt              Restricted-app message UI
    InstalledApps.kt                Launcher-app discovery
    StudyStore.kt                   Local JSON session history and duration format
    FocusAlarmReceiver.kt           Daily reminder + boot receiver
  res/xml/focus_accessibility.xml   Accessibility service configuration
  res/values/strings.xml            App name and service description
app/src/test/
  java/com/focusflow/app/DurationTest.kt
```

## Build / install

1. Install **Android Studio**, **JDK 17**, **Android SDK 35**, and an emulator or Android 8.0+ device.
2. Clone the repository: `git clone https://github.com/yogesh-43/ReGain.git`.
3. Open the cloned directory in Android Studio, set **Gradle JDK = 17**, and sync dependencies.
4. Run the **app** configuration. The repository does **not** currently include a Gradle wrapper; if using the terminal, install a compatible Gradle distribution or generate a wrapper.
5. In **Settings > Accessibility**, enable the FocusFlow service. In Android Usage Access settings, grant access separately if you want screen-time statistics. Notification permission may need granting through system settings.
6. On the **Block** tab, select apps; on **Focus**, set the session and tap **Start focus**.
7. Verify interception on a spare/test device before relying on it.

The project targets SDK 35 and uses Android Gradle Plugin 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01. The build has not been verified in this environment, and configuration/code fixes may be necessary.

## Testing

The current `DurationTest` is a unit test for `formatDuration()`. It is **not** an end-to-end timer or accessibility test. Recommended manual checks: start/expire/early-end, Strict Mode, repeated app switching, notifications, screen locked, battery-optimization scenarios, reboot, accessibility disabled, and the behavior on Android 12–15.

## Privacy, safety and limitations

- No account, backend or internet permission is declared. Session history, preferences and app selections remain on this phone.
- Accessibility access is sensitive; this code only checks the package identifier of window-change events to determine whether to redirect.
- App blocking is **best effort**. It cannot force-stop apps, guarantee showing an overlay, prevent use of Android settings, prevent uninstall, or enforce device-owner/kiosk restrictions.
- This project's version is an educational/open-source prototype, not a security boundary or child-safety parental-control product.
- Do not assume Google Play eligibility without validating Accessibility and foreground-service policy requirements.

## Contributing

Issues and pull requests are welcome. Particularly helpful: fixing build/test issues, adding device-integration tests, implementing reliable offline data persistence, runtime permission flows, notification robustness, accessible UI, pause/Pomodoro support, and policy-compliant blocking improvements.

## License

[MIT](LICENSE).
