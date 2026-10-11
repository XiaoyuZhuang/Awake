<h1 align="center">⏱️ Awake</h1>

<p align="center"><strong>One tap. A clear boundary for the time ahead.</strong></p>
<p align="center">A lightweight Android countdown timer · Tap to add time · Ongoing notification · Repeating reminders</p>

<p align="center">
  <a href="https://github.com/XiaoyuZhuang/Awake/releases/latest"><img src="https://img.shields.io/github/v/release/XiaoyuZhuang/Awake?label=Release&amp;logo=github&amp;color=168C82" alt="Latest release"></a>
  <a href="https://github.com/XiaoyuZhuang/Awake/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/XiaoyuZhuang/Awake/build.yml?branch=main&amp;label=Build&amp;logo=githubactions" alt="GitHub Actions build"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-168C82?logo=android&amp;logoColor=white" alt="Android 8.0 or later">
  <a href="https://github.com/XiaoyuZhuang/Awake/releases"><img src="https://img.shields.io/github/downloads/XiaoyuZhuang/Awake/total?label=Downloads&amp;logo=github" alt="Release downloads"></a>
</p>

<p align="center">
  <a href="https://github.com/XiaoyuZhuang/Awake/releases/latest/download/Awake-release.apk">📦 Download APK</a>
  · <a href="https://github.com/XiaoyuZhuang/Awake/releases/latest">🚀 Latest release</a>
  · <a href="README.md">🌐 简体中文</a>
  · <a href="https://github.com/XiaoyuZhuang/Awake/issues">💬 Report an issue</a>
</p>

---

**Awake has one simple core action: each tap adds a block of time to the current countdown—10 minutes by default.** Use the “Awake +1” home screen shortcut to add time without opening an app screen. The ongoing notification shows the remaining time. When time runs out, reminders repeat every 10 seconds by default until you add time or stop.

