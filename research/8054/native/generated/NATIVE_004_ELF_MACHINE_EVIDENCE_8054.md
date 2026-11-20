# NATIVE-004 — machine evidence, exact-8054 (generated, read-only)

This file is generated from **the SHA-256-verified owner firmware** on GitHub Actions.
It contains selected relocation/symbol records and bounded disassembly excerpts,
not a reconstructed call graph, implementation contract or runtime test.

## Reproduction and tool versions

```sh
python3 tools/trace_8054_config_jni.py --vendor-lib /tmp/flix8054_erofs/vendor_a_tree/lib --out /tmp/flix8054_native_report
```

- readelf: `GNU readelf (GNU Binutils for Ubuntu) 2.46`
- arm-linux-gnueabihf-objdump: `GNU objdump (GNU Binutils for Ubuntu) 2.46`
- c++filt: `GNU c++filt (GNU Binutils for Ubuntu) 2.46`

## A. ELF inputs (contents are NOT published)

| Firmware library | SHA-256 verified |
|---|---|
| `vendor_a/lib/libcom_mediatek_twoworlds_tv_jni.so` | `312fef3f17f656ac957a32dfee8187798dacac8f4df83e8219b1553b7248ab24` |
| `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so` | `e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758` |
| `vendor_a/lib/libmediatek_tv_rpcwrapper.so` | `d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b` |
| `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0.so` | `a48159c7c66e5e82a4d293bc1e59e878cb0cfafbd8f645d872332f10424711f7` |

## B. Dynamic symbols and relocations

The symbol section includes **UND imports** as well as definitions. An UND+PLT
relocation and a branch from the specific JNI function establish a direct edge;
DT_NEEDED by itself does not.

### JNI: `libcom_mediatek_twoworlds_tv_jni.so`

- `Machine:                           ARM`
- DT_NEEDED contains the following selected dependencies: `vendor.mediatek.api.mtktvapi@1.0-cwrapper.so`, `vendor.mediatek.custom.mtktvapicustom@1.0-cwrapper.so`
- Focused symbol rows (type, value, size, section, symbol):

```text
225: 00000000     0 FUNC    GLOBAL DEFAULT  UND a_mtktvapi_config_get_value
284: 00000000     0 FUNC    GLOBAL DEFAULT  UND a_mtktvapi_config_set_value
285: 00000000     0 FUNC    GLOBAL DEFAULT  UND a_mtktvapi_config_set_value_for_all_elems
834: 000566dd   104 FUNC    GLOBAL DEFAULT   14 Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native
```

- Relevant relocations:

```text
0007df0c  0000e116 R_ARM_JUMP_SLOT        00000000   a_mtktvapi_config_get_value
0007dff8  00011c16 R_ARM_JUMP_SLOT        00000000   a_mtktvapi_config_set_value
0007dffc  00011d16 R_ARM_JUMP_SLOT        00000000   a_mtktvapi_config_set_value_for_all_elems
```

### C-wrapper: `vendor.mediatek.api.mtktvapi@1.0-cwrapper.so`

- `Machine:                           ARM`
- DT_NEEDED contains the following selected dependencies: `vendor.mediatek.api.mtktvapi@1.0.so`, `libhidlbase.so`, `libhidlmemory.so`, `libhidltransport.so`
- Focused symbol rows (type, value, size, section, symbol):

```text
146: 0003f62d   168 FUNC    GLOBAL DEFAULT   14 a_mtktvapi_config_set_value_for_all_elems
686: 0003f585   168 FUNC    GLOBAL DEFAULT   14 a_mtktvapi_config_set_value
1045: 0003f419   364 FUNC    GLOBAL DEFAULT   14 a_mtktvapi_config_get_value
```

- Relevant relocations:

```text
NO_MATCH
```

### RPC-wrapper: `libmediatek_tv_rpcwrapper.so`

- `Machine:                           ARM`
- DT_NEEDED contains the following selected dependencies: `libmediatek_tv_rpccore.so`
- Focused symbol rows (type, value, size, section, symbol):

```text
626: 00076959   188 FUNC    GLOBAL DEFAULT   14 a_mtktvapi_config_get_value
676: 00076a15   196 FUNC    GLOBAL DEFAULT   14 a_mtktvapi_config_set_value
2263: 00076ad9   196 FUNC    GLOBAL DEFAULT   14 a_mtktvapi_config_set_value_for_all_elems
```

- Relevant relocations:

```text
NO_MATCH
```

### HIDL interface: `vendor.mediatek.api.mtktvapi@1.0.so`

- `Machine:                           ARM`
- DT_NEEDED contains the following selected dependencies: `libhidlbase.so`
- Focused symbol rows (type, value, size, section, symbol):

```text
620: 0024c991   152 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig34a_hidl_a_mtktvapi_config_get_valueEsRKN7android8hardware11hidl_stringENSt3__18functionIFviiEEE
670: 0024f6cd   708 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig54_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elemsEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE
701: 0024da31   532 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_get_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE
1202: 0024c55d   924 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig54_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elemsEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis
1924: 0024dc45   708 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE
2633: 0024a129   924 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis
3100: 0024ca29    52 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig34a_hidl_a_mtktvapi_config_set_valueEsRKN7android8hardware11hidl_stringEis
3106: 0024cd59    52 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig48a_hidl_a_mtktvapi_config_set_value_for_all_elemsEsRKN7android8hardware11hidl_stringEis
3530: 00249e91   664 FUNC    GLOBAL DEFAULT   14 _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_get_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringENSt3__18functionIFviiEEE
```

- Relevant relocations:

