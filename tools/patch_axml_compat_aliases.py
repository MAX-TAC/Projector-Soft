#!/usr/bin/env python3
"""Insert KuSettings-compatible activity aliases into original Android binary XML.
Preserves existing string indices, resource map, OEM resources, DEX and JNI.
Deliberately does not modify the native device firmware.
"""
import argparse, copy, struct, zipfile
from pathlib import Path
from patch_axml_version import change_version_code

XML=0x0003; STRINGS=0x0001; START=0x0102; END=0x0103; NO=0xffffffff
RESMAP=0x0180; TYPE_STRING=0x03; TYPE_BOOL=0x12

ALIASES=[
  ("legacy.SettingsAlias","com.android.tv.settings.MainSettings","android.settings.SETTINGS"),
  ("legacy.WiFiSettingsAlias","com.android.tv.settings.connectivity.NetworkActivity","android.settings.WIFI_SETTINGS"),
  ("legacy.BluetoothSettingsAlias","com.android.tv.settings.accessories.BluetoothAccessoryActivity","android.settings.BLUETOOTH_SETTINGS"),
  ("legacy.AutoBluetoothAlias","com.android.tv.settings.accessories.AddAccessoryActivity","android.settings.AUTO_BLUETOOTH"),
  ("legacy.AllAppsAlias","com.android.tv.settings.device.apps.AllAppsActivity","android.settings.ALLAPPS"),
  ("legacy.DateTimeAlias","com.android.tv.settings.system.DateTimeActivity","android.settings.DATETIME_SETTINGS"),
  ("legacy.LanguageAlias","com.android.tv.settings.system.LanguageActivity","android.settings.LAUGUAGE_SETTINGS"),
]

def u16(b,o):return struct.unpack_from("<H",b,o)[0]
def u32(b,o):return struct.unpack_from("<I",b,o)[0]
def p16(b,o,x):struct.pack_into("<H",b,o,x)
def p32(b,o,x):struct.pack_into("<I",b,o,x)
def chunkinfo(b,o):
    if o+8>len(b):raise ValueError("truncated chunk")
    kind,head,size=struct.unpack_from("<HHI",b,o)
    if head<8 or size<head or o+size>len(b):raise ValueError("malformed chunk @"+hex(o))
    return kind,head,size
def read_l8(b,o):
    n=b[o];o+=1
    if n&0x80:return ((n&0x7f)<<8)|b[o],o+1
    return n,o
def read_l16(b,o):
    n=u16(b,o);o+=2
    if n&0x8000:return ((n&0x7fff)<<16)|u16(b,o),o+2
    return n,o
def put_l8(n):
    if n<0x80:return bytes([n])
    if n>=0x8000:raise ValueError("string too long")
    return bytes([0x80|(n>>8),n&0xff])
def put_l16(n):
    if n<0x8000:return struct.pack("<H",n)
    return struct.pack("<HH",0x8000|(n>>16),n&0xffff)
def decode_string(b,offs,utf8):
    if utf8:
        _,q=read_l8(b,offs)
        n,q=read_l8(b,q)
        return b[q:q+n].decode("utf-8")
    n,q=read_l16(b,offs)
    return b[q:q+2*n].decode("utf-16le")
def encode_string(s,utf8):
    u16len=len(s.encode("utf-16le"))//2
    if utf8:
        data=s.encode("utf-8")
        return put_l8(u16len)+put_l8(len(data))+data+b"\0"
    return put_l16(u16len)+s.encode("utf-16le")+b"\0\0"

