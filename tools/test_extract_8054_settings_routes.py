import unittest
from extract_8054_settings_routes import extract


class RoutesTest(unittest.TestCase):
    def test_literals_are_not_calls_and_overwritten_registers_are_rejected(self):
        lines = ['const-string v1, "sound_bass"', 'const-string v2, "g_audio__aud_bass"',
                 'invoke-interface {v0, v1, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;',
                 'iget-object v2, p0, LExample;->field:Ljava/lang/String;',
                 'invoke-interface {v0, v1, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;']
        self.assertEqual(extract([dict(text=t,line=i+1) for i,t in enumerate(lines)]),
                         [dict(key='sound_bass',config_id='g_audio__aud_bass',line=3)])


if __name__ == '__main__':
    unittest.main()
