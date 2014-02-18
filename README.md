#Big Data Framework

Starting out as a howto illustrating some points on building a modern, scalable
backend data architecture, BigDataHowto is now BigDataFramework.
BigDataFramework is happy to offer the following features:

 - Clear separation of backend scaling concerns from your business;

 - RESTful interface available to web, mobile and other IP-enabled clients;

 - Fault tolerant & self healing;

 - Scales to any number of concurrent users;

 - Scales to any amount of data;

 - Provides a simple way to do complex data calculations;

 - Simplified authentication scheme;

 - Pluggable architecture, currently implemented on AWS;

 - Integration with a Google AppEngine & other infrastructure, notifications,
 and more features coming soon!

Please see the demo below.

##Demo

Here's a demo app, running on the BigDataFramework.  Type in some letters
to see how many times that letter combination has been entered to the system.
Submit your words to the system, where they will be counted.  See the words
other users have entered to the system.

We'll walk through the steps to build the demo below, as well as walking through
the features provided by the BigDataFramework.

The source for this demo can be found here:

<https://github.com/timfulmer/bigdatahowto/blob/master/htdocs/demo.html>

##Updates

Since we don't have notifications using WebSockets in place yet, we'll get
updates on the latest words submitted to the server by polling for them.  Data
is stored by the BigDataFramework using keys.  A key looks like
`//resource/context/key/name`, and consists of four parts:

 - A resource name identifying the backing store used to persist the data;
 - A user context identifying the user who owns the data;
 - A string representing a unique name for the data;
 - A string representing the name of the property within the data to access.

There is a test deployment at `http://bigdatahowto.info/bd/`, configured with no
authentication and using s3 as the backing store.  Since there is no
authentication, there are also no safe guards against random users making
catastrophic changes to your data.  It also makes it much easier to demo :)

The resource name part of our key is provided for us as part of the
BigDataFramework configuration.  We do need to chose a context to keep our data
separate from others' data.  We'll call this one `wordoink` for fun.  Since this
key holds a summary of the latest submission to the system,
`/wordoink/latest/summary` seems like a good fit.

We'll also need a div to show the results.  We're building this demo using
JQuery to keep things simple.

```
<div id='bd-demo'></div>
...
<script>
    $.get( 'http://bigdatahowto.info/bd/data/wordoink/latest/summary',
        function(data){
            if(!data) updateDiv('No data available');
            else updateDiv(data.word+ ' - '+ data.count);
        },
        'json'
    );
    var latest;
    function updateDiv(data){
        if( latest!= data){
            $("#bd-demo").html(data);
            latest= data;
        }
    }
</script>
```

This gives us a simple 'No data available' message.  Let's introduce some
polling so we can see changes as they are happening:

```
(function poll() {
    setTimeout(function () {
        $.ajax({
            type: 'GET',
            dataType: 'json',
            url: 'http://bigdatahowto.info/bd/data/wordoink/latest/summary',
            success: function(data){
                 if(!data) updateDiv('No data available');
                 else updateDiv(data.word+ ' - '+ data.count);
             },
            complete: poll
        });
    }, 5000);
})();
```

If you're setting up a new system, you'll see the default message.  Otherwise
you'll see the last message submitted to the system.

##Submit Word

Here's where the magic of the BigDataFramework comes in.  As we POST a new word
to the system, we get to define behavior to run on the server at the same time.
Since simply counting words is a bit simplistic, we'll count all word stems as
well.  And we need to update the latest record to get some output.  Here's a
little JavaScript snippet to get this done on the server:

```
function(env,word,meta){
    // Input validation.
    if(!word || word.length>7) return false;
    // Define stem behavior.
    env.persistFunction= function(env,word,meta){
        // Update count returned from the GET request.
        if(!meta.count) meta.count= 0;
        meta.count++;
        // Operation successful, persist results.
        return true;
    }
    // Set meta data for this word.
    if(!meta.count) meta.count= 0;
    meta.count++;
    // Decompose word into stems, run persistFunction on each one.
    var stems= [];
    for(var i=1; i<word.length;i++){
        stems.push({key:word.substring(0,i),
            persist:env.persistFunction});
    }
    // update latest record.
    var latest= {};
    latest.summary= {};
    latest.summary.word= word;
    latest.summary.count=meta.count;
    stems.push({key:"latest",meta:latest});
    return stems;
}
```

