package io.reactivex.rxjava3.core.clusters;

public class Cluster_63 {

   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

       private io.reactivex.rxjava3.internal.operators.flowable.FlowableObserveOnTest._Benchmark _Benchmark_benchmark_0;
       private io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishFunctionTest._Benchmark _Benchmark_benchmark_2;

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            _Benchmark_benchmark_0 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableObserveOnTest._Benchmark();
            _Benchmark_benchmark_2 = new io.reactivex.rxjava3.internal.operators.flowable.FlowablePublishFunctionTest._Benchmark();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_2.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
            this._Benchmark_benchmark_0.makePayloads();
        }
        
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Cluster_63() throws java.lang.Throwable {
            this._Benchmark_benchmark_0.payloads.workerNotDisposedPrematurelyNormalInNormalOut.evaluate();
            this._Benchmark_benchmark_0.payloads.moreWorkInRunAsync.evaluate();
            this._Benchmark_benchmark_2.payloads.oneByOne.evaluate();
            this._Benchmark_benchmark_0.payloads.syncFusedRequestOneByOneConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.workerNotDisposedPrematurelyNormalInNormalOutConditional.evaluate();
            this._Benchmark_benchmark_0.payloads.badSource.evaluate();
            this._Benchmark_benchmark_0.payloads.workerNotDisposedPrematurelyAsyncInNormalOut.evaluate();
            this._Benchmark_benchmark_0.payloads.backFusedMoreWork.evaluate();
        }

   }

}