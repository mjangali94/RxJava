package io.reactivex.rxjava3.flowable;


public class MergedBenchmarks {
	   @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
	    public static class _Benchmark {

		   private io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark FlowableConcatTests_Benchmarks;
		   private io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark FlowableCollectTests_Benchmarks;

	        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
	        public void makePayloads() {
	            FlowableConcatTests_Benchmarks = new io.reactivex.rxjava3.flowable.FlowableConcatTests._Benchmark();
	            FlowableCollectTests_Benchmarks = new io.reactivex.rxjava3.flowable.FlowableCollectTest._Benchmark();

	        	this.FlowableConcatTests_Benchmarks.makePayloads();
	        	this.FlowableCollectTests_Benchmarks.makePayloads();
	        }
	        
	        @org.openjdk.jmh.annotations.Benchmark
	        public void benchmark_merged1() throws java.lang.Throwable {
	        	
	            this.FlowableConcatTests_Benchmarks.payloads.concatSimple.evaluate();
	            this.FlowableConcatTests_Benchmarks.payloads.concatCovariance.evaluate();
	            this.FlowableConcatTests_Benchmarks.payloads.concatCovariance2.evaluate();
	            this.FlowableCollectTests_Benchmarks.payloads.collectToListFlowable.evaluate();
	            this.FlowableCollectTests_Benchmarks.payloads.collectToList.evaluate();
	            this.FlowableCollectTests_Benchmarks.payloads.collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable.evaluate();
	            this.FlowableCollectTests_Benchmarks.payloads.dispose.evaluate();

	        }

	   }
	   
	   
	   }
