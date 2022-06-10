package pl.edu.agh.cars

import akka.actor.ActorSystem
import pl.edu.agh.akka.pipeline.Pipeline
import pl.edu.agh.cars.batcher.OrdersBatcher
import pl.edu.agh.cars.counter.OrdersCounter
import pl.edu.agh.cars.loader.OrdersLoader
import pl.edu.agh.cars.persistence.OrderBatchesPersistencePipe
import pl.edu.agh.cars.processor.OrdersProcessor
import pl.edu.agh.config.Config

class OrdersPipeline(config: Config)(implicit as: ActorSystem)
    extends Pipeline(
      List(
        OrdersLoader(config.inputFilePath),
        OrdersProcessor(),
        OrdersBatcher(),
        OrderBatchesPersistencePipe(config.dbConfig),
        OrdersCounter()
      ),
      config.flowsConfig
    )
