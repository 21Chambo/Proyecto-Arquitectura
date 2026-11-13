"""Validate the production contract without starting containers or reading .env."""
import json
import os
from pathlib import Path
import subprocess
import tempfile
import uuid


ROOT = Path(__file__).resolve().parents[1]
REQUIRED = (
    "MYSQL_ROOT_PASSWORD", "MYSQL_PASSWORD", "APP_INITIAL_ADMIN_PASSWORD",
    "APP_INITIAL_STAFF_PASSWORD", "APP_JWT_SECRET", "APP_CORS_ALLOWED_ORIGINS", "TAG",
)


def main():
    environment = os.environ.copy()
    for key in REQUIRED:
        environment.pop(key, None)
    environment["COMPOSE_DISABLE_ENV_FILE"] = "1"
    # A host development value must never override the production image version.
    environment["APP_VERSION"] = "unrelated-host-version"
    values = {key: uuid.uuid4().hex for key in REQUIRED}
    values["TAG"] = "validation-only"
    values["APP_CORS_ALLOWED_ORIGINS"] = "https://portal.example.invalid"
    with tempfile.TemporaryDirectory() as directory:
        empty_env = Path(directory) / "empty.env"
        empty_env.touch()
        command = ["docker", "compose", "--env-file", str(empty_env),
                   "-f", str(ROOT / "compose.prod.yml"), "config", "--format", "json"]
        for key in REQUIRED:
            for missing in (None, ""):
                candidate = environment | values
                if missing is None:
                    candidate.pop(key)
                else:
                    candidate[key] = missing
                result = subprocess.run(command, env=candidate, capture_output=True, text=True)
                if result.returncode == 0 or f"falta {key}" not in result.stderr:
                    raise AssertionError(f"Compose must reject missing/empty {key}")
        result = subprocess.run(command, env=environment | values, capture_output=True, text=True)
        if result.returncode:
            raise AssertionError("Production Compose must accept explicitly supplied values")
        config = json.loads(result.stdout)
        development_command = ["docker", "compose", "--env-file", str(empty_env),
                               "-f", str(ROOT / "docker-compose.yml"), "config", "--format", "json"]
        for configured_version, expected_version in ((None, "dev"), ("local-test-version", "local-test-version")):
            candidate = environment | values
            if configured_version is None:
                candidate.pop("APP_VERSION", None)
            else:
                candidate["APP_VERSION"] = configured_version
            result = subprocess.run(development_command, env=candidate, capture_output=True, text=True)
            if result.returncode:
                raise AssertionError("Development Compose must accept explicitly supplied secrets")
            development = json.loads(result.stdout)
            assert development["services"]["api"]["environment"]["APP_VERSION"] == expected_version

    services = config["services"]
    assert services["api"]["environment"]["APP_VERSION"] == values["TAG"], "Health version must match the deployed image tag"
    assert services["api"]["environment"]["APP_CORS_ALLOWED_ORIGINS"] == values["APP_CORS_ALLOWED_ORIGINS"]
    for name, service in services.items():
        assert "build" not in service, f"{name} must use a published image"
        assert service.get("network_mode") != "host", f"{name} must use Docker networking"
        options = service["logging"]["options"]
        assert options["max-size"] == "10m" and options["max-file"] == "3"
        assert service["restart"] == ("no" if name == "flyway" else "unless-stopped")
        if name != "portal":
            assert not service.get("ports"), f"{name} must not publish host ports"
    for name in ("api", "portal"):
        assert services[name]["image"] == f"ghcr.io/21chambo/proyecto-arquitectura-{name}:validation-only"
    ports = services["portal"]["ports"]
    assert len(ports) == 1 and ports[0]["published"] == "80" and ports[0]["target"] == 8080
    assert config["volumes"]["mysql_data"]["name"] != "veterinary-mysql-data", "Production must not reuse the development volume"
    assert services["api"]["depends_on"]["flyway"]["condition"] == "service_completed_successfully"
    print("PASS: required secrets/tag, private DB/API, GHCR images, logs, restart, isolated volume and dev/prod health versions.")


if __name__ == "__main__":
    main()
