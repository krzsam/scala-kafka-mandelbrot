package kafka

import org.apache.commons.math3.complex.Complex
import org.slf4j.{Logger, LoggerFactory}

object RequestGeneratorMain {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  type OptionMap = Map[ Symbol, Any ]

  def nextOption( map: OptionMap, params: List[String] ): OptionMap = {
    params match {
      case "-k" :: kafkaUri :: tail =>
        nextOption( map + ( 'kafka -> kafkaUri ), tail )
      case "-a" :: command :: tail =>
        nextOption( map + ( 'api -> command ), tail )
      case "-tl" :: topLeft :: tail =>
        val pair = topLeft.split( ",")
        val numPair = new Complex( pair(0).toDouble, pair(1).toDouble )
        nextOption( map + ( 'topLeft -> numPair ), tail )
      case "-br" :: bottomRight :: tail =>
        val pair = bottomRight.split( ",")
        val numPair = new Complex( pair(0).toDouble, pair(1).toDouble )
        nextOption( map + ( 'bottomRight -> numPair ), tail )
      case "-sx" :: steps_x :: tail =>
        nextOption( map + ( 'steps_x -> steps_x.toInt ), tail )
      case "-sy" :: steps_y :: tail =>
        nextOption( map + ( 'steps_y -> steps_y.toInt ), tail )
      case "-i" :: iterations :: tail =>
        nextOption( map + ( 'iterations -> iterations.toInt ), tail )
      case Nil =>
        map
      case _ =>
        map
    }
  }

  def main(args: Array[String]) {
    val options = nextOption( Map(), args.toList )

    LOG.info( s"Parsed parameters: ${options}")

    val kafkaUri =    options.getOrElse( 'kafka, "unknown" ).asInstanceOf[String]
    val api     =     options.getOrElse( 'api, "unknown" ).asInstanceOf[String]
    val topLeft =     options.getOrElse( 'topLeft, new Complex(0,0) ).asInstanceOf[Complex]
    val bottomRight = options.getOrElse( 'bottomRight, new Complex(0,0) ).asInstanceOf[Complex]
    val stepsX =      options.getOrElse( 'steps_x, 640 ).asInstanceOf[Int]
    val stepsY =      options.getOrElse( 'steps_y, 480 ).asInstanceOf[Int]
    val iterations =  options.getOrElse( 'iterations, 128 ).asInstanceOf[Int]

    LOG.info( s"Starting Producer for ${api} API topics")

    val topic = api match {
      case "connector" => Topics.CONNECTOR_REQUESTS
      case "stream"    => Topics.STREAM_REQUESTS
    }

    RequestGenerator.run( kafkaUri, topic, topLeft, bottomRight, stepsX, stepsY, iterations )
  }
}
