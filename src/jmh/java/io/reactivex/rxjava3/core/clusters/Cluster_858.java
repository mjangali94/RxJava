package io.reactivex.rxjava3.core.clusters;

public class Cluster_858 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.completable.CompletableFromActionTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.observable.ObservableBufferTest._Benchmark _Benchmark_benchmark_1;
       private io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriberTest._Benchmark _Benchmark_benchmark_2;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithStartEndFlowableTest._Benchmark _Benchmark_benchmark_4;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.completable.CompletableFromActionTest._Benchmark();
            _Benchmark_benchmark_1 = new io.reactivex.rxjava3.internal.operators.observable.ObservableBufferTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriberTest._Benchmark();
            _Benchmark_benchmark_4 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowWithStartEndFlowableTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_1.makePayloads();
            this._Benchmark_benchmark_4.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_858() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.disposeWhileRunningError.evaluate();
            this._Benchmark_benchmark_1.payloads.timedSkipInternalState.evaluate();
            this._Benchmark_benchmark_2.payloads.unorderedFastPathNoRequest.evaluate();
            this._Benchmark_benchmark_1.payloads.timedSizeBufferAlreadyCleared.evaluate();
            this._Benchmark_benchmark_4.payloads.windowAbandonmentCancelsUpstream.evaluate();
        }

   }

}