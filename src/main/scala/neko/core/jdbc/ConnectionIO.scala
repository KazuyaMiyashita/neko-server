package neko.core.jdbc

import java.sql.Connection

trait ConnectionIO[T] {
  self =>

  final def map[U](f: T => U): ConnectionIO[U] = new ConnectionIO[U] {
    override protected val execute: Connection => U = {
      self.execute.andThen(f)
    }
  }
  final def flatMap[U](f: T => ConnectionIO[U]): ConnectionIO[U] = new ConnectionIO[U] {
    override protected val execute: Connection => U = {
      // self.execute.andThen(f)
      ???
    }
  }
  protected val execute: Connection => T

  final def run(pool: DBPool): T = {
    val conn = pool.getConnection()
    execute(conn)
  }

}

object ConnectionIO {

  def apply[T](f: Connection => T): ConnectionIO[T] = new ConnectionIO[T] {
    override val execute: Connection => T = f
  }

  def run[T](io: ConnectionIO[T])(pool: DBPool): T = {
    val conn = pool.getConnection()
    io.execute(conn)
  }

}

object sandbox {

  val pool: DBPool = ???
  val io: ConnectionIO[Int] = for {
    n1 <- ConnectionIO { _ => 42 }
    n2 <- ConnectionIO { _ => 42 }
  } yield n1 + n2
  val result: Int = io.run(pool)


}
