"""Regression: launch through the desktop/app drawer with Settings already alive."""
import re
import subprocess
import time
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

PACKAGE = "com.xiaoyu.awake"
OUT = Path("ui-smoke")
OUT.mkdir(exist_ok=True)

def adb(*args):
    return subprocess.check_output(["adb", *args], text=True)

def shell(command):
    return adb("shell", command)

def wait_for(predicate, message):
    for _ in range(40):
        if predicate():
            return
        time.sleep(0.25)
    raise AssertionError(message)

def resumed():
    return "\n".join(line for line in shell("dumpsys activity activities").splitlines()
                     if re.search(r"(?:mResumedActivity|topResumedActivity)\s*[:=]", line))

def prefs(name="awake_runtime"):
    result = subprocess.run(["adb", "shell", "run-as", PACKAGE, "cat", "shared_prefs/" + name + ".xml"],
                            text=True, capture_output=True)
    if result.returncode != 0:
        return {}
    xml = result.stdout
    return {node.attrib["name"]: node.attrib.get("value", node.text)
            for node in ET.fromstring(xml)}

def screenshot(name):
    with (OUT / (name + ".png")).open("wb") as output:
        subprocess.run(["adb", "exec-out", "screencap", "-p"], stdout=output, check=True)

def nodes():
    try:
        shell("uiautomator dump /sdcard/window.xml")
        return list(ET.fromstring(shell("cat /sdcard/window.xml")).iter("node"))
    except (subprocess.CalledProcessError, ET.ParseError):
        return []

