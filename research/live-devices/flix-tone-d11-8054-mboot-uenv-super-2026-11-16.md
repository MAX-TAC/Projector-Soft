# FlixTone D11 Master 8054 — real mboot/uenv/super backup audit (2026-11-16)

## Provenance and scope

Owner captured read-only images via `adb exec-out su 0 cat /dev/block/by-name/...`, uploaded a 2,011,878,129-byte ZIP to the **DRAFT** release in [MAX-TAC/FlixTone_8054_Backup](https://github.com/MAX-TAC/FlixTone_8054_Backup/releases/tag/untagged-5d4ceb0b5cb1def8796a). This draft is not a public release. Read-only [GitHub Actions run 43890576350](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43890576350) verified and parsed `mboot_a.img`, `uenv.img`, `super.img`. Extended [run 43891019885](https://github.com/MAX-TAC/FlixTone_8054_Backup/actions/runs/43891019885) inspected restricted U-Boot env keys and filesystem signatures. **No factory-private images were extracted or published.**

### SHA-256 and size validation

| Partition | Bytes | SHA-256 |
|---|---:|---|
| mboot_a.img | 10,485,760 | `ebf9a0adef859f568d792d182f4ace08d026a27dc7f322bab735c67b07cd34c1` |
| uenv.img | 16,777,216 | `ad7c75ec414d12388a4b2c459042a6815f1e8c334e0ad7f176f69ebb48aa7269` |
| super.img | 2,225,078,272 | `ac16996dddaa7c8f5e5429a0736f73d18a12df7dc6b58a4e89a475f5af0c5a02` |

ZIP SHA256: `4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362`.

## Critical: real Mboot capability is present

Static binary scan of real `mboot_a.img`:

| Literal/code marker | Occurrences |
|---|---:|
| `mtkupgrade` | 14 |
| `upgrade_image.pkg` | 2 |
| `fastboot` | 17 |
| `recovery` | 46 |
| `auto_upgrade` | 10 |
| `avb` | 81 |
| `avb_slot_verify` | present |
| `usb upgrade` | 6 |
| `usb start` | 7 |
| `saveenv` | 2 |

The substring `mtkupgrade usb` **is present** in the real bootloader binary, and `upgrade_image.pkg` is also present, but the *exact single contiguous* string `mtkupgrade usb upgrade_image.pkg` was **not** present. This makes a factory USB update command implementation highly plausible, not a verified automatic cold-boot behavior or verified success with self-built PKG.

`fastboot` and `unlock` strings do not prove fastboot mode is reachable or that unlocking works. Do not issue any flash/unlock or untrusted PKG commands.

## U-Boot environment key findings

Actual read-only `uenv.img` contains NUL-delimited environment records:
- `bootdelay=0`
- `verify=n`
- `devicestate=lock`
- `bootcmd` exists and contains a normal `bootm/booti`-style boot path; no `mtkupgrade`, `upgrade_image.pkg` or `usb upgrade` marker in this value.
- `recoverycmd` exists with bootm/booti-style boot path.
- `auto_upgrade` string occurs in `uenv.img` but not as a simple NUL-delimited `auto_upgrade=<value>` entry.

Interpretation:
- The default `bootcmd` **does not advertise automatic USB flash-on-power-on**; a special key/recovery path could still exist and requires further study.
- `verify=n` is U-Boot environment data, **not evidence AVB has been disabled**. At runtime Android reported `ro.boot.vbmeta.device_state=locked`, `verifiedbootstate=green`, and `veritymode=enforcing`.
- `devicestate=lock` matches the locked state reported by Android; do not alter U-Boot environment.

## Super logical partitions

Image is a valid raw Android dynamic super (not Android sparse ZIP payload). Geometry magic and SHA256 geometry metadata checksum both validated at offset 4096; logical block size 4096; 3 **metadata slots** (not boot slots); metadata version 10.2; slot 0/1/2 primary tables have matching partition inventory (checksum verified).

| Logical partition | Approx MiB | Filesystem (verified by magic) |
|---|---:|---|
| system_a | 816.13 | EROFS |
| vendor_a | 250.50 | EROFS |
| product_a | 73.58 | EROFS |
| system_ext_a | 83.25 | EROFS |
| system_b | 0 | empty |
| vendor_b | 0 | empty |
| product_b | 0 | empty |
| system_ext_b | 0 | empty |

The B logical partitions can be populated during A/B OTA. The current raw `super.img` is an exact local image of the *September 2026* live Android partition layout.

## Security / recovery gates

- Root shell exists, but Android Verified Boot 2.0 still signs and checks critical boot/system/vendor partitions.
- Existing signature verification: both live `vbmeta_a` and `vbmeta_system_a` carry valid SHA256_RSA4096 signatures under public key SHA1 `883f885174dcadcce97fefad1bcbd11a5c0289e7`, which does **not** match LineageOS/AOSP reference test RSA-4096 key.
- `/system/etc/security/otacerts.zip` contains AOSP testkey RSA-2048 for OTA, **not** a replacement for the private AVB RSA-4096 signing key.
- Manufacturer PKG scripts previously audited erase `persist`, `eeprom_a`, `userdata`; do NOT test a factory PKG or key combo without verified recovery, complete local private backups and hardware-specific confirmation.
- **Never publish `PRIVATE_FACTORY`**: `eeprom_a`, `persist`, `private`, `project_id`, `tvcertificate`, `tvconfig_a`. The user's upload is a DRAFT; keep it unpublished or replace with a sanitized archive before publishing.

## Feasibility update

1. Real USB upgrade code in Mboot: **confirmed statically**. Trigger on power-on with flash drive and signature enforcement: unknown.
2. `fastboot` compiled into bootloader: **strings present**; mode and unlock functionality: unknown.
3. Root filesystem backup and EROFS extraction into custom images: **now practical offline**.
4. Installing unsigned/independently signed images in `locked/green`: **not established** and expected to fail standard AVB without private key or verified alternative.
5. Next recommended development: maintain MediaTek HAL, firmware/native projector components and calibrated partitions, build clean user UI and a 8054-specific RUIOS MOD in a staging tree; no actual flash until recovery path is proven.
