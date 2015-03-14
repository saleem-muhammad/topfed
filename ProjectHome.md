#### <font color='blue'>TopFed: Cancer Genome Atlas (TCGA) tailored federated query  processing and linking to Linked Open Data (LOD) Cloud </font> [[pre-print pdf](http://www.jbiomedsem.com/imedia/1543416435123551_article.pdf)] ####

### Achievements ###

  * Best paper award at conference on Semantics in Healthcare and Life Sciences (CSHALS 2014) with paper titled GenomeSnip: Fragmenting the Genomic Wheel to augment discovery in cancer research. PDF available [here](http://goo.gl/Nkgi0P). Demo is available [here](http://srvgal78.deri.ie/genomeSnip/).
  * We are happy to announce that we won the [Semantic Web Challenge](http://challenge.semanticweb.org/) <font color='red'> (Big Data track award) </font> at [ISWC2013](http://iswc2013.semanticweb.org/) with paper [Fostering Serendipity through Big Linked Data](http://goo.gl/gGLtzi). The idea behind this paper was to show how the integration and visualization of large amounts of data can empower end users during their quest for new knowledge. In this paper, we implemented this idea for bio-medical experts by combining [Linked TCGA](http://goo.gl/bH9f9u) (the winner of the [I-Challenge 2013](http://i-semantics.tugraz.at/i-challenge/call-for-submissions)) with publications from PubMed within the visualization available [here](http://srvgal78.deri.ie/tcga-pubmed/).
  * We are happy to announce that we won the [I-Challenge](http://i-know.tugraz.at/i-know-awards-2013#linked_data_cup) ([Linked Data Cup](http://i-semantics.tugraz.at/i-challenge/call-for-submissions))<font color='red'> Linked Data Cup award </font>at [I-Semantics2013](http://i-semantics.tugraz.at/) with paper [Linked Cancer Genome Atlas Database](http://goo.gl/bH9f9u). In this paper we presented various use cases towards effective cancer treatment using Linked Data technologies. Demo available [here](http://linkeddatacup-demo.deri.ie/).

### User Manual ###

> A step-by-step [user manual](http://goo.gl/0oTAKV) for installing and configuring the complete TopFed execution environment is now available.

### Evaluation Queries ###
All of the SPARQL queries used for our evaluation can be found [here](http://goo.gl/UxUEXk).
### Linked TCGA Data Dumps and SPARQL Endpoints ###

> [Linked TCGA Data Dumps](http://tcga.deri.ie/dumps/) and [SPARQL endpoints](http://tcga.deri.ie/) (10 tumors) are now available.

### Linked TCGA Schema ###

![https://sites.google.com/site/saleemsweb/home/tcga%20schema.jpg](https://sites.google.com/site/saleemsweb/home/tcga%20schema.jpg)

For the clinical data (e.g drug, follow-up, radiation etc.), we have only included important properties as the complete list of properties is around 300.

### Linked TCGA Current Statistics ###

TCGA tumors statistics are available below:
| **S.No.** | **Tumor Type** | **Original Size(GB)** | **Refined Size (GB)** | **RDFized Size (GB)** | **Triples (Million)** |
|:----------|:---------------|:----------------------|:-----------------------|:----------------------|:----------------------|
|1 |Lymphoid Neoplasm Diffuse Large B-cell Lymphoma (DLBC)| 0.37 |0.20 |0.83 |35|
|2 |Cutaneous melanoma (UCS)| 1.2 |0.64 |2.6 |113|
|3 |Glioblastoma multiforme (GBM)| 2.3 |0.77 |2.8 |132|
|4 |Esophageal carcinoma (ESCA)| 1.5 |0.88 |3.4 |149|
|5 |Adrenocortical carcinoma (ACC)| 1.6 |0.90 |3.6 |158|
|6 |Pancreatic adenocarcinoma (PAAD)|2.6 |1.1 |4.5 |200|
|7 |Kidney Chromophobe (KICH)| 3.7 |1.4 |5.3 |242|
|8 |Sarcoma (SARC)|3.8| 1.5| 5.9| 267|
|9 |Cervical (CESC)| 8.75 |2.44 |8.86 |400.19|
|10|Ovarian serous cystadenocarcinoma (OV)| 8.2 |2.4 |8.7 |410|
|11|Rectal adenocarcinoma (READ)| 8.07| 2.25| 9.04| 413.31|
|12|Papillary Kidney (KIRP)| 10.40 |2.90 |10.4| 469.65|
|13|Stomach adenocarcinoma (STAD)|5.5| 2.9| 12| 529|
|14|Liver hepatocellular carcinoma (LIHC)| 8.2 |3.1 |12 |550|
|15|Bladder cancer (BLCA)| 12.16 |3.39 |12.3| 556.38|
|16|Acute Myeloid Leukemia (LAML)| 14.85| 4.14 |15.1| 684.05|
|17|Lower Grade Glioma (LGG)| 17.08 |4.76 |17.1 |778.82|
|18|Prostate adenocarcinoma (PRAD)| 18.05| 5.03| 18.1| 821.01|
|19|Lung squamous carcinoma (LUSC)| 20.63 |5.75| 20.5| 927.08|
|20|Cutaneous melanoma (SKCM)| 23.22 |6.47 |23.2 |1050.94|
|21|Uterine Corpus Endometrial Carcinoma (UCEC)| 13 |5.98 |24.2 |1070|
|22|Colon adenocarcinoma (COAD)| 18 |6.64 |26 |1175|
|23|Head and neck squamous cell(HNSC)| 27.6| 7.69| 27.5| 1245.37|
|24|Lung adenocarcinoma (LUAD)| 23 |9.1 |36 |1611|
|25|Kidney renal clear cell carcinoma (KIRC)| 24 | 9.4| 37|1658|
|26|Thyroid carcinoma (THCA)| 26 |10.1 |40 |1796|
|27|Breast invasive carcinoma (BRCA)| 45 |17 |65 |2959|

A total of <font color='red'> 20.4 Billion Triples </font>for 27 Tumors. New Tumors data is coming.

### TopFed Running ###

Please refer to [user manual](http://goo.gl/0oTAKV) for detail instructions.

### Experimental Data ###
The data used for the experiments can be downloaded from the links below.

**1. Blue1-virtuoso-server-Data**: https://topfed.googlecode.com/files/blue1.rar (It contains RDFized data of gene expression, protein expression, copy number result (cnr), single nucleotide polymorphism (snp), mirna expression and clinical patient data for five tumours (BLCA, CESC, HNSC, KIRP and LAML))

**2. Blue2-virtuoso-server-Data:** https://topfed.googlecode.com/files/blue2.rar (It contains RDFized data of gene expression, protein expression, copy number result (cnr), single nucleotide polymorphism (snp), mirna expression and clinical patient data for five tumours (LGG, LUSC, PRAD, READ and SKCM).)

**3. Pink1-virtuoso-server-Data:** https://docs.google.com/file/d/0B4tI4IicGSFDR3V0SXFsakltRXM/edit?usp=sharing (It contains RDFized data of Exon data for three tumours (BLCA, CESC and HNSC))

**4. Pink2-virtuoso-server-Data:** https://docs.google.com/file/d/0B4tI4IicGSFDRHd3cmxiV2NjWFE/edit?usp=sharing (It contains RDFized data of Exon data for three tumours (KIRP, LAML and LUSC))

**5. Pink3-virtuoso-server-Data:** https://docs.google.com/file/d/0B4tI4IicGSFDR3hGeHdLNTJfUWs/edit?usp=sharing (It contains RDFized data of Exon data for four tumours (LUSC, PRAD, READ and SKCM))

**6. Green1-virtuoso-server-Data:** https://docs.google.com/file/d/0B4tI4IicGSFDUk52ckpzSnVpNlU/edit?usp=sharing (It contains RDFized data of DNA Methylation data for two tumours (BLCA and CESC))

**7. Green2-virtuoso-server-Data:** https://docs.google.com/file/d/0B4tI4IicGSFDVGZ5Ql95RmdINEU/edit?usp=sharing (It contains RDFized data of DNA Methylation data for two tumours (HNSC and KIRP))

**8. Green3-virtuoso-server-Data:** https://docs.google.com/file/d/0B4tI4IicGSFDZGo3cjczU0VIeUE/edit?usp=sharing (It contains RDFized data of DNA Methylation data for two tumours (LAML and LGG))

**9. Green4-virtuoso-server-Data:** https://docs.google.com/file/d/0B4tI4IicGSFDMDFnUkZZQWVwUFU/edit?usp=sharing (It contains RDFized data of DNA Methylation data for two tumours (LUSC and PRAD))

**10. Green5-virtuoso-server-Data:** https://docs.google.com/file/d/0B4tI4IicGSFDWmZJWS1SWWc5T3M/edit?usp=sharing (It contains RDFized data of DNA Methylation data for two tumours (READ and SKCM))

### Portable Virtuoso Servers ###
To facilitate easy and fast re-produce-ability of results, we have provided portable virtuoso servers (only windows based) used in our experiments. Now you don't need to upload the data first into the servers (SPARQL endpoint) and perform experiments after that. All you need to download the server from the links given below and go to "bin folder", click on "start.bat" file. A virtuoso server will be available at http://your.system.ip.address:8890/sparql.

1. Blue1-virtuoso-server: https://docs.google.com/file/d/0BzemFAUFXpqOeTR5NWdoMG9hRms/edit?usp=sharing

2. Blue2-virtuoso-server: https://docs.google.com/file/d/0BzemFAUFXpqOeGdhenVIVW52amc/edit?usp=sharing

3. Pink1-virtuoso-server: https://docs.google.com/file/d/0BzemFAUFXpqOamJCckNCQ1JYMGs/edit?usp=sharing

4. Pink2-virtuoso-server: https://docs.google.com/file/d/0BzemFAUFXpqOOVYxOG1hX3haWHc/edit?usp=sharing

5. Pink3-virtuoso-server: https://docs.google.com/file/d/0BzemFAUFXpqOWFlRZFZhcjBMazg/edit?usp=sharing

6. Green1-virtuoso-server: https://docs.google.com/file/d/0B4tI4IicGSFDTTYwQmk5cUt4OG8/edit?usp=sharing

7. Green2-virtuoso-server: https://docs.google.com/file/d/0B4tI4IicGSFDaTJpMGsxc25tNEk/edit?usp=sharing

8. Green3-virtuoso-server: https://docs.google.com/file/d/0B4tI4IicGSFDVXozdXJMaVp3Sjg/edit?usp=sharing

9. Green4-virtuoso-server: https://docs.google.com/file/d/0B4tI4IicGSFDbGVmdjdEeFRZLXc/edit?usp=sharing

10. Green5-virtuoso-server: https://docs.google.com/file/d/0B4tI4IicGSFDenRnaktCbExHSzA/edit?usp=sharing

### Who is Behind This ###

The Linked TCGA project is developed and maintained by a team of researchers from renowned research labs:
  * [Muhammad Saleem](https://sites.google.com/site/saleemsweb/) (AKSW, University of Leipzig)
  * [Ali Hasnain](https://www.deri.ie/users/ali-hasnain/) (INSIGHT @ NUI Galway)
  * [Axel-Cyrille Ngonga Ngomo](http://aksw.org/AxelNgonga.html) (AKSW, University of Leipzig)
  * [Aftab Iqbal](http://www.deri.ie/users/aftab-iqbal/) (INSIGHT @ NUI Galway)
  * [Shanmukha Sampath](http://ts-eurotrain.eu/index.php/training-programme/fellows/sampath) (Democritus University of Thrace, Alexandroupoli, Greece)
  * [Bade Iriabho](https://sites.google.com/a/mathbiol.org/badeiriabho/) (University of Alabama at Birmingham)
  * [Jonas S. Almeida](http://jonasalmeida.info/) (University of Alabama at Birmingham)
  * [Helena F. Deus](http://lenadeus.info/) (Foundation Medicine)
  * [Sarven Capadisli ](http://csarven.ca)