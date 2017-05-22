# SparkJavaJobAnalyzer

This project is an example of the type of code analyzers that can be integrated in [Crystal](https://github.com/Crystal-SDS) for optimizing Big Data access to object storage via software-defined storage mechanisms. In particular, the objective of this project is to analyze the code of an input Spark job (Java) and to identify those operations performed on RDDs (and many of its subclasses) that can be delegated for execution to the storage side (thanks to a new [Storlet](https://github.com/Crystal-SDS/filter-samples/tree/master/Storlet_lambda_pushdown)). That is, operations like filter(), map() or reduce() are migrated and executed at the storage side for obtaining a faster application execution time.

**The development of this project continues at** https://github.com/Crystal-SDS.
