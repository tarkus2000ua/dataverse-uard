# AGENTS.md — Dataverse 6.12 Customization Project

## Project scope
This repository is a customized fork of Dataverse 6.12 for an institutional deployment.

Primary goals:
- Build a custom multilingual homepage.
- Keep as much customization as possible outside upstream Dataverse source.
- Modify the real Dataverse header/footer XHTML where needed.
- Preserve Dataverse authentication, permissions, notifications, locale switching, and other JSF-backed behavior.
- Keep future upgrades to later Dataverse releases manageable.

## Core rules for Codex
1. Target Dataverse **6.12** unless explicitly asked to upgrade.
2. Do not replace dynamic Dataverse JSF logic with static HTML.
3. Prefer this order when implementing UI changes:
   1. Dataverse configuration/settings
   2. supported branding/customization hooks
   3. custom CSS
   4. XHTML/JSF source modifications
   5. Java changes only when necessary
4. Keep upstream source modifications minimal and easy to diff.
5. Preserve existing `rendered=`, permission checks, user/authentication logic, locale logic, and action bindings when restructuring XHTML.
6. The custom homepage may become a React SPA. Do not force a complex interactive homepage into JSF/XHTML if React is a better fit.
7. Use Dataverse's locale as the source of truth for language selection.
8. Keep UI translations separate from longer editorial/content translations.
9. Do not move the repository from the WSL Linux filesystem to `C:\...`.
10. Do not manually copy/sync the repo between Windows and WSL. Edit the WSL repo directly through VS Code Remote/WSL.
11. Use the fast-redeploy workflow after source changes:
   ```bash
   ./scripts/dev/dev-frd.sh
   ```
12. Branding files mounted into the running container should update with browser refresh and should not require a Maven rebuild.
13. Do not edit upstream Docker Compose files unnecessarily; prefer `docker-compose.override.yml`.
14. When changing upstream files, update `docs/upstream-modifications.md`.
15. Keep feature work in focused branches and commits.

## Development environment
- Windows 11
- WSL2 Ubuntu
- Docker Desktop using WSL2
- Java 21
- Maven
- Git
- VS Code opened via WSL (`code .`)
- Repository: `~/projects/dataverse`
- Dataverse base: `v6.12`
- Current working branch originally created as `local-customization-6.12`

## Common commands
Start the fast-redeploy development environment:
```bash
./scripts/dev/dev-start-frd.sh
```

Redeploy changed Java/XHTML/web resources:
```bash
./scripts/dev/dev-frd.sh
```

Check Dataverse:
```bash
curl http://localhost:8080/api/info/version
```

Check running containers:
```bash
docker ps
```

## Important architecture note
`HeaderCustomizationFile` does **not** replace the Dataverse navbar. It adds custom header content. The actual application navbar is defined in:

```text
src/main/webapp/dataverse_header.xhtml
```

The standard navbar contains dynamic JSF behavior and must not be recreated as static HTML without a compelling reason.
