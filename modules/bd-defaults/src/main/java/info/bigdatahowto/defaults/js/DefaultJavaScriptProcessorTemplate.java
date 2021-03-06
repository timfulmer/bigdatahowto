package info.bigdatahowto.defaults.js;

/**
 * Default set of JavaScript glue code.
 *
 * @author timfulmer
 */
public class DefaultJavaScriptProcessorTemplate
        extends JavaScriptProcessorTemplate {

    private static final String JS_PROCESS_META_TEMPLATE =
            "function parseResults(result){\n" +
                    "  var persist;\n" +
                    "  if(result.persist) persist=result.persist.toString();\n" +
                    "  var meta= result.meta;\n" +
                    "  if(persist && meta){\n" +
                    "    processingResult.addMessage(result.key,result.meta,'Persist',persist);\n" +
                    "  }else if(persist){\n" +
                    "    processingResult.addMessage(result.key,'Persist',persist);\n" +
                    "  }else if(meta){\n" +
                    "    processingResult.addMessage(result.key,result.meta);\n" +
                    "  }else{\n" +
                    "    processingResult.addMessage(result.key);\n" +
                    "  }\n" +
                    "}\n" +
                    "var env= {};\n" +
                    "var meta= {};\n" +
                    "// inject meta\n" +
                    "%s\n" +
                    "// inject behavior function\n" +
                    "var results= %s(env,key,meta);\n" +
                    "if( !results){\n" +
                    "  processingResult.continueProcessing= false;\n" +
                    "}else if( toString.call(results) === '[object Array]'){\n" +
                    "  results.forEach(function(result){\n" +
                    "    parseResults(result);\n" +
                    "  });\n" +
                    "}";
    private static final String JS_PROCESS_TEMPLATE =
            "function parseResults(result){\n" +
                    "  var persist;\n" +
                    "  if(result.persist) persist=result.persist.toString();\n" +
                    "  var meta= result.meta;\n" +
                    "  if(persist && meta){\n" +
                    "    processingResult.addMessage(result.key,result.meta,'Persist',persist);\n" +
                    "  }else if(persist){\n" +
                    "    processingResult.addMessage(result.key,'Persist',persist);\n" +
                    "  }else if(meta){\n" +
                    "    processingResult.addMessage(result.key,result.meta);\n" +
                    "  }else{\n" +
                    "    processingResult.addMessage(result.key);\n" +
                    "  }\n" +
                    "}\n" +
                    "var env= {};\n" +
                    "var meta= {};\n" +
                    "// inject behavior function\n" +
                    "var results= %s(env,key,meta);\n" +
                    "if( !results){\n" +
                    "  processingResult.continueProcessing= false;\n" +
                    "}else if( toString.call(results) === '[object Array]'){\n" +
                    "  results.forEach(function(result){\n" +
                    "    parseResults(result);\n" +
                    "  });\n" +
                    "}";
    private static final String JS_ERROR_META_TEMPLATE=
            "function parseResults(result){\n" +
                    "  var persist;\n" +
                    "  if(result.persist) persist=result.persist.toString();\n" +
                    "  var meta= result.meta;\n" +
                    "  if(persist && meta){\n" +
                    "    processingResult.addMessage(result.key,result.meta,'Persist',persist);\n" +
                    "  }else if(persist){\n" +
                    "    processingResult.addMessage(result.key,'Persist',persist);\n" +
                    "  }else if(meta){\n" +
                    "    processingResult.addMessage(result.key,result.meta);\n" +
                    "  }else{\n" +
                    "    processingResult.addMessage(result.key);\n" +
                    "  }\n" +
                    "}\n" +
                    "var env= {};\n" +
                    "var meta= {};\n" +
                    "// inject meta\n" +
                    "%s\n" +
                    "// inject behavior function\n" +
                    "var results= %s(env,key,meta,tries);\n" +
                    "if( !results){\n" +
                    "  processingResult.continueProcessing= false;\n" +
                    "}else if( toString.call(results) === '[object Array]'){\n" +
                    "  results.forEach(function(result){\n" +
                    "    parseResults(result);\n" +
                    "  });\n" +
                    "}";
    private static final String JS_ERROR_TEMPLATE=
            "function parseResults(result){\n" +
                    "  var persist;\n" +
                    "  if(result.persist) persist=result.persist.toString();\n" +
                    "  var meta= result.meta;\n" +
                    "  if(persist && meta){\n" +
                    "    processingResult.addMessage(result.key,result.meta,'Persist',persist);\n" +
                    "  }else if(persist){\n" +
                    "    processingResult.addMessage(result.key,'Persist',persist);\n" +
                    "  }else if(meta){\n" +
                    "    processingResult.addMessage(result.key,result.meta);\n" +
                    "  }else{\n" +
                    "    processingResult.addMessage(result.key);\n" +
                    "  }\n" +
                    "}\n" +
                    "var env= {};\n" +
                    "var meta= {};\n" +
                    "// inject behavior function\n" +
                    "var results= %s(env,key,meta,tries);\n" +
                    "if( !results){\n" +
                    "  processingResult.continueProcessing= false;\n" +
                    "}else if( toString.call(results) === '[object Array]'){\n" +
                    "  results.forEach(function(result){\n" +
                    "    parseResults(result);\n" +
                    "  });\n" +
                    "}";

    @Override
    public String processWithMeta() {
        return JS_PROCESS_META_TEMPLATE;
    }

    @Override
    public String processWithoutMeta() {
        return JS_PROCESS_TEMPLATE;
    }

    @Override
    public String errorWithMeta() {
        return JS_ERROR_META_TEMPLATE;
    }

    @Override
    public String errorWithoutMeta() {
        return JS_ERROR_TEMPLATE;
    }
}
