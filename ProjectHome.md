## Is this really the easiest cache ever? ##
It doesn't get easier than this. Once setup, all you need to enable caching is an annotation:
```
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

## Links ##
  * [I still need more convincing](IStillNeedMoreConvincing.md)
  * [Show me the features](Features.md)
  * [Setup Guide](SetupGuide.md)
  * [FAQ](FAQ.md)

