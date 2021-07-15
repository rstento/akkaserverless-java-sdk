# publish sbt and maven artifacts
sbt publishM2
SDK_VERSION=$(sbt "print sdk/version" | tail -1)
cd maven-java
mvn versions:set -DnewVersion=$SDK_VERSION

# cleanup
rm pom.xml.versionsBackup
rm */pom.xml.versionsBackup

for i in samples
do
  (
    cd $i
    mvn versions:set -DnewVersion=$SDK_VERSION
    rm pom.xml.versionsBackup
  )
done