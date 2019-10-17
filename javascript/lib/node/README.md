# Ditto JavaScript Node.js client

Implementation of the Eclipse Ditto JavaScript API that uses functionality of a Node.js environment, 
e.g. `Buffer`.

It is published to the npm registry as CommonJS module.

## Building
Basically it makes sense to trigger the build process once from
the [parent module](../../README.md). Then you'll be able to
use the default build process in here:

```
npm install
npm run build
npm run lint
npm test
# or npm run test:watch
```

## Using

```
# replace <ditto-major.minor> with the major and minor version number of Eclipse Ditto you are using.
npm i --save  @eclipse/ditto-javascript-client-api_<ditto-major.minor> @eclipse/ditto-javascript-client-node_<ditto-major.minor>
```

Create an instance of a client:

```
const domain = 'localhost:8080';
const username = 'ditto';
const password = 'ditto';

// could also use newWebSocketClient() for the WebSocket implementation
const client = DittoNodeClient.newHttpClient()
            .withoutTls()
            .withDomain(domain)
            .withAuthProvider(NodeHttpBasicAuth.newInstance(username, password))
            .apiVersion2()
            .build();
```

To find out how to use the client, have a look at the [api documentation](../api/README.md#Using-the-client),
since the API will stay the same no matter what implementation is used.


### Proxy
The Node.js implementation supports setting up a proxy. 
Currently it supports either reading directly from 'https_proxy' (or 'HTTPS_PROXY') environment variable
or manually setting the proxy settings.

```
// may also omit one or more of the options
const proxyOptions = {
  url: 'PROXY-URL:PROXYPORT',
  username: 'PROXY-USERNAME',
  password: 'PROXY-PASSWORD'
}

DittoNodeClient.newHttpClient(proxyOptions)
  ...
```
Any options that are set manually will override options that are read from an environment variable.
