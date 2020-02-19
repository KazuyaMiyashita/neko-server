package neko.core.jdbc

import java.sql.Connection

case class ConnectionIO[T](run: Connection => T) {
  def map[U](f: T => U): ConnectionIO[U]                   = ConnectionIO(run andThen f)
  def flatMap[U](f: T => ConnectionIO[U]): ConnectionIO[U] = ConnectionIO(c => f(run(c)).run(c))

  def runTx(conn: Connection): Either[Throwable, T] = {
    conn.setAutoCommit(false)
    try {
      val res = run(conn)
      conn.commit()
      Right(res)
    } catch {
      case e: Throwable => {
        conn.rollback()
        Left(e)
      }
    } finally {
      conn.close()
    }
  }

  def runReadOnly(conn: Connection): Either[Throwable, T] = {
    conn.setReadOnly(true)
    try {
      Right(run(conn))
    } catch {
      case e: Throwable => {
        Left(e)
      }
    } finally {
      conn.close()
    }
  }

  def runRollback(conn: Connection): Either[Throwable, T] = {
    conn.setAutoCommit(false)
    try {
      Right(run(conn))
    } catch {
      case e: Throwable => {
        Left(e)
      }
    } finally {
      conn.rollback()
      conn.close()
    }
  }
}
