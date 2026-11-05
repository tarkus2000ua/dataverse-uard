# GIT_STRATEGY.md

Updated 2026-11-05. Target: Dataverse 6.12.

## Maintained baseline and remotes

- `origin`: git@github.com:tarkus2000ua/dataverse-uard.git (UaRD).
- `upstream`: https://github.com/IQSS/dataverse.git (official Dataverse).
- `v6.12`: original upstream release; retain it as the comparison point.
- `custom/6.12`: accepted, locally tested UaRD code and GitHub default branch.
- `codex/<task>`: temporary branch for a focused change.
- `develop`: tracks `upstream/develop`; preserve that relationship. Do not use it
  as the baseline for UaRD 6.12 features.

The maintained branch is an integration baseline, not proof of remote deployment.
The user/administrator handles deployment and records deployed revisions separately.

## Solo development workflow

Inspect `git status` first. Record or safely set aside pending work before switching
between unrelated tasks. Uncommitted changes are working-directory/index state;
creating a branch does not isolate them.

Start each independent task from the updated baseline:

```bash
git switch custom/6.12
git pull --ff-only origin custom/6.12
git switch -c codex/task-name custom/6.12
```

Make small, focused commits. Review the staged diff and select paths explicitly.
Do not include the existing staged dummy `customization/branding/custom-header.html`.
With that unrelated file still staged, use `git commit --only -- <task-paths>` or
an isolated index; an ordinary `git commit` would include all staged files.
Keep original design inputs in `customization/homepage/assets/` and `dataverse.zip`
out of source commits. Preserve all other unrelated local work.

Review and validate the task before integration:

```bash
git log --oneline custom/6.12..HEAD
git diff --stat custom/6.12...HEAD
git diff --check
```

Run checks appropriate to the change and the documented local FRD workflow after
source changes. Keep `docs/upstream-modifications.md` current for upstream edits.
The user's standing preference is that an operative request to "commit" completes
validation, task-only commit, integration into `custom/6.12`, push, verification,
and retirement of the completed local/remote feature branch. No additional prompt
is needed. Explicit limits such as "commit only", "don't push" or "keep the branch"
override this default. A code change request alone does not trigger publication;
application deployment always requires a separate request. A GitHub pull request
is an optional self-review record. With accepted work and pending files safely handled:

```bash
git switch custom/6.12
git merge --ff-only codex/task-name
git push origin custom/6.12
```

If fast-forward is refused, inspect the divergence. Merge the maintained baseline
into the feature, resolve/test, then integrate, or use a reviewed normal merge.
Never force-push shared history to reconcile unexpected remote changes.
After verifying the feature is included in the published baseline, delete its local
branch with `git branch -d codex/task-name` and its origin branch when appropriate.
Create the next independent task from the updated `custom/6.12`.

A task that needs unfinished feature work may explicitly branch from that feature.
Document the dependency, integrate the prerequisite first, then the dependent work.
Avoid accumulating a chain of completed features without updating the baseline.
A new chat should inspect status and this workflow before selecting a branch;
branch creation, commits, pushes and deployment follow the user's current scope.

## Transition from the previous feature chain (2026-11-05)

The previous `custom/6.12` tip was the unmodified `v6.12` commit `97691073fc`.
The feature branches formed a linear chain rather than independent changes.
The user authorized consolidating the chain and retiring completed feature branches.

- Fast-forwarded the baseline to `codex/login-local-guide` at `321aef0015`,
  preserving all 22 preceding customization commits.
- Committed Turnstile as `e5e1424bbf`; the contact styling checkpoint remains
  `0ee582bbbc`.
- Fast-forwarded the baseline through both contact-form commits. All former
  feature tips remain ancestors of the maintained baseline; no commits were rewritten.
- Consolidated the workflow/handoff in a separate documentation commit.
- Retired the merged `codex/` feature branches locally and on origin after publication.
  `develop`, upstream branches, release tags and unmerged Dependabot branches remain.

The 38 focused contact/configuration tests passed on November 5, with mocked mail.
JavaScript syntax, XHTML parsing, script version and whitespace checks passed.
Turnstile remains configured locally with official dummy keys. No remote application
was deployed. Code publication does not transfer Payara settings, databases or files.

## Upgrades and deployment records

For a future explicitly requested upgrade, start from the accepted UaRD baseline:

```bash
git fetch upstream --tags
git switch -c upgrade/6.13 custom/6.12
git merge v6.13
```

Review upstream source changes using `docs/upstream-modifications.md`, test, and
establish `custom/6.13` when accepted. Preserve shared history with merge-based upgrades.

Use UaRD-specific tags such as `uard-6.12.1` for confirmed deployment revisions.
Record the deployed commit, artifact and required environment configuration.
Do not invent deployment tags for code that has only been merged or tested locally.
