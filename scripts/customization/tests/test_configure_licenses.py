import copy
import importlib.util
from pathlib import Path
import unittest

SCRIPT = Path(__file__).resolve().parents[1] / "configure-licenses.py"
spec = importlib.util.spec_from_file_location("configure_licenses", SCRIPT)
licensing = importlib.util.module_from_spec(spec)
spec.loader.exec_module(licensing)


class FakeClient:
    def __init__(self, cc_by=True, active=True, default=False):
        self.licenses = [{"id": 11, "name": "CC0 1.0", "active": True,
                          "isDefault": not default,
                          "uri": "http://creativecommons.org/publicdomain/zero/1.0"}]
        if cc_by:
            self.licenses.append({"id": 37, "name": "CC BY 4.0", "active": active,
                                  "isDefault": default,
                                  "uri": "http://creativecommons.org/licenses/by/4.0/"})
        self.writes = []

    def request(self, path, method="GET", data=None):
        if method == "GET":
            return copy.deepcopy(self.licenses)
        self.writes.append((method, path))
        if method == "POST" and path == "":
            self.licenses.append({**data, "id": 37, "isDefault": False})
        elif method == "PUT" and path == "/37/:active/true":
            self.licenses[-1]["active"] = True
        elif method == "PUT" and path == "/default/37":
            for item in self.licenses:
                item["isDefault"] = item["id"] == 37
        else:
            raise AssertionError(f"Unexpected mutation: {method} {path}")
        return {}


class ConfigureLicensesTest(unittest.TestCase):
    def test_uses_actual_id_and_preserves_cc0_as_an_alternative(self):
        client = FakeClient()
        result = licensing.apply_default(client)
        self.assertEqual(client.writes, [("PUT", "/default/37")])
        self.assertTrue(result[0]["active"])
        self.assertFalse(result[0]["isDefault"])
        self.assertTrue(result[1]["isDefault"])

    def test_repeated_apply_does_not_write(self):
        client = FakeClient(default=True)
        licensing.apply_default(client)
        self.assertEqual(client.writes, [])

    def test_activates_existing_inactive_license_before_setting_default(self):
        client = FakeClient(active=False)
        licensing.apply_default(client)
        self.assertEqual(client.writes, [("PUT", "/37/:active/true"),
                                         ("PUT", "/default/37")])

    def test_adds_missing_license_then_uses_returned_id(self):
        client = FakeClient(cc_by=False)
        licensing.apply_default(client)
        self.assertEqual(client.writes, [("POST", ""), ("PUT", "/default/37")])

    def test_duplicate_license_aborts_without_writing(self):
        client = FakeClient()
        client.licenses.append(copy.deepcopy(client.licenses[-1]))
        with self.assertRaises(ValueError):
            licensing.apply_default(client)
        self.assertEqual(client.writes, [])

    def test_reports_failed_default_change(self):
        client = FakeClient()
        original_request = client.request

        def ignore_put(path, method="GET", data=None):
            return {} if method == "PUT" else original_request(path, method, data)

        client.request = ignore_put
        with self.assertRaises(RuntimeError):
            licensing.apply_default(client)


if __name__ == "__main__":
    unittest.main()