```text
00314638  000dca16 R_ARM_JUMP_SLOT        00249e91   _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_get_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringENSt3__18functionIFviiEEE
0031463c  000a4916 R_ARM_JUMP_SLOT        0024a129   _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis
0031466c  0004b216 R_ARM_JUMP_SLOT        0024c55d   _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig54_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elemsEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis
0031467c  0002bd16 R_ARM_JUMP_SLOT        0024da31   _ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_get_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE
00314680  00078416 R_ARM_JUMP_SLOT        0024dc45   _ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE
003146b0  00029e16 R_ARM_JUMP_SLOT        0024f6cd   _ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig54_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elemsEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE
```

- ABI-derived C++ signatures from **exported** HIDL symbols:

```text
vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elems(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elems(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, android::hardware::hidl_string const&, int, short)
vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, android::hardware::hidl_string const&, int, short)
vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_value(short, android::hardware::hidl_string const&, int, short)
vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_value_for_all_elems(short, android::hardware::hidl_string const&, int, short)
```

## C. Scoped machine disassembly

Instruction addresses below are ELF virtual addresses, not runtime ASLR addresses.
Raw calling instructions are shown as evidence; indirect register branches
must not be assigned a target without a verified register/dataflow trace.

### JNI setConfigValue

- Symbol: `Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native`
- Demangled: `Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native`
- Scoped instructions: 45;
 call-like instructions: 9

```asm
   566dc:	b580      	push	{r7, lr}
   566de:	b086      	sub	sp, #24
   566e0:	f8dd c024 	ldr.w	ip, [sp, #36]	@ 0x24
   566e4:	f8dd c020 	ldr.w	ip, [sp, #32]
   566e8:	9005      	str	r0, [sp, #20]
   566ea:	9104      	str	r1, [sp, #16]
   566ec:	9203      	str	r2, [sp, #12]
   566ee:	9302      	str	r3, [sp, #8]
   566f0:	2000      	movs	r0, #0
   566f2:	9001      	str	r0, [sp, #4]
   566f4:	9001      	str	r0, [sp, #4]
   566f6:	9000      	str	r0, [sp, #0]
   566f8:	9000      	str	r0, [sp, #0]
   566fa:	e7ff      	b.n	566fc <Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native@@Base+0x20>
   566fc:	e7ff      	b.n	566fe <Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native@@Base+0x22>
   566fe:	9802      	ldr	r0, [sp, #8]
   56700:	b1e8      	cbz	r0, 5673e <Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native@@Base+0x62>
   56702:	e7ff      	b.n	56704 <Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native@@Base+0x28>
   56704:	9805      	ldr	r0, [sp, #20]
   56706:	6801      	ldr	r1, [r0, #0]
   56708:	f8d1 32a4 	ldr.w	r3, [r1, #676]	@ 0x2a4
   5670c:	9902      	ldr	r1, [sp, #8]
   5670e:	2200      	movs	r2, #0
   56710:	4798      	blx	r3
   56712:	9000      	str	r0, [sp, #0]
   56714:	9800      	ldr	r0, [sp, #0]
   56716:	b150      	cbz	r0, 5672e <Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native@@Base+0x52>
   56718:	e7ff      	b.n	5671a <Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native@@Base+0x3e>
   5671a:	f9bd 000c 	ldrsh.w	r0, [sp, #12]
   5671e:	9900      	ldr	r1, [sp, #0]
   56720:	9a08      	ldr	r2, [sp, #32]
   56722:	f9bd 3024 	ldrsh.w	r3, [sp, #36]	@ 0x24
   56726:	f024 ebb4 	blx	7ae90 <jni_testsample_deinit@@Base+0x181e>
   5672a:	9001      	str	r0, [sp, #4]
   5672c:	e7ff      	b.n	5672e <Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native@@Base+0x52>
   5672e:	9805      	ldr	r0, [sp, #20]
   56730:	6801      	ldr	r1, [r0, #0]
   56732:	f8d1 32a8 	ldr.w	r3, [r1, #680]	@ 0x2a8
   56736:	9902      	ldr	r1, [sp, #8]
   56738:	9a00      	ldr	r2, [sp, #0]
   5673a:	4798      	blx	r3
   5673c:	e7ff      	b.n	5673e <Java_com_mediatek_twoworlds_tv_TVNative_setConfigValue_1native@@Base+0x62>
   5673e:	9801      	ldr	r0, [sp, #4]
   56740:	b006      	add	sp, #24
   56742:	bd80      	pop	{r7, pc}

```

### C-wrapper config set

- Symbol: `a_mtktvapi_config_set_value`
- Demangled: `a_mtktvapi_config_set_value`
- Scoped instructions: 68;
 call-like instructions: 15

