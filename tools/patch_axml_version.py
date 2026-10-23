#!/usr/bin/env python3
"""Patch Android binary XML versionCode in a COPY of an original APK, fail closed.

Only intended for same-signature, SDK33, system-UID TvSettingsPlus smoke builds.
Does not mutate any resource or dex file, or touch the user's installed device.
"""
import argparse
import struct
import zipfile
from pathlib import Path

XML_TYPE = 0x0003
RESOURCE_MAP = 0x0180
XML_START_ELEMENT = 0x0102
ATTR_ANDROID_VERSION_CODE = 0x0101021b
TYPE_INT_DEC = 0x10

def u16(b, at): return struct.unpack_from("<H",b,at)[0]
def u32(b, at): return struct.unpack_from("<I",b,at)[0]

def change_version_code(data: bytes, old: int, new: int) -> bytes:
    b=bytearray(data)
    if len(b)<8 or u16(b,0)!=XML_TYPE:
        raise ValueError("Not an Android compiled XML (AXML) manifest")
    if old<0 or new<=old:
        raise ValueError("versionCode must increase")
    total=u32(b,4)
    if total != len(b):
        raise ValueError("AXML file size mismatch")
    offset=u16(b,2)
    resource_map=[]
    changed=0
    while offset<len(b):
        if offset+8>len(b):raise ValueError("Truncated AXML chunk")
        typ=u16(b,offset)
        header=u16(b,offset+2)
        size=u32(b,offset+4)
        if size<8 or header<8 or offset+size>len(b):
            raise ValueError("Malformed AXML chunk")
        if typ==RESOURCE_MAP:
            resource_map=[u32(b,i) for i in range(offset+header,offset+size,4)]
        if typ==XML_START_ELEMENT:
            # ResXMLTree_node (16 bytes) followed by attrExt (20 bytes).
            if header<16 or offset+36>len(b):raise ValueError("Bad start element")
            astart=u16(b,offset+24)
            asize=u16(b,offset+26)
            acount=u16(b,offset+28)
            attrs=offset+16+astart
            if asize<20 or attrs+asize*acount>offset+size:
                raise ValueError("Bad AXML attributes")
            for k in range(acount):
                loc=attrs+k*asize
                name_index=u32(b,loc+4)
                resource_id=resource_map[name_index] if name_index<len(resource_map) else 0
                if resource_id != ATTR_ANDROID_VERSION_CODE:continue
                if u16(b,loc+12)!=8 or b[loc+15]!=TYPE_INT_DEC:
                    raise ValueError("versionCode has unsupported AXML value type")
                before=u32(b,loc+16)
                if before!=old:
                    raise ValueError(f"Unexpected original versionCode {before}, expected {old}")
                struct.pack_into("<I",b,loc+16,new)
                changed+=1
        offset+=size
    if offset!=len(b) or changed!=1:
        raise ValueError(f"versionCode attribute matches {changed}; expected exactly one")
    return bytes(b)

def main():
    p=argparse.ArgumentParser()
    p.add_argument("input_apk",type=Path)
    p.add_argument("output_unsigned_apk",type=Path)
    p.add_argument("--old",type=int,default=1)
    p.add_argument("--new",type=int,default=2)
    a=p.parse_args()
    a.output_unsigned_apk.parent.mkdir(exist_ok=True,parents=True)
    with zipfile.ZipFile(a.input_apk,"r") as src:
        names=src.namelist()
        if "AndroidManifest.xml" not in names:raise ValueError("APK lacks AndroidManifest.xml")
        patched=change_version_code(src.read("AndroidManifest.xml"),a.old,a.new)
        with zipfile.ZipFile(a.output_unsigned_apk,"w",allowZip64=True) as dest:
            for entry in src.infolist():
                path=entry.filename
                # Drop stale APK v1 signatures before signing new archive.
                if path.startswith("META-INF/") and path.upper().endswith((".RSA",".DSA",".EC",".SF",".MF")):
                    continue
                content=patched if path=="AndroidManifest.xml" else src.read(path)
                dest.writestr(entry,content)
    print(f"Manifest versionCode {a.old} -> {a.new}; DEX and resources copied byte-for-byte")
if __name__=="__main__":main()