class StringPool:
    def __init__(self,blob):
        self.orig=blob
        typ,header,total=chunkinfo(blob,0)
        if typ!=STRINGS or header<28 or total!=len(blob):raise ValueError("bad string pool")
        self.header=header
        self.n=u32(blob,8)
        self.styles=u32(blob,12)
        self.flags=u32(blob,16)
        self.strstart=u32(blob,20)
        self.stylestart=u32(blob,24)
        self.utf8=bool(self.flags&0x100)
        self.offsets=[u32(blob,header+i*4) for i in range(self.n)]
        self.style_offsets=[u32(blob,header+4*(self.n+i)) for i in range(self.styles)]
        arrend=header+4*(self.n+self.styles)
        if arrend>self.strstart:raise ValueError("overlap of string offsets and data")
        self.padding=blob[arrend:self.strstart]
        stringend=self.stylestart if self.stylestart else total
        if stringend<self.strstart:raise ValueError("bad string data extent")
        self.payload=blob[self.strstart:stringend]
        self.styledata=blob[self.stylestart:] if self.stylestart else b""
        self.values=[decode_string(blob,self.strstart+off,self.utf8) for off in self.offsets]
        self.lookup={v:i for i,v in enumerate(self.values)}
        self.added=[]
    def index(self,text):
        if text in self.lookup:return self.lookup[text]
        idx=self.n+len(self.added);self.lookup[text]=idx;self.added.append(text);return idx
    def output(self):
        newoffsets=[]
        data=bytearray(self.payload)
        for s in self.added:
            newoffsets.append(len(data))
            data+=encode_string(s,self.utf8)
        while len(data)%4:data.append(0)
        strings_start=self.header+4*(self.n+len(self.added)+self.styles)+len(self.padding)
        style_start=strings_start+len(data) if self.styles else 0
        prefix=bytearray(self.orig[:self.header])
        p32(prefix,8,self.n+len(self.added))
        p32(prefix,20,strings_start)
        p32(prefix,24,style_start)
        indices=b"".join(struct.pack("<I",v) for v in self.offsets+newoffsets)
        style_indices=b"".join(struct.pack("<I",v) for v in self.style_offsets)
        result=prefix+indices+style_indices+self.padding+data+self.styledata
        p32(result,4,len(result))
        return bytes(result)

def endnode(nidx,ns=NO):
    return struct.pack("<HHIIII",END,16,24,0,NO,ns)+struct.pack("<I",nidx)

def attr(nsi,name,value):
    if isinstance(value,bool):
        return struct.pack("<IIIHBBI",nsi,name,NO,8,0,TYPE_BOOL,NO if value else 0)
    return struct.pack("<IIIHBBI",nsi,name,value,8,0,TYPE_STRING,value)

def startnode(tag,attrs=None,ns=NO):
    entries=attrs or []
    head=struct.pack("<HHIIII",START,16,36+20*len(entries),0,NO,ns)
    ext=struct.pack("<IHHHHHH",tag,20,20,len(entries),0,0,0)
    return head+ext+b"".join(attrs)

