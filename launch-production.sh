#Remove old log
rm nohup.out

#Launch jar
nohup java -jar htmlcheck.jar --spring.profiles.active=prod &
