package controllers

import play.api.mvc._
import java.util.UUID
import info.bigdatahowto.api.Bd

object Application extends Controller {

  val bd:Bd = new Bd("target/file-resources")

  def index = Action {
    BadRequest("Operation not supported.")
  }

  def getData(key: String) = Action{
    val pivot:Int= key.lastIndexOf( '/')
    val userKey= key.substring( 0, pivot)
    val metaName= key.substring( pivot+ 1)
    Ok(okString(bd.queryMetaData(userKey,metaName,"test-authentication")))
  }

  def postData(key: String)= Action{request =>
    val body: Option[String] = request.body.asText
    body.map { text =>
      Ok(okString(bd.addMessage(key,text,"Persist",
        "test-authentication").toString))
    }.getOrElse {
      BadRequest("Expecting 'Content-Type:text/plain' request header.")
    }
  }

  def getJob(jobUuid: UUID) = Action{
    Ok(okString(bd.queryJob(jobUuid)))
  }

  def pollJob() = Action{
    var loopCount= 0
    for( loopCount <- 0 to 10){
      bd.processJob()
    }
    Ok("Job processing complete.")
  }

  def okString(result:Object):String = {
    var contents= "null"
    if(result!=null) contents= result.toString
    contents
  }
}