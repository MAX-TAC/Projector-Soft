"""Remove factory-junk activities from the 8054 TvSettings manifest.

The stock manifest declares 105 activities. A large part of them belongs to
features this 1LCD projector does not have (DVB channels, wireless speakers,
retail mode, Android TV slices) or to OEM screens our own menu replaced.
Everything removed here must be unreachable from the remaining UI; the list is
explicit so the intent stays auditable.

Run:  python3 strip_8054_junk_activities.py <in.apk> <out.apk>
"""
from __future__ import annotations

import struct
import sys
import zipfile

XML = 0x0003
STRINGS = 0x0001
START = 0x0102
END = 0x0103
NO = 0xFFFFFFFF

ANDROID_NS = 'http://schemas.android.com/apk/res/android'

# Activity class names (fully qualified) that this board never needs.
JUNK = {
    # DVB / satellite: the single-panel projector has no tuner at all.
    'com.android.tv.settings.partnercustomizer.vendor.channels.ChannelConfirmActivity',
    'com.android.tv.settings.partnercustomizer.vendor.channels.ChannelsActivity',
    'com.android.tv.settings.partnercustomizer.vendor.channels.ChannelsTwoPanalActivity',
    'com.android.tv.settings.partnercustomizer.vendor.channels.DiseqcTypeActivity',
    'com.android.tv.settings.partnercustomizer.vendor.ResetActivity',
    # Wireless speaker set: the owner removed speaker output entirely.
    'com.android.tv.settings.partnercustomizer.sound.wireless.ResetSpeakerActivity',
    'com.android.tv.settings.partnercustomizer.sound.wireless.SelectListActivity',
    'com.android.tv.settings.partnercustomizer.sound.wireless.SpeakerDeviceBeyondTipsActivity',
    'com.android.tv.settings.partnercustomizer.sound.wireless.SpeakerDevicesBeyondActivity',
    'com.android.tv.settings.partnercustomizer.sound.wireless.StatelliteSpeakerLayoutActivity',
    'com.android.tv.settings.partnercustomizer.sound.wireless.StatelliteSpeakerLayoutChangeTipsActivity',
    'com.android.tv.settings.partnercustomizer.sound.wireless.WirelessAddNoteActivity',
    'com.android.tv.settings.partnercustomizer.sound.wireless.WirelessNoteCommonActivity',
    # Retail demo mode.
    'com.android.tv.settings.partnercustomizer.retailmode.RetailModeActivity',
    'com.android.tv.settings.partnercustomizer.retailmode.RetailModeConfirmActivity',
    # Android TV slices: no slice surface exists in this build.
    'com.android.tv.settings.slice.SliceActivity',
    'com.android.tv.settings.accounts.AccountSliceActivity',
    'com.android.tv.settings.accounts.ManageUserServicesSliceActivity',
    'com.android.tv.settings.device.display.ambient.AmbientSliceActivity',
    # Dead stock screens.
    'com.android.tv.settings.EmptyStubActivity',
    'com.android.tv.settings.RemoteBugreportActivity',
    'com.android.tv.settings.vendor.VendorActivity',
    'com.android.tv.settings.enterprise.EnterprisePrivacySettingsActivity',
    'com.android.tv.settings.partnercustomizer.utils.ProgressDialogActivity',
    # OEM menu duplicates replaced by our own screens.
    'com.android.tv.settings.uicustom.MainActivity',
    'com.android.tv.settings.uicustom.floatmenu.MenuJumpActivity',
}


def u16(b, o):
    return struct.unpack_from('<H', b, o)[0]


def u32(b, o):
    return struct.unpack_from('<I', b, o)[0]


class Pool:
    def __init__(self, chunk):
        self.count = u32(chunk, 8)
        self.flags = u32(chunk, 16)
        self.utf8 = bool(self.flags & 0x100)
        self.strings_off = u32(chunk, 20)
        self.offsets = [u32(chunk, 28 + 4 * i) for i in range(self.count)]
        base = self.strings_off
        self.values = []
        for off in self.offsets:
            self.values.append(self._read(chunk, base + off))

    def _read(self, chunk, o):
        if self.utf8:
            n = chunk[o]
            o += 1
            if n & 0x80:
                o += 1
            n = chunk[o]
            o += 1
            if n & 0x80:
                o += 1
            data = chunk[o:]
            end = data.find(b'\x00')
            return data[:end].decode('utf-8', 'replace')
        n = u16(chunk, o)
        o += 2
        data = chunk[o:o + n * 2]
        return data.decode('utf-16le', 'replace')


