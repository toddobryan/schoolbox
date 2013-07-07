package util

import play.api.mvc.{ Action, AnyContent, BodyParser, BodyParsers, Request, Result, Results }
import javax.jdo.JDOHelper
import scalajdo.DataStore
import models.users.Visit
import play.api.mvc.WrappedRequest
import models.users.Role

case class VisitRequest[A](visit: Visit, private val request: Request[A])
  extends WrappedRequest(request)

// TODO: we need a cache system
object VisitAction {
  def apply(block: => Result): Action[AnyContent] = apply(_ => block)

  def apply(block: VisitRequest[AnyContent] => Result): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }

  def apply[A](p: BodyParser[A])(f: VisitRequest[A] => Result): Action[A] = {
    Action(p)( request => {
      val pm = DataStore.pm
      pm.beginTransaction()
      val visitRequest = VisitRequest(Visit.getFromRequest(request), request)
      val res = f(visitRequest)
      if (JDOHelper.isDeleted(visitRequest.visit)) {
        pm.commitTransaction()
        res.withNewSession
      } else {
        visitRequest.visit.expiration = System.currentTimeMillis + Visit.visitLength
        pm.makePersistent(visitRequest.visit)
        pm.commitTransactionAndClose()
        if (request.session.get(visitRequest.visit.uuid).isDefined) res
        else res.withSession("visit" -> visitRequest.visit.uuid)
      }
    })
  }
}

case class AuthenticatedRequest[A](
    role: Role, 
    visit: Visit, 
    private val request: Request[A]) extends WrappedRequest(request)

object Authenticated {
  def apply(block: => Result): Action[AnyContent] = apply(_ => block)
  
  def apply(block: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }
  
  def apply[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result): Action[A] = {
    VisitAction(p) { req =>
      req.visit.role match {
        case None => DataStore.execute { pm =>
          req.visit.redirectUrl = Call(Method(req.method), req.uri)
          pm.makePersistent(req.visit)
          Results.Redirect(controllers.routes.Users.login()).flashing("error" -> "You must log in to view that page.")
        }
        case Some(role) => f(AuthenticatedRequest(role, req.visit, req))
      }
    }
  }
}