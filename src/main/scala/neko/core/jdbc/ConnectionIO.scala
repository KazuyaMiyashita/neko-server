package neko.core.jdbc

import java.sql.{Connection, SQLException}

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
      case e: SQLException => {
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
      case e: SQLException => {
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
      case e: SQLException => {
        Left(e)
      }
    } finally {
      conn.rollback()
      conn.close()
    }
  }
}
