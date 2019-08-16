# scala-kafka-mandelbrot

An example application to calculate a [Mandelbrot Set](https://en.wikipedia.org/wiki/Mandelbrot_set) using components sending messages through Kafka topics. 
The goal is demonstrate use of Kafka Connector and Stream APIs and show use of Kafka in a clustered configuration.
The solution is not intended to be optimal in terms of choice of technologies or performance. 

## Application structure

The calculation of Mandelbrot Set is divided between three separate processes:
* *Data generation* - responsible for generating input data points. Each data point generated (represented by class *DataPoint*) contains information about positions
    of the given data point within image coordinated, the data point itself (***c*** value in the iteration formula) and the number of iterations.
* *Calculation* - responsible for executing the actual formula iteration calculation for each data point received
* *Image generation* - responsible for collecting responses containing iteration results and then based on the responses, creating an image which is their
    graphical representation. The file is created in the current directory where the process was invoked.
    
The *Data generation* process sends additionally two additional special purpose messages which are named as markers in the code: one message to denote
the beginning of a logical set of messages with data points (sent before any data points messages), and the other to denote the end of this set of messages.
Both markers are passed through the *Calculation* process without change and are used by *Image generation* process to manage creation
of internal image buffer and writing the buffer as image to teh file.
    
There are 4 topics altogether, 2 of each are used as below depending which Kafka API is used by processes. Setting up topics is described
below in the Prerequisites section. Using different topics for different APIs is done purely for demonstration purposes and is not necessary
from technical point of view.
* Connector API
    * *connector-requests* : to send message from *Data generation* process to *Calculation* process
    * *connector-responses*  : to send message from *Calculation* process to *Image generation* process
* Streaming (KStreams) API
    * *stream-requests* : to send message from *Data generation* process to *Calculation* process
    * *stream-responses* : to send message from *Calculation* process to *Image generation* process

## [Prerequisites](README-pre.md)

## [Running Kafka using Producer/Consumer API](README-connector.md)

## [Running Kafka using Streaming API](README-streaming.md)

## Links
* [Apache Kafka 2.3](https://kafka.apache.org/)
* [sbt-assembly](https://github.com/sbt/sbt-assembly): 0.14.9
* Scala: 2.12.9

## Infrastructure
* Google Cloud Platform
    * 3 nodes : *n1-standard-1 (1 vCPU, 3.75 GB memory)* , OS: Debian Stretch 9.9
        * debian-stretch-1 : Kafka broker #1, Zookeeper
        * debian-stretch-2 : Kafka broker #2
        * debian-stretch-3 : Kafka broker #3