def strip(data):
    if u16(data, 0) != XML:
        raise ValueError('not an AXML document')
    outer_size = u32(data, 4)
    # The XML chunk wraps the whole document, so sub-chunks start after its
    # own header (the stock header is 8 bytes).
    off = u16(data, 2)
    pool = None
    chunks = []
    while off < outer_size:
        kind = u16(data, off)
        size = u32(data, off + 4)
        chunks.append((kind, off, size))
        if kind == STRINGS and pool is None:
            pool = Pool(data[off:off + size])
        off += size
    if pool is None:
        raise ValueError('no string pool')
    strings = pool.values
    name_index = strings.index('name') if 'name' in strings else -1
    if name_index < 0:
        raise ValueError('no android:name attribute')
    target_index = strings.index('targetActivity') if 'targetActivity' in strings else -1

    xml_off, xml_size = 0, outer_size
    body_end = xml_off + xml_size
    # Everything before the first element node (the string pool and the
    # resource map) is copied unchanged; only element nodes are filtered.
    body_start = None
    for kind, off_, size in chunks:
        if kind in (START, END):
            body_start = off_
            break
    if body_start is None:
        raise ValueError("no element nodes")

    # Walk element nodes and remember each start/end pair.
    nodes = []
    o = body_start
    while o < body_end:
        kind = u16(data, o)
        hsize = u16(data, o + 2)
        size = u32(data, o + 4)
        nodes.append((kind, o, hsize, size))
        o += size

    removed = set()
    out = bytearray(data[:body_start])
    index = 0
    depth_removal = 0
    while index < len(nodes):
        kind, start, hsize, size = nodes[index]
        if depth_removal > 0:
            # Skipping a whole element: nested START/END must be balanced or
            # the document becomes unparsable.
            if kind == START:
                depth_removal += 1
            elif kind == END:
                depth_removal -= 1
            index += 1
            continue
        if kind == START:
            tag = strings[u32(data, start + hsize + 4)]
            # START layout after the header: ns, name, attributeStart,
            # attributeSize, attributeCount, id/class/style indexes.
            attr_start = u16(data, start + hsize + 8)
            attr_size = u16(data, start + hsize + 10)
            attr_count = u16(data, start + hsize + 12)
            attr_off = start + hsize + attr_start
            target = None
            alias_target = None
            for a in range(attr_count):
                base = attr_off + a * 20
                attr_name = u32(data, base + 4)
                raw = u32(data, base + 8)
                value = strings[raw] if raw != NO and raw < len(strings) else None
                if attr_name == name_index:
                    target = value
                elif target_index >= 0 and attr_name == target_index:
                    alias_target = value
            # An activity-alias pointing at a removed activity makes the whole
            # package uninstallable, so it must go with its target.
            if tag == 'activity-alias' and alias_target in JUNK:
                removed.add(alias_target)
                depth_removal = 1
                index += 1
                continue
            if tag in ('activity', 'activity-alias') and target in JUNK:
                removed.add(target)
                depth_removal = 1
                index += 1
                continue
        out += data[start:start + size]
        index += 1

    # Recompute the XML chunk size and the outer document size.
    struct.pack_into('<I', out, 4, len(out))
    struct.pack_into('<I', out, xml_off + 4, len(out) - xml_off)
    return bytes(out), removed


def main():
    if len(sys.argv) != 3:
        raise SystemExit(__doc__)
    source, target = sys.argv[1], sys.argv[2]
    with zipfile.ZipFile(source) as src:
        manifest = src.read('AndroidManifest.xml')
        stripped, removed = strip(manifest)
        with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as dst:
            for item in src.infolist():
                if item.filename == 'AndroidManifest.xml':
                    dst.writestr(item, stripped)
                else:
                    dst.writestr(item, src.read(item.filename))
    print('removed activities:', len(removed))
    for name in sorted(removed):
        print('  -', name)


if __name__ == '__main__':
    main()