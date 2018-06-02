import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by shreyansh on 29/5/18.
 */
public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    public long timestamp; //as the number of milliseconds since 1/1/1970
    public int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        timestamp = new Date().getTime();
        nonce = 0;
        hash = calculateHash();
    }

    public String calculateHash() {
        String calculatedHash = StringUtil.applySha256(
                previousHash+
                        Long.toString(timestamp)+
                        Integer.toString(nonce)+
                        merkleRoot
        );
        return calculatedHash;
    }

    public void mine(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);
        while (!hash.startsWith(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block mined!!! : " + hash);
    }

    public boolean addTransaction(Transaction transaction) {
        if(transaction==null)
            return false;
        if(!previousHash.equals("0")) {
            if(! transaction.processTransaction()) {
                System.out.println("#Transaction failed to process. Discarded");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added");
        return true;
    }
}
