package shine.st.dashboard.utils

/**
  * Created by shinest on 04/07/2017.
  */
object RandomUtils {

  def betweenRandomDouble(lower: Double, upper: Double): Double = {
    val r = scala.util.Random
    if (upper < lower)
      0
    else {
      r.nextDouble * 100 match {
        case x if x >= lower && x <= upper => x
        case _ => betweenRandomDouble(lower, upper)
      }
    }
  }

  def betweenRandom(lower: Int, upper: Int): Int = {
    val r = scala.util.Random
    if (upper < lower)
      0
    else
      r.nextInt(upper) + 1 match {
        case x if x >= lower => x
        case _ => betweenRandom(lower, upper)
      }
  }

  def randomInt(upper: Int) = {
    val r = scala.util.Random
    if (upper <= 0)
      1
    else
      r.nextInt(upper) + 1
  }

  def randomBoolean(chance: Double) = {
    val r = scala.util.Random
    r.nextDouble <= chance
  }

  def randomIntSeq(range: Int)(upper: Int): String = {
    Seq.fill(range)(randomInt(upper)).mkString
  }

}
