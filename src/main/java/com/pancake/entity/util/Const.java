package com.pancake.entity.util;

/**
 * Created by chao on 2017/11/9.
 */
public class Const {
    public static String BlockChainConfigFile =
            Const.class.getClassLoader().getResource("blockchain-config-test.json").getPath();

    public static String PvtKeyFile = "C:\\Users\\chao\\medical_chain\\privateKey.txt";
    public static String PubKeyFile = "C:\\Users\\chao\\medical_chain\\publicKey.txt";

    public final static String CM = "ClientMsg";
    public final static String PPM = "PrePrepareMsg";
    public final static String PM = "PrepareMsg";
    public final static String PDM = "PreparedMsg";
    public final static String CMTM = "CommitMsg";
    public final static String CMTDM = "CommittedMsg";
    public final static String BM = "BlockMsg";
    public final static String TXM = "TransactionMsg";
    public final static String LBIM = "LastBlockIdMsg";
    public final static String TIM = "TxIdMsg";
    public final static String CMTM_COUNT = "CommitMsgCount";

    public final static String TX_ID_QUEUE = "TxIdQueue";
    public final static String LAST_BLOCK_ID_QUEUE = "LastBlockIdMsgQueue";
    public final static String TX_QUEUE = "TxQueue";
    public final static String VERIFIED_TX_QUEUE = "VerifiedTxQueue";

    public final static String CHAR_SET = "UTF-8";
    public final static String HASH_ALG = "SHA-256";
    public final static String BLOCK_CHAIN = "BlockChain";
    public final static String BLOCK = "Block";
    public final static String TX = "Transaction";
    public final static String TX_ID = "TransactionId";

    public final static String PRE_BLOCK_ID = "PreBlockId";
    public final static String PRE_BLOCK_ID_COLLECTION = "PreBlockIdCollection";
    public final static String LAST_BLOCK_ID = "LastBlockId";
    public final static String LAST_BLOCK_ID_MSG = "LastBlockIdMsg";
    public final static String SIMPLE_BLOCK = "SimpleBlock";

    public final static long SLEEP_TIME = 10000;

    public final static String DESC = "descending";
    public final static String ASC = "ascending";

    public final static String GENESIS_BLOCK_ID = "00000000000000000000000000000000000000000000";
    public final static String GENESIS_TX_ID = "00000000000000000000000000000000000000000000";

    public final static double TX_ID_LIST_SIZE = 20 / 1024.0; // 单位 MB
    public final static int TIME_OUT = 10000;
    public final static int CONN_TIME_OUT = 4000; // 4 秒
}
