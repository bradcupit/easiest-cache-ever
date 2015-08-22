## Input Parameters ##
You can pass in a parameter, and the cache will be based on those inputs:
```
@CacheReturnValue
public User findUser(String name) {
    // perform some long operation
}
```
```
findUser("Elmo");   // not found in cache
findUser("Elmo");   // found in cache!
findUser("Grover"); // not found in cache
```

## Set max size ##
Let's say you want to cache only 5 items, no more. That's easy, and if more items get added, the least recently used is kicked out:
```
@CacheReturnValue(maxSize = 5)
public User lookupUser(String name) { ... }
```

## Expire by time ##
Or maybe you want to expire items 5 minutes after they were put in the cache? Here's how:
```
@CacheReturnValue(expirationTime = 5, unit = TimeUnit.MINUTES)
public User lookupUser() { ... }
```