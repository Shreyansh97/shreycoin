import java.util.Arrays;
import java.util.Date;

/**
 * Created by shreyansh on 29/5/18.
 */
public class Block {
    public String hash;
    public String previousHash;
    private String data;
    private long timestamp; //as the number of milliseconds since 1/1/1970
    private int nonce;

    public Block(String data, String previousHash) {
        this.data = data;
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
                        data
        );
        return calculatedHash;
    }

    public void mine(int difficulty) {
        char[] temp = new char[difficulty];
        Arrays.fill(temp,'0');
        String target = new String(temp);
        while (!hash.substring(0,difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined : "+hash);
    }
}
