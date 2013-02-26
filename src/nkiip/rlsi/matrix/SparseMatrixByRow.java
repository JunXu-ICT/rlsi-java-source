/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
 ****************************************************************************************
 *(C) Copyright 2013 Laboratory of Intelligent Information Processing, Nankai University
 *    All Rights Reserved.
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/
package nkiip.rlsi.matrix;

import java.util.LinkedList;

import nkiip.rlsi.basicStruct.KeyValuePair;

public class SparseMatrixByRow {

	public LinkedList<KeyValuePair>[] p_Rows = null;
	public int m_NumRow;
	public int m_NumColumn;

	public SparseMatrixByRow(int row, int col) {
		p_Rows = new LinkedList[row];
		for (int i = 0; i < row; i++) {
			p_Rows[i] = new LinkedList<KeyValuePair>();
		}
		m_NumRow = row;
		m_NumColumn = col;
	}

	public LinkedList<KeyValuePair> GetRow(int rowid) throws Exception {
		if (rowid >= m_NumRow || p_Rows == null || p_Rows[rowid] == null) {
			throw new Exception("rowid out of bound");
		}
		return p_Rows[rowid];
	}

	public boolean SetRow(int rowid, LinkedList<KeyValuePair> lstRow) {
		if (rowid >= m_NumRow) {
			return false;
		}
		p_Rows[rowid] = lstRow;
		return true;
	}

	public boolean AddValue(int rowid, int colid, float value) {
		if (rowid >= m_NumRow || colid >= m_NumColumn)
			return false;

		p_Rows[rowid].add(new KeyValuePair(colid, value));
		return true;
	}

	public void Zero() {
		for (int i = 0; i < m_NumRow; i++) {
			p_Rows[i].clear();
		}
	}

	public void ReleaseMemory() {
		p_Rows = null;
		m_NumRow = 0;
		m_NumColumn = 0;

	}
}
