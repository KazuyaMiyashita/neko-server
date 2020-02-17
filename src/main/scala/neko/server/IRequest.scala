package neko.server

import java.io.InputStream
import java.io.OutputStream

trait IRequest {
  def in: InputStream
  def out: OutputStream
  def close(): Unit
}
