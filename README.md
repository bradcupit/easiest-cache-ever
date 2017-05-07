## Is this really the easiest cache ever? ##
It doesn't get easier than this. Once setup, all you need to enable caching is an annotation:
```java
@CacheReturnValue
public User lookupUser(String userId) {
    // database call, web service call, or some other long running operation
}
```
After the method has been called once, it won't be called again until the cache fills up or time expires. easiest-cache-ever intercepts future calls and returns the cached value instead.

You can also set _size_ or _time_ limits directly on the annotation, or globally for all annotations.

Still need more convincing? Read [here](IStillNeedMoreConvincing.md).

## Prereqs ##
You must be using either Spring 2.0+ or AspectJ.

## Features ##
  1. ridiculously easy to use, read-only cache.
  1. both time and size-based caching
  1. customizable settings per annotation, or use the defaults that _you_ set for the entire application (in a Spring xml config file)
  1. completely thread-safe
  1. class-level annotations (if you need caching on every method in a class)

## Why not just manually cache? ##
Good question. A manual cache is fairly easy to implement.
```java
private Map<String, User> userCache = new HashMap<String, User>();

public User findUser(String name) {
    User user = userCache.get(name);
    if (user == null) {
        user = someLongOperation();
        userCache.put(name, user);
    }

    return user;
}
```

That's not bad. Simple enough, and any Java coder can understand it. What happens if we need a few caches though?
```java
private Map<String, User> userCache = new HashMap<String, User>();
private Map<String, Admin> adminCache = new HashMap<String, Admin>();
private Map<String, Process> processCache = new HashMap<String, Process>();

public User findUser(String name) {
    User user = userCache.get(name);
    if (user == null) {
        user = someLongOperation();
        userCache.put(name, user);
    }

    return user;
}

public User findAdmin(String name) {
    Admin admin = adminCache.get(name);
    if (admin == null) {
        admin = someLongOperation();
        adminCache.put(name, admin);
    }

    return admin;
}

public User findProcess(String name) {
    Process process = processCache.get(name);
    if (process == null) {
        process = someLongOperation();
        processCache.put(name, process);
    }

    return process;
}
```

Each chunk of code sure does look similar, doesn't it? Plus, the caching actually clouds the main logic.

Also, what if we wanted something with a little more functionality? What about a cache that expired entries after a set amount of time? Or what about a cache that had a maximum size, and kicked out the oldest entry when that max was reached?

