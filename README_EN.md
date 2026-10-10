# Awake

[中文说明](README.md) · [Latest signed APK](https://github.com/XiaoyuZhuang/Awake/releases/latest)

Tap the launcher icon to add time (10 minutes by default). Tap the ongoing notification to open settings without adding time. After the countdown ends, reminders repeat every 10 seconds by default.

Settings include minutes per launch (1–1440), reminder interval (1–3600 seconds), independent sound/vibration/notification switches, notification-only or every-launch settings access, Chinese/English, and system/light/pure-black themes.

Check for updates compares the installed version with the latest GitHub Release. A newer version prompts before opening its page; otherwise the app reports that it is up to date.

Install `Awake-release.apk` from Releases. Releases signed with the same key can update an existing installation and preserve settings. Debug and Release builds cannot replace each other.

Pushes to main run compilation and Lint. With KEYSTORE_BASE64, KEY_ALIAS, KEYSTORE_PASSWORD and KEY_PASSWORD repository secrets configured, Actions builds and publishes a signed APK. Keep app/build.gradle.kts and version.json in sync and increment versionCode for new versions.

No account or personal data collection. Only update checks contact GitHub. Device battery restrictions may delay background reminders.