```asm

Disassembly of section .text:

0003f584 <a_mtktvapi_config_set_value@@Base>:
   3f584:	b5f0      	push	{r4, r5, r6, r7, lr}
   3f586:	b091      	sub	sp, #68	@ 0x44
   3f588:	4604      	mov	r4, r0
   3f58a:	4826      	ldr	r0, [pc, #152]	@ (3f624 <a_mtktvapi_config_set_value@@Base+0xa0>)
   3f58c:	461d      	mov	r5, r3
   3f58e:	4616      	mov	r6, r2
   3f590:	460f      	mov	r7, r1
   3f592:	4478      	add	r0, pc
   3f594:	6800      	ldr	r0, [r0, #0]
   3f596:	6800      	ldr	r0, [r0, #0]
   3f598:	9010      	str	r0, [sp, #64]	@ 0x40
   3f59a:	f04f 30ff 	mov.w	r0, #4294967295	@ 0xffffffff
   3f59e:	900b      	str	r0, [sp, #44]	@ 0x2c
   3f5a0:	2000      	movs	r0, #0
   3f5a2:	900a      	str	r0, [sp, #40]	@ 0x28
   3f5a4:	a80a      	add	r0, sp, #40	@ 0x28
   3f5a6:	f020 ed74 	blx	60090 <a_mtktvapi_cas_nagra_get_info@@Base+0x788>
   3f5aa:	a80c      	add	r0, sp, #48	@ 0x30
   3f5ac:	efc0 0050 	vmov.i32	q8, #0	@ 0x00000000
   3f5b0:	f940 0acf 	vst1.64	{d16-d17}, [r0]
   3f5b4:	4639      	mov	r1, r7
   3f5b6:	f020 eb4c 	blx	5fc50 <a_mtktvapi_cas_nagra_get_info@@Base+0x348>
   3f5ba:	990a      	ldr	r1, [sp, #40]	@ 0x28
   3f5bc:	b181      	cbz	r1, 3f5e0 <a_mtktvapi_config_set_value@@Base+0x5c>
   3f5be:	6808      	ldr	r0, [r1, #0]
   3f5c0:	6bc7      	ldr	r7, [r0, #60]	@ 0x3c
   3f5c2:	e9cd 6500 	strd	r6, r5, [sp]
   3f5c6:	a803      	add	r0, sp, #12
   3f5c8:	ab0c      	add	r3, sp, #48	@ 0x30
   3f5ca:	4622      	mov	r2, r4
   3f5cc:	47b8      	blx	r7
   3f5ce:	a803      	add	r0, sp, #12
   3f5d0:	a90b      	add	r1, sp, #44	@ 0x2c
   3f5d2:	f020 ebce 	blx	5fd70 <a_mtktvapi_cas_nagra_get_info@@Base+0x468>
   3f5d6:	a803      	add	r0, sp, #12
   3f5d8:	f020 eb42 	blx	5fc60 <a_mtktvapi_cas_nagra_get_info@@Base+0x358>
   3f5dc:	9c0b      	ldr	r4, [sp, #44]	@ 0x2c
   3f5de:	e001      	b.n	3f5e4 <a_mtktvapi_config_set_value@@Base+0x60>
   3f5e0:	f04f 34ff 	mov.w	r4, #4294967295	@ 0xffffffff
   3f5e4:	a80c      	add	r0, sp, #48	@ 0x30
   3f5e6:	f020 eb44 	blx	5fc70 <a_mtktvapi_cas_nagra_get_info@@Base+0x368>
   3f5ea:	a80a      	add	r0, sp, #40	@ 0x28
   3f5ec:	f020 ec80 	blx	5fef0 <a_mtktvapi_cas_nagra_get_info@@Base+0x5e8>
   3f5f0:	9810      	ldr	r0, [sp, #64]	@ 0x40
   3f5f2:	490d      	ldr	r1, [pc, #52]	@ (3f628 <a_mtktvapi_config_set_value@@Base+0xa4>)
   3f5f4:	4479      	add	r1, pc
   3f5f6:	6809      	ldr	r1, [r1, #0]
   3f5f8:	6809      	ldr	r1, [r1, #0]
   3f5fa:	4281      	cmp	r1, r0
   3f5fc:	d102      	bne.n	3f604 <a_mtktvapi_config_set_value@@Base+0x80>
   3f5fe:	4620      	mov	r0, r4
   3f600:	b011      	add	sp, #68	@ 0x44
   3f602:	bdf0      	pop	{r4, r5, r6, r7, pc}
   3f604:	f020 ead4 	blx	5fbb0 <a_mtktvapi_cas_nagra_get_info@@Base+0x2a8>
   3f608:	a803      	add	r0, sp, #12
   3f60a:	f020 eb2a 	blx	5fc60 <a_mtktvapi_cas_nagra_get_info@@Base+0x358>
   3f60e:	e7ff      	b.n	3f610 <a_mtktvapi_config_set_value@@Base+0x8c>
   3f610:	a80c      	add	r0, sp, #48	@ 0x30
   3f612:	f020 eb2e 	blx	5fc70 <a_mtktvapi_cas_nagra_get_info@@Base+0x368>
   3f616:	e7ff      	b.n	3f618 <a_mtktvapi_config_set_value@@Base+0x94>
   3f618:	a80a      	add	r0, sp, #40	@ 0x28
   3f61a:	f020 ec6a 	blx	5fef0 <a_mtktvapi_cas_nagra_get_info@@Base+0x5e8>
   3f61e:	f020 ead0 	blx	5fbc0 <a_mtktvapi_cas_nagra_get_info@@Base+0x2b8>
   3f622:	bf00      	nop
   3f624:	345a      	adds	r4, #90	@ 0x5a
   3f626:	0002      	movs	r2, r0
   3f628:	33f8      	adds	r3, #248	@ 0xf8
```

### RPC-wrapper config set (alternative)

- Symbol: `a_mtktvapi_config_set_value`
- Demangled: `a_mtktvapi_config_set_value`
- Scoped instructions: 78;
 call-like instructions: 5

