package ie.deri.hcls.tcga.start;

import java.util.Arrays;
import java.util.List;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.sail.SailRepository;

import com.fluidops.*;
import com.fluidops.fedx.Config;
import com.fluidops.fedx.FedX;
import com.fluidops.fedx.FedXFactory;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.cache.MemoryCache;
import com.fluidops.fedx.monitoring.MonitoringImpl;
import com.fluidops.fedx.monitoring.MonitoringUtil;
import com.fluidops.fedx.structures.Endpoint;

public class QueryEvaluation<repo> {
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	
public static void main(String[] args) throws Exception 
	{
		// TODO Auto-generated method stub
	Config.initialize();
	
	SailRepository repo = FedXFactory.initializeSparqlFederation(Arrays.asList(
			"http://10.196.2.214:8890/sparql",
			"http://10.196.2.123:8890/sparql",
			"http://10.196.2.130:8890/sparql",
			"http://10.196.2.149:8890/sparql",
			"http://10.196.2.103:8890/sparql",
			"http://10.196.2.217:8890/sparql",
			"http://10.196.2.213:8890/sparql",
			"http://10.196.2.192:8890/sparql",
			"http://10.196.2.173:8890/sparql",
			"http://10.196.2.174:8890/sparql"
			
			//"http://hcls.deri.org:8080/openrdf-sesame/repositories/deri-tcga",
			//"http://localhost:8890/sparql"
			//"http://192.168.1.33:8890/sparql"
			));
			//"http://localhost:8080/openrdf-sesame/repositories/tcga-pink1",
			//"http://localhost:8080/openrdf-sesame/repositories/tcga-pink2",
			//"http://localhost:8080/openrdf-sesame/repositories/tcga-pink3",
			//"http://localhost:8080/openrdf-sesame/repositories/tcga-green1",
			//"http://localhost:8080/openrdf-sesame/repositories/tcga-green2",
			//"http://localhost:8080/openrdf-sesame/repositories/tcga-green3",
			//"http://localhost:8080/openrdf-sesame/repositories/tcga-green4",
			//"http://localhost:8080/openrdf-sesame/repositories/tcga-green5"));
        long startTime = System.currentTimeMillis();
	/*String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
		+ "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>\n"
		+ "SELECT ?President ?Party WHERE {\n"
		+ "?President rdf:type dbpedia-owl:President .\n"
		+ "?President dbpedia-owl:party ?Party . }";*/
	String q = "PREFIX tcga: <http://tcga.deri.ie/schema/> \n"
               + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
               + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" 
            //   + " select  ?mean"
                +	"select   ?chromosom  ?betaValue ?position  \n"
                +			"where \n"
                +			"{ \n" 
          	//	+				"?s        tcga:bcr_patient_barcode  \"TCGA-AB-2823\". \n"
			//	+				"?s        tcga:result   ?recordNo. \n"
			//	+				"<http://tcga.deri.ie/TCGA-AB-2821-m20> tcga:chromosome   ?chromosom. \n"
			//	+				"<http://tcga.deri.ie/TCGA-18-3406-c266>  rdf:type TCGA:copy_number_result. \n" 
			//	+				"<http://tcga.deri.ie/TCGA-AB-2821-m20> tcga:start   ?start. \n"
			//	+				"<http://tcga.deri.ie/TCGA-AB-2821-m20> tcga:stop   ?stop. \n"
			//	+				"<http://tcga.deri.ie/TCGA-AB-2821-m20> tcga:reads_per_million_miRNA_mapped  ?rpmmm. \n"
				
			//	+				"?recordNo rdf:type tcga:copy_number_result. \n" 
            //    +				"?recordNo tcga:seq_mean  \"0.0839\". \n"
    			+				"?recordNo tcga:chromosome   ?chromosom. \n"
			//	+				"?recordNo tcga:start   ?start. \n"
			//	+				"?recordNo tcga:stop   ?stop. \n"
				+				"?recordNo tcga:position   ?position. \n"
    		   				  
				+				"?recordNo tcga:beta_value   ?betaValue. \n"
			//	   +                "} UNION {"
					  
			//		+				"?recordNo tcga:chromosome   ?chromosom. \n"
					
			//	   + 				"?recordNo tcga:RPKM ?RPKM. \n"
			//   +                "} UNION {"
			//	+				"?recordNo tcga:chromosome   ?chromosom. \n"
				
		    //  +				    "?recordNo tcga:seq_mean  ?mean. \n"
		//	    + 				"?recordNo tcga:scaled_estimate ?sEst. \n"
					
			//	+				"?recordNo tcga:protein_expression_value   ?pev \n"
		  	//	+	            "  filter (xsd:integer(?start)>554268 &&	xsd:integer(?start)<5994290)\n"
                +				" }" ;
	/*String q = "PREFIX tcga: <http://tcga.deri.ie/schema/> \n"
        + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" 
      //  + "select   ?chromosom ?mean \n"
         +	"select   ?chromosom ?start ?stop ?mean \n"
         +			"where \n"
         +			"{ \n" 
     //    +         		"?s rdf:type ?p"
         
			+				"<http://tcga.deri.ie/TCGA-18-3406-c266> tcga:chromosome   ?chromosom. \n"
			//+				"<http://tcga.deri.ie/TCGA-18-3406-c266>  rdf:type TCGA:copy_number_result. \n" 
			+				"<http://tcga.deri.ie/TCGA-18-3406-c266> tcga:start   ?start. \n"
			+				"<http://tcga.deri.ie/TCGA-18-3406-c266> tcga:stop   ?stop. \n"
			+				"<http://tcga.deri.ie/TCGA-18-3406-c266> tcga:rpmmm   ?mean. \n"
			
		//		+ 				" ?recordNo TCGA:protein_expression_value ?pev \n"
		//	+				"?recordNo TCGA:start   ?start. \n"
		//	+				"?recordNo TCGA:stop   ?stop. \n"
		//	+				"?recordNo TCGA:seq_mean   ?mean. \n"
		//	+	            " #filter (xsd:integer(?start)>554268 &&	xsd:integer(?start)<654290)\n"
         +				"}" ;*/
  // MonitoringUtil.printMonitoringInformation();

	System.out.println(q);
	TupleQuery query = repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, q);
	TupleQueryResult res = query.evaluate();
    long count = 0;
    long endTime = System.currentTimeMillis();
    System.out.println("Execution time(msec) : "+ (endTime-startTime));
  /*  FedX fed = FederationManager.getInstance().getFederation();
	List<Endpoint> members = fed.getMembers();
	for (Endpoint e : members) 
	{
		MonitoringImpl.MonitoringInformation.class.
	}*/
   // MonitoringUtil.printMonitoringInformation();
    while (res.hasNext()) {
		System.out.println(count +": "+ res.next());
    	res.next();
		count++;
	}
	  endTime = System.currentTimeMillis();
	   System.out.println("Execution time(sec) : "+ (endTime-startTime)/1000);
	   System.out.println("Total Number of Records: " + count );
       FederationManager.getInstance().shutDown();
	   System.out.println("Done.");
	   System.exit(0);
	

	}

}
