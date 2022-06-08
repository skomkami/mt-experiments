package pl.edu.agh.cars

import pl.edu.agh.cars.batcher.OrdersBatcher
import pl.edu.agh.cars.counter.OrdersCounter
import pl.edu.agh.cars.laoder.OrdersLoader
import pl.edu.agh.cars.persistence.OrderBatchesPersistencePipe
import pl.edu.agh.cars.processor.OrdersProcessor
import pl.edu.agh.config.Config
import pl.edu.agh.fs2.pipeline.Pipeline

class FS2OrdersPipe(config: Config)
    extends Pipeline(
      List(
        OrdersLoader("orders.csv"),
        OrdersProcessor(),
        OrdersBatcher(),
        OrderBatchesPersistencePipe(config.dbConfig),
        OrdersCounter()
      ),
      flowsConfig = config.flowsConfig
    )
