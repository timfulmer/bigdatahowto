#Big Data Howto

A refreshingly technology independent view of Big Data.

In this second installment we introduce a simple use case,
and dive into a little detailed design for our Big Data system.

##Use Case

A variation of the classic Hadoop word counting example is counting the
occurrences of
all stems within a word.  "Hadoop" decomposes into "H", "Ha", "Had", etc.  A web
interface accepts words, showing the occurrence count of the
current letter combination in response to user typing.

When a user submits a
letter combination, it is
added to the system.  Status updates tell the user when their new word has been
processed.  Additional word stats may be displayed.  We'll call it _WorDoink!_
for now.
This gives us a nice example with all three aspects of a modern Big Data system:

 - Combinatorial data complexity;
 - Responsiveness in a large problem space;
 - High volume (when load tested).

We'll allow up to 7 characters, all lower case.  This gives us an initial search
space of:

```
   (26^7 + 26^6 + 26^5 + 26^4 + 26^3 + 26^2 + 26^1)/(1024*1024*1024) ~ 7.8GB
```

For raw data, in a worse case / fully populated scenario.  Our stemming
algorithm adds another ~ 350% to
the raw data size.  We're looking at an overall search space of around 35GB when
fully populated.  Which fits easily into an S3 bucket.

A free-tier ElastiCache instance can only hold 256MB in memory.  S3 is used as
a fallback.

##Interface Design

JSON is used for the interface.  This supports Apache Base load testing, and
makes
integrating with web technologies easy.  Interface is generalized, allowing
functions to be associated with data requests.

Our use case has the following actions:

####Query word
Ask the system the current count of a letter combination, or
word:

```
    GET /data/wordoink/<word>/count
```

Returns the count property of the meta data stored with key '`<word>`'.

####Submit word
Submit a word to the system for counting:

```
    POST /data/wordoink/<word> "function(env,word,meta){
        // Input validation.
        if(!word || word.length>7) return false;
        // Define stem behavior.
        env.persistFunction= function(env,word,meta){
            // Update count returned from the GET request.
            if(!meta.count) meta.count= 0;
            meta.count= meta.count+ 1;
            return true;
        }
        // Set meta data for this word.
        if(!meta.count) meta.count= 0;
        meta.count= meta.count+ 1;
        // Decompose into stems, run persistFunction on each one.
        var stems= [];
        for(var i=1; i<word.length;i++){
            stems.push({key:word.substring(0,i),
                    persist:env.persistFunction});
        }
        return stems;
    }"
```

Returns a Job UUID.

####Query Job
Query the status of a word submission:

```
    GET /job/<uuid>
```

Returns Job status.

##Detailed Design

The main portion of our Big Data system is built using Java, packaged as a set
of three JARs.  The three packages are:

 - Core: Contains the core abstractions tying everything together;

 - Defaults: A set of default implementations of the Core, specifically
 targeting AWS.

 - API: BigData API and helper classes.

####Core

 - Resource interface to external system;
 - Queue interface for SQS;
 - Message object: {key[,value][,persist][,delete][,get][,error][,options]};
 - Job state machine;
 - JavaScript runtime;
 - User authentication.

Where Resource implementations interface with S3 & ElastiCache for storage;
email, WebSockets, etc for notification.

Obligatory diagram:

![BigDataCollaboration](http://bigdatahowto.info/images/BigDataCollaboration.png)

Limitations:

 - A Message is only retried 5 times before being deleted.
 - Message object up to xMB (x to be determined by testing);

Options:

 - Always perform function (false);
 - Private value (false);

####Defaults

We'll be building the system above in iterations.  Initial development is scoped
to implementing an in memory queue, a JavaScript processor, an local file system
resource
and an always allow Authenticator.  SQS, S3, ElastiCache, SpringSecurity and
potentially other technologies can be
added once we've built up a test environment and can measure their impact on
the system.

####API

BD API with the following methods:

 - Add message;
 - Query job;
 - Query metadata;
 - Process job;
