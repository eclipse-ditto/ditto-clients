# JavaScript Client

## Build Commands (Lerna monorepo)

```bash
cd javascript
npm install                          # Install dependencies (runs bootstrap)
npm run build                        # Compile TypeScript + generate barrels
npm run lint                         # TSLint
npm test                             # Jest tests
npm run test:watch                   # Watch mode
npm run clean                        # Clean build artifacts
```

## Architecture

Lerna monorepo with three packages under `lib/`:
- `api/` - Core interfaces and builders (internal, not published)
- `dom/` - Browser implementation (`@eclipse-ditto/ditto-javascript-client-dom`)
- `node/` - Node.js implementation (`@eclipse-ditto/ditto-javascript-client-node`)

Barrelsby auto-generates barrel files during build.

## Key Dependencies

- TypeScript 3.9.x
- Lerna 5.x

## Code Style

TSLint configuration in `javascript/tslint-base.json`.
