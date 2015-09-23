How to make a release
=====================

* remove `-SNAPSHOT` from version in `pom.xml`
* commit/push all changes
* run the following Maven command:
  ```
  mvn clean install
  ```
* create a new release tag on github (`java-X.Y.X`, with `X.Y.Z` taken from 
  `pom.xml`), add some release notes and upload the `-bin.zip`
* increment version number in `pom.xml` and add `-SNAPSHOT` again
* commit/push all changes
