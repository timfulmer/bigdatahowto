package info.bigdatahowto.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.bigdatahowto.core.TestUtils.fakeMessage;
import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * @author timfulmer
 */
public class MessageTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testMessage(){

        MessageKey messageKey= new MessageKey(TestUtils.MESSAGE_KEY);
        Message message= new Message( messageKey);
        assert message.getMessageKey()!= null && messageKey.equals(
                message.getMessageKey()):
                "Message.key is not set correctly.";
        assert message.getValues()!= null:
                "Message.value is not set correctly.";
        assert message.getValues().isEmpty():
                "Message.value is not set correctly.";
        assert message.getBehavior()!= null:
                "Message.behavior is not set correctly.";
        assert message.getBehavior().isEmpty():
                "Message.hasBehavior is not implemented correctly.";
        assert message.getOptions()!= null:
                "Message.options is not set correctly.";
        assert message.getOptions().isEmpty():
                "Message.options is not set correctly.";

        // tf - Test mutation while we're here.
        messageKey= new MessageKey();
        message.setMessageKey(messageKey);
        Map values= new HashMap( 1);
        values.put( "k", "v");
        message.setValues( values);
        Map<BehaviorType,Behavior> behaviorMap= new HashMap<>( 1);
        Behavior behavior= new Behavior(BehaviorType.Persist, "test-value");
        behaviorMap.put(behavior.getBehaviorType(), behavior);
        message.setBehavior( behaviorMap);
        Map<String,String> options= new HashMap<>( 1);
        options.put( "name", "value");
        message.setOptions( options);
        assert message.getMessageKey()!= null && messageKey.equals(
                message.getMessageKey()):
                "Message.key is not set correctly.";
        assert message.getValues()!= null:
                "Message.value is not set correctly.";
        assert values== message.getValues():
                "Message.value is not set correctly.";
        assert behaviorMap== message.getBehavior():
                "Message.behavior is not set correctly.";
        assert options== message.getOptions():
                "Message.options is not set correctly.";
    }

    @Test
    public void testResourceKey(){

        Message message= fakeMessage();
        assert String.format( "messages/%s",
                TestUtils.MESSAGE_USER_KEY).equals(message.resourceKey()):
                "Message.resourceKey is implemented incorrectly.";
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMergeValues(){

        Message message= new Message();
        Map map= new HashMap( 1);
        map.put( "k", "v");
        message.getValues().put( "key", map);

        Map values= new HashMap( 1);
        Map value= new HashMap( 1);
        value.put( "a", "b");
        values.put( "key", value);

        message.mergeValues( values);
        assert message.getValues()!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get("key")!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get( "key") instanceof Map:
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) message.getValues().get( "key")).containsKey( "k"):
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) message.getValues().get( "key")).get( "k").equals( "v"):
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) message.getValues().get( "key")).containsKey( "a"):
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) message.getValues().get( "key")).get( "a").equals( "b"):
                "Message.mergeValues implemented incorrectly.";

        message.getValues().clear();
        values.clear();
        List l1= new ArrayList( 1);
        l1.add( map);
        message.getValues().put( "key", l1);
        List l2= new ArrayList( 1);
        l2.add( value);
        values.put( "key", l2);

        message.mergeValues( values);
        assert message.getValues()!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get("key")!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get( "key") instanceof List:
                "Message.mergeValues implemented incorrectly.";
        assert ((List) message.getValues().get( "key")).size()== 2:
                "Message.mergeValues implemented incorrectly.";
        assert ((List) message.getValues().get( "key")).get( 0) instanceof Map:
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) ((List) message.getValues().get( "key")).get(0)).containsKey( "k"):
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) ((List) message.getValues().get( "key")).get(0)).get( "k").equals( "v"):
                "Message.mergeValues implemented incorrectly.";
        assert ((List) message.getValues().get( "key")).get( 1) instanceof Map:
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) ((List) message.getValues().get( "key")).get(1)).containsKey( "a"):
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) ((List) message.getValues().get( "key")).get(1)).get( "a").equals( "b"):
                "Message.mergeValues implemented incorrectly.";

        // coverage cases
        message.getValues().clear();
        values.clear();
        message.mergeValues( values);
        assert message.getValues()!= null:
                "Message.mergeValues implemented incorrectly.";
        assert isEmpty(message.getValues()):
                "Message.mergeValues implemented incorrectly.";

        message.getValues().clear();
        values.clear();
        values.put( "k", "v");
        message.mergeValues( values);
        assert message.getValues()!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().containsKey( "k"):
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get( "k").equals( "v"):
                "Message.mergeValues implemented incorrectly.";

        message.getValues().clear();
        values.clear();
        message.getValues().put( "key", map);
        values.put( "key", l2);
        message.mergeValues( values);
        assert message.getValues()!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().containsKey( "key"):
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get( "key") instanceof List:
                "Message.mergeValues implemented incorrectly.";
        assert ((List) message.getValues().get( "key")).size()== 1:
                "Message.mergeValues implemented incorrectly.";
        assert ((List) message.getValues().get( "key")).get( 0)== value:
                "Message.mergeValues implemented incorrectly.";

        message.getValues().clear();
        values.clear();
        message.getValues().put( "key", l2);
        values.put( "key", map);
        message.mergeValues( values);
        assert message.getValues()!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().containsKey( "key"):
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get( "key") instanceof Map:
                "Message.mergeValues implemented incorrectly.";
        assert  ((Map) message.getValues().get( "key")).containsKey( "k"):
                "Message.mergeValues implemented incorrectly.";
        assert  ((Map) message.getValues().get( "key")).get( "k").equals( "v"):
                "Message.mergeValues implemented incorrectly.";
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMergeIncoming() {
        Message message= new Message();
        Map values= new HashMap( 1);
        Map value= new HashMap( 1);
        value.put( "k", "v");
        values.put( "key", value);
        message.mergeValues( values);
        assert message.getValues()!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get("key")!= null:
                "Message.mergeValues implemented incorrectly.";
        assert message.getValues().get( "key") instanceof Map:
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) message.getValues().get( "key")).containsKey( "k"):
                "Message.mergeValues implemented incorrectly.";
        assert ((Map) message.getValues().get( "key")).get( "k").equals( "v"):
                "Message.mergeValues implemented incorrectly.";
    }
}
/*
function(env,key,meta){
  // input validation.
  if(!key) return false;
  if( key.indexOf('interactions')!=0) return false;
  var activity= {};
  var pos=key.indexOf('/')+1;
  activity.name= key.substring(pos,key.indexOf('/',pos));
  key= key.substring(pos);
  var pos=key.indexOf('/')+1;
  activity.goal= key.substring(pos,key.indexOf('/',pos));
  if(!meta.activities) meta.activities=[];
  meta.activities.push(activity);
  key= key.substring(key.indexOf('/',pos));
  // decompose path and update suggestions
  var suggestions= [];
  pos=key.lastIndexOf('/');
  do{
    var suggestion= {};
    suggestion.latest=[];
    suggestion.latest.push(activity);
    key= key.substring(0,pos);
    suggestions.push({'key':'suggestions/'+key,'meta':suggestion});
  }while(pos> -1);
  // update activity
  suggestions.push({'key':'activities/'+ activity.name+ '/'+ activity.goal});
  // update activity master record
  var activityMR= {};
  activityMR.list= [];
  activityMR.list.push(activity);
  suggestions.push({'key':'activities','meta':activityMR});
  return suggestions;
}
 */