```asm

Disassembly of section .text:

00076a14 <a_mtktvapi_config_set_value@@Base>:
   76a14:	b5f0      	push	{r4, r5, r6, r7, lr}
   76a16:	b097      	sub	sp, #92	@ 0x5c
   76a18:	461d      	mov	r5, r3
   76a1a:	4b2b      	ldr	r3, [pc, #172]	@ (76ac8 <a_mtktvapi_config_set_value@@Base+0xb4>)
   76a1c:	efc0 0050 	vmov.i32	q8, #0	@ 0x00000000
   76a20:	2608      	movs	r6, #8
   76a22:	447b      	add	r3, pc
   76a24:	681b      	ldr	r3, [r3, #0]
   76a26:	681b      	ldr	r3, [r3, #0]
   76a28:	9316      	str	r3, [sp, #88]	@ 0x58
   76a2a:	af02      	add	r7, sp, #8
   76a2c:	f947 0acf 	vst1.64	{d16-d17}, [r7]
   76a30:	ab06      	add	r3, sp, #24
   76a32:	f103 0430 	add.w	r4, r3, #48	@ 0x30
   76a36:	f944 0a86 	vst1.32	{d16-d17}, [r4], r6
   76a3a:	1d1e      	adds	r6, r3, #4
   76a3c:	f946 0a8d 	vst1.32	{d16-d17}, [r6]!
   76a40:	8025      	strh	r5, [r4, #0]
   76a42:	2504      	movs	r5, #4
   76a44:	f04f 34ff 	mov.w	r4, #4294967295	@ 0xffffffff
   76a48:	f946 0a8d 	vst1.32	{d16-d17}, [r6]!
   76a4c:	e9cd 7400 	strd	r7, r4, [sp]
   76a50:	f946 0a85 	vst1.32	{d16-d17}, [r6], r5
   76a54:	f8ad 0020 	strh.w	r0, [sp, #32]
   76a58:	2044      	movs	r0, #68	@ 0x44
   76a5a:	910c      	str	r1, [sp, #48]	@ 0x30
   76a5c:	6032      	str	r2, [r6, #0]
   76a5e:	2243      	movs	r2, #67	@ 0x43
   76a60:	2660      	movs	r6, #96	@ 0x60
   76a62:	900e      	str	r0, [sp, #56]	@ 0x38
   76a64:	2000      	movs	r0, #0
   76a66:	9206      	str	r2, [sp, #24]
   76a68:	9212      	str	r2, [sp, #72]	@ 0x48
   76a6a:	960a      	str	r6, [sp, #40]	@ 0x28
   76a6c:	2204      	movs	r2, #4
   76a6e:	4917      	ldr	r1, [pc, #92]	@ (76acc <a_mtktvapi_config_set_value@@Base+0xb8>)
   76a70:	4479      	add	r1, pc
   76a72:	f032 ed1e 	blx	a94b0 <__rpc_get_desc__@@Base+0xe4>
   76a76:	4605      	mov	r5, r0
   76a78:	3005      	adds	r0, #5
   76a7a:	d10f      	bne.n	76a9c <a_mtktvapi_config_set_value@@Base+0x88>
   76a7c:	f032 ed20 	blx	a94c0 <__rpc_get_desc__@@Base+0xf4>
   76a80:	2801      	cmp	r0, #1
   76a82:	db0a      	blt.n	76a9a <a_mtktvapi_config_set_value@@Base+0x86>
   76a84:	f04f 30ff 	mov.w	r0, #4294967295	@ 0xffffffff
   76a88:	2204      	movs	r2, #4
   76a8a:	e9cd 7000 	strd	r7, r0, [sp]
   76a8e:	2000      	movs	r0, #0
   76a90:	490f      	ldr	r1, [pc, #60]	@ (76ad0 <a_mtktvapi_config_set_value@@Base+0xbc>)
   76a92:	ab06      	add	r3, sp, #24
   76a94:	4479      	add	r1, pc
   76a96:	f032 ed0c 	blx	a94b0 <__rpc_get_desc__@@Base+0xe4>
   76a9a:	4605      	mov	r5, r0
   76a9c:	9e02      	ldr	r6, [sp, #8]
   76a9e:	9f04      	ldr	r7, [sp, #16]
   76aa0:	2000      	movs	r0, #0
   76aa2:	f032 ed16 	blx	a94d0 <__rpc_get_desc__@@Base+0x104>
   76aa6:	f086 0004 	eor.w	r0, r6, #4
   76aaa:	4328      	orrs	r0, r5
   76aac:	bf08      	it	eq
   76aae:	463c      	moveq	r4, r7
   76ab0:	9816      	ldr	r0, [sp, #88]	@ 0x58
   76ab2:	4908      	ldr	r1, [pc, #32]	@ (76ad4 <a_mtktvapi_config_set_value@@Base+0xc0>)
   76ab4:	4479      	add	r1, pc
   76ab6:	6809      	ldr	r1, [r1, #0]
   76ab8:	6809      	ldr	r1, [r1, #0]
   76aba:	4281      	cmp	r1, r0
   76abc:	d102      	bne.n	76ac4 <a_mtktvapi_config_set_value@@Base+0xb0>
   76abe:	4620      	mov	r0, r4
   76ac0:	b017      	add	sp, #92	@ 0x5c
   76ac2:	bdf0      	pop	{r4, r5, r6, r7, pc}
   76ac4:	f032 ed0c 	blx	a94e0 <__rpc_get_desc__@@Base+0x114>
   76ac8:	b8e2      			@ <UNDEFINED> instruction: 0xb8e2
   76aca:	0003      	movs	r3, r0
   76acc:	83f9      	strh	r1, [r7, #30]
   76ace:	fffb 83d5 	vrsra.u64	q12, <illegal reg q2.5>, #5
```

### HIDL proxy/stub config set

- Symbol: `_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE`
- Demangled: `vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)`
- Scoped instructions: 285;
 call-like instructions: 37