You'll notice there can be two return types from a JavaScript function
associated with a key on the system.  A boolean tells the system to persist the
results of the function or not.  If a list of additional tuples is
given, they are fed back through the system as additional messages.

Backend behavior is now defined, let's setup the front end.  It's a simple text
box for right now, with a submit handler:

```
<input id="word" type="text" placeholder="Please type some letters." size="25"/>
<button onclick="sendWord()">Doink!</button>
...
function sendWord(){
    $.ajax({
            url: 'http://bigdatahowto.info/bd/data/wordoink/'+ $('#word').val(),
            type: 'POST',
            contentType: 'text/plain',
            processData: false,
            data: 'function(env,word,meta){\n \
                // Input validation.\n \
                if(!word || word.length>7) return false;\n \
                // Define stem behavior.\n \
                env.persistFunction= function(env,word,meta){\n \
                    // Update count returned from the GET request.\n \
                    if(!meta.count) meta.count= 0;\n \
                    meta.count++;\n \
                    // Operation successful, persist results.\n \
                    return true;\n \
                }\n \
                // Set meta data for this word.\n \
                if(!meta.count) meta.count= 0;\n \
                meta.count++;\n \
                // Decompose word into stems, run persistFunction on each one.\n \
                var stems= [];\n \
                for(var i=1; i<word.length;i++){\n \
                    stems.push({key:word.substring(0,i),\n \
                        persist:env.persistFunction});\n \
                }\n \
                // update latest record.\n \
                var latest= {};\n \
                latest.summary= {};\n \
                latest.summary.word= word;\n \
                latest.summary.count=meta.count;\n \
                stems.push({key:"latest",meta:latest});\n \
                return stems;\n \
            }'
        });
    $('#word').val(undefined);
}
```

Please note the use of backslashes and new line characters to continue the
string literal definition across multiple lines.

After POST'ing to the server for execution on the backend, the server responds
with a job identifier, which can be used to query job processing status with a
GET request to `http://bigdatahowto.info/bd/job/<identifier>`.  For this demo we
will skip job monitoring.

We clear the text field for a bit of immediate user feedback, and after a slight
delay for the server to process, the newly added word is displayed.

##Latest

Showing just one word at a time doesn't give a feel for the messages moving
through the system.  Let's put in a few more slots:

```
<div>
    <span id="bd-latest2">...</span><br/>
    <span id="bd-latest1">...</span><br/>
    <span id="bd-latest0">...</span><br/>
</div>
...
var latest= [];
function updateDiv(data){
    if(latest.indexOf(data)== -1){
        latest.splice(0,0,data);
        if( latest.length>3) latest.pop();
        latest.forEach(function(ld,i){
            $('#bd-latest'+ i).html(ld);
        })
    }
}
```

And a keypress event for the textfield:

```
function chkSendWord(event){
    if(event.charCode== 13){
       sendWord();
    }
}
```

Now go ahead and bang on the keyboard a bit, hitting enter every now and then.
You should start to see the messages updating occasionally as messages are
processed through the system, if they're not already.

##Autocomplete

Not really autocomplete, but it would be be nice if the current count for a
word displayed as it was typed.  Let's import jquery-ui CSS and JS files, and
add a quick autocomplete method:

```
<link href="css/jquery-ui-1.10.4.custom.min.css" rel="stylesheet">
...
<script src="js/jquery-ui-1.10.4.custom.min.js"></script>
...
$('#bd-word').autocomplete({
    source: function( req, res){
        $.ajax({
            type: 'GET',
            dataType: 'json',
            url: 'http://bigdatahowto.info/bd/data/wordoink/'+
                    req.term+ '/count',
            success: function(data){
                var tuple= [];
                tuple.push( req.term+ ' ('+ data+ ')');
                res(tuple);
            }
        });
    },
    select: function(event,ui){
        return false;
    }
});
```

The only gotcha here was returning false from the select function so we don't
end up posting invalid words like 'testing (12)'.

##Documentation

The remaining documentation follows the old howto format, walking through the
steps taken to build the initial version of the framework.  This will be moving
towards architecture, design and detailed documentation at class and package
levels soon.