def tap_text(text, exact=False):
    for attempt in range(12):
        for node in nodes():
            label = node.attrib.get("text", "")
            if (label == text if exact else text in label):
                bounds = list(map(int, re.findall(r"\d+", node.attrib["bounds"])))
                if len(bounds) == 4 and bounds[2] > bounds[0] and bounds[3] > bounds[1]:
                    shell("input tap %d %d" % ((bounds[0] + bounds[2]) // 2, (bounds[1] + bounds[3]) // 2))
                    time.sleep(0.5)
                    return
        time.sleep(0.3)
    raise AssertionError("Visible UI option missing: " + text)

def capture_failure(error_type, error, traceback):
    screenshot("failure")
    try:
        (OUT / "failure.xml").write_text(shell("cat /sdcard/window.xml"))
        (OUT / "failure-logcat.txt").write_text(shell("logcat -d -t 400"))
    finally:
        sys.__excepthook__(error_type, error, traceback)

sys.excepthook = capture_failure

size = list(map(int, re.findall(r"(\d+)x(\d+)", shell("wm size"))[-1]))
width, height = size

def home_launch():
    shell("input keyevent HOME")
    time.sleep(2)
    # Open the actual launcher app drawer, then tap Awake rather than using am start.
    shell("input swipe %d %d %d %d 300" % (width // 2, height * 9 // 10, width // 2, height * 3 // 10))
    time.sleep(0.7)
    tap_text("Awake", exact=True)

adb("install", "-r", "dist/Awake-debug.apk")
shell("pm grant " + PACKAGE + " android.permission.POST_NOTIFICATIONS")
time.sleep(5)  # Package install can finish before the cold-boot launcher is ready.
home_launch()
wait_for(lambda: PACKAGE + "/.SettingsActivity" in resumed(),
         "Fresh desktop launch must open settings")
wait_for(lambda: int(prefs().get("deadline", "0")) > int(time.time() * 1000),
         "Fresh desktop launch did not add time")
screenshot("fresh-install-settings")
time.sleep(1)

# Use the actual settings UI. Do not replace preference XML or force-stop the app.
tap_text("每次启动增加")
time.sleep(0.3)
shell("input keyevent 123 67 67")
shell("input text 1")
time.sleep(0.5)
screenshot("editing-minutes")
if "mInputShown=true" in shell("dumpsys input_method"):
    shell("input keyevent BACK")
    time.sleep(0.3)
tap_text("保存", exact=True)
wait_for(lambda: prefs("awake_settings").get("add_minutes") == "1", "Minutes were not saved")

shell("input swipe %d %d %d %d 350" % (width // 2, height * 8 // 10, width // 2, height * 3 // 10))
time.sleep(0.5)
tap_text("进入方式")
time.sleep(0.3)
tap_text("仅通过通知进入", exact=True)
wait_for(lambda: prefs("awake_settings").get("settings_entry") == "notification",
         "Entry selection was not saved by UI")

# Leave SettingsActivity alive in its task. Real desktop clicks must NOT restore it.
first_deadline = int(prefs()["deadline"])
for click in range(3):
    home_launch()
    wait_for(lambda: int(prefs()["deadline"]) - first_deadline >= (click + 1) * 59000,
             "Desktop launch did not add the configured increment")
    time.sleep(0.5)
    assert PACKAGE not in resumed(), "Desktop icon restored settings or left a visible app activity"
delta = int(prefs()["deadline"]) - first_deadline
assert 177000 <= delta <= 183000, "Desktop taps did not add exactly three increments"
notifications = shell("dumpsys notification --noredact")
assert "awake_added_time" not in notifications, "Obsolete temporary notification/channel still exists"
assert "awake_timer" in notifications, "Ongoing timer notification missing"
screenshot("notification-only-home")
print("PASS: UI-saved notification-only mode, live Settings task, actual desktop icon and exactly three increments", flush=True)

# Reopen the still-existing Settings task and confirm its stored selection.
shell("am start -W --activity-reorder-to-front -n " + PACKAGE + "/.MainActivity")
# The isolated launcher above must also remain headless; it adds one more increment.
time.sleep(0.5)
assert PACKAGE not in resumed(), "Direct launcher intent did not stay headless"
# A user enters Settings via the actual ongoing notification.
shell("cmd statusbar expand-notifications")
time.sleep(0.7)
# Tap the countdown body, not the app-name header which expands the notification.
clicked = False
for node in nodes():
    if node.attrib.get("resource-id") == "android:id/text" and re.fullmatch(r"[0-9]+:[0-9]{2}", node.attrib.get("text", "")):
        bounds = list(map(int, re.findall(r"\d+", node.attrib["bounds"])))
        shell("input tap %d %d" % ((bounds[0] + bounds[2]) // 2, (bounds[1] + bounds[3]) // 2))
        clicked = True
        break
assert clicked, "Countdown notification body missing"
wait_for(lambda: PACKAGE + "/.SettingsActivity" in resumed(), "Notification did not open settings")
screenshot("settings-from-notification")

# Settings entry is below the fold on smaller emulator screens.
try:
    tap_text("进入方式")
except AssertionError:
    shell("input swipe %d %d %d %d 350" % (width // 2, height * 8 // 10, width // 2, height * 3 // 10))
    time.sleep(0.4)
    tap_text("进入方式")
time.sleep(0.3)
tap_text("每次启动后进入设置", exact=True)
wait_for(lambda: prefs("awake_settings")["settings_entry"] == "every_launch", "Every-launch selection not saved")
home_launch()
wait_for(lambda: PACKAGE + "/.SettingsActivity" in resumed(), "Every-launch desktop mode did not open settings")
screenshot("every-launch-settings")
print("PASS: actual desktop icon, UI-saved entry preference, live Settings task, three exact increments, ongoing notification only and every-launch mode.")


# Create the pinned shortcut through Settings, then tap its real desktop icon.
for _ in range(5):
    if any("添加桌面加时快捷方式" in n.attrib.get("text", "") for n in nodes()):
        break
    shell("input swipe %d %d %d %d 350" % (width // 2, height * 8 // 10, width // 2, height * 3 // 10))
    time.sleep(0.4)
tap_text("添加桌面加时快捷方式", exact=True)
time.sleep(0.7)
screenshot("pin-shortcut-confirmation")
tap_text("Add to home screen", exact=True)
time.sleep(1)
shell("input keyevent HOME")
time.sleep(1)
screenshot("pinned-shortcut")
shortcut_deadline = int(prefs()["deadline"])
# Restore animations to exercise the no-display entry even with normal launcher settings.
for animation in ("window_animation_scale", "transition_animation_scale", "animator_duration_scale"):
    shell("settings put global " + animation + " 1")
for click in range(3):
    shell("input keyevent HOME")
    time.sleep(0.7)
    tap_text("Awake 加时", exact=True)
    wait_for(lambda: int(prefs()["deadline"]) - shortcut_deadline >= (click + 1) * 59000,
             "Pinned shortcut did not add time")
    assert PACKAGE not in resumed(), "Shortcut opened settings or left an app window"
delta = int(prefs()["deadline"]) - shortcut_deadline
assert 177000 <= delta <= 183000, "Shortcut did not add exactly three increments"
assert prefs("awake_settings")["settings_entry"] == "every_launch"
screenshot("shortcut-headless-with-animations")
print("PASS: UI-created pinned shortcut, three actual desktop taps, headless even in every-launch mode with animations enabled.", flush=True)
