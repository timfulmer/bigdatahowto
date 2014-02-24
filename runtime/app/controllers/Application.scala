package controllers

import play.api.mvc._
import java.util.UUID
import info.bigdatahowto.api.Bd
import play.api.libs.concurrent.Execution.Implicits._
import info.bigdatahowto.defaults.AlwaysAllowAuthenticator
import collection.JavaConversions._

object Application extends Controller {

  val bd:Bd = Bd.productionInstance()
  // setup for no auth.
  bd.setAuthenticator( new AlwaysAllowAuthenticator())
  val authentication= "noauth"

  def index = Action {
    BadRequest("Operation not supported.")
  }

  def postData(key: String)= Action{request =>
    val values= request.queryString.map { case (k,v) => k -> v.mkString }
    val body: Option[String] = request.body.asText
    body.map { text =>
      val jobUuid= UUID.randomUUID()
      scala.concurrent.Future { bd.addMessage(jobUuid,"//s3/"+ key,text,"Persist",mapAsJavaMap(values),authentication) }
      Ok(okString(jobUuid))
    }.getOrElse {
      BadRequest("Expecting 'Content-Type:text/plain' request header.")
    }
  }

  def getData(key: String) = Action{
    val pivot:Int= key.lastIndexOf( '/')
    val userKey= key.substring( 0, pivot)
    val metaName= key.substring( pivot+ 1)
    Ok(bd.queryMetaData("//s3/"+userKey,metaName,authentication))
  }

  def getJob(jobUuid: UUID) = Action{
    Ok(okString(bd.queryJob(jobUuid, authentication)))
  }

  def pollJob() = Action{
    scala.concurrent.Future { bd.processJob() }
    Ok("Job processing complete.")
  }

  def register(authentication: String, userContext:String) = Action{
    bd.register( authentication, userContext)
    Ok( "User registration complete.")
  }

  def okString(result:Object,defaultString:String= "null"):String = {
    if(result!= null) result.toString else defaultString
  }
}