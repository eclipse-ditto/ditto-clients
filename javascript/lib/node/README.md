# Ditto JavaScript Node.js client

Implementation of the Eclipse Ditto JavaScript API that uses functionality of a Node.js environment, 
e.g. `Buffer`.

It is published to the npm registry as CommonJS module.

## Building
Basically it makes sense to trigger the build process once from
the [parent module](../../README.md). Then you'll be able to
use the default build process in here:

```shell
npm install
npm run build
npm run lint
npm test
# or npm run test:watch
```

## Using

```shell
npm i --save  @eclipse-ditto/ditto-javascript-client-node
```

Create an instance of a client:

```javascript
const domain = 'localhost:8080';
const username = 'ditto';
const password = 'ditto';

// could also use newWebSocketClient() for the WebSocket implementation
const client = DittoNodeClient.newHttpClient()
            .withoutTls()
            .withDomain(domain)
            .withAuthProvider(NodeHttpBasicAuth.newInstance(username, password))
            .build();
```
To use a path other than `/api` to connect to ditto, the optional step `.withCustomPath('/path/to/api')` can be used.

To find out how to use the client, have a look at the [api documentation](../api/README.md#Using-the-client),
since the API will stay the same no matter what implementation is used.


### Proxy
The Node.js implementation supports setting up a proxy. 
It supports reading `http_proxy` / `HTTP_PROXY` for HTTP and WS, and `https_proxy` / `HTTPS_PROXY`
for HTTPS and WSS, or manually setting the proxy settings.

For both explicit and environment-configured proxies, `no_proxy` (or `NO_PROXY`) excludes destinations from proxying
for HTTP, HTTPS, WS and WSS. A nonempty lowercase variable takes precedence. Settings are read when
the client is constructed; each request is matched against its destination, not the proxy address.
Entries may be separated by commas or whitespace and are case-insensitive; a trailing DNS root dot is ignored. A bare hostname or IP
matches the host and its subdomains; IP addresses match exactly. `.example.org` or `*.example.org`
matches subdomains only. An optional `:port` limits the match to that port; omitted URL ports
use 80 for HTTP/WS and 443 for HTTPS/WSS. IPv6 literals such as `::1` are accepted; use brackets when
specifying a port, e.g. `[::1]:8080`. `*` excludes all destinations, including when it is an entry
in a list; `*:8080` excludes all destinations on port 8080. CIDR ranges, URLs and arbitrary wildcard
patterns are unsupported and cause one warning per client construction, not per request.

Explicit `proxyOptions.url` overrides environment proxy URLs, but still honors exclusions. Setting
`ignoreProxyFromEnv: true` ignores all proxy environment variables, including exclusions, and can
be used with an explicit URL to force every destination through that proxy.

```javascript
// may also omit one or more of the options
const proxyOptions = {
  url: 'PROXY-URL:PROXYPORT',
  username: 'PROXY-USERNAME',
  password: 'PROXY-PASSWORD'
}

DittoNodeClient.newHttpClient(proxyOptions)
//  ...
```
Any options that are set manually will override options that are read from an environment variable.


### TLS

For `wss://` (and `https://`) connections the server certificate is validated against the
system's trusted certificate authorities and the requested hostname, using Node.js' secure
defaults. No configuration is required for the common case.

If the server presents a certificate that is not part of the system trust store (e.g. a
corporate root CA or a self-signed certificate), the trusted certificate(s) can be supplied
via the optional `tlsOptions` parameter of `newWebSocketClient(...)`:

```javascript
const fs = require('fs');

const tlsOptions = {
  ca: fs.readFileSync('corp-root.pem')
};

DittoNodeClient.newWebSocketClient(undefined, tlsOptions)
//  ...
```

`tlsOptions` also accepts `cert` / `key` / `passphrase` / `pfx` for mutual TLS.

> **Warning:** certificate validation can be disabled explicitly with
> `{ rejectUnauthorized: false }`. This exposes the connection to man-in-the-middle attacks
> and must only be used for local development against a self-signed certificate — never in
> production.
