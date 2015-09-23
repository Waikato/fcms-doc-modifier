How to make a release
=====================

* remove `-SNAPSHOT` from version in `pom.xml`
* Commit/push all changes
* run the following Maven command:
  ```
  mvn clean install
  ```
* create a new release tag on github (vYYYY.MM.DD) and upload the `-bin.zip`
* increment version number in `pom.xml` and add `-SNAPSHOT` again

