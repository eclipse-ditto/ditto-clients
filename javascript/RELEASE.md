# Releasing Ditto JavaScript Client

Perform the following steps

## Update the [CHANGELOG.md](CHANGELOG.md)

The JavaScript client has a separate changelog, update the included fixes/changes in there before the release.

## Build Ditto JavaScript client release

* Go to javascript folder and execute `npm run update-version`
  * Choose/set the correct version, which is also used by Ditto.
  * You might run into an error if the current branch is not allowed: `lerna ERR! ENOTALLOWED Branch '<branch-name>' is restricted from versioning due to allowBranch config.`
    * You can use an extra parameter in this case: `npm run update-version -- --allow-branch <branch-name>`
* Verify the versions are correctly set in
  * javascript/lerna.json
  * javascript/lib/api/package.json
  * javascript/lib/api/package-lock.json
  * javascript/lib/dom/package.json
  * javascript/lib/dom/package-lock.json
  * javascript/lib/node/package.json
  * javascript/lib/node/package-lock.json
* Update the versions manually in
  * javascript/package.json
  * javascript/package-lock.json
* Create a commit with all updated versions and give it a tag: js_<client-version>
  * this might be the same tag as for the Ditto Java Client release or a separate one, e.g. js_1.1.2, js_2.1.1, etc.
  * `git tag -a js_1.0.0 -m "tag Ditto JavaScript 1.0.0 release"`
  * `git push eclipse js_1.0.0`

## Release the Client

* Retrieve the npm token of the Eclipse Ditto npm user.
* Use the release job on Eclipse Ditto jenkins: 
  * https://ci.eclipse.org/ditto/job/ditto-client-javascript-release/ 
  * it will read the version that should be published from package.json