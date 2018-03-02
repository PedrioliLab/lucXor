package lucxor;

/**
 * Created by dfermin on 5/15/14.
 */
public class ModelParameterWorkerThread implements Runnable {

    private PSM curPSM; // the PSM modeling data will be acquired from
    private int jobIdx; // index of this PSM in Globals.PSM_list

    // Default constructor for this class
    public ModelParameterWorkerThread(PSM externalPSM, int i) {
        this.curPSM = externalPSM;
        this.jobIdx = i;
    }

    @Override
    public void run() {

        try {
            synchronized (this) {
                curPSM.generatePermutations(0); // generate real permutations
                curPSM.matchAllPeaks();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
