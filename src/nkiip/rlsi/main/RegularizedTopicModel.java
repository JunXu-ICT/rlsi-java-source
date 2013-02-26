/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
 ****************************************************************************************
 *(C) Copyright 2013 Laboratory of Intelligent Information Processing, Nankai University
 *    All Rights Reserved.
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import nkiip.rlsi.basicStruct.KeyValuePair;
import nkiip.rlsi.matrix.DenseMatrix;
import nkiip.rlsi.matrix.SparseMatrixByRow;
import nkiip.rlsi.thread.D_times_VT_MultiThread;
import nkiip.rlsi.thread.STCD_Lasso_MultiThread;
import nkiip.rlsi.thread.Sigma_times_Phi_MultiThread;
import nkiip.rlsi.thread.UT_times_D_MultiThread;
import nkiip.rlsi.thread.UT_times_U_MultiThread;
import nkiip.rlsi.thread.VT_times_V_MultiThread;
import nkiip.rlsi.util.Arguments;
import nkiip.rlsi.util.StringSplitter;

public class RegularizedTopicModel {
	public static void main(String[] args) throws Exception {
		Arguments cmmdArg = new Arguments(args);
		RLSI rlsi = new RLSI();
		String fnIn_TfIDF = "";
		String fnIn_VMatrix = "";
		String fnIn_UMatrix = "";
		String str_restart = "";
		try {
			fnIn_TfIDF = cmmdArg.getValue("d");
			if (fnIn_TfIDF == null || fnIn_TfIDF.equals("")) {
				usage();
				return;
			}

			// restart options
			str_restart = cmmdArg.getValue("restart");
			if ("u".equalsIgnoreCase(str_restart)) {
				fnIn_UMatrix = cmmdArg.getValue("iu");
				rlsi.restart = str_restart;
			} else if ("v".equalsIgnoreCase(str_restart)) {
				fnIn_VMatrix = cmmdArg.getValue("iv");
				rlsi.restart = str_restart;
			}

			String str_UPrefix = cmmdArg.getValue("u");
			String str_VPrefix = cmmdArg.getValue("v");
			// filename prefix options for output files
			if (str_UPrefix != null && !str_UPrefix.equals("")) {
				rlsi.m_UMatrix_prefix = str_UPrefix;
			}
			if (str_VPrefix != null && !str_VPrefix.equals("")) {
				rlsi.m_VMatrix_prefix = str_VPrefix;
			}

			if (cmmdArg.getValue("#") != null
					&& !cmmdArg.getValue("#").equals("")) {
				rlsi.m_iMaxOutIterNum = Integer.parseInt(cmmdArg.getValue("#"));
			}

			rlsi.Init(fnIn_TfIDF, cmmdArg.getValue("t"),
					cmmdArg.getValue("l1"), cmmdArg.getValue("l2"));

			// specify the number of cores for running
			if (cmmdArg.getValue("c") != null) {
				int iNumCores = Integer.parseInt(cmmdArg.getValue("c"));
				if (iNumCores > 0) {
					VT_times_V_MultiThread.iMaxThreads = iNumCores;
					D_times_VT_MultiThread.iMaxThreads = iNumCores;
					STCD_Lasso_MultiThread.iMaxThreads = iNumCores;
					UT_times_U_MultiThread.iMaxThreads = iNumCores;
					UT_times_D_MultiThread.iMaxThreads = iNumCores;
					Sigma_times_Phi_MultiThread.iMaxThreads = iNumCores;
				}
			}

			// specify the iterations that output U and V
			if (cmmdArg.getValue("su") != null) {
				int iSkipU = Integer.parseInt(cmmdArg.getValue("su"));
				if (iSkipU > 0) {
					rlsi.iOutputIterSkipU = iSkipU;
				}
			}
			if (cmmdArg.getValue("sv") != null) {
				int iSkipV = Integer.parseInt(cmmdArg.getValue("sv"));
				if (iSkipV > 0) {
					rlsi.iOutputIterSkipV = iSkipV;
				}
			}
		} catch (Exception e) {
			usage();
			return;
		}

		int iNumberOfDocs = rlsi.m_Term_Doc_DMatrix.iNumberOfDocuments;
		int iNumberOfTerms = rlsi.m_Term_Doc_DMatrix.iNumberOfTerms;

		rlsi.m_Doc_Topic_VMatrix = new DenseMatrix(iNumberOfDocs,
				rlsi.m_NumTopics);
		if (false == LoadRandomDocTopicMatrix(fnIn_VMatrix, iNumberOfDocs,
				rlsi.m_NumTopics, rlsi.m_Doc_Topic_VMatrix)) {
			throw new Exception("Error loading initial V matrx");
		}

		System.out.println("Load " + iNumberOfDocs + " documents and "
				+ rlsi.m_NumTopics + " topics.\n");

		// Init U Matrix
		rlsi.m_Term_Topic_UMatrix = new SparseMatrixByRow(iNumberOfTerms,
				rlsi.m_NumTopics);
		if (str_restart == "u") {
			// first get the termstring to termid mapping
			Hashtable<String, Integer> dict_term2id = new Hashtable<String, Integer>();
			for (int i = 0; i < rlsi.m_Term_Doc_DMatrix.lstInternalTermID2OrgTermID
					.size(); i++) {
				dict_term2id.put(
						rlsi.m_Term_Doc_DMatrix.lstInternalTermID2OrgTermID
								.get(i), i);
			}
			if (false == LoadUMatrix(fnIn_UMatrix, dict_term2id,
					rlsi.m_Term_Topic_UMatrix)) {
				throw new Exception("Error loading initial U matrix");
			}
		}
		System.out.println("Start learn RLSI model");
		rlsi.RLSI_Learn();
		System.out.println("Success.");

		System.out.println("Write matrix U and V\n");
		rlsi.OutputU(rlsi.m_UMatrix_prefix + ".term-topic_UMatrix");
		rlsi.OutputV(rlsi.m_VMatrix_prefix + ".doc-topic_VMatrix");
		System.out.println("Suceess!");

	}

