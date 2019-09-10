# Ditto JavaScript client API
This module is a TypeScript library to facilitate working the the REST-like HTTP API and web socket API of Eclipse Ditto.

It is published with three different module types to the npm registry:
* IIFE (`dist/index.bundle.js`), for using it directly in a browser. The API is available under the base name
`EclipseDittoJavascriptClientApi`. You can use a CDN like [UNPKG](https://unpkg.com/) to directly
use it in a HTML document without the need to compile or pack anything.
* CommonJS (`dist/index.js`), typical module type for using in Node.js
* ES Module (`dist/index.es.js`)

## Implementation
The already existing implementations can be found in the [parent module](../../README.md).
When providing an own implementation we suggest to start from one of
the existing implementations. Basically you'll need to either
implement `HttpRequester` or `WebSocketImplementation` interfaces
depending on your needs and provide an implementation of its builder interface.

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

## Using the client

The different implementations (dom, node) describe how the provide instances of a `ThingsClient`.
Have a look at them on how to get an instance of the client. We assume
the variable `client` to be an instance of `ThingsClient` for the following usage
explanations.

The client provides different handles to handle requests for specific parts of the Things API:
```
const thingsHandle = client.getThingsHandle();
```

The handles' methods will send requests and return their responses asynchronously. 
For example the code to update a Thing would look like this:
```
const thing = new Thing('the:thing');
thingsHandle.putThing(thing)
    .then(result => console.log(`Finished putting thing with result: ${JSON.stringify(result)}`));
```

Additionally options for requests can be specified and passed on to the methods:
```
const options = FieldsOptions.getInstance();
options.ifMatch('A Tag').withFields('thingId', 'policyId', '_modified');
thingsHandle.getThing('Testthing:TestId', options)
  .then(returnedThing => {
    console.log(`Get returned ${JSON.stringify(returnedThing)}`);
    // handle the Thing that was returned
  });
```

### HTTP Client
Each implementation will provide an HTTP implementation of the `ThingsClient` and
will use HTTP requests to communicate with Eclipse Ditto. The builder for the
client will guide you through the following steps.

```
client = builder
  // You can decide whether the client will use a TLS (https) connection or not:
  .withTls() // or .withoutTls()
  // which domain the client will connect to
  .withDomain('localhost:8080')
  // Which auth provider to use. E.g. for basic auth there are different versions
  // depending on the client implementation. With the node client it might look like this:
  .withAuthProvider(NodeHttpBasicAuth.newInstance(username, password))
  // select the API version of Eclipse Ditto that is used
  .apiVersion2()
  // build it
  .build()
```

### WebSocket Client
Similar to the HTTP Client, each implementation will provide an WebSocket implementation
of the `ThingsClient` that will use a WebSocket connection to communicate with
Eclipse Ditto. The builder for the client will guide you through the following steps.

```
client = builder
  // You can decide whether the client will use a TLS (https) connection or not:
  .withTls() // or .withoutTls()
  // which domain the client will connect to
  .withDomain('localhost:8080')
  // Which auth provider to use. E.g. for basic auth there are different versions
  // depending on the client implementation. With the node client it might look like this:
  .withAuthProvider(NodeWebSocketBasicAuth.newInstance(username, password))
  // select the API version of Eclipse Ditto that is used
  .apiVersion2()
  // You can enable a local buffer of a specific size to handle backpressure and
  // connection problems.
  .withBuffer(15) // or withoutBuffer()
  // Define on which channel the client should work (see Eclipse Ditto documentation on Ditto protocol)
  .twinChannel()
  // build it
  .build()
```

### Errors
There are a few error responses that are not defined within the Eclipse Ditto API. These mainly relate to problems
with the web socket connection.
```
{
  status: 0,
  error: 'connection.unavailable',
  message: 'The websocket is not connected.',
  description: 'The websocket connection to the server failed.'
}
```
This error is returned when the buffer is turned off and the WebSocket connection is not currently established.
A connection is still being attempted.
```
{
  status: 1,
  error: 'connection.interrupted',
  message: 'The websocket connection to the server was interrupted.',
  description: 'The request might have been sent and processed.'
}
```
This error is returned when the WebSocket connection failed while a request was waiting for it's response.
It's not possible to tell whether the request was received by the service or not.
```
{
  status: 2,
  error: 'connection.lost',
  message: 'The websocket connection to the server was lost.',
  description: 'The reconnection to the server was unsuccessful.'
}
```
This error is returned when the connection to the service could not be established/reestablished.
Any future requests will return the same error.
```
{
  status: 3,
  error: 'buffer.overflow',
  message: 'The buffer limit is reached.',
  description: 'You can set a higher buffer size to buffer more requests.'
}
```
This error is returned when the buffer limit set at the start is reached. All requests in the buffer at the time will
stay in the buffer but any new requests will return this error until the buffer starts to be emptied.
