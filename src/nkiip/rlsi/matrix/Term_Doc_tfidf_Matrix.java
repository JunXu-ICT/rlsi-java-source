/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
 ****************************************************************************************
 *(C) Copyright 2013 Laboratory of Intelligent Information Processing, Nankai University
 *    All Rights Reserved.
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import nkiip.rlsi.basicStruct.DocID_TFIDF;
import nkiip.rlsi.util.StringSplitter;

public class Term_Doc_tfidf_Matrix {

	public int iNumberOfTerms = 0;
	public int iNumberOfDocuments = 0;
	public List<String> lstInternalTermID2OrgTermID = new ArrayList<String>();
	public List<String> lstInternalDocID2OrgDocID = new ArrayList<String>();

	DocID_TFIDF[][] pData = null;

	public boolean Load(String fn) throws Exception {
		BufferedReader sr = new BufferedReader(new FileReader(fn));
		{
			String line = sr.readLine();
			String[] toks = StringSplitter.RemoveEmptyEntries(StringSplitter
					.split(",:; ", line));

			iNumberOfTerms = Integer.parseInt(toks[1]);
			iNumberOfDocuments = Integer.parseInt(toks[3]);
			System.out.println("\nLoading " + iNumberOfDocuments
					+ " documents and " + iNumberOfTerms + " words from " + fn);

			pData = new DocID_TFIDF[iNumberOfTerms][];
			int cnt = 0;
			line = sr.readLine();
			while (line != null) {
				String[] tokens = StringSplitter
						.RemoveEmptyEntries(StringSplitter.split("\t ", line));
				String orgTerm = tokens[0];
				int iDocFreq = Integer.parseInt(tokens[1]);

				if (cnt % 10000 == 0) {
					System.out.println("\r" + cnt + "/" + iNumberOfTerms);
				}
				cnt++;

				int termid = lstInternalTermID2OrgTermID.size();
				lstInternalTermID2OrgTermID.add(orgTerm);

				pData[termid] = new DocID_TFIDF[iDocFreq];
				for (int i = 0; i < iDocFreq; i++) {
					pData[termid][i] = new DocID_TFIDF();
				}

				for (int i = 3; i < tokens.length; i++) {
					String[] tid_tfidf = tokens[i].split(":");
					pData[termid][i - 3].setDocID(Integer
							.parseInt(tid_tfidf[0]));
					pData[termid][i - 3].setTFIDF(Float
							.parseFloat(tid_tfidf[1]));
				}
				line = sr.readLine();
			}
		}
		sr.close();
		iNumberOfTerms = lstInternalTermID2OrgTermID.size();
		System.out.println("\n" + iNumberOfDocuments + " documents and "
				+ iNumberOfTerms + " words loaded");
		return true;
	}

	public DocID_TFIDF[] GetRow(int iTerm) {
		return pData[iTerm];
	}
}
