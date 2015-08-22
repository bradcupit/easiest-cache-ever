_Note: when using Spring (and not the AspectJ compiler or AspectJ load-time weaver), only public methods can be cache-enabled. Furthermore, when one method in a class calls a public method in itself (or it's subclasses) that cannot be cache-enabled either._

# webapp with Maven #

  * update your pom so it depends on easiest-cache-ever ([more info](#Make_your_Maven_project_depend_on_easiest-cache-ever.md))
  * add classpath:easiest-cache-ever-spring.xml to your web.xml's contextConfigLocation param ([more info](#More_detailed_web.xml_config.md))

See [this project](http://code.google.com/p/easiest-cache-ever/source/browse/#svn/spring-aop-test/trunk) for a working demo.

# webapp without Maven #

  * download either of the 'all dependencies' zips from [here](http://code.google.com/p/easiest-cache-ever/downloads/list)
  * unzip and put all jars on the classpath
  * add classpath:easiest-cache-ever-spring.xml to your web.xml's contextConfigLocation param ([more info](#More_detailed_web.xml_config.md))

# non-webapp with Maven #

  * update your pom so it depends on easiest-cache-ever ([more info](#Make_your_Maven_project_depend_on_easiest-cache-ever.md))
  * tell Spring to also load "classpath:easiest-cache-ever-spring.xml" ([more info](#Tell_Spring_about_easiest-cache-ever_in_a_non-webapp.md))

See [this project](http://code.google.com/p/easiest-cache-ever/source/browse/#svn/spring-aop-test/trunk) for a working demo.

# non-webapp without Maven #

  * download either of the 'all dependencies' zips from [here](http://code.google.com/p/easiest-cache-ever/downloads/list)
  * unzip and put all jars on the classpath
  * tell Spring to also load "classpath:easiest-cache-ever-spring.xml" ([more info](#Tell_Spring_about_easiest-cache-ever_in_a_non-webapp.md))

### Make your Maven project depend on easiest-cache-ever ###
If easiest cache ever isn't in the maven central repo, you can still depend on it by installing it locally on your machine. Just checkout the source, and run `mvn install`, then depend on easiest-cache-ever like normal in your pom.

### More detailed web.xml config ###
In your web.xml, you probably have something like:
```
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext*.xml</param-value>
</context-param>
```
just change this line:
```
<param-value>classpath:applicationContext*.xml</param-value>
```
to this:
```
<param-value>classpath:applicationContext*.xml classpath:easiest-cache-ever-spring.xml</param-value>
```

### Tell Spring about easiest-cache-ever in a non-webapp ###
You probably have some code like this:
```
ApplicationContext appContext = new ClasspathXmlApplicationContext("applicationContext.xml");
```

Just change that line to:

```
ApplicationContext appContext = new ClasspathXmlApplicationContext(new String[] {
                                    "applicationContext.xml",
                                    "classpath:easiest-cache-ever-spring.xml" });
```