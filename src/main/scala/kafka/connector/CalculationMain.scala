package kafka.connector

import java.time.Duration

import kafka._
import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.commons.math3.complex.Complex
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.collection.JavaConverters

object CalculationMain {
  private val LOG: Logger = LoggerFactory.getLogger( this.getClass )

  val topicIn = Topics.CONNECTOR_REQUESTS
  val topicOut = Topics.CONNECTOR_RESPONSES

  val BigNumber = new Complex( Double.MaxValue, Double.MaxValue )

  // for JSON deserialisation
  implicit val formats = DefaultFormats

  type OptionMap = Map[ Symbol, Any ]

  def nextOption( map: OptionMap, params: List[String] ): OptionMap = {
    params match {
      case "-k" :: kafkaUri :: tail =>
        nextOption( map + ( 'kafka -> kafkaUri ), tail )
      case Nil =>
        map
      case _ =>
        map
    }
  }

  def main(args: Array[String]) {
    LOG.info( "Starting Calculation Service for CONNECTOR API topics")

    val options = nextOption( Map(), args.toList )

    LOG.info( s"Parsed parameters: ${options}")

    val kafkaUri =    options.getOrElse( 'kafka, "unknown" ).asInstanceOf[String]

    val props = KafkaUtil.kafkaProps( kafkaUri, "Mandelbrot.Calculation.Connector")
    val requestConsumer = new KafkaConsumer[String, String]( props )
    val resultProducer: KafkaProducer[String, String] = new KafkaProducer[String, String]( props )

    requestConsumer.subscribe( JavaConverters.asJavaCollection( List( topicIn.topicName )  ) )

    poller( requestConsumer, resultProducer )

    requestConsumer.close()
    resultProducer.close()
  }

  /*
  Poll for calculation requests sent from Producer
  */
  @tailrec private def poller(consumer: KafkaConsumer[String, String], producer: KafkaProducer[String, String] ) {
    val records: ConsumerRecords[String, String] = consumer.poll( Duration.ofMinutes( 5 ))
    //LOG.info( s"Received message with records ${records.count()}")
    readRecords( records, producer )
    poller( consumer, producer )
  }

  private def readRecords(records: ConsumerRecords[String, String], producer: KafkaProducer[String, String] ): Unit = {
    records.forEach{
      record =>
        val request = Serialization.read[Message]( record.value() )
        val result = processDataPoint( request )
        sendResult( producer, result, record.key() )
    }
  }

  private def processDataPoint( request: Message ): Message = {
    request.data match {
      case Some( DataPoint(posX, posY, c0, iterations) ) =>
        val result = Calculate.calculateOne( c0, iterations )
        val resultAdjusted = if (result.isInfinite || result.isNaN) BigNumber else result
        //LOG.info( s"Calculating: (${posX},${posY}) , ${c0} -> ${resultAdjusted}")
        Message( Some( DataPoint(posX, posY, resultAdjusted, 0) ), None )

      case None =>
        // if it is a marker it is passed thru
        request
    }
  }

  private def sendResult(resultProducer: KafkaProducer[String, String], result: Message, key: String ): Unit = {
    val jsonStr = Serialization.write(result)
    val record = new ProducerRecord[String, String](topicOut.topicName, key, jsonStr)
    resultProducer.send(record)
  }
}
