package info.bigdatahowto.defaults.js;

import info.bigdatahowto.core.Message;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * Interface defining templates used by JavaScriptProcessor to interface with
 * JavaScript behavior functions.
 *
 * @author timfulmer
 */
public abstract class JavaScriptProcessorTemplate {

    public abstract String processWithMeta();
    public abstract String processWithoutMeta();
    public abstract String errorWithMeta();
    public abstract String errorWithoutMeta();

    public String composeProcessScript(Message message, String behavior) {

        return composeScript(message, behavior, this.processWithMeta(),
                this.processWithoutMeta());
    }

    public String composeErrorScript(Message message, String behavior) {

        return this.composeScript( message, behavior, this.errorWithMeta(),
                this.errorWithoutMeta());
    }

    private String composeScript(Message message, String behavior,
                                 String withMeta, String withoutMeta) {

        String script;
        if( !isEmpty( message.getValues())){

            StringBuilder meta= new StringBuilder();
            for( Object key: message.getValues().keySet()){

                String prefix= "meta";
                Object value= message.getValues().get( key);
                meta.append(makeJavaScript(value, key, prefix));
            }
            script= String.format(withMeta, meta.toString(),
                    behavior);
        }else{

            script= String.format(withoutMeta, behavior);
        }

        return script;
    }

    private String makeJavaScript(Object value, Object key, String prefix) {

        String name= key.toString();
        if( !isEmpty( prefix)){

            name = String.format("%s.%s", prefix, key.toString());
        }
        StringBuilder meta= new StringBuilder();
        meta.append( name);
        meta.append("=");
        if( Map.class.isAssignableFrom(value.getClass())){

            meta.append( "{};\n");
            Map map= (Map)value;
            for( Object k: map.keySet()){

                meta.append( name);
                meta.append( '.');
                meta.append( k.toString());
                meta.append( '=');
                meta.append( this.valueString(map.get(k)));
                meta.append( ";\n");
            }
        }else if( List.class.isAssignableFrom( value.getClass())
                || Set.class.isAssignableFrom( value.getClass())){

            meta.append( "[];\n");
            Iterator iterator= ((Collection)value).iterator();
            for( int i= 0; iterator.hasNext(); i++){

                Object item= iterator.next();
                String k= key+ Integer.toString(i);
                meta.append( this.makeJavaScript( item, k, null));
                meta.append( name);
                meta.append( ".push(");
                meta.append( k);
                meta.append( ");\n");
            }
        }else{

            meta.append( this.valueString(value));
            meta.append( ";\n");
        }

        return meta.toString();
    }

    private String valueString( Object value){

        // TODO: TestMe
        if( value== null) return "undefined";

        if( String.class.isAssignableFrom( value.getClass())){

            String valueString= value.toString();
            if( !valueString.startsWith("{") && !valueString.startsWith("[")){

                return String.format("'%s'", valueString);
            }
        }

        return value.toString();
    }
}
