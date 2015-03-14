# Project Information #

### Start up ###
src/ie/deri/hcls/tcga/start/QueryEvaluation.java

### Details ###

src/specification folder contains the N3 specfication file (used for load distribution) and TSS-to-Tumour (for Tumour lookup using patient barcode)

### Running in FedX Mode ###

//-optimization config--(src.com.fluidops.fedx.optimizer.Optimizer.java)

// FedX Source Selection: all nodes are annotated with their source


SourceSelection sourceSelection = new SourceSelection(members, cache, queryInfo);


sourceSelection.doSourceSelection(info.getStatements());

//----Configuration changes --(com.fluidsops.fedx.Config.java)-------

> //instance.loadTCGASpefication();

> //instance.createTSStoTumourHashTable();

> //instance.loadSource\_PredicateSets();

### Running in TopFed Mode ###

//-optimization config--(src.com.fluidops.fedx.optimizer.Optimizer.java)

// TCGA Source Selection: all nodes are annotated with their source


TCGASourceSelection sourceSelection = new TCGASourceSelection(members, cache, queryInfo);


try {


sourceSelection.doTCGASourceSelection(info.getStatements());


> } catch (RepositoryException e) {


> // TODO Auto-generated catch block
> e.printStackTrace();

> }

//----Configuration changes --(com.fluidsops.fedx.Config.java)-------


> instance.loadTCGASpefication();

> instance.createTSStoTumourHashTable();

> instance.loadSource\_PredicateSets();
