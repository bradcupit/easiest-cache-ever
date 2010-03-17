This project is a manual test to ensure easiest-cache-ever works.

To run this project, first install easiest-cache-ever to your local
maven repostory. cd to your easiest-cache-ever directory. Then run:
    mvn install

Next cd to spring-aop-test directory. Then run
    mvn tomcat:run

In your browser, go to:
    http://localhost:8080/spring-aop-test/main

You should see a page saying either "The cache is working" or
"The cache is not working".