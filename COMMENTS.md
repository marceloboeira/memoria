### Comments
> Random thoughts / decisions for the application

The implementation was done gradually, with focus on the result.

* Language - I have decided to use Scala since it is the closest exposure that I have to JVM (besides JRuby) and it is what I have been studying lately.
* HTTP Server - I have decided to use Finch, a small functional wrapper for Finagle HTTP. Mostly, because it is light-weighted, easy to bootstrap and my only exposure so far with Scala + HTTP.

### First version

My first idea was to implement a basic lock-based mutable array as the cache, so the upload endpoint only inserts in memory. O(1).

The second endpoint, for statistics, only fetches the already aggregated values from a memory entry representing the statistics. Also, O(1).

A thread "worker" runs in the background constantly, with a defined interval. The worker performs the aggregations and stores in the shared instance of the statistics cache.

Problems:
  * The lock-based mutable array did not perform well under high load, since the lock was blocking the request of the multithreaded webserver, we we're holdingthe request open waiting for the resource to be unlocked. Obviously, creating a non-linear performance for the endpoint.
  * Even though the worker runs in the background, it locks the cache to read/aggregate, which makes both enpoints slow on reading from memory.

### Second version

For the second version I have focused on making the POST faster / safer, by using a Queue. Instead of writing to the blocking array in memory, it just adds a new entry to the ConcurrentQueue and releases the thread of the HTTP server.

Now, we have 2 threads running in the background, one of them consumes the queue and perform the blocking calls to memory, the other ensures the aggregation of the cache.

Improvements:
  * Now the HTTP server does not hold the client connected to ensure the persistence of the blocking call to the huge array.
  * Two different "abstractions" for the workers, one to consume the queue and the other to aggreate.

Problems:
  * Still now performatic enough under high load on both endpoints, around 100 concurrent requests for example, it gets stuck frequently.
  * It shares too much the resources along the threads, it would be nicer to have some sort of self-managed abstraction to deal with memory from the outside of the webserver and just expect that everytime new data comes it refreshes the cache, and the the epoch timestamps act like a TTL...

### Future / Improvements
  * Fundamentals - I have enjoyed doing the challenge, I believe there are much better and more reliable options to solve it, and I am keen to continue experimenting with it. One thing that crossed my mind was to use Actors to isolate the memory and the enpoints would only send messages to the Actors, coupling the memory management with the actor, making it the absolute owner of the state. (sub-actors could work on making it concurrent enough if necessary).

  * Abstractions - One thing that I still have to study is how to create good abstractions with Scala, unfortunally, Ruby does not have so many resources like Generics, Abstract Classes, Traits, Case Classes...  so probably I haven't taken advantage of Scala for that, and even misused some concepts.

  * Testing - Finally, one thing that I would like to improve is integration tests, to simulate real-world scenarios (concurrent access) and maybe even stress tests.

### Funny thoughts
  * I have tried to implement something similar with Ruby, but considering the GIL of MRI, it has a terrible performance. It gets to a point that the aggregation thread can block the webserver.
  * I have implemented a small version with Crystal-Lang, considering the language has Channels to manage concurrent writes, the implementation was not so challenging that way, however, the language is a bit unstable for threading (they actually use Fibers) and the results were far from satisfatory (I have to invest more time to figure it out why).
