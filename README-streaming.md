## Running Kafka using Streaming API

#### Running processes

Each of the three processes was started on a different node, as below

* debian-stretch-3 : Data generation process
* debian-stretch-2 : Calculation process
* debian-stretch-1 : Image generation process

Each of the processes were connecting to its own local Kafka broker instance.

##### Data generation process

```
$ run-generator-connector.sh
...
...
2019-08-16 22:15:42 INFO  AppInfoParser - Kafka version: 2.3.0
2019-08-16 22:15:42 INFO  AppInfoParser - Kafka commitId: fc1aaa116b661c8a
2019-08-16 22:15:42 INFO  AppInfoParser - Kafka startTimeMs: 1565993742162
2019-08-16 22:15:42 INFO  Metadata - [Producer clientId=cli.Mandelbrot.Generator.DEBIAN-STRETCH-3] Cluster ID: tRHlgDthSxeIO2H8x0y4Hg
2019-08-16 22:15:58 INFO  RequestGenerator$ - Total number of messages sent 307202
2019-08-16 22:15:58 INFO  KafkaProducer - [Producer clientId=cli.Mandelbrot.Generator.DEBIAN-STRETCH-3] Closing the Kafka producer with timeoutMillis = 9223372036854775807 ms.
```

##### Calculation process

```
$ run-calculation-streaming.sh
...
...
2019-08-16 22:12:28 INFO  AppInfoParser - Kafka version: 2.3.0
2019-08-16 22:12:28 INFO  AppInfoParser - Kafka commitId: fc1aaa116b661c8a
2019-08-16 22:12:28 INFO  AppInfoParser - Kafka startTimeMs: 1565993548251
...
...
2019-08-16 22:12:28 INFO  KafkaConsumer - [Consumer clientId=cli.Mandelbrot.Calculation.Streaming.DEBIAN-STRETCH-2-StreamThread-1-consumer, groupId=Mandelbrot.Calculation.Streaming] Subscribed to pattern: 'stream-requests'
...
... 
2019-08-16 22:12:28 INFO  KafkaStreams - stream-client [cli.Mandelbrot.Calculation.Streaming.DEBIAN-STRETCH-2] State transition from REBALANCING to RUNNING
...
...
```

##### Image generation process

```
$ run-image-streaming.sh
...
...
2019-08-16 22:12:21 INFO  AppInfoParser - Kafka version: 2.3.0
2019-08-16 22:12:21 INFO  AppInfoParser - Kafka commitId: fc1aaa116b661c8a
2019-08-16 22:12:21 INFO  AppInfoParser - Kafka startTimeMs: 1565993541021
...
...
2019-08-16 22:12:21 INFO  KafkaStreams - stream-client [cli.Mandelbrot.Image.Streaming.DEBIAN-STRETCH-1] State transition from REBALANCING to RUNNING
...
...
2019-08-16 22:15:43 INFO  Calculate$ - Received BEGIN marker, creating image of size 640 x 480
2019-08-16 22:16:06 INFO  Calculate$ - Received END marker, writing image to file
2019-08-16 22:16:06 INFO  Calculate$ - Output file is: image-1565993766521.png
```

![Example Mandelbrot Set](img/image-1565993766521.png)