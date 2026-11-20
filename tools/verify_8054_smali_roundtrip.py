"""Require an instruction-exact classes13 round trip after the expected patch."""
import sys
from pathlib import Path

DEFAULT_FALSE_FIELDS = {
    'com/android/settingslib/volume/MediaSessions.smali': '.field private static final USE_SERVICE_LABEL:Z',
    'com/android/tv/settings/accessories/BluetoothAccessoryFragment$UnpairConfirmFragment.smali':
        '.field public static isUnpaired:Z',
    'com/android/tv/settings/device/apps/specialaccess/TurnScreenOn.smali':
        '.field private static final DEBUG:Z',
}


def compare(expected, candidate):
    expected_files = {p.relative_to(expected) for p in expected.rglob('*.smali')}
    actual_files = {p.relative_to(candidate) for p in candidate.rglob('*.smali')}
    if expected_files != actual_files:
        raise ValueError('Unexpected round-trip class set delta')
    for relative in sorted(expected_files):
        original = (expected / relative).read_text(encoding='utf-8')
        assembled = (candidate / relative).read_text(encoding='utf-8')
        if original == assembled:
            continue
        field = DEFAULT_FALSE_FIELDS.get(relative.as_posix())
        if field is None or original.count(field + ' = false\n') != 1:
            raise ValueError('Unexpected smali round-trip delta: ' + relative.as_posix())
        # A missing encoded static value for a boolean has the same value, false.
        if original.replace(field + ' = false\n', field + '\n', 1) != assembled:
            raise ValueError('Unexpected delta beyond the default boolean: ' + relative.as_posix())
        print('Normalized explicit false to implicit false:', relative.as_posix())


if __name__ == '__main__':
    compare(Path(sys.argv[1]), Path(sys.argv[2]))
    print('PASS classes13 smali instructions and class metadata match the expected patch')
