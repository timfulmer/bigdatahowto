import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  val BEHAVIOR= "function(env,word,meta){\n        // Input validation.\n        if(!word || word.length>7) return false;\n        // Define stem behavior.\n        env.persistFunction= function(env,word,meta){\n            // Update count returned from the GET request.\n            if(!meta.count) meta.count= 0;\n            meta.count++;\n            return true;\n        }\n        // Set meta data for this word.\n        if(!meta.count) meta.count= 0;\n        meta.count++;\n        // Decompose into stems, run persistFunction on each one.\n        var stems= [];\n        for(var i=1; i<word.length;i++){\n            stems.push({key:word.substring(0,i),\n                    persist:env.persistFunction});\n        }\n        return stems;\n    }"

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "send 400 UnsupportedOperation on the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(BAD_REQUEST)
    }

    "send 200 with 'null' on initial GET data" in new WithApplication{
      val result= route(FakeRequest(GET,"/data/context/key/name?authentication=testing")).get
      status(result) must equalTo(OK)
      contentAsString(result) must equalTo("null")
    }

    "send 200 with 'null' on initial GET job" in new WithApplication{
      val result= route(FakeRequest(GET,"/job/0bf31961-ac1b-4146-8920-8675cef98afe?authentication=testing")).get
      status(result) must equalTo(OK)
      contentAsString(result) must equalTo("null")
    }

    "send 200 on initial GET poll job" in new WithApplication{
      val result= route(FakeRequest(GET,"/job/poll")).get
      status(result) must equalTo(OK)
      contentAsString(result) must equalTo("Job processing complete.")
    }

//    "register context owner" in new WithApplication{
//      var result= route(FakeRequest(PUT,"/user/context/contextOwner")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo("User registration complete.")
//    }

//    "create new message as context owner and process" in new WithApplication{
//      // Create new message as context owner.
//      val ownerMessageKey= "/data/context/"+ System.currentTimeMillis()
//      var result= route(FakeRequest(POST,ownerMessageKey+ "?authentication=contextOwner").withTextBody( BEHAVIOR)).get
//      status(result) must equalTo(OK)
//      val ownerJobUuid= contentAsString(result)
//      ownerJobUuid must not equalTo "null"
//      result= route(FakeRequest(GET,"/job/poll")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo( "Job processing complete.")
//
//      // Check job status as context owner.
//      result= route(FakeRequest(GET,"/job/"+ ownerJobUuid+ "?authentication=contextOwner")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must contain( ownerJobUuid)
//      contentAsString(result) must contain( "Queued")
//
//      // Attempt job status as another user.
//      result= route(FakeRequest(GET,"/job/"+ ownerJobUuid+ "?authentication=contextUser")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo("null")
//    }

//    "process message" in new WithApplication{
//
//      // Process message.
//      val result= route(FakeRequest(GET,"/job/poll")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo( "Job processing complete.")
//    }
//
//    "check updated job status" in new WithApplication{
//      // Check updated job status.
//      val result= route(FakeRequest(GET,"/job/"+ ownerJobUuid+ "?authentication=contextOwner")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must contain( ownerJobUuid)
//      contentAsString(result) must contain( "Complete")
//    }
//
//    "access processed message as context owner" in new WithApplication{
//      // Access processed message as context owner.
//      val result= route(FakeRequest(GET,ownerMessageKey+ "?authentication=contextOwner")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo( "1.0")
//    }
//
//    "access processed message as guest" in new WithApplication{
//      // Access processed message as guest.
//      val result= route(FakeRequest(GET,ownerMessageKey+ "?authentication=contextUser")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo( "1.0")
//    }
//
//    "create message as guest" in new WithApplication{
//      // Create message as guest.
//      userMessageKey= "/data/context/"+ System.currentTimeMillis()
//      val result= route(FakeRequest(POST,userMessageKey+ "?authentication=contextUser").withTextBody( BEHAVIOR)).get
//      status(result) must equalTo(OK)
//      userJobUuid= contentAsString(result)
//      userJobUuid must not equalTo "null"
//    }
//
//    "access job state as guest" in new WithApplication{
//      // Access job state as guest.
//      val result= route(FakeRequest(GET,"/job/"+ userJobUuid+ "?authentication=contextUser")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must contain( userJobUuid)
//      contentAsString(result) must contain( "Queued")
//    }
//
//    "attempt job status as context owner" in new WithApplication{
//      // Attempt job status as context owner.
//      val result= route(FakeRequest(GET,"/job/"+ userJobUuid+ "?authentication=contextOwner")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must contain( userJobUuid)
//      contentAsString(result) must contain( "Queued")
//    }
//
//    "process message" in new WithApplication{
//      // Process message.
//      val result= route(FakeRequest(GET,"/job/poll")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo( "Job processing complete.")
//    }
//
//    "check updated job status" in new WithApplication{
//      // Check updated job status.
//      val result= route(FakeRequest(GET,"/job/"+ userJobUuid+ "?authentication=contextUser")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must contain( userJobUuid)
//      contentAsString(result) must contain( "Complete")
//    }
//
//    "access processed message as context owner" in new WithApplication{
//      // Access processed message as context owner.
//      val result= route(FakeRequest(GET,userMessageKey+ "?authentication=contextOwner")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo( "1.0")
//    }
//
//    "access processed message as guest" in new WithApplication{
//      // Access processed message as guest.
//      val result= route(FakeRequest(GET,userMessageKey+ "?authentication=contextUser")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo( "1.0")
//    }
//
//    "register guest user" in new WithApplication{
//      // Register guest user.
//      val result= route(FakeRequest(PUT,"/user/contextUser")).get
//      status(result) must equalTo(OK)
//      contentAsString(result) must equalTo("User registration complete.")
//    }
//
//    "update message as registered user" in new WithApplication{
//      // Update message as registered user.
//      val result= route(FakeRequest(POST,userMessageKey+ "?authentication=contextUser").withTextBody( BEHAVIOR)).get
//      status(result) must equalTo(OK)
//      userJobUuid= contentAsString(result)
//      userJobUuid must not equalTo "null"
//    }
  }
}
