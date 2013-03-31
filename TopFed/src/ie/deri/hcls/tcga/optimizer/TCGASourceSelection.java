
package ie.deri.hcls.tcga.optimizer;

import info.aduna.iteration.CloseableIteration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import com.fluidops.fedx.Config;
import com.fluidops.fedx.EndpointManager;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.algebra.EmptyStatementPattern;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.algebra.StatementSource.StatementSourceType;
import com.fluidops.fedx.algebra.StatementSourcePattern;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.cache.Cache.StatementSourceAssurance;
import com.fluidops.fedx.cache.CacheEntry;
import com.fluidops.fedx.cache.CacheUtils;
import com.fluidops.fedx.evaluation.TripleSource;
import com.fluidops.fedx.evaluation.concurrent.ControlledWorkerScheduler;
import com.fluidops.fedx.evaluation.concurrent.ParallelExecutor;
import com.fluidops.fedx.evaluation.concurrent.ParallelTask;
import com.fluidops.fedx.exception.ExceptionUtil;
import com.fluidops.fedx.exception.OptimizationException;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.structures.QueryInfo;
import com.fluidops.fedx.structures.SubQuery;
import com.fluidops.fedx.util.QueryStringUtil;

/**
 * TCGA source selection 
 * 
 * @author saleem
 *
 */
public class TCGASourceSelection {

	public static Logger log = Logger.getLogger(TCGASourceSelection.class);
	
	protected final List<Endpoint> endpoints;
	protected final Cache cache;
	protected final QueryInfo queryInfo;
	
	
	public TCGASourceSelection(List<Endpoint> endpoints, Cache cache, QueryInfo queryInfo) {
		this.endpoints = endpoints;
		this.cache = cache;
		this.queryInfo = queryInfo;
	}

	/**
	 * Map statements to their sources. Use synchronized access!
	 */
	public  Map<StatementPattern, List<StatementSource>> stmtToSources = new ConcurrentHashMap<StatementPattern, List<StatementSource>>();
	
