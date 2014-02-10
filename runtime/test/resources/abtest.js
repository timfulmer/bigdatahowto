function(env,word,meta){
    // Input validation.
    if(!word || word.length>7) return false;
    // Define stem behavior.
    env.persistFunction= function(env,word,meta){
        // Update count returned from the GET request.
        if(!meta.count) meta.count= 0;
        meta.count++;
        return true;
    }
    // Set meta data for this word.
    if(!meta.count) meta.count= 0;
    meta.count++;
    // Decompose into stems, run persistFunction on each one.
    var stems= [];
    for(var i=1; i<word.length;i++){
        stems.push({key:word.substring(0,i),
            persist:env.persistFunction});
    }
    return stems;
}