package neko.chat.repository

import neko.chat.entity.User
import neko.chat.auth.Token
import neko.core.jdbc.ConnectionIO

trait AuthRepository {

  def authenticate(token: Token): ConnectionIO[Option[User]]

  def login(loginName: String, rawPassword: String): ConnectionIO[Option[Token]]

  def logout(token: Token): ConnectionIO[Unit]

}

object AuthRepository {

  class LoginNameNotExistOrWrongPassword extends Exception

  def generateHashedPassword(rawPassword: String, salt: Any): String = {
    import javax.crypto.{SecretKey, SecretKeyFactory}
    import javax.crypto.spec.PBEKeySpec

    val keySpec = new PBEKeySpec(
      rawPassword.toCharArray,
      salt.toString.getBytes,
      /* iterationCount = */ 10000,
      /* keyLength = */ 256 /* bytes == 64 moji */
    )
    val skf                          = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val secretKey: SecretKey         = skf.generateSecret(keySpec)
    val passwordByteArr: Array[Byte] = secretKey.getEncoded
    passwordByteArr.map(_.toChar).mkString
  }

}
