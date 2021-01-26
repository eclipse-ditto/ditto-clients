# Changelog
All notable changes to the Ditto JavaScript client will be documented in this file.

## [2.1.0] - 2021-01-26

### \#108 Add a bearer token AuthProvider:
Adds a new `AuthProvider` implementation to allow authentication with a Bearer token.

### \#95 Check proxyAgent.options.path:
Fixes: #93: Fixes bug where node http client was connecting using the proxy agent, even if it was not set.
