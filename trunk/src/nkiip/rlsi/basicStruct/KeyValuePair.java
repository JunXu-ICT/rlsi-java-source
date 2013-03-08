/****************************************************************************************
 *Project: Regularized Latent Semantic Indexing 1.0
Reference: Quan Wang, Jun Xu, Hang Li, and Nick Craswell. Regularized latent semantic indexing. SIGIR 2011: 685-694. 
 ****************************************************************************************
 *Laboratory of Intelligent Information Processing, Nankai University
 *Authors: Zhicheng He, Yingjie Xu, Jun Xu, MaoQiang xie, Yalou Huang
 *****************************************************************************************/

package nkiip.rlsi.basicStruct;
public class KeyValuePair {

	public Integer key;
	public Float value;

	public KeyValuePair(Integer k, Float v) {
		key = k;
		value = v;
	};
}
