/*
 * Copyright (C) 2008-2012, fluid Operations AG
 *
 * FedX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fluidops.fedx;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import com.fluidops.fedx.cache.MemoryCache;
import com.fluidops.fedx.evaluation.concurrent.ControlledWorkerScheduler;
import com.fluidops.fedx.exception.FedXException;
import com.fluidops.fedx.exception.FedXRuntimeException;
import com.fluidops.fedx.monitoring.QueryLog;
import com.fluidops.fedx.monitoring.QueryPlanLog;


/**
 * Configuration properties for FedX based on a properties file. Prior to using this configuration
 * {@link #initialize(String)} must be invoked with the location of the properties file.
 * 
 * @author Andreas Schwarte
 *
 */
public class Config {

	protected static Logger log = Logger.getLogger(Config.class);
    public  final static Hashtable<String,String> tssTbl = new Hashtable<String,String>() ;
	private static Config instance = null;
	public static  RepositoryConnection con = null;
	  public static  ArrayList<String> lstD_blue = new ArrayList<String>() ;
	  public static  ArrayList<String> lstD_pink = new ArrayList<String>()  ;
	  public static ArrayList<String> lstD_green = new ArrayList<String>()  ;
	  public static ArrayList<String> lstA = new ArrayList<String>() ;
	  public static ArrayList<String> lstC = new ArrayList<String>() ;
	  public static ArrayList<String> lstD = new ArrayList<String>() ;
	  public static ArrayList<String> lstE = new ArrayList<String>() ;
	  public static ArrayList<String> lstM = new ArrayList<String>() ;
	  public static ArrayList<String> lstADG = new ArrayList<String>(); //UNION OF A, D, G
	  public static ArrayList<String> lstAEG = new ArrayList<String>(); 
	  public static ArrayList<String> lstAM = new ArrayList<String>(); 
	  public static ArrayList<String> lstB = new ArrayList<String>(); 
	  public static ArrayList<String> lstG = new ArrayList<String>(); 
	  public static ArrayList<String> lstF = new ArrayList<String>(); 
	
	
	public static Config getConfig() {
		if (instance==null)
			throw new FedXRuntimeException("Config not initialized. Call Config.initialize() first.");
		return instance;
	}
	
	protected static void reset() {
		instance=null;
	}
	
	/**
	 * Initialize the configuration with the specified properties file.
	 * 
	 * @param fedxConfig
	 * 			the optional location of the properties file. If not specified the default configuration is used.
	 * @throws RDFParseException 
	 * @throws QueryEvaluationException 
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static void initialize(String ...fedxConfig) throws FedXException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFParseException, IOException {
		if (instance!=null)
			throw new FedXRuntimeException("Config is already initialized.");
		instance = new Config();
		String cfg = fedxConfig!=null && fedxConfig.length==1 ? fedxConfig[0] : null;
		instance.init(cfg);
		instance.loadTCGASpefication();
		instance.createTSStoTumourHashTable();
		instance.loadSource_PredicateSets();
	}
	/**
	 * load source, predicate sets from settings/GroupSetting.n3
	 * @throws IOException 
	 * @throws RepositoryException 
	 * @throws MalformedQueryException 
	 * @throws QueryEvaluationException 
	 * @throws RDFParseException 
	 */
	public void  loadSource_PredicateSets() throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, RDFParseException 
	{
		loadD_blue();
		loadD_pink();
		loadD_green();
		loadListA();
		loadListC();
		loadListD();
		loadListE();
		loadListM();
		loadListG();
		lstADG.addAll(lstA) ;
		lstADG.addAll(lstD);
		lstADG.addAll(lstG);
		lstAEG.addAll(lstA);
		lstAEG.addAll(lstE);
		lstAEG.addAll(lstG);
		lstAM.addAll(lstA);
		lstAM.addAll(lstM);
		lstB.add("dna-methylation_result");
		lstF.add("expression-exon_result");
	}
	/**
	 * load List A from n3 distribution file
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 */
	public void loadListA() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
			String queryString = getPredicateSetQuery("A");
		     TupleQuery tupleQuery = null;
		 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			 TupleQueryResult result = tupleQuery.evaluate();
			   while(result.hasNext())
			   {
				   lstA.add(result.next().getValue("set").stringValue());
			   }
			
