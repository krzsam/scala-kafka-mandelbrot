package kafka

import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.commons.math3.complex.Complex
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

object RequestGenerator {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  case class DataPoint( posX: Int, posY: Int, c: Complex, iterations: Int )
  case class BEGIN_MARKER( sizeX: Int, sizeY: Int )
  case class END_MARKER()

  val ZERO = new Complex( 0, 0 )

  // for JSON serialisation
  implicit val formats = DefaultFormats

  var messagesSent = 0

  def run( topic: Topics.Value, topLeft: Complex, bottomRight: Complex, stepsX: Int, stepsY: Int, iterations: Int, batchSize: Int ): Unit = {
    val props = KafkaUtil.kafkaProps( "Mandelbrot.Generator")
    val producer = new KafkaProducer[String, String]( props )

    sendMarker( producer, "BEGIN", BEGIN_MARKER( stepsX,stepsY ), topic )
    calculateManyInBatches( topLeft,  bottomRight, stepsX, stepsY, iterations, batchSize, producer, topic )
    sendMarker( producer, "END", END_MARKER(), topic )

    LOG.info( s"Total number of messages sent ${messagesSent}")

    producer.flush()
    producer.close()
  }

  private def calculateManyInBatches(topLeft: Complex, bottomRight: Complex, stepsX: Int, stepsY: Int, iterations: Int, batchSize: Int,
                                     producer: KafkaProducer[String, String], topic: Topics.Value ): Unit = {
    val batch = mutable.MutableList[DataPoint]()

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
          val item = DataPoint(itemX, itemY, z0, iterations )
          batch += item
          if (batch.size >= batchSize) {
            sendBatch( producer, batch, "[" + itemX + "," + itemY + "]", topic )
            batch.clear()
          }
        }
      )
    })
    //if (batch.size >= batchSize) {
    //  sendBatch( producer, batch, "LAST", topic, partitions )
    //  batch.clear()
    //}
  }

  private def sendBatch( producer: KafkaProducer[String, String], batch: mutable.MutableList[DataPoint], key: String, topic: Topics.Value ): Unit = {
    val jsonStr = Serialization.write( batch )
    // the topics were created with only 1 partition, so no need to specify partition number below
    val record = new ProducerRecord[String, String]( topic.topicName, key, jsonStr )
    producer.send( record )
    messagesSent += batch.size
  }

  private def sendMarker( producer: KafkaProducer[String, String], key: String, marker: Any, topic: Topics.Value ): Unit = {
    LOG.info( s"Sending marker ${key}" )

    val jsonStr = Serialization.write( List( marker ) )
    // the topics were created with only 1 partition, so no need to specify partition number below
    val record = new ProducerRecord[String, String]( topic.topicName, key, jsonStr )
    producer.send( record )
  }
}
