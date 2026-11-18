"""Identify opt-in menu routes from the pinned stock APK's own preferences."""
import re
import subprocess
import sys
import zipfile
from pathlib import Path


AUDIO = "com.android.tv.settings.device.sound.AudioOutputActivity"
TIMER_ACTIVITY = "com.android.tv.settings.partnercustomizer.timer.TimerSetActivity"
TIMER_FRAGMENT = "com.android.tv.settings.partnercustomizer.timer.TimerFragment"
TIMER_PACKAGE = "com.android.tv.settings.partnercustomizer.switchofftimer."


def stock_timer_activity(manifest, activity, fragment):
    """Require the stock activity to construct the verified editor without extras."""
    declared = any(re.search(r'\bandroid:name(?:\([^)]*\))?="'+re.escape(TIMER_ACTIVITY)+r'"', block)
                   for block in re.split(r'(?m)^\s*E:\s*', manifest)
                   if re.match(r'^activity(?:\s|$)', block))
    if not declared:
        return False
    descriptor = "L" + TIMER_FRAGMENT.replace(".", "/") + ";"
    match = re.search(r'(?ms)^\.method [^\n]*\bcreateSettingsFragment\(\)Landroidx/fragment/app/Fragment;\n'
                      r'(.*?)^\.end method', activity)
    if not match:
        return False
    body = match[1]
    return (
        re.search(r'\bconst-class\s+\w+,\s*'+re.escape(descriptor), body) is not None
        and re.search(r'const/4\s+(v\d+),\s*0x0\s*(?:\.line[^\n]*\s*)?'
                      r'invoke-interface\s+\{\w+,\s*\w+,\s*\1\},\s*'
                      r'Lcom/android/tv/settings/SettingsFragmentProvider;->newSettingsFragment\(',
                      body) is not None
        and not re.search(r'->(?:getIntent|getExtras|putExtra|startActivity)\(', body)
        and 'set_timer_power_off_timer' in fragment
        and 'tv_timer_power_off_timer_values' in fragment
    )


def discover(apk, decoded):
    timer_files = sorted(decoded.glob("smali*/**/partnercustomizer/switchofftimer/*.smali"))
    timer_fragments = {
        TIMER_PACKAGE + path.stem for path in timer_files
        if "Fragment" in path.stem and "$" not in path.stem
        and ".method public constructor <init>()V" in path.read_text()
    }
    audio_activity = list(decoded.glob("smali*/**/device/sound/AudioOutputActivity.smali"))
    interesting = timer_fragments | {AUDIO}
    proven = set()
    with zipfile.ZipFile(apk) as archive:
        for entry in archive.namelist():
            if not entry.startswith("res/") or not entry.endswith(".xml"):
                continue
            raw = archive.read(entry)
            candidates = [name for name in interesting
                          if name.encode() in raw or name.encode("utf-16le") in raw]
            if not candidates:
                continue
            tree = subprocess.run(["aapt", "dump", "xmltree", apk, entry],
                                  check=True, text=True, capture_output=True).stdout
            # Require the exact stock preference target instead of constructing timer service Intents.
            for name in candidates:
                if name == AUDIO:
                    for block in re.split(r"(?m)^\s*E:", tree):
                        if (re.search(r'targetClass(?:\([^)]*\))?="'+re.escape(name)+'"', block)
                                and re.search(r'targetPackage(?:\([^)]*\))?="com\.android\.tv\.settings"', block)
                                and not re.search(r'\b(?:action|data|mimeType|flags)(?:\([^)]*\))?=', block)
                                and not re.search(r"(?m)^\s*E: extra\b", tree)):
                            proven.add(name)
                elif re.search(r'fragment(?:\([^)]*\))?="'+re.escape(name)+'"', tree) and not re.search(
                        r"(?m)^\s*E: extra\b", tree):
                    proven.add(name)

    if AUDIO not in proven and len(audio_activity) == 1:
        # The same-package Activity is also usable when it has a confirmed
        # stock no-arguments launcher in executable code.
        descriptor = "L" + AUDIO.replace(".", "/") + ";"
        for path in decoded.glob("smali*/**/*.smali"):
            raw = path.read_bytes()
            if descriptor.encode() not in raw or b"->startActivity(" not in raw:
                continue
            for method in re.findall(r"(?ms)^\.method [^\n]+\n.*?^\.end method", raw.decode()):
                if (re.search(r"const-class \w+, "+re.escape(descriptor), method)
                        and method.count("Landroid/content/Intent;->") == 1
                        and "Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V" in method
                        and len(re.findall(r"->startActivity\(Landroid/content/Intent;\)V", method)) == 1
                        and len(re.findall(r"\bconst-class\b", method)) == 1
                        and len(re.findall(r"new-instance \w+, Landroid/content/Intent;", method)) == 1):
                    proven.add(AUDIO)
                    break
    timer = sorted(timer_fragments & proven)
    timer_activity = list(decoded.glob("smali*/**/partnercustomizer/timer/TimerSetActivity.smali"))
    timer_fragment = list(decoded.glob("smali*/**/partnercustomizer/timer/TimerFragment.smali"))
    activity_proven = False
    if len(timer_activity) == 1 and len(timer_fragment) == 1:
        manifest = subprocess.run(["aapt", "dump", "xmltree", apk, "AndroidManifest.xml"],
                                  check=True, text=True, capture_output=True).stdout
        activity_proven = stock_timer_activity(manifest, timer_activity[0].read_text(),
                                               timer_fragment[0].read_text())
    print("Stock power schedule activity:", TIMER_ACTIVITY if activity_proven else "unconfirmed")
    print("Exact user routes:", "audio output" if AUDIO in proven else "audio output unconfirmed",
          "timer " + timer[0] if len(timer) == 1 else "timer fragment unconfirmed")
    print("Stock timer classes:", ", ".join(path.stem for path in timer_files) or "none")
    return (AUDIO in proven and len(audio_activity) == 1, timer[0] if len(timer) == 1 else "",
            TIMER_ACTIVITY if activity_proven else "")


if __name__ == "__main__":
    apk, decoded, output = sys.argv[1:]
    audio, timer, timer_activity = discover(apk, Path(decoded))
    Path(output).write_text(
        "package dev.projectorsoft.tvsettings;\n"
        "public final class Exact8054UserRoutes {\n"
        f'    public static final String AUDIO_OUTPUT = "{AUDIO if audio else ""}";\n'
        f'    public static final String TIMER_FRAGMENT = "{timer}";\n'
        f'    public static final String TIMER_ACTIVITY = "{timer_activity}";\n'
        "}\n", encoding="utf-8"
    )