		/*String qryStr = getPredicateSetQuery("A");
			Query query = QueryFactory.create(qryStr);
			QueryExecution qexec = QueryExecutionFactory.create(query, m);
			ResultSet rs = qexec.execSelect();
			while(rs.hasNext())
			{
				QuerySolution result = rs.nextSolution();
				lstA.add(result.get("set").toString());
			 }*/
		}
	//------------

	/**
	 * load List C from n3 distribution file
	 * @throws QueryEvaluationException 
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 */
	public void loadListC() throws QueryEvaluationException, RepositoryException, MalformedQueryException {
		
		String queryString = getPredicateSetQuery("C");
	     TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			   lstC.add(result.next().getValue("set").stringValue());
		   }
	}
	//------------
	/**
	 * load List D from n3 distribution file
	 * @throws QueryEvaluationException 
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 */
	public void loadListD() throws QueryEvaluationException, RepositoryException, MalformedQueryException {
		
		String queryString = getPredicateSetQuery("D");
	     TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			   lstD.add(result.next().getValue("set").stringValue());
		   }
		
		}
	//------------
	/**
	 * load List E from n3 distribution file
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 */
	public void loadListE() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		
		String queryString = getPredicateSetQuery("E");
	     TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			   lstE.add(result.next().getValue("set").stringValue());
		   }
		
		}
	/**
	 * load List G from n3 distribution file
	 * @throws QueryEvaluationException 
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 */
	public void loadListG() throws QueryEvaluationException, RepositoryException, MalformedQueryException {
		
		String queryString = getPredicateSetQuery("G");
	     TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			   lstG.add(result.next().getValue("set").stringValue());
		   }
		
		}
	/**
	 * load List M from n3 distribution file
	 * @throws QueryEvaluationException 
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 */
	public void loadListM() throws QueryEvaluationException, RepositoryException, MalformedQueryException {
		
		String queryString = getPredicateSetQuery("M");
	     TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			   lstM.add(result.next().getValue("set").stringValue());
		   }
		
		}
	/**
	 * predicate set loading query
	 * @param setName 
	 *                name of the set 
	 * @return SPARQL query for predicate set loading
	 */
	private String getPredicateSetQuery(String setName) {
		 String qry ="prefix tcga:<http://tcga.deri.ie/schema/> \n" +
	 		"select ?set \n" +
	 		"where \n " +
	 		"{ \n " +
	 		"?s tcga:setName \""+setName+"\". \n" +
	 	     "?s  tcga:setElements ?set. \n" +
	 	     "}";
		return qry;
		}
	//-------------------------------
	/**
	 * load D_blue source set using the n3 file
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 */
	public void loadD_blue() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		String queryString = getSourceSetQuery("blue");
	     TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			   lstD_blue.add(result.next().getValue("endpoint").stringValue());
		   }
		
	/*	String qryStr = getSourceSetQuery("blue");
		Query query = QueryFactory.create(qryStr);
		QueryExecution qexec = QueryExecutionFactory.create(query, m);
		ResultSet rs = qexec.execSelect();
		while(rs.hasNext())
		{
			QuerySolution result = rs.nextSolution();
			lstD_blue.add(result.get("endpoint").toString());
		 }*/
	}
	//-------------------------------
	/**
	 * load D_pink source set using the n3 file
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 */
	public void loadD_pink() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		
		String queryString = getSourceSetQuery("pink");
	     TupleQuery tupleQuery = null;
	 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		 TupleQueryResult result = tupleQuery.evaluate();
		   while(result.hasNext())
		   {
			   lstD_pink.add(result.next().getValue("endpoint").stringValue());
		   }
	}
	//-------------------------------
	/**
	 * load D_green source set using the n3 file
	 * @throws MalformedQueryException 
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 */
	public void loadD_green() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
			 String queryString = getSourceSetQuery("green");
		     TupleQuery tupleQuery = null;
		 	 tupleQuery = Config.con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			 TupleQueryResult result = tupleQuery.evaluate();
			   while(result.hasNext())
			   {
				   lstD_green.add(result.next().getValue("endpoint").stringValue());
			   }
			}
	//---------------------------------------
	/**
	 * 
	 * @param srcSetName
	 *        name of the set
	 * @return SPARQL source set query
	 */
