#!/usr/bin/env python3
import importlib.util
import json
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "scripts/customization/apply-bundle-update.py"
spec = importlib.util.spec_from_file_location("bundle_update", SCRIPT)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class BundleUpdateTests(unittest.TestCase):
    patch = [{"key": "dataverse", "before": "Dataverse", "after": "Колекція"}]

    def test_preserves_other_translations_comments_bom_and_crlf(self):
        raw = b"\xef\xbb\xbf# remote-only comment\r\ndataverse = Dataverse\r\ncustom.remote=Keep me\r\n"
        result, changed, already = module.build_update(raw, self.patch)
        self.assertEqual(result, b"\xef\xbb\xbf# remote-only comment\r\ndataverse = " + "Колекція".encode() + b"\r\ncustom.remote=Keep me\r\n")
        self.assertEqual((changed, already), (1, 0))

    def test_already_updated_is_idempotent(self):
        raw = "dataverse=Колекція\n".encode()
        self.assertEqual(module.build_update(raw, self.patch), (raw, 0, 1))

    def test_conflict_duplicate_missing_and_continuation_are_rejected(self):
        for value in ["dataverse=Remote wording\n", "dataverse=Dataverse\ndataverse=Dataverse\n", "other=value\n", "dataverse=Data\\\nverse\n"]:
            with self.subTest(value=value), self.assertRaises(ValueError):
                module.build_update(value.encode(), self.patch)

    def test_failed_cli_never_writes_output_or_input(self):
        with tempfile.TemporaryDirectory() as folder:
            folder = Path(folder)
            bundle = folder / "Bundle_uk.properties"
            patch = folder / "update.json"
            output = folder / "updated.properties"
            raw = b"dataverse=Remote wording\n"
            bundle.write_bytes(raw)
            patch.write_text(json.dumps({"format": 1, "entries": self.patch}))
            result = subprocess.run(["python3", str(SCRIPT), str(bundle), "--patch", str(patch), "--output", str(output)], capture_output=True)
            self.assertEqual(result.returncode, 2)
            self.assertFalse(output.exists())
            self.assertEqual(bundle.read_bytes(), raw)

    def test_cli_refuses_existing_output(self):
        with tempfile.TemporaryDirectory() as folder:
            folder = Path(folder)
            bundle, patch, output = [folder / name for name in ["Bundle_uk.properties", "update.json", "updated.properties"]]
            bundle.write_text("dataverse=Dataverse\n")
            patch.write_text(json.dumps({"format": 1, "entries": self.patch}))
            output.write_bytes(b"keep existing file")
            result = subprocess.run(["python3", str(SCRIPT), str(bundle), "--patch", str(patch), "--output", str(output)], capture_output=True)
            self.assertEqual(result.returncode, 2)
            self.assertEqual(output.read_bytes(), b"keep existing file")


if __name__ == "__main__":
    unittest.main()
