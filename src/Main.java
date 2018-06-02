import com.google.gson.GsonBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shreyansh on 29/5/18.
 */
public class Main {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    public static final int difficulty = 5;
    public static Wallet walletA;
    public static Wallet walletB;

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        walletA = new Wallet();
        walletB = new Wallet();
        System.out.println("Private and Public keys:");
        System.out.println(StringUtil.getStringFromKey(walletA.privateKey));
        System.out.println(StringUtil.getStringFromKey(walletA.publicKey));

        Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
        transaction.generateSignature(walletA.privateKey);

        System.out.println("Is signature verified");
        System.out.println(transaction.verifySignature());
    }

    public static boolean isChainValid() {
        Block previous = null;

        for (Block current:blockchain) {
            if(!current.hash.equals(current.calculateHash())) {
                System.out.println("Current hashes not equal");
                return false;
            }
            if(previous != null && !previous.hash.equals(current.previousHash)) {
                System.out.println("Previous hashes are not equal");
                return false;
            }
            previous = current;
        }

        return true;
    }
}
