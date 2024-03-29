package util

import scala.xml.Utility.trim
// test
import org.scalatest.{ FunSuite, Matchers }

import controllers.users.MenuItem

class TestMenu extends FunSuite with Matchers {
  test("correct html") {
    val menu1 = new MenuItem("Menu 1", "m1", Some("/page1"), Nil)
    val menu2 = new MenuItem("Menu 2", "m2", None, List(menu1))

    val html1 = <li>
                  <a href="/page1" id="m1">Menu 1</a>
                </li>

    val html2 = <li class="dropdown">
                  <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                    Menu 2
                    <b class="caret"></b>
                  </a>
                  <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
                    <li><a href="/page1" id="m1">Menu 1</a></li>
                  </ul>
                </li>
    trim(menu1.asHtml) shouldEqual trim(html1)
    trim(menu2.asHtml) shouldEqual trim(html2)
  }
}