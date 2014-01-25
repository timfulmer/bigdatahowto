#Big Data Howto

A refreshingly technology independent view of Big Data.

In this first installment we present a pre-Big Data checklist, a little off the
cuff theory of Big Data, and a design pattern you can apply to your environment
to solve Big Data problems.

##What is Big Data?

Big data is all about performance. Because of volume, concurrency or complexity,
performance of modern commercial software systems can easily start to slow down.
Fortunately these classes of Big Data problems are all solved using variations
of the same design pattern.

The first step to any potential Big Data problem should be applying classic
performance optimization techniques. Enhanced logging, detailed code review,
profiling and other runtime introspection techniques are generally recommended
before building out a Big Data system. Avoiding Big Data with these techniques
can save a lot, and makes an eventual Big Data system much more responsive and
reliable.

When traditional optimization techniques have failed, you more than likely have
a Big Data problem.

##Identify the Problem

The next step after application optimization is digging into the subsystems
behind an under-performing use case. Is it slow because of foreign-key
constraint checking on insert? Perhaps an update is sync'ing to an entire
cluster synchronously. We are looking for which aspect of the system is
performing slowly, after having optimized away performance issues in application
code.

It is important to identify which piece of the system and why it is behaving
slowly for two reasons. A better understanding may uncover an option specific to
your environment that can easily help. Removing unneeded foreign keys, or
turning on async updates, are two examples. It may even be discovered the system
does not need to perform better to meet it's SLAs. Again, it is recommended to
explore all other options before building a Big Data system.

Once the subsystem causing slowness has been identified and there are no obvious
solutions, it is time to break up the problem space.

##Why Big Data?

Before we go further, let's take a little departure and explore why we have Big
Data issues in the first place. Big Data systems as we know them today may be a
consequence of the "No Free Lunch" theorem from information theory. This
theorem explores the mathematical relationships between two arbitrary
algorithms, a large but finite problem space and the performance of the
algorithms when applied to the problem space.

The detailed mathematical treatment deals with machine-learning algorithm
development using techniques like simulated annealing. These techniques are
somewhat overkill for much of commercial software development today. That said,
the conclusion is generally applicable to systems dealing with large sets of
data:

> "... any elevated performance over one class of problems is offset by
performance over another class."

Essentially one algorithm, or series of programming steps, cannot effectively
handle searching through a lot of data in a general way.

Big Data exists because a system's data needs have grown beyond what the
system's current persistent strategy can effectively deliver.

##Break it Up

After optimizing application code and investigating the subsystems involved, it
is useful to pick one interaction of one subsystem causing the most slowness.
The goal is to break this one interaction up into several, limiting the scope of
each interaction's problem space. Let's look at how to separate the slow
interaction into pieces to speed things up.

Generally a subsystem acting as a data repository can be broken into four
distinct parts:

- Writing
- Long Term Storage
- Processing
- Reading

In a true Big Data system, each of these operations is treated as a separate
subsystem. If you're familiar with SOA concepts, picture each step as it's own
set of service interfaces, with a state machine carrying data through the
system.

Here's where the magic happens. Each step can be optimized separately from the
others. That problematic database insert? Doesn't need to be a database insert
any longer. It can be a JSON document in redundant memory, until an offline job
processes it into the database table.

Later, when the database table gets too big to support fast read? Change the
read model to be another document lookup. Processing becomes a transformation
between document models. Processing starting to take too long? Make it stateless
to scale.

##Big Data Design Pattern

If you are lucky enough to have a system with data needs continuing to grow,
eventually the system coalesces around four pieces:

- Input models
- Processing tier
- Long term storage
- Runtime models

Here is an obligatory diagram:

![BigDataDesignPattern](http://bigdatahowto.info/images/BigDataDesignPattern.png)

Yes, if you must, you can use Hadoop for the Long Term Storage and Processing
Tier implementation. In later installments, we will explore an implementation of
the above Big Data Design Pattern using the following:

- Scala Programming Language
- Play Runtime Environment
- SQS for Fault Tolerance and Error Recovery
- S3 for Long Term Storage
- ElastiCache for the Input & Read Models
- EC2 for the Deployment Environment
- NGINX for the Web Proxy

RESTful JSON services are used to interface with the Input & Read models, as
well as for internal communications. Eventually the goal is to make a demo
application available on the site, and have an EC2 AMI available to play around
with.

###Reference

1 - Wolpert, D.H., Macready, W.G. (1997), ["No Free Lunch Theorems for
Optimization"](http://ti.arc.nasa.gov/m/profile/dhw/papers/78.pdf), IEEE Transactions on Evolutionary Computation 1,
67.
