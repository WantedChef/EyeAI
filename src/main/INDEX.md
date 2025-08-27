# EyeAI Project Index

- Package root: `chef.sheesh.eyeai`
- Main class: `chef.sheesh.eyeai.ChefAI`
- Resources:
  - `plugin.yml`
  - `config/chefai.yml`
  - `messages/en_US.yml`

## Modules
- admin, ai (agents/ml/sim/packets), anticheat, api, bootstrap, commands, combat, core,
  data (repository/storage/cache), features, framework, gui, infra (config/database/di),
  integration, logging, test, trolls, ui, utils, vanish

## Next Steps
- Implement DI container and config loading in `ChefAI#onEnable()`.
- Add Admin GUI entrypoint and commands.
- Wire database via infra/database and repositories.
