package controllers

import play.api.mvc.{ Action, Controller }
import com.google.inject.{ Inject, Singleton }
import models.blogs._
import play.api.data._
import models.users._
import models.blogs._
import play.api.mvc.Result
import models.users.QRole
import org.dupontmanual.forms.fields.TextField
import org.dupontmanual.forms.{ Form, ValidBinding, InvalidBinding, Binding }

import models.users.Visit
import controllers.users.{ Authenticated, VisitAction }
import config.Config
import config.users.UsesDataStore

//TODO: Actually check permissions where applicable

@Singleton
class Blogs @Inject()(implicit config: Config) extends Controller with UsesDataStore {
  
  def editor() = TODO

  /** No matching route (Not sure where, or even if, this is used)
   * 
   * Display the blogs corresponding to a user's role.
   */
  def listUserBlogs(roleOpt: Option[Role]) = VisitAction { implicit req =>
    roleOpt match {
      case None => NotFound("That role doesn't exist.")
      case Some(role) => dataStore.execute { implicit pm =>
        val cand = QBlog.candidate
        val blogs: List[Blog] = Blog.listUserBlogs(role)
        Ok(views.html.blogs.blogs(blogs, role))
      }
    }
  }

  /** Regex: /blog/me 
   * 
   *  Presents a page that lists the blogs that correspond to the current user.
   */
  def listCurrentUserBlogs() = Authenticated { implicit req =>
    Ok(views.html.blogs.blogs(Blog.listUserBlogs(req.role), req.role))
  }

  /** Regex: /blog/user/:id 
   * 
   *  Presents a page with blogs corresponding to a persepective with given id
   */
  def listBlogsByRoleId(id: Long) = VisitAction { implicit req =>
    dataStore.execute { implicit pm =>
      Role.getById(id) match {
        case Some(role) => Ok(views.html.blogs.blogs(Blog.listUserBlogs(role), role))
        case None => NotFound("That role doesn't exist!")
      }
    }
  }

  object CreatePostForm extends Form {
    val title = new TextField("title")
    val content = new TextField("content") {
      override def widget = new org.dupontmanual.forms.widgets.Textarea(true)
    }

    def fields = List(title, content)
  }

  /** Regex: /blog/:id/new
   * 
   *  Create a new post in the blog with given id.
   *  If the blog id shown in the url corresponds to a blog, go for it. If not, error.
   */

  // TODO: fix this using Permissions
  /*
  def createPost(blogId: Long) = Authenticated { implicit req =>
    Blog.getById(blogId) match {
      case None => NotFound("This blog doesn't exist.")
      case Some(b) => req.role match {
          case Some(p) => {
            if (b.owner.id != p.id) {
              Redirect(routes.Blogs.showBlog(blogId)).flashing("message" -> "You don't have the proper permissions to create a post. Change roles?")
            } else {
              Ok(views.html.blogs.createPost(b.title, Binding(form)))
              } else {
                checkBindingCreatePostForm(Binding(form, req), b, form)(req)
              }
            }
          }
          case None => Redirect(routes.Users.login()).flashing("message" -> "You must log in to create a blog post.")
        }
      }
    }
  }
  */

  def checkBindingCreatePostForm(binding: Binding, b: Blog) = VisitAction { implicit req =>
    binding match {
      case ib: InvalidBinding => Ok(views.html.blogs.createPost(b.title, ib))
      case vb: ValidBinding => {
        b.createPost(vb.valueOf(CreatePostForm.title), vb.valueOf(CreatePostForm.content))
        Redirect(routes.Blogs.showBlog(b.id)).flashing("message" -> "New post created!")
      }
    }
  }

  /**    !!!!!!!!!!!!!!!! UNIMPLEMENTED !!!!!!!!!!!!!!!!!!!
   * Show the control panel for a given blog. Checks to see if the correct user is stored in the session var first.
   *
   *   @param blog the blog to show the control panel for
   */
  def showControlPanel(blog: Blog) = TODO

  /**    !!!!!!!!!!!!!!!! UNIMPLEMENTED !!!!!!!!!!!!!!!!!!!
   * Show a post. Check to see if the currently logged-in user is allowed to see the post
   *
   *   @param post the post to be shown
   */
  def showPost(post: Post) = TODO

  /** Regex: /blog/:id
   *  
   * Show a blog with given id
   */
  def showBlog(id: Long) = VisitAction { implicit req =>
    val blogOpt = Blog.getById(id)
    blogOpt match {
      case None => NotFound("This blog is not found.")
      case Some(blog) => Ok(views.html.blogs.blog(blog, Blog.getPosts(blog)))
    }
  }

  // Tester method?
  def testSubmit() = TODO
}