def patch_manifest(data):
    b=data
    if len(b)<8 or u16(b,0)!=XML or u32(b,4)!=len(b):
        raise ValueError("invalid Android binary manifest")
    offset=u16(b,2)
    chunks=[]
    pool=None;app_close=None;resmap=None
    while offset<len(b):
        kind,header,size=chunkinfo(b,offset)
        chunk=b[offset:offset+size]
        if kind==STRINGS:
            if pool:raise ValueError("duplicate string pool")
            pool=StringPool(chunk)
        if kind==RESMAP:
            resmap=[u32(chunk,k) for k in range(header,size,4)]
        chunks.append((kind,chunk))
        offset+=size
    if not pool or resmap is None:raise ValueError("missing pool or resource map")
    lookup=pool.lookup
    for required in ("application","activity-alias","intent-filter","action","category","name","targetActivity",
                     "exported","enabled","http://schemas.android.com/apk/res/android"):
        if required not in lookup:
            raise ValueError("required AXML structure/attr missing: "+required)
    # Existing manifest should declare all referenced target activities. Parse actual name attrs.
    uridx=lookup["http://schemas.android.com/apk/res/android"]
    nameidx=lookup["name"]
    declared=set();existing_actions=set()
    for kind,ch in chunks:
        if kind!=START:continue
        tag=pool.values[u32(ch,20)]
        ast=u16(ch,24); size=u16(ch,26); cnt=u16(ch,28)
        for i in range(cnt):
            o=16+ast+i*size
            nsidx,ai,raw=struct.unpack_from("<III",ch,o)
            if nsidx==uridx and ai==nameidx and raw!=NO:
                if tag=="activity":declared.add(pool.values[raw])
                elif tag=="action":existing_actions.add(pool.values[raw])
    for _,target,_ in ALIASES:
        if target not in declared and ("."+target.split(".")[-1]) not in declared:
            raise ValueError("original target activity missing: "+target)
    for i,(kind,ch) in enumerate(chunks):
        if kind==END and pool.values[u32(ch,20)]=="application":
            if app_close is not None:raise ValueError("multiple application closing nodes")
            app_close=i
    if app_close is None:raise ValueError("missing application closing node")
    fragment=b""
    enabled=pool.index("enabled"); exported=pool.index("exported")
    target_idx=pool.index("targetActivity")
    tag_alias=pool.index("activity-alias");tag_filter=pool.index("intent-filter")
    tag_action=pool.index("action");tag_category=pool.index("category")
    category_default=pool.index("android.intent.category.DEFAULT")
    added=[]
    for suffix,target,action in ALIASES:
        if action in existing_actions:
            print("Already handled in original:",action);continue
        alias_name="com.android.tv.settings."+suffix
        al=pool.index(alias_name); tgt=pool.index(target); intent=pool.index(action)
        fragment+=startnode(tag_alias,[
            attr(uridx,nameidx,al),attr(uridx,target_idx,tgt),
            attr(uridx,enabled,True),attr(uridx,exported,True)])
        fragment+=startnode(tag_filter)
        fragment+=startnode(tag_action,[attr(uridx,nameidx,intent)])+endnode(tag_action)
        fragment+=startnode(tag_category,[attr(uridx,nameidx,category_default)])+endnode(tag_category)
        fragment+=endnode(tag_filter)+endnode(tag_alias)
        added.append(action)
    if len(added)<5:raise ValueError("too few compatibility intent actions")
    rebuilt=[]
    for i,(kind,ch) in enumerate(chunks):
        if i==app_close:rebuilt.append(fragment)
        rebuilt.append(pool.output() if kind==STRINGS else ch)
    prefix=bytearray(b[:u16(b,2)])
    result=prefix+b"".join(rebuilt)
    p32(result,4,len(result))
    # VersionCode uses the existing resource-map ID so no resource linker needed.
    result=change_version_code(bytes(result),1,2)
    print("Inserted aliases:",added)
    return result,added

def main():
    p=argparse.ArgumentParser()
    p.add_argument("original_apk",type=Path)
    p.add_argument("unsigned_output",type=Path)
    o=p.parse_args()
    o.unsigned_output.parent.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(o.original_apk) as src:
        manifest,actions=patch_manifest(src.read("AndroidManifest.xml"))
        with zipfile.ZipFile(o.unsigned_output,"w",allowZip64=True) as output:
            for info in src.infolist():
                n=info.filename
                if n.startswith("META-INF/") and n.upper().endswith((".RSA",".DSA",".EC",".SF",".MF")):
                    continue
                val=manifest if n=="AndroidManifest.xml" else src.read(n)
                output.writestr(copy.copy(info),val)
    with zipfile.ZipFile(o.original_apk) as a,zipfile.ZipFile(o.unsigned_output) as c:
        assert c.read("resources.arsc")==a.read("resources.arsc")
        assert c.read("classes.dex")==a.read("classes.dex")
        assert c.read("AndroidManifest.xml")!=a.read("AndroidManifest.xml")
    print("Changed only original binary AndroidManifest, no resources/DEX/JNI changed")
if __name__=="__main__":main()
