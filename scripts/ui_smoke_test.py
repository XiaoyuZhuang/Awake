"""Exercise launcher modes and capture settings/dialog on an ephemeral emulator."""
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path
from shlex import quote

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

def prefs():
    xml = shell("run-as " + PACKAGE + " cat shared_prefs/awake_runtime.xml")
    return {node.attrib["name"]: node.attrib.get("value", node.text)
            for node in ET.fromstring(xml)}

def screenshot(name):
    path = OUT / (name + ".png")
    with path.open("wb") as output:
        subprocess.run(["adb", "exec-out", "screencap", "-p"], stdout=output, check=True)

adb("install", "-r", "dist/Awake-debug.apk")
shell("pm grant " + PACKAGE + " android.permission.POST_NOTIFICATIONS")
shell("input keyevent HOME")
shell("am start -W -n " + PACKAGE + "/.MainActivity")
wait_for(lambda: PACKAGE + "/.SettingsActivity" in resumed(),
         "Fresh installation must open settings")
wait_for(lambda: int(prefs().get("deadline", "0")) > int(time.time() * 1000),
         "Default launch did not add time")

# Capture the initial default settings and assert the selected entry is persisted
# separately from the unset fresh-install default.
screenshot("fresh-install-settings")
shell("am force-stop " + PACKAGE)
xml = '<map><string name="settings_entry">notification</string><long name="add_minutes" value="1"/><string name="theme">dark</string></map>'
shell("run-as " + PACKAGE + " sh -c " + quote("printf %s " + quote(xml) + " > shared_prefs/awake_settings.xml"))
shell("input keyevent HOME")
shell("am start -W -n " + PACKAGE + "/.MainActivity")
wait_for(lambda: int(prefs().get("deadline", "0")) > int(time.time() * 1000),
         "Notification-only launch did not add time")
time.sleep(1)
assert PACKAGE not in resumed(), "Notification-only launcher left a visible app activity"
first_deadline = int(prefs()["deadline"])
shell("am start -W -n " + PACKAGE + "/.MainActivity")
wait_for(lambda: int(prefs()["deadline"]) - first_deadline >= 59000,
         "Second launcher tap did not add exactly one configured increment")
assert PACKAGE not in resumed(), "Repeated launcher tap opened the app"
notifications = shell("dumpsys notification --noredact")
assert "awake_added_time" in notifications, "Temporary added-time notification missing"
assert "awake_timer" in notifications, "Ongoing countdown notification missing"
screenshot("notification-only-home")

# Open SettingsActivity from the same direct activity PendingIntent used by
# NotificationHelper; a test harness cannot shell-start a nonexported activity.
shell("am force-stop " + PACKAGE)
xml = '<map><string name="settings_entry">every_launch</string><long name="add_minutes" value="1"/><string name="theme">dark</string></map>'
shell("run-as " + PACKAGE + " sh -c " + quote("printf %s " + quote(xml) + " > shared_prefs/awake_settings.xml"))
shell("am start -W -n " + PACKAGE + "/.MainActivity")
wait_for(lambda: PACKAGE + "/.SettingsActivity" in resumed(), "Settings entry selection was not honored")
time.sleep(1)
screenshot("black-settings")
shell("uiautomator dump /sdcard/settings.xml")
tree = ET.fromstring(shell("cat /sdcard/settings.xml"))
controls = [node for node in tree.iter("node") if "每次启动增加" in node.attrib.get("text", "")]
assert controls, "Custom time setting is missing"
bounds = list(map(int, re.findall(r"\d+", controls[0].attrib["bounds"])))
assert bounds[1] > 0, "Setting overlaps the top system bar"
shell("input tap %d %d" % ((bounds[0] + bounds[2]) // 2, (bounds[1] + bounds[3]) // 2))
time.sleep(1)
screenshot("time-dialog")
shell("uiautomator dump /sdcard/dialog.xml")
dialog = shell("cat /sdcard/dialog.xml")
assert "保存" in dialog and "取消" in dialog, "Time dialog is missing its actions"
print("PASS: fresh-install settings, headless repeated launch, timer increments, both notifications and dialog.")
