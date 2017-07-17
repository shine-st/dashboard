package shine.st.dashboard.data

import java.sql.{PreparedStatement, ResultSet}
import javax.inject.{Inject, Singleton}

import play.api.Configuration
import shine.st.dashboard.data.Model.Category

/**
  * Created by shinest on 04/07/2017.
  */

@Singleton
class CategoryDao @Inject()(override val config: Configuration) extends DemoDao[Category] {
  override val insertSql: String = "insert into Category (name) values (?)"

  override def setData(data: Category, ps: PreparedStatement): Unit = {
    ps.setString(1, data.name)
  }

  override def generate(rs: ResultSet): Category = {
    Category(rs.getInt("id"), rs.getString("name"))
  }
}