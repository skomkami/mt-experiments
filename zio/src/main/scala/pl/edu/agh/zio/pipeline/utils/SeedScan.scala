package pl.edu.agh.zio.pipeline.utils

import zio.stream.ZStream

object SeedScan {
  implicit class SeedScanOps[R, E, In](private val stream: ZStream[R, E, In]) {
//    def seedScan[R1 <: R, E1 >: E, S](
//      seed: In => S
//    )(fn: (S, In) => S): ZStream[R1, E1, S] =
//      ZStream[R1, E1, S] {
//        for {
//          state <- Ref.makeManaged[Option[S]](None)
//          pull <- stream.process.mapM(BufferedPull.make(_))
//        } yield
//          pull.pullElement.flatMap { curr =>
//            state.get.flatMap {
//              case Some(s) =>
//                f(s, curr)
//                  .tap(o => state.set(Some(o)))
//                  .map(Chunk.single)
//                  .asSomeError
//              case None => state.set(Some(seed(curr))).as(Chunk.single(curr))
//            }
//          }
//      }

    def seedScan[R1 <: R, E1 >: E, S](
      seed: In => S
    )(fn: (S, In) => S): ZStream[R1, E1, S] =
      stream
        .scan(Option.empty[S]) {
          case (Some(state), in) => Some(fn(state, in))
          case (_, in)           => Some(seed(in))
        }
        .collect {
          case Some(state) => state
        }
  }
}
