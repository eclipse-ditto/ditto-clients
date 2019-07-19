## Eclipse Ditto Clients :: Legal

This folder contains license-information of Ditto Clients itself and used third-parties.

### Update used dependencies

Have a look at folder [3rd-party-dependencies](3rd-party-dependencies) and the shell scripts inside in order to update
the used `compile`, `provided`, `test` dependencies and used `maven-plugins`.

### Update NOTICE-THIRD-PARTY.md

Generate/update the file [NOTICE-THIRD-PARTY.md](NOTICE-THIRD-PARTY.md) like this:

```bash
$ cd ../java # switch to ditto-client/java dir
$ mvn generate-resources -Pgenerate-third-party-licenses
``` 

This will update the [NOTICE-THIRD-PARTY.md](NOTICE-THIRD-PARTY.md) according to the actually used dependencies 
including the license information.