For size-based, we could use [Apache Commons Collection's](http://commons.apache.org/collections/) [LRUMap](http://commons.apache.org/collections/api-release/org/apache/commons/collections/map/LRUMap.html) (or a version supporting generics [here](http://larvalabs.com/collections/)). For time-based we can use [Google Guava's](http://code.google.com/p/guava-libraries/) (formerly known as Google Collections) [MapMaker](http://guava-libraries.googlecode.com/svn/trunk/javadoc/index.html), which can build a map with an expiration time.

But what if we need both time-based and size-based? We'd have to write one ourselves, and it turns out that's hard to make thread-safe and highly scalable.

We could use an existing cache, like [ehcache](http://ehcache.org/), which would give us both time limits and size limits:

```java
cacheManager.addCache(new Cache(USER_CACHE, MAX_ELEMENTS_IS_FIVE_HUNDRED, false, false, FIVE_MINUTES, 0));

...

public User findUser(String name) {
    Cache cache = cacheManager.getCache(USER_CACHE);
    Element element = cache.get(name);
    User user;
    if (element != null) {
        user = (User) element.getValue();
    } else {
        user = someLongOperation();
        cache.put(new Element(name, user));
    }

    return user;
}
```

That code is more cumbersome then just using a Map. Ehcache also supports xml configuration, which might make the Cache creation look better, but then we have switch to another file to configure each cache, and we would still have to write the cumbersome code in the findUser(..) method.

Since what we want is so simple, we should have a very simple way of using it. With easiest-cache-ever you get a super-easy, read-only cache in just one line of code:
```java
@CacheReturnValue(maxSize = 500, expirationTime = 5, unit = TimeUnit.MINUTES)
public User findUser() { ... }
```

On top of that, it's built on ehcache which provides [great scalability](http://gregluck.com/blog/archives/2009/02/ehcache-1-6-2-orders-of-magnitude-faster/).

## Features ##

### Input Parameters ###
You can pass in a parameter, and the cache will be based on those inputs:
```java
@CacheReturnValue
public User findUser(String name) {
    // perform some long operation
}
```
```java
findUser("Elmo");   // not found in cache
findUser("Elmo");   // found in cache!
findUser("Grover"); // not found in cache
```

### Set max size ###
Let's say you want to cache only 5 items, no more. That's easy, and if more items get added, the least recently used is kicked out:
```java
@CacheReturnValue(maxSize = 5)
public User lookupUser(String name) { ... }
```

### Expire by time ###
Or maybe you want to expire items 5 minutes after they were put in the cache? Here's how:
```java
@CacheReturnValue(expirationTime = 5, unit = TimeUnit.MINUTES)
public User lookupUser() { ... }
```

## Setup Guide ##

_Note: when using Spring (and not the AspectJ compiler or AspectJ load-time weaver), only public methods can be cache-enabled. Furthermore, when one method in a class calls a public method in itself (or it's subclasses) that cannot be cache-enabled either._

### webapp with Maven ###

  * update your pom so it depends on easiest-cache-ever ([more info](#Make_your_Maven_project_depend_on_easiest-cache-ever.md))
  * add classpath:easiest-cache-ever-spring.xml to your web.xml's contextConfigLocation param ([more info](#More_detailed_web.xml_config.md))

See [this project](http://code.google.com/p/easiest-cache-ever/source/browse/#svn/spring-aop-test/trunk) for a working demo.

### webapp without Maven ###

  * download either of the 'all dependencies' zips from [here](http://code.google.com/p/easiest-cache-ever/downloads/list)
  * unzip and put all jars on the classpath
  * add classpath:easiest-cache-ever-spring.xml to your web.xml's contextConfigLocation param ([more info](#More_detailed_web.xml_config.md))

### non-webapp with Maven ###

  * update your pom so it depends on easiest-cache-ever ([more info](#Make_your_Maven_project_depend_on_easiest-cache-ever.md))
  * tell Spring to also load "classpath:easiest-cache-ever-spring.xml" ([more info](#Tell_Spring_about_easiest-cache-ever_in_a_non-webapp.md))

See [this project](http://code.google.com/p/easiest-cache-ever/source/browse/#svn/spring-aop-test/trunk) for a working demo.

### non-webapp without Maven ###

  * download either of the 'all dependencies' zips from [here](http://code.google.com/p/easiest-cache-ever/downloads/list)
  * unzip and put all jars on the classpath
  * tell Spring to also load "classpath:easiest-cache-ever-spring.xml" ([more info](#Tell_Spring_about_easiest-cache-ever_in_a_non-webapp.md))

### Make your Maven project depend on easiest-cache-ever ###
If easiest cache ever isn't in the maven central repo, you can still depend on it by installing it locally on your machine. Just checkout the source, and run `mvn install`, then depend on easiest-cache-ever like normal in your pom.

### More detailed web.xml config ###
In your web.xml, you probably have something like:
```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext*.xml</param-value>
</context-param>
```
just change this line:
```xml
<param-value>classpath:applicationContext*.xml</param-value>
```
to this:
```xml
<param-value>classpath:applicationContext*.xml classpath:easiest-cache-ever-spring.xml</param-value>
```

### Tell Spring about easiest-cache-ever in a non-webapp ###
You probably have some code like this:
```java
ApplicationContext appContext = new ClasspathXmlApplicationContext("applicationContext.xml");
```

Just change that line to:

```java
ApplicationContext appContext = new ClasspathXmlApplicationContext(new String[] {
                                    "applicationContext.xml",
                                    "classpath:easiest-cache-ever-spring.xml" });
```

## Questions ##

### Which license is this released under? ###
The Apache License 2.0, which basically says you can use this library or it's source code in any project (even commercial ones). You accept the code as-is, and can't sue me if something goes wrong. If you distribute the code or compiled files, or the jar (whether you modified it or not), you must include a copy of the license. If you just distribute the jar, it already includes the license in the jar, so you don't have to do anything.

### Why does this require Spring or AspectJ? ###
easiest-cache-ever uses AOP (Aspect Oriented Programming), so you have to do very little work to enable caching. A plain annotation on a method doesn't really do much on it's own. The smarts are in the code which processes the annotation, and how will that smart code have access to your method which needs caching? The answer is AOP, and the simplest way to setup AOP is through Spring. If you want to get fancy, you can use AspectJ (a language based off Java that makes Aspects first class citizens). The middle ground is an AspectJ load-time-weaver.

### Why isn't caching working? ###

Is Spring/AspectJ caching setup properly? See the SetupGuide for help.

If using Spring AOP, are you calling a non-public method? Only public methods are supported with Spring AOP. You may want to try load-time-weaving, or make your method public.

With Spring AOP, are you calling a method within your own class? Spring AOP can only intercept calls from one object to another. It can't intercept calls from one object to itself, or from one object to it's super/subclasses. You may want to try load-time-weaving, or refactor your code so it's two separate objects.

### Why use easiest-cache-ever when I already have a Hibernate/JPA cache? ###
If you're already using Hibernate's 2nd level cache, you may not need this project. However, easiest-cache-ever is more than just a database cache. It can cache any call, like a webservice call, a long running computation, an XPath query, etc.
