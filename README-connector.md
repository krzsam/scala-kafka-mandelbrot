## Running Kafka using Producer/Consumer API

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
2019-08-16 22:04:31 INFO  AppInfoParser - Kafka version: 2.3.0
2019-08-16 22:04:31 INFO  AppInfoParser - Kafka commitId: fc1aaa116b661c8a
2019-08-16 22:04:31 INFO  AppInfoParser - Kafka startTimeMs: 1565993071139
2019-08-16 22:04:31 INFO  Metadata - [Producer clientId=cli.Mandelbrot.Generator.DEBIAN-STRETCH-3] Cluster ID: tRHlgDthSxeIO2H8x0y4Hg
2019-08-16 22:04:57 INFO  RequestGenerator$ - Total number of messages sent 307202
2019-08-16 22:04:57 INFO  KafkaProducer - [Producer clientId=cli.Mandelbrot.Generator.DEBIAN-STRETCH-3] Closing the Kafka producer with timeoutMillis = 9223372036854775807 ms.
```

##### Calculation process

```
$ run-calculation-connector.sh
...
...
2019-08-16 22:03:07 INFO  AppInfoParser - Kafka version: 2.3.0
2019-08-16 22:03:07 INFO  AppInfoParser - Kafka commitId: fc1aaa116b661c8a
2019-08-16 22:03:07 INFO  AppInfoParser - Kafka startTimeMs: 1565992987903
2019-08-16 22:03:07 INFO  KafkaConsumer - [Consumer clientId=cli.Mandelbrot.Calculation.Connector.DEBIAN-STRETCH-2, groupId=Mandelbrot.Calculation.Connector] Subscribed to topic(s): connector-requests
...
... 
```

##### Image generation process

```
$ run-image-connector.sh
...
...
2019-08-16 22:02:14 INFO  AppInfoParser - Kafka version: 2.3.0
2019-08-16 22:02:14 INFO  AppInfoParser - Kafka commitId: fc1aaa116b661c8a
2019-08-16 22:02:14 INFO  AppInfoParser - Kafka startTimeMs: 1565992934717
2019-08-16 22:02:14 INFO  KafkaConsumer - [Consumer clientId=cli.Mandelbrot.Image.Connector.DEBIAN-STRETCH-1, groupId=Mandelbrot.Image.Connector] Subscribed to topic(s): connector-responses
...
...
2019-08-16 22:04:32 INFO  Calculate$ - Received BEGIN marker, creating image of size 640 x 480
2019-08-16 22:05:07 INFO  Calculate$ - Received END marker, writing image to file
2019-08-16 22:05:07 INFO  Calculate$ - Output file is: image-1565993107670.png
```

![Example Mandelbrot Set](img/image-1565993107670.png)