```asm

Disassembly of section .text:

0024dc44 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base>:
  24dc44:	e92d 4ff0 	stmdb	sp!, {r4, r5, r6, r7, r8, r9, sl, fp, lr}
  24dc48:	b095      	sub	sp, #84	@ 0x54
  24dc4a:	4606      	mov	r6, r0
  24dc4c:	48a2      	ldr	r0, [pc, #648]	@ (24ded8 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x294>)
  24dc4e:	460d      	mov	r5, r1
  24dc50:	469b      	mov	fp, r3
  24dc52:	4691      	mov	r9, r2
  24dc54:	4478      	add	r0, pc
  24dc56:	6800      	ldr	r0, [r0, #0]
  24dc58:	6800      	ldr	r0, [r0, #0]
  24dc5a:	9014      	str	r0, [sp, #80]	@ 0x50
  24dc5c:	f896 8024 	ldrb.w	r8, [r6, #36]	@ 0x24
  24dc60:	489e      	ldr	r0, [pc, #632]	@ (24dedc <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x298>)
  24dc62:	4478      	add	r0, pc
  24dc64:	6800      	ldr	r0, [r0, #0]
  24dc66:	6801      	ldr	r1, [r0, #0]
  24dc68:	4628      	mov	r0, r5
  24dc6a:	f0a9 ede2 	blx	2f7830 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x1836>
  24dc6e:	2800      	cmp	r0, #0
  24dc70:	f000 8089 	beq.w	24dd86 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x142>
  24dc74:	f10d 0a4e 	add.w	sl, sp, #78	@ 0x4e
  24dc78:	2400      	movs	r4, #0
  24dc7a:	4628      	mov	r0, r5
  24dc7c:	4651      	mov	r1, sl
  24dc7e:	f8ad 404e 	strh.w	r4, [sp, #78]	@ 0x4e
  24dc82:	e9cd 4411 	strd	r4, r4, [sp, #68]	@ 0x44
  24dc86:	f8ad 4042 	strh.w	r4, [sp, #66]	@ 0x42
  24dc8a:	f0a8 ef8a 	blx	2f6ba0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0xba6>
  24dc8e:	4607      	mov	r7, r0
  24dc90:	2800      	cmp	r0, #0
  24dc92:	d17b      	bne.n	24dd8c <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x148>
  24dc94:	940f      	str	r4, [sp, #60]	@ 0x3c
  24dc96:	aa0f      	add	r2, sp, #60	@ 0x3c
  24dc98:	ab12      	add	r3, sp, #72	@ 0x48
  24dc9a:	4628      	mov	r0, r5
  24dc9c:	2110      	movs	r1, #16
  24dc9e:	f0a9 ee28 	blx	2f78f0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x18f6>
  24dca2:	4607      	mov	r7, r0
  24dca4:	2800      	cmp	r0, #0
  24dca6:	d171      	bne.n	24dd8c <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x148>
  24dca8:	9a0f      	ldr	r2, [sp, #60]	@ 0x3c
  24dcaa:	9812      	ldr	r0, [sp, #72]	@ 0x48
  24dcac:	4629      	mov	r1, r5
  24dcae:	2300      	movs	r3, #0
  24dcb0:	f0a9 ee4e 	blx	2f7950 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x1956>
  24dcb4:	4607      	mov	r7, r0
  24dcb6:	2800      	cmp	r0, #0
  24dcb8:	d168      	bne.n	24dd8c <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x148>
  24dcba:	ac11      	add	r4, sp, #68	@ 0x44
  24dcbc:	4628      	mov	r0, r5
  24dcbe:	4621      	mov	r1, r4
  24dcc0:	f0a8 ebc6 	blx	2f6450 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x456>
  24dcc4:	4607      	mov	r7, r0
  24dcc6:	2800      	cmp	r0, #0
  24dcc8:	d160      	bne.n	24dd8c <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x148>
  24dcca:	f10d 0142 	add.w	r1, sp, #66	@ 0x42
  24dcce:	4628      	mov	r0, r5
  24dcd0:	460d      	mov	r5, r1
  24dcd2:	f0a8 ef66 	blx	2f6ba0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0xba6>
  24dcd6:	4607      	mov	r7, r0
  24dcd8:	2800      	cmp	r0, #0
  24dcda:	d157      	bne.n	24dd8c <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x148>
  24dcdc:	f0a8 eb40 	blx	2f6360 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x366>
  24dce0:	0500      	lsls	r0, r0, #20
  24dce2:	d45e      	bmi.n	24dda2 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x15e>
  24dce4:	f1b8 0f00 	cmp.w	r8, #0
  24dce8:	d162      	bne.n	24ddb0 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x16c>
  24dcea:	6c30      	ldr	r0, [r6, #64]	@ 0x40
  24dcec:	9005      	str	r0, [sp, #20]
  24dcee:	6801      	ldr	r1, [r0, #0]
  24dcf0:	f851 1c0c 	ldr.w	r1, [r1, #-12]
  24dcf4:	4408      	add	r0, r1
  24dcf6:	a905      	add	r1, sp, #20
  24dcf8:	f0a8 eab2 	blx	2f6260 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x266>
  24dcfc:	9905      	ldr	r1, [sp, #20]
  24dcfe:	f9bd 5042 	ldrsh.w	r5, [sp, #66]	@ 0x42
  24dd02:	9b12      	ldr	r3, [sp, #72]	@ 0x48
  24dd04:	f9bd 204e 	ldrsh.w	r2, [sp, #78]	@ 0x4e
  24dd08:	6808      	ldr	r0, [r1, #0]
  24dd0a:	6bc7      	ldr	r7, [r0, #60]	@ 0x3c
  24dd0c:	9811      	ldr	r0, [sp, #68]	@ 0x44
  24dd0e:	e9cd 0500 	strd	r0, r5, [sp]
  24dd12:	ad06      	add	r5, sp, #24
  24dd14:	4628      	mov	r0, r5
  24dd16:	47b8      	blx	r7
  24dd18:	4628      	mov	r0, r5
  24dd1a:	f0a8 eaca 	blx	2f62b0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x2b6>
  24dd1e:	9c0c      	ldr	r4, [sp, #48]	@ 0x30
  24dd20:	4628      	mov	r0, r5
  24dd22:	f0a8 eae6 	blx	2f62f0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x2f6>
  24dd26:	9805      	ldr	r0, [sp, #20]
  24dd28:	b130      	cbz	r0, 24dd38 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0xf4>
  24dd2a:	6801      	ldr	r1, [r0, #0]
  24dd2c:	f851 1c0c 	ldr.w	r1, [r1, #-12]
  24dd30:	4408      	add	r0, r1
  24dd32:	a905      	add	r1, sp, #20
  24dd34:	f0a8 ea9c 	blx	2f6270 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x276>
  24dd38:	940e      	str	r4, [sp, #56]	@ 0x38
  24dd3a:	ad06      	add	r5, sp, #24
  24dd3c:	4628      	mov	r0, r5
  24dd3e:	f0a9 ed98 	blx	2f7870 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x1876>
  24dd42:	4628      	mov	r0, r5
  24dd44:	4649      	mov	r1, r9
  24dd46:	f0a9 ed9c 	blx	2f7880 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x1886>
  24dd4a:	f89d 0020 	ldrb.w	r0, [sp, #32]
  24dd4e:	07c0      	lsls	r0, r0, #31
  24dd50:	d002      	beq.n	24dd58 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x114>
  24dd52:	980a      	ldr	r0, [sp, #40]	@ 0x28
  24dd54:	f0a8 e9f4 	blx	2f6140 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x146>
  24dd58:	4648      	mov	r0, r9
  24dd5a:	4621      	mov	r1, r4
  24dd5c:	f0a8 eba0 	blx	2f64a0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x4a6>
  24dd60:	4607      	mov	r7, r0
  24dd62:	f0a8 eafe 	blx	2f6360 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x366>
  24dd66:	0500      	lsls	r0, r0, #20
  24dd68:	d437      	bmi.n	24ddda <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x196>
  24dd6a:	f1b8 0f00 	cmp.w	r8, #0
  24dd6e:	d139      	bne.n	24dde4 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x1a0>
  24dd70:	b967      	cbnz	r7, 24dd8c <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x148>
  24dd72:	f8db 0010 	ldr.w	r0, [fp, #16]
  24dd76:	2800      	cmp	r0, #0
  24dd78:	f000 80ab 	beq.w	24ded2 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x28e>
  24dd7c:	6801      	ldr	r1, [r0, #0]
  24dd7e:	698a      	ldr	r2, [r1, #24]
  24dd80:	4649      	mov	r1, r9
  24dd82:	4790      	blx	r2
  24dd84:	e002      	b.n	24dd8c <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x148>
  24dd86:	2701      	movs	r7, #1
  24dd88:	f2c8 0700 	movt	r7, #32768	@ 0x8000
  24dd8c:	9814      	ldr	r0, [sp, #80]	@ 0x50
  24dd8e:	495d      	ldr	r1, [pc, #372]	@ (24df04 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x2c0>)
  24dd90:	4479      	add	r1, pc
  24dd92:	6809      	ldr	r1, [r1, #0]
  24dd94:	6809      	ldr	r1, [r1, #0]
  24dd96:	4281      	cmp	r1, r0
  24dd98:	d152      	bne.n	24de40 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x1fc>
  24dd9a:	4638      	mov	r0, r7
  24dd9c:	b015      	add	sp, #84	@ 0x54
  24dd9e:	e8bd 8ff0 	ldmia.w	sp!, {r4, r5, r6, r7, r8, r9, sl, fp, pc}
  24dda2:	484f      	ldr	r0, [pc, #316]	@ (24dee0 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x29c>)
  24dda4:	4478      	add	r0, pc
  24dda6:	f0a8 eb2c 	blx	2f6400 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x406>
  24ddaa:	f1b8 0f00 	cmp.w	r8, #0
  24ddae:	d09c      	beq.n	24dcea <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0xa6>
  24ddb0:	2000      	movs	r0, #0
  24ddb2:	f8cd a038 	str.w	sl, [sp, #56]	@ 0x38
  24ddb6:	e9cd 0006 	strd	r0, r0, [sp, #24]
  24ddba:	9008      	str	r0, [sp, #32]
  24ddbc:	a806      	add	r0, sp, #24
  24ddbe:	a90e      	add	r1, sp, #56	@ 0x38
  24ddc0:	f0a8 eb26 	blx	2f6410 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x416>
  24ddc4:	e9dd 1207 	ldrd	r1, r2, [sp, #28]
  24ddc8:	9812      	ldr	r0, [sp, #72]	@ 0x48
  24ddca:	4291      	cmp	r1, r2
  24ddcc:	900e      	str	r0, [sp, #56]	@ 0x38
  24ddce:	d239      	bcs.n	24de44 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE@@Base+0x200>
```
- Symbol: `_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis`
- Demangled: `vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, android::hardware::hidl_string const&, int, short)`
- Scoped instructions: 355;
 call-like instructions: 46

