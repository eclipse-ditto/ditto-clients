# Ditto JavaScript DOM client

Implementation of the Eclipse Ditto JavaScript API that uses functionality of DOM environments, 
e.g. `btoa()` or `WebSocket`.

It is published with two different module types to the npm registry:
* IIFE (`dist/index.bundle.js`), for using it directly in a browser. The functionality is available under the base name
`EclipseDittoJavascriptClient`. You can use a CDN like [UNPKG](https://unpkg.com/) to directly
use it in a HTML document without the need to compile or pack anything.
* ES Module (`dist/index.es.js`)


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
# replace <ditto-major.minor> with the major and minor version number of Eclipse Ditto you are using.
npm i --save  @eclipse/ditto-javascript-client-api_<ditto-major.minor> @eclipse/ditto-javascript-client-dom_<ditto-major.minor>
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
            .apiVersion2()
            .build();
```
To use a path other than `/api` to connect to ditto, the optional step `.withCustomPath('/path/to/api')` can be used.

To find out how to use the client, have a look at the [api documentation](../api/README.md#Using-the-client),
since the API will stay the same no matter what implementation is used.
