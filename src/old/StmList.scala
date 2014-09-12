package old

import scala.collection.mutable.ListBuffer

/**
 * Created by soywiz on 12/09/2014.
 */
case class StmList(nodes: ListBuffer[Stm] = new ListBuffer[Stm]()) extends Stm