public String getSourceSetQuery(String srcSetName) 
{
	 String qry ="prefix tcga:<http://tcga.deri.ie/schema/> \n" +
	 		"select ?endpoint \n" +
	 		"where \n " +
	 		"{ \n " +
	 		"?s tcga:category  \""+srcSetName+"\". \n" +
	 	     "?s  tcga:endpointUrl  ?endpoint. \n" +
	 	     "}";
		return qry;
	}

	
	/**
	 * create TSS to Tumour hash table
	 * @throws IOException 
	 */
	public void createTSStoTumourHashTable() 
	{
		
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream("specification/TSSTable.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	   	    DataInputStream in = new DataInputStream(fstream);
	   	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	   	   try {
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   	   String strLine;
	   	  	   String[] linePrts;
	   	 try {
			while((strLine = br.readLine())!=null)
			 {
				linePrts = strLine.split("\t");
				tssTbl.put(linePrts[1], linePrts[0]);
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}

/**
 * Load TCGA specification file to repository
 */
public void loadTCGASpefication() {
	File fileDir = new File("specification\\");
	Repository myRepository = new SailRepository( new MemoryStore(fileDir) );
	try {
		myRepository.initialize();
	} catch (RepositoryException e) {
		e.printStackTrace();
	}
	    File file = new File("specification\\loadDistribution.n3");
		
		try {
			con = myRepository.getConnection();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		   try {
			con.add(file, "hcls.deri.ie", RDFFormat.N3);
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		  
		
	}



	private Properties props;
	private Config() {
		props = new Properties();
	}
	
	private void init(String configFile) throws FedXException {
		if (configFile==null) {
			log.warn("No configuration file specified. Using default config initialization.");
			return;
		}
		log.info("FedX Configuration initialized from file '" + configFile + "'.");
		try {
			FileInputStream in = new FileInputStream(configFile);
			props.load( in );
			in.close();
		} catch (IOException e) {
			throw new FedXException("Failed to initialize FedX configuration with " + configFile + ": " + e.getMessage());
		}
	}
	
	public String getProperty(String propertyName) {
		return props.getProperty(propertyName);
	}
	
	public String getProperty(String propertyName, String def) {
		return props.getProperty(propertyName, def);
	}
	
	/**
	 * the base directory for any location used in fedx, e.g. for repositories
	 * 
	 * @return
	 */
	public String getBaseDir() {
		return props.getProperty("baseDir", "");
	}
	
	/**
	 * The location of the dataConfig.
	 * 
	 * @return
	 */
	public String getDataConfig() {
		return props.getProperty("dataConfig");
	}
	
	
	/**
	 * The location of the cache, i.e. currently used in {@link MemoryCache}
	 * 
	 * @return
	 */
	public String getCacheLocation() {
		return props.getProperty("cacheLocation", "cache.db");
	}
	
	/**
	 * The number of join worker threads used in the {@link ControlledWorkerScheduler}
	 * for join operations. Default is 20.
	 * 
	 * @return
	 */
	public int getJoinWorkerThreads() {
		return Integer.parseInt( props.getProperty("joinWorkerThreads", "20"));
	}
	
	/**
	 * The number of join worker threads used in the {@link ControlledWorkerScheduler}
	 * for join operations. Default is 20
	 * 
	 * @return
	 */
	public int getUnionWorkerThreads() {
		return Integer.parseInt( props.getProperty("unionWorkerThreads", "20"));
	}
	
	/**
	 * The block size for a bound join, i.e. the number of bindings that are integrated
	 * in a single subquery. Default is 15.
	 * 
	 * @return
	 */
	public int getBoundJoinBlockSize() {
		return Integer.parseInt( props.getProperty("boundJoinBlockSize", "15"));
	}
	
	/**
	 * Get the maximum query time in seconds used for query evaluation. Applied in CLI
	 * or in general if {@link QueryManager} is used to create queries.<p>
	 * 
	 * Set to 0 to disable query timeouts.
	 * 
	 * @return
	 */
	public int getEnforceMaxQueryTime() {
		return Integer.parseInt( props.getProperty("enforceMaxQueryTime", "0"));
	}
	
	/**
	 * Flag to enable/disable monitoring features. Default=false.
	 * 
	 * @return
	 */
	public boolean isEnableMonitoring() {
		return Boolean.parseBoolean( props.getProperty("enableMonitoring", "true"));	
	}
	
	/**
	 * Flag to enable/disable query plan logging via {@link QueryPlanLog}. Default=false
	 * The {@link QueryPlanLog} facility allows to retrieve the query execution plan
	 * from a variable local to the executing thread.
	 * 
	 * @return
	 */
	public boolean isLogQueryPlan() {
		return Boolean.parseBoolean( props.getProperty("monitoring.logQueryPlan", "false"));	
	}
	
	/**
	 * Flag to enable/disable query logging via {@link QueryLog}. Default=false
	 * The {@link QueryLog} facility allows to log all queries to a file. See 
	 * {@link QueryLog} for details. 
	 * 
	 * @return
	 */
	public boolean isLogQueries() {
		return Boolean.parseBoolean( props.getProperty("monitoring.logQueries", "false"));	
	}
	
	/**
	 * Returns the path to a property file containing prefix declarations as 
	 * "namespace=prefix" pairs (one per line).<p> Default: no prefixes are 
	 * replaced. Note that prefixes are only replaced when using the CLI
	 * or the {@link QueryManager} to create/evaluate queries.
	 * 
	 * Example:
	 * 
	 * <code>
	 * foaf=http://xmlns.com/foaf/0.1/
	 * rdf=http://www.w3.org/1999/02/22-rdf-syntax-ns#
	 * =http://mydefaultns.org/
	 * </code>
	 * 			
	 * @return
	 */
	public String getPrefixDeclarations() {
		return props.getProperty("prefixDeclarations");
	}
	
	/**
	 * The debug mode for worker scheduler, the scheduler prints usage stats regularly
	 * if enabled
	 * 
	 * @return
	 * 		false
	 */
	public boolean isDebugWorkerScheduler() {
		return Boolean.parseBoolean( props.getProperty("debugWorkerScheduler", "false"));
	}
	
	/**
	 * The debug mode for query plan. If enabled, the query execution plan is
	 * printed to stdout
	 * 
	 * @return
	 * 		false
	 */
	public boolean isDebugQueryPlan() {
		return Boolean.parseBoolean( props.getProperty("debugQueryPlan", "false"));
	}
	
	/**
	 * Set some property at runtime
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {
		props.setProperty(key, value);
	}
}
