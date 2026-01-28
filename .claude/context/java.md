# Java Client

## Build Commands (Maven)

```bash
cd java
mvn clean install                    # Build with tests
mvn test                             # Unit tests only
mvn verify                           # Unit + integration tests
mvn test -Dtest=ClassName            # Run single test class
mvn test -Dtest=ClassName#methodName # Run single test method
mvn license:check                    # Verify license headers
```

## Architecture

Single Maven module producing `org.eclipse.ditto:ditto-client` artifact.

Key packages under `org.eclipse.ditto.client`:
- `configuration/` - Authentication providers (Basic, OAuth2/JWT) and messaging configuration
- `twin/` - Digital twin CRUD operations via Twin channel
- `live/` - Live channel for real-time device communication
- `changes/` - Change notification subscriptions
- `messaging/` - WebSocket transport layer (uses nv-websocket-client)
- `policies/` - Policy management
- `options/` - Request options and query parameters

Entry point: `DittoClients.newDisconnectedInstance()` → `connect()` → `DittoClient`

## Key Dependencies

- nv-websocket-client (WebSocket)
- minimal-json (JSON parsing)
- reactive-streams

## Code Style

Google Java Style Guide with 120 character line limit.
