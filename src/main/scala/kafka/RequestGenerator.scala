package kafka

import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.commons.math3.complex.Complex
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}

object RequestGenerator {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val ZERO = new Complex( 0, 0 )

  // for JSON serialisation
  implicit val formats = DefaultFormats

  var messagesSent = 0

  def run(kafkaUri: String, topic: Topics.Value, topLeft: Complex, bottomRight: Complex, sizeX: Int, sizeY: Int, iterations: Int ): Unit = {
    val props = KafkaUtil.kafkaProps( kafkaUri, "Mandelbrot.Generator")
    val producer = new KafkaProducer[String, String]( props )

    sendOne( producer, Message( None, Some( Marker( true, sizeX, sizeY ) ) ), "BEGIN", topic )
    generateDataPoints( topLeft,  bottomRight, sizeX, sizeY, iterations, producer, topic )
    sendOne( producer, Message( None, Some( Marker( false, 0, 0 ) ) ), "END", topic )

    LOG.info( s"Total number of messages sent ${messagesSent}")

    producer.flush()
    producer.close()
  }

  private def generateDataPoints(topLeft: Complex, bottomRight: Complex, stepsX: Int, stepsY: Int, iterations: Int,
                                     producer: KafkaProducer[String, String], topic: Topics.Value ): Unit = {
    val distRe = bottomRight.getReal() - topLeft.getReal()
    val stepRe = distRe / stepsX

    val distIm = topLeft.getImaginary() - bottomRight.getImaginary()
    val stepIm = distIm / stepsY

    val rangeY = 0 until stepsY
    rangeY.foreach((itemY) => {
      val rangeX = 0 until stepsX
      rangeX.foreach(
        (itemX) => {
          val z0 = topLeft.add( new Complex(stepRe * itemX, -stepIm * itemY) )
          val data = DataPoint(itemX, itemY, z0, iterations )
          sendOne( producer, Message( Some( data ), None ), "[" + itemX + "," + itemY + "]", topic )
        }
      )
    })
  }

  private def sendOne(producer: KafkaProducer[String, String], message: Message, key: String, topic: Topics.Value ): Unit = {
    val jsonStr = Serialization.write( message )
    // the topics were created with only 1 partition, so no need to specify partition number below
    val record = new ProducerRecord[String, String]( topic.topicName, key, jsonStr )
    producer.send( record )
    messagesSent += 1
  }
}
