package shine.st.dashboard.utils.memo

import javax.inject.{Inject, Singleton}

import shine.st.dashboard.data.CategoryDao

/**
  * Created by shinest on 06/07/2017.
  */
@Singleton
class CategoryMemo @Inject()(categoryDao: CategoryDao) {
  val memo = Memoize.memoize(getCategoryById _)

  private def getCategoryById(id: Int) = {
    categoryDao.query("select * from Category where id = ?")(ps => ps.setInt(1, id)).head
  }

  def get(id: Int) = {
    memo(id)
  }
}
