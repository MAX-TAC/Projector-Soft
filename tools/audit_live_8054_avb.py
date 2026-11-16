#!/usr/bin/env python3
"""Read-only analysis of owner-published live boot/DTBO/vbmeta partitions."""
import hashlib, importlib.util, pathlib, subprocess, sys
root=pathlib.Path(sys.argv[1])
avbpath=pathlib.Path(sys.argv[2])
spec=importlib.util.spec_from_file_location("avbtool",avbpath)
avb=importlib.util.module_from_spec(spec)
spec.loader.exec_module(avb)
expect={
"vbmeta_a.img":("0131481e6ac5533de54c3cc6a052d149ba135d2195fd31242f18bbb2a893eb19",1048576),
"vbmeta_system_a.img":("d8b2856abd6920fb9ca25b9b487365386ca0e746e379d1275b92cc9a15d23e39",1048576),
"dtbo_a.img":("7b6face54f66968c533041d9ce38971eb7f5064d1357e46fb223a3fbe3cffded",8388608),
"boot_a.img":("f1fc341533babecc66ab697df3943464a658c9ae8a03a035f2bf5a30529f1c63",78643200)
}
for name,(digest,size) in expect.items():
    p=root/name
    h=hashlib.sha256()
    with p.open("rb") as f:
        magic=f.read(16)
        f.seek(0)
        while chunk:=f.read(4*1024*1024):h.update(chunk)
    print("\n###",name,flush=True)
    print("Bytes:",p.stat().st_size,"size_ok:",p.stat().st_size==size)
    print("SHA256:",h.hexdigest(),"release_digest_ok:",h.hexdigest()==digest)
    print("Start magic:",magic[:8].hex(),repr(magic[:8]))
    if p.stat().st_size!=size or h.hexdigest()!=digest:
        raise SystemExit("STOP: downloaded blob mismatch!")
    if name.startswith("vbmeta"):
        with p.open("rb") as f:
            header=avb.AvbVBMetaHeader(f.read(256))
            f.seek(0)
            blob=f.read(256+header.authentication_data_block_size+header.auxiliary_data_block_size)
        key_offset=256+header.authentication_data_block_size+header.public_key_offset
        key=blob[key_offset:key_offset+header.public_key_size]
        print("AVB public key SHA1:",hashlib.sha1(key).hexdigest())
        print("AVB public key SHA256:",hashlib.sha256(key).hexdigest())
        print("AVB public key length:",len(key))
        pubtest=pathlib.Path("/tmp/aosp_testkey_rsa4096_pub.pem")
        if pubtest.exists():
            cmp=subprocess.run(["openssl","rsa","-pubin","-in",str(pubtest),"-noout","-modulus"],capture_output=True,text=True)
            if cmp.returncode==0:
                modulus=cmp.stdout.strip().split("=",1)[-1].lower().lstrip("0")
                live=key[8:8+512].hex().lstrip("0")
                print("AVB KEY MATCHES PUBLIC AOSP RSA4096 TEST KEY:",modulus==live)
            else:
                print("AOSP KEY COMPARISON ERROR:",cmp.stderr[:200])

        print("Algorithm type:",header.algorithm_type)
        print("Rollback index:",header.rollback_index)
        print("Flags:",header.flags)
        print("Rollback index location:",header.rollback_index_location)
        print("Release string:",header.release_string)
        try:
            print("AVB INTERNAL SIGNATURE VERIFIED:",avb.verify_vbmeta_signature(header,blob))
        except Exception as ex:
            print("AVB INTERNAL SIGNATURE CHECK ERROR:",type(ex).__name__,str(ex)[:200])
        p2=subprocess.run([sys.executable,str(avbpath),"info_image","--image",str(p)],
                          capture_output=True,text=True)
        print("AVB INFO_IMAGE EXIT:",p2.returncode)
        print((p2.stdout+"\n"+p2.stderr)[:22000])
    else:
        p2=subprocess.run([sys.executable,str(avbpath),"info_image","--image",str(p)],
                          capture_output=True,text=True)
        print("AVB footer check (may be absent):",p2.returncode,
              (p2.stdout+"\n"+p2.stderr)[:2000])

print("\n### CHECK BOOT AND DTBO AGAINST SIGNED MAIN VBMETA")
parser=avb.Avb()
_,hdr,descs,_=parser._parse_image(avb.ImageHandler(str(root/"vbmeta_a.img"),read_only=True))
for d in descs:
    if not isinstance(d,avb.AvbHashDescriptor) or d.partition_name not in ("boot","dtbo"):
        continue
    img=root/(d.partition_name+"_a.img")
    hasher=hashlib.new(d.hash_algorithm)
    hasher.update(d.salt)
    remaining=d.image_size
    with img.open("rb") as f:
        while remaining>0:
            chunk=f.read(min(remaining,4*1024*1024))
            if not chunk:raise RuntimeError("IMAGE TRUNCATED: "+str(img))
            hasher.update(chunk)
            remaining-=len(chunk)
    verified=hasher.digest()==d.digest
    print(d.partition_name,"signed vbmeta digest matches actual partition:",verified)
    if not verified:raise RuntimeError("VERIFIED HASH DOES NOT MATCH "+d.partition_name)

print("\nREAD ONLY: no firmware writes and no key materials exported",flush=True)
