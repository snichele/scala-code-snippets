package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
  	import models._

    Ok( Scalate("index.jade").render('user -> User("Raible"))).as(HTML)
  }
  
}