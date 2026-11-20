"""Regression tests for selecting PIC14 smali evidence."""
from contextlib import redirect_stdout
from io import StringIO
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

from trace_8054_picture_format import trace


class PictureFormatTraceTest(unittest.TestCase):
    def scan(self, classes):
        with TemporaryDirectory() as tmp:
            root = Path(tmp) / "LiveTV"
            for name, content in classes.items():
                path = root / "smali_classes4" / (name + ".smali")
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(content, encoding="utf-8")
            out = StringIO()
            with redirect_stdout(out):
                trace(root)
            return out.getvalue()

    def test_retains_invocation_arguments_and_branches(self):
        output = self.scan({"com/mediatek/Controller": """
.method public selectSize(I)V
    const-string v0, "g_video__screen_mode"
    if-eqz p1, :cond_0
    invoke-static {v0, p1}, Lcom/mediatek/Display;->setScreenMode(Ljava/lang/String;I)V
    :cond_0
    return-void
.end method
.method public unrelated()V
    return-void
.end method
"""})
        self.assertIn("selectSize(I)V", output)
        self.assertIn("if-eqz p1", output)
        self.assertIn("->setScreenMode(Ljava/lang/String;I)V", output)
        self.assertNotIn("unrelated()V", output)
        self.assertIn("PIC14_SCANNED LiveTV 1", output)

    def test_excludes_framework_and_unexecuted_field_literal(self):
        output = self.scan({
            "androidx/View": """
.method public getAspectRatio()I
    const/4 v0, 0x4
    return v0
.end method
""",
            "com/vendor/DisplayOptions": """
.field private static final LABEL:Ljava/lang/String; = "Overscan"
.method public getVersion()I
    const/4 v0, 0x4
    return v0
.end method
""",
            "com/vendor/OverScanAdjustViewHolder": """
.method private updateIni()V
    invoke-static {}, Lcom/vendor/Customize;->apply()V
    return-void
.end method
""",
        })
        self.assertIn("->apply()V", output)
        self.assertNotIn("getAspectRatio()I", output)
        self.assertNotIn("getVersion()I", output)

    def test_rejects_missing_decode(self):
        with TemporaryDirectory() as tmp:
            with self.assertRaisesRegex(ValueError, "No decoded DEX"):
                trace(Path(tmp))


if __name__ == "__main__":
    unittest.main()
