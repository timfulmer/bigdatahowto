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
processed.  Additional word stats may be displayed.
This gives us a nice example with all three aspects of a modern Big Data system:

 - Combinatorial data complexity;
 - Responsiveness in a large problem space;
 - High volume (when load tested).

We'll allow up to 7 characters, all lower case.  This gives us an initial search
space of:

```
   (26^7 + 26^6 + 26^5 + 26^4 + 26^3 + 26^2 + 26^1)/(1024*1024*1024) ~= 7.8GB
```

For raw data, in a worse case / fully populated scenario.  Our stemming
algorithm adds another ~ 350% to
the raw data size.  We're looking at an overall search space of around 35GB when
fully populated.  Which fits nicely into an S3 bucket.

Our free-tier ElastiCache instance can only hold 256MB in memory.  S3 is used as
a fallback.

##Interface Design

JSON is used for the interface.  This supports Apache Base load testing, and
makes
integrating with web technologies easy.  Interface is generalized, allowing
functions to be associated with data requests.

Use case has following actions:

 - Query word: Ask the system the current count of a letter combination, or
 word;

```
    GET /public/<word>/count
```

Returns the count property of the meta data stored with key '<word>'.

 - Submit word: Submit a word to the system for counting;

```
    POST /public/<word> "function(env,word,meta){
        // Define stem behavior.
        env.persistFunction(env,word,meta){
            // Keep track of where data comes from.
            meta.type='processed';
            // Update count returned from GET request.
            if(!meta.count) meta.count= 0;
            meta.count= meta.count+ 1;
        }
        // Input validation.
        if(!word || word.length>7) return false;
        // Can't go back and infer later.
        meta.type='raw';
        // Decompose into stems, and persist each one.
        var split= [];
        for(var i=0; i<word.length;i++){
            split.push({key:word.substring(0,i),
                    persist:env.persistFunction(env,word,meta)});
        }
        return split;
    }"
```

Returns a Job UUID.

 - Query Job: Query the status of a word submission.

```
    GET /jobs/<uuid>
```

Returns Job status.

##Detailed Design

The main portion of our Big Data system is built using Java, packaged as a set
of three JARs.  The three packages are:

 - Core: Contains the core abstractions tying everything together;

 - Defaults: A set of default implementations of the Core, specifically
 targeting AWS.

 - API: BigData API and helper classes.

####Core:

 - Repository interface for storing key, meta, UUID & job information;
 - Queue interface for SQS;
 - Message object: {key[,value][,persist][,delete][,get][,error][,options]};
 - Job state machine;
 - JavaScript runtime;
 - User authentication.

Obligatory diagram:

![BigDataCollaboration](http://bigdatahowto.info/images/BigDataCollaboration.png)

Limitations:

 - Keys up to 30-ish characters;
 - Message object up to 1MB;
 - JS runtime up to 4MB.

Options:

 - Always perform function (false);
 - Private value (false);
 - Notification (false);