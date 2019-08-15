## Prerequisites

#### Install Kafka

Download and unpack Kafka as described on [Kafka Quickstart](https://kafka.apache.org/quickstart#quickstart_download)

```
export KAFKA_HOME=/opt/kafka_2.12-2.3.0
export PATH=$PATH:$KAFKA_HOME/bin
```

##### Starting Zookeeper

For the purpose of this exercise only one instance of Zookeper was started, on node1

```
$ zookeeper-server-start.sh -daemon $KAFKA_HOME/config/zookeeper.properties

$ zookeeper-shell.sh localhost:2181
Connecting to localhost:2181
Welcome to ZooKeeper!
JLine support is disabled

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
```

##### Stopping Zookeeper

```
$ zookeeper-server-stop.sh
```

##### Starting Kafka broker

```
vi $KAFKA_HOME/config/server.properties
broker.id=1  # on node1
broker.id=2  # on node2
broker.id=3  # on node3

$ kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties

$ kafka-topics.sh --bootstrap-server localhost:9092 --version
2.3.0 (Commit:fc1aaa116b661c8a)
```

##### Stopping Kafka broker

```
$ kafka-server-stop.sh
```

##### Creating Kafka topics

```
$ kafka-topics.sh --bootstrap-server localhost:9092 --create --topic connector-requests  --partitions 1 --replication-factor 1
$ kafka-topics.sh --bootstrap-server localhost:9092 --create --topic connector-responses --partitions 1 --replication-factor 1
$ kafka-topics.sh --bootstrap-server localhost:9092 --create --topic stream-requests     --partitions 1 --replication-factor 1
$ kafka-topics.sh --bootstrap-server localhost:9092 --create --topic stream-responses    --partitions 1 --replication-factor 1

$ kafka-topics.sh --bootstrap-server localhost:9092 --describe
Topic:stream-responses  PartitionCount:1        ReplicationFactor:1     Configs:segment.bytes=1073741824
        Topic: stream-responses Partition: 0    Leader: 1       Replicas: 1     Isr: 1
Topic:connector-requests        PartitionCount:1        ReplicationFactor:1     Configs:segment.bytes=1073741824
        Topic: connector-requests       Partition: 0    Leader: 1       Replicas: 1     Isr: 1
Topic:stream-requests   PartitionCount:1        ReplicationFactor:1     Configs:segment.bytes=1073741824
        Topic: stream-requests  Partition: 0    Leader: 1       Replicas: 1     Isr: 1
Topic:connector-responses       PartitionCount:1        ReplicationFactor:1     Configs:segment.bytes=1073741824
        Topic: connector-responses      Partition: 0    Leader: 1       Replicas: 1     Isr: 1
```

  