#!/usr/bin/env bash
# advance_day.sh — move the emulator clock forward by 1 day to test migration.

ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"

if ! "$ADB" devices | grep -q "emulator"; then
  echo "Error: no emulator detected. Start one first."
  exit 1
fi

ROOT_OUTPUT=$("$ADB" root 2>&1)
if echo "$ROOT_OUTPUT" | grep -q "cannot"; then
  echo ""
  echo "ERROR: adbd cannot run as root."
  echo ""
  echo "If you already switched to a Google APIs image, the emulator needs a"
  echo "cold boot to apply it (not a quick-boot resume from snapshot)."
  echo ""
  echo "In Device Manager: click the dropdown arrow next to your emulator"
  echo "and choose 'Cold Boot Now', then run this script again."
  exit 1
fi
sleep 1

# Disable automatic time sync so the manual change sticks.
"$ADB" shell settings put global auto_time 0

# Read the emulator's current date and advance it by exactly 1 day.
EMULATOR_DATE=$("$ADB" shell date)
CURRENT_TS=$(date -j -f "%a %b %d %T %Z %Y" "$EMULATOR_DATE" +%s 2>/dev/null)
TOMORROW=$(date -j -r $((CURRENT_TS + 86400)) '+%Y-%m-%d %H:%M:%S')

echo "Emulator is currently: $EMULATOR_DATE"
echo "Advancing by 1 day to: $TOMORROW"
"$ADB" shell "date -s '$TOMORROW'"

echo "Emulator date is now: $("$ADB" shell date)"

# Force-stop then relaunch so migration runs on startup.
"$ADB" shell am force-stop com.example.bullet
sleep 1
"$ADB" shell monkey -p com.example.bullet -c android.intent.category.LAUNCHER 1 2>/dev/null

echo ""
echo "Done."
