package neko.chat.application.entity

import util.Random

case class Token(value: String)

object Token {

  private val ts: Array[Char] = (('A' to 'Z').toList :::
    ('a' to 'z').toList :::
    ('0' to '9').toList :::
    List('-', '.', '_', '~', '+', '/')).toArray

  def createToken(seed: Any): Token = {
    val rnd = new Random
    rnd.setSeed(System.currentTimeMillis() + seed.##)

    val tsLen  = ts.length
    val length = 64

    Token(List.fill(length)(ts(rnd.nextInt(tsLen))).mkString)
  }

}
