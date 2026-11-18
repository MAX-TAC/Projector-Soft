"""Host-only extraction of one pinned stock APK, no boot/factory/key data or decompilation."""
import argparse
import hashlib
import shutil
import struct
import subprocess
import tempfile
import zipfile
from pathlib import Path
from kit_common import STOCK_SHA, sha256

BACKUP_SHA = '4f53f2224c98b4c832a19a591b9dd7f925bde28528b5674a09732c0f0e41d362'


def extract(archive, output):
    if archive.stat().st_size != 2011878129 or sha256(archive) != BACKUP_SHA: raise ValueError('Backup size/SHA mismatch')
    # Linux packaging host only. No Android path or device tool is involved.
    with tempfile.TemporaryDirectory(prefix='tvsettings-stock-', dir='/tmp') as temporary:
        work = Path(temporary); super_file = work / 'super.img'
        with zipfile.ZipFile(archive) as bundle:
            found = [i for i in bundle.infolist() if Path(i.filename.replace('\\', '/')).name == 'super.img']
            if len(found) != 1 or found[0].file_size != 2225078272: raise ValueError('Unexpected super image')
            with bundle.open(found[0]) as source, super_file.open('wb') as target: shutil.copyfileobj(source, target, 8 * 1024 * 1024)
        with super_file.open('rb') as source:
            source.seek(4096); geometry = source.read(52)
            if struct.unpack_from('<II', geometry) != (0x616c4467, 52): raise ValueError('LP geometry mismatch')
            checked = bytearray(geometry); checked[8:40] = bytes(32)
            if hashlib.sha256(checked).digest() != geometry[8:40]: raise ValueError('LP geometry checksum')
            size = struct.unpack_from('<I', geometry, 40)[0]
            if not 128 <= size <= 1048576: raise ValueError('LP metadata size')
            source.seek(12288); metadata = source.read(size)
            if struct.unpack_from('<I', metadata)[0] != 0x414c5030: raise ValueError('LP header magic')
            header_size = struct.unpack_from('<I', metadata, 8)[0]
            table_size = struct.unpack_from('<I', metadata, 44)[0]
            if not 128 <= header_size <= len(metadata) or table_size > len(metadata)-header_size: raise ValueError('LP bounds')
            header = bytearray(metadata[:header_size]); header[12:44] = bytes(32)
            if hashlib.sha256(header).digest() != metadata[12:44]: raise ValueError('LP header checksum')
            tables = metadata[header_size:header_size+table_size]
            if hashlib.sha256(tables).digest() != metadata[48:80]: raise ValueError('LP tables checksum')
            po, pn, ps = struct.unpack_from('<III', metadata, 80)
            eo, en, es = struct.unpack_from('<III', metadata, 92)
            if ps < 48 or es < 24 or po+pn*ps > len(tables) or eo+en*es > len(tables): raise ValueError('LP table bounds')
            matches = []
            for index in range(pn):
                offset = po+index*ps
                if tables[offset:offset+36].split(b'\0')[0] == b'system_ext_a':
                    matches.append(struct.unpack_from('<II', tables, offset+40))
            if len(matches) != 1: raise ValueError('system_ext_a not unique')
            first, count = matches[0]
            if not count or first+count > en: raise ValueError('LP extent bounds')
            image = work / 'system_ext_a.img'
            with image.open('wb') as target:
                for index in range(first, first+count):
                    offset = eo+index*es
                    sectors, kind, start, device = struct.unpack_from('<QIQI', tables, offset)
                    if kind != 0 or device != 0 or start*512+sectors*512 > super_file.stat().st_size: raise ValueError('Unsupported LP extent')
                    source.seek(start*512); remaining = sectors*512
                    while remaining:
                        data = source.read(min(remaining, 8*1024*1024))
                        if not data: raise ValueError('Truncated extent')
                        target.write(data); remaining -= len(data)
        tree = work / 'tree'; tree.mkdir()
        subprocess.run(['fsck.erofs', '--extract='+str(tree), str(image)], check=True, capture_output=True, timeout=450)
        stock = tree / 'priv-app/TvSettingsPlus/TvSettingsPlus.apk'
        if stock.is_symlink() or sha256(stock) != STOCK_SHA: raise ValueError('Extracted stock APK mismatch')
        output.parent.mkdir(parents=True, exist_ok=True); shutil.copyfile(stock, output)
    print('PASS pinned stock APK extracted; only system_ext processed, no factory/boot data published')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument('archive', type=Path); parser.add_argument('output', type=Path)
    args = parser.parse_args(); extract(args.archive, args.output)