	/**
	 * Sources, predicate Sets
	 */
	    protected String pJoinVar ;  // name of the variable which will be use as path join between tcga:result and other query triples patterns
	   protected String qryPatientBarCode; //global patient barcode if specified in the user query
	   @SuppressWarnings("rawtypes")
	   protected  HashMap starJoinGrps = new HashMap(); 
	 long startTime = System.currentTimeMillis();
	 /**
	 * Perform source selection using the predicate, source grouping specified at settings/GroupSetting.n3 and ASK queries.
	 * Remote ASK queries are only used if only object is bound in a triple pattern.
	 * 
	 * The statement patterns are replaced by appropriate annotations in this optimization.
	 * 
	 * @param stmts 
	 *               list of triple patterns
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 * @throws MalformedQueryException 
	 * @throws RDFParseException 
	 */
	public void doTCGASourceSelection(List<StatementPattern> stmts) throws RepositoryException, MalformedQueryException, QueryEvaluationException, RDFParseException {
		
	  List<CheckTaskPair> remoteCheckTasks = new ArrayList<CheckTaskPair>();
//	  long startTime = System.currentTimeMillis();
//	loadSource_PredicateSets();
//	 long endTime = System.currentTimeMillis();
	// System.out.println("load set Execution time(msec) : "+ (endTime-startTime));
	
	createStarJoinGroups(stmts);
	  String qryTumour = getQryTumour();
	// for each statement (triple pattern)of user query determine the relevant sources
		for (StatementPattern stmt : stmts) 
		 {
			stmtToSources.put(stmt, new ArrayList<StatementSource>());
			SubQuery q = new SubQuery(stmt);
			
		    ArrayList<String> c1Sources = null, c2Sources = null, c3Sources = null,sources=null, lstFinalSources=null;
			String type,s,p,o,finalSource;
			if (stmt.getSubjectVar().getValue()!=null)
			     s = stmt.getSubjectVar().getValue().stringValue();
			else
			     s ="null";  
			if (stmt.getPredicateVar().getValue()!=null)
			     p = stmt.getPredicateVar().getValue().stringValue();
			else
			     p ="null"; 
			if (stmt.getObjectVar().getValue()!=null)
			     o = stmt.getObjectVar().getValue().stringValue();
			else
			    o = "null"; 
			//---------------------------------------------
			if(!s.equals("null"))   //--- if subject is bound
			 {
				type = getResultType(s);
				String sbjTumour = getTumour(s);
				if(isSetMember(type,Config.lstC)==true )  //if type belong to set C
					sources= Config.lstD_blue;
				else if(type.equals("expression_exon_result"))
					sources =Config.lstD_pink;
				else if(type.equals("dna_methylation_result"))
					sources =Config.lstD_green;
				finalSource= sourcesFilter(sbjTumour,type);//-- filter sources capable of answering the tumour specified
			//	System.out.println(s+" "+p +" "+o+ ":\t 1");
			//	System.out.println(finalSource);
				String id = "sparql_" + finalSource.replace("http://", "").replace("/", "_");
				addSource(stmt, new StatementSource(id, StatementSourceType.REMOTE));
			 }
			 else if (!p.equals("null"))  //--- if predicate is bound
			 {
				 s= stmt.getSubjectVar().getName().toString();
				 if(C_1(s,p,o)==true)
					 c1Sources = Config.lstD_blue;
				 if(C_2(s,p,o)==true)
					 c2Sources = Config.lstD_pink;
				 if(C_3(s,p,o)==true)
					 c3Sources = Config.lstD_green;
				 sources = unionOf(c1Sources,c2Sources,c3Sources);
				 if(sources==null)
				 	 sources = Config.lstD_blue;
				 if(qryTumour!=null)
					 lstFinalSources = filterSources(qryTumour,sources);
				 else
					 lstFinalSources = sources;
			//	 System.out.println(s+" "+p +" "+o+":\t"+lstFinalSources.size());
			//	 System.out.println("\t"+lstFinalSources); 
				 bindSourcesToStatement(stmt,lstFinalSources);
			 }
			 else if (s.equals("null") && p.equals("null"))  //--- if predicate is bound
			 {
				// check for each current federation member (cache or remote ASK)
					for (Endpoint e : endpoints) {
						StatementSourceAssurance a = cache.canProvideStatements(q, e);
						if (a==StatementSourceAssurance.HAS_LOCAL_STATEMENTS) {
							addSource(stmt, new StatementSource(e.getId(), StatementSourceType.LOCAL));
						} else if (a==StatementSourceAssurance.HAS_REMOTE_STATEMENTS) {
							addSource(stmt, new StatementSource(e.getId(), StatementSourceType.REMOTE));			
						} else if (a==StatementSourceAssurance.POSSIBLY_HAS_STATEMENTS) {					
							remoteCheckTasks.add( new CheckTaskPair(e, stmt));
						}
					}
				// System.out.println("object is only bound"); 
			 }
					
		}
		 long endTime = System.currentTimeMillis();
		  
			System.out.println("Source Selection Execution time(msec) : "+ (endTime-startTime));
		
		
		// if remote checks are necessary, execute them using the concurrency
		// infrastructure and block until everything is resolved
		if (remoteCheckTasks.size()>0) {
			SourceSelectionExecutorWithLatch.run(this, remoteCheckTasks, cache);
		}
				
		for (StatementPattern stmt : stmtToSources.keySet()) {
			
			List<StatementSource> sources = stmtToSources.get(stmt);
			System.out.println("------------\n"+stmt);
			System.out.println(sources);
			// if more than one source -> StatementSourcePattern
			// exactly one source -> OwnedStatementSourcePattern
			// otherwise: No resource seems to provide results
			
			if (sources.size()>1) {
				StatementSourcePattern stmtNode = new StatementSourcePattern(stmt, queryInfo);
				for (StatementSource s : sources)
					stmtNode.addStatementSource(s);
				stmt.replaceWith(stmtNode);
			}
		
			else if (sources.size()==1) {
				stmt.replaceWith( new ExclusiveStatement(stmt, sources.get(0), queryInfo));
			}
			
			else {
				if (log.isDebugEnabled())
					log.debug("Statement " + QueryStringUtil.toString(stmt) + " does not produce any results at the provided sources, replacing node with EmptyStatementPattern." );
				stmt.replaceWith( new EmptyStatementPattern(stmt));
			}
		}		
	}
	/**
	 * Bind the finally selected sources to the corresponding statment
	 * @param stmt statement
	 * @param lstFinalSources sources to be bind
	 */
	public void bindSourcesToStatement(StatementPattern stmt,
			ArrayList<String> lstFinalSources) {
		
		for(String src:lstFinalSources)
		{
			String id = "sparql_" + src.replace("http://", "").replace("/", "_");
			addSource(stmt, new StatementSource(id, StatementSourceType.REMOTE));
		}
	}
	/**
	 * Filter the capable sources based on the query tumour specified by user in the query
	 * @param qryTumour  query tumour name
	 * @param sources list of sources which needs to be filtered
	 * @return list of filter sources
	 * @throws QueryEvaluationException 
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 */
	public ArrayList<String> filterSources(String qryTumour, ArrayList<String> sources) throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{
		// long startTime = System.currentTimeMillis();
		ArrayList<String> finalSrces = new ArrayList<String>();
		String queryString = getSourceFilterQuery(qryTumour,null);
         TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			   String endPoint = result.next().getValue("endpoint").stringValue();
			   if(sources.contains(endPoint))
					finalSrces.add(endPoint);
		   }
		
		
	/*	Query query = QueryFactory.create(qryStr);
		QueryExecution qexec = QueryExecutionFactory.create(query, m);
		ResultSet rs = qexec.execSelect();
		while(rs.hasNext())
		{
			QuerySolution result = rs.nextSolution();
			if(sources.contains(result.get("endpoint").toString()))
				finalSrces.add(result.get("endpoint").toString());
		 }*/
		// long endTime = System.currentTimeMillis();
		  