[Why I built it](#why-i-built-it) · [Getting started](#getting-started) · [Features and settings](#features-and-settings) · [FAQ](#faq) · [Build and release](#build-and-release)

## Why I built it

### From “How long have I been doing this?” to “How long do I intend to do it?”

We often watch videos or study and, without noticing, keep doing it for far longer than intended. When absorbed in something, it is easy to lose track of time, forget to drink water, and forget to rest or look after ourselves. Our health matters most. We should remember things like drinking water and leave ourselves a little time to pause and take care of our bodies.

I wanted an experience similar to a Pomodoro timer. On Pinduoduo, I saw a timer called “转一圈,” roughly “Turn Once”: turn it once to the desired time, and the countdown starts. That action feels direct and natural. You do not have to go through a whole sequence of steps just to start timing something.

Awake is my attempt to capture that feeling: **one tap adds a 10-minute countdown block; when no countdown is running, or the previous block has finished, one tap gives you the next 10 minutes.** You always have a “10-minute plan” in mind, knowing what you intend to do next and how long you intend to do it.

For example, when I want to study, I tap three times to give myself about 30 minutes. When study time ends, I tap once more for a 10-minute break. When I want to continue studying, I arrange another block of time. Keeping a countdown going helps me avoid becoming so absorbed in something that I forget how long I have been doing it.

I thought this was a very good idea, so I made this app. I wanted a sense of time that is easy to start and easy to continue: before doing something, give that period a simple, clear plan.

> If a countdown is still running, another tap adds 10 minutes to the remaining time; it does not reset the remaining time to 10 minutes. You can also change the amount added per tap.

### Make starting a countdown something you can do casually

Starting a timer manually can involve many steps: open the tool, choose a duration, confirm it, and then start. Everyone knows what a countdown is and knows it can be useful. But when the steps are too complicated, people may not feel like starting one, or the process may feel less elegant than they expected.

I wanted our app to be easier: press a button, choose a time, and bind the timer to an entry point you can trigger casually. In Awake, choose how much time each tap adds, then put the add-time shortcut on your home screen. After that, one tap starts or extends the countdown, without setting the duration again every time.

### Keep it extremely lightweight—and pleasant to use

The app must be lightweight and must start a countdown quickly. Timing something is simple; the action of starting it should be simple enough that you want to use it repeatedly.

My hope is that, with this app, starting a countdown becomes a small pleasure in itself: a good feeling, something satisfying. Tap once, arrange that block of time, and then start doing what you intended to do. That is the feeling I had in mind.

**Making “start a countdown” a comfortable, direct little action that you actually want to take is why Awake exists.**

## Getting started

### 1. Install

Download `Awake-release.apk` from the [📦 latest release](https://github.com/XiaoyuZhuang/Awake/releases/latest) and install it. **Android 8.0 or later** is required.

On a fresh installation, tapping the app icon adds the default 10-minute countdown and opens settings. Allow notification permission so the remaining time and settings entry can appear in the notification shade.

### 2. Configure once, then tap casually

Choose “Minutes added per launch” in settings; the default is 10 minutes. We recommend selecting **“Add home screen shortcut”** and confirming the system prompt to place **“Awake +1”** on your home screen.

Tapping that shortcut adds time without opening settings. A short message appears at the bottom of the screen, and the ongoing notification updates the remaining time. No separate temporary added-time notification is posted.

The original app icon follows the selected entry mode:

| Entry point | Behavior |
| --- | --- |
| “Awake +1” home screen shortcut | Add time without opening settings |
| App icon · Open settings after every launch | Add time, then open settings; the default on a fresh installation |
| App icon · Open from notification only | Add time without opening settings |
| Ongoing notification body | Open settings without adding time |
| “Add time” / “Stop” notification actions | Execute the corresponding action directly |

### 3. Give the current activity a block of time

With the default 10 minutes per tap:

| What I intend to do | Action | Plan |
| --- | --- | --- |
| Watch some videos | Tap once | Set aside 10 minutes, then get a reminder to pause |
| Study for a while | Tap three times | Starting from stopped, arrange about 30 minutes |
| Take a break after studying | Tap once after time runs out | Start the next 10-minute break |
| Stop timing for now | Select “Stop timer” | Stop the countdown and repeating reminders |

Settings also shows the live remaining time at the top, with **“Add time”** and **“Stop timer”** side by side underneath for immediate access.

When time runs out, reminders repeat every 10 seconds by default. Adding time ends the current reminders and starts a new countdown; stopping ends both the timer and reminders. You decide when to switch between studying and resting by tapping.

## Features and settings

| Setting | Default | Customization |
| --- | --- | --- |
| Time added per launch | 10 minutes | 1–1440 minutes |
| Reminder interval after expiry | 10 seconds | 1–3600 seconds |
| Vibration | On | Feedback when adding time and when reminders fire |
| Sound | On | Sound for reminders after expiry |
| Reminder notification | On | Whether to show the notification after the countdown ends |
| Settings entry mode | Open settings after every launch | Every launch / notification only |
| Language | Simplified Chinese | 简体中文 / English |
| Theme | Follow system | System / light / pure black |

- **Ongoing countdown notification:** maintained by a local foreground service, with the remaining time and settings, add-time, and stop actions. The reminder-notification switch independently controls the notification after expiry.
- **Compact settings:** a live countdown at the top; add/stop, vibration/sound, and language/theme controls arranged in pairs to reduce scrolling.
- **Home screen shortcut:** uses an activity with no visible window to reduce interruption from app screens and launch animations.
- **Background settings:** notification and system battery settings are accessible from the app.
- **Local state:** the countdown deadline and preferences are stored on the device and read when the service is restored.
- **Update checks:** compare versions first; prompt before opening a newer release, or report that the installed version is up to date.

## FAQ

### Does it need Google services or an internet connection?

Daily countdowns, vibration, sound, and the ongoing notification run locally. **They do not depend on Google Play services or Google push notifications, and do not require an internet connection.** Checking for updates and downloading a new APK require access to GitHub.

### Why does it open settings by default?

New users need an easy way to find the duration, reminder interval, and notification permission. Opening settings after every launch makes initial setup straightforward. Once familiar with the app, choose notification-only access or use the home screen shortcut.

### Will it remind me with the screen locked?

The app runs in the background using a local foreground service and an ongoing notification. Some phones may still restrict or terminate background apps. Allow notifications, enable system autostart where available, and set battery usage to unrestricted. Settings provides “Notifications” and “Background settings” entry points; exact options depend on your phone.

### Will an update erase my settings?

Download `Awake-release.apk` from Releases. Release builds signed with the same key can update an existing installation and preserve settings. Debug and Release builds use different signatures and cannot replace each other.

“Check for updates” reads the latest release version. A newer version prompts before opening the release page; otherwise the app reports that it is up to date. Network failures produce a retry message.

### Does it collect personal information?

No account is required, and no personal information is collected. Countdown state and preferences stay on the device. The app contacts GitHub only when you actively check for updates.

## Build and release

The project uses Kotlin, the Android SDK, and GitHub Actions. Its build configuration is **minimum Android 8.0 (API 26)**, **compile/target API 35**, **JDK 17**, and **Gradle 8.9**.

Push to `main` or manually run the [Build APK workflow](https://github.com/XiaoyuZhuang/Awake/actions/workflows/build.yml) to compile Debug, run Lint, and perform Android 15 UI verification. Build artifacts can be downloaded from Actions. UI verification covers desktop launches, notification access, the live countdown, add/stop actions, the pinned shortcut, and local operation with Google Play services disabled. It also saves screenshots. Documentation-only commits can use `[skip ci]` to skip builds.

Configure these Repository Secrets under **Settings → Secrets and variables → Actions** to also build a signed Release APK and automatically publish it to [Releases](https://github.com/XiaoyuZhuang/Awake/releases):

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | Complete Base64 content of the signing keystore |
| `KEY_ALIAS` | Signing key alias |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_PASSWORD` | Signing key password |

Keep the versions in `app/build.gradle.kts` and `version.json` synchronized and increment `versionCode` for new versions. Keep the signing key safe; never commit the keystore, passwords, or its Base64 content.

## Feedback

Use [💬 Issues](https://github.com/XiaoyuZhuang/Awake/issues) to report problems or suggest improvements. Include the phone model, Android version, Awake version, and steps to reproduce the issue where possible. Screenshots help with visual problems.

If this little one-tap timer helps you, a ⭐ is welcome.

<p align="center"><a href="README.md">阅读中文版 →</a></p>
