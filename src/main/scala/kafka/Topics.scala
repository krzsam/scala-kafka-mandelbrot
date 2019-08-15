package kafka

object Topics extends Enumeration {
  protected case class Val(val topicName: String) extends super.Val

  import scala.language.implicitConversions
  implicit def valueToTopicsVal(x: Value): Val = x.asInstanceOf[Val]

  val CONNECTOR_REQUESTS  = Val( "connector-requests" )
  val CONNECTOR_RESPONSES = Val( "connector-responses" )
  val STREAM_REQUESTS     = Val( "stream-requests" )
  val STREAM_RESPONSES    = Val( "stream-responses" )
}
