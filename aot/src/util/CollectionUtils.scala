package util

object CollectionUtils {
  def uniqueMap[A,B](s: Seq[(A,B)]) = {
    val h = new collection.mutable.HashMap[A,B]
    val okay = s.iterator.forall(x => {
      val y = h.put(x._1, x._2)
      y.isEmpty || y.get == x._2
    })
    if (okay) Some(h) else None
  }
}