```asm

0024a128 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base>:
  24a128:	e92d 4ff0 	stmdb	sp!, {r4, r5, r6, r7, r8, r9, sl, fp, lr}
  24a12c:	b081      	sub	sp, #4
  24a12e:	ed2d 8b04 	vpush	{d8-d9}
  24a132:	b0cc      	sub	sp, #304	@ 0x130
  24a134:	4604      	mov	r4, r0
  24a136:	48d7      	ldr	r0, [pc, #860]	@ (24a494 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x36c>)
  24a138:	4693      	mov	fp, r2
  24a13a:	4689      	mov	r9, r1
  24a13c:	4478      	add	r0, pc
  24a13e:	6800      	ldr	r0, [r0, #0]
  24a140:	6800      	ldr	r0, [r0, #0]
  24a142:	904b      	str	r0, [sp, #300]	@ 0x12c
  24a144:	985c      	ldr	r0, [sp, #368]	@ 0x170
  24a146:	f8ad 310e 	strh.w	r3, [sp, #270]	@ 0x10e
  24a14a:	7c15      	ldrb	r5, [r2, #16]
  24a14c:	f8ad 010c 	strh.w	r0, [sp, #268]	@ 0x10c
  24a150:	f0ac e906 	blx	2f6360 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x366>
  24a154:	0500      	lsls	r0, r0, #20
  24a156:	f100 80cb 	bmi.w	24a2f0 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x1c8>
  24a15a:	f8dd 8168 	ldr.w	r8, [sp, #360]	@ 0x168
  24a15e:	2d00      	cmp	r5, #0
  24a160:	f040 80cb 	bne.w	24a2fa <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x1d2>
  24a164:	ae32      	add	r6, sp, #200	@ 0xc8
  24a166:	ef80 8050 	vmov.i32	q4, #0	@ 0x00000000
  24a16a:	2700      	movs	r7, #0
  24a16c:	4630      	mov	r0, r6
  24a16e:	f900 8acd 	vst1.64	{d8-d9}, [r0]!
  24a172:	f900 8acd 	vst1.64	{d8-d9}, [r0]!
  24a176:	f900 8acd 	vst1.64	{d8-d9}, [r0]!
  24a17a:	f900 8acd 	vst1.64	{d8-d9}, [r0]!
  24a17e:	6007      	str	r7, [r0, #0]
  24a180:	4630      	mov	r0, r6
  24a182:	f0ac e8f6 	blx	2f6370 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x376>
  24a186:	a820      	add	r0, sp, #128	@ 0x80
  24a188:	4601      	mov	r1, r0
  24a18a:	f901 8acd 	vst1.64	{d8-d9}, [r1]!
  24a18e:	f901 8acd 	vst1.64	{d8-d9}, [r1]!
  24a192:	f901 8acd 	vst1.64	{d8-d9}, [r1]!
  24a196:	f901 8acd 	vst1.64	{d8-d9}, [r1]!
  24a19a:	600f      	str	r7, [r1, #0]
  24a19c:	f0ac e8e8 	blx	2f6370 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x376>
  24a1a0:	f10d 0a68 	add.w	sl, sp, #104	@ 0x68
  24a1a4:	9719      	str	r7, [sp, #100]	@ 0x64
  24a1a6:	4650      	mov	r0, sl
  24a1a8:	f900 8acd 	vst1.64	{d8-d9}, [r0]!
  24a1ac:	6007      	str	r7, [r0, #0]
  24a1ae:	48bf      	ldr	r0, [pc, #764]	@ (24a4ac <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x384>)
  24a1b0:	4478      	add	r0, pc
  24a1b2:	6800      	ldr	r0, [r0, #0]
  24a1b4:	6801      	ldr	r1, [r0, #0]
  24a1b6:	4630      	mov	r0, r6
  24a1b8:	f0ac e8e2 	blx	2f6380 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x386>
  24a1bc:	4606      	mov	r6, r0
  24a1be:	2800      	cmp	r0, #0
  24a1c0:	d155      	bne.n	24a26e <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x146>
  24a1c2:	f9bd 110e 	ldrsh.w	r1, [sp, #270]	@ 0x10e
  24a1c6:	a832      	add	r0, sp, #200	@ 0xc8
  24a1c8:	f0ac ed12 	blx	2f6bf0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0xbf6>
  24a1cc:	4606      	mov	r6, r0
  24a1ce:	2800      	cmp	r0, #0
  24a1d0:	d14d      	bne.n	24a26e <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x146>
  24a1d2:	2000      	movs	r0, #0
  24a1d4:	4641      	mov	r1, r8
  24a1d6:	2210      	movs	r2, #16
  24a1d8:	9018      	str	r0, [sp, #96]	@ 0x60
  24a1da:	a832      	add	r0, sp, #200	@ 0xc8
  24a1dc:	ab18      	add	r3, sp, #96	@ 0x60
  24a1de:	f0ac e968 	blx	2f64b0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x4b6>
  24a1e2:	4606      	mov	r6, r0
  24a1e4:	2800      	cmp	r0, #0
  24a1e6:	d142      	bne.n	24a26e <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x146>
  24a1e8:	9a18      	ldr	r2, [sp, #96]	@ 0x60
  24a1ea:	a932      	add	r1, sp, #200	@ 0xc8
  24a1ec:	4640      	mov	r0, r8
  24a1ee:	2300      	movs	r3, #0
  24a1f0:	f0ac e98e 	blx	2f6510 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x516>
  24a1f4:	4606      	mov	r6, r0
  24a1f6:	bbd0      	cbnz	r0, 24a26e <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x146>
  24a1f8:	995b      	ldr	r1, [sp, #364]	@ 0x16c
  24a1fa:	a832      	add	r0, sp, #200	@ 0xc8
  24a1fc:	f0ac e950 	blx	2f64a0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x4a6>
  24a200:	4606      	mov	r6, r0
  24a202:	bba0      	cbnz	r0, 24a26e <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x146>
  24a204:	f9bd 110c 	ldrsh.w	r1, [sp, #268]	@ 0x10c
  24a208:	a832      	add	r0, sp, #200	@ 0xc8
  24a20a:	f0ac ecf2 	blx	2f6bf0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0xbf6>
  24a20e:	4606      	mov	r6, r0
  24a210:	bb68      	cbnz	r0, 24a26e <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x146>
  24a212:	a80e      	add	r0, sp, #56	@ 0x38
  24a214:	4649      	mov	r1, r9
  24a216:	f0ac e8d4 	blx	2f63c0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x3c6>
  24a21a:	980e      	ldr	r0, [sp, #56]	@ 0x38
  24a21c:	2100      	movs	r1, #0
  24a21e:	f50d 7888 	add.w	r8, sp, #272	@ 0x110
  24a222:	9148      	str	r1, [sp, #288]	@ 0x120
  24a224:	6802      	ldr	r2, [r0, #0]
  24a226:	6816      	ldr	r6, [r2, #0]
  24a228:	e9cd 1800 	strd	r1, r8, [sp]
  24a22c:	aa32      	add	r2, sp, #200	@ 0xc8
  24a22e:	ab20      	add	r3, sp, #128	@ 0x80
  24a230:	2103      	movs	r1, #3
  24a232:	47b0      	blx	r6
  24a234:	4606      	mov	r6, r0
  24a236:	9848      	ldr	r0, [sp, #288]	@ 0x120
  24a238:	4580      	cmp	r8, r0
  24a23a:	d002      	beq.n	24a242 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x11a>
  24a23c:	b130      	cbz	r0, 24a24c <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x124>
  24a23e:	2105      	movs	r1, #5
  24a240:	e000      	b.n	24a244 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x11c>
  24a242:	2104      	movs	r1, #4
  24a244:	6802      	ldr	r2, [r0, #0]
  24a246:	f852 1021 	ldr.w	r1, [r2, r1, lsl #2]
  24a24a:	4788      	blx	r1
  24a24c:	980e      	ldr	r0, [sp, #56]	@ 0x38
  24a24e:	b130      	cbz	r0, 24a25e <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x136>
  24a250:	6801      	ldr	r1, [r0, #0]
  24a252:	f851 1c0c 	ldr.w	r1, [r1, #-12]
  24a256:	4408      	add	r0, r1
  24a258:	a90e      	add	r1, sp, #56	@ 0x38
  24a25a:	f0ac e80a 	blx	2f6270 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x276>
  24a25e:	b936      	cbnz	r6, 24a26e <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x146>
  24a260:	a81a      	add	r0, sp, #104	@ 0x68
  24a262:	a920      	add	r1, sp, #128	@ 0x80
  24a264:	f0ac e8b4 	blx	2f63d0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x3d6>
  24a268:	4606      	mov	r6, r0
  24a26a:	2800      	cmp	r0, #0
  24a26c:	d061      	beq.n	24a332 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x20a>
  24a26e:	4650      	mov	r0, sl
  24a270:	4631      	mov	r1, r6
  24a272:	f0ac e8b6 	blx	2f63e0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x3e6>
  24a276:	e9dd 011a 	ldrd	r0, r1, [sp, #104]	@ 0x68
  24a27a:	e9cd 0108 	strd	r0, r1, [sp, #32]
  24a27e:	a808      	add	r0, sp, #32
  24a280:	f10a 0108 	add.w	r1, sl, #8
  24a284:	f100 0508 	add.w	r5, r0, #8
  24a288:	4628      	mov	r0, r5
  24a28a:	f0ac e80a 	blx	2f62a0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x2a6>
  24a28e:	e9dd 0108 	ldrd	r0, r1, [sp, #32]
  24a292:	e9c4 0100 	strd	r0, r1, [r4]
  24a296:	f104 0008 	add.w	r0, r4, #8
  24a29a:	4629      	mov	r1, r5
  24a29c:	f0ac e800 	blx	2f62a0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x2a6>
  24a2a0:	f89d 0028 	ldrb.w	r0, [sp, #40]	@ 0x28
  24a2a4:	61a7      	str	r7, [r4, #24]
  24a2a6:	7527      	strb	r7, [r4, #20]
  24a2a8:	07c0      	lsls	r0, r0, #31
  24a2aa:	d002      	beq.n	24a2b2 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x18a>
  24a2ac:	980c      	ldr	r0, [sp, #48]	@ 0x30
  24a2ae:	f0ab ef48 	blx	2f6140 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x146>
  24a2b2:	f89d 0070 	ldrb.w	r0, [sp, #112]	@ 0x70
  24a2b6:	07c0      	lsls	r0, r0, #31
  24a2b8:	d002      	beq.n	24a2c0 <_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis@@Base+0x198>
  24a2ba:	981e      	ldr	r0, [sp, #120]	@ 0x78
  24a2bc:	f0ab ef40 	blx	2f6140 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x146>
  24a2c0:	a820      	add	r0, sp, #128	@ 0x80
  24a2c2:	f0ac e896 	blx	2f63f0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x3f6>
  24a2c6:	a832      	add	r0, sp, #200	@ 0xc8
  24a2c8:	f0ac e892 	blx	2f63f0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x3f6>
```
- Symbol: `_ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig34a_hidl_a_mtktvapi_config_set_valueEsRKN7android8hardware11hidl_stringEis`
- Demangled: `vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_value(short, android::hardware::hidl_string const&, int, short)`
- Scoped instructions: 19;
 call-like instructions: 1

