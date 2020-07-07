#Build (skip tests)
mvn -Dmaven.test.skip=true clean package

#Remove obsolete jar
rm htmlcheck.jar

#Copy jar
cp ./target/htmlcheck.jar htmlcheck.jar
