# NATIVE-005: exact 8054 C-wrapper / HIDL vtable, proxy, server search

Read-only machine observations from SHA-256-gated owner firmware.
No claim of runtime Binder/HIDL reachability or physical display behavior.

## Provenance and commands

- Archive SHA-256 checked by workflow before EROFS extraction: `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362`.
- `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so`: `e470029a7e5d52ea25d17be2fb255e2dfe1fe75d6fe6cc527b3146001a3c8758` (verified).
- `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0.so`: `a48159c7c66e5e82a4d293bc1e59e878cb0cfafbd8f645d872332f10424711f7` (verified).
- `vendor_a/lib/libmediatek_tv_rpcwrapper.so`: `d8938a5733c5cacd32de03eef8dafd5ea16acb9d6386bfdc7974323782b8f62b` (verified).
- Tool versions: `GNU readelf (GNU Binutils for Ubuntu) 2.46`; `GNU objdump (GNU Binutils for Ubuntu) 2.46`.
- `readelf -h -WsW -rW -SW`; `arm-linux-gnueabihf-objdump -drw --disassemble=SYMBOL`; `c++filt`.

## A. C-wrapper virtual dispatch boundary

- `a_mtktvapi_config_set_value` imported by JNI; C-wrapper function at `0x3f584` (Thumb; symbol 0x3f585).
- C-wrapper `0x3f5c0: ldr r7, [r0,#60]`, `0x3f5cc: blx r7` is a virtual call at byte offset **+60**.
- The static HIDL client method corresponding to that virtual slot still requires proof from vtable/relocation.

```asm
   3f584:	b5f0      	push	{r4, r5, r6, r7, lr}
   3f586:	b091      	sub	sp, #68	@ 0x44
   3f588:	4604      	mov	r4, r0
   3f58a:	4826      	ldr	r0, [pc, #152]	@ (3f624 <nearest-symbol-label-elided>)
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
   3f5a6:	f020 ed74 	blx	60090 <nearest-symbol-label-elided>
   3f5aa:	a80c      	add	r0, sp, #48	@ 0x30
   3f5ac:	efc0 0050 	vmov.i32	q8, #0	@ 0x00000000
   3f5b0:	f940 0acf 	vst1.64	{d16-d17}, [r0]
   3f5b4:	4639      	mov	r1, r7
   3f5b6:	f020 eb4c 	blx	5fc50 <nearest-symbol-label-elided>
   3f5ba:	990a      	ldr	r1, [sp, #40]	@ 0x28
   3f5bc:	b181      	cbz	r1, 3f5e0 <nearest-symbol-label-elided>
   3f5be:	6808      	ldr	r0, [r1, #0]
   3f5c0:	6bc7      	ldr	r7, [r0, #60]	@ 0x3c
   3f5c2:	e9cd 6500 	strd	r6, r5, [sp]
   3f5c6:	a803      	add	r0, sp, #12
   3f5c8:	ab0c      	add	r3, sp, #48	@ 0x30
   3f5ca:	4622      	mov	r2, r4
   3f5cc:	47b8      	blx	r7
   3f5ce:	a803      	add	r0, sp, #12
   3f5d0:	a90b      	add	r1, sp, #44	@ 0x2c
   3f5d2:	f020 ebce 	blx	5fd70 <nearest-symbol-label-elided>
   3f5d6:	a803      	add	r0, sp, #12
   3f5d8:	f020 eb42 	blx	5fc60 <nearest-symbol-label-elided>
   3f5dc:	9c0b      	ldr	r4, [sp, #44]	@ 0x2c
   3f5de:	e001      	b.n	3f5e4 <nearest-symbol-label-elided>
   3f5e0:	f04f 34ff 	mov.w	r4, #4294967295	@ 0xffffffff
   3f5e4:	a80c      	add	r0, sp, #48	@ 0x30
   3f5e6:	f020 eb44 	blx	5fc70 <nearest-symbol-label-elided>
   3f5ea:	a80a      	add	r0, sp, #40	@ 0x28
   3f5ec:	f020 ec80 	blx	5fef0 <nearest-symbol-label-elided>
   3f5f0:	9810      	ldr	r0, [sp, #64]	@ 0x40
   3f5f2:	490d      	ldr	r1, [pc, #52]	@ (3f628 <nearest-symbol-label-elided>)
   3f5f4:	4479      	add	r1, pc
   3f5f6:	6809      	ldr	r1, [r1, #0]
   3f5f8:	6809      	ldr	r1, [r1, #0]
   3f5fa:	4281      	cmp	r1, r0
   3f5fc:	d102      	bne.n	3f604 <nearest-symbol-label-elided>
   3f600:	b011      	add	sp, #68	@ 0x44
   3f602:	bdf0      	pop	{r4, r5, r6, r7, pc}
   3f604:	f020 ead4 	blx	5fbb0 <nearest-symbol-label-elided>
   3f608:	a803      	add	r0, sp, #12
   3f60a:	f020 eb2a 	blx	5fc60 <nearest-symbol-label-elided>
   3f60e:	e7ff      	b.n	3f610 <nearest-symbol-label-elided>
   3f610:	a80c      	add	r0, sp, #48	@ 0x30
   3f612:	f020 eb2e 	blx	5fc70 <nearest-symbol-label-elided>
   3f616:	e7ff      	b.n	3f618 <nearest-symbol-label-elided>
   3f618:	a80a      	add	r0, sp, #40	@ 0x28
   3f61a:	f020 ec6a 	blx	5fef0 <nearest-symbol-label-elided>
   3f61e:	f020 ead0 	blx	5fbc0 <nearest-symbol-label-elided>
   3f622:	bf00      	nop
   3f624:	345a      	adds	r4, #90	@ 0x5a
```

## B. HIDL BpHw/BnHw exports, vtables and relocations

### Exact methods (demangled)

```text
0024f6cd size=708 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elems(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
0024c55d size=924 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elems(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, android::hardware::hidl_string const&, int, short)
0024dc45 size=708 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
0024a129 size=924 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, android::hardware::hidl_string const&, int, short)
0024ca29 size=52 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_value(short, android::hardware::hidl_string const&, int, short)
0024cd59 size=52 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_value_for_all_elems(short, android::hardware::hidl_string const&, int, short)
```

### Exact vtables / method relocation candidates

```text
992: 00309f7c   128 OBJECT  GLOBAL DEFAULT   16 _ZTVN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfigE  vtable for vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig
1053: 0030a01c   192 OBJECT  WEAK   DEFAULT   16 _ZTVN6vendor8mediatek3api8mtktvapi4V1_016BsMtkTvApiConfigE  vtable for vendor::mediatek::api::mtktvapi::V1_0::BsMtkTvApiConfig
1062: 00309e3c   256 OBJECT  GLOBAL DEFAULT   16 _ZTVN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfigE  vtable for vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig
1638: 0030a0dc   176 OBJECT  GLOBAL DEFAULT   16 _ZTVN6vendor8mediatek3api8mtktvapi4V1_015IMtkTvApiConfigE  vtable for vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig
```

- Relocations naming config-set functions (avoid interpreting a relocation as a specific vtable edge without its section/offset):

```text
0031463c  000a4916 R_ARM_JUMP_SLOT        0024a129   _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis
0031466c  0004b216 R_ARM_JUMP_SLOT        0024c55d   _ZN6vendor8mediatek3api8mtktvapi4V1_018BpHwMtkTvApiConfig54_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elemsEPN7android8hardware10IInterfaceEPNS6_7details16HidlInstrumentorEsRKNS6_11hidl_stringEis
00314680  00078416 R_ARM_JUMP_SLOT        0024dc45   _ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig40_hidl_a_hidl_a_mtktvapi_config_set_valueEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE
003146b0  00029e16 R_ARM_JUMP_SLOT        0024f6cd   _ZN6vendor8mediatek3api8mtktvapi4V1_018BnHwMtkTvApiConfig54_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elemsEPN7android4hidl4base4V1_08BnHwBaseERKNS5_8hardware6ParcelEPSC_NSt3__18functionIFvRSC_EEE
```


### BpHw concrete virtual method

- Exact function: `vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_value(short, android::hardware::hidl_string const&, int, short)`
- ELF address `0x0024ca29`; size 52

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
  24ca54:	f0ae eb3c 	blx	2fb0d0 <nearest-symbol-label-elided>
  24ca58:	b004      	add	sp, #16
  24ca5a:	bd80      	pop	{r7, pc}