```asm
  24ca28:	b580      	push	{r7, lr}
  24ca2a:	b084      	sub	sp, #16
  24ca2c:	4694      	mov	ip, r2
  24ca2e:	efc0 0050 	vmov.i32	q8, #0	@ 0x00000000
  24ca32:	f04f 0e0c 	mov.w	lr, #12
  24ca36:	4602      	mov	r2, r0
  24ca38:	f942 0a8e 	vst1.32	{d16-d17}, [r2], lr
  24ca3c:	f942 0a8f 	vst1.32	{d16-d17}, [r2]
  24ca40:	9a07      	ldr	r2, [sp, #28]
  24ca42:	9202      	str	r2, [sp, #8]
  24ca44:	9a06      	ldr	r2, [sp, #24]
  24ca46:	e9cd 3200 	strd	r3, r2, [sp]
  24ca4a:	1d0b      	adds	r3, r1, #4
  24ca4c:	f101 0218 	add.w	r2, r1, #24
  24ca50:	4619      	mov	r1, r3
  24ca52:	4663      	mov	r3, ip
  24ca54:	f0ae eb3c 	blx	2fb0d0 <_ZN6vendor8mediatek3api8mtktvapi4V1_021writeEmbeddedToParcelERKNS3_27HIDL_MTKTVAPI_GAMCMD_INFO_TEPN7android8hardware6ParcelEjj@@Base+0x50d6>
  24ca58:	b004      	add	sp, #16
  24ca5a:	bd80      	pop	{r7, pc}

Disassembly of section .plt:
```

## D. Automated interpretation (limited)

- Direct labelled JNI branch to C-wrapper setter via PLT in scoped disassembly: **NOT_OBSERVED**.
- **NOT_OBSERVED is not proof of absence.** Inspect indirect BLX, interposition,
  veneers and JNI wrappers manually before ruling out the edge.
- The C-wrapper and RPC-wrapper both export similarly named functions, so
  ownership cannot be inferred by symbol name alone.
- JNI status mapping, JNI method static/instance declaration, inputGroup=-1,
  isUpdate=3, HIDL service implementation, persisted config, and physical PQ
  effect require additional callsites or device evidence.
- Neither the projector nor native library functions were executed.

