## Prerequisites

#### Kafka installation

Kafka was downloaded and installed as described on [Kafka Quickstart](https://kafka.apache.org/quickstart#quickstart_download)

Environment variables were set system wide to point to the installation directory, as below:
```
$ sudo vi /etc/profile.d/kafka.sh
KAFKA_HOME=/opt/kafka_2.12-2.3.0
PATH=$PATH:$KAFKA_HOME/bin
```

##### Starting Zookeeper

For the purpose of this exercise only one instance of Zookeper was started, on *debian-stretch-1*

Zookeper configuration in *$KAFKA_HOME/config/zookeeper.properties* was adjusted on *debian-stretch-1* to make the topic configuration persistent between reboots
of the instance
```
$ sudo vi $KAFKA_HOME/config/zookeeper.properties

dataDir=/opt/kafka_2.12-2.3.0/data-zookeeper
```

```
$ zookeeper-server-start.sh -daemon $KAFKA_HOME/config/zookeeper.properties

$ zookeeper-shell.sh debian-stretch-1:2181
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

Kafka configuration in *$KAFKA_HOME/config/server.properties* was adjusted on each of the nodes as below, changing the below properties:
* *broker.id*
    * 1 on *debian-stretch-1*
    * 2 on *debian-stretch-2*
    * 3 on *debian-stretch-3*
* *zookeeper.connect*=*debian-stretch-1:2181* on each node
* *log.dirs*=*/opt/kafka_2.12-2.3.0/data-kafka-logs* on each node


Kafka server was started on each of the nodes as below:
```
$ kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties

$ kafka-topics.sh --bootstrap-server debian-stretch-1:9092 --version
2.3.0 (Commit:fc1aaa116b661c8a)
```

##### Stopping Kafka broker

```
$ kafka-server-stop.sh
```

##### Creating Kafka topics

```
$ kafka-topics.sh --bootstrap-server debian-stretch-1:9092 --create --topic connector-requests  --partitions 1 --replication-factor 3
$ kafka-topics.sh --bootstrap-server debian-stretch-1:9092 --create --topic connector-responses --partitions 1 --replication-factor 3
$ kafka-topics.sh --bootstrap-server debian-stretch-1:9092 --create --topic stream-requests     --partitions 1 --replication-factor 3
$ kafka-topics.sh --bootstrap-server debian-stretch-1:9092 --create --topic stream-responses    --partitions 1 --replication-factor 3

$ kafka-topics.sh --bootstrap-server debian-stretch-1:9092 --describe
Topic:stream-responses  PartitionCount:1        ReplicationFactor:3     Configs:segment.bytes=1073741824
        Topic: stream-responses Partition: 0    Leader: 3       Replicas: 3,2,1 Isr: 3,2,1
Topic:connector-requests        PartitionCount:1        ReplicationFactor:3     Configs:segment.bytes=1073741824
        Topic: connector-requests       Partition: 0    Leader: 3       Replicas: 3,1,2 Isr: 3,1,2
Topic:stream-requests   PartitionCount:1        ReplicationFactor:3     Configs:segment.bytes=1073741824
        Topic: stream-requests  Partition: 0    Leader: 1       Replicas: 1,2,3 Isr: 1,2,3
Topic:connector-responses       PartitionCount:1        ReplicationFactor:3     Configs:segment.bytes=1073741824
        Topic: connector-responses      Partition: 0    Leader: 1       Replicas: 1,2,3 Isr: 1,2,3
```

  