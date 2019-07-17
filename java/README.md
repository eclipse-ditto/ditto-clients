# Eclipse Ditto Java client SDK

## Purpose

Deliver a client SDK for Java in order to interact with digital twins provided by an Eclipse Ditto backend.

## Features

* Digital twin management: CRUD (create, read, update, delete) of Ditto [things](https://www.eclipse.org/ditto/basic-thing.html)
* [Change notifications](https://www.eclipse.org/ditto/basic-changenotifications.html): 
  consume notifications whenever a "watched" digital twin is modified 
* Send/receive [messages](https://www.eclipse.org/ditto/basic-messages.html) to/from devices connected via a digital twin
* Use the [live channel](https://www.eclipse.org/ditto/protocol-twinlive.html#live) in order to react on commands directed
  to devices targeting their "live" state

## Communication channel

The Ditto Java client interacts with an Eclipse Ditto backend via Ditto's 
[WebSocket](https://www.eclipse.org/ditto/httpapi-protocol-bindings-websocket.html) sending and receiving messages
in [Ditto Protocol](https://www.eclipse.org/ditto/protocol-overview.html).
