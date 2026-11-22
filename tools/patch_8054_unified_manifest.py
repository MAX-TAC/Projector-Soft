"""Append the isolated UI and disabled recovery to the original 8054 manifest."""
from patch_axml_compat_aliases import (
    END, NO, RESMAP, START, STRINGS, XML, StringPool, attr, chunkinfo, endnode,
    p32, refattr, startnode, u16, u32,
)
from patch_axml_version import change_version_code

PREFIX = 'dev.projectorsoft.tvsettings.'
ACTIVITIES = (
    ('NetworkSettingsActivity', 'Wi-Fi и сеть'),
    ('BluetoothSettingsActivity', 'Bluetooth'),
    ('SourcesSettingsActivity', 'Источники'),
    ('ProjectionSettingsActivity', 'Проекция'),
    ('PictureSettingsActivity', 'Изображение'),
    ('SoundSettingsActivity', 'Звук'),
    ('AppsSettingsActivity', 'Приложения'),
    ('SystemSettingsActivity', 'Настройки устройства'),
    ('HdmiCecSettingsActivity', 'HDMI-CEC'),
    ('EqualizerSettingsActivity', 'Эквалайзер и звуковые профили'),
    ('SoundDelaySettingsActivity', 'Задержка звука'),
    ('DeviceAboutSettingsActivity', 'Об устройстве'),
    ('HardwareStatusSettingsActivity', 'Состояние оборудования'),
    ('ProjectorPowerSettingsActivity', 'Питание'),
    ('RemoteSettingsActivity', 'Пульт управления'),
    ('HdmiOneStatusActivity', 'HDMI 1'),
    ('HdmiTwoStatusActivity', 'HDMI 2'),
    ('BluetoothDevicesSettingsActivity', 'Управление устройствами'),
    ('ColorTemperatureSettingsActivity', 'Цветовая температура'),
    ('MemcSettingsActivity', 'MEMC'),
)
DISABLED = (('receiver', 'KuSourceRecoveryBootReceiver'), ('service', 'KuSourceRecoveryService'))


def patch_unified(data, version):
    if u16(data, 0) != XML or u32(data, 4) != len(data):
        raise ValueError('Invalid original 8054 manifest')
    offset = u16(data, 2)
    chunks = []
    pool = None
    resource_map = None
    while offset < len(data):
        kind, header, size = chunkinfo(data, offset)
        chunk = data[offset:offset + size]
        if kind == STRINGS:
            pool = StringPool(chunk)
        elif kind == RESMAP:
            resource_map = [u32(chunk, i) for i in range(header, size, 4)]
        chunks.append((kind, chunk))
        offset += size
    if pool is None or resource_map is None:
        raise ValueError('Missing stock manifest resources')
    android = pool.lookup['http://schemas.android.com/apk/res/android']
    for key in ('name', 'label', 'enabled', 'exported', 'icon', 'banner', 'theme', 'targetActivity'):
        index = pool.lookup[key]
        if index >= len(resource_map) or not resource_map[index]:
            raise ValueError('Missing Android resource-map attribute: ' + key)
    application = [i for i, (kind, chunk) in enumerate(chunks)
                   if kind == END and pool.values[u32(chunk, 20)] == 'application']
    if len(application) != 1:
        raise ValueError('Expected one stock application')
    if any(PREFIX + name in pool.lookup for name, _ in ACTIVITIES):
        raise ValueError('Already patched manifest or conflicting class name')

    themes = []
    for kind, chunk in chunks:
        if kind != START or pool.values[u32(chunk, 20)] != 'activity':
            continue
        attrs = {}
        for i in range(u16(chunk, 28)):
            start = 16 + u16(chunk, 24) + i * u16(chunk, 26)
            if u32(chunk, start) == android:
                attrs[pool.values[u32(chunk, start + 4)]] = (chunk[start + 15], u32(chunk, start + 16))
        name = attrs.get('name')
        if name and pool.values[name[1]] == 'com.android.tv.settings.MainSettings':
            theme = attrs.get('theme')
            if theme and theme[0] == 1:
                themes.append(theme[1])
    if len(themes) != 1:
        raise ValueError('Exact stock MainSettings theme unavailable')

    def attributes(name, exported, enabled):
        return [attr(android, pool.index('name'), pool.index(PREFIX + name)),
                attr(android, pool.index('exported'), exported),
                attr(android, pool.index('enabled'), enabled)]

    extension = b''
    activity = pool.index('activity')
    for name, label in ACTIVITIES:
        attrs = attributes(name, True, True)
        attrs.append(attr(android, pool.index('label'), pool.index(label)))
        attrs.append(refattr(android, pool.index('theme'), themes[0]))
        extension += startnode(activity, attrs)
        extension += endnode(activity)
    for tag, name in DISABLED:
        node = pool.index(tag)
        extension += startnode(node, attributes(name, False, False))
        extension += endnode(node)

    # One new explicit launcher shortcut, not an extra APK or a rewrite of
    # the original MainSettings. Asset IDs are verified against exact-8054
    # resources.arsc: ic_settings_launcher_icon / ic_launcher_banner_settings.
    # This does NOT globally intercept KEYCODE_SETTINGS from the OEM key service.
    alias_name = 'com.android.tv.settings.mod.SettingsLauncher'
    alias_tag = pool.index('activity-alias')
    filter_tag = pool.index('intent-filter')
    action_tag = pool.index('action')
    category_tag = pool.index('category')
    alias_attrs = [
        attr(android, pool.index('name'), pool.index(alias_name)),
        attr(android, pool.index('targetActivity'),
             pool.index('com.android.tv.settings.MainSettings')),
        attr(android, pool.index('enabled'), True),
        attr(android, pool.index('exported'), True),
        attr(android, pool.index('label'), pool.index('TvSettingsPlus MOD')),
        refattr(android, pool.index('icon'), 0x7f08022e),
        refattr(android, pool.index('banner'), 0x7f080142),
    ]
    extension += startnode(alias_tag, alias_attrs)
    extension += startnode(filter_tag)
    extension += startnode(action_tag, [
        attr(android, pool.index('name'), pool.index('android.intent.action.MAIN'))
    ])
    extension += endnode(action_tag)
    for category in ('android.intent.category.LAUNCHER',
                     'android.intent.category.LEANBACK_LAUNCHER'):
        extension += startnode(category_tag, [
            attr(android, pool.index('name'), pool.index(category))
        ])
        extension += endnode(category_tag)
    extension += endnode(filter_tag)
    # Reversible trial for remote implementations that use an *implicit*
    # android.settings.SETTINGS intent. If the MTK remote starts an explicit
    # KuSettings component, this filter cannot intercept its button at all.
    # Never disable KuSettings or override vendor input policy in the APK.
    extension += startnode(filter_tag)
    extension += startnode(action_tag, [
        attr(android, pool.index('name'), pool.index('android.settings.SETTINGS'))
    ])
    extension += endnode(action_tag)
    extension += startnode(category_tag, [
        attr(android, pool.index('name'), pool.index('android.intent.category.DEFAULT'))
    ])
    extension += endnode(category_tag)
    extension += endnode(filter_tag)
    extension += endnode(alias_tag)

    rebuilt = [pool.output() if kind == STRINGS else chunk for kind, chunk in chunks]
    rebuilt.insert(application[0], extension)
    updated = bytearray(data[:u16(data, 2)] + b''.join(rebuilt))
    p32(updated, 4, len(updated))
    return change_version_code(bytes(updated), 1, version)