```

### BpHw outbound HIDL proxy implementation

- Exact function: `vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, android::hardware::hidl_string const&, int, short)`
- ELF address `0x0024a129`; size 924

```asm
  24a128:	e92d 4ff0 	stmdb	sp!, {r4, r5, r6, r7, r8, r9, sl, fp, lr}
  24a12c:	b081      	sub	sp, #4
  24a12e:	ed2d 8b04 	vpush	{d8-d9}
  24a132:	b0cc      	sub	sp, #304	@ 0x130
  24a134:	4604      	mov	r4, r0
  24a136:	48d7      	ldr	r0, [pc, #860]	@ (24a494 <nearest-symbol-label-elided>)
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
  24a150:	f0ac e906 	blx	2f6360 <nearest-symbol-label-elided>
  24a154:	0500      	lsls	r0, r0, #20
  24a156:	f100 80cb 	bmi.w	24a2f0 <nearest-symbol-label-elided>
  24a15a:	f8dd 8168 	ldr.w	r8, [sp, #360]	@ 0x168
  24a15e:	2d00      	cmp	r5, #0
  24a160:	f040 80cb 	bne.w	24a2fa <nearest-symbol-label-elided>
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
  24a182:	f0ac e8f6 	blx	2f6370 <nearest-symbol-label-elided>
  24a186:	a820      	add	r0, sp, #128	@ 0x80
  24a188:	4601      	mov	r1, r0
  24a192:	f901 8acd 	vst1.64	{d8-d9}, [r1]!
  24a196:	f901 8acd 	vst1.64	{d8-d9}, [r1]!
  24a19a:	600f      	str	r7, [r1, #0]
  24a19c:	f0ac e8e8 	blx	2f6370 <nearest-symbol-label-elided>
  24a1a0:	f10d 0a68 	add.w	sl, sp, #104	@ 0x68
  24a1a4:	9719      	str	r7, [sp, #100]	@ 0x64
  24a1a6:	4650      	mov	r0, sl
  24a1a8:	f900 8acd 	vst1.64	{d8-d9}, [r0]!
  24a1ac:	6007      	str	r7, [r0, #0]
  24a1ae:	48bf      	ldr	r0, [pc, #764]	@ (24a4ac <nearest-symbol-label-elided>)
  24a1b0:	4478      	add	r0, pc
  24a1b2:	6800      	ldr	r0, [r0, #0]
  24a1b4:	6801      	ldr	r1, [r0, #0]
  24a1b6:	4630      	mov	r0, r6
  24a1b8:	f0ac e8e2 	blx	2f6380 <nearest-symbol-label-elided>
  24a1bc:	4606      	mov	r6, r0
  24a1be:	2800      	cmp	r0, #0
  24a1c0:	d155      	bne.n	24a26e <nearest-symbol-label-elided>
  24a1c2:	f9bd 110e 	ldrsh.w	r1, [sp, #270]	@ 0x10e
  24a1c6:	a832      	add	r0, sp, #200	@ 0xc8
  24a1c8:	f0ac ed12 	blx	2f6bf0 <nearest-symbol-label-elided>
  24a1cc:	4606      	mov	r6, r0
  24a1ce:	2800      	cmp	r0, #0
  24a1d0:	d14d      	bne.n	24a26e <nearest-symbol-label-elided>
  24a1d2:	2000      	movs	r0, #0
  24a1d4:	4641      	mov	r1, r8
  24a1d6:	2210      	movs	r2, #16
  24a1d8:	9018      	str	r0, [sp, #96]	@ 0x60
  24a1da:	a832      	add	r0, sp, #200	@ 0xc8
  24a1dc:	ab18      	add	r3, sp, #96	@ 0x60
  24a1de:	f0ac e968 	blx	2f64b0 <nearest-symbol-label-elided>
  24a1e2:	4606      	mov	r6, r0
  24a1e4:	2800      	cmp	r0, #0
  24a1e6:	d142      	bne.n	24a26e <nearest-symbol-label-elided>
  24a1e8:	9a18      	ldr	r2, [sp, #96]	@ 0x60
  24a1ea:	a932      	add	r1, sp, #200	@ 0xc8
  24a1ec:	4640      	mov	r0, r8
  24a1ee:	2300      	movs	r3, #0
  24a1f0:	f0ac e98e 	blx	2f6510 <nearest-symbol-label-elided>
  24a1f4:	4606      	mov	r6, r0
  24a1f6:	bbd0      	cbnz	r0, 24a26e <nearest-symbol-label-elided>
  24a1f8:	995b      	ldr	r1, [sp, #364]	@ 0x16c
  24a1fa:	a832      	add	r0, sp, #200	@ 0xc8
  24a1fc:	f0ac e950 	blx	2f64a0 <nearest-symbol-label-elided>
  24a200:	4606      	mov	r6, r0
  24a202:	bba0      	cbnz	r0, 24a26e <nearest-symbol-label-elided>
  24a204:	f9bd 110c 	ldrsh.w	r1, [sp, #268]	@ 0x10c
  24a208:	a832      	add	r0, sp, #200	@ 0xc8
  24a20a:	f0ac ecf2 	blx	2f6bf0 <nearest-symbol-label-elided>
  24a20e:	4606      	mov	r6, r0
  24a210:	bb68      	cbnz	r0, 24a26e <nearest-symbol-label-elided>
  24a212:	a80e      	add	r0, sp, #56	@ 0x38
  24a214:	4649      	mov	r1, r9
  24a216:	f0ac e8d4 	blx	2f63c0 <nearest-symbol-label-elided>
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
  24a23a:	d002      	beq.n	24a242 <nearest-symbol-label-elided>
  24a23c:	b130      	cbz	r0, 24a24c <nearest-symbol-label-elided>
  24a23e:	2105      	movs	r1, #5
  24a240:	e000      	b.n	24a244 <nearest-symbol-label-elided>
  24a242:	2104      	movs	r1, #4
  24a244:	6802      	ldr	r2, [r0, #0]
  24a246:	f852 1021 	ldr.w	r1, [r2, r1, lsl #2]
  24a24a:	4788      	blx	r1
  24a24c:	980e      	ldr	r0, [sp, #56]	@ 0x38
  24a24e:	b130      	cbz	r0, 24a25e <nearest-symbol-label-elided>
  24a250:	6801      	ldr	r1, [r0, #0]
  24a252:	f851 1c0c 	ldr.w	r1, [r1, #-12]
  24a256:	4408      	add	r0, r1
  24a258:	a90e      	add	r1, sp, #56	@ 0x38
  24a25a:	f0ac e80a 	blx	2f6270 <nearest-symbol-label-elided>
  24a25e:	b936      	cbnz	r6, 24a26e <nearest-symbol-label-elided>
  24a260:	a81a      	add	r0, sp, #104	@ 0x68
  24a262:	a920      	add	r1, sp, #128	@ 0x80
  24a264:	f0ac e8b4 	blx	2f63d0 <nearest-symbol-label-elided>
  24a268:	4606      	mov	r6, r0
  24a26a:	2800      	cmp	r0, #0
  24a26c:	d061      	beq.n	24a332 <nearest-symbol-label-elided>
  24a26e:	4650      	mov	r0, sl
  24a270:	4631      	mov	r1, r6
  24a272:	f0ac e8b6 	blx	2f63e0 <nearest-symbol-label-elided>
  24a276:	e9dd 011a 	ldrd	r0, r1, [sp, #104]	@ 0x68
  24a27a:	e9cd 0108 	strd	r0, r1, [sp, #32]
  24a284:	f100 0508 	add.w	r5, r0, #8
  24a288:	4628      	mov	r0, r5
  24a28a:	f0ac e80a 	blx	2f62a0 <nearest-symbol-label-elided>
  24a28e:	e9dd 0108 	ldrd	r0, r1, [sp, #32]
  24a292:	e9c4 0100 	strd	r0, r1, [r4]
  24a296:	f104 0008 	add.w	r0, r4, #8
  24a29a:	4629      	mov	r1, r5
  24a29c:	f0ac e800 	blx	2f62a0 <nearest-symbol-label-elided>
  24a2a0:	f89d 0028 	ldrb.w	r0, [sp, #40]	@ 0x28
  24a2a4:	61a7      	str	r7, [r4, #24]
  24a2a6:	7527      	strb	r7, [r4, #20]
  24a2a8:	07c0      	lsls	r0, r0, #31
  24a2aa:	d002      	beq.n	24a2b2 <nearest-symbol-label-elided>
  24a2ac:	980c      	ldr	r0, [sp, #48]	@ 0x30
  24a2ae:	f0ab ef48 	blx	2f6140 <nearest-symbol-label-elided>
  24a2b2:	f89d 0070 	ldrb.w	r0, [sp, #112]	@ 0x70
  24a2b6:	07c0      	lsls	r0, r0, #31
  24a2b8:	d002      	beq.n	24a2c0 <nearest-symbol-label-elided>
  24a2ba:	981e      	ldr	r0, [sp, #120]	@ 0x78
  24a2bc:	f0ab ef40 	blx	2f6140 <nearest-symbol-label-elided>
  24a2c0:	a820      	add	r0, sp, #128	@ 0x80
  24a2c2:	f0ac e896 	blx	2f63f0 <nearest-symbol-label-elided>
  24a2c6:	a832      	add	r0, sp, #200	@ 0xc8
  24a2c8:	f0ac e892 	blx	2f63f0 <nearest-symbol-label-elided>
  24a2cc:	f0ac e848 	blx	2f6360 <nearest-symbol-label-elided>
  24a2d0:	0500      	lsls	r0, r0, #20
  24a2d2:	d42b      	bmi.n	24a32c <nearest-symbol-label-elided>
  24a2d4:	984b      	ldr	r0, [sp, #300]	@ 0x12c
  24a2d6:	497a      	ldr	r1, [pc, #488]	@ (24a4c0 <nearest-symbol-label-elided>)
  24a2d8:	4479      	add	r1, pc
  24a2da:	6809      	ldr	r1, [r1, #0]
  24a2dc:	6809      	ldr	r1, [r1, #0]
  24a2de:	4281      	cmp	r1, r0
  24a2e0:	f040 80a8 	bne.w	24a434 <nearest-symbol-label-elided>
  24a2ea:	b001      	add	sp, #4
  24a2ec:	e8bd 8ff0 	ldmia.w	sp!, {r4, r5, r6, r7, r8, r9, sl, fp, pc}
  24a2f0:	4869      	ldr	r0, [pc, #420]	@ (24a498 <nearest-symbol-label-elided>)
  24a2f2:	4478      	add	r0, pc
  24a2f4:	f0ac e884 	blx	2f6400 <nearest-symbol-label-elided>
  24a2f8:	e72f      	b.n	24a15a <nearest-symbol-label-elided>
[172 additional selected lines not published; bounded excerpt]
```

### BnHw inbound HIDL parcel dispatch

- Exact function: `vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)`
- ELF address `0x0024dc45`; size 708

```asm
  24dc44:	e92d 4ff0 	stmdb	sp!, {r4, r5, r6, r7, r8, r9, sl, fp, lr}
  24dc48:	b095      	sub	sp, #84	@ 0x54
  24dc4a:	4606      	mov	r6, r0
  24dc4c:	48a2      	ldr	r0, [pc, #648]	@ (24ded8 <nearest-symbol-label-elided>)
  24dc4e:	460d      	mov	r5, r1
  24dc50:	469b      	mov	fp, r3
  24dc52:	4691      	mov	r9, r2
  24dc54:	4478      	add	r0, pc
  24dc56:	6800      	ldr	r0, [r0, #0]
  24dc58:	6800      	ldr	r0, [r0, #0]
  24dc5a:	9014      	str	r0, [sp, #80]	@ 0x50
  24dc5c:	f896 8024 	ldrb.w	r8, [r6, #36]	@ 0x24
  24dc60:	489e      	ldr	r0, [pc, #632]	@ (24dedc <nearest-symbol-label-elided>)
  24dc62:	4478      	add	r0, pc
  24dc64:	6800      	ldr	r0, [r0, #0]
  24dc66:	6801      	ldr	r1, [r0, #0]
  24dc68:	4628      	mov	r0, r5
  24dc6a:	f0a9 ede2 	blx	2f7830 <nearest-symbol-label-elided>
  24dc6e:	2800      	cmp	r0, #0
  24dc70:	f000 8089 	beq.w	24dd86 <nearest-symbol-label-elided>
  24dc74:	f10d 0a4e 	add.w	sl, sp, #78	@ 0x4e
  24dc78:	2400      	movs	r4, #0
  24dc7a:	4628      	mov	r0, r5
  24dc7c:	4651      	mov	r1, sl
  24dc7e:	f8ad 404e 	strh.w	r4, [sp, #78]	@ 0x4e
  24dc82:	e9cd 4411 	strd	r4, r4, [sp, #68]	@ 0x44
  24dc86:	f8ad 4042 	strh.w	r4, [sp, #66]	@ 0x42
  24dc8a:	f0a8 ef8a 	blx	2f6ba0 <nearest-symbol-label-elided>
  24dc8e:	4607      	mov	r7, r0
  24dc90:	2800      	cmp	r0, #0
  24dc92:	d17b      	bne.n	24dd8c <nearest-symbol-label-elided>
  24dc94:	940f      	str	r4, [sp, #60]	@ 0x3c
  24dc96:	aa0f      	add	r2, sp, #60	@ 0x3c
  24dc98:	ab12      	add	r3, sp, #72	@ 0x48
  24dc9a:	4628      	mov	r0, r5
  24dc9c:	2110      	movs	r1, #16
  24dc9e:	f0a9 ee28 	blx	2f78f0 <nearest-symbol-label-elided>
  24dca2:	4607      	mov	r7, r0
  24dca4:	2800      	cmp	r0, #0
  24dca6:	d171      	bne.n	24dd8c <nearest-symbol-label-elided>
  24dca8:	9a0f      	ldr	r2, [sp, #60]	@ 0x3c
  24dcaa:	9812      	ldr	r0, [sp, #72]	@ 0x48
  24dcac:	4629      	mov	r1, r5
  24dcae:	2300      	movs	r3, #0
  24dcb0:	f0a9 ee4e 	blx	2f7950 <nearest-symbol-label-elided>
  24dcb4:	4607      	mov	r7, r0
  24dcb6:	2800      	cmp	r0, #0
  24dcb8:	d168      	bne.n	24dd8c <nearest-symbol-label-elided>
  24dcba:	ac11      	add	r4, sp, #68	@ 0x44
  24dcbc:	4628      	mov	r0, r5
  24dcbe:	4621      	mov	r1, r4
  24dcc0:	f0a8 ebc6 	blx	2f6450 <nearest-symbol-label-elided>
  24dcc4:	4607      	mov	r7, r0
  24dcc6:	2800      	cmp	r0, #0
  24dcc8:	d160      	bne.n	24dd8c <nearest-symbol-label-elided>
  24dcca:	f10d 0142 	add.w	r1, sp, #66	@ 0x42
  24dcce:	4628      	mov	r0, r5
  24dcd0:	460d      	mov	r5, r1
  24dcd2:	f0a8 ef66 	blx	2f6ba0 <nearest-symbol-label-elided>
  24dcd6:	4607      	mov	r7, r0
  24dcd8:	2800      	cmp	r0, #0
  24dcda:	d157      	bne.n	24dd8c <nearest-symbol-label-elided>
  24dcdc:	f0a8 eb40 	blx	2f6360 <nearest-symbol-label-elided>
  24dce0:	0500      	lsls	r0, r0, #20
  24dce2:	d45e      	bmi.n	24dda2 <nearest-symbol-label-elided>
  24dce4:	f1b8 0f00 	cmp.w	r8, #0
  24dce8:	d162      	bne.n	24ddb0 <nearest-symbol-label-elided>
  24dcea:	6c30      	ldr	r0, [r6, #64]	@ 0x40
  24dcec:	9005      	str	r0, [sp, #20]
  24dcee:	6801      	ldr	r1, [r0, #0]
  24dcf0:	f851 1c0c 	ldr.w	r1, [r1, #-12]
  24dcf4:	4408      	add	r0, r1
  24dcf6:	a905      	add	r1, sp, #20
  24dcf8:	f0a8 eab2 	blx	2f6260 <nearest-symbol-label-elided>
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
  24dd1a:	f0a8 eaca 	blx	2f62b0 <nearest-symbol-label-elided>
  24dd1e:	9c0c      	ldr	r4, [sp, #48]	@ 0x30
  24dd20:	4628      	mov	r0, r5
  24dd22:	f0a8 eae6 	blx	2f62f0 <nearest-symbol-label-elided>
  24dd26:	9805      	ldr	r0, [sp, #20]
  24dd28:	b130      	cbz	r0, 24dd38 <nearest-symbol-label-elided>
  24dd2a:	6801      	ldr	r1, [r0, #0]
  24dd2c:	f851 1c0c 	ldr.w	r1, [r1, #-12]
  24dd30:	4408      	add	r0, r1
  24dd32:	a905      	add	r1, sp, #20
  24dd34:	f0a8 ea9c 	blx	2f6270 <nearest-symbol-label-elided>
  24dd38:	940e      	str	r4, [sp, #56]	@ 0x38
  24dd3a:	ad06      	add	r5, sp, #24
  24dd3c:	4628      	mov	r0, r5
  24dd3e:	f0a9 ed98 	blx	2f7870 <nearest-symbol-label-elided>
  24dd42:	4628      	mov	r0, r5
  24dd44:	4649      	mov	r1, r9
  24dd46:	f0a9 ed9c 	blx	2f7880 <nearest-symbol-label-elided>
  24dd4a:	f89d 0020 	ldrb.w	r0, [sp, #32]
  24dd4e:	07c0      	lsls	r0, r0, #31
  24dd50:	d002      	beq.n	24dd58 <nearest-symbol-label-elided>
  24dd52:	980a      	ldr	r0, [sp, #40]	@ 0x28
  24dd54:	f0a8 e9f4 	blx	2f6140 <nearest-symbol-label-elided>
  24dd58:	4648      	mov	r0, r9
  24dd5a:	4621      	mov	r1, r4
  24dd5c:	f0a8 eba0 	blx	2f64a0 <nearest-symbol-label-elided>
  24dd60:	4607      	mov	r7, r0
  24dd62:	f0a8 eafe 	blx	2f6360 <nearest-symbol-label-elided>
  24dd66:	0500      	lsls	r0, r0, #20
  24dd68:	d437      	bmi.n	24ddda <nearest-symbol-label-elided>
  24dd6a:	f1b8 0f00 	cmp.w	r8, #0
  24dd6e:	d139      	bne.n	24dde4 <nearest-symbol-label-elided>
  24dd70:	b967      	cbnz	r7, 24dd8c <nearest-symbol-label-elided>
  24dd72:	f8db 0010 	ldr.w	r0, [fp, #16]
  24dd76:	2800      	cmp	r0, #0
  24dd78:	f000 80ab 	beq.w	24ded2 <nearest-symbol-label-elided>
  24dd7c:	6801      	ldr	r1, [r0, #0]
  24dd7e:	698a      	ldr	r2, [r1, #24]
  24dd80:	4649      	mov	r1, r9
  24dd82:	4790      	blx	r2
  24dd84:	e002      	b.n	24dd8c <nearest-symbol-label-elided>
  24dd86:	2701      	movs	r7, #1
  24dd88:	f2c8 0700 	movt	r7, #32768	@ 0x8000
  24dd8c:	9814      	ldr	r0, [sp, #80]	@ 0x50
  24dd8e:	495d      	ldr	r1, [pc, #372]	@ (24df04 <nearest-symbol-label-elided>)
  24dd90:	4479      	add	r1, pc
  24dd92:	6809      	ldr	r1, [r1, #0]
  24dd94:	6809      	ldr	r1, [r1, #0]
  24dd96:	4281      	cmp	r1, r0
  24dd98:	d152      	bne.n	24de40 <nearest-symbol-label-elided>
  24dd9c:	b015      	add	sp, #84	@ 0x54
  24dd9e:	e8bd 8ff0 	ldmia.w	sp!, {r4, r5, r6, r7, r8, r9, sl, fp, pc}
  24dda2:	484f      	ldr	r0, [pc, #316]	@ (24dee0 <nearest-symbol-label-elided>)
  24dda4:	4478      	add	r0, pc
  24dda6:	f0a8 eb2c 	blx	2f6400 <nearest-symbol-label-elided>
  24ddaa:	f1b8 0f00 	cmp.w	r8, #0
  24ddae:	d09c      	beq.n	24dcea <nearest-symbol-label-elided>
  24ddb0:	2000      	movs	r0, #0
  24ddb2:	f8cd a038 	str.w	sl, [sp, #56]	@ 0x38
  24ddb6:	e9cd 0006 	strd	r0, r0, [sp, #24]
  24ddba:	9008      	str	r0, [sp, #32]
  24ddbc:	a806      	add	r0, sp, #24
  24ddbe:	a90e      	add	r1, sp, #56	@ 0x38
  24ddc0:	f0a8 eb26 	blx	2f6410 <nearest-symbol-label-elided>
  24ddc4:	e9dd 1207 	ldrd	r1, r2, [sp, #28]
  24ddc8:	9812      	ldr	r0, [sp, #72]	@ 0x48
  24ddca:	4291      	cmp	r1, r2
  24ddcc:	900e      	str	r0, [sp, #56]	@ 0x38
  24ddce:	d239      	bcs.n	24de44 <nearest-symbol-label-elided>
  24ddd0:	6008      	str	r0, [r1, #0]
  24ddd2:	9807      	ldr	r0, [sp, #28]
  24ddd4:	3004      	adds	r0, #4
  24ddd6:	9007      	str	r0, [sp, #28]
  24ddd8:	e039      	b.n	24de4e <nearest-symbol-label-elided>
  24ddda:	f0a8 eb2a 	blx	2f6430 <nearest-symbol-label-elided>
  24ddde:	f1b8 0f00 	cmp.w	r8, #0
  24dde2:	d0c5      	beq.n	24dd70 <nearest-symbol-label-elided>
  24dde4:	2000      	movs	r0, #0
  24dde6:	e9cd 0006 	strd	r0, r0, [sp, #24]
  24ddea:	9008      	str	r0, [sp, #32]
  24ddec:	a80e      	add	r0, sp, #56	@ 0x38
  24ddee:	9005      	str	r0, [sp, #20]
  24ddf0:	a806      	add	r0, sp, #24
  24ddf2:	a905      	add	r1, sp, #20
[103 additional selected lines not published; bounded excerpt]
```

## C. Read-only search for exact-8054 HIDL service implementation

### Executable/implementation-file candidates by allowlisted basename

```text
system_ext_a: lib/vendor.mediatek.api.mtktvapi@1.0.so
system_ext_a: lib/vendor.mediatek.custom.mtktvapicustom@1.0.so
system_ext_a: lib/vendor.mediatek.tv.mtkdmservice@1.0.so
system_ext_a: lib/vendor.mediatek.tv.mtkmtalservice@1.0.so
vendor_a: bin/hw/vendor.mediatek.api.mtktvapi@1.0-service
vendor_a: bin/hw/vendor.mediatek.hardware.tv.networkproxy@1.0-service
vendor_a: bin/hw/vendor.mediatek.tv.cmpb@1.0-service
vendor_a: bin/hw/vendor.mediatek.tv.mtkcalman@1.0-service
vendor_a: bin/hw/vendor.mediatek.tv.mtkdmservice@1.0-service
vendor_a: bin/hw/vendor.mediatek.tv.mtkmtalservice@1.0-service
vendor_a: bin/hw/vendor.mediatek.tv.mtkrm@1.0-service
vendor_a: bin/hw/vendor.mediatek.tv.mtktvfactory@1.0-service
vendor_a: bin/hw/vendor.mediatek.tv.mtktvvoc@1.0-service
vendor_a: bin/hw/vendor.mediatek.tv.screenrecord@1.0-service
vendor_a: lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so
vendor_a: lib/vendor.mediatek.api.mtktvapi@1.0.so
vendor_a: lib/vendor.mediatek.custom.mtktvapicustom@1.0-cwrapper.so
vendor_a: lib/vendor.mediatek.custom.mtktvapicustom@1.0-impl.so
vendor_a: lib/vendor.mediatek.custom.mtktvapicustom@1.0.so
vendor_a: lib/vendor.mediatek.tv.mtkdmservice@1.0.so
vendor_a: lib/vendor.mediatek.tv.mtkmtalservice@1.0.so
```

### Defined/undefined exports suggesting concrete receiver (per ELF)


- `system_ext_a/lib/vendor.mediatek.api.mtktvapi@1.0.so`:

```text
14 FUNC 0024c8f9 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_get_minmaxvalue(short, android::hardware::hidl_string const&, std::__1::function<void (int, int)>)
14 FUNC 00249645 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::castFrom(android::sp<android::hidl::base::V1_0::IBase> const&, bool)
14 FUNC 00249571 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::notifySyspropsChanged()
14 FUNC 00249889 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::BpHwMtkTvApiConfig(android::sp<android::hardware::IBinder> const&)
16 OBJECT 0030a530 construction vtable for android::hardware::BHwBinder-in-vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig
14 FUNC 00249505 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getDebugInfo(std::__1::function<void (android::hidl::base::V1_0::DebugInfo const&)>)
14 FUNC 0024fa29 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::onTransact(unsigned int, android::hardware::Parcel const&, android::hardware::Parcel*, unsigned int, std::__1::function<void (android::hardware::Parcel&)>)
14 FUNC 002493c1 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getHashChain(std::__1::function<void (android::hardware::hidl_vec<android::hardware::hidl_array<unsigned char, 32u> > const&)>)
14 FUNC 0024df09 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_is_visible(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
14 FUNC 0024c019 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_android_world_info(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, short)
14 FUNC 0024d4d5 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::BnHwMtkTvApiConfig(android::sp<vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig> const&)
14 FUNC 0025032d vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getService(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&, bool)
14 FUNC 0024fead vendor::mediatek::api::mtktvapi::V1_0::BsMtkTvApiConfig::BsMtkTvApiConfig(android::sp<vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig>)
14 FUNC 0024c991 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_get_value(short, android::hardware::hidl_string const&, std::__1::function<void (int, int)>)
14 FUNC 0024f6cd vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elems(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
16 OBJECT 0030a18c VTT for vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig
16 OBJECT 0030a3ac construction vtable for android::hidl::base::V1_0::IBase-in-vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig
14 FUNC 0024da31 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_get_value(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
14 FUNC 0024cd8d vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::interfaceChain(std::__1::function<void (android::hardware::hidl_vec<android::hardware::hidl_string> const&)>)
14 FUNC 0024ccd1 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_get_video_resolution(std::__1::function<void (android::hardware::hidl_vec<unsigned char> const&, int)>)
14 FUNC 00249269 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::interfaceChain(std::__1::function<void (android::hardware::hidl_vec<android::hardware::hidl_string> const&)>)
14 FUNC 002503ad vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::registerForNotifications(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&, android::sp<android::hidl::manager::V1_0::IServiceNotification> const&)
14 FUNC 0024958d vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::unlinkToDeath(android::sp<android::hardware::hidl_death_recipient> const&)
14 FUNC 0024f991 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::ping()
14 FUNC 0024e5c1 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_get_str_value(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
14 FUNC 0024f2e9 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_android_world_info(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
16 OBJECT 00309f7c vtable for vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig
14 FUNC 0024a805 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_is_enabled(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, android::hardware::hidl_string const&)
16 OBJECT 0030a60c construction vtable for vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig-in-vendor::mediatek::api::mtktvapi::V1_0::BsMtkTvApiConfig
16 OBJECT 0030a01c vtable for vendor::mediatek::api::mtktvapi::V1_0::BsMtkTvApiConfig
16 OBJECT 00309e3c vtable for vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig
14 FUNC 0024d181 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::notifySyspropsChanged()
14 FUNC 0024cca5 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_android_world_info(short, short)
14 FUNC 0024d695 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::~BnHwMtkTvApiConfig()
14 FUNC 00250339 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::registerAsService(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&)
```

- `vendor_a/bin/hw/vendor.mediatek.api.mtktvapi@1.0-service`:

```text
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::registerAsService(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&)
UND FUNC 00000000 a_mtktvapi_config_set_value
UND FUNC 00000000 a_mtktvapi_config_set_value_for_all_elems
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::interfaceChain(std::__1::function<void (android::hardware::hidl_vec<android::hardware::hidl_string> const&)>)
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::debug(android::hardware::hidl_handle const&, android::hardware::hidl_vec<android::hardware::hidl_string> const&)
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::interfaceDescriptor(std::__1::function<void (android::hardware::hidl_string const&)>)
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getHashChain(std::__1::function<void (android::hardware::hidl_vec<android::hardware::hidl_array<unsigned char, 32u> > const&)>)
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::setHALInstrumentation()
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::linkToDeath(android::sp<android::hardware::hidl_death_recipient> const&, unsigned long long)
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::ping()
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getDebugInfo(std::__1::function<void (android::hidl::base::V1_0::DebugInfo const&)>)
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::notifySyspropsChanged()
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::unlinkToDeath(android::sp<android::hardware::hidl_death_recipient> const&)
```

- `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0-cwrapper.so`:

```text
UND FUNC 00000000 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getService(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&, bool)
14 FUNC 0003f62d a_mtktvapi_config_set_value_for_all_elems
14 FUNC 0003d28d android::sp<vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig>::~sp()
14 FUNC 0003ec77 android::sp<vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig>::operator=(vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig*)
14 FUNC 0003d815 android::sp<vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig>::operator=(android::sp<vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig>&&)
14 FUNC 0003f585 a_mtktvapi_config_set_value
14 FUNC 0003d741 getMtkTvApiConfigService()
```

- `vendor_a/lib/vendor.mediatek.api.mtktvapi@1.0.so`:

```text
14 FUNC 0024c8f9 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_get_minmaxvalue(short, android::hardware::hidl_string const&, std::__1::function<void (int, int)>)
14 FUNC 00249645 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::castFrom(android::sp<android::hidl::base::V1_0::IBase> const&, bool)
14 FUNC 00249571 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::notifySyspropsChanged()
14 FUNC 00249889 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::BpHwMtkTvApiConfig(android::sp<android::hardware::IBinder> const&)
16 OBJECT 0030a530 construction vtable for android::hardware::BHwBinder-in-vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig
14 FUNC 00249505 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getDebugInfo(std::__1::function<void (android::hidl::base::V1_0::DebugInfo const&)>)
14 FUNC 0024fa29 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::onTransact(unsigned int, android::hardware::Parcel const&, android::hardware::Parcel*, unsigned int, std::__1::function<void (android::hardware::Parcel&)>)
14 FUNC 002493c1 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getHashChain(std::__1::function<void (android::hardware::hidl_vec<android::hardware::hidl_array<unsigned char, 32u> > const&)>)
14 FUNC 0024df09 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_is_visible(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
14 FUNC 0024c019 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_android_world_info(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, short)
14 FUNC 0024d4d5 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::BnHwMtkTvApiConfig(android::sp<vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig> const&)
14 FUNC 0025032d vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::getService(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&, bool)
14 FUNC 0024fead vendor::mediatek::api::mtktvapi::V1_0::BsMtkTvApiConfig::BsMtkTvApiConfig(android::sp<vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig>)
14 FUNC 0024c991 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_get_value(short, android::hardware::hidl_string const&, std::__1::function<void (int, int)>)
14 FUNC 0024f6cd vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_value_for_all_elems(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
16 OBJECT 0030a18c VTT for vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig
16 OBJECT 0030a3ac construction vtable for android::hidl::base::V1_0::IBase-in-vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig
14 FUNC 0024da31 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_get_value(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
14 FUNC 0024cd8d vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::interfaceChain(std::__1::function<void (android::hardware::hidl_vec<android::hardware::hidl_string> const&)>)
14 FUNC 0024ccd1 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_get_video_resolution(std::__1::function<void (android::hardware::hidl_vec<unsigned char> const&, int)>)
14 FUNC 00249269 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::interfaceChain(std::__1::function<void (android::hardware::hidl_vec<android::hardware::hidl_string> const&)>)
14 FUNC 002503ad vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::registerForNotifications(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&, android::sp<android::hidl::manager::V1_0::IServiceNotification> const&)
14 FUNC 0024958d vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::unlinkToDeath(android::sp<android::hardware::hidl_death_recipient> const&)
14 FUNC 0024f991 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::ping()
14 FUNC 0024e5c1 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_get_str_value(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
14 FUNC 0024f2e9 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_set_android_world_info(android::hidl::base::V1_0::BnHwBase*, android::hardware::Parcel const&, android::hardware::Parcel*, std::__1::function<void (android::hardware::Parcel&)>)
16 OBJECT 00309f7c vtable for vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig
14 FUNC 0024a805 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::_hidl_a_hidl_a_mtktvapi_config_is_enabled(android::hardware::IInterface*, android::hardware::details::HidlInstrumentor*, short, android::hardware::hidl_string const&)
16 OBJECT 0030a60c construction vtable for vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig-in-vendor::mediatek::api::mtktvapi::V1_0::BsMtkTvApiConfig
16 OBJECT 0030a01c vtable for vendor::mediatek::api::mtktvapi::V1_0::BsMtkTvApiConfig
16 OBJECT 00309e3c vtable for vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig
14 FUNC 0024d181 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::notifySyspropsChanged()
14 FUNC 0024cca5 vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig::a_hidl_a_mtktvapi_config_set_android_world_info(short, short)
14 FUNC 0024d695 vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::~BnHwMtkTvApiConfig()
14 FUNC 00250339 vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig::registerAsService(std::__1::basic_string<char, std::__1::char_traits<char>, std::__1::allocator<char> > const&)
```

### Sanitized HIDL VINTF and init metadata

- `vendor_a/etc/vintf/manifest.xml`: `vendor.mediatek.api.mtktvapi` instances `['default', 'default', 'default', 'default', 'default', 'default', 'default', 'default', 'default', 'default', 'default', 'default', 'default', 'default', 'default']`
- `vendor_a/etc/vintf/manifest.xml`: `vendor.mediatek.custom.mtktvapicustom` instances `['default']`
- `vendor_a/etc/init/vendor.mediatek.tv.mtktvapi@1.0-service.rc`:
  - `service tv-mtktvapi-1-0 /vendor/bin/hw/vendor.mediatek.api.mtktvapi@1.0-service`
- `vendor_a/etc/init/vendor.mediatek.tv.mtktvfactory@1.0-service.rc`:
  - `service tv-mtktvfactory-1-0 /vendor/bin/hw/vendor.mediatek.tv.mtktvfactory@1.0-service`
- `vendor_a/etc/init/vendor.mediatek.tv.mtktvvoc@1.0-service.rc`:
  - `service tv-mtktvvoc-1-0 /vendor/bin/hw/vendor.mediatek.tv.mtktvvoc@1.0-service`

## C2. Raw vtable slot +60 from exact ELF .data.rel.ro

The Itanium C++ vtable address point is vtable symbol + 8 on ARM32.
Static pointers carry Thumb state in bit0; compare against exact ELF symbol values.

| Vtable | Vtable address | pointer at address-point + 60 | matching exported method |
|---|---|---|---|
| `vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig` | `0x309f7c` | `0x00000000` | `NO_EXACT_FUNCTION_MATCH` |
  - vtable slot `0x309fc0` ELF relocation: NO_NAMED_RELOCATION_FOUND
| `vendor::mediatek::api::mtktvapi::V1_0::BsMtkTvApiConfig` | `0x30a01c` | `0x002516f9` | `NO_EXACT_FUNCTION_MATCH` |
  - vtable slot `0x30a060` ELF relocation: 0030a060  .data.rel.ro + 0xbcd0
| `vendor::mediatek::api::mtktvapi::V1_0::BpHwMtkTvApiConfig` | `0x309e3c` | `0x00000000` | `NO_EXACT_FUNCTION_MATCH` |
  - vtable slot `0x309e80` ELF relocation: NO_NAMED_RELOCATION_FOUND
| `vendor::mediatek::api::mtktvapi::V1_0::IMtkTvApiConfig` | `0x30a0dc` | `0x00000000` | `NO_EXACT_FUNCTION_MATCH` |
  - vtable slot `0x30a120` ELF relocation: NO_NAMED_RELOCATION_FOUND

## C3. Original HIDL service binary import and registration evidence

- Exact service ELF SHA-256: `15360fa27a7039f57f6173cc28942d4458e9f4d0beeaa31620e71bc785f275f6`.
- Selected DT_NEEDED: `vendor.mediatek.api.mtktvapi@1.0.so`, `vendor.mediatek.custom.mtktvapicustom@1.0-impl.so`, `libhidlbase.so`, `libhidltransport.so`, `libmediatek_tv_basic.so`, `libmediatek_tv_rpcwrapper.so`
- Symbol `a_mtktvapi_config_set_value`: UND FUNC 00000000
- Symbol `a_mtktvapi_config_set_value_for_all_elems`: UND FUNC 00000000
- Selected **defined** service methods: 0 (absence is not proof of no implementation; symbols may be stripped).

```text
NO_MATCHING_EXPORTED_DEFINITION
```

- Relevant service relocations:

```text
00047b04  00000616 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_09IMtkTvApi17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b10  00000916 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_015IMtkTvApiConfig17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b14  00000a16 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_012IMtkTvApiCec17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b18  00000b16 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_013IMtkTvApiScan17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b1c  00000c16 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_018IMtkTvApiBroadcast17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b20  00000d16 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_015IMtkTvApiRating17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b24  00000e16 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_018IMtkTvApiTimeshift17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b28  00000f16 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_013IMtkTvApiTime17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b2c  00001016 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_014IMtkTvApiEvent17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b30  00001116 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_019IMtkTvApiPvrbrowser17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b34  00001216 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_017IMtkTvApiInputSrc17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b38  00001316 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_015IMtkTvApiRecord17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b3c  00001416 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_018IMtkTvApiMultiview17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
00047b40  00001516 R_ARM_JUMP_SLOT        00000000   _ZN6vendor8mediatek3api8mtktvapi4V1_020IMtkTvApiChannelList17registerAsServiceERKNSt3__112basic_stringIcNS5_11char_traitsIcEENS5_9allocatorIcEEEE
```


- Exact backend setter relocation(s):

```text
00047fa0  00014316 R_ARM_JUMP_SLOT        00000000   a_mtktvapi_config_set_value
```

- Backend setter PLT decoded: NOT_FOUND
## C4. C-wrapper getService() exported helper and PLT indirection

- Service getter: getMtkTvApiConfigService()

```asm
   3d740:	b5f0      	push	{r4, r5, r6, r7, lr}
   3d742:	b085      	sub	sp, #20
   3d744:	4604      	mov	r4, r0
   3d746:	482e      	ldr	r0, [pc, #184]	@ (3d800 <nearest-symbol-label-elided>)
   3d748:	4478      	add	r0, pc
   3d74a:	6800      	ldr	r0, [r0, #0]
   3d74c:	6800      	ldr	r0, [r0, #0]
   3d74e:	9004      	str	r0, [sp, #16]
   3d750:	4f2c      	ldr	r7, [pc, #176]	@ (3d804 <nearest-symbol-label-elided>)
   3d752:	447f      	add	r7, pc
   3d754:	6838      	ldr	r0, [r7, #0]
   3d756:	b108      	cbz	r0, 3d75c <nearest-symbol-label-elided>
   3d758:	6020      	str	r0, [r4, #0]
   3d75a:	e02d      	b.n	3d7b8 <nearest-symbol-label-elided>
   3d75c:	4668      	mov	r0, sp
   3d75e:	f7ff fe6f 	bl	3d440 <nearest-symbol-label-elided>
   3d762:	9800      	ldr	r0, [sp, #0]
   3d764:	f100 0520 	add.w	r5, r0, #32
   3d768:	4628      	mov	r0, r5
   3d76a:	f022 ec22 	blx	5ffb0 <nearest-symbol-label-elided>
   3d76e:	4668      	mov	r0, sp
   3d770:	f022 ec16 	blx	5ffa0 <nearest-symbol-label-elided>
   3d774:	6838      	ldr	r0, [r7, #0]
   3d776:	b9c8      	cbnz	r0, 3d7ac <nearest-symbol-label-elided>
   3d778:	4923      	ldr	r1, [pc, #140]	@ (3d808 <nearest-symbol-label-elided>)
   3d77a:	4668      	mov	r0, sp
   3d77c:	4479      	add	r1, pc
   3d77e:	f7ff ff87 	bl	3d690 <nearest-symbol-label-elided>
   3d782:	a803      	add	r0, sp, #12
   3d784:	4669      	mov	r1, sp
   3d786:	2200      	movs	r2, #0
   3d788:	f022 ec8a 	blx	600a0 <nearest-symbol-label-elided>
   3d78c:	481f      	ldr	r0, [pc, #124]	@ (3d80c <nearest-symbol-label-elided>)
   3d78e:	ae03      	add	r6, sp, #12
   3d790:	4631      	mov	r1, r6
   3d792:	4478      	add	r0, pc
   3d794:	f022 ec8c 	blx	600b0 <nearest-symbol-label-elided>
   3d798:	4630      	mov	r0, r6
   3d79a:	f022 ebaa 	blx	5fef0 <nearest-symbol-label-elided>
   3d79e:	f89d 0000 	ldrb.w	r0, [sp]
   3d7a2:	07c0      	lsls	r0, r0, #31
   3d7a4:	d002      	beq.n	3d7ac <nearest-symbol-label-elided>
   3d7a6:	9802      	ldr	r0, [sp, #8]
   3d7a8:	f022 e9e2 	blx	5fb70 <nearest-symbol-label-elided>
   3d7ac:	4628      	mov	r0, r5
   3d7ae:	f022 ec20 	blx	5fff0 <nearest-symbol-label-elided>
   3d7b2:	6838      	ldr	r0, [r7, #0]
   3d7b4:	6020      	str	r0, [r4, #0]
   3d7b6:	b130      	cbz	r0, 3d7c6 <nearest-symbol-label-elided>
   3d7b8:	6801      	ldr	r1, [r0, #0]
   3d7ba:	f851 1c0c 	ldr.w	r1, [r1, #-12]
   3d7be:	4408      	add	r0, r1
   3d7c0:	4621      	mov	r1, r4
   3d7c2:	f022 ec0e 	blx	5ffe0 <nearest-symbol-label-elided>
   3d7c6:	9804      	ldr	r0, [sp, #16]
   3d7c8:	4911      	ldr	r1, [pc, #68]	@ (3d810 <nearest-symbol-label-elided>)
   3d7ca:	4479      	add	r1, pc
   3d7cc:	6809      	ldr	r1, [r1, #0]
   3d7ce:	6809      	ldr	r1, [r1, #0]
   3d7d0:	4281      	cmp	r1, r0
   3d7d2:	d101      	bne.n	3d7d8 <nearest-symbol-label-elided>
   3d7d4:	b005      	add	sp, #20
   3d7d6:	bdf0      	pop	{r4, r5, r6, r7, pc}
   3d7d8:	f022 e9ea 	blx	5fbb0 <nearest-symbol-label-elided>
   3d7dc:	f89d 0000 	ldrb.w	r0, [sp]
   3d7e0:	07c0      	lsls	r0, r0, #31
   3d7e2:	d003      	beq.n	3d7ec <nearest-symbol-label-elided>
   3d7e4:	9802      	ldr	r0, [sp, #8]
   3d7e6:	f022 e9c4 	blx	5fb70 <nearest-symbol-label-elided>
   3d7ea:	e7ff      	b.n	3d7ec <nearest-symbol-label-elided>
   3d7ec:	4628      	mov	r0, r5
   3d7ee:	f022 ec00 	blx	5fff0 <nearest-symbol-label-elided>
   3d7f2:	f022 e9e6 	blx	5fbc0 <nearest-symbol-label-elided>
   3d7f6:	4668      	mov	r0, sp
   3d7f8:	f022 ebd2 	blx	5ffa0 <nearest-symbol-label-elided>
   3d7fc:	f022 e9e0 	blx	5fbc0 <nearest-symbol-label-elided>
   3d800:	52a4      	strh	r4, [r4, r2]
   3d802:	0002      	movs	r2, r0
   3d804:	679e      	str	r6, [r3, #120]	@ 0x78
   3d806:	0002      	movs	r2, r0
   3d808:	5fa9      	ldrsh	r1, [r5, r6]
```

## C5. HIDL transaction number evidence

Outbound BpHw setter `0x24a230 movs r1,#3` directly precedes `0x24a232 blx r6`, where r6 is obtained from the binder object's virtual slot. This strongly indicates **HIDL transaction code 3**.
This numeric 3 belongs to the HIDL dispatch **and is not evidence** that the distinct Java/C argument `isUpdate=3` means persistence.
Inbound BnHw onTransact dispatch should be separately cross-checked.

- Inbound onTransact: `vendor::mediatek::api::mtktvapi::V1_0::BnHwMtkTvApiConfig::onTransact(unsigned int, android::hardware::Parcel const&, android::hardware::Parcel*, unsigned int, std::__1::function<void (android::hardware::Parcel&)>)`

```asm
  24fa28:	e92d 43f0 	stmdb	sp!, {r4, r5, r6, r7, r8, r9, lr}
  24fa2c:	b0e9      	sub	sp, #420	@ 0x1a4
  24fa2e:	4606      	mov	r6, r0
  24fa30:	f8df 0470 	ldr.w	r0, [pc, #1136]	@ 24fea4 <nearest-symbol-label-elided>
  24fa34:	460f      	mov	r7, r1
  24fa36:	4699      	mov	r9, r3
  24fa38:	4615      	mov	r5, r2
  24fa3a:	4478      	add	r0, pc
  24fa3c:	6800      	ldr	r0, [r0, #0]
  24fa3e:	6800      	ldr	r0, [r0, #0]
  24fa40:	9068      	str	r0, [sp, #416]	@ 0x1a0
  24fa42:	1e48      	subs	r0, r1, #1
  24fa44:	9971      	ldr	r1, [sp, #452]	@ 0x1c4
  24fa46:	280e      	cmp	r0, #14
  24fa48:	d814      	bhi.n	24fa74 <nearest-symbol-label-elided>
  24fa4a:	e8df f000 	tbb	[pc, r0]
  24fa4e:	5e08      	ldrsh	r0, [r1, r0]
  24fa50:	6820      	ldr	r0, [r4, #0]
  24fa52:	722b      	strb	r3, [r5, #8]
  24fa54:	867c      	strh	r4, [r7, #50]	@ 0x32
  24fa56:	3690      	adds	r6, #144	@ 0x90
  24fa58:	409a      	lsls	r2, r3
  24fa5a:	a34a      	add	r3, pc, #296	@ (adr r3, 24fb84 <nearest-symbol-label-elided>)
  24fa5c:	0054      	lsls	r4, r2, #1
  24fa5e:	6908      	ldr	r0, [r1, #16]
  24fa60:	2800      	cmp	r0, #0
  24fa62:	f000 80b5 	beq.w	24fbd0 <nearest-symbol-label-elided>
  24fa66:	4281      	cmp	r1, r0
  24fa68:	f000 8178 	beq.w	24fd5c <nearest-symbol-label-elided>
  24fa6c:	6801      	ldr	r1, [r0, #0]
  24fa6e:	6889      	ldr	r1, [r1, #8]
  24fa70:	4788      	blx	r1
  24fa72:	e0ae      	b.n	24fbd2 <nearest-symbol-label-elided>
  24fa74:	6908      	ldr	r0, [r1, #16]
  24fa76:	f8dd 81c0 	ldr.w	r8, [sp, #448]	@ 0x1c0
  24fa7a:	2800      	cmp	r0, #0
  24fa7c:	f000 8093 	beq.w	24fba6 <nearest-symbol-label-elided>
  24fa80:	4281      	cmp	r1, r0
  24fa82:	f000 80c0 	beq.w	24fc06 <nearest-symbol-label-elided>
  24fa86:	6801      	ldr	r1, [r0, #0]
  24fa88:	6889      	ldr	r1, [r1, #8]
  24fa8a:	4788      	blx	r1
  24fa8c:	e08c      	b.n	24fba8 <nearest-symbol-label-elided>
  24fa8e:	6908      	ldr	r0, [r1, #16]
  24fa90:	2800      	cmp	r0, #0
  24fa92:	f000 808b 	beq.w	24fbac <nearest-symbol-label-elided>
  24fa96:	4281      	cmp	r1, r0
  24fa98:	f000 80d1 	beq.w	24fc3e <nearest-symbol-label-elided>
  24fa9c:	6801      	ldr	r1, [r0, #0]
  24fa9e:	6889      	ldr	r1, [r1, #8]
  24faa0:	4788      	blx	r1
  24faa2:	e084      	b.n	24fbae <nearest-symbol-label-elided>
  24faa4:	6908      	ldr	r0, [r1, #16]
  24faa6:	2800      	cmp	r0, #0
  24faa8:	f000 8083 	beq.w	24fbb2 <nearest-symbol-label-elided>
  24faac:	4281      	cmp	r1, r0
  24faae:	f000 80d8 	beq.w	24fc62 <nearest-symbol-label-elided>
  24fab2:	6801      	ldr	r1, [r0, #0]
  24fab4:	6889      	ldr	r1, [r1, #8]
  24fab6:	4788      	blx	r1
  24fab8:	e07c      	b.n	24fbb4 <nearest-symbol-label-elided>
  24faba:	6908      	ldr	r0, [r1, #16]
  24fabc:	2800      	cmp	r0, #0
  24fabe:	d07b      	beq.n	24fbb8 <nearest-symbol-label-elided>
  24fac0:	4281      	cmp	r1, r0
  24fac2:	f000 8107 	beq.w	24fcd4 <nearest-symbol-label-elided>
  24fac6:	6801      	ldr	r1, [r0, #0]
  24fac8:	6889      	ldr	r1, [r1, #8]
  24faca:	4788      	blx	r1
  24facc:	e075      	b.n	24fbba <nearest-symbol-label-elided>
  24face:	6908      	ldr	r0, [r1, #16]
  24fad0:	2800      	cmp	r0, #0
  24fad2:	d074      	beq.n	24fbbe <nearest-symbol-label-elided>
  24fad4:	4281      	cmp	r1, r0
  24fad6:	f000 810e 	beq.w	24fcf6 <nearest-symbol-label-elided>
  24fada:	6801      	ldr	r1, [r0, #0]
  24fadc:	6889      	ldr	r1, [r1, #8]
  24fade:	4788      	blx	r1
  24fae0:	e06e      	b.n	24fbc0 <nearest-symbol-label-elided>
  24fae2:	6908      	ldr	r0, [r1, #16]
  24fae4:	2800      	cmp	r0, #0
  24fae6:	d06d      	beq.n	24fbc4 <nearest-symbol-label-elided>
  24fae8:	4281      	cmp	r1, r0
  24faea:	f000 8115 	beq.w	24fd18 <nearest-symbol-label-elided>
  24faee:	6801      	ldr	r1, [r0, #0]
  24faf0:	6889      	ldr	r1, [r1, #8]
  24faf2:	4788      	blx	r1
  24faf4:	e067      	b.n	24fbc6 <nearest-symbol-label-elided>
  24faf6:	6908      	ldr	r0, [r1, #16]
  24faf8:	2800      	cmp	r0, #0
  24fafa:	d066      	beq.n	24fbca <nearest-symbol-label-elided>
  24fafc:	4281      	cmp	r1, r0
  24fafe:	f000 811c 	beq.w	24fd3a <nearest-symbol-label-elided>
  24fb02:	6801      	ldr	r1, [r0, #0]
  24fb04:	6889      	ldr	r1, [r1, #8]
  24fb06:	4788      	blx	r1
  24fb08:	e060      	b.n	24fbcc <nearest-symbol-label-elided>
  24fb0a:	6908      	ldr	r0, [r1, #16]
  24fb0c:	2800      	cmp	r0, #0
  24fb0e:	d062      	beq.n	24fbd6 <nearest-symbol-label-elided>
  24fb10:	4281      	cmp	r1, r0
  24fb12:	f000 8135 	beq.w	24fd80 <nearest-symbol-label-elided>
  24fb16:	6801      	ldr	r1, [r0, #0]
  24fb18:	6889      	ldr	r1, [r1, #8]
  24fb1a:	4788      	blx	r1
  24fb1c:	e05c      	b.n	24fbd8 <nearest-symbol-label-elided>
  24fb1e:	6908      	ldr	r0, [r1, #16]
  24fb20:	2800      	cmp	r0, #0
  24fb22:	d05b      	beq.n	24fbdc <nearest-symbol-label-elided>
  24fb24:	4281      	cmp	r1, r0
  24fb26:	f000 813d 	beq.w	24fda4 <nearest-symbol-label-elided>
  24fb2a:	6801      	ldr	r1, [r0, #0]
  24fb2c:	6889      	ldr	r1, [r1, #8]
  24fb2e:	4788      	blx	r1
  24fb30:	e055      	b.n	24fbde <nearest-symbol-label-elided>
  24fb32:	6908      	ldr	r0, [r1, #16]
  24fb34:	2800      	cmp	r0, #0
  24fb36:	d054      	beq.n	24fbe2 <nearest-symbol-label-elided>
  24fb38:	4281      	cmp	r1, r0
  24fb3a:	f000 8145 	beq.w	24fdc8 <nearest-symbol-label-elided>
  24fb3e:	6801      	ldr	r1, [r0, #0]
  24fb40:	6889      	ldr	r1, [r1, #8]
  24fb42:	4788      	blx	r1
  24fb44:	e04e      	b.n	24fbe4 <nearest-symbol-label-elided>
  24fb46:	6908      	ldr	r0, [r1, #16]
  24fb48:	2800      	cmp	r0, #0
  24fb4a:	d04d      	beq.n	24fbe8 <nearest-symbol-label-elided>
  24fb4c:	4281      	cmp	r1, r0
  24fb4e:	f000 814d 	beq.w	24fdec <nearest-symbol-label-elided>
  24fb52:	6801      	ldr	r1, [r0, #0]
  24fb54:	6889      	ldr	r1, [r1, #8]
  24fb56:	4788      	blx	r1
  24fb58:	e047      	b.n	24fbea <nearest-symbol-label-elided>
  24fb5a:	6908      	ldr	r0, [r1, #16]
  24fb5c:	2800      	cmp	r0, #0
  24fb5e:	d046      	beq.n	24fbee <nearest-symbol-label-elided>
  24fb60:	4281      	cmp	r1, r0
  24fb62:	f000 8155 	beq.w	24fe10 <nearest-symbol-label-elided>
  24fb66:	6801      	ldr	r1, [r0, #0]
  24fb68:	6889      	ldr	r1, [r1, #8]
  24fb6a:	4788      	blx	r1
  24fb6c:	e040      	b.n	24fbf0 <nearest-symbol-label-elided>
  24fb6e:	6908      	ldr	r0, [r1, #16]
  24fb70:	2800      	cmp	r0, #0
  24fb72:	d03f      	beq.n	24fbf4 <nearest-symbol-label-elided>
  24fb74:	4281      	cmp	r1, r0
  24fb76:	f000 815d 	beq.w	24fe34 <nearest-symbol-label-elided>
  24fb7a:	6801      	ldr	r1, [r0, #0]
  24fb7c:	6889      	ldr	r1, [r1, #8]
  24fb7e:	4788      	blx	r1
  24fb80:	e039      	b.n	24fbf6 <nearest-symbol-label-elided>
  24fb82:	6908      	ldr	r0, [r1, #16]
  24fb84:	b3c8      	cbz	r0, 24fbfa <nearest-symbol-label-elided>
  24fb86:	4281      	cmp	r1, r0
  24fb88:	f000 8166 	beq.w	24fe58 <nearest-symbol-label-elided>
  24fb8c:	6801      	ldr	r1, [r0, #0]
  24fb8e:	6889      	ldr	r1, [r1, #8]
  24fb90:	4788      	blx	r1
  24fb92:	e033      	b.n	24fbfc <nearest-symbol-label-elided>
  24fb94:	6908      	ldr	r0, [r1, #16]
  24fb96:	b398      	cbz	r0, 24fc00 <nearest-symbol-label-elided>
  24fb98:	4281      	cmp	r1, r0
  24fb9a:	f000 816f 	beq.w	24fe7c <nearest-symbol-label-elided>
  24fb9e:	6801      	ldr	r1, [r0, #0]
  24fba0:	6889      	ldr	r1, [r1, #8]
  24fba2:	4788      	blx	r1
  24fba4:	e02d      	b.n	24fc02 <nearest-symbol-label-elided>
  24fba6:	2000      	movs	r0, #0
  24fba8:	900c      	str	r0, [sp, #48]	@ 0x30
  24fbaa:	e031      	b.n	24fc10 <nearest-symbol-label-elided>
  24fbac:	2000      	movs	r0, #0
  24fbae:	905a      	str	r0, [sp, #360]	@ 0x168
  24fbb0:	e04a      	b.n	24fc48 <nearest-symbol-label-elided>
  24fbb2:	2000      	movs	r0, #0
  24fbb4:	904e      	str	r0, [sp, #312]	@ 0x138
[319 additional selected lines not published; bounded excerpt]
```

## D. Evidence boundary (automatic, intentionally conservative)

- The original JNI-to-C wrapper direct branch was already PROVEN_STATIC in TRACE_004.
- HIDL BpHw/BnHw definitions and parcel-handling instructions are STATIC evidence, not proof that the instance is registered/running.
- A matching service or implementation library filename/export is a candidate, not proof of a call edge.
- `inputGroup=-1` / `isUpdate=3` are preserved as signed shorts at the C/HIDL boundary; the receiver semantics remain UNKNOWN unless separately derived.
- Hardware PQ effect, source/profile routing, firmware-side update flags and persistent storage are NOT_VERIFIED without an actual receiver trace/readback.
- No device operations, APK installation, Factory calls, I2C or EDID changes.

