Query Evaluation File (start) = TopFed\src\ie\deri\hcls\tcga\start\QueryEvaluation.java
TopFed\src\specification folder contains the N3 specfication file (used for load distribution) and TSS-to-Tumour (for Tumour lookup using patient barcode)

To switch between TopFed and FedX goto TopFed\src\com\fluidops\fedx\optimizer\Optimizer.java and comment/uncomment the follow lines of code
// FedX Source Selection: all nodes are annotated with their source
	//	SourceSelection sourceSelection = new SourceSelection(members, cache, queryInfo);
	//	sourceSelection.doSourceSelection(info.getStatements());
		
		// TCGA Source Selection: all nodes are annotated with their source
		TCGASourceSelection sourceSelection = new TCGASourceSelection(members, cache, queryInfo);
		try {
			sourceSelection.doTCGASourceSelection(info.getStatements());
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
Also the following methods at com.fluidops.fedx.Config.java are only for TopFed. You can comment while running in FedX mode
		instance.loadTCGASpefication();
		instance.createTSStoTumourHashTable();
		instance.loadSource_PredicateSets();