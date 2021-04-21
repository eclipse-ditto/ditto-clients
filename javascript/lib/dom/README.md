# Ditto JavaScript DOM client

Implementation of the Eclipse Ditto JavaScript API that uses functionality of DOM environments, 
e.g. `btoa()` or `WebSocket`.

It is published as an ES6 module. You could also use a CDN like [UNPKG](https://unpkg.com/) to directly use it
in an HTML document (although "_very experimental_", use the `?module`-flag when importing from UNPKG).


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
npm i --save  @eclipse-ditto/ditto-javascript-client-dom
```

Create an instance of a client:

```javascript
const domain = 'localhost:8080';
const username = 'ditto';
const password = 'ditto';

// could also use newWebSocketClient() for the WebSocket implementation
const client = DittoDomClient.newHttpClient()
            .withoutTls()
            .withDomain(domain)
            .withAuthProvider(DomHttpBasicAuth.newInstance(username, password))
            .build();
```
To use a path other than `/api` to connect to ditto, the optional step `.withCustomPath('/path/to/api')` can be used.

To find out how to use the client, have a look at the [api documentation](../api/README.md#Using-the-client),
since the API will stay the same no matter what implementation is used.
