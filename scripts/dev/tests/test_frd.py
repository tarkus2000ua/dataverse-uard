#!/usr/bin/env python3
"""Exercise FRD shell control flow without Docker, Maven, or database changes."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

SCRIPTS = Path(__file__).resolve().parents[1]


class FastRedeployTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        scripts = self.root / "scripts/dev"
        scripts.mkdir(parents=True)
        for name in ("dev-start-frd.sh", "dev-frd.sh", "dev-deploy-frd.sh"):
            shutil.copy2(SCRIPTS / name, scripts / name)
        (self.root / "target/classes").mkdir(parents=True)
        web = self.root / "target/dataverse/WEB-INF"
        web.mkdir(parents=True)
        (web / "web.xml").write_text("<web-app/>")
        self.bin = self.root / "bin"
        self.bin.mkdir()
        self.env = dict(os.environ, PATH=f"{self.bin}:{os.environ['PATH']}",
                        FRD_TEST_LOG=str(self.root / "commands.log"),
                        FRD_TEST_PASSWORD_FILE=str(self.root / "password-path"),
                        FRD_TEST_STATUS="0", FRD_TEST_MOUNT="0")
        for name in ("mvn", "rsync", "unzip", "curl", "sleep"):
            self.command(name, "exit 0")
        self.command("curl", 'exit "${FRD_TEST_CURL_STATUS:-0}"')
        self.command("docker", r'''printf '%s\n' "$*" >> "$FRD_TEST_LOG"
if [[ "$1" == ps ]]; then
    echo container-id
elif [[ "$1" == exec && "$3" == test ]]; then
    exit "$FRD_TEST_MOUNT"
elif [[ "$1" == exec ]]; then
    shift 2
    exec "$@"
fi''')
        self.command("asadmin", r'''while (($#)); do
    if [[ "$1" == --passwordfile ]]; then
        printf '%s' "$2" > "$FRD_TEST_PASSWORD_FILE"
    fi
    shift
done
echo "asadmin deployment output"
exit "$FRD_TEST_STATUS"''')

    def command(self, name, body):
        path = self.bin / name
        path.write_text("#!/usr/bin/env bash\nset -euo pipefail\n" + body + "\n")
        path.chmod(0o755)

    def run_script(self, name):
        return subprocess.run(["bash", str(self.root / "scripts/dev" / name)],
                              env=self.env, text=True, capture_output=True)

    def assert_password_removed(self):
        path = Path((self.root / "password-path").read_text())
        self.assertFalse(path.exists(), "Temporary admin password file was left behind")

    def test_deploy_failure_reaches_both_callers(self):
        self.env["FRD_TEST_STATUS"] = "42"
        for name in ("dev-start-frd.sh", "dev-frd.sh"):
            with self.subTest(script=name):
                result = self.run_script(name)
                self.assertEqual(result.returncode, 42, result.stderr)
                self.assertNotIn("✓", result.stdout)
                self.assert_password_removed()

    def test_success_and_application_mount_refresh(self):
        for name in ("dev-start-frd.sh", "dev-frd.sh"):
            with self.subTest(script=name):
                result = self.run_script(name)
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertIn("✓", result.stdout)
                self.assert_password_removed()
        log = (self.root / "commands.log").read_text()
        self.assertIn("up -d --no-deps --force-recreate dev_dataverse", log)
        self.assertNotIn("--force-recreate dev_postgres", log)

    def test_missing_mount_stops_before_asadmin(self):
        self.env["FRD_TEST_MOUNT"] = "1"
        result = self.run_script("dev-deploy-frd.sh")
        self.assertEqual(result.returncode, 1)
        self.assertIn("cannot see the exploded WAR", result.stderr)
        self.assertFalse((self.root / "password-path").exists())

    def test_payara_readiness_timeout_is_an_error(self):
        self.env["FRD_TEST_CURL_STATUS"] = "22"
        result = self.run_script("dev-start-frd.sh")
        self.assertEqual(result.returncode, 1)
        self.assertIn("Payara did not become ready", result.stderr)
        self.assertNotIn("✓", result.stdout)
        self.assertFalse((self.root / "password-path").exists())


if __name__ == "__main__":
    unittest.main()
