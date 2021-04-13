# Changelog
All notable changes to the Ditto JavaScript client will be documented in this file.

## [2.0.0] - 2021-xx-xx

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

### Breaking changes
* `Features`: Needs to be accessed using `thing.features` instead of `thing.features.features`
* `Features`: `toObject` instance method was removed. Use `Features#toObject` and `Features#fromObject` instead.
* `Entries`: Needs to be accessed using `policy.entries` instead of `policy.entries.entries`
* `Entries`: `toObject` instance method was removed. Use `Entries#toObject` and `Entries#fromObject` instead.
* `Resources`: Needs to be accessed using `policy.entries.<entryId>.resources` instead of `policy.entries.<entryId>.resources.resources`
* `Resources`: `toObject` instance method was removed. Use `Resources#toObject` and `Resources#fromObject` instead.
* `Subjects`: Needs to be accessed using `policy.entries.<entryId>.subjects` instead of `policy.entries.<entryId>.subjects.subjects`
* `Subjects`: `toObject` instance method was removed. Use `Subjects#toObject` and `Subjects#fromObject` instead.
* `Acl`: Needs to be accessed using `thing.acl` instead of `thing.acl.acl`
* `Acl`: `toObject` instance method was removed. Use `Acl#toObject` and `Acl#fromObject` instead.
* `IndexedEntityModel`: The signature of some methods have been changed, see e.g. `Features` on how to update custom implementations to the new format. 

## Releases in old version format

### @eclipse-ditto/ditto-javascript-client-<module>_1.0 [2.1.0] - 2021-01-26

#### \#108 Add a bearer token AuthProvider:
Adds a new `AuthProvider` implementation to allow authentication with a Bearer token.

#### \#95 Check proxyAgent.options.path:
Fixes: #93: Fixes bug where node http client was connecting using the proxy agent, even if it was not set.
