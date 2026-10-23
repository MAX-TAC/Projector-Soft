#!/usr/bin/env python3
"""Add explicit backward-compatible settings action handlers as activity aliases.
Requires APKTool decoded plain-text AndroidManifest.xml. Does not remove any OEM classes.
"""
from pathlib import Path
import argparse, re, xml.etree.ElementTree as ET

MAPPING={
 "android.settings.SETTINGS":("MainSettings","com.android.tv.settings.MainSettings"),
 "android.settings.WIFI_SETTINGS":("WiFiSettings","com.android.tv.settings.connectivity.NetworkActivity"),
 "android.settings.BLUETOOTH_SETTINGS":("BluetoothSettings","com.android.tv.settings.accessories.BluetoothAccessoryActivity"),
 "android.settings.AUTO_BLUETOOTH":("AutoBluetooth","com.android.tv.settings.accessories.AddAccessoryActivity"),
 "android.settings.ALLAPPS":("AllApps","com.android.tv.settings.device.apps.AllAppsActivity"),
 "android.settings.DATETIME_SETTINGS":("DateTime","com.android.tv.settings.system.DateTimeActivity"),
 "android.settings.LAUGUAGE_SETTINGS":("Language","com.android.tv.settings.system.LanguageActivity"),
}
ANDROID="http://schemas.android.com/apk/res/android"
def patch(raw):
    if "<manifest" not in raw or "</application>" not in raw:raise ValueError("Manifest is not decoded XML")
    root=ET.fromstring(raw)
    ns="{"+ANDROID+"}"
    apps=root.findall("application")
    if len(apps)!=1:raise ValueError("Application count != 1")
    app=apps[0]
    known={x.get(ns+"name") for x in app.findall("activity")}
    text=""; actions=[]
    for intent,(suffix,target) in MAPPING.items():
        if target not in known and ("."+target.split(".")[-1]) not in known:
            raise ValueError("Target activity not declared in stock APK: "+target)
        alias="com.android.tv.settings.legacy."+suffix+"Alias"
        if any(e.get(ns+"name")==alias for e in app.findall("activity-alias")):
            raise ValueError("Alias collision: "+alias)
        # Do not duplicate an action already exposed by TvSettingsPlus;
        # modern handlers may already exist. Only create compatibility actions absent.
        action_already=any(
            n.get(ns+"name")==intent
            for el in app
            for fil in el.findall("intent-filter")
            for n in fil.findall("action"))
        if action_already:
            print("Already handled by original TvSettingsPlus:",intent)
            continue
        text+=("\n        <activity-alias android:name=\""+alias+"\""
               " android:targetActivity=\""+target+"\" android:enabled=\"true\" android:exported=\"true\">"
               "\n            <intent-filter>"
               "\n                <action android:name=\""+intent+"\" />"
               "\n                <category android:name=\"android.intent.category.DEFAULT\" />"
               "\n            </intent-filter>"
               "\n        </activity-alias>")
        actions.append((intent,target))
    if not actions:raise ValueError("No new actions; nothing to patch")
    output=raw.replace("</application>",text+"\n    </application>")
    root2=ET.fromstring(output)
    old=root2.attrib.get(ns+"versionCode")
    if old != "1":raise ValueError("Expected versionCode=1, got "+repr(old))
    # only replace XML attribute once; preserve all other attributes verbatim
    count=len(re.findall(r'android:versionCode="1"',output))
    if count!=1:raise ValueError("Missing unambiguous android:versionCode attr: "+str(count))
    output=output.replace('android:versionCode="1"','android:versionCode="2"',1)
    ET.fromstring(output)
    return output,actions
def main():
    p=argparse.ArgumentParser()
    p.add_argument("manifest",type=Path)
    p.add_argument("--preview",action="store_true")
    a=p.parse_args()
    data=a.manifest.read_bytes()
    if not data.lstrip().startswith(b"<?xml") and not data.lstrip().startswith(b"<manifest"):
        raise ValueError("Apktool -r left a binary AndroidManifest.xml; abort without modification")
    text,actions=patch(data.decode("utf-8"))
    print("Added intent aliases:",actions)
    if not a.preview:a.manifest.write_text(text,encoding="utf-8")
if __name__=="__main__":main()
