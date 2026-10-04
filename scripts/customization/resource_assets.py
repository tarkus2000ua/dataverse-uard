"""Shared paths and content versions for UaRD application resources."""
from hashlib import sha256
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
WEBAPP = ROOT / "src/main/webapp"
IMAGES = WEBAPP / "resources/images/uard"


def versioned_url(path, data=None):
    path = Path(path)
    relative = path.relative_to(WEBAPP).as_posix()
    content = path.read_bytes() if data is None else data
    return "/" + relative + "?v=" + sha256(content).hexdigest()[:12]


def script_output(source):
    path = WEBAPP / "resources/js/uard" / source.name
    data = ("// Generated from " + source.relative_to(ROOT).as_posix() + ". Do not edit this copy.\n").encode() + source.read_bytes()
    return path, data


def write_script(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)
