package neko.core.http

import org.scalatest._

class HttpRequestLineSpec extends FunSuite with Matchers {

  test("getPath (1)") {
    val url     = "/over/there?name=ferret#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getPath shouldEqual "/over/there"
  }

  test("getPath (2)") {
    val url     = "/over/there"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getPath shouldEqual "/over/there"
  }

  test("getQueries (1)") {
    val url     = "/over/there?name=ferret#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map("name" -> List("ferret"))
  }

  test("getQueries (2)") {
    val url     = "/over/there?name=ferret&foo=bar#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map("name" -> List("ferret"), "foo" -> List("bar"))
  }

  test("getQueries (3)") {
    val url     = "/over/there?name=ferret&foo[]=bar&foo[]=baz#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map("name" -> List("ferret"), "foo[]" -> List("bar", "baz"))
  }

  test("getQueries (4)") {
    val url     = "/over/there?name#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map()
  }

  test("getQueries (5)") {
    val url     = "/over/there?name=#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map()
  }

  test("getQueries (6)") {
    val url     = "/over/there?=&#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map()
  }

  test("getQueries (7)") {
    val url     = "/over/there?=hoge#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map()
  }

  test("getQueries (8)") {
    val url     = "/over/there?&#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map()
  }

  test("getQueries (9)") {
    val url     = "/over/there?#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map()
  }

  test("getQueries (10)") {
    val url     = "/over/there#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map()
  }

  test("getQueries (11)") {
    val url     = "/"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getQueries shouldEqual Map()
  }

  test("getFlagment (1)") {
    val url     = "/over/there?name=ferret#nose"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getFlagment shouldEqual Some("nose")
  }

  test("getFlagment (2)") {
    val url     = "/over/there?name=ferret"
    val request = HttpRequestLine(GET, url, "HTTP/1.1")
    request.getFlagment shouldEqual None
  }

}
