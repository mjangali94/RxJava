package io.reactivex.rxjava3.core.clusters;

public class Cluster_697 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.mixed.MaybeFlatMapPublisherTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsFlowableTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark _Benchmark_benchmark_4;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark _Benchmark_benchmark_5;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableFlowableTest._Benchmark _Benchmark_benchmark_6;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark _Benchmark_benchmark_7;
       private io.reactivex.rxjava3.internal.operators.mixed.CompletableAndThenPublisherTest._Benchmark _Benchmark_benchmark_11;
       private io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatArrayTest._Benchmark _Benchmark_benchmark_14;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.mixed.MaybeFlatMapPublisherTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.jdk8.MaybeFlattenStreamAsFlowableTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTimeoutPublisherTest._Benchmark();
            _Benchmark_benchmark_5 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeDelayOtherTest._Benchmark();
            _Benchmark_benchmark_6 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeFlatMapIterableFlowableTest._Benchmark();
            _Benchmark_benchmark_7 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeTakeUntilTest._Benchmark();
            _Benchmark_benchmark_11 = new io.reactivex.rxjava3.internal.operators.mixed.CompletableAndThenPublisherTest._Benchmark();
            _Benchmark_benchmark_14 = new io.reactivex.rxjava3.internal.operators.maybe.MaybeConcatArrayTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
            this._Benchmark_benchmark_5.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_7.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_6.makePayloads();
            this._Benchmark_benchmark_11.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_14.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_697() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.cancelMain.evaluate();
            this._Benchmark_benchmark_0.payloads.cancelOther.evaluate();
            this._Benchmark_benchmark_2.payloads.successEmpty.evaluate();
            this._Benchmark_benchmark_2.payloads.fusedEmpty.evaluate();
            this._Benchmark_benchmark_4.payloads.mainSuccess.evaluate();
            this._Benchmark_benchmark_5.payloads.otherPublisherNextSlipsThrough.evaluate();
            this._Benchmark_benchmark_6.payloads.iteratorCrash.evaluate();
            this._Benchmark_benchmark_7.payloads.normalPublisher.evaluate();
            this._Benchmark_benchmark_2.payloads.successJust.evaluate();
            this._Benchmark_benchmark_2.payloads.successMany.evaluate();
            this._Benchmark_benchmark_6.payloads.hasNextCrash.evaluate();
            this._Benchmark_benchmark_11.payloads.cancelMain.evaluate();
            this._Benchmark_benchmark_2.payloads.mapperCrash.evaluate();
            this._Benchmark_benchmark_2.payloads.manyBackpressured.evaluate();
            this._Benchmark_benchmark_14.payloads.mixed.evaluate();
        }

   }

}