package pl.edu.agh.fs2.pipeline.utils

import fs2.{Pull, Stream}

object GroupUntil {

  implicit class GroupUntilOps[F[_], In](private val stream: Stream[F, In]) {
    def groupUntil[Out](
      init: Out
    )(until: (Out, In) => Boolean)(fn: (Out, In) => Out): Stream[F, Out] = {
      def go(acc: Out, s: Stream[F, In]): Pull[F, Out, Unit] =
        s.pull.uncons1.flatMap {
          case Some((hd, tl)) =>
            if (until(acc, hd) || acc == init) {
              val newAcc = fn(acc, hd)
              go(newAcc, tl)
            } else Pull.output1(acc) >> go(fn(init, hd), tl)
          case None => Pull.output1(acc)
        }

      go(init, stream).stream
    }
  }

}
