package shine.st.dashboard.data

import java.sql.{Connection, PreparedStatement, ResultSet}

import play.api.Configuration
import shine.st.common.db.SQLUtils
import shine.st.common.db.SQLUtils.close

/**
  * Created by shinest on 04/07/2017.
  */
trait DemoDao[A] {
  SQLUtils.driver("org.mariadb.jdbc.Driver")

  //  val config = current.configuration

  val config: Configuration

  val url = config.get[String]("url")

  def insertBatch(list: List[A]) = {
    SQLUtils.insertBatch(url)(insertSql)(list)(setData)
  }

  def query(sql: String)(ps: PreparedStatement => Unit) = {
    var conn: Connection = null
    var stmt: PreparedStatement = null
    var rs: ResultSet = null

    try {
      conn = SQLUtils.connect(url)
      stmt = SQLUtils.statement(sql, conn)
      ps(stmt)
      rs = SQLUtils.query(stmt)

      val result = scala.collection.mutable.ArrayBuffer.empty[A]

      while (rs.next()) {
        result += generate(rs)
      }
      result.toList
    }

    finally {
      close(rs)
      close(stmt)
      close(conn)
    }

  }

  def insertSql: String

  def setData(data: A, ps: PreparedStatement): Unit

  def generate(rs: ResultSet): A

}

