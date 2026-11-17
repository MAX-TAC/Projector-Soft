"""Extract literal Map.put pairs from cached exact smali bodies; never infer HAL."""
import json
import re
import sys
from pathlib import Path


def extract(lines):
    registers, pairs = {}, []
    for entry in lines:
        text, line = entry['text'].strip(), entry['line']
        literal = re.match(r'const-string(?:/jumbo)? (\w+), "([^"]*)"', text)
        if literal:
            registers[literal[1]] = literal[2]
            continue
        move = re.match(r'move-object(?:/from16|/16)? (\w+), (\w+)', text)
        if move:
            registers[move[1]] = registers.get(move[2])
            continue
        put = re.match(r'invoke-interface \{\w+, (\w+), (\w+)\}, Ljava/util/Map;->put\(', text)
        if put and registers.get(put[1]) and registers.get(put[2]):
            key, config = registers[put[1]], registers[put[2]]
            if config.startswith('g_'):
                pairs.append(dict(key=key, config_id=config, line=line))
        # A newly assigned register must not retain an earlier string literal.
        assigned = re.match(r'(?:move-result\S*|iget\S*|sget\S*|new-instance|const(?:/\S+)?) (\w+)[, ]', text)
        if assigned:
            registers.pop(assigned[1], None)
    return pairs


def main(source, target):
    data = json.loads(Path(source).read_text(encoding='utf-8-sig'))
    routes = []
    for trace in data['traces']:
        if trace['apk'] != 'TvServices' or trace['version'] != '8054':
            continue
        if 'TVMenuSettingsService;->init' not in trace['class_method']:
            continue
        for pair in extract(trace['lines']):
            routes.append(dict(pair, source_file=trace['source_file'], method=trace['class_method'],
                               group=pair['key'].split('_')[0], status='STATIC_MAPPING',
                               runtime_status='RUNTIME_TEST_REQUIRED'))
    if not any(r['key'] == 'sound_equalizer_detail_120hz' for r in routes):
        raise ValueError('Cached trace is incomplete: EQ mapping absent')
    Path(target).write_text(json.dumps(dict(source_run=44125184745, routes=routes,
        limits='Literal map pairs only; shared observer proven separately. UI bounds and special branches require group review.'),
        ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    print(f'Extracted {len(routes)} map entries, {len(set(r["key"] for r in routes))} unique keys')


if __name__ == '__main__':
    main(*sys.argv[1:])
