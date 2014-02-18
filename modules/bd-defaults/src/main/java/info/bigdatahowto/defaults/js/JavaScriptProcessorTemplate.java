package info.bigdatahowto.defaults.js;

import info.bigdatahowto.core.Message;

import java.util.Map;

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

                String name= String.format("meta.%s", key.toString());

                meta.append( name);
                meta.append("=");
                Object value= message.getValues().get( key);
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
                }else{

                    meta.append( this.valueString(value));
                    meta.append( ";\n");
                }
            }
            script= String.format(withMeta, meta.toString(),
                    behavior);
        }else{

            script= String.format(withoutMeta, behavior);
        }

        return script;
    }

    private String valueString( Object value){

        if( String.class.isAssignableFrom( value.getClass())){

            return String.format( "'%s'", value.toString());
        }

        return value.toString();
    }
}