		//	System.out.println("Filter Execution time(msec) : "+ (endTime-startTime));
		 
		return finalSrces;
	
	}
	/**
	 * Get the Tumour name from the user query if possible
	 * @return tumour name
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getQryTumour() 
	{
		String tumourName =null;
		Set<String> keySet = starJoinGrps.keySet();
		for (Iterator it = keySet.iterator(); it.hasNext();) {
			String key=  (String) it.next();
			ArrayList<String> predGrp  = (ArrayList<String>) starJoinGrps.get(key);
			if(predGrp.contains("bcr_patient_barcode")&& predGrp.contains("result"))
			{
				String []subPrts =qryPatientBarCode.split("-");
				String tss = subPrts[1];
				tumourName= Config.tssTbl.get(tss);
			}
				
			}
		return tumourName;
	}
	/**
	 * Create star join groups from user query
	 * @param stmts list of triple patterns in query
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void createStarJoinGroups(List<StatementPattern> stmts) {
		for (StatementPattern stmt : stmts) 
		{
		 if (stmt.getSubjectVar().getValue()==null && stmt.getPredicateVar().getValue()!=null) //--- we are only interested if subject is unbound and predicate is bound
			{
			 String sbjKey = stmt.getSubjectVar().getName().toString();
			 String predValue = stmt.getPredicateVar().getValue().toString();
			     if(predValue.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			     {
			    	 if (stmt.getObjectVar().getValue()!=null)
			    	 {
			    		 String [] predPrts = stmt.getObjectVar().getValue().stringValue().split("/");
						 predValue =  predPrts[4]; 
			    	 }
					else
						predValue = "type"; 
			      }
			       else
			       {
				     String [] predPrts = predValue.split("/");
					 predValue= predPrts[4]; 
					 if(predValue.equals("result"))
						pJoinVar = stmt.getObjectVar().getName().toString();
					 if(predValue.equals("bcr_patient_barcode"))
							qryPatientBarCode = stmt.getObjectVar().getValue().toString();
			        }
			     	if (starJoinGrps.containsKey(sbjKey))  
			     		{  
			     			List lstPredValues = (List)starJoinGrps.get(sbjKey);  
			     			lstPredValues.add(predValue);  
			     			}  
		   	     	else  
			     		{  
			     		List lstPredValues = new ArrayList();  
			     		lstPredValues.add(predValue);  
			     		starJoinGrps.put(sbjKey, lstPredValues);  
			     		} 
			}
		}
	}
	//--------------
	/**
	 * make union of sources
	 * @param c1Sources
	 * @param c2Sources
	 * @param c3Sources
	 * @return union of the three sets
	 */
	public ArrayList<String> unionOf(ArrayList<String> c1Sources,
			ArrayList<String> c2Sources, ArrayList<String> c3Sources) 
			{
		ArrayList<String> lstSrces =new ArrayList<String>();
		if(c1Sources!=null)
		lstSrces.addAll(c1Sources);
		if(c2Sources!=null)
		lstSrces.addAll(c2Sources);
		if(c3Sources!=null)
		lstSrces.addAll(c3Sources);
			
		return lstSrces;
	}
	//--------
	/**
	 * C-1 Condition checker
	 * @param p predicate
	 * @param o object
	 * @param s subject
	 * @return true if C-1 is satisfied
	 */
	public boolean C_1(String s,String p, String o) 
	{
		Boolean rdfType =false;
		if(p.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
		{
			String [] objPrts = o.split("/");
			o= objPrts[4];
			rdfType = true;
		}
		else
		{
		String [] predPrts = p.split("/");
		p= predPrts[4];
		}
		Boolean value = false;
		if(((rdfType ==true && isSetMember(o,Config.lstC)==true)||(isSetMember(p,Config.lstADG)==true))&& ((isStarJoin(s,p,Config.lstD)==true || isTypeJoin(s,p,Config.lstC)==true|| isPathJoin(s,p,Config.lstD)==true) || (isStarJoin(s,p,Config.lstM)==false && isStarJoin(s,p,Config.lstE)==false && isTypeJoin(s,p,Config.lstB)==false && isTypeJoin(s,p,Config.lstF)==false && isPathJoin(s,p,Config.lstM)==false && isPathJoin(s,p,Config.lstE)==false)))
		value = true;
		return value;
	}
	/**
	 * Check for path join. But the predicate must have a start join with predicate result
	 * @param s
	 * @param p
	 * @param lstM
	 * @return Boolean
	 */
	private boolean isPathJoin(String s, String p, ArrayList<String> lstM) {
		Boolean value=false;
		
		if(pJoinVar != null)
		{
			@SuppressWarnings({ "unchecked" })
		List<String>lstSJGrp= (List<String>) starJoinGrps.get(pJoinVar);
		for(int i=0;i<lstSJGrp.size();i++)
		{
			if (lstM.contains(lstSJGrp.get(i)))
				value = true;
		}
		}
		return value;
		
	}
	/**
	 * Check whether a triple has a star join with rdf:type triple pattern
	 * @param s triple subject
	 * @param p triple predicate
	 * @param lstT  rdf:type predicate list
	 * @return boolean
	 */
	private boolean isTypeJoin(String s, String p, ArrayList<String> lstT) {
		Boolean value=false;
		@SuppressWarnings({ "unchecked" })
		List<String>lstSJGrp= (List<String>) starJoinGrps.get(s);
		for(int i=0;i<lstSJGrp.size();i++)
		{
			if (lstT.contains(lstSJGrp.get(i))&& lstSJGrp.contains(p))
				value = true;
		}
		return value;
	}
	/**
	 * Check for Start join between query predicate and a set
	 * @param pred  predicate
	 * @param sbj  subject
	 * @param lst predicate set
	 * @return boolean
	 */
	private boolean isStarJoin(String sbj,String pred, ArrayList<String> lst) {
		Boolean value = false;
		@SuppressWarnings({ "unchecked" })
		List<String>lstSJGrp= (List<String>) starJoinGrps.get(sbj);
		for(int i=0;i<lstSJGrp.size();i++)
		{
			if (lst.contains(lstSJGrp.get(i))&& lstSJGrp.contains(pred))
				value = true;
		}
		return value;
	}
	/**
	 * Set membership checking
	 * @param p  element
	 * @param set  set name
	 * @return return true if set contains element  e
	 */
	private boolean isSetMember(String e, ArrayList<String> set) {
		Boolean value =false;
		if (set.contains(e))
			value = true;
		return value;
	}
	/**
	 * C-2 Condition checker
	 * @param p predicate
	 * @param o object
	 * @param s subject
	 * @return true if C-2 is satisfied
	 */
	public boolean C_2(String s, String p, String o) 
	{
		Boolean rdfType =false;
		if(p.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
		{
			String [] objPrts = o.split("/");
			o= objPrts[4];
			rdfType = true;
		}
		else
		{
		String [] predPrts = p.split("/");
		p= predPrts[4];
		}
		Boolean value = false;
		if(((rdfType ==true && o.equals("exon-expression_result"))||(isSetMember(p,Config.lstAEG)==true) )&& ((isStarJoin(s,p,Config.lstE)==true || isTypeJoin(s,p,Config.lstF)==true || isPathJoin(s,p,Config.lstE)==true) || (isStarJoin(s,p,Config.lstM)==false && isStarJoin(s,p,Config.lstD)==false && isTypeJoin(s,p,Config.lstB)==false && isTypeJoin(s,p,Config.lstC)==false&& isPathJoin(s,p,Config.lstM)==false && isPathJoin(s,p,Config.lstD)==false)))
				value = true;
		return value;
	}
	/**
	 * C-3 Condition checker
	 * @param p predicate
	 * @param o object
	 * @param s subject
	 * @return true if C-3 is satisfied
	 */
	public boolean C_3(String s, String p, String o) 
	{
		Boolean rdfType =false;
		if(p.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
		{
			String [] objPrts = o.split("/");
			o= objPrts[4];
			rdfType = true;
		}
		else
		{
		String [] predPrts = p.split("/");
		p= predPrts[4];
		}
		Boolean value = false;
		if(((rdfType ==true && o.equals("dna-methylation_result"))||(isSetMember(p,Config.lstAM)==true) )&& ((isStarJoin(s,p,Config.lstM)==true ||isTypeJoin(s,p,Config.lstB)==true || isPathJoin(s,p,Config.lstM)==true) || (isStarJoin(s,p,Config.lstE)==false && isStarJoin(s,p,Config.lstD)==false && isTypeJoin(s,p,Config.lstF)==false && isTypeJoin(s,p,Config.lstC)==false&& isPathJoin(s,p,Config.lstD)==false && isPathJoin(s,p,Config.lstE)==false)))
				 value = true;
		return value;
	}
	//---------------------
	/**
	 * filter sources based on the tumour specified
	 * @param sbjTumour 
	 *                  tumour name obtained from subject part of triple pattern
	 * @param type   type of result
	 * @return finalSources
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 */
	private String sourcesFilter(String sbjTumour, String type) throws RepositoryException, MalformedQueryException, QueryEvaluationException 
	{
		String finalSrc = null;
		if(type.equals("Expression-Exon_result"))
			type = "pink";
		else if (type.equals("DNA-Methylation_result"))
			type = "green";
		else
			type = "blue";
		String queryString = getSourceFilterQuery(sbjTumour,type);
		 TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			  finalSrc= result.next().getValue("endpoint").stringValue();
		   }
		
		/*Query query = QueryFactory.create(qryStr);
		QueryExecution qexec = QueryExecutionFactory.create(query, m);
		ResultSet rs = qexec.execSelect();
		while(rs.hasNext())
		{
			QuerySolution result = rs.nextSolution();
			finalSrc=(result.get("endpoint").toString());
		 }*/
		
		return finalSrc;
	}
	//------------------------------------------------
	/**
	 * predicate set loading query
	 * @param tumour tumour name
	 * @param sourceColor  source categories
	 * 
	 * 	 * @return SPARQL query for source filtering
	 */
	public String getSourceFilterQuery(String tumour, String sourceColor) {
		String qry = null;
		if (sourceColor!=null) 
		{
		 qry ="prefix tcga:<http://tcga.deri.ie/schema/> \n" +
	 		"select ?endpoint \n" +
	 		"where \n " +
	 		"{ \n " +
	 		"?s tcga:category  \""+sourceColor+"\". \n" +
	 	    "?s tcga:containTumours \""+tumour+"\". \n" +
	 	    "?s  tcga:endpointUrl  ?endpoint \n"+
	 	     "}";
		}
		else
		{
			 qry ="prefix tcga:<http://tcga.deri.ie/schema/> \n" +
		 		"select ?endpoint \n" +
		 		"where \n " +
		 		"{ \n " +
		 	    "?s tcga:containTumours \""+tumour+"\". \n" +
		 	    "?s  tcga:endpointUrl  ?endpoint \n"+
			 	     "}";
		}
		
		return qry;
		}
	
	
	/**
	 * get tumour from bound subject using patient barcode
	 * @param s
	 *           triple subject of format <http://tcga.deri.ie/[bcr-patienet-barcode]-[result type]>
	 *           e.g <http://tcga.deri.ie/TCGA-18-3406-c266>: bcr-patienet-barcode = TCGA-18-3406, result type = c =>copy number 
	 *           tumour name can be obtained using hash table lookeup for bcr-patienet-barcode value
	 *@return tumour name
	 *               
	 */
	public String getTumour(String s) {
	
		String tumour = null;
		String [] sbjParts = s.split("/");
		//System.out.println(sbjParts[3]);
		String []subPrts =sbjParts[3].split("-");
		String tss = subPrts[1];
		//System.out.println(tss);
		tumour= Config.tssTbl.get(tss);
		//System.out.println(tumour);
		return tumour;
		}
	//------------
	/**
	 * get class type using the subject value of triple
	 * @param s
	 *           triple subject of format <http://tcga.deri.ie/[bcr-patienet-barcode]-[result type]>
	 *           e.g <http://tcga.deri.ie/TCGA-18-3406-c266>: bcr-patienet-barcode = TCGA-18-3406, result type = c =>copy number 
	 *@return reslut type
	 *             
	 */
	public String getResultType(String s) {
		String type = null;
		String [] sbjParts = s.split("/");
		//System.out.println(sbjParts[3]);
		String []subPrts =sbjParts[3].split("-");
		type = subPrts[3].substring(0,1);
		if (type.equals("c"))
			type = "copy_number_result";
		else if (type.equals("e"))
			type = "expression_protein_result";
		else if (type.equals("m"))
			type = "miRNA_result";
		else if (type.equals("s"))
			type = "snp_result";
		else if (type.equals("d"))
			type = "DNA-Methylation_result";
		else if (type.equals("e"))
			type = "Expression-Exon_result";
		//System.out.println(type);
		return type;
	}
		/**
	 * Retrieve a set of relevant sources for this query.
	 * @return
	 */
	public Set<Endpoint> getRelevantSources() {
		Set<Endpoint> endpoints = new HashSet<Endpoint>();
		for (List<StatementSource> sourceList : stmtToSources.values())
			for (StatementSource source : sourceList)
				endpoints.add( EndpointManager.getEndpointManager().getEndpoint(source.getEndpointID()));
		return endpoints;
	}	
	
	/**
	 * Add a source to the given statement in the map (synchronized through map)
	 * 
	 * @param stmt
	 * @param source
	 */
	public void addSource(StatementPattern stmt, StatementSource source) {
		// The list for the stmt mapping is already initialized
		List<StatementSource> sources = stmtToSources.get(stmt);
		synchronized (sources) {
			sources.add(source);
		}
	}
	
	
	
	protected static class SourceSelectionExecutorWithLatch implements ParallelExecutor<BindingSet> {
		
		/**
		 * Execute the given list of tasks in parallel, and block the thread until
		 * all tasks are completed. Synchronization is achieved by means of a latch.
		 * Results are added to the map of the source selection instance. Errors 
		 * are reported as {@link OptimizationException} instances.
		 * 
		 * @param tasks
		 */
		public static void run(TCGASourceSelection sourceSelection, List<CheckTaskPair> tasks, Cache cache) {
			new SourceSelectionExecutorWithLatch(sourceSelection).executeRemoteSourceSelection(tasks, cache);
		}		
		
		private final TCGASourceSelection sourceSelection;
		private ControlledWorkerScheduler<BindingSet> scheduler = FederationManager.getInstance().getJoinScheduler();
		private CountDownLatch latch;
		private boolean finished=false;
		private Thread initiatorThread;
		protected List<Exception> errors = new ArrayList<Exception>();
		

		private SourceSelectionExecutorWithLatch(TCGASourceSelection sourceSelection) {
			this.sourceSelection = sourceSelection;
		}

		/**
		 * Execute the given list of tasks in parallel, and block the thread until
		 * all tasks are completed. Synchronization is achieved by means of a latch
		 * 
		 * @param tasks
		 */
		private void executeRemoteSourceSelection(List<CheckTaskPair> tasks, Cache cache) {
			if (tasks.size()==0)
				return;
			
			initiatorThread = Thread.currentThread();
			latch = new CountDownLatch(tasks.size());
			for (CheckTaskPair task : tasks)
				scheduler.schedule( new ParallelCheckTask(task.e, task.t, this) );
			
			try	{
				latch.await(); 	// TODO maybe add timeout here
			} catch (InterruptedException e) {
				log.debug("Error during source selection. Thread got interrupted.");
			}

			finished = true;
			
			// check for errors:
			if (errors.size()>0) {
				log.error(errors.size() + " errors were reported:");
				for (Exception e : errors)
					log.error(ExceptionUtil.getExceptionString("Error occured", e));
								
				Exception ex = errors.get(0);
				errors.clear();
				if (ex instanceof OptimizationException)
					throw (OptimizationException)ex;
				
				throw new OptimizationException(ex.getMessage(), ex);
			}
		}

		@Override
		public void run() { /* not needed */ }

		@Override
		public void addResult(CloseableIteration<BindingSet, QueryEvaluationException> res)	{
			latch.countDown();
		}

		@Override
		public void toss(Exception e) {
			errors.add(e);
			scheduler.abort(getQueryId());	// abort all tasks belonging to this query id
			if (initiatorThread!=null)
				initiatorThread.interrupt();
		}

		@Override
		public void done()	{ /* not needed */ }

		@Override
		public boolean isFinished()	{
			return finished;
		}

		@Override
		public int getQueryId()	{
			return sourceSelection.queryInfo.getQueryID();
		}
	}
	
	
	public class CheckTaskPair {
		public final Endpoint e;
		public final StatementPattern t;
		public CheckTaskPair(Endpoint e, StatementPattern t){
			this.e = e;
			this.t = t;
		}		
	}
	
	
	/**
	 * Task for sending an ASK request to the endpoints (for source selection)
	 * 
	 * @author Andreas Schwarte
	 */
	protected static class ParallelCheckTask implements ParallelTask<BindingSet> {

		protected final Endpoint endpoint;
		protected final StatementPattern stmt;
		protected final SourceSelectionExecutorWithLatch control;
		
		public ParallelCheckTask(Endpoint endpoint, StatementPattern stmt, SourceSelectionExecutorWithLatch control) {
			this.endpoint = endpoint;
			this.stmt = stmt;
			this.control = control;
		}

		
		@Override
		public CloseableIteration<BindingSet, QueryEvaluationException> performTask() throws Exception {
			try {
				TripleSource t = endpoint.getTripleSource();
				RepositoryConnection conn = endpoint.getConn(); 

				boolean hasResults = t.hasStatements(stmt, conn, EmptyBindingSet.getInstance());

				TCGASourceSelection sourceSelection = control.sourceSelection;
				CacheEntry entry = CacheUtils.createCacheEntry(endpoint, hasResults);
				sourceSelection.cache.updateEntry( new SubQuery(stmt), entry);

				if (hasResults)
					sourceSelection.addSource(stmt, new StatementSource(endpoint.getId(), StatementSourceType.REMOTE));
				
				return null;
			} catch (Exception e) {
				this.control.toss(e);
				throw new OptimizationException("Error checking results for endpoint " + endpoint.getId() + ": " + e.getMessage(), e);
			}
		}

		@Override
		public ParallelExecutor<BindingSet> getControl() {
			return control;
		}		
	}
	
		
}




