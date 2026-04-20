# Tooling

This repository keeps reusable automation under `tooling/` instead of the top-level `scripts/` folder.

- `tooling/generic-mobile-ci/`: portable CI and release helpers that can be copied into another mobile repo.
- `tooling/project/`: BiciRadar-specific helpers such as smoke commands, git hooks, icon generation, and version bumping.

`tooling/project/bump_version.sh` intentionally stays project-specific. The files to edit, version format, and release policy depend on each app; if you want it automated later, wire it into a tag-triggered GitHub Action in this repo rather than treating it as a generic helper.
