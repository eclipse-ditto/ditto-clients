# Changelog
All notable changes to the Ditto JavaScript client will be documented in this file.

## [3.1.0] - 2022-12-15

### Dependencies

No dependencies were explicitly updated.

### \#208 Add support for cursor pagination to JS clients

As per the [documentation](https://www.eclipse.org/ditto/basic-search.html#rql-paging-deprecated), 
pagination via the `limit` parameter is deprecated and might get removed in the future.  
PR #208 adds support for cursor pagination to the JS client so applications using the client can use the new 
pagination style.


## [3.0.1] - 2022-11-17

### Dependencies

No dependencies were explicitly updated.

### \#202 JS Client was incompatible with Typescript 4.7

The released Ditto JavaScript client 3.0.0 did not work in Typescript projects.  
PR #203 provides a fix for that.


## [3.0.0] - 2022-09-28

### Dependencies

No dependencies were explicitly updated.

### \#169 Improved error handling for HTTP rejections

Error handling for the Ditto JavaScript client did not return the status code.
And if the error response was not JSON formatted, the error could not be parsed and `undefined` was resolved as `error`.

This has been fixed by adding an ErrorResponse which is returned instead:
```typescript
interface ErrorResponse {
  /** The status code of the error response. */
  status: number;
  /** The body of the error response. */
  body: any;
  /** The headers of the error response inside a map. */
  headers: Map<string, string>;
}
```

### \#193 Preserve headers when responding to messages

The `DittoHeaders` of a message were not preserved when subscribing to consume messages via the Ditto JavaScript client.  
Without access to the headers and the contained `correlation-id` it was not possible to send back a correlated 
response message.

This has been fixed. The headers are now available when e.g. using the API in such a way:
```typescript
const messages = client.getMessagesHandle();
await messages.requestMessages();
messages.subscribeToThing(thingId, (msg) => {
  console.log(msg.headers);
  console.log(msg);
});
```



## [2.4.0] - 2022-04-14

### No changes
No changes comparing to 2.3.0 were done.

## [2.3.0] - 2022-01-21

### No changes
No changes comparing to 2.2.0 were done.

## [2.2.0] - 2021-11-22

### Updated dependencies
The `ws` dependency used in the `node` package was updated to the latest version in major version 7: `7.5.5` in order 
to fix a critical bug in the previously used version `7.1.2` which was fixed in `7.4.6`.

## [2.1.0] - 2021-09-27

### \#156 Fix `http:` connections
Fixes #155: Fixed connections via `http:` protocol, which used the wrong library and proxy agent. Also added a new option
`ignoreProxyFromEnv` to `ProxyOptions`, which allows disabling the automatic configuration of the proxy if
the respective environment variables are found (`HTTPS_PROXY` or `HTTP_PROXY`).

### \#160 Fix encoding of search options
Fixes #157 (1/2): The search options for search requests were encoded twice before a request and thus caused an error
for order options like `+thingId`.

### \#161 Send query params with node client
Fixes #157 (2/2): Fixes a bug, that the node client wasn't sending query params to the backend at all. 

## [2.0.0] - 2021-05-06

### \#112 Update npm module structure
The api module isn't published to npm any long. The node and dom module don't contain
the Ditto version in their name any longer, but are bound to the Ditto version itself
(and thus have the same version numbers as Ditto).

### \#142 Remove API 1
Removes all API 1 related code from the client (namely ACLs and the API 1 builders).

### \#166 Refactor model structure: 
Fixes #114: Refactor model structure to avoid "duplicated paths" like `features.features` for all entities inheriting from `IndexedEntityModel` and simplify type generics.

### \#66 fix PUT request for existing thing
Fixes #61: Ditto will return an empty body with status `204` for `PUT` requests on already existing things.
This was not handled correctly by the client.
    
### \#126 expose definition field of Thing
Fixes #127: Adds the `definition` field to the Thing representation and adds methods for retrieving, updating
and deleting the definition of a Thing.

### \#111 allow setting custom api-path
Adds a new builder step for the client, which allows setting a custom path instead of the default `/api` or `/ws`.

### \#117 add basic support for _metadata
Adds basic support for `Metadata` in the `Thing` model. Does not yet allow setting metadata from the client.

### \#124 Fix serialization of filters
Fixes #123: Fixes the serialization of booleans and numbers in filters.

### \#140 Add support for _created field
Adds the `_created` field to the `Thing` model.

### \#155 Update vulnerable dependencies
Update vulnerable dependencies as suggested by `npm audit`

### Breaking changes

* `DittoDomClient` and `DittoNodeClient`: The builder steps `apiVersion1()` and `apiVersion2()` were removed completely.
* Module `@eclipse-ditto/ditto-javascript-client-api_1.0` was removed completely and is no longer needed
  for using the client.
* Module `@eclipse-ditto/ditto-javascript-client-node_1.0` was renamed to `@eclipse-ditto/ditto-javascript-client-node`
* Module `@eclipse-ditto/ditto-javascript-client-dom_1.0` was renamed to `@eclipse-ditto/ditto-javascript-client-dom`
* `Acl` and all subclasses: Removed completely.
* `Features`: Needs to be accessed using `thing.features` instead of `thing.features.features`
* `Features`: `toObject` instance method was removed. Use `Features#toObject` and `Features#fromObject` instead.
* `Entries`: Needs to be accessed using `policy.entries` instead of `policy.entries.entries`
* `Entries`: `toObject` instance method was removed. Use `Entries#toObject` and `Entries#fromObject` instead.
* `Resources`: Needs to be accessed using `policy.entries.<entryId>.resources` instead of `policy.entries.<entryId>.resources.resources`
* `Resources`: `toObject` instance method was removed. Use `Resources#toObject` and `Resources#fromObject` instead.
* `Subjects`: Needs to be accessed using `policy.entries.<entryId>.subjects` instead of `policy.entries.<entryId>.subjects.subjects`
* `Subjects`: `toObject` instance method was removed. Use `Subjects#toObject` and `Subjects#fromObject` instead.
* `IndexedEntityModel`: The signature of some methods have been changed, see e.g. `Features` on how to update custom implementations to the new format. 

## Releases in old version format

### @eclipse-ditto/ditto-javascript-client-<module>_1.0 [2.1.0] - 2021-01-26

#### \#108 Add a bearer token AuthProvider:
Adds a new `AuthProvider` implementation to allow authentication with a Bearer token.

#### \#95 Check proxyAgent.options.path:
Fixes: #93: Fixes bug where node http client was connecting using the proxy agent, even if it was not set.
