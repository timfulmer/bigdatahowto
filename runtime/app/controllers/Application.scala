package controllers

import play.api.mvc._
import java.util.UUID
import info.bigdatahowto.api.Bd
import info.bigdatahowto.defaults.BdAuthenticator

object Application extends Controller {

  val bd:Bd = new Bd("target/file-resources")
  bd.setAuthenticator( new BdAuthenticator( bd.getUserRoadie))

  def index = Action {
    BadRequest("Operation not supported.")
  }

  def getData(key: String, authentication:String) = Action{
    val pivot:Int= key.lastIndexOf( '/')
    val userKey= key.substring( 0, pivot)
    val metaName= key.substring( pivot+ 1)
    Ok(okString(bd.queryMetaData(userKey,metaName,authentication)))
  }

  def postData(key: String, authentication:String)= Action{request =>
    val body: Option[String] = request.body.asText
    body.map { text =>
      Ok(okString(bd.addMessage(key,text,"Persist",
        authentication).toString))
    }.getOrElse {
      BadRequest("Expecting 'Content-Type:text/plain' request header.")
    }
  }

  def getJob(jobUuid: UUID, authentication:String) = Action{
    Ok(okString(bd.queryJob(jobUuid, authentication)))
  }

  def pollJob() = Action{
    for( loopCount <- 0 to 10){
      bd.processJob()
    }
    Ok("Job processing complete.")
  }

  def register(authentication: String, userContext:String) = Action{
    bd.register( authentication, userContext)
    Ok( "User registration complete.")
  }

  def okString(result:Object):String = {
    var contents= "null"
    if(result!=null) contents= result.toString
    contents
  }
}