	static void usage() {
		System.out
				.println("Usage: RLSI.exe -d word_doc_file_name [options]\n\n"
						+

						"Topic model options: \n"
						+ "   -t int      -> number of topics (default 50)\n"
						+ "   -l1 float   -> L1-norm parameter for U (default 0.5)\n"
						+ "   -l2 float   -> L2-norm parameter for V (default 0.5)\n\n"
						+

						"Output options: \n"
						+ "   -v string   -> the filename prefix for outputted V matrix (default VMatrix)\n"
						+ "   -u string   -> the filename prefix for outputted U matrix (default UMatrix)\n"
						+ "   -sv int      -> number of skipped iterations that don¡¯t output V (default 5)\n"
						+ "   -su int      -> number of skipped iterations that don¡¯t output U (default 5)\n\n"
						+

						"Optimization options:  \n"
						+ "   -# int      -> number of learning iterations (default 500)\n"
						+ "   -c int      -> number of threads running in parallel (default 24)\n\n"
						+

						"Restart options: \n"
						+ "   -restart [u|v] -> restart from initial U matrix or V matrix\n"
						+ "   -iu string  -> filename of initial U matrix \n"
						+ "   -iv string  -> filename of initial V matrix \n\n");
	}

	static boolean LoadUMatrix(String fnIn_Umatrix,
			Hashtable<String, Integer> term2id, SparseMatrixByRow UMatrix)
			throws Exception {
		System.out.println("Load U matrix from " + fnIn_Umatrix
				+ " for restart from U.");
		BufferedReader rd = new BufferedReader(new FileReader(fnIn_Umatrix));

		rd.readLine();
		String line = rd.readLine();
		while (line != null) {

			String[] toks = StringSplitter.split("\t ", line);

			if (!term2id.containsKey(toks[0])) {
				continue;
			}
			int iTermID = term2id.get(toks[0]);

			UMatrix.GetRow(iTermID).clear();
			for (int iEntry = 1; iEntry < toks.length; iEntry++) {
				String[] tid_value = toks[iEntry].split(":");
				int iTopic = Integer.parseInt(tid_value[0]);
				float value = Float.parseFloat(tid_value[1]);
				UMatrix.GetRow(iTermID).add(new KeyValuePair(iTopic, value));
			}
			line = rd.readLine();
		}
		rd.close();
		return true;
	}

	static boolean LoadRandomDocTopicMatrix(String fnIn_VMatrix, int iNumDocs,
			int iNumTopics, DenseMatrix VT_matrix) throws Exception {
		System.out.println();
		if (fnIn_VMatrix == null || fnIn_VMatrix.equals("")) {
			System.out.println("\nAuto generate random V matrix.");
			Random rd = new Random(new Date().getTime());

			float[] vals = new float[iNumTopics];
			for (int i = 0; i < iNumDocs; i++) {
				float sum = 0;
				for (int j = 0; j < iNumTopics; j++) {
					vals[j] = (float) rd.nextDouble();
					sum += (vals[j] * vals[j]);
				}
				sum = (float) Math.sqrt(sum);
				for (int j = 0; j < iNumTopics; j++) {
					VT_matrix.SetValue(i, j, vals[j] / sum);
				}

				if (i % 10000 == 0) {
					System.out.println(i + "/" + iNumDocs);
				}
			}
			System.out.println();
			return true;
		} else {
			System.out
					.println("\nLoading random matrix V from " + fnIn_VMatrix);
			BufferedReader rd = new BufferedReader(new FileReader(fnIn_VMatrix));

			// ignore the first line;
			rd.readLine();
			for (int i = 0; i < iNumDocs; i++) {
				String ln = rd.readLine();
				if (ln == null || ln == "") {
					return false;
				}
				String[] toks = StringSplitter
						.RemoveEmptyEntries(StringSplitter.split("\t ", ln));
				// assert the docid
				int docID = Integer.parseInt(toks[0]);
				if (docID != i) {
					return false;
				}

				for (int iTopicID = 0; iTopicID < iNumTopics; iTopicID++)
				{
					String[] topicid_tfidf = toks[iTopicID + 1].split(":");
					int tID = Integer.parseInt(topicid_tfidf[0]);
					if (tID != iTopicID)
						return false;

					float tfidf = Float.parseFloat(topicid_tfidf[1]);
					VT_matrix.SetValue(i, iTopicID, tfidf);
				}
			}
			rd.close();
			System.out.println();
			return true;
		}
	}
}
