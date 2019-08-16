package kafka

import java.net.InetAddress
import java.util.Properties

import org.apache.kafka.clients.consumer.RoundRobinAssignor
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.collection.mutable

object KafkaUtil {
  def kafkaProps( kafkaUri: String, groupId: String ): Properties = {
    val props = new Properties()

    for( (k,v) <- kafkaMap( kafkaUri, groupId ) ) {
      props.setProperty( k, v.toString )
    }

    props
  }

  def kafkaMap( kafkaUri: String, groupId: String ) : Map[String,AnyRef] = {
    val hostname = InetAddress.getLocalHost().getHostName().toUpperCase()
    val map = mutable.Map.empty[String,AnyRef]

    map += "bootstrap.servers" -> kafkaUri
    map += "acks" -> "all"
    map += "application.id" -> groupId
    map += "key.serializer" -> classOf[StringSerializer].getCanonicalName
    map += "value.serializer" ->classOf[StringSerializer].getCanonicalName
    map += "key.deserializer" -> classOf[StringDeserializer].getCanonicalName
    map += "value.deserializer" -> classOf[StringDeserializer].getCanonicalName
    map += "group.id" -> groupId
    map += "partition.assignment.strategy" -> classOf[RoundRobinAssignor].getCanonicalName
    map += "client.id" -> s"cli.${groupId}.${hostname}"

    map.toMap
  }

}
