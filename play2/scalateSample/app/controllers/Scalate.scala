package controllers

import play.mvc.Http
import play.api._
import http.{Writeable, ContentTypeOf, ContentTypes}
import mvc.Codec
import play.api.Play.current
import org.fusesource.scalate.layout.DefaultLayoutStrategy

object Scalate {

  import org.fusesource.scalate._
  import org.fusesource.scalate.util._

  var format = Play.configuration.getString("scalate.format") match {
    case Some(configuredFormat) => configuredFormat
    case _ => "jade" // todo : enhance this, hard coded for now.
  }

  lazy val scalateEngine = {
    val engine = new TemplateEngine
    engine.resourceLoader = new FileResourceLoader(Some(Play.getFile("/app/views")))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, Play.getFile("/app/views/layouts/default." + format).getAbsolutePath)
    engine.classpath = Play.getFile("/tmp/classes").getAbsolutePath
    engine.workingDirectory = Play.getFile("tmp")
    engine.combinedClassPath = true
    engine.classLoader = Play.classloader
    engine
  }

  def apply(template: String) = Template(template)

  case class Template(name: String) {

    def render(args: Map[String, Any]) = {
      import play.api.Play._
      val argsWithPlayObjects = args ++ Map("routes" -> routes)
      ScalateContent{
        scalateEngine.layout(name, argsWithPlayObjects)
      }
    }

  }

  case class ScalateContent(val cont: String)

  implicit def writeableOf_ScalateContent(implicit codec: Codec): Writeable[ScalateContent] = {
    Writeable[ScalateContent](scalate => codec.encode(scalate.cont))
  }

  implicit def contentTypeOf_ScalateContent(implicit codec: Codec): ContentTypeOf[ScalateContent] = {
    ContentTypeOf[ScalateContent](Some(ContentTypes.HTML))
  }

}
